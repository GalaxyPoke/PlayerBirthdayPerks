package com.birthdayperks.gui;

import com.birthdayperks.PlayerBirthdayPerks;
import com.birthdayperks.model.PlayerData;
import com.birthdayperks.util.DateUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BirthdayInfoGui extends AbstractGui {

    private final PlayerData playerData;

    public BirthdayInfoGui(PlayerBirthdayPerks plugin, Player player, PlayerData playerData) {
        super(plugin, player, "&b&l📋 生日信息", 4);
        this.playerData = playerData;
        initialize();
    }

    @Override
    public void initialize() {
        fillBorder(Material.CYAN_STAINED_GLASS_PANE);

        if (playerData == null || !playerData.hasBirthdaySet()) {
            // 未设置生日
            ItemStack notSetItem = createItem(
                    Material.BARRIER,
                    "&c&l未设置生日",
                    Arrays.asList(
                            "&7你还没有设置生日",
                            "",
                            "&e点击去设置"
                    )
            );
            setItem(13, notSetItem, event -> {
                playClickSound();
                plugin.getGuiManager().openGui(player, new SetBirthdayGui(plugin, player, playerData));
            });
        } else {
            LocalDate birthDate = playerData.getBirthDate();

            // 生日日期
            ItemStack dateItem = createItem(
                    Material.CAKE,
                    "&e&l🎂 你的生日",
                    Arrays.asList(
                            "&7日期: &e" + formatBirthDate(birthDate),
                            "&7年龄: &d" + playerData.getAge() + " 岁",
                            "&7星座: &d" + DateUtil.getZodiacSign(playerData.getBirthdayMonthDay())
                    )
            );
            setItem(11, dateItem);

            // 倒计时
            ItemStack countdownItem;
            if (playerData.isBirthdayToday()) {
                countdownItem = createItem(
                        Material.FIREWORK_ROCKET,
                        "&a&l🎉 今天是你的生日！",
                        Arrays.asList(
                                "&6祝你生日快乐！",
                                "",
                                "&7快去领取你的专属福利吧！"
                        )
                );
            } else {
                long days = playerData.getDaysUntilBirthday();
                countdownItem = createItem(
                        Material.CLOCK,
                        "&b&l⏰ 生日倒计时",
                        Arrays.asList(
                                "&7距离下次生日还有:",
                                "&e" + days + " 天"
                        )
                );
            }
            setItem(13, countdownItem);

            // 福利状态
            List<String> statusLore = new ArrayList<>();
            if (playerData.hasClaimedThisYear()) {
                statusLore.add("&a✔ 今年已领取");
                if (playerData.getLastClaimDate() != null) {
                    statusLore.add("&7领取时间: &f" + playerData.getLastClaimDate().toString());
                }
            } else if (playerData.isBirthdayInWindow(plugin.getConfigManager().getClaimWindowDays())) {
                statusLore.add("&e⚡ 可以领取！");
                statusLore.add("");
                statusLore.add("&a点击前往领取");
            } else if (DateUtil.isBirthdayPassedThisYear(playerData.getBirthdayMonthDay())) {
                statusLore.add("&c✖ 今年已过期");
            } else {
                statusLore.add("&7⏳ 等待生日到来");
            }

            ItemStack statusItem = createItem(
                    playerData.hasClaimedThisYear() ? Material.EMERALD : 
                            playerData.isBirthdayInWindow(plugin.getConfigManager().getClaimWindowDays()) ? Material.GOLDEN_APPLE : Material.GRAY_DYE,
                    "&6&l🎁 福利状态",
                    statusLore
            );
            setItem(15, statusItem, event -> {
                if (!playerData.hasClaimedThisYear() && playerData.isBirthdayInWindow(plugin.getConfigManager().getClaimWindowDays())) {
                    playClickSound();
                    plugin.getGuiManager().openGui(player, new MainMenuGui(plugin, player, playerData));
                }
            });

            // 头像框状态
            List<String> frameLore = new ArrayList<>();
            if (playerData.hasValidAvatarFrame()) {
                frameLore.add("&a✔ 头像框有效");
                if (playerData.getAvatarFrameExpiry() != null) {
                    frameLore.add("&7到期时间: &f" + playerData.getAvatarFrameExpiry().toString());
                }
            } else {
                frameLore.add("&7暂无头像框");
                frameLore.add("");
                frameLore.add("&e领取生日福利可获得");
            }

            ItemStack frameItem = createItem(
                    playerData.hasValidAvatarFrame() ? Material.PAINTING : Material.ITEM_FRAME,
                    "&d&l🖼️ 头像框",
                    frameLore
            );
            setItem(22, frameItem);
        }

        // 返回按钮
        ItemStack backItem = createItem(
                Material.ARROW,
                "&7&l← 返回主菜单",
                Arrays.asList("&7点击返回")
        );
        setItem(27, backItem, event -> {
            playClickSound();
            plugin.getGuiManager().openGui(player, new MainMenuGui(plugin, player, playerData));
        });

        fillEmpty(Material.BLACK_STAINED_GLASS_PANE);
    }

    private void playClickSound() {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }

    private String formatBirthDate(LocalDate date) {
        if (date == null) return "未设置";
        return date.getYear() + "年" + date.getMonthValue() + "月" + date.getDayOfMonth() + "日";
    }
}
