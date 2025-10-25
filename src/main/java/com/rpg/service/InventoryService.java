package com.rpg.service;

import com.rpg.model.characters.Erou;
import com.rpg.model.items.ObiectEchipament;
import com.rpg.utils.Validator;

import java.util.List;
import java.util.Scanner;

/**
 * Service pentru inventar cu display îmbunătățit de stats.
 */
public class InventoryService {

    /**
     * Deschide inventarul cu afișare îmbunătățită.
     */
    public void openInventory(Erou erou, Scanner scanner) {
        boolean inInventory = true;

        while (inInventory) {
            com.rpg.service.SaveLoadService.clearScreen();
            displayInventoryHeader(erou);

           // List<ObiectEchipament> inventar = erou.getInventar();
            List<ObiectEchipament> inventar = erou.getInventar().getItems();


            if (inventar.isEmpty()) {
                System.out.println("\n📦 Inventarul este gol!");
                System.out.println("\n1. 🔙 Înapoi");
                Validator.readValidChoice(scanner, 1, 1);
                return;
            }

            // ✨ AFIȘARE ÎMBUNĂTĂȚITĂ CU STATS
            displayInventoryItems(inventar);

            System.out.println("\n🎯 Opțiuni:");
            System.out.println((inventar.size() + 1) + ". 📊 Vezi status complet");
            System.out.println((inventar.size() + 2) + ". 💰 Acces rapid la vânzare");
            System.out.println((inventar.size() + 3) + ". 🔙 Înapoi");

            System.out.print("\n➤ Alege opțiunea (1-" + (inventar.size() + 3) + "): ");
            int choice = Validator.readValidChoice(scanner, 1, inventar.size() + 3);

            if (choice == inventar.size() + 1) {
                erou.afiseazaStatusComplet();
                waitForEnter();
            } else if (choice == inventar.size() + 2) {
                showQuickSellMenu(erou, scanner);
            } else if (choice == inventar.size() + 3) {
                inInventory = false;
            } else {
                ObiectEchipament selectedItem = inventar.get(choice - 1);
                handleItemActions(erou, selectedItem, scanner);
            }
        }
    }

    /**
     * ✨ METODĂ NOUĂ: Afișează header-ul inventarului
     */
    private void displayInventoryHeader(Erou erou) {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                    🎒  INVENTAR  🎒                        ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.printf("\n👤 %s | 💰 Gold: %d | 💎 Shards: %d\n",
                erou.getNume(), erou.getGold(), erou.getScrap());
        System.out.printf("📦 Obiecte: %d/%d\n", erou.getInventar().size(), 100);
    }

    /**
     * ✨ METODĂ NOUĂ: Afișează items cu toate stats-urile
     */
    private void displayInventoryItems(List<ObiectEchipament> inventar) {
        System.out.println("\n┌─ 📋 LISTA OBIECTELOR ─────────────────────────────────────┐");

        for (int i = 0; i < inventar.size(); i++) {
            ObiectEchipament item = inventar.get(i);

            String rarityIcon = getRarityIcon(item.getRaritate());
            String equippedMark = item.isEquipped() ? " ✅" : "";

            System.out.printf("\n%d. %s %s%s\n",
                    i + 1, rarityIcon, item.getNume(), equippedMark);

            // Linia 2: Tip și raritate
            System.out.printf("   📊 %s | %s | 🎯 Nivel %d\n",
                    item.getTip().getDisplayName(),
                    item.getRaritate().getDisplayName(),
                    item.getNivelNecesar());

            // Linia 3: Stats
            StringBuilder stats = new StringBuilder("   ");
            if (item.getStrengthBonus() > 0) {
                stats.append("💪 +").append(item.getStrengthBonus()).append(" STR | ");
            }
            if (item.getDexterityBonus() > 0) {
                stats.append("🎯 +").append(item.getDexterityBonus()).append(" DEX | ");
            }
            if (item.getIntelligenceBonus() > 0) {
                stats.append("🧠 +").append(item.getIntelligenceBonus()).append(" INT | ");
            }
            if (item.getDefenseBonus() > 0) {
                stats.append("🛡️ +").append(item.getDefenseBonus()).append(" DEF | ");
            }

            // Remove trailing " | "
            String statsStr = stats.toString();
            if (statsStr.endsWith(" | ")) {
                statsStr = statsStr.substring(0, statsStr.length() - 3);
            }
            System.out.println(statsStr);

            // Linia 4: Preț
            System.out.printf("   💰 %d gold\n", item.getPret());
        }

        System.out.println("\n└───────────────────────────────────────────────────────────┘");
    }

    /**
     * Gestionează acțiunile pentru un item selectat.
     */
    private void handleItemActions(Erou erou, ObiectEchipament item, Scanner scanner) {
        SaveLoadService.clearScreen();

        // Afișează detalii complete despre item
        displayItemDetails(item, erou);

        System.out.println("\n🎯 Ce vrei să faci?");
        System.out.println();

        if (item.isEquipped()) {
            System.out.println("1. 📤 Dezechipează");
            System.out.println("2. 🔙 Înapoi");

            int choice = Validator.readValidChoice(scanner, 1, 2);

            if (choice == 1) {
                erou.dezechipeaza(item);
                System.out.println("\n✅ Item dezechipat!");
                waitForEnter();
            }
        } else {
            System.out.println("1. ✅ Echipează");
            System.out.println("2. 🔙 Înapoi");

            int choice = Validator.readValidChoice(scanner, 1, 2);

            if (choice == 1) {
                if (erou.getNivel() >= item.getNivelNecesar()) {
                    erou.echipeaza(item);
                    System.out.println("\n✅ Item echipat cu succes!");
                } else {
                    System.out.printf("\n❌ Nivel prea mic! Necesită nivel %d\n", item.getNivelNecesar());
                }
                waitForEnter();
            }
        }
    }

    /**
     * ✨ METODĂ NOUĂ: Afișează detalii complete despre un item
     */
    private void displayItemDetails(ObiectEchipament item, Erou erou) {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                  📦  DETALII ITEM  📦                      ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");

        String rarityIcon = getRarityIcon(item.getRaritate());
        System.out.printf("\n%s %s %s\n", rarityIcon, item.getNume(), rarityIcon);
        System.out.println();

        System.out.printf("📊 Tip: %s\n", item.getTip().getDisplayName());
        System.out.printf("✨ Raritate: %s\n", item.getRaritate().getDisplayName());
        System.out.printf("🎯 Nivel necesar: %d", item.getNivelNecesar());

        if (erou.getNivel() < item.getNivelNecesar()) {
            System.out.printf(" ❌ (Nivelul tău: %d)\n", erou.getNivel());
        } else {
            System.out.printf(" ✅ (Poți echipa)\n");
        }

        System.out.printf("👔 Status: %s\n", item.isEquipped() ? "✅ ECHIPAT" : "📦 În inventar");

        if (item.getEnhancementLevel() > 0) {
            System.out.printf("⬆️ Enhancement: +%d\n", item.getEnhancementLevel());
        }

        System.out.println("\n📊 STATISTICI:");

        if (item.getStrengthBonus() > 0) {
            System.out.printf("   💪 Strength: +%d\n", item.getStrengthBonus());
        }
        if (item.getDexterityBonus() > 0) {
            System.out.printf("   🎯 Dexterity: +%d\n", item.getDexterityBonus());
        }
        if (item.getIntelligenceBonus() > 0) {
            System.out.printf("   🧠 Intelligence: +%d\n", item.getIntelligenceBonus());
        }
        if (item.getDefenseBonus() > 0) {
            System.out.printf("   🛡️ Defense: +%d\n", item.getDefenseBonus());
        }

        System.out.println();
        System.out.printf("💰 Valoare: %d gold\n", item.getPret());
    }

    /**
     * Meniu pentru vânzare rapidă.
     */
    private void showQuickSellMenu(Erou erou, Scanner scanner) {
        SaveLoadService.clearScreen();

        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║              💰  VÂNZARE RAPIDĂ  💰                        ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.printf("\n💰 Gold actual: %d\n", erou.getGold());

        List<ObiectEchipament> sellableItems = erou.getInventar().stream()
                .filter(item -> !item.isEquipped())
                .toList();

        if (sellableItems.isEmpty()) {
            System.out.println("\n📦 Nu ai obiecte neechipate de vânzat!");
            System.out.println("\n1. 🔙 Înapoi");
            Validator.readValidChoice(scanner, 1, 1);
            return;
        }

        int totalValue = sellableItems.stream()
                .mapToInt(this::calculateSellPrice)
                .sum();

        System.out.printf("\n📦 Obiecte disponibile: %d\n", sellableItems.size());
        System.out.printf("💰 Valoare totală: %d gold\n", totalValue);

        System.out.println("\n📋 Lista obiectelor:");
        for (int i = 0; i < sellableItems.size(); i++) {
            ObiectEchipament item = sellableItems.get(i);
            String rarityIcon = getRarityIcon(item.getRaritate());
            int sellPrice = calculateSellPrice(item);

            System.out.printf("%d. %s %s | 💰 %d gold\n",
                    i + 1, rarityIcon, item.getNume(), sellPrice);
        }

        System.out.println("\nℹ️ Mergi la Shop pentru vânzare individuală!");
        System.out.println("\n1. 🔙 Înapoi");
        Validator.readValidChoice(scanner, 1, 1);
    }

    /**
     * Calculează prețul de vânzare (50% din preț).
     */
    private int calculateSellPrice(ObiectEchipament item) {
        return item.getPret() / 2;
    }

    /**
     * Returnează icon-ul pentru raritate.
     */
    private String getRarityIcon(ObiectEchipament.Raritate raritate) {
        return switch (raritate) {
            case COMMON -> "⚪";
            case UNCOMMON -> "🟢";
            case RARE -> "🔵";
            case EPIC -> "🟣";
            case LEGENDARY -> "🟠";
        };
    }

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