module com.rpg {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.desktop;

    // Exportă package-urile principale
    exports com.rpg;
    exports com.rpg.controller;

    // Când adaugi model și service, decomentează:
    // exports com.rpg.model.characters;
    // exports com.rpg.model.characters.classes;
    // exports com.rpg.model.items;
    // exports com.rpg.service;
}