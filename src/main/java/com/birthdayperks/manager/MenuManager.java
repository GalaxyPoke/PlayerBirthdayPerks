package com.birthdayperks.manager;

import com.birthdayperks.PlayerBirthdayPerks;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class MenuManager {

    private final PlayerBirthdayPerks plugin;
    private final Map<String, FileConfiguration> menuConfigs;
    private File menuFolder;

    public MenuManager(PlayerBirthdayPerks plugin) {
        this.plugin = plugin;
        this.menuConfigs = new HashMap<>();
    }

    public void loadMenus() {
        menuConfigs.clear();
        
        // 创建menu文件夹
        menuFolder = new File(plugin.getDataFolder(), "menu");
        if (!menuFolder.exists()) {
            menuFolder.mkdirs();
        }

        // 保存默认菜单配置文件
        saveDefaultMenu("main-menu.yml");
        saveDefaultMenu("reward-preview.yml");
        saveDefaultMenu("set-birthday.yml");
        saveDefaultMenu("birthday-info.yml");
        saveDefaultMenu("help.yml");

        // 加载所有菜单配置
        File[] files = menuFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                String menuName = file.getName().replace(".yml", "");
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                
                // 合并默认配置
                InputStream defaultStream = plugin.getResource("menu/" + file.getName());
                if (defaultStream != null) {
                    YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                            new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
                    config.setDefaults(defaultConfig);
                }
                
                menuConfigs.put(menuName, config);
                plugin.debug("已加载菜单配置: " + menuName);
            }
        }

        plugin.log(Level.INFO, "已加载 " + menuConfigs.size() + " 个菜单配置");
    }

    private void saveDefaultMenu(String fileName) {
        File file = new File(menuFolder, fileName);
        if (!file.exists()) {
            InputStream inputStream = plugin.getResource("menu/" + fileName);
            if (inputStream != null) {
                try {
                    org.bukkit.configuration.file.YamlConfiguration config = 
                            YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                    config.save(file);
                    plugin.debug("已保存默认菜单配置: " + fileName);
                } catch (IOException e) {
                    plugin.log(Level.WARNING, "无法保存默认菜单配置: " + fileName);
                }
            }
        }
    }

    public FileConfiguration getMenuConfig(String menuName) {
        return menuConfigs.get(menuName);
    }

    public boolean hasMenu(String menuName) {
        return menuConfigs.containsKey(menuName);
    }

    public Map<String, FileConfiguration> getAllMenus() {
        return new HashMap<>(menuConfigs);
    }

    public void reload() {
        loadMenus();
    }

    public File getMenuFolder() {
        return menuFolder;
    }
}
