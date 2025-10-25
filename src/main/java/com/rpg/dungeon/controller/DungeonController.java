package com.rpg.dungeon.controller;

import com.rpg.controller.BattleControllerFX;
import com.rpg.controller.TownMenuController;
import com.rpg.dungeon.generator.SimpleMapGenerator;
import com.rpg.dungeon.generator.ProceduralMapGenerator;
import com.rpg.dungeon.model.*;
import com.rpg.dungeon.service.RoomContentService;
import com.rpg.dungeon.service.RunItemModifierService;
import com.rpg.model.characters.Erou;
import com.rpg.service.dto.AbilityDTO;
import com.rpg.utils.DialogHelper;
import com.rpg.utils.RandomUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

/**
 * Main Dungeon Controller - Primary battle system
 * Handles procedurally generated dungeons with roguelike mechanics
 */
public class DungeonController {

    private Stage stage;
    private DungeonRun currentRun;
    private Erou hero;
    private Runnable onExitCallback; // Callback to return to town

    // Statistics tracking for meta-progression
    private int enemiesKilledThisRun;
    private boolean bossKilledThisRun;
    private int totalDepthsCleared; // Track how many depths cleared in this run

    // UI Components
    private TextArea mapDisplay;
    private TextArea logDisplay;
    private VBox runItemsDisplay;
    private Label statsLabel;
    private VBox navigationButtons;

    public DungeonController(Stage stage, Erou hero, int depth) {
        this(stage, hero, depth, null, null);
    }

    public DungeonController(Stage stage, Erou hero, int depth, Runnable onExitCallback) {
        this(stage, hero, depth, onExitCallback, null);
    }

    /**
     * Constructor for continuing an existing run to a deeper floor
     */
    public DungeonController(Stage stage, Erou hero, int depth, Runnable onExitCallback, DungeonRun existingRun) {
        this.onExitCallback = onExitCallback;
        try {
            this.stage = stage;
            this.hero = hero;

            System.out.println("🔧 Initializing DungeonController...");
            System.out.println("Hero: " + hero.getNume() + ", Level: " + hero.getNivel());

            // Generează dungeonul - folosește procedural pentru depth > 1
            DungeonMap map;
            if (depth == 1) {
                // Folosește layout-ul fix pentru tutorial/primul depth
                SimpleMapGenerator generator = new SimpleMapGenerator();
                map = generator.generate(depth);
            } else {
                // Folosește generare procedurală pentru variety
                ProceduralMapGenerator generator = new ProceduralMapGenerator();
                map = generator.generate(depth);
            }

            if (map == null) {
                throw new RuntimeException("Map generation returned null!");
            }

            // Populează camerele cu conținut
            RoomContentService contentService = new RoomContentService();
            contentService.populateMap(map, hero);

            // Creează sau continuă run-ul
            if (existingRun != null) {
                // Continue existing run with new map
                this.currentRun = existingRun;
                this.currentRun.setMap(map);
                System.out.println("📈 Continuing run to depth " + depth);
            } else {
                // Create new run
                this.currentRun = new DungeonRun(map, hero);
                System.out.println("🆕 Starting new run at depth " + depth);
            }

            // Initialize statistics tracking (local to this floor)
            this.enemiesKilledThisRun = 0;
            this.bossKilledThisRun = false;
            this.totalDepthsCleared = 0;

            // Apply permanent bonuses from meta-progression
            applyPermanentBonuses();

            System.out.println("✅ DungeonController initialized successfully!");
        } catch (Exception e) {
            System.err.println("❌ Error initializing DungeonController: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize dungeon", e);
        }
    }

    /**
     * Create initial scene - starts directly in 2D exploration mode
     */
    public Scene createScene() {
        // Start directly in 2D exploration mode for the starting room
        Room startingRoom = currentRun.getMap().getCurrentRoom();
        return createExplorationScene(startingRoom);
    }

    /**
     * Create 2D exploration scene for a room
     */
    private Scene createExplorationScene(Room room) {
        RoomExplorationController explorationController = new RoomExplorationController(
            stage,
            hero,
            room,
            currentRun,
            () -> returnToDungeonMap()
        );
        return explorationController.createScene();
    }

    /**
     * Create the old map-based dungeon view (for ESC key or map overlay)
     */
    public Scene createMapScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a2e;");

        // Top - Header
        root.setTop(createHeader());

        // Center - Map și Log
        root.setCenter(createCenter());

        // Right - Run Items și Stats
        root.setRight(createRightPanel());

        // Bottom - Navigation
        root.setBottom(createBottomPanel());

        // Initial logs
        log("🗺️ Dungeon generat! Depth: " + currentRun.getMap().getDepth());
        log("👤 " + hero.getNume() + " intră în dungeon...");
        log("📍 Starting position: " + currentRun.getMap().getCurrentRoom().getType().getName());

        // Update initial display
        updateDisplay();

        return new Scene(root, 1200, 800);
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #16213e;");

        Label title = new Label("🗺️ THE DUNGEON - DEPTH " + currentRun.getMap().getDepth());
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #e94560;");

        Label subtitle = new Label("Navigate through rooms, defeat enemies, claim victory!");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #f1c40f;");

        header.getChildren().addAll(title, subtitle);
        return header;
    }

    private HBox createCenter() {
        HBox center = new HBox(15);
        center.setPadding(new Insets(20));

        // Left - Map Display
        VBox mapPanel = new VBox(10);
        mapPanel.setPrefWidth(400);

        Label mapLabel = new Label("🗺️ DUNGEON MAP");
        mapLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        mapDisplay = new TextArea();
        mapDisplay.setEditable(false);
        mapDisplay.setStyle(
            "-fx-control-inner-background: #0f0f1e; " +
            "-fx-text-fill: white; " +
            "-fx-font-family: 'Courier New'; " +
            "-fx-font-size: 14px;"
        );
        mapDisplay.setPrefHeight(400);
        VBox.setVgrow(mapDisplay, Priority.ALWAYS);

        mapPanel.getChildren().addAll(mapLabel, mapDisplay);

        // Right - Log Display
        VBox logPanel = new VBox(10);
        HBox.setHgrow(logPanel, Priority.ALWAYS);

        Label logLabel = new Label("📜 EVENT LOG");
        logLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        logDisplay = new TextArea();
        logDisplay.setEditable(false);
        logDisplay.setWrapText(true);
        logDisplay.setStyle(
            "-fx-control-inner-background: #0f0f1e; " +
            "-fx-text-fill: #00ff00; " +
            "-fx-font-family: 'Consolas'; " +
            "-fx-font-size: 12px;"
        );
        logDisplay.setPrefHeight(400);
        VBox.setVgrow(logDisplay, Priority.ALWAYS);

        logPanel.getChildren().addAll(logLabel, logDisplay);

        center.getChildren().addAll(mapPanel, logPanel);
        return center;
    }

    private VBox createRightPanel() {
        VBox rightPanel = new VBox(15);
        rightPanel.setPadding(new Insets(20));
        rightPanel.setPrefWidth(300);
        rightPanel.setStyle("-fx-background-color: #16213e;");

        // Stats
        Label statsTitle = new Label("📊 HERO STATS");
        statsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        statsLabel = new Label();
        statsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        updateStats();

        // Run Items
        Label itemsTitle = new Label("✨ ACTIVE RUN ITEMS");
        itemsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #f1c40f;");

        runItemsDisplay = new VBox(5);
        runItemsDisplay.setStyle(
            "-fx-background-color: #0f0f1e; " +
            "-fx-padding: 10; " +
            "-fx-background-radius: 5;"
        );

        ScrollPane itemsScroll = new ScrollPane(runItemsDisplay);
        itemsScroll.setFitToWidth(true);
        itemsScroll.setStyle("-fx-background: #0f0f1e; -fx-background-color: transparent;");
        VBox.setVgrow(itemsScroll, Priority.ALWAYS);

        rightPanel.getChildren().addAll(statsTitle, statsLabel, new Separator(), itemsTitle, itemsScroll);
        return rightPanel;
    }

    private VBox createBottomPanel() {
        VBox bottomPanel = new VBox(10);
        bottomPanel.setPadding(new Insets(20));
        bottomPanel.setStyle("-fx-background-color: #16213e;");

        Label navLabel = new Label("🧭 NAVIGATION");
        navLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

        navigationButtons = new VBox(10);
        navigationButtons.setAlignment(Pos.CENTER);

        Button exitButton = new Button("🚪 Exit Dungeon");
        styleButton(exitButton, "#e74c3c");
        exitButton.setOnAction(e -> exitDungeon());

        bottomPanel.getChildren().addAll(navLabel, navigationButtons, exitButton);
        return bottomPanel;
    }

    private void updateDisplay() {
        // Update map
        mapDisplay.setText(currentRun.getMap().getAsciiMap());

        // Update run items display
        updateRunItemsDisplay();

        // Update stats
        updateStats();

        // Update navigation buttons
        updateNavigationButtons();
    }

    private void updateRunItemsDisplay() {
        runItemsDisplay.getChildren().clear();

        List<RunItem> items = currentRun.getActiveRunItems();
        if (items.isEmpty()) {
            Label noItems = new Label("(No run items yet)");
            noItems.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
            runItemsDisplay.getChildren().add(noItems);
        } else {
            for (RunItem item : items) {
                Label itemLabel = new Label(item.toString());
                itemLabel.setWrapText(true);
                itemLabel.setStyle(
                    "-fx-text-fill: " + (item.isCurse() ? "#e74c3c" : "#00ff00") + "; " +
                    "-fx-font-size: 11px; " +
                    "-fx-padding: 5;"
                );
                runItemsDisplay.getChildren().add(itemLabel);
            }
        }
    }

    private void updateStats() {
        StringBuilder stats = new StringBuilder();
        stats.append(String.format("Name: %s\n", hero.getNume()));
        stats.append(String.format("HP: %d/%d\n", hero.getViata(), hero.getViataMaxima()));
        stats.append(String.format("Level: %d\n", hero.getNivel()));
        stats.append(String.format("Gold: %d\n", hero.getGold()));
        stats.append(String.format("\nRooms Cleared: %d\n", currentRun.getRoomsCleared()));
        stats.append(String.format("Enemies Killed: %d\n", currentRun.getEnemiesKilled()));
        stats.append(String.format("Highest Depth: %d\n", currentRun.getHighestDepthReached()));

        // Show estimated token earnings
        int estimatedTokens = calculateEstimatedTokens();
        stats.append(String.format("\n🎫 Tokens Earned: ~%d\n", estimatedTokens));

        statsLabel.setText(stats.toString());
    }

    /**
     * Calculate estimated tokens that will be earned
     */
    private int calculateEstimatedTokens() {
        int depth = currentRun.getHighestDepthReached();
        int tokens = depth * 10; // Base depth bonus
        tokens += currentRun.getEnemiesKilled() * 2; // Enemy kills
        // Victory bonus not included (uncertain)
        return tokens;
    }

    private void updateNavigationButtons() {
        navigationButtons.getChildren().clear();

        Room currentRoom = currentRun.getMap().getCurrentRoom();

        // Show current room info
        Label currentRoomLabel = new Label(
            "Current Room: " + currentRoom.getType().getIcon() + " " + currentRoom.getType().getName()
        );
        currentRoomLabel.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 14px; -fx-font-weight: bold;");
        navigationButtons.getChildren().add(currentRoomLabel);

        // If room not cleared, show action button
        if (!currentRoom.isCleared() && currentRoom.getType() != RoomType.START) {
            Button actionButton = new Button(getActionButtonText(currentRoom));
            styleButton(actionButton, "#27ae60");
            actionButton.setOnAction(e -> handleRoomAction(currentRoom));
            navigationButtons.getChildren().add(actionButton);
        }

        // Special case: Boss room after clearing - show descent portal
        if (currentRoom.isCleared() && currentRoom.getType() == RoomType.BOSS) {
            navigationButtons.getChildren().add(new Separator());

            Button descendButton = new Button("🚪 Descend to Next Depth");
            styleButton(descendButton, "#9b59b6");
            descendButton.setOnAction(e -> continueToNextDepth());
            navigationButtons.getChildren().add(descendButton);

            Button exitButton = new Button("🏠 Return to Town");
            styleButton(exitButton, "#e74c3c");
            exitButton.setOnAction(e -> exitDungeon());
            navigationButtons.getChildren().add(exitButton);

            return; // Don't show normal movement buttons
        }

        // Show movement buttons
        navigationButtons.getChildren().add(new Separator());
        Label moveLabel = new Label("Move to:");
        moveLabel.setStyle("-fx-text-fill: white;");
        navigationButtons.getChildren().add(moveLabel);

        for (Direction dir : currentRoom.getAvailableDirections()) {
            Room neighbor = currentRoom.getRoomInDirection(dir);
            if (neighbor != null) {
                Button moveButton = new Button(
                    dir.getArrow() + " " + dir.getName() + " - " + neighbor.getDisplayIcon()
                );
                styleButton(moveButton, "#3498db");
                moveButton.setOnAction(e -> moveToRoom(neighbor, dir));
                navigationButtons.getChildren().add(moveButton);
            }
        }
    }

    private String getActionButtonText(Room room) {
        return switch (room.getType()) {
            case COMBAT -> "⚔️ Fight Enemy!";
            case BOSS -> "👹 Fight BOSS!";
            case EVENT -> "📜 Investigate Event";
            case TREASURE -> "💎 Open Treasure";
            case SHOP -> "🛒 Browse Shop";
            case REST -> "🔥 Rest Here";
            case SHRINE -> "⛩️ Pray at Shrine";
            default -> "Interact";
        };
    }

    private void handleRoomAction(Room room) {
        switch (room.getType()) {
            case COMBAT, BOSS -> handleCombat(room);
            case EVENT -> handleEvent(room);
            case TREASURE -> handleTreasure(room);
            case SHOP -> handleShop(room);
            case REST -> handleRest(room);
            case SHRINE -> handleShrine(room);
        }
    }

    private void handleCombat(Room room) {
        log("⚔️ A " + (room.getType() == RoomType.BOSS ? "BOSS" : "monster") + " appears!");
        log("💀 " + room.getEnemy().getNume() + " (Level " + room.getEnemy().getNivel() + ")");
        log("🎮 Starting turn-based combat...");

        // Apply run item modifiers to hero before battle
        applyRunItemModifiers();

        // Launch turn-based battle
        boolean isBoss = room.getType() == RoomType.BOSS;
        BattleControllerFX battleController = new BattleControllerFX(
            stage,
            hero,
            room.getEnemy(),
            false,  // Not in dungeon mode (we handle our own flow)
            currentRun.getMap().getDepth()
        );

        // Store current room and run for after battle
        battleController.setOnBattleEnd((victory, rewards) -> {
            if (victory) {
                handleBattleVictory(room, rewards);
            } else {
                handleBattleDefeat(room);
            }
        });

        stage.setScene(battleController.createScene());
    }

    /**
     * Aplică modificatorii run item-urilor pe erou înainte de luptă
     */
    private void applyRunItemModifiers() {
        // Apply all run item modifiers to hero
        RunItemModifierService.applyRunItemModifiers(hero, currentRun);

        if (!currentRun.getActiveRunItems().isEmpty()) {
            log("✨ Active run items: " + currentRun.getActiveRunItems().size());
            for (RunItem item : currentRun.getActiveRunItems()) {
                log("  • " + item.getName());
            }
        }
    }

    /**
     * Handler pentru victorie în luptă
     */
    private void handleBattleVictory(Room room, Object rewards) {
        room.markCleared();
        currentRun.markRoomCleared();

        // Track enemy kill in dungeon run (persists across floors)
        boolean isBoss = (room.getType() == RoomType.BOSS);
        currentRun.recordEnemyKill(isBoss);

        // Also track locally for this controller instance
        enemiesKilledThisRun++;
        if (isBoss) {
            bossKilledThisRun = true;
        }

        // Return to dungeon map
        stage.setScene(createScene());

        log("✅ Victory! You defeated " + room.getEnemy().getNume());

        if (room.getType() == RoomType.BOSS) {
            log("👑 BOSS DEFEATED!");
            log("🚪 A portal to the next depth has appeared!");
            log("💰 Collecting depth rewards...");

            // Give immediate rewards for clearing this depth
            giveDepthRewards();
        } else {
            log("🗺️ Returning to dungeon map...");
        }

        updateDisplay();
    }

    /**
     * Handler pentru înfrângere în luptă
     */
    private void handleBattleDefeat(Room room) {
        log("💀 Defeat! The enemy overwhelms you...");
        log("🚪 Exiting dungeon...");

        // Record run completion (defeat still earns some tokens)
        com.rpg.dungeon.model.DungeonProgression progression = hero.getDungeonProgression();
        int depthReached = currentRun.getMap().getDepth();
        int itemsCollected = currentRun.getActiveRunItems().size();

        int tokensBefore = progression.getDungeonTokens();
        java.util.List<String> newUnlocks = progression.recordRunCompletion(
            currentRun.getHighestDepthReached(),
            false,
            currentRun.getEnemiesKilled(),
            currentRun.getBossesKilled() > 0,
            itemsCollected
        );
        int tokensEarned = progression.getDungeonTokens() - tokensBefore;

        // Show defeat summary
        String summary = "💀 DEFEATED\n\n";
        summary += "You were defeated, but your progress is saved!\n\n";
        summary += "📊 PROGRESS:\n";
        summary += String.format("  • Depth Reached: %d\n", currentRun.getHighestDepthReached());
        summary += String.format("  • Enemies Defeated: %d\n", currentRun.getEnemiesKilled());
        summary += String.format("  🎫 Tokens Earned: +%d\n", tokensEarned);

        if (!newUnlocks.isEmpty()) {
            summary += "\n✨ NEW UNLOCKS:\n";
            for (String unlock : newUnlocks) {
                summary += "  🎉 " + unlock + "\n";
            }
        }

        DialogHelper.showError("💀 Defeat", summary);

        // Return to town (defeat)
        currentRun.endRun(false);
        exitDungeon();
    }

    private void handleEvent(Room room) {
        DungeonEvent event = room.getEvent();
        log("📜 Event: " + event.getTitle());
        log(event.getDescription());

        // Show choices dialog
        showEventChoices(event, room);
    }

    private void showEventChoices(DungeonEvent event, Room room) {
        Dialog<DungeonEvent.EventChoice> dialog = new Dialog<>();
        dialog.setTitle("Event: " + event.getTitle());
        dialog.setHeaderText(event.getDescription());

        // Create buttons for each choice
        ButtonType[] buttonTypes = new ButtonType[event.getChoices().size()];
        for (int i = 0; i < event.getChoices().size(); i++) {
            DungeonEvent.EventChoice choice = event.getChoices().get(i);
            buttonTypes[i] = new ButtonType(choice.getIcon() + " " + choice.getText(), ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().add(buttonTypes[i]);
        }

        dialog.setResultConverter(buttonType -> {
            for (int i = 0; i < buttonTypes.length; i++) {
                if (buttonType == buttonTypes[i]) {
                    return event.getChoices().get(i);
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(choice -> handleEventChoice(choice, room));
    }

    private void handleEventChoice(DungeonEvent.EventChoice choice, Room room) {
        log("You chose: " + choice.getText());

        // Determine outcome
        boolean success = RandomUtils.chancePercent(choice.getSuccessChance());
        DungeonEvent.EventOutcome outcome = success ?
            choice.getSuccessOutcome() : choice.getFailureOutcome();

        if (outcome == null) outcome = choice.getSuccessOutcome();

        log(outcome.getResultText());

        // Apply outcomes
        hero.adaugaGold(outcome.getGoldChange());
        hero.vindeca(outcome.getHealthChange());

        for (RunItem item : outcome.getItemRewards()) {
            currentRun.addRunItem(item);
            log("✨ Gained run item: " + item.getName());
        }

        room.markCleared();
        currentRun.markRoomCleared();
        updateDisplay();
    }

    private void handleTreasure(Room room) {
        log("💎 You open the treasure chest!");

        for (RunItem item : room.getTreasures()) {
            currentRun.addRunItem(item);
            log("✨ Found: " + item.toString());
        }

        room.markCleared();
        currentRun.markRoomCleared();
        updateDisplay();
    }

    private void handleShop(Room room) {
        log("🛒 You enter a mysterious shop...");
        log("💬 'Welcome, traveler! I have wares if you have gold.'");

        // Generate shop inventory (3-5 run items)
        java.util.List<RunItem> shopInventory = generateShopInventory();

        showShopDialog(shopInventory, room);
    }

    private void showShopDialog(java.util.List<RunItem> inventory, Room room) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("🛒 Dungeon Shop");
        dialog.setHeaderText("Your gold: " + hero.getGold() + " 💰\n\nSelect an item to purchase:");

        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 10;");

        for (RunItem item : inventory) {
            int cost = calculateItemCost(item);
            Button buyButton = new Button(item.getName() + " - " + cost + " gold");
            buyButton.setStyle("-fx-font-size: 14px;");

            // Add item description as tooltip
            Tooltip tooltip = new Tooltip(item.getDescription());
            buyButton.setTooltip(tooltip);

            buyButton.setOnAction(e -> {
                if (hero.getGold() >= cost) {
                    hero.adaugaGold(-cost);
                    currentRun.addRunItem(item);
                    log("✅ Purchased: " + item.getName() + " for " + cost + " gold");
                    inventory.remove(item);
                    dialog.close();
                    showShopDialog(inventory, room); // Refresh shop
                } else {
                    log("❌ Not enough gold!");
                }
                updateDisplay();
            });

            if (hero.getGold() < cost) {
                buyButton.setDisable(true);
            }

            content.getChildren().add(buyButton);
        }

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();

        room.markCleared();
        currentRun.markRoomCleared();
        updateDisplay();
    }

    private java.util.List<RunItem> generateShopInventory() {
        java.util.List<RunItem> inventory = new java.util.ArrayList<>();
        RoomContentService contentService = new RoomContentService();

        int itemCount = 3 + (int)(Math.random() * 3); // 3-5 items

        for (int i = 0; i < itemCount; i++) {
            RunItem item = contentService.createRandomRunItem(currentRun.getMap().getDepth());
            inventory.add(item);
        }

        return inventory;
    }

    private int calculateItemCost(RunItem item) {
        // Base cost depends on rarity
        int baseCost = switch (item.getRarity()) {
            case COMMON -> 50;
            case UNCOMMON -> 100;
            case RARE -> 200;
            case LEGENDARY -> 400;
            case CURSED -> 25; // Cursed items are cheaper
        };

        // Scale with depth
        int depthMultiplier = currentRun.getMap().getDepth();
        return baseCost + (depthMultiplier * 20);
    }

    private void handleRest(Room room) {
        log("🔥 You find a safe place to rest...");

        int maxHp = hero.getViataMaxima();
        int currentHp = hero.getViata();
        int healAmount = maxHp / 3; // Heal 33% of max HP

        hero.vindeca(healAmount);

        log("💚 You rest and recover " + healAmount + " HP");
        log("💚 HP: " + currentHp + " → " + hero.getViata() + " / " + maxHp);

        room.markCleared();
        currentRun.markRoomCleared();
        updateDisplay();
    }

    private void handleShrine(Room room) {
        log("⛩️ You discover an ancient shrine...");
        log("✨ A mystical aura emanates from the altar");

        showShrineChoices(room);
    }

    private void showShrineChoices(Room room) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("⛩️ Ancient Shrine");
        dialog.setHeaderText("The shrine offers you a choice:\n\nYour HP: " + hero.getViata() + "/" + hero.getViataMaxima());

        ButtonType sacrificeButton = new ButtonType("🩸 Sacrifice HP for Power", ButtonBar.ButtonData.OK_DONE);
        ButtonType healButton = new ButtonType("💚 Offer Gold for Healing", ButtonBar.ButtonData.OK_DONE);
        ButtonType leaveButton = new ButtonType("🚪 Leave", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(sacrificeButton, healButton, leaveButton);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == sacrificeButton) return "sacrifice";
            if (buttonType == healButton) return "heal";
            return "leave";
        });

        dialog.showAndWait().ifPresent(choice -> handleShrineChoice(choice, room));
    }

    private void handleShrineChoice(String choice, Room room) {
        switch (choice) {
            case "sacrifice" -> {
                // Sacrifice 25% HP for a powerful run item
                int hpCost = hero.getViataMaxima() / 4;
                hero.primesteDamage(hpCost);

                log("🩸 You sacrifice " + hpCost + " HP to the shrine...");
                log("✨ The shrine grants you power!");

                // Give a rare or legendary item
                RoomContentService contentService = new RoomContentService();
                boolean isLegendary = Math.random() < 0.3; // 30% chance for legendary
                RunItem item = isLegendary ?
                    contentService.createRandomRunItemOfRarity(RunItem.RunItemRarity.LEGENDARY, currentRun.getMap().getDepth()) :
                    contentService.createRandomRunItemOfRarity(RunItem.RunItemRarity.RARE, currentRun.getMap().getDepth());

                currentRun.addRunItem(item);
                log("🎁 Received: " + item.toString());
            }
            case "heal" -> {
                // Pay gold for significant healing
                int goldCost = 100 + (currentRun.getMap().getDepth() * 25);

                if (hero.getGold() >= goldCost) {
                    hero.adaugaGold(-goldCost);
                    int healAmount = hero.getViataMaxima() / 2; // Heal 50% max HP
                    hero.vindeca(healAmount);

                    log("💰 You offer " + goldCost + " gold to the shrine");
                    log("💚 The shrine heals you for " + healAmount + " HP");
                } else {
                    log("❌ You don't have enough gold (" + goldCost + " needed)");
                    return; // Don't mark room as cleared
                }
            }
            case "leave" -> {
                log("🚪 You leave the shrine undisturbed");
                return; // Don't mark room as cleared
            }
        }

        room.markCleared();
        currentRun.markRoomCleared();
        updateDisplay();
    }

    private void moveToRoom(Room target, Direction direction) {
        log("🚶 Moving " + direction.getName() + "...");
        currentRun.getMap().moveToRoom(target);
        log("📍 Entered: " + target.getType().getName() + " room");

        // Launch 2D exploration view for the room
        enter2DExplorationMode(target);
    }

    /**
     * Enter 2D top-down exploration mode for a room
     */
    private void enter2DExplorationMode(Room room) {
        RoomExplorationController explorationController = new RoomExplorationController(
            stage,
            hero,
            room,
            currentRun,
            () -> returnToDungeonMap()
        );

        stage.setScene(explorationController.createScene());
    }

    /**
     * Return to dungeon map view from 2D exploration
     */
    private void returnToDungeonMap() {
        // Return to town menu (player either died or pressed ESC to exit)
        System.out.println("🏠 Exiting dungeon - returning to town");
        TownMenuController townController = new TownMenuController(stage, hero);
        stage.setScene(townController.createScene());
    }

    /**
     * Give rewards for clearing the current depth
     */
    private void giveDepthRewards() {
        // Calculate and apply rewards for this depth
        int goldReward = calculateGoldReward();
        int expReward = calculateExpReward();

        hero.adaugaGold(goldReward);
        hero.adaugaXp(expReward);

        // Record depth completion in progression system
        com.rpg.dungeon.model.DungeonProgression progression = hero.getDungeonProgression();
        int depthReached = currentRun.getHighestDepthReached();
        int itemsCollected = currentRun.getActiveRunItems().size();

        int tokensBefore = progression.getDungeonTokens();
        java.util.List<String> newUnlocks = progression.recordRunCompletion(
            depthReached,
            true,
            currentRun.getEnemiesKilled(),
            currentRun.getBossesKilled() > 0,
            itemsCollected
        );
        int tokensEarned = progression.getDungeonTokens() - tokensBefore;

        // Log rewards
        log(String.format("💰 Gold: +%d", goldReward));
        log(String.format("⭐ Experience: +%d", expReward));
        log(String.format("🎫 Tokens: +%d", tokensEarned));

        // Log unlocks if any
        if (!newUnlocks.isEmpty()) {
            log("");
            log("✨ NEW UNLOCKS:");
            for (String unlock : newUnlocks) {
                log("  🎉 " + unlock);
            }
        }

        log("");
        log("Choose: Descend deeper or Return to town");
    }

    /**
     * Calculează recompensa de gold bazat pe depth și camere
     */
    private int calculateGoldReward() {
        int baseGold = 50;
        int depthBonus = currentRun.getMap().getDepth() * 25;
        int roomBonus = currentRun.getRoomsCleared() * 10;

        return baseGold + depthBonus + roomBonus;
    }

    /**
     * Calculează recompensa de experiență bazat pe depth
     */
    private int calculateExpReward() {
        int baseExp = 100;
        int depthBonus = currentRun.getMap().getDepth() * 50;

        return baseExp + depthBonus;
    }

    private void exitDungeon() {
        log("🚪 Exiting dungeon...");

        // Record completion stats if player is leaving voluntarily
        com.rpg.dungeon.model.DungeonProgression progression = hero.getDungeonProgression();
        int depthReached = currentRun.getHighestDepthReached();
        int itemsCollected = currentRun.getActiveRunItems().size();

        int tokensBefore = progression.getDungeonTokens();
        progression.recordRunCompletion(
            depthReached,
            true,  // Victory - player completed floors and chose to leave
            currentRun.getEnemiesKilled(),
            currentRun.getBossesKilled() > 0,
            itemsCollected
        );
        int bonusTokens = progression.getDungeonTokens() - tokensBefore;

        log("✅ Run completed!");
        log("📊 Stats recorded:");
        log(String.format("  • Deepest Depth: %d", depthReached));
        log(String.format("  • Enemies Defeated: %d", currentRun.getEnemiesKilled()));
        log(String.format("  • Bosses Defeated: %d", currentRun.getBossesKilled()));
        log(String.format("  • Items Collected: %d", itemsCollected));
        log(String.format("🎫 Bonus Tokens Earned: +%d", bonusTokens));
        log(String.format("🎫 Total Tokens: %d", progression.getDungeonTokens()));

        log("Run items will be removed (they were temporary!)");

        // Clear all run item modifiers from hero
        RunItemModifierService.clearRunItemModifiers(hero);
        log("✨ Run item modifiers cleared");

        // Return to town using callback
        if (onExitCallback != null) {
            onExitCallback.run();
        } else {
            stage.close();
        }
    }

    /**
     * Aplică bonusurile permanente din meta-progression pe erou
     */
    private void applyPermanentBonuses() {
        com.rpg.dungeon.model.DungeonProgression progression = hero.getDungeonProgression();

        // HP bonus is now automatically applied in calculateDerivedStats()
        // Just log it if it exists
        double hpBonusPercent = progression.getMaxHpBonusPercent();
        if (hpBonusPercent > 0) {
            log("🩸 Permanent bonus: +" + (int)(hpBonusPercent * 100) + "% Max HP");
        }

        // Apply starting gold bonus
        int goldBonus = progression.getStartingGoldBonus();
        if (goldBonus > 0) {
            hero.adaugaGold(goldBonus);
            log("💰 Permanent bonus: +" + goldBonus + " starting gold");
        }
    }

    /**
     * Înregistrează completarea run-ului și returnează tokens câștigați
     */
    private int recordRunCompletion(boolean victory) {
        com.rpg.dungeon.model.DungeonProgression progression = hero.getDungeonProgression();

        int depthReached = currentRun.getHighestDepthReached();
        int itemsCollected = currentRun.getActiveRunItems().size();

        // Record in progression system
        int tokensBefore = progression.getDungeonTokens();
        java.util.List<String> newUnlocks = progression.recordRunCompletion(
            depthReached,
            victory,
            currentRun.getEnemiesKilled(),
            currentRun.getBossesKilled() > 0,
            itemsCollected
        );
        int tokensEarned = progression.getDungeonTokens() - tokensBefore;

        log("📊 Run stats recorded:");
        log("  • Depth: " + depthReached);
        log("  • Enemies killed: " + currentRun.getEnemiesKilled());
        log("  • Items collected: " + itemsCollected);

        // Show unlocks
        if (!newUnlocks.isEmpty()) {
            log("");
            log("✨ NEW UNLOCKS:");
            for (String unlock : newUnlocks) {
                log("  🎉 " + unlock);
            }
        }

        return tokensEarned;
    }

    /**
     * Continue to the next depth after clearing boss
     */
    private void continueToNextDepth() {
        log("🚀 Descending to next depth...");

        int nextDepth = currentRun.getMap().getDepth() + 1;
        totalDepthsCleared++; // Track total depths cleared this run

        // Reset room-specific stats but keep run items and overall stats
        bossKilledThisRun = false;

        // Generate new map for next depth
        DungeonMap newMap;
        if (nextDepth == 1) {
            SimpleMapGenerator generator = new SimpleMapGenerator();
            newMap = generator.generate(nextDepth);
        } else {
            ProceduralMapGenerator generator = new ProceduralMapGenerator();
            newMap = generator.generate(nextDepth);
        }

        // Populate the new map
        RoomContentService contentService = new RoomContentService();
        contentService.populateMap(newMap, hero);

        // Update current run with new map (keep run items!)
        currentRun.setMap(newMap);

        log("📍 Entered Depth " + nextDepth + "!");
        log("💪 Your run items carry over!");
        log("🗺️ New dungeon layout generated...");

        // Refresh the UI with new map
        stage.setScene(createScene());
    }

    private void log(String message) {
        if (logDisplay != null) {
            logDisplay.appendText("> " + message + "\n");
        } else {
            // Log to console if UI not ready yet
            System.out.println("[LOG] " + message);
        }
    }

    private void styleButton(Button btn, String color) {
        btn.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-background-color: " + color + "; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 8px 16px; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand;"
        );
        btn.setMaxWidth(Double.MAX_VALUE);
    }
}
