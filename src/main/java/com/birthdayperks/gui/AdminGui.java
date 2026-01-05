package com.birthdayperks.gui;

import com.birthdayperks.PlayerBirthdayPerks;
import com.birthdayperks.model.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;

public class AdminGui extends AbstractGui {

    public AdminGui(PlayerBirthdayPerks plugin, Player player) {
        super(plugin, player, "&c&l⚙ 管理员面板", 5);
        initialize();
    }

    @Override
    public void initialize() {
        fillBorder(Material.RED_STAINED_GLASS_PANE);

        // 重载配置
        ItemStack reloadItem = createItem(
                Material.REDSTONE,
                "&e&l🔄 重载配置",
                Arrays.asList(
                        "&7重新加载插件配置文件",
                        "",
                        "&a▶ 点击执行"
                )
        );
        setItem(20, reloadItem, event -> {
            playClickSound();
            plugin.reload();
            plugin.getMessageManager().send(player, "general.reload-success");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
        });

        // 查看在线玩家
        ItemStack playersItem = createItem(
                Material.PLAYER_HEAD,
                "&b&l👥 在线玩家管理",
                Arrays.asList(
                        "&7查看和管理在线玩家的生日数据",
                        "",
                        "&7当前在线: &e" + Bukkit.getOnlinePlayers().size() + " 人",
                        "",
                        "&a▶ 点击查看"
                )
        );
        setItem(22, playersItem, event -> {
            playClickSound();
            plugin.getGuiManager().openGui(player, new AdminPlayerListGui(plugin, player, 0));
        });

        // 数据库信息
        ItemStack dbItem = createItem(
                Material.CHEST,
                "&d&l💾 数据库信息",
                Arrays.asList(
                        "&7数据库类型: &e" + plugin.getDatabase().getDatabaseType(),
                        "",
                        "&7存储玩家生日数据"
                )
        );
        setItem(24, dbItem);

        // 今日过生日的玩家
        ItemStack todayItem = createItem(
                Material.CAKE,
                "&6&l🎂 今日生日",
                Arrays.asList(
                        "&7查看今天过生日的玩家",
                        "",
                        "&a▶ 点击查看"
                )
        );
        setItem(30, todayItem, event -> {
            playClickSound();
            plugin.getDatabase().getTodayBirthdayPlayers().thenAccept(players -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (players.isEmpty()) {
                        plugin.getMessageManager().send(player, "admin.no-birthday-today");
                    } else {
                        StringBuilder msg = new StringBuilder("&6今日过生日的玩家: ");
                        for (PlayerData data : players) {
                            msg.append("&e").append(data.getPlayerName()).append("&7, ");
                        }
                        player.sendMessage(com.birthdayperks.util.ColorUtil.colorize(msg.toString()));
                    }
                });
            });
        });

        // 插件信息
        ItemStack infoItem = createItem(
                Material.KNOWLEDGE_BOOK,
                "&a&l📖 插件信息",
                Arrays.asList(
                        "&7插件: &ePlayerBirthdayPerks",
                        "&7版本: &e" + plugin.getDescription().getVersion(),
                        "&7作者: &e" + String.join(", ", plugin.getDescription().getAuthors()),
                        "",
                        "&7仿王者荣耀生日福利系统"
                )
        );
        setItem(32, infoItem);

        // 关闭按钮
        ItemStack closeItem = createItem(
                Material.BARRIER,
                "&c&l✖ 关闭",
                Arrays.asList("&7点击关闭")
        );
        setItem(40, closeItem, event -> {
            playClickSound();
            player.closeInventory();
        });

        fillEmpty(Material.BLACK_STAINED_GLASS_PANE);
    }

    private void playClickSound() {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
}
