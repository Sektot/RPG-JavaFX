package com.rpg.dungeon.model;

import com.rpg.model.characters.Inamic;

import java.io.Serializable;
import java.util.*;

/**
 * Reprezintă o cameră în dungeon
 */
public class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    private int x;
    private int y;
    private RoomType type;
    private boolean visited;
    private boolean discovered;  // Room is revealed on map
    private boolean cleared;
    private Map<Direction, Room> connections;

    // Content specific fiecărui tip de cameră
    private Inamic enemy;  // Pentru COMBAT (deprecated - use enemies list)
    private List<RunItem> treasures;  // Pentru TREASURE
    private DungeonEvent event;  // Pentru EVENT

    // 2D Exploration data
    private List<InteractiveObject> objects;  // Interactive objects in room
    private List<EnemySprite> enemies;  // Multiple enemies in room
    private List<Hazard> hazards;  // Environmental hazards in room
    private double enemySpawnX = -1;  // Enemy spawn position (pixels) - deprecated
    private double enemySpawnY = -1;

    public Room(int x, int y, RoomType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.visited = false;
        this.discovered = false;
        this.cleared = false;
        this.connections = new HashMap<>();
        this.treasures = new ArrayList<>();
        this.objects = new ArrayList<>();
        this.enemies = new ArrayList<>();
        this.hazards = new ArrayList<>();
    }

    /**
     * Conectează această cameră cu alta într-o direcție
     */
    public void connect(Direction direction, Room other) {
        this.connections.put(direction, other);
        other.connections.put(direction.opposite(), this);
    }

    /**
     * Verifică dacă această cameră este conectată cu alta
     */
    public boolean isConnectedTo(Room other) {
        return connections.containsValue(other);
    }

    /**
     * Returnează camera conectată într-o direcție
     */
    public Room getRoomInDirection(Direction direction) {
        return connections.get(direction);
    }

    /**
     * Returnează toate direcțiile disponibile din această cameră
     */
    public List<Direction> getAvailableDirections() {
        return new ArrayList<>(connections.keySet());
    }

    /**
     * Marchează camera ca vizitată
     */
    public void markVisited() {
        this.visited = true;
    }

    /**
     * Marchează camera ca descoperită (vizibilă pe hartă)
     */
    public void markDiscovered() {
        this.discovered = true;
    }

    /**
     * Marchează camera ca cleared (inamicul învins sau eventul terminat)
     */
    public void markCleared() {
        this.cleared = true;
    }

    // Getters și setters
    public int getX() { return x; }
    public int getY() { return y; }
    public RoomType getType() { return type; }
    public void setType(RoomType type) { this.type = type; }
    public boolean isVisited() { return visited; }
    public boolean isDiscovered() { return discovered; }
    public boolean isCleared() { return cleared; }
    public Map<Direction, Room> getConnections() { return new HashMap<>(connections); }

    public Inamic getEnemy() { return enemy; }
    public void setEnemy(Inamic enemy) { this.enemy = enemy; }

    public List<RunItem> getTreasures() { return new ArrayList<>(treasures); }
    public void addTreasure(RunItem item) { this.treasures.add(item); }

    public DungeonEvent getEvent() { return event; }
    public void setEvent(DungeonEvent event) { this.event = event; }

    public List<InteractiveObject> getObjects() { return objects; }
    public void addObject(InteractiveObject object) { this.objects.add(object); }

    public double getEnemySpawnX() { return enemySpawnX; }
    public void setEnemySpawnX(double x) { this.enemySpawnX = x; }
    public double getEnemySpawnY() { return enemySpawnY; }
    public void setEnemySpawnY(double y) { this.enemySpawnY = y; }

    // Multiple enemies support
    public List<EnemySprite> getEnemies() { return enemies; }
    public void addEnemy(EnemySprite enemySprite) { this.enemies.add(enemySprite); }
    public void removeEnemy(EnemySprite enemySprite) { this.enemies.remove(enemySprite); }

    /**
     * Get all alive enemies in room
     */
    public List<EnemySprite> getAliveEnemies() {
        List<EnemySprite> alive = new ArrayList<>();
        for (EnemySprite sprite : enemies) {
            if (sprite.getState() != EnemySprite.EnemyState.DEFEATED) {
                alive.add(sprite);
            }
        }
        return alive;
    }

    /**
     * Check if room has any alive enemies
     */
    public boolean hasAliveEnemies() {
        for (EnemySprite sprite : enemies) {
            if (sprite.getState() != EnemySprite.EnemyState.DEFEATED) {
                return true;
            }
        }
        return false;
    }

    // Hazard management
    public List<Hazard> getHazards() { return hazards; }
    public void addHazard(Hazard hazard) { this.hazards.add(hazard); }
    public void removeHazard(Hazard hazard) { this.hazards.remove(hazard); }

    /**
     * Returnează reprezentarea vizuală a camerei
     */
    public String getDisplayIcon() {
        if (!visited) {
            return "？";  // Unexplored
        }
        if (cleared) {
            return "✅";  // Cleared
        }
        return type.getIcon();
    }

    @Override
    public String toString() {
        return String.format("Room[%d,%d]:%s", x, y, type.getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Room room = (Room) obj;
        return x == room.x && y == room.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
