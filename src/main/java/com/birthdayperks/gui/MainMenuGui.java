package com.birthdayperks.gui;

import com.birthdayperks.PlayerBirthdayPerks;
import com.birthdayperks.model.PlayerData;
import com.birthdayperks.util.DateUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class MainMenuGui extends AbstractGui {

    private final PlayerData playerData;

    public MainMenuGui(PlayerBirthdayPerks plugin, Player player, PlayerData playerData) {
        super(plugin, player, "&6&l🎂 生日福利系统", 5);
        this.playerData = playerData;
        initialize();
    }

    @Override
    public void initialize() {
        // 填充边框
        fillBorder(Material.GRAY_STAINED_GLASS_PANE);

        // 玩家头像 + 信息
        ItemStack playerHead = createPlayerInfoItem();
        setItem(13, playerHead);

        // 设置生日按钮
        ItemStack setBirthdayItem = createItem(
                Material.CLOCK,
                "&e&l📅 设置生日",
                Arrays.asList(
                        "&7点击设置你的生日日期",
                        "",
                        playerData != null && playerData.hasBirthdaySet() && playerData.getBirthDate() != null
                                ? "&7当前: &e" + formatBirthDate(playerData.getBirthDate())
                                : "&7当前: &c未设置",
                        "",
                        "&a▶ 点击设置"
                )
        );
        setItem(20, setBirthdayItem, event -> {
            playClickSound();
            plugin.getGuiManager().openGui(player, new SetBirthdayGui(plugin, player, playerData));
        });

        // 查看信息按钮
        ItemStack infoItem = createItem(
                Material.BOOK,
                "&b&l📋 生日信息",
                createInfoLore()
        );
        setItem(22, infoItem, event -> {
            playClickSound();
            plugin.getGuiManager().openGui(player, new BirthdayInfoGui(plugin, player, playerData));
        });

        // 领取福利按钮
        ItemStack claimItem = createClaimItem();
        setItem(24, claimItem, event -> {
            playClickSound();
            handleClaim();
        });

        // 福利预览按钮
        ItemStack previewItem = createItem(
                Material.ENDER_CHEST,
                "&d&l🎁 福利预览",
                Arrays.asList(
                        "&7查看生日福利内容",
                        "",
                        "&a▶ 点击查看"
                )
        );
        setItem(30, previewItem, event -> {
            playClickSound();
            plugin.getGuiManager().openGui(player, new RewardPreviewGui(plugin, player));
        });

        // 帮助按钮
        ItemStack helpItem = createItem(
                Material.KNOWLEDGE_BOOK,
                "&a&l❓ 帮助说明",
                Arrays.asList(
                        "&7了解生日福利系统",
                        "",
                        "&a▶ 点击查看"
                )
        );
        setItem(32, helpItem, event -> {
            playClickSound();
            plugin.getGuiManager().openGui(player, new HelpGui(plugin, player));
        });

        // 关闭按钮
        ItemStack closeItem = createItem(
                Material.BARRIER,
                "&c&l✖ 关闭菜单",
                Arrays.asList("&7点击关闭菜单")
        );
        setItem(40, closeItem, event -> {
            playClickSound();
            player.closeInventory();
        });

        // 填充空位
        fillEmpty(Material.BLACK_STAINED_GLASS_PANE);
    }

    private ItemStack createPlayerInfoItem() {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        org.bukkit.inventory.meta.SkullMeta meta = (org.bukkit.inventory.meta.SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);
            meta.setDisplayName(com.birthdayperks.util.ColorUtil.colorize("&6&l" + player.getName()));
            
            List<String> lore = Arrays.asList(
                    com.birthdayperks.util.ColorUtil.colorize("&7欢迎使用生日福利系统！"),
                    "",
                    com.birthdayperks.util.ColorUtil.colorize(playerData != null && playerData.hasBirthdaySet() && playerData.getBirthDate() != null
                            ? "&7你的生日: &e" + formatBirthDate(playerData.getBirthDate()) + " (&d" + playerData.getAge() + "岁&7)"
                            : "&7你还未设置生日"),
                    com.birthdayperks.util.ColorUtil.colorize(playerData != null && playerData.hasBirthdaySet() && playerData.hasValidAvatarFrame()
                            ? "&7头像框: &a有效"
                            : "&7头像框: &c无")
            );
            meta.setLore(lore);
            head.setItemMeta(meta);
        }
        return head;
    }

    private List<String> createInfoLore() {
        if (playerData == null || !playerData.hasBirthdaySet()) {
            return Arrays.asList(
                    "&7查看你的生日详情",
                    "",
                    "&c你还未设置生日",
                    "",
                    "&a▶ 点击查看"
            );
        }

        String status;
        if (playerData.isBirthdayToday()) {
            status = "&a&l今天是你的生日！";
        } else {
            long days = playerData.getDaysUntilBirthday();
            status = "&7距离生日还有 &e" + days + " &7天";
        }

        return Arrays.asList(
                "&7查看你的生日详情",
                "",
                status,
                playerData.hasClaimedThisYear() ? "&7今年福利: &a已领取" : "&7今年福利: &e未领取",
                "",
                "&a▶ 点击查看"
        );
    }

    private ItemStack createClaimItem() {
        boolean canClaim = playerData != null 
                && playerData.hasBirthdaySet() 
                && playerData.isBirthdayInWindow(plugin.getConfigManager().getClaimWindowDays())
                && !playerData.hasClaimedThisYear();

        if (canClaim) {
            return createItem(
                    Material.CAKE,
                    "&6&l🎂 领取福利",
                    Arrays.asList(
                            "&a&l可以领取！",
                            "",
                            "&7点击领取你的生日福利",
                            "",
                            "&e▶ 点击领取"
                    )
            );
        } else {
            String reason;
            if (playerData == null || !playerData.hasBirthdaySet()) {
                reason = "&c请先设置生日";
            } else if (playerData.hasClaimedThisYear()) {
                reason = "&c今年已领取";
            } else {
                reason = "&c今天不是你的生日";
            }

            return createItem(
                    Material.GRAY_DYE,
                    "&7&l🎂 领取福利",
                    Arrays.asList(
                            reason,
                            "",
                            "&7暂时无法领取"
                    )
            );
        }
    }

    private void handleClaim() {
        if (playerData == null || !playerData.hasBirthdaySet()) {
            plugin.getMessageManager().send(player, "reward.no-birthday-set");
            return;
        }

        if (!playerData.isBirthdayInWindow(plugin.getConfigManager().getClaimWindowDays())) {
            plugin.getMessageManager().send(player, "reward.not-birthday");
            return;
        }

        if (playerData.hasClaimedThisYear()) {
            plugin.getMessageManager().send(player, "reward.already-claimed");
            return;
        }

        player.closeInventory();
        
        plugin.getRewardManager().giveRewards(player, playerData).thenAccept(success -> {
            if (success) {
                playerData.markClaimed();
                plugin.getPlayerDataManager().savePlayerData(playerData);
                org.bukkit.Bukkit.getScheduler().runTask(plugin, () ->
                        plugin.getMessageManager().sendRewardSuccess(player));
            }
        });
    }

    private void playClickSound() {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }

    private String formatBirthDate(LocalDate date) {
        return date.getYear() + "年" + date.getMonthValue() + "月" + date.getDayOfMonth() + "日";
    }
}
