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
                new AbilityModifier()  // Special effect
        ));

        talents.add(new AbilityTalent(
                "iceshard_t3_permafrost",
                "Permafrost",
                "Slow duration doubled",
                "❄️",
                TalentTier.TIER_3,
                new AbilityModifier()  // Special effect
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
                new AbilityModifier()  // Special: +1 hit
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
                new AbilityModifier()  // Special ramp
        ));

        // TIER 3
        talents.add(new AbilityTalent(
                "arcanemissiles_t3_barrage",
                "Missile Barrage",
                "+2 missiles",
                "🌟",
                TalentTier.TIER_3,
                new AbilityModifier()  // Special: +2 hits
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
                new AbilityModifier()  // Special: double burn
        ));

        talents.add(new AbilityTalent(
                "meteor_t2_stun",
                "Meteor Stun",
                "Stun all enemies for 1 turn",
                "💫",
                TalentTier.TIER_2,
                new AbilityModifier()  // Special: AOE stun
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
