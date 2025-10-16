
package com.rpg.model.items;

/**
 * Enum pentru raritatea obiectelor de echipament.
 */
public enum Raritate {
    COMMON("Common", 1.0),
    UNCOMMON("Uncommon", 1.5),
    RARE("Rare", 2.0),
    EPIC("Epic", 3.0),
    LEGENDARY("Legendary", 5.0);

    private final String displayName;
    private final double multiplier;

    Raritate(String displayName, double multiplier) {
        this.displayName = displayName;
        this.multiplier = multiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getMultiplier() {
        return multiplier;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
