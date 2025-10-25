package com.rpg.dungeon.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Run Item - Item temporar valabil doar pentru dungeonul curent
 * Inspirat din Hades / Slay the Spire
 */
public class RunItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String description;
    private String icon;
    private RunItemRarity rarity;
    private Map<String, Double> statModifiers;
    private boolean isCurse;
    private int stackCount;

    public RunItem(String name, String description, String icon, RunItemRarity rarity) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.rarity = rarity;
        this.statModifiers = new HashMap<>();
        this.isCurse = false;
        this.stackCount = 1;
    }

    /**
     * Adaugă un modificator de stat
     */
    public void addStatModifier(String stat, double value) {
        statModifiers.put(stat, value);
    }

    /**
     * Marchează itemul ca fiind blestem
     */
    public void markAsCurse() {
        this.isCurse = true;
    }

    /**
     * Crește numărul de stack-uri
     */
    public void addStack() {
        this.stackCount++;
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
    public RunItemRarity getRarity() { return rarity; }
    public Map<String, Double> getStatModifiers() { return new HashMap<>(statModifiers); }
    public boolean isCurse() { return isCurse; }
    public int getStackCount() { return stackCount; }

    /**
     * Returnează modificatorul total pentru un stat (înmulțit cu stack-uri)
     */
    public double getTotalModifier(String stat) {
        return statModifiers.getOrDefault(stat, 0.0) * stackCount;
    }

    @Override
    public String toString() {
        String curseMarker = isCurse ? "🔴 " : "";
        String stackMarker = stackCount > 1 ? " x" + stackCount : "";
        return String.format("%s%s %s%s - %s", curseMarker, icon, name, stackMarker, description);
    }

    /**
     * Raritatea run item-urilor
     */
    public enum RunItemRarity {
        COMMON("Common", "⚪"),
        UNCOMMON("Uncommon", "🟢"),
        RARE("Rare", "🔵"),
        LEGENDARY("Legendary", "🟡"),
        CURSED("Cursed", "🔴");

        private final String name;
        private final String icon;

        RunItemRarity(String name, String icon) {
            this.name = name;
            this.icon = icon;
        }

        public String getName() { return name; }
        public String getIcon() { return icon; }
    }
}
