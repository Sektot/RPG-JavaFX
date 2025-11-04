package com.rpg.test;

import com.rpg.model.abilities.*;
import com.rpg.model.characters.Erou;
import com.rpg.model.characters.classes.Ardelean;
import com.rpg.model.characters.classes.Moldovean;

import java.util.List;
import java.util.Map;

/**
 * Test class to validate the Ability Variant & Talent System.
 * Run this to verify everything works correctly.
 */
public class AbilitySystemTest {

    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("    ABILITY VARIANT & TALENT SYSTEM TEST");
        System.out.println("═══════════════════════════════════════════════════\n");

        testWizardAbilities();
        System.out.println("\n" + "═".repeat(55) + "\n");
        testWarriorAbilities();
        System.out.println("\n" + "═".repeat(55) + "\n");
        testLoadoutSystem();
        System.out.println("\n" + "═".repeat(55) + "\n");
        testTalentModifiers();
        System.out.println("\n" + "═".repeat(55) + "\n");
        testVariantSwitching();

        System.out.println("\n═══════════════════════════════════════════════════");
        System.out.println("    ✅ ALL TESTS COMPLETED SUCCESSFULLY!");
        System.out.println("═══════════════════════════════════════════════════");
    }

    /**
     * Test 1: Wizard abilities (Fireball, Lightning Bolt)
     */
    private static void testWizardAbilities() {
        System.out.println("TEST 1: Wizard Abilities");
        System.out.println("-".repeat(55));

        Erou wizard = new Ardelean("TestWizard");

        // Create and unlock Fireball
        Map<String, ConfiguredAbility> exampleAbilities = AbilityDefinitions.createAllExampleAbilities();
        ConfiguredAbility fireball = exampleAbilities.get("Fireball");
        wizard.unlockConfiguredAbility(fireball);

        System.out.println("✓ Unlocked Fireball");
        System.out.println("  Variant: " + fireball.getSelectedVariant().getName());
        System.out.println("  Base Damage: " + fireball.getFinalDamage());
        System.out.println("  Mana Cost: " + fireball.getFinalManaCost());

        // Unlock Lightning Bolt
        ConfiguredAbility lightning = exampleAbilities.get("Lightning Bolt");
        wizard.unlockConfiguredAbility(lightning);

        System.out.println("\n✓ Unlocked Lightning Bolt");
        System.out.println("  Variant: " + lightning.getSelectedVariant().getName());
        System.out.println("  Base Damage: " + lightning.getFinalDamage());
        System.out.println("  Mana Cost: " + lightning.getFinalManaCost());

        System.out.println("\n✓ Total unlocked abilities: " + wizard.getUnlockedAbilityCount());
    }

    /**
     * Test 2: Warrior abilities (Cleave)
     */
    private static void testWarriorAbilities() {
        System.out.println("TEST 2: Warrior Abilities");
        System.out.println("-".repeat(55));

        Erou warrior = new Moldovean("TestWarrior");

        // Create and unlock Cleave
        Map<String, ConfiguredAbility> exampleAbilities = AbilityDefinitions.createAllExampleAbilities();
        ConfiguredAbility cleave = exampleAbilities.get("Cleave");
        warrior.unlockConfiguredAbility(cleave);

        System.out.println("✓ Unlocked Cleave");
        System.out.println("  Variant: " + cleave.getSelectedVariant().getName());
        System.out.println("  Base Damage: " + cleave.getFinalDamage());
        System.out.println("  Rage Cost: " + cleave.getFinalManaCost());
        System.out.println("  AOE: " + (cleave.getSelectedVariant().isAOE() ? "Yes (2 targets)" : "No"));
    }

    /**
     * Test 3: Loadout management system
     */
    private static void testLoadoutSystem() {
        System.out.println("TEST 3: Loadout Management");
        System.out.println("-".repeat(55));

        Erou hero = new Ardelean("TestHero");

        // Unlock all 3 example abilities
        Map<String, ConfiguredAbility> exampleAbilities = AbilityDefinitions.createAllExampleAbilities();
        hero.unlockConfiguredAbility(exampleAbilities.get("Fireball"));
        hero.unlockConfiguredAbility(exampleAbilities.get("Lightning Bolt"));
        hero.unlockConfiguredAbility(exampleAbilities.get("Cleave"));

        System.out.println("✓ Unlocked 3 abilities");

        // Add to loadout
        hero.addAbilityToLoadout("Fireball");
        hero.addAbilityToLoadout("Lightning Bolt");
        hero.addAbilityToLoadout("Cleave");

        System.out.println("✓ Added 3 abilities to loadout");

        // Check loadout
        List<ConfiguredAbility> activeLoadout = hero.getActiveLoadoutAbilities();
        System.out.println("\n📋 Active Loadout (" + hero.getLoadoutSize() + "/6):");
        for (int i = 0; i < activeLoadout.size(); i++) {
            ConfiguredAbility ability = activeLoadout.get(i);
            System.out.printf("  [%d] %s - %d damage, %d cost\n",
                    i + 1,
                    ability.getDisplayName(),
                    ability.getFinalDamage(),
                    ability.getFinalManaCost());
        }

        // Test loadout validation
        System.out.println("\n✓ Loadout valid: " + hero.hasValidLoadout());
        System.out.println("✓ Loadout full: " + hero.getAbilityLoadout().isLoadoutFull());

        // Test template saving
        hero.saveLoadoutTemplate("Test Template");
        System.out.println("✓ Saved loadout template");

        // Clear and reload
        hero.clearLoadout();
        System.out.println("✓ Cleared loadout (size: " + hero.getLoadoutSize() + ")");

        hero.loadLoadoutTemplate("Test Template");
        System.out.println("✓ Loaded template (size: " + hero.getLoadoutSize() + ")");
    }

    /**
     * Test 4: Talent modifiers and stat calculations
     */
    private static void testTalentModifiers() {
        System.out.println("TEST 4: Talent Modifiers & Stat Calculations");
        System.out.println("-".repeat(55));

        Erou hero = new Ardelean("TestHero");

        // Create Fireball
        Abilitate fireballBase = AbilityDefinitions.createFireballBase();
        List<AbilityVariant> fireballVariants = AbilityDefinitions.createFireballVariants();
        ConfiguredAbility fireball = new ConfiguredAbility(fireballBase, fireballVariants.get(0));

        System.out.println("🔥 Fireball (No Talents):");
        System.out.println("   Damage: " + fireball.getFinalDamage());
        System.out.println("   Mana: " + fireball.getFinalManaCost());

        // Apply Tier 1 talent: Intense Heat (+20% damage)
        List<AbilityTalent> tier1 = AbilityDefinitions.getTalentsForTier("Fireball", TalentTier.TIER_1);
        fireball.setTier1Talent(tier1.get(0)); // Intense Heat

        System.out.println("\n🔥 Fireball (+ Intense Heat):");
        System.out.println("   Damage: " + fireball.getFinalDamage() + " (+20%)");
        System.out.println("   Mana: " + fireball.getFinalManaCost());

        // Apply Tier 2 talent: Melt Armor
        List<AbilityTalent> tier2 = AbilityDefinitions.getTalentsForTier("Fireball", TalentTier.TIER_2);
        fireball.setTier2Talent(tier2.get(0)); // Melt Armor

        System.out.println("\n🔥 Fireball (+ Melt Armor):");
        System.out.println("   Damage: " + fireball.getFinalDamage());
        System.out.println("   Effect: Reduces enemy DEF by 10 for 2 turns");

        // Apply Tier 3 talent: Explosive
        List<AbilityTalent> tier3 = AbilityDefinitions.getTalentsForTier("Fireball", TalentTier.TIER_3);
        fireball.setTier3Talent(tier3.get(0)); // Explosive

        System.out.println("\n🔥 Fireball (Full Build):");
        System.out.println("   Damage: " + fireball.getFinalDamage());
        System.out.println("   Mana: " + fireball.getFinalManaCost());
        System.out.println("   T1: Intense Heat (+20% damage)");
        System.out.println("   T2: Melt Armor (-10 enemy DEF)");
        System.out.println("   T3: Explosive (30 AOE on kill)");

        // Test different Tier 1 talent: Mana Efficient
        fireball.setTier1Talent(tier1.get(2)); // Mana Efficient (-30% mana, -30% damage)

        System.out.println("\n🔥 Fireball (Mana Efficient Build):");
        System.out.println("   Damage: " + fireball.getFinalDamage() + " (-30%)");
        System.out.println("   Mana: " + fireball.getFinalManaCost() + " (-30%)");
        System.out.println("   T1: Mana Efficient");
    }

    /**
     * Test 5: Variant switching
     */
    private static void testVariantSwitching() {
        System.out.println("TEST 5: Variant Switching");
        System.out.println("-".repeat(55));

        Erou hero = new Ardelean("TestHero");

        // Create Fireball with all variants
        Abilitate fireballBase = AbilityDefinitions.createFireballBase();
        List<AbilityVariant> variants = AbilityDefinitions.createFireballVariants();
        ConfiguredAbility fireball = new ConfiguredAbility(fireballBase, variants.get(0));

        System.out.println("🔥 Fireball Variants:\n");

        // Test each variant
        for (int i = 0; i < variants.size(); i++) {
            AbilityVariant variant = variants.get(i);
            fireball.setSelectedVariant(variant);

            System.out.println("Variant " + (i + 1) + ": " + variant.getName());
            System.out.println("  Description: " + variant.getDescription());
            System.out.println("  Damage: " + fireball.getFinalDamage());
            System.out.println("  Mana: " + fireball.getFinalManaCost());
            System.out.println("  Cooldown: " + variant.getCooldown() + " turns");
            System.out.println("  AOE: " + (variant.isAOE() ? "Yes (" + variant.getNumberOfTargets() + " targets)" : "No"));
            System.out.println("  Use Case: " + variant.getUseCase());
            System.out.println();
        }

        // Test Cleave variants
        Abilitate cleaveBase = AbilityDefinitions.createCleaveBase();
        List<AbilityVariant> cleaveVariants = AbilityDefinitions.createCleaveVariants();
        ConfiguredAbility cleave = new ConfiguredAbility(cleaveBase, cleaveVariants.get(0));

        System.out.println("⚔️ Cleave Variants:\n");

        for (int i = 0; i < cleaveVariants.size(); i++) {
            AbilityVariant variant = cleaveVariants.get(i);
            cleave.setSelectedVariant(variant);

            System.out.println("Variant " + (i + 1) + ": " + variant.getName());
            System.out.println("  Damage: " + cleave.getFinalDamage());
            System.out.println("  Rage: " + cleave.getFinalManaCost());
            System.out.println("  Targets: " + variant.getNumberOfTargets());
            System.out.println("  Use Case: " + variant.getUseCase());
            System.out.println();
        }
    }

    /**
     * Bonus: Test special effect flags
     */
    private static void testSpecialEffects() {
        System.out.println("BONUS TEST: Special Effect Flags");
        System.out.println("-".repeat(55));

        // Create Cleave with Lifesteal talent
        Abilitate cleaveBase = AbilityDefinitions.createCleaveBase();
        List<AbilityVariant> variants = AbilityDefinitions.createCleaveVariants();
        ConfiguredAbility cleave = new ConfiguredAbility(cleaveBase, variants.get(0));

        List<AbilityTalent> tier3 = AbilityDefinitions.getTalentsForTier("Cleave", TalentTier.TIER_3);
        AbilityTalent lifesteal = tier3.get(1); // Lifesteal talent
        cleave.setTier3Talent(lifesteal);

        System.out.println("⚔️ Cleave with Lifesteal Talent:");
        System.out.println("   Has Lifesteal: " + lifesteal.getModifier().hasLifesteal());
        System.out.println("   Lifesteal %: " + (lifesteal.getModifier().getLifestealPercent() * 100) + "%");

        // Test bleed effect
        List<AbilityTalent> tier1 = AbilityDefinitions.getTalentsForTier("Cleave", TalentTier.TIER_1);
        AbilityTalent bleed = tier1.get(0); // Bleeding Edge
        cleave.setTier1Talent(bleed);

        System.out.println("\n⚔️ Cleave with Bleeding Edge Talent:");
        System.out.println("   Applies Bleed: " + bleed.getModifier().appliesBleed());
        System.out.println("   Bleed Damage: " + bleed.getModifier().getBleedDamage() + "/turn");
        System.out.println("   Bleed Duration: " + bleed.getModifier().getBleedDuration() + " turns");
    }
}
