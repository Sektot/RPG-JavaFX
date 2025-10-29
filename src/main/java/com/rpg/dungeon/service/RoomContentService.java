package com.rpg.dungeon.service;

import com.rpg.dungeon.model.*;
import com.rpg.model.characters.Erou;
import com.rpg.model.characters.Inamic;
import com.rpg.service.EnemyGeneratorRomanesc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Service pentru popularea camerelor cu conținut
 */
public class RoomContentService {

    private EnemyGeneratorRomanesc enemyGenerator;

    public RoomContentService() {
        this.enemyGenerator = new EnemyGeneratorRomanesc();
    }

    /**
     * Populează toate camerele din hartă cu conținut
     */
    public void populateMap(DungeonMap map, Erou hero) {
        for (Room room : map.getAllRooms()) {
            populateRoom(room, map.getDepth(), hero);
        }
    }

    /**
     * Populează o cameră individuală cu conținut
     */
    private void populateRoom(Room room, int depth, Erou hero) {
        switch (room.getType()) {
            case COMBAT:
                populateCombatRoom(room, depth, hero, false);
                break;
            case BOSS:
                populateCombatRoom(room, depth, hero, true);
                break;
            case EVENT:
                populateEventRoom(room, depth);
                break;
            case TREASURE:
                populateTreasureRoom(room, depth);
                break;
            case SHOP:
                // TODO: Populate shop
                break;
            case START:
                populateStartRoom(room);
                break;
            default:
                // EMPTY nu are nevoie de conținut
                break;
        }
    }

    /**
     * Populează o cameră de combat cu 1-4 inamici
     * Difficulty scaling: enemiile devin mai puternice cu depth-ul
     */
    private void populateCombatRoom(Room room, int depth, Erou hero, boolean isBoss) {
        // Difficulty scaling: +1 level la fiecare 2 depth-uri pentru normal enemies
        // +2 levels pentru bossi
        int levelBonus = isBoss ? (depth * 2) : ((depth - 1) / 2);
        int enemyLevel = hero.getNivel() + levelBonus;

        // Bossi sunt întotdeauna cu minimum 2 level-uri peste erou
        if (isBoss) {
            enemyLevel = Math.max(enemyLevel, hero.getNivel() + 2);
        }

        Random random = new Random();

        if (isBoss) {
            // Boss rooms: Single boss enemy
            Inamic boss = enemyGenerator.genereazaBoss(enemyLevel);
            room.setEnemy(boss); // Legacy single enemy field

            // Also add to new multi-enemy system with spawn position
            double bossX = 400 + random.nextDouble() * 200; // Center-ish
            double bossY = 200 + random.nextDouble() * 200;
            EnemySprite bossSprite = new EnemySprite(boss, bossX, bossY);
            room.addEnemy(bossSprite);
        } else {
            // Normal combat rooms: 1-4 enemies
            // Higher depth = more enemies
            int enemyCount;
            if (depth >= 5) {
                // Depth 5+: 2-4 enemies
                enemyCount = 2 + random.nextInt(3); // 2, 3, or 4
            } else if (depth >= 3) {
                // Depth 3-4: 1-3 enemies
                enemyCount = 1 + random.nextInt(3); // 1, 2, or 3
            } else {
                // Depth 1-2: 1-2 enemies
                enemyCount = 1 + random.nextInt(2); // 1 or 2
            }

            // Room dimensions for spawning (assuming 800x600 room size)
            double roomWidth = 800;
            double roomHeight = 600;
            double margin = 100; // Keep enemies away from edges

            for (int i = 0; i < enemyCount; i++) {
                Inamic enemy = enemyGenerator.genereazaInamicNormal(enemyLevel);

                // Random position within room bounds
                double x = margin + random.nextDouble() * (roomWidth - 2 * margin);
                double y = margin + random.nextDouble() * (roomHeight - 2 * margin);

                EnemySprite enemySprite = new EnemySprite(enemy, x, y);

                // Assign enemy type based on depth and randomness
                EnemySprite.EnemyType assignedType = assignEnemyType(depth, random);
                enemySprite.setType(assignedType);

                // Adjust stats based on type
                applyEnemyTypeModifiers(enemySprite, assignedType);

                room.addEnemy(enemySprite);

                // Set first enemy as legacy single enemy for backwards compatibility
                if (i == 0) {
                    room.setEnemy(enemy);
                }
            }
        }

        // Add hazards to combat rooms (not boss rooms)
        if (!isBoss) {
            spawnHazards(room, depth, random);
        }
    }

    /**
     * Populează o cameră cu un eveniment
     * Events pot avea recompense mai bune la depth-uri mari
     */
    private void populateEventRoom(Room room, int depth) {
        DungeonEvent event = createRandomEvent(depth);
        room.setEvent(event);
    }

    /**
     * Populează o cameră cu comori (run items)
     * Mai multe și mai bune items la depth-uri mari
     */
    private void populateTreasureRoom(Room room, int depth) {
        // Mai multe items la depth-uri mari
        int itemCount = 1 + (depth / 3); // 1 item la depth 1-2, 2 la depth 3-5, etc.
        itemCount = Math.min(itemCount, 3); // Maximum 3 items

        for (int i = 0; i < itemCount; i++) {
            RunItem item = createRandomRunItem(depth);
            room.addTreasure(item);
        }
    }

    /**
     * Creează un eveniment aleatoriu
     * Recompensele sunt mai bune la depth-uri mari
     */
    private DungeonEvent createRandomEvent(int depth) {
        List<DungeonEvent> possibleEvents = new ArrayList<>();

        // Event 1: Shrine (Altar)
        DungeonEvent shrine = new DungeonEvent(
            "🕯️ Altar Misterios",
            "Un altar vechi radiază energie întunecată.\nRune stranii strălucesc pe suprafața lui..."
        );

        DungeonEvent.EventChoice pray = new DungeonEvent.EventChoice(
            "🙏 Te rogi la altar",
            "🙏",
            50.0
        );
        DungeonEvent.EventOutcome praySuccess = new DungeonEvent.EventOutcome(
            "Zeii îți zâmbesc! Primești un buff puternic!"
        );
        RunItem blessingItem = new RunItem("Binecuvântare Divină", "+15% damage", "✨",
            RunItem.RunItemRarity.RARE);
        blessingItem.addStatModifier("damage_percent", 0.15);
        praySuccess.addItemReward(blessingItem);

        DungeonEvent.EventOutcome prayFailure = new DungeonEvent.EventOutcome(
            "Zeii te blestemă! Primești un debuff..."
        );
        RunItem curseItem = new RunItem("Blestem Minor", "-10% defense", "🔴",
            RunItem.RunItemRarity.CURSED);
        curseItem.addStatModifier("defense_percent", -0.10);
        curseItem.markAsCurse();
        prayFailure.addItemReward(curseItem);

        pray.setSuccessOutcome(praySuccess);
        pray.setFailureOutcome(prayFailure);
        shrine.addChoice(pray);

        DungeonEvent.EventChoice offer = new DungeonEvent.EventChoice(
            "💰 Oferă 50 gold",
            "💰",
            100.0
        );
        DungeonEvent.EventOutcome offerSuccess = new DungeonEvent.EventOutcome(
            "Altarul acceptă ofranda! Primești putere!"
        );
        RunItem powerItem = new RunItem("Putere Divină", "+20% damage this run", "⚡",
            RunItem.RunItemRarity.RARE);
        powerItem.addStatModifier("damage_percent", 0.20);
        offerSuccess.addItemReward(powerItem);
        offerSuccess.setGoldChange(-50);
        offer.setSuccessOutcome(offerSuccess);
        shrine.addChoice(offer);

        DungeonEvent.EventChoice sacrifice = new DungeonEvent.EventChoice(
            "🩸 Sacrifică 30 HP",
            "🩸",
            100.0
        );
        DungeonEvent.EventOutcome sacrificeSuccess = new DungeonEvent.EventOutcome(
            "Sângele tău hrănește altarul! Primești un item rar!"
        );
        RunItem vampiric = new RunItem("Colți Vampirici", "Heal 10% of damage dealt", "🩸",
            RunItem.RunItemRarity.LEGENDARY);
        vampiric.addStatModifier("lifesteal", 0.10);
        sacrificeSuccess.addItemReward(vampiric);
        sacrificeSuccess.setHealthChange(-30);
        sacrifice.setSuccessOutcome(sacrificeSuccess);
        shrine.addChoice(sacrifice);

        DungeonEvent.EventChoice leave = new DungeonEvent.EventChoice(
            "🚪 Pleacă fără să atingi nimic",
            "🚪",
            100.0
        );
        DungeonEvent.EventOutcome leaveOutcome = new DungeonEvent.EventOutcome(
            "Te îndepărtezi de altar în siguranță."
        );
        leave.setSuccessOutcome(leaveOutcome);
        shrine.addChoice(leave);

        possibleEvents.add(shrine);

        // Event 2: Fântână
        DungeonEvent fountain = new DungeonEvent(
            "⛲ Fântână Magică",
            "O fântână de cristal cu apă strălucitoare.\nPoate fi vindecătoare... sau otrăvitoare."
        );

        DungeonEvent.EventChoice drink = new DungeonEvent.EventChoice(
            "💧 Bea din fântână",
            "💧",
            70.0
        );
        DungeonEvent.EventOutcome drinkSuccess = new DungeonEvent.EventOutcome(
            "Apa este vindecătoare! Te simți revigorat!"
        );
        drinkSuccess.setHealthChange(50);
        DungeonEvent.EventOutcome drinkFailure = new DungeonEvent.EventOutcome(
            "Apa este otrăvită! Pierzi viață!"
        );
        drinkFailure.setHealthChange(-20);
        drink.setSuccessOutcome(drinkSuccess);
        drink.setFailureOutcome(drinkFailure);
        fountain.addChoice(drink);

        DungeonEvent.EventChoice ignoreFountain = new DungeonEvent.EventChoice(
            "🚪 Ignore fântâna",
            "🚪",
            100.0
        );
        DungeonEvent.EventOutcome ignoreOutcome = new DungeonEvent.EventOutcome(
            "Continui drumul fără să bei."
        );
        ignoreFountain.setSuccessOutcome(ignoreOutcome);
        fountain.addChoice(ignoreFountain);

        possibleEvents.add(fountain);

        // Returnează un event aleatoriu
        return possibleEvents.get((int)(Math.random() * possibleEvents.size()));
    }

    /**
     * Creează un run item aleatoriu
     * Items mai puternice la depth-uri mari
     */
    public RunItem createRandomRunItem(int depth) {
        // Șansa de rarity crește cu depth-ul
        double rarityRoll = Math.random();
        RunItem.RunItemRarity targetRarity;

        if (depth >= 5 && rarityRoll < 0.15) {
            targetRarity = RunItem.RunItemRarity.LEGENDARY;
        } else if (depth >= 3 && rarityRoll < 0.35) {
            targetRarity = RunItem.RunItemRarity.RARE;
        } else if (depth >= 2 && rarityRoll < 0.60) {
            targetRarity = RunItem.RunItemRarity.UNCOMMON;
        } else {
            targetRarity = RunItem.RunItemRarity.COMMON;
        }

        return createRandomRunItemOfRarity(targetRarity, depth);
    }

    /**
     * Creează un run item de o anumită raritate
     */
    public RunItem createRandomRunItemOfRarity(RunItem.RunItemRarity rarity, int depth) {
        List<RunItem> possibleItems = new ArrayList<>();

        // Scalează valoarea bonusurilor cu depth-ul
        double depthScaling = 1.0 + (depth * 0.1); // +10% per depth

        switch (rarity) {
            case COMMON:
                RunItem sharpBlade = new RunItem("Lamă Ascuțită", "+15% physical damage", "🗡️", rarity);
                sharpBlade.addStatModifier("damage_percent", 0.15 * depthScaling);
                possibleItems.add(sharpBlade);

                RunItem ironSkin = new RunItem("Piele de Fier", "+20 defense", "🛡️", rarity);
                ironSkin.addStatModifier("defense_flat", 20.0 * depthScaling);
                possibleItems.add(ironSkin);

                RunItem luckyCoin = new RunItem("Monedă Norocoasă", "+25% gold", "💰", rarity);
                luckyCoin.addStatModifier("gold_percent", 0.25);
                possibleItems.add(luckyCoin);
                break;

            case UNCOMMON:
                RunItem vampiricBlade = new RunItem("Lamă Vampirică", "5% lifesteal", "🩸", rarity);
                vampiricBlade.addStatModifier("lifesteal", 0.05);
                possibleItems.add(vampiricBlade);

                RunItem critGem = new RunItem("Cristal Critic", "+10% crit chance", "⚡", rarity);
                critGem.addStatModifier("crit_chance", 0.10);
                possibleItems.add(critGem);

                RunItem quickBoots = new RunItem("Boots Rapide", "+10% dodge", "💨", rarity);
                quickBoots.addStatModifier("dodge_percent", 0.10);
                possibleItems.add(quickBoots);
                break;

            case RARE:
                RunItem fireEnchant = new RunItem("Enchant de Foc", "+8 fire damage", "🔥", rarity);
                fireEnchant.addStatModifier("fire_damage", 8.0 * depthScaling);
                possibleItems.add(fireEnchant);

                RunItem regenAmulet = new RunItem("Amuletă Regenerare", "Heal 5 HP per turn", "💚", rarity);
                regenAmulet.addStatModifier("regen_per_turn", 5.0);
                possibleItems.add(regenAmulet);

                RunItem evasionCloak = new RunItem("Mantie de Evaziune", "+15% dodge", "💨", rarity);
                evasionCloak.addStatModifier("dodge_percent", 0.15);
                possibleItems.add(evasionCloak);
                break;

            case LEGENDARY:
                RunItem berserkerRage = new RunItem("Furia Berserkerului", "+30% damage, +15% crit", "💀", rarity);
                berserkerRage.addStatModifier("damage_percent", 0.30 * depthScaling);
                berserkerRage.addStatModifier("crit_chance", 0.15);
                possibleItems.add(berserkerRage);

                RunItem dragonHeart = new RunItem("Inima Dragonului", "+50 HP regen/turn, +20% defense", "🐉", rarity);
                dragonHeart.addStatModifier("regen_per_turn", 10.0);
                dragonHeart.addStatModifier("defense_percent", 0.20);
                possibleItems.add(dragonHeart);

                RunItem shadowDancer = new RunItem("Dansator de Umbre", "+20% dodge, +15% damage", "🌑", rarity);
                shadowDancer.addStatModifier("dodge_percent", 0.20);
                shadowDancer.addStatModifier("damage_percent", 0.15 * depthScaling);
                possibleItems.add(shadowDancer);
                break;
        }

        // Fallback dacă lista e goală
        if (possibleItems.isEmpty()) {
            RunItem defaultItem = new RunItem("Item Generic", "+10% damage", "❓", RunItem.RunItemRarity.COMMON);
            defaultItem.addStatModifier("damage_percent", 0.10);
            return defaultItem;
        }

        // Returnează un item aleatoriu din rarity-ul cerut
        return possibleItems.get((int)(Math.random() * possibleItems.size()));
    }

    /**
     * Populează camera de start cu un portal de escape
     */
    private void populateStartRoom(Room room) {
        // Add escape portal in the center-bottom of the room
        InteractiveObject escapePortal = new InteractiveObject(
            InteractiveObject.ObjectType.PORTAL,
            350, // Center horizontally (800px wide room, 64px portal = (800-64)/2 = 368, rounded to 350)
            450  // Bottom area (600px tall room, place at 450)
        );
        escapePortal.setData("ESCAPE_PORTAL"); // Mark this as escape portal vs boss portal
        room.addObject(escapePortal);
    }

    /**
     * Assign enemy type based on depth
     * Early depths: mostly melee
     * Later depths: more variety with ranged, chargers, tanks
     */
    private EnemySprite.EnemyType assignEnemyType(int depth, Random random) {
        // Depth 1-2: 90% melee, 10% charger
        if (depth <= 2) {
            return random.nextDouble() < 0.9 ? EnemySprite.EnemyType.MELEE : EnemySprite.EnemyType.CHARGER;
        }

        // Depth 3-4: 60% melee, 20% ranged, 20% charger
        if (depth <= 4) {
            double roll = random.nextDouble();
            if (roll < 0.6) return EnemySprite.EnemyType.MELEE;
            if (roll < 0.8) return EnemySprite.EnemyType.RANGED;
            return EnemySprite.EnemyType.CHARGER;
        }

        // Depth 5+: 40% melee, 30% ranged, 20% charger, 10% tanky
        double roll = random.nextDouble();
        if (roll < 0.4) return EnemySprite.EnemyType.MELEE;
        if (roll < 0.7) return EnemySprite.EnemyType.RANGED;
        if (roll < 0.9) return EnemySprite.EnemyType.CHARGER;
        return EnemySprite.EnemyType.TANKY;
    }

    /**
     * Apply stat modifiers based on enemy type
     * Note: Since enemy HP and damage are not directly modifiable,
     * we use multipliers stored in EnemySprite
     */
    private void applyEnemyTypeModifiers(EnemySprite sprite, EnemySprite.EnemyType type) {
        switch (type) {
            case MELEE:
                // Standard stats - balanced
                sprite.setMoveSpeed(1.5);
                sprite.setDamageMultiplier(1.0);
                break;

            case RANGED:
                // Slower, keeps distance, lower melee damage (shoots from afar)
                sprite.setMoveSpeed(1.0);
                sprite.setDamageMultiplier(0.7); // Less damage in melee combat
                break;

            case CHARGER:
                // Fast, charges in straight lines, higher impact damage
                sprite.setMoveSpeed(2.0);
                sprite.setDamageMultiplier(1.2); // Hits harder when connecting
                break;

            case TANKY:
                // Slow but hits hard, acts as blocker
                sprite.setMoveSpeed(0.8);
                sprite.setDamageMultiplier(1.3); // High damage output
                break;

            case SUMMONER:
                // Future implementation - will summon minions
                sprite.setMoveSpeed(1.0);
                sprite.setDamageMultiplier(0.9); // Slightly weaker direct combat
                break;
        }
    }

    /**
     * Spawn environmental hazards in a room based on depth
     * Higher depths have more and deadlier hazards
     */
    private void spawnHazards(Room room, int depth, Random random) {
        // Room dimensions for spawning (assuming 800x600 room size)
        double roomWidth = 800;
        double roomHeight = 600;
        double wallThickness = 20;
        double margin = 80; // Keep hazards away from edges and spawn points

        // Determine hazard count based on depth
        int hazardCount = 0;
        if (depth >= 5) {
            // Depth 5+: 2-4 hazards
            hazardCount = 2 + random.nextInt(3);
        } else if (depth >= 3) {
            // Depth 3-4: 1-3 hazards
            hazardCount = 1 + random.nextInt(3);
        } else if (depth >= 2) {
            // Depth 2: 0-2 hazards
            hazardCount = random.nextInt(3);
        }
        // Depth 1: no hazards

        for (int i = 0; i < hazardCount; i++) {
            // Choose hazard type based on depth
            Hazard.HazardType type = chooseHazardType(depth, random);

            // Determine hazard size based on type
            double width, height;
            switch (type) {
                case SPIKES -> {
                    // Spikes are smaller, more precise
                    width = 40 + random.nextDouble() * 30; // 40-70px
                    height = 40 + random.nextDouble() * 30;
                }
                case FIRE_PIT -> {
                    // Fire pits are medium-sized circles
                    width = 60 + random.nextDouble() * 40; // 60-100px
                    height = width; // Circular
                }
                case POISON_GAS -> {
                    // Poison clouds are large areas
                    width = 80 + random.nextDouble() * 80; // 80-160px
                    height = 80 + random.nextDouble() * 80;
                }
                default -> {
                    width = 50;
                    height = 50;
                }
            }

            // Random position within room bounds, avoiding edges
            double x = margin + random.nextDouble() * (roomWidth - 2 * margin - width);
            double y = margin + random.nextDouble() * (roomHeight - 2 * margin - height);

            Hazard hazard = new Hazard(x, y, width, height, type);
            room.addHazard(hazard);
        }
    }

    /**
     * Choose hazard type based on depth and randomness
     */
    private Hazard.HazardType chooseHazardType(int depth, Random random) {
        // Depth 1-2: Only spikes
        if (depth <= 2) {
            return Hazard.HazardType.SPIKES;
        }

        // Depth 3-4: 60% spikes, 40% fire
        if (depth <= 4) {
            return random.nextDouble() < 0.6 ? Hazard.HazardType.SPIKES : Hazard.HazardType.FIRE_PIT;
        }

        // Depth 5+: 40% spikes, 40% fire, 20% poison
        double roll = random.nextDouble();
        if (roll < 0.4) return Hazard.HazardType.SPIKES;
        if (roll < 0.8) return Hazard.HazardType.FIRE_PIT;
        return Hazard.HazardType.POISON_GAS;
    }
}
