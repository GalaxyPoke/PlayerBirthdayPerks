package com.birthdayperks.model;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private String playerName;
    private LocalDate birthDate;  // 完整出生日期（年月日）
    private int lastClaimYear;
    private LocalDate lastClaimDate;
    private int modifyCountThisYear;
    private int lastModifyYear;
    private LocalDate avatarFrameExpiry;
    private LocalDate createdAt;
    private LocalDate updatedAt;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.lastClaimYear = 0;
        this.modifyCountThisYear = 0;
        this.lastModifyYear = 0;
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
    }

    public PlayerData(UUID uuid, String playerName, LocalDate birthDate, 
                      int lastClaimYear, LocalDate lastClaimDate,
                      int modifyCountThisYear, int lastModifyYear,
                      LocalDate avatarFrameExpiry,
                      LocalDate createdAt, LocalDate updatedAt) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.birthDate = birthDate;
        this.lastClaimYear = lastClaimYear;
        this.lastClaimDate = lastClaimDate;
        this.modifyCountThisYear = modifyCountThisYear;
        this.lastModifyYear = lastModifyYear;
        this.avatarFrameExpiry = avatarFrameExpiry;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public boolean hasBirthdaySet() {
        return birthDate != null;
    }

    public boolean isBirthdayToday() {
        if (birthDate == null) return false;
        MonthDay today = MonthDay.now();
        MonthDay birthday = MonthDay.from(birthDate);
        return birthday.equals(today);
    }

    public boolean isBirthdayInWindow(int windowDays) {
        if (birthDate == null) return false;
        
        LocalDate today = LocalDate.now();
        MonthDay birthdayMD = MonthDay.from(birthDate);
        LocalDate birthdayThisYear = birthdayMD.atYear(today.getYear());
        
        // 如果今年的生日已过，检查是否在窗口期内
        if (birthdayThisYear.isBefore(today)) {
            long daysSince = ChronoUnit.DAYS.between(birthdayThisYear, today);
            return daysSince <= windowDays;
        }
        
        // 生日还没到或就是今天
        return birthdayThisYear.equals(today);
    }

    public long getDaysUntilBirthday() {
        if (birthDate == null) return -1;
        
        LocalDate today = LocalDate.now();
        MonthDay birthdayMD = MonthDay.from(birthDate);
        LocalDate birthdayThisYear = birthdayMD.atYear(today.getYear());
        
        if (birthdayThisYear.isBefore(today) || birthdayThisYear.equals(today)) {
            // 今年生日已过或就是今天，计算到明年
            birthdayThisYear = birthdayMD.atYear(today.getYear() + 1);
        }
        
        return ChronoUnit.DAYS.between(today, birthdayThisYear);
    }

    /**
     * 获取当前年龄
     */
    public int getAge() {
        if (birthDate == null) return -1;
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * 获取生日的MonthDay（月日）
     */
    public MonthDay getBirthdayMonthDay() {
        if (birthDate == null) return null;
        return MonthDay.from(birthDate);
    }

    public boolean hasClaimedThisYear() {
        return lastClaimYear == LocalDate.now().getYear();
    }

    public boolean canModifyBirthday(boolean allowModify, int modifyLimit) {
        if (!allowModify && birthDate != null) {
            return false;
        }
        
        if (modifyLimit < 0) {
            return true; // 无限制
        }
        
        int currentYear = LocalDate.now().getYear();
        if (lastModifyYear != currentYear) {
            return true; // 新的一年，重置计数
        }
        
        return modifyCountThisYear < modifyLimit;
    }

    public void incrementModifyCount() {
        int currentYear = LocalDate.now().getYear();
        if (lastModifyYear != currentYear) {
            modifyCountThisYear = 1;
            lastModifyYear = currentYear;
        } else {
            modifyCountThisYear++;
        }
        this.updatedAt = LocalDate.now();
    }

    public int getRemainingModifyCount(int modifyLimit) {
        if (modifyLimit < 0) return -1;
        
        int currentYear = LocalDate.now().getYear();
        if (lastModifyYear != currentYear) {
            return modifyLimit;
        }
        
        return Math.max(0, modifyLimit - modifyCountThisYear);
    }

    public void markClaimed() {
        this.lastClaimYear = LocalDate.now().getYear();
        this.lastClaimDate = LocalDate.now();
        this.updatedAt = LocalDate.now();
    }

    public boolean hasValidAvatarFrame() {
        if (avatarFrameExpiry == null) return false;
        return !LocalDate.now().isAfter(avatarFrameExpiry);
    }

    // Getters and Setters
    public UUID getUuid() {
        return uuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
        this.updatedAt = LocalDate.now();
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
        this.updatedAt = LocalDate.now();
    }

    /**
     * 兼容旧版本的getBirthday方法
     */
    public MonthDay getBirthday() {
        return getBirthdayMonthDay();
    }

    public int getLastClaimYear() {
        return lastClaimYear;
    }

    public void setLastClaimYear(int lastClaimYear) {
        this.lastClaimYear = lastClaimYear;
        this.updatedAt = LocalDate.now();
    }

    public LocalDate getLastClaimDate() {
        return lastClaimDate;
    }

    public void setLastClaimDate(LocalDate lastClaimDate) {
        this.lastClaimDate = lastClaimDate;
        this.updatedAt = LocalDate.now();
    }

    public int getModifyCountThisYear() {
        return modifyCountThisYear;
    }

    public int getLastModifyYear() {
        return lastModifyYear;
    }

    public LocalDate getAvatarFrameExpiry() {
        return avatarFrameExpiry;
    }

    public void setAvatarFrameExpiry(LocalDate avatarFrameExpiry) {
        this.avatarFrameExpiry = avatarFrameExpiry;
        this.updatedAt = LocalDate.now();
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * 获取下次生日日期
     */
    public LocalDate getNextBirthday() {
        if (birthDate == null) return null;
        LocalDate today = LocalDate.now();
        MonthDay birthdayMD = MonthDay.from(birthDate);
        LocalDate birthdayThisYear = birthdayMD.atYear(today.getYear());
        
        if (birthdayThisYear.isBefore(today) || birthdayThisYear.equals(today)) {
            return birthdayMD.atYear(today.getYear() + 1);
        }
        return birthdayThisYear;
    }

    /**
     * 获取总领取次数
     */
    public int getTotalClaimCount() {
        return lastClaimYear > 0 ? 1 : 0;
    }

    /**
     * 获取首次领取日期
     */
    public LocalDate getFirstClaimDate() {
        return lastClaimDate;
    }

    /**
     * 获取头像框过期日期
     */
    public LocalDate getAvatarFrameExpireDate() {
        return avatarFrameExpiry;
    }
}
