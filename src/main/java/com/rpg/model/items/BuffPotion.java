package com.rpg.model.items;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * BuffPotion - poțiuni care oferă buffuri temporare.
 * FIX: Adăugat metode lipsă în BuffType
 */
public class BuffPotion implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nume;
    private BuffType type;
    private int quantity;
    private int duration; // numărul de lupte
    private Map<String, Double> bonuses;

    public enum BuffType {
        STRENGTH("Strength Potion", "💪", "Crește puterea temporar", Map.of("strength", 5.0)),
        DEXTERITY("Dexterity Potion", "🏃", "Crește agilitatea temporar", Map.of("dexterity", 5.0)),
        INTELLIGENCE("Intelligence Potion", "🧠", "Crește inteligența temporar", Map.of("intelligence", 5.0)),
        DAMAGE("Damage Potion", "⚔️", "Crește damage-ul temporar", Map.of("damage_bonus", 10.0)),
        DEFENSE("Defense Potion", "🛡️", "Crește apărarea temporar", Map.of("defense", 8.0)),
        CRITICAL("Critical Potion", "⚡", "Crește șansa de crit temporar", Map.of("crit_chance", 15.0)),
        SPEED("Speed Potion", "💨", "Crește dodge și hit chance", Map.of("dodge_chance", 12.0, "hit_chance", 10.0)),
        BERSERKER("Berserker Potion", "🔥", "Damage mare dar defense scăzută",
                Map.of("damage_bonus", 20.0, "defense", -5.0)),
        FORTIFICATION("Fortification Potion", "⛰️", "Defense mare dar damage scăzut",
                Map.of("defense", 15.0, "damage_bonus", -8.0)),
        MASTER("Master Potion", "🌟", "Toate statisticile crescute",
                Map.of("strength", 3.0, "dexterity", 3.0, "intelligence", 3.0, "defense", 5.0));

        private final String displayName;
        private final String icon;
        private final String description;
        private final Map<String, Double> bonuses;

        BuffType(String displayName, String icon, String description, Map<String, Double> bonuses) {
            this.displayName = displayName;
            this.icon = icon;
            this.description = description;
            this.bonuses = new HashMap<>(bonuses);
        }

        public String getDisplayName() { return displayName; }
        public String getIcon() { return icon; }
        public String getDescription() { return description; }
        public Map<String, Double> getBonuses() { return new HashMap<>(bonuses); }

        // FIX: Adăugat metodele lipsă care sunt căutate în cod
        public int getDuration() {
            return getDefaultDuration(this);
        }

        public Map<String, Double> getEffect() {
            return getBonuses();
        }
    }

    private static int getDefaultDuration(BuffType type) {
        return switch (type) {
            case BERSERKER, FORTIFICATION -> 2; // Puternice dar scurte
            case MASTER -> 4; // Master potion durează mai mult
            default -> 3; // Durata standard
        };
    }

    public BuffPotion(BuffType type, int quantity, int duration) {
        this.type = type;
        this.quantity = quantity;
        this.duration = duration;
        this.nume = type.getDisplayName();
        this.bonuses = type.getBonuses();
    }

    // Getteri și setteri
    public String getNume() { return nume; }
    public BuffType getType() { return type; }
    public int getQuantity() { return quantity; }
    public int getDuration() { return duration; }
    public Map<String, Double> getBonuses() { return new HashMap<>(bonuses); }

    public void setQuantity(int quantity) { this.quantity = Math.max(0, quantity); }
    public void setDuration(int duration) { this.duration = Math.max(1, duration); }

    public void addQuantity(int amount) {
        this.quantity += Math.max(0, amount);
    }

    public boolean consumeQuantity(int amount) {
        if (quantity >= amount) {
            quantity -= amount;
            return true;
        }
        return false;
    }

    public boolean canUse() {
        return quantity > 0;
    }

    public String getEffectDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("Efecte (").append(duration).append(" lupte): ");
        bonuses.forEach((stat, value) -> {
            String sign = value >= 0 ? "+" : "";
            String unit = stat.contains("chance") ? "%" : "";
            sb.append(sign).append(value.intValue()).append(unit).append(" ").append(stat).append(" ");
        });
        return sb.toString().trim();
    }

    @Override
    public String toString() {
        return String.format("%s %s x%d (%d lupte) - %s",
                type.getIcon(), type.getDisplayName(), quantity, duration, getEffectDescription());
    }
}