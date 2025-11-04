package com.rpg.model.abilities;

import java.util.*;

/**
 * Factory class that defines all abilities with their variants and talents.
 * This serves as the content database for the ability customization system.
 *
 * Each ability includes:
 * - 3 variants (different ways to use the ability)
 * - 9 talents (3 tiers × 3 options per tier)
 */
public class AbilityDefinitions {

    /**
     * Creates a base Fireball ability (Wizard).
     */
    public static Abilitate createFireballBase() {
        Map<String, Double> statInfluence = new HashMap<>();
        statInfluence.put("Intelligence", 0.5); // +0.5 damage per INT point

        return new Abilitate(
                "Fireball",
                80,  // Base damage
                List.of("Fire"),
                20,  // Mana cost
                0,   // No cooldown
                0,   // Hit chance bonus
                statInfluence,
                "Burn",  // Debuff
                3,       // Burn duration
                10       // Burn damage per turn
        ).setAbilityType(AbilityType.OFFENSIVE)
         .setRequiredLevel(1);
    }

    /**
     * Creates all 3 variants for Fireball.
     */
    public static List<AbilityVariant> createFireballVariants() {
        List<AbilityVariant> variants = new ArrayList<>();

        // Variant A: Fireball (Default)
        variants.add(new AbilityVariant(
                "fireball_default",
                "Fireball",
                "Classic fireball that burns a single enemy",
                true
        )
        .withDamage(80)
        .withManaCost(20)
        .withCooldown(0)
        .withDebuff("Burn", 3, 10)
        .withDamageTypes("Fire")
        .withUseCase("Reliable single-target damage"));

        // Variant B: Firestorm (AOE)
        variants.add(new AbilityVariant(
                "fireball_firestorm",
                "Firestorm",
                "Unleash flames that hit all enemies",
                false
        )
        .withDamage(40)
        .withManaCost(40)
        .withCooldown(0)
        .asAOE(99, 1.0)  // Hits all enemies
        .withDamageTypes("Fire")
        .withUseCase("Multi-enemy fights"));

        // Variant C: Inferno Bolt (Burst)
        variants.add(new AbilityVariant(
                "fireball_inferno",
                "Inferno Bolt",
                "Devastating single-target burst",
                false
        )
        .withDamage(150)
        .withManaCost(50)
        .withCooldown(2)
        .withDamageTypes("Fire")
        .withUseCase("Boss killing"));

        return variants;
    }

    /**
     * Creates all 9 talents for Fireball (3 tiers × 3 options).
     */
    public static List<AbilityTalent> createFireballTalents() {
        List<AbilityTalent> talents = new ArrayList<>();

        // ===== TIER 1 =====
        talents.add(new AbilityTalent(
                "fireball_t1_intense_heat",
                "Intense Heat",
                "+20% damage",
                "🔥",
                TalentTier.TIER_1,
                new AbilityModifier().withDamageMultiplier(1.2)
        ));

        talents.add(new AbilityTalent(
                "fireball_t1_swift_cast",
                "Swift Cast",
                "-5 mana cost",
                "💨",
                TalentTier.TIER_1,
                new AbilityModifier().withFlatManaCostReduction(5)
        ));

        talents.add(new AbilityTalent(
                "fireball_t1_mana_efficient",
                "Mana Efficient",
                "-30% mana cost, -30% damage",
                "🧠",
                TalentTier.TIER_1,
                new AbilityModifier()
                        .withManaCostMultiplier(0.7)
                        .withDamageMultiplier(0.7)
        ));

        // ===== TIER 2 =====
        talents.add(new AbilityTalent(
                "fireball_t2_melt_armor",
                "Melt Armor",
                "Reduces enemy defense by 10 for 2 turns",
                "🌊",
                TalentTier.TIER_2,
                new AbilityModifier().withArmorReduction(10, 2)
        ));

        talents.add(new AbilityTalent(
                "fireball_t2_chain_fire",
                "Chain Fire",
                "25% chance to hit a second enemy for 50% damage",
                "⚡",
                TalentTier.TIER_2,
                new AbilityModifier().withChainEffect(0.5)
        ));

        talents.add(new AbilityTalent(
                "fireball_t2_precision",
                "Precision",
                "+15% crit chance, crits double burn duration",
                "🎯",
                TalentTier.TIER_2,
                new AbilityModifier()
                        .withCritChanceBonus(0.15)
                        .withCustomProperty("crit_doubles_burn", true)
        ));

        // ===== TIER 3 =====
        talents.add(new AbilityTalent(
                "fireball_t3_explosive",
                "Explosive",
                "Killing blow explodes for 30 AOE damage",
                "💥",
                TalentTier.TIER_3,
                new AbilityModifier().withExplosionOnKill(30)
        ));

        talents.add(new AbilityTalent(
                "fireball_t3_cooldown_reset",
                "Cooldown Reset",
                "Kills refund 50% mana cost",
                "🔄",
                TalentTier.TIER_3,
                new AbilityModifier().withManaRefundOnKill(0.5)
        ));

        talents.add(new AbilityTalent(
                "fireball_t3_flame_shield",
                "Flame Shield",
                "Grants +20 defense for 1 turn when cast",
                "🛡️",
                TalentTier.TIER_3,
                new AbilityModifier().withDefenseOnCast(20, 1)
        ));

        return talents;
    }

    // ========================================================================
    // CLEAVE (Warrior ability)
    // ========================================================================

    /**
     * Creates a base Cleave ability (Warrior).
     */
    public static Abilitate createCleaveBase() {
        Map<String, Double> statInfluence = new HashMap<>();
        statInfluence.put("Strength", 0.6); // +0.6 damage per STR point

        return new Abilitate(
                "Cleave",
                70,  // Base damage
                List.of("Physical"),
                10,  // Rage cost
                0,   // No cooldown
                0,   // Hit chance bonus
                statInfluence,
                null,  // No debuff by default
                0,
                0
        ).setAbilityType(AbilityType.OFFENSIVE)
         .setRequiredLevel(1);
    }

    /**
     * Creates all 3 variants for Cleave.
     */
    public static List<AbilityVariant> createCleaveVariants() {
        List<AbilityVariant> variants = new ArrayList<>();

        // Variant A: Cleave (Default) - hits 2 enemies
        variants.add(new AbilityVariant(
                "cleave_default",
                "Cleave",
                "Strike two adjacent enemies",
                true
        )
        .withDamage(70)
        .withManaCost(10)
        .withCooldown(0)
        .asAOE(2, 1.0)
        .withDamageTypes("Physical")
        .withUseCase("General farming"));

        // Variant B: Focused Strike - single target, higher damage
        variants.add(new AbilityVariant(
                "cleave_focused",
                "Focused Strike",
                "Concentrate power into one devastating blow",
                false
        )
        .withDamage(140)
        .withManaCost(10)
        .withCooldown(0)
        .withDamageTypes("Physical")
        .withUseCase("Boss killing"));

        // Variant C: Sweeping Blade - hits all enemies
        variants.add(new AbilityVariant(
                "cleave_sweeping",
                "Sweeping Blade",
                "Wide arc that hits all enemies",
                false
        )
        .withDamage(35)
        .withManaCost(20)
        .withCooldown(0)
        .asAOE(99, 1.0)
        .withDamageTypes("Physical")
        .withUseCase("Large mob groups"));

        return variants;
    }

    /**
     * Creates all 9 talents for Cleave.
     */
    public static List<AbilityTalent> createCleaveTalents() {
        List<AbilityTalent> talents = new ArrayList<>();

        // ===== TIER 1 =====
        talents.add(new AbilityTalent(
                "cleave_t1_bleeding_edge",
                "Bleeding Edge",
                "+15% damage, applies bleed (5 damage/turn × 3)",
                "🩸",
                TalentTier.TIER_1,
                new AbilityModifier()
                        .withDamageMultiplier(1.15)
                        .withBleed(5, 3)
        ));

        talents.add(new AbilityTalent(
                "cleave_t1_rage_gain",
                "Rage Gain",
                "Each hit generates +3 rage",
                "😤",
                TalentTier.TIER_1,
                new AbilityModifier().withResourceGeneration(3)
        ));

        talents.add(new AbilityTalent(
                "cleave_t1_quick_strike",
                "Quick Strike",
                "-5 rage cost",
                "⚡",
                TalentTier.TIER_1,
                new AbilityModifier().withFlatManaCostReduction(5)
        ));

        // ===== TIER 2 =====
        talents.add(new AbilityTalent(
                "cleave_t2_armor_shatter",
                "Armor Shatter",
                "Reduces enemy defense by 15 for 2 turns",
                "💥",
                TalentTier.TIER_2,
                new AbilityModifier().withArmorReduction(15, 2)
        ));

        talents.add(new AbilityTalent(
                "cleave_t2_momentum",
                "Cleaving Momentum",
                "Each kill reduces cooldown by 1 turn (if variant has cooldown)",
                "🔄",
                TalentTier.TIER_2,
                new AbilityModifier()
                        .withCustomProperty("momentum_cooldown_reduction", 1)
        ));

        talents.add(new AbilityTalent(
                "cleave_t2_wide_arc",
                "Wide Arc",
                "+1 enemy hit (Cleave hits 3, Sweeping hits 5)",
                "🌀",
                TalentTier.TIER_2,
                new AbilityModifier().withAdditionalAOETargets(1)
        ));

        // ===== TIER 3 =====
        talents.add(new AbilityTalent(
                "cleave_t3_execute",
                "Execute",
                "+100% damage vs enemies below 30% HP",
                "⚔️",
                TalentTier.TIER_3,
                new AbilityModifier().withExecute(0.3, 1.0)
        ));

        talents.add(new AbilityTalent(
                "cleave_t3_lifesteal",
                "Lifesteal",
                "Heal for 25% of damage dealt",
                "💉",
                TalentTier.TIER_3,
                new AbilityModifier().withLifesteal(0.25)
        ));

        talents.add(new AbilityTalent(
                "cleave_t3_rage_dump",
                "Rage Dump",
                "Spend extra rage for +10% damage per rage point",
                "🔥",
                TalentTier.TIER_3,
                new AbilityModifier()
                        .withCustomProperty("rage_dump_active", true)
                        .withCustomProperty("rage_dump_per_point", 0.1)
        ));

        return talents;
    }

    // ========================================================================
    // LIGHTNING BOLT (Wizard ability)
    // ========================================================================

    /**
     * Creates a base Lightning Bolt ability (Wizard).
     */
    public static Abilitate createLightningBoltBase() {
        Map<String, Double> statInfluence = new HashMap<>();
        statInfluence.put("Intelligence", 0.4);
        statInfluence.put("Dexterity", 0.2); // Lightning is fast and precise

        return new Abilitate(
                "Lightning Bolt",
                60,  // Base damage
                List.of("Lightning"),
                15,  // Mana cost
                0,   // No cooldown
                10,  // +10% hit chance (fast attack)
                statInfluence,
                "Shock",  // Debuff
                2,        // Shock duration
                8         // Shock damage per turn
        ).setAbilityType(AbilityType.OFFENSIVE)
         .setRequiredLevel(5);
    }

    /**
     * Creates all 3 variants for Lightning Bolt.
     */
    public static List<AbilityVariant> createLightningBoltVariants() {
        List<AbilityVariant> variants = new ArrayList<>();

        // Variant A: Lightning Bolt (Default)
        variants.add(new AbilityVariant(
                "lightning_default",
                "Lightning Bolt",
                "Fast, precise strike with high hit chance",
                true
        )
        .withDamage(60)
        .withManaCost(15)
        .withCooldown(0)
        .withHitChanceBonus(10)
        .withDebuff("Shock", 2, 8)
        .withDamageTypes("Lightning")
        .withUseCase("Quick reliable damage"));

        // Variant B: Chain Lightning (Bounces)
        variants.add(new AbilityVariant(
                "lightning_chain",
                "Chain Lightning",
                "Bounces between 3 enemies, 70% damage each bounce",
                false
        )
        .withDamage(60)
        .withManaCost(30)
        .withCooldown(0)
        .asAOE(3, 0.7)  // Hits 3 enemies, 70% per target
        .withDebuff("Shock", 2, 8)
        .withDamageTypes("Lightning")
        .withUseCase("Multiple enemies"));

        // Variant C: Overcharge (High risk, high reward)
        variants.add(new AbilityVariant(
                "lightning_overcharge",
                "Overcharge",
                "Massive damage but damages yourself",
                false
        )
        .withDamage(180)
        .withManaCost(40)
        .withCooldown(1)
        .withSelfDamage(20)
        .withDamageTypes("Lightning")
        .withUseCase("Burst damage when winning"));

        return variants;
    }

    /**
     * Creates all 9 talents for Lightning Bolt.
     */
    public static List<AbilityTalent> createLightningBoltTalents() {
        List<AbilityTalent> talents = new ArrayList<>();

        // ===== TIER 1 =====
        talents.add(new AbilityTalent(
                "lightning_t1_voltage",
                "High Voltage",
                "+25% damage",
                "⚡",
                TalentTier.TIER_1,
                new AbilityModifier().withDamageMultiplier(1.25)
        ));

        talents.add(new AbilityTalent(
                "lightning_t1_energy_efficient",
                "Energy Efficient",
                "-5 mana cost",
                "💡",
                TalentTier.TIER_1,
                new AbilityModifier().withFlatManaCostReduction(5)
        ));

        talents.add(new AbilityTalent(
                "lightning_t1_static",
                "Static Buildup",
                "Doubles shock duration and damage",
                "⚡",
                TalentTier.TIER_1,
                new AbilityModifier()
                        .withCustomProperty("double_shock_duration", true)
                        .withCustomProperty("double_shock_damage", true)
        ));

        // ===== TIER 2 =====
        talents.add(new AbilityTalent(
                "lightning_t2_paralyze",
                "Paralyzing Strike",
                "10% chance to stun for 1 turn",
                "🎯",
                TalentTier.TIER_2,
                new AbilityModifier()
                        .withCustomProperty("stun_chance", 0.1)
                        .withCustomProperty("stun_duration", 1)
        ));

        talents.add(new AbilityTalent(
                "lightning_t2_arc",
                "Arc Discharge",
                "Chains to 1 additional enemy for 40% damage",
                "🌐",
                TalentTier.TIER_2,
                new AbilityModifier().withChainEffect(0.4)
        ));

        talents.add(new AbilityTalent(
                "lightning_t2_precision",
                "Perfect Aim",
                "+20% hit chance, +10% crit chance",
                "🎯",
                TalentTier.TIER_2,
                new AbilityModifier()
                        .withCritChanceBonus(0.1)
                        .withCustomProperty("hit_chance_bonus", 20)
        ));

        // ===== TIER 3 =====
        talents.add(new AbilityTalent(
                "lightning_t3_thunderstorm",
                "Thunderstorm",
                "Kills trigger lightning to hit all enemies for 20 damage",
                "⛈️",
                TalentTier.TIER_3,
                new AbilityModifier().withExplosionOnKill(20)
        ));

        talents.add(new AbilityTalent(
                "lightning_t3_surge",
                "Power Surge",
                "Restore 10 mana on critical hit",
                "⚡",
                TalentTier.TIER_3,
                new AbilityModifier()
                        .withCustomProperty("mana_on_crit", 10)
        ));

        talents.add(new AbilityTalent(
                "lightning_t3_reflect",
                "Lightning Reflexes",
                "+10% dodge chance for 2 turns after casting",
                "💨",
                TalentTier.TIER_3,
                new AbilityModifier()
                        .withCustomProperty("dodge_bonus", 0.1)
                        .withCustomProperty("dodge_duration", 2)
        ));

        return talents;
    }

    // ========================================================================
    // 🆕 NEW WIZARD ABILITIES
    // ========================================================================

    /**
     * ICE SHARD - Control/Damage ability with freeze/slow
     */
    public static Abilitate createIceShardBase() {
        Map<String, Double> statInfluence = new HashMap<>();
        statInfluence.put("Intelligence", 0.5);

        return new Abilitate(
                "Ice Shard",
                70,  // Base damage
                List.of("Ice"),
                18,  // Mana cost
                0,   // No cooldown
                0,   // Hit chance bonus
                statInfluence,
                "Slow",
                2,
                0
        ).setAbilityType(AbilityType.OFFENSIVE)
         .setRequiredLevel(3);
    }

    public static List<AbilityVariant> createIceShardVariants() {
        List<AbilityVariant> variants = new ArrayList<>();

        // Variant A: Ice Shard (Default - Slow)
        variants.add(new AbilityVariant(
                "iceshard_default",
                "Ice Shard",
                "Sharp ice that slows the enemy",
                true
        )
        .withDamage(70)
        .withManaCost(18)
        .withDebuff("Slow", 2, 0)
        .withDamageTypes("Ice")
        .withUseCase("Control + damage"));

        // Variant B: Frost Lance (Freeze)
        variants.add(new AbilityVariant(
                "iceshard_frost",
                "Frost Lance",
                "Freeze enemy solid for 1 turn",
                false
        )
        .withDamage(60)
        .withManaCost(25)
        .withDebuff("Freeze", 1, 0)
        .withDamageTypes("Ice")
        .withUseCase("Hard control"));

        // Variant C: Blizzard (AOE Slow)
        variants.add(new AbilityVariant(
                "iceshard_blizzard",
                "Blizzard",
                "Ice storm that slows all enemies",
                false
        )
        .withDamage(35)
        .withManaCost(30)
        .asAOE(99, 1.0)
        .withDebuff("Slow", 2, 0)
        .withDamageTypes("Ice")
        .withUseCase("AOE control"));

        return variants;
    }

    public static List<AbilityTalent> createIceShardTalents() {
        List<AbilityTalent> talents = new ArrayList<>();

        // TIER 1
        talents.add(new AbilityTalent(
                "iceshard_t1_deep_freeze",
                "Deep Freeze",
                "+25% damage, +1 slow duration",
                "❄️",
                TalentTier.TIER_1,
                new AbilityModifier().withDamageMultiplier(1.25)
        ));

        talents.add(new AbilityTalent(
                "iceshard_t1_chill",
                "Chilling Touch",
                "Apply chill (5 DOT for 3 turns)",
                "🧊",
                TalentTier.TIER_1,
                new AbilityModifier().withBleed(5, 3)  // Using bleed for DOT
        ));

        talents.add(new AbilityTalent(
                "iceshard_t1_efficient",
                "Ice Mastery",
                "-25% mana cost",
                "💧",
                TalentTier.TIER_1,
                new AbilityModifier().withManaCostMultiplier(0.75)
        ));

        // TIER 2
        talents.add(new AbilityTalent(
                "iceshard_t2_shatter",
                "Shatter",
                "+50% damage to slowed/frozen",
                "💎",
                TalentTier.TIER_2,
                new AbilityModifier().withDamageMultiplier(1.5)  // Conditional bonus
        ));

        talents.add(new AbilityTalent(
                "iceshard_t2_brittle",
                "Brittle",
                "Reduce enemy defense by 15",
                "🛡️",
                TalentTier.TIER_2,
                new AbilityModifier().withArmorReduction(15, 3)
        ));

        talents.add(new AbilityTalent(
                "iceshard_t2_pierce",
                "Ice Pierce",
                "+20% crit chance",
                "🎯",
                TalentTier.TIER_2,
                new AbilityModifier().withCritChanceBonus(20.0)
        ));

        // TIER 3
        talents.add(new AbilityTalent(
                "iceshard_t3_frozen_tomb",
                "Frozen Tomb",
                "Freeze all nearby on crit",
                "🧊",
                TalentTier.TIER_3,
                new AbilityModifier().withCustomProperty("frozen_tomb", true)
        ));

        talents.add(new AbilityTalent(
                "iceshard_t3_permafrost",
                "Permafrost",
                "Slow duration doubled",
                "❄️",
                TalentTier.TIER_3,
                new AbilityModifier().withCustomProperty("permafrost", true)
        ));

        talents.add(new AbilityTalent(
                "iceshard_t3_cold_snap",
                "Cold Snap",
                "Refund 50% mana on kill",
                "💙",
                TalentTier.TIER_3,
                new AbilityModifier().withManaRefundOnKill(0.5)
        ));

        return talents;
    }

    /**
     * ARCANE MISSILES - Multi-hit magic damage
     */
    public static Abilitate createArcaneMissilesBase() {
        Map<String, Double> statInfluence = new HashMap<>();
        statInfluence.put("Intelligence", 0.4);

        return new Abilitate(
                "Arcane Missiles",
                25,  // Damage per missile
                List.of("Arcane"),
                22,  // Mana cost
                0,
                5,  // +5 hit chance
                statInfluence,
                null,
                0,
                0
        ).setAbilityType(AbilityType.OFFENSIVE)
         .setRequiredLevel(7)
         .setNumberOfHits(3);  // 3 missiles
    }

    public static List<AbilityVariant> createArcaneMissilesVariants() {
        List<AbilityVariant> variants = new ArrayList<>();

        // Variant A: Arcane Missiles (Default - 3 hits)
        variants.add(new AbilityVariant(
                "arcanemissiles_default",
                "Arcane Missiles",
                "Fire 3 arcane missiles",
                true
        )
        .withDamage(25)
        .withManaCost(22)
        .withNumberOfHits(3)
        .withDamageTypes("Arcane")
        .withUseCase("Multi-hit damage"));

        // Variant B: Arcane Barrage (5 hits, lower damage)
        variants.add(new AbilityVariant(
                "arcanemissiles_barrage",
                "Arcane Barrage",
                "Fire 5 weaker missiles",
                false
        )
        .withDamage(18)
        .withManaCost(28)
        .withNumberOfHits(5)
        .withDamageTypes("Arcane")
        .withUseCase("Proc-based builds"));

        // Variant C: Arcane Blast (Single huge hit)
        variants.add(new AbilityVariant(
                "arcanemissiles_blast",
                "Arcane Blast",
                "Single powerful arcane blast",
                false
        )
        .withDamage(110)
        .withManaCost(35)
        .withNumberOfHits(1)
        .withDamageTypes("Arcane")
        .withUseCase("Burst damage"));

        return variants;
    }

    public static List<AbilityTalent> createArcaneMissilesTalents() {
        List<AbilityTalent> talents = new ArrayList<>();

        // TIER 1
        talents.add(new AbilityTalent(
                "arcanemissiles_t1_power",
                "Arcane Power",
                "+20% damage per missile",
                "✨",
                TalentTier.TIER_1,
                new AbilityModifier().withDamageMultiplier(1.2)
        ));

        talents.add(new AbilityTalent(
                "arcanemissiles_t1_extra",
                "Extra Missile",
                "+1 missile fired",
                "🎯",
                TalentTier.TIER_1,
                new AbilityModifier().withCustomProperty("extra_hit", 1)
        ));

        talents.add(new AbilityTalent(
                "arcanemissiles_t1_efficient",
                "Arcane Efficiency",
                "-30% mana cost",
                "💧",
                TalentTier.TIER_1,
                new AbilityModifier().withManaCostMultiplier(0.7)
        ));

        // TIER 2
        talents.add(new AbilityTalent(
                "arcanemissiles_t2_chain",
                "Arcane Chain",
                "Each missile can chain to +1 enemy",
                "⚡",
                TalentTier.TIER_2,
                new AbilityModifier().withChain(1, 0.75)
        ));

        talents.add(new AbilityTalent(
                "arcanemissiles_t2_precision",
                "Missile Precision",
                "+25% crit chance",
                "💥",
                TalentTier.TIER_2,
                new AbilityModifier().withCritChanceBonus(25.0)
        ));

        talents.add(new AbilityTalent(
                "arcanemissiles_t2_amplify",
                "Arcane Amplification",
                "Each hit increases next hit by 10%",
                "📈",
                TalentTier.TIER_2,
                new AbilityModifier().withCustomProperty("amplification", true)
        ));

        // TIER 3
        talents.add(new AbilityTalent(
                "arcanemissiles_t3_barrage",
                "Missile Barrage",
                "+2 missiles",
                "🌟",
                TalentTier.TIER_3,
                new AbilityModifier().withCustomProperty("barrage_hits", 2)
        ));

        talents.add(new AbilityTalent(
                "arcanemissiles_t3_overload",
                "Arcane Overload",
                "Kill resets cooldown",
                "🔄",
                TalentTier.TIER_3,
                new AbilityModifier().withCooldownResetOnKill()
        ));

        talents.add(new AbilityTalent(
                "arcanemissiles_t3_lifesteal",
                "Arcane Absorption",
                "10% lifesteal per missile",
                "🩸",
                TalentTier.TIER_3,
                new AbilityModifier().withLifesteal(0.10)
        ));

        return talents;
    }

    /**
     * METEOR STRIKE - Massive AOE with cooldown
     */
    public static Abilitate createMeteorStrikeBase() {
        Map<String, Double> statInfluence = new HashMap<>();
        statInfluence.put("Intelligence", 0.8);

        return new Abilitate(
                "Meteor Strike",
                200,  // Massive damage
                List.of("Fire"),
                60,  // High mana cost
                3,   // 3 turn cooldown
                0,
                statInfluence,
                "Burn",
                3,
                20
        ).setAbilityType(AbilityType.OFFENSIVE)
         .setRequiredLevel(10)
         .setAOE(true);
    }

    public static List<AbilityVariant> createMeteorStrikeVariants() {
        List<AbilityVariant> variants = new ArrayList<>();

        // Variant A: Meteor Strike (Default - AOE)
        variants.add(new AbilityVariant(
                "meteor_default",
                "Meteor Strike",
                "Call down a meteor on all enemies",
                true
        )
        .withDamage(200)
        .withManaCost(60)
        .withCooldown(3)
        .asAOE(99, 1.0)
        .withDebuff("Burn", 3, 20)
        .withDamageTypes("Fire")
        .withUseCase("AOE burst"));

        // Variant B: Meteor Shower (Multiple smaller meteors)
        variants.add(new AbilityVariant(
                "meteor_shower",
                "Meteor Shower",
                "Rain multiple meteors",
                false
        )
        .withDamage(80)
        .withManaCost(50)
        .withCooldown(2)
        .withNumberOfHits(3)
        .asAOE(99, 1.0)
        .withDamageTypes("Fire")
        .withUseCase("Sustained AOE"));

        // Variant C: Comet (Single target devastation)
        variants.add(new AbilityVariant(
                "meteor_comet",
                "Comet",
                "Single target meteor",
                false
        )
        .withDamage(350)
        .withManaCost(70)
        .withCooldown(4)
        .withDebuff("Burn", 4, 30)
        .withDamageTypes("Fire")
        .withUseCase("Boss nuke"));

        return variants;
    }

    public static List<AbilityTalent> createMeteorStrikeTalents() {
        List<AbilityTalent> talents = new ArrayList<>();

        // TIER 1
        talents.add(new AbilityTalent(
                "meteor_t1_impact",
                "Impact Force",
                "+30% damage",
                "💥",
                TalentTier.TIER_1,
                new AbilityModifier().withDamageMultiplier(1.3)
        ));

        talents.add(new AbilityTalent(
                "meteor_t1_fastcast",
                "Rapid Casting",
                "-1 cooldown turn",
                "⚡",
                TalentTier.TIER_1,
                new AbilityModifier().withCooldownReduction(1)
        ));

        talents.add(new AbilityTalent(
                "meteor_t1_cheap",
                "Astral Focus",
                "-20% mana cost",
                "💧",
                TalentTier.TIER_1,
                new AbilityModifier().withManaCostMultiplier(0.8)
        ));

        // TIER 2
        talents.add(new AbilityTalent(
                "meteor_t2_inferno",
                "Inferno",
                "Burn damage doubled",
                "🔥",
                TalentTier.TIER_2,
                new AbilityModifier().withCustomProperty("inferno", true)
        ));

        talents.add(new AbilityTalent(
                "meteor_t2_stun",
                "Meteor Stun",
                "Stun all enemies for 1 turn",
                "💫",
                TalentTier.TIER_2,
                new AbilityModifier().withCustomProperty("meteor_stun", true)
        ));

        talents.add(new AbilityTalent(
                "meteor_t2_crit",
                "Devastating Impact",
                "+30% crit chance",
                "💥",
                TalentTier.TIER_2,
                new AbilityModifier().withCritChanceBonus(30.0)
        ));

        // TIER 3
        talents.add(new AbilityTalent(
                "meteor_t3_apocalypse",
                "Apocalypse",
                "+100% damage, +2 cooldown",
                "☄️",
                TalentTier.TIER_3,
                new AbilityModifier().withDamageMultiplier(2.0)
                        .withCustomProperty("apocalypse_cooldown", 2)
        ));

        talents.add(new AbilityTalent(
                "meteor_t3_reset",
                "Meteor Storm",
                "Kill resets cooldown",
                "🔄",
                TalentTier.TIER_3,
                new AbilityModifier().withCooldownResetOnKill()
        ));

        talents.add(new AbilityTalent(
                "meteor_t3_shield",
                "Flame Barrier",
                "Gain shield equal to damage dealt",
                "🛡️",
                TalentTier.TIER_3,
                new AbilityModifier()  // Special: shield
        ));

        return talents;
    }

    // ========================================================================
    // 🆕 NEW WARRIOR ABILITIES
    // ========================================================================

    /**
     * SHIELD BASH - Defensive stun using shield
     */
    public static Abilitate createShieldBashBase() {
        Map<String, Double> statInfluence = new HashMap<>();
        statInfluence.put("Strength", 0.6);

        return new Abilitate(
                "Shield Bash",
                80,  // Base damage
                List.of("Physical"),
                25,  // Rage cost
                0,   // No cooldown
                10,  // +10 hit chance
                statInfluence,
                "Stun",
                1,
                0
        ).setAbilityType(AbilityType.OFFENSIVE)
         .setRequiredLevel(4);
    }

    public static List<AbilityVariant> createShieldBashVariants() {
        List<AbilityVariant> variants = new ArrayList<>();

        // Variant A: Shield Bash (Default - Stun)
        variants.add(new AbilityVariant(
                "shieldbash_default",
                "Shield Bash",
                "Bash enemy with shield, stunning them",
                true
        )
        .withDamage(80)
        .withManaCost(25)
        .withDebuff("Stun", 1, 0)
        .withDamageTypes("Physical")
        .withUseCase("Control + damage"));

        // Variant B: Shield Slam (Higher damage, daze)
        variants.add(new AbilityVariant(
                "shieldbash_slam",
                "Shield Slam",
                "Powerful slam that dazes enemy",
                false
        )
        .withDamage(120)
        .withManaCost(35)
        .withDebuff("Dazed", 2, 0)
        .withDamageTypes("Physical")
        .withUseCase("Burst damage"));

        // Variant C: Shield Wall (AOE knockback)
        variants.add(new AbilityVariant(
                "shieldbash_wall",
                "Shield Wall",
                "Knock back all nearby enemies",
                false
        )
        .withDamage(50)
        .withManaCost(40)
        .asAOE(99, 1.0)
        .withDebuff("Slow", 2, 0)
        .withDamageTypes("Physical")
        .withUseCase("AOE control"));

        return variants;
    }

    public static List<AbilityTalent> createShieldBashTalents() {
        List<AbilityTalent> talents = new ArrayList<>();

        // TIER 1
        talents.add(new AbilityTalent(
                "shieldbash_t1_power",
                "Heavy Impact",
                "+30% damage",
                "💥",
                TalentTier.TIER_1,
                new AbilityModifier().withDamageMultiplier(1.3)
        ));

        talents.add(new AbilityTalent(
                "shieldbash_t1_duration",
                "Concussive Blow",
                "+1 stun duration",
                "💫",
                TalentTier.TIER_1,
                new AbilityModifier().withCustomProperty("concussive_blow", true)
        ));

        talents.add(new AbilityTalent(
                "shieldbash_t1_efficient",
                "Efficient Bash",
                "-30% rage cost",
                "⚡",
                TalentTier.TIER_1,
                new AbilityModifier().withManaCostMultiplier(0.7)
        ));

        // TIER 2
        talents.add(new AbilityTalent(
                "shieldbash_t2_armor",
                "Shield Expert",
                "Gain 20 armor for 2 turns",
                "🛡️",
                TalentTier.TIER_2,
                new AbilityModifier().withCustomProperty("shield_expert", true)
        ));

        talents.add(new AbilityTalent(
                "shieldbash_t2_heal",
                "Defensive Stance",
                "Heal 15% of damage dealt",
                "❤️",
                TalentTier.TIER_2,
                new AbilityModifier().withLifesteal(0.15)
        ));

        talents.add(new AbilityTalent(
                "shieldbash_t2_interrupt",
                "Interrupt",
                "Silence enemy for 1 turn",
                "🚫",
                TalentTier.TIER_2,
                new AbilityModifier().withCustomProperty("interrupt", true)
        ));

        // TIER 3
        talents.add(new AbilityTalent(
                "shieldbash_t3_reflect",
                "Retribution",
                "Reflect 30% damage back to attacker",
                "⚡",
                TalentTier.TIER_3,
                new AbilityModifier().withCustomProperty("retribution", 0.3)
        ));

        talents.add(new AbilityTalent(
                "shieldbash_t3_chain",
                "Chain Bash",
                "Hit chains to 2 additional enemies",
                "⛓️",
                TalentTier.TIER_3,
                new AbilityModifier().withChain(2, 0.6)
        ));

        talents.add(new AbilityTalent(
                "shieldbash_t3_rage",
                "Rage Generation",
                "Refund 50% rage on stun",
                "🔥",
                TalentTier.TIER_3,
                new AbilityModifier().withManaRefundOnKill(0.5)
        ));

        return talents;
    }

    /**
     * WHIRLWIND - AOE rage spender
     */
    public static Abilitate createWhirlwindBase() {
        Map<String, Double> statInfluence = new HashMap<>();
        statInfluence.put("Strength", 0.7);

        return new Abilitate(
                "Whirlwind",
                60,  // Base damage per target
                List.of("Physical"),
                40,  // High rage cost
                0,
                0,
                statInfluence,
                null,
                0,
                0
        ).setAbilityType(AbilityType.OFFENSIVE)
         .setRequiredLevel(6)
         .setAOE(true);
    }

    public static List<AbilityVariant> createWhirlwindVariants() {
        List<AbilityVariant> variants = new ArrayList<>();

        // Variant A: Whirlwind (Default - AOE)
        variants.add(new AbilityVariant(
                "whirlwind_default",
                "Whirlwind",
                "Spin attack hitting all enemies",
                true
        )
        .withDamage(60)
        .withManaCost(40)
        .asAOE(99, 1.0)
        .withDamageTypes("Physical")
        .withUseCase("AOE damage"));

        // Variant B: Bladestorm (More hits, lower damage)
        variants.add(new AbilityVariant(
                "whirlwind_bladestorm",
                "Bladestorm",
                "Rapid spins hitting enemies 3 times",
                false
        )
        .withDamage(25)
        .withManaCost(45)
        .withNumberOfHits(3)
        .asAOE(99, 1.0)
        .withDamageTypes("Physical")
        .withUseCase("Multi-hit AOE"));

        // Variant C: Cleave (Fewer targets, more damage)
        variants.add(new AbilityVariant(
                "whirlwind_cleave",
                "Cleave",
                "Heavy swing hitting 3 targets",
                false
        )
        .withDamage(110)
        .withManaCost(35)
        .asAOE(3, 1.0)
        .withDamageTypes("Physical")
        .withUseCase("Limited AOE burst"));

        return variants;
    }

    public static List<AbilityTalent> createWhirlwindTalents() {
        List<AbilityTalent> talents = new ArrayList<>();

        // TIER 1
        talents.add(new AbilityTalent(
                "whirlwind_t1_power",
                "Momentum",
                "+25% damage",
                "💪",
                TalentTier.TIER_1,
                new AbilityModifier().withDamageMultiplier(1.25)
        ));

        talents.add(new AbilityTalent(
                "whirlwind_t1_bleed",
                "Rending Strikes",
                "Apply bleed (8 damage, 3 turns)",
                "🩸",
                TalentTier.TIER_1,
                new AbilityModifier().withBleed(8, 3)
        ));

        talents.add(new AbilityTalent(
                "whirlwind_t1_efficient",
                "Controlled Fury",
                "-25% rage cost",
                "⚡",
                TalentTier.TIER_1,
                new AbilityModifier().withManaCostMultiplier(0.75)
        ));

        // TIER 2
        talents.add(new AbilityTalent(
                "whirlwind_t2_crit",
                "Precise Strikes",
                "+20% crit chance",
                "🎯",
                TalentTier.TIER_2,
                new AbilityModifier().withCritChanceBonus(20.0)
        ));

        talents.add(new AbilityTalent(
                "whirlwind_t2_heal",
                "Battle Trance",
                "5% lifesteal per enemy hit",
                "❤️",
                TalentTier.TIER_2,
                new AbilityModifier().withLifesteal(0.05)
        ));

        talents.add(new AbilityTalent(
                "whirlwind_t2_armor_shred",
                "Sunder Armor",
                "Reduce armor by 10 for 3 turns",
                "🛡️",
                TalentTier.TIER_2,
                new AbilityModifier().withArmorReduction(10, 3)
        ));

        // TIER 3
        talents.add(new AbilityTalent(
                "whirlwind_t3_double",
                "Double Spin",
                "Hit enemies twice",
                "🌪️",
                TalentTier.TIER_3,
                new AbilityModifier().withCustomProperty("double_hit", true)
        ));

        talents.add(new AbilityTalent(
                "whirlwind_t3_reset",
                "Endless Rage",
                "Kill resets cooldown",
                "🔄",
                TalentTier.TIER_3,
                new AbilityModifier().withCooldownResetOnKill()
        ));

        talents.add(new AbilityTalent(
                "whirlwind_t3_execute",
                "Execute Weakness",
                "+100% damage to enemies below 30% HP",
                "💀",
                TalentTier.TIER_3,
                new AbilityModifier().withCustomProperty("execute_scaling", true)
        ));

        return talents;
    }

    /**
     * EXECUTE - High damage finisher for low HP enemies
     */
    public static Abilitate createExecuteBase() {
        Map<String, Double> statInfluence = new HashMap<>();
        statInfluence.put("Strength", 1.0);

        return new Abilitate(
                "Execute",
                150,  // High base damage
                List.of("Physical"),
                50,  // High rage cost
                2,   // 2 turn cooldown
                0,
                statInfluence,
                null,
                0,
                0
        ).setAbilityType(AbilityType.OFFENSIVE)
         .setRequiredLevel(9);
    }

    public static List<AbilityVariant> createExecuteVariants() {
        List<AbilityVariant> variants = new ArrayList<>();

        // Variant A: Execute (Default - scales with missing HP)
        variants.add(new AbilityVariant(
                "execute_default",
                "Execute",
                "Massive damage to low HP enemies",
                true
        )
        .withDamage(150)
        .withManaCost(50)
        .withCooldown(2)
        .withDamageTypes("Physical")
        .withUseCase("Execute finisher"));

        // Variant B: Mortal Strike (Healing reduction)
        variants.add(new AbilityVariant(
                "execute_mortal",
                "Mortal Strike",
                "Heavy hit reducing enemy healing",
                false
        )
        .withDamage(130)
        .withManaCost(45)
        .withCooldown(1)
        .withDebuff("Healing_Reduced", 3, 0)
        .withDamageTypes("Physical")
        .withUseCase("Anti-heal"));

        // Variant C: Rampage (Multi-target execute)
        variants.add(new AbilityVariant(
                "execute_rampage",
                "Rampage",
                "Execute all low HP enemies",
                false
        )
        .withDamage(100)
        .withManaCost(60)
        .withCooldown(3)
        .asAOE(99, 1.0)
        .withDamageTypes("Physical")
        .withUseCase("AOE execute"));

        return variants;
    }

    public static List<AbilityTalent> createExecuteTalents() {
        List<AbilityTalent> talents = new ArrayList<>();

        // TIER 1
        talents.add(new AbilityTalent(
                "execute_t1_power",
                "Killing Blow",
                "+35% damage",
                "⚔️",
                TalentTier.TIER_1,
                new AbilityModifier().withDamageMultiplier(1.35)
        ));

        talents.add(new AbilityTalent(
                "execute_t1_threshold",
                "Early Execute",
                "Can use at 40% HP (instead of 20%)",
                "💀",
                TalentTier.TIER_1,
                new AbilityModifier().withCustomProperty("early_execute", 0.4)
        ));

        talents.add(new AbilityTalent(
                "execute_t1_efficient",
                "Efficient Kill",
                "-30% rage cost",
                "⚡",
                TalentTier.TIER_1,
                new AbilityModifier().withManaCostMultiplier(0.7)
        ));

        // TIER 2
        talents.add(new AbilityTalent(
                "execute_t2_crit",
                "Executioner's Precision",
                "+40% crit chance",
                "🎯",
                TalentTier.TIER_2,
                new AbilityModifier().withCritChanceBonus(40.0)
        ));

        talents.add(new AbilityTalent(
                "execute_t2_bleed",
                "Grievous Wounds",
                "Apply massive bleed (20, 3 turns)",
                "🩸",
                TalentTier.TIER_2,
                new AbilityModifier().withBleed(20, 3)
        ));

        talents.add(new AbilityTalent(
                "execute_t2_fast",
                "Rapid Execution",
                "-1 cooldown turn",
                "⏱️",
                TalentTier.TIER_2,
                new AbilityModifier().withCooldownReduction(1)
        ));

        // TIER 3
        talents.add(new AbilityTalent(
                "execute_t3_scaling",
                "Sudden Death",
                "Damage scales 200% with missing HP",
                "💀",
                TalentTier.TIER_3,
                new AbilityModifier().withCustomProperty("sudden_death", true)
        ));

        talents.add(new AbilityTalent(
                "execute_t3_reset",
                "Fresh Meat",
                "Kill resets cooldown and refunds rage",
                "🔄",
                TalentTier.TIER_3,
                new AbilityModifier().withCooldownResetOnKill()
                        .withManaRefundOnKill(1.0)
        ));

        talents.add(new AbilityTalent(
                "execute_t3_cleave",
                "Cleaving Execute",
                "Chains to 2 additional low HP enemies",
                "⚡",
                TalentTier.TIER_3,
                new AbilityModifier().withChain(2, 0.8)
        ));

        return talents;
    }

    // ========================================================================
    // 🆕 NEW ROGUE ABILITIES
    // ========================================================================

    /**
     * BACKSTAB - High crit stealth attack
     */
    public static Abilitate createBackstabBase() {
        Map<String, Double> statInfluence = new HashMap<>();
        statInfluence.put("Dexterity", 0.8);

        return new Abilitate(
                "Backstab",
                100,  // High base damage
                List.of("Physical"),
                30,  // Energy cost
                0,
                15,  // High hit chance bonus
                statInfluence,
                null,
                0,
                0
        ).setAbilityType(AbilityType.OFFENSIVE)
         .setRequiredLevel(3);
    }

    public static List<AbilityVariant> createBackstabVariants() {
        List<AbilityVariant> variants = new ArrayList<>();

        // Variant A: Backstab (Default - High crit)
        variants.add(new AbilityVariant(
                "backstab_default",
                "Backstab",
                "Strike from shadows with high crit",
                true
        )
        .withDamage(100)
        .withManaCost(30)
        .withDamageTypes("Physical")
        .withUseCase("Burst crit damage"));

        // Variant B: Ambush (Guaranteed crit from stealth)
        variants.add(new AbilityVariant(
                "backstab_ambush",
                "Ambush",
                "Guaranteed crit, requires stealth",
                false
        )
        .withDamage(140)
        .withManaCost(45)
        .withDamageTypes("Physical")
        .withUseCase("Stealth opener"));

        // Variant C: Cheap Shot (Stun + damage)
        variants.add(new AbilityVariant(
                "backstab_cheapshot",
                "Cheap Shot",
                "Strike and stun enemy",
                false
        )
        .withDamage(75)
        .withManaCost(35)
        .withDebuff("Stun", 1, 0)
        .withDamageTypes("Physical")
        .withUseCase("Control + damage"));

        return variants;
    }

    public static List<AbilityTalent> createBackstabTalents() {
        List<AbilityTalent> talents = new ArrayList<>();

        // TIER 1
        talents.add(new AbilityTalent(
                "backstab_t1_power",
                "Find Weakness",
                "+30% damage",
                "🗡️",
                TalentTier.TIER_1,
                new AbilityModifier().withDamageMultiplier(1.3)
        ));

        talents.add(new AbilityTalent(
                "backstab_t1_crit",
                "Ruthlessness",
                "+30% crit chance",
                "💥",
                TalentTier.TIER_1,
                new AbilityModifier().withCritChanceBonus(30.0)
        ));

        talents.add(new AbilityTalent(
                "backstab_t1_efficient",
                "Silent Technique",
                "-25% energy cost",
                "⚡",
                TalentTier.TIER_1,
                new AbilityModifier().withManaCostMultiplier(0.75)
        ));

        // TIER 2
        talents.add(new AbilityTalent(
                "backstab_t2_bleed",
                "Serrated Blade",
                "Apply heavy bleed (15, 3 turns)",
                "🩸",
                TalentTier.TIER_2,
                new AbilityModifier().withBleed(15, 3)
        ));

        talents.add(new AbilityTalent(
                "backstab_t2_armor",
                "Expose Armor",
                "Reduce armor by 20 for 3 turns",
                "🛡️",
                TalentTier.TIER_2,
                new AbilityModifier().withArmorReduction(20, 3)
        ));

        talents.add(new AbilityTalent(
                "backstab_t2_multistrike",
                "Combo Strike",
                "Hit twice",
                "⚔️",
                TalentTier.TIER_2,
                new AbilityModifier().withCustomProperty("double_hit", true)
        ));

        // TIER 3
        talents.add(new AbilityTalent(
                "backstab_t3_critdamage",
                "Deadly Precision",
                "Crits deal +150% damage",
                "💀",
                TalentTier.TIER_3,
                new AbilityModifier().withCustomProperty("deadly_precision", true)
        ));

        talents.add(new AbilityTalent(
                "backstab_t3_reset",
                "Silent Killer",
                "Kill resets cooldown and refunds energy",
                "🔄",
                TalentTier.TIER_3,
                new AbilityModifier().withCooldownResetOnKill()
                        .withManaRefundOnKill(1.0)
        ));

        talents.add(new AbilityTalent(
                "backstab_t3_heal",
                "Predator",
                "Heal for 30% of crit damage",
                "🩸",
                TalentTier.TIER_3,
                new AbilityModifier().withLifesteal(0.30)
        ));

        return talents;
    }

    /**
     * POISON BLADE - Stacking poison DOT
     */
    public static Abilitate createPoisonBladeBase() {
        Map<String, Double> statInfluence = new HashMap<>();
        statInfluence.put("Dexterity", 0.5);

        return new Abilitate(
                "Poison Blade",
                50,  // Moderate damage
                List.of("Poison"),
                20,  // Energy cost
                0,
                5,
                statInfluence,
                "Poison",
                4,
                12
        ).setAbilityType(AbilityType.OFFENSIVE)
         .setRequiredLevel(5);
    }

    public static List<AbilityVariant> createPoisonBladeVariants() {
        List<AbilityVariant> variants = new ArrayList<>();

        // Variant A: Poison Blade (Default - Stacking poison)
        variants.add(new AbilityVariant(
                "poisonblade_default",
                "Poison Blade",
                "Apply stacking poison",
                true
        )
        .withDamage(50)
        .withManaCost(20)
        .withDebuff("Poison", 4, 12)
        .withDamageTypes("Poison")
        .withUseCase("DOT stacking"));

        // Variant B: Deadly Poison (Higher DOT)
        variants.add(new AbilityVariant(
                "poisonblade_deadly",
                "Deadly Poison",
                "Apply powerful poison",
                false
        )
        .withDamage(40)
        .withManaCost(25)
        .withDebuff("Poison", 5, 20)
        .withDamageTypes("Poison")
        .withUseCase("High DOT"));

        // Variant C: Envenom (Burst poison damage)
        variants.add(new AbilityVariant(
                "poisonblade_envenom",
                "Envenom",
                "Consume poisons for instant damage",
                false
        )
        .withDamage(150)
        .withManaCost(35)
        .withDamageTypes("Poison")
        .withUseCase("Poison burst"));

        return variants;
    }

    public static List<AbilityTalent> createPoisonBladeTalents() {
        List<AbilityTalent> talents = new ArrayList<>();

        // TIER 1
        talents.add(new AbilityTalent(
                "poisonblade_t1_potency",
                "Deadly Toxin",
                "+40% poison damage",
                "☠️",
                TalentTier.TIER_1,
                new AbilityModifier().withCustomProperty("deadly_toxin", true)
        ));

        talents.add(new AbilityTalent(
                "poisonblade_t1_duration",
                "Lingering Poison",
                "+2 poison duration",
                "⏱️",
                TalentTier.TIER_1,
                new AbilityModifier().withCustomProperty("lingering_poison", true)
        ));

        talents.add(new AbilityTalent(
                "poisonblade_t1_efficient",
                "Efficient Coating",
                "-30% energy cost",
                "⚡",
                TalentTier.TIER_1,
                new AbilityModifier().withManaCostMultiplier(0.7)
        ));

        // TIER 2
        talents.add(new AbilityTalent(
                "poisonblade_t2_stack",
                "Toxic Buildup",
                "Poison stacks 2x faster",
                "💚",
                TalentTier.TIER_2,
                new AbilityModifier().withCustomProperty("toxic_buildup", true)
        ));

        talents.add(new AbilityTalent(
                "poisonblade_t2_spread",
                "Contagion",
                "Poison spreads to nearby enemies",
                "🦠",
                TalentTier.TIER_2,
                new AbilityModifier().withCustomProperty("contagion", true)
        ));

        talents.add(new AbilityTalent(
                "poisonblade_t2_crit",
                "Toxic Injection",
                "+25% crit chance",
                "🎯",
                TalentTier.TIER_2,
                new AbilityModifier().withCritChanceBonus(25.0)
        ));

        // TIER 3
        talents.add(new AbilityTalent(
                "poisonblade_t3_deadly",
                "Venomous Wounds",
                "Poison can crit",
                "💀",
                TalentTier.TIER_3,
                new AbilityModifier().withCustomProperty("poison_crit", true)
        ));

        talents.add(new AbilityTalent(
                "poisonblade_t3_refresh",
                "Master Poisoner",
                "Refresh all poison durations on kill",
                "🔄",
                TalentTier.TIER_3,
                new AbilityModifier().withCustomProperty("master_poisoner", true)
        ));

        talents.add(new AbilityTalent(
                "poisonblade_t3_explosion",
                "Noxious Cloud",
                "Poisoned enemies explode on death",
                "💥",
                TalentTier.TIER_3,
                new AbilityModifier().withExplosionOnKill(80)
        ));

        return talents;
    }

    /**
     * SHADOW STEP - Mobility + damage
     */
    public static Abilitate createShadowStepBase() {
        Map<String, Double> statInfluence = new HashMap<>();
        statInfluence.put("Dexterity", 0.6);

        return new Abilitate(
                "Shadow Step",
                80,  // Moderate damage
                List.of("Shadow"),
                35,  // Energy cost
                1,   // 1 turn cooldown
                20,  // High hit chance
                statInfluence,
                null,
                0,
                0
        ).setAbilityType(AbilityType.OFFENSIVE)
         .setRequiredLevel(8);
    }

    public static List<AbilityVariant> createShadowStepVariants() {
        List<AbilityVariant> variants = new ArrayList<>();

        // Variant A: Shadow Step (Default - Teleport + damage)
        variants.add(new AbilityVariant(
                "shadowstep_default",
                "Shadow Step",
                "Teleport to enemy and strike",
                true
        )
        .withDamage(80)
        .withManaCost(35)
        .withCooldown(1)
        .withDamageTypes("Shadow")
        .withUseCase("Mobility + damage"));

        // Variant B: Shadowstrike (Ambush from shadows)
        variants.add(new AbilityVariant(
                "shadowstep_strike",
                "Shadowstrike",
                "Teleport and strike with high crit",
                false
        )
        .withDamage(120)
        .withManaCost(40)
        .withCooldown(2)
        .withDamageTypes("Shadow")
        .withUseCase("Burst teleport"));

        // Variant C: Shadowdance (AOE teleport)
        variants.add(new AbilityVariant(
                "shadowstep_dance",
                "Shadowdance",
                "Blink through enemies hitting all",
                false
        )
        .withDamage(55)
        .withManaCost(50)
        .withCooldown(2)
        .asAOE(99, 1.0)
        .withDamageTypes("Shadow")
        .withUseCase("AOE mobility"));

        return variants;
    }

    public static List<AbilityTalent> createShadowStepTalents() {
        List<AbilityTalent> talents = new ArrayList<>();

        // TIER 1
        talents.add(new AbilityTalent(
                "shadowstep_t1_power",
                "Shadow Blade",
                "+30% damage",
                "🌑",
                TalentTier.TIER_1,
                new AbilityModifier().withDamageMultiplier(1.3)
        ));

        talents.add(new AbilityTalent(
                "shadowstep_t1_fastcast",
                "Quick Shadow",
                "-1 cooldown turn",
                "⚡",
                TalentTier.TIER_1,
                new AbilityModifier().withCooldownReduction(1)
        ));

        talents.add(new AbilityTalent(
                "shadowstep_t1_efficient",
                "Shadow Mastery",
                "-30% energy cost",
                "💫",
                TalentTier.TIER_1,
                new AbilityModifier().withManaCostMultiplier(0.7)
        ));

        // TIER 2
        talents.add(new AbilityTalent(
                "shadowstep_t2_dodge",
                "Evasive Shadow",
                "+30% dodge for 2 turns",
                "💨",
                TalentTier.TIER_2,
                new AbilityModifier().withCustomProperty("evasive_shadow", true)
        ));

        talents.add(new AbilityTalent(
                "shadowstep_t2_crit",
                "Shadow Assassin",
                "+35% crit chance",
                "🎯",
                TalentTier.TIER_2,
                new AbilityModifier().withCritChanceBonus(35.0)
        ));

        talents.add(new AbilityTalent(
                "shadowstep_t2_chain",
                "Shadow Chain",
                "Chain to 2 additional enemies",
                "⛓️",
                TalentTier.TIER_2,
                new AbilityModifier().withChain(2, 0.7)
        ));

        // TIER 3
        talents.add(new AbilityTalent(
                "shadowstep_t3_double",
                "Shadow Clone",
                "Strike twice with shadow clone",
                "👤",
                TalentTier.TIER_3,
                new AbilityModifier().withCustomProperty("double_hit", true)
        ));

        talents.add(new AbilityTalent(
                "shadowstep_t3_reset",
                "Shadow Master",
                "Kill resets cooldown",
                "🔄",
                TalentTier.TIER_3,
                new AbilityModifier().withCooldownResetOnKill()
        ));

        talents.add(new AbilityTalent(
                "shadowstep_t3_heal",
                "Shadow Drain",
                "20% lifesteal + remove debuffs",
                "🩸",
                TalentTier.TIER_3,
                new AbilityModifier().withLifesteal(0.20)
                        .withCustomProperty("shadow_drain", true)
        ));

        return talents;
    }

    // ========================================================================
    // Utility Methods
    // ========================================================================

    /**
     * Creates a fully configured ability with default variant and no talents.
     */
    public static ConfiguredAbility createDefaultConfiguredAbility(
            Abilitate base,
            List<AbilityVariant> variants,
            List<AbilityTalent> talents
    ) {
        // Find default variant
        AbilityVariant defaultVariant = variants.stream()
                .filter(AbilityVariant::isDefault)
                .findFirst()
                .orElse(variants.get(0));

        return new ConfiguredAbility(base, defaultVariant);
    }

    /**
     * Creates all example abilities (for testing/demo).
     */
    public static Map<String, ConfiguredAbility> createAllExampleAbilities() {
        Map<String, ConfiguredAbility> abilities = new HashMap<>();

        // Fireball
        abilities.put("Fireball", createDefaultConfiguredAbility(
                createFireballBase(),
                createFireballVariants(),
                createFireballTalents()
        ));

        // Cleave
        abilities.put("Cleave", createDefaultConfiguredAbility(
                createCleaveBase(),
                createCleaveVariants(),
                createCleaveTalents()
        ));

        // Lightning Bolt
        abilities.put("Lightning Bolt", createDefaultConfiguredAbility(
                createLightningBoltBase(),
                createLightningBoltVariants(),
                createLightningBoltTalents()
        ));

        // 🆕 NEW WIZARD ABILITIES
        abilities.put("Ice Shard", createDefaultConfiguredAbility(
                createIceShardBase(),
                createIceShardVariants(),
                createIceShardTalents()
        ));

        abilities.put("Arcane Missiles", createDefaultConfiguredAbility(
                createArcaneMissilesBase(),
                createArcaneMissilesVariants(),
                createArcaneMissilesTalents()
        ));

        abilities.put("Meteor Strike", createDefaultConfiguredAbility(
                createMeteorStrikeBase(),
                createMeteorStrikeVariants(),
                createMeteorStrikeTalents()
        ));

        // 🆕 NEW WARRIOR ABILITIES
        abilities.put("Shield Bash", createDefaultConfiguredAbility(
                createShieldBashBase(),
                createShieldBashVariants(),
                createShieldBashTalents()
        ));

        abilities.put("Whirlwind", createDefaultConfiguredAbility(
                createWhirlwindBase(),
                createWhirlwindVariants(),
                createWhirlwindTalents()
        ));

        abilities.put("Execute", createDefaultConfiguredAbility(
                createExecuteBase(),
                createExecuteVariants(),
                createExecuteTalents()
        ));

        // 🆕 NEW ROGUE ABILITIES
        abilities.put("Backstab", createDefaultConfiguredAbility(
                createBackstabBase(),
                createBackstabVariants(),
                createBackstabTalents()
        ));

        abilities.put("Poison Blade", createDefaultConfiguredAbility(
                createPoisonBladeBase(),
                createPoisonBladeVariants(),
                createPoisonBladeTalents()
        ));

        abilities.put("Shadow Step", createDefaultConfiguredAbility(
                createShadowStepBase(),
                createShadowStepVariants(),
                createShadowStepTalents()
        ));

        return abilities;
    }

    /**
     * Gets all variants for a specific ability.
     */
    public static List<AbilityVariant> getVariantsForAbility(String abilityName) {
        return switch (abilityName) {
            // Wizard
            case "Fireball" -> createFireballVariants();
            case "Lightning Bolt" -> createLightningBoltVariants();
            case "Ice Shard" -> createIceShardVariants();
            case "Arcane Missiles" -> createArcaneMissilesVariants();
            case "Meteor Strike" -> createMeteorStrikeVariants();
            // Warrior
            case "Cleave" -> createCleaveVariants();
            case "Shield Bash" -> createShieldBashVariants();
            case "Whirlwind" -> createWhirlwindVariants();
            case "Execute" -> createExecuteVariants();
            // Rogue
            case "Backstab" -> createBackstabVariants();
            case "Poison Blade" -> createPoisonBladeVariants();
            case "Shadow Step" -> createShadowStepVariants();
            default -> new ArrayList<>();
        };
    }

    /**
     * Gets all talents for a specific ability.
     */
    public static List<AbilityTalent> getTalentsForAbility(String abilityName) {
        return switch (abilityName) {
            // Wizard
            case "Fireball" -> createFireballTalents();
            case "Lightning Bolt" -> createLightningBoltTalents();
            case "Ice Shard" -> createIceShardTalents();
            case "Arcane Missiles" -> createArcaneMissilesTalents();
            case "Meteor Strike" -> createMeteorStrikeTalents();
            // Warrior
            case "Cleave" -> createCleaveTalents();
            case "Shield Bash" -> createShieldBashTalents();
            case "Whirlwind" -> createWhirlwindTalents();
            case "Execute" -> createExecuteTalents();
            // Rogue
            case "Backstab" -> createBackstabTalents();
            case "Poison Blade" -> createPoisonBladeTalents();
            case "Shadow Step" -> createShadowStepTalents();
            default -> new ArrayList<>();
        };
    }

    /**
     * Gets talents for a specific tier of an ability.
     */
    public static List<AbilityTalent> getTalentsForTier(String abilityName, TalentTier tier) {
        List<AbilityTalent> allTalents = getTalentsForAbility(abilityName);
        return allTalents.stream()
                .filter(t -> t.getTier() == tier)
                .toList();
    }
}
