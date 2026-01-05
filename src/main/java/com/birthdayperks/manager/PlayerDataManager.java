package com.birthdayperks.manager;

import com.birthdayperks.PlayerBirthdayPerks;
import com.birthdayperks.model.PlayerData;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class PlayerDataManager {

    private final PlayerBirthdayPerks plugin;
    private final Map<UUID, CacheEntry> cache;
    private final long cacheExpireMillis;
    private final int maxCacheSize;

    public PlayerDataManager(PlayerBirthdayPerks plugin) {
        this.plugin = plugin;
        this.cache = new ConcurrentHashMap<>();
        this.cacheExpireMillis = TimeUnit.MINUTES.toMillis(plugin.getConfigManager().getCacheExpireMinutes());
        this.maxCacheSize = plugin.getConfigManager().getCacheMaxSize();
    }

    public CompletableFuture<PlayerData> getPlayerData(UUID uuid) {
        // 检查缓存
        CacheEntry cached = cache.get(uuid);
        if (cached != null && !cached.isExpired()) {
            plugin.debug("从缓存获取玩家数据: " + uuid);
            return CompletableFuture.completedFuture(cached.getData());
        }

        // 从数据库获取
        return plugin.getDatabase().getPlayerData(uuid).thenApply(data -> {
            if (data != null) {
                cachePlayerData(uuid, data);
            }
            return data;
        });
    }

    public CompletableFuture<PlayerData> getOrCreatePlayerData(UUID uuid, String playerName) {
        return getPlayerData(uuid).thenCompose(data -> {
            if (data != null) {
                // 更新玩家名称
                if (!playerName.equals(data.getPlayerName())) {
                    data.setPlayerName(playerName);
                    return savePlayerData(data).thenApply(v -> data);
                }
                return CompletableFuture.completedFuture(data);
            }

            // 创建新的玩家数据
            PlayerData newData = new PlayerData(uuid);
            newData.setPlayerName(playerName);
            return savePlayerData(newData).thenApply(v -> newData);
        });
    }

    public CompletableFuture<Void> savePlayerData(PlayerData data) {
        cachePlayerData(data.getUuid(), data);
        return plugin.getDatabase().savePlayerData(data);
    }

    public CompletableFuture<Void> deletePlayerData(UUID uuid) {
        cache.remove(uuid);
        return plugin.getDatabase().deletePlayerData(uuid);
    }

    private void cachePlayerData(UUID uuid, PlayerData data) {
        // 检查缓存大小
        if (cache.size() >= maxCacheSize) {
            cleanupCache();
        }
        cache.put(uuid, new CacheEntry(data));
    }

    private void cleanupCache() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired(now));

        // 如果仍然超过限制，移除最旧的条目
        if (cache.size() >= maxCacheSize) {
            cache.entrySet().stream()
                    .sorted((a, b) -> Long.compare(a.getValue().getTimestamp(), b.getValue().getTimestamp()))
                    .limit(cache.size() - maxCacheSize + 100)
                    .forEach(entry -> cache.remove(entry.getKey()));
        }
    }

    public void invalidateCache(UUID uuid) {
        cache.remove(uuid);
    }

    public void clearCache() {
        cache.clear();
    }

    private class CacheEntry {
        private final PlayerData data;
        private final long timestamp;

        public CacheEntry(PlayerData data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        public PlayerData getData() {
            return data;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public boolean isExpired() {
            return isExpired(System.currentTimeMillis());
        }

        public boolean isExpired(long now) {
            return now - timestamp > cacheExpireMillis;
        }
    }
}
