package com.birthdayperks.util;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<gradient:([A-Fa-f0-9]{6}):([A-Fa-f0-9]{6})>(.*?)</gradient>");

    /**
     * 转换颜色代码，支持:
     * - 传统颜色代码 (&a, &b, &c 等)
     * - 十六进制颜色 (&#RRGGBB)
     * - 渐变色 (<gradient:RRGGBB:RRGGBB>文本</gradient>)
     */
    public static String colorize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // 处理渐变色
        text = applyGradient(text);

        // 处理十六进制颜色
        text = applyHexColors(text);

        // 处理传统颜色代码
        text = ChatColor.translateAlternateColorCodes('&', text);

        return text;
    }

    private static String applyHexColors(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder buffer = new StringBuilder();

        while (matcher.find()) {
            String hexColor = matcher.group(1);
            String replacement = ChatColor.of("#" + hexColor).toString();
            matcher.appendReplacement(buffer, replacement);
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String applyGradient(String text) {
        Matcher matcher = GRADIENT_PATTERN.matcher(text);
        StringBuilder buffer = new StringBuilder();

        while (matcher.find()) {
            String startHex = matcher.group(1);
            String endHex = matcher.group(2);
            String content = matcher.group(3);

            String gradientText = createGradient(content, startHex, endHex);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(gradientText));
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String createGradient(String text, String startHex, String endHex) {
        if (text.isEmpty()) {
            return text;
        }

        int[] startRGB = hexToRGB(startHex);
        int[] endRGB = hexToRGB(endHex);

        StringBuilder result = new StringBuilder();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);

            // 跳过空格
            if (c == ' ') {
                result.append(c);
                continue;
            }

            float ratio = length > 1 ? (float) i / (length - 1) : 0;

            int r = (int) (startRGB[0] + ratio * (endRGB[0] - startRGB[0]));
            int g = (int) (startRGB[1] + ratio * (endRGB[1] - startRGB[1]));
            int b = (int) (startRGB[2] + ratio * (endRGB[2] - startRGB[2]));

            String hex = String.format("#%02x%02x%02x", r, g, b);
            result.append(ChatColor.of(hex)).append(c);
        }

        return result.toString();
    }

    private static int[] hexToRGB(String hex) {
        return new int[]{
                Integer.parseInt(hex.substring(0, 2), 16),
                Integer.parseInt(hex.substring(2, 4), 16),
                Integer.parseInt(hex.substring(4, 6), 16)
        };
    }

    /**
     * 移除所有颜色代码
     */
    public static String stripColor(String text) {
        if (text == null) {
            return null;
        }
        return ChatColor.stripColor(colorize(text));
    }
}
