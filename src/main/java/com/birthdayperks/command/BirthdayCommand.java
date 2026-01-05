package com.birthdayperks.command;

import com.birthdayperks.PlayerBirthdayPerks;
import com.birthdayperks.gui.AdminGui;
import com.birthdayperks.gui.ConfigurableGui;
import com.birthdayperks.gui.MainMenuGui;
import com.birthdayperks.manager.ConfigManager;
import com.birthdayperks.manager.MessageManager;
import com.birthdayperks.manager.PlayerDataManager;
import com.birthdayperks.model.PlayerData;
import com.birthdayperks.util.DateUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.util.Map;

public class BirthdayCommand implements CommandExecutor {

    private final PlayerBirthdayPerks plugin;
    private final ConfigManager configManager;
    private final MessageManager messageManager;
    private final PlayerDataManager playerDataManager;

    public BirthdayCommand(PlayerBirthdayPerks plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.messageManager = plugin.getMessageManager();
        this.playerDataManager = plugin.getPlayerDataManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // 无参数时打开GUI（玩家）或显示帮助（控制台）
            return handleOpenGui(sender);
        }

        String subCommand = args[0].toLowerCase();

        return switch (subCommand) {
            case "gui", "menu" -> handleOpenGui(sender);
            case "set" -> handleSet(sender, args);
            case "info" -> handleInfo(sender, args);
            case "claim" -> handleClaim(sender, args);
            case "list" -> handleList(sender, args);
            case "admin" -> handleAdmin(sender, args);
            case "help" -> {
                showHelp(sender);
                yield true;
            }
            default -> {
                messageManager.send(sender, "general.invalid-args");
                yield true;
            }
        };
    }

    private boolean handleOpenGui(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            showHelp(sender);
            return true;
        }

        if (!player.hasPermission("birthday.use")) {
            messageManager.send(sender, "general.no-permission");
            return true;
        }

        playerDataManager.getOrCreatePlayerData(player.getUniqueId(), player.getName())
                .thenAccept(data -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        // 优先使用配置文件加载GUI
                        if (plugin.getMenuManager().hasMenu("main-menu")) {
                            plugin.getGuiManager().openGui(player, 
                                    new ConfigurableGui(plugin, player, "main-menu", data));
                        } else {
                            plugin.getGuiManager().openGui(player, new MainMenuGui(plugin, player, data));
                        }
                    });
                });

        return true;
    }

    private boolean handleSet(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            messageManager.send(sender, "general.player-only");
            return true;
        }

        if (!player.hasPermission("birthday.set")) {
            messageManager.send(sender, "general.no-permission");
            return true;
        }

        // 支持 /pbp set <年> <月> <日> 格式
        if (args.length < 4) {
            messageManager.send(sender, "birthday-set.invalid-format");
            return true;
        }

        int year, month, day;
        try {
            year = Integer.parseInt(args[1]);
            month = Integer.parseInt(args[2]);
            day = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            messageManager.send(sender, "birthday-set.invalid-format");
            return true;
        }

        // 验证年份范围
        int currentYear = LocalDate.now().getYear();
        if (year < 1920 || year > currentYear) {
            messageManager.send(sender, "birthday-set.invalid-date");
            return true;
        }

        if (!DateUtil.isValidDate(month, day)) {
            messageManager.send(sender, "birthday-set.invalid-date");
            return true;
        }

        // 验证完整日期
        LocalDate birthDate;
        try {
            birthDate = LocalDate.of(year, month, day);
            if (birthDate.isAfter(LocalDate.now())) {
                messageManager.send(sender, "birthday-set.invalid-date");
                return true;
            }
        } catch (Exception e) {
            messageManager.send(sender, "birthday-set.invalid-date");
            return true;
        }

        final LocalDate finalBirthDate = birthDate;
        playerDataManager.getOrCreatePlayerData(player.getUniqueId(), player.getName())
                .thenAccept(data -> {
                    boolean allowModify = configManager.isAllowModify();
                    int modifyLimit = configManager.getModifyLimitPerYear();

                    if (!data.canModifyBirthday(allowModify, modifyLimit)) {
                        if (!allowModify && data.hasBirthdaySet()) {
                            Bukkit.getScheduler().runTask(plugin, () ->
                                    messageManager.send(player, "birthday-set.already-set"));
                        } else {
                            Bukkit.getScheduler().runTask(plugin, () ->
                                    messageManager.send(player, "birthday-set.modify-limit"));
                        }
                        return;
                    }

                    data.setBirthDate(finalBirthDate);
                    data.incrementModifyCount();

                    playerDataManager.savePlayerData(data).thenRun(() -> {
                        Bukkit.getScheduler().runTask(plugin, () ->
                                messageManager.send(player, "birthday-set.success",
                                        Map.of("year", String.valueOf(year),
                                                "month", String.valueOf(month),
                                                "day", String.valueOf(day))));
                    });
                });

        return true;
    }

    private boolean handleInfo(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            messageManager.send(sender, "general.player-only");
            return true;
        }

        if (!player.hasPermission("birthday.info")) {
            messageManager.send(sender, "general.no-permission");
            return true;
        }

        playerDataManager.getPlayerData(player.getUniqueId())
                .thenAccept(data -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        messageManager.sendRaw(player, "birthday-info.header");

                        if (data == null || !data.hasBirthdaySet()) {
                            messageManager.sendRaw(player, "birthday-info.not-set");
                        } else {
                            LocalDate birthDate = data.getBirthDate();
                            messageManager.sendRaw(player, "birthday-info.date",
                                    Map.of("year", String.valueOf(birthDate.getYear()),
                                            "month", String.valueOf(birthDate.getMonthValue()),
                                            "day", String.valueOf(birthDate.getDayOfMonth())));
                            
                            // 显示年龄
                            int age = data.getAge();
                            if (age >= 0) {
                                messageManager.sendRaw(player, "birthday-info.age",
                                        Map.of("age", String.valueOf(age)));
                            }

                            if (data.isBirthdayToday()) {
                                messageManager.sendRaw(player, "birthday-info.today");
                            } else {
                                long days = data.getDaysUntilBirthday();
                                messageManager.sendRaw(player, "birthday-info.days-until",
                                        Map.of("days", String.valueOf(days)));
                            }

                            if (data.hasClaimedThisYear()) {
                                messageManager.sendRaw(player, "birthday-info.claimed-this-year");
                            } else if (data.isBirthdayInWindow(configManager.getClaimWindowDays())) {
                                messageManager.sendRaw(player, "birthday-info.not-claimed");
                            } else if (DateUtil.isBirthdayPassedThisYear(data.getBirthdayMonthDay())) {
                                messageManager.sendRaw(player, "birthday-info.claim-expired");
                            }

                            if (configManager.isAllowModify()) {
                                int remaining = data.getRemainingModifyCount(configManager.getModifyLimitPerYear());
                                String countStr = remaining < 0 ? "无限制" : String.valueOf(remaining);
                                messageManager.sendRaw(player, "birthday-info.modify-count",
                                        Map.of("count", countStr));
                            }
                        }

                        messageManager.sendRaw(player, "birthday-info.footer");
                    });
                });

        return true;
    }

    private boolean handleClaim(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            messageManager.send(sender, "general.player-only");
            return true;
        }

        if (!player.hasPermission("birthday.claim")) {
            messageManager.send(sender, "general.no-permission");
            return true;
        }

        playerDataManager.getPlayerData(player.getUniqueId())
                .thenAccept(data -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (data == null || !data.hasBirthdaySet()) {
                            messageManager.send(player, "reward.no-birthday-set");
                            return;
                        }

                        if (!data.isBirthdayInWindow(configManager.getClaimWindowDays())) {
                            messageManager.send(player, "reward.not-birthday");
                            return;
                        }

                        if (data.hasClaimedThisYear()) {
                            messageManager.send(player, "reward.already-claimed");
                            return;
                        }

                        // 检查背包空间
                        int requiredSlots = plugin.getRewardManager().getRewardItemCount();
                        if (!plugin.getRewardManager().hasEnoughInventorySpace(player, requiredSlots)) {
                            messageManager.send(player, "reward.inventory-full");
                            return;
                        }

                        // 发放奖励
                        plugin.getRewardManager().giveRewards(player, data).thenAccept(success -> {
                            if (success) {
                                data.markClaimed();
                                playerDataManager.savePlayerData(data);

                                Bukkit.getScheduler().runTask(plugin, () ->
                                        messageManager.sendRewardSuccess(player));
                            }
                        });
                    });
                });

        return true;
    }

    private boolean handleList(CommandSender sender, String[] args) {
        if (!sender.hasPermission("birthday.list")) {
            messageManager.send(sender, "general.no-permission");
            return true;
        }

        // 默认显示未来7天内过生日的玩家
        int days = 7;
        if (args.length >= 2) {
            try {
                days = Integer.parseInt(args[1]);
                days = Math.min(Math.max(days, 1), 365); // 限制1-365天
            } catch (NumberFormatException ignored) {}
        }

        final int queryDays = days;
        plugin.getDatabase().getUpcomingBirthdays(days).thenAccept(players -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (players.isEmpty()) {
                    messageManager.send(sender, "list.no-upcoming", 
                            Map.of("days", String.valueOf(queryDays)));
                    return;
                }

                messageManager.sendRaw(sender, "list.header", 
                        Map.of("days", String.valueOf(queryDays), "count", String.valueOf(players.size())));

                for (int i = 0; i < Math.min(players.size(), 10); i++) {
                    var data = players.get(i);
                    long daysUntil = data.getDaysUntilBirthday();
                    String status = daysUntil == 0 ? "&a今天生日！" : "&7" + daysUntil + "天后";
                    String date = data.getBirthDate().getMonthValue() + "月" + data.getBirthDate().getDayOfMonth() + "日";
                    
                    messageManager.sendRaw(sender, "list.entry",
                            Map.of("rank", String.valueOf(i + 1),
                                    "player", data.getPlayerName() != null ? data.getPlayerName() : "未知",
                                    "date", date,
                                    "status", status));
                }

                if (players.size() > 10) {
                    messageManager.sendRaw(sender, "list.more",
                            Map.of("count", String.valueOf(players.size() - 10)));
                }

                messageManager.sendRaw(sender, "list.footer");
            });
        });

        return true;
    }

    private boolean handleAdmin(CommandSender sender, String[] args) {
        if (!sender.hasPermission("birthday.admin")) {
            messageManager.send(sender, "general.no-permission");
            return true;
        }

        if (args.length < 2) {
            showAdminHelp(sender);
            return true;
        }

        String adminSubCommand = args[1].toLowerCase();

        return switch (adminSubCommand) {
            case "gui" -> handleAdminGui(sender);
            case "reload" -> handleAdminReload(sender);
            case "reset" -> handleAdminReset(sender, args);
            case "give" -> handleAdminGive(sender, args);
            case "check" -> handleAdminCheck(sender, args);
            default -> {
                showAdminHelp(sender);
                yield true;
            }
        };
    }

    private boolean handleAdminGui(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            messageManager.send(sender, "general.player-only");
            return true;
        }

        plugin.getGuiManager().openGui(player, new AdminGui(plugin, player));
        return true;
    }

    private boolean handleAdminReload(CommandSender sender) {
        if (!sender.hasPermission("birthday.admin.reload")) {
            messageManager.send(sender, "general.no-permission");
            return true;
        }

        plugin.reload();
        messageManager.send(sender, "general.reload-success");
        return true;
    }

    private boolean handleAdminReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("birthday.admin.reset")) {
            messageManager.send(sender, "general.no-permission");
            return true;
        }

        if (args.length < 3) {
            messageManager.send(sender, "general.invalid-args");
            return true;
        }

        String targetName = args[2];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (target.getUniqueId() == null) {
            messageManager.send(sender, "general.player-not-found", Map.of("player", targetName));
            return true;
        }

        playerDataManager.deletePlayerData(target.getUniqueId())
                .thenRun(() -> {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            messageManager.send(sender, "admin.reset-success",
                                    Map.of("player", targetName)));
                });

        return true;
    }

    private boolean handleAdminGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("birthday.admin.give")) {
            messageManager.send(sender, "general.no-permission");
            return true;
        }

        if (args.length < 3) {
            messageManager.send(sender, "general.invalid-args");
            return true;
        }

        String targetName = args[2];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            messageManager.send(sender, "general.player-not-found", Map.of("player", targetName));
            return true;
        }

        playerDataManager.getOrCreatePlayerData(target.getUniqueId(), target.getName())
                .thenAccept(data -> {
                    plugin.getRewardManager().giveRewards(target, data).thenAccept(success -> {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            if (success) {
                                messageManager.send(sender, "admin.give-success",
                                        Map.of("player", targetName));
                            } else {
                                messageManager.send(sender, "admin.give-failed");
                            }
                        });
                    });
                });

        return true;
    }

    private boolean handleAdminCheck(CommandSender sender, String[] args) {
        if (!sender.hasPermission("birthday.admin.check")) {
            messageManager.send(sender, "general.no-permission");
            return true;
        }

        if (args.length < 3) {
            messageManager.send(sender, "general.invalid-args");
            return true;
        }

        String targetName = args[2];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        playerDataManager.getPlayerData(target.getUniqueId())
                .thenAccept(data -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        messageManager.sendRaw(sender, "admin.check.header");
                        messageManager.sendRaw(sender, "admin.check.player",
                                Map.of("player", targetName));

                        if (data == null || !data.hasBirthdaySet()) {
                            messageManager.sendRaw(sender, "admin.check.not-set");
                        } else {
                            LocalDate birthDate = data.getBirthDate();
                            messageManager.sendRaw(sender, "admin.check.date",
                                    Map.of("year", String.valueOf(birthDate.getYear()),
                                            "month", String.valueOf(birthDate.getMonthValue()),
                                            "day", String.valueOf(birthDate.getDayOfMonth())));
                            
                            // 显示年龄
                            int age = data.getAge();
                            if (age >= 0) {
                                messageManager.sendRaw(sender, "admin.check.age",
                                        Map.of("age", String.valueOf(age)));
                            }
                            
                            messageManager.sendRaw(sender, "admin.check.claimed",
                                    Map.of("status", data.hasClaimedThisYear() ? "是" : "否"));

                            if (data.getLastClaimDate() != null) {
                                messageManager.sendRaw(sender, "admin.check.last-claim",
                                        Map.of("date", messageManager.formatDate(data.getLastClaimDate())));
                            }
                        }

                        messageManager.sendRaw(sender, "admin.check.footer");
                    });
                });

        return true;
    }

    private void showHelp(CommandSender sender) {
        messageManager.sendRaw(sender, "help.header");
        messageManager.sendList(sender, "help.commands");

        if (sender.hasPermission("birthday.admin")) {
            messageManager.sendList(sender, "help.admin-commands");
        }

        messageManager.sendRaw(sender, "help.footer");
    }

    private void showAdminHelp(CommandSender sender) {
        messageManager.sendRaw(sender, "help.header");
        messageManager.sendList(sender, "help.admin-commands");
        messageManager.sendRaw(sender, "help.footer");
    }
}
