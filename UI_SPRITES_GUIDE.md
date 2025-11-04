# 🎨 UI Sprites & Textures Guide

This guide explains how to add custom textures and sprites to all UI menus in the game.

## 📂 Directory Structure

All UI sprites should be placed in the `resources/sprites/ui/` directory with the following structure:

```
resources/
└── sprites/
    └── ui/
        ├── main_menu/
        ├── town_menu/
        ├── shop/
        ├── character_sheet/
        ├── battle/
        ├── dungeon/
        └── common/
```

## 🖼️ Supported Formats

- **PNG** (recommended - supports transparency)
- **JPG** (for backgrounds without transparency)
- **GIF** (animated textures - experimental)

## 📋 Complete Sprite List

### 1. Main Menu (`ui/main_menu/`)

| File Name | Purpose | Recommended Size | Required |
|-----------|---------|-----------------|----------|
| `background.png` | Main menu background | 800x600+ | No |
| `button_normal.png` | Normal button state | 250x50 | No |
| `button_hover.png` | Hover button state | 250x50 | No |
| `title_banner.png` | Title decoration | Flexible | No |
| `frame.png` | Menu frame/border | Flexible | No |

**Button Textures Apply To:**
- New Game button
- Load Game button
- Options button
- Exit button

**Fallback Behavior:**
- If textures missing → uses default solid color buttons (#333 normal, #555 hover)
- If background missing → uses fallback hardcoded image (`/com/garaDeNord.png`)

---

### 2. Town Menu (`ui/town_menu/`)

| File Name | Purpose | Recommended Size | Required |
|-----------|---------|-----------------|----------|
| `background.png` | Town menu background | 900x700+ | No |
| `header_bg.png` | Header background | 900x100 | No |
| `menu_bg.png` | Menu panel background | Flexible | No |
| `panel_frame.png` | Panel frame decoration | Flexible | No |

**Button Textures:**

| File Name | Purpose | Size |
|-----------|---------|------|
| `button_dungeon.png` / `button_dungeon_hover.png` | Enter Dungeon | 250x50 |
| `button_shop.png` / `button_shop_hover.png` | Shop | 250x50 |
| `button_smith.png` / `button_smith_hover.png` | Smith | 250x50 |
| `button_alchemy.png` / `button_alchemy_hover.png` | Alchemy | 250x50 |
| `button_tavern.png` / `button_tavern_hover.png` | Tavern | 250x50 |
| `button_character.png` / `button_character_hover.png` | Character | 250x50 |
| `button_save.png` / `button_save_hover.png` | Save Game | 250x50 |
| `button_options.png` / `button_options_hover.png` | Options | 250x50 |
| `button_exit.png` / `button_exit_hover.png` | Exit to Main Menu | 250x50 |

**Fallback Behavior:**
- Background → solid color (#2c3e50)
- Buttons → colored buttons with text (various colors)

---

### 3. Shop (`ui/shop/`)

| File Name | Purpose | Recommended Size | Required |
|-----------|---------|-----------------|----------|
| `background.png` | Shop background | 1000x700+ | No |
| `header_bg.png` | Header background | 1000x100 | No |
| `panel_bg.png` | Panel backgrounds | Flexible | No |
| `button_buy.png` / `button_buy_hover.png` | Buy button | 200x50 | No |
| `button_back.png` / `button_back_hover.png` | Back button | 150x50 | No |
| `item_frame.png` | Item display frame | Flexible | No |

**Fallback Behavior:**
- Background → solid color (#2c3e50)
- Buttons → default styled buttons

---

### 4. Character Sheet (`ui/character_sheet/`)

*Note: Character Sheet currently uses programmatic UI. Texture support can be added following the same pattern.*

Suggested textures:
- `background.png` - Character sheet background
- `panel_equipment.png` - Equipment panel background
- `panel_stats.png` - Stats panel background
- `slot_weapon.png` - Weapon slot frame
- `slot_armor.png` - Armor slot frame
- `slot_ring.png` - Ring slot frame
- etc.

---

### 5. Battle UI (`ui/battle/`)

*Note: Battle UI texture support can be added following the same pattern.*

Suggested textures:
- `background.png` - Battle background
- `action_panel.png` - Action button panel
- `button_attack.png` / `button_attack_hover.png`
- `button_skill.png` / `button_skill_hover.png`
- `button_item.png` / `button_item_hover.png`
- `button_flee.png` / `button_flee_hover.png`
- `health_bar_bg.png`
- `health_bar_fill.png`
- `mana_bar_bg.png`
- `mana_bar_fill.png`

---

### 6. Common UI Elements (`ui/common/`)

Reusable textures across multiple screens:
- `button_generic.png` / `button_generic_hover.png` - Generic button
- `panel_generic.png` - Generic panel background
- `frame_generic.png` - Generic frame decoration
- `separator.png` - Visual separator line
- `icon_gold.png` - Gold icon
- `icon_health.png` - Health icon
- `icon_mana.png` - Mana icon

---

## 🔧 How It Works

### SpriteManager

The game uses `SpriteManager` to load and cache all sprites:

```java
Image sprite = SpriteManager.getSprite("ui/main_menu", "background");
```

**Features:**
- ✅ Automatic caching (loads once, reuses)
- ✅ Returns `null` if sprite not found (graceful fallback)
- ✅ Supports PNG, JPG, GIF
- ✅ Logs loading status to console

**Usage Pattern:**
```java
// Load texture
Image buttonTexture = SpriteManager.getSprite("ui/town_menu", "button_shop");

// Check if loaded
if (buttonTexture != null) {
    // Use texture
} else {
    // Use fallback styling
}
```

---

## 📐 Design Guidelines

### Button Textures

**Recommended Approach:**
1. Create a 250x50px button texture
2. Use 9-slice scaling if you want the button to stretch
3. Include text in the texture OR leave room for overlay text
4. Create separate normal/hover states for visual feedback

**Example Button States:**
- Normal: Subtle shadow, neutral colors
- Hover: Brighter, glowing effect, lifted appearance

### Background Textures

**Best Practices:**
- Use high resolution (at least screen size)
- Backgrounds will stretch to fill screen
- Use `preserveRatio: false` → image fills entire screen
- Consider performance (large images = slower load)

**Recommended Sizes:**
- Main Menu: 1920x1080 (scales down)
- Town Menu: 1920x1080
- Shop: 1920x1080
- Battle: 1920x1080

### Panel Textures

**Design Tips:**
- Use semi-transparent backgrounds for panels
- Include decorative borders/frames
- Consider contrast with text (white text needs dark bg)
- Test with different content lengths

---

## 🎨 Example Workflow

### Adding Main Menu Background

1. **Create/Obtain Texture:**
   - Create a 1920x1080 background image
   - Save as `background.png`

2. **Place in Correct Directory:**
   ```
   resources/sprites/ui/main_menu/background.png
   ```

3. **Run Game:**
   - SpriteManager automatically loads it
   - Console shows: `✅ Loaded sprite: /sprites/ui/main_menu/background.png`
   - If missing: `⚠️ Sprite not found: ui/main_menu/background`

4. **Verify:**
   - Main menu now uses your custom background!

### Adding Button Textures

1. **Create Two Button States:**
   - `button_normal.png` (250x50)
   - `button_hover.png` (250x50)

2. **Place in Directory:**
   ```
   resources/sprites/ui/main_menu/button_normal.png
   resources/sprites/ui/main_menu/button_hover.png
   ```

3. **Run Game:**
   - All main menu buttons now use your textures
   - Hover effect automatically switches between normal/hover

---

## 🐛 Troubleshooting

### Sprite Not Loading

**Console shows: ⚠️ Sprite not found**

1. Check file path:
   - ✅ Correct: `resources/sprites/ui/main_menu/background.png`
   - ❌ Wrong: `resources/ui/main_menu/background.png`

2. Check file extension:
   - Must be `.png`, `.jpg`, or `.gif`
   - File names are case-sensitive on some systems

3. Check build:
   - Maven: resources should be copied to target automatically
   - If using IDE: ensure resources folder is marked as "Resources Root"

### Texture Looks Stretched/Distorted

1. **Background Images:**
   - Use `preserveRatio: false` (already set)
   - Create image at target aspect ratio (16:9 recommended)

2. **Button Images:**
   - Create at exact size (250x50)
   - Or use 9-slice scaling (advanced)

### Performance Issues

1. **Reduce Image Size:**
   - Use JPG for backgrounds (smaller than PNG)
   - Optimize PNGs (use tools like TinyPNG)

2. **Cache Cleared:**
   - SpriteManager caches automatically
   - Clear cache: `SpriteManager.clearCache()` (dev only)

---

## 📊 Current Implementation Status

| UI Screen | Texture Support | Status |
|-----------|----------------|---------|
| Main Menu | ✅ Full | Complete |
| Town Menu | ✅ Full | Complete |
| Shop | ✅ Full | Complete |
| Character Sheet | ⚠️ Partial | Uses graphics but no texture loader yet |
| Battle | ❌ None | Programmatic UI only |
| Dungeon | ✅ Full | Complete (tiles, enemies, objects) |
| Smith | ⚠️ Can add | Follow shop pattern |
| Alchemy | ⚠️ Can add | Follow shop pattern |
| Tavern | ⚠️ Can add | Follow shop pattern |

---

## 🚀 Adding Texture Support to New Screens

To add texture support to any new UI screen, follow this pattern:

### 1. Add SpriteManager Import
```java
import com.rpg.utils.SpriteManager;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
```

### 2. Declare Texture Fields
```java
private Image backgroundTexture;
private Image buttonNormalTexture;
private Image buttonHoverTexture;
```

### 3. Load Textures in Constructor
```java
private void loadTextures() {
    backgroundTexture = SpriteManager.getSprite("ui/your_screen", "background");
    buttonNormalTexture = SpriteManager.getSprite("ui/your_screen", "button_normal");
    buttonHoverTexture = SpriteManager.getSprite("ui/your_screen", "button_hover");
}
```

### 4. Use Textures with Fallback
```java
if (backgroundTexture != null) {
    ImageView bg = new ImageView(backgroundTexture);
    bg.setPreserveRatio(false);
    // Use texture
} else {
    // Use fallback color
    root.setStyle("-fx-background-color: #2c3e50;");
}
```

### 5. Document in Class Javadoc
```java
/**
 * 🎨 SPRITE SUPPORT:
 * Place UI sprites in: resources/sprites/ui/your_screen/
 * - background.png - Screen background
 * - button_normal.png - Normal button
 * - button_hover.png - Hover button
 */
```

---

## 📝 Notes

- All textures are **optional** - the game works without any custom sprites
- Textures automatically override default styling when present
- SpriteManager logs all loading attempts for easy debugging
- Textures are cached for performance (loaded once per game session)
- No code changes needed to add/remove textures - just add/remove files!

---

## 🎯 Quick Start

**Want to quickly test the system?**

1. Create a simple 800x600 PNG background
2. Save as: `resources/sprites/ui/main_menu/background.png`
3. Run the game
4. Check console for: `✅ Loaded sprite: /sprites/ui/main_menu/background.png`
5. See your background on the main menu!

That's it! The system handles everything else automatically. 🎉
