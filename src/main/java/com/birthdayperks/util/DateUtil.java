package com.birthdayperks.util;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.YearMonth;

public class DateUtil {

    /**
     * 验证日期是否有效
     */
    public static boolean isValidDate(int month, int day) {
        if (month < 1 || month > 12) {
            return false;
        }

        if (day < 1) {
            return false;
        }

        // 获取该月最大天数
        int maxDays = getMaxDaysInMonth(month);
        return day <= maxDays;
    }

    /**
     * 获取指定月份的最大天数
     */
    public static int getMaxDaysInMonth(int month) {
        // 使用非闰年的天数，2月用28天
        return switch (month) {
            case 1, 3, 5, 7, 8, 10, 12 -> 31;
            case 4, 6, 9, 11 -> 30;
            case 2 -> 29; // 允许2月29日，闰年会正确处理
            default -> 0;
        };
    }

    /**
     * 解析月日字符串
     * 支持格式: "1-15", "1/15", "01-15", "01/15"
     */
    public static MonthDay parseMonthDay(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        String[] parts = input.split("[-/]");
        if (parts.length != 2) {
            return null;
        }

        try {
            int month = Integer.parseInt(parts[0].trim());
            int day = Integer.parseInt(parts[1].trim());

            if (!isValidDate(month, day)) {
                return null;
            }

            return MonthDay.of(month, day);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 格式化MonthDay为中文格式
     */
    public static String formatMonthDayChinese(MonthDay monthDay) {
        if (monthDay == null) {
            return "未设置";
        }
        return monthDay.getMonthValue() + "月" + monthDay.getDayOfMonth() + "日";
    }

    /**
     * 获取下一个生日的日期
     */
    public static LocalDate getNextBirthday(MonthDay birthday) {
        if (birthday == null) {
            return null;
        }

        LocalDate today = LocalDate.now();
        LocalDate birthdayThisYear = birthday.atYear(today.getYear());

        if (birthdayThisYear.isBefore(today) || birthdayThisYear.equals(today)) {
            return birthday.atYear(today.getYear() + 1);
        }

        return birthdayThisYear;
    }

    /**
     * 检查今年的生日是否已过
     */
    public static boolean isBirthdayPassedThisYear(MonthDay birthday) {
        if (birthday == null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        LocalDate birthdayThisYear = birthday.atYear(today.getYear());

        return birthdayThisYear.isBefore(today);
    }

    /**
     * 获取生日的中文星座
     */
    public static String getZodiacSign(MonthDay birthday) {
        if (birthday == null) {
            return "未知";
        }

        int month = birthday.getMonthValue();
        int day = birthday.getDayOfMonth();

        return switch (month) {
            case 1 -> day >= 20 ? "水瓶座" : "摩羯座";
            case 2 -> day >= 19 ? "双鱼座" : "水瓶座";
            case 3 -> day >= 21 ? "白羊座" : "双鱼座";
            case 4 -> day >= 20 ? "金牛座" : "白羊座";
            case 5 -> day >= 21 ? "双子座" : "金牛座";
            case 6 -> day >= 22 ? "巨蟹座" : "双子座";
            case 7 -> day >= 23 ? "狮子座" : "巨蟹座";
            case 8 -> day >= 23 ? "处女座" : "狮子座";
            case 9 -> day >= 23 ? "天秤座" : "处女座";
            case 10 -> day >= 24 ? "天蝎座" : "天秤座";
            case 11 -> day >= 23 ? "射手座" : "天蝎座";
            case 12 -> day >= 22 ? "摩羯座" : "射手座";
            default -> "未知";
        };
    }
}
