package com.rpg.service.dto;

/**
 * Informații despre odihnă
 */
public class RestInfoDTO {
    private final int cost;
    private final int hpToRecover;
    private final int resourceToRecover;
    private final boolean canAfford;
    private final boolean needsRest;

    public RestInfoDTO(int cost, int hpToRecover, int resourceToRecover,
                       boolean canAfford, boolean needsRest) {
        this.cost = cost;
        this.hpToRecover = hpToRecover;
        this.resourceToRecover = resourceToRecover;
        this.canAfford = canAfford;
        this.needsRest = needsRest;
    }

    public int getCost() {
        return cost;
    }

    public int getHpToRecover() {
        return hpToRecover;
    }

    public int getResourceToRecover() {
        return resourceToRecover;
    }

    public boolean canAfford() {
        return canAfford;
    }

    public boolean needsRest() {
        return needsRest;
    }
}
