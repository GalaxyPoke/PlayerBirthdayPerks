package com.birthdayperks.command;

import com.birthdayperks.PlayerBirthdayPerks;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BirthdayTabCompleter implements TabCompleter {

    private final PlayerBirthdayPerks plugin;

    private static final List<String> MAIN_COMMANDS = Arrays.asList("gui", "set", "info", "claim", "help", "admin");
    private static final List<String> ADMIN_COMMANDS = Arrays.asList("gui", "reload", "reset", "give", "check");
    private static final List<String> MONTHS = IntStream.rangeClosed(1, 12)
            .mapToObj(String::valueOf)
            .collect(Collectors.toList());
    private static final List<String> YEARS = IntStream.rangeClosed(1950, java.time.LocalDate.now().getYear())
            .mapToObj(String::valueOf)
            .collect(Collectors.toList());

    public BirthdayTabCompleter(PlayerBirthdayPerks plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // 主命令补全
            String input = args[0].toLowerCase();
            for (String cmd : MAIN_COMMANDS) {
                if (cmd.startsWith(input)) {
                    // 检查权限
                    if (cmd.equals("admin") && !sender.hasPermission("birthday.admin")) {
                        continue;
                    }
                    if (cmd.equals("set") && !sender.hasPermission("birthday.set")) {
                        continue;
                    }
                    if (cmd.equals("info") && !sender.hasPermission("birthday.info")) {
                        continue;
                    }
                    if (cmd.equals("claim") && !sender.hasPermission("birthday.claim")) {
                        continue;
                    }
                    completions.add(cmd);
                }
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();

            if (subCommand.equals("set") && sender.hasPermission("birthday.set")) {
                // 年份补全
                String input = args[1].toLowerCase();
                completions.addAll(YEARS.stream()
                        .filter(y -> y.startsWith(input))
                        .collect(Collectors.toList()));
            } else if (subCommand.equals("admin") && sender.hasPermission("birthday.admin")) {
                // 管理员子命令补全
                String input = args[1].toLowerCase();
                for (String cmd : ADMIN_COMMANDS) {
                    if (cmd.startsWith(input)) {
                        completions.add(cmd);
                    }
                }
            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();

            if (subCommand.equals("set") && sender.hasPermission("birthday.set")) {
                // 月份补全
                String input = args[2].toLowerCase();
                completions.addAll(MONTHS.stream()
                        .filter(m -> m.startsWith(input))
                        .collect(Collectors.toList()));
            } else if (subCommand.equals("admin") && sender.hasPermission("birthday.admin")) {
                String adminSubCommand = args[1].toLowerCase();

                if (adminSubCommand.equals("reset") || adminSubCommand.equals("give") || adminSubCommand.equals("check")) {
                    // 玩家名补全
                    String input = args[2].toLowerCase();
                    completions.addAll(Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(input))
                            .collect(Collectors.toList()));
                }
            }
        } else if (args.length == 4) {
            String subCommand = args[0].toLowerCase();

            if (subCommand.equals("set") && sender.hasPermission("birthday.set")) {
                // 日期补全 (根据年份和月份)
                completions.addAll(getDayCompletions(args));
            }
        }

        return completions;
    }

    private List<String> getDayCompletions(String[] args) {
        List<String> days = new ArrayList<>();
        try {
            int year = Integer.parseInt(args[1]);
            int month = Integer.parseInt(args[2]);
            int maxDays = java.time.YearMonth.of(year, month).lengthOfMonth();
            String input = args[3].toLowerCase();

            days.addAll(IntStream.rangeClosed(1, maxDays)
                    .mapToObj(String::valueOf)
                    .filter(d -> d.startsWith(input))
                    .collect(Collectors.toList()));
        } catch (Exception ignored) {
        }
        return days;
    }

    private int getMaxDaysInMonth(int month) {
        return switch (month) {
            case 1, 3, 5, 7, 8, 10, 12 -> 31;
            case 4, 6, 9, 11 -> 30;
            case 2 -> 29;
            default -> 31;
        };
    }
}
