package com.rpg.controller;

import com.rpg.model.characters.Erou;
import com.rpg.model.items.Jewel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.ScaleTransition;
import javafx.animation.FadeTransition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Path of Exile-style talent tree UI
 */
public class TalentTreeController {

    private Stage stage;
    private Erou hero;
    private Runnable onBackCallback;
    private Pane treePane;
    private StackPane treeContainer;
    private Label passivePointsLabel;
    private Label allocatedNodesLabel;
    private Label totalStatsLabel;
    private double currentZoom = 1.0;

    // Tree state
    private Set<Integer> allocatedNodes = new HashSet<>();
    private TalentNode selectedNode = null;
    private List<TalentNode> allNodes = new ArrayList<>();
    private Map<String, Line> connectionLines = new HashMap<>(); // Key: "fromId-toId"

    // Visual settings
    private static final double NODE_SMALL_RADIUS = 12;
    private static final double NODE_NOTABLE_RADIUS = 20;
    private static final double NODE_KEYSTONE_RADIUS = 30;
    private static final double TREE_WIDTH = 2400;  // Increased from 1600
    private static final double TREE_HEIGHT = 2400; // Increased from 1200

    // Colors
    private static final Color COLOR_STRENGTH = Color.rgb(255, 100, 100);
    private static final Color COLOR_DEXTERITY = Color.rgb(100, 255, 100);
    private static final Color COLOR_INTELLIGENCE = Color.rgb(100, 150, 255);
    private static final Color COLOR_HYBRID = Color.rgb(200, 200, 100);
    private static final Color COLOR_ALLOCATED = Color.rgb(255, 215, 0);
    private static final Color COLOR_UNALLOCATED = Color.rgb(60, 60, 60);
    private static final Color COLOR_AVAILABLE = Color.rgb(150, 150, 150);

    public TalentTreeController(Stage stage, Erou hero) {
        this(stage, hero, null);
    }

    public TalentTreeController(Stage stage, Erou hero, Runnable onBackCallback) {
        this.stage = stage;
        this.hero = hero;
        this.onBackCallback = onBackCallback;

        // Load previously allocated nodes from hero
        this.allocatedNodes = new HashSet<>(hero.getAllocatedTalentNodes());

        // Start node (0) is always allocated
        this.allocatedNodes.add(0);
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0a0a0a;");

        // Top header
        root.setTop(createHeader());

        // Center - scrollable tree
        root.setCenter(createTreeView());

        // Bottom - controls
        root.setBottom(createControls());

        return new Scene(root, 1400, 900);
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(15));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: #1a1a2e;");

        Label title = new Label("🌳 TALENT TREE");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        HBox statsBox = new HBox(30);
        statsBox.setAlignment(Pos.CENTER);

        passivePointsLabel = new Label("Passive Points: " + hero.getPassivePoints());
        passivePointsLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #FFD700; -fx-font-weight: bold;");

        allocatedNodesLabel = new Label("Allocated Nodes: " + (allocatedNodes.size() - 1));
        allocatedNodesLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");

        Label levelLabel = new Label("Level: " + hero.getNivel());
        levelLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");

        statsBox.getChildren().addAll(passivePointsLabel, allocatedNodesLabel, levelLabel);

        // Stat summary panel
        HBox statSummaryBox = new HBox(40);
        statSummaryBox.setAlignment(Pos.CENTER);
        statSummaryBox.setPadding(new Insets(10, 0, 0, 0));

        totalStatsLabel = new Label();
        updateStatSummary();
        totalStatsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #aaa;");

        statSummaryBox.getChildren().add(totalStatsLabel);

        header.getChildren().addAll(title, statsBox, statSummaryBox);
        return header;
    }

    private ScrollPane createTreeView() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-background: #000000; -fx-background-color: #000000;");
        scrollPane.setPannable(true);
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Create a container for the tree with proper background
        treeContainer = new StackPane();
        treeContainer.setMinSize(TREE_WIDTH, TREE_HEIGHT);
        treeContainer.setPrefSize(TREE_WIDTH, TREE_HEIGHT);
        treeContainer.setMaxSize(TREE_WIDTH, TREE_HEIGHT);
        treeContainer.setStyle("-fx-background-color: #000000;");

        treePane = new Pane();
        treePane.setMinSize(TREE_WIDTH, TREE_HEIGHT);
        treePane.setPrefSize(TREE_WIDTH, TREE_HEIGHT);
        treePane.setMaxSize(TREE_WIDTH, TREE_HEIGHT);

        // Generate the talent tree
        generateTalentTree();

        // Render connections and nodes
        renderTree();

        treeContainer.getChildren().add(treePane);
        scrollPane.setContent(treeContainer);

        // Center the view on the middle
        scrollPane.setHvalue(0.5);
        scrollPane.setVvalue(0.5);

        // Mouse wheel zoom - block scrolling and only zoom
        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            // Store current scroll position before zoom
            double oldHvalue = scrollPane.getHvalue();
            double oldVvalue = scrollPane.getVvalue();

            // Apply zoom
            double zoomFactor = event.getDeltaY() > 0 ? 1.1 : 0.9;
            double newZoom = currentZoom * zoomFactor;

            // Limit zoom range
            if (newZoom >= 0.3 && newZoom <= 3.0) {
                currentZoom = newZoom;
                treeContainer.setScaleX(currentZoom);
                treeContainer.setScaleY(currentZoom);

                // Restore scroll position after zoom to prevent scrolling
                javafx.application.Platform.runLater(() -> {
                    scrollPane.setHvalue(oldHvalue);
                    scrollPane.setVvalue(oldVvalue);
                });
            }

            event.consume(); // Critical: consume event to block default scroll behavior
        });

        return scrollPane;
    }

    private void generateTalentTree() {
        allNodes.clear();

        double centerX = TREE_WIDTH / 2;
        double centerY = TREE_HEIGHT / 2;
        double spacing = 80; // Standard spacing for nodes

        // Starting node at center
        TalentNode startNode = new TalentNode(
            0, "Start", NodeType.START,
            centerX, centerY,
            "Starting point for all heroes",
            null, 0, 0, 0
        );
        allNodes.add(startNode);
        allocatedNodes.add(0); // Start node is always allocated

        // Generate Strength path (top-left, red)
        generateStrengthPath(centerX, centerY);

        // Generate Dexterity path (top-right, green)
        generateDexterityPath(centerX, centerY);

        // Generate Intelligence path (bottom, blue)
        generateIntelligencePath(centerX, centerY);

        // Add jewel sockets strategically placed
        addJewelSockets(centerX, centerY, spacing);
    }

    private void addJewelSockets(double centerX, double centerY, double spacing) {
        int socketId = 1000; // Start jewel socket IDs at 1000
        Color socketColor = Color.rgb(200, 100, 255); // Purple for jewel sockets

        // Socket 1: Between STR and DEX paths (top-right area)
        TalentNode socket1 = new TalentNode(
            socketId++, "💎 Jewel Socket", NodeType.JEWEL_SOCKET,
            centerX + spacing * 2.5, centerY - spacing * 4.5,
            "Socket for jewels\n\nAllocate to unlock",
            socketColor, 0, 0, 0
        );
        socket1.addConnection(6); // Connect to STR path
        socket1.addConnection(104); // Connect to DEX path
        allNodes.add(socket1);

        // Socket 2: Between DEX and INT paths (bottom-right area)
        TalentNode socket2 = new TalentNode(
            socketId++, "💎 Jewel Socket", NodeType.JEWEL_SOCKET,
            centerX + spacing * 2.5, centerY + spacing * 4.5,
            "Socket for jewels\n\nAllocate to unlock",
            socketColor, 0, 0, 0
        );
        socket2.addConnection(109); // Connect to DEX path
        socket2.addConnection(204); // Connect to INT path
        allNodes.add(socket2);

        // Socket 3: Between INT and STR paths (left area)
        TalentNode socket3 = new TalentNode(
            socketId++, "💎 Jewel Socket", NodeType.JEWEL_SOCKET,
            centerX - spacing * 5.5, centerY - spacing * 0.5,
            "Socket for jewels\n\nAllocate to unlock",
            socketColor, 0, 0, 0
        );
        socket3.addConnection(207); // Connect to INT path
        socket3.addConnection(8); // Connect to STR path
        allNodes.add(socket3);
    }

    private void generateStrengthPath(double centerX, double centerY) {
        int nodeId = 1;
        double spacing = 80;

        // Main strength line going up-left
        double angle = Math.toRadians(135); // 135 degrees = up-left

        for (int i = 1; i <= 15; i++) {
            double x = centerX + Math.cos(angle) * spacing * i;
            double y = centerY + Math.sin(angle) * spacing * i;

            NodeType type = NodeType.SMALL;
            String name = "+10 Strength";
            String desc = "Increases Strength by 10";
            int strBonus = 10;

            // Every 5th node is a notable
            if (i % 5 == 0) {
                type = NodeType.NOTABLE;
                name = "💪 Warrior's Might";
                desc = "Powerful physique";
                strBonus = 30;
            }

            // Last node is a keystone
            if (i == 15) {
                type = NodeType.KEYSTONE;
                name = "⚔️ BERSERKER";
                desc = "Trade defense for overwhelming offense\n\n[KEYSTONE]";
                strBonus = 0;
            }

            TalentNode node = new TalentNode(
                nodeId, name, type, x, y, desc,
                COLOR_STRENGTH, strBonus, 0, 0
            );

            // Apply special effects
            if (i % 5 == 0 && i != 15) { // Notable nodes
                node.withFlatHP(50).withFlatDefense(5); // +50 HP, +5 Defense
            }
            if (i == 15) { // Keystone
                node.withDamage(50)      // +50% increased damage
                    .withHPPercent(-20); // -20% max HP (trade-off)
            }

            // Connect to previous node (first node connects to start node 0)
            if (i == 1) {
                node.addConnection(0); // Connect to start node
            } else {
                node.addConnection(nodeId - 1); // Connect to previous strength node
            }

            allNodes.add(node);
            nodeId++;
        }

        // Add branching paths
        addStrengthBranches(centerX, centerY, nodeId, angle, spacing);
    }

    private void addStrengthBranches(double centerX, double centerY, int startId, double mainAngle, double spacing) {
        // Branch 1: HP focused (connects at node 3)
        double branchAngle1 = mainAngle + Math.toRadians(30);
        int branchId = startId;

        for (int i = 1; i <= 8; i++) {
            double x = centerX + Math.cos(branchAngle1) * spacing * (3 + i);
            double y = centerY + Math.sin(branchAngle1) * spacing * (3 + i);

            NodeType type = NodeType.SMALL;
            String name = "+3% Max HP";
            String desc = "Increases Maximum HP by 3%";
            int bonus = 0;

            if (i == 4) {
                type = NodeType.NOTABLE;
                name = "🛡️ Iron Constitution";
                desc = "+30 Strength\n+8% Max HP\n+5% Defense";
                bonus = 30;
            } else if (i == 8) {
                type = NodeType.KEYSTONE;
                name = "🏰 JUGGERNAUT";
                desc = "Unstoppable tank\n\n[KEYSTONE]";
            }

            TalentNode node = new TalentNode(
                branchId, name, type, x, y, desc,
                COLOR_STRENGTH, bonus, 0, 0
            );

            // Apply notable effects
            if (i == 4) {
                node.withHPPercent(8)           // +8% max HP
                    .withDefensePercent(5);     // +5% defense
            }
            // Apply keystone effects with conditional bonus
            if (i == 8) {
                node.withHPPercent(30)          // +30% max HP
                    .withDefensePercent(20)     // +20% defense
                    .withDamage(-15)            // -15% damage (defensive trade-off)
                    .withConditional(BonusCondition.FULL_HP, "+40% Defense while at full HP")
                    .withConditionalDefense(40); // Massive defense when healthy
            }

            if (i == 1) {
                node.addConnection(3); // Connect to 3rd node in main path
            } else {
                node.addConnection(branchId - 1);
            }

            allNodes.add(node);
            branchId++;
        }

        // Branch 2: Damage focused (connects at node 7)
        double branchAngle2 = mainAngle - Math.toRadians(30);
        for (int i = 1; i <= 8; i++) {
            double x = centerX + Math.cos(branchAngle2) * spacing * (7 + i);
            double y = centerY + Math.sin(branchAngle2) * spacing * (7 + i);

            NodeType type = NodeType.SMALL;
            String name = "+5% Physical Damage";
            String desc = "Increases physical damage by 5%";
            int bonus = 0;

            if (i == 4) {
                type = NodeType.NOTABLE;
                name = "⚔️ Weapon Master";
                desc = "+30 Strength\n+10% Damage\n+5% Crit Chance";
                bonus = 30;
            } else if (i == 8) {
                type = NodeType.KEYSTONE;
                name = "💀 BLOOD RAGE";
                desc = "Fury unleashed\n\n[KEYSTONE]";
            }

            TalentNode node = new TalentNode(
                branchId, name, type, x, y, desc,
                COLOR_STRENGTH, bonus, 0, 0
            );

            // Apply notable effects
            if (i == 4) {
                node.withDamage(10)             // +10% damage
                    .withCritChance(5.0);       // +5% crit chance
            }
            // Apply keystone effects with conditional bonus
            if (i == 8) {
                node.withAttackSpeed(40)    // +40% attack speed
                    .withDamage(30)         // +30% damage
                    .withLifesteal(8.0)     // +8% lifesteal (to offset HP loss)
                    .withConditional(BonusCondition.LOW_HP, "+60% Damage while below 35% HP")
                    .withConditionalDamage(60); // Berserk rage when low HP
            }

            if (i == 1) {
                node.addConnection(7); // Connect to 7th node in main path
            } else {
                node.addConnection(branchId - 1);
            }

            allNodes.add(node);
            branchId++;
        }
    }

    private void generateDexterityPath(double centerX, double centerY) {
        int nodeId = 100;
        double spacing = 80;

        // Main dexterity line going up-right
        double angle = Math.toRadians(45); // 45 degrees = up-right

        for (int i = 1; i <= 15; i++) {
            double x = centerX + Math.cos(angle) * spacing * i;
            double y = centerY + Math.sin(angle) * spacing * i;

            NodeType type = NodeType.SMALL;
            String name = "+10 Dexterity";
            String desc = "Increases Dexterity by 10";
            int dexBonus = 10;

            if (i % 5 == 0) {
                type = NodeType.NOTABLE;
                name = "🎯 Assassin's Precision";
                desc = "Deadly accuracy";
                dexBonus = 30;
            }

            if (i == 15) {
                type = NodeType.KEYSTONE;
                name = "👤 SHADOW DANCER";
                desc = "Elusive combatant\n\n[KEYSTONE]";
                dexBonus = 0;
            }

            TalentNode node = new TalentNode(
                nodeId, name, type, x, y, desc,
                COLOR_DEXTERITY, 0, dexBonus, 0
            );

            // Apply special effects
            if (i % 5 == 0 && i != 15) { // Notable nodes
                node.withCritChance(5.0).withAttackSpeed(10); // +5% crit, +10% attack speed
            }
            if (i == 15) { // Keystone
                node.withDodge(30.0)            // +30% dodge chance
                    .withCritMultiplier(50);    // +50% crit multiplier
            }

            // Connect to previous node (first node connects to start node 0)
            if (i == 1) {
                node.addConnection(0); // Connect to start node
            } else {
                node.addConnection(nodeId - 1); // Connect to previous dex node
            }

            allNodes.add(node);
            nodeId++;
        }

        // Add branching paths
        addDexterityBranches(centerX, centerY, nodeId, angle, spacing);
    }

    private void addDexterityBranches(double centerX, double centerY, int startId, double mainAngle, double spacing) {
        // Branch 1: Crit focused (connects at node 103)
        double branchAngle1 = mainAngle + Math.toRadians(30);
        int branchId = startId;

        for (int i = 1; i <= 8; i++) {
            double x = centerX + Math.cos(branchAngle1) * spacing * (3 + i);
            double y = centerY + Math.sin(branchAngle1) * spacing * (3 + i);

            NodeType type = NodeType.SMALL;
            String name = "+8% Crit Chance";
            String desc = "Increases critical strike chance by 8%";
            int bonus = 0;

            if (i == 4) {
                type = NodeType.NOTABLE;
                name = "⚡ Deadly Precision";
                desc = "+30 Dexterity\n+15% Crit Chance\n+20% Crit Damage";
                bonus = 30;
            } else if (i == 8) {
                type = NodeType.KEYSTONE;
                name = "🎯 PERFECT AIM";
                desc = "Absolute precision\n\n[KEYSTONE]";
            }

            TalentNode node = new TalentNode(
                branchId, name, type, x, y, desc,
                COLOR_DEXTERITY, 0, bonus, 0
            );

            // Apply notable effects
            if (i == 4) {
                node.withCritChance(15.0)       // +15% crit chance
                    .withCritMultiplier(20);    // +20% crit damage
            }
            // Apply keystone effects with conditional bonus
            if (i == 8) {
                node.withCritChance(25.0)       // +25% crit chance
                    .withCritMultiplier(100)    // +100% crit multiplier (2x → 3x)
                    .withDodge(-100)            // Cannot dodge (trade-off)
                    .withConditional(BonusCondition.NOT_HIT_RECENTLY, "+50% Crit Chance if not hit recently")
                    .withConditionalCrit(50);   // Bonus precision when safely positioned
            }

            if (i == 1) {
                node.addConnection(103); // Connect to 3rd dex node
            } else {
                node.addConnection(branchId - 1);
            }

            allNodes.add(node);
            branchId++;
        }

        // Branch 2: Evasion focused (connects at node 107)
        double branchAngle2 = mainAngle - Math.toRadians(30);
        for (int i = 1; i <= 8; i++) {
            double x = centerX + Math.cos(branchAngle2) * spacing * (7 + i);
            double y = centerY + Math.sin(branchAngle2) * spacing * (7 + i);

            NodeType type = NodeType.SMALL;
            String name = "+5% Dodge";
            String desc = "Increases dodge chance by 5%";
            int bonus = 0;

            if (i == 4) {
                type = NodeType.NOTABLE;
                name = "💨 Acrobatics";
                desc = "+30 Dexterity\n+15% Dodge\n+10% Movement Speed";
                bonus = 30;
            } else if (i == 8) {
                type = NodeType.KEYSTONE;
                name = "👻 PHASE SHIFT";
                desc = "Extreme evasion\n\n[KEYSTONE]";
            }

            TalentNode node = new TalentNode(
                branchId, name, type, x, y, desc,
                COLOR_DEXTERITY, 0, bonus, 0
            );

            // Apply notable effects (movement speed not implemented, use attack speed)
            if (i == 4) {
                node.withDodge(15.0)            // +15% dodge
                    .withAttackSpeed(10);       // +10% attack speed (represents movement)
            }
            // Apply keystone effects
            if (i == 8) {
                node.withDodge(40.0)            // +40% dodge
                    .withAttackSpeed(25)        // +25% attack speed
                    .withDefensePercent(-50);   // -50% defense (trade-off)
            }

            if (i == 1) {
                node.addConnection(107); // Connect to 7th dex node
            } else {
                node.addConnection(branchId - 1);
            }

            allNodes.add(node);
            branchId++;
        }
    }

    private void generateIntelligencePath(double centerX, double centerY) {
        int nodeId = 200;
        double spacing = 80;

        // Main intelligence line going down
        double angle = Math.toRadians(270); // 270 degrees = down

        for (int i = 1; i <= 15; i++) {
            double x = centerX + Math.cos(angle) * spacing * i;
            double y = centerY + Math.sin(angle) * spacing * i;

            NodeType type = NodeType.SMALL;
            String name = "+10 Intelligence";
            String desc = "Increases Intelligence by 10";
            int intBonus = 10;

            // Notable nodes at positions 4, 8, and 12 (instead of 5, 10, 15)
            if (i == 4 || i == 8 || i == 12) {
                type = NodeType.NOTABLE;
                name = "🧠 Arcane Mastery";
                desc = "Mystical power";
                intBonus = 30;
            }

            if (i == 15) {
                type = NodeType.KEYSTONE;
                name = "⚡ ARCHMAGE";
                desc = "Supreme magical prowess\n\n[KEYSTONE]";
                intBonus = 0;
            }

            TalentNode node = new TalentNode(
                nodeId, name, type, x, y, desc,
                COLOR_INTELLIGENCE, 0, 0, intBonus
            );

            // Apply special effects
            if ((i == 4 || i == 8 || i == 12) && i != 15) { // Notable nodes
                node.withCritChance(3.0)        // +3% spell crit
                    .withCritMultiplier(15);    // +15% crit damage
            }
            if (i == 15) { // Keystone
                node.withDamage(40)             // +40% increased damage
                    .withCritMultiplier(80);    // +80% crit multiplier (powerful caster)
            }

            // Connect to previous node (first node connects to start node 0)
            if (i == 1) {
                node.addConnection(0); // Connect to start node
            } else {
                node.addConnection(nodeId - 1); // Connect to previous int node
            }

            allNodes.add(node);
            nodeId++;
        }

        // Add branching paths
        addIntelligenceBranches(centerX, centerY, nodeId, angle, spacing);

        // Add hybrid connecting paths
        generateHybridPaths(centerX, centerY);
    }

    private void addIntelligenceBranches(double centerX, double centerY, int startId, double mainAngle, double spacing) {
        // Branch 1: Mana focused (connects at node 203)
        double branchAngle1 = mainAngle + Math.toRadians(35);
        int branchId = startId;

        for (int i = 1; i <= 8; i++) {
            double x = centerX + Math.cos(branchAngle1) * spacing * (3 + i);
            double y = centerY + Math.sin(branchAngle1) * spacing * (3 + i);

            NodeType type = NodeType.SMALL;
            String name = "+5% Mana";
            String desc = "Increases maximum mana by 5%";
            int bonus = 0;

            if (i == 4) {
                type = NodeType.NOTABLE;
                name = "💙 Mana Reservoir";
                desc = "+30 Intelligence\n+15% Mana\n+10% Mana Regen";
                bonus = 30;
            } else if (i == 8) {
                type = NodeType.KEYSTONE;
                name = "🌊 ELDRITCH BATTERY";
                desc = "Mana shield\n\n[KEYSTONE]";
            }

            TalentNode node = new TalentNode(
                branchId, name, type, x, y, desc,
                COLOR_INTELLIGENCE, 0, 0, bonus
            );

            // Apply notable effects (mana/regen not implemented, use HP/damage)
            if (i == 4) {
                node.withHPPercent(15)          // +15% HP (represents mana pool)
                    .withDamage(10);            // +10% damage (mana regen → more casting)
            }
            // Apply keystone effects (mana conversion is complex, give HP/defense instead)
            if (i == 8) {
                node.withHPPercent(20)          // +20% max HP (represents mana shield)
                    .withDefensePercent(15)     // +15% defense
                    .withDamage(10);            // +10% damage
            }

            if (i == 1) {
                node.addConnection(203); // Connect to 3rd int node
            } else {
                node.addConnection(branchId - 1);
            }

            allNodes.add(node);
            branchId++;
        }

        // Branch 2: Spell damage focused (connects at node 207)
        double branchAngle2 = mainAngle - Math.toRadians(35);
        for (int i = 1; i <= 8; i++) {
            double x = centerX + Math.cos(branchAngle2) * spacing * (7 + i);
            double y = centerY + Math.sin(branchAngle2) * spacing * (7 + i);

            NodeType type = NodeType.SMALL;
            String name = "+6% Spell Damage";
            String desc = "Increases spell damage by 6%";
            int bonus = 0;

            if (i == 4) {
                type = NodeType.NOTABLE;
                name = "⚡ Elemental Focus";
                desc = "+30 Intelligence\n+15% Spell Damage\n+10% Cast Speed";
                bonus = 30;
            } else if (i == 8) {
                type = NodeType.KEYSTONE;
                name = "🔮 PAIN ATTUNEMENT";
                desc = "Power through sacrifice\n\n[KEYSTONE]";
            }

            TalentNode node = new TalentNode(
                branchId, name, type, x, y, desc,
                COLOR_INTELLIGENCE, 0, 0, bonus
            );

            // Apply notable effects (cast speed → attack speed)
            if (i == 4) {
                node.withDamage(15)             // +15% spell damage
                    .withAttackSpeed(10);       // +10% cast speed (as attack speed)
            }
            // Apply keystone effects
            if (i == 8) {
                node.withDamage(50)             // +50% spell damage
                    .withCritMultiplier(50)     // +50% crit multiplier
                    .withHPPercent(-15);        // -15% max HP (sacrifice)
            }

            if (i == 1) {
                node.addConnection(207); // Connect to 7th int node
            } else {
                node.addConnection(branchId - 1);
            }

            allNodes.add(node);
            branchId++;
        }
    }

    private void generateHybridPaths(double centerX, double centerY) {
        // Hybrid path connecting STR and DEX (top of tree)
        int hybridId = 500;
        double spacing = 80;

        // STR-DEX connector (10-12 nodes in an arc)
        for (int i = 0; i < 10; i++) {
            double progress = (double) i / 9;
            double angle = Math.toRadians(135 - (progress * 90)); // Arc from STR to DEX
            double distance = spacing * 10;

            double x = centerX + Math.cos(angle) * distance;
            double y = centerY + Math.sin(angle) * distance;

            NodeType type = NodeType.SMALL;
            String name = "+5 STR, +5 DEX";
            String desc = "Balanced offensive stats";
            int strBonus = 5, dexBonus = 5;

            if (i == 5) {
                type = NodeType.NOTABLE;
                name = "⚔️🎯 Battle Reflexes";
                desc = "Swift warrior";
                strBonus = 15;
                dexBonus = 15;
            }

            TalentNode node = new TalentNode(
                hybridId, name, type, x, y, desc,
                COLOR_HYBRID, strBonus, dexBonus, 0
            );

            // Add notable effects
            if (i == 5) {
                node.withAttackSpeed(15)    // +15% attack speed
                    .withDamage(10);        // +10% damage
            }

            if (i == 0) {
                node.addConnection(10); // Connect to mid-STR path
            } else {
                node.addConnection(hybridId - 1);
            }

            if (i == 9) {
                node.addConnection(110); // Connect to mid-DEX path
            }

            allNodes.add(node);
            hybridId++;
        }

        // DEX-INT connector (right side)
        for (int i = 0; i < 10; i++) {
            double progress = (double) i / 9;
            double angle = Math.toRadians(45 - (progress * 135)); // Arc from DEX to INT
            double distance = spacing * 10;

            double x = centerX + Math.cos(angle) * distance;
            double y = centerY + Math.sin(angle) * distance;

            NodeType type = NodeType.SMALL;
            String name = "+5 DEX, +5 INT";
            String desc = "Balanced precision and magic";
            int dexBonus = 5, intBonus = 5;

            if (i == 5) {
                type = NodeType.NOTABLE;
                name = "🎯🧠 Spellblade";
                desc = "Magical precision";
                dexBonus = 15;
                intBonus = 15;
            }

            TalentNode node = new TalentNode(
                hybridId, name, type, x, y, desc,
                COLOR_HYBRID, 0, dexBonus, intBonus
            );

            // Add notable effects
            if (i == 5) {
                node.withCritChance(8.0)        // +8% spell crit
                    .withCritMultiplier(25);    // +25% crit multiplier
            }

            if (i == 0) {
                node.addConnection(110); // Connect to mid-DEX path
            } else {
                node.addConnection(hybridId - 1);
            }

            if (i == 9) {
                node.addConnection(210); // Connect to mid-INT path
            }

            allNodes.add(node);
            hybridId++;
        }

        // INT-STR connector (left side)
        for (int i = 0; i < 10; i++) {
            double progress = (double) i / 9;
            double angle = Math.toRadians(270 - (progress * 135)); // Arc from INT to STR
            double distance = spacing * 10;

            double x = centerX + Math.cos(angle) * distance;
            double y = centerY + Math.sin(angle) * distance;

            NodeType type = NodeType.SMALL;
            String name = "+5 INT, +5 STR";
            String desc = "Balanced magic and might";
            int intBonus = 5, strBonus = 5;

            if (i == 5) {
                type = NodeType.NOTABLE;
                name = "🧠💪 Battlemage";
                desc = "Magical warrior";
                intBonus = 15;
                strBonus = 15;
            }

            TalentNode node = new TalentNode(
                hybridId, name, type, x, y, desc,
                COLOR_HYBRID, strBonus, 0, intBonus
            );

            // Add notable effects
            if (i == 5) {
                node.withDamage(15)         // +15% damage
                    .withHPPercent(10);     // +10% max HP
            }

            if (i == 0) {
                node.addConnection(210); // Connect to mid-INT path
            } else {
                node.addConnection(hybridId - 1);
            }

            if (i == 9) {
                node.addConnection(10); // Connect to mid-STR path
            }

            allNodes.add(node);
            hybridId++;
        }
    }

    private void renderTree() {
        treePane.getChildren().clear();
        connectionLines.clear();

        // Draw all connection lines first (behind nodes)
        for (TalentNode node : allNodes) {
            for (int connectionId : node.getConnections()) {
                TalentNode connectedNode = getNodeById(connectionId);
                if (connectedNode != null) {
                    Line line = createConnectionLine(node, connectedNode);
                    treePane.getChildren().add(line);

                    // Store line reference for hover highlighting
                    String key = node.id + "-" + connectedNode.id;
                    connectionLines.put(key, line);
                }
            }
        }

        // Draw all nodes on top of lines
        for (TalentNode node : allNodes) {
            Pane nodeVisual = createNodeVisual(node);
            treePane.getChildren().add(nodeVisual);
        }
    }

    private Line createConnectionLine(TalentNode from, TalentNode to) {
        Line line = new Line(from.x, from.y, to.x, to.y);

        boolean fromAllocated = allocatedNodes.contains(from.id);
        boolean toAllocated = allocatedNodes.contains(to.id);

        if (fromAllocated && toAllocated) {
            // Allocated path - bright golden glow
            line.setStroke(COLOR_ALLOCATED);
            line.setStrokeWidth(4);

            DropShadow pathGlow = new DropShadow();
            pathGlow.setColor(Color.GOLD);
            pathGlow.setRadius(10);
            pathGlow.setSpread(0.5);
            line.setEffect(pathGlow);
        } else if (fromAllocated || toAllocated) {
            // Partially allocated path - dimmer highlight
            line.setStroke(Color.rgb(150, 130, 50));
            line.setStrokeWidth(2.5);
        } else {
            // Unallocated path
            line.setStroke(Color.rgb(40, 40, 40));
            line.setStrokeWidth(2);
        }

        return line;
    }

    private Pane createNodeVisual(TalentNode node) {
        StackPane nodePane = new StackPane();
        nodePane.setLayoutX(node.x - node.getRadius());
        nodePane.setLayoutY(node.y - node.getRadius());

        // Background circle
        Circle circle = new Circle(node.getRadius());

        // Determine node color and state
        boolean isAllocated = allocatedNodes.contains(node.id);
        boolean isAvailable = isNodeAvailable(node);

        if (isAllocated) {
            circle.setFill(COLOR_ALLOCATED);
            circle.setStroke(Color.GOLD);
            circle.setStrokeWidth(3);

            // Add glow effect for allocated nodes
            DropShadow glow = new DropShadow();
            glow.setColor(Color.GOLD);
            glow.setRadius(15);
            glow.setSpread(0.6);
            circle.setEffect(glow);

            // Subtle pulsing animation for allocated nodes
            ScaleTransition pulse = new ScaleTransition(Duration.seconds(1.5), circle);
            pulse.setFromX(1.0);
            pulse.setFromY(1.0);
            pulse.setToX(1.05);
            pulse.setToY(1.05);
            pulse.setCycleCount(ScaleTransition.INDEFINITE);
            pulse.setAutoReverse(true);
            pulse.play();
        } else if (isAvailable) {
            circle.setFill(COLOR_AVAILABLE);
            circle.setStroke(node.color != null ? node.color : Color.WHITE);
            circle.setStrokeWidth(2);

            // Subtle glow for available nodes
            DropShadow availableGlow = new DropShadow();
            availableGlow.setColor(node.color != null ? node.color : Color.WHITE);
            availableGlow.setRadius(8);
            availableGlow.setSpread(0.3);
            circle.setEffect(availableGlow);
        } else {
            circle.setFill(COLOR_UNALLOCATED);
            circle.setStroke(Color.rgb(80, 80, 80));
            circle.setStrokeWidth(1);
        }

        nodePane.getChildren().add(circle);

        // Add icon/text for keystone, notable, and jewel socket nodes
        if (node.type == NodeType.KEYSTONE || node.type == NodeType.NOTABLE || node.type == NodeType.JEWEL_SOCKET) {
            Label label = new Label(node.name.split(" ")[0]); // First word (emoji)
            int fontSize = node.type == NodeType.KEYSTONE ? 20 :
                          node.type == NodeType.JEWEL_SOCKET ? 18 : 14;
            label.setStyle("-fx-text-fill: white; -fx-font-size: " + fontSize + "px;");
            nodePane.getChildren().add(label);
        }

        // Tooltip
        Tooltip tooltip = new Tooltip(node.getTooltipText());
        tooltip.setShowDelay(Duration.millis(100));
        tooltip.setStyle("-fx-font-size: 13px; -fx-background-color: #1a1a2e; -fx-text-fill: white;");
        Tooltip.install(nodePane, tooltip);

        // Click handler
        nodePane.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                handleNodeClick(node);
            } else if (event.getButton() == MouseButton.SECONDARY) {
                // Right-click: Handle jewel sockets or deallocation
                if (node.type == NodeType.JEWEL_SOCKET && allocatedNodes.contains(node.id)) {
                    handleJewelSocketInteraction(node);
                } else {
                    handleNodeDeallocation(node);
                }
            }
        });

        // Enhanced hover effect with connection highlighting
        nodePane.setOnMouseEntered(event -> {
            if (!isAllocated) {
                circle.setScaleX(1.15);
                circle.setScaleY(1.15);
            }
            // Highlight connections
            highlightNodeConnections(node, true);
        });

        nodePane.setOnMouseExited(event -> {
            circle.setScaleX(1.0);
            circle.setScaleY(1.0);
            // Remove highlight from connections
            highlightNodeConnections(node, false);
        });

        return nodePane;
    }

    private boolean isNodeAvailable(TalentNode node) {
        // Start node is always allocated
        if (node.id == 0) return false;

        // Already allocated
        if (allocatedNodes.contains(node.id)) return false;

        // Check if any connected node is allocated
        for (int connectionId : node.getConnections()) {
            if (allocatedNodes.contains(connectionId)) {
                return true;
            }
        }

        return false;
    }

    private void highlightNodeConnections(TalentNode node, boolean highlight) {
        Color highlightColor = highlight ? Color.rgb(255, 255, 150) : null;
        double highlightWidth = highlight ? 4.0 : -1;

        for (int connectionId : node.getConnections()) {
            TalentNode connectedNode = getNodeById(connectionId);
            if (connectedNode != null) {
                String key = node.id + "-" + connectedNode.id;
                Line line = connectionLines.get(key);

                if (line != null && highlight) {
                    // Apply highlight
                    line.setStroke(highlightColor);
                    line.setStrokeWidth(highlightWidth);

                    // Add glow effect
                    DropShadow glow = new DropShadow();
                    glow.setColor(Color.rgb(255, 255, 100));
                    glow.setRadius(10);
                    glow.setSpread(0.5);
                    line.setEffect(glow);
                } else if (line != null) {
                    // Remove highlight - restore original appearance
                    boolean fromAllocated = allocatedNodes.contains(node.id);
                    boolean toAllocated = allocatedNodes.contains(connectedNode.id);

                    if (fromAllocated && toAllocated) {
                        line.setStroke(Color.rgb(255, 215, 0));
                        line.setStrokeWidth(3);
                    } else if (fromAllocated || toAllocated) {
                        line.setStroke(Color.rgb(150, 130, 50));
                        line.setStrokeWidth(2.5);
                    } else {
                        line.setStroke(Color.rgb(40, 40, 40));
                        line.setStrokeWidth(2);
                    }
                    line.setEffect(null);
                }
            }
        }
    }

    private void handleNodeClick(TalentNode node) {
        if (allocatedNodes.contains(node.id)) {
            // TODO: Implement deallocation
            System.out.println("Node already allocated: " + node.name);
            return;
        }

        if (!isNodeAvailable(node)) {
            System.out.println("Node not available: " + node.name);
            return;
        }

        if (hero.getPassivePoints() <= 0) {
            System.out.println("No passive points available!");
            return;
        }

        // Allocate node
        allocatedNodes.add(node.id);
        hero.decreasePassivePoints(1);

        // Apply node bonuses
        if (node.strBonus > 0) hero.increaseStrength(node.strBonus);
        if (node.dexBonus > 0) hero.increaseDexterity(node.dexBonus);
        if (node.intBonus > 0) hero.increaseIntelligence(node.intBonus);

        // Apply advanced bonuses
        if (node.critChanceBonus > 0) hero.modifyTalentCritChance(node.critChanceBonus);
        if (node.critMultiplierBonus > 0) hero.modifyTalentCritMultiplier(node.critMultiplierBonus);
        if (node.lifestealBonus > 0) hero.modifyTalentLifesteal(node.lifestealBonus);
        if (node.dodgeBonus > 0) hero.modifyTalentDodge(node.dodgeBonus);
        if (node.attackSpeedBonus > 0) hero.modifyTalentAttackSpeed(node.attackSpeedBonus);
        if (node.damageBonus > 0) hero.modifyTalentDamageBonus(node.damageBonus);
        if (node.flatHPBonus > 0) hero.modifyTalentFlatHP(node.flatHPBonus);
        if (node.flatDefenseBonus > 0) hero.modifyTalentFlatDefense(node.flatDefenseBonus);
        if (node.hpPercentBonus > 0) hero.modifyTalentHPPercent(node.hpPercentBonus);
        if (node.defensePercentBonus > 0) hero.modifyTalentDefensePercent(node.defensePercentBonus);

        // Save to hero
        hero.setAllocatedTalentNodes(new HashSet<>(allocatedNodes));

        // Recalculate conditional bonuses
        recalculateConditionalBonuses();

        // Update UI with animation
        updateLabels();
        renderTree(); // Full re-render with new allocated state

        System.out.println("Allocated node: " + node.name);
    }

    private void handleNodeDeallocation(TalentNode node) {
        // Can't deallocate start node
        if (node.id == 0) {
            System.out.println("Cannot deallocate start node!");
            return;
        }

        // Check if node is allocated
        if (!allocatedNodes.contains(node.id)) {
            System.out.println("Node not allocated: " + node.name);
            return;
        }

        // Check if any other allocated nodes depend on this one
        // (i.e., they're only connected through this node)
        if (hasNodesDependingOn(node.id)) {
            System.out.println("Cannot deallocate: other nodes depend on this path!");
            return;
        }

        // Deallocate node
        allocatedNodes.remove(node.id);
        hero.increasePassivePoints(1);

        // Remove node bonuses
        if (node.strBonus > 0) hero.increaseStrength(-node.strBonus);
        if (node.dexBonus > 0) hero.increaseDexterity(-node.dexBonus);
        if (node.intBonus > 0) hero.increaseIntelligence(-node.intBonus);

        // Remove advanced bonuses
        if (node.critChanceBonus > 0) hero.modifyTalentCritChance(-node.critChanceBonus);
        if (node.critMultiplierBonus > 0) hero.modifyTalentCritMultiplier(-node.critMultiplierBonus);
        if (node.lifestealBonus > 0) hero.modifyTalentLifesteal(-node.lifestealBonus);
        if (node.dodgeBonus > 0) hero.modifyTalentDodge(-node.dodgeBonus);
        if (node.attackSpeedBonus > 0) hero.modifyTalentAttackSpeed(-node.attackSpeedBonus);
        if (node.damageBonus > 0) hero.modifyTalentDamageBonus(-node.damageBonus);
        if (node.flatHPBonus > 0) hero.modifyTalentFlatHP(-node.flatHPBonus);
        if (node.flatDefenseBonus > 0) hero.modifyTalentFlatDefense(-node.flatDefenseBonus);
        if (node.hpPercentBonus > 0) hero.modifyTalentHPPercent(-node.hpPercentBonus);
        if (node.defensePercentBonus > 0) hero.modifyTalentDefensePercent(-node.defensePercentBonus);

        // Save to hero
        hero.setAllocatedTalentNodes(new HashSet<>(allocatedNodes));

        // Recalculate conditional bonuses
        recalculateConditionalBonuses();

        // Update UI
        updateLabels();
        renderTree();

        System.out.println("Deallocated node: " + node.name);
    }

    /**
     * Handles jewel socket interaction (insert/remove jewels)
     */
    private void handleJewelSocketInteraction(TalentNode node) {
        if (node.type != NodeType.JEWEL_SOCKET) {
            return;
        }

        if (node.hasSocketedJewel) {
            // Remove jewel
            showRemoveJewelDialog(node);
        } else {
            // Insert jewel
            showInsertJewelDialog(node);
        }
    }

    /**
     * Shows dialog to insert a jewel into socket
     */
    private void showInsertJewelDialog(TalentNode node) {
        List<Jewel> availableJewels = hero.getAvailableJewels();

        if (availableJewels.isEmpty()) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("No Jewels Available");
            alert.setHeaderText("💎 No Jewels to Socket");
            alert.setContentText("You don't have any jewels in your inventory.\n\nJewels can be obtained from:\n• Combat loot drops\n• Shop purchases\n• Quest rewards");
            alert.showAndWait();
            return;
        }

        // Create dialog with jewel selection
        Dialog<Jewel> dialog = new Dialog<>();
        dialog.setTitle("Socket Jewel");
        dialog.setHeaderText("💎 Select a jewel to socket");

        // Create list view of jewels
        ListView<Jewel> jewelListView = new ListView<>();
        jewelListView.getItems().addAll(availableJewels);
        jewelListView.setPrefHeight(300);

        // Custom cell factory to display jewel info
        jewelListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Jewel jewel, boolean empty) {
                super.updateItem(jewel, empty);
                if (empty || jewel == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox jewelBox = new VBox(5);
                    jewelBox.setPadding(new Insets(5));

                    // Jewel header
                    Label nameLabel = new Label(jewel.getType().getIcon() + " " + jewel.getName());
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                    // Jewel details
                    Label detailsLabel = new Label(String.format("%s | Lv%d | %d mods",
                            jewel.getRarity().getDisplayName(),
                            jewel.getRequiredLevel(),
                            jewel.getModifiers().size()));
                    detailsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888888;");

                    // Modifiers
                    Label modsLabel = new Label(jewel.getModifiersDescription());
                    modsLabel.setStyle("-fx-font-size: 11px;");

                    jewelBox.getChildren().addAll(nameLabel, detailsLabel, modsLabel);
                    setGraphic(jewelBox);
                }
            }
        });

        // Buttons
        ButtonType socketButtonType = new ButtonType("Socket Jewel", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(socketButtonType, cancelButtonType);

        // Content
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.getChildren().addAll(
                new Label("Available Jewels (" + availableJewels.size() + "):"),
                jewelListView
        );
        dialog.getDialogPane().setContent(content);

        // Result converter
        dialog.setResultConverter(buttonType -> {
            if (buttonType == socketButtonType) {
                return jewelListView.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        // Show dialog and handle result
        dialog.showAndWait().ifPresent(selectedJewel -> {
            if (selectedJewel != null) {
                socketJewel(node, selectedJewel);
            }
        });
    }

    /**
     * Shows dialog to remove a jewel from socket
     */
    private void showRemoveJewelDialog(TalentNode node) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Remove Jewel");
        alert.setHeaderText("💎 Remove Socketed Jewel?");
        alert.setContentText("Remove " + node.socketedJewelName + " from this socket?\n\nThe jewel will be returned to your inventory.");

        ButtonType removeButton = new ButtonType("Remove Jewel");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(removeButton, cancelButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == removeButton) {
                unsocketJewel(node);
            }
        });
    }

    /**
     * Sockets a jewel into a node
     */
    private void socketJewel(TalentNode node, Jewel jewel) {
        // Mark jewel as socketed
        jewel.setSocketed(true);

        // Update node
        node.hasSocketedJewel = true;
        node.socketedJewelName = jewel.getName();
        node.jewelBonuses = new HashMap<>(jewel.getModifiers());

        // Apply jewel bonuses to hero
        applyJewelBonuses(jewel.getModifiers(), 1);

        // Update UI
        updateLabels();
        renderTree();

        System.out.println("💎 Socketed " + jewel.getName() + " into talent tree!");
    }

    /**
     * Unsockets a jewel from a node
     */
    private void unsocketJewel(TalentNode node) {
        // Find the jewel
        Jewel jewel = hero.findJewelByName(node.socketedJewelName);
        if (jewel != null) {
            // Mark jewel as unsocketed
            jewel.setSocketed(false);

            // Remove jewel bonuses from hero
            applyJewelBonuses(node.jewelBonuses, -1);
        }

        // Clear node
        node.hasSocketedJewel = false;
        node.socketedJewelName = "";
        node.jewelBonuses.clear();

        // Update UI
        updateLabels();
        renderTree();

        System.out.println("💎 Removed jewel from socket!");
    }

    /**
     * Applies or removes jewel bonuses to/from hero
     */
    private void applyJewelBonuses(Map<String, Double> bonuses, int multiplier) {
        for (Map.Entry<String, Double> entry : bonuses.entrySet()) {
            String stat = entry.getKey();
            double value = entry.getValue() * multiplier;

            switch (stat) {
                case "hp_percent":
                    hero.modifyTalentHPPercent(value);
                    break;
                case "defense_percent":
                    hero.modifyTalentDefensePercent(value);
                    break;
                case "damage_percent":
                    hero.modifyTalentDamageBonus(value);
                    break;
                case "crit_chance":
                    hero.modifyTalentCritChance(value);
                    break;
                case "crit_multiplier":
                    hero.modifyTalentCritMultiplier(value);
                    break;
                case "dodge_chance":
                    hero.modifyTalentDodge(value);
                    break;
                case "attack_speed":
                    hero.modifyTalentAttackSpeed(value);
                    break;
                case "lifesteal":
                    hero.modifyTalentLifesteal(value);
                    break;
                case "str_bonus":
                    hero.increaseStrength((int) value);
                    break;
                case "dex_bonus":
                    hero.increaseDexterity((int) value);
                    break;
                case "int_bonus":
                    hero.increaseIntelligence((int) value);
                    break;
                case "all_stats":
                    int statBonus = (int) value;
                    hero.increaseStrength(statBonus);
                    hero.increaseDexterity(statBonus);
                    hero.increaseIntelligence(statBonus);
                    break;
                // Note: gold_find and exp_bonus would need to be added to Erou if desired
            }
        }
    }

    private boolean hasNodesDependingOn(int nodeId) {
        // Find all nodes that are allocated
        for (int allocatedId : allocatedNodes) {
            if (allocatedId == nodeId || allocatedId == 0) continue;

            TalentNode allocatedNode = getNodeById(allocatedId);
            if (allocatedNode == null) continue;

            // Check if this allocated node would become unreachable
            // if we remove nodeId
            Set<Integer> tempAllocated = new HashSet<>(allocatedNodes);
            tempAllocated.remove(nodeId);

            if (!isNodeReachable(allocatedNode, tempAllocated)) {
                return true; // This node depends on the one we're trying to remove
            }
        }

        return false;
    }

    private boolean isNodeReachable(TalentNode node, Set<Integer> allocated) {
        // BFS to check if node is reachable from start node (0)
        Set<Integer> visited = new HashSet<>();
        List<Integer> queue = new ArrayList<>();
        queue.add(0);
        visited.add(0);

        while (!queue.isEmpty()) {
            int currentId = queue.remove(0);

            if (currentId == node.id) {
                return true; // Found the node!
            }

            TalentNode current = getNodeById(currentId);
            if (current == null) continue;

            // Explore connections
            for (int connectedId : current.getConnections()) {
                if (!visited.contains(connectedId) && allocated.contains(connectedId)) {
                    visited.add(connectedId);
                    queue.add(connectedId);
                }
            }
        }

        return false; // Node not reachable
    }

    private void updateLabels() {
        passivePointsLabel.setText("Passive Points: " + hero.getPassivePoints());
        allocatedNodesLabel.setText("Allocated Nodes: " + (allocatedNodes.size() - 1)); // -1 for start node
        updateStatSummary();
    }

    private void updateStatSummary() {
        int totalStr = 0, totalDex = 0, totalInt = 0;
        double totalCrit = 0, totalDodge = 0, totalDamage = 0;
        double totalHP = 0, totalDefense = 0;
        int socketedJewels = 0;

        for (int nodeId : allocatedNodes) {
            if (nodeId == 0) continue; // Skip start node

            TalentNode node = getNodeById(nodeId);
            if (node != null) {
                totalStr += node.strBonus;
                totalDex += node.dexBonus;
                totalInt += node.intBonus;
                totalCrit += node.critChanceBonus;
                totalDodge += node.dodgeBonus;
                totalDamage += node.damageBonus;
                totalHP += node.hpPercentBonus;
                totalDefense += node.defensePercentBonus;

                // Count socketed jewels and add their bonuses
                if (node.type == NodeType.JEWEL_SOCKET && node.hasSocketedJewel) {
                    socketedJewels++;
                    for (Map.Entry<String, Double> bonus : node.jewelBonuses.entrySet()) {
                        String stat = bonus.getKey();
                        double value = bonus.getValue();

                        switch (stat) {
                            case "hp_percent" -> totalHP += value;
                            case "defense_percent" -> totalDefense += value;
                            case "damage_percent" -> totalDamage += value;
                            case "crit_chance" -> totalCrit += value;
                            case "dodge_chance" -> totalDodge += value;
                            case "str_bonus" -> totalStr += (int) value;
                            case "dex_bonus" -> totalDex += (int) value;
                            case "int_bonus" -> totalInt += (int) value;
                            case "all_stats" -> {
                                int statBonus = (int) value;
                                totalStr += statBonus;
                                totalDex += statBonus;
                                totalInt += statBonus;
                            }
                        }
                    }
                }
            }
        }

        StringBuilder summary = new StringBuilder();
        summary.append(String.format("💪 +%d STR  |  🎯 +%d DEX  |  🧠 +%d INT", totalStr, totalDex, totalInt));

        if (totalDamage > 0) summary.append(String.format("  |  ⚔️ +%.0f%% DMG", totalDamage));
        if (totalHP > 0) summary.append(String.format("  |  ❤️ +%.0f%% HP", totalHP));
        if (totalDefense > 0) summary.append(String.format("  |  🛡️ +%.0f%% DEF", totalDefense));
        if (totalCrit > 0) summary.append(String.format("  |  ⚡ +%.0f%% CRIT", totalCrit));
        if (totalDodge > 0) summary.append(String.format("  |  💨 +%.0f%% DODGE", totalDodge));

        if (socketedJewels > 0) {
            summary.append(String.format("  |  💎 %d Jewel%s", socketedJewels, socketedJewels > 1 ? "s" : ""));
        }

        if (totalStatsLabel != null) {
            totalStatsLabel.setText(summary.toString());
        }
    }

    private TalentNode getNodeById(int id) {
        for (TalentNode node : allNodes) {
            if (node.id == id) return node;
        }
        return null;
    }

    private HBox createControls() {
        HBox controls = new HBox(15);
        controls.setPadding(new Insets(15));
        controls.setAlignment(Pos.CENTER);
        controls.setStyle("-fx-background-color: #1a1a2e;");

        Button resetButton = new Button("🔄 Reset Tree");
        styleButton(resetButton, "#e74c3c");
        resetButton.setOnAction(e -> resetTree());

        // Zoom controls
        Button zoomOutButton = new Button("🔍−");
        styleButton(zoomOutButton, "#555");
        zoomOutButton.setOnAction(e -> adjustZoom(0.9));

        Button zoomResetButton = new Button("100%");
        styleButton(zoomResetButton, "#555");
        zoomResetButton.setOnAction(e -> adjustZoom(1.0 / currentZoom));

        Button zoomInButton = new Button("🔍+");
        styleButton(zoomInButton, "#555");
        zoomInButton.setOnAction(e -> adjustZoom(1.1));

        HBox zoomBox = new HBox(5);
        zoomBox.getChildren().addAll(zoomOutButton, zoomResetButton, zoomInButton);

        Button closeButton = new Button("✓ Done");
        styleButton(closeButton, "#27ae60");
        closeButton.setOnAction(e -> {
            if (onBackCallback != null) {
                onBackCallback.run();
            } else {
                stage.close();
            }
        });

        Label helpLabel = new Label("Left-click: Allocate | Right-click: Deallocate | Mouse Wheel: Zoom | Drag: Pan");
        helpLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        controls.getChildren().addAll(resetButton, zoomBox, spacer1, helpLabel, spacer2, closeButton);
        return controls;
    }

    private void adjustZoom(double factor) {
        double newZoom = currentZoom * factor;

        // Limit zoom range
        if (newZoom >= 0.5 && newZoom <= 2.0) {
            currentZoom = newZoom;
            treeContainer.setScaleX(currentZoom);
            treeContainer.setScaleY(currentZoom);
        }
    }

    private void styleButton(Button button, String color) {
        button.setStyle(
            "-fx-background-color: " + color + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 5;"
        );

        button.setOnMouseEntered(e -> button.setOpacity(0.8));
        button.setOnMouseExited(e -> button.setOpacity(1.0));
    }

    private void resetTree() {
        // Calculate total bonuses from allocated nodes (excluding start node)
        int totalStrBonus = 0, totalDexBonus = 0, totalIntBonus = 0;
        double totalCrit = 0, totalCritMult = 0, totalLifesteal = 0, totalDodge = 0;
        double totalAttackSpeed = 0, totalDamage = 0, totalHPPercent = 0, totalDefPercent = 0;
        int totalFlatHP = 0, totalFlatDef = 0;
        int refundPoints = 0;

        for (int nodeId : allocatedNodes) {
            if (nodeId == 0) continue; // Skip start node

            TalentNode node = getNodeById(nodeId);
            if (node != null) {
                totalStrBonus += node.strBonus;
                totalDexBonus += node.dexBonus;
                totalIntBonus += node.intBonus;
                totalCrit += node.critChanceBonus;
                totalCritMult += node.critMultiplierBonus;
                totalLifesteal += node.lifestealBonus;
                totalDodge += node.dodgeBonus;
                totalAttackSpeed += node.attackSpeedBonus;
                totalDamage += node.damageBonus;
                totalFlatHP += node.flatHPBonus;
                totalFlatDef += node.flatDefenseBonus;
                totalHPPercent += node.hpPercentBonus;
                totalDefPercent += node.defensePercentBonus;
                refundPoints++;
            }
        }

        // Remove all stat bonuses
        if (totalStrBonus > 0) hero.increaseStrength(-totalStrBonus);
        if (totalDexBonus > 0) hero.increaseDexterity(-totalDexBonus);
        if (totalIntBonus > 0) hero.increaseIntelligence(-totalIntBonus);

        // Remove all advanced bonuses
        if (totalCrit > 0) hero.modifyTalentCritChance(-totalCrit);
        if (totalCritMult > 0) hero.modifyTalentCritMultiplier(-totalCritMult);
        if (totalLifesteal > 0) hero.modifyTalentLifesteal(-totalLifesteal);
        if (totalDodge > 0) hero.modifyTalentDodge(-totalDodge);
        if (totalAttackSpeed > 0) hero.modifyTalentAttackSpeed(-totalAttackSpeed);
        if (totalDamage > 0) hero.modifyTalentDamageBonus(-totalDamage);
        if (totalFlatHP > 0) hero.modifyTalentFlatHP(-totalFlatHP);
        if (totalFlatDef > 0) hero.modifyTalentFlatDefense(-totalFlatDef);
        if (totalHPPercent > 0) hero.modifyTalentHPPercent(-totalHPPercent);
        if (totalDefPercent > 0) hero.modifyTalentDefensePercent(-totalDefPercent);

        // Refund passive points
        hero.increasePassivePoints(refundPoints);

        // Clear all allocated nodes except start node
        allocatedNodes.clear();
        allocatedNodes.add(0);

        // Save to hero
        hero.setAllocatedTalentNodes(new HashSet<>(allocatedNodes));

        // Clear conditional bonuses
        recalculateConditionalBonuses();

        // Update UI
        updateLabels();
        renderTree();

        System.out.println("🔄 Tree reset! Refunded " + refundPoints + " passive points.");
    }

    /**
     * Recalculate all conditional bonuses from allocated nodes
     */
    private void recalculateConditionalBonuses() {
        // Reset all conditional bonuses
        hero.setConditionalDamage_FullHP(0);
        hero.setConditionalDamage_LowHP(0);
        hero.setConditionalDefense_FullHP(0);
        hero.setConditionalCrit_NotHitRecently(0);

        // Sum up conditional bonuses from all allocated nodes
        for (int nodeId : allocatedNodes) {
            TalentNode node = getNodeById(nodeId);
            if (node != null && node.condition != BonusCondition.ALWAYS) {
                // Add conditional bonuses based on condition type
                switch (node.condition) {
                    case FULL_HP:
                        if (node.conditionalDamage > 0) {
                            hero.setConditionalDamage_FullHP(hero.getConditionalDamageBonus() + node.conditionalDamage);
                        }
                        if (node.conditionalDefense > 0) {
                            hero.setConditionalDefense_FullHP(hero.getConditionalDefenseBonus() + node.conditionalDefense);
                        }
                        break;
                    case LOW_HP:
                        if (node.conditionalDamage > 0) {
                            hero.setConditionalDamage_LowHP(node.conditionalDamage);
                        }
                        break;
                    case NOT_HIT_RECENTLY:
                        if (node.conditionalCrit > 0) {
                            hero.setConditionalCrit_NotHitRecently(node.conditionalCrit);
                        }
                        break;
                    // Add more cases as needed
                }
            }
        }
    }

    // ==================== INNER CLASSES ====================

    enum NodeType {
        START, SMALL, NOTABLE, KEYSTONE, JEWEL_SOCKET, ASCENDANCY
    }

    enum BonusCondition {
        ALWAYS("Always active"),
        FULL_HP("While at full HP"),
        LOW_HP("While below 35% HP"),
        RECENTLY_HIT("If hit recently (4s)"),
        NOT_HIT_RECENTLY("If not hit recently (4s)"),
        MOVING("While moving"),
        STATIONARY("While stationary (1s)"),
        ON_KILL("On kill (4s)"),
        BOSS_FIGHT("During boss fights"),
        SOLO("While solo"),
        WITH_ALLY("With an ally nearby");

        final String description;

        BonusCondition(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    static class TalentNode {
        int id;
        String name;
        NodeType type;
        double x, y;
        String description;
        Color color;
        int strBonus, dexBonus, intBonus;

        // Advanced bonuses
        double critChanceBonus = 0;      // % critical strike chance
        double critMultiplierBonus = 0;  // % critical damage multiplier
        double lifestealBonus = 0;       // % lifesteal
        double dodgeBonus = 0;           // % dodge chance
        double attackSpeedBonus = 0;     // % attack speed
        double damageBonus = 0;          // % increased damage
        int flatHPBonus = 0;             // flat max HP
        int flatDefenseBonus = 0;        // flat defense
        double hpPercentBonus = 0;       // % increased max HP
        double defensePercentBonus = 0;  // % increased defense

        // Conditional bonuses
        BonusCondition condition = BonusCondition.ALWAYS;
        double conditionalDamage = 0;    // % conditional damage
        double conditionalDefense = 0;   // % conditional defense
        double conditionalCrit = 0;      // % conditional crit chance
        double conditionalDodge = 0;     // % conditional dodge
        String conditionalDescription = ""; // Custom description for tooltip

        // Jewel socket data
        boolean hasSocketedJewel = false;
        String socketedJewelName = "";
        Map<String, Double> jewelBonuses = new HashMap<>(); // Bonuses from socketed jewel

        List<Integer> connections = new ArrayList<>();

        TalentNode(int id, String name, NodeType type, double x, double y,
                   String description, Color color, int strBonus, int dexBonus, int intBonus) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.x = x;
            this.y = y;
            this.description = description;
            this.color = color;
            this.strBonus = strBonus;
            this.dexBonus = dexBonus;
            this.intBonus = intBonus;
        }

        // Builder methods for advanced bonuses
        TalentNode withCritChance(double value) { this.critChanceBonus = value; return this; }
        TalentNode withCritMultiplier(double value) { this.critMultiplierBonus = value; return this; }
        TalentNode withLifesteal(double value) { this.lifestealBonus = value; return this; }
        TalentNode withDodge(double value) { this.dodgeBonus = value; return this; }
        TalentNode withAttackSpeed(double value) { this.attackSpeedBonus = value; return this; }
        TalentNode withDamage(double value) { this.damageBonus = value; return this; }
        TalentNode withFlatHP(int value) { this.flatHPBonus = value; return this; }
        TalentNode withFlatDefense(int value) { this.flatDefenseBonus = value; return this; }
        TalentNode withHPPercent(double value) { this.hpPercentBonus = value; return this; }
        TalentNode withDefensePercent(double value) { this.defensePercentBonus = value; return this; }

        // Builder methods for conditional bonuses
        TalentNode withConditional(BonusCondition condition, String description) {
            this.condition = condition;
            this.conditionalDescription = description;
            return this;
        }
        TalentNode withConditionalDamage(double value) { this.conditionalDamage = value; return this; }
        TalentNode withConditionalDefense(double value) { this.conditionalDefense = value; return this; }
        TalentNode withConditionalCrit(double value) { this.conditionalCrit = value; return this; }
        TalentNode withConditionalDodge(double value) { this.conditionalDodge = value; return this; }

        void addConnection(int nodeId) {
            if (!connections.contains(nodeId)) {
                connections.add(nodeId);
            }
        }

        List<Integer> getConnections() {
            return connections;
        }

        double getRadius() {
            return switch (type) {
                case KEYSTONE -> NODE_KEYSTONE_RADIUS;
                case NOTABLE -> NODE_NOTABLE_RADIUS;
                case JEWEL_SOCKET -> NODE_NOTABLE_RADIUS; // Same size as notable
                case ASCENDANCY -> NODE_KEYSTONE_RADIUS; // Same size as keystone
                default -> NODE_SMALL_RADIUS;
            };
        }

        String getTooltipText() {
            StringBuilder sb = new StringBuilder();
            sb.append(name).append("\n\n");
            sb.append(description);

            // Basic stats
            if (strBonus > 0) sb.append("\n+").append(strBonus).append(" Strength");
            if (dexBonus > 0) sb.append("\n+").append(dexBonus).append(" Dexterity");
            if (intBonus > 0) sb.append("\n+").append(intBonus).append(" Intelligence");

            // Advanced bonuses
            if (critChanceBonus > 0) sb.append("\n+").append(String.format("%.1f", critChanceBonus)).append("% Critical Strike Chance");
            if (critMultiplierBonus > 0) sb.append("\n+").append(String.format("%.0f", critMultiplierBonus)).append("% Critical Strike Multiplier");
            if (lifestealBonus > 0) sb.append("\n+").append(String.format("%.1f", lifestealBonus)).append("% Lifesteal");
            if (dodgeBonus > 0) sb.append("\n+").append(String.format("%.1f", dodgeBonus)).append("% Dodge Chance");
            if (attackSpeedBonus > 0) sb.append("\n+").append(String.format("%.0f", attackSpeedBonus)).append("% Attack Speed");
            if (damageBonus > 0) sb.append("\n+").append(String.format("%.0f", damageBonus)).append("% Increased Damage");
            if (flatHPBonus > 0) sb.append("\n+").append(flatHPBonus).append(" Maximum Life");
            if (flatDefenseBonus > 0) sb.append("\n+").append(flatDefenseBonus).append(" Defense");
            if (hpPercentBonus > 0) sb.append("\n+").append(String.format("%.0f", hpPercentBonus)).append("% Increased Maximum Life");
            if (defensePercentBonus > 0) sb.append("\n+").append(String.format("%.0f", defensePercentBonus)).append("% Increased Defense");

            // Conditional bonuses
            if (condition != BonusCondition.ALWAYS && !conditionalDescription.isEmpty()) {
                sb.append("\n\n🔮 CONDITIONAL EFFECT:");
                sb.append("\n").append(conditionalDescription);
                sb.append("\n(").append(condition.getDescription()).append(")");
            }

            // Jewel socket info
            if (type == NodeType.JEWEL_SOCKET) {
                if (hasSocketedJewel) {
                    sb.append("\n\n💎 SOCKETED: ").append(socketedJewelName);
                    sb.append("\n(Right-click to remove jewel)");
                } else {
                    sb.append("\n\n💎 Empty Socket");
                    sb.append("\n(Right-click to insert jewel)");
                }
            }

            return sb.toString();
        }
    }
}
