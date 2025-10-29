package com.rpg.model.characters.classes;

import com.rpg.model.abilities.Abilitate;
import com.rpg.model.characters.Erou;
import com.rpg.utils.GameConstants;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

/**
 * Clasa Moldovean (fost Warrior) - Viteaz puternic din Moldova.
 * Specializat în atacuri fizice devastatoare și rezistență legendară.
 */
public class Moldovean extends Erou implements Serializable {
    private static final long serialVersionUID = 1L;

    // Stări speciale moldovenești
    private boolean modFurios = false;
    private int turiFurie = 0;
    private boolean scutDeTari = false;
    private int damageAbsorbit = 0;


    /**
     * Constructor pentru Moldovean.
     */
    public Moldovean(String nume) {
        super(nume, GameConstants.WARRIOR_BASE_STRENGTH,
                GameConstants.WARRIOR_BASE_DEXTERITY, GameConstants.WARRIOR_BASE_INTELLIGENCE);

        this.resursaMaxima = GameConstants.WARRIOR_RAGE_MAX;
        this.resursaCurenta = 0;
        this.setTipResursa("Furie");

        this.setViataCurenta(GameConstants.WARRIOR_BASE_HEALTH);
        initializeazaAbilitati();

        System.out.println("\n🇷🇴 \"Bine ai venit, " + nume + " din Moldova!\"");
        System.out.println("💪 \"Cu sarmale și pălincă o să-i băgăm pe toți în pământ!\"");
    }

    @Override
    public void initializeazaAbilitati() {
        abilitati.clear();

        // ==================== BASIC ABILITIES (Level 1) ====================

        // 1. 🗡️ Lovitură Furioasă - Rage generator
        Abilitate lovitura = new Abilitate("Lovitură Furioasă", 15, Arrays.asList("physical"), 0, 0, 90,
                Map.of("strength", 1.3), null, 0, 0);
        lovitura.setAbilityType(AbilityType.OFFENSIVE)
                .setRequiredLevel(1)
                .setResourceGenerated(15);  // Generates 15 Rage on hit
        abilitati.add(lovitura);

        // 2. 🛡️ Scut de Țară - Defensive stance with Rage cost
        Abilitate scutTara = new Abilitate("Scut de Țară", 0, Arrays.asList("defensive"), 20, 3, 100,
                Map.of("strength", 0.3), null, 0, 0);
        scutTara.setAbilityType(AbilityType.BUFF)
                .setRequiredLevel(1)
                .setBuff("ScutDeTara", 3, Map.of("defense", 1.4, "damage_reduction", 1.25));
        abilitati.add(scutTara);

        // 3. ⚔️ Tăietură Sălbatică - Moderate damage, generates Rage
        Abilitate taietura = new Abilitate("Tăietură Sălbatică", 25, Arrays.asList("physical"), 0, 1, 85,
                Map.of("strength", 1.6), null, 0, 0);
        taietura.setAbilityType(AbilityType.OFFENSIVE)
                .setRequiredLevel(1)
                .setResourceGenerated(20);  // Generates 20 Rage
        abilitati.add(taietura);
    }

    @Override
    public Abilitate abilitateSpecialaNivel(int nivel) {
        return switch (nivel) {
            case 3 -> {
                // 💥 Lovitură Devastatoare - High damage Rage spender
                Abilitate lovit = new Abilitate("Lovitură Devastatoare", 40, Arrays.asList("physical"), 30, 2, 88,
                        Map.of("strength", 2.0), null, 0, 0);
                lovit.setAbilityType(AbilityType.OFFENSIVE)
                        .setRequiredLevel(3);
                yield lovit;
            }

            case 5 -> {
                // 🩸 Furia Sângelui - Berserker mode (costs HP, massive damage boost)
                Abilitate furie = new Abilitate("Furia Sângelui", 0, Arrays.asList("special"), 40, 5, 100,
                        Map.of("strength", 0.5), null, 0, 0);
                furie.setAbilityType(AbilityType.BUFF)
                        .setRequiredLevel(5)
                        .setSelfDamage(20)  // Costs 20 HP to activate
                        .setBuff("FuriaSangelui", 4, Map.of("strength", 1.5, "crit_chance", 1.4, "lifesteal", 1.15));
                yield furie;
            }

            case 8 -> {
                // ⚡ Șarjă Furioasă - Charge attack that generates Rage
                Abilitate sarja = new Abilitate("Șarjă Furioasă", 35, Arrays.asList("physical"), 25, 3, 90,
                        Map.of("strength", 1.8, "dexterity", 0.5), "Stun", 1, 0);
                sarja.setAbilityType(AbilityType.OFFENSIVE)
                        .setRequiredLevel(8)
                        .setResourceGenerated(25);
                yield sarja;
            }

            case 10 -> {
                // 🌪️ Vârtej de Oțel - Whirlwind AOE attack
                Abilitate vartej = new Abilitate("Vârtej de Oțel", 30, Arrays.asList("physical"), 50, 4, 85,
                        Map.of("strength", 1.6), null, 0, 0);
                vartej.setAbilityType(AbilityType.OFFENSIVE)
                        .setRequiredLevel(10)
                        .setAOE(true)
                        .setNumberOfHits(3);  // Hits 3 times
                yield vartej;
            }

            case 15 -> {
                // 🛡️ Fortăreața Carpatină - Iron defense
                Abilitate fort = new Abilitate("Fortăreața Carpatină", 0, Arrays.asList("defensive"), 60, 6, 100,
                        Map.of("strength", 0.4), null, 0, 0);
                fort.setAbilityType(AbilityType.BUFF)
                        .setRequiredLevel(15)
                        .setHealPercent(0.15)  // Heals 15% max HP
                        .setBuff("Fortareata", 5, Map.of("defense", 1.8, "damage_reduction", 1.4, "block_chance", 1.25));
                yield fort;
            }

            case 20 -> {
                // 💀 Execuția - Execute low HP enemies
                Abilitate exec = new Abilitate("Execuția", 60, Arrays.asList("physical"), 50, 4, 92,
                        Map.of("strength", 2.5), null, 0, 0);
                exec.setAbilityType(AbilityType.OFFENSIVE)
                        .setRequiredLevel(20);
                // TODO: Add special logic for bonus damage on low HP enemies
                yield exec;
            }

            case 25 -> {
                // 🔥 Furie Primordială - Combo finisher
                Abilitate furie25 = new Abilitate("Furie Primordială", 70, Arrays.asList("physical"), 70, 5, 90,
                        Map.of("strength", 2.8), null, 0, 0);
                furie25.setAbilityType(AbilityType.OFFENSIVE)
                        .setRequiredLevel(25)
                        .setComboRequirement("Șarjă Furioasă")  // Must use Șarjă first
                        .setComboBonusDamage(0.5);  // +50% damage if combo'd
                yield furie25;
            }

            case 30 -> {
                // ⚔️ ULTIMATE: Spiritul Dacilor - Legendary ultimate
                Abilitate ult = new Abilitate("Spiritul Dacilor", 100, Arrays.asList("physical", "legendary"), 100, 10, 95,
                        Map.of("strength", 3.5), "Stun", 2, 10);
                ult.setAbilityType(AbilityType.OFFENSIVE)
                        .setRequiredLevel(30)
                        .setUltimate(true)
                        .setNumberOfHits(5)  // Hits 5 times!
                        .setHealPercent(0.25)  // Heals 25% max HP
                        .setBuff("SpiritulDacilor", 3, Map.of("strength", 1.6, "defense", 1.4, "crit_damage", 1.5));
                yield ult;
            }

            default -> null;
        };
    }



    @Override
    public int regenNormal() {
        int baseRegen = 5;
        if (modFurios) {
            baseRegen = (int)(baseRegen * 1.5);
        }
        return baseRegen;
    }

    @Override
    public void iaDamage(int damage) {
        if (scutDeTari && damage > 0) {
            int absorbed = Math.min(damage / 2, 20);
            damage -= absorbed;
            damageAbsorbit += absorbed;
            System.out.println("🛡️ " + getNume() + " absoarbe " + absorbed + " damage cu Scutul de Țară!");
        }

        super.iaDamage(damage);

        // Generează furie când primește damage
        int originalDamage = damage + (scutDeTari ? damageAbsorbit : 0);
        int rageLaGenerat = originalDamage / GameConstants.WARRIOR_RAGE_GENERATION_DIVISOR;
        regenResursa(rageLaGenerat);
    }

    /**
     * Activează modul furios (Pălinca Fierbinte).
     */
    public void activeazaModFurios(int tururi) {
        this.modFurios = true;
        this.turiFurie = tururi;
        System.out.println("\n🔥 " + getNume() + " bea o pălincă și intră în FURIE!");
       // System.out.println(getAbilityOneLiner("Pălinca Fierbinte"));
    }

    /**
     * Activează scutul de țară.
     */
    public void activeazaScutDeTara() {
        this.scutDeTari = true;
        System.out.println("\n🛡️ " + getNume() + " ridică Scutul de Țară!");
       // System.out.println(getAbilityOneLiner("Scut de Țară"));
    }


}