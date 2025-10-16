package com.rpg.service;

import com.rpg.model.characters.Erou;
import com.rpg.service.dto.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * TavernServiceFX - Serviciu pentru tavernă (odihnă, gambling, quests)
 * Refactorizat pentru JavaFX
 */
public class TavernServiceFX {

    private static final int REST_COST = 50;
    private static final int GAMBLE_MIN_BET = 10;
    private static final int GAMBLE_MAX_BET = 500;

    private Random random = new Random();

    /**
     * Odihnește eroul (recuperează HP și resursă)
     */
    public AbilityDTO.RestResultDTO rest(Erou hero) {
        if (hero.getGold() < REST_COST) {
            return new AbilityDTO.RestResultDTO(
                    false,
                    "Nu ai destul gold pentru odihnă! Necesar: " + REST_COST + " gold",
                    0, 0, 0
            );
        }

        int hpBefore = hero.getViata();
        int resourceBefore = hero.getResursaCurenta();

        hero.scadeGold(REST_COST);
        hero.vindeca(hero.getViataMaxima()); // Recuperează toată viața
        hero.regenereazaResursa(hero.getResursaMaxima()); // Recuperează toată resursa

        int hpRecovered = hero.getViata() - hpBefore;
        int resourceRecovered = hero.getResursaCurenta() - resourceBefore;

        return new AbilityDTO.RestResultDTO(
                true,
                "Te-ai odihnit la tavernă! Ești fresh ca un morcov!",
                REST_COST,
                hpRecovered,
                resourceRecovered
        );
    }

    /**
     * Informații despre odihnă
     */
    public RestInfoDTO getRestInfo(Erou hero) {
        int hpToRecover = hero.getViataMaxima() - hero.getViata();
        int resourceToRecover = hero.getResursaMaxima() - hero.getResursaCurenta();
        boolean canAfford = hero.getGold() >= REST_COST;
        boolean needsRest = hpToRecover > 0 || resourceToRecover > 0;

        return new RestInfoDTO(
                REST_COST,
                hpToRecover,
                resourceToRecover,
                canAfford,
                needsRest
        );
    }

    /**
     * Gambling - Joacă zaruri
     */
    public GambleResultDTO gambleDice(Erou hero, int bet) {
        // Validări
        if (bet < GAMBLE_MIN_BET) {
            return new GambleResultDTO(
                    false,
                    "Pariul minim este " + GAMBLE_MIN_BET + " gold!",
                    0, 0, 0, 0
            );
        }

        if (bet > GAMBLE_MAX_BET) {
            return new GambleResultDTO(
                    false,
                    "Pariul maxim este " + GAMBLE_MAX_BET + " gold!",
                    0, 0, 0, 0
            );
        }

        if (hero.getGold() < bet) {
            return new GambleResultDTO(
                    false,
                    "Nu ai destul gold! Ai doar " + hero.getGold() + " gold.",
                    0, 0, 0, 0
            );
        }

        // Aruncă zaruri
        int heroRoll = random.nextInt(6) + 1;
        int tavernKeeperRoll = random.nextInt(6) + 1;

        hero.scadeGold(bet);

        boolean won = heroRoll > tavernKeeperRoll;
        int winnings = 0;

        if (won) {
            winnings = bet * 2; // Câștigi dublu
            hero.adaugaGold(winnings);
        } else if (heroRoll == tavernKeeperRoll) {
            // Egalitate - primești banii înapoi
            hero.adaugaGold(bet);
            winnings = bet;
        }

        String message = buildGambleMessage(heroRoll, tavernKeeperRoll, won, bet, winnings);

        return new GambleResultDTO(
                true,
                message,
                bet,
                winnings,
                heroRoll,
                tavernKeeperRoll
        );
    }

    private String buildGambleMessage(int heroRoll, int keeperRoll, boolean won, int bet, int winnings) {
        StringBuilder msg = new StringBuilder();
        msg.append("🎲 Zaruri aruncate!\n\n");
        msg.append("Tu: ").append(heroRoll).append("\n");
        msg.append("Hangiul: ").append(keeperRoll).append("\n\n");

        if (heroRoll > keeperRoll) {
            msg.append("🎉 AI CÂȘTIGAT!\n");
            msg.append("Primești: ").append(winnings).append(" gold\n");
            msg.append("Profit: +").append(winnings - bet).append(" gold");
        } else if (heroRoll == keeperRoll) {
            msg.append("🤝 EGALITATE!\n");
            msg.append("Îți primești banii înapoi: ").append(bet).append(" gold");
        } else {
            msg.append("😢 AI PIERDUT!\n");
            msg.append("Pierzi: ").append(bet).append(" gold");
        }

        return msg.toString();
    }

    /**
     * Informații despre gambling
     */
    public GambleInfoDTO getGambleInfo() {
        return new GambleInfoDTO(
                GAMBLE_MIN_BET,
                GAMBLE_MAX_BET,
                "Arunci un zar împotriva hangiului.\n" +
                        "Dacă arunci mai mult, câștigi dublu!\n" +
                        "Dacă e egalitate, primești banii înapoi.\n" +
                        "Dacă pierzi, pierzi pariul."
        );
    }

    /**
     * Achiziționează băuturi speciale (buff-uri temporare)
     */
    public DrinkResultDTO buyDrink(Erou hero, DrinkType drinkType) {
        if (hero.getGold() < drinkType.getPrice()) {
            return new DrinkResultDTO(
                    false,
                    "Nu ai destul gold! Necesar: " + drinkType.getPrice() + " gold",
                    drinkType,
                    0
            );
        }

        hero.scadeGold(drinkType.getPrice());

        // Aplică efectul băuturii
        applyDrinkEffect(hero, drinkType);

        return new DrinkResultDTO(
                true,
                "Ai cumpărat " + drinkType.getDisplayName() + "!\n" + drinkType.getEffect(),
                drinkType,
                drinkType.getPrice()
        );
    }

    private void applyDrinkEffect(Erou hero, DrinkType drinkType) {
        // Efectele sunt temporare - poți implementa un sistem de buff-uri
        // Pentru moment, doar recuperează puțin HP/resursă
        switch (drinkType) {
            case BERE -> hero.vindeca(30);
            case VIN -> hero.regenereazaResursa(20);
            case PALINCA -> {
                hero.vindeca(50);
                hero.regenereazaResursa(30);
            }
            case TUICA -> hero.regenereazaResursa(50);
        }
    }

    /**
     * Lista de băuturi disponibile
     */
    public List<DrinkDTO> getAvailableDrinks() {
        List<DrinkDTO> drinks = new ArrayList<>();

        for (DrinkType type : DrinkType.values()) {
            drinks.add(new DrinkDTO(
                    type,
                    type.getDisplayName(),
                    type.getDescription(),
                    type.getPrice(),
                    type.getEffect()
            ));
        }

        return drinks;
    }

    /**
     * Povești de la tavernă (pentru atmosferă)
     */
    public String getRandomTavernStory() {
        String[] stories = {
                "🍺 Hangiul: 'Ai auzit? Se spune că în Gara de Nord apare un monstru la miezul nopții...'",
                "👴 Bătrân: 'Pe vremea mea, boss-ii erau mai grei! Acum sunt ușori ca o plăcintă.'",
                "🎭 Trubadur: '♫ În Bucale-n zori de zi, Un erou va învinge... ♫'",
                "🥃 Client beat: '*hîc* Am pierdut tot aurul la zaruri... *hîc*'",
                "👨‍🌾 Țăran: 'Dacă mergi în dungeon, ia-ți echipament bun! Nu ca mine...'",
                "🧙 Vrăjitor: 'Poțiunile de buff sunt esențiale pentru victorii! Cumpără din shop!'",
                "⚔️ Veteran: 'Învață-ți abilitățile bine! Cooldown-urile te pot costa viața!'",
                "🌯 Vânzător: 'Șaormele de Revival sunt rare! Doar boss-ii le au!'"
        };

        return stories[random.nextInt(stories.length)];
    }

    /**
     * Quest-uri simple zilnice (feature opțional)
     */
    public List<TavernQuestDTO> getDailyQuests(Erou hero) {
        // Poți implementa un sistem de quest-uri zilnice
        // Pentru moment, returnăm o listă goală
        return new ArrayList<>();
    }

    // ==================== TIPURI DE BĂUTURI ====================

    public enum DrinkType {
        BERE("🍺 Bere", "O bere rece din Bucale", 20, "Recuperează 30 HP"),
        VIN("🍷 Vin", "Vin roșu de casă", 30, "Recuperează 20 resurse"),
        TUICA("🥃 Țuică", "Țuică tare de prună", 50, "Recuperează 50 resurse"),
        PALINCA("🔥 Pălincă", "Pălincă de 60 grade!", 80, "Recuperează 50 HP și 30 resurse");

        private final String displayName;
        private final String description;
        private final int price;
        private final String effect;

        DrinkType(String displayName, String description, int price, String effect) {
            this.displayName = displayName;
            this.description = description;
            this.price = price;
            this.effect = effect;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public int getPrice() { return price; }
        public String getEffect() { return effect; }
    }
}