package com.rpg.utils;

import com.rpg.model.characters.Inamic;

public class EnemyOneLiners {

    public static void displayEnemyAttackLine(Inamic inamic) {
        String line = getAttackLineForEnemy(inamic.getNume());
        if (line != null) {
            System.out.println("\n💢 " + line);
            pause(600);
        }
    }

    private static String getAttackLineForEnemy(String enemyName) {
        // NIVELE 1-10
        if (enemyName.contains("Cerșetor")) return "Domle, un leu... sau îți dau în cap!";
        if (enemyName.contains("Manelist")) return "♫ Hop am aparut, cum apare curcubeu ....! ♫";
        if (enemyName.contains("Maidanez")) return "*MEOW NIGGA!";
        if (enemyName.contains("Interlop")) return "Bă, tu știi cu cine vorbești?!";
        if (enemyName.contains("Lăutar")) return "Dă-mi banii sau îți cânt manele!";
        if (enemyName.contains("Karen")) return "VREAU SĂ VORBESC CU MANAGERUL!";
        if (enemyName.contains("Vecin")) return "BRRRRRR! (zgomot de bormașină)";

        // NIVELE 11-20
        if (enemyName.contains("ANAF")) return "AMENDĂ! Daune de 50,000 RON!";
        if (enemyName.contains("Profesor")) return "Te pun în restanță pentru totdeauna!";
        if (enemyName.contains("Jandarm")) return "STOP! Ordin de dispersare!";
        if (enemyName.contains("Controlor")) return "LEGITIMAȚIE! Amendă 200 lei!";
        if (enemyName.contains("Polițist")) return "Dă șpaga sau te duc la secție!";
        if (enemyName.contains("Kaufland")) return "AI FURAT! Cheamă securitatea!";

        // NIVELE 21-30
        if (enemyName.contains("Ceaușescu")) return "Trădătorilor de patrie!";
        if (enemyName.contains("Vadim")) return "AFARĂ DIN ȚARĂ, HOȚILOR!";
        if (enemyName.contains("Iliescu")) return "Refuz să mor, bă!";
        if (enemyName.contains("Videanu")) return "*sunet de lopată* DIN BUCUREȘTIUL MEU!";
        if (enemyName.contains("Basescu")) return "*hickup* Muie la tine!";
        if (enemyName.contains("Dragnea")) return "Dosarele mele te distrug!";
        if (enemyName.contains("Becali")) return "Cu ingerașii mei te fac praf!";

        // NIVELE 31-40
        if (enemyName.contains("PSD-ist")) return "Pensia mea e mai mare ca salariul tău!";
        if (enemyName.contains("Groapă")) return "*te înghite în groapă*";
        if (enemyName.contains("TIR")) return "*nu are RCA* CRASH!";
        if (enemyName.contains("Colectiv")) return "...";  // too dark
        if (enemyName.contains("Cutremur")) return "*tremur 7.5* TOTUL CADE!";
        if (enemyName.contains("Latrina")) return "BREAKING NEWS: Mințile noastre!";

        // NIVELE 41-50
        if (enemyName.contains("ANAF")) return "AUDIT FINAL! 100% din venit confiscat!";
        if (enemyName.contains("Antena 3")) return "BREAKING: Adevărul nu mai există!";
        if (enemyName.contains("PSD Cu Majoritate")) return "Democrația a murit!";
        if (enemyName.contains("Iliescu Level 999")) return "REFUZ SĂ MOR! REFUZ!";

        return null;
    }

    private static void pause(int ms) {
        try { Thread.sleep(ms); } catch (Exception e) {}
    }
}