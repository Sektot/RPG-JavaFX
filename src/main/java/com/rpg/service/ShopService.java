package com.rpg.service;

import com.rpg.model.characters.Erou;
import com.rpg.model.items.BuffPotion;
import com.rpg.model.items.ObiectEchipament;
import com.rpg.utils.RandomUtils;
import com.rpg.utils.Validator;

import java.util.*;

/**
 * Service îmbunătățit pentru shop cu funcția de vânzare echipament.
 * VERSIUNEA ÎMBUNĂTĂȚITĂ cu clear screen și vânzare echipament.
 */
public class ShopService {
    private List<ObiectEchipament> shopInventory;

    public ShopService() {
        generateShopInventory();
    }

    /**
     * Deschide shop-ul principal cu clear screen.
     */
    public void openShop(Erou erou, Scanner scanner) {
        boolean shopping = true;

        while (shopping) {
            SaveLoadService.clearScreen();
            showShopMenu(erou);

            System.out.println("\n🎯 Alege o categorie:");
            System.out.println();
            System.out.println("1. 🧪 Berice și Energizant");
            System.out.println("2. ✨ Buff Potions");
            System.out.println("3. 🛒 Cumpără echipament");
            System.out.println("4. 💰 Vinde echipament"); // NOUĂ OPȚIUNE!
            System.out.println("5. 🔄 Regenerează stock echipament");
            System.out.println("0. 🏠 Ieși din shop");

            System.out.print("\n➤ Alege opțiunea (1-5): ");
            int choice = Validator.readValidChoice(scanner, 0, 5);

            switch (choice) {
                case 1 -> buyPotions(erou, scanner);
                case 2 -> buyBuffPotions(erou, scanner);
                case 3 -> buyEquipment(erou, scanner);
                case 4 -> sellEquipment(erou, scanner); // FUNCȚIA NOUĂ!
                case 5 -> regenerateShopInventory(erou, scanner);
                case 0 -> shopping = false;
            }
        }
    }

    /**
     * Afișează meniul principal al shop-ului îmbunătățit.
     */
    private void showShopMenu(Erou erou) {
        System.out.println("\n" + "═".repeat(60));
        System.out.println("        🏪 MAGAZINUL AVENTURIERILOR");
        System.out.println("═".repeat(60));
        System.out.printf("👤 Bine ai venit, %s! (Nivel %d)\n", erou.getNume(), erou.getNivel());
        System.out.printf("💰 Gold disponibil: %d | 🎒 Obiecte în inventar: %d\n",
                erou.getGold(), erou.getInventar().size());

        // Afișează o statistică despre echipament
        long equippedItems = erou.getInventar().stream()
                .filter(ObiectEchipament::isEquipped)
                .count();
        System.out.printf("✅ Echipament activ: %d/%d obiecte\n",
                equippedItems, erou.getInventar().size());
    }

    // ================== FUNCȚIA NOUĂ DE VÂNZARE ECHIPAMENT ==================

    /**
     * Gestionează vânzarea echipamentului - FUNCȚIA PRINCIPALĂ NOUĂ!
     */
    private void sellEquipment(Erou erou, Scanner scanner) {
        boolean selling = true;

        while (selling) {
            SaveLoadService.clearScreen();

            System.out.println("\n" + "═".repeat(60));
            System.out.println("        💰 VÂNZARE ECHIPAMENT");
            System.out.println("═".repeat(60));
            System.out.printf("💰 Gold actual: %d\n", erou.getGold());
            System.out.println();

            List<ObiectEchipament> sellableItems = getSellableItems(erou);

            if (sellableItems.isEmpty()) {
                System.out.println("📦 Nu ai niciun obiect de vânzare în inventar!");
                System.out.println();
                System.out.println("💡 Sfat: Explorează dungeon-urile pentru a găsi echipament!");
                System.out.println();
                System.out.println("1. 🔙 Înapoi la shop");

                Validator.readValidChoice(scanner, 1, 1);
                selling = false;
                continue;
            }

            System.out.println("🎒 Obiecte disponibile pentru vânzare:");
            System.out.println();

            for (int i = 0; i < sellableItems.size(); i++) {
                ObiectEchipament item = sellableItems.get(i);
                int sellPrice = calculateSellPrice(item);
                String equippedStatus = item.isEquipped() ? " [ECHIPAT]" : "";

                System.out.printf("%d. %s%s\n", i + 1, item.getNume(), equippedStatus);
                System.out.printf("   📊 %s | Raritate: %s | Nivel: %d\n",
                        item.getTip().getDisplayName(),
                        item.getRaritate().getDisplayName(),
                        item.getNivelNecesar());

                // Afișează bonusurile
                if (!item.getBonuses().isEmpty()) {
                    System.out.print("   ✨ Bonusuri: ");
                    item.getBonuses().forEach((stat, bonus) ->
                            System.out.print("+" + bonus + " " + stat + " "));
                    System.out.println();
                }

                System.out.printf("   💰 Preț vânzare: %d gold (%.0f%% din prețul original)\n",
                        sellPrice, (sellPrice / (double) item.getPret()) * 100);

                if (item.isEquipped()) {
                    System.out.println("   ⚠️  ATENȚIE: Obiectul este echipat! Va fi dezechipat la vânzare.");
                }

                System.out.println();
            }

            System.out.println((sellableItems.size() + 1) + ". 💰 Vinde toate obiectele neechipate");
            System.out.println((sellableItems.size() + 2) + ". 🔙 Înapoi la shop");

            System.out.print("\n➤ Alege obiectul de vânzat (1-" + (sellableItems.size() + 2) + "): ");
            int choice = Validator.readValidChoice(scanner, 1, sellableItems.size() + 2);

            if (choice == sellableItems.size() + 2) {
                selling = false;
            } else if (choice == sellableItems.size() + 1) {
                sellAllUnequippedItems(erou, sellableItems, scanner);
            } else {
                sellSingleItem(erou, sellableItems.get(choice - 1), scanner);
            }
        }
    }

    /**
     * Returnează lista obiectelor care pot fi vândute.
     */
//    private List<ObiectEchipament> getSellableItems(Erou erou) {
//        return new ArrayList<>(erou.getInventar());
//    }

    private List<ObiectEchipament> getSellableItems(Erou erou) {
        return new ArrayList<>(erou.getInventar().getItems());  // <-- ADAUGĂ .getItems()
    }

    /**
     * Vinde un singur obiect.
     */
    private void sellSingleItem(Erou erou, ObiectEchipament item, Scanner scanner) {
        int sellPrice = calculateSellPrice(item);

        System.out.println("\n" + "═".repeat(50));
        System.out.println("💰 CONFIRMARE VÂNZARE");
        System.out.println("═".repeat(50));
        System.out.println("📦 Obiect: " + item.getNume());
        System.out.println("💰 Vei primi: " + sellPrice + " gold");
        System.out.println("🎯 Gold după vânzare: " + (erou.getGold() + sellPrice));

        if (item.isEquipped()) {
            System.out.println("⚠️  Obiectul va fi dezechipat înainte de vânzare!");
        }

        System.out.println();
        System.out.print("❓ Confirmi vânzarea? (y/n): ");

        if (Validator.readConfirmation(scanner, "")) {
            // Dezechipează obiectul dacă e echipat
            if (item.isEquipped()) {
                erou.dezechipeaza(item);
                System.out.println("🔧 " + item.getNume() + " a fost dezechipat.");
            }

            // Înlătură din inventar și adaugă gold
            erou.getInventar().remove(item);
            erou.adaugaGold(sellPrice);

            System.out.println("\n✅ VÂNZARE REUȘITĂ!");
            System.out.printf("💰 Ai primit %d gold pentru %s!\n", sellPrice, item.getNume());
            System.out.printf("💳 Gold total: %d\n", erou.getGold());
        } else {
            System.out.println("\n❌ Vânzarea a fost anulată.");
        }

        waitForEnter();
    }

    /**
     * Vinde toate obiectele neechipate.
     */
    private void sellAllUnequippedItems(Erou erou, List<ObiectEchipament> sellableItems, Scanner scanner) {
        List<ObiectEchipament> unequippedItems = sellableItems.stream()
                .filter(item -> !item.isEquipped())
                .toList();

        if (unequippedItems.isEmpty()) {
            System.out.println("\n📦 Nu ai obiecte neechipate de vânzat!");
            waitForEnter();
            return;
        }

        int totalValue = unequippedItems.stream()
                .mapToInt(this::calculateSellPrice)
                .sum();

        System.out.println("\n" + "═".repeat(50));
        System.out.println("💰 VÂNZARE ÎN MASĂ - OBIECTE NEECHIPATE");
        System.out.println("═".repeat(50));
        System.out.printf("📦 Obiecte de vânzat: %d\n", unequippedItems.size());
        System.out.printf("💰 Valoare totală: %d gold\n", totalValue);
        System.out.printf("🎯 Gold după vânzare: %d\n", erou.getGold() + totalValue);

        System.out.println("\n📋 Lista obiectelor:");
        for (ObiectEchipament item : unequippedItems) {
            System.out.printf("  • %s - %d gold\n", item.getNume(), calculateSellPrice(item));
        }

        System.out.println();
        System.out.print("❓ Confirmi vânzarea tuturor obiectelor neechipate? (y/n): ");

        if (Validator.readConfirmation(scanner, "")) {
            // Înlătură toate obiectele neechipate și adaugă gold-ul
            for (ObiectEchipament item : unequippedItems) {
                erou.getInventar().remove(item);
            }
            erou.adaugaGold(totalValue);

            System.out.println("\n✅ VÂNZARE ÎN MASĂ REUȘITĂ!");
            System.out.printf("💰 Ai primit %d gold pentru %d obiecte!\n",
                    totalValue, unequippedItems.size());
            System.out.printf("💳 Gold total: %d\n", erou.getGold());
        } else {
            System.out.println("\n❌ Vânzarea a fost anulată.");
        }

        waitForEnter();
    }

    /**
     * Calculează prețul de vânzare pentru un obiect (60% din prețul original).
     */
    private int calculateSellPrice(ObiectEchipament item) {
        // Prețul de vânzare este 60% din prețul original
        double sellMultiplier = 0.6;

        // Bonus pentru raritate
        switch (item.getRaritate()) {
            case UNCOMMON -> sellMultiplier = 0.65;
            case RARE -> sellMultiplier = 0.70;
            case EPIC -> sellMultiplier = 0.75;
            case LEGENDARY -> sellMultiplier = 0.80;
        }

        return Math.max(1, (int) (item.getPret() * sellMultiplier));
    }

    // ================== RESTUL METODELOR EXISTENTE (PĂSTRATE) ==================

    /**
     * Gestionează cumpărarea de poțiuni.
     */
    private void buyPotions(Erou erou, Scanner scanner) {
        boolean buyingPotions = true;

        while (buyingPotions) {
            SaveLoadService.clearScreen();

            System.out.println("\n" + "═".repeat(60));
            System.out.println("        🧪 POȚIUNI ȘI CONSUMABILE");
            System.out.println("═".repeat(60));
            System.out.printf("💰 Gold disponibil: %d\n", erou.getGold());
            System.out.println();

            System.out.println("📋 Consumabile disponibile:");
            System.out.println();

            System.out.println("1. 🧪 Berice (25 gold)");
            System.out.println("   • Restaurează 50 HP instant");
            System.out.printf("   • În posesie: %d\n", erou.getHealthPotions());
            System.out.println();

            System.out.println("2. 💙 Energizat Profi de " + erou.getTipResursa() + " (35 gold)");
            System.out.println("   • Restaurează 30 " + erou.getTipResursa());
            System.out.println("   • Bonus regenerare timp de 3 ture");
            System.out.printf("   • În posesie: %d\n", erou.getManaPotions());
            System.out.println();

            System.out.println("3. 📦 Set 5 Poțiuni Vindecare (100 gold - REDUCERE 20%!)");
            System.out.println("4. 📦 Set 3 Poțiuni " + erou.getTipResursa() + " (87 gold - REDUCERE 17%!)");
            System.out.println();
            System.out.println("0. 🔙 Înapoi la shop");

            System.out.print("\n➤ Alege opțiunea (0-4): ");
            int choice = Validator.readValidChoice(scanner, 0, 4);

            switch (choice) {
                case 1 -> buyPotion(erou, "Poțiune Vindecare", 25, "health", 1, scanner);
                case 2 -> buyPotion(erou, "Poțiune " + erou.getTipResursa(), 35, "resource", 1, scanner);
                case 3 -> buyPotion(erou, "Set 5 Poțiuni Vindecare", 100, "health", 5, scanner);
                case 4 -> buyPotion(erou, "Set 3 Poțiuni " + erou.getTipResursa(), 87, "resource", 3, scanner);
                case 0 -> buyingPotions = false;
            }
        }
    }

    private void buyBuffPotions(Erou erou, Scanner scanner) {
        boolean buyingBuffs = true;

        while (buyingBuffs) {
            SaveLoadService.clearScreen();

            System.out.println("\n" + "═".repeat(60));
            System.out.println("        ✨ BUFF POTIONS SHOP");
            System.out.println("═".repeat(60));
            System.out.printf("💰 Gold disponibil: %d\n", erou.getGold());
            System.out.println();

            System.out.println("🧪 Buff Potions disponibile:");
            System.out.println("(Toate poțiunile durează 3 lupte la nivel Basic)");
            System.out.println();

            // Strength Potion
            System.out.println("1. 💪 Strength Potion (50 gold)");
            System.out.println("   • +5 Strength pentru 3 lupte");
            System.out.println("   • Perfect pentru Warrior și damage dealers");
            System.out.printf("   • În posesie: %d\n", erou.getBuffPotionQuantity(BuffPotion.BuffType.STRENGTH));
            System.out.println();

            // Dexterity Potion
            System.out.println("2. 🏃 Dexterity Potion (50 gold)");
            System.out.println("   • +5 Dexterity pentru 3 lupte");
            System.out.println("   • Mărește dodge și hit chance");
            System.out.printf("   • În posesie: %d\n", erou.getBuffPotionQuantity(BuffPotion.BuffType.DEXTERITY));
            System.out.println();

            // Intelligence Potion
            System.out.println("3. 🧠 Intelligence Potion (50 gold)");
            System.out.println("   • +5 Intelligence pentru 3 lupte");
            System.out.println("   • Perfect pentru Mage");
            System.out.printf("   • În posesie: %d\n", erou.getBuffPotionQuantity(BuffPotion.BuffType.INTELLIGENCE));
            System.out.println();

            // Damage Potion
            System.out.println("4. ⚔️ Damage Potion (75 gold)");
            System.out.println("   • +10% Damage pentru 3 lupte");
            System.out.println("   • Bonus direct la damage-ul total");
            System.out.printf("   • În posesie: %d\n", erou.getBuffPotionQuantity(BuffPotion.BuffType.DAMAGE));
            System.out.println();

            // Defense Potion
            System.out.println("5. 🛡️ Defense Potion (75 gold)");
            System.out.println("   • +8 Defense pentru 3 lupte");
            System.out.println("   • Reduce damage-ul primit");
            System.out.printf("   • În posesie: %d\n", erou.getBuffPotionQuantity(BuffPotion.BuffType.DEFENSE));
            System.out.println();

            // Critical Potion
            System.out.println("6. ⚡ Critical Potion (100 gold)");
            System.out.println("   • +15% Critical Chance pentru 3 lupte");
            System.out.println("   • Șanse mai mari de lovituri critice");
            System.out.printf("   • În posesie: %d\n", erou.getBuffPotionQuantity(BuffPotion.BuffType.CRITICAL));
            System.out.println();

            // Speed Potion
            System.out.println("7. 💨 Speed Potion (90 gold)");
            System.out.println("   • +12% Dodge și +10% Hit Chance pentru 3 lupte");
            System.out.println("   • Combo defensiv și ofensiv");
            System.out.printf("   • În posesie: %d\n", erou.getBuffPotionQuantity(BuffPotion.BuffType.SPEED));
            System.out.println();

            // Berserker Potion
            System.out.println("8. 🔥 Berserker Potion (120 gold)");
            System.out.println("   • +20% Damage dar -5 Defense pentru 3 lupte");
            System.out.println("   • ⚠️ Risc ridicat, reward ridicat!");
            System.out.printf("   • În posesie: %d\n", erou.getBuffPotionQuantity(BuffPotion.BuffType.BERSERKER));
            System.out.println();

            // Fortification Potion
            System.out.println("9. ⛰️ Fortification Potion (120 gold)");
            System.out.println("   • +15 Defense dar -8% Damage pentru 3 lupte");
            System.out.println("   • Pentru supraviețuire în boss fights");
            System.out.printf("   • În posesie: %d\n", erou.getBuffPotionQuantity(BuffPotion.BuffType.FORTIFICATION));
            System.out.println();

            // Master Potion
            System.out.println("10. 🌟 Master Potion (250 gold) ⭐ PREMIUM");
            System.out.println("    • +3 toate statisticile + 5 Defense pentru 3 lupte");
            System.out.println("    • Balansat și puternic");
            System.out.printf("    • În posesie: %d\n", erou.getBuffPotionQuantity(BuffPotion.BuffType.MASTER));
            System.out.println();

            // Sets cu reducere
            System.out.println("11. 📦 Starter Pack (200 gold - REDUCERE 20%!)");
            System.out.println("    • 1x Strength, 1x Dexterity, 1x Intelligence");
            System.out.println();

            System.out.println("12. 📦 Combat Pack (350 gold - REDUCERE 15%!)");
            System.out.println("    • 2x Damage, 2x Defense, 1x Critical");
            System.out.println();

            System.out.println("13. 🔙 Înapoi la shop");
            System.out.println();

            System.out.print("➤ Ce vrei să cumperi? (1-13): ");
            int choice = Validator.readValidChoice(scanner, 1, 13);

            switch (choice) {
                case 1 -> buyBuffPotionSingle(erou, BuffPotion.BuffType.STRENGTH, 50, scanner);
                case 2 -> buyBuffPotionSingle(erou, BuffPotion.BuffType.DEXTERITY, 50, scanner);
                case 3 -> buyBuffPotionSingle(erou, BuffPotion.BuffType.INTELLIGENCE, 50, scanner);
                case 4 -> buyBuffPotionSingle(erou, BuffPotion.BuffType.DAMAGE, 75, scanner);
                case 5 -> buyBuffPotionSingle(erou, BuffPotion.BuffType.DEFENSE, 75, scanner);
                case 6 -> buyBuffPotionSingle(erou, BuffPotion.BuffType.CRITICAL, 100, scanner);
                case 7 -> buyBuffPotionSingle(erou, BuffPotion.BuffType.SPEED, 90, scanner);
                case 8 -> buyBuffPotionSingle(erou, BuffPotion.BuffType.BERSERKER, 120, scanner);
                case 9 -> buyBuffPotionSingle(erou, BuffPotion.BuffType.FORTIFICATION, 120, scanner);
                case 10 -> buyBuffPotionSingle(erou, BuffPotion.BuffType.MASTER, 250, scanner);
                case 11 -> buyStarterPack(erou, scanner);
                case 12 -> buyCombatPack(erou, scanner);
                case 13 -> buyingBuffs = false;
            }
        }
    }

    private void buyBuffPotionSingle(Erou erou, BuffPotion.BuffType type, int price, Scanner scanner) {
        System.out.print("\nCâte poțiuni vrei să cumperi? ");
        int quantity = Validator.readValidChoice(scanner, 1, 99);

        int totalCost = price * quantity;

        if (erou.getGold() < totalCost) {
            System.out.println("\n❌ Nu ai destul gold!");
            System.out.printf("Cost: %d gold | Ai: %d gold\n", totalCost, erou.getGold());
            waitForEnter();
            return;
        }

        System.out.printf("\nConfirmi cumpărarea a %d x %s %s pentru %d gold? (y/n): ",
                quantity, type.getIcon(), type.getDisplayName(), totalCost);

        if (Validator.readConfirmation(scanner, "")) {
            erou.scadeGold(totalCost);
            erou.addBuffPotion(type, quantity);

            System.out.println("\n✅ Achiziție reușită!");
            System.out.printf("💰 Gold rămas: %d\n", erou.getGold());
            waitForEnter();
        }
    }

    private void buyStarterPack(Erou erou, Scanner scanner) {
        int cost = 200;

        if (erou.getGold() < cost) {
            System.out.println("\n❌ Nu ai destul gold! Necesită: " + cost + " gold");
            waitForEnter();
            return;
        }

        System.out.println("\n📦 STARTER PACK:");
        System.out.println("  • 1x 💪 Strength Potion");
        System.out.println("  • 1x 🏃 Dexterity Potion");
        System.out.println("  • 1x 🧠 Intelligence Potion");
        System.out.printf("\nPreț: %d gold (economisești 50 gold!)\n", cost);
        System.out.print("Confirmi cumpărarea? (y/n): ");

        if (Validator.readConfirmation(scanner, "")) {
            erou.scadeGold(cost);
            erou.addBuffPotion(BuffPotion.BuffType.STRENGTH, 1);
            erou.addBuffPotion(BuffPotion.BuffType.DEXTERITY, 1);
            erou.addBuffPotion(BuffPotion.BuffType.INTELLIGENCE, 1);

            System.out.println("\n✅ Starter Pack cumpărat!");
            System.out.printf("💰 Gold rămas: %d\n", erou.getGold());
            waitForEnter();
        }
    }

    private void buyCombatPack(Erou erou, Scanner scanner) {
        int cost = 350;

        if (erou.getGold() < cost) {
            System.out.println("\n❌ Nu ai destul gold! Necesită: " + cost + " gold");
            waitForEnter();
            return;
        }

        System.out.println("\n📦 COMBAT PACK:");
        System.out.println("  • 2x ⚔️ Damage Potion");
        System.out.println("  • 2x 🛡️ Defense Potion");
        System.out.println("  • 1x ⚡ Critical Potion");
        System.out.printf("\nPreț: %d gold (economisești 100 gold!)\n", cost);
        System.out.print("Confirmi cumpărarea? (y/n): ");

        if (Validator.readConfirmation(scanner, "")) {
            erou.scadeGold(cost);
            erou.addBuffPotion(BuffPotion.BuffType.DAMAGE, 2);
            erou.addBuffPotion(BuffPotion.BuffType.DEFENSE, 2);
            erou.addBuffPotion(BuffPotion.BuffType.CRITICAL, 1);

            System.out.println("\n✅ Combat Pack cumpărat!");
            System.out.printf("💰 Gold rămas: %d\n", erou.getGold());
            waitForEnter();
        }
    }

    /**
     * Procesează cumpărarea unei poțiuni.
     */
    private void buyPotion(Erou erou, String itemName, int price, String type, int quantity, Scanner scanner) {
        if (erou.getGold() < price) {
            System.out.println("\n💰 Nu ai suficient gold pentru " + itemName + "!");
            waitForEnter();
            return;
        }

        System.out.println("\nVrei să cumperi " + itemName + " cu " + price + " gold?");
        if (!Validator.readConfirmation(scanner, "Confirmi cumpărarea?")) {
            return;
        }

        erou.scadeGold(price);

        if (type.equals("health")) {
            erou.adaugaHealthPotions(quantity);
            System.out.printf("\n✅ Ai cumpărat %d poțiuni de vindecare!\n", quantity);
        } else {
            erou.adaugaManaPotions(quantity);
            System.out.printf("\n✅ Ai cumpărat %d poțiuni de %s!\n", quantity, erou.getTipResursa().toLowerCase());
        }

        System.out.println("💰 Gold rămas: " + erou.getGold());
        waitForEnter();
    }

    /**
     * Gestionează cumpărarea de echipament.
     */
    private void buyEquipment(Erou erou, Scanner scanner) {
        boolean buyingEquipment = true;

        while (buyingEquipment) {
            SaveLoadService.clearScreen();

            System.out.println("\n" + "═".repeat(60));
            System.out.println("        ⚔️ ECHIPAMENT DISPONIBIL");
            System.out.println("═".repeat(60));
            System.out.printf("💰 Gold disponibil: %d\n", erou.getGold());
            System.out.println();

            if (shopInventory.isEmpty()) {
                System.out.println("📦 Shop-ul nu are echipament disponibil!");
                System.out.println("💡 Încearcă să regenerezi stock-ul.");
                System.out.println();
                System.out.println("1. 🔄 Regenerează stock");
                System.out.println("0. 🔙 Înapoi");

                int choice = Validator.readValidChoice(scanner, 0, 1);
                if (choice == 1) {
                    generateShopInventory();
                    System.out.println("✅ Stock regenerat!");
                    waitForEnter();
                } else {
                    buyingEquipment = false;
                }
                continue;
            }

            // Afișează echipamentul disponibil
            System.out.println("🛒 Echipament disponibil:");
            System.out.println();

            for (int i = 0; i < shopInventory.size(); i++) {
                ObiectEchipament item = shopInventory.get(i);
                System.out.printf("%d. %s (Nivel %d) - %d gold\n",
                        i + 1, item.getNume(), item.getNivelNecesar(), item.getPret());

                // Afișează bonusurile
                if (!item.getBonuses().isEmpty()) {
                    System.out.print("   ✨ Bonusuri: ");
                    item.getBonuses().forEach((stat, bonus) ->
                            System.out.print("+" + bonus + " " + stat + " "));
                    System.out.println();
                }

                // Verifică dacă eroul poate echipa obiectul
                if (erou.getNivel() < item.getNivelNecesar()) {
                    System.out.println("   ⚠️ Nivel necesar: " + item.getNivelNecesar());
                } else if (erou.getGold() < item.getPret()) {
                    System.out.println("   💰 Nu ai suficient gold!");
                } else {
                    System.out.println("   ✅ Poți cumpăra!");
                }
                System.out.println();
            }

            System.out.println((shopInventory.size() + 1) + ". 🔄 Regenerează stock (" + (erou.getNivel() * 10) + " gold)");
            System.out.println("0. 🔙 Înapoi");

            System.out.print("\n➤ Alege opțiunea (0-" + (shopInventory.size() + 1) + "): ");
            int choice = Validator.readValidChoice(scanner, 0, shopInventory.size() + 1);

            if (choice == 0) {
                buyingEquipment = false;
            } else if (choice == shopInventory.size() + 1) {
                regenerateShopInventory(erou, scanner);
            } else {
                buyEquipmentItem(erou, shopInventory.get(choice - 1), scanner);
            }
        }
    }

    /**
     * Procesează cumpărarea unui echipament.
     */
    private void buyEquipmentItem(Erou erou, ObiectEchipament item, Scanner scanner) {
        if (erou.getNivel() < item.getNivelNecesar()) {
            System.out.println("\n⚠️ Nu poți echipa acest obiect! Nivel necesar: " + item.getNivelNecesar());
            waitForEnter();
            return;
        }

        if (erou.getGold() < item.getPret()) {
            System.out.println("\n💰 Nu ai suficient gold pentru " + item.getNume() + "!");
            waitForEnter();
            return;
        }

        System.out.println("\n" + "═".repeat(40));
        System.out.println("🛒 CONFIRMARE CUMPĂRARE");
        System.out.println("═".repeat(40));
        System.out.println("📦 Obiect: " + item.getNume());
        System.out.println("💰 Preț: " + item.getPret() + " gold");
        System.out.println("🎯 Tip: " + item.getTip().getDisplayName());
        System.out.println("✨ Raritate: " + item.getRaritate().getDisplayName());

        if (!item.getBonuses().isEmpty()) {
            System.out.println("📊 Bonusuri:");
            item.getBonuses().forEach((stat, bonus) ->
                    System.out.println("   • +" + bonus + " " + stat));
        }

        if (!Validator.readConfirmation(scanner, "\nConfirmi cumpărarea?")) {
            return;
        }

        erou.scadeGold(item.getPret());

        // Creează o copie a obiectului pentru inventar
        ObiectEchipament itemCopy = item.createCopy();
        erou.adaugaInInventar(itemCopy);
        shopInventory.remove(item);

        System.out.println("\n✅ Ai cumpărat " + item.getNume() + "!");
        System.out.println("📦 Obiectul a fost adăugat în inventar.");
        System.out.println("💰 Gold rămas: " + erou.getGold());

        waitForEnter();
    }

    /**
     * Regenerează inventarul shop-ului.
     */
    private void regenerateShopInventory(Erou erou, Scanner scanner) {
        int cost = erou.getNivel() * 10;

        System.out.println("\n🔄 Regenerarea stock-ului costă " + cost + " gold.");
        System.out.println("Aceasta va aduce echipament nou potrivit pentru nivelul tău.");

        if (erou.getGold() < cost) {
            System.out.println("\n💰 Nu ai suficient gold!");
            waitForEnter();
            return;
        }

        if (!Validator.readConfirmation(scanner, "Vrei să regenerezi stock-ul?")) {
            return;
        }

        erou.scadeGold(cost);
        generateShopInventory();

        System.out.println("\n✅ Stock-ul a fost regenerat!");
        System.out.println("🆕 Noi obiecte sunt acum disponibile!");

        waitForEnter();
    }

    /**
     * Generează inventarul inițial al shop-ului.
     */
    private void generateShopInventory() {
        shopInventory = new ArrayList<>();

        // Generează 8-12 obiecte aleatorii pentru shop
        int itemCount = RandomUtils.randomInt(8, 12);
        for (int i = 0; i < itemCount; i++) {
            ObiectEchipament item = generateRandomEquipment();
            shopInventory.add(item);
        }

        // Sortează după nivel și raritate
        shopInventory.sort((a, b) -> {
            int levelCompare = a.getNivelNecesar() - b.getNivelNecesar();
            if (levelCompare != 0) return levelCompare;
            return b.getRaritate().ordinal() - a.getRaritate().ordinal();
        });
    }

    /**
     * Generează un echipament aleatoriu pentru shop.
     */
    private ObiectEchipament generateRandomEquipment() {
        // Folosește LootGenerator pentru consistență
        // Pentru acum, creez un obiect simplu
        ObiectEchipament.TipEchipament tip = RandomUtils.randomElement(ObiectEchipament.TipEchipament.values());
        ObiectEchipament.Raritate raritate = selectRandomRarity();
        int nivel = RandomUtils.randomInt(1, 30);
        String nume = generateItemName(tip, raritate);

        Map<String, Integer> bonuses = new HashMap<>();
        bonuses.put("strength", RandomUtils.randomInt(1, 5));
        bonuses.put("dexterity", RandomUtils.randomInt(1, 3));

        int pret = nivel * 15 + (int)(raritate.ordinal() * 25);

        return new ObiectEchipament(nume, nivel, raritate, tip,
                bonuses.getOrDefault("strength", 0),
                bonuses.getOrDefault("dexterity", 0),
                bonuses.getOrDefault("intelligence", 0),
                bonuses.getOrDefault("defense", 0),
                pret);
    }

    /**
     * Selectează o raritate aleatorie cu probabilități realiste.
     */
    private ObiectEchipament.Raritate selectRandomRarity() {
        double rand = RandomUtils.randomDouble();
        if (rand < 0.5) return ObiectEchipament.Raritate.COMMON;
        else if (rand < 0.75) return ObiectEchipament.Raritate.UNCOMMON;
        else if (rand < 0.9) return ObiectEchipament.Raritate.RARE;
        else if (rand < 0.98) return ObiectEchipament.Raritate.EPIC;
        else return ObiectEchipament.Raritate.LEGENDARY;
    }

    /**
     * Generează nume pentru obiecte.
     */
    private String generateItemName(ObiectEchipament.TipEchipament tip, ObiectEchipament.Raritate raritate) {
        String[] prefixes = {"Rusty", "Iron", "Steel", "Mystic", "Enchanted", "Legendary", "Divine"};
        String[] suffixes = {"of Power", "of Agility", "of Wisdom", "of Protection", "of the Brave"};

        String prefix = RandomUtils.randomElement(prefixes);
        String baseName = tip.getDisplayName();

        if (raritate.ordinal() >= 2) {
            String suffix = RandomUtils.randomElement(suffixes);
            return prefix + " " + baseName + " " + suffix;
        } else {
            return prefix + " " + baseName;
        }
    }

    /**
     * Așteaptă apăsarea tastei Enter.
     */
    private void waitForEnter() {
        System.out.println("\n📝 Apasă Enter pentru a continua...");
        try {
            System.in.read();
        } catch (Exception e) {
            // Ignore
        }
    }
}