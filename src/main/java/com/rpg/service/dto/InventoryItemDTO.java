package com.rpg.service.dto;

import com.rpg.model.items.ObiectEchipament;

/**
 * DTO pentru itemuri din inventar
 */
public class InventoryItemDTO {

    public enum ItemType {
        EQUIPMENT,              // Echipament în inventar
        EQUIPMENT_EQUIPPED,     // Echipament echipat
        HEALING_POTION,         // Poțiune vindecare
        BUFF_POTION,            // Poțiune buff
        ENCHANT_SCROLL,         // Scroll enhancement
        SPECIAL                 // Iteme speciale (flask, shaorma)
    }

    private final String id;
    private final String name;
    private final String description;
    private final ItemType type;
    private final int quantity;
    private final Object data; // Poate fi ObiectEchipament, BuffType, Integer etc.

    public InventoryItemDTO(String id, String name, String description,
                            ItemType type, int quantity, Object data) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.quantity = quantity;
        this.data = data;
    }

    // Constructor pentru echipament
    public InventoryItemDTO(ObiectEchipament equipment, String name,
                            String description, ItemType type, int quantity, Object data) {
        this.id = "equipment_" + equipment.hashCode();
        this.name = name;
        this.description = description;
        this.type = type;
        this.quantity = quantity;
        this.data = data;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ItemType getType() { return type; }
    public int getQuantity() { return quantity; }
    public Object getData() { return data; }

    public boolean isEquipment() {
        return type == ItemType.EQUIPMENT || type == ItemType.EQUIPMENT_EQUIPPED;
    }

    public boolean isEquipped() {
        return type == ItemType.EQUIPMENT_EQUIPPED;
    }

    public boolean isConsumable() {
        return type == ItemType.HEALING_POTION ||
                type == ItemType.BUFF_POTION;
    }

    public ObiectEchipament getEquipment() {
        if (isEquipment() && data instanceof ObiectEchipament) {
            return (ObiectEchipament) data;
        }
        return null;
    }

    @Override
    public String toString() {
        return name + (quantity > 1 ? " (x" + quantity + ")" : "");
    }
}

