package com.rpg.utils;

/**
 * Constante pentru jocul RPG
 */
public class GameConstants {

    // ==================== HERO BASE STATS ====================
    public static final int BASE_HEALTH = 100;
    public static final int BASE_MANA = 50;
    public static final int BASE_DEFENSE = 5;

    // Health și Mana scaling
    public static final int HEALTH_PER_STRENGTH = 5;
    public static final int HEALTH_PER_LEVEL = 10;
    public static final int MANA_PER_INTELLIGENCE = 3;
    public static final int MANA_PER_LEVEL = 5;

    // ==================== CLASS BASE STATS ====================
    // WARRIOR (Moldovean)
    public static final int WARRIOR_BASE_STRENGTH = 15;
    public static final int WARRIOR_BASE_DEXTERITY = 8;
    public static final int WARRIOR_BASE_INTELLIGENCE = 5;
    public static final int WARRIOR_BASE_HEALTH = 120;
    public static final int WARRIOR_RAGE_MAX = 100;
    public static final int WARRIOR_RAGE_GENERATION_DIVISOR = 10;

    // WIZARD (Ardelean)
    public static final int WIZARD_BASE_STRENGTH = 5;
    public static final int WIZARD_BASE_DEXTERITY = 8;
    public static final int WIZARD_BASE_INTELLIGENCE = 15;
    public static final int WIZARD_BASE_HEALTH = 80;
    public static final int WIZARD_MANA_MAX = 150;

    // ROGUE (Oltean)
    public static final int ROGUE_BASE_STRENGTH = 8;
    public static final int ROGUE_BASE_DEXTERITY = 15;
    public static final int ROGUE_BASE_INTELLIGENCE = 5;
    public static final int ROGUE_BASE_HEALTH = 100;
    public static final int ROGUE_ENERGY_MAX = 120;

    // ==================== COMBAT ====================
    public static final double BASE_HIT_CHANCE = 85.0;
    public static final double HIT_CHANCE_PER_DEX = 0.5;
    public static final double HIT_CHANCE_PER_LEVEL = 0.5;

    public static final double BASE_CRIT_CHANCE = 5.0;
    public static final double CRIT_CHANCE_PER_DEX = 0.3;
    public static final double CRIT_DAMAGE_MULTIPLIER = 2.0;

    public static final double BASE_DODGE_CHANCE = 5.0;
    public static final double DODGE_CHANCE_PER_DEX = 0.4;

    // ==================== ENEMY STATS ====================
    public static final double ENEMY_BASE_CRIT_CHANCE = 3.0;

    // ================== CONSTANTE PENTRU INAMICI ==================

    // ================== CONSTANTE PENTRU INAMICI ==================
    public static final int ENEMY_BASE_HEALTH = 60;        // ✅ TREBUIE > 0
    public static final int ENEMY_HEALTH_PER_LEVEL = 25;   // ✅ TREBUIE > 0
    public static final int ENEMY_BASE_DEFENSE = 3;        // ✅ TREBUIE >= 0
    public static final int ENEMY_DEFENSE_PER_LEVEL = 2;   // ✅ TREBUIE >= 0
    public static final int ENEMY_BASE_GOLD = 15;          // ✅ TREBUIE > 0
    public static final int ENEMY_GOLD_PER_LEVEL = 8;      // ✅ TREBUIE > 0
    public static final int ENEMY_BASE_XP = 25;            // ✅ TREBUIE > 0
    public static final int ENEMY_XP_PER_LEVEL = 10;       // ✅ TREBUIE > 0
    public static final int ENEMY_BASE_DAMAGE = 20;        // ✅ TREBUIE > 0
    public static final int ENEMY_DAMAGE_PER_LEVEL = 5;    // ✅ TREBUIE > 0


    // ==================== PROGRESSION ====================
    // XP Curve: Hybrid formula for smoother progression
    public static final int BASE_XP_REQUIRED = 100;
    public static final double XP_MULTIPLIER = 1.2;  // Reduced from 1.5 (much gentler curve)
    public static final int XP_FLAT_BONUS_PER_LEVEL = 20;  // Linear component
    public static final int STAT_POINTS_PER_LEVEL = 3;
    public static final int MAX_LEVEL = 100;
    // ==================== RESOURCES ====================
    public static final int INITIAL_GOLD = 100;
    public static final int INITIAL_POTIONS = 3;
    public static final int INITIAL_MANA_POTIONS = 3;
    public static final int NATURAL_HEALTH_REGEN = 2;
    public static final int RESOURCE_REGEN_PER_TURN = 10;

    // ==================== POTIONS ====================
    public static final int HEALTH_POTION_HEAL = 30;
    public static final int MANA_POTION_RESTORE = 25;
    public static final int SIMPLE_MANA_POTION_RESTORE = 25; // sau ce valoare vrei


    // ==================== BUFFS/DEBUFFS ====================
    public static final int MAX_BUFF_STACKS = 3;
    public static final int MAX_DEBUFF_STACKS = 5;

    // ==================== MÉTODOS DE CÁLCULO ====================

    // ================== CONSTANTE PENTRU NOILE SISTEME ==================

    // Flask Pieces drop chances din boss-i
    public static final double BOSS_FLASK_DROP_CHANCE = 75.0; // 75% șansă

    // Buff Potions
    public static final int BUFF_POTION_DURATION = 3; // Numărul de lupte

    // Enhancement costs
    public static final int ENHANCEMENT_BASE_COST = 10; // Cost de bază în shards

    // Enchant Scrolls
    public static final double BOSS_SCROLL_DROP_CHANCE = 25.0; // 25% șansă
    public static final int ENCHANT_APPLICATION_COST = 100; // Cost în gold


    /**
     * Calculează damage-ul unui inamic bazat pe nivel
     * NEW FORMULA: 15 + (level * 4) + (level / 5) with ±15% variation
     */
    public static int calculateEnemyDamage(int nivel) {
        // 🔧 FIXED: Now uses level parameter! (was broken - constant 30 damage)
        int baseDamage = 15 + (nivel * 4) + (nivel / 5);

        // Adaugă variație ±15% (reduced from 20% for more consistency)
        int variation = (int)(baseDamage * 0.15);
        java.util.Random random = new java.util.Random();
        int minDamage = baseDamage - variation;
        int maxDamage = baseDamage + variation;

        return random.nextInt(maxDamage - minDamage + 1) + minDamage;
    }

    /**
     * Calculează health-ul maxim al unui inamic
     * NEW FORMULA: 50 + (level * 20) + (level² * 0.5) - quadratic growth
     */
    public static int calculateEnemyHealth(int nivel, boolean isBoss) {
        // Quadratic scaling: HP grows faster at higher levels
        int baseHealth = 50 + (nivel * 20) + (int)(nivel * nivel * 0.5);
        // Boss multiplier reduced from 2.5x to 2.0x (less HP sponge)
        return isBoss ? (int)(baseHealth * 2.0) : baseHealth;
    }

    /**
     * Calculează defense-ul unui inamic
     * NEW FORMULA: 2 + (level * 1.5) + (level / 10) - slightly faster growth
     */
    public static int calculateEnemyDefense(int nivel) {
        return 2 + (int)(nivel * 1.5) + (nivel / 10);
    }

    /**
     * Calculează drop chance pentru loot
     */
    public static double calculateDropChance(boolean isBoss) {
        return isBoss ? 100.0 : 30.0;
    }

    /**
     * Calculează gold reward de la inamic
     */
    public static int calculateGoldReward(int nivel, boolean isBoss) {
        int baseGold = 10 + (nivel * 5);
        return isBoss ? baseGold * 3 : baseGold;
    }

    /**
     * Calculează XP reward de la inamic
     */
    public static int calculateXPReward(int nivel, boolean isBoss) {
        int baseXP = 25 + (nivel * 10);
        return isBoss ? baseXP * 2 : baseXP;
    }
}