# ⚔️ Advanced Ability Customization System

## 🎯 Core Design Philosophy

**Problem:** Current abilities are passive unlocks that all players use the same way.

**Solution:** Transform abilities into **customizable tools** with:
- **Variants** - Different versions of the same ability
- **Talent Trees** - Customize each ability individually
- **Loadout System** - Strategic pre-planning (6 ability limit)
- **Discovery** - Unlock abilities through gameplay, not just leveling

---

## 🏗️ System Architecture

### **1. Ability Variants** (Choose ONE per ability)

Each ability has 3 variants that fundamentally change how it works.

**Example: "Fireball" (Wizard Basic Attack)**

**Variant A: "Fireball" (Default)**
- Single target
- 80 damage
- 20 mana cost
- Burn: 10 damage/turn for 3 turns

**Variant B: "Firestorm"**
- Hits all enemies
- 40 damage each
- 40 mana cost
- No burn effect
- **Use Case:** Multi-enemy fights

**Variant C: "Inferno Bolt"**
- Single target
- 150 damage
- 50 mana cost
- No burn
- 2 turn cooldown
- **Use Case:** Boss burst damage

---

### **2. Ability Talent Trees** (Customize EACH ability)

Each ability has a small talent tree (3 tiers, choose 1 per tier).

**Example: "Fireball" Talent Tree**

```
TIER 1 (Choose 1):
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│ 🔥 Intense Heat  │  │ 💨 Swift Cast    │  │ 🧠 Mana Efficient│
│ +20% damage      │  │ -5 mana cost     │  │ -30% mana cost   │
│                  │  │ Cast time -50%   │  │ -30% damage      │
└──────────────────┘  └──────────────────┘  └──────────────────┘

TIER 2 (Choose 1):
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│ 🌊 Melt Armor    │  │ ⚡ Chain Fire    │  │ 🎯 Precision     │
│ -10 enemy DEF    │  │ 25% chance to    │  │ +15% crit chance │
│ for 2 turns      │  │ hit 2nd enemy    │  │ Crit = 2x burn   │
└──────────────────┘  └──────────────────┘  └──────────────────┘

TIER 3 (Choose 1):
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│ 💥 Explosive     │  │ 🔄 Cooldown Reset│  │ 🛡️ Flame Shield │
│ On kill: AOE     │  │ Kills refund     │  │ On cast: +20 DEF │
│ 30 damage burst  │  │ 50% mana cost    │  │ for 1 turn       │
└──────────────────┘  └──────────────────┘  └──────────────────┘
```

**Result:** 3 × 3 × 3 = **27 different builds** for ONE ability!

---

### **3. Loadout System** (6 Ability Limit)

**Before entering dungeon:** Choose which 6 abilities to bring.

**UI Mockup:**
```
┌─────────────────────────────────────────────────────────────┐
│  🎒 ABILITY LOADOUT - Choose 6 abilities for this run      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ACTIVE LOADOUT (6/6):                                      │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐   │
│  │  🔥  │ │  ⚡  │ │  ❄️  │ │  🌊  │ │  💨  │ │  ⭐  │   │
│  │Fire  │ │Light │ │Frost │ │Water │ │Wind  │ │Arcane│   │
│  │ball  │ │ning  │ │Nova  │ │Shield│ │Dash  │ │Blast │   │
│  └──────┘ └──────┘ └──────┘ └──────┘ └──────┘ └──────┘   │
│                                                             │
│  AVAILABLE ABILITIES (12 unlocked):                         │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐   │
│  │  🔥  │ │  ⚡  │ │  💀  │ │  🌟  │ │  🛡️  │ │  ⚔️  │   │
│  │Meteor│ │Chain │ │Curse │ │Heal  │ │Barrier│ │Weapon│   │
│  │      │ │      │ │      │ │      │ │      │ │Enchant│   │
│  └──────┘ └──────┘ └──────┘ └──────┘ └──────┘ └──────┘   │
│                                                             │
│  Click ability to view variants and talents                 │
│  Drag to swap abilities in loadout                         │
└─────────────────────────────────────────────────────────────┘
```

**Strategic Depth:**
- **General dungeon run:** Balanced offense/defense
- **Boss rush:** All burst damage abilities
- **Endless mode:** Sustain and crowd control
- **Fire-themed floor:** Bring ice abilities

---

### **4. Ability Discovery System**

**Methods to Unlock Abilities:**

#### **A. Level Unlocks (Basic abilities - 6 total)**
- Level 1: Starting ability
- Level 5, 10, 15, 20, 25: New ability unlock
- These are guaranteed, form the core kit

#### **B. Quest Rewards (4-6 abilities)**
**Example Quests:**

**"The Frozen Tomb" Quest:**
- NPC in town: "My brother is trapped in the ice caves..."
- Objective: Clear depth 15-20 without dying
- Reward: **"Glacial Prison"** ability
  - Freeze enemy for 2 turns, +50% damage when broken

**"Master's Challenge" Quest:**
- Trainer NPC: "Prove your worth in combat"
- Objective: Win 5 fights without using potions
- Reward: **"Second Wind"** ability
  - Heal 30% HP, costs all resource (emergency button)

#### **C. Secret Rooms (6-8 abilities)**
**Puzzle Room Example:**

```
┌─────────────────────────────┐
│  🗝️ PUZZLE ROOM            │
│                             │
│  Riddle: "I burn but give   │
│  no warmth, I light but     │
│  cast no shadow..."         │
│                             │
│  Options: Fire | Lightning  │
│           | Arcane          │
│                             │
│  (Correct: Arcane)          │
│                             │
│  Reward: "Arcane Missiles"  │
└─────────────────────────────┘
```

**Secret Room Example:**
- Hidden wall (found by exploration)
- Contains scroll pedestal
- Teaches: **"Shadow Step"** (teleport behind enemy, free attack)

#### **D. Boss Drops (Rare - 3-4 abilities)**
- 10% chance per boss to drop ability scroll
- Guaranteed at depths 25, 50, 75, 100

**Example:**
- Depth 25 Boss: "Vampire Lord"
- Drops: **"Lifesteal Strike"** - Deal damage, heal for 50% of damage dealt

#### **E. Achievement Unlocks (4-5 abilities)**

**"Untouchable" Achievement:**
- Requirement: Win 20 battles without taking damage
- Reward: **"Perfect Dodge"** - Next 3 attacks auto-dodge

**"Pyromaniac" Achievement:**
- Requirement: Deal 10,000 fire damage total
- Reward: **"Combustion"** - All burn effects explode for instant damage

---

## 📊 Example Ability Pool

### **Moldovean (Warrior) - 20 Total Abilities**

#### **Core Abilities (Level Unlocks):**
1. **Cleave** (Lvl 1) - Basic attack, 2 enemies
2. **Battle Shout** (Lvl 5) - +20% damage for 3 turns
3. **Shield Block** (Lvl 10) - Reduce next damage by 50%
4. **Whirlwind** (Lvl 15) - Hit all enemies for 60% damage
5. **Execute** (Lvl 20) - Massive damage below 30% HP
6. **Berserker Rage** (Lvl 25) - Sacrifice HP for damage

#### **Quest Rewards:**
7. **Second Wind** - Quest: "Survivor's Trial"
8. **Armor Break** - Quest: "Tank Buster"
9. **Rally** - Quest: "Leader's Path"
10. **Intimidate** - Quest: "Fear Incarnate"

#### **Secret/Puzzle Rooms:**
11. **Leap Attack** - Secret: Depth 8 hidden room
12. **Ground Slam** - Puzzle: "Shake the earth"
13. **Bloodlust** - Secret: Depth 18
14. **Taunt** - Puzzle: "Draw their ire"

#### **Boss Drops:**
15. **Titan's Grip** - Depth 25 boss (guaranteed)
16. **Unstoppable** - Depth 50 boss (guaranteed)

#### **Achievement Unlocks:**
17. **Last Stand** - Achievement: "Clutch Victory"
18. **Counterattack** - Achievement: "Retribution"
19. **Warcry** - Achievement: "Warrior's Spirit"
20. **Weapon Master** - Achievement: "Arsenal"

---

## 🎮 Gameplay Flow

### **1. Town - Ability Management**

**New UI Tab in Character Menu:**

```
┌─────────────────────────────────────────────────────────────┐
│  ⚔️ ABILITIES                                               │
├───────────────┬─────────────────────────────────────────────┤
│               │                                             │
│  UNLOCKED:    │  📖 FIREBALL (Selected)                    │
│               │                                             │
│  🔥 Fireball  │  Variant: [Fireball ▼]                     │
│  ⚡ Lightning │    ○ Fireball (Default)                     │
│  ❄️ Frost Nova│    ○ Firestorm (AOE)                       │
│  🌊 Ice Shield│    ○ Inferno Bolt (Single target burst)    │
│  💨 Wind Dash │                                             │
│  ⭐ Arcane    │  Talents:                                   │
│  🔥 Meteor    │    Tier 1: [Intense Heat ✓] +20% damage   │
│  ⚡ Chain     │    Tier 2: [Melt Armor ✓] -10 enemy DEF   │
│  💀 Curse     │    Tier 3: [Explosive ✓] On kill AOE      │
│               │                                             │
│  LOCKED:      │  Description:                               │
│  🌟 Heal      │  Launch a bolt of fire at target enemy.    │
│  🛡️ Barrier   │  Deals 96 damage (+20% from talent)       │
│  ⚔️ Weapon En │  Burns for 10/turn × 3 turns              │
│               │  On kill: Explode for 30 AOE damage       │
│               │                                             │
│               │  Mana Cost: 20                              │
│               │  Cooldown: None                             │
│               │                                             │
│               │  [Save Changes]                             │
└───────────────┴─────────────────────────────────────────────┘
```

**Actions:**
- Select ability → Choose variant
- Customize talent tree
- Save configuration

### **2. Pre-Dungeon - Loadout Selection**

**Before clicking "Enter Dungeon":**

```
┌─────────────────────────────────────────────────────────────┐
│  🏰 PREPARE FOR DUNGEON                                     │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  SELECT YOUR LOADOUT (6 abilities):                         │
│                                                             │
│  Slot 1: [🔥 Fireball        ▼] - Main damage             │
│  Slot 2: [⚡ Lightning Bolt  ▼] - Fast cast               │
│  Slot 3: [❄️ Frost Nova      ▼] - AOE control             │
│  Slot 4: [🌊 Ice Shield      ▼] - Defense                 │
│  Slot 5: [💨 Wind Dash       ▼] - Mobility                │
│  Slot 6: [⭐ Arcane Blast    ▼] - Utility                 │
│                                                             │
│  Quick Templates:                                           │
│  [Boss Rush] [Balanced] [Survival] [AOE Farm]              │
│                                                             │
│  [Enter Dungeon] [Back to Town]                            │
└─────────────────────────────────────────────────────────────┘
```

**Templates:**
- **Boss Rush:** All single-target burst
- **Balanced:** Mix of offense/defense
- **Survival:** Sustain and healing
- **AOE Farm:** Multi-target abilities

### **3. In Combat - Use Your Loadout**

Only the 6 selected abilities appear in combat UI.

```
Combat Actions:
[⚔️ ATTACK]  [🏃 FLEE]

Abilities (3/6 available):
[🔥 Fireball] [⚡ Lightning] [❄️ Frost]
[🌊 Shield] [💨 Dash] [⭐ Arcane]

Potions:
[🍺 Berice] [⚡ Energizant]
```

---

## 🎲 Ability Design Framework

### **Categorize Abilities by Role:**

#### **1. Damage Dealers (30%)**
- Single target burst
- AOE damage
- Damage over time

#### **2. Control/Utility (25%)**
- Stuns/slows
- Debuffs
- Positioning

#### **3. Defense (20%)**
- Shields/armor
- Healing
- Damage reduction

#### **4. Resource Management (15%)**
- Mana/rage/energy generation
- Cooldown reduction
- Cost reduction

#### **5. Mobility (10%)**
- Dashes/teleports
- Repositioning
- Escape tools

---

## 🔧 Balancing Principles

### **1. Power Budget**

Each ability gets "power points" to spend:

**100 Power Points Example:**
- Damage: 80 points → 80 damage
- Mana Cost: -20 points → 20 mana

**Alternative Build:**
- Damage: 40 points → 40 damage
- Utility: 30 points → Slow effect
- AOE: 20 points → Hits 3 enemies
- Mana Cost: -10 points → 10 mana

### **2. Situational Power**

**General abilities** should be weaker than **situational abilities**:

- **Fireball:** 80 damage, always useful
- **Frostbolt:** 60 damage + slow, useful vs melee
- **Undead Bane:** 150 damage, ONLY vs undead enemies

### **3. No Strict Upgrades**

**Bad Design:**
- Fireball: 80 damage
- Fireball II: 120 damage (same ability, bigger numbers)

**Good Design:**
- Fireball: 80 damage, reliable
- Meteor: 200 damage, 3 turn cooldown, 50 mana (different use case)

---

## 📅 Implementation Plan

### **Phase 1: Data Structures (2-3 days)**

**New Classes:**

```java
public class AbilityVariant {
    private String id;
    private String name;
    private String description;
    private int basePower;
    private int manaCost;
    // ... ability effects
}

public class AbilityTalent {
    private String id;
    private String name;
    private String description;
    private int tier; // 1, 2, or 3
    private AbilityModifier modifier;
}

public class AbilityLoadout {
    private List<ConfiguredAbility> activeAbilities; // Max 6
    private Map<String, ConfiguredAbility> unlockedAbilities;
}

public class ConfiguredAbility {
    private Abilitate baseAbility;
    private AbilityVariant selectedVariant;
    private AbilityTalent tier1Talent;
    private AbilityTalent tier2Talent;
    private AbilityTalent tier3Talent;
}
```

### **Phase 2: Ability Discovery (2 days)**

**Quest System Integration:**
```java
public class AbilityQuest extends Quest {
    private String abilityRewardId;

    @Override
    public void onComplete(Erou hero) {
        hero.unlockAbility(abilityRewardId);
        // Show UI notification
    }
}
```

**Secret Room Integration:**
```java
public class AbilityScrollObject extends InteractiveObject {
    private String abilityId;

    @Override
    public void onInteract(Erou hero) {
        if (!hero.hasAbility(abilityId)) {
            hero.unlockAbility(abilityId);
            showDiscoveryAnimation();
        }
    }
}
```

### **Phase 3: UI - Ability Management (3-4 days)**

**New Controller:**
```java
public class AbilityCustomizationController {
    private Erou hero;

    public Scene createScene() {
        // Left panel: Ability list
        // Right panel: Variant + talent selection
        // Bottom: Save/cancel buttons
    }
}
```

### **Phase 4: UI - Loadout Selection (2 days)**

**Integrate into DungeonController:**
```java
public class DungeonController {

    private void showLoadoutSelection() {
        // Before entering dungeon
        // Show 6-slot loadout UI
        // Save selected abilities
    }

    private void enterDungeon() {
        // Only use abilities in loadout
    }
}
```

### **Phase 5: Combat Integration (2-3 days)**

**Update BattleControllerFX:**
```java
public class BattleControllerFX {

    private void createAbilityButtons() {
        // Only show loadout abilities (6 max)
        // Apply variant effects
        // Apply talent modifiers
    }

    private void executeAbility(ConfiguredAbility ability) {
        // Use variant power
        // Apply talent bonuses
        // Calculate final damage/effects
    }
}
```

### **Phase 6: Content Creation (5-7 days)**

**Create 20 abilities per class:**
- 6 core (level unlocks)
- 4 quest rewards
- 6 secret rooms
- 2 boss drops
- 2 achievements

**Design 3 variants per ability:**
- 20 abilities × 3 variants = 60 variant designs

**Design talent trees:**
- 20 abilities × 9 talents (3 tiers × 3 options) = 180 talents

---

## 🎯 Example: Complete Ability Design

### **"CLEAVE" - Warrior Basic Attack**

#### **Variants:**

**A. "Cleave" (Default)**
- Hits 2 adjacent enemies
- 70 damage each
- 10 rage cost
- **Use:** General farming

**B. "Focused Strike"**
- Single target
- 140 damage
- 10 rage cost
- **Use:** Boss killing

**C. "Sweeping Blade"**
- Hits ALL enemies
- 35 damage each
- 20 rage cost
- **Use:** Large mob groups

#### **Talent Tree:**

**Tier 1:**
- **Bleeding Edge:** +15% damage, applies bleed (5 damage/turn × 3)
- **Rage Gain:** Hits generate +3 rage each
- **Quick Strike:** -5 rage cost

**Tier 2:**
- **Armor Shatter:** -15 enemy defense for 2 turns
- **Cleaving Momentum:** Each kill reduces cooldown by 1 turn
- **Wide Arc:** +1 enemy hit (Cleave hits 3, Sweeping hits 5)

**Tier 3:**
- **Execute:** +100% damage vs enemies below 30% HP
- **Lifesteal:** Heal for 25% of damage dealt
- **Rage Dump:** Spend extra rage for +10% damage per rage

#### **Build Examples:**

**"Bleed Build":**
- Variant: Cleave (2 enemies)
- Talents: Bleeding Edge + Wide Arc + Lifesteal
- Result: Hit 3 enemies, apply bleeds, sustain HP

**"Boss Killer":**
- Variant: Focused Strike (single target)
- Talents: Bleeding Edge + Armor Shatter + Execute
- Result: Massive single-target burst

**"Rage Engine":**
- Variant: Sweeping Blade (all enemies)
- Talents: Rage Gain + Cleaving Momentum + Rage Dump
- Result: Generate and spend rage rapidly

---

## 💡 Discovery Examples

### **Quest: "The Blacksmith's Secret"**

**NPC:** Fierarul (Blacksmith in town)

**Dialogue:**
> "My grandfather was a legendary warrior. He developed a technique called 'Titan's Grip' that let him wield massive weapons with one hand. I found his notes in the forge... if you prove yourself in combat, I'll teach you."

**Requirements:**
- Reach level 15
- Deal 500 damage in a single hit
- Return to blacksmith

**Reward:** **"Titan's Grip"** ability
- Passive: Two-handed weapons can be wielded in one hand
- Active: +50% weapon damage for 3 turns, 30 rage cost

---

### **Secret Room: "Ancient Library"**

**Location:** Depth 12, hidden wall in east corridor

**Description:**
```
You find a dusty library with ancient tomes.
One book glows with arcane energy...

📖 "The Codex of Teleportation"

[Take Book] [Leave]
```

**Reward:** **"Blink"** ability
- Teleport to target location
- Next attack from teleport location deals +50% damage
- 25 mana, 2 turn cooldown

---

## ✨ Summary

This system creates:

✅ **Strategic Depth** - Loadout choices matter
✅ **Build Diversity** - 27 builds per ability × 20 abilities = massive variety
✅ **Exploration Rewards** - Secrets give meaningful rewards
✅ **Replayability** - Different loadouts for different challenges
✅ **Player Expression** - "This is MY fireball build"

**Total Customization Space:**
- 20 abilities to discover
- 3 variants each (60 choices)
- 27 talent combinations each (540 builds per ability)
- 6-ability loadouts (millions of combinations)

**This transforms the game from "use all abilities" to "craft your playstyle"!**

---

## 🚀 Ready to Implement?

I can start coding:
1. **Data structures** (AbilityVariant, Talent, Loadout classes)
2. **Ability management UI** (customize variants/talents)
3. **Loadout selection UI** (pre-dungeon)
4. **Discovery system** (quests, secrets, achievements)
5. **Example abilities** (complete Fireball, Cleave, Lightning with all variants/talents)

Which part should I start with?
