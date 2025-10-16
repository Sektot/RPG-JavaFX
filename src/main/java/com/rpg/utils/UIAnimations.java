package com.rpg.utils;

/**
 * 🎨 SISTEM DE ANIMAȚII ȘI EFECTE VIZUALE
 * Aduce viață în tot jocul RPG!
 */
public class UIAnimations {

    // ⚙️ CONFIGURĂRI
   // private static final int DELAY_INSTANT = 0;
    //private static final int DELAY_VERY_SHORT = 300;
    //private static final int DELAY_SHORT = 600;
    //private static final int DELAY_MEDIUM = 1000;
    //private static final int DELAY_LONG = 1500;
    private static final boolean ENABLE_ANIMATIONS = true;

    // 🎨 CULORI ANSI (pentru terminale care suportă)
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String BOLD = "\u001B[1m";



    // 📜 EFECT TYPEWRITER pentru mesaje importante
    public static void typewriterEffect(String text, int delayPerChar) {
        if (!ENABLE_ANIMATIONS) {
            System.out.println(text);
            return;
        }

        for (char c : text.toCharArray()) {
            System.out.print(c);
            try {
                Thread.sleep(delayPerChar);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(text.substring(text.indexOf(c)));
                return;
            }
        }
        System.out.println();
    }


//metode helper
    public static void pause(int milliseconds) {
        if (!ENABLE_ANIMATIONS) return;
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


}