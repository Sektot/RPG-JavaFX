package com.rpg.service.dto;

/**
 * Rezultatul începerii dungeonului
 */
public class DungeonStartResult {
    private final boolean success;
    private final String message;
    private final int startLevel;

    public DungeonStartResult(boolean success, String message, int startLevel) {
        this.success = success;
        this.message = message;
        this.startLevel = startLevel;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getStartLevel() {
        return startLevel;
    }
}
