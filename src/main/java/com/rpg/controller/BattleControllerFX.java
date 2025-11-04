package com.rpg.controller;

import com.rpg.dungeon.model.MultiBattleState;
import com.rpg.model.characters.Erou;
import com.rpg.model.characters.Inamic;
import com.rpg.model.effects.DebuffStack;
import com.rpg.service.BattleServiceFX;
import com.rpg.service.EnemyGeneratorRomanesc;
import com.rpg.service.dto.AbilityDTO;
import com.rpg.service.dto.BattleInitDTO;
import com.rpg.utils.DialogHelper;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * BattleControllerFX - Controller îmbunătățit pentru lupte
 * Suportă atât single-enemy cât și multi-enemy battles
 */
public class BattleControllerFX {

    private Stage stage;
    private Erou hero;
    private Inamic enemy; // For single-enemy battles
    //private Inamic currentEnemy;
    private BattleServiceFX battleService;

    // Multi-enemy battle support
    private MultiBattleState multiBattleState;
    private boolean isMultiBattle = false;
    private Inamic currentTarget; // Current target in multi-enemy battle
    private int selectedSlotIndex = 0; // Currently selected enemy slot (0-3)

    // 🆕 ADAUGĂ ACESTEA
    private final boolean inDungeon;
    private int dungeonDepth = 1;

    // 🆕 CALLBACK pentru dungeon system
    private BattleEndCallback onBattleEndCallback;

    // UI Components - Hero
    private Label heroNameLabel;
    private Label heroHPLabel;
    private ProgressBar heroHPBar;
    private Label heroResourceLabel;
    private ProgressBar heroResourceBar;

    // UI Components - Enemy
    private Label enemyNameLabel;
    private Label enemyHPLabel;
    private ProgressBar enemyHPBar;

    // Visual Feedback Components
    private BorderPane battleRoot; // Root for screen shake
    private com.rpg.ui.AnimatedHealthBar heroAnimatedHealthBar;
    private com.rpg.ui.AnimatedHealthBar enemyAnimatedHealthBar;
    private com.rpg.ui.StatusEffectDisplay heroBuffDisplay;
    private com.rpg.ui.StatusEffectDisplay heroDebuffDisplay;
    private com.rpg.ui.StatusEffectDisplay enemyBuffDisplay;
    private com.rpg.ui.StatusEffectDisplay enemyDebuffDisplay;
    private javafx.scene.layout.Pane battleCanvas; // For floating text

    // UI Components - Multi-Enemy
    private VBox enemyPanelContainer; // Container for enemy panel (to refresh) - OLD LAYOUT
    private VBox battleUILayer; // Main UI layer for cinematic layout
    private HBox cinematicEnemySection; // Current enemy section in cinematic layout
    private com.rpg.ui.AnimatedHealthBar[] multiEnemyHealthBars; // Health bars for each enemy slot (0-3)

    // Battle Log
    private TextArea battleLog;

    // Action Buttons
    private Button attackButton;
    private Button fleeButton;
    private VBox abilityButtonsPanel;
    private VBox potionButtonsPanel;

    // 🆕 CONSTRUCTOR NOU cu support dungeon (single enemy)
    public BattleControllerFX(Stage stage, Erou hero, Inamic enemy, boolean inDungeon, int depth) {
        this.stage = stage;
        this.hero = hero;
        this.enemy = enemy;
        this.inDungeon = inDungeon;
        this.dungeonDepth = depth;
        this.battleService = new BattleServiceFX();
        this.isMultiBattle = false;
    }

    // 🆕 CONSTRUCTOR for multi-enemy battles
    public BattleControllerFX(Stage stage, Erou hero, MultiBattleState battleState, boolean inDungeon, int depth) {
        this.stage = stage;
        this.hero = hero;
        this.multiBattleState = battleState;
        this.inDungeon = inDungeon;
        this.dungeonDepth = depth;
        this.battleService = new BattleServiceFX();
        this.isMultiBattle = true;

        // Set current target to first enemy
        if (!battleState.getActiveEnemies().isEmpty()) {
            this.currentTarget = battleState.getActiveEnemies().get(0);
            this.enemy = this.currentTarget; // For UI compatibility
        }
    }

    public Scene createScene() {
        battleRoot = new BorderPane();

        // Create main battle view (Pokemon/Fear & Hunger style)
        javafx.scene.layout.StackPane mainBattleView = createCinematicBattleView();

        battleRoot.setCenter(mainBattleView);
        battleRoot.setStyle("-fx-background-color: #0a0a0f;"); // Darker background

        // Inițializează bătălia
        initializeBattle();

        return new Scene(battleRoot, 1900, 1080); // Full HD cinematic window
    }

    /**
     * Create cinematic battle view (Pokemon/Fear & Hunger style)
     */
    private javafx.scene.layout.StackPane createCinematicBattleView() {
        javafx.scene.layout.StackPane mainStack = new javafx.scene.layout.StackPane();

        // Layer 1: Battle background
        javafx.scene.layout.Pane backgroundLayer = createBattleBackground();

        // Layer 2: Main battle UI
        battleUILayer = new VBox(); // Store reference for refreshing
        battleUILayer.setAlignment(Pos.CENTER);
        battleUILayer.setSpacing(20);
        battleUILayer.setStyle("-fx-background-color: transparent;");

        // Top section: Enemy
        cinematicEnemySection = createCinematicEnemySection(); // Store reference for refreshing
        cinematicEnemySection.setAlignment(Pos.TOP_CENTER);

        // Middle section: Battle area (spacer)
        javafx.scene.layout.Region battleSpacer = new javafx.scene.layout.Region();
        VBox.setVgrow(battleSpacer, Priority.ALWAYS);

        // Bottom section: Hero + Actions
        javafx.scene.layout.HBox bottomSection = new javafx.scene.layout.HBox(20);
        bottomSection.setAlignment(Pos.BOTTOM_CENTER);
        bottomSection.setPadding(new Insets(20));

        VBox heroSection = createCinematicHeroSection();
        VBox actionsSection = createCinematicActionsSection();
        VBox logSection = createCompactLogSection();

        bottomSection.getChildren().addAll(heroSection, actionsSection, logSection);

        battleUILayer.getChildren().addAll(cinematicEnemySection, battleSpacer, bottomSection);

        // Layer 3: Floating text overlay
        battleCanvas = new javafx.scene.layout.Pane();
        battleCanvas.setMouseTransparent(true);

        mainStack.getChildren().addAll(backgroundLayer, battleUILayer, battleCanvas);

        return mainStack;
    }

    /**
     * Create battle background with atmosphere
     */
    private javafx.scene.layout.Pane createBattleBackground() {
        javafx.scene.layout.Pane background = new javafx.scene.layout.Pane();
        background.setStyle(
            "-fx-background-color: linear-gradient(to bottom, " +
            "#1a1a2e 0%, " +
            "#16213e 50%, " +
            "#0f0f1e 100%);"
        );

        // Add some atmospheric elements (adjusted for 1900x1080 window)
        for (int i = 0; i < 40; i++) {
            javafx.scene.shape.Circle particle = new javafx.scene.shape.Circle(
                Math.random() * 1900,
                Math.random() * 1080,
                1 + Math.random() * 2
            );
            particle.setFill(javafx.scene.paint.Color.rgb(255, 255, 255, 0.1 + Math.random() * 0.2));
            background.getChildren().add(particle);
        }

        return background;
    }

    /**
     * Create cinematic enemy section (top of screen)
     */
    private javafx.scene.layout.HBox createCinematicEnemySection() {
        if (isMultiBattle && multiBattleState != null) {
            return createMultiEnemyCinematicSection();
        }

        javafx.scene.layout.HBox enemyContainer = new javafx.scene.layout.HBox(30);
        enemyContainer.setAlignment(Pos.CENTER);
        enemyContainer.setPadding(new Insets(40, 40, 20, 40));
        enemyContainer.setStyle(
            "-fx-background-color: rgba(20, 20, 30, 0.8); " +
            "-fx-background-radius: 15; " +
            "-fx-border-color: #e74c3c; " +
            "-fx-border-width: 3; " +
            "-fx-border-radius: 15; " +
            "-fx-effect: dropshadow(gaussian, rgba(231, 76, 60, 0.5), 20, 0.5, 0, 0);"
        );

        // Enemy portrait placeholder
        VBox enemyPortrait = createEnemyPortraitPlaceholder();

        // Enemy info panel
        VBox enemyInfo = new VBox(10);
        enemyInfo.setAlignment(Pos.CENTER_LEFT);
        enemyInfo.setPrefWidth(400);

        enemyNameLabel = new Label(enemy.getNume());
        enemyNameLabel.setStyle(
            "-fx-font-size: 32px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #e74c3c; " +
            "-fx-effect: dropshadow(gaussian, black, 3, 0.8, 0, 0);"
        );

        if (enemy.isBoss()) {
            Label bossLabel = new Label("💀 BOSS 💀");
            bossLabel.setStyle(
                "-fx-font-size: 20px; " +
                "-fx-text-fill: #ff6b6b; " +
                "-fx-font-weight: bold;"
            );
            enemyInfo.getChildren().add(bossLabel);
        }

        // Large animated health bar
        enemyAnimatedHealthBar = new com.rpg.ui.AnimatedHealthBar(enemy.getViataMaxima());
        enemyAnimatedHealthBar.updateHP(enemy.getViata(), false);
        enemyAnimatedHealthBar.setPrefWidth(400);

        // Status effects
        HBox enemyStatus = new HBox(15);
        enemyStatus.setAlignment(Pos.CENTER_LEFT);

        enemyBuffDisplay = new com.rpg.ui.StatusEffectDisplay();
        enemyDebuffDisplay = new com.rpg.ui.StatusEffectDisplay();

        VBox buffsBox = new VBox(3);
        Label buffsLabel = new Label("Buffs:");
        buffsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #3498db; -fx-font-weight: bold;");
        buffsBox.getChildren().addAll(buffsLabel, enemyBuffDisplay);

        VBox debuffsBox = new VBox(3);
        Label debuffsLabel = new Label("Debuffs:");
        debuffsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9b59b6; -fx-font-weight: bold;");
        debuffsBox.getChildren().addAll(debuffsLabel, enemyDebuffDisplay);

        enemyStatus.getChildren().addAll(buffsBox, debuffsBox);

        enemyInfo.getChildren().addAll(enemyNameLabel, enemyAnimatedHealthBar, enemyStatus);

        // Update displays
        enemyBuffDisplay.updateBuffs(new java.util.HashMap<>());
        enemyDebuffDisplay.updateDebuffs(enemy.getDebuffStacksActive()); // Full debuff info

        enemyContainer.getChildren().addAll(enemyPortrait, enemyInfo);
        return enemyContainer;
    }

    /**
     * Create enemy portrait placeholder
     */
    private VBox createEnemyPortraitPlaceholder() {
        VBox portrait = new VBox();
        portrait.setAlignment(Pos.CENTER);
        portrait.setPrefSize(150, 150);
        portrait.setStyle(
            "-fx-background-color: rgba(231, 76, 60, 0.2); " +
            "-fx-background-radius: 10; " +
            "-fx-border-color: #e74c3c; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 10;"
        );

        Label enemyIcon = new Label("👹");
        enemyIcon.setStyle("-fx-font-size: 80px;");

        Label levelLabel = new Label("Lvl " + (enemy != null ? enemy.getNivel() : "?"));
        levelLabel.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold;"
        );

        portrait.getChildren().addAll(enemyIcon, levelLabel);
        return portrait;
    }

    /**
     * Create multi-enemy cinematic section (4 individual boxes in a horizontal row)
     */
    private javafx.scene.layout.HBox createMultiEnemyCinematicSection() {
        javafx.scene.layout.VBox mainContainer = new javafx.scene.layout.VBox(15);
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setPadding(new Insets(20));

        // Initialize health bar array for 4 enemy slots (only if null - preserve existing bars)
        if (multiEnemyHealthBars == null) {
            multiEnemyHealthBars = new com.rpg.ui.AnimatedHealthBar[4];
        }

        // Title
        Label title = new Label("⚔️ MULTI-BATTLE: " + multiBattleState.getActiveEnemyCount() + " ENEMIES");
        title.setStyle("-fx-font-size: 20px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");

        // Horizontal row for 4 enemy boxes (1x4 layout)
        javafx.scene.layout.HBox enemyRow = new javafx.scene.layout.HBox(15);
        enemyRow.setAlignment(Pos.CENTER);

        // Create 4 enemy boxes in a row
        for (int i = 0; i < 4; i++) {
            VBox enemyBox = createIndividualEnemyBox(i);
            enemyRow.getChildren().add(enemyBox);
        }

        mainContainer.getChildren().addAll(title, enemyRow);

        // Wrap in HBox for centering
        javafx.scene.layout.HBox wrapper = new javafx.scene.layout.HBox(mainContainer);
        wrapper.setAlignment(Pos.TOP_CENTER);

        return wrapper;
    }

    /**
     * Create individual enemy box for multi-battle
     */
    private VBox createIndividualEnemyBox(int slotIndex) {
        VBox enemyBox = new VBox(10);
        enemyBox.setAlignment(Pos.CENTER);
        enemyBox.setPadding(new Insets(15));
        enemyBox.setPrefWidth(320);
        enemyBox.setPrefHeight(200);

        MultiBattleState.BattleSlot slot = multiBattleState.getSlot(slotIndex);

        if (slot.isActive() && slot.getEnemy() != null && slot.getEnemy().esteViu()) {
            // Active enemy
            Inamic enemy = slot.getEnemy();
            boolean isSelected = (slotIndex == selectedSlotIndex);

            // Style based on selection
            String borderColor = isSelected ? "#FFD700" : "#e74c3c";
            String borderWidth = isSelected ? "4" : "2";
            String glowColor = isSelected ? "rgba(255, 215, 0, 0.6)" : "rgba(231, 76, 60, 0.4)";

            enemyBox.setStyle(
                "-fx-background-color: rgba(20, 20, 30, 0.85); " +
                "-fx-background-radius: 12; " +
                "-fx-border-color: " + borderColor + "; " +
                "-fx-border-width: " + borderWidth + "; " +
                "-fx-border-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, " + glowColor + ", 15, 0.6, 0, 0); " +
                "-fx-cursor: hand;"
            );

            // Enemy portrait (smaller for grid)
            VBox portrait = new VBox();
            portrait.setAlignment(Pos.CENTER);
            portrait.setPrefSize(80, 80);
            portrait.setStyle(
                "-fx-background-color: rgba(231, 76, 60, 0.2); " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: #e74c3c; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 8;"
            );

            Label enemyIcon = new Label("👹");
            enemyIcon.setStyle("-fx-font-size: 50px;");
            portrait.getChildren().add(enemyIcon);

            // Enemy info
            HBox nameRow = new HBox(5);
            nameRow.setAlignment(Pos.CENTER);

            if (isSelected) {
                Label targetIcon = new Label("🎯");
                targetIcon.setStyle("-fx-font-size: 14px;");
                nameRow.getChildren().add(targetIcon);
            }

            Label nameLabel = new Label("Slot " + (slotIndex + 1) + ": " + enemy.getNume());
            nameLabel.setStyle(
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: " + (isSelected ? "#FFD700" : "#e74c3c") + ";"
            );
            nameLabel.setWrapText(true);
            nameLabel.setMaxWidth(280);
            nameRow.getChildren().add(nameLabel);

            Label levelLabel = new Label("Lvl " + enemy.getNivel());
            levelLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #95a5a6;");

            // Animated health bar (compact) - reuse existing if available and same enemy
            com.rpg.ui.AnimatedHealthBar healthBar;
            if (multiEnemyHealthBars[slotIndex] != null &&
                multiEnemyHealthBars[slotIndex].getMaxHP() == enemy.getViataMaxima()) {
                // Reuse existing health bar for the same enemy
                healthBar = multiEnemyHealthBars[slotIndex];
                // Don't animate when just refreshing the UI
                // The actual damage animation happens in showCombatVisualFeedback
            } else {
                // Create new health bar (first time or different enemy/reinforcement)
                healthBar = new com.rpg.ui.AnimatedHealthBar(enemy.getViataMaxima());
                healthBar.updateHP(enemy.getViata(), false);
                multiEnemyHealthBars[slotIndex] = healthBar;
            }
            healthBar.setPrefWidth(280);

            // Buff/Debuff displays (compact)
            HBox statusRow = new HBox(10);
            statusRow.setAlignment(Pos.CENTER);

            com.rpg.ui.StatusEffectDisplay buffsDisplay = new com.rpg.ui.StatusEffectDisplay();
            com.rpg.ui.StatusEffectDisplay debuffsDisplay = new com.rpg.ui.StatusEffectDisplay();

            VBox buffsBox = new VBox(2);
            Label buffsLabel = new Label("Buffs:");
            buffsLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #3498db; -fx-font-weight: bold;");
            buffsBox.getChildren().addAll(buffsLabel, buffsDisplay);

            VBox debuffsBox = new VBox(2);
            Label debuffsLabel = new Label("Debuffs:");
            debuffsLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #9b59b6; -fx-font-weight: bold;");
            debuffsBox.getChildren().addAll(debuffsLabel, debuffsDisplay);

            statusRow.getChildren().addAll(buffsBox, debuffsBox);

            // Update displays
            buffsDisplay.updateBuffs(new java.util.HashMap<>());
            debuffsDisplay.updateDebuffs(enemy.getDebuffStacksActive()); // Show enemy debuffs with full info

            enemyBox.getChildren().addAll(portrait, nameRow, levelLabel, healthBar, statusRow);

            // Make clickable for targeting
            enemyBox.setOnMouseClicked(event -> selectEnemyTarget(slotIndex));

            // Hover effect
            enemyBox.setOnMouseEntered(event -> {
                if (slotIndex != selectedSlotIndex) {
                    enemyBox.setStyle(
                        "-fx-background-color: rgba(30, 30, 40, 0.9); " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: #e74c3c; " +
                        "-fx-border-width: 3; " +
                        "-fx-border-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(231, 76, 60, 0.7), 20, 0.8, 0, 0); " +
                        "-fx-cursor: hand;"
                    );
                }
            });

            enemyBox.setOnMouseExited(event -> {
                if (slotIndex != selectedSlotIndex) {
                    enemyBox.setStyle(
                        "-fx-background-color: rgba(20, 20, 30, 0.85); " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: #e74c3c; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(231, 76, 60, 0.4), 15, 0.6, 0, 0); " +
                        "-fx-cursor: hand;"
                    );
                }
            });

        } else {
            // Empty slot or incoming reinforcement
            // Clear health bar reference for this slot
            multiEnemyHealthBars[slotIndex] = null;

            MultiBattleState.ReinforcementEntry nextReinforcement = multiBattleState.getNextReinforcement();

            if (nextReinforcement != null && multiBattleState.getActiveEnemyCount() < MultiBattleState.MAX_ACTIVE_ENEMIES) {
                // Show reinforcement countdown
                enemyBox.setStyle(
                    "-fx-background-color: rgba(20, 20, 30, 0.5); " +
                    "-fx-background-radius: 12; " +
                    "-fx-border-color: #f39c12; " +
                    "-fx-border-width: 2; " +
                    "-fx-border-style: dashed; " +
                    "-fx-border-radius: 12;"
                );

                Label emptyIcon = new Label("⏳");
                emptyIcon.setStyle("-fx-font-size: 50px;");

                Label slotLabel = new Label("Slot " + (slotIndex + 1) + ": EMPTY");
                slotLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #95a5a6;");

                int turnsRemaining = nextReinforcement.getTurnsRemaining(multiBattleState.getCurrentTurn());
                Label countdownLabel = new Label("⏰ " + nextReinforcement.getEnemy().getNume());
                countdownLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #f39c12;");
                countdownLabel.setWrapText(true);
                countdownLabel.setMaxWidth(280);

                Label turnsLabel = new Label("Arriving in " + turnsRemaining + " turns");
                turnsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #f39c12;");

                enemyBox.getChildren().addAll(emptyIcon, slotLabel, countdownLabel, turnsLabel);

            } else {
                // Completely empty
                enemyBox.setStyle(
                    "-fx-background-color: rgba(15, 15, 20, 0.5); " +
                    "-fx-background-radius: 12; " +
                    "-fx-border-color: #34495e; " +
                    "-fx-border-width: 1; " +
                    "-fx-border-style: dashed; " +
                    "-fx-border-radius: 12;"
                );

                Label emptyIcon = new Label("○");
                emptyIcon.setStyle("-fx-font-size: 60px; -fx-text-fill: #34495e;");

                Label emptyLabel = new Label("Slot " + (slotIndex + 1) + ": EMPTY");
                emptyLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #34495e;");

                enemyBox.getChildren().addAll(emptyIcon, emptyLabel);
            }
        }

        return enemyBox;
    }

    /**
     * Create cinematic hero section (bottom-left)
     */
    private VBox createCinematicHeroSection() {
        VBox heroContainer = new VBox(15);
        heroContainer.setAlignment(Pos.BOTTOM_LEFT);
        heroContainer.setPadding(new Insets(20));
        heroContainer.setPrefWidth(400);
        heroContainer.setStyle(
            "-fx-background-color: rgba(20, 30, 20, 0.8); " +
            "-fx-background-radius: 15; " +
            "-fx-border-color: #27ae60; " +
            "-fx-border-width: 3; " +
            "-fx-border-radius: 15; " +
            "-fx-effect: dropshadow(gaussian, rgba(39, 174, 96, 0.5), 20, 0.5, 0, 0);"
        );

        // Hero portrait
        HBox heroTop = new HBox(20);
        heroTop.setAlignment(Pos.CENTER_LEFT);

        VBox heroPortrait = createHeroPortraitPlaceholder();

        VBox heroInfo = new VBox(10);
        heroInfo.setAlignment(Pos.CENTER_LEFT);

        heroNameLabel = new Label(hero.getNume());
        heroNameLabel.setStyle(
            "-fx-font-size: 28px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #27ae60; " +
            "-fx-effect: dropshadow(gaussian, black, 3, 0.8, 0, 0);"
        );

        Label classLabel = new Label(hero.getClass().getSimpleName() + " • Lvl " + hero.getNivel());
        classLabel.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-text-fill: #95a5a6;"
        );

        heroInfo.getChildren().addAll(heroNameLabel, classLabel);
        heroTop.getChildren().addAll(heroPortrait, heroInfo);

        // Health bar
        Label hpLabel = new Label("❤️ HEALTH");
        hpLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-font-weight: bold;");

        heroAnimatedHealthBar = new com.rpg.ui.AnimatedHealthBar(hero.getViataMaxima());
        heroAnimatedHealthBar.updateHP(hero.getViata(), false);
        heroAnimatedHealthBar.setPrefWidth(360);

        // Resource bar
        Label resourceLabel = new Label("💙 " + hero.getTipResursa().toUpperCase());
        resourceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-font-weight: bold;");

        heroResourceBar = new ProgressBar();
        heroResourceBar.setPrefWidth(360);
        heroResourceBar.setPrefHeight(15);
        heroResourceBar.setProgress((double)hero.getResursaCurenta() / hero.getResursaMaxima());
        heroResourceBar.setStyle("-fx-accent: #3498db;");

        heroResourceLabel = new Label(hero.getResursaCurenta() + " / " + hero.getResursaMaxima());
        heroResourceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white;");

        // Status effects
        HBox statusRow = new HBox(15);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        heroBuffDisplay = new com.rpg.ui.StatusEffectDisplay();
        heroDebuffDisplay = new com.rpg.ui.StatusEffectDisplay();

        VBox buffsBox = new VBox(3);
        Label buffsLabel = new Label("Buffs:");
        buffsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #3498db; -fx-font-weight: bold;");
        buffsBox.getChildren().addAll(buffsLabel, heroBuffDisplay);

        VBox debuffsBox = new VBox(3);
        Label debuffsLabel = new Label("Debuffs:");
        debuffsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9b59b6; -fx-font-weight: bold;");
        debuffsBox.getChildren().addAll(debuffsLabel, heroDebuffDisplay);

        statusRow.getChildren().addAll(buffsBox, debuffsBox);

        // Update displays
        heroBuffDisplay.updateBuffs(hero.getBuffuriActive());
        heroDebuffDisplay.updateDebuffs(hero.getDebuffuriActive()); // Full debuff info

        heroContainer.getChildren().addAll(
            heroTop,
            hpLabel, heroAnimatedHealthBar,
            resourceLabel, heroResourceBar, heroResourceLabel,
            statusRow
        );

        return heroContainer;
    }

    /**
     * Create hero portrait placeholder
     */
    private VBox createHeroPortraitPlaceholder() {
        VBox portrait = new VBox();
        portrait.setAlignment(Pos.CENTER);
        portrait.setPrefSize(100, 100);
        portrait.setStyle(
            "-fx-background-color: rgba(39, 174, 96, 0.2); " +
            "-fx-background-radius: 10; " +
            "-fx-border-color: #27ae60; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 10;"
        );

        Label heroIcon = new Label("⚔️");
        heroIcon.setStyle("-fx-font-size: 60px;");

        portrait.getChildren().add(heroIcon);
        return portrait;
    }

    /**
     * Create cinematic actions section (bottom-right)
     */
    private VBox createCinematicActionsSection() {
        VBox actionsContainer = new VBox(10);
        actionsContainer.setAlignment(Pos.BOTTOM_RIGHT);
        actionsContainer.setPadding(new Insets(20));
        actionsContainer.setPrefWidth(450);
        actionsContainer.setStyle(
            "-fx-background-color: rgba(30, 30, 40, 0.9); " +
            "-fx-background-radius: 15; " +
            "-fx-border-color: #9b59b6; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 15;"
        );

        // Main actions
        HBox mainActions = new HBox(10);
        mainActions.setAlignment(Pos.CENTER);

        attackButton = createCinematicActionButton("⚔️ ATTACK", "#27ae60");
        attackButton.setOnAction(e -> handleNormalAttack());

        fleeButton = createCinematicActionButton("🏃 FLEE", "#e67e22");
        fleeButton.setOnAction(e -> handleFlee());

        mainActions.getChildren().addAll(attackButton, fleeButton);

        // Abilities
        Label abilitiesLabel = new Label("✨ ABILITIES");
        abilitiesLabel.setStyle(
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #9b59b6;"
        );

        abilityButtonsPanel = new VBox(5);
        abilityButtonsPanel.setAlignment(Pos.CENTER);

        // Potions
        Label potionsLabel = new Label("🧪 POTIONS");
        potionsLabel.setStyle(
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #2ecc71;"
        );

        potionButtonsPanel = new VBox(5);
        potionButtonsPanel.setAlignment(Pos.CENTER);

        actionsContainer.getChildren().addAll(
            mainActions,
            new javafx.scene.control.Separator(),
            abilitiesLabel, abilityButtonsPanel,
            new javafx.scene.control.Separator(),
            potionsLabel, potionButtonsPanel
        );

        return actionsContainer;
    }

    /**
     * Create compact battle log section
     */
    private VBox createCompactLogSection() {
        VBox logContainer = new VBox(5);
        logContainer.setAlignment(Pos.BOTTOM_RIGHT);
        logContainer.setPadding(new Insets(10));
        logContainer.setPrefWidth(300);
        logContainer.setPrefHeight(400);
        logContainer.setStyle(
            "-fx-background-color: rgba(15, 15, 20, 0.9); " +
            "-fx-background-radius: 10; " +
            "-fx-border-color: #34495e; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 10;"
        );

        Label logLabel = new Label("📜 BATTLE LOG");
        logLabel.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-text-fill: #95a5a6; " +
            "-fx-font-weight: bold;"
        );

        battleLog = new TextArea();
        battleLog.setEditable(false);
        battleLog.setWrapText(true);
        battleLog.setStyle(
            "-fx-control-inner-background: #0f0f1e; " +
            "-fx-text-fill: #ecf0f1; " +
            "-fx-font-family: 'Courier New'; " +
            "-fx-font-size: 11px;"
        );
        battleLog.setPrefWidth(280);
        battleLog.setPrefHeight(350);
        VBox.setVgrow(battleLog, Priority.ALWAYS);

        logContainer.getChildren().addAll(logLabel, battleLog);
        return logContainer;
    }

    /**
     * Create cinematic action button
     */
    private Button createCinematicActionButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefWidth(200);
        btn.setPrefHeight(45);
        btn.setStyle(
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-color: " + color + "; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 10; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.5), 5, 0.5, 0, 2);"
        );

        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-color: derive(" + color + ", 20%); " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 10; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, " + color + ", 10, 0.8, 0, 0); " +
            "-fx-scale-x: 1.05; " +
            "-fx-scale-y: 1.05;"
        ));

        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-color: " + color + "; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 10; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.5), 5, 0.5, 0, 2);"
        ));

        return btn;
    }

    /**
     * Header cu titlul (DEPRECATED - keeping for compatibility)
     */
    private VBox createHeader() {
        VBox header = new VBox(5);
        header.setPadding(new Insets(15));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #1a1a2e;");

        // 🔄 MODIFICĂ title să includă depth
        String titleText;
        if (isMultiBattle && multiBattleState != null) {
            titleText = "⚔️ MULTI-BATTLE: " + hero.getNume() + " VS " + multiBattleState.getActiveEnemyCount() + " ENEMIES";
        } else {
            titleText = "⚔️ LUPTĂ: " + hero.getNume() + " VS " + enemy.getNume();
        }

        if (inDungeon) {
            titleText += " | 🏰 Depth: " + dungeonDepth;
        }
        titleText += " ⚔️";

        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #e94560;");

        if (enemy != null && enemy.isBoss()) {
            Label bossLabel = new Label("💀 BOSS BATTLE 💀");
            bossLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ff6b6b;");
            header.getChildren().addAll(title, bossLabel);
        } else {
            header.getChildren().add(title);
        }

        return header;
    }


    /**
     * Zona de luptă - Hero, Log, Enemy
     */
    private javafx.scene.layout.StackPane createBattleArea() {
        // Base layer with panels
        HBox battleArea = new HBox(20);
        battleArea.setPadding(new Insets(20));
        battleArea.setAlignment(Pos.CENTER);

        VBox heroPanel = createHeroPanel();
        VBox logPanel = createLogPanel();
        enemyPanelContainer = createEnemyPanel(); // Store reference for refreshing

        battleArea.getChildren().addAll(heroPanel, logPanel, enemyPanelContainer);

        // Overlay layer for floating text
        battleCanvas = new javafx.scene.layout.Pane();
        battleCanvas.setMouseTransparent(true); // Allow clicks to pass through

        // Stack them
        javafx.scene.layout.StackPane stack = new javafx.scene.layout.StackPane();
        stack.getChildren().addAll(battleArea, battleCanvas);

        return stack;
    }

    /**
     * Panel erou
     */
    private VBox createHeroPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setAlignment(Pos.CENTER);
        panel.setStyle(
                "-fx-background-color: #16213e; " +
                        "-fx-background-radius: 15; " +
                        "-fx-border-color: #27ae60; " +
                        "-fx-border-width: 3; " +
                        "-fx-border-radius: 15;"
        );
        panel.setPrefWidth(300);

        heroNameLabel = new Label(hero.getNume());
        heroNameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        Label hpTextLabel = new Label("❤️ HP");
        hpTextLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

        heroHPBar = new ProgressBar(1.0);
        heroHPBar.setPrefWidth(250);
        heroHPBar.setPrefHeight(25);
        heroHPBar.setStyle("-fx-accent: #e74c3c;");

        heroHPLabel = new Label(hero.getViata() + " / " + hero.getViataMaxima());
        heroHPLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;");

        Label resourceTextLabel = new Label("💙 " + hero.getTipResursa());
        resourceTextLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

        heroResourceBar = new ProgressBar(1.0);
        heroResourceBar.setPrefWidth(250);
        heroResourceBar.setPrefHeight(20);
        heroResourceBar.setStyle("-fx-accent: #3498db;");

        heroResourceLabel = new Label(hero.getResursaCurenta() + " / " + hero.getResursaMaxima());
        heroResourceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

        // Animated health bar
        heroAnimatedHealthBar = new com.rpg.ui.AnimatedHealthBar(hero.getViataMaxima());
        heroAnimatedHealthBar.updateHP(hero.getViata(), false);

        // Status effect displays
        heroBuffDisplay = new com.rpg.ui.StatusEffectDisplay();
        heroDebuffDisplay = new com.rpg.ui.StatusEffectDisplay();

        HBox statusRow = new HBox(5);
        statusRow.setAlignment(Pos.CENTER);

        VBox buffsSection = new VBox(3);
        Label buffsTitle = new Label("Buffs:");
        buffsTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #3498db; -fx-font-weight: bold;");
        buffsSection.getChildren().addAll(buffsTitle, heroBuffDisplay);

        VBox debuffsSection = new VBox(3);
        Label debuffsTitle = new Label("Debuffs:");
        debuffsTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #9b59b6; -fx-font-weight: bold;");
        debuffsSection.getChildren().addAll(debuffsTitle, heroDebuffDisplay);

        statusRow.getChildren().addAll(buffsSection, debuffsSection);

        panel.getChildren().addAll(
                heroNameLabel,
                hpTextLabel, heroAnimatedHealthBar,
                resourceTextLabel, heroResourceBar, heroResourceLabel,
                statusRow
        );

        // Update status displays with initial buffs and debuffs
        heroBuffDisplay.updateBuffs(hero.getBuffuriActive());
        heroDebuffDisplay.updateDebuffs(hero.getDebuffuriActive()); // Show hero debuffs with full info

        // Show active run item buffs
        VBox buffsPanel = createRunItemBuffsPanel();
        if (buffsPanel != null) {
            panel.getChildren().add(buffsPanel);
        }

        return panel;
    }

    /**
     * Creates a panel showing active run item effects
     */
    private VBox createRunItemBuffsPanel() {
        VBox buffsPanel = new VBox(3);
        buffsPanel.setStyle("-fx-padding: 5; -fx-background-color: rgba(39, 174, 96, 0.1); -fx-background-radius: 5;");

        java.util.List<String> activeEffects = new java.util.ArrayList<>();

        // Damage modifiers
        if (hero.getRunItemDamageMultiplier() > 1.0) {
            int percent = (int)((hero.getRunItemDamageMultiplier() - 1.0) * 100);
            activeEffects.add("⚔️ +" + percent + "% Damage");
        }
        if (hero.getRunItemFlatDamage() > 0) {
            activeEffects.add("⚔️ +" + hero.getRunItemFlatDamage() + " Damage");
        }

        // Defense modifiers
        if (hero.getRunItemDefenseMultiplier() > 1.0) {
            int percent = (int)((hero.getRunItemDefenseMultiplier() - 1.0) * 100);
            activeEffects.add("🛡️ +" + percent + "% Defense");
        }
        if (hero.getRunItemFlatDefense() > 0) {
            activeEffects.add("🛡️ +" + hero.getRunItemFlatDefense() + " Defense");
        }

        // Combat modifiers
        if (hero.getRunItemCritBonus() > 0) {
            int percent = (int)(hero.getRunItemCritBonus() * 100);
            activeEffects.add("⚡ +" + percent + "% Crit");
        }
        if (hero.getRunItemDodgeBonus() > 0) {
            int percent = (int)(hero.getRunItemDodgeBonus() * 100);
            activeEffects.add("💨 +" + percent + "% Dodge");
        }

        // Special effects
        if (hero.getRunItemLifesteal() > 0) {
            int percent = (int)(hero.getRunItemLifesteal() * 100);
            activeEffects.add("🩸 " + percent + "% Lifesteal");
        }
        if (hero.getRunItemRegenPerTurn() > 0) {
            activeEffects.add("💚 +" + hero.getRunItemRegenPerTurn() + " HP/turn");
        }

        // Elemental damage
        java.util.Map<String, Integer> elementalDamage = hero.getRunItemElementalDamageMap();
        if (!elementalDamage.isEmpty()) {
            for (java.util.Map.Entry<String, Integer> entry : elementalDamage.entrySet()) {
                String icon = entry.getKey().equals("fire") ? "🔥" : "❄️";
                activeEffects.add(icon + " +" + entry.getValue() + " " + entry.getKey());
            }
        }

        if (activeEffects.isEmpty()) {
            return null; // No buffs to display
        }

        Label buffsTitle = new Label("✨ Active Run Items:");
        buffsTitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
        buffsPanel.getChildren().add(buffsTitle);

        for (String effect : activeEffects) {
            Label effectLabel = new Label(effect);
            effectLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: white;");
            buffsPanel.getChildren().add(effectLabel);
        }

        return buffsPanel;
    }

    /**
     * Panel inamic - supports both single and multi-enemy battles
     */
    private VBox createEnemyPanel() {
        if (isMultiBattle && multiBattleState != null) {
            return createMultiEnemyPanel();
        } else {
            return createSingleEnemyPanel();
        }
    }

    /**
     * Single enemy panel (original)
     */
    private VBox createSingleEnemyPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setAlignment(Pos.CENTER);
        panel.setStyle(
                "-fx-background-color: #16213e; " +
                        "-fx-background-radius: 15; " +
                        "-fx-border-color: #e74c3c; " +
                        "-fx-border-width: 3; " +
                        "-fx-border-radius: 15;"
        );
        panel.setPrefWidth(300);

        enemyNameLabel = new Label(enemy.getNume());
        enemyNameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");

        if (enemy.isBoss()) {
            Label bossIndicator = new Label("💀 BOSS");
            bossIndicator.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff6b6b; -fx-font-weight: bold;");
            panel.getChildren().add(bossIndicator);
        }

        Label hpTextLabel = new Label("❤️ HP");
        hpTextLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

        enemyHPBar = new ProgressBar(1.0);
        enemyHPBar.setPrefWidth(250);
        enemyHPBar.setPrefHeight(25);
        enemyHPBar.setStyle("-fx-accent: #e74c3c;");

        enemyHPLabel = new Label(enemy.getViata() + " / " + enemy.getViataMaxima());
        enemyHPLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;");

        // Animated health bar
        enemyAnimatedHealthBar = new com.rpg.ui.AnimatedHealthBar(enemy.getViataMaxima());
        enemyAnimatedHealthBar.updateHP(enemy.getViata(), false);

        // Status effect displays
        enemyBuffDisplay = new com.rpg.ui.StatusEffectDisplay();
        enemyDebuffDisplay = new com.rpg.ui.StatusEffectDisplay();

        HBox statusRow = new HBox(5);
        statusRow.setAlignment(Pos.CENTER);

        VBox buffsSection = new VBox(3);
        Label buffsTitle = new Label("Buffs:");
        buffsTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #3498db; -fx-font-weight: bold;");
        buffsSection.getChildren().addAll(buffsTitle, enemyBuffDisplay);

        VBox debuffsSection = new VBox(3);
        Label debuffsTitle = new Label("Debuffs:");
        debuffsTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #9b59b6; -fx-font-weight: bold;");
        debuffsSection.getChildren().addAll(debuffsTitle, enemyDebuffDisplay);

        statusRow.getChildren().addAll(buffsSection, debuffsSection);

        panel.getChildren().addAll(
                enemyNameLabel,
                hpTextLabel, enemyAnimatedHealthBar,
                statusRow
        );

        // Update status displays (enemies don't have buffs, debuffs are shown)
        enemyBuffDisplay.updateBuffs(new java.util.HashMap<>()); // Empty - enemies don't get buffed
        enemyDebuffDisplay.updateDebuffs(enemy.getDebuffStacksActive()); // Show enemy debuffs with full info

        return panel;
    }

    /**
     * Multi-enemy panel showing all 4 slots
     */
    private VBox createMultiEnemyPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setStyle(
                "-fx-background-color: #16213e; " +
                        "-fx-background-radius: 15; " +
                        "-fx-border-color: #e74c3c; " +
                        "-fx-border-width: 3; " +
                        "-fx-border-radius: 15;"
        );
        panel.setPrefWidth(350);

        Label titleLabel = new Label("⚔️ ENEMIES (" + multiBattleState.getActiveEnemyCount() + "/4)");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
        panel.getChildren().add(titleLabel);

        // Create 4 slots
        for (int i = 0; i < MultiBattleState.MAX_ACTIVE_ENEMIES; i++) {
            MultiBattleState.BattleSlot slot = multiBattleState.getSlot(i);
            VBox slotPanel = createEnemySlotPanel(slot, i);
            panel.getChildren().add(slotPanel);
        }

        // Reinforcement queue info
        if (multiBattleState.getReinforcementQueueSize() > 0) {
            Label queueLabel = new Label("📢 Queue: " + multiBattleState.getReinforcementQueueSize() + " waiting");
            queueLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #f39c12;");
            panel.getChildren().add(queueLabel);
        }

        return panel;
    }

    /**
     * Create individual enemy slot panel (clickable for target selection)
     */
    private VBox createEnemySlotPanel(MultiBattleState.BattleSlot slot, int slotIndex) {
        VBox slotPanel = new VBox(5);
        slotPanel.setPadding(new Insets(8));
        slotPanel.setAlignment(Pos.CENTER_LEFT);

        if (slot.isActive() && slot.getEnemy() != null && slot.getEnemy().esteViu()) {
            // Active enemy - clickable for target selection
            Inamic enemy = slot.getEnemy();

            // Check if this is the selected target
            boolean isSelected = (slotIndex == selectedSlotIndex);

            // Style with selection highlight
            String borderColor = isSelected ? "#FFD700" : "#e74c3c"; // Gold if selected, red otherwise
            String borderWidth = isSelected ? "4" : "2";

            slotPanel.setStyle(
                    "-fx-background-color: #1a1a2e; " +
                            "-fx-background-radius: 8; " +
                            "-fx-border-color: " + borderColor + "; " +
                            "-fx-border-width: " + borderWidth + "; " +
                            "-fx-border-radius: 8; " +
                            "-fx-cursor: hand;" // Show it's clickable
            );

            // Add selection indicator
            Label nameLabel;
            if (isSelected) {
                nameLabel = new Label("🎯 Slot " + (slotIndex + 1) + ": " + enemy.getNume() + " [TARGET]");
                nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #FFD700;");
            } else {
                nameLabel = new Label("Slot " + (slotIndex + 1) + ": " + enemy.getNume());
                nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
            }

            ProgressBar hpBar = new ProgressBar((double) enemy.getViata() / enemy.getViataMaxima());
            hpBar.setPrefWidth(250);
            hpBar.setPrefHeight(15);
            hpBar.setStyle("-fx-accent: #e74c3c;");

            Label hpLabel = new Label(enemy.getViata() + " / " + enemy.getViataMaxima() + " HP");
            hpLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white;");

            slotPanel.getChildren().addAll(nameLabel, hpBar, hpLabel);

            // Make clickable to select this enemy as target
            slotPanel.setOnMouseClicked(event -> {
                selectEnemyTarget(slotIndex);
            });

            // Hover effect
            slotPanel.setOnMouseEntered(event -> {
                if (slotIndex != selectedSlotIndex) {
                    slotPanel.setStyle(
                            "-fx-background-color: #252540; " +
                                    "-fx-background-radius: 8; " +
                                    "-fx-border-color: #e74c3c; " +
                                    "-fx-border-width: 2; " +
                                    "-fx-border-radius: 8; " +
                                    "-fx-cursor: hand;"
                    );
                }
            });

            slotPanel.setOnMouseExited(event -> {
                if (slotIndex != selectedSlotIndex) {
                    slotPanel.setStyle(
                            "-fx-background-color: #1a1a2e; " +
                                    "-fx-background-radius: 8; " +
                                    "-fx-border-color: #e74c3c; " +
                                    "-fx-border-width: 2; " +
                                    "-fx-border-radius: 8; " +
                                    "-fx-cursor: hand;"
                    );
                }
            });

        } else {
            // Empty slot - check if reinforcement incoming
            MultiBattleState.ReinforcementEntry nextReinforcement = multiBattleState.getNextReinforcement();

            if (nextReinforcement != null && multiBattleState.getActiveEnemyCount() < MultiBattleState.MAX_ACTIVE_ENEMIES) {
                // Show countdown
                slotPanel.setStyle(
                        "-fx-background-color: #1a1a2e; " +
                                "-fx-background-radius: 8; " +
                                "-fx-border-color: #f39c12; " +
                                "-fx-border-width: 2; " +
                                "-fx-border-style: dashed; " +
                                "-fx-border-radius: 8;"
                );

                int turnsRemaining = nextReinforcement.getTurnsRemaining(multiBattleState.getCurrentTurn());
                Label emptyLabel = new Label("Slot " + (slotIndex + 1) + ": EMPTY");
                emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #95a5a6;");

                Label countdownLabel = new Label("⏰ " + nextReinforcement.getEnemy().getNume() + " arriving in " + turnsRemaining + " turns");
                countdownLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #f39c12;");

                slotPanel.getChildren().addAll(emptyLabel, countdownLabel);
            } else {
                // Completely empty
                slotPanel.setStyle(
                        "-fx-background-color: #0f0f1e; " +
                                "-fx-background-radius: 8; " +
                                "-fx-border-color: #34495e; " +
                                "-fx-border-width: 1; " +
                                "-fx-border-style: dashed; " +
                                "-fx-border-radius: 8;"
                );

                Label emptyLabel = new Label("Slot " + (slotIndex + 1) + ": EMPTY");
                emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");
                slotPanel.getChildren().add(emptyLabel);
            }
        }

        return slotPanel;
    }

    /**
     * Refresh multi-enemy panel (update HP bars, countdowns, slots)
     */
    private void refreshMultiEnemyPanel() {
        if (!isMultiBattle || multiBattleState == null) {
            return;
        }

        // CINEMATIC LAYOUT (new)
        if (battleUILayer != null && cinematicEnemySection != null) {
            // Recreate the cinematic enemy section
            HBox newEnemySection = createMultiEnemyCinematicSection();
            newEnemySection.setAlignment(Pos.TOP_CENTER);

            // Replace old enemy section with new one in battleUILayer
            int enemySectionIndex = battleUILayer.getChildren().indexOf(cinematicEnemySection);
            if (enemySectionIndex >= 0) {
                battleUILayer.getChildren().set(enemySectionIndex, newEnemySection);
                cinematicEnemySection = newEnemySection; // Update reference
            }
            return;
        }

        // OLD LAYOUT (fallback for non-cinematic battles)
        if (enemyPanelContainer != null) {
            // Recreate the multi-enemy panel
            VBox newPanel = createMultiEnemyPanel();

            // Replace content in container
            enemyPanelContainer.getChildren().clear();
            enemyPanelContainer.getChildren().addAll(newPanel.getChildren());

            // Copy styles
            enemyPanelContainer.setStyle(newPanel.getStyle());
            enemyPanelContainer.setPadding(newPanel.getPadding());
            enemyPanelContainer.setAlignment(newPanel.getAlignment());
        }
    }

    /**
     * Select enemy target by slot index
     */
    private void selectEnemyTarget(int slotIndex) {
        if (!isMultiBattle || multiBattleState == null) {
            System.out.println("🐛 selectEnemyTarget: Not a multi-battle");
            return;
        }

        MultiBattleState.BattleSlot slot = multiBattleState.getSlot(slotIndex);
        System.out.println("🐛 selectEnemyTarget: Slot " + slotIndex + " clicked");

        // Only allow selecting active, alive enemies
        if (slot != null && slot.isActive() && slot.getEnemy() != null && slot.getEnemy().esteViu()) {
            selectedSlotIndex = slotIndex;
            currentTarget = slot.getEnemy();

            System.out.println("✅ Target set: " + currentTarget.getNume() + " at slot " + selectedSlotIndex);
            addToLog("🎯 Target selected: " + currentTarget.getNume() + " (Slot " + (slotIndex + 1) + ")");

            // Refresh UI to show selection
            refreshMultiEnemyPanel();
        } else {
            System.out.println("❌ Slot " + slotIndex + " is not selectable (empty or dead)");
        }
    }

    /**
     * Auto-select next alive enemy when current target dies
     */
    private void autoSelectNextEnemy() {
        if (!isMultiBattle || multiBattleState == null) {
            return;
        }

        // Find first alive enemy
        for (int i = 0; i < MultiBattleState.MAX_ACTIVE_ENEMIES; i++) {
            MultiBattleState.BattleSlot slot = multiBattleState.getSlot(i);
            if (slot.isActive() && slot.getEnemy() != null && slot.getEnemy().esteViu()) {
                selectedSlotIndex = i;
                currentTarget = slot.getEnemy();
                addToLog("🎯 Auto-targeting: " + currentTarget.getNume() + " (Slot " + (i + 1) + ")");
                return;
            }
        }

        // No enemies left
        currentTarget = null;
    }

    /**
     * Panel log
     */
    private VBox createLogPanel() {
        VBox panel = new VBox(5);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #16213e; -fx-background-radius: 10;");

        Label logLabel = new Label("📜 Battle Log");
        logLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;");

        battleLog = new TextArea();
        battleLog.setEditable(false);
        battleLog.setWrapText(true);
        battleLog.setStyle(
                "-fx-control-inner-background: #0f0f1e; " +
                        "-fx-text-fill: #f1f1f1; " +
                        "-fx-font-family: 'Courier New'; " +
                        "-fx-font-size: 13px;"
        );
        battleLog.setPrefWidth(400);
        battleLog.setPrefHeight(400);
        VBox.setVgrow(battleLog, Priority.ALWAYS);

        panel.getChildren().addAll(logLabel, battleLog);
        return panel;
    }

    /**
     * Panel acțiuni
     */
    private VBox createActionPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #1a1a2e;");

        // Butoane principale
        HBox mainButtons = new HBox(10);
        mainButtons.setAlignment(Pos.CENTER);

        attackButton = createActionButton("⚔️ ATAC NORMAL", "#27ae60");
        attackButton.setOnAction(e -> handleNormalAttack());

        fleeButton = createActionButton("🏃 FUGI", "#e67e22");
        fleeButton.setOnAction(e -> handleFlee());

        mainButtons.getChildren().addAll(attackButton, fleeButton);

        // Panel abilități
        Label abilitiesLabel = new Label("✨ Abilități:");
        abilitiesLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-font-weight: bold;");

        abilityButtonsPanel = new VBox(5);
        abilityButtonsPanel.setAlignment(Pos.CENTER);

        // Panel poțiuni
        Label potionsLabel = new Label("🧪 Poțiuni:");
        potionsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-font-weight: bold;");

        potionButtonsPanel = new VBox(5);
        potionButtonsPanel.setAlignment(Pos.CENTER);

        HBox bottomPanels = new HBox(30);
        bottomPanels.setAlignment(Pos.CENTER);

        VBox abilitiesContainer = new VBox(5, abilitiesLabel, abilityButtonsPanel);
        abilitiesContainer.setAlignment(Pos.CENTER);

        VBox potionsContainer = new VBox(5, potionsLabel, potionButtonsPanel);
        potionsContainer.setAlignment(Pos.CENTER);

        bottomPanels.getChildren().addAll(abilitiesContainer, potionsContainer);

        panel.getChildren().addAll(mainButtons, bottomPanels);
        return panel;
    }

    /**
     * Creează un buton de acțiune
     */
    private Button createActionButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-color: " + color + "; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 12px 30px; " +
                        "-fx-background-radius: 10; " +
                        "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e ->
                btn.setStyle(
                        "-fx-font-size: 16px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-background-color: derive(" + color + ", 20%); " +
                                "-fx-text-fill: white; " +
                                "-fx-padding: 12px 30px; " +
                                "-fx-background-radius: 10; " +
                                "-fx-cursor: hand;"
                )
        );

        btn.setOnMouseExited(e ->
                btn.setStyle(
                        "-fx-font-size: 16px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-background-color: " + color + "; " +
                                "-fx-text-fill: white; " +
                                "-fx-padding: 12px 30px; " +
                                "-fx-background-radius: 10; " +
                                "-fx-cursor: hand;"
                )
        );

        return btn;
    }

    // ==================== BATTLE LOGIC ====================

    private void initializeBattle() {
        // Check if multi-battle or single battle
        if (isMultiBattle && multiBattleState != null) {
            // Multi-enemy battle initialization
            BattleInitDTO initData = battleService.initializeMultiBattle(hero, multiBattleState);
            updateUI(new AbilityDTO.BattleStateDTO(
                    initData.getHeroHP(),
                    initData.getHeroMaxHP(),
                    initData.getHeroResource(),
                    initData.getHeroMaxResource(),
                    initData.getEnemyHP(),
                    initData.getEnemyMaxHP(),
                    initData.getAbilities()
            ));

            // Auto-select first enemy as target
            autoSelectNextEnemy();

            battleLog.appendText("⚔️ MULTI-ENEMY BATTLE!\n");
            battleLog.appendText("Active enemies: " + multiBattleState.getActiveEnemyCount() + "/4\n");
            if (multiBattleState.getReinforcementQueueSize() > 0) {
                battleLog.appendText("Reinforcements: " + multiBattleState.getReinforcementQueueSize() + " incoming!\n");
            }
            battleLog.appendText("💡 Click on enemy slots to select target!\n");
            battleLog.appendText("\n");
            return;
        }

        // Single enemy battle initialization (original code)
        // 🆕 GENEREAZĂ INAMIC DACĂ LIPSEȘTE (pentru dungeon)
        if (this.enemy == null) {
            EnemyGeneratorRomanesc generator = new EnemyGeneratorRomanesc();
            int scaledLevel = hero.getNivel() + Math.max(0, (dungeonDepth - 1) / 3);

            if (dungeonDepth % 5 == 0) {
                this.enemy = generator.genereazaBoss(scaledLevel);
            } else {
                this.enemy = generator.genereazaInamicNormal(scaledLevel);
            }

            System.out.printf("🏰 GENERATED ENEMY: Depth %d, Level %d -> %s \n",
                    dungeonDepth, scaledLevel, enemy.getNume());
        }

        // Restul codului rămâne la fel...
        BattleInitDTO initData = battleService.initializeBattle(hero, enemy);
        updateUI(new AbilityDTO.BattleStateDTO(
                initData.getHeroHP(),
                initData.getHeroMaxHP(),
                initData.getHeroResource(),
                initData.getHeroMaxResource(),
                initData.getEnemyHP(),
                initData.getEnemyMaxHP(),
                initData.getAbilities()
        ));

        addToLog("⚔️ Bătălia începe!");
        addToLog(hero.getNume() + " vs " + enemy.getNume());

        if (inDungeon) {
            addToLog("🏰 Dungeon Depth: " + dungeonDepth);
        }

        if (enemy.isBoss()) {
            addToLog("💀 BOSS BATTLE! Pregătește-te!");
        }
        addToLog("━━━━━━━━━━━━━━━━━━━━━━━");
    }

    /**
     * Refresh battle display after revival (restore abilities and update stats)
     */
    private void refreshBattleDisplay() {
        // Re-initialize battle state to get fresh abilities
        BattleInitDTO initData = battleService.initializeBattle(hero, enemy);
        updateUI(new AbilityDTO.BattleStateDTO(
                hero.getViata(),
                hero.getViataMaxima(),
                hero.getResursaCurenta(),
                hero.getResursaMaxima(),
                enemy.getViata(),
                enemy.getViataMaxima(),
                initData.getAbilities()  // Use abilities from re-initialization
        ));
    }

    private void handleNormalAttack() {
        disableAllButtons();

        // Use currentTarget for multi-battles, enemy for single battles
        Inamic target = (isMultiBattle && currentTarget != null) ? currentTarget : enemy;

        AbilityDTO.BattleTurnResultDTO result = battleService.executeNormalAttack(hero, target);

        addToLog(result.getLog());

        // Visual feedback
        showCombatVisualFeedback(result, target);

        if (result.isBattleOver()) {
            handleBattleEnd(result);
        } else {
            updateUI(result.getCurrentState());

            // Auto-select next alive enemy if current target died
            if (isMultiBattle && multiBattleState != null && !target.esteViu()) {
                autoSelectNextEnemy();
            }

            enableAllButtons();
        }
    }

    private void handleFlee() {
        // Check for boss in multi-battle
        boolean hasBoss = false;
        if (isMultiBattle && multiBattleState != null) {
            for (MultiBattleState.BattleSlot slot : multiBattleState.getSlots()) {
                if (slot.isActive() && slot.getEnemy() != null && slot.getEnemy().isBoss()) {
                    hasBoss = true;
                    break;
                }
            }
        } else if (enemy != null && enemy.isBoss()) {
            hasBoss = true;
        }

        if (hasBoss) {
            addToLog("❌ Nu poți fugi de la un BOSS!");
            return;
        }

        if (DialogHelper.showConfirmation("Fugă", "Ești sigur că vrei să fugi din luptă?")) {
            disableAllButtons();

            // For multi-battle, need to pass correct enemy reference
            Inamic fleeTarget = isMultiBattle && currentTarget != null ? currentTarget : enemy;
            AbilityDTO.BattleTurnResultDTO result = battleService.attemptFlee(hero, fleeTarget);
            addToLog(result.getLog());

            if (result.hasFled()) {
                DialogHelper.showInfo("Fugă Reușită", "Ai scăpat din luptă!");

                // 🆕 In dungeon mode, call callback for flee (victory=false)
                if (onBattleEndCallback != null) {
                    System.out.println("🏃 Calling battle end callback for flee");
                    onBattleEndCallback.onBattleEnd(false, null);
                } else {
                    returnToTown();
                }
            } else if (result.isBattleOver()) {
                handleBattleEnd(result);
            } else {
                updateUI(result.getCurrentState());
                enableAllButtons();
            }
        }
    }

    private void handleAbilityUse(String abilityName) {
        disableAllButtons();

        // Use currentTarget for multi-battles, enemy for single battles
        Inamic target = (isMultiBattle && currentTarget != null) ? currentTarget : enemy;

        AbilityDTO.BattleTurnResultDTO result = battleService.executeAbility(hero, target, abilityName);

        if (!result.isSuccess()) {
            // Abilitatea nu a putut fi folosită
            addToLog(result.getLog());
            enableAllButtons();
            return;
        }

        addToLog(result.getLog());

        // Visual feedback
        showCombatVisualFeedback(result, target);

        if (result.isBattleOver()) {
            handleBattleEnd(result);
        } else {
            updateUI(result.getCurrentState());

            // Auto-select next alive enemy if current target died
            if (isMultiBattle && multiBattleState != null && !target.esteViu()) {
                autoSelectNextEnemy();
            }

            enableAllButtons();
        }
    }

    private void handlePotionUse(int healAmount) {
        // Sound effect for potion use
        com.rpg.utils.SoundManager.play(com.rpg.utils.SoundManager.SoundEffect.POTION_USE);

        // 🔄 MIGRARE AUTOMATĂ (păstrează din fix-ul anterior)
        if (hero.getInventar().getHealthPotions().isEmpty() && hero.getHealthPotions() > 0) {
            hero.getInventar().getHealthPotions().put(healAmount, hero.getHealthPotions());
            System.out.printf("🔄 AUTO-MIGRATED: %d poțiuni HP → Map\\n", hero.getHealthPotions());
        }

        // 🆕 DIALOG PENTRU ALEGEREA TIPULUI DE POȚIUNE
        if (hero.getHealthPotions() > 0 && hero.getManaPotions() > 0) {
            // Dacă ai și HP și resource potions, întreabă care vrea
            boolean useHealthPotion = DialogHelper.showConfirmation(
                    "Alegere Poțiune",
                    "Ce poțiune vrei să folosești?\\n\\n" +
                            "🧪 HP: Berice (+" + hero.getHealthPotionHealing() + " HP) x" + hero.getHealthPotions() + "\\n" +
                            "💙 " + hero.getTipResursa() + ": Energizant (+" + hero.getManaPotionRestore() + ") x" + hero.getManaPotions() + "\\n\\n" +
                            "✅ OK = HP Potion\\n" +
                            "❌ Cancel = " + hero.getTipResursa() + " Potion"
            );

            if (!useHealthPotion) {
                // Folosește resource potion în schimb
                handleResourcePotionUse(hero.getManaPotionRestore());
                return; // Exit early pentru a nu executa codul de health potion
            }
        } else if (hero.getHealthPotions() <= 0 && hero.getManaPotions() > 0) {
            // Dacă ai doar mana potions, folosește direct
            handleResourcePotionUse(hero.getManaPotionRestore());
            return;
        }

        // ✅ RESTUL CODULUI EXISTENT pentru health potions (NU schimba!)
        disableAllButtons();

        AbilityDTO.BattleTurnResultDTO result = battleService.usePotion(hero, enemy, healAmount);

        addToLog(result.getLog()); // ✅ Folosește getLog() ca în original

        if (result.isBattleOver()) { // ✅ Folosește isBattleOver() ca în original
            handleBattleEnd(result);
        } else {
            updateUI(result.getCurrentState());
            enableAllButtons();
        }
    }


    private void handleBattleEnd(AbilityDTO.BattleTurnResultDTO turnResult) {
        AbilityDTO.BattleResultDTO result = turnResult.getFinalResult();

        if (result == null) {
            // Fled
            returnToTown();
            return;
        }

        if (result.isVictory()) {
            showVictoryScreen(result);
        } else {
            // Hero died - check for shaorma revival
            if (hero.areShaormaRevival()) {
                boolean useRevival = DialogHelper.showConfirmation(
                    "💀 AI MURIT! 💀",
                    "Vrei să folosești o Șaorma de Revival?\n" +
                    "🌯 Șaorme disponibile: " + hero.getShaormaRevival() + "\n\n" +
                    "✅ Da = Reînvie cu 50% HP/Resources\n" +
                    "❌ Nu = Game Over"
                );

                if (useRevival && hero.folosesteShaormaRevival()) {
                    addToLog("🌯✨ ȘAORMA DE REVIVAL ACTIVATĂ! ✨🌯");
                    addToLog("💚 Te-ai reîntors din tărâmul umbrelor!");

                    // Refresh the battle display after revival
                    refreshBattleDisplay();
                    enableAllButtons();
                    return; // Continue battle
                }
            }

            // No shaorma or declined - show defeat
            showDefeatScreen();
        }
    }

    private void showVictoryScreen(AbilityDTO.BattleResultDTO result) {
        // Victory sound
        com.rpg.utils.SoundManager.play(com.rpg.utils.SoundManager.SoundEffect.VICTORY);

        StringBuilder victoryMsg = new StringBuilder();
        victoryMsg.append("🎉 VICTORIE! 🎉 \n \n");
        victoryMsg.append("Recompense: \n");
        victoryMsg.append("💰 Gold: ").append(result.getGoldEarned()).append(" \n");
        victoryMsg.append("⭐ Experiență: ").append(result.getExperienceEarned()).append(" \n");

        if (result.getShaormaReward() > 0) {
            victoryMsg.append("🌯 Șaorma Revival: ").append(result.getShaormaReward()).append(" \n");
        }

        if (result.hasLoot()) {
            victoryMsg.append(" \n📦 Loot primit: \n");
            for (var item : result.getLoot()) {
                victoryMsg.append("  • ").append(item.getNume()).append(" \n");
            }
        }

        // 💎 Display jewel drop
        if (result.hasJewelDrop()) {
            victoryMsg.append(" \n💎 JEWEL DROP! \n");
            victoryMsg.append("  • ").append(result.getJewelDrop().getName()).append(" \n");
            victoryMsg.append("    ").append(result.getJewelDrop().getRarity().getDisplayName());
            victoryMsg.append(" | ").append(result.getJewelDrop().getModifiers().size()).append(" mods \n");
        }

        DialogHelper.showSuccess("Victorie!", victoryMsg.toString());

        // 🆕 In dungeon mode with callback, DON'T add rewards directly to hero
        // They will be stored temporarily in DungeonRun and only saved on escape
        if (onBattleEndCallback == null) {
            // Non-dungeon mode or old dungeon system - add rewards directly
            System.out.println("💰 Adding rewards directly to hero (no callback)");

            hero.adaugaGold(result.getGoldEarned());

            // 🆕 LEVEL-UP UI FEEDBACK
            int oldLevel = hero.getNivel();
            hero.adaugaXp(result.getExperienceEarned());
            int newLevel = hero.getNivel();

            // 🎉 DIALOG LEVEL-UP
            if (newLevel > oldLevel) {
                // Level up sound
                com.rpg.utils.SoundManager.play(com.rpg.utils.SoundManager.SoundEffect.LEVEL_UP);

                StringBuilder levelUpMsg = new StringBuilder();
                levelUpMsg.append("🎉 LEVEL UP! 🎉 \n \n");

                if (newLevel - oldLevel > 1) {
                    levelUpMsg.append("🌟 MULTIPLE LEVEL UP! ").append(oldLevel).append(" → ").append(newLevel).append(" \n \n");
                } else {
                    levelUpMsg.append("🌟 Noul nivel: ").append(newLevel).append(" \n \n");
                }

                levelUpMsg.append("📈 Îmbunătățiri: \n");
                levelUpMsg.append("💪 Stat Points noi: ").append(hero.getStatPoints()).append(" \n");
                levelUpMsg.append("❤️  HP Maxim: ").append(hero.getViataMaxima()).append(" \n");
                levelUpMsg.append("🔋 ").append(hero.getTipResursa()).append(" Maxim: ").append(hero.getResursaMaxima()).append(" \n \n");
                levelUpMsg.append("💡 Vizitează Trainer-ul pentru stat upgrades!");

                DialogHelper.showSuccess("🎉 LEVEL UP! 🎉", levelUpMsg.toString());
            }

            // Restul reward-urilor
            if (result.getShaormaReward() > 0) {
                hero.adaugaShaormaRevival(result.getShaormaReward());
            }

            if (result.hasLoot()) {
                for (var item : result.getLoot()) {
                    hero.getInventar().addItem(item);
                }
            }

            // 💎 Add jewel to inventory
            if (result.hasJewelDrop()) {
                hero.addJewel(result.getJewelDrop());
            }
        } else {
            System.out.println("💼 Dungeon mode with callback: Rewards stored temporarily (not added to hero yet)");
            System.out.println("   Gold: " + result.getGoldEarned() + ", Exp: " + result.getExperienceEarned() + ", Items: " + result.getLoot().size());
        }

        // 🆕 ALEGERI DUPĂ VICTORIE (only for old dungeon system without callbacks)
        if (inDungeon && onBattleEndCallback == null) {
            String depthInfo = dungeonDepth > 1 ? " \n🏰 Depth actual: " + dungeonDepth : "";
            boolean continua = DialogHelper.showConfirmation(
                    "Continuă explorarea?",
                    "Ai învins " + enemy.getNume() + "!" + depthInfo + " \n \n" +
                            "Vrei să continui mai adânc în dungeon sau să te întorci în oraș? \n \n" +
                            "✅ OK = Continuă explorarea \n" +
                            "❌ Cancel = Întoarce-te în oraș"
            );

            if (continua) {
                // Creează următorul inamic cu dificultate crescută
                EnemyGeneratorRomanesc generator = new EnemyGeneratorRomanesc();

                // Crește dificultatea pe baza depth-ului
                int scaledLevel = hero.getNivel() + Math.max(0, dungeonDepth / 3);

                // La fiecare 5 depth-uri, boss battle
                Inamic nextEnemy;
                if ((dungeonDepth + 1) % 5 == 0) {
                    nextEnemy = generator.genereazaBoss(scaledLevel);
                    DialogHelper.showInfo("Boss Ahead!", "🔥 Un BOSS te așteaptă la depth " + (dungeonDepth + 1) + "!");
                } else {
                    nextEnemy = generator.genereazaInamicNormal(scaledLevel);
                }

                // Bonus healing între lupte (5% HP)
                int healing = Math.max(1, hero.getViataMaxima() / 20);
                hero.vindeca(healing);

                // Bonus mana/energy regen
                hero.regenereazaResursa(Math.max(5, hero.getResursaMaxima() / 10));

                System.out.printf("🏰 DUNGEON PROGRESS: Depth %d -> %d | Scaled Level: %d \n",
                        dungeonDepth, dungeonDepth + 1, scaledLevel);

                // Avansează în dungeon
                this.enemy = nextEnemy;
                this.dungeonDepth++;

                // Re-pornește lupta cu noul inamic
                stage.setScene(this.createScene());
                return;
            }
        }

        // 🆕 Dacă avem callback (dungeon mode), apelează-l
        if (onBattleEndCallback != null) {
            onBattleEndCallback.onBattleEnd(true, result);
            return;
        }

        // Default sau alegerea "Cancel": întoarce-te în oraș
        returnToTown();
    }


    private void showDefeatScreen() {
        // Defeat sound
        com.rpg.utils.SoundManager.play(com.rpg.utils.SoundManager.SoundEffect.DEFEAT);

        // Disable all buttons to prevent further actions
        disableAllButtons();

        // Build defeat message
        StringBuilder defeatMsg = new StringBuilder();
        defeatMsg.append("💀 AI FOST ÎNVINS! 💀\n\n");
        defeatMsg.append("Ucis de: ").append(enemy != null ? enemy.getNume() : "Unknown").append("\n");

        if (inDungeon) {
            defeatMsg.append("Depth: ").append(dungeonDepth).append("\n");
            defeatMsg.append("\n⚠️ PENALITATE:\n");
            defeatMsg.append("❌ Ai pierdut tot loot-ul temporar din run\n");
            defeatMsg.append("❌ Ai pierdut 30% din gold\n");
        }

        defeatMsg.append("\n⚰️ Game Over\n");
        defeatMsg.append("Vei fi trimis înapoi în oraș.");

        // Show dialog and wait for user to acknowledge
        DialogHelper.showError("Înfrângere!", defeatMsg.toString());

        System.out.println("🔴 DEFEAT: Returning to town/dungeon exit. Callback exists: " + (onBattleEndCallback != null));

        // 🆕 Dacă avem callback (dungeon mode), apelează-l
        if (onBattleEndCallback != null) {
            System.out.println("🔴 DEFEAT: Calling battle end callback with victory=false");
            onBattleEndCallback.onBattleEnd(false, null);
        } else {
            System.out.println("🔴 DEFEAT: No callback, returning to town directly");
            // Altfel, întoarce-te în oraș
            returnToTown();
        }
    }

    private void returnToTown() {
        // 🆕 Dacă avem callback (dungeon mode), apelează-l pentru victory
        if (onBattleEndCallback != null) {
            // This is called from showVictoryScreen, result is passed separately
            return;
        }

        TownMenuController townController = new TownMenuController(stage, hero);
        stage.setScene(townController.createScene());
    }

    // ==================== VISUAL FEEDBACK ====================

    /**
     * Show visual feedback for combat actions (floating text, screen shake, etc.)
     */
    private void showCombatVisualFeedback(AbilityDTO.BattleTurnResultDTO result, Inamic target) {
        if (result == null || battleCanvas == null || battleRoot == null) {
            return;
        }

        String log = result.getLog();

        // Calculate positions
        double canvasWidth = battleCanvas.getWidth() > 0 ? battleCanvas.getWidth() : 1900;
        double canvasHeight = battleCanvas.getHeight() > 0 ? battleCanvas.getHeight() : 1080;

        double enemyX;
        double enemyY;

        // For multi-battle, position floating text next to the specific enemy slot
        if (isMultiBattle && selectedSlotIndex >= 0 && selectedSlotIndex < 4) {
            // Calculate position for the specific enemy slot (1x4 horizontal layout)
            // Each box is 320px wide with 15px spacing
            int boxWidth = 320;
            int spacing = 15;
            int totalWidth = (boxWidth * 4) + (spacing * 3); // 1325px
            double startX = (canvasWidth - totalWidth) / 2.0;

            // Position at the center of the selected slot
            enemyX = startX + (selectedSlotIndex * (boxWidth + spacing)) + (boxWidth / 2.0);
            enemyY = 180; // Near the top where enemy boxes are
        } else {
            // Single enemy battle - use default positioning
            enemyX = canvasWidth * 0.75; // Right side for enemy
            enemyY = canvasHeight * 0.3;  // Upper area
        }

        double heroX = canvasWidth * 0.25;  // Left side for hero
        double heroY = canvasHeight * 0.7;   // Lower area

        // Parse damage dealt to enemy
        int damageToEnemy = parseDamageFromLog(log, "damage");
        boolean isCrit = log.contains("CRIT") || log.contains("CRITICAL");
        boolean isDodge = log.contains("DODGE") || log.contains("a ratat");
        boolean isMiss = log.contains("MISS") || log.contains("a ratat");

        // Show damage to enemy
        if (damageToEnemy > 0) {
            com.rpg.ui.FloatingText.TextType textType = isCrit ?
                    com.rpg.ui.FloatingText.TextType.CRITICAL :
                    com.rpg.ui.FloatingText.TextType.DAMAGE;
            com.rpg.ui.FloatingText.show(battleCanvas, String.valueOf(damageToEnemy), enemyX, enemyY, textType);

            // Sound effect for damage
            com.rpg.utils.SoundManager.playDamageSound(damageToEnemy, target.getViataMaxima(), isCrit);

            // Screen shake for damage
            if (isCrit) {
                com.rpg.ui.ScreenShake.shake(battleRoot, com.rpg.ui.ScreenShake.ShakeIntensity.CRITICAL);
            } else if (damageToEnemy > target.getViataMaxima() * 0.3) {
                com.rpg.ui.ScreenShake.shake(battleRoot, com.rpg.ui.ScreenShake.ShakeIntensity.HEAVY);
            } else {
                com.rpg.ui.ScreenShake.shakeForDamage(battleRoot, damageToEnemy, target.getViataMaxima());
            }

            // Update enemy health bar
            if (isMultiBattle && multiEnemyHealthBars != null && selectedSlotIndex >= 0 && selectedSlotIndex < 4) {
                // Multi-battle: update only the specific enemy's health bar
                if (multiEnemyHealthBars[selectedSlotIndex] != null && target != null) {
                    multiEnemyHealthBars[selectedSlotIndex].updateHP(target.getViata(), true);
                }
            } else if (enemyAnimatedHealthBar != null && target != null) {
                // Single enemy battle: update the single health bar
                enemyAnimatedHealthBar.updateHP(target.getViata(), true);
            }
        }

        // Show dodge/miss
        if (isDodge) {
            com.rpg.ui.FloatingText.show(battleCanvas, "DODGE!", enemyX, enemyY, com.rpg.ui.FloatingText.TextType.DODGE);
            com.rpg.utils.SoundManager.play(com.rpg.utils.SoundManager.SoundEffect.DODGE);
        } else if (isMiss) {
            com.rpg.ui.FloatingText.show(battleCanvas, "MISS!", enemyX, enemyY, com.rpg.ui.FloatingText.TextType.MISS);
            com.rpg.utils.SoundManager.play(com.rpg.utils.SoundManager.SoundEffect.ATTACK_MISS);
        }

        // Parse damage taken by hero (from enemy counterattack)
        int damageToHero = parseDamageFromLog(log, "counterattack");
        if (damageToHero > 0) {
            com.rpg.ui.FloatingText.show(battleCanvas, String.valueOf(damageToHero), heroX, heroY, com.rpg.ui.FloatingText.TextType.DAMAGE);
            com.rpg.ui.ScreenShake.shakeForDamage(battleRoot, damageToHero, hero.getViataMaxima());

            // Sound effect for taking damage
            com.rpg.utils.SoundManager.playDamageSound(damageToHero, hero.getViataMaxima(), false);

            // Update hero health bar
            if (heroAnimatedHealthBar != null) {
                heroAnimatedHealthBar.updateHP(hero.getViata(), true);
            }
        }

        // Parse healing
        int healing = parseDamageFromLog(log, "vindec");
        if (healing > 0) {
            com.rpg.ui.FloatingText.show(battleCanvas, "+" + healing, heroX, heroY, com.rpg.ui.FloatingText.TextType.HEAL);
            com.rpg.utils.SoundManager.play(com.rpg.utils.SoundManager.SoundEffect.HEAL);
            if (heroAnimatedHealthBar != null) {
                heroAnimatedHealthBar.updateHP(hero.getViata(), false);
            }
        }

        // 🆕 PARSE DEBUFF DAMAGE (DOT - Damage Over Time)
        int debuffDamage = parseDamageFromLog(log, "debuff_damage");
        if (debuffDamage > 0) {
            String debuffType = getDebuffTypeFromLog(log);

            // Show debuff damage with thematic icon
            String debuffIcon = switch (debuffType) {
                case "burn" -> "🔥";
                case "poison" -> "☠️";
                case "bleed" -> "🩸";
                case "freeze" -> "❄️";
                case "shock" -> "⚡";
                default -> "💀";
            };

            // Display floating text for debuff damage
            com.rpg.ui.FloatingText.show(battleCanvas, debuffIcon + " " + debuffDamage,
                    enemyX, enemyY, com.rpg.ui.FloatingText.TextType.DAMAGE);

            // Play appropriate sound for debuff
            com.rpg.utils.SoundManager.playDamageSound(debuffDamage, target.getViataMaxima(), false);

            // Update enemy health bar
            if (isMultiBattle && multiEnemyHealthBars != null && selectedSlotIndex >= 0 && selectedSlotIndex < 4) {
                if (multiEnemyHealthBars[selectedSlotIndex] != null && target != null) {
                    multiEnemyHealthBars[selectedSlotIndex].updateHP(target.getViata(), true);
                }
            } else if (enemyAnimatedHealthBar != null && target != null) {
                enemyAnimatedHealthBar.updateHP(target.getViata(), true);
            }
        }

        // 🆕 PARSE ENEMY REGENERATION
        int enemyRegen = parseDamageFromLog(log, "regenerate");
        if (enemyRegen > 0 && log.contains(target.getNume())) {
            com.rpg.ui.FloatingText.show(battleCanvas, "💚 +" + enemyRegen,
                    enemyX, enemyY, com.rpg.ui.FloatingText.TextType.HEAL);

            if (isMultiBattle && multiEnemyHealthBars != null && selectedSlotIndex >= 0 && selectedSlotIndex < 4) {
                if (multiEnemyHealthBars[selectedSlotIndex] != null && target != null) {
                    multiEnemyHealthBars[selectedSlotIndex].updateHP(target.getViata(), false);
                }
            } else if (enemyAnimatedHealthBar != null && target != null) {
                enemyAnimatedHealthBar.updateHP(target.getViata(), false);
            }
        }

        // Update status effect displays
        if (heroBuffDisplay != null) {
            heroBuffDisplay.updateBuffs(hero.getBuffuriActive());
        }
        if (heroDebuffDisplay != null) {
            // Use full DebuffStack for detailed tooltips
            heroDebuffDisplay.updateDebuffs(hero.getDebuffuriActive());
        }
        if (enemyBuffDisplay != null && target != null) {
            enemyBuffDisplay.updateBuffs(new java.util.HashMap<>()); // Enemies don't get buffed
        }
        if (enemyDebuffDisplay != null && target != null) {
            // Use full DebuffStack for detailed tooltips
            enemyDebuffDisplay.updateDebuffs(target.getDebuffStacksActive());
        }
    }

    /**
     * Parse damage numbers from log messages
     */
    private int parseDamageFromLog(String log, String context) {
        try {
            // Look for patterns like "X damage" or "X HP"
            if (context.equals("damage")) {
                // Pattern: "deals X damage"
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)\\s+damage");
                java.util.regex.Matcher matcher = pattern.matcher(log);
                if (matcher.find()) {
                    return Integer.parseInt(matcher.group(1));
                }
            } else if (context.equals("counterattack")) {
                // Pattern for enemy counterattack damage
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("contraatac.*?(\\d+)\\s+damage");
                java.util.regex.Matcher matcher = pattern.matcher(log);
                if (matcher.find()) {
                    return Integer.parseInt(matcher.group(1));
                }
            } else if (context.equals("vindec")) {
                // Pattern for healing
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)\\s+HP");
                java.util.regex.Matcher matcher = pattern.matcher(log);
                if (matcher.find()) {
                    return Integer.parseInt(matcher.group(1));
                }
            } else if (context.equals("debuff_damage")) {
                // Pattern for debuff damage: "takes X damage from"
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("takes\\s+(\\d+)\\s+damage\\s+from");
                java.util.regex.Matcher matcher = pattern.matcher(log);
                if (matcher.find()) {
                    return Integer.parseInt(matcher.group(1));
                }
            } else if (context.equals("regenerate")) {
                // Pattern for regeneration: "regenerates X HP"
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("regenerates\\s+(\\d+)\\s+HP");
                java.util.regex.Matcher matcher = pattern.matcher(log);
                if (matcher.find()) {
                    return Integer.parseInt(matcher.group(1));
                }
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        return 0;
    }

    /**
     * Get debuff type from log message
     */
    private String getDebuffTypeFromLog(String log) {
        String lower = log.toLowerCase();
        if (lower.contains("burn") || lower.contains("fire")) return "burn";
        if (lower.contains("poison") || lower.contains("otrava")) return "poison";
        if (lower.contains("bleed") || lower.contains("sangerare")) return "bleed";
        if (lower.contains("freeze") || lower.contains("ice") || lower.contains("gheata")) return "freeze";
        if (lower.contains("shock") || lower.contains("lightning")) return "shock";
        return "generic";
    }

    // ==================== UI UPDATE ====================

    private void updateUI(AbilityDTO.BattleStateDTO state) {
        // 🛡️ GUARD împotriva null state
        if (state == null) {
            addToLog("⚠️ Eroare internă: starea luptei e nulă după acțiune.");
            enableAllButtons();
            return;
        }

        // 🛡️ NORMALIZE max values pentru a evita division by zero
        int heroMaxHP = Math.max(1, state.getHeroMaxHP());
        int heroMaxRes = Math.max(1, state.getHeroMaxResource());
        int enemyMaxHP = Math.max(1, state.getEnemyMaxHP());

        // 🔍 DEBUG OPTIONAL (decomentează dacă vrei să vezi valorile)
        // addToLog(String.format("DEBUG State: H %d/%d R %d/%d | E %d/%d",
        //         state.getHeroHP(), heroMaxHP,
        //         state.getHeroResource(), heroMaxRes,
        //         state.getEnemyHP(), enemyMaxHP));

        // ✅ UPDATE HERO cu protecție
        if (heroHPLabel != null) {
            heroHPLabel.setText(state.getHeroHP() + " / " + heroMaxHP);
        }
        if (heroHPBar != null) {
            double heroHpProgress = Math.min(1.0, Math.max(0.0, (double) state.getHeroHP() / heroMaxHP));
            heroHPBar.setProgress(heroHpProgress);
            animateHealthBar(heroHPBar);
        }
        // Update animated health bar
        if (heroAnimatedHealthBar != null) {
            heroAnimatedHealthBar.updateHP(state.getHeroHP(), false);
        }

        heroResourceLabel.setText(state.getHeroResource() + " / " + heroMaxRes);
        double heroResProgress = Math.min(1.0, Math.max(0.0, (double) state.getHeroResource() / heroMaxRes));
        heroResourceBar.setProgress(heroResProgress);

        // ✅ UPDATE ENEMY cu protecție
        if (enemyHPLabel != null && enemyHPBar != null) {
            enemyHPLabel.setText(state.getEnemyHP() + " / " + enemyMaxHP);
            double enemyHpProgress = Math.min(1.0, Math.max(0.0, (double) state.getEnemyHP() / enemyMaxHP));
            enemyHPBar.setProgress(enemyHpProgress);
            animateHealthBar(enemyHPBar);
        }
        // Update animated health bar
        if (enemyAnimatedHealthBar != null) {
            enemyAnimatedHealthBar.updateHP(state.getEnemyHP(), false);
        }

        // ✅ UPDATE MULTI-ENEMY PANEL (if multi-battle)
        if (isMultiBattle && multiBattleState != null) {
            refreshMultiEnemyPanel();
        }

        // ✅ UPDATE STATUS EFFECTS
        if (heroBuffDisplay != null && hero != null) {
            heroBuffDisplay.updateBuffs(hero.getBuffuriActive());
        }
        if (heroDebuffDisplay != null) {
            heroDebuffDisplay.updateDebuffs(hero.getDebuffuriActive()); // Display hero debuffs with full info
        }
        if (enemyBuffDisplay != null) {
            enemyBuffDisplay.updateBuffs(new java.util.HashMap<>()); // Enemies don't get buffed
        }
        if (enemyDebuffDisplay != null && enemy != null) {
            enemyDebuffDisplay.updateDebuffs(enemy.getDebuffStacksActive()); // Show enemy debuffs with full info
        }

        // ✅ UPDATE ABILITIES și POTIONS cu protecție
        if (state.getAbilities() != null) {
            updateAbilityButtons(state.getAbilities());
        }
        updatePotionButtons();
    }


    private void updateAbilityButtons(java.util.List<AbilityDTO> abilities) {
        abilityButtonsPanel.getChildren().clear();

        for (AbilityDTO ability : abilities) {
            // Find the actual ability object to generate detailed tooltip
            com.rpg.model.abilities.Abilitate actualAbility = findAbilityByName(ability.getName());

            Button btn = new Button(ability.getDisplayName());
            btn.setDisable(!ability.isAvailable());

            String color = ability.isAvailable() ? "#9b59b6" : "#7f8c8d";
            btn.setStyle(
                    "-fx-font-size: 14px; " +
                            "-fx-background-color: " + color + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-padding: 8px 20px; " +
                            "-fx-background-radius: 8; " +
                            "-fx-min-width: 200px;"
            );

            btn.setOnAction(e -> handleAbilityUse(ability.getName()));

            // 🆕 Add detailed tooltip
            if (actualAbility != null && hero != null) {
                try {
                    String tooltipText = com.rpg.utils.AbilityTooltipGenerator.generateTooltip(actualAbility, hero);
                    if (tooltipText != null && !tooltipText.isEmpty()) {
                        Tooltip tooltip = new Tooltip(tooltipText);
                        tooltip.setStyle(
                            "-fx-font-family: 'Courier New', monospace; " +
                            "-fx-font-size: 11px; " +
                            "-fx-background-color: #2c3e50; " +
                            "-fx-text-fill: #ecf0f1; " +
                            "-fx-padding: 10px; " +
                            "-fx-background-radius: 5px; " +
                            "-fx-max-width: 500px; " +
                            "-fx-wrap-text: true;"
                        );
                        tooltip.setShowDelay(javafx.util.Duration.millis(300)); // Show after 300ms hover
                        btn.setTooltip(tooltip);
                    } else {
                        // Fallback to simple tooltip
                        Tooltip simpleTooltip = new Tooltip(ability.getName() + "\nCost: " + actualAbility.getCostMana());
                        btn.setTooltip(simpleTooltip);
                    }
                } catch (Exception ex) {
                    // Fallback to simple tooltip on error
                    Tooltip simpleTooltip = new Tooltip(ability.getName());
                    btn.setTooltip(simpleTooltip);
                }
            } else {
                // Basic tooltip when ability not found
                Tooltip basicTooltip = new Tooltip(ability.getName());
                btn.setTooltip(basicTooltip);
            }

            abilityButtonsPanel.getChildren().add(btn);
        }
    }

    /**
     * Find the actual Abilitate object by name from hero's abilities
     */
    private com.rpg.model.abilities.Abilitate findAbilityByName(String name) {
        if (hero == null) return null;

        // Try to find in the new ability loadout system first
        if (hero.getAbilityLoadout() != null) {
            for (com.rpg.model.abilities.ConfiguredAbility configured : hero.getAbilityLoadout().getActiveAbilities()) {
                if (configured != null) {
                    // Check variant name (display name)
                    if (configured.getDisplayName().equals(name)) {
                        return configured.getBaseAbility();
                    }
                    // Check base ability name
                    if (configured.getBaseAbility() != null && configured.getBaseAbility().getNume().equals(name)) {
                        return configured.getBaseAbility();
                    }
                }
            }
        }

        // Fallback to old ability system
        for (com.rpg.model.abilities.Abilitate ability : hero.getAbilitati()) {
            if (ability.getNume().equals(name)) {
                return ability;
            }
        }

        return null;
    }

    /**
     * Find the ConfiguredAbility object by name (for enhanced tooltips)
     */
    private com.rpg.model.abilities.ConfiguredAbility findConfiguredAbilityByName(String name) {
        if (hero == null || hero.getAbilityLoadout() == null) return null;

        for (com.rpg.model.abilities.ConfiguredAbility configured : hero.getAbilityLoadout().getActiveAbilities()) {
            if (configured != null) {
                // Check variant name (display name)
                if (configured.getDisplayName().equals(name)) {
                    return configured;
                }
                // Check base ability name
                if (configured.getBaseAbility() != null && configured.getBaseAbility().getNume().equals(name)) {
                    return configured;
                }
            }
        }

        return null;
    }

    private void updatePotionButtons() {
        // 🔍 GĂSEȘTE CONTAINERUL CORECT - înlocuiește cu numele real
        // Caută în createActionPanel() sau createUI() numele containerului pentru poțiuni
        VBox potionsContainer = potionButtonsPanel; // SAU orice se numește containerul tău

        if (potionsContainer != null) {
            potionsContainer.getChildren().clear();

            // 🧪 AFIȘEAZĂ POȚIUNI SIMPLE (sistemul vechi)
            int healthPotions = hero.getHealthPotions();
            if (healthPotions > 0) {
                int healAmount = hero.getHealthPotionHealing();

                Button potionBtn = new Button("🧪 Berice (" + healAmount + " HP) x" + healthPotions);
                potionBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
                potionBtn.setOnAction(e -> handlePotionUse(healAmount));
                potionsContainer.getChildren().add(potionBtn);
            } else {
                Label noLabel = new Label("❌ Nu ai poțiuni");
                noLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-style: italic;");
                potionsContainer.getChildren().add(noLabel);
            }

            // 🧪 AFIȘEAZĂ MANA POTIONS dacă există
            int manaPotions = hero.getManaPotions();
            if (manaPotions > 0) {
                int restoreAmount = hero.getManaPotionRestore();

                Button manaBtn = new Button("💙 " + hero.getTipResursa() + " (" + restoreAmount + ") x" + manaPotions);
                manaBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
                manaBtn.setOnAction(e -> handleResourcePotionUse(restoreAmount));
                potionsContainer.getChildren().add(manaBtn);
            }
        }
    }

    /**
     * Gestionează folosirea poțiunilor de resurse
     */
    private void handleResourcePotionUse(int restoreAmount) {
        disableAllButtons();

        AbilityDTO.BattleTurnResultDTO result = battleService.useResourcePotion(hero, enemy, restoreAmount);

        // ✅ FOLOSEȘTE getLog() ca în codul existent
        addToLog(result.getLog());

        // ✅ FOLOSEȘTE isBattleOver() ca în codul existent
        if (result.isBattleOver()) {
            // Call handleBattleEnd which handles victory/defeat properly
            handleBattleEnd(result);
        } else {
            // Continuă lupta
            updateUI(result.getCurrentState());
            enableAllButtons();
        }
    }

    /**
     * Helper pentru a verifica dacă eroul a murit din BattleTurnResultDTO
     */
    private boolean isHeroDead(AbilityDTO.BattleTurnResultDTO result) {
        // Verifică din starea curentă dacă eroul are 0 HP
        return result.getCurrentState().getHeroHP() <= 0;
    }


    private void addToLog(String message) {
        battleLog.appendText(message + "\n");
        battleLog.setScrollTop(Double.MAX_VALUE);
    }

    private void disableAllButtons() {
        attackButton.setDisable(true);
        fleeButton.setDisable(true);
        abilityButtonsPanel.getChildren().forEach(node -> {
            if (node instanceof Button) {
                ((Button)node).setDisable(true);
            }
        });
        potionButtonsPanel.getChildren().forEach(node -> {
            if (node instanceof Button) {
                ((Button)node).setDisable(true);
            }
        });
    }

    private void enableAllButtons() {
        attackButton.setDisable(false);
        fleeButton.setDisable(false);
        // Abilities și potions vor fi update-ate prin updateUI
    }

    private void animateHealthBar(ProgressBar bar) {
        FadeTransition ft = new FadeTransition(Duration.millis(200), bar);
        ft.setFromValue(0.5);
        ft.setToValue(1.0);
        ft.play();
    }

    // ==================== DUNGEON INTEGRATION ====================

    /**
     * Callback interface pentru dungeon system
     */
    @FunctionalInterface
    public interface BattleEndCallback {
        void onBattleEnd(boolean victory, AbilityDTO.BattleResultDTO rewards);
    }

    /**
     * Setează callback-ul pentru când se termină bătălia
     */
    public void setOnBattleEnd(BattleEndCallback callback) {
        this.onBattleEndCallback = callback;
    }

    /**
     * Convertește Map<String, DebuffStack> în Map<String, Integer> pentru afișare
     * Similar cu cum funcționează pentru enemy debuffs
     */
    private Map<String, Integer> convertDebuffStacksToMap(Map<String, DebuffStack> debuffStacks) {
        Map<String, Integer> result = new java.util.HashMap<>();
        if (debuffStacks != null) {
            for (Map.Entry<String, DebuffStack> entry : debuffStacks.entrySet()) {
                if (entry.getValue() != null && entry.getValue().isActive()) {
                    result.put(entry.getKey(), entry.getValue().getDurata());
                }
            }
        }
        return result;
    }
}