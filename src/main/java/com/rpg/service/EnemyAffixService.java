package com.rpg.service;

import com.rpg.model.characters.Inamic;
import com.rpg.model.enemies.EnemyAbility;
import com.rpg.model.enemies.EnemyAffix;
import com.rpg.model.enemies.EnemyArchetype;
import com.rpg.model.enemies.EnemyTier;
import com.rpg.utils.RandomUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service for assigning tiers and affixes to enemies.
 * Handles random generation and balancing of elite enemies.
 */
public class EnemyAffixService {

    /**
     * Determines enemy tier based on level and random chance.
     * Higher levels have better chances for elite tiers.
     */
    public static EnemyTier assignTier(int dungeonLevel, boolean forceBoss) {
        if (forceBoss) {
            return EnemyTier.BOSS;
        }

        // Calculate tier probabilities based on dungeon level
        double eliteChance = Math.min(20.0 + (dungeonLevel * 0.5), 40.0);      // 20-40%
        double championChance = Math.min(5.0 + (dungeonLevel * 0.3), 20.0);    // 5-20%
        double legendaryChance = Math.min(1.0 + (dungeonLevel * 0.2), 10.0);   // 1-10%

        double roll = RandomUtils.randomDouble() * 100.0;

        if (roll < legendaryChance) {
            return EnemyTier.LEGENDARY;
        } else if (roll < legendaryChance + championChance) {
            return EnemyTier.CHAMPION;
        } else if (roll < legendaryChance + championChance + eliteChance) {
            return EnemyTier.ELITE;
        } else {
            return EnemyTier.NORMAL;
        }
    }

    /**
     * Assigns random affixes to an enemy based on its tier.
     */
    public static void assignAffixes(Inamic enemy) {
        EnemyTier tier = enemy.getTier();
        int maxAffixes = tier.getMaxAffixes();

        if (maxAffixes == 0) {
            return; // Normal enemies have no affixes
        }

        List<EnemyAffix> availableAffixes = new ArrayList<>(Arrays.asList(EnemyAffix.values()));
        List<EnemyAffix> selectedAffixes = new ArrayList<>();

        // Select random affixes
        for (int i = 0; i < maxAffixes && !availableAffixes.isEmpty(); i++) {
            EnemyAffix affix = RandomUtils.randomElement(availableAffixes);
            selectedAffixes.add(affix);
            availableAffixes.remove(affix); // No duplicate affixes

            // Remove conflicting affixes
            removeConflictingAffixes(availableAffixes, affix);
        }

        enemy.setAffixes(selectedAffixes);

        // Initialize affix states
        initializeAffixStates(enemy);
    }

    /**
     * Removes affixes that conflict with the given affix.
     */
    private static void removeConflictingAffixes(List<EnemyAffix> available, EnemyAffix selected) {
        switch (selected) {
            case FAST -> available.remove(EnemyAffix.PHASING); // Can't be fast and phasing
            case SHIELDED -> available.remove(EnemyAffix.ARMORED); // Can't have both shields and armor
            case ENRAGED -> available.remove(EnemyAffix.BERSERKER); // Both are damage buffs
            case VAMPIRIC -> available.remove(EnemyAffix.REGENERATING); // Both are healing
            case BURNING -> {
                available.remove(EnemyAffix.FROZEN_AURA); // Fire vs Ice
                available.remove(EnemyAffix.SHOCKING);
            }
            case FROZEN_AURA -> {
                available.remove(EnemyAffix.BURNING); // Fire vs Ice
                available.remove(EnemyAffix.SHOCKING);
            }
        }
    }

    /**
     * Initializes state for affixes that need setup.
     */
    private static void initializeAffixStates(Inamic enemy) {
        for (EnemyAffix affix : enemy.getAffixes()) {
            switch (affix) {
                case SHIELDED -> {
                    enemy.setHasShield(true);
                    enemy.setShieldHealth((int) (enemy.getViataMaxima() * 0.5)); // Shield = 50% of max HP
                }
                case ARMORED -> {
                    // Handled in damage calculation
                }
                case REGENERATING -> {
                    enemy.setRegenerareViata((int) (enemy.getViataMaxima() * 0.05)); // 5% per turn
                }
            }
        }
    }

    /**
     * Applies tier stat modifiers to an enemy.
     */
    public static void applyTierModifiers(Inamic enemy) {
        EnemyTier tier = enemy.getTier();

        // Apply HP multiplier via reflection (since HP is set in constructor)
        try {
            java.lang.reflect.Field viataMaxField = Inamic.class.getDeclaredField("viataMaxima");
            viataMaxField.setAccessible(true);
            int baseViataMax = viataMaxField.getInt(enemy);
            int newViataMax = (int) (baseViataMax * tier.getHealthMultiplier());
            viataMaxField.setInt(enemy, newViataMax);

            java.lang.reflect.Field viataField = Inamic.class.getDeclaredField("viata");
            viataField.setAccessible(true);
            viataField.setInt(enemy, newViataMax); // Set current HP to max

            System.out.printf("🔧 Applied tier modifier: %s HP %d → %d (×%.1f)\n",
                    tier.getDisplayName(), baseViataMax, newViataMax, tier.getHealthMultiplier());

        } catch (Exception e) {
            System.err.println("⚠️ Failed to apply tier HP modifier: " + e.getMessage());
        }

        // Apply damage multiplier (NEW: uses tier.getDamageMultiplier())
        try {
            java.lang.reflect.Field damageField = Inamic.class.getDeclaredField("damage");
            damageField.setAccessible(true);
            int baseDamage = damageField.getInt(enemy);
            int newDamage = (int) (baseDamage * tier.getDamageMultiplier());
            damageField.setInt(enemy, newDamage);

            System.out.printf("🔧 Applied tier modifier: Damage %d → %d (×%.1f)\n", baseDamage, newDamage, tier.getDamageMultiplier());

        } catch (Exception e) {
            System.err.println("⚠️ Failed to apply tier damage modifier: " + e.getMessage());
        }

        // Apply reward multiplier
        try {
            java.lang.reflect.Field goldField = Inamic.class.getDeclaredField("goldReward");
            goldField.setAccessible(true);
            int baseGold = goldField.getInt(enemy);
            int newGold = (int) (baseGold * tier.getRewardMultiplier());
            goldField.setInt(enemy, newGold);

            java.lang.reflect.Field xpField = Inamic.class.getDeclaredField("xpReward");
            xpField.setAccessible(true);
            int baseXp = xpField.getInt(enemy);
            int newXp = (int) (baseXp * tier.getRewardMultiplier());
            xpField.setInt(enemy, newXp);

            System.out.printf("🔧 Applied tier rewards: Gold %d → %d, XP %d → %d\n",
                    baseGold, newGold, baseXp, newXp);

        } catch (Exception e) {
            System.err.println("⚠️ Failed to apply tier reward modifier: " + e.getMessage());
        }
    }

    /**
     * Full enemy enhancement - assigns tier, affixes, and applies modifiers.
     */
    public static void enhanceEnemy(Inamic enemy, int dungeonLevel, boolean forceBoss) {
        // Assign tier
        EnemyTier tier = assignTier(dungeonLevel, forceBoss);
        enemy.setTier(tier);

        System.out.printf("🎯 Enhancing enemy: %s → %s\n", enemy.getNume(), tier.getFormattedName());

        // Assign archetype (only for Elite+ enemies)
        if (tier != EnemyTier.NORMAL) {
            assignArchetype(enemy);
        }

        // Apply tier modifiers
        applyTierModifiers(enemy);

        // Apply archetype modifiers
        if (enemy.getArchetype() != null) {
            applyArchetypeModifiers(enemy);
        }

        // Assign affixes
        assignAffixes(enemy);

        // Assign abilities (archetype-aware)
        assignAbilities(enemy);

        // Log final result
        if (!enemy.getAffixes().isEmpty()) {
            System.out.printf("✨ Affixes: ");
            for (EnemyAffix affix : enemy.getAffixes()) {
                System.out.printf("%s ", affix.getFormattedName());
            }
            System.out.println();
        }

        if (!enemy.getAbilities().isEmpty()) {
            System.out.printf("💫 Abilities: ");
            for (EnemyAbility ability : enemy.getAbilities()) {
                System.out.printf("%s ", ability.getFormattedName());
            }
            System.out.println();
        }

        if (enemy.getArchetype() != null) {
            System.out.printf("🎭 Archetype: %s\n", enemy.getArchetype().getFormattedName());
        }
    }

    /**
     * Assigns a random archetype to an enemy.
     * All elite+ enemies get an archetype that defines their behavior.
     */
    private static void assignArchetype(Inamic enemy) {
        EnemyArchetype[] allArchetypes = EnemyArchetype.values();
        EnemyArchetype archetype = RandomUtils.randomElement(Arrays.asList(allArchetypes));
        enemy.setArchetype(archetype);
    }

    /**
     * Applies stat modifiers based on archetype.
     */
    private static void applyArchetypeModifiers(Inamic enemy) {
        EnemyArchetype archetype = enemy.getArchetype();
        if (archetype == null) return;

        // Apply HP multiplier
        int currentMaxHP = enemy.getViataMaxima();
        int newMaxHP = (int) (currentMaxHP * archetype.getHpMultiplier());
        enemy.setViata(newMaxHP); // Set current HP to new max
        // Note: Can't modify viataMaxima as it's final, but viata represents current HP

        // Apply damage multiplier
        int currentDamage = enemy.getDamage();
        int newDamage = (int) (currentDamage * archetype.getDamageMultiplier());
        enemy.setDamage(newDamage);

        System.out.printf("   🎭 Archetype modifiers: HP %.0f%%, Damage %.0f%%\n",
                archetype.getHpMultiplier() * 100, archetype.getDamageMultiplier() * 100);
    }

    /**
     * Assigns abilities to an enemy based on tier AND archetype.
     * Archetype determines which abilities are preferred for this enemy's behavior.
     * - NORMAL: 0 abilities
     * - ELITE: 1 ability
     * - CHAMPION: 2 abilities
     * - BOSS: 3 abilities
     * - LEGENDARY: 4 abilities
     */
    public static void assignAbilities(Inamic enemy) {
        EnemyTier tier = enemy.getTier();
        EnemyArchetype archetype = enemy.getArchetype();
        int abilityCount = tier.getMaxAffixes(); // Reuse maxAffixes count (0, 1, 2, 3, 4)

        if (abilityCount == 0) {
            return; // Normal enemies have no abilities
        }

        List<EnemyAbility> selectedAbilities = new ArrayList<>();

        // Get archetype-specific ability preferences
        List<EnemyAbility> preferredOffensive = archetype != null ?
                Arrays.asList(archetype.getPreferredOffensiveAbilities()) :
                Arrays.asList(EnemyAbility.FIREBALL, EnemyAbility.LIGHTNING_BOLT, EnemyAbility.POISON_STRIKE);

        List<EnemyAbility> preferredDefensive = archetype != null ?
                Arrays.asList(archetype.getPreferredDefensiveAbilities()) :
                Arrays.asList(EnemyAbility.SHIELD_WALL, EnemyAbility.EVASION, EnemyAbility.DESPERATE_HEAL);

        List<EnemyAbility> preferredTactical = archetype != null ?
                Arrays.asList(archetype.getPreferredTacticalAbilities()) :
                Arrays.asList(EnemyAbility.BATTLE_CRY, EnemyAbility.ENRAGE);

        // Fallback pools if archetype has no preferences
        List<EnemyAbility> offensiveAbilities = Arrays.asList(
                EnemyAbility.FIREBALL,
                EnemyAbility.LIGHTNING_BOLT,
                EnemyAbility.POISON_STRIKE
        );

        List<EnemyAbility> eliteOffensiveAbilities = Arrays.asList(
                EnemyAbility.POWER_STRIKE,
                EnemyAbility.EXECUTE
        );

        List<EnemyAbility> defensiveAbilities = Arrays.asList(
                EnemyAbility.SHIELD_WALL,
                EnemyAbility.EVASION,
                EnemyAbility.DESPERATE_HEAL
        );

        List<EnemyAbility> crowdControlAbilities = Arrays.asList(
                EnemyAbility.STUN_STRIKE,
                EnemyAbility.WEAKENING_CURSE,
                EnemyAbility.CRIPPLING_BLOW
        );

        List<EnemyAbility> tacticalAbilities = Arrays.asList(
                EnemyAbility.BATTLE_CRY,
                EnemyAbility.ENRAGE,
                EnemyAbility.DESPERATE_GAMBIT,
                EnemyAbility.BLOOD_FRENZY
        );

        // Select abilities based on tier (using archetype preferences when available)
        switch (tier) {
            case ELITE -> {
                // 1 ability: offensive or tactical (archetype-preferred)
                if (!preferredOffensive.isEmpty() && RandomUtils.randomBoolean()) {
                    selectedAbilities.add(RandomUtils.randomElement(preferredOffensive));
                } else if (!preferredTactical.isEmpty()) {
                    selectedAbilities.add(RandomUtils.randomElement(preferredTactical));
                } else {
                    selectedAbilities.add(RandomUtils.randomElement(offensiveAbilities));
                }
            }

            case CHAMPION -> {
                // 2 abilities: offensive + defensive (archetype-preferred)
                if (!preferredOffensive.isEmpty()) {
                    selectedAbilities.add(RandomUtils.randomElement(preferredOffensive));
                } else {
                    selectedAbilities.add(RandomUtils.randomElement(offensiveAbilities));
                }

                if (!preferredDefensive.isEmpty() && RandomUtils.randomBoolean()) {
                    selectedAbilities.add(RandomUtils.randomElement(preferredDefensive));
                } else {
                    selectedAbilities.add(RandomUtils.randomElement(crowdControlAbilities));
                }
            }

            case BOSS -> {
                // 3 abilities: offensive + defensive + tactical (archetype-preferred)
                if (!preferredOffensive.isEmpty()) {
                    selectedAbilities.add(RandomUtils.randomElement(preferredOffensive));
                } else {
                    selectedAbilities.add(RandomUtils.randomElement(
                            combineList(offensiveAbilities, eliteOffensiveAbilities)));
                }

                if (!preferredDefensive.isEmpty()) {
                    selectedAbilities.add(RandomUtils.randomElement(preferredDefensive));
                } else {
                    selectedAbilities.add(RandomUtils.randomElement(defensiveAbilities));
                }

                if (!preferredTactical.isEmpty()) {
                    selectedAbilities.add(RandomUtils.randomElement(preferredTactical));
                } else {
                    selectedAbilities.add(RandomUtils.randomElement(Arrays.asList(
                            EnemyAbility.BATTLE_CRY, EnemyAbility.ENRAGE)));
                }
            }

            case LEGENDARY -> {
                // 4 abilities: best of all types (archetype-preferred)
                if (!preferredOffensive.isEmpty()) {
                    selectedAbilities.add(RandomUtils.randomElement(preferredOffensive));
                } else {
                    selectedAbilities.add(RandomUtils.randomElement(eliteOffensiveAbilities));
                }

                if (!preferredDefensive.isEmpty()) {
                    selectedAbilities.add(RandomUtils.randomElement(preferredDefensive));
                } else {
                    selectedAbilities.add(RandomUtils.randomElement(defensiveAbilities));
                }

                selectedAbilities.add(RandomUtils.randomElement(crowdControlAbilities));

                if (!preferredTactical.isEmpty()) {
                    selectedAbilities.add(RandomUtils.randomElement(preferredTactical));
                } else {
                    selectedAbilities.add(RandomUtils.randomElement(tacticalAbilities));
                }
            }
        }

        enemy.setAbilities(selectedAbilities);
    }

    /**
     * Helper method to combine two lists.
     */
    private static <T> List<T> combineList(List<T> list1, List<T> list2) {
        List<T> combined = new ArrayList<>(list1);
        combined.addAll(list2);
        return combined;
    }
}
