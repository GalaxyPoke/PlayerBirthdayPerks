package com.birthdayperks.listener;

import com.birthdayperks.PlayerBirthdayPerks;
import com.birthdayperks.manager.ConfigManager;
import com.birthdayperks.manager.MessageManager;
import com.birthdayperks.manager.PlayerDataManager;
import com.birthdayperks.model.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoinListener implements Listener {

    private final PlayerBirthdayPerks plugin;
    private final ConfigManager configManager;
    private final MessageManager messageManager;
    private final PlayerDataManager playerDataManager;

    public PlayerJoinListener(PlayerBirthdayPerks plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.messageManager = plugin.getMessageManager();
        this.playerDataManager = plugin.getPlayerDataManager();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 异步处理玩家数据
        playerDataManager.getOrCreatePlayerData(player.getUniqueId(), player.getName())
                .thenAccept(data -> {
                    if (data == null) return;

                    // 延迟执行生日检查，确保玩家完全加入
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!player.isOnline()) return;

                            checkBirthday(player, data);
                            checkAvatarFrameExpiry(player, data);
                        }
                    }.runTaskLater(plugin, 40L); // 2秒延迟
                });
    }

    private void checkBirthday(Player player, PlayerData data) {
        if (!data.hasBirthdaySet()) {
            return;
        }

        // 检查是否是生日
        if (data.isBirthdayToday()) {
            handleBirthdayLogin(player, data);
            return;
        }

        // 检查是否在生日窗口期内（生日已过但还能领取）
        if (data.isBirthdayInWindow(configManager.getClaimWindowDays()) && !data.hasClaimedThisYear()) {
            // 发送提醒可以领取
            if (configManager.isLoginNotificationEnabled()) {
                messageManager.sendBirthdayReminder(player);
            }
            return;
        }

        // 检查即将到来的生日
        long daysUntil = data.getDaysUntilBirthday();
        if (daysUntil > 0 && daysUntil <= 7) {
            // 一周内有生日，发送提醒
            messageManager.sendUpcomingBirthdayReminder(player, daysUntil);
        }
    }

    private void handleBirthdayLogin(Player player, PlayerData data) {
        plugin.debug("玩家 " + player.getName() + " 在生日当天登录");

        // 发送生日登录提醒
        if (configManager.isLoginNotificationEnabled()) {
            messageManager.sendBirthdayReminder(player);
        }

        // 全服广播（如果启用且今天还没广播过）
        if (configManager.isBroadcastEnabled() && !data.hasClaimedThisYear()) {
            String message = configManager.getBroadcastMessage();
            message = message.replace("%player%", player.getName());
            Bukkit.broadcastMessage(com.birthdayperks.util.ColorUtil.colorize(message));
        }
    }

    private void checkAvatarFrameExpiry(Player player, PlayerData data) {
        // 检查头像框是否过期
        if (configManager.isAvatarFrameEnabled()) {
            if (data.hasValidAvatarFrame()) {
                plugin.debug("玩家 " + player.getName() + " 拥有有效的生日头像框");
            } else if (data.getAvatarFrameExpiry() != null) {
                // 头像框已过期
                messageManager.send(player, "avatar-frame.expired");
            }
        }
    }
}
