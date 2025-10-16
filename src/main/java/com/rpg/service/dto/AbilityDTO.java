package com.rpg.service.dto;

import com.rpg.model.items.ObiectEchipament;

import java.util.List;

/**
 * Date despre o abilitate pentru UI
 */
public class AbilityDTO {
    private final String name;
    private final String description;
    private final int cost;
    private final int cooldown;
    private final boolean canUse; // Poate fi folosită (nu e în cooldown)
    private final boolean hasResource; // Are destulă resursă

    public AbilityDTO(String name, String description, int cost,
                      int cooldown, boolean canUse, boolean hasResource) {
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.cooldown = cooldown;
        this.canUse = canUse;
        this.hasResource = hasResource;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getCost() {
        return cost;
    }

    public int getCooldown() {
        return cooldown;
    }

    public boolean canUse() {
        return canUse;
    }

    public boolean hasResource() {
        return hasResource;
    }

    public boolean isAvailable() {
        return canUse && hasResource;
    }

    public String getDisplayName() {
        if (!canUse) {
            return name + " (Cooldown: " + cooldown + ")";
        } else if (!hasResource) {
            return name + " (Cost: " + cost + ")";
        }
        return name;
    }

    /**
     * Rezultatul odihnei la tavernă
     */
    public static class RestResultDTO {
        private final boolean success;
        private final String message;
        private final int goldSpent;
        private final int hpRecovered;
        private final int resourceRecovered;

        public RestResultDTO(boolean success, String message, int goldSpent,
                             int hpRecovered, int resourceRecovered) {
            this.success = success;
            this.message = message;
            this.goldSpent = goldSpent;
            this.hpRecovered = hpRecovered;
            this.resourceRecovered = resourceRecovered;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public int getGoldSpent() { return goldSpent; }
        public int getHpRecovered() { return hpRecovered; }
        public int getResourceRecovered() { return resourceRecovered; }
    }

    /**
     * Rezultatul final al bătăliei
     */
    public static class BattleResultDTO {
        private final boolean victory;
        private final String message;
        private final int goldEarned;
        private final int experienceEarned;
        private final List<ObiectEchipament> loot;
        private final int shaormaReward;

        public BattleResultDTO(boolean victory, String message, int goldEarned,
                               int experienceEarned, List<ObiectEchipament> loot,
                               int shaormaReward) {
            this.victory = victory;
            this.message = message;
            this.goldEarned = goldEarned;
            this.experienceEarned = experienceEarned;
            this.loot = loot;
            this.shaormaReward = shaormaReward;
        }

        public boolean isVictory() { return victory; }
        public String getMessage() { return message; }
        public int getGoldEarned() { return goldEarned; }
        public int getExperienceEarned() { return experienceEarned; }
        public List<ObiectEchipament> getLoot() { return loot; }
        public int getShaormaReward() { return shaormaReward; }
        public boolean hasLoot() { return loot != null && !loot.isEmpty(); }
    }

    /**
     * Starea curentă a bătăliei
     */
    public static class BattleStateDTO {
        private final int heroHP;
        private final int heroMaxHP;
        private final int heroResource;
        private final int heroMaxResource;
        private final int enemyHP;
        private final int enemyMaxHP;
        private final List<AbilityDTO> abilities;

        public BattleStateDTO(int heroHP, int heroMaxHP, int heroResource, int heroMaxResource,
                              int enemyHP, int enemyMaxHP, List<AbilityDTO> abilities) {
            this.heroHP = heroHP;
            this.heroMaxHP = heroMaxHP;
            this.heroResource = heroResource;
            this.heroMaxResource = heroMaxResource;
            this.enemyHP = enemyHP;
            this.enemyMaxHP = enemyMaxHP;
            this.abilities = abilities;
        }

        public int getHeroHP() { return heroHP; }
        public int getHeroMaxHP() { return heroMaxHP; }
        public int getHeroResource() { return heroResource; }
        public int getHeroMaxResource() { return heroMaxResource; }
        public int getEnemyHP() { return enemyHP; }
        public int getEnemyMaxHP() { return enemyMaxHP; }
        public List<AbilityDTO> getAbilities() { return abilities; }
    }

    /**
     * Rezultatul unei ture de luptă
     */
    public static class BattleTurnResultDTO {
        private final boolean success;
        private final String log;
        private final boolean fled; // True dacă a fugit cu succes
        private final BattleStateDTO currentState; // Starea curentă (null dacă s-a terminat)
        private final BattleResultDTO finalResult; // Rezultat final (null dacă continuă)

        public BattleTurnResultDTO(boolean success, String log, boolean fled,
                                   BattleStateDTO currentState) {
            this.success = success;
            this.log = log;
            this.fled = fled;
            this.currentState = currentState;
            this.finalResult = null;
        }

        public BattleTurnResultDTO(boolean success, String log, boolean fled,
                                   BattleStateDTO currentState, BattleResultDTO finalResult) {
            this.success = success;
            this.log = log;
            this.fled = fled;
            this.currentState = currentState;
            this.finalResult = finalResult;
        }

        public boolean isSuccess() { return success; }
        public String getLog() { return log; }
        public boolean hasFled() { return fled; }
        public BattleStateDTO getCurrentState() { return currentState; }
        public BattleResultDTO getFinalResult() { return finalResult; }
        public boolean isBattleOver() { return finalResult != null || fled; }
    }
}
