package com.rpg.service.dto;

import java.util.List;

/**
 * Date inițiale pentru o bătălie
 */
public class BattleInitDTO {
    private final String heroName;
    private final int heroHP;
    private final int heroMaxHP;
    private final int heroResource;
    private final int heroMaxResource;
    private final String resourceType;
    private final String enemyName;
    private final int enemyHP;
    private final int enemyMaxHP;
    private final boolean isBoss;
    private final List<AbilityDTO> abilities;

    public BattleInitDTO(String heroName, int heroHP, int heroMaxHP,
                         int heroResource, int heroMaxResource, String resourceType,
                         String enemyName, int enemyHP, int enemyMaxHP,
                         boolean isBoss, List<AbilityDTO> abilities) {
        this.heroName = heroName;
        this.heroHP = heroHP;
        this.heroMaxHP = heroMaxHP;
        this.heroResource = heroResource;
        this.heroMaxResource = heroMaxResource;
        this.resourceType = resourceType;
        this.enemyName = enemyName;
        this.enemyHP = enemyHP;
        this.enemyMaxHP = enemyMaxHP;
        this.isBoss = isBoss;
        this.abilities = abilities;
    }

    // Getters
    public String getHeroName() { return heroName; }
    public int getHeroHP() { return heroHP; }
    public int getHeroMaxHP() { return heroMaxHP; }
    public int getHeroResource() { return heroResource; }
    public int getHeroMaxResource() { return heroMaxResource; }
    public String getResourceType() { return resourceType; }
    public String getEnemyName() { return enemyName; }
    public int getEnemyHP() { return enemyHP; }
    public int getEnemyMaxHP() { return enemyMaxHP; }
    public boolean isBoss() { return isBoss; }
    public List<AbilityDTO> getAbilities() { return abilities; }
}


