package com.rpg;

public class TestConstants {
    public static void main(String[] args) {
        System.out.println("=== TESTARE GAME CONSTANTS ===");

        try {
            // Test dacă GameConstants există
            Class<?> gameConstantsClass = Class.forName("com.rpg.utils.GameConstants");
            System.out.println("✅ GameConstants găsit!");

            // Test constante enemy cu reflection (în caz că import-ul nu merge)
            java.lang.reflect.Field enemyBaseHealth = gameConstantsClass.getField("ENEMY_BASE_HEALTH");
            System.out.println("ENEMY_BASE_HEALTH: " + enemyBaseHealth.get(null));

            java.lang.reflect.Field enemyHealthPerLevel = gameConstantsClass.getField("ENEMY_HEALTH_PER_LEVEL");
            System.out.println("ENEMY_HEALTH_PER_LEVEL: " + enemyHealthPerLevel.get(null));

            java.lang.reflect.Field enemyBaseGold = gameConstantsClass.getField("ENEMY_BASE_GOLD");
            System.out.println("ENEMY_BASE_GOLD: " + enemyBaseGold.get(null));

            java.lang.reflect.Field enemyGoldPerLevel = gameConstantsClass.getField("ENEMY_GOLD_PER_LEVEL");
            System.out.println("ENEMY_GOLD_PER_LEVEL: " + enemyGoldPerLevel.get(null));

            java.lang.reflect.Field enemyBaseXp = gameConstantsClass.getField("ENEMY_BASE_XP");
            System.out.println("ENEMY_BASE_XP: " + enemyBaseXp.get(null));

            java.lang.reflect.Field enemyXpPerLevel = gameConstantsClass.getField("ENEMY_XP_PER_LEVEL");
            System.out.println("ENEMY_XP_PER_LEVEL: " + enemyXpPerLevel.get(null));

        } catch (ClassNotFoundException e) {
            System.out.println("❌ GameConstants nu găsit: " + e.getMessage());
            System.out.println("💡 Verifică că fișierul GameConstants.java există în src/main/java/com/rpg/utils/");
        } catch (NoSuchFieldException e) {
            System.out.println("❌ Constantă lipsă: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Eroare: " + e.getMessage());
        }

        // Test direct cu import (dacă merge)
        System.out.println("\n=== TEST DIRECT IMPORT ===");
        try {
            com.rpg.utils.GameConstants constants = new com.rpg.utils.GameConstants();
            System.out.println("✅ Import direct funcționează!");
        } catch (Exception e) {
            System.out.println("❌ Import direct nu merge: " + e.getMessage());
        }

        // Test calculul manual (fără constante)
        System.out.println("\n=== CALCUL MANUAL FĂRĂ CONSTANTE ===");
        int hp_manual = 60 + (1 * 25);  // Valorile din GameConstants.java
        int gold_manual = 15 + (1 * 8);
        int xp_manual = 25 + (1 * 10);
        System.out.println("HP manual (60+25): " + hp_manual);
        System.out.println("Gold manual (15+8): " + gold_manual);
        System.out.println("XP manual (25+10): " + xp_manual);

        System.out.println("\n=== TESTARE RANDOM UTILS ===");

        // Test randomInt
        for (int i = 0; i < 5; i++) {
            int result = com.rpg.utils.RandomUtils.randomInt(1, 10);
            System.out.println("RandomInt(1,10): " + result);
        }

        // Test randomElement cu array simplu
        String[] testArray = {"Test1", "Test2", "Test3"};
        for (int i = 0; i < 3; i++) {
            String result = com.rpg.utils.RandomUtils.randomElement(testArray);
            System.out.println("RandomElement: " + result);
        }

        // Test randomElement cu String[][]
        String[][] testArray2D = {
                {"Enemy1", "🐉", "fire", "ice"},
                {"Enemy2", "👹", "poison", "holy"},
                {"Enemy3", "💀", "shadow", "light"}
        };

        for (int i = 0; i < 3; i++) {
            String[] result = com.rpg.utils.RandomUtils.randomElement(testArray2D);
            System.out.println("RandomElement 2D: " + java.util.Arrays.toString(result));
        }
    }
}
