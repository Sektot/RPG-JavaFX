package com.rpg.ui;

import com.rpg.model.characters.Erou;
import com.rpg.model.characters.classes.Moldovean;  // ✨ SCHIMBAT
import com.rpg.model.characters.classes.Oltean;     // ✨ SCHIMBAT
import com.rpg.model.characters.classes.Ardelean;   // ✨ SCHIMBAT
import com.rpg.service.GameService;
import com.rpg.service.SaveLoadService;
import com.rpg.utils.Validator;
import java.util.Scanner;

/**
 * Clasă îmbunătățită pentru interfața utilizatorului - versiunea românească.
 */
public class GameUI {

    /**
     * Afișează meniul de start.
     */
    public Erou showStartupMenu(Scanner scanner, GameService gameService) {
        return gameService.loadHeroFromMenu(scanner);
    }

    /**
     * Creează un erou nou cu interfață îmbunătățită - CLASELE ROMÂNEȘTI.
     */
    public Erou createNewHero(Scanner scanner) {
        SaveLoadService.clearScreen();

        System.out.println("\n" + "═".repeat(60));
        System.out.println("        🆕 CREARE EROU NOU");
        System.out.println("═".repeat(60));

        // Citește numele eroului
        String numeErou = Validator.readValidCharacterName(scanner);

        // Afișează clasele românești disponibile
        System.out.println("\n🎯 Alege clasa vitejului tău:");
        System.out.println();
        displayClassInfo();

        int claseChoice = Validator.readValidChoice(scanner, 1, 3);

        Erou erou = switch (claseChoice) {
            case 1 -> new Moldovean(numeErou);   // ✨ SCHIMBAT
            case 2 -> new Oltean(numeErou);      // ✨ SCHIMBAT
            case 3 -> new Ardelean(numeErou);    // ✨ SCHIMBAT
            default -> null;
        };

        if (erou != null) {
            displayHeroCreationSuccess(erou);
        }

        return erou;
    }

    /**
     * Afișează informațiile despre clasele românești disponibile.
     */
    private void displayClassInfo() {
        System.out.println("┌─ 💪 MOLDOVEAN ────────────────────────────────────────────┐");
        System.out.println("│ Viteaz puternic din Moldova                               │");
        System.out.println("│ ⚔️  Specializare: Atacuri fizice devastatoare             │");
        System.out.println("│ 💚 Viață mare și rezistență legendară                     │");
        System.out.println("│ 🍖 Abilități: Sarmale, Pălincă, Jocul Caprei              │");
        System.out.println("│ 📊 Stats: ⭐⭐⭐ STR | ⭐ DEX | ⭐ INT                   │");
        System.out.println("└───────────────────────────────────────────────────────────┘");
        System.out.println();

        System.out.println("┌─ 🗡️ OLTEAN ───────────────────────────────────────────────┐");
        System.out.println("│ Hoț viclean și rapid din Oltenia                          │");
        System.out.println("│ ⚡ Specializare: Atacuri rapide și stealth                 │");
        System.out.println("│ 🎯 Critical damage uriaș și agilitate maximă              │");
        System.out.println("│ 💨 Abilități: Înjunghiere, Cuțit Otrăvit, Clona           │");
        System.out.println("│ 📊 Stats: ⭐ STR | ⭐⭐⭐ DEX | ⭐ INT                   │");
        System.out.println("└───────────────────────────────────────────────────────────┘");
        System.out.println();

        System.out.println("┌─ 🔮 ARDELEAN ─────────────────────────────────────────────┐");
        System.out.println("│ Vrăjitor înțelept din Ardeal                              │");
        System.out.println("│ ✨ Specializare: Magie puternică și farmece               │");
        System.out.println("│ 📚 Spell damage mare și control battlefield               │");
        System.out.println("│ ⚡ Abilități: Mămăligă, Fulger, Puterea Dacilor            │");
        System.out.println("│ 📊 Stats: ⭐ STR | ⭐ DEX | ⭐⭐⭐ INT                   │");
        System.out.println("└───────────────────────────────────────────────────────────┘");
        System.out.println();

        System.out.print("➤ Alege clasa (1-3): ");
    }

    /**
     * Afișează mesajul de succes după crearea eroului.
     */
    private void displayHeroCreationSuccess(Erou erou) {
        SaveLoadService.clearScreen();

        System.out.println("\n" + "═".repeat(60));
        System.out.println("        ✅ EROU CREAT CU SUCCES!");
        System.out.println("═".repeat(60));
        System.out.println();

        String clasa = erou.getClass().getSimpleName();
        String clasaRomaneasca = switch (clasa) {
            case "Moldovean" -> "💪 Moldovean";
            case "Oltean" -> "🗡️ Oltean";
            case "Ardelean" -> "🔮 Ardelean";
            default -> clasa;
        };

        System.out.printf("👤 Nume: %s%n", erou.getNume());
        System.out.printf("⚔️  Clasă: %s%n", clasaRomaneasca);
        System.out.printf("📊 Nivel: %d%n", erou.getNivel());
        System.out.println();
        System.out.printf("💪 Strength: %d%n", erou.getStrength());
        System.out.printf("🎯 Dexterity: %d%n", erou.getDexterity());
        System.out.printf("🧠 Intelligence: %d%n", erou.getIntelligence());
        System.out.println();
        System.out.printf("❤️  Viață: %d/%d%n", erou.getViata(), erou.getViataMaxima());
        System.out.printf("💙 %s: %d/%d%n", erou.getTipResursa(),
                erou.getResursaCurenta(), erou.getResursaMaxima());
        System.out.println();
        System.out.println("🎊 Aventura ta începe acum!");
        System.out.println("🗡️ Baftă în luptele ce vor urma!");

        waitForEnter();
    }

//    /**
//     * Afișează meniul principal (DEPRECATED - folosește TownService acum).
//     */
//    @Deprecated
//    public void showMainMenu(Erou erou) {
//        System.out.println("\n" + "═".repeat(50));
//        System.out.println("        🏛️  ORAȘUL PRINCIPAL");
//        System.out.println("═".repeat(50));
//        System.out.printf("👤 %s (Nivel %d)%n", erou.getNume(), erou.getNivel());
//    }

    /**
     * Așteaptă Enter.
     */
    private void waitForEnter() {
        System.out.println("\n📝 Apasă Enter pentru a continua...");
        try {
            System.in.read();
            while (System.in.available() > 0) {
                System.in.read();
            }
        } catch (Exception e) {
            // Ignore
        }
    }
}