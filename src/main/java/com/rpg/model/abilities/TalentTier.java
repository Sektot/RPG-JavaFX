package com.rpg.model.abilities;

/**
 * Enum that defines the three tiers of talents for each ability.
 * Each tier offers different choices for customizing an ability.
 */
public enum TalentTier {
    TIER_1(1, "Tier 1"),
    TIER_2(2, "Tier 2"),
    TIER_3(3, "Tier 3");

    private final int level;
    private final String displayName;

    TalentTier(int level, String displayName) {
        this.level = level;
        this.displayName = displayName;
    }

    public int getLevel() {
        return level;
    }

    public String getDisplayName() {
        return displayName;
    }
}
