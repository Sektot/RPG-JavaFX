package com.rpg.service;

import com.rpg.dungeon.model.MultiBattleState;
import com.rpg.model.abilities.Abilitate;
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

        // Reset cooldown-uri
        resetAbilityCooldowns(hero);

        log("⚔️ MULTI-ENEMY BATTLE!");
        log(hero.getNume() + " vs " + battleState.getActiveEnemyCount() + " enemies!");

        if (battleState.getReinforcementQueueSize() > 0) {
            log("⚠️ " + battleState.getReinforcementQueueSize() + " reinforcements incoming!");
        }

        // Get first active enemy for initial display
        Inamic firstEnemy = battleState.getActiveEnemies().get(0);

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
            logs.add("✅ " + enemy.getNume() + " a fost învins!");
            return finalizeBattle(hero, enemy, true, logs);
        }

        // Tura inamicului
        logs.add("");
        logs.add(enemy.getNume() + " contraatacă!");

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
        }

        // ✅ UPDATE BUFFS/DEBUFFS AT TURN END
        hero.aplicaEfecteleBuffurilor();

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

            // Remove from battle state
            for (int i = 0; i < MultiBattleState.MAX_ACTIVE_ENEMIES; i++) {
                MultiBattleState.BattleSlot slot = multiBattleState.getSlot(i);
                if (slot.getEnemy() == targetEnemy) {
                    multiBattleState.removeEnemy(i);
                    break;
                }
            }

            // Check if battle is over
            if (multiBattleState.getActiveEnemyCount() == 0 && multiBattleState.getReinforcementQueueSize() == 0) {
                logs.add("🎉 All enemies defeated!");
                return finalizeMultiBattle(hero, true, logs);
            }
        }

        // All alive enemies attack
        logs.add("");
        List<Inamic> aliveEnemies = multiBattleState.getActiveEnemies();

        for (Inamic enemy : aliveEnemies) {
            if (!enemy.esteViu()) continue;

            logs.add(enemy.getNume() + " contraatacă!");

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
            }

            if (!hero.esteViu()) {
                logs.add("💀 " + hero.getNume() + " a fost învins!");
                return finalizeMultiBattle(hero, false, logs);
            }
        }

        // Turn end effects
        hero.aplicaEfecteleBuffurilor();

        int regenAmount = hero.getRunItemRegenPerTurn();
        if (regenAmount > 0 && hero.esteViu()) {
            hero.vindeca(regenAmount);
            logs.add("💚 Regenerare: +" + regenAmount + " HP!");
        }

        // Return current state (use first alive enemy for display)
        Inamic displayEnemy = aliveEnemies.isEmpty() ? targetEnemy : aliveEnemies.get(0);

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
     * Execută o abilitate
     */
    public AbilityDTO.BattleTurnResultDTO executeAbility(Erou hero, Inamic enemy, String abilityName) {
        if (!battleActive) {
            return new AbilityDTO.BattleTurnResultDTO(false, "Bătălia nu este activă!", false, null);
        }

        Abilitate abilitate = findAbility(hero, abilityName);

        if (abilitate == null) {
            return new AbilityDTO.BattleTurnResultDTO(false, "Abilitatea nu a fost găsită!", false, null);
        }

        if (!abilitate.poateFiFolosita()) {
            return new AbilityDTO.BattleTurnResultDTO(
                    false,
                    "Abilitatea este în cooldown! Încă " + abilitate.getCooldownRamasa() + " ture.",
                    false,
                    null
            );
        }

        if (hero.getResursaCurenta() < abilitate.getCostMana()) {
            return new AbilityDTO.BattleTurnResultDTO(
                    false,
                    "Nu ai destul " + hero.getTipResursa() + "! Necesar: " + abilitate.getCostMana(),
                    false,
                    null
            );
        }

        turnCount++;
        List<String> logs = new ArrayList<>();

        logs.add("╔═══ Tura " + turnCount + " ═══");
        logs.add("✨ " + hero.getNume() + " folosește " + abilitate.getNume() + "!");

        // Folosește abilitatea
        hero.consumaResursa(abilitate.getCostMana());
        abilitate.aplicaCooldown();

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

        int abilityDamage = abilitate.calculeazaDamage(statsMap);

        // Only deal damage if ability has damage (some abilities are pure buffs)
        if (abilityDamage > 0) {
            // ✅ HIT CHANCE CHECK WITH BONUS
            double hitChance = hero.getHitChance() + abilitate.getHitChanceBonus();
            if (!RandomUtils.chancePercent(hitChance)) {
                logs.add("❌ Abilitatea ratează ținta!");
            } else {
                // ✅ CRITICAL HIT CHECK
                double critChance = hero.getCritChanceTotal();
                boolean isCrit = RandomUtils.chancePercent(critChance);

                if (isCrit) {
                    abilityDamage = (int) (abilityDamage * hero.getCritMultiplierTotal());
                    logs.add("⚡ CRITICAL HIT!");
                }

                int actualDamage = enemy.primesteDamage(abilityDamage);
                logs.add("💥 " + enemy.getNume() + " primește " + actualDamage + " damage!" + (isCrit ? " (CRIT)" : ""));

                // ✅ LIFESTEAL from run items + talent tree
                double lifestealPercent = hero.getLifestealTotal();
                if (lifestealPercent > 0) {
                    int healAmount = (int) (actualDamage * lifestealPercent);
                    if (healAmount > 0) {
                        hero.vindeca(healAmount);
                        logs.add("🩸 Lifesteal: +" + healAmount + " HP!");
                    }
                }

                // ✅ APPLY DEBUFF TO ENEMY IF ABILITY HAS DEBUFF
                if (abilitate.getDebuffAplicat() != null && abilitate.getDurataDebuff() > 0) {
                    Map<String, Double> debuffMods = new HashMap<>();
                    if (abilitate.getDamageDebuff() > 0) {
                        debuffMods.put("damage_over_time", (double) abilitate.getDamageDebuff());
                    }
                    // Note: Inamic doesn't have aplicaDebuff method, but we log it
                    logs.add("🔥 Debuff aplicat pe inamic: " + abilitate.getDebuffAplicat() + " pentru " + abilitate.getDurataDebuff() + " ture!");
                }
            }
        }

        // Verifică dacă inamicul a murit
        if (!enemy.esteViu()) {
            logs.add("✅ " + enemy.getNume() + " a fost învins!");
            return finalizeBattle(hero, enemy, true, logs);
        }

        // Tura inamicului (dacă eroul nu a omorât inamicul)
        logs.add("");
        logs.add(enemy.getNume() + " contraatacă!");

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
        }

        // ✅ UPDATE BUFFS/DEBUFFS AT TURN END
        hero.aplicaEfecteleBuffurilor();

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

            // Collect rewards from all slots
            for (MultiBattleState.BattleSlot slot : multiBattleState.getSlots()) {
                if (slot.getEnemy() != null) {
                    Inamic enemy = slot.getEnemy();
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
            }

            // Roll for jewel if no boss (regular jewel drop)
            if (jewelDrop == null) {
                int avgLevel = 1;
                int enemyCount = 0;
                for (MultiBattleState.BattleSlot slot : multiBattleState.getSlots()) {
                    if (slot.getEnemy() != null) {
                        avgLevel += slot.getEnemy().getNivel();
                        enemyCount++;
                    }
                }
                if (enemyCount > 0) {
                    avgLevel = avgLevel / enemyCount;
                    jewelDrop = LootGenerator.rollRegularJewelDrop(avgLevel);
                }
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
    protected List<AbilityDTO> getAvailableAbilities(Erou hero) {
        List<AbilityDTO> abilities = new ArrayList<>();

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

        return abilities;
    }
    // 🔧 FIX 3: Înlocuiește metoda findAbility()
    private Abilitate findAbility(Erou hero, String abilityName) {
        for (Abilitate abilitate : hero.getAbilitati()) {
            if (abilitate.getNume().equals(abilityName)) {
                return abilitate;
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
}