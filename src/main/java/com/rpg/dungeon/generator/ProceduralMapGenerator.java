package com.rpg.dungeon.generator;

import com.rpg.dungeon.model.*;
import com.rpg.utils.RandomUtils;

import java.util.*;

/**
 * Generator procedural de hărți pentru dungeon
 * Folosește random walk cu branching pentru a crea layout-uri variate
 */
public class ProceduralMapGenerator {

    private static final int MIN_ROOMS = 12;
    private static final double BRANCH_CHANCE = 0.4; // 40% șansă de branching
    private static final int MAX_BRANCHES = 5;

    /**
     * Generează o hartă procedurală bazată pe depth
     */
    public DungeonMap generate(int depth) {
        try {
            // Determină dimensiunea gridului bazat pe depth
            int gridSize = calculateGridSize(depth);
            int targetRooms = calculateTargetRooms(gridSize, depth);

            System.out.println("🗺️ Generating dungeon: depth=" + depth + ", gridSize=" + gridSize + ", targetRooms=" + targetRooms);

            DungeonMap map = new DungeonMap(gridSize, gridSize, depth);

            // Generează layout-ul
            List<Room> rooms = generateRooms(map, gridSize, targetRooms);

            if (rooms.size() < MIN_ROOMS) {
                System.err.println("⚠️ Generated too few rooms (" + rooms.size() + "), retrying...");
                return generate(depth); // Retry
            }

            // Setează tipurile camerelor
            assignRoomTypes(rooms, depth);

            // Găsește și setează start și boss
            Room start = rooms.stream().filter(r -> r.getType() == RoomType.START).findFirst().orElse(rooms.get(0));
            Room boss = rooms.stream().filter(r -> r.getType() == RoomType.BOSS).findFirst().orElse(rooms.get(rooms.size() - 1));

            map.setStartRoom(start);
            map.setBossRoom(boss);
            map.setCurrentRoom(start);
            start.markVisited();

            System.out.println("✅ Dungeon generated: " + rooms.size() + " rooms");
            return map;
        } catch (Exception e) {
            System.err.println("❌ Error generating procedural map: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate dungeon", e);
        }
    }

    /**
     * Calculează dimensiunea gridului bazat pe depth
     */
    private int calculateGridSize(int depth) {
        if (depth <= 1) return 5;
        if (depth <= 3) return 6;
        if (depth <= 5) return 7;
        return 8;
    }

    /**
     * Calculează numărul țintă de camere
     */
    private int calculateTargetRooms(int gridSize, int depth) {
        int minRooms = gridSize * 2 + 4;
        int maxRooms = gridSize * gridSize - 6;

        // Mai multe camere la depth-uri mai mari
        double factor = 0.5 + (depth * 0.06);
        factor = Math.min(factor, 0.75); // Cap la 75% din grid

        return (int) (minRooms + (maxRooms - minRooms) * factor);
    }

    /**
     * Generează camerele folosind random walk cu branching
     */
    private List<Room> generateRooms(DungeonMap map, int gridSize, int targetRooms) {
        List<Room> rooms = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        // Start din centru
        int startX = gridSize / 2;
        int startY = gridSize / 2;

        Room start = new Room(startX, startY, RoomType.START);
        map.setRoom(startX, startY, start);
        rooms.add(start);
        visited.add(startX + "," + startY);

        // Queue pentru random walk cu branching
        Queue<WalkPath> paths = new LinkedList<>();
        paths.add(new WalkPath(startX, startY, 0));

        int branchCount = 0;

        while (!paths.isEmpty() && rooms.size() < targetRooms) {
            WalkPath current = paths.poll();

            // Încearcă să adaugi camere în direcții random
            List<Direction> directions = new ArrayList<>(Arrays.asList(Direction.values()));
            Collections.shuffle(directions);

            boolean addedRoom = false;
            for (Direction dir : directions) {
                int newX = current.x + dir.getDx();
                int newY = current.y + dir.getDy();

                // Verifică bounds
                if (newX < 0 || newX >= gridSize || newY < 0 || newY >= gridSize) {
                    continue;
                }

                String key = newX + "," + newY;
                if (visited.contains(key)) {
                    continue;
                }

                // Creează camera nouă
                Room newRoom = new Room(newX, newY, RoomType.COMBAT); // Temporary type
                map.setRoom(newX, newY, newRoom);
                rooms.add(newRoom);
                visited.add(key);

                // Conectează cu camera curentă
                Room currentRoom = map.getRoom(current.x, current.y);
                if (currentRoom != null) {
                    currentRoom.connect(dir, newRoom);
                }

                // Continuă path-ul
                paths.add(new WalkPath(newX, newY, current.depth + 1));
                addedRoom = true;

                // Branch: adaugă un path secundar
                if (branchCount < MAX_BRANCHES && RandomUtils.chancePercent(BRANCH_CHANCE * 100)) {
                    paths.add(new WalkPath(newX, newY, current.depth + 1));
                    branchCount++;
                }

                break; // O singură cameră pe iterație pentru un path mai natural
            }

            // Dacă nu am adăugat nicio cameră, path-ul se încheie
        }

        // Dacă nu am ajuns la target, adaugă camere random
        while (rooms.size() < targetRooms) {
            if (!addRandomRoom(map, rooms, visited, gridSize)) {
                break; // Nu mai avem spațiu
            }
        }

        return rooms;
    }

    /**
     * Adaugă o cameră random conectată la o cameră existentă
     */
    private boolean addRandomRoom(DungeonMap map, List<Room> rooms, Set<String> visited, int gridSize) {
        // Încearcă să găsești o poziție validă lângă camerele existente
        Collections.shuffle(rooms);

        for (Room room : rooms) {
            List<Direction> directions = new ArrayList<>(Arrays.asList(Direction.values()));
            Collections.shuffle(directions);

            for (Direction dir : directions) {
                int newX = room.getX() + dir.getDx();
                int newY = room.getY() + dir.getDy();

                if (newX < 0 || newX >= gridSize || newY < 0 || newY >= gridSize) {
                    continue;
                }

                String key = newX + "," + newY;
                if (visited.contains(key)) {
                    continue;
                }

                Room newRoom = new Room(newX, newY, RoomType.COMBAT);
                map.setRoom(newX, newY, newRoom);
                room.connect(dir, newRoom);
                rooms.add(newRoom);
                visited.add(key);

                return true;
            }
        }

        return false;
    }

    /**
     * Atribuie tipuri camerelor bazat pe poziție și depth
     */
    private void assignRoomTypes(List<Room> rooms, int depth) {
        if (rooms.isEmpty()) return;

        // Prima cameră = START
        rooms.get(0).setType(RoomType.START);

        // Ultima cameră = BOSS
        Room boss = rooms.get(rooms.size() - 1);
        boss.setType(RoomType.BOSS);

        // Calculează distribuția tipurilor bazat pe depth
        int totalSpecialRooms = rooms.size() - 2; // Exclude start și boss

        // More special rooms for larger dungeons
        int treasureCount = Math.max(2, totalSpecialRooms / 6);
        int eventCount = Math.max(2, totalSpecialRooms / 7);
        int restCount = depth >= 2 ? Math.max(1, totalSpecialRooms / 10) : 0;
        int shrineCount = depth >= 3 ? Math.max(1, totalSpecialRooms / 12) : 0;
        int shopCount = depth >= 2 ? 1 : 0;

        // Creează o listă de tipuri disponibile
        List<RoomType> availableTypes = new ArrayList<>();
        for (int i = 0; i < treasureCount; i++) availableTypes.add(RoomType.TREASURE);
        for (int i = 0; i < eventCount; i++) availableTypes.add(RoomType.EVENT);
        for (int i = 0; i < restCount; i++) availableTypes.add(RoomType.REST);
        for (int i = 0; i < shrineCount; i++) availableTypes.add(RoomType.SHRINE);
        for (int i = 0; i < shopCount; i++) availableTypes.add(RoomType.SHOP);

        Collections.shuffle(availableTypes);

        // Atribuie tipuri camerelor (skip start și boss)
        int typeIndex = 0;
        for (int i = 1; i < rooms.size() - 1; i++) {
            Room room = rooms.get(i);

            if (typeIndex < availableTypes.size()) {
                room.setType(availableTypes.get(typeIndex));
                typeIndex++;
            } else {
                // Restul sunt COMBAT
                room.setType(RoomType.COMBAT);
            }
        }
    }

    /**
     * Clasă helper pentru random walk
     */
    private static class WalkPath {
        int x, y, depth;

        WalkPath(int x, int y, int depth) {
            this.x = x;
            this.y = y;
            this.depth = depth;
        }
    }
}
