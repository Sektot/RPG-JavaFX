package com.rpg.service.dto;

import com.rpg.service.TrainerSmithServiceFX;
import com.rpg.service.TrainerSmithServiceFX.StatType;

/**
 * Rezultatul alocării unui punct de stat gratuit
 */
public class AllocationResultDTO {
    private final boolean success;
    private final String message;
    private final StatType statType;
    private final int newValue;

    public AllocationResultDTO(boolean success, String message,
                               StatType statType, int newValue) {
        this.success = success;
        this.message = message;
        this.statType = statType;
        this.newValue = newValue;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public StatType getStatType() {
        return statType;
    }

    public int getNewValue() {
        return newValue;
    }
}
