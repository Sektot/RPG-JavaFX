# 🧪 Ability System Testing Guide

**Last Updated**: 2025-10-30
**Purpose**: Comprehensive guide for testing the 9 new abilities and 40+ special effects

---

## 🚀 Quick Start: How to Test

### Step 1: Run the Game
```bash
# In your terminal, navigate to the project directory
cd C:\Users\danta\IdeaProjects\test

# Run the JavaFX application
./mvnw javafx:run
```

### Step 2: Create a GOD MODE Character
1. Click **"🎮 Joc Nou"** (New Game)
2. Enter any character name (e.g., `TestHero`, `Wizard1`, `MyWarrior`)
3. **CHECK THE "⚡ GOD MODE (Testing)" CHECKBOX** - This is the important part!
4. Select your class:
   - **🔮 ARDELEAN** (Wizard) - Test Ice Shard, Arcane Missiles, Meteor Strike
   - **⚔️ MOLDOVEAN** (Warrior) - Test Shield Bash, Whirlwind, Execute
   - **🗡️ OLTEAN** (Rogue) - Test Backstab, Poison Blade, Shadow Step
5. Click **"✨ Creează Erou ✨"** (Create Hero)

**Note**: You can also enable GOD MODE by naming your character starting with "GOD" or "TEST" (e.g., `GOD`, `TestWizard`), but the checkbox is easier!

**GOD MODE Stats:**
- ⚡ Level 30 (all abilities unlocked)
- 💰 50,000 Gold
- 💎 1,000 Shards
- 🧪 50 Health Potions + 50 Mana Potions
- 🌯 10 Șaorme Revival
- 🎁 Epic equipment pre-equipped

### Step 3: Access Abilities
1. From Town Menu, enter **"🏛️ Temniță"** (Dungeon)
2. Your abilities should be auto-equipped to your loadout
3. Abilities unlock by level:
   - **Wizard**: Ice Shard (L3), Arcane Missiles (L7), Meteor Strike (L10)
   - **Warrior**: Shield Bash (L4), Whirlwind (L6), Execute (L9)
   - **Rogue**: Backstab (L3), Poison Blade (L5), Shadow Step (L8)

### Step 4: Customize Abilities (Optional)
Before entering dungeon:
1. Go to **Character Sheet** or **Ability Customization** (if available)
2. Select each ability and choose:
   - **Variant** (3 options per ability)
   - **Talents** (9 talents per ability, 3 tiers)

### Step 5: Enter Combat and Test!
1. Start dungeon run
2. Use abilities in combat
3. Watch combat log for special effect messages
4. Verify effects trigger correctly

---

## 📋 Systematic Testing Checklist

### ❄️ ICE SHARD (Wizard - Level 3)

**Base Test:**
- [ ] Cast Ice Shard on enemy
- [ ] Verify damage dealt
- [ ] Verify Slow debuff applied (2 turns)
- [ ] Verify mana cost (18)

**Variants:**
- [ ] **Frost Lance**: Freeze enemy for 1 turn (hard CC)
- [ ] **Blizzard**: Hits all enemies, applies Slow

**Tier 1 Talents:**
- [ ] **Deep Freeze**: +25% damage, +1 slow duration
- [ ] **Chilling Touch**: 5 DOT for 3 turns
- [ ] **Ice Mastery**: -25% mana cost

**Tier 2 Talents:**
- [ ] **Shatter**: +50% damage to slowed/frozen enemies
- [ ] **Brittle**: -15 enemy defense
- [ ] **Ice Pierce**: +20% crit chance

**Tier 3 Talents (Special Effects):**
- [ ] **Frozen Tomb** ❄️: On crit, freeze ALL enemies for 1 turn
  - *Expected*: Critical hit → All enemies get Freeze debuff → Combat log shows "🧊 FROZEN TOMB!"
- [ ] **Permafrost** ⏱️: Slow duration doubled
  - *Expected*: Slow lasts 4 turns instead of 2 → Combat log shows "❄️ Permafrost!"
- [ ] **Cold Snap**: Refund 50% mana on kill

---

### ✨ ARCANE MISSILES (Wizard - Level 7)

**Base Test:**
- [ ] Cast Arcane Missiles (3 hits)
- [ ] Verify each missile hits separately
- [ ] Verify total damage = 3 × 25 (base)
- [ ] Verify mana cost (22)

**Variants:**
- [ ] **Arcane Barrage**: 5 missiles × 18 damage
- [ ] **Arcane Blast**: Single hit × 110 damage

**Tier 1 Talents:**
- [ ] **Arcane Power**: +20% damage per missile
- [ ] **Extra Missile** ✨: +1 missile fired
  - *Expected*: 4 missiles fire instead of 3 → Combat log shows "✨ Extra Hit!"
- [ ] **Arcane Efficiency**: -30% mana cost

**Tier 2 Talents:**
- [ ] **Arcane Chain**: Each missile chains to +1 enemy
- [ ] **Missile Precision**: +25% crit chance
- [ ] **Arcane Amplification** 📈: Each hit increases next by 10%
  - *Expected*: Hit 1: 100%, Hit 2: 110%, Hit 3: 120% damage

**Tier 3 Talents (Special Effects):**
- [ ] **Missile Barrage** 🌟: +2 missiles (total 5-6 missiles)
  - *Expected*: 5+ missiles fire → Combat log shows "🌟 Missile Barrage!"
- [ ] **Arcane Overload**: Kill resets cooldown
- [ ] **Arcane Absorption**: 10% lifesteal per missile

---

### ☄️ METEOR STRIKE (Wizard - Level 10)

**Base Test:**
- [ ] Cast Meteor Strike (AOE)
- [ ] Verify hits ALL enemies
- [ ] Verify Burn applied (3 turns, 20 dmg/turn)
- [ ] Verify cooldown (3 turns)
- [ ] Verify mana cost (60)

**Variants:**
- [ ] **Meteor Shower**: 3 hits × 80 damage, 2 turn cooldown
- [ ] **Comet**: Single target, 350 damage, 4 turn cooldown

**Tier 1 Talents:**
- [ ] **Impact Force**: +30% damage
- [ ] **Rapid Casting**: -1 cooldown turn
- [ ] **Astral Focus**: -20% mana cost

**Tier 2 Talents (Special Effects):**
- [ ] **Inferno** 🔥: Burn damage doubled
  - *Expected*: Burn deals 40 dmg/turn instead of 20 → Combat log shows "🔥 Inferno!"
- [ ] **Meteor Stun** 💫: Stun all enemies for 1 turn
  - *Expected*: All enemies stunned → Combat log shows "💫 METEOR STUN!"
- [ ] **Devastating Impact**: +30% crit chance

**Tier 3 Talents:**
- [ ] **Apocalypse**: +100% damage, +2 cooldown
- [ ] **Meteor Storm**: Kill resets cooldown
- [ ] **Flame Barrier**: Gain shield equal to damage dealt

---

### 🛡️ SHIELD BASH (Warrior - Level 4)

**Base Test:**
- [ ] Cast Shield Bash
- [ ] Verify Stun applied (1 turn)
- [ ] Verify rage cost (25)
- [ ] Verify hit bonus (+10)

**Variants:**
- [ ] **Shield Slam**: 120 damage, Dazed (2 turns)
- [ ] **Shield Wall**: AOE, Slow (2 turns)

**Tier 1 Talents:**
- [ ] **Heavy Impact**: +30% damage
- [ ] **Concussive Blow** 💫: +1 stun duration
  - *Expected*: Stun lasts 2 turns instead of 1 → Combat log shows "💫 Concussive Blow!"
- [ ] **Efficient Bash**: -30% rage cost

**Tier 2 Talents (Special Effects):**
- [ ] **Shield Expert** 🛡️: Gain 20 armor for 2 turns
  - *Expected*: Hero gains armor buff on cast → Combat log shows "🛡️ Shield Expert!"
- [ ] **Defensive Stance**: Heal 15% of damage dealt
- [ ] **Interrupt**: Silence enemy for 1 turn

**Tier 3 Talents (Special Effects):**
- [ ] **Retribution** ⚡: Reflect 30% damage back
  - *Expected*: When enemy attacks you → Enemy takes reflected damage → Combat log shows "⚡ Retribution! X damage reflected back!"
  - *Edge case*: Enemy can be killed by reflected damage
- [ ] **Chain Bash**: Hit chains to 2 additional enemies
- [ ] **Rage Generation**: Refund 50% rage on stun

---

### 🌪️ WHIRLWIND (Warrior - Level 6)

**Base Test:**
- [ ] Cast Whirlwind (AOE)
- [ ] Verify hits ALL enemies
- [ ] Verify rage cost (40)

**Variants:**
- [ ] **Bladestorm**: 3 hits per enemy
- [ ] **Cleave**: Hits 3 targets, higher damage

**Tier 1 Talents:**
- [ ] **Momentum**: +25% damage
- [ ] **Rending Strikes**: Apply bleed (8 dmg, 3 turns)
- [ ] **Controlled Fury**: -25% rage cost

**Tier 2 Talents:**
- [ ] **Precise Strikes**: +20% crit chance
- [ ] **Battle Trance**: 5% lifesteal per enemy hit
- [ ] **Sunder Armor**: Reduce armor by 10 for 3 turns

**Tier 3 Talents (Special Effects):**
- [ ] **Double Spin** 👥: Hit enemies twice
  - *Expected*: Each enemy takes 2 hits → Combat log shows "👥 Double Strike!"
- [ ] **Endless Rage**: Kill resets cooldown
- [ ] **Execute Weakness** 💀: +100% damage to enemies below 30% HP
  - *Expected*: Low HP enemies take double damage → Combat log shows "💀 Execute Weakness!"

---

### 💀 EXECUTE (Warrior - Level 9)

**Base Test:**
- [ ] Cast Execute on low HP enemy
- [ ] Verify damage scales with missing HP
- [ ] Verify rage cost (50)
- [ ] Verify cooldown (2 turns)

**Variants:**
- [ ] **Mortal Strike**: Reduces enemy healing
- [ ] **Rampage**: AOE execute

**Tier 1 Talents:**
- [ ] **Killing Blow**: +35% damage
- [ ] **Early Execute**: Can use at 40% HP instead of 20%
- [ ] **Efficient Kill**: -30% rage cost

**Tier 2 Talents:**
- [ ] **Executioner's Precision**: +40% crit chance
- [ ] **Grievous Wounds**: Apply massive bleed (20 dmg, 3 turns)
- [ ] **Rapid Execution**: -1 cooldown turn

**Tier 3 Talents (Special Effects):**
- [ ] **Sudden Death** 💀: Damage scales 200% with missing HP
  - *Expected*: Enemy at 50% HP → +100% damage, at 80% missing → +160% damage
  - *Combat log*: "💀 Sudden Death! +X% damage based on missing HP!"
- [ ] **Fresh Meat**: Kill resets cooldown and refunds rage
- [ ] **Cleaving Execute**: Chains to 2 additional low HP enemies

---

### 🗡️ BACKSTAB (Rogue - Level 3)

**Base Test:**
- [ ] Cast Backstab
- [ ] Verify high crit chance
- [ ] Verify energy cost (30)
- [ ] Verify hit bonus (+15)

**Variants:**
- [ ] **Ambush**: Guaranteed crit, 140 damage
- [ ] **Cheap Shot**: Stun (1 turn)

**Tier 1 Talents:**
- [ ] **Find Weakness**: +30% damage
- [ ] **Ruthlessness**: +30% crit chance
- [ ] **Silent Technique**: -25% energy cost

**Tier 2 Talents (Special Effects):**
- [ ] **Serrated Blade**: Apply heavy bleed (15 dmg, 3 turns)
- [ ] **Expose Armor**: Reduce armor by 20 for 3 turns
- [ ] **Combo Strike** 👥: Hit twice
  - *Expected*: 2 backstabs → Combat log shows "👥 Double Strike!"

**Tier 3 Talents (Special Effects):**
- [ ] **Deadly Precision**: Crits deal +150% damage
  - *Expected*: Critical hit damage multiplier increased → Higher crit numbers
- [ ] **Silent Killer**: Kill resets cooldown and refunds energy
- [ ] **Predator**: Heal for 30% of crit damage

---

### ☠️ POISON BLADE (Rogue - Level 5)

**Base Test:**
- [ ] Cast Poison Blade
- [ ] Verify Poison applied (4 turns, 12 dmg/turn)
- [ ] Verify energy cost (20)
- [ ] Verify poison stacks

**Variants:**
- [ ] **Deadly Poison**: 5 turns, 20 dmg/turn
- [ ] **Envenom**: Consume poisons for burst damage

**Tier 1 Talents (Special Effects):**
- [ ] **Deadly Toxin** ☠️: +40% poison damage
  - *Expected*: Poison deals 16-17 dmg/turn instead of 12 → Combat log shows "☠️ Deadly Toxin!"
- [ ] **Lingering Poison** ⏱️: +2 poison duration
  - *Expected*: Poison lasts 6 turns instead of 4 → Combat log shows "⏱️ Lingering Poison!"
- [ ] **Efficient Coating**: -30% energy cost

**Tier 2 Talents (Special Effects):**
- [ ] **Toxic Buildup** 💚: Poison stacks 2x faster
  - *Expected*: On kill, poisons stack more aggressively → Combat log shows "💚 Toxic Buildup!"
- [ ] **Contagion** 🦠: Poison spreads to nearby enemies
  - *Expected*: All enemies get poisoned → Combat log shows "🦠 Contagion!"
- [ ] **Toxic Injection**: +25% crit chance

**Tier 3 Talents (Special Effects):**
- [ ] **Venomous Wounds** ☠️: Poison can crit
  - *Expected*: Each turn, poison has chance to crit → Extra damage burst from poison
  - *Combat log*: "☠️ Venomous Wounds! Poison critically strikes for X bonus damage!"
- [ ] **Master Poisoner** 🔄: Refresh all poison durations on kill
  - *Expected*: Kill enemy → All other enemies' poison durations reset → Combat log shows "🔄 Master Poisoner!"
- [ ] **Noxious Cloud**: Poisoned enemies explode on death for 80 damage

---

### 🌑 SHADOW STEP (Rogue - Level 8)

**Base Test:**
- [ ] Cast Shadow Step
- [ ] Verify teleport + strike
- [ ] Verify energy cost (35)
- [ ] Verify cooldown (1 turn)
- [ ] Verify hit bonus (+20)

**Variants:**
- [ ] **Shadowstrike**: High crit, 2 turn cooldown
- [ ] **Shadowdance**: AOE, 2 turn cooldown

**Tier 1 Talents:**
- [ ] **Shadow Blade**: +30% damage
- [ ] **Quick Shadow**: -1 cooldown turn
- [ ] **Shadow Mastery**: -30% energy cost

**Tier 2 Talents (Special Effects):**
- [ ] **Evasive Shadow** 💨: +30% dodge for 2 turns
  - *Expected*: Hero gains dodge buff on cast → Harder to hit for 2 turns
- [ ] **Shadow Assassin**: +35% crit chance
- [ ] **Shadow Chain**: Chain to 2 additional enemies

**Tier 3 Talents (Special Effects):**
- [ ] **Shadow Clone** 👥: Strike twice with shadow clone
  - *Expected*: 2 hits → Combat log shows "👥 Double Strike!"
- [ ] **Shadow Master**: Kill resets cooldown
- [ ] **Shadow Drain** 🌑: 20% lifesteal + remove debuffs
  - *Expected*: Heal on hit + hero debuffs cleared → Already implemented

---

## 🔬 Advanced Testing Scenarios

### Multi-Battle Special Effects
Test abilities in battles with 2-4 enemies:
- [ ] AOE abilities hit all targets
- [ ] Chain effects bounce correctly
- [ ] Contagion spreads to all enemies
- [ ] Frozen Tomb affects all enemies
- [ ] Retribution works against multiple attackers
- [ ] Master Poisoner refreshes poisons on all enemies

### Edge Cases
- [ ] **Retribution Kill**: Enemy dies from reflected damage
- [ ] **Poison Crit Kill**: Enemy dies from critical poison tick
- [ ] **On-Kill Effects with Last Enemy**: Cooldown resets, mana/rage refunds work
- [ ] **Chain Effects with 1 Enemy**: No error, chains don't fire
- [ ] **Double Hit with AOE**: Each enemy gets hit twice
- [ ] **Debuff Stacking**: Multiple applications increase duration/stacks
- [ ] **Lifesteal Healing**: Healing numbers match damage dealt

### Resource Management
- [ ] Mana/Rage/Energy costs apply correctly
- [ ] Cost reduction talents work
- [ ] Refund talents work (on kill effects)
- [ ] Resources don't go negative

### Cooldowns
- [ ] Cooldowns track correctly
- [ ] Cooldown reduction talents work
- [ ] Reset on kill effects work
- [ ] Can't use abilities on cooldown

---

## 🐛 Common Issues to Watch For

### Compilation/Runtime Errors
- [ ] No null pointer exceptions when using abilities
- [ ] No errors when checking for custom properties
- [ ] No errors in multi-battle scenarios

### Visual/Display Issues
- [ ] Combat log messages display correctly
- [ ] Special effect emojis render properly
- [ ] Ability names show correctly in UI
- [ ] Damage numbers are correct

### Logic Bugs
- [ ] Effects trigger at the right time (pre-hit, on-hit, on-crit, on-kill)
- [ ] Debuffs apply with correct duration/damage
- [ ] AOE abilities don't hit dead enemies
- [ ] Percentage calculations are correct (30% reflect = actual 30%)

---

## 📊 Testing Results Template

Copy this template to track your testing:

```
## Test Session: [Date]
**Character**: [Class] - [Name]
**Level**: 30 (GOD MODE)
**Dungeon Depth**: [X]

### Abilities Tested:
- [ ] Ice Shard
- [ ] Arcane Missiles
- [ ] Meteor Strike
- [ ] Shield Bash
- [ ] Whirlwind
- [ ] Execute
- [ ] Backstab
- [ ] Poison Blade
- [ ] Shadow Step

### Special Effects Verified:
- [ ] Frozen Tomb (Ice Shard)
- [ ] Permafrost (Ice Shard)
- [ ] Extra Missile (Arcane Missiles)
- [ ] Arcane Amplification (Arcane Missiles)
- [ ] Missile Barrage (Arcane Missiles)
- [ ] Inferno (Meteor Strike)
- [ ] Meteor Stun (Meteor Strike)
- [ ] Concussive Blow (Shield Bash)
- [ ] Shield Expert (Shield Bash)
- [ ] Retribution (Shield Bash)
- [ ] Double Spin (Whirlwind)
- [ ] Execute Weakness (Whirlwind)
- [ ] Sudden Death (Execute)
- [ ] Combo Strike (Backstab)
- [ ] Deadly Precision (Backstab)
- [ ] Deadly Toxin (Poison Blade)
- [ ] Lingering Poison (Poison Blade)
- [ ] Toxic Buildup (Poison Blade)
- [ ] Contagion (Poison Blade)
- [ ] Venomous Wounds (Poison Blade)
- [ ] Master Poisoner (Poison Blade)
- [ ] Evasive Shadow (Shadow Step)
- [ ] Shadow Clone (Shadow Step)

### Bugs Found:
1. [Description of bug]
2. [Description of bug]

### Notes:
[Any observations about balance, UI, or gameplay]
```

---

## 🎯 Priority Testing Order

If you're short on time, test in this order:

1. **High Priority** (Core mechanics):
   - [ ] Basic ability damage/costs
   - [ ] Debuff application
   - [ ] AOE targeting
   - [ ] Cooldowns

2. **Medium Priority** (Special effects):
   - [ ] Retribution (damage reflection)
   - [ ] Venomous Wounds (poison crit)
   - [ ] Frozen Tomb (AOE freeze on crit)
   - [ ] Double Hit effects
   - [ ] Amplification (ramping damage)

3. **Low Priority** (Polish):
   - [ ] Combat log messages
   - [ ] Edge cases
   - [ ] All variant combinations
   - [ ] All talent combinations

---

## 💡 Tips for Effective Testing

1. **Use Console Output**: Watch the console for debug messages and special effect triggers
2. **Test One Thing at a Time**: Change one talent/variant, test, then change another
3. **Take Screenshots**: Capture interesting interactions or bugs
4. **Note Balance Issues**: If something feels too weak/strong, write it down
5. **Test Multi-Battle**: Most special effects are designed for multi-enemy fights
6. **Kill Enemies Slowly**: Test DOT effects, on-kill effects require actual kills

---

## 🚀 Next Steps After Testing

1. **Document Bugs**: Create a bug list for any issues found
2. **Balance Feedback**: Note which abilities/talents feel too strong/weak
3. **UI Improvements**: Suggest better combat log messages or visual feedback
4. **Expansion Ideas**: Think about what abilities/effects you'd like to see next

---

**Happy Testing!** 🎮

If you find any bugs or have questions, check the code at:
- `BattleServiceFX.java` (lines 532-1040) - Special effect implementations
- `AbilityDefinitions.java` - Ability and talent definitions
- `Inamic.java` (lines 345-362) - Debuff helper methods
