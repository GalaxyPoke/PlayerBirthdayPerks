package com.birthdayperks.database;

import com.birthdayperks.PlayerBirthdayPerks;
import com.birthdayperks.model.PlayerData;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public abstract class AbstractDatabase implements Database {

    protected final PlayerBirthdayPerks plugin;
    protected HikariDataSource dataSource;

    protected static final String TABLE_NAME = "birthday_players";

    public AbstractDatabase(PlayerBirthdayPerks plugin) {
        this.plugin = plugin;
    }

    protected abstract HikariDataSource createDataSource();

    @Override
    public void initialize() throws Exception {
        this.dataSource = createDataSource();
        createTables();
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    protected void createTables() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "player_name VARCHAR(16), " +
                "birth_year INT, " +
                "birthday_month INT, " +
                "birthday_day INT, " +
                "last_claim_year INT DEFAULT 0, " +
                "last_claim_date DATE, " +
                "modify_count_this_year INT DEFAULT 0, " +
                "last_modify_year INT DEFAULT 0, " +
                "avatar_frame_expiry DATE, " +
                "created_at DATE, " +
                "updated_at DATE" +
                ")";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }

        // 尝试添加birth_year列（升级旧数据库）
        tryAddBirthYearColumn();

        // 创建索引以优化生日查询
        createIndexIfNotExists();
    }

    protected void tryAddBirthYearColumn() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE " + TABLE_NAME + " ADD COLUMN birth_year INT");
        } catch (SQLException e) {
            // 列已存在，忽略错误
        }
    }

    protected abstract void createIndexIfNotExists() throws SQLException;

    @Override
    public CompletableFuture<Void> savePlayerData(PlayerData data) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT OR REPLACE INTO " + TABLE_NAME + " " +
                    "(uuid, player_name, birth_year, birthday_month, birthday_day, last_claim_year, " +
                    "last_claim_date, modify_count_this_year, last_modify_year, " +
                    "avatar_frame_expiry, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            // MySQL使用不同的语法
            if (getDatabaseType().equals("MySQL")) {
                sql = "INSERT INTO " + TABLE_NAME + " " +
                        "(uuid, player_name, birth_year, birthday_month, birthday_day, last_claim_year, " +
                        "last_claim_date, modify_count_this_year, last_modify_year, " +
                        "avatar_frame_expiry, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "player_name = VALUES(player_name), " +
                        "birth_year = VALUES(birth_year), " +
                        "birthday_month = VALUES(birthday_month), " +
                        "birthday_day = VALUES(birthday_day), " +
                        "last_claim_year = VALUES(last_claim_year), " +
                        "last_claim_date = VALUES(last_claim_date), " +
                        "modify_count_this_year = VALUES(modify_count_this_year), " +
                        "last_modify_year = VALUES(last_modify_year), " +
                        "avatar_frame_expiry = VALUES(avatar_frame_expiry), " +
                        "updated_at = VALUES(updated_at)";
            }

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, data.getUuid().toString());
                stmt.setString(2, data.getPlayerName());

                if (data.getBirthDate() != null) {
                    stmt.setInt(3, data.getBirthDate().getYear());
                    stmt.setInt(4, data.getBirthDate().getMonthValue());
                    stmt.setInt(5, data.getBirthDate().getDayOfMonth());
                } else {
                    stmt.setNull(3, Types.INTEGER);
                    stmt.setNull(4, Types.INTEGER);
                    stmt.setNull(5, Types.INTEGER);
                }

                stmt.setInt(6, data.getLastClaimYear());
                stmt.setDate(7, data.getLastClaimDate() != null ? 
                        Date.valueOf(data.getLastClaimDate()) : null);
                stmt.setInt(8, data.getModifyCountThisYear());
                stmt.setInt(9, data.getLastModifyYear());
                stmt.setDate(10, data.getAvatarFrameExpiry() != null ? 
                        Date.valueOf(data.getAvatarFrameExpiry()) : null);
                stmt.setDate(11, data.getCreatedAt() != null ? 
                        Date.valueOf(data.getCreatedAt()) : Date.valueOf(LocalDate.now()));
                stmt.setDate(12, Date.valueOf(LocalDate.now()));

                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.log(Level.SEVERE, "保存玩家数据失败: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<PlayerData> getPlayerData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM " + TABLE_NAME + " WHERE uuid = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, uuid.toString());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return parsePlayerData(rs);
                    }
                }
            } catch (SQLException e) {
                plugin.log(Level.SEVERE, "获取玩家数据失败: " + e.getMessage());
                throw new RuntimeException(e);
            }

            return null;
        });
    }

    @Override
    public CompletableFuture<Void> deletePlayerData(UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM " + TABLE_NAME + " WHERE uuid = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.log(Level.SEVERE, "删除玩家数据失败: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<List<PlayerData>> getTodayBirthdayPlayers() {
        MonthDay today = MonthDay.now();
        return getBirthdayPlayers(today.getMonthValue(), today.getDayOfMonth());
    }

    @Override
    public CompletableFuture<List<PlayerData>> getBirthdayPlayers(int month, int day) {
        return CompletableFuture.supplyAsync(() -> {
            List<PlayerData> players = new ArrayList<>();
            String sql = "SELECT * FROM " + TABLE_NAME + 
                    " WHERE birthday_month = ? AND birthday_day = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, month);
                stmt.setInt(2, day);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        players.add(parsePlayerData(rs));
                    }
                }
            } catch (SQLException e) {
                plugin.log(Level.SEVERE, "获取生日玩家列表失败: " + e.getMessage());
                throw new RuntimeException(e);
            }

            return players;
        });
    }

    @Override
    public CompletableFuture<Boolean> playerDataExists(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT 1 FROM " + TABLE_NAME + " WHERE uuid = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, uuid.toString());

                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            } catch (SQLException e) {
                plugin.log(Level.SEVERE, "检查玩家数据失败: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<List<PlayerData>> getUpcomingBirthdays(int days) {
        return CompletableFuture.supplyAsync(() -> {
            List<PlayerData> players = new ArrayList<>();
            String sql = "SELECT * FROM " + TABLE_NAME + " WHERE birthday_month IS NOT NULL AND birthday_day IS NOT NULL";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                LocalDate today = LocalDate.now();
                while (rs.next()) {
                    PlayerData data = parsePlayerData(rs);
                    if (data.hasBirthdaySet()) {
                        long daysUntil = data.getDaysUntilBirthday();
                        if (daysUntil >= 0 && daysUntil <= days) {
                            players.add(data);
                        }
                    }
                }
            } catch (SQLException e) {
                plugin.log(Level.SEVERE, "获取即将过生日玩家列表失败: " + e.getMessage());
                throw new RuntimeException(e);
            }

            // 按距离生日天数排序
            players.sort((a, b) -> Long.compare(a.getDaysUntilBirthday(), b.getDaysUntilBirthday()));
            return players;
        });
    }

    @Override
    public CompletableFuture<List<PlayerData>> getAllPlayersWithBirthday() {
        return CompletableFuture.supplyAsync(() -> {
            List<PlayerData> players = new ArrayList<>();
            String sql = "SELECT * FROM " + TABLE_NAME + " WHERE birthday_month IS NOT NULL AND birthday_day IS NOT NULL";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    players.add(parsePlayerData(rs));
                }
            } catch (SQLException e) {
                plugin.log(Level.SEVERE, "获取所有玩家生日数据失败: " + e.getMessage());
                throw new RuntimeException(e);
            }

            return players;
        });
    }

    protected PlayerData parsePlayerData(ResultSet rs) throws SQLException {
        UUID uuid = UUID.fromString(rs.getString("uuid"));
        String playerName = rs.getString("player_name");

        LocalDate birthDate = null;
        int year = rs.getInt("birth_year");
        boolean hasYear = !rs.wasNull() && year > 0;
        int month = rs.getInt("birthday_month");
        int day = rs.getInt("birthday_day");
        if (!rs.wasNull() && month > 0 && day > 0) {
            // 如果没有年份，默认使用2000年
            int birthYear = hasYear ? year : 2000;
            birthDate = LocalDate.of(birthYear, month, day);
        }

        int lastClaimYear = rs.getInt("last_claim_year");
        
        LocalDate lastClaimDate = null;
        Date lastClaimDateSql = rs.getDate("last_claim_date");
        if (lastClaimDateSql != null) {
            lastClaimDate = lastClaimDateSql.toLocalDate();
        }

        int modifyCountThisYear = rs.getInt("modify_count_this_year");
        int lastModifyYear = rs.getInt("last_modify_year");

        LocalDate avatarFrameExpiry = null;
        Date avatarExpiryDateSql = rs.getDate("avatar_frame_expiry");
        if (avatarExpiryDateSql != null) {
            avatarFrameExpiry = avatarExpiryDateSql.toLocalDate();
        }

        LocalDate createdAt = null;
        Date createdAtSql = rs.getDate("created_at");
        if (createdAtSql != null) {
            createdAt = createdAtSql.toLocalDate();
        }

        LocalDate updatedAt = null;
        Date updatedAtSql = rs.getDate("updated_at");
        if (updatedAtSql != null) {
            updatedAt = updatedAtSql.toLocalDate();
        }

        return new PlayerData(uuid, playerName, birthDate, lastClaimYear, lastClaimDate,
                modifyCountThisYear, lastModifyYear, avatarFrameExpiry, createdAt, updatedAt);
    }
}
