package com.rpg.service;

import com.rpg.model.characters.Erou;
import com.rpg.model.items.BuffPotion;
import com.rpg.model.items.ObiectEchipament;
import com.rpg.service.dto.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * InventoryServiceFX - Refactorizat pentru JavaFX
 * Gestionează inventarul fără Scanner sau System.out
 */
public class InventoryServiceFX {

    public enum InventoryCategory {
        TOATE("📦 Toate Itemurile"),
        ECHIPAT("⚔️ Echipament Echipat"),
        ARME("🗡️ Arme"),
        ARMURI("🛡️ Armuri"),
        ACCESORII("💍 Accesorii"),
        CONSUMABILE("🧪 Consumabile"),
        POTIUNI_VINDECARE("❤️ Poțiuni Vindecare"),
        POTIUNI_BUFF("💪 Poțiuni Buff"),
        SPECIALE("✨ Iteme Speciale");

        private final String displayName;

        InventoryCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Obține toate itemurile din inventar filtrate pe categorie
     */
    public List<InventoryItemDTO> getItemsByCategory(Erou erou, InventoryCategory category) {
        return switch (category) {
            case TOATE -> getAllItems(erou);
            case ECHIPAT -> getEquippedItems(erou);
            case ARME -> getWeapons(erou);
            case ARMURI -> getArmors(erou);
            case ACCESORII -> getAccessories(erou);
            case CONSUMABILE -> getConsumables(erou);
            case POTIUNI_VINDECARE -> getHealingPotions(erou);
            case POTIUNI_BUFF -> getBuffPotions(erou);
            case SPECIALE -> getSpecialItems(erou);
        };
    }

    /**
     * Toate itemurile
     */
    private List<InventoryItemDTO> getAllItems(Erou erou) {
        List<InventoryItemDTO> allItems = new ArrayList<>();

        // Echipament din inventar
        for (ObiectEchipament item : erou.getInventar().getItems()) {
            allItems.add(createEquipmentDTO(item, false));
        }

        // Echipament echipat
        if (erou.getArmaEchipata() != null) {
            allItems.add(createEquipmentDTO(erou.getArmaEchipata(), true));
        }
        if (erou.getArmuraEchipata() != null) {
            allItems.add(createEquipmentDTO(erou.getArmuraEchipata(), true));
        }
        if (erou.getAccesoriuEchipat() != null) {
            allItems.add(createEquipmentDTO(erou.getAccesoriuEchipat(), true));
        }

        // Poțiuni vindecare
        for (Map.Entry<Integer, Integer> entry : erou.getInventar().getHealthPotions().entrySet()) {
            allItems.add(createHealingPotionDTO(entry.getKey(), entry.getValue()));
        }

        // Poțiuni buff
        for (Map.Entry<BuffPotion.BuffType, Integer> entry : erou.getInventar().getBuffPotions().entrySet()) {
            allItems.add(createBuffPotionDTO(entry.getKey(), entry.getValue()));
        }

        // Enchant scrolls
        int scrollCount = erou.getInventar().getEnchantScrolls().size();
        if (scrollCount > 0) {
            allItems.add(createEnchantScrollDTO(scrollCount));
        }

        // Flask pieces
        int flaskCount = erou.getInventar().getFlaskPieces().size();
        if (flaskCount > 0) {
            allItems.add(createFlaskPieceDTO(flaskCount));
        }

        // Shaorma revival
        int shaormaCount = erou.getShaormaRevivalCount();
        if (shaormaCount > 0) {
            allItems.add(createShaormaDTO(shaormaCount));
        }

        return allItems;
    }

    /**
     * Echipament echipat
     */
    private List<InventoryItemDTO> getEquippedItems(Erou erou) {
        List<InventoryItemDTO> items = new ArrayList<>();

        if (erou.getArmaEchipata() != null) {
            items.add(createEquipmentDTO(erou.getArmaEchipata(), true));
        }
        if (erou.getArmuraEchipata() != null) {
            items.add(createEquipmentDTO(erou.getArmuraEchipata(), true));
        }
        if (erou.getAccesoriuEchipat() != null) {
            items.add(createEquipmentDTO(erou.getAccesoriuEchipat(), true));
        }

        return items;
    }

    /**
     * Arme - ÎNLOCUIEȘTE METODA COMPLETĂ
     */
    private List<InventoryItemDTO> getWeapons(Erou erou) {
        return erou.getInventar().getItems().stream()
                .filter(item -> item.getTip() == ObiectEchipament.TipEchipament.WEAPON)  // ✅ WEAPON
                .map(item -> createEquipmentDTO(item, false))
                .collect(Collectors.toList());
    }
    /**
     * Armuri - ÎNLOCUIEȘTE METODA COMPLETĂ
     */
    private List<InventoryItemDTO> getArmors(Erou erou) {
        return erou.getInventar().getItems().stream()
                .filter(item -> item.getTip() == ObiectEchipament.TipEchipament.ARMOR)  // ✅ ARMOR
                .map(item -> createEquipmentDTO(item, false))
                .collect(Collectors.toList());
    }
    /**
     * Accesorii - ÎNLOCUIEȘTE METODA COMPLETĂ
     */
    private List<InventoryItemDTO> getAccessories(Erou erou) {
        return erou.getInventar().getItems().stream()
                .filter(item -> {
                    ObiectEchipament.TipEchipament tip = item.getTip();
                    // Accesorii = RING, NECKLACE, etc (tot ce nu e armă sau armură)
                    return tip == ObiectEchipament.TipEchipament.RING ||
                            tip == ObiectEchipament.TipEchipament.NECKLACE ||
                            tip == ObiectEchipament.TipEchipament.HELMET ||
                            tip == ObiectEchipament.TipEchipament.BOOTS ||
                            tip == ObiectEchipament.TipEchipament.GLOVES ||
                            tip == ObiectEchipament.TipEchipament.SHIELD;
                })
                .map(item -> createEquipmentDTO(item, false))
                .collect(Collectors.toList());
    }
    /**
     * Consumabile (toate poțiunile și scrollurile)
     */
    private List<InventoryItemDTO> getConsumables(Erou erou) {
        List<InventoryItemDTO> items = new ArrayList<>();
        items.addAll(getHealingPotions(erou));
        items.addAll(getBuffPotions(erou));

        int scrollCount = erou.getInventar().getEnchantScrolls().size();
        if (scrollCount > 0) {
            items.add(createEnchantScrollDTO(scrollCount));
        }

        return items;
    }

    /**
     * Poțiuni vindecare
     */
    private List<InventoryItemDTO> getHealingPotions(Erou erou) {
        List<InventoryItemDTO> items = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : erou.getInventar().getHealthPotions().entrySet()) {
            items.add(createHealingPotionDTO(entry.getKey(), entry.getValue()));
        }

        return items;
    }

    /**
     * Poțiuni buff
     */
    private List<InventoryItemDTO> getBuffPotions(Erou erou) {
        List<InventoryItemDTO> items = new ArrayList<>();

        for (Map.Entry<BuffPotion.BuffType, Integer> entry : erou.getInventar().getBuffPotions().entrySet()) {
            items.add(createBuffPotionDTO(entry.getKey(), entry.getValue()));
        }

        return items;
    }

    /**
     * Iteme speciale
     */
    private List<InventoryItemDTO> getSpecialItems(Erou erou) {
        List<InventoryItemDTO> items = new ArrayList<>();

        int flaskCount = erou.getInventar().getFlaskPieces().size();
        if (flaskCount > 0) {
            items.add(createFlaskPieceDTO(flaskCount));
        }

        int shaormaCount = erou.getShaormaRevivalCount();
        if (shaormaCount > 0) {
            items.add(createShaormaDTO(shaormaCount));
        }

        return items;
    }

    // ==================== ACȚIUNI PE ITEME ====================

    /**
     * EquipItem - ÎNLOCUIEȘTE METODA COMPLETĂ
     */
    public EquipResult equipItem(Erou erou, ObiectEchipament item) {
        ObiectEchipament previousItem = null;

        switch (item.getTip()) {
            case WEAPON -> {  // ✅ WEAPON în loc de ARMA
                previousItem = erou.getArmaEchipata();
                erou.echipeazaArma(item);
            }
            case ARMOR -> {  // ✅ ARMOR în loc de ARMURA
                previousItem = erou.getArmuraEchipata();
                erou.echipeazaArmura(item);
            }
            case RING, NECKLACE, HELMET, BOOTS, GLOVES, SHIELD -> {  // ✅ Accesorii multiple
                previousItem = erou.getAccesoriuEchipat();
                erou.echipeazaAccesoriu(item);
            }
        }

        return new EquipResult(true, "Ai echipat: " + item.getNume(), previousItem);
    }

    /**
     * UnequipItem - ÎNLOCUIEȘTE METODA COMPLETĂ
     */
    public EquipResult unequipItem(Erou erou, ObiectEchipament item) {
        boolean success = false;

        switch (item.getTip()) {
            case WEAPON -> {  // ✅ WEAPON
                if (erou.getArmaEchipata() == item) {
                    erou.deechipeazaArma();
                    success = true;
                }
            }
            case ARMOR -> {  // ✅ ARMOR
                if (erou.getArmuraEchipata() == item) {
                    erou.deechipeazaArmura();
                    success = true;
                }
            }
            case RING, NECKLACE, HELMET, BOOTS, GLOVES, SHIELD -> {  // ✅ Accesorii
                if (erou.getAccesoriuEchipat() == item) {
                    erou.deechipeazaAccesoriu();
                    success = true;
                }
            }
        }

        if (success) {
            return new EquipResult(true, "Ai deechipat: " + item.getNume(), item);
        }

        return new EquipResult(false, "Nu s-a putut deechipa itemul!", null);
    }
    /**
     * Folosește o poțiune de vindecare
     */
    public UseItemResult useHealingPotion(Erou erou, int healAmount) {
        if (!erou.getInventar().hasHealthPotion(healAmount)) {
            return new UseItemResult(false, "Nu ai această poțiune!", 0);
        }

        int viataInainte = erou.getViata();
        int viataMaxima = erou.getViataMaxima();

        if (viataInainte >= viataMaxima) {
            return new UseItemResult(false, "Ai deja viața plină!", 0);
        }

        erou.vindeca(healAmount);
        erou.getInventar().removeHealthPotion(healAmount);

        int viataVindecata = Math.min(healAmount, viataMaxima - viataInainte);

        return new UseItemResult(
                true,
                "Ai folosit poțiunea și ai vindecat " + viataVindecata + " HP!",
                viataVindecata
        );
    }

    /**
     * Folosește o poțiune de buff
     */
    public UseItemResult useBuffPotion(Erou erou, BuffPotion.BuffType buffType) {
        if (!erou.getInventar().hasBuffPotion(buffType)) {
            return new UseItemResult(false, "Nu ai această poțiune!", 0);
        }

        erou.getInventar().removeBuffPotion(buffType);
        erou.aplicaBuff(buffType);

        return new UseItemResult(
                true,
                "Ai activat buff-ul: " + buffType.getDisplayName() + "!",
                0
        );
    }

    /**
     * Aruncă un item din inventar
     */
    public DropItemResult dropItem(Erou erou, ObiectEchipament item) {
        boolean removed = erou.getInventar().removeItem(item);

        if (removed) {
            return new DropItemResult(true, "Ai aruncat: " + item.getNume());
        }

        return new DropItemResult(false, "Nu s-a putut arunca itemul!");
    }

    // ==================== CREATE DTO HELPERS ====================

    /**
     * CreateEquipmentDTO - ÎNLOCUIEȘTE METODA COMPLETĂ
     */
    private InventoryItemDTO createEquipmentDTO(ObiectEchipament item, boolean equipped) {
        String icon = switch (item.getTip()) {
            case WEAPON -> "⚔️";     // ✅ WEAPON
            case ARMOR -> "🛡️";      // ✅ ARMOR
            case HELMET -> "⛑️";
            case BOOTS -> "🥾";
            case GLOVES -> "🧤";
            case RING -> "💍";
            case NECKLACE -> "📿";
            case SHIELD -> "🛡️";
        };

        StringBuilder description = new StringBuilder();
        description.append("Raritate: ").append(item.getRaritate()).append("\n");
        description.append("Nivel: ").append(item.getNivelNecesar()).append("\n");
        description.append("Enhancement: +").append(item.getEnhancementLevel()).append("\n");
        description.append("Preț: ").append(item.getPret()).append(" gold\n\n");
        description.append("Bonusuri:\n");

        item.getTotalBonuses().forEach((stat, bonus) ->
                description.append("  • +").append(bonus).append(" ").append(stat).append("\n")
        );

        return new InventoryItemDTO(
                item,
                icon + " " + item.getNume(),
                description.toString(),
                equipped ? InventoryItemDTO.ItemType.EQUIPMENT : InventoryItemDTO.ItemType.EQUIPMENT,
                1,
                item
        );
    }
    private InventoryItemDTO createHealingPotionDTO(int healAmount, int quantity) {
        String name = switch (healAmount) {
            case 50 -> "🧪 Poțiune Mică";
            case 100 -> "🧪 Poțiune Medie";
            case 200 -> "🧪 Poțiune Mare";
            case 500 -> "🧪 Poțiune Maximă";
            default -> "🧪 Poțiune (" + healAmount + " HP)";
        };

        String description = "Restabilește " + healAmount + " HP\n" +
                "Cantitate: " + quantity;

        return new InventoryItemDTO(
                "healing_" + healAmount,
                name,
                description,
                InventoryItemDTO.ItemType.HEALING_POTION,
                quantity,
                healAmount
        );
    }

    private InventoryItemDTO createBuffPotionDTO(BuffPotion.BuffType buffType, int quantity) {
        String description = buffType.getDescription() + "\n" +
                "Durată: " + buffType.getDuration() + " lupte\n" +
                "Cantitate: " + quantity;

        return new InventoryItemDTO(
                "buff_" + buffType.name(),
                buffType.getIcon() + " " + buffType.getDisplayName(),
                description,
                InventoryItemDTO.ItemType.BUFF_POTION,
                quantity,
                buffType
        );
    }

    private InventoryItemDTO createEnchantScrollDTO(int quantity) {
        return new InventoryItemDTO(
                "enchant_scroll",
                "📜 Enchant Scroll",
                "Îmbunătățește un echipament cu +1 nivel\n" +
                        "Cantitate: " + quantity,
                InventoryItemDTO.ItemType.ENCHANT_SCROLL,
                quantity,
                null
        );
    }

    private InventoryItemDTO createFlaskPieceDTO(int quantity) {
        return new InventoryItemDTO(
                "flask_piece",
                "⚗️ Flask Piece",
                "Bucată de flask misterioasă\n" +
                        "Colectează 5 pentru a obține un reward!\n" +
                        "Cantitate: " + quantity + "/5",
                InventoryItemDTO.ItemType.SPECIAL,
                quantity,
                null
        );
    }

    private InventoryItemDTO createShaormaDTO(int quantity) {
        return new InventoryItemDTO(
                "shaorma_revival",
                "🌯 Șaorma de Revival",
                "Te readuce la viață în luptă!\n" +
                        "Cantitate: " + quantity,
                InventoryItemDTO.ItemType.SPECIAL,
                quantity,
                null
        );
    }

    /**
     * Obține statistici despre inventar
     */
    public InventoryStatsDTO getInventoryStats(Erou erou) {
        int totalItems = erou.getInventar().getItems().size();
        int maxCapacity = erou.getInventar().getCapacitateMaxima();
        int equipmentCount = totalItems;
        int potionCount = erou.getInventar().getHealthPotions().values().stream()
                .mapToInt(Integer::intValue).sum();
        int buffPotionCount = erou.getInventar().getBuffPotions().values().stream()
                .mapToInt(Integer::intValue).sum();
        int specialItemCount = erou.getInventar().getEnchantScrolls().size() +
                erou.getInventar().getFlaskPieces().size() +
                erou.getShaormaRevivalCount();

        return new InventoryStatsDTO(
                totalItems,
                maxCapacity,
                equipmentCount,
                potionCount,
                buffPotionCount,
                specialItemCount
        );
    }
}