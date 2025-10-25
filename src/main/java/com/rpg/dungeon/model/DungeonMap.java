package com.rpg.dungeon.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Harta completă a dungeonului
 */
public class DungeonMap implements Serializable {
    private static final long serialVersionUID = 1L;

    private Room[][] grid;
    private int width;
    private int height;
    private Room startRoom;
    private Room bossRoom;
    private Room currentRoom;
    private int depth;
    private List<Room> allRooms;

    public DungeonMap(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.grid = new Room[height][width];
        this.allRooms = new ArrayList<>();
    }

    /**
     * Plasează o cameră în grid
     */
    public void setRoom(int x, int y, Room room) {
        if (isValidPosition(x, y)) {
            grid[y][x] = room;
            allRooms.add(room);
        }
    }

    /**
     * Returnează camera de la poziția dată
     */
    public Room getRoom(int x, int y) {
        if (isValidPosition(x, y)) {
            return grid[y][x];
        }
        return null;
    }

    /**
     * Verifică dacă o poziție este validă în grid
     */
    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /**
     * Mută jucătorul într-o cameră
     */
    public void moveToRoom(Room room) {
        this.currentRoom = room;
        room.markVisited();
    }

    /**
     * Returnează toate camerele adiacente camerei curente
     */
    public List<Room> getAdjacentRooms(Room room) {
        List<Room> adjacent = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            Room neighbor = room.getRoomInDirection(dir);
            if (neighbor != null) {
                adjacent.add(neighbor);
            }
        }
        return adjacent;
    }

    /**
     * Generează o reprezentare ASCII a hărții
     */
    public String getAsciiMap() {
        StringBuilder sb = new StringBuilder();
        sb.append("╔═══════════════════════════════╗\n");
        sb.append("║  🗺️  DUNGEON MAP - DEPTH ").append(depth).append("    ║\n");
        sb.append("╠═══════════════════════════════╣\n");
        sb.append("║                               ║\n");

        for (int y = 0; y < height; y++) {
            sb.append("║  ");
            for (int x = 0; x < width; x++) {
                Room room = grid[y][x];
                if (room == null) {
                    sb.append("   ");
                } else if (room == currentRoom) {
                    sb.append(" 👤 ");
                } else {
                    sb.append(" ").append(room.getDisplayIcon()).append(" ");
                }
            }
            sb.append(" ║\n");
        }

        sb.append("║                               ║\n");
        sb.append("╠═══════════════════════════════╣\n");
        sb.append("║ Legend: 👤=You ⚔️=Combat     ║\n");
        sb.append("║ 📜=Event 💎=Treasure 👹=Boss ║\n");
        sb.append("║ ？=Unexplored ✅=Cleared     ║\n");
        sb.append("╚═══════════════════════════════╝\n");

        return sb.toString();
    }

    // Getters și setters
    public Room[][] getGrid() { return grid; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Room getStartRoom() { return startRoom; }
    public void setStartRoom(Room startRoom) { this.startRoom = startRoom; }
    public Room getBossRoom() { return bossRoom; }
    public void setBossRoom(Room bossRoom) { this.bossRoom = bossRoom; }
    public Room getCurrentRoom() { return currentRoom; }
    public void setCurrentRoom(Room currentRoom) { this.currentRoom = currentRoom; }
    public int getDepth() { return depth; }
    public List<Room> getAllRooms() { return new ArrayList<>(allRooms); }
}
