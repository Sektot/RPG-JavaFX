package com.rpg.service.dto;

/**
 * Rezultatul unei operațiuni de save/load
 */
public class SaveResult {
    private final boolean success;
    private final String message;
    private final String fileName;

    public SaveResult(boolean success, String message, String fileName) {
        this.success = success;
        this.message = message;
        this.fileName = fileName;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getFileName() {
        return fileName;
    }
}
