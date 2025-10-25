package com.rpg.utils;

import javafx.scene.image.Image;

/**
 * 🎬 Animated Sprite - Handles frame-based sprite animations
 *
 * USAGE:
 * AnimatedSprite playerAnim = new AnimatedSprite("player", "walk_down", 4, 0.15);
 * playerAnim.update(deltaTime);
 * Image currentFrame = playerAnim.getCurrentFrame();
 *
 * SPRITE NAMING CONVENTION:
 * player/walk_down_0.png
 * player/walk_down_1.png
 * player/walk_down_2.png
 * player/walk_down_3.png
 *
 * OR for single sprites:
 * player/idle_down.png (no frame number needed)
 */
public class AnimatedSprite {

    private String category;
    private String baseName;
    private int frameCount;
    private double frameDuration; // Seconds per frame
    private int currentFrame;
    private double frameTimer;
    private boolean loop;
    private Image[] frames;
    private Image fallbackImage;

    /**
     * Create an animated sprite
     *
     * @param category Sprite category (player, enemies, etc.)
     * @param baseName Base name (walk_down, attack, etc.)
     * @param frameCount Number of animation frames
     * @param frameDuration Seconds per frame (0.1 = 10 FPS)
     */
    public AnimatedSprite(String category, String baseName, int frameCount, double frameDuration) {
        this.category = category;
        this.baseName = baseName;
        this.frameCount = frameCount;
        this.frameDuration = frameDuration;
        this.currentFrame = 0;
        this.frameTimer = 0;
        this.loop = true;
        this.frames = new Image[frameCount];

        loadFrames();
    }

    /**
     * Create a static (non-animated) sprite
     */
    public AnimatedSprite(String category, String spriteName) {
        this(category, spriteName, 1, 0);
    }

    /**
     * Load all animation frames
     */
    private void loadFrames() {
        for (int i = 0; i < frameCount; i++) {
            String frameName = frameCount > 1 ? baseName + "_" + i : baseName;
            frames[i] = SpriteManager.getSprite(category, frameName);

            // Keep first valid frame as fallback
            if (frames[i] != null && fallbackImage == null) {
                fallbackImage = frames[i];
            }
        }

        // If no frames loaded, try loading just the base name (static sprite)
        if (fallbackImage == null) {
            fallbackImage = SpriteManager.getSprite(category, baseName);
        }
    }

    /**
     * Update animation (call every frame)
     *
     * @param deltaTime Time since last frame in seconds (typically 1/60 = 0.0167)
     */
    public void update(double deltaTime) {
        if (frameCount <= 1) return; // Static sprite

        frameTimer += deltaTime;

        if (frameTimer >= frameDuration) {
            frameTimer = 0;
            currentFrame++;

            if (currentFrame >= frameCount) {
                if (loop) {
                    currentFrame = 0;
                } else {
                    currentFrame = frameCount - 1; // Stay on last frame
                }
            }
        }
    }

    /**
     * Get current animation frame
     */
    public Image getCurrentFrame() {
        if (frames[currentFrame] != null) {
            return frames[currentFrame];
        }
        return fallbackImage; // Use fallback if current frame missing
    }

    /**
     * Check if sprite has valid frames loaded
     */
    public boolean hasSprite() {
        return fallbackImage != null;
    }

    /**
     * Reset animation to first frame
     */
    public void reset() {
        currentFrame = 0;
        frameTimer = 0;
    }

    /**
     * Set whether animation should loop
     */
    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    /**
     * Check if animation is finished (only relevant for non-looping animations)
     */
    public boolean isFinished() {
        return !loop && currentFrame >= frameCount - 1;
    }

    // Getters
    public int getCurrentFrameIndex() { return currentFrame; }
    public int getFrameCount() { return frameCount; }
}
