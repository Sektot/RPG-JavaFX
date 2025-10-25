package com.rpg.dungeon.model;

import java.io.Serializable;

/**
 * Represents an interactive object in a dungeon room
 * (Chests, Altars, Fountains, etc.)
 */
public class InteractiveObject implements Serializable {
    private static final long serialVersionUID = 1L;

    private ObjectType type;
    private double x; // Position in room (pixels)
    private double y;
    private double width;
    private double height;
    private boolean interacted; // Has the player already interacted with this?
    private Object data; // Type-specific data (RunItem for chest, DungeonEvent for altar, etc.)

    public InteractiveObject(ObjectType type, double x, double y) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.width = type.getDefaultWidth();
        this.height = type.getDefaultHeight();
        this.interacted = false;
    }

    /**
     * Check if a point is within interaction range of this object
     */
    public boolean isInRange(double px, double py, double range) {
        double centerX = x + width / 2;
        double centerY = y + height / 2;
        double distance = Math.sqrt(Math.pow(px - centerX, 2) + Math.pow(py - centerY, 2));
        return distance <= range;
    }

    /**
     * Check if this object collides with a rectangle
     */
    public boolean collidesWith(double px, double py, double pWidth, double pHeight) {
        return px < x + width &&
               px + pWidth > x &&
               py < y + height &&
               py + pHeight > y;
    }

    // Getters and setters
    public ObjectType getType() { return type; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public boolean isInteracted() { return interacted; }
    public void setInteracted(boolean interacted) { this.interacted = interacted; }
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    /**
     * Types of interactive objects
     */
    public enum ObjectType {
        CHEST("Chest", "📦", 48, 48),
        ALTAR("Altar", "⛩️", 64, 64),
        FOUNTAIN("Fountain", "⛲", 64, 64),
        SHOP_TABLE("Shop Table", "🛒", 96, 64),
        CAMPFIRE("Campfire", "🔥", 48, 48),
        PORTAL("Portal", "🌀", 64, 64),
        STATUE("Statue", "🗿", 48, 64);

        private final String name;
        private final String icon;
        private final double defaultWidth;
        private final double defaultHeight;

        ObjectType(String name, String icon, double width, double height) {
            this.name = name;
            this.icon = icon;
            this.defaultWidth = width;
            this.defaultHeight = height;
        }

        public String getName() { return name; }
        public String getIcon() { return icon; }
        public double getDefaultWidth() { return defaultWidth; }
        public double getDefaultHeight() { return defaultHeight; }
    }
}
