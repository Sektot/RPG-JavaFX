# 🎮 Ability System - Complete Player Flow

## Visual Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                         GAME START                                   │
│                    Create Character                                  │
│              (Ardelean / Moldovean / Oltean)                        │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
                 ✨ Starting Ability Unlocked
              (Fireball / Cleave / Lightning Bolt)
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         TOWN MENU                                    │
│  🏛️ ORAȘUL BUCUREȘTI                                               │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ 🗺️  Enter the Dungeon                                        │   │
│  │ 🏛️  Dungeon Upgrades Shop                                    │   │
│  │ 🛍️  Advanced Shop                                            │   │
│  │ 🔨 Fierăria                                                  │   │
│  │ 🧙‍♂️ Alchemy Workshop                                         │   │
│  │ 🍺 Taverna                                                   │   │
│  │ 👤 Character                                                 │   │
│  │ ⚡ Customize Abilities  ← NEW! STEP 1                        │   │
│  │ 💾 Save Game                                                 │   │
│  │ ⚙️  Options                                                   │   │
│  └─────────────────────────────────────────────────────────────┘   │
└──────────┬──────────────────────────────┬────────────────────────────┘
           │                              │
           │                              │
    Click "⚡ Customize"          Click "🗺️ Dungeon"
           │                              │
           ▼                              ▼
┌──────────────────────────┐    ┌─────────────────────────┐
│  ABILITY CUSTOMIZATION   │    │   SELECT DEPTH          │
│  SCREEN                  │    │   (1-10)                │
│  ┌────────────────────┐  │    └────────┬────────────────┘
│  │ Unlocked Abilities │  │             │
│  │ ───────────────────│  │             ▼
│  │ • Fireball         │  │    ┌─────────────────────────┐
│  │ • Lightning Bolt   │  │    │  LOADOUT SELECTION      │
│  │ • Săgeată Arcanică │  │    │  ← NEW! STEP 2          │
│  └────────────────────┘  │    │  ┌──────────────────┐   │
│                          │    │  │ Choose 6 abilities│   │
│  ┌────────────────────┐  │    │  │ from unlocked    │   │
│  │ VARIANT SELECTION  │  │    │  │                  │   │
│  │ ○ Fireball         │  │    │  │ [1] Fireball     │   │
│  │ ● Firestorm (AOE)  │  │    │  │ [2] Lightning    │   │
│  │ ○ Inferno Bolt     │  │    │  │ [3] Săgeată      │   │
│  └────────────────────┘  │    │  │ [4] Barieră      │   │
│                          │    │  │ [5] Minge Foc    │   │
│  ┌────────────────────┐  │    │  │ [6] Gheață       │   │
│  │ TALENT TREE        │  │    │  └──────────────────┘   │
│  │ Tier 1:            │  │    │                         │
│  │ ● Intense Heat     │  │    │  📋 Templates:          │
│  │ ○ Swift Cast       │  │    │  • Balanced             │
│  │ ○ Mana Efficient   │  │    │  • Boss Rush            │
│  │                    │  │    │  • AOE Farm             │
│  │ Tier 2:            │  │    │  • Survival             │
│  │ ○ Melt Armor       │  │    │                         │
│  │ ● Chain Fire       │  │    │  [Enter Dungeon]        │
│  │ ○ Precision        │  │    │  [Cancel]               │
│  │                    │  │    └────┬──────────┬─────────┘
│  │ Tier 3:            │  │         │          │
│  │ ● Explosive        │  │    Confirm   Cancel
│  │ ○ CD Reset         │  │         │          │
│  │ ○ Flame Shield     │  │         ▼          ▼
│  └────────────────────┘  │    ┌─────────┐  Return
│                          │    │ DUNGEON │  to Town
│  ┌────────────────────┐  │    │ COMBAT  │
│  │ STATS PREVIEW      │  │    │         │
│  │ Damage: 96         │  │    │ Only 6  │
│  │ Mana: 40           │  │    │ selected│
│  │ Cooldown: 0        │  │    │ abilities│
│  │ Crit: 5%           │  │    │ available│
│  └────────────────────┘  │    └─────────┘
│                          │
│  [Save Changes]          │
│  [Back to Town]          │
└──────┬───────────────────┘
       │
       ▼
   Return to Town
```

---

## 🎯 System Flow Points

### Point 1: Starting Abilities

**Trigger:** Character creation
**Location:** `Ardelean.initializeazaAbilitati()` / `Moldovean.initializeazaAbilitati()`

```java
// Automatically unlocks starting ability
ConfiguredAbility fireball = AbilityDefinitions.createDefaultConfiguredAbility(...);
this.unlockConfiguredAbility(fireball);
this.addAbilityToLoadout("Fireball");
```

**Result:** Character starts with 1 ability unlocked and in loadout

---

### Point 2: Level-Up Unlocks

**Trigger:** Reaching specific levels
**Location:** `abilitateSpecialaNivel(int nivel)` in character classes

**Wizard (Ardelean):**
- Level 1: Fireball (3 variants, 9 talents)
- Level 5: Lightning Bolt (3 variants, 9 talents)

**Warrior (Moldovean):**
- Level 1: Cleave (3 variants, 9 talents)

**Result:** New ConfiguredAbility unlocked and added to loadout (if space)

---

### Point 3: Town Menu Integration ✅ NEW

**Location:** `TownMenuController.createMenu()`
**Button:** "⚡ Customize Abilities"

```java
Button abilityCustomBtn = createMenuButton("⚡ Customize Abilities", "#e94560");
abilityCustomBtn.setOnAction(e -> {
    AbilityCustomizationController controller = new AbilityCustomizationController(
        stage, hero, createScene()
    );
    stage.setScene(controller.createScene());
});
```

**Result:** Opens ability customization screen

---

### Point 4: Ability Customization ✅ INTEGRATED

**Controller:** `AbilityCustomizationController`

**Features:**
- View all unlocked abilities (left panel)
- Select ability to customize
- Choose variant (3 options per ability)
- Select talents (3 tiers × 3 options)
- Real-time stats preview
- Save changes

**Navigation:**
- Entry: Town Menu → "⚡ Customize Abilities"
- Exit: "Back to Town" → Returns to town menu

---

### Point 5: Dungeon Entry Flow ✅ INTEGRATED

**Location:** `TownMenuController.handleDungeonMain()`

**New Flow:**
1. Click "🗺️ Enter the Dungeon"
2. Select depth (1-10)
3. **🆕 Loadout Selection opens automatically**
4. Choose 6 abilities for combat
5. Confirm → Enter dungeon
6. Cancel → Return to town

```java
LoadoutSelectionController loadoutController = new LoadoutSelectionController(
    stage, hero,
    (enterDungeon) -> {
        if (enterDungeon) {
            // Launch dungeon
        } else {
            // Return to town
        }
    }
);
```

---

### Point 6: Loadout Selection ✅ INTEGRATED

**Controller:** `LoadoutSelectionController`

**Features:**
- Drag & drop ability management
- 6-slot active loadout
- Quick templates:
  - Balanced (mix of damage, utility, defense)
  - Boss Rush (single-target burst)
  - AOE Farm (multi-target clearing)
  - Survival (defensive + sustain)
- Save/load custom templates
- Ability preview with stats

**Navigation:**
- Entry: Dungeon depth selection → Automatic
- Exit: "Enter Dungeon" → Launch combat
- Exit: "Cancel" → Return to town

---

### Point 7: Combat Integration

**Location:** `BattleServiceFX`

**Combat Behavior:**
```java
if (hero.hasValidLoadout() && hero.getLoadoutSize() > 0) {
    // NEW SYSTEM: Use ConfiguredAbility from loadout
    List<ConfiguredAbility> loadout = hero.getActiveLoadoutAbilities();
    // Only these 6 abilities available
    // Final damage/mana from talents applied
} else {
    // OLD SYSTEM: Fallback for backward compatibility
    // Use all abilities from hero.getAbilitati()
}
```

**Result:**
- Only loadout abilities available in combat
- Final stats (damage, mana) reflect customization
- Max 6 abilities per battle

---

## 🔄 Complete Player Journey Example

### Day 1: Character Creation
1. Create Ardelean (Wizard) - "TestWizard"
2. ✨ Starting ability unlocked: **Fireball**
3. Auto-added to loadout [1/6]

### Day 2: Customization
1. Town Menu → "⚡ Customize Abilities"
2. Select Fireball
3. Change variant: Fireball → **Firestorm** (AOE)
4. Add talents:
   - T1: **Intense Heat** (+20% damage)
   - T2: **Chain Fire** (chain to 2nd enemy)
   - T3: **Explosive** (AOE on kill)
5. Final stats: 96 damage, 40 mana, AOE all enemies
6. Save changes → Return to town

### Day 3: First Dungeon Run
1. Town Menu → "🗺️ Enter the Dungeon"
2. Select depth: 1
3. **Loadout Selection opens**
4. Current loadout: [Firestorm (customized)]
5. Click "Enter Dungeon"
6. Combat: Only Firestorm available
7. Damage: 96 (with +20% talent)
8. Mana cost: 40
9. Hits all enemies (AOE variant)

### Day 4: Level Up
1. During combat, reach Level 5
2. ✨ New ability unlocked: **Lightning Bolt**
3. Auto-added to loadout [2/6]

### Day 5: Build Two Abilities
1. Town → "⚡ Customize Abilities"
2. Customize Lightning Bolt:
   - Variant: **Chain Lightning**
   - T1: High Voltage (+30% damage)
   - T2: Arc Discharge (bounces +1 target)
   - T3: Thunderstorm (AOE on crit)
3. Save → Return to town

### Day 6: Strategic Loadout
1. Town → "🗺️ Dungeon" → Depth 3
2. Loadout Selection:
   - [1] Firestorm (AOE clear)
   - [2] Chain Lightning (multi-target)
   - [3] Săgeată Arcanică (single-target)
   - [4] Barieră Magică (defense)
   - [5] (empty)
   - [6] (empty)
3. Template: "AOE Farm" for efficiency
4. Enter Dungeon → Strategic combat with 4 abilities

---

## 📊 System Integration Summary

### ✅ Completed Integration:

| Component | Status | Location |
|-----------|--------|----------|
| **Ability Customization UI** | ✅ Integrated | Town Menu Button |
| **Loadout Selection UI** | ✅ Integrated | Pre-Dungeon Flow |
| **Town Menu Button** | ✅ Added | TownMenuController.java |
| **Dungeon Entry Flow** | ✅ Modified | handleDungeonMain() |
| **Combat System** | ✅ Already Integrated | BattleServiceFX.java |
| **Level-Up Unlocks** | ✅ Working | Character classes |
| **Starting Abilities** | ✅ Working | initializeazaAbilitati() |

### 🎯 User Experience:

- **Customization:** Accessible anytime from town
- **Loadout Selection:** Automatic before dungeon entry
- **Strategic Depth:** Choose 6 from 20+ abilities (when expanded)
- **Build Variety:** 27 builds per ability (3 variants × 9 talent combinations)
- **Player Agency:** "This is MY custom Fireball build"

---

## 🚀 Ready for Production

The complete ability system flow is now fully integrated and ready for players to experience!

**Next Phase:** Implement talent special effects in combat (lifesteal, bleed, chains, etc.)
