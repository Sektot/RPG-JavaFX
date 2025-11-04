package com.rpg.model.characters.classes;

import com.rpg.model.abilities.Abilitate;
import com.rpg.model.abilities.AbilityDefinitions;
import com.rpg.model.abilities.AbilityType;
import com.rpg.model.abilities.ConfiguredAbility;
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

    /**i thin
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

        // ==================== BASIC ABILITIES (Level 1) ====================

        // 1. 🔮 Săgeată Arcanică - Basic magic missile (low cost, reliable)
        Abilitate sageata = new Abilitate("Săgeată Arcanică", 12, Arrays.asList("magical"), 10, 0, 92,
                Map.of("intelligence", 1.3), null, 0, 0);
        sageata.setAbilityType(AbilityType.OFFENSIVE)
                .setRequiredLevel(1);
        abilitati.add(sageata);

        // 2. 🔥 Minge de Foc - Fireball with burn DoT
        Abilitate foc = new Abilitate("Minge de Foc", 20, Arrays.asList("magical", "fire"), 20, 2, 85,
                Map.of("intelligence", 1.6), "Burn", 3, 5);
        foc.setAbilityType(AbilityType.OFFENSIVE)
                .setRequiredLevel(1);
        abilitati.add(foc);

        // 3. 🛡️ Barieră Magică - Mana shield
        Abilitate bariera = new Abilitate("Barieră Magică", 0, Arrays.asList("magical"), 25, 4, 100,
                Map.of("intelligence", 0.4), null, 0, 0);
        bariera.setAbilityType(AbilityType.BUFF)
                .setRequiredLevel(1)
                .setBuff("BarieraMagica", 3, Map.of("defense", 1.3, "magic_resistance", 1.4));
        abilitati.add(bariera);

        // 🆕 NEW SYSTEM: Unlock starting configured ability (Fireball)
        if (AbilityDefinitions.getVariantsForAbility("Fireball") != null &&
            !AbilityDefinitions.getVariantsForAbility("Fireball").isEmpty()) {
            ConfiguredAbility fireball = AbilityDefinitions.createDefaultConfiguredAbility(
                    AbilityDefinitions.createFireballBase(),
                    AbilityDefinitions.createFireballVariants(),
                    AbilityDefinitions.createFireballTalents()
            );
            this.unlockConfiguredAbility(fireball);
            this.addAbilityToLoadout("Fireball");
            System.out.println("✨ Starting Ability Unlocked: Fireball (customizable)");
        }
    }

    @Override
    public Abilitate abilitateSpecialaNivel(int nivel) {
        return switch (nivel) {
            case 3 -> {
                // ❄️ Gheață Ascuțită - Ice shard (freeze + damage)
                Abilitate gheata = new Abilitate("Gheață Ascuțită", 25, Arrays.asList("magical", "ice"), 30, 2, 88,
                        Map.of("intelligence", 1.7), "Freeze", 2, 0);
                gheata.setAbilityType(AbilityType.OFFENSIVE)
                        .setRequiredLevel(3);

                // 🆕 NEW SYSTEM: Unlock Ice Shard at level 3
                if (AbilityDefinitions.getVariantsForAbility("Ice Shard") != null) {
                    ConfiguredAbility iceShard = AbilityDefinitions.createDefaultConfiguredAbility(
                            AbilityDefinitions.createIceShardBase(),
                            AbilityDefinitions.createIceShardVariants(),
                            AbilityDefinitions.createIceShardTalents()
                    );
                    this.unlockConfiguredAbility(iceShard);
                    if (this.getLoadoutSize() < 6) {
                        this.addAbilityToLoadout("Ice Shard");
                    }
                    System.out.println("❄️ New Ability Unlocked: Ice Shard (customizable)");
                }

                yield gheata;
            }

            case 5 -> {
                // 💫 Explozie Arcanică - AOE blast
                Abilitate explozie = new Abilitate("Explozie Arcanică", 30, Arrays.asList("magical", "arcane"), 40, 3, 83,
                        Map.of("intelligence", 1.8), null, 0, 0);
                explozie.setAbilityType(AbilityType.OFFENSIVE)
                        .setRequiredLevel(5)
                        .setAOE(true)
                        .setNumberOfHits(2);  // Hits twice

                // 🆕 NEW SYSTEM: Unlock Lightning Bolt at level 5
                if (AbilityDefinitions.getVariantsForAbility("Lightning Bolt") != null) {
                    ConfiguredAbility lightning = AbilityDefinitions.createDefaultConfiguredAbility(
                            AbilityDefinitions.createLightningBoltBase(),
                            AbilityDefinitions.createLightningBoltVariants(),
                            AbilityDefinitions.createLightningBoltTalents()
                    );
                    this.unlockConfiguredAbility(lightning);
                    // Auto-add to loadout if space available
                    if (this.getLoadoutSize() < 6) {
                        this.addAbilityToLoadout("Lightning Bolt");
                    }
                    System.out.println("⚡ New Ability Unlocked: Lightning Bolt (customizable)");
                }

                yield explozie;
            }

            case 7 -> {
                // 🆕 NEW SYSTEM: Unlock Arcane Missiles at level 7
                if (AbilityDefinitions.getVariantsForAbility("Arcane Missiles") != null) {
                    ConfiguredAbility arcaneMissiles = AbilityDefinitions.createDefaultConfiguredAbility(
                            AbilityDefinitions.createArcaneMissilesBase(),
                            AbilityDefinitions.createArcaneMissilesVariants(),
                            AbilityDefinitions.createArcaneMissilesTalents()
                    );
                    this.unlockConfiguredAbility(arcaneMissiles);
                    if (this.getLoadoutSize() < 6) {
                        this.addAbilityToLoadout("Arcane Missiles");
                    }
                    System.out.println("✨ New Ability Unlocked: Arcane Missiles (customizable)");
                }
                yield null;  // No old-system ability at level 7
            }

            case 8 -> {
                // 💙 Sifon de Mană - Mana steal ability
                Abilitate sifon = new Abilitate("Sifon de Mană", 18, Arrays.asList("magical"), 15, 3, 90,
                        Map.of("intelligence", 1.4), null, 0, 0);
                sifon.setAbilityType(AbilityType.OFFENSIVE)
                        .setRequiredLevel(8)
                        .setResourceGenerated(30);  // Restores 30 mana on hit!
                yield sifon;
            }

            case 10 -> {
                // ⚡ Lanț de Fulgere - Chain lightning
                Abilitate fulger = new Abilitate("Lanț de Fulgere", 22, Arrays.asList("magical", "lightning"), 45, 3, 85,
                        Map.of("intelligence", 1.6), "Shock", 2, 4);
                fulger.setAbilityType(AbilityType.OFFENSIVE)
                        .setRequiredLevel(10)
                        .setNumberOfHits(3)  // Hits 3 targets
                        .setAOE(true);

                // 🆕 NEW SYSTEM: Unlock Meteor Strike at level 10
                if (AbilityDefinitions.getVariantsForAbility("Meteor Strike") != null) {
                    ConfiguredAbility meteorStrike = AbilityDefinitions.createDefaultConfiguredAbility(
                            AbilityDefinitions.createMeteorStrikeBase(),
                            AbilityDefinitions.createMeteorStrikeVariants(),
                            AbilityDefinitions.createMeteorStrikeTalents()
                    );
                    this.unlockConfiguredAbility(meteorStrike);
                    if (this.getLoadoutSize() < 6) {
                        this.addAbilityToLoadout("Meteor Strike");
                    }
                    System.out.println("☄️ New Ability Unlocked: Meteor Strike (customizable)");
                }

                yield fulger;
            }

            case 15 -> {
                // 🧙 Putere Magică - Mana surge buff
                Abilitate putere = new Abilitate("Putere Magică", 0, Arrays.asList("magical"), 50, 5, 100,
                        Map.of("intelligence", 0.5), null, 0, 0);
                putere.setAbilityType(AbilityType.BUFF)
                        .setRequiredLevel(15)
                        .setBuff("PutereMagica", 4, Map.of("intelligence", 1.5, "crit_chance", 1.3, "spell_power", 1.4));
                yield putere;
            }

            case 20 -> {
                // 🌟 Rază Prismatică - Multi-element beam
                Abilitate raza = new Abilitate("Rază Prismatică", 50, Arrays.asList("magical", "fire", "ice", "lightning"), 60, 4, 87,
                        Map.of("intelligence", 2.3), null, 0, 0);
                raza.setAbilityType(AbilityType.OFFENSIVE)
                        .setRequiredLevel(20)
                        .setNumberOfHits(3);  // Tri-elemental beam
                yield raza;
            }

            case 25 -> {
                // ☄️ Meteorit - Massive AOE damage + burn
                Abilitate meteor = new Abilitate("Meteorit", 65, Arrays.asList("magical", "fire"), 75, 5, 82,
                        Map.of("intelligence", 2.7), "Burn", 4, 8);
                meteor.setAbilityType(AbilityType.OFFENSIVE)
                        .setRequiredLevel(25)
                        .setAOE(true)
                        .setComboRequirement("Minge de Foc")  // Must use Fireball first
                        .setComboBonusDamage(0.6);  // +60% damage if combo'd
                yield meteor;
            }

            case 30 -> {
                // 🌌 ULTIMATE: Maelstrom Arcanic - Ultimate spell storm
                Abilitate ult = new Abilitate("Maelstrom Arcanic", 90, Arrays.asList("magical", "arcane", "legendary"), 100, 10, 90,
                        Map.of("intelligence", 3.5), "Silence", 3, 15);
                ult.setAbilityType(AbilityType.OFFENSIVE)
                        .setRequiredLevel(30)
                        .setUltimate(true)
                        .setAOE(true)
                        .setNumberOfHits(7)  // Hits 7 times!
                        .setResourceGenerated(50)  // Restores 50 mana
                        .setBuff("MaelstromPower", 3, Map.of("intelligence", 1.7, "crit_damage", 1.6, "mana_regen", 2.0));
                yield ult;
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