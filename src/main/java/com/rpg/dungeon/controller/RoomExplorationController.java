package com.rpg.dungeon.controller;

import com.rpg.controller.BattleControllerFX;
import com.rpg.dungeon.model.*;
import com.rpg.dungeon.service.RoomContentService;
import com.rpg.model.characters.Erou;
import com.rpg.model.characters.Inamic;
import com.rpg.utils.AnimatedSprite;
import com.rpg.utils.DialogHelper;
import com.rpg.utils.SpriteManager;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.*;

/**
 * 2D Top-down room exploration controller (Binding of Isaac style)
 * Handles WASD movement, collision, interactions, and combat triggers
 */
public class RoomExplorationController {

    private Stage stage;
    private Erou hero;
    private Room currentRoom;
    private DungeonRun dungeonRun;
    private Runnable onRoomExit;  // Callback to return to dungeon map

    // Room dimensions
    private static final double ROOM_WIDTH = 1000;
    private static final double ROOM_HEIGHT = 750;
    private static final double WALL_THICKNESS = 50;
    private static final double DOOR_WIDTH = 80;
    private static final double INTERACTION_RANGE = 60;
    private static final double COMBAT_TRIGGER_RANGE = 50;  // Engagement range - battle starts
    private static final double ENEMY_VISION_RANGE = 250;   // Vision range - enemy starts chasing
    private static final double REINFORCEMENT_RANGE = 400;  // Max range for reinforcements

    // Reinforcement zones (divided into 4 parts)
    private static final double ZONE_1_RANGE = REINFORCEMENT_RANGE * 0.25; // 100px - 2 turns
    private static final double ZONE_2_RANGE = REINFORCEMENT_RANGE * 0.50; // 200px - 4 turns
    private static final double ZONE_3_RANGE = REINFORCEMENT_RANGE * 0.75; // 300px - 6 turns
    private static final double ZONE_4_RANGE = REINFORCEMENT_RANGE;        // 400px - 8 turns

    // Canvas and rendering
    private Canvas canvas;
    private GraphicsContext gc;
    private AnimationTimer gameLoop;

    // Player
    private PlayerSprite player;
    private Map<Direction, AnimatedSprite> playerAnimations;
    private AnimatedSprite currentPlayerAnimation;

    // Sprites
    private Image floorTile;
    private Image wallTile;
    private Image doorTile;
    private Image enemySprite;
    private Map<InteractiveObject.ObjectType, Image> objectSprites;

    // Input state
    private Set<KeyCode> pressedKeys = new HashSet<>();

    // UI Labels
    private Label hpLabel;
    private Label resourceLabel;
    private Label goldLabel;
    private Label interactionPrompt;
    private Label roomInfoLabel;
    private ProgressBar hpBar;
    private ProgressBar resourceBar;
    private VBox runItemsPanel;

    // Multi-enemy support
    private List<EnemySprite> activeEnemies = new ArrayList<>();
    private boolean hasLoggedEnemyDraw = false; // Debug flag

    // Legacy single enemy support (for backwards compatibility)
    private boolean enemyAlive = false;
    private double enemyX = -1;
    private double enemyY = -1;

    // Minimap
    private Canvas minimapCanvas;
    private GraphicsContext minimapGC;
    private boolean minimapVisible = true;
    private static final double MINIMAP_SIZE = 200;
    private static final double MINIMAP_MARGIN = 10;
    private static final double MINIMAP_ROOM_SIZE = 16;

    // Effects HUD toggle
    private boolean effectsHUDVisible = true;
    private Label runItemsTitle;
    private Separator effectsSeparator;

    public RoomExplorationController(Stage stage, Erou hero, Room room, DungeonRun dungeonRun, Runnable onRoomExit) {
        this.stage = stage;
        this.hero = hero;
        this.currentRoom = room;
        this.dungeonRun = dungeonRun;
        this.onRoomExit = onRoomExit;

        // Initialize player at center of room
        player = new PlayerSprite(ROOM_WIDTH / 2 - 16, ROOM_HEIGHT / 2 - 16);

        // Initialize enemies (new multi-enemy system)
        if (!room.isCleared()) {
            System.out.println("🐛 DEBUG: Room not cleared, checking for enemies...");
            System.out.println("🐛 DEBUG: room.getEnemies().size() = " + room.getEnemies().size());
            System.out.println("🐛 DEBUG: room.getEnemy() = " + room.getEnemy());

            // If room has new multi-enemy list, use that
            if (!room.getEnemies().isEmpty()) {
                List<EnemySprite> loadedEnemies = room.getAliveEnemies();
                System.out.println("✅ Found " + loadedEnemies.size() + " alive enemies in room");

                for (EnemySprite e : loadedEnemies) {
                    System.out.println("   - " + e.getEnemy().getNume() + " at (" + e.getX() + ", " + e.getY() + ") state=" + e.getState());
                    activeEnemies.add(e);
                }

                System.out.println("✅ activeEnemies.size() after loading = " + activeEnemies.size());
            }
            // Otherwise, check for legacy single enemy
            else if (room.getEnemy() != null) {
                // Convert legacy single enemy to EnemySprite
                double spawnX, spawnY;
                if (room.getEnemySpawnX() > 0) {
                    spawnX = room.getEnemySpawnX();
                    spawnY = room.getEnemySpawnY();
                } else {
                    spawnX = WALL_THICKNESS + 100 + Math.random() * (ROOM_WIDTH - WALL_THICKNESS * 2 - 200);
                    spawnY = WALL_THICKNESS + 100 + Math.random() * (ROOM_HEIGHT - WALL_THICKNESS * 2 - 200);
                    room.setEnemySpawnX(spawnX);
                    room.setEnemySpawnY(spawnY);
                }
                EnemySprite enemySprite = new EnemySprite(room.getEnemy(), spawnX, spawnY);
                room.addEnemy(enemySprite);
                activeEnemies.add(enemySprite);
                System.out.println("✅ Converted legacy enemy to EnemySprite at (" + spawnX + ", " + spawnY + ")");

                // Legacy compatibility
                enemyAlive = true;
                enemyX = spawnX;
                enemyY = spawnY;
            }
        } else {
            System.out.println("🐛 DEBUG: Room is already cleared, no enemies");
        }

        // Initialize interactive objects if room doesn't have any yet
        if (room.getObjects().isEmpty()) {
            initializeRoomObjects(room);
        }

        // Mark current room and adjacent rooms as discovered
        discoverRoom(room);

        // Load sprites
        loadSprites();
    }

    /**
     * Load all sprites for the room
     */
    private void loadSprites() {
        // Load player animations (4 directions)
        playerAnimations = new HashMap<>();
        playerAnimations.put(Direction.NORTH, new AnimatedSprite("player", "walk_up", 4, 0.1));
        playerAnimations.put(Direction.SOUTH, new AnimatedSprite("player", "walk_down", 4, 0.1));
        playerAnimations.put(Direction.EAST, new AnimatedSprite("player", "walk_right", 4, 0.1));
        playerAnimations.put(Direction.WEST, new AnimatedSprite("player", "walk_left", 4, 0.1));
        currentPlayerAnimation = playerAnimations.get(Direction.SOUTH); // Default facing down

        // Load tile sprites
        floorTile = SpriteManager.getSprite("tiles", "floor");
        wallTile = SpriteManager.getSprite("tiles", "wall");
        doorTile = SpriteManager.getSprite("tiles", "door");

        // Load enemy sprite
        enemySprite = SpriteManager.getSprite("enemies", "enemy_basic");

        // Load object sprites
        objectSprites = new HashMap<>();
        for (InteractiveObject.ObjectType type : InteractiveObject.ObjectType.values()) {
            String spriteName = type.name().toLowerCase();
            Image sprite = SpriteManager.getSprite("objects", spriteName);
            objectSprites.put(type, sprite);
        }

        System.out.println("📊 " + SpriteManager.getCacheStats());
    }

    /**
     * Initialize interactive objects based on room type
     */
    private void initializeRoomObjects(Room room) {
        switch (room.getType()) {
            case TREASURE:
                // Add chest in center
                room.addObject(new InteractiveObject(
                    InteractiveObject.ObjectType.CHEST,
                    ROOM_WIDTH / 2 - 24,
                    ROOM_HEIGHT / 2 - 24
                ));
                break;

            case EVENT:
            case SHRINE:
                // Add altar
                room.addObject(new InteractiveObject(
                    InteractiveObject.ObjectType.ALTAR,
                    ROOM_WIDTH / 2 - 32,
                    ROOM_HEIGHT / 2 - 32
                ));
                break;

            case REST:
                // Add campfire
                room.addObject(new InteractiveObject(
                    InteractiveObject.ObjectType.CAMPFIRE,
                    ROOM_WIDTH / 2 - 24,
                    ROOM_HEIGHT / 2 - 24
                ));
                break;

            case SHOP:
                // Add shop table
                room.addObject(new InteractiveObject(
                    InteractiveObject.ObjectType.SHOP_TABLE,
                    ROOM_WIDTH / 2 - 48,
                    ROOM_HEIGHT / 2 - 32
                ));
                break;
        }
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a2e;");

        // Top - Room info
        root.setTop(createHeader());

        // Center - Canvas for room rendering with HUD overlay
        canvas = new Canvas(ROOM_WIDTH, ROOM_HEIGHT);
        gc = canvas.getGraphicsContext2D();

        // Create HUD overlay
        StackPane canvasContainer = new StackPane();
        canvasContainer.getChildren().add(canvas);

        // Add HUD elements on top of canvas
        VBox hudOverlay = createHUDOverlay();
        canvasContainer.getChildren().add(hudOverlay);
        StackPane.setAlignment(hudOverlay, Pos.TOP_LEFT);

        // Add minimap overlay
        minimapCanvas = new Canvas(MINIMAP_SIZE, MINIMAP_SIZE);
        minimapGC = minimapCanvas.getGraphicsContext2D();
        canvasContainer.getChildren().add(minimapCanvas);
        StackPane.setAlignment(minimapCanvas, Pos.TOP_RIGHT);
        StackPane.setMargin(minimapCanvas, new Insets(MINIMAP_MARGIN));

        canvasContainer.setStyle("-fx-background-color: #0f0f1e; -fx-padding: 20;");
        root.setCenter(canvasContainer);

        // Bottom - Controls and info
        root.setBottom(createBottomPanel());

        // Create scene and setup input
        Scene scene = new Scene(root, 1400, 1000);
        setupInput(scene);

        // Start game loop
        startGameLoop();

        return scene;
    }

    /**
     * Create HUD overlay that appears on top of the game canvas
     */
    private VBox createHUDOverlay() {
        VBox hud = new VBox(5);
        hud.setPadding(new Insets(10));
        hud.setMaxWidth(250);
        hud.setStyle(
            "-fx-background-color: rgba(20, 20, 30, 0.85);" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 10;" +
            "-fx-border-color: #e94560;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 10;"
        );

        // HP Bar
        hpLabel = new Label(String.format("❤️ HP: %d/%d", hero.getViata(), hero.getViataMaxima()));
        hpLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-font-weight: bold;");

        hpBar = new ProgressBar();
        hpBar.setProgress((double) hero.getViata() / hero.getViataMaxima());
        hpBar.setPrefWidth(230);
        hpBar.setStyle(
            "-fx-accent: #e74c3c;" +
            "-fx-control-inner-background: #2c3e50;"
        );

        // Resource Bar (Mana/Energy/Rage)
        resourceLabel = new Label(String.format("🔋 %s: %d/%d",
            hero.getTipResursa(), hero.getResursaCurenta(), hero.getResursaMaxima()));
        resourceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-font-weight: bold;");

        resourceBar = new ProgressBar();
        resourceBar.setProgress((double) hero.getResursaCurenta() / hero.getResursaMaxima());
        resourceBar.setPrefWidth(230);
        resourceBar.setStyle(
            "-fx-accent: #3498db;" +
            "-fx-control-inner-background: #2c3e50;"
        );

        // Gold counter
        goldLabel = new Label(String.format("💰 Gold: %d", hero.getGold()));
        goldLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #f1c40f; -fx-font-weight: bold;");

        // Separator
        effectsSeparator = new Separator();
        effectsSeparator.setStyle("-fx-background-color: #e94560;");

        // Run items panel
        runItemsTitle = new Label("✨ Active Buffs (B to toggle)");
        runItemsTitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #95a5a6; -fx-font-weight: bold;");

        runItemsPanel = new VBox(3);
        runItemsPanel.setMaxHeight(120);
        runItemsPanel.setStyle("-fx-padding: 5;");
        updateRunItemsDisplay();

        hud.getChildren().addAll(
            hpLabel, hpBar,
            resourceLabel, resourceBar,
            goldLabel,
            effectsSeparator,
            runItemsTitle,
            runItemsPanel
        );

        // Make HUD non-interactive (mouse events pass through)
        hud.setMouseTransparent(true);

        return hud;
    }

    /**
     * Update the run items display on HUD
     */
    private void updateRunItemsDisplay() {
        runItemsPanel.getChildren().clear();

        List<RunItem> items = dungeonRun.getActiveRunItems();
        if (items.isEmpty()) {
            Label noItems = new Label("(None)");
            noItems.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 10px; -fx-font-style: italic;");
            runItemsPanel.getChildren().add(noItems);
        } else {
            for (RunItem item : items) {
                Label itemLabel = new Label("• " + item.getName());
                itemLabel.setWrapText(true);
                itemLabel.setStyle(
                    "-fx-text-fill: " + (item.isCurse() ? "#e74c3c" : "#2ecc71") + ";" +
                    "-fx-font-size: 10px;"
                );
                runItemsPanel.getChildren().add(itemLabel);

                // Limit to 5 items displayed
                if (runItemsPanel.getChildren().size() >= 5) {
                    Label more = new Label("...");
                    more.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 10px;");
                    runItemsPanel.getChildren().add(more);
                    break;
                }
            }
        }
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(15));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #16213e;");

        roomInfoLabel = new Label(currentRoom.getType().getIcon() + " " + currentRoom.getType().getName());
        roomInfoLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #e94560;");

        header.getChildren().add(roomInfoLabel);
        return header;
    }

    private VBox createBottomPanel() {
        VBox bottom = new VBox(10);
        bottom.setPadding(new Insets(15));
        bottom.setAlignment(Pos.CENTER);
        bottom.setStyle("-fx-background-color: #16213e;");

        interactionPrompt = new Label("");
        interactionPrompt.setStyle(
            "-fx-font-size: 18px;" +
            "-fx-text-fill: #f1c40f;" +
            "-fx-font-weight: bold;" +
            "-fx-background-color: rgba(241, 196, 15, 0.2);" +
            "-fx-padding: 8;" +
            "-fx-background-radius: 5;" +
            "-fx-border-color: #f1c40f;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 5;"
        );
        interactionPrompt.setMinHeight(40);
        interactionPrompt.setMaxWidth(600);

        Label controls = new Label("⌨️ WASD - Move | E - Interact | M - Map | ESC - Exit Room");
        controls.setStyle("-fx-font-size: 14px; -fx-text-fill: #95a5a6;");

        bottom.getChildren().addAll(interactionPrompt, controls);
        return bottom;
    }

    private void setupInput(Scene scene) {
        scene.setOnKeyPressed(event -> {
            pressedKeys.add(event.getCode());

            // Handle special keys
            switch (event.getCode()) {
                case E -> handleInteraction();
                case M -> showMap();
                case B -> toggleEffectsHUD();
                case ESCAPE -> exitRoom();
            }
        });

        scene.setOnKeyReleased(event -> {
            pressedKeys.remove(event.getCode());
        });
    }

    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render();
            }
        };
        gameLoop.start();
    }

    /**
     * Update game state every frame
     */
    private void update() {
        // Update player movement based on pressed keys
        player.setMovingUp(pressedKeys.contains(KeyCode.W));
        player.setMovingDown(pressedKeys.contains(KeyCode.S));
        player.setMovingLeft(pressedKeys.contains(KeyCode.A));
        player.setMovingRight(pressedKeys.contains(KeyCode.D));

        // Store old position for collision rollback
        double oldX = player.getX();
        double oldY = player.getY();

        // Update player position
        player.update();

        // Update player animation
        if (player.isMoving()) {
            currentPlayerAnimation = playerAnimations.get(player.getFacing());
            currentPlayerAnimation.update(0.0167); // ~60 FPS
        } else {
            currentPlayerAnimation.reset(); // Stop animation when not moving
        }

        // Check wall collisions
        if (checkWallCollision()) {
            player.setX(oldX);
            player.setY(oldY);
        }

        // Check object collisions
        for (InteractiveObject obj : currentRoom.getObjects()) {
            if (!obj.isInteracted() && obj.collidesWith(player.getX(), player.getY(), player.getWidth(), player.getHeight())) {
                player.setX(oldX);
                player.setY(oldY);
                break;
            }
        }

        // Check door transitions
        checkDoorTransitions();

        // Update interaction prompt
        updateInteractionPrompt();

        // Update enemy AI (vision, chasing)
        updateEnemyAI();

        // Check combat trigger
        checkCombatTrigger();

        // Update HUD
        updateHUD();
    }

    /**
     * Update HUD elements (HP bar, resource bar, gold, etc.)
     */
    private void updateHUD() {
        // Update HP
        hpLabel.setText(String.format("❤️ HP: %d/%d", hero.getViata(), hero.getViataMaxima()));
        hpBar.setProgress((double) hero.getViata() / hero.getViataMaxima());

        // Update Resource
        resourceLabel.setText(String.format("🔋 %s: %d/%d",
            hero.getTipResursa(), hero.getResursaCurenta(), hero.getResursaMaxima()));
        resourceBar.setProgress((double) hero.getResursaCurenta() / hero.getResursaMaxima());

        // Update Gold
        goldLabel.setText(String.format("💰 Gold: %d", hero.getGold()));
    }

    /**
     * Check if player collides with walls
     */
    private boolean checkWallCollision() {
        double px = player.getX();
        double py = player.getY();
        double pw = player.getWidth();
        double ph = player.getHeight();

        // Check each wall
        // Top wall
        if (py < WALL_THICKNESS && !isDoorAt(Direction.NORTH, px, pw)) return true;
        // Bottom wall
        if (py + ph > ROOM_HEIGHT - WALL_THICKNESS && !isDoorAt(Direction.SOUTH, px, pw)) return true;
        // Left wall
        if (px < WALL_THICKNESS && !isDoorAt(Direction.WEST, py, ph)) return true;
        // Right wall
        if (px + pw > ROOM_WIDTH - WALL_THICKNESS && !isDoorAt(Direction.EAST, py, ph)) return true;

        return false;
    }

    /**
     * Check if there's a door at the given wall position
     */
    private boolean isDoorAt(Direction dir, double pos, double size) {
        if (currentRoom.getRoomInDirection(dir) == null) return false;

        double doorStart, doorEnd;
        if (dir == Direction.NORTH || dir == Direction.SOUTH) {
            doorStart = ROOM_WIDTH / 2 - DOOR_WIDTH / 2;
            doorEnd = ROOM_WIDTH / 2 + DOOR_WIDTH / 2;
            return pos + size > doorStart && pos < doorEnd;
        } else {
            doorStart = ROOM_HEIGHT / 2 - DOOR_WIDTH / 2;
            doorEnd = ROOM_HEIGHT / 2 + DOOR_WIDTH / 2;
            return pos + size > doorStart && pos < doorEnd;
        }
    }

    /**
     * Check if player is in a door and trigger room transition
     */
    private void checkDoorTransitions() {
        double px = player.getCenterX();
        double py = player.getCenterY();

        // Check north door
        if (py < WALL_THICKNESS / 2 && currentRoom.getRoomInDirection(Direction.NORTH) != null) {
            transitionToRoom(Direction.NORTH);
        }
        // Check south door
        else if (py > ROOM_HEIGHT - WALL_THICKNESS / 2 && currentRoom.getRoomInDirection(Direction.SOUTH) != null) {
            transitionToRoom(Direction.SOUTH);
        }
        // Check west door
        else if (px < WALL_THICKNESS / 2 && currentRoom.getRoomInDirection(Direction.WEST) != null) {
            transitionToRoom(Direction.WEST);
        }
        // Check east door
        else if (px > ROOM_WIDTH - WALL_THICKNESS / 2 && currentRoom.getRoomInDirection(Direction.EAST) != null) {
            transitionToRoom(Direction.EAST);
        }
    }

    /**
     * Update the interaction prompt based on nearby objects
     */
    private void updateInteractionPrompt() {
        InteractiveObject nearestObject = findNearestInteractableObject();

        if (nearestObject != null) {
            String action = getInteractionText(nearestObject);
            interactionPrompt.setText("Press E to " + action);
        } else {
            interactionPrompt.setText("");
        }
    }

    private String getInteractionText(InteractiveObject obj) {
        if (obj.isInteracted()) return "already used ✓";
        return switch (obj.getType()) {
            case CHEST -> "💎 OPEN CHEST";
            case ALTAR -> "⛩️ PRAY AT ALTAR";
            case FOUNTAIN -> "⛲ DRINK FROM FOUNTAIN";
            case SHOP_TABLE -> "🛒 BROWSE SHOP";
            case CAMPFIRE -> "🔥 REST AT CAMPFIRE";
            case PORTAL -> "🌀 ENTER PORTAL";
            case STATUE -> "🗿 EXAMINE STATUE";
        };
    }

    /**
     * Find the nearest interactable object within range
     */
    private InteractiveObject findNearestInteractableObject() {
        for (InteractiveObject obj : currentRoom.getObjects()) {
            if (obj.isInRange(player.getCenterX(), player.getCenterY(), INTERACTION_RANGE)) {
                return obj;
            }
        }
        return null;
    }

    /**
     * Handle player pressing E to interact
     */
    private void handleInteraction() {
        InteractiveObject obj = findNearestInteractableObject();
        if (obj != null && !obj.isInteracted()) {
            obj.setInteracted(true);

            // Clear pressed keys before interaction to prevent stuck movement
            pressedKeys.clear();

            performInteraction(obj);
        }
    }

    /**
     * Perform the interaction based on object type
     */
    private void performInteraction(InteractiveObject obj) {
        switch (obj.getType()) {
            case CHEST -> handleChestInteraction();
            case ALTAR -> handleAltarInteraction();
            case CAMPFIRE -> handleCampfireInteraction();
            case SHOP_TABLE -> handleShopInteraction();
            case FOUNTAIN -> handleFountainInteraction();
            case PORTAL -> handlePortalInteraction();
            default -> DialogHelper.showInfo("Interaction", "You interact with " + obj.getType().getName());
        }
    }

    /**
     * Handle chest interaction - give treasures/run items
     */
    private void handleChestInteraction() {
        List<RunItem> treasures = currentRoom.getTreasures();

        if (treasures.isEmpty()) {
            // Treasure room should already have items, but generate one if needed
            // Note: We can't access createRandomRunItem() directly as it's private in RoomContentService
            // So we'll just give gold if there are no items
            int goldAmount = 100 + (dungeonRun.getMap().getDepth() * 20);
            hero.adaugaGold(goldAmount);
            DialogHelper.showSuccess("Empty Chest!", "You found " + goldAmount + " gold!");
            return;
        }

        StringBuilder lootMessage = new StringBuilder();
        lootMessage.append("💎 Chest opened!\n\n");

        for (RunItem treasure : treasures) {
            dungeonRun.addRunItem(treasure);
            lootMessage.append("✨ ").append(treasure.getName()).append("\n");
            lootMessage.append("   ").append(treasure.getDescription()).append("\n\n");
        }

        // Add some gold
        int goldAmount = 50 + (dungeonRun.getMap().getDepth() * 10);
        hero.adaugaGold(goldAmount);
        lootMessage.append("💰 +").append(goldAmount).append(" Gold");

        DialogHelper.showSuccess("Treasure Found!", lootMessage.toString());

        // Update HUD to show new items and gold
        updateRunItemsDisplay();
    }

    /**
     * Handle campfire interaction - rest and restore HP
     */
    private void handleCampfireInteraction() {
        int healAmount = hero.getViataMaxima() / 2; // 50% HP
        int resourceAmount = hero.getResursaMaxima() / 2; // 50% resource

        hero.vindeca(healAmount);
        hero.regenereazaResursa(resourceAmount);

        StringBuilder restMessage = new StringBuilder();
        restMessage.append("🔥 You rest by the campfire...\n\n");
        restMessage.append("❤️ Restored ").append(healAmount).append(" HP\n");
        restMessage.append("🔋 Restored ").append(resourceAmount).append(" ").append(hero.getTipResursa());

        DialogHelper.showSuccess("Rested!", restMessage.toString());
    }

    /**
     * Handle altar interaction - simple random effects
     */
    private void handleAltarInteraction() {
        java.util.Random rand = new java.util.Random();
        int eventType = rand.nextInt(3);

        String title;
        String message;

        switch (eventType) {
            case 0 -> {
                // Blessing - add damage RunItem
                title = "Ancient Shrine";
                message = "An ancient shrine radiates holy energy. You feel blessed!\n\n+20 Damage bonus!";
                RunItem boost = new RunItem(
                    "Shrine Blessing",
                    "+20 Damage from shrine blessing",
                    "⛩️",
                    RunItem.RunItemRarity.RARE
                );
                boost.addStatModifier("damage_flat", 20.0);
                dungeonRun.addRunItem(boost);
                updateRunItemsDisplay();
            }
            case 1 -> {
                // Heal
                title = "Healing Altar";
                int healAmount = 50 + (dungeonRun.getMap().getDepth() * 10);
                hero.vindeca(healAmount);
                message = "The altar restores your vitality!\n\n❤️ +" + healAmount + " HP";
            }
            default -> {
                // Gold
                title = "Golden Shrine";
                int goldAmount = 100 + (dungeonRun.getMap().getDepth() * 25);
                hero.adaugaGold(goldAmount);
                message = "You discover a hidden cache of gold!\n\n💰 +" + goldAmount + " Gold";
            }
        }

        DialogHelper.showSuccess(title, message);
        currentRoom.markCleared();
    }

    /**
     * Handle shop interaction - launch shop UI
     */
    private void handleShopInteraction() {
        gameLoop.stop();

        // Clear all pressed keys to prevent movement after shop
        pressedKeys.clear();

        // Launch dungeon upgrade shop
        com.rpg.dungeon.controller.DungeonUpgradeShopController shopController =
            new com.rpg.dungeon.controller.DungeonUpgradeShopController(
                stage,
                hero,
                () -> {
                    // Return to room exploration after shop
                    stage.setScene(createScene());
                }
            );

        stage.setScene(shopController.createScene());
    }

    /**
     * Handle fountain interaction - random effect
     */
    private void handleFountainInteraction() {
        java.util.Random rand = new java.util.Random();
        int effect = rand.nextInt(4);

        String message;
        switch (effect) {
            case 0 -> {
                // Full heal
                int healAmount = hero.getViataMaxima() - hero.getViata();
                hero.vindeca(healAmount);
                message = "The fountain's water fully restores your health!\n❤️ +" + healAmount + " HP";
            }
            case 1 -> {
                // Gold
                int goldAmount = 75 + (dungeonRun.getMap().getDepth() * 15);
                hero.adaugaGold(goldAmount);
                message = "You find gold coins at the bottom of the fountain!\n💰 +" + goldAmount + " Gold";
            }
            case 2 -> {
                // Curse (damage)
                int damage = 15 + dungeonRun.getMap().getDepth() * 5;
                hero.primesteDamage(damage);
                message = "The water was poisoned!\n💀 -" + damage + " HP";
            }
            default -> {
                // Buff (run item)
                RunItem buff = new RunItem(
                    "Fountain's Blessing",
                    "Enhanced defense and agility from the fountain",
                    "⛲",
                    RunItem.RunItemRarity.UNCOMMON
                );
                buff.addStatModifier("defense_flat", 15.0);
                buff.addStatModifier("dodge_percent", 0.05);
                dungeonRun.addRunItem(buff);
                message = "You feel empowered by the fountain!\n✨ +15 Defense, +5% Dodge";
            }
        }

        DialogHelper.showInfo("Fountain Effect", message);

        // Update HUD if fountain gave a buff
        if (effect == 3) {
            updateRunItemsDisplay();
        }
    }

    /**
     * Update enemy AI (vision detection, chasing behavior, cooldown checks)
     */
    private void updateEnemyAI() {
        double playerCenterX = player.getCenterX();
        double playerCenterY = player.getCenterY();

        for (EnemySprite enemy : activeEnemies) {
            // Skip if enemy is in combat or defeated
            if (enemy.getState() == EnemySprite.EnemyState.IN_COMBAT ||
                enemy.getState() == EnemySprite.EnemyState.DEFEATED) {
                continue;
            }

            // Check if cooldown expired
            if (enemy.getState() == EnemySprite.EnemyState.COOLDOWN) {
                if (enemy.canChaseAgain()) {
                    enemy.setState(EnemySprite.EnemyState.IDLE);
                } else {
                    continue; // Still in cooldown
                }
            }

            // Check vision range
            boolean canSee = enemy.canSeePlayer(playerCenterX, playerCenterY, ENEMY_VISION_RANGE);

            if (canSee && enemy.getState() == EnemySprite.EnemyState.IDLE) {
                // Player entered vision range - start chasing
                enemy.setState(EnemySprite.EnemyState.CHASING);
                System.out.println("👁️ " + enemy.getEnemy().getNume() + " spotted you!");
            }

            // If chasing, move towards player
            if (enemy.getState() == EnemySprite.EnemyState.CHASING) {
                // If player is still in vision range, keep chasing
                if (canSee) {
                    enemy.moveTowards(playerCenterX, playerCenterY);

                    // Update legacy enemy position for rendering (if this is the first enemy)
                    if (activeEnemies.indexOf(enemy) == 0) {
                        enemyX = enemy.getX();
                        enemyY = enemy.getY();
                    }
                } else {
                    // Player left vision range - stop chasing
                    enemy.setState(EnemySprite.EnemyState.IDLE);
                    System.out.println("👁️ " + enemy.getEnemy().getNume() + " lost sight of you");
                }
            }
        }
    }

    /**
     * Check if player is close to any enemy and trigger combat
     */
    private void checkCombatTrigger() {
        // Legacy single enemy support
        if (enemyAlive && player.isInRangeOf(enemyX + 16, enemyY + 16, COMBAT_TRIGGER_RANGE)) {
            triggerCombat();
            return;
        }

        // Multi-enemy support - check all active enemies
        for (EnemySprite enemy : activeEnemies) {
            if (enemy.getState() != EnemySprite.EnemyState.DEFEATED &&
                enemy.canEngagePlayer(player.getCenterX(), player.getCenterY(), COMBAT_TRIGGER_RANGE)) {
                triggerMultiEnemyCombat(enemy);
                return;
            }
        }
    }

    /**
     * Trigger combat with the room's enemy
     */
    private void triggerCombat() {
        gameLoop.stop();

        // Clear all pressed keys to prevent movement after battle
        pressedKeys.clear();

        BattleControllerFX battleController = new BattleControllerFX(
            stage,
            hero,
            currentRoom.getEnemy(),
            true, // inDungeon = true
            dungeonRun.getMap().getDepth()
        );

        // Set callback for when battle ends
        battleController.setOnBattleEnd((victory, rewards) -> returnFromBattle(victory));

        stage.setScene(battleController.createScene());
    }

    /**
     * Trigger multi-enemy combat with reinforcement system
     */
    private void triggerMultiEnemyCombat(EnemySprite engagedEnemy) {
        gameLoop.stop();
        pressedKeys.clear();

        // Create multi-battle state
        MultiBattleState battleState = new MultiBattleState();

        // Add the engaged enemy as the initial combatant
        battleState.addInitialEnemy(engagedEnemy.getEnemy());
        engagedEnemy.setState(EnemySprite.EnemyState.IN_COMBAT);
        engagedEnemy.saveBattlePosition();

        System.out.println("⚔️ Battle started with " + engagedEnemy.getEnemy().getNume());

        // Find all other enemies within reinforcement range and add them to queue
        double battleX = engagedEnemy.getCenterX();
        double battleY = engagedEnemy.getCenterY();

        List<EnemySprite> reinforcements = new ArrayList<>();
        for (EnemySprite other : activeEnemies) {
            if (other == engagedEnemy || other.getState() == EnemySprite.EnemyState.DEFEATED) {
                continue;
            }

            double distance = other.getDistanceToPoint(battleX, battleY);
            if (distance <= REINFORCEMENT_RANGE) {
                // Determine which zone this enemy is in
                int zone;
                if (distance <= ZONE_1_RANGE) {
                    zone = 1;
                } else if (distance <= ZONE_2_RANGE) {
                    zone = 2;
                } else if (distance <= ZONE_3_RANGE) {
                    zone = 3;
                } else {
                    zone = 4;
                }

                // Add to reinforcement queue
                battleState.addReinforcement(other.getEnemy(), zone);
                other.saveBattlePosition(); // Save position for flee mechanics
                reinforcements.add(other);
            }
        }

        // Create multi-enemy battle controller with battleState
        System.out.println("⚔️ Starting multi-enemy battle!");
        System.out.println("   Active enemies: " + battleState.getActiveEnemyCount());
        System.out.println("   Reinforcements: " + battleState.getReinforcementQueueSize() + " in queue");

        BattleControllerFX battleController = new BattleControllerFX(
            stage,
            hero,
            battleState, // Pass the multi-battle state
            true,
            dungeonRun.getMap().getDepth()
        );

        battleController.setOnBattleEnd((victory, rewards) -> returnFromMultiBattle(victory, engagedEnemy, reinforcements));
        stage.setScene(battleController.createScene());
    }

    /**
     * Return from multi-enemy battle
     */
    private void returnFromMultiBattle(boolean victory, EnemySprite defeatedEnemy, List<EnemySprite> reinforcements) {
        if (victory) {
            // Mark defeated enemy
            defeatedEnemy.setState(EnemySprite.EnemyState.DEFEATED);
            activeEnemies.remove(defeatedEnemy);

            // Check if all enemies defeated
            if (activeEnemies.isEmpty()) {
                currentRoom.markCleared();
            }

            System.out.println("✅ Victory! Remaining enemies: " + activeEnemies.size());
        } else {
            // Player fled - set cooldowns on all enemies
            defeatedEnemy.returnToBattlePosition();
            defeatedEnemy.setChaseCooldown(3000); // 3 second cooldown

            for (EnemySprite other : reinforcements) {
                other.returnToBattlePosition();
                other.setChaseCooldown(3000);
            }

            System.out.println("🏃 Fled from battle! Enemies will resume chase in 3 seconds...");
        }

        // Return to exploration
        stage.setScene(createScene());
        gameLoop.start();
    }

    /**
     * Return from battle (called after battle ends) - Legacy single enemy
     */
    private void returnFromBattle(boolean victory) {
        System.out.println("🔄 returnFromBattle called. Victory: " + victory);

        if (victory) {
            // Enemy was defeated
            enemyAlive = false;
            currentRoom.markCleared();

            // If this was a boss room, spawn portal for progression choice
            if (currentRoom.getType() == RoomType.BOSS) {
                spawnBossPortal();
            }

            // Return to this room to continue exploration
            System.out.println("✅ Victory - returning to exploration");
            stage.setScene(createScene());
        } else {
            // Player died - exit dungeon and return to town
            System.out.println("💀 Defeat - exiting dungeon. onRoomExit exists: " + (onRoomExit != null));

            if (onRoomExit != null) {
                System.out.println("🔴 Calling onRoomExit callback");
                onRoomExit.run();
            } else {
                // Fallback: create town menu directly
                System.out.println("🔴 No callback - creating town menu directly");
                com.rpg.controller.TownMenuController townController =
                    new com.rpg.controller.TownMenuController(stage, hero);
                stage.setScene(townController.createScene());
            }
        }
    }

    /**
     * Spawn a portal in boss room after defeating the boss
     */
    private void spawnBossPortal() {
        // Remove any existing portals
        currentRoom.getObjects().removeIf(obj -> obj.getType() == InteractiveObject.ObjectType.PORTAL);

        // Spawn portal in center of room
        InteractiveObject portal = new InteractiveObject(
            InteractiveObject.ObjectType.PORTAL,
            ROOM_WIDTH / 2 - 32,
            ROOM_HEIGHT / 2 - 32
        );

        currentRoom.addObject(portal);
    }

    /**
     * Transition to adjacent room
     */
    private void transitionToRoom(Direction direction) {
        gameLoop.stop();
        Room nextRoom = currentRoom.getRoomInDirection(direction);
        dungeonRun.getMap().setCurrentRoom(nextRoom);

        // Create new exploration controller for next room
        RoomExplorationController nextController = new RoomExplorationController(
            stage, hero, nextRoom, dungeonRun, onRoomExit
        );

        // Position player at opposite door
        repositionPlayerForEntry(nextController.player, direction.opposite());

        stage.setScene(nextController.createScene());
    }

    /**
     * Position player at door when entering from another room
     */
    private void repositionPlayerForEntry(PlayerSprite playerSprite, Direction entryDirection) {
        switch (entryDirection) {
            case NORTH -> {
                playerSprite.setX(ROOM_WIDTH / 2 - 16);
                playerSprite.setY(WALL_THICKNESS + 10);
            }
            case SOUTH -> {
                playerSprite.setX(ROOM_WIDTH / 2 - 16);
                playerSprite.setY(ROOM_HEIGHT - WALL_THICKNESS - 42);
            }
            case WEST -> {
                playerSprite.setX(WALL_THICKNESS + 10);
                playerSprite.setY(ROOM_HEIGHT / 2 - 16);
            }
            case EAST -> {
                playerSprite.setX(ROOM_WIDTH - WALL_THICKNESS - 42);
                playerSprite.setY(ROOM_HEIGHT / 2 - 16);
            }
        }
    }

    private void showMap() {
        // Toggle minimap visibility
        minimapVisible = !minimapVisible;
        minimapCanvas.setVisible(minimapVisible);
    }

    private void toggleEffectsHUD() {
        // Toggle effects HUD visibility
        effectsHUDVisible = !effectsHUDVisible;
        effectsSeparator.setVisible(effectsHUDVisible);
        effectsSeparator.setManaged(effectsHUDVisible);
        runItemsTitle.setVisible(effectsHUDVisible);
        runItemsTitle.setManaged(effectsHUDVisible);
        runItemsPanel.setVisible(effectsHUDVisible);
        runItemsPanel.setManaged(effectsHUDVisible);
    }

    private void exitRoom() {
        boolean confirmExit = DialogHelper.showConfirmation(
            "Exit Dungeon?",
            "Are you sure you want to leave the dungeon?\n\n" +
            "⚠️ You will lose progress on this floor!\n" +
            "💰 Gold and items collected will be kept.\n\n" +
            "✅ YES = Exit to town\n" +
            "❌ NO = Continue exploring"
        );

        if (confirmExit) {
            gameLoop.stop();
            if (onRoomExit != null) {
                onRoomExit.run();
            }
        }
    }

    /**
     * Render the room and all objects
     */
    private void render() {
        // Clear canvas
        gc.setFill(Color.rgb(20, 20, 30));
        gc.fillRect(0, 0, ROOM_WIDTH, ROOM_HEIGHT);

        // Draw floor
        drawFloor();

        // Draw walls
        drawWalls();

        // Draw doors
        drawDoors();

        // Draw interactive objects
        drawObjects();

        // Draw enemies (both new multi-enemy system and legacy single enemy)
        if (enemyAlive || !activeEnemies.isEmpty()) {
            drawEnemy();
        }

        // Draw player
        drawPlayer();

        // Draw minimap
        if (minimapVisible) {
            drawMinimap();
        }
    }

    private void drawFloor() {
        if (floorTile != null) {
            // Tile the floor with sprite
            double tileSize = 64; // Standard tile size
            for (double x = WALL_THICKNESS; x < ROOM_WIDTH - WALL_THICKNESS; x += tileSize) {
                for (double y = WALL_THICKNESS; y < ROOM_HEIGHT - WALL_THICKNESS; y += tileSize) {
                    gc.drawImage(floorTile, x, y, tileSize, tileSize);
                }
            }
        } else {
            // Fallback: solid color floor
            gc.setFill(Color.rgb(40, 40, 50));
            gc.fillRect(WALL_THICKNESS, WALL_THICKNESS,
                    ROOM_WIDTH - WALL_THICKNESS * 2,
                    ROOM_HEIGHT - WALL_THICKNESS * 2);
        }
    }

    private void drawWalls() {
        if (wallTile != null) {
            // Tile the walls with sprite
            double tileSize = 64;

            // Top wall
            for (double x = 0; x < ROOM_WIDTH; x += tileSize) {
                for (double y = 0; y < WALL_THICKNESS; y += tileSize) {
                    gc.drawImage(wallTile, x, y, tileSize, tileSize);
                }
            }
            // Bottom wall
            for (double x = 0; x < ROOM_WIDTH; x += tileSize) {
                for (double y = ROOM_HEIGHT - WALL_THICKNESS; y < ROOM_HEIGHT; y += tileSize) {
                    gc.drawImage(wallTile, x, y, tileSize, tileSize);
                }
            }
            // Left wall
            for (double x = 0; x < WALL_THICKNESS; x += tileSize) {
                for (double y = 0; y < ROOM_HEIGHT; y += tileSize) {
                    gc.drawImage(wallTile, x, y, tileSize, tileSize);
                }
            }
            // Right wall
            for (double x = ROOM_WIDTH - WALL_THICKNESS; x < ROOM_WIDTH; x += tileSize) {
                for (double y = 0; y < ROOM_HEIGHT; y += tileSize) {
                    gc.drawImage(wallTile, x, y, tileSize, tileSize);
                }
            }
        } else {
            // Fallback: solid color walls
            gc.setFill(Color.rgb(60, 60, 70));

            // Top wall
            gc.fillRect(0, 0, ROOM_WIDTH, WALL_THICKNESS);
            // Bottom wall
            gc.fillRect(0, ROOM_HEIGHT - WALL_THICKNESS, ROOM_WIDTH, WALL_THICKNESS);
            // Left wall
            gc.fillRect(0, 0, WALL_THICKNESS, ROOM_HEIGHT);
            // Right wall
            gc.fillRect(ROOM_WIDTH - WALL_THICKNESS, 0, WALL_THICKNESS, ROOM_HEIGHT);
        }
    }

    private void drawDoors() {
        if (doorTile != null) {
            // Draw doors with sprite
            // North door
            if (currentRoom.getRoomInDirection(Direction.NORTH) != null) {
                gc.drawImage(doorTile, ROOM_WIDTH / 2 - DOOR_WIDTH / 2, 0, DOOR_WIDTH, WALL_THICKNESS);
            }
            // South door
            if (currentRoom.getRoomInDirection(Direction.SOUTH) != null) {
                gc.drawImage(doorTile, ROOM_WIDTH / 2 - DOOR_WIDTH / 2, ROOM_HEIGHT - WALL_THICKNESS, DOOR_WIDTH, WALL_THICKNESS);
            }
            // West door (rotated)
            if (currentRoom.getRoomInDirection(Direction.WEST) != null) {
                gc.save();
                gc.translate(WALL_THICKNESS / 2, ROOM_HEIGHT / 2);
                gc.rotate(90);
                gc.drawImage(doorTile, -DOOR_WIDTH / 2, -WALL_THICKNESS / 2, DOOR_WIDTH, WALL_THICKNESS);
                gc.restore();
            }
            // East door (rotated)
            if (currentRoom.getRoomInDirection(Direction.EAST) != null) {
                gc.save();
                gc.translate(ROOM_WIDTH - WALL_THICKNESS / 2, ROOM_HEIGHT / 2);
                gc.rotate(90);
                gc.drawImage(doorTile, -DOOR_WIDTH / 2, -WALL_THICKNESS / 2, DOOR_WIDTH, WALL_THICKNESS);
                gc.restore();
            }
        } else {
            // Fallback: solid color doors
            gc.setFill(Color.rgb(80, 80, 90));

            // North door
            if (currentRoom.getRoomInDirection(Direction.NORTH) != null) {
                gc.fillRect(ROOM_WIDTH / 2 - DOOR_WIDTH / 2, 0, DOOR_WIDTH, WALL_THICKNESS);
            }
            // South door
            if (currentRoom.getRoomInDirection(Direction.SOUTH) != null) {
                gc.fillRect(ROOM_WIDTH / 2 - DOOR_WIDTH / 2, ROOM_HEIGHT - WALL_THICKNESS, DOOR_WIDTH, WALL_THICKNESS);
            }
            // West door
            if (currentRoom.getRoomInDirection(Direction.WEST) != null) {
                gc.fillRect(0, ROOM_HEIGHT / 2 - DOOR_WIDTH / 2, WALL_THICKNESS, DOOR_WIDTH);
            }
            // East door
            if (currentRoom.getRoomInDirection(Direction.EAST) != null) {
                gc.fillRect(ROOM_WIDTH - WALL_THICKNESS, ROOM_HEIGHT / 2 - DOOR_WIDTH / 2, WALL_THICKNESS, DOOR_WIDTH);
            }
        }
    }

    private void drawObjects() {
        for (InteractiveObject obj : currentRoom.getObjects()) {
            Image sprite = objectSprites.get(obj.getType());

            if (sprite != null) {
                // Draw sprite
                gc.drawImage(sprite, obj.getX(), obj.getY(), obj.getWidth(), obj.getHeight());

                // Dim if already used
                if (obj.isInteracted()) {
                    gc.setFill(Color.color(0, 0, 0, 0.5));
                    gc.fillRect(obj.getX(), obj.getY(), obj.getWidth(), obj.getHeight());
                }
            } else {
                // Fallback: colored rectangle with icon
                if (obj.isInteracted()) {
                    gc.setFill(Color.rgb(100, 100, 100));
                } else {
                    gc.setFill(switch (obj.getType()) {
                        case CHEST -> Color.rgb(218, 165, 32);
                        case ALTAR -> Color.rgb(138, 43, 226);
                        case FOUNTAIN -> Color.rgb(64, 224, 208);
                        case SHOP_TABLE -> Color.rgb(255, 140, 0);
                        case CAMPFIRE -> Color.rgb(255, 69, 0);
                        default -> Color.rgb(150, 150, 150);
                    });
                }

                gc.fillRect(obj.getX(), obj.getY(), obj.getWidth(), obj.getHeight());

                gc.setFill(Color.WHITE);
                gc.setFont(new Font(24));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText(obj.getType().getIcon(),
                        obj.getX() + obj.getWidth() / 2,
                        obj.getY() + obj.getHeight() / 2 + 8);
            }
        }
    }

    private void drawEnemy() {
        // DEBUG: Log enemy count (first time only)
        if (activeEnemies.size() > 0 && !activeEnemies.isEmpty()) {
            // Print once at start
            long currentTime = System.currentTimeMillis();
            if (!hasLoggedEnemyDraw) {
                System.out.println("🎨 DRAW METHOD: activeEnemies.size() = " + activeEnemies.size());
                for (int i = 0; i < activeEnemies.size(); i++) {
                    EnemySprite e = activeEnemies.get(i);
                    System.out.println("   [" + i + "] " + e.getEnemy().getNume() + " at (" + e.getX() + ", " + e.getY() + ") state=" + e.getState());
                }
                hasLoggedEnemyDraw = true;
            }
        }

        // Draw all active enemies
        for (EnemySprite enemy : activeEnemies) {
            if (enemy.getState() == EnemySprite.EnemyState.DEFEATED) {
                continue; // Don't draw defeated enemies
            }

            double x = enemy.getX();
            double y = enemy.getY();

            if (enemySprite != null) {
                // Draw enemy sprite
                gc.drawImage(enemySprite, x, y, 32, 32);
            } else {
                // Fallback: red circle with emoji
                gc.setFill(Color.rgb(200, 50, 50));
                gc.fillOval(x, y, 32, 32);

                gc.setFill(Color.WHITE);
                gc.setFont(new Font(20));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText("👾", x + 16, y + 22);
            }

            // Draw vision range (debug - can remove later)
            if (enemy.getState() == EnemySprite.EnemyState.CHASING) {
                gc.setStroke(Color.rgb(255, 100, 100, 0.3));
                gc.setLineWidth(2);
                gc.strokeOval(x + 16 - ENEMY_VISION_RANGE, y + 16 - ENEMY_VISION_RANGE,
                        ENEMY_VISION_RANGE * 2, ENEMY_VISION_RANGE * 2);
            }
        }

        // Legacy: draw old single enemy if using old system
        if (enemyAlive && activeEnemies.isEmpty()) {
            if (enemySprite != null) {
                gc.drawImage(enemySprite, enemyX, enemyY, 32, 32);
            } else {
                gc.setFill(Color.rgb(200, 50, 50));
                gc.fillOval(enemyX, enemyY, 32, 32);
                gc.setFill(Color.WHITE);
                gc.setFont(new Font(20));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText("👾", enemyX + 16, enemyY + 22);
            }
        }
    }

    private void drawPlayer() {
        Image sprite = currentPlayerAnimation.getCurrentFrame();

        if (sprite != null) {
            // Draw sprite
            gc.drawImage(sprite, player.getX(), player.getY(), player.getWidth(), player.getHeight());
        } else {
            // Fallback: blue circle with emoji
            gc.setFill(Color.rgb(64, 156, 255));
            gc.fillOval(player.getX(), player.getY(), player.getWidth(), player.getHeight());

            gc.setFill(Color.WHITE);
            gc.setFont(new Font(20));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("🧙", player.getX() + player.getWidth() / 2, player.getY() + player.getHeight() / 2 + 6);
        }
    }

    /**
     * Draw minimap showing dungeon layout
     */
    private void drawMinimap() {
        // Clear minimap
        minimapGC.setFill(Color.color(20.0/255, 20.0/255, 30.0/255, 0.9));
        minimapGC.fillRect(0, 0, MINIMAP_SIZE, MINIMAP_SIZE);

        // Draw border
        minimapGC.setStroke(Color.rgb(233, 69, 96));
        minimapGC.setLineWidth(2);
        minimapGC.strokeRect(1, 1, MINIMAP_SIZE - 2, MINIMAP_SIZE - 2);

        // Get dungeon map
        DungeonMap map = dungeonRun.getMap();
        Room[][] grid = map.getGrid();

        // Find min/max coordinates of discovered rooms
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;

        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[x].length; y++) {
                Room room = grid[x][y];
                if (room != null && room.isDiscovered()) {
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        // Calculate grid dimensions
        int gridWidth = maxX - minX + 1;
        int gridHeight = maxY - minY + 1;

        // Calculate room size to fit in minimap
        double roomSize = Math.min(
            (MINIMAP_SIZE - 20) / gridWidth,
            (MINIMAP_SIZE - 20) / gridHeight
        );
        roomSize = Math.min(roomSize, MINIMAP_ROOM_SIZE); // Cap at max size

        // Calculate offset to center minimap
        double offsetX = (MINIMAP_SIZE - (gridWidth * roomSize)) / 2;
        double offsetY = (MINIMAP_SIZE - (gridHeight * roomSize)) / 2;

        // Draw rooms (rotated 90° clockwise and mirrored)
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[x].length; y++) {
                Room room = grid[x][y];
                if (room != null && room.isDiscovered()) {
                    // Transform coordinates: 90° clockwise rotation + vertical flip
                    // Maps dungeon coordinates to visual minimap correctly
                    int transformedX = (y - minY);
                    int transformedY = (x - minX); // Flipped to fix up/down orientation

                    double roomX = offsetX + transformedX * roomSize;
                    double roomY = offsetY + transformedY * roomSize;

                    // Determine room color based on type and state
                    Color roomColor;
                    if (room == currentRoom) {
                        roomColor = Color.rgb(100, 200, 255); // Current room: bright blue
                    } else if (room.isCleared()) {
                        roomColor = Color.rgb(60, 60, 70); // Cleared: dark gray
                    } else {
                        roomColor = getRoomTypeColor(room.getType());
                    }

                    // Draw room rectangle
                    minimapGC.setFill(roomColor);
                    minimapGC.fillRect(roomX, roomY, roomSize - 2, roomSize - 2);

                    // Draw room border
                    minimapGC.setStroke(Color.rgb(150, 150, 160));
                    minimapGC.setLineWidth(1);
                    minimapGC.strokeRect(roomX, roomY, roomSize - 2, roomSize - 2);

                    // Draw room type icon
                    drawRoomIcon(room, roomX, roomY, roomSize);

                    // Draw connections (doors) - transformed to match 90° clockwise rotation
                    minimapGC.setStroke(Color.rgb(100, 100, 110));
                    minimapGC.setLineWidth(2);

                    // After 90° clockwise rotation:
                    // NORTH -> EAST, SOUTH -> WEST, WEST -> NORTH, EAST -> SOUTH
                    if (room.getRoomInDirection(Direction.NORTH) != null) {
                        // NORTH becomes EAST
                        minimapGC.strokeLine(roomX + roomSize - 2, roomY + roomSize/2 - 1, roomX + roomSize, roomY + roomSize/2 - 1);
                    }
                    if (room.getRoomInDirection(Direction.SOUTH) != null) {
                        // SOUTH becomes WEST
                        minimapGC.strokeLine(roomX, roomY + roomSize/2 - 1, roomX - 2, roomY + roomSize/2 - 1);
                    }
                    if (room.getRoomInDirection(Direction.WEST) != null) {
                        // WEST becomes NORTH
                        minimapGC.strokeLine(roomX + roomSize/2 - 1, roomY, roomX + roomSize/2 - 1, roomY - 2);
                    }
                    if (room.getRoomInDirection(Direction.EAST) != null) {
                        // EAST becomes SOUTH
                        minimapGC.strokeLine(roomX + roomSize/2 - 1, roomY + roomSize - 2, roomX + roomSize/2 - 1, roomY + roomSize);
                    }
                }
            }
        }

        // Draw title
        minimapGC.setFill(Color.WHITE);
        minimapGC.setFont(new Font(10));
        minimapGC.setTextAlign(TextAlignment.CENTER);
        minimapGC.fillText("MAP (M to toggle)", MINIMAP_SIZE / 2, 12);
    }

    /**
     * Get color for room type
     */
    private Color getRoomTypeColor(RoomType type) {
        return switch (type) {
            case START -> Color.rgb(50, 200, 50);      // Green
            case COMBAT -> Color.rgb(200, 50, 50);     // Red
            case TREASURE -> Color.rgb(255, 215, 0);   // Gold
            case SHOP -> Color.rgb(100, 200, 255);     // Light blue
            case REST -> Color.rgb(255, 165, 0);       // Orange
            case EVENT, SHRINE -> Color.rgb(200, 100, 255); // Purple
            case BOSS -> Color.rgb(150, 0, 0);         // Dark red
            case EMPTY -> Color.rgb(120, 120, 130);    // Gray
        };
    }

    /**
     * Draw icon representing room type
     */
    private void drawRoomIcon(Room room, double x, double y, double size) {
        minimapGC.setFill(Color.WHITE);
        minimapGC.setFont(new Font(size * 0.6));
        minimapGC.setTextAlign(TextAlignment.CENTER);

        String icon = switch (room.getType()) {
            case START -> "🏠";
            case COMBAT -> enemyAlive && room == currentRoom ? "⚔️" : "💀";
            case TREASURE -> room.isCleared() ? "📭" : "📦";
            case SHOP -> "🛒";
            case REST -> "🔥";
            case EVENT, SHRINE -> "⛩️";
            case BOSS -> "👹";
            case EMPTY -> "";
        };

        if (!icon.isEmpty()) {
            minimapGC.fillText(icon, x + size/2 - 1, y + size * 0.7);
        }
    }

    /**
     * Mark a room and its adjacent rooms as discovered
     */
    private void discoverRoom(Room room) {
        // Mark current room as discovered and visited
        room.markDiscovered();
        room.markVisited();

        // Mark adjacent rooms as discovered (but not visited)
        for (Direction dir : Direction.values()) {
            Room adjacent = room.getRoomInDirection(dir);
            if (adjacent != null) {
                adjacent.markDiscovered();
            }
        }
    }

    /**
     * Handle portal interaction - choice to leave or go deeper
     */
    private void handlePortalInteraction() {
        // Award dungeon tokens for completing this floor
        int currentDepth = dungeonRun.getMap().getDepth();
        int tokensEarned = 10; // Base reward per floor
        hero.adaugaDungeonTickets(tokensEarned);

        boolean goDeeper = DialogHelper.showConfirmation(
            "🌀 Boss Portal",
            "You've defeated the boss!\n\n" +
            "💎 Gold: " + hero.getGold() + "\n" +
            "🎫 Dungeon Tokens: +" + tokensEarned + " (Total: " + hero.getDungeonTickets() + ")\n" +
            "⭐ Current depth: " + currentDepth + "\n\n" +
            "What do you want to do?\n\n" +
            "✅ YES = Go deeper (next floor)\n" +
            "❌ NO = Leave dungeon (return to town)"
        );

        gameLoop.stop();

        if (goDeeper) {
            // Calculate next depth
            int nextDepth = currentDepth + 1;

            // Show transition message
            DialogHelper.showInfo(
                "Descending...",
                "You descend to floor " + nextDepth + "!\n\n" +
                "⚠️ Enemies will be stronger!"
            );

            // Create new dungeon controller for next floor, passing the existing run
            com.rpg.dungeon.controller.DungeonController nextDungeon =
                new com.rpg.dungeon.controller.DungeonController(stage, hero, nextDepth, null, dungeonRun);

            stage.setScene(nextDungeon.createScene());
        } else {
            // Player chose to leave - just show summary (stats will be recorded on actual exit)
            String summary = "✅ LEAVING DUNGEON!\n\n";
            summary += "Returning to town...\n\n";
            summary += "📊 CURRENT RUN STATS:\n";
            summary += String.format("  • Deepest Depth: %d\n", dungeonRun.getHighestDepthReached());
            summary += String.format("  • Enemies Defeated: %d\n", dungeonRun.getEnemiesKilled());
            summary += String.format("  • Bosses Defeated: %d\n", dungeonRun.getBossesKilled());
            summary += String.format("  • Items Collected: %d\n", dungeonRun.getActiveRunItems().size());
            summary += String.format("\n🎫 Tokens Earned This Floor: +%d\n", tokensEarned);
            summary += String.format("🎫 Total Tokens: %d\n", hero.getDungeonTickets());
            summary += "\n💡 Full stats will be recorded when you exit!";

            com.rpg.utils.DialogHelper.showSuccess("Leaving Dungeon", summary);

            // Return to town (exitDungeon will record completion stats)
            if (onRoomExit != null) {
                onRoomExit.run();
            }
        }
    }
}
