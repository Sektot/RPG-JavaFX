package com.rpg.model.items;

import java.io.Serializable;

/**
 * EnchantScroll - scrolluri care adaugă enchantments la arme.
 * Oferă damage types speciale (fire, ice, lightning, etc.).
 * FIX: Adăugat constructor fără parametri
 */
public class EnchantScroll implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nume;
    private EnchantType type;
    private int quantity;
    private int enchantLevel; // 1-5, crește puterea enchantment-ului

    public enum EnchantType {
        FIRE("Fire Enchant Scroll", "🔥", "Adaugă fire damage", "fire",
                new int[]{5, 8, 12, 18, 25}),
        ICE("Ice Enchant Scroll", "❄️", "Adaugă ice damage și slow", "ice",
                new int[]{4, 7, 10, 15, 22}),
        LIGHTNING("Lightning Enchant Scroll", "⚡", "Adaugă lightning damage și stun chance", "lightning",
                new int[]{6, 9, 13, 19, 28}),
        POISON("Poison Enchant Scroll", "☠️", "Adaugă poison damage over time", "poison",
                new int[]{3, 6, 9, 14, 20}),
        HOLY("Holy Enchant Scroll", "✨", "Adaugă holy damage și healing", "holy",
                new int[]{4, 8, 11, 17, 24}),
        SHADOW("Shadow Enchant Scroll", "🌑", "Adaugă shadow damage și life steal", "shadow",
                new int[]{5, 7, 11, 16, 23}),
        ARCANE("Arcane Enchant Scroll", "🔮", "Adaugă arcane damage și mana burn", "arcane",
                new int[]{6, 10, 15, 22, 32}),
        NATURE("Nature Enchant Scroll", "🌿", "Adaugă nature damage și regeneration", "nature",
                new int[]{4, 6, 10, 14, 21});

        private final String displayName;
        private final String icon;
        private final String description;
        private final String damageType;
        private final int[] damageValues; // damage per level (1-5)

        EnchantType(String displayName, String icon, String description,
                    String damageType, int[] damageValues) {
            this.displayName = displayName;
            this.icon = icon;
            this.description = description;
            this.damageType = damageType;
            this.damageValues = damageValues.clone();
        }

        public String getDisplayName() { return displayName; }
        public String getIcon() { return icon; }
        public String getDescription() { return description; }
        public String getDamageType() { return damageType; }

        public int getDamageForLevel(int level) {
            int index = Math.max(0, Math.min(level - 1, damageValues.length - 1));
            return damageValues[index];
        }

        public String getSpecialEffect() {
            return switch (this) {
                case FIRE -> "Burn pentru 3 ture";
                case ICE -> "Slow attack speed cu 20%";
                case LIGHTNING -> "5% șansă de stun";
                case POISON -> "Poison damage 3 ture";
                case HOLY -> "Heal 10% din damage dat";
                case SHADOW -> "Life steal 15% din damage";
                case ARCANE -> "Burn mana țintei";
                case NATURE -> "Regen 5 HP per tură";
            };
        }
    }

    // FIX: Constructor fără parametri pentru ShopServiceFX
    public EnchantScroll() {
        this(EnchantType.FIRE, 1, 1); // default values
    }

    public EnchantScroll(EnchantType type, int quantity, int enchantLevel) {
        this.type = type;
        this.quantity = quantity;
        this.enchantLevel = Math.max(1, Math.min(5, enchantLevel));
        this.nume = type.getDisplayName() + " (Nivel " + this.enchantLevel + ")";
    }

    public EnchantScroll(EnchantType type, int quantity) {
        this(type, quantity, 1); // Default nivel 1
    }

    // Getteri și setteri
    public String getNume() { return nume; }
    public EnchantType getType() { return type; }
    public int getQuantity() { return quantity; }
    public int getEnchantLevel() { return enchantLevel; }

    public void setQuantity(int quantity) { this.quantity = Math.max(0, quantity); }

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

    public int getEnchantDamage() {
        return type.getDamageForLevel(enchantLevel);
    }

    public String getFullDescription() {
        return String.format("%s (+%d %s damage) - %s",
                type.getDescription(), getEnchantDamage(),
                type.getDamageType(), type.getSpecialEffect());
    }

    public int getApplicationCost() {
        // Cost în gold pentru aplicarea enchantment-ului
        return enchantLevel * 50 + (type.ordinal() + 1) * 25;
    }

    @Override
    public String toString() {
        return String.format("%s %s x%d - Nivel %d (+%d %s dmg)",
                type.getIcon(), type.getDisplayName(), quantity, enchantLevel,
                getEnchantDamage(), type.getDamageType());
    }
}