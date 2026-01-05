package com.birthdayperks.gui;

import com.birthdayperks.PlayerBirthdayPerks;
import com.birthdayperks.model.PlayerData;
import com.birthdayperks.util.ColorUtil;
import com.birthdayperks.util.DateUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminPlayerListGui extends AbstractGui {

    private final int page;
    private static final int PLAYERS_PER_PAGE = 28;

    public AdminPlayerListGui(PlayerBirthdayPerks plugin, Player player, int page) {
        super(plugin, player, "&c&l👥 在线玩家管理 &7- 第" + (page + 1) + "页", 5);
        this.page = page;
        initialize();
    }

    @Override
    public void initialize() {
        fillBorder(Material.RED_STAINED_GLASS_PANE);

        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        int totalPages = (int) Math.ceil((double) onlinePlayers.size() / PLAYERS_PER_PAGE);
        int start = page * PLAYERS_PER_PAGE;
        int end = Math.min(start + PLAYERS_PER_PAGE, onlinePlayers.size());

        // 显示玩家
        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34
        };

        int slotIndex = 0;
        for (int i = start; i < end && slotIndex < slots.length; i++) {
            Player target = onlinePlayers.get(i);
            int slot = slots[slotIndex++];
            
            // 异步获取玩家数据
            plugin.getPlayerDataManager().getPlayerData(target.getUniqueId())
                    .thenAccept(data -> {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            ItemStack head = createPlayerHead(target, data);
                            setItem(slot, head, event -> {
                                playClickSound();
                                plugin.getGuiManager().openGui(player, new AdminPlayerDetailGui(plugin, player, target, data));
                            });
                        });
                    });

            // 先放置一个占位符
            ItemStack placeholder = createPlayerHeadPlaceholder(target);
            setItem(slot, placeholder);
        }

        // 上一页
        if (page > 0) {
            ItemStack prevItem = createItem(
                    Material.ARROW,
                    "&e&l◀ 上一页",
                    Arrays.asList("&7点击查看上一页")
            );
            setItem(38, prevItem, event -> {
                playClickSound();
                plugin.getGuiManager().openGui(player, new AdminPlayerListGui(plugin, player, page - 1));
            });
        }

        // 页码信息
        ItemStack pageItem = createItem(
                Material.PAPER,
                "&7第 &e" + (page + 1) + "&7/&e" + Math.max(1, totalPages) + " &7页",
                Arrays.asList("&7共 &e" + onlinePlayers.size() + " &7名在线玩家")
        );
        setItem(40, pageItem);

        // 下一页
        if (page < totalPages - 1) {
            ItemStack nextItem = createItem(
                    Material.ARROW,
                    "&e&l▶ 下一页",
                    Arrays.asList("&7点击查看下一页")
            );
            setItem(42, nextItem, event -> {
                playClickSound();
                plugin.getGuiManager().openGui(player, new AdminPlayerListGui(plugin, player, page + 1));
            });
        }

        // 返回按钮
        ItemStack backItem = createItem(
                Material.BARRIER,
                "&c&l← 返回",
                Arrays.asList("&7返回管理面板")
        );
        setItem(36, backItem, event -> {
            playClickSound();
            plugin.getGuiManager().openGui(player, new AdminGui(plugin, player));
        });

        fillEmpty(Material.BLACK_STAINED_GLASS_PANE);
    }

    private ItemStack createPlayerHeadPlaceholder(Player target) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(target);
            meta.setDisplayName(ColorUtil.colorize("&e" + target.getName()));
            meta.setLore(Arrays.asList(ColorUtil.colorize("&7加载中...")));
            head.setItemMeta(meta);
        }
        return head;
    }

    private ItemStack createPlayerHead(Player target, PlayerData data) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(target);
            meta.setDisplayName(ColorUtil.colorize("&e" + target.getName()));
            
            List<String> lore = new ArrayList<>();
            if (data != null && data.hasBirthdaySet()) {
                lore.add(ColorUtil.colorize("&7生日: &e" + DateUtil.formatMonthDayChinese(data.getBirthday())));
                lore.add(ColorUtil.colorize("&7今年已领取: " + (data.hasClaimedThisYear() ? "&a是" : "&c否")));
            } else {
                lore.add(ColorUtil.colorize("&7生日: &c未设置"));
            }
            lore.add("");
            lore.add(ColorUtil.colorize("&a▶ 点击管理"));
            
            meta.setLore(lore);
            head.setItemMeta(meta);
        }
        return head;
    }

    private void playClickSound() {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
}
