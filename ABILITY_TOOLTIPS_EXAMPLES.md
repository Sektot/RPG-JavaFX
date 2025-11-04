# 📖 Ability Tooltip System - Examples

This document shows examples of the new tooltip system for abilities.

## 🎮 How to Use Tooltips

### **JavaFX (GUI Mode)**
- **Hover** your mouse over any ability button for 300ms to see the detailed tooltip
- The tooltip shows all ability mechanics, damage calculations, and requirements

### **CLI (Console Mode)**
- Select ability menu option **"📖 Vezi Detalii Abilitate"** to view detailed tooltips
- Short tooltips are shown automatically next to each ability name

---

## 📊 Tooltip Examples

### **Example 1: Basic Damage Ability**
```
╔════════════════════════════════════════╗
║         Săgeată Arcanică               ║
╠════════════════════════════════════════╣
║ 💙 Cost: 10     Mană                   ║
║ ⚔️  Damage: 12 → 45                     ║
║ 📊 Scaling:                            ║
║    • Intelligence: 1.3x (+33)          ║
╚════════════════════════════════════════╝
```
**Short Tooltip**: `⚔️45 dmg`

---

### **Example 2: Multi-Hit Ultimate Ability**
```
╔════════════════════════════════════════╗
║ 🌟⚡ ULTIMATE ABILITY ⚡🌟            ║
║          Spiritul Dacilor              ║
╠════════════════════════════════════════╣
║ 💙 Cost: 100    Furie                  ║
║ ⏱️  Cooldown: 10 turns                 ║
║ 🔒 Required Level: 30                  ║
╠════════════════════════════════════════╣
║ ⚔️  Damage: 100 → 350                   ║
║ 📊 Scaling:                            ║
║    • Strength: 3.5x (+250)             ║
╠════════════════════════════════════════╣
║ ⚔️  Multi-Hit: 5x hits                  ║
║    Total Damage: 1750                  ║
╠════════════════════════════════════════╣
║ 💚 Heals: 0 + 25% max HP               ║
║    Total: ~125 HP                      ║
╠════════════════════════════════════════╣
║ ✨ Buff: SpiritulDacilor               ║
║    Duration: 3 turns                   ║
║    • Strength: +60%                    ║
║    • Defense: +40%                     ║
║    • Crit damage: +50%                 ║
╠════════════════════════════════════════╣
║ 🔥 Debuff: Stun                        ║
║    Duration: 2 turns                   ║
║    DoT: 10 damage/turn                 ║
╚════════════════════════════════════════╝
```
**Short Tooltip**: `🌟ULTIMATE | ⚔️350 dmg | 5x hits | 💚Heal | ✨Buff | 🔥Debuff`

---

### **Example 3: Combo Ability**
```
╔════════════════════════════════════════╗
║         Furie Primordială              ║
╠════════════════════════════════════════╣
║ 💙 Cost: 70     Furie                  ║
║ ⏱️  Cooldown: 5 turns                  ║
║ 🔒 Required Level: 25                  ║
╠════════════════════════════════════════╣
║ ⚔️  Damage: 70 → 280                    ║
║ 📊 Scaling:                            ║
║    • Strength: 2.8x (+210)             ║
╠════════════════════════════════════════╣
║ 🔥 COMBO ABILITY                       ║
║    Requires: Șarjă Furioasă            ║
║    Bonus: +50% damage                  ║
╚════════════════════════════════════════╝
```
**Short Tooltip**: `⚔️280 dmg | 🔥COMBO`

**Combo Activated**: When used after Șarjă Furioasă, damage becomes **420** (280 + 50%)!

---

### **Example 4: Resource Generation Ability**
```
╔════════════════════════════════════════╗
║         Lovitură Furioasă              ║
╠════════════════════════════════════════╣
║ 💙 Cost: 0      Furie                  ║
╠════════════════════════════════════════╣
║ ⚔️  Damage: 15 → 65                     ║
║ 📊 Scaling:                            ║
║    • Strength: 1.3x (+50)              ║
╠════════════════════════════════════════╣
║ ⚡ Generates 15 Furie                  ║
╚════════════════════════════════════════╝
```
**Short Tooltip**: `⚔️65 dmg | ⚡+15`

---

### **Example 5: Self-Damage (Berserker) Ability**
```
╔════════════════════════════════════════╗
║          Furia Sângelui                ║
╠════════════════════════════════════════╣
║ 💙 Cost: 40     Furie                  ║
║ ⏱️  Cooldown: 5 turns                  ║
║ 🔒 Required Level: 5                   ║
╠════════════════════════════════════════╣
║ 💔 Costs 20 HP to activate             ║
╠════════════════════════════════════════╣
║ ✨ Buff: FuriaSangelui                 ║
║    Duration: 4 turns                   ║
║    • Strength: +50%                    ║
║    • Crit chance: +40%                 ║
║    • Lifesteal: +15%                   ║
╚════════════════════════════════════════╝
```
**Short Tooltip**: `✨Buff`

---

### **Example 6: AOE Multi-Hit Ability**
```
╔════════════════════════════════════════╗
║         Vârtej de Oțel                 ║
╠════════════════════════════════════════╣
║ 💙 Cost: 50     Furie                  ║
║ ⏱️  Cooldown: 4 turns                  ║
║ 🔒 Required Level: 10                  ║
╠════════════════════════════════════════╣
║ ⚔️  Damage: 30 → 140                    ║
║ 📊 Scaling:                            ║
║    • Strength: 1.6x (+110)             ║
╠════════════════════════════════════════╣
║ ⚔️  Multi-Hit: 3x hits                  ║
║    Total Damage: 420                   ║
║ 💥 Area of Effect                      ║
╚════════════════════════════════════════╝
```
**Short Tooltip**: `⚔️140 dmg | 3x hits | 💥AOE`

---

### **Example 7: Healing Ability**
```
╔════════════════════════════════════════╗
║       Fortăreața Carpatină             ║
╠════════════════════════════════════════╣
║ 💙 Cost: 60     Furie                  ║
║ ⏱️  Cooldown: 6 turns                  ║
║ 🔒 Required Level: 15                  ║
╠════════════════════════════════════════╣
║ 💚 Heals: 0 + 15% max HP               ║
║    Total: ~75 HP                       ║
╠════════════════════════════════════════╣
║ ✨ Buff: Fortareata                    ║
║    Duration: 5 turns                   ║
║    • Defense: +80%                     ║
║    • Damage reduction: +40%            ║
║    • Block chance: +25%                ║
╚════════════════════════════════════════╝
```
**Short Tooltip**: `💚Heal | ✨Buff`

---

### **Example 8: Mage Combo Ability**
```
╔════════════════════════════════════════╗
║            Meteorit                    ║
╠════════════════════════════════════════╣
║ 💙 Cost: 75     Mană                   ║
║ ⏱️  Cooldown: 5 turns                  ║
║ 🔒 Required Level: 25                  ║
╠════════════════════════════════════════╣
║ ⚔️  Damage: 65 → 240                    ║
║ 📊 Scaling:                            ║
║    • Intelligence: 2.7x (+175)         ║
║ 💥 Area of Effect                      ║
╠════════════════════════════════════════╣
║ 🔥 COMBO ABILITY                       ║
║    Requires: Minge de Foc              ║
║    Bonus: +60% damage                  ║
╠════════════════════════════════════════╣
║ 🔥 Debuff: Burn                        ║
║    Duration: 4 turns                   ║
║    DoT: 8 damage/turn                  ║
╚════════════════════════════════════════╝
```
**Short Tooltip**: `⚔️240 dmg | 💥AOE | 🔥COMBO | 🔥Debuff`

**Combo Activated**: When used after Minge de Foc, damage becomes **384** (240 + 60%)!

---

### **Example 9: Rogue Stealth Combo**
```
╔════════════════════════════════════════╗
║            Asasinare                   ║
╠════════════════════════════════════════╣
║ 💙 Cost: 60     Energie                ║
║ ⏱️  Cooldown: 3 turns                  ║
║ 🔒 Required Level: 25                  ║
╠════════════════════════════════════════╣
║ ⚔️  Damage: 70 → 300                    ║
║ 📊 Scaling:                            ║
║    • Dexterity: 3.0x (+230)            ║
╠════════════════════════════════════════╣
║ 🔥 COMBO ABILITY                       ║
║    Requires: Ascuns în Umbră           ║
║    Bonus: +75% damage                  ║
╠════════════════════════════════════════╣
║ 🔥 Debuff: DeepWound                   ║
║    Duration: 4 turns                   ║
║    DoT: 10 damage/turn                 ║
╚════════════════════════════════════════╝
```
**Short Tooltip**: `⚔️300 dmg | 🔥COMBO | 🔥Debuff`

**Combo Activated**: When used after Ascuns în Umbră, damage becomes **525** (300 + 75%)!

---

## 🎯 Tooltip Features

### **Information Shown**:
✅ **Cost** - Resource cost (Mana/Rage/Energy)
✅ **Cooldown** - Turn-based cooldown
✅ **Level Requirement** - Unlock level
✅ **Base → Scaled Damage** - Shows how stats affect damage
✅ **Stat Scaling** - Exact multipliers and bonuses
✅ **Multi-Hit** - Number of hits and total damage
✅ **AOE** - Area of effect indicator
✅ **Combo** - Combo requirements and bonus damage
✅ **Resource Generation** - Resources gained on hit
✅ **Healing** - HP restoration (flat + percentage)
✅ **Self-Damage** - HP cost for activation
✅ **Buffs** - Temporary stat boosts with duration
✅ **Debuffs** - Enemy afflictions with DoT
✅ **Hit Chance Bonus** - Accuracy improvements
✅ **Ultimate Status** - Special indicator for ultimate abilities

---

## 💡 Tips for Using Tooltips

1. **Hover over abilities** in JavaFX to see all details before using them
2. **Check combo requirements** to maximize damage output
3. **Look at resource generation** to plan your rotation
4. **Read ultimate tooltips** carefully - they're game-changing!
5. **Compare scaled damage** between abilities to choose the best option
6. **Plan combos** by checking which abilities require others first

---

## 🔄 Example Combat Rotation (Based on Tooltips)

### **Warrior Rotation:**
```
1. Lovitură Furioasă (⚡+15 Rage)
2. Tăietură Sălbatică (⚡+20 Rage) → Total: 35 Rage
3. Șarjă Furioasă (Cost 25, ⚡+25 Rage) → Total: 35 Rage, enemy stunned
4. Furie Primordială (🔥COMBO +50% = 420 damage!)
```

### **Mage Rotation:**
```
1. Săgeată Arcanică (cheap spam)
2. Minge de Foc (apply burn)
3. Meteorit (🔥COMBO +60% = 384 damage + AOE!)
4. Sifon de Mană (restore 30 mana)
5. Repeat or use Ultimate at 100 mana
```

### **Rogue Rotation:**
```
1. Lovitură Rapidă (⚡+20 Energy)
2. Ascuns în Umbră (buff: +60% dodge, +50% crit)
3. Asasinare (🔥COMBO +75% = 525 damage from stealth!)
4. Sângerare (apply bleed DoT)
```

---

**Enjoy the new tooltip system! Now you'll always know exactly what your abilities do! ⚔️🔮🗡️**
