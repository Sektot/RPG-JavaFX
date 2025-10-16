package com.rpg.model.items;

import java.io.Serializable;

/**
 * Flask Piece - material pentru upgrade poțiuni.
 * Drop din boss-i cu șansă mare.
 */
public class FlaskPiece implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nume;
    private FlaskType type;
    private int quantity;

    public enum FlaskType {
        HEALTH("Health Flask Piece", "🧪", "Pentru upgrade health potions"),
        MANA("Mana Flask Piece", "💙", "Pentru upgrade mana potions"),
        UNIVERSAL("Universal Flask Piece", "✨", "Pentru orice tip de poțiune");

        private final String displayName;
        private final String icon;
        private final String description;

        FlaskType(String displayName, String icon, String description) {
            this.displayName = displayName;
            this.icon = icon;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getIcon() { return icon; }
        public String getDescription() { return description; }
    }

    public FlaskPiece(FlaskType type, int quantity) {
        this.type = type;
        this.quantity = quantity;
        this.nume = type.getDisplayName();
    }

    // Getteri și setteri
    public String getNume() { return nume; }
    public FlaskType getType() { return type; }
//    public int getQuantity() { return quantity; }
//    public void setQuantity(int quantity) { this.quantity = Math.max(0, quantity); }
//
//    public void addQuantity(int amount) {
//        this.quantity += Math.max(0, amount);
//    }
//
//    public boolean consumeQuantity(int amount) {
//        if (quantity >= amount) {
//            quantity -= amount;
//            return true;
//        }
//        return false;
//    }

    @Override
    public String toString() {
        return String.format("%s %s x%d - %s",
                type.getIcon(), type.getDisplayName(), quantity, type.getDescription());
    }
}