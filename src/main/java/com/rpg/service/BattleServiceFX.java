package com.rpg.service;

import com.rpg.model.abilities.Abilitate;
import com.rpg.model.characters.Erou;
import com.rpg.model.characters.Inamic;
import com.rpg.service.dto.AbilityDTO;
import com.rpg.service.dto.BattleInitDTO;

import java.util.*;

/**
 * BattleServiceFX - Sistem de luptă refactorizat pentru JavaFX
 * Permite control granular al fiecărei ture
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

    public BattleServiceFX() {
        this.battleActive = false;
        this.turnCount = 0;
    }

    public void setListener(BattleListener listener) {
        this.listener = listener;
    }

    /**
     * Inițializează o nouă bătălie
     */
    public BattleInitDTO initializeBattle(Erou hero, Inamic enemy) {
        battleActive = true;
        turnCount = 0;

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
     * Execută atacul normal al eroului
     */
    public AbilityDTO.BattleTurnResultDTO executeNormalAttack(Erou hero, Inamic enemy) {
        if (!battleActive) {
            return new AbilityDTO.BattleTurnResultDTO(false, "Bătălia nu este activă!", false, null);
        }

        turnCount++;
        List<String> logs = new ArrayList<>();

        // Tura eroului
        logs.add("━━━ Tura " + turnCount + " ━━━");
        logs.add(hero.getNume() + " atacă!");

        int damage = hero.calculeazaDamage();
        int actualDamage = enemy.primesteDamage(damage);

        logs.add("💥 " + hero.getNume() + " face " + actualDamage + " damage!");

        // Verifică dacă inamicul a murit
        if (!enemy.esteViu()) {
            logs.add("✅ " + enemy.getNume() + " a fost învins!");
            return finalizeBattle(hero, enemy, true, logs);
        }

        // Tura inamicului
        logs.add("");
        logs.add(enemy.getNume() + " contraatacă!");

        int enemyDamage = enemy.calculeazaDamage();
        int actualEnemyDamage = hero.primesteDamage(enemyDamage);

        logs.add("💥 " + enemy.getNume() + " face " + actualEnemyDamage + " damage!");

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

        // Calculează damage-ul abilității
        Map<String, Integer> statsMap = new HashMap<>();
        statsMap.put("strength", hero.getStrengthTotal());
        statsMap.put("dexterity", hero.getDexterityTotal());
        statsMap.put("intelligence", hero.getIntelligenceTotal());

        int abilityDamage = abilitate.calculeazaDamage(statsMap);
        int actualDamage = enemy.primesteDamage(abilityDamage);

        logs.add("💥 " + enemy.getNume() + " primește " + actualDamage + " damage!");

        // Verifică dacă inamicul a murit
        if (!enemy.esteViu()) {
            logs.add("✅ " + enemy.getNume() + " a fost învins!");
            return finalizeBattle(hero, enemy, true, logs);
        }

        // Aplică debuff dacă există
        if (abilitate.getDebuffAplicat() != null) {
            logs.add("🔥 Debuff aplicat: " + abilitate.getDebuffAplicat());
            // Aici poți adăuga logica pentru aplicarea debuff-ului pe inamic
            // enemy.aplicaDebuff(...) dacă există metoda
        }

        // Tura inamicului (dacă eroul nu a omorât inamicul)
        logs.add("");
        logs.add(enemy.getNume() + " contraatacă!");

        int enemyDamage = enemy.calculeazaDamage();
        int actualEnemyDamage = hero.primesteDamage(enemyDamage);

        logs.add("💥 " + enemy.getNume() + " face " + actualEnemyDamage + " damage!");

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

    private AbilityDTO.BattleTurnResultDTO finalizeBattle(Erou hero, Inamic enemy, boolean victory, List<String> logs) {
        battleActive = false;

        AbilityDTO.BattleResultDTO result = new AbilityDTO.BattleResultDTO(
                victory,
                victory ? "Victorie!" : "Înfrângere!",
                enemy.getGoldReward(),
                enemy.getExpReward(),
                enemy.getLoot(),
                enemy.isBoss() ? enemy.getShaormaReward() : 0
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
}