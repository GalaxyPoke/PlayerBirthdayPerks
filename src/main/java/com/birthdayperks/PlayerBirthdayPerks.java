package com.birthdayperks;

import com.birthdayperks.command.BirthdayCommand;
import com.birthdayperks.command.BirthdayTabCompleter;
import com.birthdayperks.database.Database;
import com.birthdayperks.database.DatabaseFactory;
import com.birthdayperks.gui.GuiManager;
import com.birthdayperks.listener.PlayerJoinListener;
import com.birthdayperks.manager.ConfigManager;
import com.birthdayperks.manager.MenuManager;
import com.birthdayperks.manager.MessageManager;
import com.birthdayperks.manager.PlayerDataManager;
import com.birthdayperks.manager.RewardManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class PlayerBirthdayPerks extends JavaPlugin {

    private static PlayerBirthdayPerks instance;
    
    private ConfigManager configManager;
    private MenuManager menuManager;
    private MessageManager messageManager;
    private Database database;
    private PlayerDataManager playerDataManager;
    private RewardManager rewardManager;
    private GuiManager guiManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // 初始化配置管理器
        this.configManager = new ConfigManager(this);
        this.configManager.loadConfig();
        
        // 初始化菜单管理器
        this.menuManager = new MenuManager(this);
        this.menuManager.loadMenus();
        
        // 初始化消息管理器
        this.messageManager = new MessageManager(this);
        
        // 初始化数据库
        try {
            this.database = DatabaseFactory.createDatabase(this);
            this.database.initialize();
            log(Level.INFO, "数据库连接成功！");
        } catch (Exception e) {
            log(Level.SEVERE, "数据库初始化失败: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // 初始化玩家数据管理器
        this.playerDataManager = new PlayerDataManager(this);
        
        // 初始化奖励管理器
        this.rewardManager = new RewardManager(this);
        
        // 初始化GUI管理器
        this.guiManager = new GuiManager(this);
        
        // 注册命令
        registerCommands();
        
        // 注册事件监听器
        registerListeners();
        
        log(Level.INFO, "PlayerBirthdayPerks v" + getDescription().getVersion() + " 已启用！");
    }

    @Override
    public void onDisable() {
        // 关闭数据库连接
        if (database != null) {
            database.close();
        }
        
        // 清理缓存
        if (playerDataManager != null) {
            playerDataManager.clearCache();
        }
        
        log(Level.INFO, "PlayerBirthdayPerks 已禁用！");
    }

    private void registerCommands() {
        PluginCommand birthdayCmd = getCommand("pbp");
        if (birthdayCmd != null) {
            BirthdayCommand commandExecutor = new BirthdayCommand(this);
            birthdayCmd.setExecutor(commandExecutor);
            birthdayCmd.setTabCompleter(new BirthdayTabCompleter(this));
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
    }

    public void reload() {
        configManager.loadConfig();
        menuManager.reload();
        rewardManager.reload();
        messageManager.reload();
        log(Level.INFO, "配置已重新加载！");
    }

    public void log(Level level, String message) {
        getLogger().log(level, message);
    }

    public void debug(String message) {
        if (configManager.isDebugEnabled()) {
            log(Level.INFO, "[DEBUG] " + message);
        }
    }

    public static PlayerBirthdayPerks getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public Database getDatabase() {
        return database;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }
}
