package com.rpg.service;

import com.rpg.model.characters.Erou;
import com.rpg.utils.Validator;

import java.util.Scanner;

//service pt oras (Main Menu)
public class TownService {

    private static final String TOWN_BANNER = """
            ╔══════════════════════════════════════════════════════════════╗
            ║                    🏛️  BUCUREJTI CITY  🏛️                    ║
            ║              "De unde nimeni nu pleacă nejepcărit"           ║
            ╚══════════════════════════════════════════════════════════════╝
            """;


     // Afișează meniul orașului si info erou

    public void displayTownMenu(Erou erou) {
        clearScreen();
        System.out.println(TOWN_BANNER);

        // Info erou
        System.out.println("\n┌─ 👤 PROFIL       ─────────────────────────────────────────┐");
        System.out.printf("│ ⚔️  %s (%s) - Nivel %d%n",
                erou.getNume(),
                  (erou.getClass().getSimpleName()),
                erou.getNivel());
        System.out.printf("│ ❤️  Viață: %d/%d  │  💙 %s: %d/%d%n",
                erou.getViata(), erou.getViataMaxima(),
                erou.getTipResursa(), erou.getResursaCurenta(), erou.getResursaMaxima());
        System.out.printf("│ 💰 Gold: %d  │  💎 Shards: %d  │  ⭐ XP: %d/%d%n",
                erou.getGold(), erou.getScrap(),
                erou.getXp(), erou.getXpNecesarPentruUrmatoarelNivel());

        if (erou.getStatPoints() > 0) {
            System.out.printf("│ 🎯 ATENȚIE: Ai %d puncte de stat nealocat!%n", erou.getStatPoints());
        }

        if (erou.hasLeveledUp()) {
            System.out.println("│ 🎊 ⚡ LEVEL UP DISPONIBIL! ⚡ Mergi la Antrenor!");
        }

        System.out.println("└─────────────────────────────────────────────────────────────┘");

        // Meniu principal
        System.out.println("\n╔═══════════════ 🏛️  LOCAȚII DISPONIBILE ════════════════╗");
        System.out.println("║                                                         ║");
        System.out.println("║  🗡️  [1] Gara de Nord                                   ║");
        System.out.println("║      └─ Explorează dungeon-uri pline de pericole        ║");
        System.out.println("║                                                         ║");
        System.out.println("║  🛒 [2] Piața Centrală                                  ║");
        System.out.println("║      └─ Cumpără și vinde echipament și poțiuni          ║");
        System.out.println("║                                                         ║");
        System.out.println("║  🍺 [3] Taverna   LA NEA' GICU                          ║");
        System.out.println("║      └─ Relaxează-te și obține bonusuri                 ║");
        System.out.println("║                                                         ║");
        System.out.println("║  💪 [4] Sala de Antrenament & Fierărie                  ║");
        System.out.println("║      └─ Îmbunătățește stats, abilități și echipament    ║");
        System.out.println("║                                                         ║");
        System.out.println("║  🎒 [5] Inventarul Tău                                  ║");
        System.out.println("║      └─ Gestionează obiecte și echipament               ║");
        System.out.println("║                                                         ║");
        System.out.println("╠══════════════════ ⚙️  OPȚIUNI  ═════════════════════════╣");
        System.out.println("║                                                         ║");
        System.out.println("║  💾 [6] Salvează Progresul                              ║");
        System.out.println("║  📂 [7] Încarcă Joc Salvat                              ║");
        System.out.println("║  📊 [8] Vizualizează Statistici Complete                ║");
        System.out.println("║  🚪 [9] Ieși din Joc                                    ║");
        System.out.println("║                                                         ║");
        System.out.println("╚═════════════════════════════════════════════════════════╝");

        // Tips & tricks aleatorii
        displayRandomTip();

        System.out.print("\n➤ Unde dorești să mergi? (1-9): ");
    }


    // Afișează tips aleatorii pentru jucător.

    private void displayRandomTip() {
        String[] tips = {
                "💡 Sfat: Moldovenii au forță mare și rezistență maximă!",
                "💡 Sfat: Ardelenii folosesc magie puternică - intelligence e cheia!",
                "💡 Sfat: Oltenii sunt agili și vicleni - dexterity pentru damage mare!",
                "💡 Sfat: Boss-ii la fiecare 5 nivele lasă loot epic!",
                "💡 Sfat: Upgradeează echipamentul la Fierărie pentru bonusuri mari!",
                "💡 Sfat: Taverna oferă buff-uri temporare foarte utile!",
                "💡 Sfat: Disenchant echipamentul slab pentru shards valoroși!",
                "💡 Sfat: Inspectează inamicii pentru +15% hit chance!",
                "💡 Sfat: Salvează des - nu știi când dai de un boss greu!"
        };

        int randomIndex = (int)(Math.random() * tips.length);
        System.out.println("\n" + tips[randomIndex]);
    }



// clear screen cica care ar trebui sa functioneze
    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

// doar un display de informatii utile despre dungeon
    public void displayDungeonInfo(Erou erou, int currentLevel, int checkpoint) {
        clearScreen();
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("  ║                   🗡️  GARA DE NORD  🗡️                     ║");
        System.out.println("  ╚════════════════════════════════════════════════════════════╝");

        System.out.printf("\n📍 Nivel curent: %d/50%n", currentLevel);
        System.out.printf("✅ Checkpoint: Nivel %d%n", checkpoint);
        System.out.printf("⚔️  Nivelul tău: %d%n", erou.getNivel());

        System.out.println("\n┌─ 🎯 INFORMAȚII UTILE ────────────────────────────────────┐");

        // Info despre ce inamici apar
        String enemyType = getEnemyTypeForLevel(currentLevel);
        System.out.printf("│ Nivele %d-%d: %s%n",
                ((currentLevel - 1) / 10) * 10 + 1,
                Math.min(((currentLevel - 1) / 10 + 1) * 10, 50),
                enemyType);
//la fiecare 5 nivele se seteaza un boss
        if (currentLevel % 5 == 0) {
            System.out.println("│ ⚠️  BOSS BATTLE la acest nivel!");
        }
//cu un nivel inainte de boss da mesajul dat
        if ((currentLevel + 1) % 5 == 0) {
            System.out.println("│ 🎁 Boss-ul de la nivel următor lasă loot epic!");
        }

        System.out.println("└──────────────────────────────────────────────────────────┘");
    }

//in dependenta de nivelul actual la dungeon o sa apara una din liniile astea
    private String getEnemyTypeForLevel(int level) {
        if (level <= 10) return "🐀 Inamici Slabi (Șobolani, Hoți, Cerșetori)";
        if (level <= 20) return "👹 Inamici Medii (Inspectori ANAF, Jandarmi, Politisti)";
        if (level <= 30) return "🐉 Inamici Puternici (Ceausescu, Dragnea, Vadimi)";
        if (level <= 40) return "💀 Inamici Legendari (Ororile din Romania...)";
        return "🔥 Inamici Epici (Ororile din Romania maximizate)";
    }


    /**
     * Afișează animație de level up.
     */
    public void displayLevelUpAnimation(int newLevel) {
        System.out.println("\n");
        System.out.println("    ⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐");
        System.out.println("    ⭐                                      ⭐");
        System.out.println("    ⭐        🎊  LEVEL UP!  🎊             ⭐");
        System.out.printf("    ⭐          Nivel %d Atins!              ⭐%n", newLevel+1);
        System.out.println("    ⭐                                      ⭐");
        System.out.println("    ⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐");
        System.out.println("\n💪 Ai devenit mai puternic!");
        System.out.println("🎯 Primești 2 puncte de stat pentru alocare!");
    }

    /**
     * Confirmă ieșirea din joc.
     */
    public boolean confirmExit(Scanner scanner) {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                     ⚠️  ATENȚIE  ⚠️                        ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println("\nEști sigur că vrei să ieși din joc?");
        System.out.println("Asigură-te că ai salvat progresul!");
        System.out.print("\n➤ Ieși din joc? (y/n): ");

        return Validator.readConfirmation(scanner, "");
    }
}