package com.rpg.utils;

import java.util.Scanner;

/**
 * Clasă utilitară pentru validarea input-urilor utilizatorului.
 * Asigură că toate datele introduse sunt corecte și sigure.
 */
public final class Validator {

    /**
     * Constructor privat pentru a preveni instanțierea.
     */
    private Validator() {
        throw new AssertionError("Validator nu poate fi instanțiat");
    }

    /**
     * Citește o alegere validă din intervalul specificat.
     * @param scanner Scanner pentru input
     * @param min Valoarea minimă acceptată
     * @param max Valoarea maximă acceptată
     * @return Alegerea validă
     */
    public static int readValidChoice(Scanner scanner, int min, int max) {
        int choice = -1;

        while (choice < min || choice > max) {
            System.out.print("Introdu alegerea ta (" + min + "-" + max + "): ");

            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                if (choice < min || choice > max) {
                    System.out.println("❌ Alegere invalidă. Te rog să alegi între " + min + " și " + max + ".");
                }
            } else {
                scanner.nextLine(); // Clear invalid input
                System.out.println("❌ Te rog să introduci un număr valid.");
            }
        }

        return choice;
    }

    /**
     * Citește o confirmație (da/nu) de la utilizator.
     * @param scanner Scanner pentru input
     * @param message Mesajul de afișat
     * @return true pentru da, false pentru nu
     */
    public static boolean readConfirmation(Scanner scanner, String message) {
        if (message != null && !message.isEmpty()) {
            System.out.println(message);
        }

        while (true) {
            System.out.print("Confirmi? (da/nu): ");
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "da", "d", "yes", "y", "1" -> {
                    return true;
                }
                case "nu", "n", "no", "0" -> {
                    return false;
                }
                default -> System.out.println("❌ Te rog să răspunzi cu 'da' sau 'nu'.");
            }
        }
    }

    /**
     * Citește un nume valid pentru personaj.
     * @param scanner Scanner pentru input
     * @return Numele valid
     */
    public static String readValidCharacterName(Scanner scanner) {
        String name = "";

        while (!isValidString(name)) {
            System.out.print("Introdu numele eroului tău: ");
            name = scanner.nextLine().trim();

            if (!isValidString(name)) {
                System.out.println("❌ Numele trebuie să aibă între 2 și 20 de caractere și să conțină doar litere.");
            }
        }

        // Capitalizează prima literă
        return capitalizeFirst(name);
    }

    /**
     * Verifică dacă un string este valid pentru nume.
     * @param str String-ul de verificat
     * @return true dacă este valid
     */
    public static boolean isValidString(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }

        str = str.trim();

        // Verifică lungimea
        if (str.length() < 2 || str.length() > 20) {
            return false;
        }

        // Verifică că conține doar litere și spații
        return str.matches("[a-zA-ZăâîșțĂÂÎȘȚ ]+");
    }

    /**
     * Capitalizează prima literă a unui string.
     * @param str String-ul de capitalizat
     * @return String-ul cu prima literă mare
     */
    private static String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * Citește un număr întreg în intervalul specificat.
     * @param scanner Scanner pentru input
     * @param min Valoarea minimă
     * @param max Valoarea maximă
     * @param prompt Mesajul de afișat
     * @return Numărul valid
     */
    public static int readValidInteger(Scanner scanner, int min, int max, String prompt) {
        int number = min - 1;

        while (number < min || number > max) {
            System.out.print(prompt + " (" + min + "-" + max + "): ");

            if (scanner.hasNextInt()) {
                number = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                if (number < min || number > max) {
                    System.out.println("❌ Valoarea trebuie să fie între " + min + " și " + max + ".");
                }
            } else {
                scanner.nextLine(); // Clear invalid input
                System.out.println("❌ Te rog să introduci un număr valid.");
            }
        }

        return number;
    }

    /**
     * Citește un string non-gol.
     * @param scanner Scanner pentru input
     * @param prompt Mesajul de afișat
     * @return String-ul valid
     */
    public static String readNonEmptyString(Scanner scanner, String prompt) {
        String input = "";

        while (input.trim().isEmpty()) {
            System.out.print(prompt + ": ");
            input = scanner.nextLine();

            if (input.trim().isEmpty()) {
                System.out.println("❌ Câmpul nu poate fi gol.");
            }
        }

        return input.trim();
    }

    /**
     * Verifică dacă un email este valid (format basic).
     * @param email Email-ul de verificat
     * @return true dacă este valid
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    /**
     * Așteaptă ca utilizatorul să apese Enter.
     * @param scanner Scanner pentru input
     * @param message Mesajul de afișat
     */
    public static void waitForEnter(Scanner scanner, String message) {
        if (message != null && !message.isEmpty()) {
            System.out.println(message);
        }
        System.out.print("Apasă Enter pentru a continua...");
        scanner.nextLine();
    }

    /**
     * Curăță input-ul invalid din scanner.
     * @param scanner Scanner de curățat
     */
    public static void clearInvalidInput(Scanner scanner) {
        while (scanner.hasNext() && !scanner.hasNextInt()) {
            scanner.next();
        }
    }

    /**
     * Verifică dacă un număr este în intervalul specificat.
     * @param number Numărul de verificat
     * @param min Minimul
     * @param max Maximul
     * @return true dacă este în interval
     */
    public static boolean isInRange(int number, int min, int max) {
        return number >= min && number <= max;
    }

    /**
     * Verifică dacă un număr este pozitiv.
     * @param number Numărul de verificat
     * @return true dacă este pozitiv
     */
    public static boolean isPositive(int number) {
        return number > 0;
    }

    /**
     * Verifică dacă un număr este non-negativ.
     * @param number Numărul de verificat
     * @return true dacă este non-negativ
     */
    public static boolean isNonNegative(int number) {
        return number >= 0;
    }
}