package com.rpg.service.dto;

/**
 * Rezultatul unei achiziții din shop
 */
public class PurchaseResult {
    private final boolean success;
    private final String message;
    private final int goldSpent;

    public PurchaseResult(boolean success, String message, int goldSpent) {
        this.success = success;
        this.message = message;
        this.goldSpent = goldSpent;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public int getGoldSpent() { return goldSpent; }
}
