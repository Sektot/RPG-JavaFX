package com.rpg.dungeon.model;

/**
 * Direcții pentru navigare între camere
 */
public enum Direction {
    NORTH("North", 0, -1, "↑"),
    SOUTH("South", 0, 1, "↓"),
    EAST("East", 1, 0, "→"),
    WEST("West", -1, 0, "←");

    private final String name;
    private final int dx;
    private final int dy;
    private final String arrow;

    Direction(String name, int dx, int dy, String arrow) {
        this.name = name;
        this.dx = dx;
        this.dy = dy;
        this.arrow = arrow;
    }

    public String getName() { return name; }
    public int getDx() { return dx; }
    public int getDy() { return dy; }
    public String getArrow() { return arrow; }

    public Direction opposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
        };
    }
}
