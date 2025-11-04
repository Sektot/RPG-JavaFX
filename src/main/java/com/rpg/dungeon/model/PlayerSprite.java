package com.rpg.dungeon.model;

import java.io.Serializable;

/**
 * Represents the player's sprite and position in a room
 */
public class PlayerSprite implements Serializable {
    private static final long serialVersionUID = 1L;

    private double x;
    private double y;
    private double width = 64;  // Increased from 32 for better visibility
    private double height = 64; // Increased from 32 for better visibility
    private double speed = 3.0; // Pixels per frame

    private Direction facing = Direction.SOUTH; // Current facing direction
    private boolean moving = false;

    // Movement state
    private boolean movingUp = false;
    private boolean movingDown = false;
    private boolean movingLeft = false;
    private boolean movingRight = false;

    public PlayerSprite(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Update player position based on current movement state
     */
    public void update() {
        double dx = 0;
        double dy = 0;

        if (movingUp) dy -= speed;
        if (movingDown) dy += speed;
        if (movingLeft) dx -= speed;
        if (movingRight) dx += speed;

        // Normalize diagonal movement
        if (dx != 0 && dy != 0) {
            double length = Math.sqrt(dx * dx + dy * dy);
            dx = (dx / length) * speed;
            dy = (dy / length) * speed;
        }

        // Update position
        if (dx != 0 || dy != 0) {
            x += dx;
            y += dy;
            moving = true;

            // Update facing direction based on movement
            if (Math.abs(dx) > Math.abs(dy)) {
                facing = dx > 0 ? Direction.EAST : Direction.WEST;
            } else {
                facing = dy > 0 ? Direction.SOUTH : Direction.NORTH;
            }
        } else {
            moving = false;
        }
    }

    /**
     * Check collision with a rectangle
     */
    public boolean collidesWith(double rx, double ry, double rWidth, double rHeight) {
        return x < rx + rWidth &&
               x + width > rx &&
               y < ry + rHeight &&
               y + height > ry;
    }

    /**
     * Get center X position
     */
    public double getCenterX() {
        return x + width / 2;
    }

    /**
     * Get center Y position
     */
    public double getCenterY() {
        return y + height / 2;
    }

    /**
     * Check if player is in range of a position
     */
    public boolean isInRangeOf(double targetX, double targetY, double range) {
        double distance = Math.sqrt(
            Math.pow(getCenterX() - targetX, 2) +
            Math.pow(getCenterY() - targetY, 2)
        );
        return distance <= range;
    }

    // Movement controls
    public void setMovingUp(boolean moving) { this.movingUp = moving; }
    public void setMovingDown(boolean moving) { this.movingDown = moving; }
    public void setMovingLeft(boolean moving) { this.movingLeft = moving; }
    public void setMovingRight(boolean moving) { this.movingRight = moving; }

    // Getters and setters
    public double getX() { return x; }
    public double getY() { return y; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }
    public Direction getFacing() { return facing; }
    public boolean isMoving() { return moving; }
}
