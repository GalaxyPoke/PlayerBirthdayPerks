package com.birthdayperks.hook;

import com.birthdayperks.PlayerBirthdayPerks;
import com.birthdayperks.model.PlayerData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final PlayerBirthdayPerks plugin;

    public PlaceholderAPIHook(PlayerBirthdayPerks plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "birthday";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        // 同步获取玩家数据（PAPI需要同步返回）
        PlayerData data = plugin.getPlayerDataManager().getPlayerDataSync(player.getUniqueId());

        switch (params.toLowerCase()) {
            // 生日日期
            case "date":
                if (data == null || !data.hasBirthdaySet() || data.getBirthDate() == null) {
                    return "未设置";
                }
                return data.getBirthDate().format(DateTimeFormatter.ofPattern("MM月dd日"));

            case "date_full":
                if (data == null || !data.hasBirthdaySet() || data.getBirthDate() == null) {
                    return "未设置";
                }
                return data.getBirthDate().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"));

            // 距离生日天数
            case "days_until":
                if (data == null || !data.hasBirthdaySet()) {
                    return "-1";
                }
                return String.valueOf(data.getDaysUntilBirthday());

            // 年龄
            case "age":
                if (data == null || !data.hasBirthdaySet()) {
                    return "0";
                }
                return String.valueOf(data.getAge());

            // 是否今天生日
            case "is_today":
                if (data == null || !data.hasBirthdaySet()) {
                    return "false";
                }
                return String.valueOf(data.isBirthdayToday());

            // 是否已领取今年福利
            case "claimed":
                if (data == null) {
                    return "false";
                }
                return String.valueOf(data.hasClaimedThisYear());

            case "claimed_status":
                if (data == null) {
                    return "未设置";
                }
                return data.hasClaimedThisYear() ? "已领取" : "未领取";

            // 是否有有效头像框
            case "has_frame":
                if (data == null) {
                    return "false";
                }
                return String.valueOf(data.hasValidAvatarFrame());

            // 头像框前缀
            case "frame_prefix":
                if (data == null || !data.hasValidAvatarFrame()) {
                    return "";
                }
                return plugin.getRewardManager().getRewardsConfig().getString("avatar-frame.prefix", "");

            // 星座
            case "zodiac":
                if (data == null || !data.hasBirthdaySet() || data.getBirthDate() == null) {
                    return "未知";
                }
                return getZodiac(data.getBirthDate());

            // 下次生日日期
            case "next_birthday":
                if (data == null || !data.hasBirthdaySet()) {
                    return "未设置";
                }
                LocalDate next = data.getNextBirthday();
                return next != null ? next.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")) : "未知";

            // 是否已设置生日
            case "has_set":
                return String.valueOf(data != null && data.hasBirthdaySet());

            // 生日状态文本
            case "status":
                if (data == null || !data.hasBirthdaySet()) {
                    return "未设置生日";
                }
                if (data.isBirthdayToday()) {
                    return "今天是你的生日！";
                }
                long days = data.getDaysUntilBirthday();
                return "距离生日还有" + days + "天";

            default:
                return null;
        }
    }

    private String getZodiac(LocalDate date) {
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        if ((month == 3 && day >= 21) || (month == 4 && day <= 19)) return "白羊座";
        if ((month == 4 && day >= 20) || (month == 5 && day <= 20)) return "金牛座";
        if ((month == 5 && day >= 21) || (month == 6 && day <= 21)) return "双子座";
        if ((month == 6 && day >= 22) || (month == 7 && day <= 22)) return "巨蟹座";
        if ((month == 7 && day >= 23) || (month == 8 && day <= 22)) return "狮子座";
        if ((month == 8 && day >= 23) || (month == 9 && day <= 22)) return "处女座";
        if ((month == 9 && day >= 23) || (month == 10 && day <= 23)) return "天秤座";
        if ((month == 10 && day >= 24) || (month == 11 && day <= 22)) return "天蝎座";
        if ((month == 11 && day >= 23) || (month == 12 && day <= 21)) return "射手座";
        if ((month == 12 && day >= 22) || (month == 1 && day <= 19)) return "摩羯座";
        if ((month == 1 && day >= 20) || (month == 2 && day <= 18)) return "水瓶座";
        return "双鱼座";
    }
}
