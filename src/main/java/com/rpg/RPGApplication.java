package com.rpg;

import com.rpg.controller.MainMenuController;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Clasa principală JavaFX pentru RPG Românesc
 * Aceasta este entry point-ul aplicației
 */
public class RPGApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Setează titlul ferestrei
        primaryStage.setTitle("RPG Românesc: Legenda din Bucale");

        // Creează și afișează meniul principal
        MainMenuController mainMenu = new MainMenuController(primaryStage);
        primaryStage.setScene(mainMenu.createScene());

        // Previne redimensionarea (opțional)
         primaryStage.setResizable(false);

        // Afișează fereastra
        primaryStage.show();
    }

}