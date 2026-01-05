package com.birthdayperks.manager;

import com.birthdayperks.PlayerBirthdayPerks;
import com.birthdayperks.model.PlayerData;
import com.birthdayperks.util.ColorUtil;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class RewardManager {

    private final PlayerBirthdayPerks plugin;
    private final ConfigManager configManager;
    private final Random random = new Random();
    private FileConfiguration rewardsConfig;

    public RewardManager(PlayerBirthdayPerks plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        loadRewardsConfig();
    }

    public void loadRewardsConfig() {
        File rewardsFile = new File(plugin.getDataFolder(), "rewards.yml");
        
        // 保存默认配置
        if (!rewardsFile.exists()) {
            plugin.saveResource("rewards.yml", false);
        }
        
        // 加载配置
        rewardsConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(rewardsFile);
        
        // 合并默认配置
        InputStream defaultStream = plugin.getResource("rewards.yml");
        if (defaultStream != null) {
            org.bukkit.configuration.file.YamlConfiguration defaultConfig = 
                    org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(
                            new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            rewardsConfig.setDefaults(defaultConfig);
        }
        
        plugin.debug("已加载奖励配置文件");
    }

    public void reload() {
        loadRewardsConfig();
    }

    public FileConfiguration getRewardsConfig() {
        return rewardsConfig;
    }

    public CompletableFuture<Boolean> giveRewards(Player player, PlayerData playerData) {
        if (!configManager.isRewardsEnabled()) {
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                // 在主线程执行奖励发放
                Bukkit.getScheduler().runTask(plugin, () -> {
                    // 执行命令奖励（包含物品发放）
                    executeCommandRewards(player);

                    // 发放经验奖励
                    giveExperienceReward(player);

                    // 播放音效
                    playSoundEffect(player);

                    // 发射烟花
                    launchFireworks(player);

                    // 全服广播
                    broadcastBirthday(player);

                    // 发放头像框
                    giveAvatarFrame(player, playerData);
                });

                return true;
            } catch (Exception e) {
                plugin.getLogger().severe("发放生日福利时出错: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }

    private void giveItemRewards(Player player) {
        // 优先从 menu/rewards.yml 加载物品配置
        FileConfiguration rewardsConfig = plugin.getMenuManager().getMenuConfig("rewards");
        List<Map<?, ?>> items;
        
        if (rewardsConfig != null && rewardsConfig.getConfigurationSection("items") != null) {
            // 从 rewards.yml 的 items section 加载
            items = new ArrayList<>();
            ConfigurationSection itemsSection = rewardsConfig.getConfigurationSection("items");
            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                if (itemSection != null) {
                    java.util.HashMap<String, Object> itemMap = new java.util.HashMap<>();
                    itemMap.put("material", itemSection.getString("material"));
                    itemMap.put("amount", itemSection.getInt("amount", 1));
                    if (itemSection.contains("name")) itemMap.put("name", itemSection.getString("name"));
                    if (itemSection.contains("lore")) itemMap.put("lore", itemSection.getStringList("lore"));
                    items.add(itemMap);
                }
            }
        } else {
            // 回退到 config.yml
            items = plugin.getConfig().getMapList("rewards.items");
        }
        
        for (Map<?, ?> itemConfig : items) {
            try {
                String materialName = (String) itemConfig.get("material");
                Material material = Material.valueOf(materialName.toUpperCase());
                int amount = itemConfig.containsKey("amount") ? ((Number) itemConfig.get("amount")).intValue() : 1;

                ItemStack item = new ItemStack(material, amount);
                ItemMeta meta = item.getItemMeta();

                if (meta != null) {
                    // 设置名称
                    if (itemConfig.containsKey("name")) {
                        String name = ColorUtil.colorize((String) itemConfig.get("name"));
                        meta.setDisplayName(name);
                    }

                    // 设置Lore
                    if (itemConfig.containsKey("lore")) {
                        @SuppressWarnings("unchecked")
                        List<String> loreList = (List<String>) itemConfig.get("lore");
                        List<String> coloredLore = new ArrayList<>();
                        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        
                        for (String line : loreList) {
                            String coloredLine = ColorUtil.colorize(line);
                            coloredLine = coloredLine.replace("%date%", dateStr);
                            coloredLine = coloredLine.replace("%player%", player.getName());
                            coloredLore.add(coloredLine);
                        }
                        meta.setLore(coloredLore);
                    }

                    item.setItemMeta(meta);
                }

                // 给予物品，如果背包满则掉落
                if (player.getInventory().firstEmpty() == -1) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                } else {
                    player.getInventory().addItem(item);
                }

                plugin.debug("给予玩家 " + player.getName() + " 物品: " + materialName + " x" + amount);
            } catch (Exception e) {
                plugin.getLogger().warning("给予物品奖励失败: " + e.getMessage());
            }
        }
    }

    private void executeCommandRewards(Player player) {
        // 从 rewards.yml 加载命令配置
        if (rewardsConfig != null && rewardsConfig.getConfigurationSection("commands") != null) {
            ConfigurationSection commandsSection = rewardsConfig.getConfigurationSection("commands");
            for (String key : commandsSection.getKeys(false)) {
                ConfigurationSection cmdSection = commandsSection.getConfigurationSection(key);
                if (cmdSection != null && cmdSection.getBoolean("enabled", true)) {
                    try {
                        String command = cmdSection.getString("command", "");
                        int delay = cmdSection.getInt("delay", 0);
                        String parsedCommand = command
                                .replace("%player%", player.getName())
                                .replace("%uuid%", player.getUniqueId().toString())
                                .replace("%year%", String.valueOf(LocalDate.now().getYear()));
                        
                        if (delay > 0) {
                            Bukkit.getScheduler().runTaskLater(plugin, () -> 
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand), delay);
                        } else {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
                        }
                        plugin.debug("执行命令: " + parsedCommand);
                    } catch (Exception e) {
                        plugin.getLogger().warning("执行命令奖励失败: " + e.getMessage());
                    }
                }
            }
        } else {
            // 回退到 config.yml
            List<String> commands = plugin.getConfig().getStringList("rewards.commands");
            for (String command : commands) {
                try {
                    String parsedCommand = command.replace("%player%", player.getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
                    plugin.debug("执行命令: " + parsedCommand);
                } catch (Exception e) {
                    plugin.getLogger().warning("执行命令奖励失败: " + e.getMessage());
                }
            }
        }
    }

    private void giveExperienceReward(Player player) {
        // 从 rewards.yml 加载经验配置
        boolean enabled;
        int amount;
        
        if (rewardsConfig != null && rewardsConfig.contains("experience")) {
            enabled = rewardsConfig.getBoolean("experience.enabled", true);
            amount = rewardsConfig.getInt("experience.amount", 500);
        } else {
            enabled = configManager.isExperienceEnabled();
            amount = configManager.getExperienceAmount();
        }
        
        if (!enabled) return;
        player.giveExp(amount);
        plugin.debug("给予玩家 " + player.getName() + " 经验: " + amount);
    }

    private void playSoundEffect(Player player) {
        boolean enabled;
        String soundName;
        float volume, pitch;
        
        if (rewardsConfig != null && rewardsConfig.contains("sound")) {
            enabled = rewardsConfig.getBoolean("sound.enabled", true);
            soundName = rewardsConfig.getString("sound.type", "UI_TOAST_CHALLENGE_COMPLETE");
            volume = (float) rewardsConfig.getDouble("sound.volume", 1.0);
            pitch = (float) rewardsConfig.getDouble("sound.pitch", 1.0);
        } else {
            enabled = configManager.isSoundEnabled();
            soundName = configManager.getSoundType();
            volume = configManager.getSoundVolume();
            pitch = configManager.getSoundPitch();
        }
        
        if (!enabled) return;

        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            player.playSound(player.getLocation(), sound, volume, pitch);
            plugin.debug("播放音效: " + soundName);
        } catch (Exception e) {
            plugin.getLogger().warning("播放音效失败: " + e.getMessage());
        }
    }

    private void launchFireworks(Player player) {
        boolean enabled;
        int amount;
        long delay;
        boolean trail, flicker;
        List<Color> customColors = new ArrayList<>();
        
        if (rewardsConfig != null && rewardsConfig.contains("firework")) {
            enabled = rewardsConfig.getBoolean("firework.enabled", true);
            amount = rewardsConfig.getInt("firework.amount", 3);
            delay = rewardsConfig.getLong("firework.delay-ticks", 20);
            trail = rewardsConfig.getBoolean("firework.trail", true);
            flicker = rewardsConfig.getBoolean("firework.flicker", true);
            // 解析自定义颜色
            List<String> colorList = rewardsConfig.getStringList("firework.colors");
            for (String colorStr : colorList) {
                try {
                    String[] rgb = colorStr.split(",");
                    if (rgb.length == 3) {
                        customColors.add(Color.fromRGB(
                                Integer.parseInt(rgb[0].trim()),
                                Integer.parseInt(rgb[1].trim()),
                                Integer.parseInt(rgb[2].trim())));
                    }
                } catch (Exception ignored) {}
            }
        } else {
            enabled = configManager.isFireworkEnabled();
            amount = configManager.getFireworkAmount();
            delay = configManager.getFireworkDelayTicks();
            trail = true;
            flicker = true;
        }
        
        if (!enabled) return;
        
        final boolean finalTrail = trail;
        final boolean finalFlicker = flicker;
        final List<Color> finalColors = customColors;

        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= amount || !player.isOnline()) {
                    cancel();
                    return;
                }

                Location loc = player.getLocation().add(
                        random.nextDouble() * 4 - 2,
                        0,
                        random.nextDouble() * 4 - 2
                );

                Firework firework = player.getWorld().spawn(loc, Firework.class);
                FireworkMeta meta = firework.getFireworkMeta();

                Color color = finalColors.isEmpty() ? getRandomColor() : finalColors.get(random.nextInt(finalColors.size()));
                FireworkEffect effect = FireworkEffect.builder()
                        .withColor(color)
                        .withFade(getRandomColor())
                        .with(FireworkEffect.Type.values()[random.nextInt(FireworkEffect.Type.values().length)])
                        .trail(finalTrail)
                        .flicker(finalFlicker)
                        .build();

                meta.addEffect(effect);
                meta.setPower(1);
                firework.setFireworkMeta(meta);

                count++;
            }
        }.runTaskTimer(plugin, 0, delay);
    }

    private Color getRandomColor() {
        Color[] colors = {
                Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW,
                Color.ORANGE, Color.PURPLE, Color.AQUA, Color.WHITE,
                Color.FUCHSIA, Color.LIME, Color.MAROON, Color.NAVY
        };
        return colors[random.nextInt(colors.length)];
    }

    private void broadcastBirthday(Player player) {
        boolean enabled;
        String message;
        
        if (rewardsConfig != null && rewardsConfig.contains("broadcast")) {
            enabled = rewardsConfig.getBoolean("broadcast.enabled", true);
            message = rewardsConfig.getString("broadcast.message", 
                    "&6&l🎂 今天是 &e%player% &6&l的生日！让我们一起祝TA生日快乐！🎉");
            
            // 发送标题
            if (rewardsConfig.getBoolean("broadcast.title.enabled", false)) {
                String mainTitle = rewardsConfig.getString("broadcast.title.main", "&6&l🎂 生日快乐！");
                String subTitle = rewardsConfig.getString("broadcast.title.sub", "&e祝 %player% 生日快乐！");
                int fadeIn = rewardsConfig.getInt("broadcast.title.fade-in", 10);
                int stay = rewardsConfig.getInt("broadcast.title.stay", 70);
                int fadeOut = rewardsConfig.getInt("broadcast.title.fade-out", 20);
                
                mainTitle = ColorUtil.colorize(mainTitle.replace("%player%", player.getName()));
                subTitle = ColorUtil.colorize(subTitle.replace("%player%", player.getName()));
                
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendTitle(mainTitle, subTitle, fadeIn, stay, fadeOut);
                }
            }
        } else {
            enabled = configManager.isBroadcastEnabled();
            message = configManager.getBroadcastMessage();
        }
        
        if (!enabled) return;
        
        message = ColorUtil.colorize(message.replace("%player%", player.getName()));
        Bukkit.broadcastMessage(message);
        plugin.debug("广播生日祝福: " + player.getName());
    }

    private void giveAvatarFrame(Player player, PlayerData playerData) {
        boolean enabled;
        int durationDays;
        
        if (rewardsConfig != null && rewardsConfig.contains("avatar-frame")) {
            enabled = rewardsConfig.getBoolean("avatar-frame.enabled", true);
            durationDays = rewardsConfig.getInt("avatar-frame.duration-days", 30);
        } else {
            enabled = configManager.isAvatarFrameEnabled();
            durationDays = configManager.getAvatarFrameDurationDays();
        }
        
        if (!enabled) return;

        LocalDate expiry;
        if (durationDays < 0) {
            expiry = LocalDate.of(9999, 12, 31); // 永久
        } else {
            expiry = LocalDate.now().plusDays(durationDays);
        }

        playerData.setAvatarFrameExpiry(expiry);

        // 通知玩家
        String daysStr = durationDays < 0 ? "永久" : String.valueOf(durationDays);
        plugin.getMessageManager().send(player, "avatar-frame.granted", Map.of("days", daysStr));

        plugin.debug("给予玩家 " + player.getName() + " 头像框，有效期: " + daysStr);
    }

    public boolean hasEnoughInventorySpace(Player player, int requiredSlots) {
        int emptySlots = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType() == Material.AIR) {
                emptySlots++;
            }
        }
        return emptySlots >= requiredSlots;
    }

    public int getRewardItemCount() {
        // 统计启用的命令数量
        if (rewardsConfig != null && rewardsConfig.getConfigurationSection("commands") != null) {
            int count = 0;
            ConfigurationSection commandsSection = rewardsConfig.getConfigurationSection("commands");
            for (String key : commandsSection.getKeys(false)) {
                ConfigurationSection cmd = commandsSection.getConfigurationSection(key);
                if (cmd != null && cmd.getBoolean("enabled", true)) {
                    count++;
                }
            }
            return count;
        }
        return plugin.getConfig().getStringList("rewards.commands").size();
    }
}
