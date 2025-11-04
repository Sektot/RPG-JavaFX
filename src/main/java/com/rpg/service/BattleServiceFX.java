package com.rpg.service;

import com.rpg.dungeon.model.MultiBattleState;
import com.rpg.model.abilities.Abilitate;
import com.rpg.model.abilities.ConfiguredAbility;
import com.rpg.model.abilities.AbilityModifier;
import com.rpg.model.characters.Erou;
import com.rpg.model.characters.Inamic;
import com.rpg.service.dto.AbilityDTO;
import com.rpg.service.dto.BattleInitDTO;
import com.rpg.utils.RandomUtils;
import com.rpg.utils.GameConstants;

import java.util.*;

/**
 * BattleServiceFX - Sistem de luptă refactorizat pentru JavaFX
 * Suportă atât lupte single-enemy cât și multi-enemy (până la 4 inamici)
 */
public class BattleServiceFX {

    // Listener pentru evenimente de luptă
    public interface BattleListener {
        void onBattleLog(String message);
        void onTurnStart(boolean isHeroTurn);
        void onHealthChanged(Erou hero, Inamic enemy);
        void onAbilityUsed(String abilityName, int damage);
        void onBattleEnd(AbilityDTO.BattleResultDTO result);
    }

    private BattleListener listener;
    private boolean battleActive;
    private int turnCount;

    // Multi-enemy battle state
    private MultiBattleState multiBattleState;
    private boolean isMultiBattle = false;

    // 🆕 Combo tracking - tracks the last ability used by the hero
    private String lastAbilityUsed = null;

    public BattleServiceFX() {
        this.battleActive = false;
        this.turnCount = 0;
    }

    public void setListener(BattleListener listener) {
        this.listener = listener;
    }

    /**
     * Inițializează o nouă bătălie (single enemy - backwards compatible)
     */
    public BattleInitDTO initializeBattle(Erou hero, Inamic enemy) {
        battleActive = true;
        turnCount = 0;
        isMultiBattle = false;
        multiBattleState = null;
        lastAbilityUsed = null;  // 🆕 Reset combo tracking

        // Reset cooldown-uri
        resetAbilityCooldowns(hero);

        log("⚔️ Bătălia începe!");
        log(hero.getNume() + " vs " + enemy.getNume());

        if (enemy.isBoss()) {
            log("💀 BOSS BATTLE! Pregătește-te pentru o luptă grea!");
        }

        return new BattleInitDTO(
                hero.getNume(),
                hero.getViata(),
                hero.getViataMaxima(),
                hero.getResursaCurenta(),
                hero.getResursaMaxima(),
                hero.getTipResursa(),
                enemy.getNume(),
                enemy.getViata(),
                enemy.getViataMaxima(),
                enemy.isBoss(),
                getAvailableAbilities(hero)
        );
    }

    /**
     * Inițializează o bătălie cu mai mulți inamici
     */
    public BattleInitDTO initializeMultiBattle(Erou hero, MultiBattleState battleState) {
        battleActive = true;
        turnCount = 0;
        isMultiBattle = true;
        this.multiBattleState = battleState;
        lastAbilityUsed = null;  // 🆕 Reset combo tracking

        // Reset cooldown-uri
        resetAbilityCooldowns(hero);

        // Check if there are any enemies to fight
        if (battleState.getActiveEnemyCount() == 0 && battleState.getReinforcementQueueSize() == 0) {
            System.out.println("❌ ERROR: Attempted to start multi-battle with 0 enemies!");
            throw new IllegalStateException("Cannot start battle with no enemies!");
        }

        log("⚔️ MULTI-ENEMY BATTLE!");
        log(hero.getNume() + " vs " + battleState.getActiveEnemyCount() + " enemies!");

        if (battleState.getReinforcementQueueSize() > 0) {
            log("⚠️ " + battleState.getReinforcementQueueSize() + " reinforcements incoming!");
        }

        // Get first active enemy for initial display
        List<Inamic> activeEnemies = battleState.getActiveEnemies();
        if (activeEnemies.isEmpty()) {
            throw new IllegalStateException("No active enemies in battle state!");
        }
        Inamic firstEnemy = activeEnemies.get(0);

        return new BattleInitDTO(
                hero.getNume(),
                hero.getViata(),
                hero.getViataMaxima(),
                hero.getResursaCurenta(),
                hero.getResursaMaxima(),
                hero.getTipResursa(),
                firstEnemy.getNume(),
                firstEnemy.getViata(),
                firstEnemy.getViataMaxima(),
                firstEnemy.isBoss(),
                getAvailableAbilities(hero)
        );
    }

    /**
     * Execută atacul normal al eroului (single or multi-enemy)
     */
    public AbilityDTO.BattleTurnResultDTO executeNormalAttack(Erou hero, Inamic enemy) {
        if (!battleActive) {
            return new AbilityDTO.BattleTurnResultDTO(false, "Bătălia nu este activă!", false, null);
        }

        // Redirect to multi-enemy version if applicable
        if (isMultiBattle && multiBattleState != null) {
            return executeNormalAttackMulti(hero, enemy);
        }

        turnCount++;
        List<String> logs = new ArrayList<>();

        // Tura eroului
        logs.add("━━━ Tura " + turnCount + " ━━━");
        logs.add(hero.getNume() + " atacă!");

        // ✅ HIT CHANCE CHECK
        double hitChance = hero.getHitChance();
        if (!RandomUtils.chancePercent(hitChance)) {
            logs.add("❌ " + hero.getNume() + " ratează atacul!");
        } else {
            int damage = hero.calculeazaDamage();

            // ✅ CRITICAL HIT CHECK
            double critChance = hero.getCritChanceTotal();
            boolean isCrit = RandomUtils.chancePercent(critChance);

            if (isCrit) {
                damage = (int) (damage * hero.getCritMultiplierTotal());
                logs.add("⚡ CRITICAL HIT!");
            }

            int actualDamage = enemy.primesteDamage(damage);
            logs.add("💥 " + hero.getNume() + " face " + actualDamage + " damage!" + (isCrit ? " (CRIT)" : ""));

            // ✅ LIFESTEAL from run items + talent tree
            double lifestealPercent = hero.getLifestealTotal();
            if (lifestealPercent > 0) {
                int healAmount = (int) (actualDamage * lifestealPercent);
                if (healAmount > 0) {
                    hero.vindeca(healAmount);
                    logs.add("🩸 Lifesteal: +" + healAmount + " HP!");
                }
            }
        }

        // Verifică dacă inamicul a murit
        if (!enemy.esteViu()) {
            // 🆕 EXPLOSIVE: Deal damage on death
            if (enemy.hasAffix(com.rpg.model.enemies.EnemyAffix.EXPLOSIVE)) {
                int explosionDamage = 50;
                int actualExplosion = hero.primesteDamage(explosionDamage);
                logs.add("💣 EXPLOSIVE! " + enemy.getNume() + " explodes, dealing " + actualExplosion + " damage!");
            }

            logs.add("✅ " + enemy.getNume() + " a fost învins!");
            return finalizeBattle(hero, enemy, true, logs);
        }

        // Tura inamicului
        logs.add("");
        logs.add(enemy.getNume() + " contraatacă!");

        // 🆕 CHECK FOR STUN - Stunned enemies cannot act
        if (enemy.getDebuffDuration("Stun") > 0) {
            logs.add("💫 " + enemy.getNume() + " is STUNNED and cannot act!");
            // Skip to end-of-turn effects (debuffs still process)
        } else {

        // 🆕 REGENERATING: Heal at turn start
        if (enemy.hasAffix(com.rpg.model.enemies.EnemyAffix.REGENERATING)) {
            int healAmount = (int) (enemy.getViataMaxima() * 0.05);
            enemy.vindeca(healAmount);
            logs.add("💚 Regenerating! " + enemy.getNume() + " heals " + healAmount + " HP!");
        }

        // 🆕 ENEMY ABILITY SYSTEM: Try to use an ability
        boolean usedAbility = tryUseEnemyAbility(enemy, hero, logs);

        // If ability was used, skip normal attack (but update effects at end)
        if (!usedAbility) {
            // 🆕 FAST: Attack twice per turn
            int numberOfAttacks = enemy.hasAffix(com.rpg.model.enemies.EnemyAffix.FAST) ? 2 : 1;
            if (numberOfAttacks > 1) {
                logs.add("⚡ Fast! " + enemy.getNume() + " attacks twice!");
            }

            for (int attackNum = 1; attackNum <= numberOfAttacks; attackNum++) {
            if (!hero.esteViu()) break; // Stop if hero dies

            if (numberOfAttacks > 1) {
                logs.add("  ➤ Attack " + attackNum + "/" + numberOfAttacks + ":");
            }

            // ✅ DODGE CHANCE CHECK
            double dodgeChance = hero.getDodgeChanceTotal();
            if (RandomUtils.chancePercent(dodgeChance)) {
                logs.add((numberOfAttacks > 1 ? "    " : "") + "💨 " + hero.getNume() + " evită atacul!");
            } else {
                int enemyDamage = enemy.calculeazaDamage();

                // 🆕 ENRAGED: +50% damage, +30% crit chance
                double critBonus = 0;
                if (enemy.hasAffix(com.rpg.model.enemies.EnemyAffix.ENRAGED)) {
                    enemyDamage = (int) (enemyDamage * 1.5);
                    critBonus = 30.0;
                }

                // 🆕 BERSERKER: Gain damage as HP decreases
                if (enemy.hasAffix(com.rpg.model.enemies.EnemyAffix.BERSERKER)) {
                    double missingHpPercent = 1.0 - ((double) enemy.getViata() / enemy.getViataMaxima());
                    double damageBonus = missingHpPercent * 0.8; // Up to +80% at low HP
                    enemyDamage = (int) (enemyDamage * (1.0 + damageBonus));
                    if (damageBonus > 0.3) {
                        logs.add("😡 Berserker! +" + (int)(damageBonus * 100) + "% damage from low HP!");
                    }
                }

                // ✅ ENEMY CRITICAL HIT CHECK
                double critChance = enemy.getCritChance() + critBonus;
                double critMultiplier = GameConstants.CRIT_DAMAGE_MULTIPLIER;

                // 🆕 CRITICAL: Enhanced crit chance and multiplier
                if (enemy.hasAffix(com.rpg.model.enemies.EnemyAffix.CRITICAL)) {
                    critChance += 40.0;
                    critMultiplier = 3.0; // 3x damage instead of 2.5x
                }

                boolean enemyCrit = RandomUtils.chancePercent(critChance);
                if (enemyCrit) {
                    enemyDamage = (int) (enemyDamage * critMultiplier);
                    logs.add((numberOfAttacks > 1 ? "    " : "") + "⚡ " + enemy.getNume() + " lovește CRITIC!");
                }

                int actualEnemyDamage = hero.primesteDamage(enemyDamage);
                logs.add((numberOfAttacks > 1 ? "    " : "") + "💥 " + enemy.getNume() + " face " + actualEnemyDamage + " damage!" + (enemyCrit ? " (CRIT)" : ""));

                // 🆕 VAMPIRIC: Heal for 30% of damage dealt
                if (enemy.hasAffix(com.rpg.model.enemies.EnemyAffix.VAMPIRIC) && actualEnemyDamage > 0) {
                    int healAmount = (int) (actualEnemyDamage * 0.3);
                    enemy.vindeca(healAmount);
                    logs.add((numberOfAttacks > 1 ? "    " : "") + "🧛 Vampiric! " + enemy.getNume() + " heals " + healAmount + " HP!");
                }
            }
            }
        } // End of if (!usedAbility) - ability replaces normal attack

        // 🆕 UPDATE ENEMY ABILITY EFFECTS (duration-based buffs/debuffs)
        if (enemy.esteViu()) {
            enemy.updateAbilityEffects();
        }

        } // End of else - enemy not stunned, performed actions

        // ✅ UPDATE BUFFS/DEBUFFS AT TURN END (happens regardless of stun)
        hero.aplicaEfecteleBuffurilor();

        // 🆕 UPDATE HERO DEBUFFS (process DOT, decrease duration)
        List<String> heroDebuffLogs = hero.processDebuffsWithLogs();
        logs.addAll(heroDebuffLogs); // Add hero debuff damage logs for UI visualization

        // 🆕 UPDATE ENEMY DEBUFFS (process DOT, decrease duration)
        if (enemy.esteViu()) {
            List<String> debuffLogs = enemy.processDebuffsWithLogs();
            logs.addAll(debuffLogs); // Add debuff damage logs for UI visualization

            // ☠️ VENOMOUS WOUNDS: Poison can crit
            if (enemy.esteViu()) {
                for (ConfiguredAbility ability : hero.getAbilityLoadout().getActiveAbilities()) {
                    if (ability != null && ability.getCombinedModifiers() != null) {
                        AbilityModifier modifier = ability.getCombinedModifiers();
                        if (modifier.getCustomProperty("poison_crit") != null) {
                            // Check if enemy has poison debuff
                            if (enemy.getDebuffDuration("Poison") > 0) {
                                double critChance = hero.getCritChanceTotal();
                                if (RandomUtils.chancePercent(critChance)) {
                                    int poisonDamage = enemy.getDebuffDamage("Poison");
                                    int bonusDamage = (int) (poisonDamage * GameConstants.CRIT_DAMAGE_MULTIPLIER);
                                    int actualBonus = enemy.primesteDamage(bonusDamage);
                                    logs.add("☠️ Venomous Wounds! Poison critically strikes for " + actualBonus + " bonus damage!");

                                    if (!enemy.esteViu()) {
                                        logs.add("💀 " + enemy.getNume() + " died from critical poison!");
                                        return finalizeBattle(hero, enemy, true, logs);
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }

            // Check if enemy died from DOT
            if (!enemy.esteViu()) {
                logs.add("💀 " + enemy.getNume() + " a murit de la debuff-uri!");
                return finalizeBattle(hero, enemy, true, logs);
            }
        }

        // ✅ REGENERATION from run items
        int regenAmount = hero.getRunItemRegenPerTurn();
        if (regenAmount > 0 && hero.esteViu()) {
            hero.vindeca(regenAmount);
            logs.add("💚 Regenerare: +" + regenAmount + " HP!");
        }

        // Verifică dacă eroul a murit
        if (!hero.esteViu()) {
            logs.add("💀 " + hero.getNume() + " a fost învins!");
            return finalizeBattle(hero, enemy, false, logs);
        }

        return new AbilityDTO.BattleTurnResultDTO(
                true,
                String.join("\n", logs),
                false,
                new AbilityDTO.BattleStateDTO(
                        hero.getViata(),
                        hero.getViataMaxima(),
                        hero.getResursaCurenta(),
                        hero.getResursaMaxima(),
                        enemy.getViata(),
                        enemy.getViataMaxima(),
                        getAvailableAbilities(hero)
                )
        );
    }

    /**
     * Execută atacul normal în bătălie cu mai mulți inamici
     */
    private AbilityDTO.BattleTurnResultDTO executeNormalAttackMulti(Erou hero, Inamic targetEnemy) {
        turnCount++;
        List<String> logs = new ArrayList<>();

        // Process reinforcements at turn start
        multiBattleState.processTurn();

        logs.add("━━━ Tura " + turnCount + " ━━━");
        logs.add("🎯 Enemies active: " + multiBattleState.getActiveEnemyCount() + "/4");

        if (multiBattleState.getReinforcementQueueSize() > 0) {
            MultiBattleState.ReinforcementEntry next = multiBattleState.getNextReinforcement();
            int turnsRemaining = next.getTurnsRemaining(multiBattleState.getCurrentTurn());
            logs.add("⏰ Reinforcement arriving in " + turnsRemaining + " turns");
        }
        logs.add("");

        // Hero attacks target enemy
        logs.add(hero.getNume() + " atacă " + targetEnemy.getNume() + "!");

        double hitChance = hero.getHitChance();
        if (!RandomUtils.chancePercent(hitChance)) {
            logs.add("❌ " + hero.getNume() + " ratează atacul!");
        } else {
            int damage = hero.calculeazaDamage();

            double critChance = hero.getCritChanceTotal();
            boolean isCrit = RandomUtils.chancePercent(critChance);

            if (isCrit) {
                damage = (int) (damage * hero.getCritMultiplierTotal());
                logs.add("⚡ CRITICAL HIT!");
            }

            int actualDamage = targetEnemy.primesteDamage(damage);
            logs.add("💥 " + hero.getNume() + " face " + actualDamage + " damage!" + (isCrit ? " (CRIT)" : ""));

            double lifestealPercent = hero.getLifestealTotal();
            if (lifestealPercent > 0) {
                int healAmount = (int) (actualDamage * lifestealPercent);
                if (healAmount > 0) {
                    hero.vindeca(healAmount);
                    logs.add("🩸 Lifesteal: +" + healAmount + " HP!");
                }
            }
        }

        // Check if target enemy died
        if (!targetEnemy.esteViu()) {
            logs.add("✅ " + targetEnemy.getNume() + " a fost învins!");
        }

        // Clean up ALL dead enemies from slots
        System.out.println("🧹 Before cleanup:");
        for (int i = 0; i < MultiBattleState.MAX_ACTIVE_ENEMIES; i++) {
            MultiBattleState.BattleSlot slot = multiBattleState.getSlot(i);
            if (slot != null && slot.isActive() && slot.getEnemy() != null) {
                System.out.println("  Slot " + i + ": " + slot.getEnemy().getNume() +
                    " - HP: " + slot.getEnemy().getViata() + "/" + slot.getEnemy().getViataMaxima() +
                    " - Alive: " + slot.getEnemy().esteViu());
            }
        }

        multiBattleState.cleanupDeadEnemies();

        System.out.println("🧹 After cleanup:");
        System.out.println("  Active enemies: " + multiBattleState.getActiveEnemyCount());
        System.out.println("  Reinforcements: " + multiBattleState.getReinforcementQueueSize());

        // Check if battle is over (no alive enemies and no reinforcements)
        if (multiBattleState.getActiveEnemyCount() == 0 && multiBattleState.getReinforcementQueueSize() == 0) {
            logs.add("🎉 All enemies defeated!");
            return finalizeMultiBattle(hero, true, logs);
        }

        // All alive enemies attack
        logs.add("");
        List<Inamic> aliveEnemies = multiBattleState.getActiveEnemies();

        for (Inamic enemy : aliveEnemies) {
            if (!enemy.esteViu()) continue;

            logs.add(enemy.getNume() + " contraatacă!");

            // 🆕 CHECK FOR STUN - Stunned enemies cannot act
            if (enemy.getDebuffDuration("Stun") > 0) {
                logs.add("💫 " + enemy.getNume() + " is STUNNED and cannot act!");
                continue; // Skip to next enemy
            }

            double dodgeChance = hero.getDodgeChanceTotal();
            if (RandomUtils.chancePercent(dodgeChance)) {
                logs.add("💨 " + hero.getNume() + " evită atacul!");
            } else {
                int enemyDamage = enemy.calculeazaDamage();

                boolean enemyCrit = RandomUtils.chancePercent(enemy.getCritChance());
                if (enemyCrit) {
                    enemyDamage = (int) (enemyDamage * GameConstants.CRIT_DAMAGE_MULTIPLIER);
                    logs.add("⚡ " + enemy.getNume() + " lovește CRITIC!");
                }

                int actualEnemyDamage = hero.primesteDamage(enemyDamage);
                logs.add("💥 " + enemy.getNume() + " face " + actualEnemyDamage + " damage!" + (enemyCrit ? " (CRIT)" : ""));

                // ⚡ RETRIBUTION: Check if hero has Shield Bash with Retribution talent
                if (actualEnemyDamage > 0) {
                    for (ConfiguredAbility ability : hero.getAbilityLoadout().getActiveAbilities()) {
                        if (ability != null && ability.getCombinedModifiers() != null) {
                            AbilityModifier modifier = ability.getCombinedModifiers();
                            if (modifier.getCustomProperty("retribution") != null) {
                                double reflectPercent = ((Number) modifier.getCustomProperty("retribution")).doubleValue();
                                int reflectedDamage = (int) (actualEnemyDamage * reflectPercent);
                                if (reflectedDamage > 0 && enemy.esteViu()) {
                                    int actualReflect = enemy.primesteDamage(reflectedDamage);
                                    logs.add("⚡ Retribution! " + actualReflect + " damage reflected back to " + enemy.getNume() + "!");
                                    if (!enemy.esteViu()) {
                                        logs.add("✅ " + enemy.getNume() + " killed by reflected damage!");
                                        multiBattleState.cleanupDeadEnemies();
                                    }
                                }
                                break; // Only apply retribution once
                            }
                        }
                    }
                }
            }

            if (!hero.esteViu()) {
                logs.add("💀 " + hero.getNume() + " a fost învins!");
                return finalizeMultiBattle(hero, false, logs);
            }
        }

        // Turn end effects
        hero.aplicaEfecteleBuffurilor();

        // 🆕 UPDATE HERO DEBUFFS (process DOT, decrease duration)
        List<String> heroDebuffLogs = hero.processDebuffsWithLogs();
        logs.addAll(heroDebuffLogs); // Add hero debuff damage logs for UI visualization

        // 🆕 UPDATE ALL ENEMY DEBUFFS (process DOT for all enemies)
        List<Inamic> allEnemiesForDebuff = multiBattleState.getActiveEnemies();
        for (Inamic enemy : allEnemiesForDebuff) {
            if (enemy.esteViu()) {
                enemy.actualizeazaStari();

                // ☠️ VENOMOUS WOUNDS: Poison can crit (multi-battle)
                if (enemy.esteViu()) {
                    for (ConfiguredAbility ability : hero.getAbilityLoadout().getActiveAbilities()) {
                        if (ability != null && ability.getCombinedModifiers() != null) {
                            AbilityModifier modifier = ability.getCombinedModifiers();
                            if (modifier.getCustomProperty("poison_crit") != null) {
                                // Check if enemy has poison debuff
                                if (enemy.getDebuffDuration("Poison") > 0) {
                                    double critChance = hero.getCritChanceTotal();
                                    if (RandomUtils.chancePercent(critChance)) {
                                        int poisonDamage = enemy.getDebuffDamage("Poison");
                                        int bonusDamage = (int) (poisonDamage * GameConstants.CRIT_DAMAGE_MULTIPLIER);
                                        int actualBonus = enemy.primesteDamage(bonusDamage);
                                        logs.add("☠️ Venomous Wounds! " + enemy.getNume() + "'s poison critically strikes for " + actualBonus + " bonus damage!");

                                        if (!enemy.esteViu()) {
                                            logs.add("💀 " + enemy.getNume() + " died from critical poison!");
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }

        // Clean up any enemies that died from DOT
        multiBattleState.cleanupDeadEnemies();

        // Check if all enemies died from DOT
        if (multiBattleState.getActiveEnemyCount() == 0 && multiBattleState.getReinforcementQueueSize() == 0) {
            logs.add("🎉 All enemies defeated (some from debuffs)!");
            return finalizeMultiBattle(hero, true, logs);
        }

        int regenAmount = hero.getRunItemRegenPerTurn();
        if (regenAmount > 0 && hero.esteViu()) {
            hero.vindeca(regenAmount);
            logs.add("💚 Regenerare: +" + regenAmount + " HP!");
        }

        // Return current state (use first alive enemy for display)
        List<Inamic> aliveEnemies2 = multiBattleState.getActiveEnemies();
        Inamic displayEnemy = aliveEnemies2.isEmpty() ? targetEnemy : aliveEnemies2.get(0);

        return new AbilityDTO.BattleTurnResultDTO(
                true,
                String.join("\n", logs),
                false,
                new AbilityDTO.BattleStateDTO(
                        hero.getViata(),
                        hero.getViataMaxima(),
                        hero.getResursaCurenta(),
                        hero.getResursaMaxima(),
                        displayEnemy.getViata(),
                        displayEnemy.getViataMaxima(),
                        getAvailableAbilities(hero)
                )
        );
    }

    /**
     * Execută o abilitate cu suport complet pentru toate mecanicile noi
     */
    public AbilityDTO.BattleTurnResultDTO executeAbility(Erou hero, Inamic enemy, String abilityName) {
        if (!battleActive) {
            return new AbilityDTO.BattleTurnResultDTO(false, "Bătălia nu este activă!", false, null);
        }

        Abilitate abilitate = findAbility(hero, abilityName);

        if (abilitate == null) {
            return new AbilityDTO.BattleTurnResultDTO(false, "Abilitatea nu a fost găsită!", false, null);
        }

        // 🆕 Check if this is a ConfiguredAbility (new system)
        ConfiguredAbility configuredAbility = findConfiguredAbility(hero, abilityName);
        boolean usingNewSystem = (configuredAbility != null);

        // Use final stats from ConfiguredAbility if available, otherwise use base
        int finalManaCost = usingNewSystem ? configuredAbility.getFinalManaCost() : abilitate.getCostMana();
        int baseDamage = usingNewSystem ? configuredAbility.getFinalDamage() : abilitate.getDamage();

        if (!abilitate.poateFiFolosita()) {
            return new AbilityDTO.BattleTurnResultDTO(
                    false,
                    "Abilitatea este în cooldown! Încă " + abilitate.getCooldownRamasa() + " ture.",
                    false,
                    null
            );
        }

        if (hero.getResursaCurenta() < finalManaCost) {
            return new AbilityDTO.BattleTurnResultDTO(
                    false,
                    "Nu ai destul " + hero.getTipResursa() + "! Necesar: " + finalManaCost,
                    false,
                    null
            );
        }

        turnCount++;
        List<String> logs = new ArrayList<>();

        logs.add("╔═══ Tura " + turnCount + " ═══");

        // 🆕 ULTIMATE INDICATOR
        if (abilitate.isUltimate()) {
            logs.add("🌟⚡ ULTIMATE ABILITY ⚡🌟");
        }

        logs.add("✨ " + hero.getNume() + " folosește " + abilitate.getNume() + "!");

        // 🆕 SELF-DAMAGE (Berserker abilities)
        if (abilitate.getSelfDamage() > 0) {
            hero.iaDamage(abilitate.getSelfDamage());
            logs.add("💔 " + hero.getNume() + " își sacrifică " + abilitate.getSelfDamage() + " HP pentru putere!");

            // Check if hero killed himself
            if (!hero.esteViu()) {
                logs.add("💀 " + hero.getNume() + " și-a sacrificat prea multă viață!");
                return finalizeBattle(hero, enemy, false, logs);
            }
        }

        // Folosește abilitatea (use final mana cost)
        hero.consumaResursa(finalManaCost);
        abilitate.aplicaCooldown();

        // 🆕 HEALING (healAmount or healPercent)
        if (abilitate.getHealAmount() > 0 || abilitate.getHealPercent() > 0) {
            int healAmount = abilitate.getHealAmount();

            // Percentage-based healing
            if (abilitate.getHealPercent() > 0) {
                healAmount += (int) (hero.getViataMaxima() * abilitate.getHealPercent());
            }

            if (healAmount > 0) {
                hero.vindeca(healAmount);
                logs.add("💚 " + hero.getNume() + " se vindecă cu " + healAmount + " HP!");
            }
        }

        // ✅ APPLY BUFF TO HERO IF ABILITY HAS BUFF
        if (abilitate.getBuffAplicat() != null && !abilitate.getModificatoriBuff().isEmpty()) {
            hero.aplicaBuff(abilitate.getBuffAplicat(), abilitate.getModificatoriBuff(), abilitate.getDurataBuff());
            logs.add("✨ Buff aplicat: " + abilitate.getBuffAplicat() + " pentru " + abilitate.getDurataBuff() + " ture!");
        }

        // Calculează damage-ul abilității
        Map<String, Integer> statsMap = new HashMap<>();
        statsMap.put("strength", hero.getStrengthTotal());
        statsMap.put("dexterity", hero.getDexterityTotal());
        statsMap.put("intelligence", hero.getIntelligenceTotal());

        // Use final damage from ConfiguredAbility if available
        int abilityDamage;
        if (usingNewSystem) {
            // For ConfiguredAbility: use final damage (already includes talent modifiers)
            // Still apply stat scaling from base ability
            abilityDamage = baseDamage + abilitate.calculeazaDamage(statsMap) - abilitate.getDamage();
        } else {
            // Old system: just calculate normally
            abilityDamage = abilitate.calculeazaDamage(statsMap);
        }

        // 🆕 COMBO BONUS DAMAGE
        boolean comboActivated = false;
        if (abilitate.getComboRequirement() != null && !abilitate.getComboRequirement().isEmpty()) {
            if (abilitate.getComboRequirement().equals(lastAbilityUsed)) {
                comboActivated = true;
                int bonusDamage = (int) (abilityDamage * abilitate.getComboBonusDamage());
                abilityDamage += bonusDamage;
                logs.add("🔥 COMBO ACTIVATED! +" + bonusDamage + " bonus damage!");
            } else {
                logs.add("⚠️ Combo failed! Need to use " + abilitate.getComboRequirement() + " first.");
            }
        }

        // Track this ability for future combos
        lastAbilityUsed = abilitate.getNume();

        // 🆕 SPECIAL EFFECT: Extra hits from talents
        if (usingNewSystem && configuredAbility != null) {
            AbilityModifier modifier = configuredAbility.getCombinedModifiers();

            // Check for +1 hit talents (Arcane Missiles)
            if (modifier.getCustomProperty("extra_hit") != null) {
                int extraHits = ((Number) modifier.getCustomProperty("extra_hit")).intValue();
                abilitate.setNumberOfHits(abilitate.getNumberOfHits() + extraHits);
                logs.add("✨ Extra Hit! +" + extraHits + " additional strike(s)!");
            }

            // Check for +2 hits (Missile Barrage)
            if (modifier.getCustomProperty("barrage_hits") != null) {
                int barrageHits = ((Number) modifier.getCustomProperty("barrage_hits")).intValue();
                abilitate.setNumberOfHits(abilitate.getNumberOfHits() + barrageHits);
                logs.add("🌟 Missile Barrage! +" + barrageHits + " additional missiles!");
            }

            // Check for Double Hit talents (Whirlwind, Backstab, Shadow Step)
            if (modifier.getCustomProperty("double_hit") != null && (Boolean) modifier.getCustomProperty("double_hit")) {
                abilitate.setNumberOfHits(abilitate.getNumberOfHits() * 2);
                logs.add("👥 Double Strike! Hits doubled!");
            }

            // Execute mechanics - damage scaling based on missing HP
            if (modifier.getCustomProperty("execute_scaling") != null && enemy.esteViu()) {
                double hpPercent = (double) enemy.getViata() / enemy.getViataMaxima();
                if (hpPercent <= 0.3) { // Execute Weakness (Whirlwind)
                    abilityDamage *= 2;
                    logs.add("💀 Execute Weakness! +100% damage to low HP enemy!");
                }
            }

            if (modifier.getCustomProperty("sudden_death") != null && enemy.esteViu()) {
                double missingHpPercent = 1.0 - ((double) enemy.getViata() / enemy.getViataMaxima());
                double damageBonus = missingHpPercent * 2.0; // 200% scaling
                abilityDamage = (int) (abilityDamage * (1.0 + damageBonus));
                logs.add("💀 Sudden Death! +" + (int)(damageBonus * 100) + "% damage based on missing HP!");
            }
        }

        // Only deal damage if ability has damage (some abilities are pure buffs)
        if (abilityDamage > 0) {
            // 🆕 MULTI-HIT MECHANICS
            int numberOfHits = Math.max(1, abilitate.getNumberOfHits());
            int totalDamageDealt = 0;
            boolean anyCrit = false; // Track if any hit was a crit

            if (numberOfHits > 1) {
                logs.add("⚔️ Multi-hit ability! Strikes " + numberOfHits + " times!");
            }

            for (int hit = 1; hit <= numberOfHits; hit++) {
                if (!enemy.esteViu()) break;  // Stop if enemy dies mid-combo

                if (numberOfHits > 1) {
                    logs.add("  ➤ Hit " + hit + "/" + numberOfHits + ":");
                }

                // ✅ HIT CHANCE CHECK WITH BONUS
                double hitChance = hero.getHitChance() + abilitate.getHitChanceBonus();
                if (!RandomUtils.chancePercent(hitChance)) {
                    logs.add("    ❌ Miss!");
                }
                // 🆕 PHASING: Enemy dodge chance
                else if (enemy.hasAffix(com.rpg.model.enemies.EnemyAffix.PHASING) && RandomUtils.chancePercent(25.0)) {
                    logs.add("    👻 Phasing! " + enemy.getNume() + " phases out!");
                }
                else {
                    int hitDamage = abilityDamage;

                    // ✅ CRITICAL HIT CHECK (per hit)
                    double critChance = hero.getCritChanceTotal();
                    boolean isCrit = RandomUtils.chancePercent(critChance);

                    if (isCrit) {
                        double critMultiplier = hero.getCritMultiplierTotal();

                        // 🆕 DEADLY PRECISION: Enhanced crit damage
                        if (usingNewSystem && configuredAbility != null) {
                            AbilityModifier modifier = configuredAbility.getCombinedModifiers();
                            if (modifier.getCustomProperty("deadly_precision") != null) {
                                critMultiplier += 1.5; // +150% crit damage
                            }
                        }

                        hitDamage = (int) (hitDamage * critMultiplier);
                        logs.add("    ⚡ CRITICAL HIT!");
                        anyCrit = true; // Mark that we got a crit
                    }

                    // 🆕 ARCANE AMPLIFICATION: Ramping damage
                    if (usingNewSystem && configuredAbility != null && hit > 1) {
                        AbilityModifier modifier = configuredAbility.getCombinedModifiers();
                        if (modifier.getCustomProperty("amplification") != null) {
                            double rampBonus = (hit - 1) * 0.1; // 10% per previous hit
                            hitDamage = (int) (hitDamage * (1.0 + rampBonus));
                            if (hit == 2) {
                                logs.add("    📈 Arcane Amplification activating...");
                            }
                        }
                    }

                    // 🆕 DEFENSIVE AFFIX DAMAGE REDUCTION
                    // ARMORED: +50% defense (reduce damage)
                    if (enemy.hasAffix(com.rpg.model.enemies.EnemyAffix.ARMORED)) {
                        hitDamage = (int) (hitDamage * 0.67); // ~33% damage reduction
                    }

                    // SHIELDED: Damage shield first, then HP
                    int actualDamage = 0;
                    if (enemy.hasAffix(com.rpg.model.enemies.EnemyAffix.SHIELDED) && enemy.getShieldHealth() > 0) {
                        if (hitDamage >= enemy.getShieldHealth()) {
                            // Break through shield
                            int remainingDamage = hitDamage - enemy.getShieldHealth();
                            logs.add("    🛡️ Shield absorbed " + enemy.getShieldHealth() + " damage!");
                            enemy.setShieldHealth(0);
                            enemy.setHasShield(false);
                            actualDamage = enemy.primesteDamage(remainingDamage);
                            logs.add("    💥 Shield broken! " + actualDamage + " damage to HP!");
                        } else {
                            // Shield absorbs all damage
                            enemy.setShieldHealth(enemy.getShieldHealth() - hitDamage);
                            actualDamage = 0;
                            logs.add("    🛡️ Shield absorbed " + hitDamage + " damage! (" + enemy.getShieldHealth() + " shield remaining)");
                        }
                    } else {
                        actualDamage = enemy.primesteDamage(hitDamage);
                    }

                    totalDamageDealt += actualDamage;

                    String prefix = numberOfHits > 1 ? "    " : "";
                    // Only show damage message if not already shown by shield logic
                    if (!(enemy.hasAffix(com.rpg.model.enemies.EnemyAffix.SHIELDED) && enemy.getShieldHealth() > 0)) {
                        logs.add(prefix + "💥 " + actualDamage + " damage!" + (isCrit ? " (CRIT)" : ""));
                    }

                    // 🆕 ENEMY AFFIX REACTIONS TO DAMAGE
                    if (actualDamage > 0 && enemy.esteViu()) {
                        // BURNING: Reflect fire damage
                        if (enemy.hasAffix(com.rpg.model.enemies.EnemyAffix.BURNING)) {
                            int reflectedDamage = (int) (actualDamage * 0.3);
                            if (reflectedDamage > 0) {
                                int actualReflect = hero.primesteDamage(reflectedDamage);
                                logs.add(prefix + "🔥 Burning! " + actualReflect + " fire damage reflected!");
                            }
                        }

                        // SHOCKING: Chain lightning
                        if (enemy.hasAffix(com.rpg.model.enemies.EnemyAffix.SHOCKING)) {
                            int lightningDamage = 20;
                            int actualLightning = hero.primesteDamage(lightningDamage);
                            logs.add(prefix + "⚡ Shocking! " + actualLightning + " lightning damage!");
                        }

                        // FROZEN AURA: Apply slow (reduce dexterity)
                        if (enemy.hasAffix(com.rpg.model.enemies.EnemyAffix.FROZEN_AURA)) {
                            // Apply slow via buff system (30% dex reduction for 2 turns)
                            Map<String, Double> slowEffects = new HashMap<>();
                            slowEffects.put("dexterity", 0.7); // -30% dexterity
                            hero.aplicaBuff("Frozen Aura", slowEffects, 2);
                            logs.add(prefix + "❄️ Frozen Aura! You are slowed!");
                        }

                        // POISONOUS: Apply poison damage over time
                        if (enemy.hasAffix(com.rpg.model.enemies.EnemyAffix.POISONOUS)) {
                            // Directly deal poison damage (since heroes might not have DOT system)
                            int poisonDamage = 15;
                            logs.add(prefix + "☠️ Poisonous! You take " + poisonDamage + " poison damage!");
                            // Note: For full DOT effect, hero would need debuff system like enemies
                        }

                        // ARCANE: Reflect magic damage (simplified - just reflect all damage)
                        if (enemy.hasAffix(com.rpg.model.enemies.EnemyAffix.ARCANE)) {
                            int reflectedMagic = (int) (actualDamage * 0.3);
                            if (reflectedMagic > 0) {
                                int actualReflect = hero.primesteDamage(reflectedMagic);
                                logs.add(prefix + "🌟 Arcane Reflection! " + actualReflect + " magic damage reflected!");
                            }
                        }

                        // 🆕 HP THRESHOLD CHECKS
                        double hpPercent = (double) enemy.getViata() / enemy.getViataMaxima();

                        // TELEPORTING: Escape at 30% HP (once per battle)
                        if (enemy.hasAffix(com.rpg.model.enemies.EnemyAffix.TELEPORTING) && hpPercent <= 0.3 && !enemy.hasTeleported()) {
                            enemy.vindeca((int) (enemy.getViataMaxima() * 0.2)); // Heal 20%
                            enemy.setHasTeleported(true);
                            logs.add(prefix + "🌀 Teleporting! " + enemy.getNume() + " escapes and heals!");
                        }

                        // SUMMONER: Spawn minion at 50% HP (once per battle)
                        if (enemy.hasAffix(com.rpg.model.enemies.EnemyAffix.SUMMONER) && hpPercent <= 0.5 && !enemy.hasSummonedMinion()) {
                            enemy.setHasSummonedMinion(true);
                            logs.add(prefix + "👥 Summoner! " + enemy.getNume() + " summons a minion!");
                            logs.add(prefix + "(Minion support not yet implemented - you get a free turn!)");
                            // TODO: Implement multi-enemy combat for minion spawning
                        }
                    }

                    // ✅ LIFESTEAL from run items + talent tree (per hit)
                    double lifestealPercent = hero.getLifestealTotal();
                    if (lifestealPercent > 0) {
                        int healAmount = (int) (actualDamage * lifestealPercent);
                        if (healAmount > 0) {
                            hero.vindeca(healAmount);
                            logs.add(prefix + "🩸 Lifesteal: +" + healAmount + " HP!");
                        }
                    }
                }
            }

            if (numberOfHits > 1 && totalDamageDealt > 0) {
                logs.add("💥 Total damage: " + totalDamageDealt + "!");
            }

            // ✅ APPLY DEBUFF TO ENEMY IF ABILITY HAS DEBUFF
            if (abilitate.getDebuffAplicat() != null && abilitate.getDurataDebuff() > 0) {
                Map<String, Double> debuffMods = new HashMap<>();
                int debuffDuration = abilitate.getDurataDebuff();
                int debuffDamage = abilitate.getDamageDebuff();

                // 🆕 PERMAFROST: Double slow duration
                if (usingNewSystem && configuredAbility != null) {
                    AbilityModifier modifier = configuredAbility.getCombinedModifiers();
                    if (modifier.getCustomProperty("permafrost") != null &&
                        abilitate.getDebuffAplicat().toLowerCase().contains("slow")) {
                        debuffDuration *= 2;
                        logs.add("❄️ Permafrost! Slow duration doubled!");
                    }

                    // 🆕 INFERNO: Double burn damage
                    if (modifier.getCustomProperty("inferno") != null &&
                        abilitate.getDebuffAplicat().toLowerCase().contains("burn")) {
                        debuffDamage *= 2;
                        logs.add("🔥 Inferno! Burn damage doubled!");
                    }

                    // 🆕 CONCUSSIVE BLOW: +1 stun duration
                    if (modifier.getCustomProperty("concussive_blow") != null &&
                        abilitate.getDebuffAplicat().toLowerCase().contains("stun")) {
                        debuffDuration += 1;
                        logs.add("💫 Concussive Blow! Stun duration extended!");
                    }

                    // 🆕 LINGERING POISON: +2 poison duration
                    if (modifier.getCustomProperty("lingering_poison") != null &&
                        abilitate.getDebuffAplicat().toLowerCase().contains("poison")) {
                        debuffDuration += 2;
                        logs.add("⏱️ Lingering Poison! Poison duration extended!");
                    }

                    // 🆕 DEADLY TOXIN: +40% poison damage
                    if (modifier.getCustomProperty("deadly_toxin") != null &&
                        abilitate.getDebuffAplicat().toLowerCase().contains("poison")) {
                        debuffDamage = (int) (debuffDamage * 1.4);
                        logs.add("☠️ Deadly Toxin! Poison damage increased!");
                    }
                }

                if (debuffDamage > 0) {
                    debuffMods.put("damage_over_time", (double) debuffDamage);
                }

                enemy.aplicaDebuff(abilitate.getDebuffAplicat(), debuffDuration, debuffDamage);
                logs.add("🔥 Debuff aplicat pe inamic: " + abilitate.getDebuffAplicat() + " pentru " + debuffDuration + " ture!");
            }

            // 🆕 FROZEN TOMB: Freeze all enemies on crit
            if (usingNewSystem && configuredAbility != null && anyCrit && isMultiBattle && multiBattleState != null) {
                AbilityModifier modifier = configuredAbility.getCombinedModifiers();
                if (modifier.getCustomProperty("frozen_tomb") != null) {
                    List<Inamic> aliveEnemies = multiBattleState.getActiveEnemies();
                    logs.add("🧊 FROZEN TOMB! All enemies frozen by critical strike!");
                    for (Inamic target : aliveEnemies) {
                        if (target.esteViu() && target != enemy) {
                            target.aplicaDebuff("Freeze", 1, 0);
                            logs.add("  🧊 " + target.getNume() + " is frozen solid!");
                        }
                    }
                }
            }

            // 🆕 METEOR STUN: AOE stun
            if (usingNewSystem && configuredAbility != null && isMultiBattle && multiBattleState != null) {
                AbilityModifier modifier = configuredAbility.getCombinedModifiers();
                if (modifier.getCustomProperty("meteor_stun") != null) {
                    List<Inamic> aliveEnemies = multiBattleState.getActiveEnemies();
                    logs.add("💫 METEOR STUN! All enemies stunned by impact!");
                    for (Inamic target : aliveEnemies) {
                        if (target.esteViu()) {
                            target.aplicaDebuff("Stun", 1, 0);
                            logs.add("  💫 " + target.getNume() + " is stunned!");
                        }
                    }
                }
            }

            // 🆕 INTERRUPT: Silence enemy
            if (usingNewSystem && configuredAbility != null && enemy.esteViu()) {
                AbilityModifier modifier = configuredAbility.getCombinedModifiers();
                if (modifier.getCustomProperty("interrupt") != null) {
                    enemy.aplicaDebuff("Silence", 1, 0);
                    logs.add("🚫 Interrupt! Enemy silenced!");
                }
            }

            // 🆕 STUN CHANCE: Chance to stun enemy (e.g., Lightning Paralyzing Strike)
            if (usingNewSystem && configuredAbility != null && enemy.esteViu()) {
                AbilityModifier modifier = configuredAbility.getCombinedModifiers();
                if (modifier.getCustomProperty("stun_chance") != null) {
                    double stunChance = ((Number) modifier.getCustomProperty("stun_chance")).doubleValue();
                    int stunDuration = modifier.getCustomProperty("stun_duration") != null ?
                            ((Number) modifier.getCustomProperty("stun_duration")).intValue() : 1;

                    if (RandomUtils.chancePercent(stunChance * 100)) {
                        enemy.aplicaDebuff("Stun", stunDuration, 0);
                        logs.add("⚡ STUN! " + enemy.getNume() + " is paralyzed for " + stunDuration + " turn(s)!");
                    }
                }
            }

            // 🆕 SHIELD EXPERT: Gain armor on cast
            if (usingNewSystem && configuredAbility != null) {
                AbilityModifier modifier = configuredAbility.getCombinedModifiers();
                if (modifier.getCustomProperty("shield_expert") != null) {
                    // Apply armor buff to hero (would need buff system expansion)
                    logs.add("🛡️ Shield Expert! Gained 20 armor for 2 turns!");
                }
            }

            // 🆕 EVASIVE SHADOW: Dodge buff on cast
            if (usingNewSystem && configuredAbility != null) {
                AbilityModifier modifier = configuredAbility.getCombinedModifiers();
                if (modifier.getCustomProperty("evasive_shadow") != null) {
                    logs.add("💨 Evasive Shadow! +30% dodge chance for 2 turns!");
                }
            }

            // 🆕 SHADOW DRAIN: Remove debuffs from hero
            if (usingNewSystem && configuredAbility != null) {
                AbilityModifier modifier = configuredAbility.getCombinedModifiers();
                if (modifier.getCustomProperty("shadow_drain") != null) {
                    // Remove all debuffs from hero (would need debuff system on hero)
                    logs.add("🌑 Shadow Drain! Debuffs removed!");
                }
            }

            // 🆕 CONTAGION: Poison spreads to nearby enemies
            if (usingNewSystem && configuredAbility != null && isMultiBattle && multiBattleState != null) {
                AbilityModifier modifier = configuredAbility.getCombinedModifiers();
                if (modifier.getCustomProperty("contagion") != null &&
                    abilitate.getDebuffAplicat() != null &&
                    abilitate.getDebuffAplicat().toLowerCase().contains("poison")) {
                    List<Inamic> aliveEnemies = multiBattleState.getActiveEnemies();
                    logs.add("🦠 Contagion! Poison spreads to nearby enemies!");
                    for (Inamic target : aliveEnemies) {
                        if (target.esteViu() && target != enemy) {
                            target.aplicaDebuff("Poison", abilitate.getDurataDebuff(), abilitate.getDamageDebuff());
                            logs.add("  🦠 " + target.getNume() + " is poisoned!");
                        }
                    }
                }
            }

            // 🆕 TALENT SPECIAL EFFECTS (from ConfiguredAbility)
            if (usingNewSystem && configuredAbility != null && totalDamageDealt > 0) {
                AbilityModifier modifier = configuredAbility.getCombinedModifiers();

                // 🩸 LIFESTEAL (from talents - additional to run items)
                if (modifier.hasLifesteal()) {
                    int healAmount = (int) (totalDamageDealt * modifier.getLifestealPercent());
                    if (healAmount > 0) {
                        hero.vindeca(healAmount);
                        logs.add("🩸 Talent Lifesteal: +" + healAmount + " HP!");
                    }
                }

                // 🔴 BLEED EFFECT (damage over time)
                if (modifier.appliesBleed() && enemy.esteViu()) {
                    int bleedDamage = modifier.getBleedDamage();
                    int bleedDuration = modifier.getBleedDuration();
                    enemy.aplicaDebuff("Bleed", bleedDuration, bleedDamage);
                    logs.add("🔴 Bleed applied: " + bleedDamage + " damage/turn for " + bleedDuration + " turns!");
                }

                // 🛡️ ARMOR REDUCTION (temporary debuff)
                if (modifier.getArmorReduction() > 0 && enemy.esteViu()) {
                    int armorReduction = modifier.getArmorReduction();
                    int duration = modifier.getArmorReductionDuration();
                    // Use defense_down debuff type which reduces defense
                    enemy.aplicaDebuff("defense_down", duration, armorReduction);
                    logs.add("🛡️ Armor Shredded: -" + armorReduction + " defense for " + duration + " turns!");
                }

                // ⚡ CHAIN TO ADDITIONAL ENEMIES
                if (modifier.getChainsToTargets() > 0 && isMultiBattle && multiBattleState != null) {
                    List<Inamic> aliveEnemies = multiBattleState.getActiveEnemies();
                    int chainsLeft = modifier.getChainsToTargets();

                    logs.add("⚡ Chain Effect: Bouncing to " + chainsLeft + " additional targets!");

                    for (Inamic chainTarget : aliveEnemies) {
                        if (chainTarget == enemy || !chainTarget.esteViu()) continue;
                        if (chainsLeft <= 0) break;

                        int chainDamage = (int) (abilityDamage * modifier.getChainDamageMultiplier());
                        int actualChainDamage = chainTarget.primesteDamage(chainDamage);
                        logs.add("  ⚡ → " + chainTarget.getNume() + ": " + actualChainDamage + " damage!");

                        chainsLeft--;

                        if (!chainTarget.esteViu()) {
                            logs.add("  ✅ " + chainTarget.getNume() + " defeated by chain!");
                        }
                    }
                }
            }
        }

        // 🆕 RESOURCE GENERATION (after successful hit)
        if (abilitate.getResourceGenerated() > 0) {
            hero.regenResursa(abilitate.getResourceGenerated());
            logs.add("⚡ Generat " + abilitate.getResourceGenerated() + " " + hero.getTipResursa() + "!");
        }

        // Verifică dacă inamicul a murit
        if (!enemy.esteViu()) {
            logs.add("✅ " + enemy.getNume() + " a fost învins!");

            // 🆕 ON-KILL EFFECTS (from talents)
            if (usingNewSystem && configuredAbility != null) {
                AbilityModifier modifier = configuredAbility.getCombinedModifiers();

                // 💥 EXPLOSION ON KILL (AOE damage)
                if (modifier.hasExplosionOnKill()) {
                    int explosionDamage = modifier.getExplosionDamage();
                    logs.add("💥 EXPLOSION! " + enemy.getNume() + " explodes for " + explosionDamage + " AOE damage!");

                    // Deal explosion damage to other enemies if multi-battle
                    if (isMultiBattle && multiBattleState != null) {
                        List<Inamic> aliveEnemies = multiBattleState.getActiveEnemies();
                        for (Inamic target : aliveEnemies) {
                            if (!target.esteViu()) continue;
                            int actualExplosionDamage = target.primesteDamage(explosionDamage);
                            logs.add("  💥 → " + target.getNume() + ": " + actualExplosionDamage + " explosion damage!");

                            if (!target.esteViu()) {
                                logs.add("  ✅ " + target.getNume() + " killed by explosion!");
                            }
                        }
                    }
                }

                // 🔄 COOLDOWN RESET ON KILL
                if (modifier.resetsAbilityCooldownOnKill() && abilitate != null) {
                    abilitate.setCooldownRamasa(0);
                    logs.add("🔄 Cooldown Reset! " + abilitate.getNume() + " is ready to use again!");
                }

                // 💙 MANA REFUND ON KILL
                if (modifier.refundsManaOnKill()) {
                    int manaRefund = (int) (finalManaCost * modifier.getManaRefundPercent());
                    if (manaRefund > 0) {
                        hero.regenResursa(manaRefund);
                        logs.add("💙 Mana Refund: +" + manaRefund + " " + hero.getTipResursa() + "!");
                    }
                }

                // 🔥 BURN ALL ENEMIES ON KILL (for multi-battle)
                if (modifier.burnAllEnemiesOnKill() && isMultiBattle && multiBattleState != null) {
                    List<Inamic> aliveEnemies = multiBattleState.getActiveEnemies();
                    if (!aliveEnemies.isEmpty()) {
                        logs.add("🔥 Firestorm! All enemies are burning!");
                        int burnDamage = 15; // Base burn damage
                        int burnDuration = 3; // Burns for 3 turns
                        for (Inamic burnTarget : aliveEnemies) {
                            if (burnTarget.esteViu()) {
                                burnTarget.aplicaDebuff("burn", burnDuration, burnDamage);
                                logs.add("  🔥 " + burnTarget.getNume() + " is burning!");
                            }
                        }
                    }
                }

                // ☠️ MASTER POISONER: Refresh all poison durations on kill
                if (modifier.getCustomProperty("master_poisoner") != null && isMultiBattle && multiBattleState != null) {
                    List<Inamic> aliveEnemies = multiBattleState.getActiveEnemies();
                    int refreshCount = 0;
                    for (Inamic target : aliveEnemies) {
                        if (target.esteViu()) {
                            // Refresh poison debuff (reapply with same damage/duration)
                            // This is a simplified version - ideally we'd extend existing poison
                            if (abilitate.getDebuffAplicat() != null &&
                                abilitate.getDebuffAplicat().toLowerCase().contains("poison")) {
                                target.aplicaDebuff("Poison", abilitate.getDurataDebuff(), abilitate.getDamageDebuff());
                                refreshCount++;
                            }
                        }
                    }
                    if (refreshCount > 0) {
                        logs.add("🔄 Master Poisoner! Refreshed poison on " + refreshCount + " enemies!");
                    }
                }

                // 💚 TOXIC BUILDUP: Poison stacks 2x (on kill, apply double stacks to survivors)
                if (modifier.getCustomProperty("toxic_buildup") != null && isMultiBattle && multiBattleState != null) {
                    if (abilitate.getDebuffAplicat() != null &&
                        abilitate.getDebuffAplicat().toLowerCase().contains("poison")) {
                        logs.add("💚 Toxic Buildup! Poison intensity increases!");
                        List<Inamic> aliveEnemies = multiBattleState.getActiveEnemies();
                        for (Inamic target : aliveEnemies) {
                            if (target.esteViu()) {
                                // Apply additional poison stack
                                target.aplicaDebuff("Poison", abilitate.getDurataDebuff(), abilitate.getDamageDebuff());
                            }
                        }
                    }
                }
            }

            return finalizeBattle(hero, enemy, true, logs);
        }

        // Tura inamicului (dacă eroul nu a omorât inamicul)
        logs.add("");
        logs.add(enemy.getNume() + " contraatacă!");

        // 🆕 CHECK FOR STUN - Stunned enemies cannot act
        if (enemy.getDebuffDuration("Stun") > 0) {
            logs.add("💫 " + enemy.getNume() + " is STUNNED and cannot act!");
            // Skip to end-of-turn effects (debuffs still process)
        } else {

        // ✅ DODGE CHANCE CHECK
        double dodgeChance = hero.getDodgeChanceTotal();
        if (RandomUtils.chancePercent(dodgeChance)) {
            logs.add("💨 " + hero.getNume() + " evită atacul!");
        } else {
            int enemyDamage = enemy.calculeazaDamage();

            // ✅ ENEMY CRITICAL HIT CHECK
            boolean enemyCrit = RandomUtils.chancePercent(enemy.getCritChance());
            if (enemyCrit) {
                enemyDamage = (int) (enemyDamage * GameConstants.CRIT_DAMAGE_MULTIPLIER);
                logs.add("⚡ " + enemy.getNume() + " lovește CRITIC!");
            }

            int actualEnemyDamage = hero.primesteDamage(enemyDamage);
            logs.add("💥 " + enemy.getNume() + " face " + actualEnemyDamage + " damage!" + (enemyCrit ? " (CRIT)" : ""));

            // ⚡ RETRIBUTION: Check if hero has Shield Bash with Retribution talent
            if (actualEnemyDamage > 0) {
                for (ConfiguredAbility ability : hero.getAbilityLoadout().getActiveAbilities()) {
                    if (ability != null && ability.getCombinedModifiers() != null) {
                        AbilityModifier modifier = ability.getCombinedModifiers();
                        if (modifier.getCustomProperty("retribution") != null) {
                            double reflectPercent = ((Number) modifier.getCustomProperty("retribution")).doubleValue();
                            int reflectedDamage = (int) (actualEnemyDamage * reflectPercent);
                            if (reflectedDamage > 0 && enemy.esteViu()) {
                                int actualReflect = enemy.primesteDamage(reflectedDamage);
                                logs.add("⚡ Retribution! " + actualReflect + " damage reflected back to " + enemy.getNume() + "!");
                                if (!enemy.esteViu()) {
                                    logs.add("✅ " + enemy.getNume() + " killed by reflected damage!");
                                    return finalizeBattle(hero, enemy, true, logs);
                                }
                            }
                            break; // Only apply retribution once
                        }
                    }
                }
            }
        }

        } // End of else - enemy not stunned, performed actions

        // ✅ UPDATE BUFFS/DEBUFFS AT TURN END (happens regardless of stun)
        hero.aplicaEfecteleBuffurilor();

        // 🆕 UPDATE HERO DEBUFFS (process DOT, decrease duration)
        List<String> heroDebuffLogs = hero.processDebuffsWithLogs();
        logs.addAll(heroDebuffLogs); // Add hero debuff damage logs for UI visualization

        // 🆕 UPDATE ENEMY DEBUFFS (process DOT, decrease duration)
        if (enemy.esteViu()) {
            List<String> debuffLogs = enemy.processDebuffsWithLogs();
            logs.addAll(debuffLogs); // Add debuff damage logs for UI visualization

            // ☠️ VENOMOUS WOUNDS: Poison can crit
            if (enemy.esteViu()) {
                for (ConfiguredAbility ability : hero.getAbilityLoadout().getActiveAbilities()) {
                    if (ability != null && ability.getCombinedModifiers() != null) {
                        AbilityModifier modifier = ability.getCombinedModifiers();
                        if (modifier.getCustomProperty("poison_crit") != null) {
                            // Check if enemy has poison debuff
                            if (enemy.getDebuffDuration("Poison") > 0) {
                                double critChance = hero.getCritChanceTotal();
                                if (RandomUtils.chancePercent(critChance)) {
                                    int poisonDamage = enemy.getDebuffDamage("Poison");
                                    int bonusDamage = (int) (poisonDamage * GameConstants.CRIT_DAMAGE_MULTIPLIER);
                                    int actualBonus = enemy.primesteDamage(bonusDamage);
                                    logs.add("☠️ Venomous Wounds! Poison critically strikes for " + actualBonus + " bonus damage!");

                                    if (!enemy.esteViu()) {
                                        logs.add("💀 " + enemy.getNume() + " died from critical poison!");
                                        return finalizeBattle(hero, enemy, true, logs);
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }

            // Check if enemy died from DOT
            if (!enemy.esteViu()) {
                logs.add("💀 " + enemy.getNume() + " a murit de la debuff-uri!");
                return finalizeBattle(hero, enemy, true, logs);
            }
        }

        // ✅ REGENERATION from run items
        int regenAmount = hero.getRunItemRegenPerTurn();
        if (regenAmount > 0 && hero.esteViu()) {
            hero.vindeca(regenAmount);
            logs.add("💚 Regenerare: +" + regenAmount + " HP!");
        }

        // Verifică dacă eroul a murit
        if (!hero.esteViu()) {
            logs.add("💀 " + hero.getNume() + " a fost învins!");
            return finalizeBattle(hero, enemy, false, logs);
        }

        // Update cooldowns
        updateCooldowns(hero);

        return new AbilityDTO.BattleTurnResultDTO(
                true,
                String.join("\n", logs),
                false,
                new AbilityDTO.BattleStateDTO(
                        hero.getViata(),
                        hero.getViataMaxima(),
                        hero.getResursaCurenta(),
                        hero.getResursaMaxima(),
                        enemy.getViata(),
                        enemy.getViataMaxima(),
                        getAvailableAbilities(hero)
                )
        );
    }

    /**
     * Eroul încearcă să fugă din luptă
     */
    public AbilityDTO.BattleTurnResultDTO attemptFlee(Erou hero, Inamic enemy) {
        if (!battleActive) {
            return new AbilityDTO.BattleTurnResultDTO(false, "Bătălia nu este activă!", false, null);
        }

        // Redirect to multi-enemy version if applicable
        if (isMultiBattle && multiBattleState != null) {
            return attemptFleeMulti(hero);
        }

        List<String> logs = new ArrayList<>();

        // Boss-ii nu te lasă să fugi
        if (enemy.isBoss()) {
            logs.add("❌ Nu poți fugi de la un BOSS!");
            logs.add("");

            // Boss-ul atacă
            logs.add(enemy.getNume() + " te atacă în timp ce încerci să fugi!");
            int enemyDamage = enemy.calculeazaDamage();
            int actualDamage = hero.primesteDamage(enemyDamage);
            logs.add("💥 Primești " + actualDamage + " damage!");

            if (!hero.esteViu()) {
                logs.add("💀 " + hero.getNume() + " a fost învins!");
                return finalizeBattle(hero, enemy, false, logs);
            }

            return new AbilityDTO.BattleTurnResultDTO(
                    false,
                    String.join("\n", logs),
                    false,
                    new AbilityDTO.BattleStateDTO(
                            hero.getViata(),
                            hero.getViataMaxima(),
                            hero.getResursaCurenta(),
                            hero.getResursaMaxima(),
                            enemy.getViata(),
                            enemy.getViataMaxima(),
                            getAvailableAbilities(hero)
                    )
            );
        }

        // Șansă de fugă: 70%
        Random random = new Random();
        boolean fleeSuccess = random.nextDouble() < 0.7;

        if (fleeSuccess) {
            logs.add("🏃 Ai reușit să fugi din luptă!");
            battleActive = false;

            return new AbilityDTO.BattleTurnResultDTO(
                    true,
                    String.join("\n", logs),
                    true, // fled successfully
                    null
            );
        } else {
            logs.add("❌ Nu ai reușit să fugi!");
            logs.add("");
            logs.add(enemy.getNume() + " te atacă!");

            int enemyDamage = enemy.calculeazaDamage();
            int actualDamage = hero.primesteDamage(enemyDamage);
            logs.add("💥 Primești " + actualDamage + " damage!");

            if (!hero.esteViu()) {
                logs.add("💀 " + hero.getNume() + " a fost învins!");
                return finalizeBattle(hero, enemy, false, logs);
            }

            return new AbilityDTO.BattleTurnResultDTO(
                    false,
                    String.join("\n", logs),
                    false,
                    new AbilityDTO.BattleStateDTO(
                            hero.getViata(),
                            hero.getViataMaxima(),
                            hero.getResursaCurenta(),
                            hero.getResursaMaxima(),
                            enemy.getViata(),
                            enemy.getViataMaxima(),
                            getAvailableAbilities(hero)
                    )
            );
        }
    }

    /**
     * Eroul încearcă să fugă din bătălie multi-enemy
     */
    private AbilityDTO.BattleTurnResultDTO attemptFleeMulti(Erou hero) {
        List<String> logs = new ArrayList<>();

        // Check if any boss is in battle
        boolean hasBoss = false;
        for (MultiBattleState.BattleSlot slot : multiBattleState.getSlots()) {
            if (slot.isActive() && slot.getEnemy() != null && slot.getEnemy().isBoss()) {
                hasBoss = true;
                break;
            }
        }

        if (hasBoss) {
            logs.add("❌ Nu poți fugi când există un BOSS în bătălie!");
            logs.add("");

            // All enemies attack
            List<Inamic> enemies = multiBattleState.getActiveEnemies();
            for (Inamic enemy : enemies) {
                if (!enemy.esteViu()) continue;

                logs.add(enemy.getNume() + " te atacă!");
                int enemyDamage = enemy.calculeazaDamage();
                int actualDamage = hero.primesteDamage(enemyDamage);
                logs.add("💥 Primești " + actualDamage + " damage!");

                if (!hero.esteViu()) {
                    logs.add("💀 " + hero.getNume() + " a fost învins!");
                    return finalizeMultiBattle(hero, false, logs);
                }
            }

            Inamic displayEnemy = enemies.isEmpty() ? null : enemies.get(0);
            return new AbilityDTO.BattleTurnResultDTO(
                    false,
                    String.join("\n", logs),
                    false,
                    displayEnemy != null ? new AbilityDTO.BattleStateDTO(
                            hero.getViata(),
                            hero.getViataMaxima(),
                            hero.getResursaCurenta(),
                            hero.getResursaMaxima(),
                            displayEnemy.getViata(),
                            displayEnemy.getViataMaxima(),
                            getAvailableAbilities(hero)
                    ) : null
            );
        }

        // Șansă de fugă: 70% (scade cu fiecare inamic: -10% per inamic)
        int enemyCount = multiBattleState.getActiveEnemyCount();
        double fleeChance = 0.7 - ((enemyCount - 1) * 0.1);
        fleeChance = Math.max(0.3, fleeChance); // Minimum 30% chance

        Random random = new Random();
        boolean fleeSuccess = random.nextDouble() < fleeChance;

        if (fleeSuccess) {
            logs.add("🏃 Ai reușit să fugi din luptă!");
            logs.add("(Inamicii vor rămâne la poziția de luptă pentru 3 secunde)");
            battleActive = false;

            return new AbilityDTO.BattleTurnResultDTO(
                    true,
                    String.join("\n", logs),
                    true, // fled successfully
                    null
            );
        } else {
            logs.add("❌ Nu ai reușit să fugi! (" + (int)(fleeChance * 100) + "% șansă)");
            logs.add("");

            // All enemies attack
            List<Inamic> enemies = multiBattleState.getActiveEnemies();
            for (Inamic enemy : enemies) {
                if (!enemy.esteViu()) continue;

                logs.add(enemy.getNume() + " te atacă!");
                int enemyDamage = enemy.calculeazaDamage();
                int actualDamage = hero.primesteDamage(enemyDamage);
                logs.add("💥 Primești " + actualDamage + " damage!");

                if (!hero.esteViu()) {
                    logs.add("💀 " + hero.getNume() + " a fost învins!");
                    return finalizeMultiBattle(hero, false, logs);
                }
            }

            Inamic displayEnemy = enemies.isEmpty() ? null : enemies.get(0);
            return new AbilityDTO.BattleTurnResultDTO(
                    false,
                    String.join("\n", logs),
                    false,
                    displayEnemy != null ? new AbilityDTO.BattleStateDTO(
                            hero.getViata(),
                            hero.getViataMaxima(),
                            hero.getResursaCurenta(),
                            hero.getResursaMaxima(),
                            displayEnemy.getViata(),
                            displayEnemy.getViataMaxima(),
                            getAvailableAbilities(hero)
                    ) : null
            );
        }
    }

    /**
     * Folosește o poțiune în timpul luptei
     */
    public AbilityDTO.BattleTurnResultDTO usePotion(Erou hero, Inamic enemy, int healAmount) {
        List<String> logs = new ArrayList<>();

        // 🔄 VERIFICARE CU FALLBACK la sistemul vechi
        boolean hasNewSystemPotion = hero.getInventar().hasHealthPotion(healAmount);
        boolean hasOldSystemPotion = hero.getHealthPotions() > 0;

        if (!hasNewSystemPotion && !hasOldSystemPotion) {
            return new AbilityDTO.BattleTurnResultDTO(
                    false,
                    "Nu ai poțiuni de vindecare în inventar!",
                    false,
                    currentBattleState(hero, enemy)
            );
        }

        // Verifică dacă ești deja la full HP
        if (hero.getViata() >= hero.getViataMaxima()) {
            return new AbilityDTO.BattleTurnResultDTO(
                    false,
                    "Ești deja la viață maximă! Nu ai nevoie de vindecare.",
                    false,
                    currentBattleState(hero, enemy)
            );
        }

        // 🔄 CONSUMĂ din sistemul disponibil
        if (hasNewSystemPotion) {
            // Folosește sistemul nou
            hero.getInventar().removeHealthPotion(healAmount);
            logs.add(String.format("💚 %s folosește o poțiune avansată și se vindecă cu %d HP!",
                    hero.getNume(), healAmount));
        } else {
            // Fallback la sistemul vechi
            hero.vindeca(healAmount);
            // Scade manual din sistemul vechi (nu există removeHealthPotion în sistemul vechi)
            try {
                java.lang.reflect.Field healthPotionsField = Erou.class.getDeclaredField("healthPotions");
                healthPotionsField.setAccessible(true);
                int currentPotions = healthPotionsField.getInt(hero);
                healthPotionsField.setInt(hero, Math.max(0, currentPotions - 1));
            } catch (Exception e) {
                System.out.println("⚠️ Nu pot scădea poțiunea din sistemul vechi: " + e.getMessage());
            }
            logs.add(String.format("💚 %s folosește o poțiune clasică și se vindecă cu %d HP!",
                    hero.getNume(), healAmount));
        }

        // Vindecă eroul (dacă nu s-a făcut deja)
        if (hasNewSystemPotion) {
            hero.vindeca(healAmount);
        }
        // Enemy răspunde (dacă e viu)
        if (enemy.esteViu()) {
            int enemyDamage = enemy.calculeazaDamage();
            int actualDamage = hero.primesteDamage(enemyDamage);

            if (actualDamage > 0) {
                logs.add(String.format("💥 %s atacă și face %d damage!",
                        enemy.getNume(), actualDamage));
            } else {
                logs.add(String.format("🛡️ Atacul lui %s a fost blocat complet!",
                        enemy.getNume()));
            }

            // Verifică moarte după atac
            if (!hero.esteViu()) {
                return new AbilityDTO.BattleTurnResultDTO(
                        true,
                        String.join(" \n", logs),
                        true, // gameOver = true
                        currentBattleState(hero, enemy)
                );
            }
        }

        return new AbilityDTO.BattleTurnResultDTO(
                true,
                String.join(" \n", logs),
                false,
                currentBattleState(hero, enemy)
        );
    }

    /**
     * Folosește o poțiune de resurse (mana/energy/rage)
     */
    public AbilityDTO.BattleTurnResultDTO useResourcePotion(Erou hero, Inamic enemy, int restoreAmount) {
        List<String> logs = new ArrayList<>();

        // Verifică dacă are poțiuni de resurse
        if (hero.getManaPotions() <= 0) {
            AbilityDTO.BattleStateDTO currentState = new AbilityDTO.BattleStateDTO(
                    hero.getViata(), hero.getViataMaxima(),
                    hero.getResursaCurenta(), hero.getResursaMaxima(),
                    enemy.getViata(), enemy.getViataMaxima(),
                    getAvailableAbilities(hero) // ✅ ABILITĂȚI REALE
            );

            return new AbilityDTO.BattleTurnResultDTO(
                    false,
                    "Nu ai poțiuni de " + hero.getTipResursa().toLowerCase() + " în inventar!",
                    false,
                    currentState,
                    null
            );
        }

        // Verifică dacă resursa e deja la maxim
        if (hero.getResursaCurenta() >= hero.getResursaMaxima()) {
            AbilityDTO.BattleStateDTO currentState = new AbilityDTO.BattleStateDTO(
                    hero.getViata(), hero.getViataMaxima(),
                    hero.getResursaCurenta(), hero.getResursaMaxima(),
                    enemy.getViata(), enemy.getViataMaxima(),
                    getAvailableAbilities(hero) // ✅ ABILITĂȚI REALE
            );

            return new AbilityDTO.BattleTurnResultDTO(
                    false,
                    "Ai deja " + hero.getTipResursa().toLowerCase() + " la maxim! Nu ai nevoie de regenerare.",
                    false,
                    currentState,
                    null
            );
        }

        int oldResource = hero.getResursaCurenta();
       // hero.regenereazaResursa(restoreAmount); // ✅ Folosește parametrul, nu getManaPotionRestore()
        int actualRestore = hero.getResursaCurenta() - oldResource;


        // Scade poțiunea folosind metoda din Erou
        boolean potionUsed = hero.useManaPotion();
        if (!potionUsed) {
            // Fallback cu reflection dacă useManaPotion() nu merge
            try {
                java.lang.reflect.Field manaPotionsField = Erou.class.getDeclaredField("manaPotions");
                manaPotionsField.setAccessible(true);
                int currentPotions = manaPotionsField.getInt(hero);
                manaPotionsField.setInt(hero, Math.max(0, currentPotions - 1));
            } catch (Exception e) {
                System.out.println("⚠️ Nu pot scădea poțiunea de resurse: " + e.getMessage());
            }
        }

        logs.add(String.format("💙 %s folosește un Energizant Profi și regenerează %d %s!",
                hero.getNume(), actualRestore, hero.getTipResursa()));

        // Enemy răspunde (dacă e viu)
        if (enemy.esteViu()) {
            int enemyDamage = enemy.calculeazaDamage();
            int actualDamage = hero.primesteDamage(enemyDamage);

            if (actualDamage > 0) {
                logs.add(String.format("💥 %s atacă și face %d damage!",
                        enemy.getNume(), actualDamage));
            } else {
                logs.add(String.format("🛡️ Atacul lui %s a fost blocat complet!",
                        enemy.getNume()));
            }
        }

        // Verifică dacă eroul a murit
        boolean battleOver = !hero.esteViu();

        // Starea finală
        AbilityDTO.BattleStateDTO finalState = new AbilityDTO.BattleStateDTO(
                hero.getViata(), hero.getViataMaxima(),
                hero.getResursaCurenta(), hero.getResursaMaxima(),
                enemy.getViata(), enemy.getViataMaxima(),
                getAvailableAbilities(hero)

        );

        // ✅ Constructorul CORECT cu 5 parametri
        return new AbilityDTO.BattleTurnResultDTO(
                true, // success
                String.join(" \n", logs), // log message
                battleOver, // battle over
                finalState, // current state
                null // final result (doar pentru victory/defeat)
        );
    }


    // 🆕 HELPER METHOD pentru state consistent
    private AbilityDTO.BattleStateDTO currentBattleState(Erou hero, Inamic enemy) {
        return new AbilityDTO.BattleStateDTO(
                hero.getViata(),
                Math.max(1, hero.getViataMaxima()), // Protecție împotriva 0
                hero.getResursaCurenta(),
                Math.max(1, hero.getResursaMaxima()), // Protecție împotriva 0
                enemy.esteViu() ? enemy.getViata() : 0,
                Math.max(1, enemy.getViataMaxima()), // Protecție împotriva 0
                getAvailableAbilities(hero)
        );
    }
    // ==================== HELPER METHODS ====================

    /**
     * Finalizează o bătălie cu mai mulți inamici
     */
    private AbilityDTO.BattleTurnResultDTO finalizeMultiBattle(Erou hero, boolean victory, List<String> logs) {
        battleActive = false;

        if (victory) {
            hero.recordKill();

            // Calculate total rewards from all defeated enemies
            int totalGold = 0;
            int totalExp = 0;
            int totalShaorma = 0;
            List<com.rpg.model.items.ObiectEchipament> allLoot = new ArrayList<>();
            com.rpg.model.items.Jewel jewelDrop = null;

            // Collect rewards from defeated enemies list
            List<Inamic> defeatedEnemies = multiBattleState.getDefeatedEnemies();
            System.out.println("💰 Calculating rewards from " + defeatedEnemies.size() + " defeated enemies");

            for (Inamic enemy : defeatedEnemies) {
                System.out.println("  - " + enemy.getNume() + ": " + enemy.getGoldReward() + " gold, " + enemy.getExpReward() + " exp");
                totalGold += enemy.getGoldReward();
                totalExp += enemy.getExpReward();

                if (enemy.getLoot() != null) {
                    allLoot.addAll(enemy.getLoot());
                }

                if (enemy.isBoss()) {
                    totalShaorma += enemy.getShaormaReward();

                    // Boss jewel drop
                    if (jewelDrop == null) {
                        jewelDrop = LootGenerator.rollBossJewelDrop(enemy.getNivel());
                    }

                    // Boss flask pieces
                    logs.add("\n🧪 ════════════════════════════════════════");
                    logs.add("🧪 FLASK PIECE DROP!");
                    logs.add("🧪 ════════════════════════════════════════");

                    com.rpg.model.items.FlaskPiece.FlaskType[] types = com.rpg.model.items.FlaskPiece.FlaskType.values();
                    com.rpg.model.items.FlaskPiece.FlaskType randomType = types[new java.util.Random().nextInt(types.length)];
                    int quantity = 1 + new java.util.Random().nextInt(2);

                    hero.addFlaskPieces(randomType, quantity);
                    logs.add(String.format("🧪 Ai primit %d x %s Flask Piece!", quantity, randomType.getIcon()));
                }
            }

            // Roll for jewel if no boss (regular jewel drop)
            if (jewelDrop == null && defeatedEnemies.size() > 0) {
                int avgLevel = 0;
                for (Inamic enemy : defeatedEnemies) {
                    avgLevel += enemy.getNivel();
                }
                avgLevel = avgLevel / defeatedEnemies.size();
                jewelDrop = LootGenerator.rollRegularJewelDrop(avgLevel);
            }

            if (jewelDrop != null) {
                logs.add("\n💎 ════════════════════════════════════════");
                logs.add("💎 JEWEL DROP!");
                logs.add("💎 ════════════════════════════════════════");
                LootGenerator.displayJewelDrop(jewelDrop);
            }

            logs.add("\n💰 Total Gold: " + totalGold);
            logs.add("⭐ Total EXP: " + totalExp);
            if (totalShaorma > 0) {
                logs.add("🌯 Total Shaorma: " + totalShaorma);
            }

            AbilityDTO.BattleResultDTO result = new AbilityDTO.BattleResultDTO(
                    true,
                    "Multi-Enemy Victory!",
                    totalGold,
                    totalExp,
                    allLoot,
                    totalShaorma,
                    jewelDrop
            );

            if (listener != null) {
                listener.onBattleEnd(result);
            }

            return new AbilityDTO.BattleTurnResultDTO(
                    true,
                    String.join("\n", logs),
                    false,
                    null,
                    result
            );
        } else {
            // Defeat
            AbilityDTO.BattleResultDTO result = new AbilityDTO.BattleResultDTO(
                    false,
                    "Defeat!",
                    0,
                    0,
                    new ArrayList<>(),
                    0,
                    null
            );

            if (listener != null) {
                listener.onBattleEnd(result);
            }

            return new AbilityDTO.BattleTurnResultDTO(
                    true,
                    String.join("\n", logs),
                    false,
                    null,
                    result
            );
        }
    }

    private AbilityDTO.BattleTurnResultDTO finalizeBattle(Erou hero, Inamic enemy, boolean victory, List<String> logs) {
        battleActive = false;

        // Track kill for conditional bonuses
        if (victory) {
            hero.recordKill();
        }

        // 💎 Roll for jewel drop
        com.rpg.model.items.Jewel jewelDrop = null;
        if (victory) {
            if (enemy.isBoss()) {
                jewelDrop = LootGenerator.rollBossJewelDrop(enemy.getNivel());
            } else {
                jewelDrop = LootGenerator.rollRegularJewelDrop(enemy.getNivel());
            }

            // Display jewel drop if one was rolled
            if (jewelDrop != null) {
                logs.add("\n💎 ════════════════════════════════════════");
                logs.add("💎 JEWEL DROP!");
                logs.add("💎 ════════════════════════════════════════");
                LootGenerator.displayJewelDrop(jewelDrop);
            }

            // Boss flask piece drops
            if (enemy.isBoss()) {
                logs.add("\n🧪 ════════════════════════════════════════");
                logs.add("🧪 FLASK PIECE DROP!");
                logs.add("🧪 ════════════════════════════════════════");

                // Random flask piece type
                com.rpg.model.items.FlaskPiece.FlaskType[] types = com.rpg.model.items.FlaskPiece.FlaskType.values();
                com.rpg.model.items.FlaskPiece.FlaskType randomType = types[new java.util.Random().nextInt(types.length)];

                // Boss drops 1-2 flask pieces
                int quantity = 1 + new java.util.Random().nextInt(2);

                hero.addFlaskPieces(randomType, quantity);
                logs.add(String.format("🧪 Ai primit %d x %s Flask Piece!", quantity, randomType.getIcon()));

                // Boss enchantment scroll drop (50% chance)
                if (new java.util.Random().nextDouble() < 0.5) {
                    logs.add("\n📜 ════════════════════════════════════════");
                    logs.add("📜 ENCHANTMENT SCROLL DROP!");
                    logs.add("📜 ════════════════════════════════════════");

                    // Random enchant type
                    com.rpg.model.items.EnchantScroll.EnchantType[] enchantTypes =
                        com.rpg.model.items.EnchantScroll.EnchantType.values();
                    com.rpg.model.items.EnchantScroll.EnchantType randomEnchantType =
                        enchantTypes[new java.util.Random().nextInt(enchantTypes.length)];

                    // Level based on enemy level (1-3)
                    int scrollLevel = 1 + Math.min(2, enemy.getNivel() / 10);

                    hero.addEnchantScroll(randomEnchantType, 1, scrollLevel);
                    logs.add(String.format("📜 Ai primit %s %s (Level %d)!",
                        randomEnchantType.getIcon(), randomEnchantType.getDisplayName(), scrollLevel));
                }
            }
        }

        AbilityDTO.BattleResultDTO result = new AbilityDTO.BattleResultDTO(
                victory,
                victory ? "Victorie!" : "Înfrângere!",
                enemy.getGoldReward(),
                enemy.getExpReward(),
                enemy.getLoot(),
                enemy.isBoss() ? enemy.getShaormaReward() : 0,
                jewelDrop  // 💎 Add jewel drop to result
        );

        if (listener != null) {
            listener.onBattleEnd(result);
        }

        return new AbilityDTO.BattleTurnResultDTO(
                true,
                String.join("\n", logs),
                false,
                null,
                result
        );
    }

    // 🔧 FIX 2: Înlocuiește metoda getAvailableAbilities()
    // 🆕 UPDATED to support new ConfiguredAbility system
    protected List<AbilityDTO> getAvailableAbilities(Erou hero) {
        List<AbilityDTO> abilities = new ArrayList<>();

        // Check if hero has the new ability loadout system
        if (hero.hasValidLoadout() && hero.getLoadoutSize() > 0) {
            // Use NEW system: ConfiguredAbility from loadout
            List<ConfiguredAbility> loadout = hero.getActiveLoadoutAbilities();

            for (ConfiguredAbility configured : loadout) {
                // Get final stats after variant & talents are applied
                int finalDamage = configured.getFinalDamage();
                int finalManaCost = configured.getFinalManaCost();
                int finalCooldown = configured.getFinalCooldown();

                // Check if base ability exists (for cooldown tracking)
                Abilitate baseAbility = configured.getBaseAbility();
                boolean canUse = baseAbility != null && baseAbility.poateFiFolosita();
                int cooldownRemaining = baseAbility != null ? baseAbility.getCooldownRamasa() : 0;

                // Build description with variant and talent info
                String descriere = String.format("Damage: %d | Cost: %d | Cooldown: %d\nVariant: %s",
                        finalDamage,
                        finalManaCost,
                        finalCooldown,
                        configured.getSelectedVariant().getName());

                abilities.add(new AbilityDTO(
                        configured.getDisplayName(),
                        descriere,
                        finalManaCost,
                        cooldownRemaining,
                        canUse,
                        hero.getResursaCurenta() >= finalManaCost
                ));
            }
        } else {
            // Fallback to OLD system: regular Abilitate list (backward compatibility)
            for (Abilitate abilitate : hero.getAbilitati()) {
                // Creează o descriere pentru abilitate
                String descriere = String.format("Damage: %d | Cost: %d | Cooldown: %d",
                        abilitate.getDamage(),
                        abilitate.getCostMana(),
                        abilitate.getCooldown());

                abilities.add(new AbilityDTO(
                        abilitate.getNume(),
                        descriere,  // folosim descrierea construită
                        abilitate.getCostMana(),
                        abilitate.getCooldownRamasa(),
                        abilitate.poateFiFolosita(),
                        hero.getResursaCurenta() >= abilitate.getCostMana()
                ));
            }
        }

        return abilities;
    }
    // 🔧 FIX 3: Înlocuiește metoda findAbility()
    // 🆕 UPDATED to try ConfiguredAbility first, then fall back
    private Abilitate findAbility(Erou hero, String abilityName) {
        // Try NEW system first: find ConfiguredAbility in loadout
        if (hero.hasValidLoadout()) {
            List<ConfiguredAbility> loadout = hero.getActiveLoadoutAbilities();
            for (ConfiguredAbility configured : loadout) {
                if (configured.getDisplayName().equals(abilityName) ||
                    configured.getBaseAbilityId().equals(abilityName)) {
                    // Return the base ability (we'll handle ConfiguredAbility stats separately)
                    return configured.getBaseAbility();
                }
            }
        }

        // Fallback to OLD system
        for (Abilitate abilitate : hero.getAbilitati()) {
            if (abilitate.getNume().equals(abilityName)) {
                return abilitate;
            }
        }
        return null;
    }

    /**
     * 🆕 NEW: Find ConfiguredAbility by name (for accessing talent modifiers)
     */
    private ConfiguredAbility findConfiguredAbility(Erou hero, String abilityName) {
        if (!hero.hasValidLoadout()) {
            return null;
        }

        List<ConfiguredAbility> loadout = hero.getActiveLoadoutAbilities();
        for (ConfiguredAbility configured : loadout) {
            if (configured.getDisplayName().equals(abilityName) ||
                configured.getBaseAbilityId().equals(abilityName)) {
                return configured;
            }
        }
        return null;
    }

    // 🔧 FIX 4: Înlocuiește metoda resetAbilityCooldowns()
    private void resetAbilityCooldowns(Erou hero) {
        for (Abilitate abilitate : hero.getAbilitati()) {
            abilitate.setCooldownRamasa(0);
        }
    }

    // 🔧 FIX 5: Înlocuiește metoda updateCooldowns()
    private void updateCooldowns(Erou hero) {
        for (Abilitate abilitate : hero.getAbilitati()) {
            abilitate.reduceCooldown();
        }
    }

    private void log(String message) {
        if (listener != null) {
            listener.onBattleLog(message);
        }
    }

    public boolean isBattleActive() {
        return battleActive;
    }

    public int getTurnCount() {
        return turnCount;
    }

    public boolean isMultiBattle() {
        return isMultiBattle;
    }

    public MultiBattleState getMultiBattleState() {
        return multiBattleState;
    }

    // ==================== ENEMY ABILITY SYSTEM ====================

    /**
     * Attempts to use an enemy ability. Returns true if ability was used, false otherwise.
     * Call this before normal enemy attack.
     */
    private boolean tryUseEnemyAbility(Inamic enemy, Erou hero, List<String> logs) {
        // Update cooldowns at start of turn
        enemy.updateAbilityCooldowns();

        // Check if enemy has abilities
        if (enemy.getAbilities() == null || enemy.getAbilities().isEmpty()) {
            return false;
        }

        // Get archetype-based ability usage chance
        int baseChance = 30;
        int archetypeBonus = 0;
        if (enemy.getArchetype() != null) {
            archetypeBonus = enemy.getArchetype().getAbilityUseBonus();
        }
        double totalChance = baseChance + archetypeBonus;

        // Random check
        if (!RandomUtils.chancePercent(totalChance)) {
            return false;
        }

        // 🧠 SMART AI: Choose ability tactically based on situation
        com.rpg.model.enemies.EnemyAbility ability = chooseSmartAbility(enemy, hero);
        if (ability == null) {
            return false; // No good ability choices available
        }

        // Execute the ability
        executeEnemyAbility(enemy, hero, ability, logs);
        enemy.useAbility(ability); // Put on cooldown
        return true;
    }

    /**
     * Executes an enemy ability based on its type.
     */
    private void executeEnemyAbility(Inamic enemy, Erou hero, com.rpg.model.enemies.EnemyAbility ability, List<String> logs) {
        logs.add("🌟 " + enemy.getNume() + " uses " + ability.getFormattedName() + "!");
        logs.add("   ➤ " + ability.getDescription());

        switch (ability) {
            // ==================== OFFENSIVE ABILITIES ====================
            case FIREBALL:
                executeFireball(enemy, hero, logs);
                break;

            case POWER_STRIKE:
                executePowerStrike(enemy, hero, logs);
                break;

            case LIGHTNING_BOLT:
                executeLightningBolt(enemy, hero, logs);
                break;

            case POISON_STRIKE:
                executePoisonStrike(enemy, hero, logs);
                break;

            case EXECUTE:
                executeExecute(enemy, hero, logs);
                break;

            // ==================== DEFENSIVE ABILITIES ====================
            case SHIELD_WALL:
                executeShieldWall(enemy, logs);
                break;

            case DESPERATE_HEAL:
                executeDesperateHeal(enemy, logs);
                break;

            case EVASION:
                executeEvasion(enemy, logs);
                break;

            // ==================== CROWD CONTROL ABILITIES ====================
            case STUN_STRIKE:
                executeStunStrike(enemy, hero, logs);
                break;

            case WEAKENING_CURSE:
                executeWeakeningCurse(enemy, hero, logs);
                break;

            case CRIPPLING_BLOW:
                executeCripplingBlow(enemy, hero, logs);
                break;

            // ==================== TACTICAL ABILITIES ====================
            case BATTLE_CRY:
                executeBattleCry(enemy, logs);
                break;

            case DESPERATE_GAMBIT:
                executeDesperateGambit(enemy, logs);
                break;

            case ENRAGE:
                executeEnrage(enemy, logs);
                break;

            case BLOOD_FRENZY:
                executeBloodFrenzy(enemy, logs);
                break;

            // ==================== NEW CC ABILITIES ====================
            case SILENCE:
                executeSilence(hero, logs);
                break;

            case SLOW:
                executeSlow(hero, logs);
                break;

            case ARMOR_SHATTER:
                executeArmorShatter(hero, logs);
                break;

            case CURSE_OF_WEAKNESS:
                executeCurseOfWeakness(hero, logs);
                break;

            case FROST_NOVA:
                executeFrostNova(enemy, hero, logs);
                break;

            case LIFE_DRAIN:
                executeLifeDrain(enemy, hero, logs);
                break;

            case CORRUPTION:
                executeCorruption(enemy, hero, logs);
                break;
        }
    }

    // ==================== OFFENSIVE ABILITY IMPLEMENTATIONS ====================

    private void executeFireball(Inamic enemy, Erou hero, List<String> logs) {
        int damage = (int) (enemy.getEffectiveDamage() * 1.5);
        int actualDamage = hero.primesteDamage(damage);
        logs.add("🔥 Fireball deals " + actualDamage + " fire damage!");

        // 🆕 Apply BURN debuff (DoT damage over time)
        int burnDamage = Math.max(5, enemy.getNivel() * 3); // Scales with enemy level
        Map<String, Double> burnEffects = new HashMap<>();
        burnEffects.put("damage_per_turn", (double) burnDamage);

        if (hero.getDebuffuriActive().containsKey("burn")) {
            hero.getDebuffuriActive().get("burn").addStack(3); // Refresh and stack
        } else {
            hero.getDebuffuriActive().put("burn", new com.rpg.model.effects.DebuffStack(burnEffects, 3, 3));
        }
        logs.add("   🔥 Hero is BURNING! " + burnDamage + " damage per turn for 3 turns!");
    }

    private void executePowerStrike(Inamic enemy, Erou hero, List<String> logs) {
        int baseDamage = (int) (enemy.getEffectiveDamage() * 2.0);
        // Ignores 50% of defense - apply damage directly
        int heroDefense = hero.getDefense();
        int ignoredDefense = heroDefense / 2;
        int damage = Math.max(1, baseDamage - ignoredDefense);

        hero.setViataCurenta(Math.max(0, hero.getViata() - damage));
        logs.add("💪 Power Strike deals " + damage + " damage (ignoring 50% defense)!");

        // 🆕 Apply ARMOR BREAK debuff (reduces defense by 30%)
        Map<String, Double> armorBreakEffects = new HashMap<>();
        armorBreakEffects.put("defense_reduction", -0.30); // -30% defense

        if (hero.getDebuffuriActive().containsKey("armor_break")) {
            hero.getDebuffuriActive().get("armor_break").addStack(3); // Refresh and stack
        } else {
            hero.getDebuffuriActive().put("armor_break", new com.rpg.model.effects.DebuffStack(armorBreakEffects, 3, 2));
        }
        logs.add("   💔 ARMOR BREAK! Hero's defense reduced by 30% for 3 turns!");
    }

    private void executeLightningBolt(Inamic enemy, Erou hero, List<String> logs) {
        int damage = (int) (enemy.getEffectiveDamage() * 1.2);
        int actualDamage = hero.primesteDamage(damage);
        logs.add("⚡ Lightning chains, dealing " + actualDamage + " damage!");

        // 🆕 30% chance to apply SHOCK debuff (reduces dodge by 40%)
        if (RandomUtils.chancePercent(30.0)) {
            Map<String, Double> shockEffects = new HashMap<>();
            shockEffects.put("dodge_reduction", -0.40); // -40% dodge chance

            if (hero.getDebuffuriActive().containsKey("shock")) {
                hero.getDebuffuriActive().get("shock").addStack(2); // Refresh
            } else {
                hero.getDebuffuriActive().put("shock", new com.rpg.model.effects.DebuffStack(shockEffects, 2, 2));
            }
            logs.add("   ⚡ SHOCKED! Hero's dodge reduced by 40% for 2 turns!");
        }
    }

    private void executePoisonStrike(Inamic enemy, Erou hero, List<String> logs) {
        // Normal attack damage
        int baseDamage = enemy.getEffectiveDamage();
        int actualDamage = hero.primesteDamage(baseDamage);
        logs.add("☠️ Poison Strike deals " + actualDamage + " damage!");

        // 🆕 Apply POISON debuff (DoT damage - stacks up to 3 times)
        int poisonDamage = Math.max(8, enemy.getNivel() * 4); // Scales with enemy level
        Map<String, Double> poisonEffects = new HashMap<>();
        poisonEffects.put("damage_per_turn", (double) poisonDamage);

        if (hero.getDebuffuriActive().containsKey("poison")) {
            hero.getDebuffuriActive().get("poison").addStack(4); // Refresh and stack
        } else {
            hero.getDebuffuriActive().put("poison", new com.rpg.model.effects.DebuffStack(poisonEffects, 4, 3));
        }
        logs.add("   ☠️ POISONED! " + poisonDamage + " damage per turn for 4 turns (stacks up to 3x)!");
    }

    private void executeExecute(Inamic enemy, Erou hero, List<String> logs) {
        double heroHpPercent = (double) hero.getViata() / hero.getViataMaxima();

        if (heroHpPercent <= 0.3) {
            // Hero below 30% HP - devastating damage
            int damage = (int) (enemy.getEffectiveDamage() * 3.0);
            int actualDamage = hero.primesteDamage(damage);
            logs.add("💀 EXECUTE! Hero is low HP - " + actualDamage + " MASSIVE damage!");

            // 🆕 Apply BLEED debuff on successful execute
            int bleedDamage = Math.max(10, enemy.getNivel() * 5);
            Map<String, Double> bleedEffects = new HashMap<>();
            bleedEffects.put("damage_per_turn", (double) bleedDamage);

            if (hero.getDebuffuriActive().containsKey("bleed")) {
                hero.getDebuffuriActive().get("bleed").addStack(3); // Refresh and stack
            } else {
                hero.getDebuffuriActive().put("bleed", new com.rpg.model.effects.DebuffStack(bleedEffects, 3, 3));
            }
            logs.add("   🩸 BLEEDING! " + bleedDamage + " damage per turn for 3 turns!");
        } else {
            // Normal damage if hero above 30% HP
            int damage = enemy.getEffectiveDamage();
            int actualDamage = hero.primesteDamage(damage);
            logs.add("💀 Execute deals " + actualDamage + " damage (hero not low enough for bonus)");
        }
    }

    // ==================== DEFENSIVE ABILITY IMPLEMENTATIONS ====================

    private void executeShieldWall(Inamic enemy, List<String> logs) {
        enemy.applyShieldWall(2, 0.6); // 60% damage reduction for 2 turns
        logs.add("🛡️ Shield Wall raised! 60% damage reduction for 2 turns!");
    }

    private void executeDesperateHeal(Inamic enemy, List<String> logs) {
        int healAmount = (int) (enemy.getViataMaxima() * 0.35);
        enemy.vindeca(healAmount);
        logs.add("💚 Desperate Heal restores " + healAmount + " HP!");
    }

    private void executeEvasion(Inamic enemy, List<String> logs) {
        enemy.activateEvasion(1); // 100% dodge next turn
        logs.add("💨 Evasion activated! Next attack will miss!");
    }

    // ==================== CROWD CONTROL ABILITY IMPLEMENTATIONS ====================

    private void executeStunStrike(Inamic enemy, Erou hero, List<String> logs) {
        // Apply stun debuff to hero
        hero.aplicaDebuff("stun", 1, 0); // Stun for 1 turn

        // Deal moderate damage along with stun
        int damage = (int) (enemy.getEffectiveDamage() * 0.8);
        int actualDamage = hero.primesteDamage(damage);

        logs.add("💫 Stun Strike deals " + actualDamage + " damage and STUNS hero!");
        logs.add("   ➤ Hero cannot act next turn!");
    }

    private void executeWeakeningCurse(Inamic enemy, Erou hero, List<String> logs) {
        // Apply weaken debuff: reduces damage dealt and defense
        hero.aplicaDebuff("weaken", 3, 0); // Lasts 3 turns

        // Minimal damage with the curse
        int damage = (int) (enemy.getEffectiveDamage() * 0.5);
        int actualDamage = hero.primesteDamage(damage);

        logs.add("🌀 Weakening Curse deals " + actualDamage + " damage!");
        logs.add("   ➤ Hero's damage and defense reduced by 30%/20% for 3 turns!");
    }

    private void executeCripplingBlow(Inamic enemy, Erou hero, List<String> logs) {
        // Apply cripple debuff: reduces dodge chance and defense
        hero.aplicaDebuff("cripple", 2, 0); // Lasts 2 turns

        // Moderate damage with the blow
        int damage = (int) (enemy.getEffectiveDamage() * 0.9);
        int actualDamage = hero.primesteDamage(damage);

        logs.add("🎯 Crippling Blow deals " + actualDamage + " damage!");
        logs.add("   ➤ Hero's dodge reduced by 50% and defense by 15% for 2 turns!");
    }

    // ==================== TACTICAL ABILITY IMPLEMENTATIONS ====================

    private void executeBattleCry(Inamic enemy, List<String> logs) {
        enemy.applyDamageBuff(3, 0.5); // +50% damage for 3 turns
        logs.add("📢 Battle Cry! Enemy gains +50% damage for 3 turns!");
    }

    private void executeDesperateGambit(Inamic enemy, List<String> logs) {
        enemy.applyDamageBuff(3, 1.0); // +100% damage for 3 turns
        enemy.applyDefenseDebuff(3, 0.5); // -50% defense for 3 turns
        logs.add("🎲 Desperate Gambit! +100% damage, -50% defense for 3 turns!");
    }

    private void executeEnrage(Inamic enemy, List<String> logs) {
        enemy.applyDamageBuff(2, 0.75); // +75% damage for 2 turns
        // Note: +30% crit would need crit chance tracking, simplified as damage buff
        logs.add("😡 Enrage! Enemy gains +75% damage and +30% crit for 2 turns!");
    }

    private void executeBloodFrenzy(Inamic enemy, List<String> logs) {
        // Sacrifice 20% HP
        int sacrifice = (int) (enemy.getViataMaxima() * 0.2);
        enemy.setViata(Math.max(1, enemy.getViata() - sacrifice));
        logs.add("🩸 Blood Frenzy! Enemy sacrifices " + sacrifice + " HP!");

        // Gain massive damage buff for next attack (one-time, not duration-based)
        enemy.applyDamageBuff(1, 1.5); // +150% damage for 1 turn
        logs.add("   ➤ Next attack will deal +150% damage!");
    }

    // ==================== NEW CC ABILITY IMPLEMENTATIONS ====================

    private void executeSilence(Erou hero, List<String> logs) {
        // Apply silence debuff - prevents ability usage
        Map<String, Double> silenceEffects = new HashMap<>();
        silenceEffects.put("silenced", 1.0); // Flag for silenced state

        if (hero.getDebuffuriActive().containsKey("silence")) {
            hero.getDebuffuriActive().get("silence").addStack(2); // Refresh
        } else {
            hero.getDebuffuriActive().put("silence", new com.rpg.model.effects.DebuffStack(silenceEffects, 2, 1));
        }
        logs.add("🔇 SILENCED! Hero cannot use abilities for 2 turns!");
    }

    private void executeSlow(Erou hero, List<String> logs) {
        // Apply slow debuff - reduces dodge and hit chance
        Map<String, Double> slowEffects = new HashMap<>();
        slowEffects.put("dodge_reduction", -0.50); // -50% dodge
        slowEffects.put("hit_reduction", -0.50); // -50% hit chance (would need implementation)

        if (hero.getDebuffuriActive().containsKey("slow")) {
            hero.getDebuffuriActive().get("slow").addStack(3); // Refresh
        } else {
            hero.getDebuffuriActive().put("slow", new com.rpg.model.effects.DebuffStack(slowEffects, 3, 2));
        }
        logs.add("🐌 SLOWED! Hero's dodge and hit chance reduced by 50% for 3 turns!");
    }

    private void executeArmorShatter(Erou hero, List<String> logs) {
        // Apply armor shatter - massive defense reduction
        Map<String, Double> shatterEffects = new HashMap<>();
        shatterEffects.put("defense_reduction", -0.60); // -60% defense

        if (hero.getDebuffuriActive().containsKey("armor_shatter")) {
            hero.getDebuffuriActive().get("armor_shatter").addStack(4); // Refresh and stack
        } else {
            hero.getDebuffuriActive().put("armor_shatter", new com.rpg.model.effects.DebuffStack(shatterEffects, 4, 2));
        }
        logs.add("💥 ARMOR SHATTERED! Hero's defense reduced by 60% for 4 turns!");
    }

    private void executeCurseOfWeakness(Erou hero, List<String> logs) {
        // Apply curse - reduces all stats
        Map<String, Double> curseEffects = new HashMap<>();
        curseEffects.put("damage_reduction", -0.40); // -40% damage dealt
        curseEffects.put("defense_reduction", -0.40); // -40% defense
        curseEffects.put("dodge_reduction", -0.40); // -40% dodge

        if (hero.getDebuffuriActive().containsKey("curse_of_weakness")) {
            hero.getDebuffuriActive().get("curse_of_weakness").addStack(3); // Refresh
        } else {
            hero.getDebuffuriActive().put("curse_of_weakness", new com.rpg.model.effects.DebuffStack(curseEffects, 3, 1));
        }
        logs.add("👁️ CURSE OF WEAKNESS! All hero stats reduced by 40% for 3 turns!");
    }

    private void executeFrostNova(Inamic enemy, Erou hero, List<String> logs) {
        // Deal cold damage
        int damage = enemy.getEffectiveDamage();
        int actualDamage = hero.primesteDamage(damage);
        logs.add("❄️ Frost Nova deals " + actualDamage + " cold damage!");

        // Apply freeze (stun) for 1 turn
        Map<String, Double> freezeEffects = new HashMap<>();
        freezeEffects.put("stunned", 1.0); // Stun flag
        freezeEffects.put("damage_per_turn", 5.0); // Minimal frost damage

        if (hero.getDebuffuriActive().containsKey("freeze")) {
            hero.getDebuffuriActive().get("freeze").addStack(1); // Refresh
        } else {
            hero.getDebuffuriActive().put("freeze", new com.rpg.model.effects.DebuffStack(freezeEffects, 1, 1));
        }
        logs.add("   ❄️ FROZEN! Hero is stunned for 1 turn!");
    }

    private void executeLifeDrain(Inamic enemy, Erou hero, List<String> logs) {
        // Drain HP from hero
        int drainAmount = (int) (hero.getViataMaxima() * 0.20); // 20% of max HP
        int actualDrain = Math.min(drainAmount, hero.getViata()); // Can't drain more than current HP

        hero.setViataCurenta(Math.max(0, hero.getViata() - actualDrain));
        logs.add("🌑 Life Drain! Enemy drains " + actualDrain + " HP from hero!");

        // Heal enemy
        int healAmount = actualDrain;
        enemy.vindeca(healAmount);
        logs.add("   ➤ Enemy heals for " + healAmount + " HP!");
    }

    private void executeCorruption(Inamic enemy, Erou hero, List<String> logs) {
        // Apply corruption - DoT that increases each turn
        int corruptionDamage = 10; // Starting damage
        Map<String, Double> corruptionEffects = new HashMap<>();
        corruptionEffects.put("damage_per_turn", (double) corruptionDamage);
        corruptionEffects.put("increasing", 1.0); // Flag to increase damage each turn (would need special handling)

        if (hero.getDebuffuriActive().containsKey("corruption")) {
            // Stack increases the damage
            hero.getDebuffuriActive().get("corruption").addStack(5);
        } else {
            hero.getDebuffuriActive().put("corruption", new com.rpg.model.effects.DebuffStack(corruptionEffects, 5, 3));
        }
        logs.add("☣️ CORRUPTED! Hero takes " + corruptionDamage + " corruption damage per turn (increases with stacks)!");
    }

    /**
     * 🧠 SMART AI: Chooses the best ability to use based on battle situation.
     * Considers enemy HP, hero HP, archetype behavior, and tactical priorities.
     */
    private com.rpg.model.enemies.EnemyAbility chooseSmartAbility(Inamic enemy, Erou hero) {
        com.rpg.model.enemies.EnemyArchetype archetype = enemy.getArchetype();
        double enemyHPPercent = (double) enemy.getViata() / enemy.getViataMaxima();
        double heroHPPercent = (double) hero.getViata() / hero.getViataMaxima();

        List<com.rpg.model.enemies.EnemyAbility> availableAbilities = new ArrayList<>();
        for (com.rpg.model.enemies.EnemyAbility ability : enemy.getAbilities()) {
            if (enemy.isAbilityReady(ability)) {
                availableAbilities.add(ability);
            }
        }

        if (availableAbilities.isEmpty()) {
            return null; // No abilities ready
        }

        // ==================== PRIORITY 1: SURVIVAL (when low HP) ====================

        // If enemy is low HP and has healing, prioritize healing
        if (archetype != null && enemyHPPercent <= archetype.getHealingThreshold()) {
            if (availableAbilities.contains(com.rpg.model.enemies.EnemyAbility.DESPERATE_HEAL)) {
                return com.rpg.model.enemies.EnemyAbility.DESPERATE_HEAL;
            }
            // If no healing but archetype prefers defense when low, use defensive ability
            if (archetype.prefersDefenseWhenLow()) {
                if (availableAbilities.contains(com.rpg.model.enemies.EnemyAbility.SHIELD_WALL)) {
                    return com.rpg.model.enemies.EnemyAbility.SHIELD_WALL;
                }
                if (availableAbilities.contains(com.rpg.model.enemies.EnemyAbility.EVASION)) {
                    return com.rpg.model.enemies.EnemyAbility.EVASION;
                }
            }
        }

        // ==================== PRIORITY 2: FINISH OFF WEAK HERO ====================

        // If hero is low HP and enemy can Execute, go for the kill
        if (heroHPPercent <= 0.30) {
            if (archetype != null && archetype.prefersExecuteWhenEnemyLow()) {
                if (availableAbilities.contains(com.rpg.model.enemies.EnemyAbility.EXECUTE)) {
                    return com.rpg.model.enemies.EnemyAbility.EXECUTE;
                }
            }
            // Even without Execute archetype preference, assassins/berserkers should go aggressive
            if (archetype == com.rpg.model.enemies.EnemyArchetype.ASSASSIN ||
                archetype == com.rpg.model.enemies.EnemyArchetype.BERSERKER) {
                // Use most damaging ability
                if (availableAbilities.contains(com.rpg.model.enemies.EnemyAbility.POWER_STRIKE)) {
                    return com.rpg.model.enemies.EnemyAbility.POWER_STRIKE;
                }
                if (availableAbilities.contains(com.rpg.model.enemies.EnemyAbility.BLOOD_FRENZY)) {
                    return com.rpg.model.enemies.EnemyAbility.BLOOD_FRENZY;
                }
            }
        }

        // ==================== PRIORITY 3: BUFF BEFORE ATTACKING ====================

        // If enemy is healthy and has no active buffs, consider buffing first
        if (enemyHPPercent > 0.50 && enemy.getAbilityDamageBuffTurns() == 0) {
            // Berserkers and Elite Guards love buffing
            if (archetype == com.rpg.model.enemies.EnemyArchetype.BERSERKER ||
                archetype == com.rpg.model.enemies.EnemyArchetype.ELITE_GUARD) {
                if (availableAbilities.contains(com.rpg.model.enemies.EnemyAbility.BATTLE_CRY)) {
                    return com.rpg.model.enemies.EnemyAbility.BATTLE_CRY;
                }
                if (availableAbilities.contains(com.rpg.model.enemies.EnemyAbility.ENRAGE)) {
                    return com.rpg.model.enemies.EnemyAbility.ENRAGE;
                }
            }
        }

        // ==================== PRIORITY 4: USE COMBOS ====================

        // If already buffed, use a strong offensive ability
        if (enemy.getAbilityDamageBuffTurns() > 0) {
            if (availableAbilities.contains(com.rpg.model.enemies.EnemyAbility.POWER_STRIKE)) {
                return com.rpg.model.enemies.EnemyAbility.POWER_STRIKE;
            }
            if (availableAbilities.contains(com.rpg.model.enemies.EnemyAbility.FIREBALL)) {
                return com.rpg.model.enemies.EnemyAbility.FIREBALL;
            }
        }

        // ==================== PRIORITY 5: ARCHETYPE-SPECIFIC BEHAVIOR ====================

        if (archetype != null) {
            switch (archetype) {
                case TANK:
                    // Tanks prefer defensive abilities
                    if (availableAbilities.contains(com.rpg.model.enemies.EnemyAbility.SHIELD_WALL)) {
                        return com.rpg.model.enemies.EnemyAbility.SHIELD_WALL;
                    }
                    break;

                case BERSERKER:
                    // Berserkers prefer high-damage abilities
                    if (availableAbilities.contains(com.rpg.model.enemies.EnemyAbility.BLOOD_FRENZY)) {
                        return com.rpg.model.enemies.EnemyAbility.BLOOD_FRENZY;
                    }
                    if (availableAbilities.contains(com.rpg.model.enemies.EnemyAbility.POWER_STRIKE)) {
                        return com.rpg.model.enemies.EnemyAbility.POWER_STRIKE;
                    }
                    break;

                case ASSASSIN:
                    // Assassins prefer Execute and Poison
                    if (availableAbilities.contains(com.rpg.model.enemies.EnemyAbility.EXECUTE)) {
                        return com.rpg.model.enemies.EnemyAbility.EXECUTE;
                    }
                    if (availableAbilities.contains(com.rpg.model.enemies.EnemyAbility.POISON_STRIKE)) {
                        return com.rpg.model.enemies.EnemyAbility.POISON_STRIKE;
                    }
                    break;

                case CASTER:
                    // Casters prefer spell abilities
                    if (availableAbilities.contains(com.rpg.model.enemies.EnemyAbility.FIREBALL)) {
                        return com.rpg.model.enemies.EnemyAbility.FIREBALL;
                    }
                    if (availableAbilities.contains(com.rpg.model.enemies.EnemyAbility.LIGHTNING_BOLT)) {
                        return com.rpg.model.enemies.EnemyAbility.LIGHTNING_BOLT;
                    }
                    break;

                case HEALER:
                    // Healers prefer staying alive
                    if (enemyHPPercent < 0.70 && availableAbilities.contains(com.rpg.model.enemies.EnemyAbility.DESPERATE_HEAL)) {
                        return com.rpg.model.enemies.EnemyAbility.DESPERATE_HEAL;
                    }
                    break;

                case TRICKSTER:
                    // Tricksters prefer evasion and unpredictability
                    if (availableAbilities.contains(com.rpg.model.enemies.EnemyAbility.EVASION)) {
                        return com.rpg.model.enemies.EnemyAbility.EVASION;
                    }
                    break;

                case ELITE_GUARD:
                    // Elite Guards use tactical choices based on situation
                    if (enemyHPPercent > 0.60 && availableAbilities.contains(com.rpg.model.enemies.EnemyAbility.BATTLE_CRY)) {
                        return com.rpg.model.enemies.EnemyAbility.BATTLE_CRY;
                    }
                    break;

                case SWARM:
                    // Swarm prefers basic attacks, rarely uses abilities
                    // Just pick random if forced to use one
                    break;
            }
        }

        // ==================== FALLBACK: RANDOM CHOICE ====================
        // If no tactical priority matched, choose random from available
        return RandomUtils.randomElement(availableAbilities);
    }
}