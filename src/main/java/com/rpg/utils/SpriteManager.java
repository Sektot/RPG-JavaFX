package com.rpg.utils;

import javafx.scene.image.Image;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 🎨 Sprite Manager - Loads and caches sprites for the game
 *
 * USAGE:
 * 1. Place sprite files in: resources/sprites/
 * 2. Organize by category: player/, enemies/, objects/, tiles/
 * 3. Use getSprite(category, name) to load sprites
 *
 * AUTOMATIC FEATURES:
 * - Caches loaded sprites (loads once, reuses)
 * - Returns null if sprite not found (use fallback rendering)
 * - Supports PNG, JPG, GIF
 *
 * EXAMPLE STRUCTURE:
 * resources/
 *   sprites/
 *     player/
 *       player_down.png
 *       player_up.png
 *       player_left.png
 *       player_right.png
 *     enemies/
 *       enemy_basic.png
 *       enemy_boss.png
 *     objects/
 *       chest_closed.png
 *       chest_open.png
 *       altar.png
 *       campfire.png
 *       fountain.png
 *     tiles/
 *       floor.png
 *       wall.png
 *       door.png
 */
public class SpriteManager {

    private static final String SPRITE_BASE_PATH = "/sprites/";
    private static final Map<String, Image> spriteCache = new HashMap<>();

    /**
     * Load a sprite by category and name
     *
     * @param category Folder name (player, enemies, objects, tiles)
     * @param name File name without extension
     * @return Image or null if not found
     */
    public static Image getSprite(String category, String name) {
        String key = category + "/" + name;

        // Check cache first
        if (spriteCache.containsKey(key)) {
            return spriteCache.get(key);
        }

        // Try loading sprite
        Image sprite = loadSprite(category, name);

        // Cache result (even if null, so we don't keep trying to load missing sprites)
        spriteCache.put(key, sprite);

        return sprite;
    }

    /**
     * Load sprite from resources
     */
    private static Image loadSprite(String category, String name) {
        String[] extensions = {".png", ".jpg", ".gif"};

        for (String ext : extensions) {
            String path = SPRITE_BASE_PATH + category + "/" + name + ext;

            try {
                InputStream stream = SpriteManager.class.getResourceAsStream(path);
                if (stream != null) {
                    Image image = new Image(stream);
                    System.out.println("✅ Loaded sprite: " + path);
                    return image;
                }
            } catch (Exception e) {
                // Try next extension
            }
        }

        // Sprite not found
        System.out.println("⚠️ Sprite not found: " + category + "/" + name);
        return null;
    }

    /**
     * Check if a sprite exists
     */
    public static boolean hasSprite(String category, String name) {
        return getSprite(category, name) != null;
    }

    /**
     * Preload all sprites in a category (optional optimization)
     */
    public static void preloadCategory(String category, String... names) {
        for (String name : names) {
            getSprite(category, name);
        }
    }

    /**
     * Clear sprite cache (useful for hot-reloading sprites during development)
     */
    public static void clearCache() {
        spriteCache.clear();
        System.out.println("🗑️ Sprite cache cleared");
    }

    /**
     * Get cache statistics
     */
    public static String getCacheStats() {
        long loadedCount = spriteCache.values().stream().filter(img -> img != null).count();
        long missingCount = spriteCache.size() - loadedCount;
        return String.format("Sprites loaded: %d | Missing: %d", loadedCount, missingCount);
    }
}
