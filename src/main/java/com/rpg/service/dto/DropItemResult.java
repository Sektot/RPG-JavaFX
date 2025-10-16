package com.rpg.service.dto;

/**
 * Rezultatul aruncării unui item
 */
public class DropItemResult {
    private final boolean success;
    private final String message;

    public DropItemResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
