package com.birthdayperks.manager;

import com.birthdayperks.PlayerBirthdayPerks;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final PlayerBirthdayPerks plugin;
    private FileConfiguration config;

    public ConfigManager(PlayerBirthdayPerks plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    // 数据库配置
    public String getDatabaseType() {
        return config.getString("database.type", "sqlite");
    }

    // SQLite配置
    public String getSQLiteFile() {
        return config.getString("database.sqlite.file", "data.db");
    }

    // MySQL配置
    public String getMySQLHost() {
        return config.getString("database.mysql.host", "localhost");
    }

    public int getMySQLPort() {
        return config.getInt("database.mysql.port", 3306);
    }

    public String getMySQLDatabase() {
        return config.getString("database.mysql.database", "birthday_perks");
    }

    public String getMySQLUsername() {
        return config.getString("database.mysql.username", "root");
    }

    public String getMySQLPassword() {
        return config.getString("database.mysql.password", "password");
    }

    public int getMySQLMaxPoolSize() {
        return config.getInt("database.mysql.pool.maximum-pool-size", 10);
    }

    public int getMySQLMinIdle() {
        return config.getInt("database.mysql.pool.minimum-idle", 2);
    }

    public long getMySQLConnectionTimeout() {
        return config.getLong("database.mysql.pool.connection-timeout", 30000);
    }

    public long getMySQLIdleTimeout() {
        return config.getLong("database.mysql.pool.idle-timeout", 600000);
    }

    public long getMySQLMaxLifetime() {
        return config.getLong("database.mysql.pool.max-lifetime", 1800000);
    }

    // 生日配置
    public boolean isAllowModify() {
        return config.getBoolean("birthday.allow-modify", false);
    }

    public int getModifyLimitPerYear() {
        return config.getInt("birthday.modify-limit-per-year", 1);
    }

    public int getClaimWindowDays() {
        return config.getInt("birthday.claim-window-days", 7);
    }

    // 福利配置
    public boolean isRewardsEnabled() {
        return config.getBoolean("rewards.enabled", true);
    }

    public boolean isLoginNotificationEnabled() {
        return config.getBoolean("rewards.login-notification", true);
    }

    public boolean isBroadcastEnabled() {
        return config.getBoolean("rewards.broadcast.enabled", true);
    }

    public String getBroadcastMessage() {
        return config.getString("rewards.broadcast.message", 
                "&6&l🎂 今天是 &e%player% &6&l的生日！让我们一起祝TA生日快乐！🎉");
    }

    public boolean isFireworkEnabled() {
        return config.getBoolean("rewards.firework.enabled", true);
    }

    public int getFireworkAmount() {
        return config.getInt("rewards.firework.amount", 3);
    }

    public long getFireworkDelayTicks() {
        return config.getLong("rewards.firework.delay-ticks", 20);
    }

    public boolean isSoundEnabled() {
        return config.getBoolean("rewards.sound.enabled", true);
    }

    public String getSoundType() {
        return config.getString("rewards.sound.type", "UI_TOAST_CHALLENGE_COMPLETE");
    }

    public float getSoundVolume() {
        return (float) config.getDouble("rewards.sound.volume", 1.0);
    }

    public float getSoundPitch() {
        return (float) config.getDouble("rewards.sound.pitch", 1.0);
    }

    public boolean isExperienceEnabled() {
        return config.getBoolean("rewards.experience.enabled", true);
    }

    public int getExperienceAmount() {
        return config.getInt("rewards.experience.amount", 500);
    }

    public boolean isMoneyEnabled() {
        return config.getBoolean("rewards.money.enabled", false);
    }

    public double getMoneyAmount() {
        return config.getDouble("rewards.money.amount", 1000.0);
    }

    // 头像框配置
    public boolean isAvatarFrameEnabled() {
        return config.getBoolean("avatar-frame.enabled", true);
    }

    public int getAvatarFrameDurationDays() {
        return config.getInt("avatar-frame.duration-days", 30);
    }

    public String getAvatarFramePrefix() {
        return config.getString("avatar-frame.prefix", "&6🎂&r ");
    }

    // 缓存配置
    public int getCacheExpireMinutes() {
        return config.getInt("cache.expire-minutes", 30);
    }

    public int getCacheMaxSize() {
        return config.getInt("cache.max-size", 1000);
    }

    // 调试模式
    public boolean isDebugEnabled() {
        return config.getBoolean("debug", false);
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
