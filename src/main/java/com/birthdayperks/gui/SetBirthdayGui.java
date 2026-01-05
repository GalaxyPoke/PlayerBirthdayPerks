package com.birthdayperks.gui;

import com.birthdayperks.PlayerBirthdayPerks;
import com.birthdayperks.model.PlayerData;
import com.birthdayperks.util.DateUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDate;
import java.util.Arrays;

public class SetBirthdayGui extends AbstractGui {

    private final PlayerData playerData;
    private int selectedYear = 2000;
    private int selectedMonth = 1;
    private int selectedDay = 1;

    private static final int YEAR_SLOT = 11;
    private static final int MONTH_SLOT = 13;
    private static final int DAY_SLOT = 15;
    private static final int CONFIRM_SLOT = 31;

    public SetBirthdayGui(PlayerBirthdayPerks plugin, Player player, PlayerData playerData) {
        super(plugin, player, "&e&l📅 设置生日", 5);
        this.playerData = playerData;
        
        // 如果已设置，使用当前值
        if (playerData != null && playerData.hasBirthdaySet() && playerData.getBirthDate() != null) {
            this.selectedYear = playerData.getBirthDate().getYear();
            this.selectedMonth = playerData.getBirthDate().getMonthValue();
            this.selectedDay = playerData.getBirthDate().getDayOfMonth();
        } else {
            this.selectedYear = 2000; // 默认年份
        }
        
        initialize();
    }

    @Override
    public void initialize() {
        fillBorder(Material.LIGHT_BLUE_STAINED_GLASS_PANE);

        // 标题
        ItemStack titleItem = createItem(
                Material.NAME_TAG,
                "&6&l设置你的生日",
                Arrays.asList(
                        "&7选择你的出生年月日",
                        "",
                        "&7生日设置后" + (plugin.getConfigManager().isAllowModify() ? "可修改" : "&c不可修改")
                )
        );
        setItem(4, titleItem);

        // 年份选择
        updateYearDisplay();

        // 月份选择
        updateMonthDisplay();

        // 日期选择
        updateDayDisplay();

        // 年份减少按钮 (-10年)
        setItem(YEAR_SLOT - 9, createItem(Material.RED_STAINED_GLASS_PANE, "&c&l◀◀ -10年"), event -> {
            playClickSound();
            selectedYear = Math.max(1920, selectedYear - 10);
            adjustDayIfNeeded();
            updateYearDisplay();
            updateDayDisplay();
            updateConfirmButton();
        });

        // 年份减少按钮 (-1年)
        setItem(YEAR_SLOT - 1, createItem(Material.ORANGE_STAINED_GLASS_PANE, "&6&l◀ -1年"), event -> {
            playClickSound();
            selectedYear = Math.max(1920, selectedYear - 1);
            adjustDayIfNeeded();
            updateYearDisplay();
            updateDayDisplay();
            updateConfirmButton();
        });

        // 年份增加按钮 (+1年)
        setItem(YEAR_SLOT + 1, createItem(Material.LIME_STAINED_GLASS_PANE, "&a&l▶ +1年"), event -> {
            playClickSound();
            int currentYear = LocalDate.now().getYear();
            selectedYear = Math.min(currentYear, selectedYear + 1);
            adjustDayIfNeeded();
            updateYearDisplay();
            updateDayDisplay();
            updateConfirmButton();
        });

        // 年份增加按钮 (+10年)
        setItem(YEAR_SLOT + 9, createItem(Material.GREEN_STAINED_GLASS_PANE, "&2&l▶▶ +10年"), event -> {
            playClickSound();
            int currentYear = LocalDate.now().getYear();
            selectedYear = Math.min(currentYear, selectedYear + 10);
            adjustDayIfNeeded();
            updateYearDisplay();
            updateDayDisplay();
            updateConfirmButton();
        });

        // 月份减少按钮
        setItem(MONTH_SLOT - 1, createItem(Material.RED_STAINED_GLASS_PANE, "&c&l◀ 上一月"), event -> {
            playClickSound();
            selectedMonth = selectedMonth > 1 ? selectedMonth - 1 : 12;
            adjustDayIfNeeded();
            updateMonthDisplay();
            updateDayDisplay();
            updateConfirmButton();
        });

        // 月份增加按钮
        setItem(MONTH_SLOT + 1, createItem(Material.LIME_STAINED_GLASS_PANE, "&a&l▶ 下一月"), event -> {
            playClickSound();
            selectedMonth = selectedMonth < 12 ? selectedMonth + 1 : 1;
            adjustDayIfNeeded();
            updateMonthDisplay();
            updateDayDisplay();
            updateConfirmButton();
        });

        // 日期减少按钮
        setItem(DAY_SLOT - 1, createItem(Material.RED_STAINED_GLASS_PANE, "&c&l◀ 上一日"), event -> {
            playClickSound();
            int maxDay = getMaxDaysInMonth(selectedYear, selectedMonth);
            selectedDay = selectedDay > 1 ? selectedDay - 1 : maxDay;
            updateDayDisplay();
            updateConfirmButton();
        });

        // 日期增加按钮
        setItem(DAY_SLOT + 1, createItem(Material.LIME_STAINED_GLASS_PANE, "&a&l▶ 下一日"), event -> {
            playClickSound();
            int maxDay = getMaxDaysInMonth(selectedYear, selectedMonth);
            selectedDay = selectedDay < maxDay ? selectedDay + 1 : 1;
            updateDayDisplay();
            updateConfirmButton();
        });

        // 确认按钮
        updateConfirmButton();

        // 返回按钮
        ItemStack backItem = createItem(
                Material.ARROW,
                "&7&l← 返回主菜单",
                Arrays.asList("&7点击返回")
        );
        setItem(36, backItem, event -> {
            playClickSound();
            if (plugin.getMenuManager().hasMenu("main-menu")) {
                plugin.getGuiManager().openGui(player, 
                        new ConfigurableGui(plugin, player, "main-menu", playerData));
            } else {
                plugin.getGuiManager().openGui(player, new MainMenuGui(plugin, player, playerData));
            }
        });

        fillEmpty(Material.BLACK_STAINED_GLASS_PANE);
    }

    private void updateYearDisplay() {
        int age = LocalDate.now().getYear() - selectedYear;
        ItemStack yearItem = createItem(
                Material.BOOK,
                "&d&l" + selectedYear + " 年",
                Arrays.asList(
                        "&7当前选择: &d" + selectedYear + "年",
                        "&7年龄: &e" + age + " 岁",
                        "",
                        "&7使用两侧按钮调整"
                )
        );
        setItem(YEAR_SLOT, yearItem);
    }

    private void updateMonthDisplay() {
        ItemStack monthItem = createItem(
                Material.SUNFLOWER,
                "&e&l" + selectedMonth + " 月",
                Arrays.asList(
                        "&7当前选择: &e" + selectedMonth + "月",
                        "",
                        "&7使用两侧按钮调整"
                )
        );
        setItem(MONTH_SLOT, monthItem);
    }

    private void updateDayDisplay() {
        ItemStack dayItem = createItem(
                Material.CLOCK,
                "&b&l" + selectedDay + " 日",
                Arrays.asList(
                        "&7当前选择: &b" + selectedDay + "日",
                        "",
                        "&7使用两侧按钮调整"
                )
        );
        setItem(DAY_SLOT, dayItem);
    }

    private void updateConfirmButton() {
        boolean canSet = canSetBirthday();
        int age = LocalDate.now().getYear() - selectedYear;
        
        ItemStack confirmItem;
        if (canSet) {
            confirmItem = createItem(
                    Material.EMERALD,
                    "&a&l✔ 确认设置",
                    Arrays.asList(
                            "&7将生日设置为:",
                            "&e" + selectedYear + "年" + selectedMonth + "月" + selectedDay + "日",
                            "&7年龄: &e" + age + " 岁",
                            "",
                            "&a▶ 点击确认"
                    )
            );
        } else {
            String reason = getCannotSetReason();
            confirmItem = createItem(
                    Material.GRAY_DYE,
                    "&c&l✖ 无法设置",
                    Arrays.asList(reason)
            );
        }

        setItem(CONFIRM_SLOT, confirmItem, event -> {
            if (!canSet) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                return;
            }

            playClickSound();
            confirmSetBirthday();
        });
    }

    private boolean canSetBirthday() {
        if (playerData == null) return true;
        
        boolean allowModify = plugin.getConfigManager().isAllowModify();
        int modifyLimit = plugin.getConfigManager().getModifyLimitPerYear();
        
        return playerData.canModifyBirthday(allowModify, modifyLimit);
    }

    private String getCannotSetReason() {
        if (playerData != null && playerData.hasBirthdaySet()) {
            if (!plugin.getConfigManager().isAllowModify()) {
                return "&c生日已设置且不允许修改";
            }
            if (playerData.getRemainingModifyCount(plugin.getConfigManager().getModifyLimitPerYear()) <= 0) {
                return "&c今年修改次数已用完";
            }
        }
        return "&c无法设置";
    }

    private void adjustDayIfNeeded() {
        int maxDay = getMaxDaysInMonth(selectedYear, selectedMonth);
        if (selectedDay > maxDay) {
            selectedDay = maxDay;
        }
    }

    private int getMaxDaysInMonth(int year, int month) {
        return java.time.YearMonth.of(year, month).lengthOfMonth();
    }

    private void confirmSetBirthday() {
        LocalDate birthDate = LocalDate.of(selectedYear, selectedMonth, selectedDay);
        
        if (playerData != null) {
            playerData.setBirthDate(birthDate);
            playerData.incrementModifyCount();
            
            plugin.getPlayerDataManager().savePlayerData(playerData).thenRun(() -> {
                org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                    plugin.getMessageManager().send(player, "birthday-set.success",
                            java.util.Map.of("year", String.valueOf(selectedYear),
                                    "month", String.valueOf(selectedMonth),
                                    "day", String.valueOf(selectedDay)));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
                    if (plugin.getMenuManager().hasMenu("main-menu")) {
                        plugin.getGuiManager().openGui(player, 
                                new ConfigurableGui(plugin, player, "main-menu", playerData));
                    } else {
                        plugin.getGuiManager().openGui(player, new MainMenuGui(plugin, player, playerData));
                    }
                });
            });
        }
    }

    private void playClickSound() {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
}
