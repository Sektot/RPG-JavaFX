package com.rpg.model.enemies;

import java.io.Serializable;

/**
 * Enemy tier system - determines difficulty and affix count.
 * Inspired by Diablo's color-coded enemy types.
 */
public enum EnemyTier implements Serializable {
    // 🔧 REBALANCED: Reduced HP multipliers, added damage multipliers (less HP sponge, more dangerous)
    NORMAL("Normal", "⚪", 1.0, 1.0, 0, 1.0),           // White - basic enemy
    ELITE("Elite", "🔵", 1.4, 1.2, 1, 1.5),            // Blue - 1 affix (was 1.5x HP)
    CHAMPION("Champion", "🟡", 1.8, 1.4, 2, 2.0),      // Yellow - 2 affixes (was 2.0x HP)
    BOSS("Boss", "🔴", 2.0, 1.5, 3, 3.0),              // Red - 3 affixes (was 3.0x HP)
    LEGENDARY("Legendary", "🟣", 2.5, 1.6, 4, 5.0);    // Purple - 4 affixes (was 4.0x HP)

    private final String displayName;
    private final String icon;
    private final double healthMultiplier;
    private final double damageMultiplier;  // NEW: Elite enemies hit harder
    private final int maxAffixes;
    private final double rewardMultiplier;

    EnemyTier(String displayName, String icon, double healthMultiplier, double damageMultiplier, int maxAffixes, double rewardMultiplier) {
        this.displayName = displayName;
        this.icon = icon;
        this.healthMultiplier = healthMultiplier;
        this.damageMultiplier = damageMultiplier;
        this.maxAffixes = maxAffixes;
        this.rewardMultiplier = rewardMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public double getHealthMultiplier() {
        return healthMultiplier;
    }

    public double getDamageMultiplier() {
        return damageMultiplier;
    }

    public int getMaxAffixes() {
        return maxAffixes;
    }

    public double getRewardMultiplier() {
        return rewardMultiplier;
    }

    /**
     * Gets the color code for UI display.
     */
    public String getColorCode() {
        return switch (this) {
            case NORMAL -> "#FFFFFF";      // White
            case ELITE -> "#4A90E2";       // Blue
            case CHAMPION -> "#FFD700";    // Gold
            case BOSS -> "#E74C3C";        // Red
            case LEGENDARY -> "#9B59B6";   // Purple
        };
    }

    /**
     * Returns formatted tier name with icon.
     */
    public String getFormattedName() {
        return icon + " " + displayName;
    }
}
