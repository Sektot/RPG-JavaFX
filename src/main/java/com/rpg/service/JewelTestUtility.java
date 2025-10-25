package com.rpg.service;

import com.rpg.model.characters.Erou;
import com.rpg.model.items.Jewel;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for testing the jewel system
 * Provides methods to generate test jewels and verify functionality
 */
public class JewelTestUtility {

    /**
     * Adds a variety of test jewels to the hero's inventory for testing
     */
    public static void addTestJewelsToHero(Erou hero) {
        System.out.println("\n💎 ═══════════════════════════════════════");
        System.out.println("💎 ADDING TEST JEWELS TO INVENTORY");
        System.out.println("💎 ═══════════════════════════════════════\n");

        // Add one of each type and rarity for comprehensive testing
        hero.addJewel(createTestCrimsonJewel());
        hero.addJewel(createTestViridianJewel());
        hero.addJewel(createTestCobaltJewel());
        hero.addJewel(createTestPrismaticJewel());
        hero.addJewel(createTestLegendaryJewel());

        System.out.println("\n✅ Added 5 test jewels to inventory!");
        System.out.println("💎 Total jewels: " + hero.getJewelCount());
        System.out.println("💎 Available (unsocketed): " + hero.getAvailableJewels().size());
        System.out.println("\n💎 ═══════════════════════════════════════\n");
    }

    /**
     * Creates a test Crimson Jewel (STR-focused)
     */
    public static Jewel createTestCrimsonJewel() {
        Map<String, Double> mods = new HashMap<>();
        mods.put("hp_percent", 8.0);
        mods.put("defense_percent", 5.0);
        mods.put("damage_percent", 6.0);
        mods.put("str_bonus", 5.0);

        Jewel jewel = new Jewel(
                "Test Crimson Jewel",
                Jewel.JewelType.CRIMSON,
                Jewel.JewelRarity.RARE,
                1,
                mods,
                "A test jewel for STR builds"
        );

        System.out.println("🔴 Created: " + jewel.getName());
        return jewel;
    }

    /**
     * Creates a test Viridian Jewel (DEX-focused)
     */
    public static Jewel createTestViridianJewel() {
        Map<String, Double> mods = new HashMap<>();
        mods.put("crit_chance", 4.5);
        mods.put("dodge_chance", 3.0);
        mods.put("attack_speed", 5.0);
        mods.put("dex_bonus", 6.0);

        Jewel jewel = new Jewel(
                "Test Viridian Jewel",
                Jewel.JewelType.VIRIDIAN,
                Jewel.JewelRarity.RARE,
                1,
                mods,
                "A test jewel for DEX builds"
        );

        System.out.println("🟢 Created: " + jewel.getName());
        return jewel;
    }

    /**
     * Creates a test Cobalt Jewel (INT-focused)
     */
    public static Jewel createTestCobaltJewel() {
        Map<String, Double> mods = new HashMap<>();
        mods.put("damage_percent", 7.0);
        mods.put("crit_chance", 3.5);
        mods.put("int_bonus", 7.0);

        Jewel jewel = new Jewel(
                "Test Cobalt Jewel",
                Jewel.JewelType.COBALT,
                Jewel.JewelRarity.UNCOMMON,
                1,
                mods,
                "A test jewel for INT builds"
        );

        System.out.println("🔵 Created: " + jewel.getName());
        return jewel;
    }

    /**
     * Creates a test Prismatic Jewel (Balanced)
     */
    public static Jewel createTestPrismaticJewel() {
        Map<String, Double> mods = new HashMap<>();
        mods.put("all_stats", 3.0);
        mods.put("hp_percent", 5.0);
        mods.put("damage_percent", 4.0);

        Jewel jewel = new Jewel(
                "Test Prismatic Jewel",
                Jewel.JewelType.PRISMATIC,
                Jewel.JewelRarity.EPIC,
                1,
                mods,
                "A balanced test jewel"
        );

        System.out.println("⚪ Created: " + jewel.getName());
        return jewel;
    }

    /**
     * Creates a powerful test Legendary Jewel
     */
    public static Jewel createTestLegendaryJewel() {
        Map<String, Double> mods = new HashMap<>();
        mods.put("damage_percent", 12.0);
        mods.put("hp_percent", 10.0);
        mods.put("crit_chance", 6.0);
        mods.put("crit_multiplier", 25.0);
        mods.put("all_stats", 5.0);

        Jewel jewel = new Jewel(
                "Blood of the Immortal",
                Jewel.JewelType.UNIQUE,
                Jewel.JewelRarity.LEGENDARY,
                1,
                mods,
                "The essence of eternal warriors flows through this jewel"
        );

        System.out.println("🟡 Created: " + jewel.getName());
        return jewel;
    }

    /**
     * Prints detailed stats about hero's jewel collection
     */
    public static void printJewelInventoryStats(Erou hero) {
        System.out.println("\n💎 ═══════════════════════════════════════");
        System.out.println("💎 JEWEL INVENTORY STATS");
        System.out.println("💎 ═══════════════════════════════════════\n");

        System.out.println("📊 Total Jewels: " + hero.getJewelCount());
        System.out.println("✅ Available (Unsocketed): " + hero.getAvailableJewels().size());
        System.out.println("💎 Socketed: " + hero.getSocketedJewels().size());

        if (hero.getJewelCount() > 0) {
            System.out.println("\n📝 Jewel List:");
            for (Jewel jewel : hero.getJewelInventory()) {
                String status = jewel.isSocketed() ? "[SOCKETED]" : "[AVAILABLE]";
                System.out.printf("  %s %s %s - %d mods\n",
                        jewel.getType().getIcon(),
                        jewel.getName(),
                        status,
                        jewel.getModifiers().size());
            }
        }

        System.out.println("\n💎 ═══════════════════════════════════════\n");
    }

    /**
     * Verifies that jewel bonuses are being applied correctly
     */
    public static void verifyJewelBonuses(Erou hero) {
        System.out.println("\n🔍 ═══════════════════════════════════════");
        System.out.println("🔍 JEWEL BONUS VERIFICATION");
        System.out.println("🔍 ═══════════════════════════════════════\n");

        // Check talent tree bonuses
        System.out.println("📊 Talent Tree Bonuses:");
        System.out.printf("  💪 STR: %d | DEX: %d | INT: %d\n",
                hero.getStrength(), hero.getDexterity(), hero.getIntelligence());
        System.out.printf("  ❤️  HP: %d | 🛡️  Defense: %d\n",
                hero.getViataMaxima(), hero.getDefenseTotal());
        System.out.printf("  ⚡ Crit: %.1f%% | 💨 Dodge: %.1f%%\n",
                hero.getCritChanceTotal(), hero.getTalentDodge());
        System.out.printf("  ⚔️  Damage Bonus: %.1f%%\n",
                hero.getTalentDamageBonus());

        System.out.println("\n💎 Socketed Jewels:");
        if (hero.getSocketedJewels().isEmpty()) {
            System.out.println("  ⚠️  No jewels currently socketed!");
            System.out.println("  💡 Socket some jewels in the talent tree to see their bonuses!");
        } else {
            for (Jewel jewel : hero.getSocketedJewels()) {
                System.out.println("\n  " + jewel.getType().getIcon() + " " + jewel.getName() + ":");
                for (Map.Entry<String, Double> mod : jewel.getModifiers().entrySet()) {
                    System.out.printf("    • %s: +%.1f\n", mod.getKey(), mod.getValue());
                }
            }
        }

        System.out.println("\n🔍 ═══════════════════════════════════════\n");
    }

    /**
     * Generates random jewels for drop testing
     */
    public static void testJewelDrops(Erou hero) {
        System.out.println("\n🎲 ═══════════════════════════════════════");
        System.out.println("🎲 TESTING JEWEL DROP SYSTEM");
        System.out.println("🎲 ═══════════════════════════════════════\n");

        int heroLevel = hero.getNivel();

        // Test boss drop
        System.out.println("🔥 Testing BOSS drop (40% chance):");
        Jewel bossJewel = LootGenerator.rollBossJewelDrop(heroLevel);
        if (bossJewel != null) {
            System.out.println("✅ Boss jewel dropped!");
            LootGenerator.displayJewelDrop(bossJewel);
            hero.addJewel(bossJewel);
        } else {
            System.out.println("❌ No boss jewel dropped this time");
        }

        System.out.println("\n👹 Testing REGULAR enemy drop (5% chance):");
        Jewel regularJewel = LootGenerator.rollRegularJewelDrop(heroLevel);
        if (regularJewel != null) {
            System.out.println("✅ Regular enemy jewel dropped!");
            LootGenerator.displayJewelDrop(regularJewel);
            hero.addJewel(regularJewel);
        } else {
            System.out.println("❌ No regular enemy jewel dropped this time");
        }

        System.out.println("\n🎁 Testing GUARANTEED drop (for quests/secrets):");
        Jewel guaranteedJewel = LootGenerator.generateGuaranteedJewel(heroLevel, true);
        System.out.println("✅ Guaranteed jewel generated!");
        LootGenerator.displayJewelDrop(guaranteedJewel);
        hero.addJewel(guaranteedJewel);

        System.out.println("\n💰 Current jewel count: " + hero.getJewelCount());
        System.out.println("\n🎲 ═══════════════════════════════════════\n");
    }
}
