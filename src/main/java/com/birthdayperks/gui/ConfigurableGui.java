package com.birthdayperks.gui;

import com.birthdayperks.PlayerBirthdayPerks;
import com.birthdayperks.model.PlayerData;
import com.birthdayperks.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

public class ConfigurableGui extends AbstractGui {

    protected final String menuName;
    protected final FileConfiguration menuConfig;
    protected PlayerData playerData;
    protected final Map<String, Function<String, String>> placeholderHandlers;

    public ConfigurableGui(PlayerBirthdayPerks plugin, Player player, String menuName, PlayerData playerData) {
        super(plugin, player, 
              getTitle(plugin, menuName), 
              getRows(plugin, menuName));
        this.menuName = menuName;
        this.menuConfig = plugin.getMenuManager().getMenuConfig(menuName);
        this.playerData = playerData;
        this.placeholderHandlers = new HashMap<>();
        registerDefaultPlaceholders();
        initialize();
    }

    private static String getTitle(PlayerBirthdayPerks plugin, String menuName) {
        FileConfiguration config = plugin.getMenuManager().getMenuConfig(menuName);
        return config != null ? config.getString("title", "&6菜单") : "&6菜单";
    }

    private static int getRows(PlayerBirthdayPerks plugin, String menuName) {
        FileConfiguration config = plugin.getMenuManager().getMenuConfig(menuName);
        return config != null ? config.getInt("rows", 3) : 3;
    }

    protected void registerDefaultPlaceholders() {
        // 玩家相关
        placeholderHandlers.put("%player%", s -> player.getName());
        
        // 生日相关
        placeholderHandlers.put("%birthday%", s -> {
            if (playerData != null && playerData.hasBirthdaySet() && playerData.getBirthDate() != null) {
                LocalDate date = playerData.getBirthDate();
                return date.getYear() + "年" + date.getMonthValue() + "月" + date.getDayOfMonth() + "日";
            }
            return "&c未设置";
        });
        
        placeholderHandlers.put("%age%", s -> {
            if (playerData != null && playerData.hasBirthdaySet()) {
                return String.valueOf(playerData.getAge());
            }
            return "0";
        });
        
        placeholderHandlers.put("%birthday_status%", s -> {
            if (playerData == null || !playerData.hasBirthdaySet()) {
                return "&c你还未设置生日";
            }
            if (playerData.isBirthdayToday()) {
                return "&a&l今天是你的生日！";
            }
            long days = playerData.getDaysUntilBirthday();
            return "&7距离生日还有 &e" + days + " &7天";
        });
        
        placeholderHandlers.put("%claim_status%", s -> {
            if (playerData != null && playerData.hasClaimedThisYear()) {
                return "&a已领取";
            }
            return "&e未领取";
        });
        
        placeholderHandlers.put("%avatar_frame_status%", s -> {
            if (playerData != null && playerData.hasValidAvatarFrame()) {
                return "&a有效";
            }
            return "&c无";
        });
        
        placeholderHandlers.put("%cannot_claim_reason%", s -> {
            if (playerData == null || !playerData.hasBirthdaySet()) {
                return "&c请先设置生日";
            }
            if (playerData.hasClaimedThisYear()) {
                return "&c今年已领取";
            }
            return "&c今天不是你的生日";
        });
        
        // 奖励相关
        placeholderHandlers.put("%exp_amount%", s -> 
                String.valueOf(plugin.getConfigManager().getExperienceAmount()));
        placeholderHandlers.put("%money_amount%", s -> 
                String.valueOf(plugin.getConfigManager().getMoneyAmount()));
        placeholderHandlers.put("%firework_amount%", s -> 
                String.valueOf(plugin.getConfigManager().getFireworkAmount()));
        placeholderHandlers.put("%avatar_duration%", s -> {
            int days = plugin.getConfigManager().getAvatarFrameDurationDays();
            return days < 0 ? "永久" : days + " 天";
        });
        placeholderHandlers.put("%claim_window%", s -> 
                String.valueOf(plugin.getConfigManager().getClaimWindowDays()));
        
        // 奖励物品列表
        placeholderHandlers.put("%reward_items%", s -> {
            List<Map<?, ?>> items = plugin.getConfig().getMapList("rewards.items");
            StringBuilder sb = new StringBuilder();
            for (Map<?, ?> itemConfig : items) {
                String name = itemConfig.containsKey("name") 
                        ? (String) itemConfig.get("name") 
                        : (String) itemConfig.get("material");
                int amount = itemConfig.containsKey("amount") 
                        ? ((Number) itemConfig.get("amount")).intValue() : 1;
                sb.append("&e• &f").append(ColorUtil.stripColor(ColorUtil.colorize(name)))
                  .append(" &7x").append(amount).append("\n");
            }
            return sb.toString().trim();
        });
        
        // 星座
        placeholderHandlers.put("%zodiac%", s -> {
            if (playerData != null && playerData.hasBirthdaySet() && playerData.getBirthDate() != null) {
                return getZodiac(playerData.getBirthDate());
            }
            return "未知";
        });
        
        // 下次生日
        placeholderHandlers.put("%next_birthday%", s -> {
            if (playerData != null && playerData.hasBirthdaySet() && playerData.getBirthDate() != null) {
                LocalDate nextBirthday = playerData.getNextBirthday();
                return nextBirthday.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
            }
            return "未知";
        });
        
        // 生日倒计时
        placeholderHandlers.put("%birthday_countdown%", s -> {
            if (playerData == null || !playerData.hasBirthdaySet()) {
                return "&c请先设置生日";
            }
            if (playerData.isBirthdayToday()) {
                return "&a&l🎉 今天是你的生日！";
            }
            long days = playerData.getDaysUntilBirthday();
            return "&7距离生日还有 &e&l" + days + " &7天";
        });
        
        // 修改相关
        placeholderHandlers.put("%modify_answer%", s -> 
                plugin.getConfigManager().isAllowModify() ? "允许修改" : "不允许修改");
        placeholderHandlers.put("%modify_status%", s -> 
                plugin.getConfigManager().isAllowModify() ? "&a允许修改生日" : "&c不允许修改生日");
        placeholderHandlers.put("%modify_remaining%", s -> {
            int limit = plugin.getConfigManager().getModifyLimitPerYear();
            if (limit < 0) return "无限制";
            if (playerData != null) {
                int used = playerData.getModifyCountThisYear();
                return String.valueOf(Math.max(0, limit - used));
            }
            return String.valueOf(limit);
        });
        
        // 历史记录
        placeholderHandlers.put("%total_claims%", s -> 
                playerData != null ? String.valueOf(playerData.getTotalClaimCount()) : "0");
        placeholderHandlers.put("%first_claim_date%", s -> {
            if (playerData != null && playerData.getFirstClaimDate() != null) {
                return playerData.getFirstClaimDate().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
            }
            return "从未领取";
        });
        placeholderHandlers.put("%avatar_frame_expire%", s -> {
            if (playerData != null && playerData.getAvatarFrameExpireDate() != null) {
                if (playerData.hasValidAvatarFrame()) {
                    return playerData.getAvatarFrameExpireDate().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
                }
            }
            return "无";
        });
    }

    protected String replacePlaceholders(String text) {
        if (text == null) return "";
        String result = text;
        for (Map.Entry<String, Function<String, String>> entry : placeholderHandlers.entrySet()) {
            if (result.contains(entry.getKey())) {
                result = result.replace(entry.getKey(), entry.getValue().apply(entry.getKey()));
            }
        }
        return result;
    }

    protected List<String> replacePlaceholders(List<String> lines) {
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            String replaced = replacePlaceholders(line);
            // 处理多行占位符（如奖励物品列表）
            if (replaced.contains("\n")) {
                result.addAll(Arrays.asList(replaced.split("\n")));
            } else {
                result.add(replaced);
            }
        }
        return result;
    }

    @Override
    public void initialize() {
        if (menuConfig == null) {
            plugin.log(java.util.logging.Level.WARNING, "菜单配置不存在: " + menuName);
            return;
        }

        // 填充边框
        if (menuConfig.getBoolean("fill.border.enabled", false)) {
            String materialName = menuConfig.getString("fill.border.material", "GRAY_STAINED_GLASS_PANE");
            Material material = Material.matchMaterial(materialName);
            if (material != null) {
                fillBorder(material);
            }
        }

        // 加载物品
        ConfigurationSection itemsSection = menuConfig.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String itemKey : itemsSection.getKeys(false)) {
                loadItem(itemsSection.getConfigurationSection(itemKey), itemKey);
            }
        }

        // 填充空位
        if (menuConfig.getBoolean("fill.empty.enabled", false)) {
            String materialName = menuConfig.getString("fill.empty.material", "BLACK_STAINED_GLASS_PANE");
            Material material = Material.matchMaterial(materialName);
            if (material != null) {
                fillEmpty(material);
            }
        }
    }

    protected void loadItem(ConfigurationSection section, String itemKey) {
        if (section == null) return;

        int slot = section.getInt("slot", -1);
        if (slot < 0 || slot >= inventory.getSize()) return;

        // 检查显示条件
        String showCondition = section.getString("show-condition");
        if (showCondition != null && !checkCondition(showCondition)) {
            return;
        }

        // 检查条件物品
        ConfigurationSection conditions = section.getConfigurationSection("conditions");
        if (conditions != null) {
            loadConditionalItem(slot, conditions, itemKey);
            return;
        }

        // 获取材质
        String materialName = section.getString("material", "STONE");
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            material = Material.STONE;
        }

        // 创建物品
        ItemStack item;
        if (material == Material.PLAYER_HEAD && section.contains("skull-owner")) {
            item = createPlayerHead(section.getString("skull-owner"));
        } else {
            item = new ItemStack(material);
        }

        // 设置元数据
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String name = replacePlaceholders(section.getString("name", ""));
            meta.setDisplayName(ColorUtil.colorize(name));

            List<String> lore = section.getStringList("lore");
            if (!lore.isEmpty()) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : replacePlaceholders(lore)) {
                    coloredLore.add(ColorUtil.colorize(line));
                }
                meta.setLore(coloredLore);
            }

            item.setItemMeta(meta);
        }

        // 设置点击处理
        List<String> actions = section.getStringList("actions");
        if (!actions.isEmpty()) {
            setItem(slot, item, event -> handleActions(actions));
        } else {
            setItem(slot, item);
        }
    }

    protected void loadConditionalItem(int slot, ConfigurationSection conditions, String itemKey) {
        // 默认先检查can-claim条件
        if (conditions.contains("can-claim") && canClaim()) {
            loadItemFromSection(slot, conditions.getConfigurationSection("can-claim"));
        } else if (conditions.contains("cannot-claim")) {
            loadItemFromSection(slot, conditions.getConfigurationSection("cannot-claim"));
        }
    }

    protected void loadItemFromSection(int slot, ConfigurationSection section) {
        if (section == null) return;

        String materialName = section.getString("material", "STONE");
        Material material = Material.matchMaterial(materialName);
        if (material == null) material = Material.STONE;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String name = replacePlaceholders(section.getString("name", ""));
            meta.setDisplayName(ColorUtil.colorize(name));

            List<String> lore = section.getStringList("lore");
            if (!lore.isEmpty()) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : replacePlaceholders(lore)) {
                    coloredLore.add(ColorUtil.colorize(line));
                }
                meta.setLore(coloredLore);
            }

            item.setItemMeta(meta);
        }

        List<String> actions = section.getStringList("actions");
        if (!actions.isEmpty()) {
            setItem(slot, item, event -> handleActions(actions));
        } else {
            setItem(slot, item);
        }
    }

    protected ItemStack createPlayerHead(String owner) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            String ownerName = replacePlaceholders(owner);
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(ownerName));
            head.setItemMeta(meta);
        }
        return head;
    }

    protected boolean checkCondition(String condition) {
        switch (condition.toLowerCase()) {
            case "has_item_rewards":
                return !plugin.getConfig().getMapList("rewards.items").isEmpty();
            case "exp_enabled":
                return plugin.getConfigManager().isExperienceEnabled();
            case "money_enabled":
                return plugin.getConfigManager().isMoneyEnabled();
            case "firework_enabled":
                return plugin.getConfigManager().isFireworkEnabled();
            case "broadcast_enabled":
                return plugin.getConfigManager().isBroadcastEnabled();
            case "avatar_frame_enabled":
                return plugin.getConfigManager().isAvatarFrameEnabled();
            case "has_command_rewards":
                return !plugin.getConfig().getStringList("rewards.commands").isEmpty();
            case "allow_modify":
                return plugin.getConfigManager().isAllowModify();
            case "can_claim":
                return canClaim();
            default:
                return true;
        }
    }

    protected boolean canClaim() {
        return playerData != null 
                && playerData.hasBirthdaySet() 
                && playerData.isBirthdayInWindow(plugin.getConfigManager().getClaimWindowDays())
                && !playerData.hasClaimedThisYear();
    }

    protected void handleActions(List<String> actions) {
        for (String action : actions) {
            if (action.startsWith("[SOUND]")) {
                String soundName = action.replace("[SOUND]", "").trim();
                try {
                    Sound sound = Sound.valueOf(soundName);
                    player.playSound(player.getLocation(), sound, 0.5f, 1.0f);
                } catch (IllegalArgumentException ignored) {}
            } else if (action.startsWith("[OPEN]")) {
                String targetMenu = action.replace("[OPEN]", "").trim();
                openMenu(targetMenu);
            } else if (action.equals("[CLOSE]")) {
                player.closeInventory();
            } else if (action.equals("[CLAIM]")) {
                handleClaim();
            } else if (action.equals("[CONFIRM_BIRTHDAY]")) {
                // 由子类处理
            } else if (action.startsWith("[COMMAND]")) {
                String command = action.replace("[COMMAND]", "").trim();
                player.performCommand(replacePlaceholders(command));
            } else if (action.startsWith("[CONSOLE]")) {
                String command = action.replace("[CONSOLE]", "").trim();
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), replacePlaceholders(command));
            }
        }
    }

    protected void openMenu(String targetMenu) {
        plugin.getPlayerDataManager().getPlayerData(player.getUniqueId())
                .thenAccept(data -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (targetMenu.equals("set-birthday")) {
                            plugin.getGuiManager().openGui(player, 
                                    new SetBirthdayGui(plugin, player, data));
                        } else {
                            plugin.getGuiManager().openGui(player, 
                                    new ConfigurableGui(plugin, player, targetMenu, data));
                        }
                    });
                });
    }

    protected void handleClaim() {
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
                Bukkit.getScheduler().runTask(plugin, () ->
                        plugin.getMessageManager().sendRewardSuccess(player));
            }
        });
    }

    private String getZodiac(LocalDate date) {
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();
        
        if ((month == 3 && day >= 21) || (month == 4 && day <= 19)) return "白羊座 ♈";
        if ((month == 4 && day >= 20) || (month == 5 && day <= 20)) return "金牛座 ♉";
        if ((month == 5 && day >= 21) || (month == 6 && day <= 21)) return "双子座 ♊";
        if ((month == 6 && day >= 22) || (month == 7 && day <= 22)) return "巨蟹座 ♋";
        if ((month == 7 && day >= 23) || (month == 8 && day <= 22)) return "狮子座 ♌";
        if ((month == 8 && day >= 23) || (month == 9 && day <= 22)) return "处女座 ♍";
        if ((month == 9 && day >= 23) || (month == 10 && day <= 23)) return "天秤座 ♎";
        if ((month == 10 && day >= 24) || (month == 11 && day <= 22)) return "天蝎座 ♏";
        if ((month == 11 && day >= 23) || (month == 12 && day <= 21)) return "射手座 ♐";
        if ((month == 12 && day >= 22) || (month == 1 && day <= 19)) return "摩羯座 ♑";
        if ((month == 1 && day >= 20) || (month == 2 && day <= 18)) return "水瓶座 ♒";
        return "双鱼座 ♓";
    }
}
