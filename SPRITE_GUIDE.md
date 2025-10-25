# 🎨 Sprite System Guide

Complete guide for adding sprites to your dungeon crawler game.

## 📁 Folder Structure

All sprites go in: `src/main/resources/sprites/`

```
resources/
└── sprites/
    ├── player/          # Player character animations
    ├── enemies/         # Enemy sprites
    ├── objects/         # Interactive objects (chests, altars, etc.)
    └── tiles/           # Floor, wall, and door tiles
```

Each folder contains a README.md with specific requirements.

## 🎬 How the Sprite System Works

### Automatic Loading
The game automatically loads sprites from the folders above. If a sprite is missing, the game uses colored placeholder graphics instead.

### Caching System
- Sprites are loaded once and cached for performance
- Missing sprites are also cached (to avoid repeated failed loads)
- Cache statistics are printed to console on startup

### Fallback Rendering
Every visual element has a fallback:
- **Player**: Blue circle with 🧙 emoji
- **Enemies**: Red circle with ⚔️ emoji
- **Objects**: Colored rectangles with emojis (📦, ⛩️, ⛲, etc.)
- **Tiles**: Solid colors (gray walls, dark gray floor)

This means **you can run the game right now without any sprites** and it will work fine!

## 📋 Required Sprites

### Player (32x32 pixels)
```
player/walk_down_0.png   → Frame 0 of walking down
player/walk_down_1.png   → Frame 1 of walking down
player/walk_down_2.png   → Frame 2 of walking down
player/walk_down_3.png   → Frame 3 of walking down
player/walk_up_0.png     → Walking up frames (4 total)
player/walk_up_1.png
player/walk_up_2.png
player/walk_up_3.png
player/walk_left_0.png   → Walking left frames (4 total)
player/walk_left_1.png
player/walk_left_2.png
player/walk_left_3.png
player/walk_right_0.png  → Walking right frames (4 total)
player/walk_right_1.png
player/walk_right_2.png
player/walk_right_3.png
```

### Enemies (32x32 to 64x64 pixels)
```
enemies/enemy_basic.png  → Default enemy sprite
```

### Objects (various sizes)
```
objects/chest.png        → Treasure chest (48x48)
objects/altar.png        → Shrine/altar (64x64)
objects/fountain.png     → Healing fountain (64x64)
objects/shop_table.png   → Shop counter (96x64)
objects/campfire.png     → Rest campfire (48x48)
objects/portal.png       → Portal/exit (64x64)
objects/statue.png       → Decorative statue (48x64)
```

### Tiles (64x64 pixels)
```
tiles/floor.png          → Floor tile (will tile across room)
tiles/wall.png           → Wall tile (will tile room borders)
tiles/door.png           → Door sprite (80x50)
```

## 🎮 Adding Your Sprites

### Step 1: Find Free Sprites
Great resources for free game sprites:
1. **Kenney.nl** - Free game assets (Dungeon Pack, Roguelike Pack)
2. **OpenGameArt.org** - Search "top-down dungeon" or "roguelike"
3. **itch.io** - Search "pixel art sprites" (many are free)

### Step 2: Organize Files
1. Download your sprite pack
2. Rename files to match the naming convention above
3. Place them in the appropriate `sprites/` subfolder

### Step 3: Test
1. Run the game
2. Enter a dungeon
3. Check the console for sprite loading messages:
   - `✅ Loaded sprite: /sprites/player/walk_down_0.png`
   - `⚠️ Sprite not found: /sprites/player/walk_up_0.png`
4. See cache statistics: `📊 Sprites loaded: 12 | Missing: 4`

### Step 4: Iterate
If sprites aren't loading:
- Check file names (must match exactly, case-sensitive)
- Check file format (PNG, JPG, or GIF only)
- Check file location (must be in `resources/sprites/`)
- Look for errors in console output

## 🛠️ Advanced Usage

### Animated Objects
You can animate objects by creating multiple frames:
```
objects/campfire_0.png
objects/campfire_1.png
objects/campfire_2.png
```

Then modify the code in `RoomExplorationController.loadSprites()` to load them as AnimatedSprite instead of static Image.

### Custom Enemy Types
Add new enemy sprite files:
```
enemies/enemy_boss.png
enemies/enemy_elite.png
enemies/enemy_swarm.png
```

Then update the enemy spawning code to use different sprites based on enemy type.

### Multiple Floor/Wall Variants
Create variations like:
```
tiles/floor_variant1.png
tiles/floor_variant2.png
tiles/wall_brick.png
tiles/wall_stone.png
```

Then modify `loadSprites()` to randomly select or use specific variants per room.

## 🎨 Sprite Design Tips

### Pixel Art Basics
- Use a limited color palette (8-16 colors)
- Keep it simple and readable at small sizes
- Add a 1-pixel outline for visibility
- Test sprites on dark and light backgrounds

### Animation Tips
- Use 4 frames for smooth walking
- Middle frames should show leg movement
- Keep the head/body relatively stable
- Frame duration: 0.1 seconds (10 FPS) works well

### Tiling Tiles
- Design edges to match seamlessly
- Test by placing 2x2 tiles together
- Use subtle variations to avoid monotony
- Keep consistent lighting direction

## 🔧 Technical Details

### SpriteManager.java
Located in: `src/main/java/com/rpg/utils/SpriteManager.java`
- Loads sprites from resources
- Caches loaded sprites in HashMap
- Auto-tries PNG, JPG, GIF extensions
- Returns null if sprite not found

### AnimatedSprite.java
Located in: `src/main/java/com/rpg/utils/AnimatedSprite.java`
- Handles frame-based animations
- Updates based on deltaTime (~60 FPS)
- Supports looping and non-looping
- Falls back to first valid frame if frame missing

### RoomExplorationController.java
Located in: `src/main/java/com/rpg/dungeon/controller/RoomExplorationController.java`
- Loads all sprites in `loadSprites()` method
- Renders sprites in `drawPlayer()`, `drawEnemy()`, `drawObjects()`, etc.
- Falls back to colored shapes if sprites missing

## 📝 File Naming Convention

**IMPORTANT**: File names are case-sensitive and must match exactly!

```
Format: {category}/{name}_{frame}.{ext}

Examples:
✅ player/walk_down_0.png
✅ enemies/enemy_basic.png
✅ objects/chest.png
✅ tiles/floor.png

❌ Player/Walk_Down_0.png      (wrong case)
❌ player/walk-down-0.png      (wrong separator)
❌ player/walkdown0.png        (missing underscores)
❌ enemy_basic.png             (wrong folder)
```

## 🚀 Quick Start

Want to test the system quickly? Here's the minimum viable sprite set:

1. **One player sprite**: Just use `walk_down_0.png` and copy it to all 16 slots
2. **One enemy sprite**: `enemy_basic.png`
3. **One floor tile**: `floor.png`
4. **One wall tile**: `wall.png`

The rest will use fallback rendering, and you can add more later!

## 💡 Example Workflow

1. Download "Tiny Dungeon" pack from Kenney.nl
2. Extract the sprites
3. Find the character sprite you like
4. Copy it to all player animation slots: `walk_down_0.png` through `walk_right_3.png`
5. Find a floor tile, copy to `tiles/floor.png`
6. Find a wall tile, copy to `tiles/wall.png`
7. Find enemy sprites, copy to `enemies/enemy_basic.png`
8. Find object sprites, rename to match object types
9. Run the game and see your sprites in action!
10. Later, add proper animations by replacing the copied sprites with actual frames

## ❓ Troubleshooting

**Q: Sprites not showing up?**
- Check console for `⚠️ Sprite not found` messages
- Verify file is in `src/main/resources/sprites/`
- Check file name matches exactly (case-sensitive)
- Ensure file format is PNG, JPG, or GIF

**Q: Game won't compile after adding sprites?**
- Sprites are loaded at runtime, not compile time
- If compile error, it's unrelated to sprites
- Check Java syntax errors in code files

**Q: Sprites look blurry or stretched?**
- Make sure sprite dimensions match recommended sizes
- Player: 32x32, Objects: 48-96 pixels, Tiles: 64x64

**Q: Animation looks choppy?**
- Check frame count (4 frames recommended)
- Verify frame timing (0.1 seconds per frame)
- Ensure all frames are same size

**Q: How do I create my own sprites?**
- Use tools like Aseprite, Piskel, or GraphicsGale
- Export as PNG with transparent background
- Follow size guidelines above
- Test in-game frequently

## 🎓 Next Steps

After adding basic sprites:
1. Add proper walking animations (4 frames per direction)
2. Create unique sprites for each object type
3. Add boss enemy sprites
4. Design themed tilesets (dungeon, cave, temple, etc.)
5. Add sprite variations for visual variety
6. Implement animated objects (flickering campfire, etc.)

---

**Remember**: The game works perfectly without any sprites! Add them at your own pace and test frequently. The fallback system ensures the game is always playable.
