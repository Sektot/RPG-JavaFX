package com.rpg.dungeon.model;

/**
 * Tipuri de camere în dungeon
 */
public enum RoomType {
    START("Start", "🚪", "Camera de start"),
    COMBAT("Combat", "⚔️", "Camera cu inamici"),
    EVENT("Event", "📜", "Camera cu eveniment"),
    TREASURE("Treasure", "💎", "Camera cu comori"),
    SHOP("Shop", "🛒", "Camera cu magazin"),
    REST("Rest", "🔥", "Camera de odihnă"),
    SHRINE("Shrine", "⛩️", "Camera cu altar magic"),
    BOSS("Boss", "👹", "Camera cu boss"),
    EMPTY("Empty", "　", "Camera goală");

    private final String name;
    private final String icon;
    private final String description;

    RoomType(String name, String icon, String description) {
        this.name = name;
        this.icon = icon;
        this.description = description;
    }

    public String getName() { return name; }
    public String getIcon() { return icon; }
    public String getDescription() { return description; }
}
