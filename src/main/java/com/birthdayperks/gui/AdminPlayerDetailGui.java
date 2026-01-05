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

import java.util.Arrays;

public class AdminPlayerDetailGui extends AbstractGui {

    private final Player target;
    private PlayerData targetData;

    public AdminPlayerDetailGui(PlayerBirthdayPerks plugin, Player player, Player target, PlayerData targetData) {
        super(plugin, player, "&c&l管理玩家: " + target.getName(), 4);
        this.target = target;
        this.targetData = targetData;
        initialize();
    }

    @Override
    public void initialize() {
        fillBorder(Material.RED_STAINED_GLASS_PANE);

        // 玩家头像
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(target);
            meta.setDisplayName(ColorUtil.colorize("&6&l" + target.getName()));
            
            if (targetData != null && targetData.hasBirthdaySet() && targetData.getBirthDate() != null) {
                java.time.LocalDate bd = targetData.getBirthDate();
                String birthDateStr = bd.getYear() + "年" + bd.getMonthValue() + "月" + bd.getDayOfMonth() + "日";
                meta.setLore(Arrays.asList(
                        ColorUtil.colorize("&7UUID: &f" + target.getUniqueId().toString().substring(0, 8) + "..."),
                        "",
                        ColorUtil.colorize("&7生日: &e" + birthDateStr),
                        ColorUtil.colorize("&7年龄: &d" + targetData.getAge() + " 岁"),
                        ColorUtil.colorize("&7星座: &d" + DateUtil.getZodiacSign(targetData.getBirthdayMonthDay())),
                        ColorUtil.colorize("&7今年已领取: " + (targetData.hasClaimedThisYear() ? "&a是" : "&c否")),
                        targetData.getLastClaimDate() != null 
                                ? ColorUtil.colorize("&7上次领取: &f" + targetData.getLastClaimDate())
                                : ColorUtil.colorize("&7上次领取: &7从未"),
                        ColorUtil.colorize("&7头像框: " + (targetData.hasValidAvatarFrame() ? "&a有效" : "&c无"))
                ));
            } else {
                meta.setLore(Arrays.asList(
                        ColorUtil.colorize("&7UUID: &f" + target.getUniqueId().toString().substring(0, 8) + "..."),
                        "",
                        ColorUtil.colorize("&7生日: &c未设置")
                ));
            }
            head.setItemMeta(meta);
        }
        setItem(13, head);

        // 给予福利
        ItemStack giveItem = createItem(Material.EMERALD, "&a&l🎁 给予福利",
                Arrays.asList("&7立即给予该玩家生日福利", "&7（无视生日限制）", "", "&a▶ 点击执行"));
        setItem(20, giveItem, event -> {
            playClickSound();
            giveReward();
        });

        // 重置数据
        ItemStack resetItem = createItem(Material.TNT, "&c&l🗑 重置数据",
                Arrays.asList("&7删除该玩家的所有生日数据", "&c此操作不可撤销！", "", "&c▶ 点击执行"));
        setItem(22, resetItem, event -> {
            playClickSound();
            resetData();
        });

        // 重置今年领取状态
        ItemStack resetClaimItem = createItem(Material.GOLD_INGOT, "&e&l↻ 重置领取状态",
                Arrays.asList("&7重置该玩家今年的领取状态", "&7使其可以再次领取福利", "", "&e▶ 点击执行"));
        setItem(24, resetClaimItem, event -> {
            playClickSound();
            resetClaimStatus();
        });

        // 返回按钮
        ItemStack backItem = createItem(Material.ARROW, "&7&l← 返回", Arrays.asList("&7返回玩家列表"));
        setItem(27, backItem, event -> {
            playClickSound();
            plugin.getGuiManager().openGui(player, new AdminPlayerListGui(plugin, player, 0));
        });

        fillEmpty(Material.BLACK_STAINED_GLASS_PANE);
    }

    private void giveReward() {
        if (!target.isOnline()) {
            plugin.getMessageManager().send(player, "general.player-not-found", 
                    java.util.Map.of("player", target.getName()));
            return;
        }

        PlayerData data = targetData;
        if (data == null) {
            data = new PlayerData(target.getUniqueId());
            data.setPlayerName(target.getName());
        }

        PlayerData finalData = data;
        plugin.getRewardManager().giveRewards(target, data).thenAccept(success -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (success) {
                    plugin.getMessageManager().send(player, "admin.give-success",
                            java.util.Map.of("player", target.getName()));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
                } else {
                    plugin.getMessageManager().send(player, "admin.give-failed");
                }
            });
        });
    }

    private void resetData() {
        plugin.getPlayerDataManager().deletePlayerData(target.getUniqueId()).thenRun(() -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getMessageManager().send(player, "admin.reset-success",
                        java.util.Map.of("player", target.getName()));
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.3f, 1.0f);
                targetData = null;
                initialize();
            });
        });
    }

    private void resetClaimStatus() {
        if (targetData == null) {
            plugin.getMessageManager().send(player, "admin.check.not-set");
            return;
        }

        targetData.setLastClaimYear(0);
        targetData.setLastClaimDate(null);
        plugin.getPlayerDataManager().savePlayerData(targetData).thenRun(() -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getMessageManager().send(player, "admin.reset-claim-success",
                        java.util.Map.of("player", target.getName()));
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
                initialize();
            });
        });
    }

    private void playClickSound() {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
}
