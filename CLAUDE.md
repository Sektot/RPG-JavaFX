# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**RPG Românesc: Legenda din Bucale** - A Romanian-themed RPG built with JavaFX 21 and Java 17. This is a dual-interface game supporting both console (CLI) and JavaFX GUI modes with a dungeon crawler combat system, character progression, equipment management, and Romanian cultural elements.

## Build & Run Commands

```bash
# Build the project
mvnw clean compile

# Run the JavaFX application (GUI mode)
mvnw javafx:run

# Run with Maven
mvnw clean javafx:run

# Package the application
mvnw clean package
```

## Architecture Overview

### Dual Interface Pattern
The game implements **parallel service/controller layers** for both CLI and JavaFX:
- **Console/CLI**: `*Service.java` classes (BattleService, InventoryService, etc.)
- **JavaFX GUI**: `*ServiceFX.java` and `*ControllerFX.java` classes
- **Shared**: All model classes in `com.rpg.model.*` are interface-agnostic

### Core Architecture Layers

**Model Layer** (`com.rpg.model.*`):
- `Erou` (Hero) - Main player character class with extensive stat tracking, equipment slots (9 slots: main hand, off hand, armor, helmet, gloves, boots, 2 rings, necklace), buff/debuff system, and potion tiers
- Three character classes extend `Erou`:
  - `Moldovean` (Warrior) - Uses Rage resource, high strength
  - `Ardelean` (Wizard) - Uses Mana resource, high intelligence
  - `Oltean` (Rogue) - Uses Energy resource, high dexterity
- `Inamic` (Enemy) - Enemy characters with level scaling and boss variants
- `ObiectEchipament` - Equipment system with 11 equipment types, rarity tiers, enchantments, and bonus stats
- `Abilitate` - Ability system with resource costs and cooldowns
- Effects: `BuffStack` and `DebuffStack` for temporary stat modifications

**Service Layer** (`com.rpg.service.*`):
- Dual implementation pattern: `*Service` for CLI, `*ServiceFX` for JavaFX
- `BattleService[FX]` - Combat mechanics: hit chance, dodge, critical hits, ability execution
- `InventoryService[FX]` - Equipment management, potion usage
- `DungeonService[FX]` - Dungeon progression with checkpoint system (every 5 levels)
- `SaveLoadService[FX]` - Game state serialization
- `ShopService[FX]` - Vendor interactions and item purchasing
- `TrainerSmithService[FX]` - Stat allocation and equipment enhancement
- `TavernService[FX]` - Quest system
- `EnemyGeneratorRomanesc` - Procedural enemy generation with Romanian themes
- `LootGenerator` - Item drop system with rarity-based probabilities

**Controller Layer** (`com.rpg.controller.*` - JavaFX only):
- `MainMenuController` - Entry point, character creation/loading
- `TownMenuController` - Hub for accessing all game features
- `BattleControllerFX` - Real-time combat UI with animations
- `InventoryControllerFX` - Equipment and item management UI
- `CharacterSheetController` - Character stats display
- `ShopAdvancedController` - Shop interface
- Supports dungeon mode: tracks depth, scales difficulty, boss battles every 5 levels

**Factory Pattern**:
- `CharacterFactory` - Creates heroes with class-specific stats and abilities, includes GOD MODE for testing

**DTO Pattern** (`com.rpg.service.dto.*`):
- Data transfer objects decouple UI from business logic
- Key DTOs: `BattleInitDTO`, `AbilityDTO`, `ShopItemDTO`, `InventoryItemDTO`, `DungeonStartResult`, `PurchaseResult`, `EnhancementResultDTO`

## Key Game Systems

### Combat System
- Turn-based combat with action economy
- Hit chance calculation: base 85% + dexterity bonuses + level bonuses
- Critical hits: base 5% + dexterity scaling (max 50%)
- Dodge mechanics: base 5% + dexterity scaling (max 75%)
- Damage = (Strength × 2) + weapon bonuses - enemy defense
- Resource management (Mana/Rage/Energy) for abilities
- Flee mechanic (not allowed against bosses)

### Equipment System
**9 Equipment Slots**:
- Main Hand (one-handed or two-handed weapons)
- Off Hand (shields, off-hand weapons, magic items)
- Armor, Helmet, Gloves, Boots
- Ring1, Ring2, Necklace

**Features**:
- Two-handed weapons lock off-hand slot
- Dual-wielding support (off-hand weapons at 50% effectiveness)
- Equipment bonuses: damage, defense, strength, dexterity, intelligence, crit chance, dodge chance
- Enchantment system: Apply elemental damage (Fire, Ice, Lightning, Poison, Holy, Shadow, Arcane, Nature)
- Rarity tiers affect stat bonuses and drop rates

### Progression Systems
**Experience & Leveling**:
- Base XP requirement: 100, multiplied by 1.5 per level
- Stat points per level: 3 (bonus +2 every 5 levels)
- Auto-leveling with multiple level-ups in one go
- Level-up bonuses: increased max HP, stat points, class-specific abilities

**Potion Upgrade System**:
- Health Potions (Berice): Upgradeable tiers increase healing amount
- Mana Potions (Energizant Profi): Upgradeable tiers increase resource restoration
- Flask Pieces: Collected to upgrade potion tiers (3 types: Health, Mana, Universal)
- Buff Potions: Temporary stat boosts (Strength, Dexterity, Intelligence variants)

**Enchantment System**:
- Enchant Scrolls drop from bosses (25% chance)
- Apply elemental damage to weapons
- Multiple enchantments can stack on one weapon
- Cost: Gold + scroll consumption

**Enhancement System**:
- Spend Shards to upgrade equipment stats
- Cost scales with enhancement level

### Dungeon System
- Progressive difficulty: enemy level = hero level + (depth - 1) / 3
- Checkpoint autosave every 5 levels
- Boss battles every 5 levels (2.5× health, 3× gold, 2× XP)
- Between-battle healing: 5% HP + 10% resource regeneration
- Continue or exit after each victory

### Shaorma Revival System
A unique death prevention mechanic:
- Shaormas drop exclusively from boss enemies (1 per boss)
- On death, can consume 1 Shaorma to revive with 50% HP and resources
- Player chooses whether to use revival via confirmation dialog

### Save System
- Java serialization for game state persistence
- Autosave at checkpoints (every 5 dungeon levels)
- Manual save/load from town menu
- Compatible with both CLI and JavaFX modes

## Important Constants & Balance

Located in `GameConstants.java`:
- Base Health: 100 (+5 per Strength, +10 per Level)
- Health Potion Heal: 30 (base tier)
- Mana Potion Restore: 25 (base tier)
- Enemy scaling formulas for health, damage, defense
- Boss Flask Drop Chance: 75%
- Boss Scroll Drop Chance: 25%
- Combat chances: Hit (85% base), Crit (5% base), Dodge (5% base)

## Romanian Theme Elements

The game uses Romanian cultural references:
- Class names: Moldovean (from Moldova), Ardelean (from Transylvania), Oltean (from Oltenia)
- Item names: Berice (local beer brand for health potions), Energizant Profi (energy drink for mana)
- Shaorma (shawarma) - iconic Romanian street food used as revival mechanic
- Enemy names generated with Romanian themes in `EnemyGeneratorRomanesc`
- UI text and flavor messages use Romanian language

## Code Patterns & Conventions

### Service Duplication Pattern
When modifying business logic, check for both implementations:
```java
// Console version
public class BattleService { ... }

// JavaFX version
public class BattleServiceFX { ... }
```
Changes to game mechanics often need updates in both.

### Equipment Slot Management
The `Erou` class uses a `Map<String, ObiectEchipament>` for equipment:
- Slot keys defined as constants: `MAIN_HAND`, `OFF_HAND`, `ARMOR`, etc.
- `equipItem()` master method handles all equipment types with logic for:
  - Two-handed weapon restrictions
  - Ring slot management (2 rings)
  - Auto-unequipping conflicts
- Compatibility methods maintain old API: `echipeazaArma()`, `getArmaEchipata()`

### InventarWrapper Pattern
`Erou.InventarWrapper` provides a facade over the internal inventory list with specialized collections for potions, buff potions, scrolls, and flask pieces. This encapsulation supports both old and new inventory APIs.

### Battle State Transfer
Combat uses DTOs to decouple state from controllers:
```java
BattleInitDTO -> AbilityDTO.BattleStateDTO -> BattleTurnResultDTO -> BattleResultDTO
```
This pattern allows UI updates without direct model manipulation.

### Resource System Abstraction
Each class uses a different resource (Mana/Rage/Energy) but shares common methods:
- `getTipResursa()` - returns resource type name
- `getResursaCurenta()` / `getResursaMaxima()` - unified accessors
- `consumaResursa()` / `regenResursa()` - unified operations
- Subclasses override `regenNormal()` for class-specific mechanics

## Testing & Development

**GOD MODE Character**:
Use `CharacterFactory.createGodModeHero()` for testing:
- Level 50
- 9999 Gold, 500 Shards
- Max stat points (200)
- All potion types
- High-tier equipment

**Debug Mode**:
Uncomment debug print statements in combat methods (look for `System.out.printf` lines marked with DEBUG)

## Common Development Tasks

### Adding a New Ability
1. Create ability in character class's `initializeazaAbilitati()` method
2. Add level-gated unlock in `abilitateSpecialaNivel(int nivel)`
3. Implement ability effect in `Abilitate` subclass or lambda
4. Update both `BattleService` and `BattleServiceFX` for execution logic
5. Add to `AbilityDTO` for UI display

### Adding New Equipment
1. Create `ObiectEchipament` with proper `TipEchipament` enum
2. Define bonuses in `Map<String, Integer>`
3. Add to loot tables in `LootGenerator` or shop inventory
4. Test equip/unequip logic for slot conflicts

### Modifying Enemy Scaling
Adjust formulas in `GameConstants`:
- `ENEMY_BASE_HEALTH`, `ENEMY_HEALTH_PER_LEVEL`
- `ENEMY_BASE_DAMAGE`, `ENEMY_DAMAGE_PER_LEVEL`
- `ENEMY_BASE_DEFENSE`, `ENEMY_DEFENSE_PER_LEVEL`

Or modify methods:
- `calculateEnemyHealth()`, `calculateEnemyDamage()`, `calculateEnemyDefense()`

### Adding UI Screens (JavaFX)
1. Create controller in `com.rpg.controller.*`
2. Implement `createScene()` method returning `Scene`
3. Use `DialogHelper` for confirmations/alerts
4. Follow existing styling patterns (dark theme: #0f0f1e background, #e94560 accents)
5. Link from `TownMenuController` or appropriate parent

## Recent Changes (from git status)

`ShopController.java` has been modified - check this file for recent shop system updates.

## Notes

- The codebase supports both CLI and GUI modes but JavaFX is the primary interface
- Character serialization uses `@Serial private static final long serialVersionUID = 1L`
- Combat system is deterministic (no hidden RNG seeds) for testing
- Equipment bonuses are calculated dynamically, not cached
- The game supports Romanian language UI elements throughout
