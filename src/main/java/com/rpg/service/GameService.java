package com.rpg.service;

import com.rpg.factory.CharacterFactory;
import com.rpg.model.characters.Erou;
import com.rpg.model.characters.Inamic;
import com.rpg.model.items.EnchantScroll;
import com.rpg.model.items.FlaskPiece;
import com.rpg.model.items.ObiectEchipament;
import com.rpg.ui.GameUI;
import com.rpg.utils.GameConstants;
import com.rpg.utils.Validator;

import java.util.List;
import java.util.Scanner;

/**
 * Service principal adaptat pentru sistemul românesc.
 * Include TownService pentru UI îmbunătățit și EnemyGeneratorRomanesc pentru inamici pe nivele.
 */
public class GameService {
    private final TrainerSmithService trainerSmithService;
    private final BattleService battleService;
    private final InventoryService inventoryService;
    private final SaveLoadService saveLoadService;
    private final ShopService shopService;
    private final EnemyGeneratorRomanesc enemyService;  // ✨ SCHIMBAT
    private final DungeonService dungeonService;
    private final GameUI gameUI;
    private final TavernService tavernService;
    private final TownService townService;  // ✨ NOU

    private static DungeonService currentDungeonService;

    public GameService() {
        this.trainerSmithService = new TrainerSmithService();
        this.battleService = new BattleService();
        this.inventoryService = new InventoryService();
        this.saveLoadService = new SaveLoadService();
        this.shopService = new ShopService();
        this.enemyService = new EnemyGeneratorRomanesc();  // ✨ SCHIMBAT
        this.gameUI = new GameUI();
        this.tavernService = new TavernService();
        this.townService = new TownService();  // ✨ NOU

        if (currentDungeonService != null) {
            this.dungeonService = currentDungeonService;
        } else {
            this.dungeonService = new DungeonService();
            currentDungeonService = this.dungeonService;
        }
    }

    /**
     * Începe jocul cu meniul de startup îmbunătățit.
     */
    public void startGame() {
        Scanner scanner = new Scanner(System.in);

        SaveLoadService.clearScreen();
        displaySplashScreen();

        Erou erou = showStartupMenu(scanner);

        if (erou == null) {
            System.out.println("👋 La revedere!");
            scanner.close();
            return;
        }

        runMainGameLoop(erou, scanner);
        scanner.close();
    }

    /**
     * Afișează splash screen-ul jocului.
     */
    private void displaySplashScreen() {
        System.out.println("\n  ╔══════════════════════════════════════════════════════════════╗");
        System.out.println("  ║                                                              ║");
        System.out.println("  ║              RPG ROMÂNESC: LEGENDA DIN BUCALE                ║");
        System.out.println("  ║                                                              ║");
        System.out.println("  ║              Aventură în stil autentic românesc              ║");
        System.out.println("  ║                                                              ║");
        System.out.println("  ╚══════════════════════════════════════════════════════════════╝");

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Meniul de startup.
     */
    private Erou showStartupMenu(Scanner scanner) {
        return gameUI.showStartupMenu(scanner, this);
    }

    /**
     * Încarcă sau creează erou din meniu.
     */
    public Erou loadHeroFromMenu(Scanner scanner) {
        SaveLoadService.clearScreen();

        System.out.println("\n  ╔════════════════════════════════════════════════════════════╗");
        System.out.println("  ║                🎮  MENIU PRINCIPAL  🎮                     ║");
        System.out.println("  ╚════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("1. 🆕 Creează Erou Nou");
        System.out.println("2. 📂 Încarcă Joc Salvat");
        System.out.println("3. ⚡ GOD MODE (Testing)");
        System.out.println("4. 🚪 Ieși din Joc");
        System.out.print("\n➤ Alege opțiunea (1-3): ");

        int choice = Validator.readValidChoice(scanner, 1, 4);

        return switch (choice) {
            case 1 -> gameUI.createNewHero(scanner);
            case 2 -> saveLoadService.loadGame(scanner);
            case 3 -> createGodModeHeroMenu(scanner);
            case 4 -> null;
            default -> null;
        };
    }

    private Erou createGodModeHeroMenu(Scanner scanner) {
        SaveLoadService.clearScreen();
        System.out.println("\n⚡ === GOD MODE CHARACTER ===");
        System.out.println("Alege clasa:");
        System.out.println("1. 💪 Moldovean (Warrior)");
        System.out.println("2. 🗡️ Oltean (Rogue)");
        System.out.println("3. 🔮 Ardelean (Wizard)");

        int classChoice = Validator.readValidChoice(scanner, 1, 3);

        CharacterFactory.CharacterClass characterClass = switch (classChoice) {
            case 1 -> CharacterFactory.CharacterClass.WARRIOR;
            case 2 -> CharacterFactory.CharacterClass.ROGUE;
            case 3 -> CharacterFactory.CharacterClass.WIZARD;
            default -> CharacterFactory.CharacterClass.WARRIOR;
        };

        System.out.print("\nNume erou (sau Enter pentru 'TestGod'): ");
        scanner.nextLine(); // consume newline
        String nume = scanner.nextLine().trim();
        if (nume.isEmpty()) {
            nume = "TestGod";
        }

        return CharacterFactory.createGodModeHero(characterClass, nume);
    }

    /**
     * Loop-ul principal al jocului.
     */
    private void runMainGameLoop(Erou erou, Scanner scanner) {
        boolean playing = true;

        while (playing && erou != null) {
            townService.displayTownMenu(erou);  // ✨ FOLOSEȘTE UI NOU

            int choice = Validator.readValidChoice(scanner, 1, 9);

            switch (choice) {
                case 1 -> handleDungeonBattle(erou, scanner);
                case 2 -> shopService.openShop(erou, scanner);
                case 3 -> tavernService.openTavern(erou, scanner);
                case 4 -> trainerSmithService.openService(erou, scanner);
                case 5 -> inventoryService.openInventory(erou, scanner);
                case 6 -> saveLoadService.saveGame(erou, scanner);
                case 7 -> {
                    Erou loadedErou = saveLoadService.loadGame(scanner);
                    if (loadedErou != null) {
                        erou = loadedErou;
                        System.out.println("✅ Joc încărcat cu succes!");
                    }
                }
                case 8 -> {
                    SaveLoadService.clearScreen();
                    erou.afiseazaStatusComplet();
                    waitForEnter(scanner);
                }
                case 9 -> {
                    if (townService.confirmExit(scanner)) {
                        playing = false;
                    }
                }
            }

            if (!erou.esteViu()) {
                handleHeroDeath(erou, scanner);
                if (!erou.esteViu()) {
                    playing = false;
                }
            }
        }
    }

    /**
     * Gestionează luptele din dungeon cu inamici romanizați.
     */
    private void handleDungeonBattle(Erou erou, Scanner scanner) {
        SaveLoadService.clearScreen();

        int startLevel = dungeonService.chooseDungeonStart(erou, scanner);
        if (startLevel == -1) {
            return;
        }

        System.out.println("🔄 Pregătirea pentru dungeon...");
        battleService.resetAbilityCooldowns(erou);

        boolean dungeonActive = true;
        int currentLevel = startLevel;

        while (dungeonActive && erou.esteViu() && dungeonService.canContinue()) {
            try {
                SaveLoadService.clearScreen();

                // ✨ AFIȘEAZĂ INFO DESPRE NIVEL
                townService.displayDungeonInfo(erou, currentLevel, dungeonService.getHighestCheckpoint());

                List<Inamic> inamici = enemyService.genereazaInamici(currentLevel);
                if (inamici.isEmpty()) {
                    System.out.println("❌ Niciun inamic generat!");
                    break;
                }

                Inamic inamic = inamici.get(0);
                System.out.println("\n🎯 Dungeon Nivel " + currentLevel);
                System.out.println("👹 " + inamic.getNume() + " (Nivel " + inamic.getNivel() + ") apare!");

                if (inamic.isBoss()) {
                    System.out.println("\n" + "═".repeat(60));
                    System.out.println("💀 BOSS BATTLE! 💀");
                    System.out.println("🌯 Boss-ii pot lăsa șaorme de Revival!");
                    System.out.println("═".repeat(60));
                }

                System.out.println("\nCe vrei să faci?");
                System.out.println("1. ⚔️  Luptă cu " + inamic.getNume());
                System.out.println("2. 🏃 Ieși din dungeon");

                int choice = Validator.readValidChoice(scanner, 1, 2);

                if (choice == 2) {
                    dungeonService.setInDungeon(false);
                    performExitDungeonActions(erou);
                    break;
                }

                boolean victory = battleService.executeBattle(erou, inamic, scanner);

                if (victory && erou.esteViu()) {
                    handleVictoryWithShaorma(erou, inamic, currentLevel, scanner);
                    boolean newCheckpoint = dungeonService.processVictory(currentLevel, erou);

                    if (newCheckpoint) {
                        System.out.println("\n🎊 FELICITĂRI pentru noul checkpoint!");
                        System.out.println("💾 Salvare automată la checkpoint...");
                        saveLoadService.autoSave(erou);

                        System.out.println("\nVrei să continui în dungeon?");
                        System.out.println("1. ✅ Continuă la următorul nivel");
                        System.out.println("2. 🏃 Ieși din dungeon (progresul e salvat)");

                        int c = Validator.readValidChoice(scanner, 1, 2);
                        if (c == 2) {
                            performExitDungeonActions(erou);
                            break;
                        }
                    }
                    currentLevel++;

                    if (erou.esteViu()) {
                       // dungeonService.saveProgressToErou(erou);
                        saveLoadService.autoSave(erou);
                    }

                } else if (!erou.esteViu()) {
                    dungeonService.processDefeat(currentLevel);
                    break;
                } else {
                    System.out.println("\n🏃 Ai fugit din luptă!");
                    dungeonService.setInDungeon(false);
                    performExitDungeonActions(erou);
                    break;
                }

            } catch (Exception e) {
                System.out.println("❌ Eroare în dungeon: " + e.getMessage());
                e.printStackTrace();
                break;
            }
        }
    }

    /**
     * Gestionează victoria cu drop de șaorma.
     */
    private void handleVictoryWithShaorma(Erou erou, Inamic inamic, int currentLevel, Scanner scanner) {
        System.out.println("\n" + "═".repeat(60));
        System.out.println("        🎉 VICTORIE! 🎉");
        System.out.println("═".repeat(60));

        int goldCastigat = inamic.getGold();
        int xpCastigat = inamic.getXpOferit();

        erou.adaugaXp(xpCastigat);
        erou.adaugaGold(goldCastigat);

        System.out.printf("\n💰 Gold câștigat: %d\n", goldCastigat);
        System.out.printf("⭐ XP câștigat: %d\n", xpCastigat);

        if (inamic.isBoss()) {
            int shaormeDropate = 1;
            erou.adaugaShaormaRevival(shaormeDropate);
            System.out.println("\n🌯 ✨ BOSS-UL A LĂSAT O ȘAORMA DE REVIVAL! ✨");


            System.out.println("\n🎲 Verificare drop bonus de la boss...");

            // Flask Pieces drop
            if (Math.random() < GameConstants.BOSS_FLASK_DROP_CHANCE / 100.0) {
                FlaskPiece.FlaskType randomType = FlaskPiece.FlaskType.values()[
                        (int)(Math.random() * FlaskPiece.FlaskType.values().length)];
                erou.addFlaskPieces(randomType, 1 + (int)(Math.random() * 3));
            }

            // Enchant Scrolls drop
            if (Math.random() < GameConstants.BOSS_SCROLL_DROP_CHANCE / 100.0) {
                EnchantScroll.EnchantType randomType = EnchantScroll.EnchantType.values()[
                        (int)(Math.random() * EnchantScroll.EnchantType.values().length)];
                int level = 1 + (int)(Math.random() * 3);
                erou.addEnchantScroll(randomType, 1, level);
            }
        }

        List<ObiectEchipament> loot = LootGenerator.rollForLoot(
                inamic.getLootTable(),
                inamic.getDropChance()
        );

        if (!loot.isEmpty()) {
            System.out.println("\n🎁 LOOT OBȚINUT:");
            for (ObiectEchipament item : loot) {
                erou.adaugaInInventar(item);
                LootGenerator.displayItemDrop(item); // ✨ AFIȘEAZĂ STATS
            }
        }

        if (erou.hasLeveledUp()) {
            townService.displayLevelUpAnimation(erou.getNivel());
            erou.processLevelUp();
        }

        waitForEnter(scanner);
    }

    /**
     * Gestionează moartea eroului.
     */
    private void handleHeroDeath(Erou erou, Scanner scanner) {
        SaveLoadService.clearScreen();
        erou.afiseazaMeniuMoarte();

        if (erou.areShaormaRevival()) {
            System.out.println("\n🌯 Vrei să folosești o Șaorma de Revival?");
            System.out.println("1. ✅ DA - Folosește Șaorma și revino în luptă!");
            System.out.println("2. ❌ NU - Acceptă înfrângerea");

            int choice = Validator.readValidChoice(scanner, 1, 2);

            if (choice == 1) {
                boolean revived = erou.folosesteShaormaRevival();
                if (revived) {
                    System.out.println("\n✨ Te-ai întors la viață!");
                    waitForEnter(scanner);
                    return;
                }
            }
        }

        System.out.println("\n💀 Game Over");
        System.out.println("Spiritul tău se stinge...");
        waitForEnter(scanner);
    }

    /**
     * Acțiuni când iese din dungeon.
     */
    private void performExitDungeonActions(Erou erou) {
        System.out.println("\n🏃 Ai ieșit din dungeon.");
        System.out.println("💾 Progresul a fost salvat automat.");
        dungeonService.setInDungeon(false);
    }

    /**
     * Așteaptă Enter.
     */
    private void waitForEnter(Scanner scanner) {
        System.out.println("\n📝 Apasă Enter pentru a continua...");
        try {
            scanner.nextLine();
        } catch (Exception e) {
            // Ignore
        }
    }
}