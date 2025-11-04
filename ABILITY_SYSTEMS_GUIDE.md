# ⚔️ Complete Ability Systems Guide

This document details all abilities for each class, including mechanics, combos, and progression.

---

## 🎮 New Ability Mechanics

### **Ultimate Abilities**
- Unlocked at level 30
- Extremely powerful (100 resource cost, 10-turn cooldown)
- Game-changing effects (multi-hit, healing, massive buffs)
- Marked with `isUltimate = true`

### **Combo System**
- Some abilities have `comboRequirement` - must use specific ability first
- Combo abilities deal bonus damage (`comboBonusDamage` multiplier)
- Example: Warrior's "Furie Primordială" requires "Șarjă Furioasă" first for +50% damage

### **Resource Generation**
- `resourceGenerated` - abilities that restore resources
- Warrior: Generate Rage on attacks
- Mage: Mana Siphon restores mana
- Rogue: Energy builders

### **Multi-Hit Abilities**
- `numberOfHits` - hits multiple times in one cast
- Each hit calculates damage separately
- Great for triggering on-hit effects

### **AOE Abilities**
- `isAOE = true` - hits all enemies (future: multiple enemies in dungeon)
- Massive damage potential

### **Self-Damage Abilities**
- `selfDamage` - costs HP to activate
- High risk, high reward (e.g., Berserker mode)

### **Healing Abilities**
- `healAmount` - flat healing
- `healPercent` - percentage of max HP (scales with level)

---

## ⚔️ MOLDOVEAN (WARRIOR) - Rage-Based Tank/DPS

**Resource:** Furie (Rage) - Starts at 0, builds during combat (max 100)

### **Class Fantasy**
Mighty warrior from Moldova who builds Rage through combat, then unleashes devastating attacks. Can tank damage or go berserker mode.

### **Level 1 Abilities (Always Available)**

| Icon | Name | Type | Cost | Cooldown | Effect |
|------|------|------|------|----------|--------|
| 🗡️ | **Lovitură Furioasă** | Attack | 0 Rage | 0 | 15 damage (1.3x STR), generates 15 Rage |
| 🛡️ | **Scut de Țară** | Buff | 20 Rage | 3 | +40% defense, +25% damage reduction for 3 turns |
| ⚔️ | **Tăietură Sălbatică** | Attack | 0 Rage | 1 | 25 damage (1.6x STR), generates 20 Rage |

### **Unlockable Abilities**

**Level 3: Lovitură Devastatoare** 💥
- **Cost:** 30 Rage | **Cooldown:** 2 turns
- **Damage:** 40 base (2.0x STR scaling)
- **Purpose:** Your first Rage spender - high damage finisher

**Level 5: Furia Sângelui** 🩸
- **Cost:** 40 Rage + 20 HP | **Cooldown:** 5 turns
- **Damage:** 0 (buff only)
- **Buff:** +50% STR, +40% crit chance, +15% lifesteal for 4 turns
- **Purpose:** Berserker mode - trade HP for massive power

**Level 8: Șarjă Furioasă** ⚡
- **Cost:** 25 Rage | **Cooldown:** 3 turns
- **Damage:** 35 base (1.8x STR, 0.5x DEX)
- **Debuff:** Stun for 1 turn
- **Special:** Generates 25 Rage on hit
- **Purpose:** Gap closer with stun + Rage generation

**Level 10: Vârtej de Oțel** 🌪️
- **Cost:** 50 Rage | **Cooldown:** 4 turns
- **Damage:** 30 base (1.6x STR) × 3 hits = 90 total
- **Special:** AOE, hits 3 times
- **Purpose:** AOE damage for multiple enemies

**Level 15: Fortăreața Carpatină** 🛡️
- **Cost:** 60 Rage | **Cooldown:** 6 turns
- **Healing:** 15% max HP
- **Buff:** +80% defense, +40% damage reduction, +25% block chance for 5 turns
- **Purpose:** Tank mode - massive survival

**Level 20: Execuția** 💀
- **Cost:** 50 Rage | **Cooldown:** 4 turns
- **Damage:** 60 base (2.5x STR)
- **Purpose:** Execute ability - massive single target damage
- *Note: Future - could add bonus damage vs low HP enemies*

**Level 25: Furie Primordială** 🔥
- **Cost:** 70 Rage | **Cooldown:** 5 turns
- **Damage:** 70 base (2.8x STR)
- **Combo:** Requires "Șarjă Furioasă" first → +50% damage if combo'd!
- **Purpose:** Combo finisher - 105 base damage when combo'd

**Level 30: Spiritul Dacilor (ULTIMATE)** ⚔️
- **Cost:** 100 Rage | **Cooldown:** 10 turns
- **Damage:** 100 base (3.5x STR) × 5 hits = 500 total!
- **Healing:** 25% max HP
- **Debuff:** Stun for 2 turns, 10 DoT damage
- **Buff:** +60% STR, +40% defense, +50% crit damage for 3 turns
- **Purpose:** Ultimate ability - devastating multi-hit with huge buffs

### **Warrior Gameplay Loop**
1. **Build Rage:** Use Lovitură Furioasă + Tăietură Sălbatică to generate Rage
2. **Spend Rage:** Use high-damage abilities (Lovitură Devastatoare, Execuția)
3. **Combos:** Șarjă Furioasă → Furie Primordială for massive damage
4. **Survival:** Use Scut de Țară or Fortăreața when low HP
5. **Berserker:** Activate Furia Sângelui when you want all-out offense
6. **Ultimate:** Spiritul Dacilor for game-ending damage

---

## 🔮 ARDELEAN (MAGE) - Mana-Based Spellcaster

**Resource:** Mană (Mana) - Starts at max, depletes when casting (max based on INT)

### **Class Fantasy**
Powerful mage from Transylvania who wields elemental magic. Manages mana carefully for devastating spells.

### **Level 1 Abilities (Always Available)**

| Icon | Name | Type | Cost | Cooldown | Effect |
|------|------|------|------|----------|--------|
| 🔮 | **Săgeată Arcanică** | Attack | 10 Mana | 0 | 12 damage (1.3x INT), reliable spam ability |
| 🔥 | **Minge de Foc** | Attack | 20 Mana | 2 | 20 damage (1.6x INT) + Burn (5 DoT for 3 turns) |
| 🛡️ | **Barieră Magică** | Buff | 25 Mana | 4 | +30% defense, +40% magic resistance for 3 turns |

### **Unlockable Abilities**

**Level 3: Gheață Ascuțită** ❄️
- **Cost:** 30 Mana | **Cooldown:** 2 turns
- **Damage:** 25 base (1.7x INT)
- **Debuff:** Freeze for 2 turns
- **Purpose:** Crowd control with damage

**Level 5: Explozie Arcanică** 💫
- **Cost:** 40 Mana | **Cooldown:** 3 turns
- **Damage:** 30 base (1.8x INT) × 2 hits = 60 total
- **Special:** AOE, double hit
- **Purpose:** AOE burst damage

**Level 8: Sifon de Mană** 💙
- **Cost:** 15 Mana | **Cooldown:** 3 turns
- **Damage:** 18 base (1.4x INT)
- **Special:** Restores 30 mana on hit!
- **Purpose:** Mana sustain - spend 15, get back 30

**Level 10: Lanț de Fulgere** ⚡
- **Cost:** 45 Mana | **Cooldown:** 3 turns
- **Damage:** 22 base (1.6x INT) × 3 hits = 66 total
- **Debuff:** Shock for 2 turns (4 DoT)
- **Special:** AOE, hits 3 targets
- **Purpose:** Chain damage across multiple enemies

**Level 15: Putere Magică** 🧙
- **Cost:** 50 Mana | **Cooldown:** 5 turns
- **Buff:** +50% INT, +30% crit chance, +40% spell power for 4 turns
- **Purpose:** Mage power-up buff

**Level 20: Rază Prismatică** 🌟
- **Cost:** 60 Mana | **Cooldown:** 4 turns
- **Damage:** 50 base (2.3x INT) × 3 hits = 150 total
- **Elements:** Fire + Ice + Lightning combined!
- **Purpose:** Tri-elemental beam attack

**Level 25: Meteorit** ☄️
- **Cost:** 75 Mana | **Cooldown:** 5 turns
- **Damage:** 65 base (2.7x INT)
- **Debuff:** Burn for 4 turns (8 DoT)
- **Combo:** Requires "Minge de Foc" first → +60% damage if combo'd!
- **Special:** AOE
- **Purpose:** Massive AOE combo finisher (104 base when combo'd!)

**Level 30: Maelstrom Arcanic (ULTIMATE)** 🌌
- **Cost:** 100 Mana | **Cooldown:** 10 turns
- **Damage:** 90 base (3.5x INT) × 7 hits = 630 total!
- **Debuff:** Silence for 3 turns (15 DoT)
- **Special:** AOE, restores 50 mana
- **Buff:** +70% INT, +60% crit damage, +100% mana regen for 3 turns
- **Purpose:** Ultimate spell storm - highest damage in game

### **Mage Gameplay Loop**
1. **Mana Management:** Balance high-cost spells with Săgeată Arcanică
2. **Sustain:** Use Sifon de Mană to restore mana mid-fight
3. **Combos:** Minge de Foc → Meteorit for huge AOE damage
4. **Crowd Control:** Use Gheață Ascuțită to freeze dangerous enemies
5. **Buff:** Activate Putere Magică before big damage phases
6. **Ultimate:** Maelstrom Arcanic for massive AOE + mana restore

---

## 🗡️ OLTEAN (ROGUE) - Energy-Based Assassin

**Resource:** Energie (Energy) - Starts at max, regenerates quickly

### **Class Fantasy**
Swift assassin from Oltenia who builds energy with quick attacks, then spends it on devastating finishers.

### **Level 1 Abilities (Always Available)**

| Icon | Name | Type | Cost | Cooldown | Effect |
|------|------|------|------|----------|--------|
| 🗡️ | **Lovitură Rapidă** | Attack | 15 Energy | 0 | 12 damage (1.2x DEX), generates 20 Energy |
| 🔪 | **Înjunghiere** | Attack | 25 Energy | 1 | 22 damage (1.7x DEX), high crit chance |
| 👤 | **Ascuns în Umbră** | Buff | 30 Energy | 3 | +60% dodge, +50% crit chance for 3 turns |

### **Unlockable Abilities**

**Level 3: Sângerare** 🩸
- **Cost:** 30 Energy | **Cooldown:** 2 turns
- **Damage:** 28 base (1.8x DEX)
- **Debuff:** Bleed for 3 turns (6 DoT)
- **Purpose:** DoT damage finisher

**Level 5: Lamă Otrăvită** ☠️
- **Cost:** 25 Energy | **Cooldown:** 2 turns
- **Damage:** 18 base (1.4x DEX)
- **Debuff:** Poison for 4 turns (5 DoT)
- **Special:** Generates 15 Energy
- **Purpose:** Poison application + energy generation

**Level 8: Viteză Mortală** ⚡
- **Cost:** 40 Energy | **Cooldown:** 4 turns
- **Buff:** +40% DEX, +60% attack speed, +30% crit chance for 3 turns
- **Purpose:** Speed boost for DPS phase

**Level 10: Lovitură Dublă** 🗡️🗡️
- **Cost:** 35 Energy | **Cooldown:** 2 turns
- **Damage:** 20 base (1.5x DEX) × 2 hits = 40 total
- **Purpose:** Double strike

**Level 15: Vârtej de Lame** 🌪️
- **Cost:** 50 Energy | **Cooldown:** 3 turns
- **Damage:** 25 base (1.6x DEX) × 4 hits = 100 total
- **Special:** AOE, quad hit
- **Purpose:** AOE burst

**Level 20: Dispariție** 💨
- **Cost:** 45 Energy | **Cooldown:** 5 turns
- **Healing:** 20% max HP
- **Buff:** +150% dodge, +80% stealth bonus for 3 turns
- **Purpose:** Escape/reset ability with healing

**Level 25: Asasinare** 💀
- **Cost:** 60 Energy | **Cooldown:** 3 turns
- **Damage:** 70 base (3.0x DEX)
- **Debuff:** Deep Wound for 4 turns (10 DoT)
- **Combo:** Requires "Ascuns în Umbră" first → +75% damage if combo'd from stealth!
- **Purpose:** Assassinate combo (122.5 base when combo'd!)

**Level 30: Dans al Umbrelor (ULTIMATE)** 👥
- **Cost:** 100 Energy | **Cooldown:** 10 turns
- **Damage:** 45 base (2.5x DEX) × 8 hits = 360 total!
- **Debuff:** Bleed for 5 turns (15 DoT)
- **Healing:** 30% max HP (lifesteal theme)
- **Buff:** +80% DEX, +70% crit chance, +60% dodge for 4 turns
- **Purpose:** Ultimate shadow dance - massive multi-hit with healing

### **Rogue Gameplay Loop**
1. **Build Energy:** Use Lovitură Rapidă to generate energy
2. **Stealth Setup:** Activate Ascuns în Umbră for buffs
3. **Combos:** Ascuns în Umbră → Asasinare for massive burst
4. **DoT Damage:** Apply Sângerare and Lamă Otrăvită for sustained damage
5. **Burst Windows:** Activate Viteză Mortală for DPS phase
6. **Survival:** Use Dispariție to heal and reset
7. **Ultimate:** Dans al Umbrelor for massive damage + healing

---

## 🎮 Ability Progression Summary

### **Unlock Schedule**
- **Level 1:** 3 basic abilities (always available)
- **Level 3:** First advanced ability
- **Level 5:** Second advanced ability
- **Level 8:** Third advanced ability
- **Level 10:** Fourth advanced ability (usually AOE)
- **Level 15:** Fifth advanced ability (usually powerful buff)
- **Level 20:** Sixth advanced ability
- **Level 25:** Seventh advanced ability (combo finisher)
- **Level 30:** ULTIMATE ABILITY

Total: **11 abilities per class** (3 basic + 8 unlockable)

---

## 🔥 Best Combos by Class

### **Warrior Combos**
1. **Rage Builder:** Tăietură Sălbatică → Lovitură Devastatoare
2. **Stun Combo:** Șarjă Furioasă → Furie Primordială (+50% damage!)
3. **Berserker:** Furia Sângelui → Execuția (massive crit damage)
4. **Tank Mode:** Scut de Țară → Fortăreața Carpatină (unkillable)
5. **Ultimate Burst:** Furia Sângelui → Spiritul Dacilor

### **Mage Combos**
1. **Mana Sustain:** Sifon de Mană → spam high-cost spells
2. **Fire Combo:** Minge de Foc → Meteorit (+60% damage!)
3. **Buff Burst:** Putere Magică → Rază Prismatică
4. **AOE Clear:** Explozie Arcanică → Lanț de Fulgere
5. **Ultimate Nuke:** Putere Magică → Maelstrom Arcanic

### **Rogue Combos**
1. **Assassinate:** Ascuns în Umbră → Asasinare (+75% damage!)
2. **DoT Stack:** Sângerare → Lamă Otrăvită (11 DoT/turn!)
3. **Speed Burst:** Viteză Mortală → Lovitură Dublă × spam
4. **Stealth Reset:** Dispariție → Ascuns în Umbră → Asasinare
5. **Ultimate Burst:** Viteză Mortală → Dans al Umbrelor

---

## 📊 Damage Comparison (Level 30, Max Stats)

### **Single Target Burst (One Turn)**
1. **Mage Ultimate:** 630 base damage (7 hits)
2. **Warrior Ultimate:** 500 base damage (5 hits)
3. **Rogue Ultimate:** 360 base damage (8 hits)

### **Sustained DPS (Over 5 Turns)**
1. **Rogue:** High energy regen allows constant ability spam
2. **Warrior:** Moderate - depends on Rage generation
3. **Mage:** Lower - limited by mana pool

### **Survivability**
1. **Warrior:** Highest - tank buffs + high HP
2. **Rogue:** Medium - dodge + heal abilities
3. **Mage:** Lowest - relies on burst damage

---

## 🚀 Next Steps for Implementation

1. **Battle Service Updates:** Handle resource generation, combos, multi-hit, healing, self-damage
2. **UI Indicators:** Show ultimate availability, combo availability, resource bars
3. **Sound Effects:** Different sounds for ultimates, combos, multi-hit
4. **Ability Tooltips:** Show all mechanics (combo requirements, resource generation, etc.)
5. **Balance Testing:** Ensure no class is overpowered

---

## 💡 Design Philosophy

**Warrior:** High risk, high reward with Rage management and berserker mechanics
**Mage:** Strategic mana management with devastating burst potential
**Rogue:** Fast-paced combo gameplay with stealth assassination fantasy

Each class has:
- ✅ Unique resource mechanic
- ✅ 11 distinct abilities
- ✅ Combo system for skilled play
- ✅ Ultimate ability that feels epic
- ✅ Both offensive and defensive options
- ✅ Clear progression from level 1-30

Enjoy the new ability systems! ⚔️🔮🗡️
