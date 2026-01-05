package com.birthdayperks.database;

import com.birthdayperks.model.PlayerData;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Database {

    /**
     * 初始化数据库连接和表结构
     */
    void initialize() throws Exception;

    /**
     * 关闭数据库连接
     */
    void close();

    /**
     * 保存玩家数据
     */
    CompletableFuture<Void> savePlayerData(PlayerData data);

    /**
     * 获取玩家数据
     */
    CompletableFuture<PlayerData> getPlayerData(UUID uuid);

    /**
     * 删除玩家数据
     */
    CompletableFuture<Void> deletePlayerData(UUID uuid);

    /**
     * 获取今天过生日的所有玩家
     */
    CompletableFuture<List<PlayerData>> getTodayBirthdayPlayers();

    /**
     * 获取指定日期过生日的玩家
     */
    CompletableFuture<List<PlayerData>> getBirthdayPlayers(int month, int day);

    /**
     * 检查玩家数据是否存在
     */
    CompletableFuture<Boolean> playerDataExists(UUID uuid);

    /**
     * 获取数据库类型名称
     */
    String getDatabaseType();

    /**
     * 获取即将过生日的玩家列表（未来N天内）
     */
    CompletableFuture<List<PlayerData>> getUpcomingBirthdays(int days);

    /**
     * 获取所有已设置生日的玩家数据
     */
    CompletableFuture<List<PlayerData>> getAllPlayersWithBirthday();
}
