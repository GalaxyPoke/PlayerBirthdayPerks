package com.birthdayperks.database;

import com.birthdayperks.PlayerBirthdayPerks;

public class DatabaseFactory {

    public static Database createDatabase(PlayerBirthdayPerks plugin) {
        String type = plugin.getConfigManager().getDatabaseType().toLowerCase();

        return switch (type) {
            case "mysql" -> new MySQLDatabase(plugin);
            case "sqlite" -> new SQLiteDatabase(plugin);
            default -> {
                plugin.getLogger().warning("未知的数据库类型: " + type + "，使用默认SQLite");
                yield new SQLiteDatabase(plugin);
            }
        };
    }
}
