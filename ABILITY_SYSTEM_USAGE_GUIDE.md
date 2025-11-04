# 🎮 Ability Variant & Talent System - Usage Guide

## 📚 Overview

The new ability customization system has been implemented! This guide explains how to use the system in your code.

## 🏗️ Architecture Summary

### **Core Components**

1. **AbilityVariant** - Different versions of an ability
2. **AbilityTalent** - Modifications that customize abilities
3. **AbilityModifier** - Stats and effects applied by talents
4. **ConfiguredAbility** - An ability with selected variant + talents
5. **AbilityLoadout** - Manages the 6-ability active loadout
6. **AbilityDefinitions** - Factory class with example abilities

### **Integration Points**

- **Erou.java** - Hero class now has `AbilityLoadout abilityLoadout` field
- All loadout management methods added to Erou

## 🚀 Quick Start Example

```java
// 1. Create a hero
Erou hero = new Ardelean("TestWizard", 10, 10, 20);

// 2. Create and unlock Fireball ability
Abilitate fireballBase = AbilityDefinitions.createFireballBase();
List<AbilityVariant> fireballVariants = AbilityDefinitions.createFireballVariants();
List<AbilityTalent> fireballTalents = AbilityDefinitions.createFireballTalents();

ConfiguredAbility fireball = new ConfiguredAbility(fireballBase, fireballVariants.get(0));
hero.unlockConfiguredAbility(fireball);

// 3. Customize the ability (select talents)
List<AbilityTalent> tier1 = AbilityDefinitions.getTalentsForTier("Fireball", TalentTier.TIER_1);
List<AbilityTalent> tier2 = AbilityDefinitions.getTalentsForTier("Fireball", TalentTier.TIER_2);
List<AbilityTalent> tier3 = AbilityDefinitions.getTalentsForTier("Fireball", TalentTier.TIER_3);

fireball.setTier1Talent(tier1.get(0));  // Intense Heat (+20% damage)
fireball.setTier2Talent(tier2.get(0));  // Melt Armor (-10 enemy DEF)
fireball.setTier3Talent(tier3.get(0));  // Explosive (AOE on kill)

// 4. Add to active loadout
hero.addAbilityToLoadout("Fireball");

// 5. In combat, use the configured ability
List<ConfiguredAbility> loadout = hero.getActiveLoadoutAbilities();
ConfiguredAbility activeFireball = loadout.get(0);

int damage = activeFireball.getFinalDamage();  // Damage with talents applied
int manaCost = activeFireball.getFinalManaCost();
```

## 📖 Detailed Usage

### **1. Creating Abilities**

Use `AbilityDefinitions` to create abilities with all their variants and talents:

```java
// Get all 3 variants for Fireball
List<AbilityVariant> variants = AbilityDefinitions.createFireballVariants();

// Variants:
// [0] = Fireball (default) - Single target, burn
// [1] = Firestorm - AOE, hits all enemies
// [2] = Inferno Bolt - High burst, cooldown

// Get all 9 talents (3 tiers × 3 options)
List<AbilityTalent> talents = AbilityDefinitions.createFireballTalents();
```

### **2. Unlocking Abilities**

When a hero discovers an ability (level up, quest, secret room):

```java
// Create the base ability
Abilitate base = AbilityDefinitions.createFireballBase();

// Get default variant
List<AbilityVariant> variants = AbilityDefinitions.createFireballVariants();
AbilityVariant defaultVariant = variants.get(0);

// Create configured ability (starts with default variant, no talents)
ConfiguredAbility fireball = new ConfiguredAbility(base, defaultVariant);

// Unlock it for the hero
hero.unlockConfiguredAbility(fireball);
```

Or use the helper method:

```java
Map<String, ConfiguredAbility> exampleAbilities = AbilityDefinitions.createAllExampleAbilities();
hero.unlockConfiguredAbility(exampleAbilities.get("Fireball"));
hero.unlockConfiguredAbility(exampleAbilities.get("Cleave"));
hero.unlockConfiguredAbility(exampleAbilities.get("Lightning Bolt"));
```

### **3. Customizing Abilities (Town/Character Screen)**

Players customize abilities before entering dungeons:

```java
// Get the hero's unlocked ability
ConfiguredAbility fireball = hero.getConfiguredAbility("Fireball");

// Change variant
List<AbilityVariant> variants = AbilityDefinitions.getVariantsForAbility("Fireball");
fireball.setSelectedVariant(variants.get(1));  // Switch to Firestorm (AOE)

// Select talents (one per tier)
List<AbilityTalent> tier1Talents = AbilityDefinitions.getTalentsForTier("Fireball", TalentTier.TIER_1);
fireball.setTier1Talent(tier1Talents.get(0));  // Intense Heat

List<AbilityTalent> tier2Talents = AbilityDefinitions.getTalentsForTier("Fireball", TalentTier.TIER_2);
fireball.setTier2Talent(tier2Talents.get(1));  // Chain Fire

List<AbilityTalent> tier3Talents = AbilityDefinitions.getTalentsForTier("Fireball", TalentTier.TIER_3);
fireball.setTier3Talent(tier3Talents.get(2));  // Flame Shield

// Stats are automatically recalculated
int finalDamage = fireball.getFinalDamage();
```

### **4. Managing Loadout (Pre-Dungeon)**

Before entering a dungeon, player selects 6 abilities:

```java
// Check how many abilities are unlocked
int unlockedCount = hero.getUnlockedAbilityCount();  // e.g., 12

// Add abilities to loadout (max 6)
hero.addAbilityToLoadout("Fireball");
hero.addAbilityToLoadout("Lightning Bolt");
hero.addAbilityToLoadout("Ice Shield");
hero.addAbilityToLoadout("Wind Dash");
hero.addAbilityToLoadout("Arcane Blast");
hero.addAbilityToLoadout("Meteor");

// Check loadout status
int loadoutSize = hero.getLoadoutSize();  // 6
boolean isValid = hero.hasValidLoadout();  // true
boolean isFull = hero.getAbilityLoadout().isLoadoutFull();  // true

// Or set entire loadout at once
List<String> loadoutIds = List.of("Fireball", "Lightning Bolt", "Ice Shield",
                                   "Wind Dash", "Arcane Blast", "Meteor");
hero.setLoadout(loadoutIds);
```

### **5. Using Abilities in Combat**

Only loadout abilities are available in combat:

```java
// Get active loadout (max 6 abilities)
List<ConfiguredAbility> activeAbilities = hero.getActiveLoadoutAbilities();

// Display in UI
for (int i = 0; i < activeAbilities.size(); i++) {
    ConfiguredAbility ability = activeAbilities.get(i);
    System.out.printf("[%d] %s - %d damage, %d mana\n",
                      i + 1,
                      ability.getDisplayName(),
                      ability.getFinalDamage(),
                      ability.getFinalManaCost());
}

// Player selects ability
int choice = 1;  // Fireball
ConfiguredAbility selectedAbility = activeAbilities.get(choice - 1);

// Execute ability (integrate with existing BattleService)
int damage = selectedAbility.getFinalDamage();
int manaCost = selectedAbility.getFinalManaCost();

if (hero.areResursaSuficienta(manaCost)) {
    hero.consumaResursa(manaCost);

    // Apply talent modifiers (check for special effects)
    AbilityModifier mods = selectedAbility.getTier1Talent().getModifier();
    if (mods.appliesBleed()) {
        // Apply bleed to enemy
        int bleedDamage = mods.getBleedDamage();
        int bleedDuration = mods.getBleedDuration();
    }

    // Deal damage to enemy
    enemy.primesteDamage(damage);
}
```

### **6. Loadout Templates**

Save and load loadout configurations:

```java
// Save current loadout as template
hero.saveLoadoutTemplate("Boss Rush");

// Later, load it
hero.loadLoadoutTemplate("Boss Rush");

// Built-in templates (empty by default):
// - "Balanced"
// - "Boss Rush"
// - "AOE Farm"
// - "Survival"
```

## 🎯 Example Ability Builds

### **Fireball - AOE Farmer Build**

```java
ConfiguredAbility fireball = hero.getConfiguredAbility("Fireball");

// Variant: Firestorm (hits all enemies)
fireball.setSelectedVariant(variants.get(1));

// Talents:
// T1: Mana Efficient (-30% mana, -30% damage) - sustainability
// T2: Chain Fire (25% chance to chain) - more coverage
// T3: Cooldown Reset (kills refund mana) - spam ability

// Result: Efficient AOE farmer that refunds mana on kills
```

### **Cleave - Boss Killer Build**

```java
ConfiguredAbility cleave = hero.getConfiguredAbility("Cleave");

// Variant: Focused Strike (140 damage single target)
cleave.setSelectedVariant(variants.get(1));

// Talents:
// T1: Bleeding Edge (+15% damage, bleed) - more damage
// T2: Armor Shatter (-15 enemy DEF) - reduce tankiness
// T3: Execute (+100% damage below 30% HP) - finish boss

// Result: Massive single-target burst for boss fights
```

## 🔧 Integration with Existing Systems

### **Level-Up Unlocks**

In `Erou` subclasses (Moldovean, Ardelean, Oltean):

```java
@Override
public Abilitate abilitateSpecialaNivel(int nivel) {
    if (nivel == 5) {
        // Unlock Lightning Bolt at level 5
        ConfiguredAbility lightning = AbilityDefinitions.createAllExampleAbilities().get("Lightning Bolt");
        this.unlockConfiguredAbility(lightning);

        // Auto-add to loadout if space available
        if (!this.getAbilityLoadout().isLoadoutFull()) {
            this.addAbilityToLoadout("Lightning Bolt");
        }
    }
    // ... other level unlocks
    return null;  // Return old system ability or null
}
```

### **Quest Rewards**

When quest is completed:

```java
public void onQuestComplete(Erou hero) {
    // Create unique quest reward ability
    Abilitate special = new Abilitate("Second Wind", /* ... params ... */);
    List<AbilityVariant> variants = /* create variants */;

    ConfiguredAbility reward = new ConfiguredAbility(special, variants.get(0));
    hero.unlockConfiguredAbility(reward);

    // Show notification
    System.out.println("✨ Ability Unlocked: Second Wind!");
}
```

### **Secret Room Discovery**

When player finds secret room:

```java
public void onSecretRoomInteract(Erou hero) {
    if (!hero.hasUnlockedAbility("Shadow Step")) {
        ConfiguredAbility shadowStep = /* create ability */;
        hero.unlockConfiguredAbility(shadowStep);

        System.out.println("📖 You found an ancient scroll!");
        System.out.println("✨ Ability Discovered: Shadow Step");
    }
}
```

## 📊 Stat Calculations

### **How Final Stats are Calculated**

```java
// 1. Start with variant base stats
int damage = variant.getBaseDamage();  // e.g., 80
int manaCost = variant.getManaCost();  // e.g., 20

// 2. Apply Tier 1 talent modifiers
damage *= tier1Talent.getModifier().getDamageMultiplier();  // e.g., 1.2 = 96 damage
manaCost -= tier1Talent.getModifier().getFlatManaCostReduction();  // e.g., -5 = 15 mana

// 3. Apply Tier 2 talent modifiers
// (additional effects like chain, armor reduction)

// 4. Apply Tier 3 talent modifiers
// (on-kill effects, lifesteal, etc.)

// 5. Return final calculated stats
int finalDamage = ability.getFinalDamage();  // Cached, recalculated when dirty
```

### **Checking for Special Effects**

```java
// Check if ability has special effects from talents
AbilityTalent t3 = ability.getTier3Talent();
if (t3 != null) {
    AbilityModifier mod = t3.getModifier();

    if (mod.hasLifesteal()) {
        double lifesteal = mod.getLifestealPercent();  // 0.25 = 25%
        int healAmount = (int)(damageDealt * lifesteal);
        hero.vindeca(healAmount);
    }

    if (mod.triggersExplosionOnKill() && enemyKilled) {
        int explosionDamage = mod.getExplosionDamage();  // 30
        // Deal AOE damage to all other enemies
    }
}
```

## ✅ Implementation Checklist

### **Phase 1: Data Structures** ✅ COMPLETE
- [x] AbilityVariant class
- [x] AbilityTalent class
- [x] AbilityModifier class
- [x] ConfiguredAbility class
- [x] AbilityLoadout class
- [x] TalentTier enum

### **Phase 2: Example Content** ✅ COMPLETE
- [x] AbilityDefinitions factory
- [x] Fireball with 3 variants + 9 talents
- [x] Cleave with 3 variants + 9 talents
- [x] Lightning Bolt with 3 variants + 9 talents

### **Phase 3: Hero Integration** ✅ COMPLETE
- [x] Add AbilityLoadout field to Erou
- [x] Add loadout management methods
- [x] Lazy initialization for backward compatibility

### **Phase 4: Discovery System** 🔄 TODO
- [ ] Level-up unlock integration
- [ ] Quest reward system
- [ ] Secret room discovery
- [ ] Boss drop system
- [ ] Achievement unlocks

### **Phase 5: UI Implementation** 🔄 TODO
- [ ] Ability customization screen (town)
- [ ] Variant selection UI
- [ ] Talent tree selection UI
- [ ] Pre-dungeon loadout selection
- [ ] Combat ability display (loadout only)

### **Phase 6: Combat Integration** 🔄 TODO
- [ ] Update BattleServiceFX to use ConfiguredAbility
- [ ] Apply talent modifiers in combat
- [ ] Implement special effects (bleed, lifesteal, etc.)
- [ ] On-kill triggers (explosions, mana refund)
- [ ] AOE ability execution

## 🎨 Next Steps

### **Immediate Priorities:**

1. **Create More Abilities** - Expand to 20 abilities per class
   - 6 core (level unlocks)
   - 4 quest rewards
   - 6 secret room discoveries
   - 2 boss drops
   - 2 achievements

2. **Build Ability Customization UI** - JavaFX screen for:
   - Viewing all unlocked abilities
   - Selecting variants
   - Choosing talents
   - Preview final stats

3. **Build Loadout Selection UI** - Pre-dungeon screen:
   - Drag-and-drop 6 abilities
   - Template management
   - Quick stats comparison

4. **Integrate with Combat** - Update BattleControllerFX:
   - Show only loadout abilities
   - Execute with talent effects
   - Apply modifiers dynamically

## 💡 Tips for Content Creation

### **Designing New Abilities**

Follow the framework from ABILITY_CUSTOMIZATION_SYSTEM.md:

1. **Choose role**: Damage / Control / Defense / Resource / Mobility
2. **Create 3 variants**: Default, AOE, Burst
3. **Design 9 talents**:
   - Tier 1: Stat modifiers (damage, mana, etc.)
   - Tier 2: Mechanical changes (chain, armor shred)
   - Tier 3: On-hit/kill effects (explosions, lifesteal)

4. **Balance power budget**: Total power should be consistent across variants

### **Example Template**

```java
public static Abilitate createNewAbilityBase() {
    Map<String, Double> statInfluence = new HashMap<>();
    statInfluence.put("Intelligence", 0.5);

    return new Abilitate(
            "Ability Name",
            baseValue,
            List.of("DamageType"),
            resourceCost,
            cooldown,
            hitChanceBonus,
            statInfluence,
            debuff,
            debuffDuration,
            debuffDamage
    ).setAbilityType(AbilityType.OFFENSIVE)
     .setRequiredLevel(levelRequirement);
}
```

## 🚀 System is Ready!

The core ability variant and talent system is **fully implemented**. The data structures, example content, and hero integration are complete.

**What remains:**
- UI screens for customization
- Combat system integration
- Discovery mechanics
- More ability content (expand to 20+ per class)

The foundation is solid and ready for the next phase of development!
