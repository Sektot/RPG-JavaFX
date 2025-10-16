package com.rpg.service.dto;

import com.rpg.service.TrainerSmithServiceFX;

/**
 * Rezultatul antrenamentului (costă gold)
 */
public class TrainingResultDTO {
    private final boolean success;
    private final String message;
    private final TrainerSmithServiceFX.StatType statType;
    private final int newValue;
    private final int goldSpent;

    public TrainingResultDTO(boolean success, String message, TrainerSmithServiceFX.StatType statType,
                             int newValue, int goldSpent) {
        this.success = success;
        this.message = message;
        this.statType = statType;
        this.newValue = newValue;
        this.goldSpent = goldSpent;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public TrainerSmithServiceFX.StatType getStatType() {
        return statType;
    }

    public int getNewValue() {
        return newValue;
    }

    public int getGoldSpent() {
        return goldSpent;
    }
}
