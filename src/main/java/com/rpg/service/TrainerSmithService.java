package com.rpg.service;

import com.rpg.model.characters.Erou;
import com.rpg.model.items.BuffPotion;
import com.rpg.model.items.EnchantScroll;
import com.rpg.model.items.ObiectEchipament;
import com.rpg.utils.Validator;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * 🏋️ TRAINER & SMITH SERVICE - Serviciu pentru îmbunătățirea eroului
 *
 * Oferă 3 tipuri principale de upgrade-uri:
 * 1. Stats Training - Folosește stat points câștigate la level-up
 * 2. Ability Upgrades - Îmbunătățește damage și efecte abilități cu gold
 * 3. Equipment Enhancement - Upgrade echipament cu shards
 */
public class TrainerSmithService {

    /**
     * Cost pentru îmbunătățirea unei statistici (STR/DEX/INT).
     * Fiecare +1 stat consumă 1 stat point.
     * Stat points se câștigă DOAR la level-up (2 points per nivel).
     */

    private static final int STAT_UPGRADE_COST_POINTS = 1; // Cost în stat points


    /**
     * Cost de bază în gold pentru upgrade abilități (DEPRECAT - vezi BASE_ABILITY_UPGRADE_COST).
     * Această constantă nu mai este folosită, păstrată pentru compatibilitate.
     */
    private static final int BASE_ABILITY_UPGRADE_COST = 100;  // Cost pentru primul upgrade

    /**
     * Nivelul minim al EROULUI pentru PRIMUL upgrade de abilitate.
     *
     * FORMULA LEVEL REQUIREMENT: baseLevel + (upgradeLevel × 5)
     *
     * Progresie cerințe nivel:
     * - Upgrade 0→1: Nivel 5  (5 + 0×5)
     * - Upgrade 1→2: Nivel 10 (5 + 1×5)
     * - Upgrade 2→3: Nivel 15 (5 + 2×5)
     * - Upgrade 3→4: Nivel 20 (5 + 3×5)
     * - Upgrade 4→5: Nivel 25 (5 + 4×5)
     *
     * Această restricție de nivel previne upgrade-urile prea timpurii
     * și menține balanța progresiei jocului.
     */

    private static final int BASE_LEVEL_REQUIREMENT = 5;

    private final InventoryService inventoryService = new InventoryService();

    /**
     * Deschide interfața Trainer & Smith cu toate serviciile disponibile.
     *
     * Aceasta este MAIN LOOP-ul serviciului - rulează până când jucătorul
     * alege să iasă (opțiunea 5).
     *
     * @param erou   Referința la eroul jucătorului (modificat direct)
     * @param scanner Scanner pentru citirea input-ului utilizatorului
     *
     * FLOW:
     * 1. Afișează header cu statistici curente
     * 2. Prezintă 5 opțiuni de servicii
     * 3. Procesează alegerea jucătorului
     * 4. Repetă până la exit
     */

    public void openService(Erou erou, Scanner scanner) {
        // Flag pentru controlul buclei principale
        // Setează la false când jucătorul alege "Înapoi" (opțiunea 5)
        boolean inService = true;

        // ═══════════════════════════════════════════════════════════
        // 🔄 MAIN SERVICE LOOP - Rulează până la exit
        // ═══════════════════════════════════════════════════════════

        while (inService) {
            System.out.println("\n🏋️ ===== TRAINER & FIERAR =====");

            /**
             * Afișează numele și nivelul eroului.
             * Format: "👤 NumeErou | Nivel: X"
             *
             * Folosește String.format() pentru formatare precisă.
             * %s = String (nume), %d = decimal integer (nivel)
             */

            System.out.printf("👤 %s | Nivel: %d\n", erou.getNume(), erou.getNivel());
            System.out.printf("💰 Gold: %d | 🔮 Shards: %d | 🎯 Stat Points: %d\n",
                    erou.getGold(), erou.getShards(), erou.getStatPoints());
            System.out.printf("💪 STR: %d | 🎯 DEX: %d | 🧠 INT: %d\n",
                    erou.getStrength(), erou.getDexterity(), erou.getIntelligence());
            System.out.println("\n1. 🏋️ Antrenează Statistici");
            System.out.println("2. 💥 Îmbunătățește Abilități");
            System.out.println("3. 🛠️ Îmbunătățește Echipament");

            /**
             * OPȚIUNEA 4: 🌟 Servicii Suplimentare
             * - Potion Upgrades (cu Flask Pieces)
             * - Disenchant (sparge items pentru shards)
             * - Enchant Weapons (aplică scrolls)
             * - Buff Potions (poțiuni temporare)
             */

            System.out.println("4. 🌟 Servicii Suplimentare");

            /**
             * OPȚIUNEA 5: 🔙 Înapoi
             * - Iese din serviciu
             * - Se întoarce la Town Hub
             * - Setează inService = false
             */

            System.out.println("0. 🔙 Înapoi");
            System.out.print("Alege o opțiune: ");

            /**
             * Validator.readValidChoice() face următoarele:
             * 1. Citește input-ul de la tastatură
             * 2. Verifică dacă e număr valid între 1 și 5
             * 3. Gestionează input invalid (re-prompt automat)
             * 4. Returnează alegerea validată
             *
             * Parametri: scanner, min (1), max (5)
             */

            int choice = Validator.readValidChoice(scanner, 0, 5);
            switch (choice) {
                case 1 -> trainStats(erou, scanner);
                case 2 -> upgradeAbilities(erou, scanner);
                case 3 -> upgradeEquipment(erou, scanner);
                case 4 -> additionalServices(erou, scanner);
                case 0 -> inService = false;


            }
        }
    }

    private void waitForEnter() {
        System.out.println("\n📝 Apasă Enter pentru a continua...");
        try {
            System.in.read();
        } catch (Exception e) {
            // Ignore
        }
    }


    /**
     * 🏋️ TRAIN STATS - Sistem de îmbunătățire statistici cu stat points
     *
     * Permite jucătorului să convertească stat points în creșteri permanente
     * de STR/DEX/INT. Stat points se câștigă DOAR la level-up (2 per nivel).
     *
     * CARACTERISTICI:
     * - Cost fix: 1 stat point = +1 la orice stat
     * - Loop continuu până când se termină punctele sau jucătorul iese
     * - Validare automată pentru resurse insuficiente
     * - Feedback imediat cu noul stat după upgrade
     *
     * @param erou   Referința la eroul jucătorului (modificat direct)
     * @param scanner Scanner pentru citirea input-ului utilizatorului
     */

    private void trainStats(Erou erou, Scanner scanner) {

        // ═══════════════════════════════════════════════════════════════
        // ✅ VALIDARE INIȚIALĂ - Verifică dacă există stat points
        // ═══════════════════════════════════════════════════════════════

        /**
         * EARLY RETURN GUARD - Verifică condiția de intrare în serviciu.
         *
         * Dacă jucătorul are 0 sau mai puțin stat points, OPREȘTE execuția
         * și afișează mesaj informativ despre cum se obțin puncte.
         *
         * Design pattern: Guard Clause - validare timpurie pentru a evita
         * cod imbricat și pentru a face flow-ul mai clar.
         */

        if (erou.getStatPoints() <= 0) {
            System.out.println("\n⚠️ Nu ai stat points disponibile!");
            System.out.println("💡 Stat points se câștigă la level up!");
            return;
        }

        // ═══════════════════════════════════════════════════════════════
        // 🔄 TRAINING LOOP SETUP - Inițializare buclă principală
        // ═══════════════════════════════════════════════════════════════

        /**
         * Flag de control pentru bucla de training.
         *
         * Setează la FALSE în 2 situații:
         * 1. Jucătorul alege "Gata cu antrenamentul" (case 4)
         * 2. Se termină toate stat points-urile
         *
         * Design: Boolean flag pentru control explicit al loop-ului.
         */

        boolean training = true;

        /**
         * COMPOUND CONDITION LOOP - Două condiții de continuare:
         *
         * 1. training == true  → Jucătorul nu a ales să iasă
         * 2. statPoints > 0    → Mai există resurse pentru upgrade
         *
         * Loop-ul SE OPREȘTE când ORICARE condiție devine false.
         * Acest pattern previne situații unde ai stat points dar ești blocat,
         * sau situații unde continui fără puncte disponibile.
         */

        while (training && erou.getStatPoints() > 0) {
            System.out.println("\n🎯 ===== ANTRENARE STATISTICI =====");

            /**
             * Afișează numărul de stat points DISPONIBILE (nu folosite).
             * Actualizat automat după fiecare upgrade în loop.
             *
             * String.format() pentru formatare:
             * %d = decimal integer (statPoints)
             */

            System.out.printf("🎯 Stat Points disponibile: %d\n", erou.getStatPoints());
            System.out.printf("💪 Strength: %d | 🎯 Dexterity: %d | 🧠 Intelligence: %d\n",
                    erou.getStrength(), erou.getDexterity(), erou.getIntelligence());
            System.out.println("\nCe stat vrei să îmbunătățești?");

            /**
             * OPȚIUNEA 1: Upgrade STRENGTH (+1)
             *
             * EFECTE:
             * - +1 Strength permanent
             * - +8 HP maxim (conform GameConstants.HEALTH_PER_STRENGTH)
             * - +1 Damage fizic în luptă
             *
             * BEST FOR: Warriors, physical damage dealers
             *
             * Printf pentru formatare dinamică cu constanta STAT_UPGRADE_COST_POINTS.
             */

            System.out.printf("1. 💪 +1 Strength (Cost: %d stat point)\n", STAT_UPGRADE_COST_POINTS);

            /**
             * OPȚIUNEA 2: Upgrade DEXTERITY (+1)
             *
             * EFECTE:
             * - +1 Dexterity permanent
             * - +0.8% Hit Chance (conform GameConstants.HIT_CHANCE_PER_DEX)
             * - +0.6% Critical Chance (conform GameConstants.CRIT_CHANCE_PER_DEX)
             * - +0.5% Dodge Chance (conform GameConstants.DODGE_CHANCE_PER_DEX)
             *
             * BEST FOR: Rogues, classes bazate pe crit și evasion
             */

            System.out.printf("2. 🎯 +1 Dexterity (Cost: %d stat point)\n", STAT_UPGRADE_COST_POINTS);

            /**
             * OPȚIUNEA 3: Upgrade INTELLIGENCE (+1)
             *
             * EFECTE:
             * - +1 Intelligence permanent
             * - +6 Mana maxim (conform GameConstants.MANA_PER_INTELLIGENCE)
             * - Damage crescut pentru spell-uri (pentru clase caster)
             *
             * BEST FOR: Wizards, mage classes, resource-dependent builds
             */

            System.out.printf("3. 🧠 +1 Intelligence (Cost: %d stat point)\n", STAT_UPGRADE_COST_POINTS);
            System.out.println("0. 🔙 Gata cu antrenamentul");

            int choice = Validator.readValidChoice(scanner, 0, 4);
            switch (choice) {
                case 1 -> {
                    if (erou.getStatPoints() >= STAT_UPGRADE_COST_POINTS) {
                        erou.decreaseStatPoints(STAT_UPGRADE_COST_POINTS);
                        erou.increaseStrength(1);
                        System.out.println("✅ Strength a fost crescută cu 1!");
                        System.out.println("💪 Noul tău Strength: " + erou.getStrength());
                    } else {
                        System.out.println("❌ Nu ai destule stat points!");
                    }
                }
                case 2 -> {
                    if (erou.getStatPoints() >= STAT_UPGRADE_COST_POINTS) {
                        erou.decreaseStatPoints(STAT_UPGRADE_COST_POINTS);
                        erou.increaseDexterity(1);
                        System.out.println("✅ Dexterity a fost crescută cu 1!");
                        System.out.println("🎯 Noul tău Dexterity: " + erou.getDexterity());
                    } else {
                        System.out.println("❌ Nu ai destule stat points!");
                    }
                }
                case 3 -> {
                    if (erou.getStatPoints() >= STAT_UPGRADE_COST_POINTS) {
                        erou.decreaseStatPoints(STAT_UPGRADE_COST_POINTS);
                        erou.increaseIntelligence(1);
                        System.out.println("✅ Intelligence a fost crescută cu 1!");
                        System.out.println("🧠 Noul tău Intelligence: " + erou.getIntelligence());
                    } else {
                        System.out.println("❌ Nu ai destule stat points!");
                    }
                }
                case 0 -> training = false;

            }

            if (erou.getStatPoints() <= 0 && choice != 4) {
                System.out.println("\n🎯 Ai folosit toate stat points-urile!");
                System.out.println("💡 Câștigă mai multe la următorul level up!");
                training = false;
            }
        }
    }

    /**
     * 💥 UPGRADE ABILITIES - Sistem progresiv de îmbunătățire abilități
     *
     * Sistem COMPLEX cu multiple validări și feedback detaliat:
     * - Cost PROGRESIV: 100g → 200g → 300g → 400g → 500g
     * - Level REQUIREMENTS: 5 → 10 → 15 → 20 → 25
     * - Upgrade effects: +5 damage, +3% hit chance per nivel
     * - Max 5 upgrade levels per abilitate
     *
     * FLOW:
     * 1. List all abilities cu status upgrade
     * 2. Show cost și requirements pentru next upgrade
     * 3. Validează gold + hero level
     * 4. Preview upgrade effects
     * 5. Confirmă și aplică upgrade
     * 6. Display results și next upgrade info
     *
     * @param erou   Referința la eroul jucătorului (modificat direct)
     * @param scanner Scanner pentru citirea input-ului utilizatorului
     */
    private void upgradeAbilities(Erou erou, Scanner scanner) {

        // ═══════════════════════════════════════════════════════════════
        // ✅ VALIDARE INIȚIALĂ - Verifică existența abilităților
        // ═══════════════════════════════════════════════════════════════

        /**
         * Obține lista de abilități a eroului.
         *
         * JAVA 10+ VAR KEYWORD:
         * - Type inference automat (List<Abilitate>)
         * - Cod mai clean și concis
         * - Compilatorul deduce tipul din getAbilitati()
         *
         * getAbilitati() returnează:
         * - Lista de abilități specifice clasei (Moldovean/Oltean/Ardelean)
         * - Fiecare clasă are 3 abilități unice
         * - Abilități inițializate în constructor Erou
         */

        var abilitati = erou.getAbilitati();

        /**
         * GUARD CLAUSE - Verifică dacă lista este goală.
         *
         * Situații când ar fi goală (edge cases rare):
         * - Bug în inițializare
         * - Corrupted save file
         * - Clasa nouă fără abilități definite
         *
         * Design: Early return pentru cod mai clean.
         */

        if (abilitati.isEmpty()) {
            System.out.println("⚠️ Nu ai abilități de îmbunătățit.");
            return;
        }

        /**
         * Flag de control pentru bucla de upgrading.
         * Setează la FALSE când jucătorul alege "Gata" (exit option).
         */

        boolean upgrading = true;


        /**
         * MAIN UPGRADE LOOP - Rulează până când jucătorul iese manual.
         *
         * Spre deosebire de trainStats() care are și condiție de resurse,
         * acest loop rulează DOAR cu flag control pentru că:
         * - Gold-ul poate fi câștigat oricând (nu e limitat ca stat points)
         * - Jucătorul poate vrea să vadă info fără să facă upgrade
         * - Multiple abilități pot fi upgraded în aceeași sesiune
         */

        while (upgrading) {
            System.out.println("\n💥 ===== ÎMBUNĂTĂȚEȘTE ABILITĂȚI =====");
            System.out.printf("💰 Gold disponibil: %d | 🎖️ Nivel erou: %d\n",
                    erou.getGold(), erou.getNivel());
            System.out.println("\n📊 Sistem de upgrade progresiv:");
            System.out.println("   • Cost crește cu fiecare nivel: 100g → 200g → 300g → 400g → 500g");
            System.out.println("   • Nivel necesar: 5 → 10 → 15 → 20 → 25");
            System.out.println();

            /**
             * LOOP PRIN TOATE ABILILITĂȚILE pentru display complet.
             *
             * Classic indexed for-loop (nu enhanced for) pentru că avem
             * nevoie de index pentru numerotare (1, 2, 3...).
             */

            for (int i = 0; i < abilitati.size(); i++) {

                /**
                 * Obține abilitatea curentă.
                 *
                 * Type inference cu var - compilatorul știe că e Abilitate.
                 */

                var ability = abilitati.get(i);

                /**
                 * Extrage nivelul curent de upgrade (0-5).
                 *
                 * getUpgradeLevel() returnează numărul de upgrade-uri aplicate.
                 * Starts at 0 (no upgrades) → max 5 (fully upgraded).
                 */
                int currentLevel = ability.getUpgradeLevel();

                /**
                 * Extrage nivelul MAXIM permis (de obicei 5).
                 *
                 * getMaxUpgradeLevel() poate varia per abilitate dacă:
                 * - Ultimate abilities au max level mai mare
                 * - Basic abilities au max level mai mic
                 * - Future expansions cu unlock-uri speciale
                 */

                int maxLevel = ability.getMaxUpgradeLevel();


                /**
                 * Afișează numărul opțiunii și numele abilității.
                 *
                 * i + 1 pentru că:
                 * - Indexul începe de la 0 (programare)
                 * - Utilizatorul vede numerotare de la 1 (UI friendly)
                 *
                 * Format: "1. ✨ Lovitură Puternică"
                 */

                System.out.printf("%d. ✨ %s\n", i + 1, ability.getNume());

                /**
                 * Afișează PROGRESS și DAMAGE CURRENT:
                 *
                 * 📊 Upgrade Level: X/Y  → Progress bar vizual
                 * Damage: Z              → Damage-ul actual
                 *
                 * Exemplu: "📊 Upgrade Level: 2/5 | Damage: 35"
                 * (Starting damage 25 + 2×5 upgrade bonus = 35)
                 */

                System.out.printf("   📊 Upgrade Level: %d/%d | Damage: %d\n",
                        currentLevel, maxLevel, ability.getDamage());

                /**
                 * Verifică dacă mai sunt upgrade-uri disponibile.
                 *
                 * If TRUE: Afișează next upgrade info + requirements
                 * If FALSE: Afișează "MAX LEVEL" message
                 */

                if (currentLevel < maxLevel) {

                    /**
                     * CALCULEAZĂ COST pentru URMĂTORUL upgrade.
                     *
                     * Apelează helper method calculateAbilityUpgradeCost()
                     * care implementează formula:
                     * cost = BASE_ABILITY_UPGRADE_COST × (currentLevel + 1)
                     *
                     * Exemplu pentru level 2→3:
                     * cost = 100 × (2 + 1) = 300 gold
                     */
                    int upgradeCost = calculateAbilityUpgradeCost(currentLevel);

                    /**
                     * CALCULEAZĂ LEVEL REQUIREMENT pentru URMĂTORUL upgrade.
                     *
                     * Apelează helper method calculateRequiredHeroLevel()
                     * care implementează formula:
                     * required = BASE_LEVEL_REQUIREMENT + (currentLevel × 5)
                     *
                     * Exemplu pentru level 2→3:
                     * required = 5 + (2 × 5) = 15
                     */
                    int requiredLevel = calculateRequiredHeroLevel(currentLevel);

                    /**
                     * Afișează NEXT UPGRADE INFO cu ambele cerințe.
                     *
                     * Format: "💰 Next upgrade: 300 gold | 🎖️ Nivel necesar: 15"
                     *
                     * Acest line dă jucătorului INFO COMPLETĂ pentru decizie.
                     */

                    System.out.printf("   💰 Next upgrade: %d gold | 🎖️ Nivel necesar: %d\n",
                            upgradeCost, requiredLevel);

                    // ─────────────────────────────────────────────────
                    // ✅ REQUIREMENT CHECKS - Validează ambele cerințe
                    // ─────────────────────────────────────────────────

                    /**
                     * CHECK 1: Are destul GOLD?
                     *
                     * Boolean flag pentru gold availability.
                     * TRUE = poate plăti, FALSE = gold insuficient
                     */
                    boolean canAfford = erou.getGold() >= upgradeCost;

                    /**
                     * CHECK 2: Are NIVELUL necesar?
                     *
                     * Boolean flag pentru level requirement.
                     * TRUE = nivel suficient, FALSE = nivel prea mic
                     */
                    boolean hasLevel = erou.getNivel() >= requiredLevel;


                    //afiseaza daca ai deficit de gold
                    if (!canAfford) {
                        System.out.printf("   ❌ Îți lipsesc %d gold\n", upgradeCost - erou.getGold());
                    }
                    //afiseaza daca ai deficit de nivel si nivelul actual
                    if (!hasLevel) {
                        System.out.printf("   ❌ Ai nevoie de nivel %d (actuală: %d)\n",
                                requiredLevel, erou.getNivel());
                    }
                    //afiseaza daca ambele conditii sunt indeplinite si valideaza ca poti sa faci upgrade
                    if (canAfford && hasLevel) {
                        System.out.println("   ✅ Poți face upgrade!");
                    }
                } else {
                    //caz contrar daca currentLevel == max level imi zice ca nivelul maxim a fost atins
                    System.out.println("   🌟 NIVEL MAXIM ATINS!");
                }
                System.out.println();
            }
//afiseaza exit pe baza a cate abilitati sunt, astfel incat sa asigure ca ultima optiune sa fie cea de iesire
            System.out.println((abilitati.size() + 1) + ". 🔙 Gata");
            System.out.print("Alege abilitatea de îmbunătățit: ");

            //afiseaza input intre 1 si (size+1)
            //verifica daca ai ales exit, si comparatie
            int choice = Validator.readValidChoice(scanner, 1, abilitati.size() + 1);
            if (choice == abilitati.size() + 1) {
                //seteaza flag pentru a opri loop-ul
                upgrading = false;
                //Continue statement - sare peste restul loop-ului, previne procesarea unui upgrade daca user-ul a ales exit
                continue;
            }

            //obtine abilitatea aleasa din lista
            //indexarea array-ului
            //useru vede 1,2,3
            //indexurile de fapt sunt 0,1,2
            //le converteste cu - 1
            var ability = abilitati.get(choice - 1);

            //extrage nivelul curent pentru validari si calcule
            int currentLevel = ability.getUpgradeLevel();

            // verificare daca abilitatea este la nivel maxim
            if (currentLevel >= ability.getMaxUpgradeLevel()) {
                System.out.println("⚠️ Abilitatea a atins nivelul maxim!");
                //revine la inceputul loop-ului, ofera sansa sa alegi alta abilitate si nu iese complet din loop
                continue;
            }

            //cost pentru urmatorul upgrade al acesti abilitati,recalculat
            int upgradeCost = calculateAbilityUpgradeCost(currentLevel);

            //level req pentru urmatorul upgrade
            int requiredLevel = calculateRequiredHeroLevel(currentLevel);

            // Verifică gold-ul
            if (erou.getGold() < upgradeCost) {
                //mesaj pentru eroare
                System.out.println("💰 Nu ai destul gold pentru upgrade!");

                //mesaj detaliat cu cat iti lipseste si cat costa si cat ai
                System.out.printf("   Îți lipsesc %d gold (cost: %d, ai: %d)\n",
                        upgradeCost - erou.getGold(), upgradeCost, erou.getGold());

                //revine la meniul principal
                continue;
            }

            // Verifică nivelul eroului necesar pentru upgrade
            if (erou.getNivel() < requiredLevel) {

                //mesaje informationale
                System.out.println("🎖️ Nivelul tău este prea mic pentru acest upgrade!");
                System.out.printf("   Ai nevoie de nivel %d (nivel actual: %d)\n",
                        requiredLevel, erou.getNivel());
                System.out.printf("   💡 Mai trebuie să avansezi %d nivele!\n",
                        requiredLevel - erou.getNivel());
                continue;
            }

            // Afișează preview upgrade
            System.out.println("\n📈 PREVIEW UPGRADE:");

            //arata cum ar progresa abilitatea
            System.out.printf("   Nivel: %d → %d\n", currentLevel, currentLevel + 1);

            //cat dmg increase
            System.out.printf("   Damage: %d → %d (+5)\n",
                    ability.getDamage(), ability.getDamage() + 5);

            //cat hit chance increase
            System.out.printf("   Hit Chance: +%d%% → +%d%% (+3%%)\n",
                    ability.getHitChanceBonus(), ability.getHitChanceBonus() + 3);

            // Confirmă upgrade-ul, cu validator de yes/no
            //parametri scanner pentru citire si message pentru confirmare
            //si mesaj cu toate datele complete
            if (Validator.readConfirmation(scanner,
                    String.format("Confirmi upgrade pentru %s? (Cost: %d gold, Nivel necesar: %d)",
                            ability.getNume(), upgradeCost, requiredLevel))) {


                //scade goldul direct din fieldul de la erou
                erou.scadeGold(upgradeCost);

                //aplica upgrade
                //incrementeaza upgradeLevel cu 1
                //creste damage cu 5
                //creste hit chance cu 3
                //daca are si buffuri sau debuffuri da increse si la efectele lor
                ability.upgradeAbility();

                //mesaje explicative cu cat si ce s-a imbunatatit si cu progresul facut
                System.out.println("\n✅ ===== UPGRADE REUȘIT! =====");
                System.out.println("✨ " + ability.getNume() + " a fost îmbunătățită!");
                System.out.printf("🎯 Nivel nou: %d/%d\n",
                        ability.getUpgradeLevel(), ability.getMaxUpgradeLevel());
                System.out.printf("⚔️ Damage nou: %d\n", ability.getDamage());
                System.out.printf("🎲 Hit Chance nou: +%d%%\n", ability.getHitChanceBonus());
                System.out.printf("💰 Gold rămas: %d\n", erou.getGold());

                // Afișează următorul upgrade dacă mai este disponibil
                //ofera informatii daca mai sunt disponibile upgrade-uri
                if (ability.getUpgradeLevel() < ability.getMaxUpgradeLevel()) {
                    //calculeaza costul urmatorului upgrade considerand nivelul actual de upgrade
                    int nextCost = calculateAbilityUpgradeCost(ability.getUpgradeLevel());
                   //calculeaza level req
                    int nextLevel = calculateRequiredHeroLevel(ability.getUpgradeLevel());

                    //afiseaza datele
                    System.out.printf("\n💡 Next upgrade: %d gold, nivel %d necesar\n",
                            nextCost, nextLevel);
                }
                //dupa succes loop continua automat, permite ugradarea multipla in aceeasi sesiune
            }
        }
        //exit point revine la openService
    }

    //calculeaza pretul in baza costului de baza si niveluui de upgrade curent
    private int calculateAbilityUpgradeCost(int currentUpgradeLevel) {
        return BASE_ABILITY_UPGRADE_COST * (currentUpgradeLevel + 1);
    }

//calculeaza nivelul necesar pe baza la base level req si face astfel incat sa fie accesibil doar la fiecare 5 nivele
    private int calculateRequiredHeroLevel(int currentUpgradeLevel) {
        return BASE_LEVEL_REQUIREMENT + (currentUpgradeLevel * 5);
    }

//upgrade la statistici la echipament
    //cost exponential, bonusuri de raritate
    //foloseste shards nu gold
    //aplica la items nu la caracter

    //flow:
    //   1. List all inventory items cu enhancement status
    // 2. Show cost pentru next enhancement level
    // 3. Validează shards availability
    // 4. Delegate actual upgrade la enhanceEquipmentItem()
    private void upgradeEquipment(Erou erou, Scanner scanner) {
        //obtine lista completa din inventar nu si cele echipate
        //
        List<ObiectEchipament> items = erou.getInventar().getItems();

        //verifica daca echipamentul e gol
        if (items.isEmpty()) {
            System.out.println("\n⚠️ Nu ai echipament în inventar!");
            //exit method fara procesare si revine la open service
            return;
        }

        //flag de control pentru bucla
        boolean upgrading = true;
        //ruleaza pana la exit
        while (upgrading) {
            System.out.println("\n🛠️ ===== ÎMBUNĂTĂȚEȘTE ECHIPAMENT =====");
            //afiseaza shards available
            System.out.printf("🔮 Shards disponibile: %d\n", erou.getShards());
            System.out.println();

            System.out.println("📦 Echipament disponibil:");

            //loop prin tot inventarul pentru display
            for (int i = 0; i < items.size(); i++) {
                //obtine item-ul curent
                ObiectEchipament item = items.get(i);
                //calculeaza costul pentru upgrade
                int enhanceCost = item.getNextEnhancementCost();
                //construieste un display string conditionat
                //daca e upgradat ii adauga (+*)
                String enhancementDisplay = item.getEnhancementLevel() > 0 ?
                        " (+" + item.getEnhancementLevel() + ")" : "";

                //afiseaza informatii dewspre item intr-o linie
                System.out.printf("%d. %s%s\n", i + 1, item.getNume(), enhancementDisplay);
                System.out.printf("   📊 Nivel: %d | Raritate: %s | Enhancement: %d/%d\n",
                        item.getNivelNecesar(), item.getRaritate().getDisplayName(),
                        item.getEnhancementLevel(), item.getMaxEnhancementLevel());

                //verifica daca itemul mai poate fi enchanced
                if (item.canBeEnhanced()) {
                    System.out.printf("   🔮 Cost pentru +%d: %d shards\n",
                            item.getEnhancementLevel() + 1, enhanceCost);
                   //verifica daca eroul are destule shards
                    if (erou.getShards() >= enhanceCost) {
                        System.out.println("   ✅ Poți face enhancement!");
                    } else {
                        //zice daca nu ai suficient
                        System.out.println("   ❌ Shards insuficiente!");
                    }
                } else {
                    //daca nivelul max e atins
                    System.out.println("   🌟 Enhancement maxim atins!");
                }
                System.out.println();
            }

            //afisare exit dinamic in dependenta de cat echipament e
            System.out.println((items.size() + 1) + ". 🔙 Înapoi");
            System.out.print("Alege obiectul de îmbunătățit: ");
            int choice = Validator.readValidChoice(scanner, 1, items.size() + 1);

            //verifica daca utilizatoru a ales ultima optiune adica exit
            if (choice == items.size() + 1) {
               //seteaza false pentru a iesi din loop
                upgrading = false;
                continue;
            }

            //obtine item-ul selctat din lista
            ObiectEchipament selectedItem = items.get(choice - 1);
            //cheama metoda urmatoare
            //valideaza, confirma dialogul, calculeaza costul si consumul, aplica uppgrade
            //
            enhanceEquipmentItem(erou, selectedItem, scanner);
        }
    }

    /**
     * Enhanced equipment enhancement cu preview și opțiuni multiple.
     */
    private void enhanceEquipmentItem(Erou erou, ObiectEchipament item, Scanner scanner) {
        //verifica daca itemul poate fi enchanced
        if (!item.canBeEnhanced()) {
            System.out.printf("\n🌟 %s a atins nivelul maxim de enhancement!\n", item.getNume());
            waitForEnter();
            return;
        }

        //verifica  costul si cate shards ai available pentru enchant
        int enhanceCost = item.getNextEnhancementCost();
        if (erou.getShards() < enhanceCost) {
            System.out.printf("\n❌ Nu ai destule shards!\n");
            System.out.printf("🔮 Ai: %d shards | Necesare: %d shards\n",
                    erou.getShards(), enhanceCost);
            waitForEnter();
            return;
        }

        // afiseaza detalii despre obiect
        System.out.println("\n" + "═".repeat(60));
        System.out.println(" 🛠️ EQUIPMENT ENHANCEMENT");
        System.out.println("═".repeat(60));

        System.out.printf("📦 Obiect: %s\n", item.getNume());
        System.out.printf("✨ Raritate: %s | 📊 Nivel: %d\n",
                item.getRaritate().getDisplayName(), item.getNivelNecesar());
        System.out.printf("⬆️ Enhancement: %d/%d\n",
                item.getEnhancementLevel(), item.getMaxEnhancementLevel());

        // Afișează bonusurile actuale
        //getTotalBonuses() returneaza Map<String, integer>
        //numele la staturi si valorile lor
        //map pentru flexibilitate ca pot avea bonusuri diferite
        System.out.println("\n💪 BONUSURI ACTUALE:");
        Map<String, Integer> currentBonuses = item.getTotalBonuses();

        //verifica daca are bonusuri
        if (currentBonuses.isEmpty()) {
            System.out.println("  • Nici un bonus");
        } else {
            //else toate bonusurile cu stat si bonus
            currentBonuses.forEach((stat, bonus) ->
                    System.out.printf("  • +%d %s\n", bonus, stat));
        }

        // Preview bonusurile după enhancement
        System.out.println("\n✨ PREVIEW DUPĂ ENHANCEMENT +1:");

        // Simulează enhancement-ul pentru preview, creeaza o copie a itemului
        ObiectEchipament previewItem = item.createCopy();
        //apica enchant pe copy cu un nivel
        previewItem.enhanceEquipment(1);
        Map<String, Integer> newBonuses = previewItem.getTotalBonuses();

        //afiseaza bonusurile noi
        System.out.printf("⬆️ Nou enhancement level: %d\n", previewItem.getEnhancementLevel());
        System.out.println("💪 BONUSURI NOI:");

        //obtine bonusul vechi pentru un stat
        newBonuses.forEach((stat, newBonus) -> {
            //calculeaza increase-ul
            int currentBonus = currentBonuses.getOrDefault(stat, 0);
            int increase = newBonus - currentBonus;
            //afiseaza cand exista improvement
            if (increase > 0) {
                System.out.printf("  • +%d %s (+%d îmbunătățire)\n", newBonus, stat, increase);
            } else {
               //cand nu exista increase
                System.out.printf("  • +%d %s\n", newBonus, stat);
            }
        });

        // Afișează costul si shards ramase
        System.out.printf(" \n 💰 COST ENHANCEMENT: \n ");
        System.out.printf("🔮 Cost: %d shards (Rămân: %d)\n",
                enhanceCost, erou.getShards() - enhanceCost);

        System.out.println("\n🎯 Opțiuni:");
        // single time enchance
        System.out.println("1. ✅ Enhance +1 nivel");

        // Opțiune pentru enhancement multiplu dacă are destule shards
        //face loop prin metoda pana cand e posibil calculand costul exponential
        //returneaza nivelele
        int maxAffordable = calculateMaxAffordableEnhancements(item, erou.getShards());
        //doar daca se pot 2 sau mai multe
        if (maxAffordable > 1) {
            System.out.printf("2. 🚀 Enhance la maxim (%d nivele)\n", maxAffordable);
           //cand pot fi facute mai multe upgrade-uri
            System.out.println("3. 🔙 Înapoi");
        } else {
            //cannd nu e available mai multe upgrade-uri
            System.out.println("2. 🔙 Înapoi");
        }


        //calculeaza numarul maxim de optiuni pt validation
        int maxChoice = maxAffordable > 1 ? 3 : 2;
        System.out.printf("\n➤ Alege opțiunea (1-%d): ", maxChoice);
        int choice = Validator.readValidChoice(scanner, 1, maxChoice);


        //promt pt alegere de actiuni intre single ecnh sau multiple
        switch (choice) {
            case 1 -> {
                // Enhancement +1
                if (Validator.readConfirmation(scanner,
                        "Confirmi enhancement pentru " + enhanceCost + " shards?")) {

                    erou.scadeShards(enhanceCost);
                    item.enhanceEquipment(1);

                    System.out.println("\n✅ ENHANCEMENT REUȘIT!");
                    System.out.printf("⬆️ %s este acum +%d!\n",
                            item.getNume(), item.getEnhancementLevel());
                    System.out.printf("🔮 Shards rămase: %d\n", erou.getShards());

                    waitForEnter();
                }
            }
            case 2 -> {
                if (maxAffordable > 1) {
                    // Enhancement maxim
                    int totalCost = calculateTotalEnhancementCost(item, maxAffordable);
                    System.out.printf("\n🚀 ENHANCEMENT MAXIM:\n");
                    System.out.printf("⬆️ Enhancement: %d → %d\n",
                            item.getEnhancementLevel(), item.getEnhancementLevel() + maxAffordable);
                    System.out.printf("🔮 Cost total: %d shards\n", totalCost);

                    if (Validator.readConfirmation(scanner, "Confirmi enhancement maxim?")) {
                        erou.scadeShards(totalCost);
                        item.enhanceEquipment(maxAffordable);

                        System.out.println("\n🚀 ENHANCEMENT MAXIM REUȘIT!");
                        System.out.printf("⬆️ %s este acum +%d!\n",
                                item.getNume(), item.getEnhancementLevel());
                        System.out.printf("🔮 Shards rămase: %d\n", erou.getShards());

                        waitForEnter();
                    }
                }
                // else - Înapoi
            }
            case 3 -> {
                // Înapoi (dacă maxAffordable > 1)
            }
        }
    }


    //metoda helper
    // Calculează numărul maxim de enhancement-uri pe care le poate face.

    private int calculateMaxAffordableEnhancements(ObiectEchipament item, int availableShards) {
        //counter pt nr de aenchanchements care pot si facute
        //initializat la 0 si incrementat cu 1 pt fiecare nivel accesibil
        int maxEnhancements = 0;
        //cate sharduri ai(copie) nu afecteaza cat ai de fapt
        int currentShards = availableShards;
        //nivelul de start pt enchant ia nivelul curent
        int currentLevel = item.getEnhancementLevel();

        //while loop de limita superioara
        //atinge max lvl sau sharduri insuficiente si devine false
        while (currentLevel + maxEnhancements < item.getMaxEnhancementLevel()) {
            // Simulează costul pentru următorul nivel
            //creaza copie pt simulare
            ObiectEchipament tempItem = item.createCopy();
            //seteaza nivelul pt copie
            tempItem.setEnhancementLevel(currentLevel + maxEnhancements);
            //check pt daca e affordable
            int nextCost = tempItem.getNextEnhancementCost();

            //verifica sharduri simulate si le consuma
            if (currentShards >= nextCost) {
                currentShards -= nextCost;
                maxEnhancements++;
            } else {
                //opreste loopul daca nu mai ai sharduri sau daca a atins max
                break;
            }
        }

        //returneaza nr de enchant care se pot face
        return maxEnhancements;
    }


     // Calculează costul total pentru mai multe enhancement-uri.

    private int calculateTotalEnhancementCost(ObiectEchipament item, int enhancements) {
       //acumulator pt suma totala de shards
        int totalCost = 0;
        //nivelul de start pt enchant
        int currentLevel = item.getEnhancementLevel();

        //for loop cu nr fix de iteratii
        for (int i = 0; i < enhancements; i++) {
            //creaza copie
            ObiectEchipament tempItem = item.createCopy();
            //seteaa ench level
            tempItem.setEnhancementLevel(currentLevel + i);
            //acumuleaza costul total
            totalCost += tempItem.getNextEnhancementCost();
        }

        //returneaza costul total
        return totalCost;
    }



      //Meniu pentru servicii suplimentare (potion upgrades, disenchant, enchanting).

    private void additionalServices(Erou erou, Scanner scanner) {
        boolean inServices = true;

        while (inServices) {
            System.out.println("\n🌟 ===== SERVICII SUPLIMENTARE =====");
            System.out.printf("👤 %s | 💰 Gold: %d | 🔮 Shards: %d\n",
                    erou.getNume(), erou.getGold(), erou.getShards());

            System.out.println("\n🎯 Servicii disponibile:");
            System.out.println("1. 🧪 Upgrade Poțiuni (Flask Pieces)");
            System.out.println("2. 🔮 Disenchant Echipament");
            System.out.println("3. 📜 Aplicare Enchant Scrolls");
            System.out.println("4. 🧪 Upgrade Poțiuni Buffuri");
            System.out.println("5. ✨ Folosește Buff Potions");
            System.out.println("0. 🔙 Înapoi la Trainer");

            System.out.print("\n➤ Alege serviciul (1-5): ");
            int choice = Validator.readValidChoice(scanner, 0, 5);

            switch (choice) {
                case 1 -> {
                    PotionUpgradeService potionService = new PotionUpgradeService();
                    potionService.openUpgradeService(erou, scanner);
                }
                case 2 -> {
                    DisenchantService disenchantService = new DisenchantService();
                    disenchantService.openDisenchantService(erou, scanner);
                }
                case 3 -> enchantWeapons(erou, scanner);
                case 4 -> {
                    BuffPotionUpgradeService buffService = new BuffPotionUpgradeService();
                    buffService.openUpgradeService(erou, scanner);
                }
                case 5 -> useBuffPotions(erou, scanner);
                case 0 -> inServices = false;
            }
        }
    }



    /**
     * Meniu pentru aplicarea enchant scrolls pe arme - VERSIUNE ÎMBUNĂTĂȚITĂ.
     */
    private void enchantWeapons(Erou erou, Scanner scanner) {
        List<ObiectEchipament> weapons = erou.getInventar().stream()
                .filter(item -> item.getTip() == ObiectEchipament.TipEchipament.WEAPON)
                .toList();

        if (weapons.isEmpty()) {
            System.out.println("\\n⚔️ Nu ai arme în inventar!");
            waitForEnter();
            return;
        }

        Map<EnchantScroll.EnchantType, EnchantScroll> scrolls = erou.getAllEnchantScrolls();
        if (scrolls.isEmpty()) {
            System.out.println("\\n📜 Nu ai Enchant Scrolls!");
            System.out.println("💡 Enchant Scrolls se găsesc în dungeon-uri sau la shop!");
            waitForEnter();
            return;
        }

        boolean enchanting = true;
        while (enchanting) {
            System.out.println("\\n📜 " + "═".repeat(60));
            System.out.println("   WEAPON ENCHANTING - MAGICAL ENHANCEMENT");
            System.out.println("═".repeat(60));
            System.out.printf("👤 %s | 💰 Gold: %d%n", erou.getNume(), erou.getGold());

            // Afișează armele cu enchantment-urile actuale
            System.out.println("\\n⚔️ ARME DISPONIBILE:");
            for (int i = 0; i < weapons.size(); i++) {
                ObiectEchipament weapon = weapons.get(i);
                String status = weapon.isEquipped() ? " [ECHIPATĂ]" : "";
                System.out.printf("%d. %s%s%n", i + 1, weapon.getNume(), status);

                // Afișează enchantment-urile existente
                Map<String, Integer> enchants = weapon.getAllEnchantments();
                if (enchants.isEmpty()) {
                    System.out.println("   ✨ Fără enchantments");
                } else {
                    System.out.print("   🌟 Enchants: ");
                    enchants.forEach((type, damage) ->
                            System.out.printf("%s+%d %s ",
                                    getEnchantIcon(type), damage, type.toUpperCase()));
                    System.out.println();
                }
                System.out.println();
            }

            // Afișează scrolls disponibile
            System.out.println("📜 ENCHANT SCROLLS DISPONIBILE:");
            int scrollIndex = 1;
            List<EnchantScroll> availableScrolls = scrolls.values().stream()
                    .filter(EnchantScroll::canUse)
                    .toList();

            if (availableScrolls.isEmpty()) {
                System.out.println("❌ Nu ai scrolls disponibile!");
                enchanting = false;
                continue;
            }

            for (EnchantScroll scroll : availableScrolls) {
                System.out.printf("%d. %s %s (x%d)%n", scrollIndex++,
                        scroll.getType().getIcon(), scroll.getType().getDisplayName(),
                        scroll.getQuantity());
                System.out.printf("   💥 Damage: +%d %s | 💰 Cost: %d gold%n",
                        scroll.getEnchantDamage(), scroll.getType().getDamageType(),
                        scroll.getApplicationCost());
                System.out.printf("   ✨ Efect: %s%n", scroll.getType().getSpecialEffect());
                System.out.println();
            }

            System.out.println("🎯 OPȚIUNI:");
            System.out.println("1. ✨ Aplică Enchantment");
            System.out.println("2. 📊 Detalii Enchantment");
            System.out.println("3. 🔙 Înapoi");

            int choice = Validator.readValidChoice(scanner, 1, 3);

            switch (choice) {
                case 1 -> performWeaponEnchanting(erou, weapons, availableScrolls, scanner);
                case 2 -> showEnchantmentDetails(availableScrolls, scanner);
                case 3 -> enchanting = false;
            }
        }
    }


    // Afișează detalii despre enchantment-uri.
    private void showEnchantmentDetails(List<EnchantScroll> scrolls, Scanner scanner) {
        System.out.println("\\n📚 " + "═".repeat(60));
        System.out.println("   GHID ENCHANTMENT-URI");
        System.out.println("═".repeat(60));

        for (EnchantScroll scroll : scrolls) {
            EnchantScroll.EnchantType type = scroll.getType();
            System.out.printf("\\n%s %s (Nivel %d)%n",
                    type.getIcon(), type.getDisplayName(), scroll.getEnchantLevel());
            System.out.printf("🔥 Damage: +%d %s damage%n",
                    scroll.getEnchantDamage(), type.getDamageType());
            System.out.printf("✨ Efect special: %s%n", type.getSpecialEffect());
            System.out.printf("💰 Cost aplicare: %d gold%n", scroll.getApplicationCost());
            System.out.printf("📦 Disponibile: %d bucăți%n", scroll.getQuantity());

            // Detalii despre efectul special
            System.out.printf("📖 Descriere: %s%n", type.getDescription());
            System.out.println("─".repeat(40));
        }

        waitForEnter();
    }


    private void performWeaponEnchanting(Erou erou, List<ObiectEchipament> weapons,
                                         List<EnchantScroll> scrolls, Scanner scanner) {

        System.out.println("\\n⚔️ SELECTARE ARMĂ:");
        for (int i = 0; i < weapons.size(); i++) {
            ObiectEchipament weapon = weapons.get(i);
            System.out.printf("%d. %s%n", i + 1, weapon.getNume());

            // Afișează ce enchantment-uri are deja
            Map<String, Integer> enchants = weapon.getAllEnchantments();
            if (!enchants.isEmpty()) {
                System.out.print("   Enchants: ");
                enchants.forEach((type, damage) ->
                        System.out.printf("%s%s(+%d) ", getEnchantIcon(type),
                                type.toUpperCase(), damage));
                System.out.println();
            }
        }

        int weaponChoice = Validator.readValidChoice(scanner, 1, weapons.size());
        ObiectEchipament selectedWeapon = weapons.get(weaponChoice - 1);

        System.out.println("\\n📜 SELECTARE ENCHANT SCROLL:");
        for (int i = 0; i < scrolls.size(); i++) {
            EnchantScroll scroll = scrolls.get(i);
            System.out.printf("%d. %s %s%n", i + 1,
                    scroll.getType().getIcon(), scroll.getType().getDisplayName());
            System.out.printf("   💥 +%d %s damage | 💰 %d gold%n",
                    scroll.getEnchantDamage(), scroll.getType().getDamageType(),
                    scroll.getApplicationCost());

            // Verifică dacă weapon-ul are deja acest enchant
            if (selectedWeapon.hasEnchantment(scroll.getType().getDamageType())) {
                int current = selectedWeapon.getEnchantmentDamage(scroll.getType().getDamageType());
                System.out.printf("   ⚠️  Weapon-ul are deja acest enchant (+%d)%n", current);
            }
        }

        int scrollChoice = Validator.readValidChoice(scanner, 1, scrolls.size());
        EnchantScroll selectedScroll = scrolls.get(scrollChoice - 1);

        // Afișează preview-ul
        System.out.println("\\n✨ " + "═".repeat(50));
        System.out.println("   PREVIEW ENCHANTMENT");
        System.out.println("═".repeat(50));
        System.out.printf("⚔️  Armă: %s%n", selectedWeapon.getNume());
        System.out.printf("📜 Scroll: %s (Nivel %d)%n",
                selectedScroll.getType().getDisplayName(), selectedScroll.getEnchantLevel());
        System.out.printf("🔥 Enchantment: +%d %s damage%n",
                selectedScroll.getEnchantDamage(), selectedScroll.getType().getDamageType());
        System.out.printf("✨ Efect special: %s%n", selectedScroll.getType().getSpecialEffect());
        System.out.printf("💰 Cost total: %d gold (Rămân: %d)%n",
                selectedScroll.getApplicationCost(),
                erou.getGold() - selectedScroll.getApplicationCost());
        System.out.println("═".repeat(50));

        if (Validator.readConfirmation(scanner, "Confirmi aplicarea enchantment-ului?")) {
            boolean success = erou.useEnchantScroll(selectedScroll.getType(), selectedWeapon);
            if (success) {
                System.out.println("\\n🎉 ENCHANTMENT APLICAT CU SUCCES!");
            } else {
                System.out.println("\\n❌ Enchantment-ul a eșuat!");
            }
        } else {
            System.out.println("\\n🔙 Enchantment anulat.");
        }

        waitForEnter();
    }

    private String getEnchantIcon(String enchantType) {
        return switch (enchantType.toLowerCase()) {
            case "fire" -> "🔥";
            case "ice" -> "❄️";
            case "lightning" -> "⚡";
            case "poison" -> "☠️";
            case "holy" -> "✨";
            case "shadow" -> "🌑";
            case "arcane" -> "🔮";
            case "nature" -> "🌿";
            default -> "✨";
        };
    }

     //Meniu pentru folosirea buff potions.
//inca nu e folosita nicaieri si nu e completa
    private void useBuffPotions(Erou erou, Scanner scanner) {
        Map<BuffPotion.BuffType, Integer> potions = erou.getAllBuffPotions();

        if (potions.isEmpty()) {
            System.out.println("\n✨ Nu ai Buff Potions!");
            System.out.println("💡 Buff Potions se craftează sau se cumpără!");
            waitForEnter();
            return;
        }

        System.out.println("\n✨ ===== BUFF POTIONS =====");
        System.out.println("🧪 Folosește poțiuni magice pentru buffuri temporare!");



    }



//metoda veche inlocuita/poate
    private int calculateShardCost(ObiectEchipament item) {
        int baseCost = item.getNivelNecesar() * 2;
        int rarityCost = item.getRaritate().ordinal() * 5;
        return baseCost + rarityCost + 5; // Cost minim de 5 shards
    }
}