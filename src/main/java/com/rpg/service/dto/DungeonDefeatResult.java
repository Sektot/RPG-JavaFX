package com.rpg.service.dto;

/**
 * Rezultatul unei înfrângeri în dungeon
 */
public class DungeonDefeatResult {
    private final int defeatedAtLevel;
    private final int resetToLevel;
    private final boolean hadCheckpoint;
    private final String message;

    public DungeonDefeatResult(int defeatedAtLevel, int resetToLevel,
                               boolean hadCheckpoint, String message) {
        this.defeatedAtLevel = defeatedAtLevel;
        this.resetToLevel = resetToLevel;
        this.hadCheckpoint = hadCheckpoint;
        this.message = message;
    }

    public int getDefeatedAtLevel() {
        return defeatedAtLevel;
    }

    public int getResetToLevel() {
        return resetToLevel;
    }

    public boolean hadCheckpoint() {
        return hadCheckpoint;
    }

    public String getMessage() {
        return message;
    }
}
