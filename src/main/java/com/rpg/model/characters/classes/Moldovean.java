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

        // 1. Lovitura cu Sarmale - atac basic puternic
        Abilitate sarmale = new Abilitate("Palmă Moldovenească", 20, Arrays.asList("physical"), 0, 0, 85,
                Map.of("strength", 1.5), null, 0, 0);
        abilitati.add(sarmale);

        // 2. Capul cu Scutul - stun attack
        Abilitate scutBatere = new Abilitate("Cap în Gură", 12, Arrays.asList("physical"), 20, 2, 80,
                Map.of("strength", 1.2), "Stun", 1, 0);
        abilitati.add(scutBatere);

        // 3. Scut de Țară - defensive stance
        Abilitate scutTara = new Abilitate("No Entry 404", 0, Arrays.asList("physical"), 25, 4, 100,
                Map.of("strength", 0.5), null, 0, 0);
        scutTara.setBuff("ScutDeTara", 3, Map.of("defense", 1.5, "damage_reduction", 1.3));
        abilitati.add(scutTara);
    }

    @Override
    public Abilitate abilitateSpecialaNivel(int nivel) {
        return switch (nivel) {
            case 5 -> {
                //  taunt
                Abilitate urla = new Abilitate("Rugăciune de Război Moldovenească", 5, Arrays.asList("special"), 30, 3, 90,
                        Map.of("strength", 0.8), "Provoke", 2, 0);
                yield urla;
            }

            case 10 -> {
                // berserker mode
                Abilitate palinca = new Abilitate("Overclock cu Horincă", 0, Arrays.asList("special"), 40, 5, 100,
                        Map.of("strength", 0.5), null, 0, 0);
                palinca.setBuff("PalincaFierbinte", 4, Map.of("strength", 1.4, "dexterity", 1.2, "crit_chance", 1.3));
                yield palinca;
            }

            case 15 -> {
                //  whirlwind attack
                Abilitate capra = new Abilitate("Furtună de la Hâncești", 30, Arrays.asList("physical"), 45, 3, 82,
                        Map.of("strength", 1.8), null, 0, 0);
                yield capra;
            }

            case 20 -> {
                // execute
                Abilitate hat = new Abilitate("Punct Final pe Dialect", 50, Arrays.asList("physical"), 50, 4, 85,
                        Map.of("strength", 2.2), null, 0, 0);
                yield hat;
            }

            case 25 -> {
                // Zid Moldovenesc - shield wall
                Abilitate zid = new Abilitate("Cortina de Fier", 0, Arrays.asList("special"), 60, 6, 100,
                        Map.of("strength", 0.3), null, 0, 0);
                zid.setBuff("ZidMoldovenesc", 5, Map.of("defense", 2.0, "damage_reduction", 1.5));
                yield zid;
            }

            case 30 -> {
                // Puterea Carpaților - ultimate
                Abilitate carpati = new Abilitate("Spiritul Subcarpatic", 80, Arrays.asList("physical"), 80, 8, 88,
                        Map.of("strength", 2.5), "Stun", 2, 0);
                yield carpati;
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