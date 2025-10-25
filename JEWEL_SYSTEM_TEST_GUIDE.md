# 💎 Jewel System Testing Guide

## Overview
This guide will help you systematically test the complete jewel system implementation.

---

## 🎯 Test Checklist

### ✅ Phase 1: Basic Functionality

#### 1.1 Jewel Creation & Display
- [ ] Test jewels are created with correct properties
- [ ] Jewel tooltips show all modifiers
- [ ] Jewel rarity colors display correctly
- [ ] Jewel type icons appear properly

**How to Test:**
```java
// Add to your main game startup or create a test method:
JewelTestUtility.addTestJewelsToHero(hero);
JewelTestUtility.printJewelInventoryStats(hero);
```

---

#### 1.2 Jewel Inventory Management
- [ ] Jewels are added to separate `jewelInventory` list
- [ ] `getAvailableJewels()` returns only unsocketed jewels
- [ ] `getSocketedJewels()` returns only socketed jewels
- [ ] Jewel count is accurate

**How to Test:**
```java
// Check counts
System.out.println("Total: " + hero.getJewelCount());
System.out.println("Available: " + hero.getAvailableJewels().size());
System.out.println("Socketed: " + hero.getSocketedJewels().size());
```

---

### ✅ Phase 2: Combat Integration

#### 2.1 Boss Jewel Drops
- [ ] Bosses have ~40% chance to drop jewels
- [ ] Jewel level matches boss level approximately
- [ ] Drop notification displays correctly
- [ ] Jewel is added to inventory automatically

**How to Test:**
1. Fight multiple boss battles
2. Check battle log for "💎 JEWEL DROP!" message
3. Verify jewel appears in inventory
4. Run automated test:
```java
JewelTestUtility.testJewelDrops(hero);
```

---

#### 2.2 Regular Enemy Drops
- [ ] Regular enemies have ~5% chance to drop jewels
- [ ] Drop rate feels balanced (not too common)
- [ ] Jewels match enemy level

**How to Test:**
1. Fight 20+ regular enemies
2. Expect 1-2 jewel drops statistically
3. Check inventory after battles

---

#### 2.3 Victory Screen Display
- [ ] Victory screen shows jewel drops
- [ ] Jewel name and rarity displayed
- [ ] Modifier count shown

**Expected Output:**
```
💎 JEWEL DROP!
  • Radiant Crimson Jewel
    Rare | 4 mods
```

---

### ✅ Phase 3: Shop Integration

#### 3.1 Shop Category
- [ ] "💎 Bijuterii (Jewels)" category appears in shop
- [ ] Category opens without errors
- [ ] 5-8 jewels are displayed

**How to Test:**
1. Open shop
2. Navigate to Jewels category
3. Count visible jewels

---

#### 3.2 Jewel Display in Shop
- [ ] Jewel names show with type icon
- [ ] Description shows type, rarity, level
- [ ] First 3 modifiers preview displayed
- [ ] "... and X more" shown for jewels with >3 mods
- [ ] Price displayed correctly

**Expected Format:**
```
🔴 Radiant Crimson Jewel
Crimson Jewel | Rare
Level 5 | 4 modifiers

• +6.5% Increased Maximum HP
• +4.2% Increased Defense
• +5.1% Increased Damage
... and 1 more
```

---

#### 3.3 Jewel Purchasing
- [ ] Can purchase jewels with enough gold
- [ ] Gold is deducted correctly
- [ ] Jewel appears in inventory after purchase
- [ ] "Not enough gold" message if insufficient funds
- [ ] Purchase confirmation shows jewel name

**How to Test:**
1. Note current gold amount
2. Purchase a jewel
3. Verify gold decreased by jewel price
4. Check jewel is in inventory
5. Try purchasing without enough gold

---

#### 3.4 Shop Restock
- [ ] New jewels generate on shop restock
- [ ] Old jewels are cleared from cache
- [ ] Jewel variety changes after restock

---

### ✅ Phase 4: Talent Tree Integration

#### 4.1 Jewel Socket Nodes
- [ ] 3 jewel socket nodes visible in talent tree
- [ ] Sockets have purple color (200, 100, 255)
- [ ] Socket positions are between major paths
- [ ] Socket nodes show "💎 Jewel Socket" name

**Socket Locations:**
- Socket 1: Between STR & DEX paths (top)
- Socket 2: Between DEX & INT paths (right)
- Socket 3: Between INT & STR paths (left)

---

#### 4.2 Socket Allocation
- [ ] Left-click allocates socket (costs 1 passive point)
- [ ] Socket becomes selectable after allocation
- [ ] Tooltip shows "Empty Socket" when allocated but empty

---

#### 4.3 Jewel Insertion
- [ ] Right-click allocated socket opens jewel dialog
- [ ] Dialog shows only available (unsocketed) jewels
- [ ] "No Jewels Available" message if inventory empty
- [ ] Jewel list shows type icon, name, rarity
- [ ] Modifiers display in list view
- [ ] "Socket Jewel" button works

**How to Test:**
1. Allocate a jewel socket node (left-click, costs 1 passive point)
2. Right-click the allocated socket
3. Select a jewel from the dialog
4. Click "Socket Jewel"

---

#### 4.4 Jewel Removal
- [ ] Right-click socketed jewel opens removal dialog
- [ ] Confirmation shows jewel name
- [ ] "Remove Jewel" button works
- [ ] Jewel returns to available inventory
- [ ] Socket becomes empty again

---

#### 4.5 Tooltip Updates
- [ ] Empty socket shows: "💎 Empty Socket"
- [ ] Socketed jewel shows: "💎 SOCKETED: {name}"
- [ ] Instructions show for insertion/removal
- [ ] Socket status updates immediately

---

### ✅ Phase 5: Bonus Application

#### 5.1 Stat Bonuses
- [ ] `hp_percent` increases maximum HP
- [ ] `defense_percent` increases defense
- [ ] `damage_percent` increases damage
- [ ] `str_bonus`, `dex_bonus`, `int_bonus` increase stats
- [ ] `all_stats` increases all three stats

**How to Test:**
```java
// Before socketing
int oldHP = hero.getViataMaxima();
int oldSTR = hero.getStrength();

// Socket a jewel with +8% HP and +5 STR

// After socketing
int newHP = hero.getViataMaxima();
int newSTR = hero.getStrength();

// Verify increases
System.out.println("HP increased: " + (newHP > oldHP));
System.out.println("STR increased by 5: " + (newSTR == oldSTR + 5));
```

---

#### 5.2 Combat Bonuses
- [ ] `crit_chance` increases crit rate
- [ ] `crit_multiplier` increases crit damage
- [ ] `dodge_chance` increases dodge rate
- [ ] `attack_speed` increases attack speed
- [ ] `lifesteal` increases life steal

**How to Test:**
1. Note combat stats before socketing
2. Socket jewel with combat bonuses
3. Verify stats increased in character sheet
4. Test in actual combat

---

#### 5.3 Summary Panel
- [ ] Summary panel shows total bonuses including jewels
- [ ] "💎 X Jewel(s)" appears when jewels socketed
- [ ] Stat totals are accurate
- [ ] Updates immediately when socketing/removing

**Expected Format:**
```
💪 +25 STR  |  🎯 +18 DEX  |  🧠 +15 INT  |  ⚔️ +35% DMG  |  ❤️ +25% HP  |  💎 2 Jewels
```

---

#### 5.4 Bonus Removal
- [ ] Removing jewel removes all bonuses
- [ ] Stats return to pre-socket values
- [ ] No lingering bonus effects

---

### ✅ Phase 6: Save/Load System

#### 6.1 Saving
- [ ] Jewel inventory saves correctly
- [ ] Socketed status persists
- [ ] Jewel modifiers save
- [ ] Socket allocations save

**How to Test:**
1. Add jewels to inventory
2. Socket some jewels
3. Save game
4. Check save file for jewel data

---

#### 6.2 Loading
- [ ] Jewels load from save file
- [ ] Socketed jewels restore to correct nodes
- [ ] Bonuses re-apply on load
- [ ] Jewel inventory intact

**How to Test:**
1. Load a save with jewels
2. Verify jewel count matches
3. Check talent tree shows socketed jewels
4. Verify stats reflect jewel bonuses

---

### ✅ Phase 7: Edge Cases & Error Handling

#### 7.1 Empty States
- [ ] Opening talent tree with no passive points works
- [ ] Right-clicking socket with no jewels shows proper message
- [ ] Shop with no jewels generated doesn't crash
- [ ] Empty jewel inventory doesn't cause errors

---

#### 7.2 Maximum Capacity
- [ ] Can socket all 3 jewel sockets
- [ ] Can't socket more than 3 jewels total
- [ ] Jewel inventory can hold many jewels (test with 20+)

---

#### 7.3 Invalid Operations
- [ ] Can't socket a jewel that's already socketed
- [ ] Can't remove jewel from unallocated socket
- [ ] Can't allocate socket without passive points
- [ ] Can't purchase jewel without enough gold

---

### ✅ Phase 8: Performance & UX

#### 8.1 Performance
- [ ] Talent tree opens quickly
- [ ] Jewel dialog opens without lag
- [ ] Socketing/removing is instant
- [ ] Shop jewel generation is fast

---

#### 8.2 User Experience
- [ ] All interactions feel responsive
- [ ] Tooltips are informative
- [ ] Error messages are clear
- [ ] Visual feedback is adequate

---

## 🐛 Known Issues to Check

1. **Mouse Wheel Scrolling**: Verify mouse wheel only zooms (doesn't scroll)
2. **Node Accessibility**: All nodes should be reachable
3. **Background Color**: Should be solid black everywhere
4. **Jewel Socket Visibility**: Sockets should be visible and clickable

---

## 🔧 Quick Test Commands

Add these to a test menu or method:

```java
// Quick jewel inventory test
JewelTestUtility.addTestJewelsToHero(hero);
JewelTestUtility.printJewelInventoryStats(hero);

// Test drop system
JewelTestUtility.testJewelDrops(hero);

// Verify bonuses
JewelTestUtility.verifyJewelBonuses(hero);
```

---

## 📊 Success Criteria

The jewel system is fully functional when:
- ✅ All combat drops work (boss & regular)
- ✅ Shop purchasing works without errors
- ✅ Socketing/removal works smoothly
- ✅ All bonuses apply correctly
- ✅ Save/load preserves jewel data
- ✅ No crashes or game-breaking bugs
- ✅ User experience feels polished

---

## 🚀 Next Steps After Testing

Once all tests pass:
1. Balance jewel drop rates if needed
2. Adjust jewel modifier values for balance
3. Consider implementing Ascendancy Classes
4. Add quest rewards and secret rooms for jewels
5. Expand with unique/corrupted jewels

---

**Good luck with testing! Report any bugs you find.** 💎✨
