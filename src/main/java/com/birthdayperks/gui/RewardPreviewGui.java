package com.birthdayperks.gui;

import com.birthdayperks.PlayerBirthdayPerks;
import com.birthdayperks.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RewardPreviewGui extends AbstractGui {

    public RewardPreviewGui(PlayerBirthdayPerks plugin, Player player) {
        super(plugin, player, "&d&l🎁 福利预览", 5);
        initialize();
    }

    @Override
    public void initialize() {
        fillBorder(Material.MAGENTA_STAINED_GLASS_PANE);

        // 标题
        ItemStack titleItem = createItem(
                Material.NETHER_STAR,
                "&6&l生日福利内容",
                Arrays.asList(
                        "&7以下是生日当天可获得的福利",
                        "",
                        "&7设置生日后，在生日当天",
                        "&7登录服务器即可领取！"
                )
        );
        setItem(4, titleItem);

        int slot = 19;

        // 物品奖励预览
        List<Map<?, ?>> items = plugin.getConfig().getMapList("rewards.items");
        if (!items.isEmpty()) {
            List<String> itemLore = new ArrayList<>();
            itemLore.add("&7包含以下物品:");
            itemLore.add("");
            
            for (Map<?, ?> itemConfig : items) {
                String materialName = (String) itemConfig.get("material");
                int amount = itemConfig.containsKey("amount") ? ((Number) itemConfig.get("amount")).intValue() : 1;
                String name = itemConfig.containsKey("name") ? (String) itemConfig.get("name") : materialName;
                itemLore.add("&e• &f" + ColorUtil.stripColor(ColorUtil.colorize(name)) + " &7x" + amount);
            }
            
            ItemStack itemReward = createItem(Material.CHEST, "&e&l📦 物品奖励", itemLore);
            setItem(slot++, itemReward);
        }

        // 经验奖励
        if (plugin.getConfigManager().isExperienceEnabled()) {
            int exp = plugin.getConfigManager().getExperienceAmount();
            ItemStack expItem = createItem(
                    Material.EXPERIENCE_BOTTLE,
                    "&a&l✨ 经验奖励",
                    Arrays.asList(
                            "&7获得经验值:",
                            "&e" + exp + " 点经验"
                    )
            );
            setItem(slot++, expItem);
        }

        // 金钱奖励
        if (plugin.getConfigManager().isMoneyEnabled()) {
            double money = plugin.getConfigManager().getMoneyAmount();
            ItemStack moneyItem = createItem(
                    Material.GOLD_INGOT,
                    "&6&l💰 金钱奖励",
                    Arrays.asList(
                            "&7获得金钱:",
                            "&e" + money + " 元"
                    )
            );
            setItem(slot++, moneyItem);
        }

        // 烟花效果
        if (plugin.getConfigManager().isFireworkEnabled()) {
            int amount = plugin.getConfigManager().getFireworkAmount();
            ItemStack fireworkItem = createItem(
                    Material.FIREWORK_ROCKET,
                    "&c&l🎆 烟花庆祝",
                    Arrays.asList(
                            "&7释放庆祝烟花:",
                            "&e" + amount + " 发烟花"
                    )
            );
            setItem(slot++, fireworkItem);
        }

        // 全服广播
        if (plugin.getConfigManager().isBroadcastEnabled()) {
            ItemStack broadcastItem = createItem(
                    Material.BELL,
                    "&b&l📢 全服广播",
                    Arrays.asList(
                            "&7生日当天登录时",
                            "&7全服玩家都会收到祝福！"
                    )
            );
            setItem(slot++, broadcastItem);
        }

        // 头像框
        if (plugin.getConfigManager().isAvatarFrameEnabled()) {
            int days = plugin.getConfigManager().getAvatarFrameDurationDays();
            String duration = days < 0 ? "永久" : days + " 天";
            ItemStack frameItem = createItem(
                    Material.PAINTING,
                    "&d&l🖼️ 专属头像框",
                    Arrays.asList(
                            "&7获得生日专属头像框",
                            "&7有效期: &e" + duration
                    )
            );
            setItem(slot++, frameItem);
        }

        // 命令奖励（如果有）
        List<String> commands = plugin.getConfig().getStringList("rewards.commands");
        if (!commands.isEmpty()) {
            ItemStack cmdItem = createItem(
                    Material.COMMAND_BLOCK,
                    "&5&l⚡ 特殊奖励",
                    Arrays.asList(
                            "&7还有更多惊喜奖励",
                            "&7等你来发现！"
                    )
            );
            setItem(slot++, cmdItem);
        }

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
