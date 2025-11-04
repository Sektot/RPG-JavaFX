# ⚡ Visual Feedback Quick Reference Card

## 🎯 Quick Integration Snippets

### **Show Damage Number**
```java
FloatingText.show(battleCanvas, "85", enemyX, enemyY, FloatingText.TextType.DAMAGE);
```

### **Show Critical Hit**
```java
FloatingText.show(battleCanvas, "150", enemyX, enemyY, FloatingText.TextType.CRITICAL);
ScreenShake.shake(battleRoot, ScreenShake.ShakeIntensity.HEAVY);
```

### **Show Healing**
```java
FloatingText.show(battleCanvas, "+50", heroX, heroY, FloatingText.TextType.HEAL);
heroHealthBar.updateHP(hero.getViata(), false);
```

### **Show Dodge**
```java
FloatingText.show(battleCanvas, "DODGE!", enemyX, enemyY, FloatingText.TextType.DODGE);
```

### **Show Multi-Hit**
```java
int[] damages = {45, 45, 45}; // 3 hits
boolean[] crits = {false, true, false}; // 2nd hit is crit
FloatingText.showMultiHit(battleCanvas, damages, enemyX, enemyY, crits);
```

### **Update Health Bar**
```java
// Taking damage
enemyHealthBar.updateHP(newHP, true); // true = damage

// Healing
heroHealthBar.updateHP(newHP, false); // false = heal
```

### **Screen Shake**
```java
// Auto-scale shake based on damage
ScreenShake.shakeForDamage(battleRoot, damageAmount, maxHP);

// Manual intensity
ScreenShake.shake(battleRoot, ScreenShake.ShakeIntensity.CRITICAL);
```

### **Update Status Effects**
```java
heroBuffDisplay.updateBuffs(hero.getBuffuriActive());
heroDebuffDisplay.updateDebuffs(hero.getDebuffuriActive());
enemyBuffDisplay.updateBuffs(enemy.getBuffuriActive());
enemyDebuffDisplay.updateDebuffs(enemy.getDebuffuriActive());
```

### **Show Buff Applied**
```java
FloatingText.show(battleCanvas, "✨ Power Up!", heroX, heroY, FloatingText.TextType.BUFF);
heroBuffDisplay.updateBuffs(hero.getBuffuriActive());
```

### **Show Debuff Applied**
```java
FloatingText.show(battleCanvas, "🔥 Burn!", enemyX, enemyY, FloatingText.TextType.DEBUFF);
enemyDebuffDisplay.updateDebuffs(enemy.getDebuffuriActive());
```

### **Show Resource Gain**
```java
FloatingText.show(battleCanvas, "+25 Rage", heroX, heroY, FloatingText.TextType.RESOURCE_GAIN);
```

---

## 📊 Common Patterns

### **Pattern 1: Regular Attack**
```java
// Execute attack
int damage = 42;
boolean isCrit = false;

// Visual feedback
FloatingText.show(battleCanvas, String.valueOf(damage), enemyX, enemyY, FloatingText.TextType.DAMAGE);
ScreenShake.shakeForDamage(battleRoot, damage, enemy.getViataMaxima());
enemyHealthBar.updateHP(enemy.getViata(), true);
```

### **Pattern 2: Critical Hit**
```java
int critDamage = 120;

FloatingText.show(battleCanvas, String.valueOf(critDamage), enemyX, enemyY, FloatingText.TextType.CRITICAL);
ScreenShake.shake(battleRoot, ScreenShake.ShakeIntensity.CRITICAL);
enemyHealthBar.updateHP(enemy.getViata(), true);
```

### **Pattern 3: Multi-Hit Ability**
```java
int[] damages = {30, 30, 30, 30}; // 4 hits
boolean[] crits = {false, false, true, false}; // 3rd is crit

FloatingText.showMultiHit(battleCanvas, damages, enemyX, enemyY, crits);
ScreenShake.shake(battleRoot, ScreenShake.ShakeIntensity.HEAVY);
enemyHealthBar.updateHP(enemy.getViata(), true);
```

### **Pattern 4: Buff + Heal Combo**
```java
// Apply buff
FloatingText.show(battleCanvas, "✨ Fortified!", heroX, heroY, FloatingText.TextType.BUFF);
heroBuffDisplay.updateBuffs(hero.getBuffuriActive());

// Heal
FloatingText.show(battleCanvas, "+75", heroX, heroY + 30, FloatingText.TextType.HEAL);
heroHealthBar.updateHP(hero.getViata(), false);
```

### **Pattern 5: Resource Generation Attack**
```java
// Deal damage
FloatingText.show(battleCanvas, "50", enemyX, enemyY, FloatingText.TextType.DAMAGE);
enemyHealthBar.updateHP(enemy.getViata(), true);

// Generate resource
FloatingText.show(battleCanvas, "+15 Rage", heroX, heroY, FloatingText.TextType.RESOURCE_GAIN);
```

---

## 🎨 Text Type Colors

| Type | Color | Use Case |
|------|-------|----------|
| `DAMAGE` | White | Regular damage |
| `CRITICAL` | Red/Orange | Critical hits |
| `HEAL` | Green | Healing |
| `DODGE` | Gray | Dodged attacks |
| `MISS` | Gray | Missed attacks |
| `BUFF` | Blue | Buffs applied |
| `DEBUFF` | Purple | Debuffs applied |
| `RESOURCE_GAIN` | Cyan | Resources gained |
| `RESOURCE_LOSS` | Yellow | Resources spent |

---

## 🎚️ Shake Intensities

| Intensity | Magnitude | Duration | Use Case |
|-----------|-----------|----------|----------|
| `LIGHT` | 2px | 100ms | Small damage (<15% HP) |
| `MEDIUM` | 5px | 200ms | Medium damage (15-30% HP) |
| `HEAVY` | 10px | 300ms | Large damage (30-50% HP) |
| `CRITICAL` | 15px | 400ms | Massive damage (>50% HP), crits, ultimates |

---

## 🔧 Field Setup

Add these fields to `BattleControllerFX`:

```java
// Visual feedback components
private Pane battleCanvas;
private AnimatedHealthBar heroHealthBar;
private AnimatedHealthBar enemyHealthBar;
private StatusEffectDisplay heroBuffDisplay;
private StatusEffectDisplay heroDebuffDisplay;
private StatusEffectDisplay enemyBuffDisplay;
private StatusEffectDisplay enemyDebuffDisplay;
private BorderPane battleRoot; // For screen shake
```

---

## 📍 Common Positions

```java
// Enemy (top center)
double enemyX = battleCanvas.getWidth() / 2;
double enemyY = 100;

// Hero (bottom center)
double heroX = battleCanvas.getWidth() / 2;
double heroY = battleCanvas.getHeight() - 150;

// Resource indicator (hero, above portrait)
double resourceX = heroX;
double resourceY = heroY - 50;
```

---

## ⚠️ Important Notes

1. **Always create battleCanvas as a Pane** (not VBox/HBox) for floating text positioning
2. **Screen shake requires a root container** (use BorderPane as battleRoot)
3. **Update status effects after every action** to keep them in sync
4. **Use animation delays** for multi-hit (150ms between hits)
5. **Parse combat logs** to extract damage/crit/effect info

---

## 🚀 Example: Complete Turn

```java
private void executeTurn(String abilityName) {
    // Execute ability through service
    BattleTurnResultDTO result = battleService.executeAbility(hero, enemy, abilityName);

    if (!result.isSuccess()) return;

    // Parse results
    int damage = parseDamage(result.getLogMessage());
    boolean isCrit = result.getLogMessage().contains("CRIT");
    boolean hasCombo = result.getLogMessage().contains("COMBO");

    // Positions
    double enemyX = battleCanvas.getWidth() / 2;
    double enemyY = 100;

    // Show damage
    if (damage > 0) {
        FloatingText.TextType type = isCrit ? FloatingText.TextType.CRITICAL : FloatingText.TextType.DAMAGE;
        FloatingText.show(battleCanvas, String.valueOf(damage), enemyX, enemyY, type);

        // Shake screen
        if (isCrit || hasCombo) {
            ScreenShake.shake(battleRoot, ScreenShake.ShakeIntensity.CRITICAL);
        } else {
            ScreenShake.shakeForDamage(battleRoot, damage, enemy.getViataMaxima());
        }

        // Animate health
        enemyHealthBar.updateHP(enemy.getViata(), true);
    }

    // Update status effects
    heroBuffDisplay.updateBuffs(hero.getBuffuriActive());
    enemyDebuffDisplay.updateDebuffs(enemy.getDebuffuriActive());

    // Update text log
    battleLog.appendText(result.getLogMessage() + "\n");
}
```

---

**Keep this reference handy while implementing! 📋⚡**
