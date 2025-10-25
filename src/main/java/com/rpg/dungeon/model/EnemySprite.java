package com.rpg.dungeon.model;

import com.rpg.model.characters.Inamic;

import java.io.Serializable;

/**
 * Represents an enemy sprite in a dungeon room with position and AI state
 */
public class EnemySprite implements Serializable {
    private static final long serialVersionUID = 1L;

    private Inamic enemy; // The actual enemy data
    private double x; // Position in pixels
    private double y;
    private double width = 32;
    private double height = 32;

    // AI State
    private EnemyState state = EnemyState.IDLE;
    private double battleStartX = -1; // Position when battle started (for flee mechanics)
    private double battleStartY = -1;
    private long chaseResumeTime = 0; // Timestamp when enemy can resume chasing after flee

    // Movement
    private double moveSpeed = 1.5; // Pixels per frame when chasing

    public EnemySprite(Inamic enemy, double x, double y) {
        this.enemy = enemy;
        this.x = x;
        this.y = y;
    }

    /**
     * Check if player is in vision range
     */
    public boolean canSeePlayer(double playerX, double playerY, double visionRange) {
        return getDistanceToPoint(playerX, playerY) <= visionRange;
    }

    /**
     * Check if player is in engagement range (battle starts)
     */
    public boolean canEngagePlayer(double playerX, double playerY, double engagementRange) {
        return getDistanceToPoint(playerX, playerY) <= engagementRange;
    }

    /**
     * Get distance to a point from enemy center
     */
    public double getDistanceToPoint(double px, double py) {
        double centerX = x + width / 2;
        double centerY = y + height / 2;
        return Math.sqrt(Math.pow(px - centerX, 2) + Math.pow(py - centerY, 2));
    }

    /**
     * Move towards target position (player chase logic)
     */
    public void moveTowards(double targetX, double targetY) {
        double centerX = x + width / 2;
        double centerY = y + height / 2;

        double dx = targetX - centerX;
        double dy = targetY - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > 1) { // Don't move if already at target
            double moveX = (dx / distance) * moveSpeed;
            double moveY = (dy / distance) * moveSpeed;

            x += moveX;
            y += moveY;
        }
    }

    /**
     * Save battle start position for flee mechanics
     */
    public void saveBattlePosition() {
        this.battleStartX = x;
        this.battleStartY = y;
    }

    /**
     * Return to battle start position
     */
    public void returnToBattlePosition() {
        if (battleStartX >= 0 && battleStartY >= 0) {
            this.x = battleStartX;
            this.y = battleStartY;
        }
    }

    /**
     * Set chase cooldown after player flees
     */
    public void setChaseCooldown(long milliseconds) {
        this.chaseResumeTime = System.currentTimeMillis() + milliseconds;
        this.state = EnemyState.COOLDOWN;
    }

    /**
     * Check if chase cooldown has expired
     */
    public boolean canChaseAgain() {
        if (state == EnemyState.COOLDOWN) {
            if (System.currentTimeMillis() >= chaseResumeTime) {
                state = EnemyState.IDLE;
                return true;
            }
            return false;
        }
        return true;
    }

    // Getters and setters
    public Inamic getEnemy() { return enemy; }
    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public EnemyState getState() { return state; }
    public void setState(EnemyState state) { this.state = state; }
    public double getMoveSpeed() { return moveSpeed; }
    public void setMoveSpeed(double moveSpeed) { this.moveSpeed = moveSpeed; }

    public double getCenterX() { return x + width / 2; }
    public double getCenterY() { return y + height / 2; }

    /**
     * Enemy AI states
     */
    public enum EnemyState {
        IDLE,       // Standing still, not aware of player
        CHASING,    // Following player (in vision range)
        IN_COMBAT,  // Currently in battle
        COOLDOWN,   // Cooldown after player fled
        DEFEATED    // Enemy is dead
    }
}
