package com.rpg.dungeon.model;

import java.io.Serializable;

/**
 * Represents an environmental hazard in a dungeon room
 */
public class Hazard implements Serializable {
    private static final long serialVersionUID = 1L;

    private double x;
    private double y;
    private double width;
    private double height;
    private HazardType type;
    private int damage; // Deprecated - kept for backwards compatibility
    private double damagePercent; // Percentage of max HP to deal (0.0 to 1.0)
    private double tickRate; // Seconds between damage ticks (for continuous hazards)
    private double lastDamageTime; // Timestamp of last damage dealt
    private boolean isActive = true;
    private double invulnerabilityDuration = 1.5; // Seconds of invulnerability after being hit

    // Animation state
    private double animationProgress = 0;
    private boolean isPulsing = false;

    public Hazard(double x, double y, double width, double height, HazardType type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
        this.lastDamageTime = 0;

        // Set type-specific properties (percentage-based damage)
        switch (type) {
            case SPIKES -> {
                this.damagePercent = 0.15; // 15% of max HP
                this.tickRate = 0; // Instant damage on contact (but with invulnerability)
                this.invulnerabilityDuration = 1.5; // 1.5s immunity after hit
                this.damage = 15; // Fallback for old code
            }
            case FIRE_PIT -> {
                this.damagePercent = 0.08; // 8% of max HP per tick
                this.tickRate = 0.5; // Damage every 0.5 seconds
                this.invulnerabilityDuration = 1.0; // 1s immunity after hit
                this.damage = 8; // Fallback
            }
            case POISON_GAS -> {
                this.damagePercent = 0.06; // 6% of max HP per tick
                this.tickRate = 0.3; // Damage every 0.3 seconds
                this.invulnerabilityDuration = 0.8; // 0.8s immunity after hit
                this.damage = 5; // Fallback
            }
        }
    }

    /**
     * Check if player collides with this hazard
     */
    public boolean collidesWith(double px, double py, double pWidth, double pHeight) {
        return px < x + width &&
               px + pWidth > x &&
               py < y + height &&
               py + pHeight > y;
    }

    /**
     * Check if hazard can deal damage (based on tick rate)
     */
    public boolean canDealDamage(double currentTime) {
        if (!isActive) return false;

        // Instant damage hazards (like spikes) can always damage
        if (tickRate == 0) return true;

        // Continuous hazards check tick rate
        if (currentTime - lastDamageTime >= tickRate) {
            lastDamageTime = currentTime;
            return true;
        }

        return false;
    }

    /**
     * Update hazard animation state
     */
    public void update(double deltaTime) {
        animationProgress += deltaTime * 2; // Animation speed multiplier
        if (animationProgress > Math.PI * 2) {
            animationProgress -= Math.PI * 2;
        }
    }

    /**
     * Get current animation frame for pulsing effects
     */
    public double getPulseIntensity() {
        return (Math.sin(animationProgress) + 1) / 2; // Returns 0 to 1
    }

    // Getters and setters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public HazardType getType() { return type; }
    public int getDamage() { return damage; }
    public double getDamagePercent() { return damagePercent; }
    public double getInvulnerabilityDuration() { return invulnerabilityDuration; }
    public double getTickRate() { return tickRate; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public double getAnimationProgress() { return animationProgress; }

    /**
     * Types of environmental hazards
     */
    public enum HazardType {
        SPIKES,      // Instant damage on contact, retractable
        FIRE_PIT,    // Continuous fire damage while standing in it
        POISON_GAS   // Continuous poison damage, cloud-like area
    }
}
