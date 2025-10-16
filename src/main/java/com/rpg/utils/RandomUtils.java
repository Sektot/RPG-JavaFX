package com.rpg.utils;

import java.util.List;
import java.util.Random;

/**
 * Utilități pentru generare de valori random
 */
public class RandomUtils {

    private static final Random random = new Random();

    /**
     * Returnează true cu o anumită probabilitate (0-100%)
     */
    public static boolean chancePercent(double percent) {
        return random.nextDouble() * 100 < percent;
    }

    /**
     * Verifică că randomInt funcționează corect
     */
    public static int randomInt(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("Min nu poate fi mai mare decât max!");
        }
        java.util.Random random = new java.util.Random();
        return random.nextInt((max - min) + 1) + min;
    }
    /**
     * Returnează un element random dintr-o listă
     */
    public static <T> T randomElement(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(random.nextInt(list.size()));
    }

    /**
     * Returnează un element aleatoriu dintr-un array
     */
    public static <T> T randomElement(T[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        int index = randomInt(0, array.length - 1);
        return array[index];
    }

    /**
     * Overload special pentru String[][]
     */
    public static String[] randomElement(String[][] array) {
        if (array == null || array.length == 0) {
            return new String[]{"Default Enemy", "👹", "physical", "magical"};
        }
        int index = randomInt(0, array.length - 1);
        return array[index];
    }
    /**
     * Returnează true/false random
     */
    public static boolean randomBoolean() {
        return random.nextBoolean();
    }

    /**
     * Returnează un double random între 0.0 și 1.0
     */
    public static double randomDouble() {
        return random.nextDouble();
    }

    public static int applyRandomVariation(int baseValue, int variationPercent) {
        if (baseValue <= 0) return baseValue;  // Nu modifica dacă e deja 0

        double variation = baseValue * (variationPercent / 100.0);
        int minValue = (int)(baseValue - variation);
        int maxValue = (int)(baseValue + variation);

        int result = randomInt(Math.max(1, minValue), maxValue);  // MINIMUM 1!
        return result;
    }

}