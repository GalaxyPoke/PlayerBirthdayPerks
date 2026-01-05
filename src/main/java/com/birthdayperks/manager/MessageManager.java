package com.birthdayperks.manager;

import com.birthdayperks.PlayerBirthdayPerks;
import com.birthdayperks.util.ColorUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MessageManager {

    private final PlayerBirthdayPerks plugin;
    private FileConfiguration messages;
    private String prefix;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public MessageManager(PlayerBirthdayPerks plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    public void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");

        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        messages = YamlConfiguration.loadConfiguration(messagesFile);

        // 合并默认配置
        InputStream defaultStream = plugin.getResource("messages.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            messages.setDefaults(defaultConfig);
        }

        this.prefix = ColorUtil.colorize(messages.getString("prefix", "&6[生日福利] &r"));
    }

    public void reload() {
        loadMessages();
    }

    public String getMessage(String path) {
        return ColorUtil.colorize(messages.getString(path, "&cMissing message: " + path));
    }

    public String getMessage(String path, Map<String, String> placeholders) {
        String message = getMessage(path);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return message;
    }

    public List<String> getMessageList(String path) {
        return messages.getStringList(path).stream()
                .map(ColorUtil::colorize)
                .collect(Collectors.toList());
    }

    public List<String> getMessageList(String path, Map<String, String> placeholders) {
        return messages.getStringList(path).stream()
                .map(line -> {
                    String colorized = ColorUtil.colorize(line);
                    for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                        colorized = colorized.replace("%" + entry.getKey() + "%", entry.getValue());
                    }
                    return colorized;
                })
                .collect(Collectors.toList());
    }

    public void send(CommandSender sender, String path) {
        sender.sendMessage(prefix + getMessage(path));
    }

    public void send(CommandSender sender, String path, Map<String, String> placeholders) {
        sender.sendMessage(prefix + getMessage(path, placeholders));
    }

    public void sendRaw(CommandSender sender, String path) {
        sender.sendMessage(getMessage(path));
    }

    public void sendRaw(CommandSender sender, String path, Map<String, String> placeholders) {
        sender.sendMessage(getMessage(path, placeholders));
    }

    public void sendList(CommandSender sender, String path) {
        getMessageList(path).forEach(sender::sendMessage);
    }

    public void sendList(CommandSender sender, String path, Map<String, String> placeholders) {
        getMessageList(path, placeholders).forEach(sender::sendMessage);
    }

    public void sendBirthdayReminder(Player player) {
        getMessageList("reminder.login").forEach(player::sendMessage);
    }

    public void sendUpcomingBirthdayReminder(Player player, long days) {
        player.sendMessage(prefix + getMessage("reminder.upcoming", Map.of("days", String.valueOf(days))));
    }

    public void sendRewardSuccess(Player player) {
        getMessageList("reward.success").forEach(player::sendMessage);
    }

    public String getPrefix() {
        return prefix;
    }

    public String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "N/A";
    }
}
