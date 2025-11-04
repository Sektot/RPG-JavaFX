# 🎨 Spritesheet Support Guide

## ✅ What's New

The animation system now supports **both individual frame files AND spritesheets**! You can choose whichever method suits your workflow better.

---

## 📋 Two Methods to Load Animations

### **Method 1: Individual Frame Files** (Current Default)

**How it works:**
- Each animation frame is a separate PNG file
- Named with frame numbers: `walk_down_0.png`, `walk_down_1.png`, etc.

**File Structure:**
```
src/main/resources/sprites/player/
├── walk_down_0.png
├── walk_down_1.png
├── walk_down_2.png
├── walk_down_3.png
├── walk_down_4.png
├── walk_down_5.png
├── walk_down_6.png
├── walk_down_7.png
├── walk_up_0.png ... walk_up_7.png
├── walk_left_0.png ... walk_left_7.png
└── walk_right_0.png ... walk_right_7.png
```

**Code (RoomExplorationController.java:223-226):**
```java
playerAnimations.put(Direction.NORTH, new AnimatedSprite("player", "walk_up", 8, 0.1));
playerAnimations.put(Direction.SOUTH, new AnimatedSprite("player", "walk_down", 8, 0.1));
playerAnimations.put(Direction.EAST, new AnimatedSprite("player", "walk_right", 8, 0.1));
playerAnimations.put(Direction.WEST, new AnimatedSprite("player", "walk_left", 8, 0.1));
```

**Pros:**
- Easy to edit individual frames
- Simple to replace specific frames
- Good for iterative development

**Cons:**
- Many separate files to manage
- More disk I/O when loading

---

### **Method 2: Spritesheet** (New!)

**How it works:**
- All animation frames in a single image file
- Frames arranged in a grid (rows × columns)
- System automatically extracts individual frames

**File Structure:**
```
src/main/resources/sprites/player/
├── walk_down_sheet.png   (512×64 pixels: 8 frames × 64px each in a row)
├── walk_up_sheet.png     (512×64 pixels)
├── walk_left_sheet.png   (512×64 pixels)
└── walk_right_sheet.png  (512×64 pixels)
```

**Spritesheet Layout Example:**
```
walk_down_sheet.png (512×64 pixels):
┌────────┬────────┬────────┬────────┬────────┬────────┬────────┬────────┐
│Frame 0 │Frame 1 │Frame 2 │Frame 3 │Frame 4 │Frame 5 │Frame 6 │Frame 7 │
│ 64×64  │ 64×64  │ 64×64  │ 64×64  │ 64×64  │ 64×64  │ 64×64  │ 64×64  │
└────────┴────────┴────────┴────────┴────────┴────────┴────────┴────────┘
```

**Code (Uncomment in RoomExplorationController.java:232-239):**
```java
playerAnimations.put(Direction.NORTH,
    AnimatedSprite.fromSpritesheet("player", "walk_up_sheet", 8, 0.1, 64, 64, 8, 1));
playerAnimations.put(Direction.SOUTH,
    AnimatedSprite.fromSpritesheet("player", "walk_down_sheet", 8, 0.1, 64, 64, 8, 1));
playerAnimations.put(Direction.EAST,
    AnimatedSprite.fromSpritesheet("player", "walk_right_sheet", 8, 0.1, 64, 64, 8, 1));
playerAnimations.put(Direction.WEST,
    AnimatedSprite.fromSpritesheet("player", "walk_left_sheet", 8, 0.1, 64, 64, 8, 1));
```

**Parameters Explained:**
```java
AnimatedSprite.fromSpritesheet(
    "player",           // Category (folder name)
    "walk_up_sheet",    // Spritesheet filename (without .png)
    8,                  // Number of frames to extract
    0.1,                // Seconds per frame (animation speed)
    64,                 // Width of each frame in pixels
    64,                 // Height of each frame in pixels
    8,                  // Number of columns (frames per row)
    1                   // Number of rows
);
```

**Pros:**
- Fewer files to manage (4 files instead of 32)
- Faster loading (fewer disk reads)
- Standard format for game development
- Easy to export from animation software

**Cons:**
- Requires image editing to modify individual frames
- Larger single file size

---

## 🔧 How to Switch Methods

### **To Switch from Individual Frames → Spritesheet:**

1. **Create your spritesheets** (see creation guide below)

2. **Place spritesheets in:** `src/main/resources/sprites/player/`
   - `walk_down_sheet.png`
   - `walk_up_sheet.png`
   - `walk_left_sheet.png`
   - `walk_right_sheet.png`

3. **Edit `RoomExplorationController.java`:**
   - Comment out lines 223-226 (individual frames)
   - Uncomment lines 232-239 (spritesheet)

4. **Recompile and run!**

### **To Switch back to Individual Frames:**

1. Reverse the process above (comment spritesheet, uncomment individual frames)

---

## 🎨 Creating Spritesheets

### **Using Image Editing Software:**

#### **Method A: Photoshop/GIMP**
1. Create a canvas: **512×64 pixels** (for 8 frames of 64×64)
2. Place each frame side by side in a single row
3. Export as PNG with transparency

#### **Method B: Free Online Tools**
- **TexturePacker** - https://www.codeandweb.com/texturepacker (free version)
- **Leshy SpriteSheet Tool** - https://www.leshylabs.com/apps/sstool/
- **Piskel** - https://www.piskelapp.com/ (pixel art editor with spritesheet export)

#### **Method C: Command Line (ImageMagick)**
```bash
# Combine individual frames into a spritesheet
convert walk_down_*.png +append walk_down_sheet.png
```

### **Spritesheet Dimensions for Different Frame Counts:**

| Frames | Frame Size | Layout  | Spritesheet Size |
|--------|-----------|---------|------------------|
| 4      | 64×64     | 4×1     | 256×64           |
| 8      | 64×64     | 8×1     | 512×64           |
| 8      | 64×64     | 4×2     | 256×128          |
| 12     | 64×64     | 6×2     | 384×128          |
| 16     | 64×64     | 8×2     | 512×128          |

---

## 📊 Advanced Spritesheet Layouts

### **Multi-Row Spritesheets:**

You can arrange frames in a grid for larger animations:

**Example: 16 frames in 4 rows × 4 columns:**
```
walk_full_sheet.png (256×256 pixels):
┌────────┬────────┬────────┬────────┐
│Frame 0 │Frame 1 │Frame 2 │Frame 3 │
├────────┼────────┼────────┼────────┤
│Frame 4 │Frame 5 │Frame 6 │Frame 7 │
├────────┼────────┼────────┼────────┤
│Frame 8 │Frame 9 │Frame 10│Frame 11│
├────────┼────────┼────────┼────────┤
│Frame 12│Frame 13│Frame 14│Frame 15│
└────────┴────────┴────────┴────────┘
```

**Code:**
```java
AnimatedSprite.fromSpritesheet(
    "player", "walk_full_sheet",
    16,      // 16 frames total
    0.1,     // Animation speed
    64, 64,  // Frame size
    4, 4     // 4 columns × 4 rows
);
```

---

## 🚀 Best Practices

### **When to Use Individual Frames:**
- Rapid prototyping and testing
- Frequent iteration on specific frames
- Working with artists who deliver separate files
- Simple animations (4 frames or less)

### **When to Use Spritesheets:**
- Production/final builds
- Many animation frames (8+)
- Professional game development workflow
- Working with animation software that exports spritesheets
- Need to reduce file count

### **Performance Notes:**
- **Spritesheets are slightly faster** to load (fewer file reads)
- Both methods use the same amount of memory once loaded
- **Frame extraction is done once** at startup (negligible overhead)

---

## 🔍 Troubleshooting

### **"Spritesheet not found" Error:**
- Check filename matches exactly (case-sensitive)
- Ensure file is in correct folder: `src/main/resources/sprites/[category]/`
- Verify the file extension is `.png`

### **"Cannot read spritesheet pixels" Error:**
- Spritesheet file may be corrupted
- File format might not be supported (use PNG with transparency)

### **Frames Look Wrong:**
- Verify frame dimensions match your spritesheet
- Check columns/rows parameters are correct
- Ensure frames are aligned to pixel grid in spritesheet

### **Animation Too Fast/Slow:**
- Adjust the `frameDuration` parameter (e.g., 0.1 = 10 FPS)
- Lower values = faster animation
- Higher values = slower animation

---

## 📝 Quick Reference

### **Individual Frames:**
```java
new AnimatedSprite("player", "walk_down", 8, 0.1)
```

### **Spritesheet (1 row):**
```java
AnimatedSprite.fromSpritesheet("player", "walk_sheet", 8, 0.1, 64, 64, 8, 1)
```

### **Spritesheet (2 rows):**
```java
AnimatedSprite.fromSpritesheet("player", "walk_sheet", 8, 0.1, 64, 64, 4, 2)
```

---

## ✨ Summary

You now have **complete flexibility** in how you organize your sprite assets:

- ✅ **Individual frames** - Easy editing, many files
- ✅ **Spritesheets** - Professional workflow, fewer files
- ✅ **Both supported** - Switch anytime by changing code
- ✅ **Automatic extraction** - System handles all frame slicing

Choose the method that best fits your workflow and switch between them as needed!
