package com.rpg.test;

import com.rpg.model.abilities.*;
import com.rpg.model.characters.Erou;
import com.rpg.model.characters.classes.Ardelean;

import java.util.List;
import java.util.Map;

/**
 * Quick demo that can be called from the main menu or character creation.
 * Shows a simple example of the ability system in action.
 */
public class QuickAbilityDemo {

    /**
     * Run a quick demonstration of the ability system.
     */
    public static void runDemo() {
        System.out.println("\n╔════════════════════════════════════════════════════╗");
        System.out.println("║     🎮 ABILITY CUSTOMIZATION SYSTEM DEMO 🎮      ║");
        System.out.println("╚════════════════════════════════════════════════════╝\n");

        // Create a wizard
        Erou wizard = new Ardelean("Demo Wizard");
        System.out.println("Created: " + wizard.getNume() + " (Level " + wizard.getNivel() + ")");
        System.out.println("Intelligence: " + wizard.getIntelligenceTotal() + "\n");

        // Unlock Fireball
        Map<String, ConfiguredAbility> abilities = AbilityDefinitions.createAllExampleAbilities();
        ConfiguredAbility fireball = abilities.get("Fireball");
        wizard.unlockConfiguredAbility(fireball);

        System.out.println("═══ ABILITY UNLOCKED ═══");
        System.out.println("🔥 Fireball");
        System.out.println("   Damage: " + fireball.getFinalDamage());
        System.out.println("   Mana Cost: " + fireball.getFinalManaCost());
        System.out.println("   Variant: " + fireball.getSelectedVariant().getName());
        System.out.println();

        // Show all 3 variants
        System.out.println("═══ AVAILABLE VARIANTS ═══");
        List<AbilityVariant> variants = AbilityDefinitions.getVariantsForAbility("Fireball");
        for (int i = 0; i < variants.size(); i++) {
            AbilityVariant v = variants.get(i);
            System.out.println((i + 1) + ". " + v.getName());
            System.out.println("   " + v.getDescription());
            System.out.println("   Damage: " + v.getBaseDamage() + ", Mana: " + v.getManaCost());
            if (v.isAOE()) {
                System.out.println("   AOE: Hits " + v.getNumberOfTargets() + " enemies");
            }
            System.out.println();
        }

        // Apply talents
        System.out.println("═══ CUSTOMIZING WITH TALENTS ═══");
        List<AbilityTalent> tier1 = AbilityDefinitions.getTalentsForTier("Fireball", TalentTier.TIER_1);
        List<AbilityTalent> tier2 = AbilityDefinitions.getTalentsForTier("Fireball", TalentTier.TIER_2);
        List<AbilityTalent> tier3 = AbilityDefinitions.getTalentsForTier("Fireball", TalentTier.TIER_3);

        fireball.setTier1Talent(tier1.get(0));  // Intense Heat (+20% damage)
        fireball.setTier2Talent(tier2.get(1));  // Chain Fire
        fireball.setTier3Talent(tier3.get(0));  // Explosive

        System.out.println("Applied Talents:");
        System.out.println("  T1: " + tier1.get(0).getName() + " - " + tier1.get(0).getDescription());
        System.out.println("  T2: " + tier2.get(1).getName() + " - " + tier2.get(1).getDescription());
        System.out.println("  T3: " + tier3.get(0).getName() + " - " + tier3.get(0).getDescription());
        System.out.println();

        System.out.println("═══ FINAL STATS ═══");
        System.out.println("🔥 Customized Fireball");
        System.out.println("   Damage: " + fireball.getFinalDamage() + " (+20% from Intense Heat)");
        System.out.println("   Mana Cost: " + fireball.getFinalManaCost());
        System.out.println("   Effect: 25% chance to chain to 2nd enemy");
        System.out.println("   Effect: Explodes for 30 AOE damage on kill");
        System.out.println();

        // Add to loadout
        wizard.addAbilityToLoadout("Fireball");
        System.out.println("═══ LOADOUT ═══");
        System.out.println("Added to active loadout: " + wizard.getLoadoutSize() + "/6 abilities");
        System.out.println();

        System.out.println("╔════════════════════════════════════════════════════╗");
        System.out.println("║  ✅ This ability is now ready for combat!         ║");
        System.out.println("║  • Discover 20+ abilities through gameplay        ║");
        System.out.println("║  • Customize each with variants & talents         ║");
        System.out.println("║  • Build your perfect 6-ability loadout           ║");
        System.out.println("╚════════════════════════════════════════════════════╝\n");
    }

    /**
     * Initialize a new hero with starting abilities.
     * Call this during character creation.
     */
    public static void initializeStartingAbilities(Erou hero) {
        // Give the hero their starting ability based on class
        Map<String, ConfiguredAbility> exampleAbilities = AbilityDefinitions.createAllExampleAbilities();

        // Wizards start with Fireball
        if (hero instanceof Ardelean) {
            ConfiguredAbility fireball = exampleAbilities.get("Fireball");
            hero.unlockConfiguredAbility(fireball);
            hero.addAbilityToLoadout("Fireball");
            System.out.println("✨ Starting Ability: Fireball unlocked!");
        }
        // Warriors start with Cleave
        else if (hero.getClass().getSimpleName().equals("Moldovean")) {
            ConfiguredAbility cleave = exampleAbilities.get("Cleave");
            hero.unlockConfiguredAbility(cleave);
            hero.addAbilityToLoadout("Cleave");
            System.out.println("✨ Starting Ability: Cleave unlocked!");
        }
        // Rogues start with... (Lightning for now)
        else {
            ConfiguredAbility lightning = exampleAbilities.get("Lightning Bolt");
            hero.unlockConfiguredAbility(lightning);
            hero.addAbilityToLoadout("Lightning Bolt");
            System.out.println("✨ Starting Ability: Lightning Bolt unlocked!");
        }
    }

    /**
     * Show a comparison of different builds for the same ability.
     */
    public static void showBuildComparison() {
        System.out.println("\n═══ BUILD COMPARISON: FIREBALL ═══\n");

        // Build 1: AOE Farmer
        Abilitate base1 = AbilityDefinitions.createFireballBase();
        List<AbilityVariant> variants = AbilityDefinitions.createFireballVariants();
        ConfiguredAbility aoeFarmer = new ConfiguredAbility(base1, variants.get(1)); // Firestorm

        List<AbilityTalent> tier1 = AbilityDefinitions.getTalentsForTier("Fireball", TalentTier.TIER_1);
        List<AbilityTalent> tier2 = AbilityDefinitions.getTalentsForTier("Fireball", TalentTier.TIER_2);
        List<AbilityTalent> tier3 = AbilityDefinitions.getTalentsForTier("Fireball", TalentTier.TIER_3);

        aoeFarmer.setTier1Talent(tier1.get(2)); // Mana Efficient
        aoeFarmer.setTier2Talent(tier2.get(1)); // Chain Fire
        aoeFarmer.setTier3Talent(tier3.get(1)); // Cooldown Reset

        System.out.println("🌾 AOE Farmer Build:");
        System.out.println("   Variant: Firestorm (hits all enemies)");
        System.out.println("   Damage: " + aoeFarmer.getFinalDamage() + " per enemy");
        System.out.println("   Mana: " + aoeFarmer.getFinalManaCost());
        System.out.println("   Strategy: Spam AOE, kills refund mana");
        System.out.println();

        // Build 2: Boss Killer
        Abilitate base2 = AbilityDefinitions.createFireballBase();
        ConfiguredAbility bossKiller = new ConfiguredAbility(base2, variants.get(2)); // Inferno Bolt

        bossKiller.setTier1Talent(tier1.get(0)); // Intense Heat
        bossKiller.setTier2Talent(tier2.get(2)); // Precision
        bossKiller.setTier3Talent(tier3.get(2)); // Flame Shield

        System.out.println("👑 Boss Killer Build:");
        System.out.println("   Variant: Inferno Bolt (high burst)");
        System.out.println("   Damage: " + bossKiller.getFinalDamage());
        System.out.println("   Mana: " + bossKiller.getFinalManaCost());
        System.out.println("   Cooldown: 2 turns");
        System.out.println("   Strategy: Massive burst with crit bonus + defense");
        System.out.println();

        // Build 3: Balanced
        Abilitate base3 = AbilityDefinitions.createFireballBase();
        ConfiguredAbility balanced = new ConfiguredAbility(base3, variants.get(0)); // Default

        balanced.setTier1Talent(tier1.get(0)); // Intense Heat
        balanced.setTier2Talent(tier2.get(0)); // Melt Armor
        balanced.setTier3Talent(tier3.get(0)); // Explosive

        System.out.println("⚖️ Balanced Build:");
        System.out.println("   Variant: Fireball (single target + burn)");
        System.out.println("   Damage: " + balanced.getFinalDamage());
        System.out.println("   Mana: " + balanced.getFinalManaCost());
        System.out.println("   Strategy: Reliable damage with armor shred + AOE on kill");
        System.out.println();
    }
}
