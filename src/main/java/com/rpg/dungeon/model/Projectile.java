package com.rpg.dungeon.model;

import java.io.Serializable;

/**
 * Represents a projectile fired by ranged enemies
 */
public class Projectile implements Serializable {
    private static final long serialVersionUID = 1L;

    private double x;
    private double y;
    private double velocityX;
    private double velocityY;
    private double radius = 6; // Collision radius
    private int damage;
    private boolean active = true;
    private EnemySprite source; // Enemy that fired this projectile

    public Projectile(double x, double y, double velocityX, double velocityY, int damage, EnemySprite source) {
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.damage = damage;
        this.source = source;
    }

    /**
     * Update projectile position
     */
    public void update() {
        x += velocityX;
        y += velocityY;
    }

    /**
     * Check if projectile hits a circle (player)
     */
    public boolean hitsCircle(double cx, double cy, double cRadius) {
        double distance = Math.sqrt(Math.pow(x - cx, 2) + Math.pow(y - cy, 2));
        return distance <= (radius + cRadius);
    }

    /**
     * Check if projectile hits a rectangle
     */
    public boolean hitsRectangle(double rx, double ry, double rWidth, double rHeight) {
        // Find closest point on rectangle to circle center
        double closestX = Math.max(rx, Math.min(x, rx + rWidth));
        double closestY = Math.max(ry, Math.min(y, ry + rHeight));

        // Calculate distance between circle center and closest point
        double distanceX = x - closestX;
        double distanceY = y - closestY;
        double distanceSquared = (distanceX * distanceX) + (distanceY * distanceY);

        return distanceSquared <= (radius * radius);
    }

    // Getters and setters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getRadius() { return radius; }
    public int getDamage() { return damage; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public EnemySprite getSource() { return source; }
}
