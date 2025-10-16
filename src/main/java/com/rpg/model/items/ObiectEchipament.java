package com.rpg.model.items;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static com.rpg.model.items.ObiectEchipament.TipEchipament.WEAPON;

/**
 * Reprezintă un obiect de echipament.
 * VERSIUNEA FINALĂ cu TOATE constructorii și metodele necesare.
 */
public class ObiectEchipament implements Serializable {
    private static final long serialVersionUID = 1L;

    // ================== ENUM-URI NECESARE ==================

    /**
     * Enum pentru raritatea obiectelor.
     */
    public enum Raritate {
        COMMON("Common", 1.0),
        UNCOMMON("Uncommon", 1.5),
        RARE("Rare", 2.0),
        EPIC("Epic", 3.0),
        LEGENDARY("Legendary", 5.0);

        private final String displayName;
        private final double multiplier;

        Raritate(String displayName, double multiplier) {
            this.displayName = displayName;
            this.multiplier = multiplier;
        }

        public String getDisplayName() {
            return displayName;
        }

        public double getMultiplier() {
            return multiplier;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    /**
     * Enum pentru tipurile de echipament.
     */
    public enum TipEchipament {
        WEAPON("Weapon", "⚔️"),
        ARMOR("Armor", "🛡️"),
        HELMET("Helmet", "⛑️"),
        BOOTS("Boots", "🥾"),
        GLOVES("Gloves", "🧤"),
        RING("Ring", "💍"),
        NECKLACE("Necklace", "📿"),
        SHIELD("Shield", "🛡️");



        private final String displayName;
        private final String icon;

        TipEchipament(String displayName, String icon) {
            this.displayName = displayName;
            this.icon = icon;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getIcon() {
            return icon;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }



    // ================== CAMPURI ==================

    private String nume;
    private int nivelNecesar;
    private Raritate raritate;
    private TipEchipament tip;
    private boolean equipped = false;
    private Map<String, Integer> bonuses = new HashMap<>();
    private int duritate = 100;
    private int pret;
    private int enhancementLevel = 0;

    private Map<String, Integer> enhancementBonuses = new HashMap<>(); // Bonusuri din enhancement

    // ================== CONSTRUCTORI ==================

    /**
     * Constructor COMPLET pentru ShopService.
     */
    public ObiectEchipament(String nume, int nivelNecesar, Raritate raritate, TipEchipament tip,
                            int strengthBonus, int dexterityBonus, int intelligenceBonus,
                            int defenseBonus, int pret) {
        this.nume = nume;
        this.nivelNecesar = nivelNecesar;
        this.raritate = raritate;
        this.tip = tip;
        this.pret = pret;
        this.bonuses = new HashMap<>();

        if (strengthBonus > 0) bonuses.put("strength", strengthBonus);
        if (dexterityBonus > 0) bonuses.put("dexterity", dexterityBonus);
        if (intelligenceBonus > 0) bonuses.put("intelligence", intelligenceBonus);
        if (defenseBonus > 0) bonuses.put("defense", defenseBonus);
    }


    /**
     * Calculează prețul default bazat pe nivel și raritate.
     */
    private int calculateDefaultPrice() {
        int basePrice = nivelNecesar * 10 + 20;
        return (int)(basePrice * (raritate != null ? raritate.getMultiplier() : 1.0));
    }

    // ================== METODE NECESARE PENTRU SHOPSERVICE ==================

    public int getPret() {
        return pret;
    }

    public void setPret(int pret) {
        this.pret = Math.max(0, pret);
    }

    public TipEchipament getTip() {
        return tip;
    }

    public void setTip(TipEchipament tip) {
        this.tip = tip;
    }

    // Modifică getStrengthBonus() să folosească bonusurile totale:
    public int getStrengthBonus() {
        return getTotalBonuses().getOrDefault("strength", 0);
    }

    // Similar pentru celelalte bonusuri:
    public int getDexterityBonus() {
        return getTotalBonuses().getOrDefault("dexterity", 0);
    }

    public int getIntelligenceBonus() {
        return getTotalBonuses().getOrDefault("intelligence", 0);
    }

    public int getDefenseBonus() {
        return getTotalBonuses().getOrDefault("defense", 0);
    }
    // ================== METODE PENTRU LOOT GENERATOR ==================

    public void setBonuses(Map<String, Integer> bonuses) {
        this.bonuses = new HashMap<>(bonuses);
    }

    public void increaseLevel(int amount) {
        this.nivelNecesar += amount;
        // Îmbunătățește și bonusurile
        for (Map.Entry<String, Integer> entry : bonuses.entrySet()) {
            bonuses.put(entry.getKey(), entry.getValue() + 1);
        }
        this.pret = calculateDefaultPrice();
    }

    // ================== METODE PENTRU ENHANCEMENT SYSTEM ==================

    /**
     * Crește nivelul de enhancement al obiectului.
     */
    public void enhanceEquipment(int levels) {
        this.enhancementLevel += Math.max(0, levels);
        updateEnhancementBonuses();
        updateItemName();
    }

    /**
     * Returnează nivelul de enhancement actual.
     */
    public int getEnhancementLevel() {
        return enhancementLevel;
    }

    /**
     * Setează nivelul de enhancement (pentru loading).
     */
    public void setEnhancementLevel(int level) {
        this.enhancementLevel = Math.max(0, level);
        updateEnhancementBonuses();
        updateItemName();
    }

    /**
     * Calculează bonusurile de enhancement bazate pe nivelul actual.
     */
    private void updateEnhancementBonuses() {
        enhancementBonuses.clear();

        if (enhancementLevel > 0) {
            // Calculează bonusurile bazate pe enhancement level și raritate
            double enhancementMultiplier = enhancementLevel * getRarityEnhancementMultiplier();

            // Crește toate bonusurile existente
            bonuses.forEach((stat, baseValue) -> {
                int enhancementBonus = (int) (baseValue * enhancementMultiplier * 0.1); // 10% per nivel
                if (enhancementBonus > 0) {
                    enhancementBonuses.put(stat, enhancementBonus);
                }
            });

            // Adaugă bonusuri speciale la anumite nivele
            if (enhancementLevel >= 3) {
                enhancementBonuses.put("damage_bonus", enhancementLevel * 2);
            }
            if (enhancementLevel >= 5) {
                enhancementBonuses.put("crit_chance", enhancementLevel);
            }
        }
    }

    /**
     * Returnează multiplicatorul de enhancement bazat pe raritate.
     */
    private double getRarityEnhancementMultiplier() {
        return switch (raritate) {
            case COMMON -> 1.0;
            case UNCOMMON -> 1.2;
            case RARE -> 1.5;
            case EPIC -> 2.0;
            case LEGENDARY -> 3.0;
        };
    }

    /**
     * Actualizează numele obiectului să includă enhancement level.
     */
    private void updateItemName() {
        // Înlătură enhancement-ul anterior din nume dacă există
        String baseName = nume.replaceAll("\\s*\\+\\d+$", "");

        if (enhancementLevel > 0) {
            this.nume = baseName + " +" + enhancementLevel;
        } else {
            this.nume = baseName;
        }
    }

    /**
     * Returnează bonusurile totale (de bază + enhancement).
     */
    public Map<String, Integer> getTotalBonuses() {
        Map<String, Integer> totalBonuses = new HashMap<>(bonuses);

        // Adaugă bonusurile de enhancement
        enhancementBonuses.forEach((stat, enhancementBonus) ->
                totalBonuses.merge(stat, enhancementBonus, Integer::sum));

        return totalBonuses;
    }

    /**
     * Returnează bonusul de atac al weapon-ului (pentru arme).
     */
    public int getAttackBonus() {
        if (tip != WEAPON) {
            return 0; // Doar armele au attack bonus
        }
        return getTotalBonuses().getOrDefault("attack_bonus", 0) +
                getTotalBonuses().getOrDefault("damage_bonus", 0);
    }


    /**
     * Verifică dacă weapon-ul are enchantment de un anumit tip.
     */
    public boolean hasEnchantment(String enchantType) {
        return getTotalBonuses().containsKey("enchant_" + enchantType.toLowerCase()) &&
                getTotalBonuses().get("enchant_" + enchantType.toLowerCase()) > 0;
    }

    /**
     * Returnează damage-ul enchantment-ului de un anumit tip.
     */
    public int getEnchantmentDamage(String enchantType) {
        return getTotalBonuses().getOrDefault("enchant_" + enchantType.toLowerCase(), 0);
    }

    /**
     * Returnează toate enchantment-urile active pe weapon.
     */
    public Map<String, Integer> getAllEnchantments() {
        Map<String, Integer> enchantments = new HashMap<>();
        getTotalBonuses().forEach((key, value) -> {
            if (key.startsWith("enchant_") && value > 0) {
                String enchantType = key.substring(8); // Remove "enchant_" prefix
                enchantments.put(enchantType, value);
            }
        });
        return enchantments;
    }



    /**
     * Returnează doar bonusurile de enhancement.
     */
    public Map<String, Integer> getEnhancementBonuses() {
        return new HashMap<>(enhancementBonuses);
    }

    /**
     * Calculează costul pentru următorul nivel de enhancement.
     */
    public int getNextEnhancementCost() {
        int baseCost = switch (raritate) {
            case COMMON -> 10;
            case UNCOMMON -> 15;
            case RARE -> 25;
            case EPIC -> 40;
            case LEGENDARY -> 60;
        };

        // Costul crește exponențial cu nivelul
        int levelMultiplier = (int) Math.pow(2, enhancementLevel);
        return baseCost * levelMultiplier;
    }

    /**
     * Verifică dacă obiectul poate fi enhanced.
     */
    public boolean canBeEnhanced() {
        return enhancementLevel < getMaxEnhancementLevel();
    }

    /**
     * Returnează nivelul maxim de enhancement.
     */
    public int getMaxEnhancementLevel() {
        return switch (raritate) {
            case COMMON -> 5;
            case UNCOMMON -> 7;
            case RARE -> 10;
            case EPIC -> 12;
            case LEGENDARY -> 15;
        };
    }




    // ================== METODE PENTRU BATTLE SYSTEM ==================

    // Adaugă această metodă în clasa ObiectEchipament:
// FIX: Alias pentru getNivelNecesar()
    public int getNivel() {
        return getNivelNecesar();
    }

    public boolean isEquipped() {
        return equipped;
    }

    public void setEquipped(boolean equipped) {
        this.equipped = equipped;
    }


    // ================== METODE HELPER ==================



    public ObiectEchipament createCopy() {
        ObiectEchipament copy = new ObiectEchipament(nume, nivelNecesar, raritate, tip,
                getStrengthBonus(), getDexterityBonus(), getIntelligenceBonus(), getDefenseBonus(), pret);
        copy.setDuritate(this.duritate);
        return copy;
    }

    // ================== GETTERI ȘI SETTERI ==================

    public String getNume() { return nume; }
    public void setNume(String nume) { this.nume = nume; }

    public int getNivelNecesar() { return nivelNecesar; }
    public void setNivelNecesar(int nivel) { this.nivelNecesar = nivel; }

    public Raritate getRaritate() { return raritate; }
    public void setRaritate(Raritate raritate) { this.raritate = raritate; }

    public int getDuritate() { return duritate; }
    public void setDuritate(int duritate) { this.duritate = Math.max(0, Math.min(100, duritate)); }

    public Map<String, Integer> getBonuses() { return new HashMap<>(bonuses); }

    // ================== METODE OVERRIDE ==================

    @Override
    public String toString() {
        String status = equipped ? " [ECHIPAT]" : "";
        String tipDisplay = tip != null ? tip.getIcon() + tip.getDisplayName() : "Unknown";
        String enhancement = enhancementLevel > 0 ? " (+" + enhancementLevel + ")" : "";

        // Adaugă enchantment display
        StringBuilder enchantDisplay = new StringBuilder();
        Map<String, Integer> enchantments = getAllEnchantments();
        if (!enchantments.isEmpty()) {
            enchantDisplay.append(" [");
            enchantments.forEach((type, damage) -> {
                String icon = getEnchantmentIcon(type);
                enchantDisplay.append(icon).append(type.toUpperCase()).append(" ");
            });
            enchantDisplay.append("]");
        }

        return String.format("%s %s%s%s (Nivel %d, %s, Duritate: %d%%, %d gold)%s",
                tipDisplay, nume, enhancement, enchantDisplay.toString(),
                nivelNecesar, raritate, duritate, pret, status);
    }

    /**
     * Aplică un enchantment pe weapon (folosit de useEnchantScroll din Erou).
     */
    public void applyEnchantment(String enchantType, int damage) {
        if (tip != WEAPON) {
            throw new IllegalArgumentException("Enchantments pot fi aplicate doar pe arme!");
        }

        Map<String, Integer> currentBonuses = new HashMap<>(bonuses);
        String enchantKey = "enchant_" + enchantType.toLowerCase();
        currentBonuses.put(enchantKey, damage);
        setBonuses(currentBonuses);

        // Nu modificăm numele aici - se va face în Erou.useEnchantScroll()
    }

    /**
     * Returnează icon-ul pentru un tip de enchantment.
     */
    private String getEnchantmentIcon(String enchantType) {
        return switch (enchantType.toLowerCase()) {
            case "fire" -> "🔥";
            case "ice" -> "❄️";
            case "lightning" -> "⚡";
            case "poison" -> "☠️";
            case "holy" -> "✨";
            case "shadow" -> "🌑";
            case "arcane" -> "🔮";
            case "nature" -> "🌿";
            default -> "✨";
        };
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ObiectEchipament item = (ObiectEchipament) obj;
        return nume.equals(item.nume) &&
                nivelNecesar == item.nivelNecesar &&
                raritate == item.raritate &&
                tip == item.tip;
    }

    @Override
    public int hashCode() {
        return nume.hashCode() * 31 + nivelNecesar * 7 + raritate.hashCode() + tip.hashCode();
    }


}