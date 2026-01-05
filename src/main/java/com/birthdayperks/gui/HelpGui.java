package com.birthdayperks.gui;

import com.birthdayperks.PlayerBirthdayPerks;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class HelpGui extends AbstractGui {

    public HelpGui(PlayerBirthdayPerks plugin, Player player) {
        super(plugin, player, "&a&l❓ 帮助说明", 5);
        initialize();
    }

    @Override
    public void initialize() {
        fillBorder(Material.LIME_STAINED_GLASS_PANE);

        // 系统介绍
        ItemStack introItem = createItem(
                Material.BOOK,
                "&6&l系统介绍",
                Arrays.asList(
                        "&7生日福利系统让你可以在",
                        "&7游戏中设置自己的生日，",
                        "&7并在生日当天获得专属福利！",
                        "",
                        "&e类似王者荣耀的生日福利系统"
                )
        );
        setItem(11, introItem);

        // 如何使用
        ItemStack howToItem = createItem(
                Material.WRITABLE_BOOK,
                "&e&l如何使用",
                Arrays.asList(
                        "&a1. &7首先设置你的生日",
                        "&a2. &7生日当天登录服务器",
                        "&a3. &7点击领取福利按钮",
                        "&a4. &7获得专属生日奖励！",
                        "",
                        "&c注意: 生日设置后可能无法修改"
                )
        );
        setItem(13, howToItem);

        // 命令帮助
        ItemStack cmdItem = createItem(
                Material.COMMAND_BLOCK,
                "&b&l命令列表",
                Arrays.asList(
                        "&e/bd &7- 打开主菜单",
                        "&e/bd set <月> <日> &7- 设置生日",
                        "&e/bd info &7- 查看信息",
                        "&e/bd claim &7- 领取福利",
                        "&e/bd help &7- 显示帮助"
                )
        );
        setItem(15, cmdItem);

        // 福利说明
        ItemStack rewardItem = createItem(
                Material.DIAMOND,
                "&d&l福利内容",
                Arrays.asList(
                        "&7生日福利包括:",
                        "",
                        "&e• &7专属物品奖励",
                        "&e• &7经验值奖励",
                        "&e• &7烟花庆祝效果",
                        "&e• &7全服生日广播",
                        "&e• &7限时专属头像框",
                        "",
                        "&7具体内容请查看福利预览"
                )
        );
        setItem(21, rewardItem);

        // 注意事项
        ItemStack noticeItem = createItem(
                Material.PAPER,
                "&c&l注意事项",
                Arrays.asList(
                        "&7• 生日每年只能领取一次福利",
                        "&7• 福利有领取时间窗口期",
                        "&7• 错过窗口期则今年无法领取",
                        "&7• 请在背包有空位时领取",
                        "",
                        "&e请认真设置你的真实生日！"
                )
        );
        setItem(23, noticeItem);

        // 返回按钮
        ItemStack backItem = createItem(
                Material.ARROW,
                "&7&l← 返回主菜单",
                Arrays.asList("&7点击返回")
        );
        setItem(36, backItem, event -> {
            playClickSound();
            plugin.getPlayerDataManager().getPlayerData(player.getUniqueId())
                    .thenAccept(data -> {
                        org.bukkit.Bukkit.getScheduler().runTask(plugin, () ->
                                plugin.getGuiManager().openGui(player, new MainMenuGui(plugin, player, data)));
                    });
        });

        fillEmpty(Material.BLACK_STAINED_GLASS_PANE);
    }

    private void playClickSound() {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
}
