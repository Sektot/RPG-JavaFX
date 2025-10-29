package com.rpg.model.characters.classes;

import com.rpg.model.abilities.Abilitate;
import com.rpg.utils.GameConstants;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

/**
 * Clasa Oltean (fost Rogue) - Hoț viclean și rapid din Oltenia.
 * Specializat în atacuri rapide, stealth și damage mare pe critice.
 */
public class Oltean extends com.rpg.model.characters.Erou implements Serializable {
    private static final long serialVersionUID = 1L;

    // Stări speciale oltenești
    private boolean ascuns = false;
    private int turiAscuns = 0;
    private boolean cuțitOtrăvit = false;
   // private int stackuriOtravă = 0;
    private boolean umbretPrinUmbră = false;

    /**
     * Constructor pentru Oltean.
     */
    public Oltean(String nume) {
        super(nume, GameConstants.ROGUE_BASE_STRENGTH,
                GameConstants.ROGUE_BASE_DEXTERITY, GameConstants.ROGUE_BASE_INTELLIGENCE);

        this.resursaMaxima = GameConstants.ROGUE_ENERGY_MAX;
        this.resursaCurenta = resursaMaxima;
        this.setTipResursa("Energie");

        this.setViataCurenta(GameConstants.ROGUE_BASE_HEALTH);
        initializeazaAbilitati();

        System.out.println("\n🗡️ \"Bine ai venit, " + nume + " din Oltenia!\"");
        System.out.println("⚡ \"Cu rapiditate și viclenie o să-i înșelăm pe toți!\"");
    }

    @Override
    public void initializeazaAbilitati() {
        abilitati.clear();

        // ==================== BASIC ABILITIES (Level 1) ====================

        // 1. 🗡️ Lovitură Rapidă - Basic combo builder (generates energy)
        Abilitate rapid = new Abilitate("Lovitură Rapidă", 12, Arrays.asList("physical"), 15, 0, 92,
                Map.of("dexterity", 1.2), null, 0, 0);
        rapid.setAbilityType(AbilityType.OFFENSIVE)
                .setRequiredLevel(1)
                .setResourceGenerated(20);  // Generates 20 Energy
        abilitati.add(rapid);

        // 2. 🔪 Înjunghiere - Backstab with high crit chance
        Abilitate injung = new Abilitate("Înjunghiere", 22, Arrays.asList("physical"), 25, 1, 88,
                Map.of("dexterity", 1.7), null, 0, 0);
        injung.setAbilityType(AbilityType.OFFENSIVE)
                .setRequiredLevel(1);
        abilitati.add(injung);

        // 3. 👤 Ascuns în Umbră - Stealth mode (dodge + crit boost)
        Abilitate ascuns = new Abilitate("Ascuns în Umbră", 0, Arrays.asList("special"), 30, 3, 100,
                Map.of("dexterity", 0.3), null, 0, 0);
        ascuns.setAbilityType(AbilityType.BUFF)
                .setRequiredLevel(1)
                .setBuff("Ascuns", 3, Map.of("dodge_chance", 1.6, "crit_chance", 1.5));
        abilitati.add(ascuns);
    }

    @Override
    public Abilitate abilitateSpecialaNivel(int nivel) {
        return switch (nivel) {
            case 3 -> {
                // 🩸 Sângerare - Bleed damage finisher
                Abilitate sang = new Abilitate("Sângerare", 28, Arrays.asList("physical"), 30, 2, 90,
                        Map.of("dexterity", 1.8), "Bleed", 3, 6);
                sang.setAbilityType(AbilityType.OFFENSIVE)
                        .setRequiredLevel(3);
                yield sang;
            }

            case 5 -> {
                // ☠️ Lamă Otrăvită - Poison application
                Abilitate otrava = new Abilitate("Lamă Otrăvită", 18, Arrays.asList("physical", "poison"), 25, 2, 88,
                        Map.of("dexterity", 1.4), "Poison", 4, 5);
                otrava.setAbilityType(AbilityType.OFFENSIVE)
                        .setRequiredLevel(5)
                        .setResourceGenerated(15);  // Generates 15 Energy
                yield otrava;
            }

            case 8 -> {
                // ⚡ Viteză Mortală - Attack speed burst
                Abilitate viteza = new Abilitate("Viteză Mortală", 0, Arrays.asList("special"), 40, 4, 100,
                        Map.of("dexterity", 0.5), null, 0, 0);
                viteza.setAbilityType(AbilityType.BUFF)
                        .setRequiredLevel(8)
                        .setBuff("VitezaMortala", 3, Map.of("dexterity", 1.4, "attack_speed", 1.6, "crit_chance", 1.3));
                yield viteza;
            }

            case 10 -> {
                // 🗡️🗡️ Lovitură Dublă - Two-hit combo
                Abilitate dubla = new Abilitate("Lovitură Dublă", 20, Arrays.asList("physical"), 35, 2, 87,
                        Map.of("dexterity", 1.5), null, 0, 0);
                dubla.setAbilityType(AbilityType.OFFENSIVE)
                        .setRequiredLevel(10)
                        .setNumberOfHits(2);  // Hits twice
                yield dubla;
            }

            case 15 -> {
                // 🌪️ Vârtej de Lame - Blade flurry AOE
                Abilitate vartej = new Abilitate("Vârtej de Lame", 25, Arrays.asList("physical"), 50, 3, 85,
                        Map.of("dexterity", 1.6), null, 0, 0);
                vartej.setAbilityType(AbilityType.OFFENSIVE)
                        .setRequiredLevel(15)
                        .setAOE(true)
                        .setNumberOfHits(4);  // Hits 4 times
                yield vartej;
            }

            case 20 -> {
                // 💨 Dispariție - Vanish (massive dodge + heal)
                Abilitate dispari = new Abilitate("Dispariție", 0, Arrays.asList("special"), 45, 5, 100,
                        Map.of("dexterity", 0.4), null, 0, 0);
                dispari.setAbilityType(AbilityType.BUFF)
                        .setRequiredLevel(20)
                        .setHealPercent(0.20)  // Heals 20% max HP
                        .setBuff("Disparitie", 3, Map.of("dodge_chance", 2.5, "stealth_bonus", 1.8));
                yield dispari;
            }

            case 25 -> {
                // 💀 Asasinare - Combo finisher (massive damage after stealth)
                Abilitate asasinat = new Abilitate("Asasinare", 70, Arrays.asList("physical"), 60, 3, 92,
                        Map.of("dexterity", 3.0), "DeepWound", 4, 10);
                asasinat.setAbilityType(AbilityType.OFFENSIVE)
                        .setRequiredLevel(25)
                        .setComboRequirement("Ascuns în Umbră")  // Must use stealth first
                        .setComboBonusDamage(0.75);  // +75% damage if combo'd from stealth!
                yield asasinat;
            }

            case 30 -> {
                // 👥 ULTIMATE: Dans al Umbrelor - Shadow dance ultimate
                Abilitate ult = new Abilitate("Dans al Umbrelor", 45, Arrays.asList("physical", "shadow", "legendary"), 100, 10, 95,
                        Map.of("dexterity", 2.5), "Bleed", 5, 15);
                ult.setAbilityType(AbilityType.OFFENSIVE)
                        .setRequiredLevel(30)
                        .setUltimate(true)
                        .setNumberOfHits(8)  // Hits 8 times!
                        .setHealPercent(0.30)  // Heals 30% max HP (lifesteal theme)
                        .setBuff("DansUmbrelor", 4, Map.of("dexterity", 1.8, "crit_chance", 1.7, "dodge_chance", 1.6));
                yield ult;
            }

            default -> null;
        };
    }


    @Override
    public int regenNormal() {
        int baseRegen = 12;
        int dexBonus = getDexterityTotal() / 4;

        if (ascuns) {
            baseRegen = (int)(baseRegen * 1.3);
        }

        return baseRegen + dexBonus;
    }

    @Override
    public void iaDamage(int damage) {
        if (ascuns && turiAscuns > 0) {
            double evitare = 70.0;
            if (Math.random() * 100 < evitare) {
                System.out.println("💨 " + getNume() + " evită damage-ul fiind ascuns!");
              //  System.out.println(getRandomLine(STEALTH_LINES));
                return;
            } else {
                //dezactiveazaAscuns();
                System.out.println("🔍 " + getNume() + " a fost descoperit!");
            }
        }

        super.iaDamage(damage);

        // Activează umbre când e la viață scăzută
        if (getViata() <= getViataMaxima() * 0.4 && !umbretPrinUmbră) {
            activeazaUmbrePrinUmbra();
        }
    }

    @Override
    public double getCritChanceTotal() {
        double baseCrit = super.getCritChanceTotal();
        double dexCrit = getDexterityTotal() * 0.6;

        if (ascuns) {
            dexCrit += 35.0;
        }

        if (cuțitOtrăvit) {
            dexCrit += 10.0;
        }

        return Math.min(baseCrit + dexCrit, 75.0);
    }

    @Override
    public double getDodgeChanceTotal() {
        double baseDodge = super.getDodgeChanceTotal();
        double dexDodge = getDexterityTotal() * 1.2;

        if (ascuns) {
            dexDodge += 30.0;
        }

        return Math.min(baseDodge + dexDodge, 85.0);
    }

    public void activeazaUmbrePrinUmbra() {
        this.umbretPrinUmbră = true;
        System.out.println("\n🌙 " + getNume() + " se topește în umbre!");
        System.out.println("💨 \"Bag noroc, că dispar!\"");
    }


    /**
     * Activează modul ascuns.
     */
    public void activeazaAscuns(int durată) {
        this.ascuns = true;
        this.turiAscuns = durată;
        System.out.println("\n👻 " + getNume() + " se ascunde în umbre!");
        //System.out.println(getRandomLine(STEALTH_LINES));
    }

    /**
     * Dezactivează modul ascuns.
     */
    public void dezactiveazaAscuns() {
        if (ascuns) {
            this.ascuns = false;
            this.turiAscuns = 0;
            System.out.println("🔍 " + getNume() + " iese din umbră!");
        }
    }

//    /**
//     * Activează cuțitul otrăvit.
//     */
//    public void activeazaCuțitOtrăvit(int stackuri) {
//        this.cuțitOtrăvit = true;
//        this.stackuriOtravă = stackuri;
//        System.out.println("\n☠️ " + getNume() + " își otrăvește cuțitele!");
//        //System.out.println(getRandomLine(POISON_LINES));
//    }



//    // Getteri pentru stările speciale
//    public boolean esteAscuns() { return ascuns; }
//    public boolean areCuțitOtrăvit() { return cuțitOtrăvit; }
//    public int getStackuriOtravă() { return stackuriOtravă; }
}