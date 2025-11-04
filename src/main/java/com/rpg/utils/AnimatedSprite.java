package com.rpg.utils;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

/**
 * 🎬 Animated Sprite - Handles frame-based sprite animations
 *
 * USAGE - Individual Frames:
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
 * USAGE - Spritesheet:
 * AnimatedSprite playerAnim = AnimatedSprite.fromSpritesheet(
 *     "player", "walk_down_sheet", 8, 0.15, 64, 64, 8, 1
 * );
 *
 * This loads from a spritesheet with 8 frames of 64x64 pixels arranged in a single row
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
     * Private constructor for spritesheet-based animations
     */
    private AnimatedSprite(String category, String baseName, int frameCount, double frameDuration, Image[] preloadedFrames) {
        this.category = category;
        this.baseName = baseName;
        this.frameCount = frameCount;
        this.frameDuration = frameDuration;
        this.currentFrame = 0;
        this.frameTimer = 0;
        this.loop = true;
        this.frames = preloadedFrames;

        // Set fallback to first frame
        if (frames.length > 0 && frames[0] != null) {
            fallbackImage = frames[0];
        }
    }

    /**
     * Create an animated sprite from a spritesheet
     *
     * @param category Sprite category (player, enemies, etc.)
     * @param sheetName Name of the spritesheet file (e.g., "player_walk_sheet")
     * @param frameCount Number of frames to extract
     * @param frameDuration Seconds per frame (0.1 = 10 FPS)
     * @param frameWidth Width of each frame in pixels
     * @param frameHeight Height of each frame in pixels
     * @param columns Number of frames per row in the spritesheet
     * @param rows Number of rows in the spritesheet
     * @return AnimatedSprite instance with frames extracted from spritesheet
     */
    public static AnimatedSprite fromSpritesheet(String category, String sheetName,
                                                  int frameCount, double frameDuration,
                                                  int frameWidth, int frameHeight,
                                                  int columns, int rows) {
        // Load the spritesheet image
        Image spritesheet = SpriteManager.getSprite(category, sheetName);

        if (spritesheet == null) {
            System.err.println("⚠️ Spritesheet not found: " + category + "/" + sheetName);
            // Return empty animation with fallback
            Image[] emptyFrames = new Image[frameCount];
            return new AnimatedSprite(category, sheetName, frameCount, frameDuration, emptyFrames);
        }

        // Extract frames from spritesheet
        Image[] frames = extractFramesFromSheet(spritesheet, frameCount, frameWidth, frameHeight, columns, rows);

        return new AnimatedSprite(category, sheetName, frameCount, frameDuration, frames);
    }

    /**
     * Extract individual frames from a spritesheet
     */
    private static Image[] extractFramesFromSheet(Image spritesheet, int frameCount,
                                                   int frameWidth, int frameHeight,
                                                   int columns, int rows) {
        Image[] frames = new Image[frameCount];
        PixelReader reader = spritesheet.getPixelReader();

        if (reader == null) {
            System.err.println("⚠️ Cannot read spritesheet pixels");
            return frames;
        }

        int frameIndex = 0;
        for (int row = 0; row < rows && frameIndex < frameCount; row++) {
            for (int col = 0; col < columns && frameIndex < frameCount; col++) {
                int x = col * frameWidth;
                int y = row * frameHeight;

                // Extract frame
                WritableImage frame = new WritableImage(reader, x, y, frameWidth, frameHeight);
                frames[frameIndex] = frame;
                frameIndex++;
            }
        }

        System.out.println("✅ Extracted " + frameIndex + " frames from spritesheet");
        return frames;
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
