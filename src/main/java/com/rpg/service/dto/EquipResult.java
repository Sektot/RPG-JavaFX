package com.rpg.service.dto;

import com.rpg.model.items.ObiectEchipament;

/**
 * Rezultatul echipării/deechipării unui item
 */
public class EquipResult {
    private final boolean success;
    private final String message;
    private final ObiectEchipament replacedItem; // Itemul deechipat anterior (dacă există)

    public EquipResult(boolean success, String message, ObiectEchipament replacedItem) {
        this.success = success;
        this.message = message;
        this.replacedItem = replacedItem;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public ObiectEchipament getPreviousItem() {
        return replacedItem;
    }

    public boolean hasPreviousItem() {
        return replacedItem != null;
    }
}
