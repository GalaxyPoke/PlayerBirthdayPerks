package com.birthdayperks.database;

import com.birthdayperks.PlayerBirthdayPerks;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteDatabase extends AbstractDatabase {

    private final String dbFile;

    public SQLiteDatabase(PlayerBirthdayPerks plugin) {
        super(plugin);
        this.dbFile = plugin.getConfigManager().getSQLiteFile();
    }

    @Override
    protected HikariDataSource createDataSource() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File dbPath = new File(dataFolder, dbFile);

        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + dbPath.getAbsolutePath());
        config.setConnectionTestQuery("SELECT 1");
        config.setMaximumPoolSize(1); // SQLite只支持单连接写入
        config.setMinimumIdle(1);
        config.setIdleTimeout(60000);
        config.setMaxLifetime(60000);
        config.setConnectionTimeout(30000);
        config.setPoolName("BirthdayPerks-SQLite");

        // SQLite优化配置
        config.addDataSourceProperty("journal_mode", "WAL");
        config.addDataSourceProperty("synchronous", "NORMAL");
        config.addDataSourceProperty("cache_size", "10000");
        config.addDataSourceProperty("temp_store", "MEMORY");

        return new HikariDataSource(config);
    }

    @Override
    protected void createIndexIfNotExists() throws SQLException {
        String indexSql = "CREATE INDEX IF NOT EXISTS idx_birthday ON " + 
                TABLE_NAME + " (birthday_month, birthday_day)";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(indexSql);
        }
    }

    @Override
    public String getDatabaseType() {
        return "SQLite";
    }
}
