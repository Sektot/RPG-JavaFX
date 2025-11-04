package com.rpg.service;

import com.rpg.model.abilities.Abilitate;
import com.rpg.model.characters.Erou;
import com.rpg.model.characters.Inamic;
import com.rpg.model.items.BuffPotion;
import com.rpg.model.items.ObiectEchipament;
import com.rpg.utils.*;

import java.util.*;
/**
 * 🎮 BATTLE SERVICE - Versiunea îmbunătățită cu battle log progresiv
 * ✨ Features:
 * - Damage breakdown detaliat
 * - Loading animation între acțiuni
 * - Verificare corectă enchantments
 * - Battle log organizat și lizibil
 */
public class BattleService {
    private int turnNumber = 0;
    private boolean turnExecuted = false;

    // 🆕 Combo tracking - tracks the last ability used by the hero
    private String lastAbilityUsed = null;

    // ⚙️ Configurări pentru battle log
    private static final int DELAY_SHORT = 800;   // 0.8s
    private static final int DELAY_MEDIUM = 1200; // 1.2s
    private static final int DELAY_LONG = 1800;   // 1.8s
    private static final boolean ENABLE_DELAYS = false; // Set false pentru testing rapid

    // 🎨 SECȚIUNEA 1: METODE HELPER VIZUALE

    /**
     * Așteaptă cu loading animation
     */



    private void pauseWithLoading(String message, int duration) {
        if (!ENABLE_DELAYS) return;

        System.out.print(message);
        int dots = 3;
        int delayPerDot = duration / dots;

        try {
            for (int i = 0; i < dots; i++) {
                Thread.sleep(delayPerDot);
                System.out.print(".");
            }
            System.out.println();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Pauză simplă
     */
    private void pause(int duration) {
        if (!ENABLE_DELAYS) return;
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Afișare progresivă linie cu linie
     */
    private void printProgressive(String text) {
        System.out.println(text);
        pause(300);
    }

    /**
     * Box fancy pentru mesaje importante
     */
    private void printBox(String title, List<String> lines) {
        int maxWidth = Math.max(title.length(),
                lines.stream().mapToInt(String::length).max().orElse(40)) + 4;

        System.out.println("\n┌" + "─".repeat(maxWidth) + "┐");
        System.out.println("│ " + centerText(title, maxWidth - 2) + " │");
        System.out.println("├" + "─".repeat(maxWidth) + "┤");

        for (String line : lines) {
            System.out.println("│ " + padRight(line, maxWidth - 2) + " │");
            pause(200);
        }

        System.out.println("└" + "─".repeat(maxWidth) + "┘");
    }

    private String centerText(String text, int width) {
        int padding = (width - text.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + text +
                " ".repeat(Math.max(0, width - text.length() - padding));
    }

    private String padRight(String text, int width) {
        return text + " ".repeat(Math.max(0, width - text.length()));
    }

    // 🎯 SECȚIUNEA 2: DAMAGE CALCULATION - COMPLET REFĂCUT

    /**
     * 📊 DAMAGE BREAKDOWN - Calculează și afișează totul pas cu pas
     */
    private class DamageBreakdown {
        int basePhysical = 0;
        int weaponBonus = 0;
        int statBonus = 0;
        Map<String, Integer> enchantDamage = new HashMap<>();
        int totalBeforeDefense = 0;
        int defense = 0;
        int finalDamage = 0;
        boolean isCrit = false;
        double critMultiplier = 1.0;

        void display(String attackerName) {
            List<String> lines = new java.util.ArrayList<>();

            lines.add("⚔️ Atacator: " + attackerName);
            lines.add("");
            lines.add("📊 CALCULUL DAMAGE-ULUI:");
            lines.add("├─ Base Strength: " + basePhysical);

            if (weaponBonus > 0) {
                lines.add("├─ Weapon Bonus: +" + weaponBonus);
            }

            if (statBonus > 0) {
                lines.add("├─ Stat Bonus: +" + statBonus);
            }

            if (!enchantDamage.isEmpty()) {
                lines.add("├─ 🌟 ENCHANTMENTS:");
                enchantDamage.forEach((type, dmg) -> {
                    String icon = getEnchantIcon(type);
                    lines.add("│  " + icon + " " + type.toUpperCase() + ": +" + dmg);
                });
            }

            lines.add("├─ Total Before Defense: " + totalBeforeDefense);

            if (isCrit) {
                lines.add("├─ 💥 CRITICAL HIT! (x" + String.format("%.1f", critMultiplier) + ")");
                lines.add("├─ After Crit: " + (int)(totalBeforeDefense * critMultiplier));
            }

            lines.add("├─ 🛡️ Enemy Defense: -" + defense);
            lines.add("└─ ⚡ FINAL DAMAGE: " + finalDamage);

            printBox("⚔️ DAMAGE CALCULATION", lines);
        }
    }

    /**
     * 🎯 CALCULEAZĂ DAMAGE-UL COMPLET - Versiunea corectată
     */
    private DamageBreakdown calculateDetailedDamage(Erou erou, Inamic inamic, boolean isAbility,
                                                    Abilitate ability) {
        DamageBreakdown breakdown = new DamageBreakdown();

        pauseWithLoading("⚙️ Calculare damage", DELAY_SHORT);

        // 1️⃣ BASE PHYSICAL DAMAGE
        breakdown.basePhysical = erou.getStrengthTotal();
        breakdown.statBonus = erou.getStrengthTotal() - erou.getStrength();

        // 2️⃣ WEAPON DAMAGE
        ObiectEchipament weapon = erou.getEchipat().get("Weapon");
        if (weapon != null) {
            breakdown.weaponBonus = weapon.getAttackBonus();

            // 3️⃣ ENCHANTMENT DAMAGE - CORECT!
            Map<String, Integer> enchants = weapon.getAllEnchantments();
            for (Map.Entry<String, Integer> enchant : enchants.entrySet()) {
                String type = enchant.getKey();
                int baseDmg = enchant.getValue();

                // Aplică multiplicatori pentru rezistențe
                double multiplier = getDamageMultiplier(type, inamic);
                int finalEnchantDmg = (int)(baseDmg * multiplier);

                breakdown.enchantDamage.put(type, finalEnchantDmg);
            }
        }

        // 4️⃣ ABILITY DAMAGE (dacă e cazul)
        int abilityDamage = 0;
        if (isAbility && ability != null) {
            Map<String, Integer> statsMap = new HashMap<>();
            statsMap.put("strength", erou.getStrengthTotal());
            statsMap.put("dexterity", erou.getDexterityTotal());
            statsMap.put("intelligence", erou.getIntelligenceTotal());
            abilityDamage = ability.calculeazaDamage(statsMap);
        }

        // 5️⃣ TOTAL BEFORE DEFENSE
        breakdown.totalBeforeDefense = breakdown.basePhysical + breakdown.weaponBonus +
                breakdown.enchantDamage.values().stream()
                        .mapToInt(Integer::intValue).sum() + abilityDamage;

        // 6️⃣ CRIT CHECK
        double critChance = erou.getCritChanceTotal();
        if (inamic.esteInspectat()) {
            critChance += 15.0;
        }
        breakdown.isCrit = RandomUtils.chancePercent(critChance);

        if (breakdown.isCrit) {
            breakdown.critMultiplier = GameConstants.CRIT_DAMAGE_MULTIPLIER;
            breakdown.totalBeforeDefense = (int)(breakdown.totalBeforeDefense * breakdown.critMultiplier);
        }

        // 7️⃣ APPLY DEFENSE
        breakdown.defense = inamic.getDefenseTotal();
        breakdown.finalDamage = Math.max(1, breakdown.totalBeforeDefense - breakdown.defense);

        return breakdown;
    }

    /**
     * Returnează multiplicatorul de damage pentru enchantments
     */
    private double getDamageMultiplier(String damageType, Inamic target) {
        if (target.getTipDamageVulnerabil() != null &&
                target.getTipDamageVulnerabil().equalsIgnoreCase(damageType)) {
            return 1.5; // +50% damage
        } else if (target.getTipDamageRezistent() != null &&
                target.getTipDamageRezistent().equalsIgnoreCase(damageType)) {
            return 0.5; // -50% damage
        }
        return 1.0;
    }

    /**
     * Icon pentru fiecare tip de enchant
     */
    private String getEnchantIcon(String type) {
        return switch (type.toLowerCase()) {
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

    // ⚔️ SECȚIUNEA 3: EXECUTE BATTLE - REFĂCUT COMPLET

    /**
     * 🎮 EXECUTE BATTLE - Main battle loop îmbunătățit
     */
    public boolean executeBattle(Erou erou, Inamic inamic, Scanner scanner) {
        if (erou == null || inamic == null) {
            System.out.println("❌ Eroare: Erou sau inamic null!");
            return false;
        }

        turnNumber = 0;
        displayBattleStart(erou, inamic);
        pause(DELAY_LONG);

        while (erou.esteViu() && inamic.esteViu()) {
            try {
                turnNumber++;

                // 🎯 TURN HEADER
                SaveLoadService.clearScreen();
                displayTurnHeader(turnNumber);
                pause(DELAY_SHORT);

                // 📊 STATUS
                displayCurrentStatus(erou, inamic);
                pause(DELAY_MEDIUM);

                // ⚔️ TURA EROULUI
                boolean continueBattle = executeTurnErou(erou, inamic, scanner);
                if (!continueBattle) {
                    displayFleeMessage(erou.getNume());
                    return false;
                }

                if (!turnExecuted) {
                    continue;
                }

                if (!inamic.esteViu()) {
                    break;
                }

                pause(DELAY_MEDIUM);
                System.out.println("\n" + "─".repeat(50));
                pause(DELAY_SHORT);

                // 👹 TURA INAMICULUI
                executeTurnInamic(erou, inamic);

                pause(DELAY_MEDIUM);

                // 🔄 EFECTE END OF TURN
                processTurnEffects(erou, inamic);

                waitForInput(scanner, "\n⏸️ Apasă Enter pentru următoarea tură...");

            } catch (Exception e) {
                System.out.println("❌ Eroare în timpul luptei: " + e.getMessage());
                return false;
            }
        }

        return processBattleResult(erou, inamic);
    }

    /**
     * 🎯 TURA EROULUI - Cu meniu îmbunătățit
     */
    private boolean executeTurnErou(Erou erou, Inamic inamic, Scanner scanner) {
        turnExecuted = false;

        while (!turnExecuted) {
            System.out.println("\n⚔️ TURA TA - Alege acțiunea:");
            System.out.println();
            System.out.println("1. 🗡️ Atac Normal");
            System.out.println("2. ✨ Folosește Abilitate");
            System.out.println("3. 🧪 Folosește Poțiune");
            System.out.println("4. 🔍 Inspectează Inamicul");
            System.out.println("5. 🏃 Fugi din Luptă");
            System.out.println();

            int choice = com.rpg.utils.Validator.readValidChoice(scanner, 1, 5);

            switch (choice) {
                case 1 -> {
                    executeNormalAttackEnhanced(erou, inamic);
                    turnExecuted = true;
                }
                case 2 -> {
                    boolean used = executeAbilityAttackEnhanced(erou, inamic, scanner);
                    turnExecuted = used;
                }
                case 3 -> {
                    boolean used = executeUsePotionExtended(erou, scanner);
                    turnExecuted = used;
                }
                case 4 -> {
                    executeInspectEnemy(erou, inamic);
                    turnExecuted = true;
                }
                case 5 -> {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * ⚔️ ATAC NORMAL - Versiunea îmbunătățită cu breakdown
     */
    private void executeNormalAttackEnhanced(Erou erou, Inamic inamic) {
        System.out.println("\n" + "═".repeat(50));
        System.out.println("⚔️ ATAC NORMAL");
        System.out.println("═".repeat(50));

        pauseWithLoading("⚙️ Pregătire atac", DELAY_SHORT);

        // 1️⃣ HIT CHANCE
        double hitChance = erou.getHitChance();
        if (inamic.esteInspectat()) {
            hitChance += 15.0;
        }

        System.out.printf("\n🎯 Șansă lovire: %.1f%%\n", hitChance);
        if (inamic.esteInspectat()) {
            printProgressive("✨ Bonus inspecție: +15% hit chance");
        }

        pauseWithLoading("🎲 Verificare hit", DELAY_MEDIUM);

        // 2️⃣ ROLL HIT
        if (RandomUtils.chancePercent(hitChance)) {
            System.out.println("✅ LOVITURĂ REUȘITĂ!");
            pause(DELAY_SHORT);

            BattleOneLiners.displayNormalAttackOneLiner(erou);

            // 3️⃣ CALCULEAZĂ ȘI AFIȘEAZĂ DAMAGE
            DamageBreakdown breakdown = calculateDetailedDamage(erou, inamic, false, null);
            breakdown.display(erou.getNume());

            pause(DELAY_MEDIUM);

            // 4️⃣ APLICĂ DAMAGE
            if (breakdown.isCrit) {
                System.out.println("\n═══════════════════════════");
                System.out.println("     ⭐ CRITICAL HIT! ⭐");
                System.out.println(" ═══════════════════════════");
                pause(DELAY_SHORT);
            }

            pauseWithLoading("⚡ Aplicare damage", DELAY_SHORT);
            inamic.iaDamage(breakdown.finalDamage);

            System.out.printf("\n💢 %s primește %d damage!\n",
                    inamic.getNume(), breakdown.finalDamage);
            System.out.printf("❤️ %s: %d/%d HP\n",
                    inamic.getNume(), inamic.getViata(), inamic.getViataMaxima());

            // 5️⃣ APLICĂ ENCHANTMENT EFFECTS
            if (!breakdown.enchantDamage.isEmpty()) {
                pause(DELAY_MEDIUM);
                System.out.println("\n🌟 EFECTE ENCHANTMENT:");
                applyEnchantmentEffectsEnhanced(breakdown.enchantDamage, inamic, erou);
            }

//            // 6️⃣ RAGE GEN pentru Warrior
//            if (erou instanceof rpg.model.characters.classes.Moldovean warrior) {
//                Moldovean.generateRageFromAttack(breakdown.finalDamage);
//            }

        } else {
            System.out.println("❌ LOVITURĂ RATATĂ!");
            pause(DELAY_SHORT);
            System.out.println("💨 " + erou.getNume() + " ratează atacul...");
        }

        pause(DELAY_MEDIUM);
    }

    /**
     * 🌟 APLICĂ EFECTELE ENCHANTMENT-URILOR - Îmbunătățit
     */
    private void applyEnchantmentEffectsEnhanced(Map<String, Integer> enchantDamage,
                                                 Inamic target, Erou attacker) {
        for (Map.Entry<String, Integer> entry : enchantDamage.entrySet()) {
            String type = entry.getKey();
            int damage = entry.getValue();

            pause(400);

            switch (type.toLowerCase()) {
                case "fire" -> {
                    int burnDmg = damage / 2;
                    target.aplicaDebuff("burn", 3, burnDmg);
                    System.out.println("🔥 Target burns! (-" + burnDmg + " HP/turn x3)");
                }
                case "ice" -> {
                    target.aplicaDebuff("slow", 2, 0);
                    System.out.println("❄️ Target slowed! (-30% DEX x2 turns)");
                }
                case "lightning" -> {
                    if (RandomUtils.chancePercent(5.0)) {
                        target.aplicaDebuff("stun", 1, 0);
                        System.out.println("⚡ Target STUNNED! (1 turn)");
                    } else {
                        System.out.println("⚡ Lightning crackles...");
                    }
                }
                case "poison" -> {
                    int poisonDmg = damage / 3;
                    target.aplicaDebuff("poison", 3, poisonDmg);
                    System.out.println("☠️ Target poisoned! (-" + poisonDmg + " HP/turn x3)");
                }
                case "holy" -> {
                    int heal = Math.max(1, damage / 10);
                    attacker.vindeca(heal);
                    System.out.println("✨ Holy light heals +" + heal + " HP!");
                }
                case "shadow" -> {
                    int lifesteal = Math.max(1, damage * 15 / 100);
                    attacker.vindeca(lifesteal);
                    System.out.println("🌑 Shadow lifesteal +" + lifesteal + " HP!");
                }
                case "arcane" -> {
                    System.out.println("🔮 Arcane energy distorts reality!");
                }
                case "nature" -> {
                    int regen = Math.max(1, damage * 5 / 100);
                    Map<String, Double> regenBuff = new HashMap<>();
                    regenBuff.put("heal_per_turn", (double)regen);
                    attacker.aplicaBuff("regeneration", regenBuff, 3);
                    System.out.println("🌿 Nature regen: +" + regen + " HP/turn x3!");
                }
            }
        }
    }

    private String formatStatName(String stat) {
        return switch (stat.toLowerCase()) {
            case "strength" -> "💪 Strength";
            case "dexterity" -> "🎯 Dexterity";
            case "intelligence" -> "🧠 Intelligence";
            case "defense" -> "🛡️ Defense";
            case "damage_bonus" -> "⚔️ Damage";
            case "crit_chance" -> "💥 Crit Chance";
            case "dodge_chance" -> "💨 Dodge Chance";
            case "damage_reduction" -> "🛡️ Damage Reduction";
            default -> stat;
        };
    }

    /**
     * ✨ ABILITY ATTACK - Versiunea îmbunătățită
     */
    private boolean executeAbilityAttackEnhanced(Erou erou, Inamic inamic, Scanner scanner) {
        List<Abilitate> abilitati = erou.getAbilitati();
        if (abilitati.isEmpty()) {
            System.out.println("❌ Nu ai abilități disponibile!");
            pause(DELAY_SHORT);
            return false;
        }

        System.out.println("\n✨ ABILITĂȚI DISPONIBILE:");
        System.out.println("(Apasă numărul abilității pentru detalii)");
        System.out.println();

        for (int i = 0; i < abilitati.size(); i++) {
            Abilitate ab = abilitati.get(i);

            // Linia 1: Nume și status
            System.out.printf("%d. %s", i + 1, ab.getNume());

            if (ab.isUltimate()) {
                System.out.print(" 🌟 ULTIMATE");
            }

            if (!ab.poateFiFolosita()) {
                System.out.printf(" ⏱️ (Cooldown: %d ture)", ab.getCooldownRamasa());
            }
            System.out.println();

            // Linia 2: Short tooltip
            System.out.print("   ");
            System.out.println(com.rpg.utils.AbilityTooltipGenerator.generateShortTooltip(ab, erou));

            System.out.println(); // Linie goală între abilități
        }

        System.out.println((abilitati.size() + 1) + ". ❌ Anulează");
        System.out.println((abilitati.size() + 2) + ". 📖 Vezi Detalii Abilitate");

        int choice = Validator.readValidChoice(scanner, 1, abilitati.size() + 2);

        // Check if user wants to see details
        if (choice == abilitati.size() + 2) {
            System.out.println("\n📖 DETALII ABILITĂȚI:");
            System.out.println("Alege abilitatea pentru a vedea detalii complete:");

            for (int i = 0; i < abilitati.size(); i++) {
                System.out.printf("%d. %s\n", i + 1, abilitati.get(i).getNume());
            }
            System.out.println((abilitati.size() + 1) + ". ❌ Înapoi");

            int detailChoice = Validator.readValidChoice(scanner, 1, abilitati.size() + 1);

            if (detailChoice == abilitati.size() + 1) {
                return false; // Go back
            }

            Abilitate selectedAbility = abilitati.get(detailChoice - 1);

            // Show detailed tooltip
            System.out.println("\n");
            System.out.println(com.rpg.utils.AbilityTooltipGenerator.generateTooltip(selectedAbility, erou));
            System.out.println("\nApasă ENTER pentru a continua...");
            scanner.nextLine();

            return false; // Go back to ability selection
        }

        if (choice == abilitati.size() + 1) {
            return false;
        }

        Abilitate abilitate = abilitati.get(choice - 1);

        if (!abilitate.poateFiFolosita()) {
            System.out.println("❌ Abilitate în cooldown!");
            pause(DELAY_SHORT);
            return false;
        }

        if (!erou.areResursaSuficienta(abilitate)) {
            System.out.println("❌ Resurse insuficiente!");
            pause(DELAY_SHORT);
            return false;
        }

        // 🆕 EXECUTĂ ABILITATEA CU TOATE MECANICILE NOI

        System.out.println("\n" + "═".repeat(50));

        // 🆕 ULTIMATE INDICATOR
        if (abilitate.isUltimate()) {
            System.out.println("🌟⚡ ULTIMATE ABILITY ⚡🌟");
        }

        System.out.println("✨ FOLOSIRE ABILITATE: " + abilitate.getNume());
        System.out.println("═".repeat(50));

        // 🆕 SELF-DAMAGE (Berserker abilities)
        if (abilitate.getSelfDamage() > 0) {
            erou.iaDamage(abilitate.getSelfDamage());
            System.out.println("💔 " + erou.getNume() + " își sacrifică " + abilitate.getSelfDamage() + " HP pentru putere!");
            pause(DELAY_SHORT);

            if (!erou.esteViu()) {
                System.out.println("💀 " + erou.getNume() + " și-a sacrificat prea multă viață!");
                return false;
            }
        }

        erou.consumaResursa(abilitate);
        abilitate.aplicaCooldown();

        // 🆕 HEALING (healAmount or healPercent)
        if (abilitate.getHealAmount() > 0 || abilitate.getHealPercent() > 0) {
            int healAmount = abilitate.getHealAmount();

            // Percentage-based healing
            if (abilitate.getHealPercent() > 0) {
                healAmount += (int) (erou.getViataMaxima() * abilitate.getHealPercent());
            }

            if (healAmount > 0) {
                erou.vindeca(healAmount);
                System.out.println("💚 " + erou.getNume() + " se vindecă cu " + healAmount + " HP!");
                pause(DELAY_SHORT);
            }
        }

        // ✅ APPLY BUFF TO HERO IF ABILITY HAS BUFF
        if (abilitate.getBuffAplicat() != null && !abilitate.getModificatoriBuff().isEmpty()) {
            erou.aplicaBuff(abilitate.getBuffAplicat(), abilitate.getModificatoriBuff(), abilitate.getDurataBuff());
            System.out.println("✨ Buff aplicat: " + abilitate.getBuffAplicat() + " pentru " + abilitate.getDurataBuff() + " ture!");
            pause(DELAY_SHORT);
        }

        pauseWithLoading("⚡ Concentrare energie magică", DELAY_MEDIUM);

        // Calculează damage
        Map<String, Integer> statsMap = new HashMap<>();
        statsMap.put("strength", erou.getStrengthTotal());
        statsMap.put("dexterity", erou.getDexterityTotal());
        statsMap.put("intelligence", erou.getIntelligenceTotal());

        int abilityDamage = abilitate.calculeazaDamage(statsMap);

        // 🆕 COMBO BONUS DAMAGE
        boolean comboActivated = false;
        if (abilitate.getComboRequirement() != null && !abilitate.getComboRequirement().isEmpty()) {
            if (abilitate.getComboRequirement().equals(lastAbilityUsed)) {
                comboActivated = true;
                int bonusDamage = (int) (abilityDamage * abilitate.getComboBonusDamage());
                abilityDamage += bonusDamage;
                System.out.println("🔥 COMBO ACTIVATED! +" + bonusDamage + " bonus damage!");
                pause(DELAY_SHORT);
            } else {
                System.out.println("⚠️ Combo failed! Need to use " + abilitate.getComboRequirement() + " first.");
                pause(DELAY_SHORT);
            }
        }

        // Track this ability for future combos
        lastAbilityUsed = abilitate.getNume();

        // Only deal damage if ability has damage (some abilities are pure buffs)
        if (abilityDamage > 0) {
            // 🆕 MULTI-HIT MECHANICS
            int numberOfHits = Math.max(1, abilitate.getNumberOfHits());
            int totalDamageDealt = 0;

            if (numberOfHits > 1) {
                System.out.println("⚔️ Multi-hit ability! Strikes " + numberOfHits + " times!");
                pause(DELAY_SHORT);
            }

            for (int hit = 1; hit <= numberOfHits; hit++) {
                if (!inamic.esteViu()) break;  // Stop if enemy dies mid-combo

                if (numberOfHits > 1) {
                    System.out.println("\n  ➤ Hit " + hit + "/" + numberOfHits + ":");
                }

                // Hit check
                double hitChance = erou.getHitChance() + abilitate.getHitChanceBonus();
                if (inamic.esteInspectat()) {
                    hitChance += 15.0;
                }

                if (numberOfHits == 1) {
                    System.out.printf("\n🎯 Șansă lovire: %.1f%%\n", hitChance);
                    pauseWithLoading("🎲 Verificare hit", DELAY_MEDIUM);
                }

                if (RandomUtils.chancePercent(hitChance)) {
                    System.out.println("    ✅ HIT!");
                    pause(DELAY_SHORT);

                    // Calculate damage for this hit
                    int hitDamage = abilityDamage;

                    // ✅ CRITICAL HIT CHECK (per hit)
                    double critChance = erou.getCritChanceTotal();
                    boolean isCrit = RandomUtils.chancePercent(critChance);

                    if (isCrit) {
                        hitDamage = (int) (hitDamage * erou.getCritMultiplierTotal());
                        System.out.println("    ⚡ CRITICAL HIT!");
                        pause(DELAY_SHORT);
                    }

                    int actualDamage = inamic.primesteDamage(hitDamage);
                    totalDamageDealt += actualDamage;

                    System.out.printf("    💥 %d damage!%s\n", actualDamage, (isCrit ? " (CRIT)" : ""));
                    pause(DELAY_SHORT);

                    // ✅ LIFESTEAL from run items + talent tree (per hit)
                    double lifestealPercent = erou.getLifestealTotal();
                    if (lifestealPercent > 0) {
                        int healAmount = (int) (actualDamage * lifestealPercent);
                        if (healAmount > 0) {
                            erou.vindeca(healAmount);
                            System.out.println("    🩸 Lifesteal: +" + healAmount + " HP!");
                            pause(DELAY_SHORT);
                        }
                    }
                } else {
                    System.out.println("    ❌ Miss!");
                }
            }

            if (numberOfHits > 1 && totalDamageDealt > 0) {
                System.out.println("\n💥 Total damage: " + totalDamageDealt + "!");
                pause(DELAY_MEDIUM);
            }

            // Debuff/Buff effects
            if (abilitate.getDebuffAplicat() != null) {
                pause(DELAY_SHORT);
                inamic.aplicaDebuff(abilitate.getDebuffAplicat(),
                        abilitate.getDurataDebuff(),
                        abilitate.getDamageDebuff());
                System.out.println("🔥 Debuff aplicat: " + abilitate.getDebuffAplicat());
            }

        } else {
            System.out.println("✅ Abilitate de buff/heal folosită cu succes!");
        }

        // 🆕 RESOURCE GENERATION (after successful use)
        if (abilitate.getResourceGenerated() > 0) {
            erou.regenResursa(abilitate.getResourceGenerated());
            System.out.println("⚡ Generat " + abilitate.getResourceGenerated() + " " + erou.getTipResursa() + "!");
            pause(DELAY_SHORT);
        }

        // Reduce cooldowns
        for (Abilitate ab : abilitati) {
            if (ab != abilitate) {
                ab.reduceCooldown();
            }
        }

        pause(DELAY_MEDIUM);
        return true;
    }

    // 🎨 SECȚIUNEA 4: UI HELPERS

    private void displayBattleStart(Erou erou, Inamic inamic) {
        System.out.println("\n" + "═".repeat(60));
        System.out.println("        ⚔️ ÎNCEPE LUPTA! ⚔️");
        System.out.println("═".repeat(60));

        printProgressive("\n🛡️ EROUL:");
        printProgressive(String.format(" 👤 %s (Nivel %d)", erou.getNume(), erou.getNivel()));
        printProgressive(String.format(" ❤️ %d/%d HP 🔋 %d/%d %s",
                erou.getViata(), erou.getViataMaxima(),
                erou.getResursaCurenta(), erou.getResursaMaxima(), erou.getTipResursa()));

        printProgressive("\n👹 INAMICUL:");
        printProgressive(String.format("%s (Nivel %d)%s",
                inamic.getNume(), inamic.getNivel(),
                inamic.isBoss() ? " 💀 BOSS" : ""));
        printProgressive(String.format(" ❤️ %d/%d HP 🛡️ %d Defense",
                inamic.getViata(), inamic.getViataMaxima(), inamic.getDefenseTotal()));

        System.out.println("\n" + "═".repeat(60));
    }

    private void displayTurnHeader(int turn) {
        System.out.println("┌" + "─".repeat(56) + "┐");
        System.out.printf(" │ 🎯 TURA %-48s│%n", turn);
        System.out.println(" └" + "─".repeat(56) + "┘");
    }

    private void displayCurrentStatus(Erou erou, Inamic inamic) {
        System.out.println("\n📊 STATUS CURENT:");

        // Hero status
        double heroHP = (double)erou.getViata() / erou.getViataMaxima() * 100;
        System.out.printf("🛡️ %s: %s %d/%d HP\n",
                erou.getNume(), createHealthBar(heroHP),
                erou.getViata(), erou.getViataMaxima());

        // ✨ ADAUGĂ RESOURCE BAR
        double heroResource = (double)erou.getResursaCurenta() / erou.getResursaMaxima() * 100;
        String resourceIcon = getResourceIcon(erou.getTipResursa());
        System.out.printf("   %s: %s %d/%d %s\n",
                resourceIcon, createResourceBar(heroResource),
                erou.getResursaCurenta(), erou.getResursaMaxima(),
                erou.getTipResursa());

        // Weapon enchants
        ObiectEchipament weapon = erou.getEchipat().get("Weapon");
        if (weapon != null && !weapon.getAllEnchantments().isEmpty()) {
            System.out.print("   ✨ Enchants: ");
            weapon.getAllEnchantments().forEach((type, dmg) -> {
                System.out.printf("%s%s(+%d) ", getEnchantIcon(type), type.toUpperCase(), dmg);
            });
            System.out.println();
        }

        // Active buffs
        if (!erou.getBuffuriActive().isEmpty()) {
            System.out.print("   💪 Buffs: ");
            erou.getBuffuriActive().forEach((name, buff) -> {
                if (buff.isActive()) {
                    System.out.print(name + "(" + buff.getDurata() + ") ");
                }
            });
            System.out.println();
        }

        System.out.println();

        // Enemy status
        double enemyHP = (double)inamic.getViata() / inamic.getViataMaxima() * 100;
        System.out.printf("👹 %s: %s %d/%d HP\n",
                inamic.getNume(), createHealthBar(enemyHP),
                inamic.getViata(), inamic.getViataMaxima());

        // Active debuffs
        if (!inamic.getDebuffuriActive().isEmpty()) {
            System.out.print("   🔥 Debuffs: ");
            inamic.getDebuffuriActive().forEach((name, durata) -> {
                System.out.print(name + "(" + durata + ") ");
            });
            System.out.println();
        }

        System.out.println("\n" + "─".repeat(50));
    }

    private String createHealthBar(double percent) {
        int segments = 10;
        int filled = (int)(percent / 100 * segments);
        StringBuilder bar = new StringBuilder("[");

        for (int i = 0; i < segments; i++) {
            if (i < filled) {
                bar.append("█");
            } else {
                bar.append("░");
            }
        }
        bar.append("]");

        if (percent > 60) return "🟢 " + bar;
        else if (percent > 30) return "🟡 " + bar;
        else return "🔴 " + bar;
    }

    /**
     * Creează bara vizuală pentru resurse (folosește același format ca health bar).
     */
    private String createResourceBar(double percentage) {
        int barLength = 20;
        int filledBlocks = (int) (percentage / 100.0 * barLength);

        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < barLength; i++) {
            if (i < filledBlocks) {
                bar.append("█");
            } else {
                bar.append("░");
            }
        }
        bar.append("]");

        return bar.toString();
    }

    private String getResourceIcon(String tipResursa) {
        return switch (tipResursa.toLowerCase()) {
            case "rage", "furie" -> "💢";
            case "energy", "energie" -> "⚡";
            case "mana", "mană" -> "💙";
            default -> "💙";
        };
    }

    /**
     * 👹 TURA INAMICULUI - Îmbunătățită
     */
    private void executeTurnInamic(Erou erou, Inamic inamic) {
        System.out.println("\n" + "═".repeat(50));
        System.out.println("👹 TURA INAMICULUI");
        System.out.println("═".repeat(50));

        if (!inamic.esteViu()) {
            return;
        }

        pauseWithLoading("⚙️ Inamicul se pregătește", DELAY_SHORT);
        EnemyOneLiners.displayEnemyAttackLine(inamic);

        // ==================== CHECK STUN ====================
        if (inamic.esteStunned()) {
            System.out.println("\n⚡ " + inamic.getNume() + " este STUNNED!");
            System.out.println("💫 Nu poate acționa în acest turn!");
            pause(DELAY_MEDIUM);
            return;
        }

        // ==================== CHECK DODGE FIRST ====================
        double dodgeChance = erou.getDodgeChanceTotal();
        System.out.printf("\n🎯 Șansă evitare: %.1f%%\n", dodgeChance);

        pauseWithLoading("🎲 Verificare dodge", DELAY_MEDIUM);

        if (RandomUtils.chancePercent(dodgeChance)) {
            System.out.println("✅ EVITAT!");
            pause(DELAY_SHORT);
            BattleOneLiners.displayDodgeOneLiner(erou);
            System.out.println("💨 " + erou.getNume() + " evită atacul complet!");
            pause(DELAY_MEDIUM);
            return;
        }

        System.out.println("❌ NU A EVITAT!");
        pause(DELAY_SHORT);

        // ==================== DAMAGE CALCULATION ====================
        System.out.println("\n👹 " + inamic.getNume() + " atacă!");
        pauseWithLoading("⚙️ Calculare damage", DELAY_SHORT);

        // Base damage cu variație
        int baseDamage = inamic.getDamage();
        baseDamage = RandomUtils.applyRandomVariation(baseDamage, 20);
        int damageBeforeCrit = baseDamage;

        // ==================== CHECK CRIT ====================
        pauseWithLoading("🎲 Verificare critical", DELAY_SHORT);

        boolean isCrit = RandomUtils.chancePercent(inamic.getCritChance());
        double critMultiplier = GameConstants.CRIT_DAMAGE_MULTIPLIER;

        if (isCrit) {
            baseDamage = (int)(baseDamage * critMultiplier);
            System.out.println("\n💥 ═══════════════════════════════════════");
            System.out.println("       ⚡ ENEMY CRITICAL HIT! ⚡");
            System.out.println("   ═══════════════════════════════════════");
            pause(DELAY_MEDIUM);
        }

        // ==================== APPLY DEFENSE ====================
        int heroDefense = erou.getDefense();
        int damageReduced = Math.min(baseDamage - 1, heroDefense);
        int finalDamage = Math.max(1, baseDamage - heroDefense);

        double reductionPercent = baseDamage > 0 ? (double) damageReduced / baseDamage * 100 : 0;

        // ==================== FANCY DAMAGE BREAKDOWN ====================
        pause(DELAY_SHORT);

        System.out.println("\n╔═════════════ 💥 DAMAGE BREAKDOWN ═════════════╗");
        System.out.println("║                                                ║");
        System.out.printf("║  ⚔️  Base Attack: %d%n", inamic.getDamage());
        System.out.printf("║  🎲 Variation: %d (±20%%)%n", damageBeforeCrit);

        if (isCrit) {
            System.out.printf("║  💥 CRITICAL: %d (x%.1f)%n", baseDamage, critMultiplier);
        }

        System.out.println("║                                                ║");
        System.out.println("╟────────────────────────────────────────────────╢");
        System.out.printf("║  🛡️  Your Defense: %d%n", heroDefense);
        System.out.printf("║  📉 Damage Reduced: -%d (%.1f%% reduction)%n",
                damageReduced, reductionPercent);
        System.out.println("║                                                ║");
        System.out.println("╟════════════════════════════════════════════════╢");
        System.out.printf("║  💢 FINAL DAMAGE: %d%n", finalDamage);
        System.out.println("║                                                ║");
        System.out.println("╚════════════════════════════════════════════════╝");

        pauseWithLoading("⚡ Aplicare damage", DELAY_MEDIUM);

        // ==================== APPLY DAMAGE ====================
        erou.iaDamage(finalDamage);

        System.out.printf("\n💥 %s primește %d damage!\n", erou.getNume(), finalDamage);

        // Health bar actualizat
        double heroHP = (double)erou.getViata() / erou.getViataMaxima() * 100;
        System.out.printf("❤️  %s: %s %d/%d HP\n",
                erou.getNume(), createHealthBar(heroHP),
                erou.getViata(), erou.getViataMaxima());

        // ==================== SPECIAL ABILITIES ====================
        if (RandomUtils.chancePercent(25.0) && !inamic.getAbilitatiSpeciale().isEmpty()) {
            pause(DELAY_MEDIUM);
            System.out.println("\n⚡ " + inamic.getNume() + " folosește o abilitate specială!");
            pause(DELAY_SHORT);

            // Aici poți adăuga efecte speciale pentru inamic
            String specialAbility = RandomUtils.randomElement(inamic.getAbilitatiSpeciale());
            System.out.println("🌟 Abilitate: " + specialAbility);
        }

        pause(DELAY_MEDIUM);
    }
    /**
     * 🔄 PROCESARE EFECTE END OF TURN
     */
    private void processTurnEffects(Erou erou, Inamic inamic) {
        System.out.println("\n" + "═".repeat(50));
        System.out.println("🔄 PROCESARE EFECTE END OF TURN");
        System.out.println("═".repeat(50));

        pauseWithLoading("⚙️ Aplicare efecte", DELAY_SHORT);

        // Efecte erou
        System.out.println("\n🛡️ Efecte pe " + erou.getNume() + ":");
        erou.actualizeazaStari();
        pause(DELAY_SHORT);

        // Efecte inamic
        if (inamic.esteViu()) {
            System.out.println("\n👹 Efecte pe " + inamic.getNume() + ":");
            inamic.actualizeazaStari();
            pause(DELAY_SHORT);
        }

        System.out.println("\n✅ Efecte procesate!");
    }

    /**
     * 🏆 REZULTAT LUPTĂ
     */
    private boolean processBattleResult(Erou erou, Inamic inamic) {
        pause(DELAY_LONG);
        System.out.println("\n" + "═".repeat(60));

        if (erou.esteViu()) {
            System.out.println("        🎉 VICTORIE! 🎉");
            System.out.println("═".repeat(60));
            System.out.println("\n✨ " + erou.getNume() + " a câștigat lupta!");
            return true;
        } else {
            System.out.println("        💀 ÎNFRÂNGERE! 💀");
            System.out.println("═".repeat(60));
            System.out.println("\n⚰️ " + erou.getNume() + " a fost învins...");
            return false;
        }
    }

    // 🔧 METODE HELPER

    private Map<String, Integer> createStatsMap(Erou erou) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("strength", erou.getStrengthTotal());
        stats.put("dexterity", erou.getDexterityTotal());
        stats.put("intelligence", erou.getIntelligenceTotal());
        return stats;
    }

    private boolean executeUsePotionExtended(Erou erou, Scanner scanner) {
        System.out.println("\n🧪 CE POȚIUNE VREI SĂ FOLOSEȘTI?");
        System.out.println();

        // Health și Mana potions
        System.out.println("═══ POȚIUNI NORMALE ═══");
        System.out.println("1. ❤️ Berice (" + erou.getHealthPotions() + " disponibile)");
        System.out.printf("   • Restabilește %d HP\n", erou.getHealthPotionHealing());

        System.out.println("2. 💙 Energizant Profi " + erou.getTipResursa() + " (" + erou.getManaPotions() + " disponibile)");
        System.out.printf("   • Restabilește %d %s\n", erou.getManaPotionRestore(), erou.getTipResursa());

        // Buff potions
        System.out.println("\n═══ BUFF POTIONS ═══");
        Map<BuffPotion.BuffType, Integer> buffPotions = erou.getAllBuffPotions();

        if (buffPotions.isEmpty()) {
            System.out.println("❌ Nu ai Buff Potions! Cumpără de la shop!");
        } else {
            int optionNumber = 3;
            Map<Integer, BuffPotion.BuffType> optionMap = new HashMap<>();

            for (Map.Entry<BuffPotion.BuffType, Integer> entry : buffPotions.entrySet()) {
                BuffPotion.BuffType type = entry.getKey();
                int quantity = entry.getValue();

                if (quantity > 0) {
                    optionMap.put(optionNumber, type);
                    System.out.printf("%d. %s %s (%d disponibile)\n",
                            optionNumber, type.getIcon(), type.getDisplayName(), quantity);
                    System.out.printf("   • %s\n", type.getDescription());
                    optionNumber++;
                }
            }

            System.out.println("\n" + optionNumber + ". ❌ Anulează");

            int maxChoice = optionNumber;
            System.out.print("\n➤ Alege opțiunea (1-" + maxChoice + "): ");
            int choice = Validator.readValidChoice(scanner, 1, maxChoice);

            // Health potion
            if (choice == 1) {
                if (erou.useHealthPotion()) {
                    System.out.println("✅ Poțiunea de viață a fost folosită!");
                    pause(DELAY_SHORT);
                    return true;
                } else {
                    System.out.println("❌ Nu ai poțiuni sau ești deja la viață maximă!");
                    pause(DELAY_SHORT);
                    return false;
                }
            }
            // Mana potion
            else if (choice == 2) {
                if (erou.useManaPotion()) {
                    System.out.println("✅ Poțiunea de " + erou.getTipResursa() + " a fost folosită!");
                    pause(DELAY_SHORT);
                    return true;
                } else {
                    System.out.println("❌ Nu ai poțiuni sau ești deja la maxim!");
                    pause(DELAY_SHORT);
                    return false;
                }
            }
            // Buff potions
            else if (choice < maxChoice) {
                BuffPotion.BuffType selectedType = optionMap.get(choice);
                if (selectedType != null) {
                    if (erou.useBuffPotion(selectedType)) {
                        System.out.println("✅ " + selectedType.getDisplayName() + " a fost folosită!");
                        System.out.println("✨ Buff-ul va dura pentru restul acestei lupte!");
                        pause(DELAY_SHORT);
                        return true;
                    } else {
                        System.out.println("❌ Nu ai această poțiune!");
                        pause(DELAY_SHORT);
                        return false;
                    }
                }
            }
            // Cancel
            else {
                System.out.println("🔙 Acțiune anulată.");
                pause(DELAY_SHORT);
                return false;
            }
        }

        // Dacă nu are buff potions, oferă doar opțiuni 1, 2, 3 (cancel)
        if (buffPotions.isEmpty()) {
            System.out.println("\n3. ❌ Anulează");
            System.out.print("\n➤ Alege opțiunea (1-3): ");
            int choice = Validator.readValidChoice(scanner, 1, 3);

            switch (choice) {
                case 1 -> {
                    if (erou.useHealthPotion()) {
                        System.out.println("✅ Poțiunea de viață a fost folosită!");
                        pause(DELAY_SHORT);
                        return true;
                    } else {
                        System.out.println("❌ Nu ai poțiuni sau ești deja la viață maximă!");
                        pause(DELAY_SHORT);
                        return false;
                    }
                }
                case 2 -> {
                    if (erou.useManaPotion()) {
                        System.out.println("✅ Poțiunea de " + erou.getTipResursa() + " a fost folosită!");
                        pause(DELAY_SHORT);
                        return true;
                    } else {
                        System.out.println("❌ Nu ai poțiuni sau ești deja la maxim!");
                        pause(DELAY_SHORT);
                        return false;
                    }
                }
                case 3 -> {
                    System.out.println("🔙 Acțiune anulată.");
                    pause(DELAY_SHORT);
                    return false;
                }
            }
        }

        return false;
    }

    private void executeInspectEnemy(Erou erou, Inamic inamic) {
        if (!inamic.esteInspectat()) {
            inamic.setInspectat(true);

            pauseWithLoading("🔍 Inspectare inamic", DELAY_MEDIUM);

            List<String> info = new java.util.ArrayList<>();
            info.add(inamic.getNume() + " (Nivel " + inamic.getNivel() + ")");
            info.add("");
            info.add("❤️ Viață: " + inamic.getViata() + "/" + inamic.getViataMaxima());
            info.add("🛡️ Defense : " + inamic.getDefenseTotal());
            info.add("⚔️ Attack: " + inamic.getDamage());
            info.add("💥 Crit Chance: " + String.format("%.1f%%", inamic.getCritChance()));

            if (inamic.getTipDamageVulnerabil() != null) {
                info.add("");
                info.add("💥 VULNERABIL la: " + inamic.getTipDamageVulnerabil() + " (+50% dmg)");
            }

            if (inamic.getTipDamageRezistent() != null) {
                info.add("🛡️ REZISTENT la: " + inamic.getTipDamageRezistent() + " (-50% dmg) ");
            }

            info.add("");
            info.add("✨ BONUS OBȚINUT");
            info.add("🎯 +15% Hit Chance");
            info.add("💥 +15% Crit Chance");

            printBox("🔍 INSPECȚIE REUȘITĂ", info);

        } else {
            System.out.println("🔍 " + inamic.getNume() + " este deja inspectat!");
        }

        pause(DELAY_MEDIUM);
    }

    private void displayFleeMessage(String heroName) {
        System.out.println("\n🏃 " + heroName + " fuge din luptă!");
        System.out.println("💨 Uneori discreția este mai bună decât valoarea...");
        pause(DELAY_MEDIUM);
    }

    private void waitForInput(Scanner scanner, String message) {
        System.out.println("\n" + message);
        scanner.nextLine();
    }

    public void resetAbilityCooldowns(Erou erou) {
        for (Abilitate abilitate : erou.getAbilitati()) {
            abilitate.setCooldownRamasa(0);
        }
        lastAbilityUsed = null;  // 🆕 Reset combo tracking
    }
}