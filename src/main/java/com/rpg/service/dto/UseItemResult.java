package com.rpg.service.dto;

/**
 * Rezultatul folosirii unui item consumabil
 */
public class UseItemResult {
    private final boolean success;
    private final String message;
    private final int effectValue; // Valoarea efectului (HP vindecat, damage bonus, etc.)

    public UseItemResult(boolean success, String message, int effectValue) {
        this.success = success;
        this.message = message;
        this.effectValue = effectValue;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getEffectValue() {
        return effectValue;
    }
}
