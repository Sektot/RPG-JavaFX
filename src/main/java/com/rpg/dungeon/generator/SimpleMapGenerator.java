package com.rpg.dungeon.generator;

import com.rpg.dungeon.model.*;
import com.rpg.utils.RandomUtils;

/**
 * Generator simplu de hărți 3x3 pentru proof of concept
 * Layout:
 *
 *   C - E - T
 *   |   |
 *   C - S - C
 *       |   |
 *       C - B
 *
 * S = Start, C = Combat, E = Event, T = Treasure, B = Boss
 */
public class SimpleMapGenerator {

    /**
     * Generează o hartă simplă 3x3
     */
    public DungeonMap generate(int depth) {
        try {
            DungeonMap map = new DungeonMap(3, 3, depth);

            // Creează camerele în poziții fixe pentru POC
            Room start = new Room(1, 1, RoomType.START);
            Room boss = new Room(2, 2, RoomType.BOSS);

            Room combat1 = new Room(0, 0, RoomType.COMBAT);
            Room combat2 = new Room(0, 1, RoomType.COMBAT);
            Room combat3 = new Room(2, 1, RoomType.COMBAT);
            Room combat4 = new Room(1, 2, RoomType.COMBAT);

            Room event = new Room(1, 0, RoomType.EVENT);
            Room treasure = new Room(2, 0, RoomType.TREASURE);

            // Plasează camerele în grid
            map.setRoom(0, 0, combat1);
            map.setRoom(1, 0, event);
            map.setRoom(2, 0, treasure);
            map.setRoom(0, 1, combat2);
            map.setRoom(1, 1, start);
            map.setRoom(2, 1, combat3);
            map.setRoom(1, 2, combat4);
            map.setRoom(2, 2, boss);

            // Conectează camerele - NU folosi connect() care conectează ambele direcții
            // Rândul 0 - conectări orizontale
            combat1.connect(Direction.EAST, event);
            event.connect(Direction.EAST, treasure);

            // Coloane verticale
            combat1.connect(Direction.SOUTH, combat2);
            event.connect(Direction.SOUTH, start);
            treasure.connect(Direction.SOUTH, combat3);

            // Rândul 2
            start.connect(Direction.SOUTH, combat4);
            combat3.connect(Direction.SOUTH, boss);
            combat4.connect(Direction.EAST, boss);

            // Setează start și boss
            map.setStartRoom(start);
            map.setBossRoom(boss);
            map.setCurrentRoom(start);
            start.markVisited();

            System.out.println("✅ Map generated successfully!");
            System.out.println("Start room connections: " + start.getAvailableDirections().size());

            return map;
        } catch (Exception e) {
            System.err.println("❌ Error generating map: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate dungeon map", e);
        }
    }

    /**
     * Generează o hartă cu layout aleatoriu (pentru viitor)
     */
    public DungeonMap generateRandom(int depth) {
        // TODO: Implementare mai târziu cu random walk algorithm
        return generate(depth);
    }
}
