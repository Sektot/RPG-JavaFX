package com.rpg.model.characters.classes;

import com.rpg.model.abilities.Abilitate;
import com.rpg.utils.GameConstants;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

/**
 * Clasa Ardelean (fost Wizard) - Vrăjitor înțelept din Ardeal.
 * Specializat în magie puternică și farmece elementale.
 */
public class Ardelean extends com.rpg.model.characters.Erou implements Serializable {
    private static final long serialVersionUID = 1L;

    // Stări speciale ardeleNești
    private boolean scutMagic = false;
    private int turiScutMagic = 0;
    private int absorbtieScutMagic = 0;
    private boolean inteligenţaArdealului = false;
    private int putereVrajă = 0;

    /**
     * Constructor pentru Ardelean.
     */
    public Ardelean(String nume) {
        super(nume, GameConstants.WIZARD_BASE_STRENGTH,
                GameConstants.WIZARD_BASE_DEXTERITY, GameConstants.WIZARD_BASE_INTELLIGENCE);

        this.resursaMaxima = GameConstants.WIZARD_MANA_MAX;
        this.resursaCurenta = resursaMaxima;
        this.setTipResursa("Mană");

        this.setViataCurenta(GameConstants.WIZARD_BASE_HEALTH);
        initializeazaAbilitati();

        System.out.println("\n🏛️ \"Bine ai venit, " + nume + " din Ardeal!\"");
        System.out.println("🔮 \"Cu mintea și cu magia o să-i băgăm pe toți în pământ, frate!\"");
    }

    @Override
    public void initializeazaAbilitati() {
        abilitati.clear();

        // 1. Săgeata Magică - atac basic magic
        Abilitate sageata = new Abilitate("Testicular Torsion", 12, Arrays.asList("magical"), 15, 0, 85,
                Map.of("intelligence", 1.4), null, 0, 0);
        abilitati.add(sageata);

        // 2. Mămăligă Fierbinte - fireball
        Abilitate mamaliga = new Abilitate("Mămăligă Fierbinte", 18, Arrays.asList("magical"), 25, 3, 75,
                Map.of("intelligence", 1.8), "Burn", 3, 4);
        abilitati.add(mamaliga);

        // 3. Scutul Corvinilor - mana shield
        Abilitate scut = new Abilitate("VPN din Brașov", 0, Arrays.asList("magical"), 30, 5, 100,
                Map.of("intelligence", 0.5), null, 0, 0);
        scut.setBuff("ScutCorvini", 4, Map.of("defense", 1.3, "magic_resistance", 1.5));
        abilitati.add(scut);
    }

    @Override
    public Abilitate abilitateSpecialaNivel(int nivel) {
        return switch (nivel) {
            case 5 -> {
                // Gheața Apusenilor - ice spike
                Abilitate gheata = new Abilitate("Geata din Frigider", 20, Arrays.asList("magical"), 35, 2, 80,
                        Map.of("intelligence", 1.6), "Freeze", 2, 0);
                yield gheata;
            }

            case 10 -> {
                // Fulgerul din Retezat - chain lightning
                Abilitate fulger = new Abilitate("Curent Monofazat", 16, Arrays.asList("magical"), 40, 3, 78,
                        Map.of("intelligence", 1.5), "Shock", 2, 3);
                yield fulger;
            }

            case 15 -> {
                // Trăsnetul Solomonarului - lightning bolt
                Abilitate trasnet = new Abilitate("Curent Trifazat", 35, Arrays.asList("magical"), 50, 3, 82,
                        Map.of("intelligence", 2.2), null, 0, 0);
                yield trasnet;
            }

            case 20 -> {
                // Explozia de la Cluj - arcane explosion
                Abilitate explozie = new Abilitate("Explozia de la Cluj", 25, Arrays.asList("magical"), 45, 4, 79,
                        Map.of("intelligence", 1.9), "Silence", 3, 0);
                yield explozie;
            }

            case 25 -> {
                // Meteoritul Ardealului - meteor
                Abilitate meteor = new Abilitate("Downclock Solar", 45, Arrays.asList("magical"), 70, 5, 77,
                        Map.of("intelligence", 2.5), "Burn", 4, 6);
                yield meteor;
            }

            case 30 -> {
                // Puterea Dacilor - ultimate spell
                Abilitate daci = new Abilitate("Mass Curse", 60, Arrays.asList("magical"), 90, 7, 80,
                        Map.of("intelligence", 3.0), "Curse", 5, 10);
                yield daci;
            }

            default -> null;
        };
    }

    /**
     * Activează scutul magic.
     */
    public void activeazaScutMagic(int tururi, int absorbtie) {
        this.scutMagic = true;
        this.turiScutMagic = tururi;
        this.absorbtieScutMagic = absorbtie;
        System.out.println("\n🛡️ " + getNume() + " invocă Scutul Corvinilor!");
       // System.out.println(getAbilityOneLiner("Scutul Corvinilor"));
    }

    /**
     * Activează inteligența Ardealului (buff).
     */
    public void activeazaInteligenţaArdealului() {
        this.inteligenţaArdealului = true;
        System.out.println("\n📚 " + getNume() + " folosește cunoștințele învățate la Cluj!");
       // System.out.println(getRandomLine(MAGIC_LINES));
    }

    @Override
    public void iaDamage(int damage) {
        if (scutMagic && turiScutMagic > 0) {
            int damageAbsorbit = Math.min(damage, absorbtieScutMagic);
            damage -= damageAbsorbit;
            absorbtieScutMagic -= damageAbsorbit;

            System.out.println("✨ Scutul magic absoarbe " + damageAbsorbit + " damage!");

            if (absorbtieScutMagic <= 0) {
                scutMagic = false;
                turiScutMagic = 0;
                System.out.println("💔 Scutul magic s-a spart!");
            }
        }

        super.iaDamage(damage);
    }

    @Override
    public double getCritChanceTotal() {
        double baseCrit = super.getCritChanceTotal();
        double intCrit = getIntelligenceTotal() * 0.4;

        if (inteligenţaArdealului) {
            intCrit += 15.0;
        }

        return Math.min(baseCrit + intCrit, 60.0);
    }

    /**
     * Bonus la regenerare mană pentru ardeleni.
     */
    @Override
    public int regenNormal() {
        int baseRegen = 8;
        int intBonus = getIntelligenceTotal() / 3;

        if (inteligenţaArdealului) {
            baseRegen = (int)(baseRegen * 1.3);
        }

        return baseRegen + intBonus;
    }

    // Getteri pentru stările speciale
    public boolean areScutMagic() { return scutMagic; }
    public int getTuriScutMagic() { return turiScutMagic; }
    public boolean areInteligenţaArdealului() { return inteligenţaArdealului; }
}