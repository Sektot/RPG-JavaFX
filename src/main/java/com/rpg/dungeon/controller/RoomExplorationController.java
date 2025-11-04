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
    private double mouseX = 0;
    private double mouseY = 0;

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
    private List<Projectile> activeProjectiles = new ArrayList<>();
    private List<Hazard> activeHazards = new ArrayList<>();
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

    // Dash and Push abilities
    private static final double DASH_DISTANCE = 150; // Pixels to dash
    private static final double DASH_COOLDOWN = 3.0; // Seconds
    private static final double PUSH_RANGE = 200; // Range to push enemies (from push origin)
    private static final double PUSH_DISTANCE = 120; // Distance to push enemies
    private static final double PUSH_COOLDOWN = 5.0; // Seconds
    private static final double PUSH_WAVE_WIDTH = 120; // Width of push cone
    private static final double PUSH_WAVE_LENGTH = 200; // Length of push wave
    private double dashCooldownRemaining = 0;
    private double pushCooldownRemaining = 0;
    private long lastFrameTime = 0;
    private boolean isDashing = false;
    private double dashProgress = 0;
    private double dashTargetX = 0;
    private double dashTargetY = 0;
    private Label dashCooldownLabel;
    private Label pushCooldownLabel;

    // Push wave animation
    private boolean isPushActive = false;
    private double pushWaveProgress = 0;
    private double pushAngle = 0; // Direction of push in radians

    // Hazard invulnerability
    private double playerHazardInvulnerabilityEndTime = 0; // Timestamp when player invulnerability ends

    public RoomExplorationController(Stage stage, Erou hero, Room room, DungeonRun dungeonRun, Runnable onRoomExit) {
        this.stage = stage;
        this.hero = hero;
        this.currentRoom = room;
        this.dungeonRun = dungeonRun;
        this.onRoomExit = onRoomExit;

        // Initialize player at center of room (sprite is 64x64, so offset by half)
        player = new PlayerSprite(ROOM_WIDTH / 2 - 32, ROOM_HEIGHT / 2 - 32);

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

        // Initialize hazards from room
        if (!room.getHazards().isEmpty()) {
            activeHazards.addAll(room.getHazards());
            System.out.println("✅ Loaded " + activeHazards.size() + " hazards from room");
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
        // Load player animations (4 directions, 8 frames each)
        playerAnimations = new HashMap<>();

        // METHOD 1: Individual frame files (current method)
        // Requires: walk_up_0.png, walk_up_1.png, ..., walk_up_7.png
//        playerAnimations.put(Direction.NORTH, new AnimatedSprite("player", "walk_up", 8, 0.1));
//        playerAnimations.put(Direction.SOUTH, new AnimatedSprite("player", "walk_down", 8, 0.1));
//        playerAnimations.put(Direction.EAST, new AnimatedSprite("player", "walk_right", 8, 0.1));
//        playerAnimations.put(Direction.WEST, new AnimatedSprite("player", "walk_left", 8, 0.1));

        // METHOD 2: Spritesheet (alternative - uncomment to use)
        // Requires: Single spritesheet file per direction (e.g., walk_up_sheet.png)
        // Each spritesheet should have 8 frames of 64x64 pixels arranged in a single row (512x64)

        playerAnimations.put(Direction.NORTH,
            AnimatedSprite.fromSpritesheet("player", "walk_up_sheet", 8, 0.1, 64, 64, 8, 1));
        playerAnimations.put(Direction.SOUTH,
            AnimatedSprite.fromSpritesheet("player", "walk_down_sheet", 8, 0.1, 64, 64, 8, 1));
        playerAnimations.put(Direction.EAST,
            AnimatedSprite.fromSpritesheet("player", "walk_right_sheet", 8, 0.1, 64, 64, 8, 1));
        playerAnimations.put(Direction.WEST,
            AnimatedSprite.fromSpritesheet("player", "walk_left_sheet", 8, 0.1, 64, 64, 8, 1));


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
        Scene scene = new Scene(root, 1900, 1080);
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

        // Ability Cooldowns
        dashCooldownLabel = new Label("💨 Dash (SPACE): Ready");
        dashCooldownLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #3498db; -fx-font-weight: bold;");

        pushCooldownLabel = new Label("👊 Push (Q): Ready");
        pushCooldownLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #e67e22; -fx-font-weight: bold;");

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
            dashCooldownLabel,
            pushCooldownLabel,
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

    /**
     * Update cooldown UI labels
     */
    private void updateCooldownUI() {
        if (dashCooldownLabel != null) {
            if (dashCooldownRemaining > 0) {
                dashCooldownLabel.setText(String.format("💨 Dash (SPACE): %.1fs", dashCooldownRemaining));
                dashCooldownLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #95a5a6; -fx-font-weight: bold;");
            } else {
                dashCooldownLabel.setText("💨 Dash (SPACE): Ready");
                dashCooldownLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #3498db; -fx-font-weight: bold;");
            }
        }

        if (pushCooldownLabel != null) {
            if (pushCooldownRemaining > 0) {
                pushCooldownLabel.setText(String.format("👊 Push (Q): %.1fs", pushCooldownRemaining));
                pushCooldownLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #95a5a6; -fx-font-weight: bold;");
            } else {
                pushCooldownLabel.setText("👊 Push (Q): Ready");
                pushCooldownLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #e67e22; -fx-font-weight: bold;");
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

        Label controls = new Label("⌨️ WASD - Move | SPACE - Dash | Q - Push | E - Interact | M - Map");
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
                case SPACE -> handleDash();
                case Q -> handlePush();
            }
        });

        scene.setOnKeyReleased(event -> {
            pressedKeys.remove(event.getCode());
        });

        // Track mouse position relative to canvas
        canvas.setOnMouseMoved(event -> {
            mouseX = event.getX();
            mouseY = event.getY();
        });
    }

    private void startGameLoop() {
        lastFrameTime = System.nanoTime();
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update(now);
                render();
            }
        };
        gameLoop.start();
    }

    /**
     * Update game state every frame
     */
    private void update(long now) {
        // Calculate delta time
        double deltaTime = (now - lastFrameTime) / 1_000_000_000.0; // Convert to seconds
        lastFrameTime = now;

        // Update cooldowns
        if (dashCooldownRemaining > 0) {
            dashCooldownRemaining = Math.max(0, dashCooldownRemaining - deltaTime);
            updateCooldownUI();
        }
        if (pushCooldownRemaining > 0) {
            pushCooldownRemaining = Math.max(0, pushCooldownRemaining - deltaTime);
            updateCooldownUI();
        }

        // Update push wave animation
        if (isPushActive) {
            pushWaveProgress += deltaTime * 3.0; // Animation speed (complete in ~0.33 seconds)
            if (pushWaveProgress >= 1.0) {
                isPushActive = false;
                pushWaveProgress = 0;
            }
        }

        // Update enemy action cooldowns
        for (EnemySprite enemy : activeEnemies) {
            if (enemy.getActionCooldown() > 0) {
                enemy.setActionCooldown(Math.max(0, enemy.getActionCooldown() - deltaTime));
            }
        }

        // Update projectiles
        updateProjectiles();

        // Update hazards
        updateHazards(deltaTime);

        // Handle dash movement
        if (isDashing) {
            handleDashMovement();
            return; // Skip normal movement during dash
        }

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

        // Check hazard collisions and damage
        checkHazardCollisions();

        // Check enemy hazard collisions
        checkEnemyHazardCollisions();

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
            case PORTAL -> handlePortalInteraction(obj);
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
     * Handle dash ability - quick movement in facing direction
     */
    private void handleDash() {
        if (dashCooldownRemaining > 0) {
            System.out.println("⏰ Dash on cooldown: " + String.format("%.1f", dashCooldownRemaining) + "s remaining");
            return;
        }

        if (isDashing) {
            return; // Already dashing
        }

        // Start dash
        isDashing = true;
        dashProgress = 0;

        // Calculate dash target based on facing direction
        Direction facing = player.getFacing();
        double startX = player.getX();
        double startY = player.getY();

        switch (facing) {
            case NORTH -> {
                dashTargetX = startX;
                dashTargetY = startY - DASH_DISTANCE;
            }
            case SOUTH -> {
                dashTargetX = startX;
                dashTargetY = startY + DASH_DISTANCE;
            }
            case EAST -> {
                dashTargetX = startX + DASH_DISTANCE;
                dashTargetY = startY;
            }
            case WEST -> {
                dashTargetX = startX - DASH_DISTANCE;
                dashTargetY = startY;
            }
        }

        System.out.println("💨 Dashing " + facing + "!");
        dashCooldownRemaining = DASH_COOLDOWN;
    }

    /**
     * Handle dash movement each frame
     */
    private void handleDashMovement() {
        dashProgress += 0.15; // Dash speed (0-1 in ~6-7 frames)

        if (dashProgress >= 1.0) {
            // Dash complete
            isDashing = false;
            dashProgress = 0;
            return;
        }

        // Interpolate position
        double startX = player.getX();
        double startY = player.getY();
        double newX = startX + (dashTargetX - startX) * 0.15;
        double newY = startY + (dashTargetY - startY) * 0.15;

        // Store old position for collision check
        double oldX = player.getX();
        double oldY = player.getY();

        player.setX(newX);
        player.setY(newY);

        // Check collision - stop dash if hit wall
        if (checkWallCollision()) {
            player.setX(oldX);
            player.setY(oldY);
            isDashing = false;
            dashProgress = 0;
        }
    }

    /**
     * Handle push ability - knockback enemies in mouse direction
     */
    private void handlePush() {
        if (pushCooldownRemaining > 0) {
            System.out.println("⏰ Push on cooldown: " + String.format("%.1f", pushCooldownRemaining) + "s remaining");
            return;
        }

        double playerCenterX = player.getCenterX();
        double playerCenterY = player.getCenterY();

        // Calculate push direction from player toward mouse
        double dx = mouseX - playerCenterX;
        double dy = mouseY - playerCenterY;
        double length = Math.sqrt(dx * dx + dy * dy);

        if (length < 1) {
            System.out.println("❌ Mouse too close to player");
            return;
        }

        // Normalize direction
        dx /= length;
        dy /= length;

        // Store push angle for visual effect
        pushAngle = Math.atan2(dy, dx);

        // Start push wave animation
        isPushActive = true;
        pushWaveProgress = 0;

        int pushedCount = 0;

        // Find and push all enemies in the cone
        for (EnemySprite enemy : activeEnemies) {
            if (enemy.getState() == EnemySprite.EnemyState.DEFEATED) {
                continue;
            }

            double enemyCenterX = enemy.getCenterX();
            double enemyCenterY = enemy.getCenterY();

            // Vector from player to enemy
            double toEnemyX = enemyCenterX - playerCenterX;
            double toEnemyY = enemyCenterY - playerCenterY;
            double distanceToEnemy = Math.sqrt(toEnemyX * toEnemyX + toEnemyY * toEnemyY);

            if (distanceToEnemy < 1) continue; // Skip if enemy is at player position

            // Normalize
            double toEnemyNormX = toEnemyX / distanceToEnemy;
            double toEnemyNormY = toEnemyY / distanceToEnemy;

            // Calculate dot product to check if enemy is in front of push direction
            double dotProduct = toEnemyNormX * dx + toEnemyNormY * dy;

            // Calculate angle between push direction and enemy direction
            double angleToEnemy = Math.acos(Math.max(-1, Math.min(1, dotProduct)));

            // Check if enemy is within cone (45 degrees on each side = 90 degree cone)
            double coneAngle = Math.PI / 2; // 90 degrees total cone

            if (angleToEnemy <= coneAngle / 2 && distanceToEnemy <= PUSH_RANGE) {
                // Enemy is in push cone - push it in the push direction
                double newX = enemy.getX() + dx * PUSH_DISTANCE;
                double newY = enemy.getY() + dy * PUSH_DISTANCE;

                // Clamp to room bounds
                newX = Math.max(WALL_THICKNESS, Math.min(ROOM_WIDTH - WALL_THICKNESS - enemy.getWidth(), newX));
                newY = Math.max(WALL_THICKNESS, Math.min(ROOM_HEIGHT - WALL_THICKNESS - enemy.getHeight(), newY));

                enemy.setX(newX);
                enemy.setY(newY);

                pushedCount++;
                System.out.println("👊 Pushed " + enemy.getEnemy().getNume() + " in direction " + Math.toDegrees(pushAngle) + "°");
            }
        }

        System.out.println("💥 Push activated! Pushed " + pushedCount + " enemies toward mouse direction");
        pushCooldownRemaining = PUSH_COOLDOWN;
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

            // Update type-specific behaviors based on enemy type
            switch (enemy.getType()) {
                case MELEE, TANKY -> updateMeleeEnemyAI(enemy, playerCenterX, playerCenterY);
                case RANGED -> updateRangedEnemyAI(enemy, playerCenterX, playerCenterY);
                case CHARGER -> updateChargerEnemyAI(enemy, playerCenterX, playerCenterY);
                case SUMMONER -> updateMeleeEnemyAI(enemy, playerCenterX, playerCenterY); // Future: implement summoning
            }
        }
    }

    /**
     * Update melee enemy AI - standard chasing behavior
     */
    private void updateMeleeEnemyAI(EnemySprite enemy, double playerCenterX, double playerCenterY) {
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
                // Try to move towards player while avoiding hazards
                moveEnemyWithHazardAvoidance(enemy, playerCenterX, playerCenterY);

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

    /**
     * Update ranged enemy AI - keeps distance and shoots projectiles
     */
    private void updateRangedEnemyAI(EnemySprite enemy, double playerCenterX, double playerCenterY) {
        boolean canSee = enemy.canSeePlayer(playerCenterX, playerCenterY, ENEMY_VISION_RANGE);
        double distance = enemy.getDistanceToPoint(playerCenterX, playerCenterY);

        if (canSee && enemy.getState() == EnemySprite.EnemyState.IDLE) {
            enemy.setState(EnemySprite.EnemyState.CHASING);
            System.out.println("🏹 Ranged enemy " + enemy.getEnemy().getNume() + " spotted you!");
        }

        if (enemy.getState() == EnemySprite.EnemyState.CHASING) {
            if (!canSee) {
                enemy.setState(EnemySprite.EnemyState.IDLE);
                return;
            }

            double preferredDistance = 150; // Keep this distance from player
            double tooClose = 100; // Retreat if closer than this

            if (distance < tooClose) {
                // Too close - retreat away from player (with hazard avoidance)
                double dx = enemy.getCenterX() - playerCenterX;
                double dy = enemy.getCenterY() - playerCenterY;
                double length = Math.sqrt(dx * dx + dy * dy);
                if (length > 0) {
                    dx /= length;
                    dy /= length;
                    double newX = enemy.getX() + dx * enemy.getMoveSpeed();
                    double newY = enemy.getY() + dy * enemy.getMoveSpeed();

                    // Check if retreat would lead into hazard
                    if (!isPositionInHazard(newX, newY, enemy.getWidth(), enemy.getHeight())) {
                        enemy.setX(newX);
                        enemy.setY(newY);
                    }
                    // If hazard blocks retreat, just stay still
                }
            } else if (distance > preferredDistance + 50) {
                // Too far - move closer (with hazard avoidance)
                moveEnemyWithHazardAvoidance(enemy, playerCenterX, playerCenterY);
            }

            // Shoot projectile if cooldown is ready
            if (enemy.getActionCooldown() <= 0 && distance < ENEMY_VISION_RANGE) {
                shootProjectile(enemy, playerCenterX, playerCenterY);
                enemy.setActionCooldown(2.0); // 2 second cooldown between shots
            }
        }
    }

    /**
     * Update charger enemy AI - charges in straight line toward player
     */
    private void updateChargerEnemyAI(EnemySprite enemy, double playerCenterX, double playerCenterY) {
        boolean canSee = enemy.canSeePlayer(playerCenterX, playerCenterY, ENEMY_VISION_RANGE);

        if (canSee && enemy.getState() == EnemySprite.EnemyState.IDLE) {
            enemy.setState(EnemySprite.EnemyState.CHASING);
            System.out.println("⚡ Charger " + enemy.getEnemy().getNume() + " spotted you!");
        }

        if (enemy.getState() == EnemySprite.EnemyState.CHASING) {
            if (!canSee) {
                enemy.setState(EnemySprite.EnemyState.IDLE);
                enemy.setCharging(false);
                return;
            }

            if (enemy.isCharging()) {
                // Execute charge - move in straight line toward target
                double dx = enemy.getChargeTargetX() - enemy.getX();
                double dy = enemy.getChargeTargetY() - enemy.getY();
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance < 5) {
                    // Reached target or close enough - stop charging
                    enemy.setCharging(false);
                    enemy.setActionCooldown(3.0); // Cooldown after charge
                    System.out.println("💥 Charger finished charge!");
                } else {
                    // Continue charging
                    double chargeSpeed = enemy.getMoveSpeed() * 2.5; // Extra fast during charge
                    enemy.setX(enemy.getX() + (dx / distance) * chargeSpeed);
                    enemy.setY(enemy.getY() + (dy / distance) * chargeSpeed);
                }
            } else {
                // Not charging - move toward player normally
                enemy.moveTowards(playerCenterX, playerCenterY);

                // Initiate charge if cooldown ready and player in range
                double distance = enemy.getDistanceToPoint(playerCenterX, playerCenterY);
                if (enemy.getActionCooldown() <= 0 && distance < 250 && distance > 80) {
                    // Start charge!
                    enemy.setCharging(true);
                    enemy.setChargeTargetX(playerCenterX);
                    enemy.setChargeTargetY(playerCenterY);
                    System.out.println("⚡ Charger begins charge toward player!");
                }
            }
        }
    }

    /**
     * Shoot a projectile from enemy toward player
     */
    private void shootProjectile(EnemySprite enemy, double targetX, double targetY) {
        double enemyCenterX = enemy.getCenterX();
        double enemyCenterY = enemy.getCenterY();

        // Calculate direction
        double dx = targetX - enemyCenterX;
        double dy = targetY - enemyCenterY;
        double length = Math.sqrt(dx * dx + dy * dy);

        if (length > 0) {
            dx /= length;
            dy /= length;

            // Create projectile
            double projectileSpeed = 4.0;
            int projectileDamage = (int)(enemy.getEnemy().getDamage() * 0.5); // Projectiles do half damage
            Projectile projectile = new Projectile(
                enemyCenterX,
                enemyCenterY,
                dx * projectileSpeed,
                dy * projectileSpeed,
                projectileDamage,
                enemy
            );

            activeProjectiles.add(projectile);
            System.out.println("🏹 " + enemy.getEnemy().getNume() + " shoots projectile!");
        }
    }

    /**
     * Update all active projectiles - movement and collision
     */
    private void updateProjectiles() {
        activeProjectiles.removeIf(projectile -> {
            if (!projectile.isActive()) {
                return true; // Remove inactive projectiles
            }

            // Update position
            projectile.update();

            // Check if out of bounds
            if (projectile.getX() < 0 || projectile.getX() > ROOM_WIDTH ||
                projectile.getY() < 0 || projectile.getY() > ROOM_HEIGHT) {
                projectile.setActive(false);
                return true;
            }

            // Check collision with player
            if (projectile.hitsRectangle(player.getX(), player.getY(), player.getWidth(), player.getHeight())) {
                // Hit player!
                hero.primesteDamage(projectile.getDamage());
                System.out.println("💥 Projectile hit player for " + projectile.getDamage() + " damage!");
                projectile.setActive(false);

                // Update HUD
                hpLabel.setText(String.format("❤️ HP: %d/%d", hero.getViata(), hero.getViataMaxima()));
                hpBar.setProgress((double) hero.getViata() / hero.getViataMaxima());

                // Check if player died
                if (!hero.esteViu()) {
                    System.out.println("💀 Player killed by projectile!");
                    // Apply death penalty
                    dungeonRun.applyDeathPenalty();
                    if (gameLoop != null) {
                        gameLoop.stop();
                    }
                    if (onRoomExit != null) {
                        onRoomExit.run();
                    }
                }
                return true;
            }

            // Check collision with walls
            if (projectile.getX() < WALL_THICKNESS || projectile.getX() > ROOM_WIDTH - WALL_THICKNESS ||
                projectile.getY() < WALL_THICKNESS || projectile.getY() > ROOM_HEIGHT - WALL_THICKNESS) {
                projectile.setActive(false);
                return true;
            }

            return false; // Keep projectile active
        });
    }

    /**
     * Update hazard animations and state
     */
    private void updateHazards(double deltaTime) {
        for (Hazard hazard : activeHazards) {
            if (hazard.isActive()) {
                hazard.update(deltaTime);
            }
        }
    }

    /**
     * Check if player is colliding with any hazards and apply damage
     */
    private void checkHazardCollisions() {
        if (!hero.esteViu()) {
            return; // Don't damage dead players
        }

        double currentTime = System.currentTimeMillis() / 1000.0; // Convert to seconds

        // Check if player is still invulnerable
        if (currentTime < playerHazardInvulnerabilityEndTime) {
            return; // Still invulnerable
        }

        for (Hazard hazard : activeHazards) {
            if (!hazard.isActive()) {
                continue;
            }

            // Check collision with player
            if (hazard.collidesWith(player.getX(), player.getY(), player.getWidth(), player.getHeight())) {
                // Check if hazard can deal damage (based on tick rate)
                if (hazard.canDealDamage(currentTime)) {
                    // Calculate percentage-based damage
                    int damage = (int) (hero.getViataMaxima() * hazard.getDamagePercent());
                    damage = Math.max(1, damage); // At least 1 damage

                    // Apply damage
                    hero.primesteDamage(damage);
                    System.out.println("💥 " + hazard.getType() + " hit player for " + damage + " damage (" +
                        String.format("%.0f%%", hazard.getDamagePercent() * 100) + " of max HP)!");

                    // Set invulnerability
                    playerHazardInvulnerabilityEndTime = currentTime + hazard.getInvulnerabilityDuration();
                    System.out.println("🛡️ Player invulnerable for " + hazard.getInvulnerabilityDuration() + "s");

                    // Update HUD
                    hpLabel.setText(String.format("❤️ HP: %d/%d", hero.getViata(), hero.getViataMaxima()));
                    hpBar.setProgress((double) hero.getViata() / hero.getViataMaxima());

                    // Check if player died
                    if (!hero.esteViu()) {
                        System.out.println("💀 Player killed by " + hazard.getType() + "!");
                        // Apply death penalty
                        dungeonRun.applyDeathPenalty();
                        if (gameLoop != null) {
                            gameLoop.stop();
                        }
                        if (onRoomExit != null) {
                            onRoomExit.run();
                        }
                        return; // Stop checking other hazards
                    }

                    return; // Only one hazard can hit per frame
                }
            }
        }
    }

    /**
     * Check if a position would place an enemy in a hazard
     */
    private boolean isPositionInHazard(double x, double y, double width, double height) {
        for (Hazard hazard : activeHazards) {
            if (hazard.isActive() && hazard.collidesWith(x, y, width, height)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a path from current position to target will pass through a hazard
     * Uses lookahead to detect hazards before walking into them
     */
    private boolean isPathThroughHazard(double startX, double startY, double endX, double endY, double width, double height) {
        // Check multiple points along the path
        int checkPoints = 5;
        for (int i = 0; i <= checkPoints; i++) {
            double t = i / (double) checkPoints;
            double checkX = startX + (endX - startX) * t;
            double checkY = startY + (endY - startY) * t;

            if (isPositionInHazard(checkX, checkY, width, height)) {
                return true; // Path goes through hazard
            }
        }
        return false; // Path is clear
    }

    /**
     * Check if moving toward a target will lead into a hazard (lookahead)
     */
    private boolean isMovingTowardHazard(double x, double y, double targetX, double targetY, double width, double height, double lookAheadDistance) {
        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < 1) return false;

        dx /= distance;
        dy /= distance;

        // Check position after moving lookAheadDistance forward
        double futureX = x + dx * lookAheadDistance;
        double futureY = y + dy * lookAheadDistance;

        return isPositionInHazard(futureX, futureY, width, height);
    }

    /**
     * Evaluate how good a path is by simulating multiple steps ahead
     * Returns a score where higher = better path to target
     */
    private double evaluatePathQuality(double startX, double startY, double dirX, double dirY,
                                       double targetX, double targetY, double width, double height, double speed) {
        double score = 0;
        double currentX = startX;
        double currentY = startY;

        // Simulate 5 steps ahead
        int steps = 5;
        for (int i = 0; i < steps; i++) {
            currentX += dirX * speed;
            currentY += dirY * speed;

            // Check if this future position is in a hazard
            if (isPositionInHazard(currentX, currentY, width, height)) {
                return -10000; // Very bad - leads into hazard
            }

            // Check if out of bounds
            if (currentX < WALL_THICKNESS || currentX > ROOM_WIDTH - WALL_THICKNESS - width ||
                currentY < WALL_THICKNESS || currentY > ROOM_HEIGHT - WALL_THICKNESS - height) {
                return -5000; // Bad - leads to wall
            }

            // Calculate distance to target at this point
            double dx = targetX - (currentX + width / 2);
            double dy = targetY - (currentY + height / 2);
            double distToTarget = Math.sqrt(dx * dx + dy * dy);

            // Reward getting closer to target (exponentially better for later steps)
            score -= distToTarget * (i + 1); // Later steps weighted more
        }

        return score;
    }

    /**
     * Find the best direction to move when going around an obstacle
     * Uses multi-step simulation to pick the path that actually reaches the target
     */
    private double[] findBestPathAroundObstacle(double startX, double startY, double targetX, double targetY,
                                                 double width, double height, double speed) {
        double bestScore = Double.NEGATIVE_INFINITY;
        double bestDirX = 0;
        double bestDirY = 0;

        // Try 32 directions for very smooth pathfinding
        for (int i = 0; i < 32; i++) {
            double angle = (i / 32.0) * Math.PI * 2;
            double dirX = Math.cos(angle);
            double dirY = Math.sin(angle);

            double testX = startX + dirX * speed;
            double testY = startY + dirY * speed;

            // First check: is immediate position safe?
            if (isPositionInHazard(testX, testY, width, height)) {
                continue; // Skip this direction
            }

            // Evaluate how good this path is over multiple steps
            double pathScore = evaluatePathQuality(startX, startY, dirX, dirY, targetX, targetY, width, height, speed);

            // Add bonus for being in the general direction of target
            double dx = targetX - (startX + width / 2);
            double dy = targetY - (startY + height / 2);
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist > 0) {
                dx /= dist;
                dy /= dist;
                double dotProduct = dirX * dx + dirY * dy;
                pathScore += dotProduct * 200; // Bonus for right direction
            }

            if (pathScore > bestScore) {
                bestScore = pathScore;
                bestDirX = dirX;
                bestDirY = dirY;
            }
        }

        if (bestScore > Double.NEGATIVE_INFINITY) {
            return new double[]{bestDirX, bestDirY, bestScore};
        }

        return null; // No path found
    }

    /**
     * Move enemy towards target while avoiding hazards
     * Enhanced pathfinding with lookahead that can navigate around obstacles
     */
    private void moveEnemyWithHazardAvoidance(EnemySprite enemy, double targetX, double targetY) {
        double oldX = enemy.getX();
        double oldY = enemy.getY();
        double centerX = enemy.getCenterX();
        double centerY = enemy.getCenterY();

        // Calculate direct path to target
        double dx = targetX - centerX;
        double dy = targetY - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < 1) return; // Already at target

        // Normalize direction
        dx /= distance;
        dy /= distance;

        double moveSpeed = enemy.getMoveSpeed();
        double lookAhead = 50.0; // How far ahead to check for hazards

        // PRIORITY 1: If currently IN a hazard, ESCAPE IMMEDIATELY
        if (isPositionInHazard(oldX, oldY, enemy.getWidth(), enemy.getHeight())) {
            if (tryEscapeFromHazard(enemy, oldX, oldY)) {
                return; // Successfully escaped
            }
            // If escape failed, continue trying other movements below
        }

        // PRIORITY 2: Check if direct path leads into hazard (with lookahead)
        boolean directPathClear = !isMovingTowardHazard(oldX, oldY, targetX, targetY, enemy.getWidth(), enemy.getHeight(), lookAhead);

        if (directPathClear) {
            double newX = oldX + dx * moveSpeed;
            double newY = oldY + dy * moveSpeed;

            // Double-check immediate position is safe
            if (!isPositionInHazard(newX, newY, enemy.getWidth(), enemy.getHeight())) {
                enemy.setX(newX);
                enemy.setY(newY);
                return;
            }
        }

        // PRIORITY 3: Direct path blocked - use SMART pathfinding to go around
        // This uses multi-step simulation to find the best route
        double[] bestPath = findBestPathAroundObstacle(oldX, oldY, targetX, targetY,
                                                        enemy.getWidth(), enemy.getHeight(), moveSpeed);

        if (bestPath != null) {
            double bestDirX = bestPath[0];
            double bestDirY = bestPath[1];
            double bestScore = bestPath[2];

            // Move in the best direction found
            double newX = oldX + bestDirX * moveSpeed;
            double newY = oldY + bestDirY * moveSpeed;

            enemy.setX(newX);
            enemy.setY(newY);
            return;
        }

        // PRIORITY 4: Still stuck - try any safe direction (don't be picky)
        for (int i = 0; i < 16; i++) {
            double angle = (i / 16.0) * Math.PI * 2;
            double testX = oldX + Math.cos(angle) * moveSpeed;
            double testY = oldY + Math.sin(angle) * moveSpeed;

            if (!isPositionInHazard(testX, testY, enemy.getWidth(), enemy.getHeight())) {
                enemy.setX(testX);
                enemy.setY(testY);
                return;
            }
        }

        // Completely stuck - don't move
    }

    /**
     * Try to escape from a hazard by moving in any safe direction
     * AGGRESSIVE: Tries multiple distances and all angles
     */
    private boolean tryEscapeFromHazard(EnemySprite enemy, double x, double y) {
        // Try escaping at increasing distances (try closer first, then further)
        double[] escapeSpeeds = {
            enemy.getMoveSpeed() * 2.0,  // Try 2x speed first
            enemy.getMoveSpeed() * 3.0,  // Then 3x speed
            enemy.getMoveSpeed() * 4.0   // Then 4x speed (really far!)
        };

        for (double speed : escapeSpeeds) {
            // Try 16 directions for better coverage
            for (int i = 0; i < 16; i++) {
                double angle = (i / 16.0) * Math.PI * 2;
                double escapeX = x + Math.cos(angle) * speed;
                double escapeY = y + Math.sin(angle) * speed;

                // Check if this position is safe
                if (!isPositionInHazard(escapeX, escapeY, enemy.getWidth(), enemy.getHeight())) {
                    // Check it's not out of bounds
                    if (escapeX >= WALL_THICKNESS && escapeX <= ROOM_WIDTH - WALL_THICKNESS - enemy.getWidth() &&
                        escapeY >= WALL_THICKNESS && escapeY <= ROOM_HEIGHT - WALL_THICKNESS - enemy.getHeight()) {
                        enemy.setX(escapeX);
                        enemy.setY(escapeY);
                        System.out.println("🏃 " + enemy.getEnemy().getNume() + " escaped from hazard!");
                        return true;
                    }
                }
            }
        }

        // Last resort: try ANY position nearby that's safe (desperate escape)
        for (double radius = 10; radius <= 100; radius += 10) {
            for (int i = 0; i < 32; i++) {
                double angle = (i / 32.0) * Math.PI * 2;
                double escapeX = x + Math.cos(angle) * radius;
                double escapeY = y + Math.sin(angle) * radius;

                if (!isPositionInHazard(escapeX, escapeY, enemy.getWidth(), enemy.getHeight())) {
                    if (escapeX >= WALL_THICKNESS && escapeX <= ROOM_WIDTH - WALL_THICKNESS - enemy.getWidth() &&
                        escapeY >= WALL_THICKNESS && escapeY <= ROOM_HEIGHT - WALL_THICKNESS - enemy.getHeight()) {
                        enemy.setX(escapeX);
                        enemy.setY(escapeY);
                        System.out.println("🏃 " + enemy.getEnemy().getNume() + " desperately escaped!");
                        return true;
                    }
                }
            }
        }

        System.out.println("⚠️ " + enemy.getEnemy().getNume() + " completely trapped in hazard!");
        return false; // No escape found (very rare)
    }

    /**
     * Check if enemies are colliding with hazards and apply damage
     */
    private void checkEnemyHazardCollisions() {
        double currentTime = System.currentTimeMillis() / 1000.0; // Convert to seconds

        for (EnemySprite enemy : activeEnemies) {
            // Skip defeated enemies
            if (enemy.getState() == EnemySprite.EnemyState.DEFEATED) {
                continue;
            }

            // Skip enemies that are dead but not marked defeated yet
            if (!enemy.getEnemy().esteViu()) {
                continue;
            }

            // Check if enemy is invulnerable
            if (enemy.isInvulnerableToHazards(currentTime)) {
                continue; // Still invulnerable
            }

            for (Hazard hazard : activeHazards) {
                if (!hazard.isActive()) {
                    continue;
                }

                // Check collision with enemy
                if (hazard.collidesWith(enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight())) {
                    // Check if hazard can deal damage (based on tick rate)
                    if (hazard.canDealDamage(currentTime)) {
                        // Calculate percentage-based damage
                        int damage = (int) (enemy.getEnemy().getViataMaxima() * hazard.getDamagePercent());
                        damage = Math.max(1, damage); // At least 1 damage

                        // Apply damage to enemy
                        enemy.getEnemy().primesteDamage(damage);
                        System.out.println("💥 " + hazard.getType() + " hit " + enemy.getEnemy().getNume() +
                            " for " + damage + " damage (" + String.format("%.0f%%", hazard.getDamagePercent() * 100) +
                            " of max HP)! (HP: " + enemy.getEnemy().getViata() + "/" + enemy.getEnemy().getViataMaxima() + ")");

                        // Set invulnerability
                        enemy.setHazardInvulnerability(currentTime, hazard.getInvulnerabilityDuration());

                        // Check if enemy died from hazard
                        if (!enemy.getEnemy().esteViu()) {
                            System.out.println("💀 " + enemy.getEnemy().getNume() + " killed by " + hazard.getType() + "!");
                            enemy.setState(EnemySprite.EnemyState.DEFEATED);
                            activeEnemies.remove(enemy);

                            // Check if room is now cleared
                            if (activeEnemies.isEmpty()) {
                                currentRoom.markCleared();
                                System.out.println("🎉 All enemies defeated by hazards! Room cleared!");

                                // If boss room, spawn portal
                                if (currentRoom.getType() == RoomType.BOSS) {
                                    System.out.println("👑 Boss killed by hazards - spawning portal!");
                                    spawnBossPortal();
                                }
                            }
                            return; // Stop checking this enemy
                        }

                        break; // Only one hazard can hit this enemy per frame
                    }
                }
            }
        }
    }

    /**
     * Check if player is close to any enemy and trigger combat
     */
    private void checkCombatTrigger() {
        // Don't trigger combat if hero is dead
        if (!hero.esteViu()) {
            return;
        }

        // Legacy single enemy support
        if (enemyAlive && player.isInRangeOf(enemyX + 16, enemyY + 16, COMBAT_TRIGGER_RANGE)) {
            triggerCombat();
            return;
        }

        // Multi-enemy support - check all active enemies
        for (EnemySprite enemy : activeEnemies) {
            // Don't trigger combat if enemy is defeated or on cooldown
            if (enemy.getState() == EnemySprite.EnemyState.DEFEATED ||
                enemy.getState() == EnemySprite.EnemyState.COOLDOWN) {
                continue;
            }

            if (enemy.canEngagePlayer(player.getCenterX(), player.getCenterY(), COMBAT_TRIGGER_RANGE)) {
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
        battleController.setOnBattleEnd((victory, rewards) -> returnFromBattle(victory, rewards));

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

        System.out.println("⚔️ Battle started with " + engagedEnemy.getEnemy().getNume() +
            " (HP: " + engagedEnemy.getEnemy().getViata() + "/" + engagedEnemy.getEnemy().getViataMaxima() + ")");

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

        battleController.setOnBattleEnd((victory, rewards) -> returnFromMultiBattle(victory, rewards, engagedEnemy, reinforcements));
        stage.setScene(battleController.createScene());
    }

    /**
     * Return from multi-enemy battle
     */
    private void returnFromMultiBattle(boolean victory, com.rpg.service.dto.AbilityDTO.BattleResultDTO rewards,
                                      EnemySprite defeatedEnemy, List<EnemySprite> reinforcements) {
        if (victory) {
            // Store rewards temporarily (not saved until escape)
            if (rewards != null) {
                dungeonRun.addTemporaryGold(rewards.getGoldEarned());
                dungeonRun.addTemporaryExp(rewards.getExperienceEarned());
                dungeonRun.addTemporaryLoot(rewards.getLoot());

                // Also track jewels and shaorma
                if (rewards.hasJewelDrop()) {
                    dungeonRun.addTemporaryJewel(rewards.getJewelDrop());
                }
                if (rewards.getShaormaReward() > 0) {
                    dungeonRun.addTemporaryShaorma(rewards.getShaormaReward());
                }

                System.out.println("💰 Added temporary rewards: " + rewards.getGoldEarned() + " gold, " +
                    rewards.getExperienceEarned() + " exp, " + rewards.getLoot().size() + " items, " +
                    (rewards.hasJewelDrop() ? "1 jewel, " : "") +
                    (rewards.getShaormaReward() > 0 ? rewards.getShaormaReward() + " shaorma" : ""));
            }

            // Mark ALL defeated enemies (initial + reinforcements that joined)
            // Only mark and remove if actually dead
            if (!defeatedEnemy.getEnemy().esteViu()) {
                System.out.println("💀 Marking initial enemy as defeated: " + defeatedEnemy.getEnemy().getNume());
                defeatedEnemy.setState(EnemySprite.EnemyState.DEFEATED);
                activeEnemies.remove(defeatedEnemy);
            }

            // Also mark all reinforcements that joined the battle as defeated
            for (EnemySprite reinforcement : reinforcements) {
                if (!reinforcement.getEnemy().esteViu()) {
                    System.out.println("💀 Marking reinforcement as defeated: " + reinforcement.getEnemy().getNume());
                    reinforcement.setState(EnemySprite.EnemyState.DEFEATED);
                    activeEnemies.remove(reinforcement);
                }
            }

            // Check if all enemies defeated
            if (activeEnemies.isEmpty()) {
                currentRoom.markCleared();
                System.out.println("🎉 All enemies defeated! Room cleared!");

                // If this was a boss room, spawn portal for progression choice
                if (currentRoom.getType() == RoomType.BOSS) {
                    System.out.println("👑 Boss room cleared - spawning portal!");
                    spawnBossPortal();
                }
            }

            System.out.println("✅ Victory! Remaining enemies: " + activeEnemies.size());
        } else {
            // Player either fled OR died - check hero HP
            if (!hero.esteViu()) {
                // Player DIED - apply death penalty and exit to town
                System.out.println("💀 Player died in multi-battle - applying death penalty");
                System.out.println("💀 Hero HP: " + hero.getViata() + "/" + hero.getViataMaxima());
                dungeonRun.applyDeathPenalty();

                // Stop game loop
                if (gameLoop != null) {
                    System.out.println("🛑 Stopping game loop...");
                    gameLoop.stop();
                    System.out.println("🛑 Game loop stopped");
                }

                // Exit to town
                System.out.println("💀 Exiting to town. onRoomExit exists: " + (onRoomExit != null));
                if (onRoomExit != null) {
                    System.out.println("🔴 Calling onRoomExit callback");
                    try {
                        onRoomExit.run();
                        System.out.println("✅ onRoomExit callback completed");
                    } catch (Exception e) {
                        System.out.println("❌ ERROR in onRoomExit: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("🔴 No callback - creating town menu directly");
                    com.rpg.controller.TownMenuController townController =
                        new com.rpg.controller.TownMenuController(stage, hero);
                    stage.setScene(townController.createScene());
                    System.out.println("✅ Town menu scene set");
                }
                return; // IMPORTANT: Don't continue to createScene() or gameLoop.start()
            }

            // Player FLED - set cooldowns on all enemies
            defeatedEnemy.returnToBattlePosition();
            defeatedEnemy.setChaseCooldown(3000); // 3 second cooldown

            for (EnemySprite other : reinforcements) {
                other.returnToBattlePosition();
                other.setChaseCooldown(3000);
            }

            // Push player back slightly to create distance
            double fleeDistance = 80; // Push back 80 pixels
            double dx = player.getX() - defeatedEnemy.getX();
            double dy = player.getY() - defeatedEnemy.getY();
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance > 0) {
                // Normalize and push back
                double pushX = (dx / distance) * fleeDistance;
                double pushY = (dy / distance) * fleeDistance;
                player.setX(player.getX() + pushX);
                player.setY(player.getY() + pushY);

                // Make sure player stays in bounds
                player.setX(Math.max(50, Math.min(750, player.getX())));
                player.setY(Math.max(50, Math.min(550, player.getY())));
            }

            System.out.println("🏃 Fled from battle! Enemies will resume chase in 3 seconds...");
        }

        // Return to exploration (only if player is alive and fled)
        stage.setScene(createScene());
        gameLoop.start();
    }

    /**
     * Return from battle (called after battle ends) - Legacy single enemy
     */
    private void returnFromBattle(boolean victory, com.rpg.service.dto.AbilityDTO.BattleResultDTO rewards) {
        System.out.println("🔄 returnFromBattle called. Victory: " + victory);

        if (victory) {
            // Store rewards temporarily (not saved until escape)
            if (rewards != null) {
                dungeonRun.addTemporaryGold(rewards.getGoldEarned());
                dungeonRun.addTemporaryExp(rewards.getExperienceEarned());
                dungeonRun.addTemporaryLoot(rewards.getLoot());

                // Also track jewels and shaorma
                if (rewards.hasJewelDrop()) {
                    dungeonRun.addTemporaryJewel(rewards.getJewelDrop());
                }
                if (rewards.getShaormaReward() > 0) {
                    dungeonRun.addTemporaryShaorma(rewards.getShaormaReward());
                }

                System.out.println("💰 Added temporary rewards: " + rewards.getGoldEarned() + " gold, " +
                    rewards.getExperienceEarned() + " exp, " + rewards.getLoot().size() + " items, " +
                    (rewards.hasJewelDrop() ? "1 jewel, " : "") +
                    (rewards.getShaormaReward() > 0 ? rewards.getShaormaReward() + " shaorma" : ""));
            }

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
            // Player died - apply death penalty
            System.out.println("💀 Player died - applying death penalty");
            System.out.println("💀 Hero HP: " + hero.getViata() + "/" + hero.getViataMaxima());
            dungeonRun.applyDeathPenalty();

            // IMPORTANT: Stop the game loop to prevent further updates
            if (gameLoop != null) {
                System.out.println("🛑 Stopping game loop...");
                gameLoop.stop();
                System.out.println("🛑 Game loop stopped");
            } else {
                System.out.println("⚠️ WARNING: gameLoop is null!");
            }

            // Exit dungeon and return to town (death message already shown by BattleController)
            System.out.println("💀 Defeat - exiting dungeon. onRoomExit exists: " + (onRoomExit != null));

            if (onRoomExit != null) {
                System.out.println("🔴 Calling onRoomExit callback");
                try {
                    onRoomExit.run();
                    System.out.println("✅ onRoomExit callback completed");
                } catch (Exception e) {
                    System.out.println("❌ ERROR in onRoomExit callback: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                // Fallback: create town menu directly
                System.out.println("🔴 No callback - creating town menu directly");
                com.rpg.controller.TownMenuController townController =
                    new com.rpg.controller.TownMenuController(stage, hero);
                stage.setScene(townController.createScene());
                System.out.println("✅ Town menu scene set");
            }

            System.out.println("💀 returnFromBattle (death) completed");
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
        // Player sprite is now 64x64 (was 32x32)
        switch (entryDirection) {
            case NORTH -> {
                playerSprite.setX(ROOM_WIDTH / 2 - 32);  // Center horizontally (64/2)
                playerSprite.setY(WALL_THICKNESS + 10);
            }
            case SOUTH -> {
                playerSprite.setX(ROOM_WIDTH / 2 - 32);  // Center horizontally (64/2)
                playerSprite.setY(ROOM_HEIGHT - WALL_THICKNESS - 74);  // 64 + 10 margin
            }
            case WEST -> {
                playerSprite.setX(WALL_THICKNESS + 10);
                playerSprite.setY(ROOM_HEIGHT / 2 - 32);  // Center vertically (64/2)
            }
            case EAST -> {
                playerSprite.setX(ROOM_WIDTH - WALL_THICKNESS - 74);  // 64 + 10 margin
                playerSprite.setY(ROOM_HEIGHT / 2 - 32);  // Center vertically (64/2)
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

        // Draw hazards (under objects and entities)
        drawHazards();

        // Draw interactive objects
        drawObjects();

        // Draw enemies (both new multi-enemy system and legacy single enemy)
        if (enemyAlive || !activeEnemies.isEmpty()) {
            drawEnemy();
        }

        // Draw player
        drawPlayer();

        // Draw projectiles
        drawProjectiles();

        // Draw push wave effect
        if (isPushActive) {
            drawPushWave();
        }

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
        double currentTime = System.currentTimeMillis() / 1000.0;

        for (EnemySprite enemy : activeEnemies) {
            if (enemy.getState() == EnemySprite.EnemyState.DEFEATED) {
                continue; // Don't draw defeated enemies
            }

            double x = enemy.getX();
            double y = enemy.getY();

            // Check if enemy is invulnerable
            boolean isInvulnerable = enemy.isInvulnerableToHazards(currentTime);

            // Flash effect during invulnerability
            if (isInvulnerable) {
                double flashCycle = (currentTime * 10) % 2; // Flashes 5 times per second
                if (flashCycle < 1) {
                    // Flash white overlay
                    gc.save();
                    gc.setGlobalAlpha(0.4);
                    gc.setFill(Color.WHITE);
                    gc.fillOval(x - 2, y - 2, 36, 36);
                    gc.restore();
                }
            }

            // Choose color and emoji based on enemy type
            Color enemyColor;
            String enemyEmoji;
            switch (enemy.getType()) {
                case RANGED -> {
                    enemyColor = Color.rgb(150, 100, 200); // Purple for ranged
                    enemyEmoji = "🏹";
                }
                case CHARGER -> {
                    enemyColor = Color.rgb(255, 150, 0); // Orange for charger
                    enemyEmoji = enemy.isCharging() ? "⚡" : "🐂"; // Lightning when charging
                }
                case TANKY -> {
                    enemyColor = Color.rgb(100, 100, 100); // Gray for tank
                    enemyEmoji = "🛡️";
                }
                case SUMMONER -> {
                    enemyColor = Color.rgb(200, 100, 255); // Magenta for summoner
                    enemyEmoji = "🔮";
                }
                default -> { // MELEE
                    enemyColor = Color.rgb(200, 50, 50); // Red for melee
                    enemyEmoji = "👾";
                }
            }

            if (enemySprite != null && enemy.getType() == EnemySprite.EnemyType.MELEE) {
                // Draw enemy sprite (only for melee for now)
                gc.drawImage(enemySprite, x, y, 32, 32);
            } else {
                // Draw colored circle with type-specific emoji
                gc.setFill(enemyColor);
                gc.fillOval(x, y, 32, 32);

                // Add glow effect for charging enemies
                if (enemy.isCharging()) {
                    gc.setFill(Color.rgb(255, 255, 0, 0.5));
                    gc.fillOval(x - 4, y - 4, 40, 40);
                }

                gc.setFill(Color.WHITE);
                gc.setFont(new Font(20));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText(enemyEmoji, x + 16, y + 22);
            }

            // Draw vision range (debug - can remove later)
            if (enemy.getState() == EnemySprite.EnemyState.CHASING) {
                gc.setStroke(Color.rgb(255, 100, 100, 0.3));
                gc.setLineWidth(2);
                gc.strokeOval(x + 16 - ENEMY_VISION_RANGE, y + 16 - ENEMY_VISION_RANGE,
                        ENEMY_VISION_RANGE * 2, ENEMY_VISION_RANGE * 2);
            }

            // Draw charge indicator
            if (enemy.isCharging()) {
                // Draw line showing charge direction
                gc.setStroke(Color.rgb(255, 200, 0, 0.7));
                gc.setLineWidth(3);
                gc.strokeLine(x + 16, y + 16, enemy.getChargeTargetX(), enemy.getChargeTargetY());
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
        // Check if player is invulnerable
        double currentTime = System.currentTimeMillis() / 1000.0;
        boolean isInvulnerable = currentTime < playerHazardInvulnerabilityEndTime;

        // Flash effect during invulnerability (blink on/off every 0.1 seconds)
        if (isInvulnerable) {
            double flashCycle = (currentTime * 10) % 2; // Flashes 5 times per second
            if (flashCycle < 1) {
                // Flash white overlay
                gc.save();
                gc.setGlobalAlpha(0.5);
                gc.setFill(Color.WHITE);
                gc.fillOval(player.getX() - 2, player.getY() - 2, player.getWidth() + 4, player.getHeight() + 4);
                gc.restore();
            }
        }

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
     * Draw all active projectiles
     */
    /**
     * Draw environmental hazards with visual effects
     */
    private void drawHazards() {
        for (Hazard hazard : activeHazards) {
            if (!hazard.isActive()) {
                continue;
            }

            double x = hazard.getX();
            double y = hazard.getY();
            double width = hazard.getWidth();
            double height = hazard.getHeight();
            double pulseIntensity = hazard.getPulseIntensity(); // 0 to 1 for pulsing effects

            switch (hazard.getType()) {
                case SPIKES -> {
                    // Draw spikes - dark gray rectangles with triangular spikes
                    gc.setFill(Color.rgb(60, 60, 60));
                    gc.fillRect(x, y, width, height);

                    // Draw spike pattern
                    gc.setFill(Color.rgb(100, 100, 100));
                    int spikeCount = (int) (width / 10);
                    for (int i = 0; i < spikeCount; i++) {
                        double spikeX = x + (i * width / spikeCount);
                        double spikeWidth = width / spikeCount;
                        double spikeHeight = 8;

                        // Triangle spike
                        gc.fillPolygon(
                            new double[]{spikeX, spikeX + spikeWidth / 2, spikeX + spikeWidth},
                            new double[]{y + height, y + height - spikeHeight, y + height},
                            3
                        );
                    }

                    // Border
                    gc.setStroke(Color.rgb(80, 80, 80));
                    gc.setLineWidth(2);
                    gc.strokeRect(x, y, width, height);
                }

                case FIRE_PIT -> {
                    // Draw fire pit - orange/red with pulsing glow
                    double glowIntensity = 0.5 + pulseIntensity * 0.5;

                    // Outer glow (pulsing)
                    gc.setFill(Color.rgb(255, 100, 0, glowIntensity * 0.3));
                    gc.fillOval(x - width * 0.2, y - height * 0.2, width * 1.4, height * 1.4);

                    // Main fire circle
                    gc.setFill(Color.rgb(255, 100, 0, 0.8));
                    gc.fillOval(x, y, width, height);

                    // Inner fire (brighter, pulsing)
                    gc.setFill(Color.rgb(255, 200, 0, glowIntensity));
                    gc.fillOval(x + width * 0.2, y + height * 0.2, width * 0.6, height * 0.6);

                    // Flame particles (random flickering)
                    gc.setFill(Color.rgb(255, 150, 0, pulseIntensity * 0.6));
                    for (int i = 0; i < 5; i++) {
                        double particleX = x + width * 0.3 + Math.random() * width * 0.4;
                        double particleY = y + height * 0.2 + Math.random() * height * 0.3;
                        double particleSize = 4 + Math.random() * 6;
                        gc.fillOval(particleX, particleY, particleSize, particleSize);
                    }
                }

                case POISON_GAS -> {
                    // Draw poison gas - green cloud with transparency and pulsing
                    double opacity = 0.3 + pulseIntensity * 0.2;

                    // Draw multiple overlapping circles for cloud effect
                    gc.setFill(Color.rgb(100, 200, 50, opacity * 0.4));
                    gc.fillOval(x - width * 0.1, y - height * 0.1, width * 1.2, height * 1.2);

                    gc.setFill(Color.rgb(80, 180, 40, opacity * 0.5));
                    gc.fillOval(x, y, width, height);

                    gc.setFill(Color.rgb(120, 220, 60, opacity * 0.6));
                    gc.fillOval(x + width * 0.15, y + height * 0.15, width * 0.7, height * 0.7);

                    // Poison particles drifting
                    gc.setFill(Color.rgb(100, 200, 50, opacity * 0.8));
                    for (int i = 0; i < 8; i++) {
                        double particleX = x + Math.random() * width;
                        double particleY = y + Math.random() * height;
                        double particleSize = 3 + Math.random() * 5;
                        gc.fillOval(particleX, particleY, particleSize, particleSize);
                    }

                    // Warning text for poison
                    gc.setFill(Color.rgb(120, 220, 60, opacity * 0.9));
                    gc.setFont(new javafx.scene.text.Font("Arial", 16));
                    gc.fillText("☠", x + width / 2 - 8, y + height / 2 + 6);
                }
            }
        }
    }

    private void drawProjectiles() {
        for (Projectile projectile : activeProjectiles) {
            // Draw projectile as a glowing circle
            double x = projectile.getX();
            double y = projectile.getY();
            double radius = projectile.getRadius();

            // Outer glow
            gc.setFill(Color.rgb(255, 100, 100, 0.3));
            gc.fillOval(x - radius * 2, y - radius * 2, radius * 4, radius * 4);

            // Inner projectile
            gc.setFill(Color.rgb(255, 50, 50));
            gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

            // Highlight
            gc.setFill(Color.rgb(255, 200, 200, 0.8));
            gc.fillOval(x - radius * 0.5, y - radius * 0.5, radius, radius);
        }
    }

    /**
     * Draw push wave visual effect
     */
    private void drawPushWave() {
        double playerCenterX = player.getCenterX();
        double playerCenterY = player.getCenterY();

        // Calculate current wave length based on progress
        double currentLength = PUSH_WAVE_LENGTH * pushWaveProgress;
        double currentWidth = PUSH_WAVE_WIDTH * (1 - pushWaveProgress * 0.5); // Slightly narrow as it extends

        // Calculate wave opacity (fade out as it extends)
        double opacity = 1.0 - pushWaveProgress;

        // Draw the push wave as a cone/arc
        gc.save();

        // Set color with opacity (orange/yellow for push effect)
        gc.setStroke(Color.rgb(255, 165, 0, opacity * 0.8));
        gc.setFill(Color.rgb(255, 200, 0, opacity * 0.3));
        gc.setLineWidth(3);

        // Draw multiple wave rings for better visual effect
        int waveRings = 3;
        for (int i = 0; i < waveRings; i++) {
            double ringProgress = pushWaveProgress - (i * 0.15);
            if (ringProgress < 0) continue;
            if (ringProgress > 1) continue;

            double ringLength = PUSH_WAVE_LENGTH * ringProgress;
            double ringOpacity = (1.0 - ringProgress) * opacity;

            gc.setStroke(Color.rgb(255, 165, 0, ringOpacity * 0.6));
            gc.setLineWidth(4 - i);

            // Draw arc representing the push cone
            // Calculate the cone's endpoints
            double coneHalfAngle = Math.PI / 4; // 45 degrees on each side

            // Left edge of cone
            double leftAngle = pushAngle - coneHalfAngle;
            double leftX = playerCenterX + Math.cos(leftAngle) * ringLength;
            double leftY = playerCenterY + Math.sin(leftAngle) * ringLength;

            // Right edge of cone
            double rightAngle = pushAngle + coneHalfAngle;
            double rightX = playerCenterX + Math.cos(rightAngle) * ringLength;
            double rightY = playerCenterY + Math.sin(rightAngle) * ringLength;

            // Center of push
            double centerX = playerCenterX + Math.cos(pushAngle) * ringLength;
            double centerY = playerCenterY + Math.sin(pushAngle) * ringLength;

            // Draw lines forming the cone
            gc.strokeLine(playerCenterX, playerCenterY, leftX, leftY);
            gc.strokeLine(playerCenterX, playerCenterY, rightX, rightY);

            // Draw arc at the end
            gc.strokeArc(
                centerX - ringLength * 0.5,
                centerY - ringLength * 0.5,
                ringLength,
                ringLength,
                -Math.toDegrees(pushAngle + coneHalfAngle),
                Math.toDegrees(coneHalfAngle * 2),
                javafx.scene.shape.ArcType.OPEN
            );

            // Add some particle effects
            if (i == 0) {
                int particleCount = 5;
                for (int p = 0; p < particleCount; p++) {
                    double particleAngle = pushAngle + (Math.random() - 0.5) * coneHalfAngle * 2;
                    double particleDistance = ringLength * (0.8 + Math.random() * 0.4);
                    double px = playerCenterX + Math.cos(particleAngle) * particleDistance;
                    double py = playerCenterY + Math.sin(particleAngle) * particleDistance;

                    gc.setFill(Color.rgb(255, 220, 100, ringOpacity * 0.8));
                    gc.fillOval(px - 3, py - 3, 6, 6);
                }
            }
        }

        gc.restore();
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
    private void handlePortalInteraction(InteractiveObject portal) {
        // Check if this is an escape portal (in start room) or boss portal
        if ("ESCAPE_PORTAL".equals(portal.getData())) {
            handleEscapePortal();
            return;
        }

        // Otherwise, handle as boss portal
        // Award dungeon tokens for completing this floor
        int currentDepth = dungeonRun.getMap().getDepth();
        int tokensEarned = 10; // Base reward per floor
        hero.adaugaDungeonTickets(tokensEarned);

        // Build dialog showing temporary loot that will be saved
        StringBuilder message = new StringBuilder();
        message.append("You've defeated the boss!\n\n");
        message.append("💼 TEMPORARY LOOT TO BE SAVED:\n");
        message.append(String.format("  💰 Gold: %d\n", dungeonRun.getTemporaryGold()));
        message.append(String.format("  ⭐ Exp: %d\n", dungeonRun.getTemporaryExp()));
        message.append(String.format("  🎒 Items: %d\n", dungeonRun.getTemporaryLoot().size()));
        message.append("\n");
        message.append("💎 Current Gold: ").append(hero.getGold()).append("\n");
        message.append("🎫 Dungeon Tokens: +").append(tokensEarned).append(" (Total: ").append(hero.getDungeonTickets()).append(")\n");
        message.append("⭐ Current depth: ").append(currentDepth).append("\n\n");
        message.append("What do you want to do?\n\n");
        message.append("✅ YES = Go deeper (next floor)\n");
        message.append("❌ NO = Leave dungeon (SAVE LOOT and return to town)");

        boolean goDeeper = DialogHelper.showConfirmation("🌀 Boss Portal", message.toString());

        gameLoop.stop();

        if (goDeeper) {
            // Calculate next depth
            int nextDepth = currentDepth + 1;

            // Show transition message
            DialogHelper.showInfo(
                "Descending...",
                "You descend to floor " + nextDepth + "!\n\n" +
                "⚠️ Enemies will be stronger!\n" +
                "💼 Your temporary loot is still being tracked."
            );

            // Create new dungeon controller for next floor, passing the existing run
            com.rpg.dungeon.controller.DungeonController nextDungeon =
                new com.rpg.dungeon.controller.DungeonController(stage, hero, nextDepth, null, dungeonRun);

            stage.setScene(nextDungeon.createScene());
        } else {
            // Player chose to leave - SAVE ALL TEMPORARY LOOT!
            dungeonRun.escapeSuccessfully();

            String summary = "✅ ESCAPED SUCCESSFULLY!\n\n";
            summary += "💾 LOOT SAVED TO INVENTORY!\n\n";
            summary += "📊 CURRENT RUN STATS:\n";
            summary += String.format("  • Deepest Depth: %d\n", dungeonRun.getHighestDepthReached());
            summary += String.format("  • Enemies Defeated: %d\n", dungeonRun.getEnemiesKilled());
            summary += String.format("  • Bosses Defeated: %d\n", dungeonRun.getBossesKilled());
            summary += String.format("  • Run Items Collected: %d\n", dungeonRun.getActiveRunItems().size());
            summary += String.format("\n🎫 Tokens Earned This Floor: +%d\n", tokensEarned);
            summary += String.format("🎫 Total Tokens: %d\n", hero.getDungeonTickets());

            com.rpg.utils.DialogHelper.showSuccess("Escaped Dungeon", summary);

            // Return to town (exitDungeon will record completion stats)
            if (onRoomExit != null) {
                onRoomExit.run();
            }
        }
    }

    /**
     * Handle escape portal in start room - allows player to leave and save loot
     */
    private void handleEscapePortal() {
        // Build dialog showing what will be saved
        StringBuilder message = new StringBuilder();
        message.append("🌀 ESCAPE PORTAL\n\n");
        message.append("Use this portal to escape the dungeon and SAVE your temporary loot!\n\n");
        message.append("💼 TEMPORARY LOOT TO BE SAVED:\n");
        message.append(String.format("  💰 Gold: %d\n", dungeonRun.getTemporaryGold()));
        message.append(String.format("  ⭐ Exp: %d\n", dungeonRun.getTemporaryExp()));
        message.append(String.format("  🎒 Items: %d\n", dungeonRun.getTemporaryLoot().size()));
        message.append("\n");
        message.append("⚠️ You won't get tokens for escaping early!\n");
        message.append("⚠️ Only boss portals grant dungeon tokens.\n\n");
        message.append("Do you want to escape and save your loot?");

        boolean escape = DialogHelper.showConfirmation("🌀 Escape Portal", message.toString());

        if (escape) {
            gameLoop.stop();

            // SAVE ALL TEMPORARY LOOT!
            dungeonRun.escapeSuccessfully();

            String summary = "✅ ESCAPED SUCCESSFULLY!\n\n";
            summary += "💾 LOOT SAVED TO INVENTORY!\n\n";
            summary += "📊 RUN STATS:\n";
            summary += String.format("  • Deepest Depth: %d\n", dungeonRun.getHighestDepthReached());
            summary += String.format("  • Enemies Defeated: %d\n", dungeonRun.getEnemiesKilled());
            summary += String.format("  • Bosses Defeated: %d\n", dungeonRun.getBossesKilled());
            summary += String.format("  • Run Items Collected: %d\n", dungeonRun.getActiveRunItems().size());

            com.rpg.utils.DialogHelper.showSuccess("Escaped Dungeon", summary);

            // Return to town
            if (onRoomExit != null) {
                onRoomExit.run();
            }
        }
        // If they chose not to escape, just return to exploration (gameLoop keeps running)
    }
}
