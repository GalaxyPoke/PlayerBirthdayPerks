package com.birthdayperks.database;

import com.birthdayperks.PlayerBirthdayPerks;
import com.birthdayperks.manager.ConfigManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLDatabase extends AbstractDatabase {

    public MySQLDatabase(PlayerBirthdayPerks plugin) {
        super(plugin);
    }

    @Override
    protected HikariDataSource createDataSource() {
        ConfigManager config = plugin.getConfigManager();

        String host = config.getMySQLHost();
        int port = config.getMySQLPort();
        String database = config.getMySQLDatabase();
        String username = config.getMySQLUsername();
        String password = config.getMySQLPassword();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + 
                "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8");
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        // 连接池配置
        hikariConfig.setMaximumPoolSize(config.getMySQLMaxPoolSize());
        hikariConfig.setMinimumIdle(config.getMySQLMinIdle());
        hikariConfig.setConnectionTimeout(config.getMySQLConnectionTimeout());
        hikariConfig.setIdleTimeout(config.getMySQLIdleTimeout());
        hikariConfig.setMaxLifetime(config.getMySQLMaxLifetime());
        hikariConfig.setPoolName("BirthdayPerks-MySQL");

        // MySQL优化配置
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
        hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
        hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
        hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
        hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
        hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
        hikariConfig.addDataSourceProperty("maintainTimeStats", "false");

        return new HikariDataSource(hikariConfig);
    }

    @Override
    protected void createTables() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "player_name VARCHAR(16), " +
                "birthday_month TINYINT, " +
                "birthday_day TINYINT, " +
                "last_claim_year SMALLINT DEFAULT 0, " +
                "last_claim_date DATE, " +
                "modify_count_this_year TINYINT DEFAULT 0, " +
                "last_modify_year SMALLINT DEFAULT 0, " +
                "avatar_frame_expiry DATE, " +
                "created_at DATE, " +
                "updated_at DATE, " +
                "INDEX idx_birthday (birthday_month, birthday_day)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    @Override
    protected void createIndexIfNotExists() throws SQLException {
        // MySQL在创建表时已经创建了索引，这里检查并添加（如果不存在）
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getIndexInfo(null, null, TABLE_NAME, false, false)) {
                boolean hasIndex = false;
                while (rs.next()) {
                    String indexName = rs.getString("INDEX_NAME");
                    if ("idx_birthday".equals(indexName)) {
                        hasIndex = true;
                        break;
                    }
                }
                
                if (!hasIndex) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute("CREATE INDEX idx_birthday ON " + TABLE_NAME + 
                                " (birthday_month, birthday_day)");
                    }
                }
            }
        }
    }

    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
