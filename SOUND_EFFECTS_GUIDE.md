# 🔊 Sound Effects Integration Guide

## ✅ SoundManager Created

The `SoundManager` utility class has been created to handle all game sounds with:
- **Automatic caching** for better performance
- **Volume control** (0.0 to 1.0)
- **Mute/unmute** functionality
- **Graceful fallback** if sound files are missing (game continues without sound)

---

## 📁 Where to Place Sound Files

Create this directory structure:

```
src/main/resources/sounds/
├── attack_hit.wav
├── attack_miss.wav
├── critical_hit.wav
├── dodge.wav
├── damage_light.wav
├── damage_heavy.wav
├── heal.wav
├── buff_apply.wav
├── debuff_apply.wav
├── ability_cast.wav
├── ultimate_cast.wav
├── button_click.wav
├── potion_use.wav
├── victory.wav
├── defeat.wav
├── level_up.wav
├── combo.wav
└── screen_shake.wav
```

**Format:** `.wav` files (best compatibility with JavaFX AudioClip)

---

## 🎵 How to Use SoundManager

### **In BattleControllerFX (already integrated):**

```java
// When hero attacks
SoundManager.play(SoundManager.SoundEffect.ATTACK_HIT);

// On critical hit
SoundManager.play(SoundManager.SoundEffect.CRITICAL_HIT);

// On dodge
SoundManager.play(SoundManager.SoundEffect.DODGE);

// On heal
SoundManager.play(SoundManager.SoundEffect.HEAL);

// Auto-select damage sound based on amount
SoundManager.playDamageSound(damageAmount, maxHP, isCrit);

// On ability use
SoundManager.play(SoundManager.SoundEffect.ABILITY_CAST);

// On victory
SoundManager.play(SoundManager.SoundEffect.VICTORY);
```

### **Volume Control:**

```java
// Set volume to 50%
SoundManager.setVolume(0.5);

// Mute all sounds
SoundManager.mute();

// Unmute
SoundManager.unmute();

// Toggle mute
SoundManager.toggleMute();
```

### **Preloading Sounds:**

Call this when the game starts for faster playback:

```java
SoundManager.preloadCommonSounds();
```

---

## 🆓 Where to Get Free Sound Effects

### **Recommended Sources:**

1. **Freesound.org** - https://freesound.org/
   - Search for: "sword hit", "critical", "heal", "dodge", "buff"
   - Filter by: CC0 license (no attribution required)

2. **OpenGameArt.org** - https://opengameart.org/
   - Browse: Audio > Sound Effects
   - Look for RPG/Battle sound packs

3. **Mixkit** - https://mixkit.co/free-sound-effects/game/
   - Free game sound effects
   - No attribution required

4. **Zapsplat** - https://www.zapsplat.com/
   - Free with attribution

### **What to Search For:**

- **attack_hit**: "sword slash", "punch", "hit"
- **critical_hit**: "explosion", "power hit", "crit"
- **dodge**: "whoosh", "evade", "miss"
- **damage_light**: "thud", "impact light"
- **damage_heavy**: "heavy hit", "slam"
- **heal**: "healing", "magic sparkle", "restore"
- **buff_apply**: "power up", "buff", "enchant"
- **ability_cast**: "spell cast", "magic"
- **button_click**: "UI click", "button"
- **victory**: "fanfare", "win", "success"

---

## 🎮 Sound Integration Status

### ✅ Already Integrated in BattleControllerFX:
- Attack hits
- Critical hits
- Dodge/miss
- Damage (light/heavy auto-selection)
- Healing
- Buff application
- Ability casting
- Potion use
- Victory/defeat
- Button clicks

### 🔧 To Add Sound Files:
1. Download `.wav` files from sources above
2. Place in `src/main/resources/sounds/`
3. Sounds will play automatically (no code changes needed!)

---

## 🔇 If You Don't Have Sounds Yet

**The game will work perfectly fine without sound files!**

- SoundManager gracefully fails if sounds are missing
- No errors, no crashes
- Just silent gameplay until you add sound files

---

## 🎛️ Testing Sounds

Run the game and:
1. Start a battle
2. Attack enemies - hear attack sounds
3. Get hit - hear damage sounds
4. Use abilities - hear casting sounds
5. Win battle - hear victory sound

If you don't hear anything, check:
- Sound files are in `src/main/resources/sounds/`
- Files are named exactly as shown above
- Files are `.wav` format
- Volume is not muted: `SoundManager.setVolume(0.8)`

---

## 📋 Quick Sound Download Checklist

- [ ] Download `attack_hit.wav`
- [ ] Download `critical_hit.wav`
- [ ] Download `dodge.wav`
- [ ] Download `damage_light.wav`
- [ ] Download `damage_heavy.wav`
- [ ] Download `heal.wav`
- [ ] Download `buff_apply.wav`
- [ ] Download `ability_cast.wav`
- [ ] Download `potion_use.wav`
- [ ] Download `victory.wav`
- [ ] Download `defeat.wav`
- [ ] Download `button_click.wav`

**Once you have these 12 sounds, your combat will feel 10x more impactful!** 🎵⚔️
