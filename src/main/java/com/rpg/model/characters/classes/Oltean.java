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

        // 1.quick strike
        Abilitate rapid = new Abilitate("Lovitură Rapidă", 15, Arrays.asList("physical"), 10, 0, 90,
                Map.of("dexterity", 1.3), null, 0, 0);
        abilitati.add(rapid);

        // 2.backstab
        Abilitate spate = new Abilitate("Înjunghiere pe la Spate", 22, Arrays.asList("physical"), 18, 1, 88,
                Map.of("dexterity", 1.8), null, 0, 0);
        abilitati.add(spate);

        // 3. stealth
        Abilitate ascuns = new Abilitate("Ascuns în Umbră", 0, Arrays.asList("special"), 25, 3, 100,
                Map.of("dexterity", 0.3), null, 0, 0);
        ascuns.setBuff("Ascuns", 3, Map.of("dodge_chance", 1.5, "crit_chance", 1.4));
        abilitati.add(ascuns);
    }

    @Override
    public Abilitate abilitateSpecialaNivel(int nivel) {
        return switch (nivel) {
            case 5 -> {
                //poison blade
                Abilitate otrava = new Abilitate("Cuțit Otrăvit", 12, Arrays.asList("physical"), 20, 2, 85,
                        Map.of("dexterity", 1.2), "Poison", 3, 4);
                yield otrava;
            }

            case 10 -> {
                //  shadow step
                Abilitate umbre = new Abilitate("Umbre prin Noapte", 8, Arrays.asList("special"), 30, 4, 100,
                        Map.of("dexterity", 0.8), null, 0, 0);
                umbre.setBuff("UmbrePrinNoapte", 2, Map.of("dodge_chance", 1.8, "movement_speed", 1.5));
                yield umbre;
            }

            case 15 -> {
                //  dual strike
                Abilitate dubla = new Abilitate("Dublă Lovitură", 18, Arrays.asList("physical"), 35, 2, 82,
                        Map.of("dexterity", 1.6), null, 0, 0);
                yield dubla;
            }

            case 20 -> {
                //smoke bomb
                Abilitate fum = new Abilitate("Bomba de Fum", 0, Arrays.asList("special"), 40, 5, 100,
                        Map.of("dexterity", 0.4), "Blind", 2, 0);
                fum.setBuff("BombaDeFum", 4, Map.of("dodge_chance", 2.0, "stealth_bonus", 1.3));
                yield fum;
            }

            case 25 -> {
                //   assassinate
                Abilitate asasinat = new Abilitate("Cutit la Jugulara", 60, Arrays.asList("physical"), 50, 3, 75,
                        Map.of("dexterity", 2.8), "DeepWound", 4, 8);
                yield asasinat;
            }

            case 30 -> {
                // shadow clone
                Abilitate clona = new Abilitate("Clona de Umbră", 25, Arrays.asList("special"), 60, 8, 85,
                        Map.of("dexterity", 1.5), null, 0, 0);
                clona.setBuff("ClonaDUmbra", 5, Map.of("attack_speed", 2.0, "crit_chance", 1.6));
                yield clona;
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