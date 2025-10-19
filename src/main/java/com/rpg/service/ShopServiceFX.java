package com.rpg.service;

import com.rpg.model.characters.Erou;
import com.rpg.model.items.BuffPotion;
import com.rpg.model.items.EnchantScroll;
import com.rpg.model.items.ObiectEchipament;
import com.rpg.service.dto.PurchaseResult;
import com.rpg.service.dto.ShopItemDTO;
import com.rpg.service.LootGenerator;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ShopService refactorizat pentru JavaFX
 * NU mai folosește Scanner sau System.out
 * Returnează date pentru UI și gestionează logica de business
 */
public class ShopServiceFX {

    // Cache intern pentru sample-uri generate (id -> item)
    private final Map<String, ObiectEchipament> generatedSamples = new HashMap<>();

    // Contor restock - ca să schimbi seed-ul logic dacă vrei
    private int restockCycle = 0;

    // Iteme de bază cu stoc limitat (id -> stoc ramas)
    private final Map<String, Integer> baseStock = new HashMap<>();


    // Categoriile de produse
    public enum ShopCategory {
        POTIUNI("🧪 Poțiuni de Vindecare"),
        BUFF_POTIUNI("💪 Poțiuni de Buff"),
        ECHIPAMENT("⚔️ Echipament"),
        CONSUMABILE("🎁 Consumabile Speciale"),
        PACK_URI("📦 Pack-uri Combo");

        private final String displayName;

        // 🏪 CACHE pentru sample-urile generate - vindem exact ce afișăm


        ShopCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public void forceRestock() {
        // 1) golește cache-ul de sample-uri
        generatedSamples.clear();

        // 2) reinitializează stocul pentru itemele de bază
        // poți personaliza aceste id-uri în funcție de ce generezi în getShopItems
        baseStock.clear();
        baseStock.put("potion_small", 5);
        baseStock.put("potion_medium", 3);
        baseStock.put("potion_large", 1);

        // 3) bump un ciclu de restock (dacă vrei să alterezi RNG)
        restockCycle++;
    }

    /**
     * Returnează toate produsele disponibile pentru o categorie
     */
    public List<ShopItemDTO> getItemsByCategory(ShopCategory category, int heroLevel) {
        return switch (category) {
            case POTIUNI -> getHealingPotions();
            case BUFF_POTIUNI -> getBuffPotions();
            case ECHIPAMENT -> getEquipment(heroLevel);
            case CONSUMABILE -> getSpecialConsumables();
            case PACK_URI -> getPacks();
        };
    }

    /**
     * Poțiuni de vindecare
     */
    private List<ShopItemDTO> getHealingPotions() {
        List<ShopItemDTO> items = new ArrayList<>();

        items.add(new ShopItemDTO(
                "potiune_mica",
                "🧪 Poțiune Mică",
                "Restabilește 50 HP",
                15,
                ShopCategory.POTIUNI,
                1
        ));

        items.add(new ShopItemDTO(
                "potiune_medie",
                "🧪 Poțiune Medie",
                "Restabilește 100 HP",
                25,
                ShopCategory.POTIUNI,
                1
        ));

        items.add(new ShopItemDTO(
                "potiune_mare",
                "🧪 Poțiune Mare",
                "Restabilește 200 HP",
                45,
                ShopCategory.POTIUNI,
                1
        ));

        items.add(new ShopItemDTO(
                "potiune_maxima",
                "🧪 Poțiune Maximă",
                "Restabilește 500 HP",
                90,
                ShopCategory.POTIUNI,
                1
        ));

        return items;
    }

    /**
     * Poțiuni de buff
     */
    private List<ShopItemDTO> getBuffPotions() {
        List<ShopItemDTO> items = new ArrayList<>();

        // Poțiuni de stat
        items.add(new ShopItemDTO(
                "buff_strength",
                "💪 Poțiune de Strength",
                "+5 Strength pentru 3 lupte",
                50,
                ShopCategory.BUFF_POTIUNI,
                1
        ));

        items.add(new ShopItemDTO(
                "buff_dexterity",
                "🎯 Poțiune de Dexterity",
                "+5 Dexterity pentru 3 lupte",
                50,
                ShopCategory.BUFF_POTIUNI,
                1
        ));

        items.add(new ShopItemDTO(
                "buff_intelligence",
                "🧠 Poțiune de Intelligence",
                "+5 Intelligence pentru 3 lupte",
                50,
                ShopCategory.BUFF_POTIUNI,
                1
        ));

        // Poțiuni de combat
        items.add(new ShopItemDTO(
                "buff_damage",
                "⚔️ Poțiune de Damage",
                "+15% Damage pentru 3 lupte",
                75,
                ShopCategory.BUFF_POTIUNI,
                1
        ));

        items.add(new ShopItemDTO(
                "buff_defense",
                "🛡️ Poțiune de Defense",
                "+15% Defense pentru 3 lupte",
                75,
                ShopCategory.BUFF_POTIUNI,
                1
        ));

        items.add(new ShopItemDTO(
                "buff_critical",
                "💥 Poțiune de Critical",
                "+20% Critical Chance pentru 3 lupte",
                100,
                ShopCategory.BUFF_POTIUNI,
                1
        ));

        return items;
    }

    /**
     * ✅ METODĂ PRINCIPALĂ - returnează toate itemele disponibile în shop
     */
    public List<ShopItemDTO> getShopItems(int heroLevel) {
        List<ShopItemDTO> allItems = new ArrayList<>();

        // Adaugă toate categoriile
        allItems.addAll(getHealingPotions());
        allItems.addAll(getBuffPotions());
        allItems.addAll(getEquipment(heroLevel));
        allItems.addAll(getSpecialConsumables());
        allItems.addAll(getPacks());

        return allItems;
    }


    public ObiectEchipament createEquipmentFromShopItem(ShopItemDTO shopItem) {
        if (shopItem == null) return null;

        // 1) dacă avem sample în cache, întoarce o copie
        ObiectEchipament cached = generatedSamples.get(shopItem.getId());
        if (cached != null) {
            return cached.createCopy();
        }

        // 2) dacă nu avem, încearcă să deduci itemul din ID (ex: weapon_one_12)
        // Formaturi posibile (după cum ai folosit mai sus):
        // weapon_one_{level}, weapon_two_{level}, armor_{level}, shield_{level}, offhand_weapon_{level}, offhand_magic_{level}
        try {
            String id = shopItem.getId();
            String[] parts = id.split("_");
            int level = extractLevelFromId(parts);

            ObiectEchipament.TipEchipament tip = deduceTipFromId(parts);

            if (tip != null) {
                ObiectEchipament item = com.rpg.service.LootGenerator.generateItemByType(
                        tip,
                        level,
                        deduceRarityFromDescription(shopItem.getDescription()) // încearcă să păstrezi raritatea
                );

                // Salvează sample-ul în cache pentru WYSIWYG pe viitor
                generatedSamples.put(id, item);
                return item.createCopy();
            }

        } catch (Exception ignored) {
            // dacă nu reușim să deducem, fallback
        }

        // 3) fallback - generează ceva generic pe nivelul eroului
        int guessedLevel = Math.max(1, heroLevelFallback());
        ObiectEchipament item = com.rpg.service.LootGenerator.generateRandomItem(guessedLevel);
        return item;
    }

    private int extractLevelFromId(String[] parts) {
        // Caută un token numeric în id
        for (int i = parts.length - 1; i >= 0; i--) {
            try {
                return Integer.parseInt(parts[i]);
            } catch (NumberFormatException ignored) {}
        }
        return 1;
    }

    private ObiectEchipament.TipEchipament deduceTipFromId(String[] parts) {
        String joined = String.join("_", parts).toLowerCase();
        if (joined.contains("weapon_one")) return ObiectEchipament.TipEchipament.WEAPON_ONE_HANDED;
        if (joined.contains("weapon_two")) return ObiectEchipament.TipEchipament.WEAPON_TWO_HANDED;
        if (joined.contains("armor"))      return ObiectEchipament.TipEchipament.ARMOR;
        if (joined.contains("shield"))     return ObiectEchipament.TipEchipament.SHIELD;
        if (joined.contains("offhand_weapon")) return ObiectEchipament.TipEchipament.OFF_HAND_WEAPON;
        if (joined.contains("offhand_magic"))  return ObiectEchipament.TipEchipament.OFF_HAND_MAGIC;
        return null;
    }

    private ObiectEchipament.Raritate deduceRarityFromDescription(String desc) {
        if (desc == null) return ObiectEchipament.Raritate.COMMON;
        String d = desc.toLowerCase();
        if (d.contains("legendary")) return ObiectEchipament.Raritate.LEGENDARY;
        if (d.contains("epic"))      return ObiectEchipament.Raritate.EPIC;
        if (d.contains("rare"))      return ObiectEchipament.Raritate.RARE;
        if (d.contains("uncommon"))  return ObiectEchipament.Raritate.UNCOMMON;
        return ObiectEchipament.Raritate.COMMON;
    }

    private int heroLevelFallback() {
        // Dacă nu ai acces direct la erou aici, treci nivelul drept parametru când creezi ShopServiceFX
        // sau păstrează-l într-un field la creare.
        return 5;
    }


    /**
     * Echipament (generat dinamic pe nivel) - COMPLETĂ CU NOUL SISTEM
     */
    private List<ShopItemDTO> getEquipment(int heroLevel) {
        List<ShopItemDTO> items = new ArrayList<>();

        // 🗡️ ARME ONE-HANDED
        String weaponOneKey = "weapon_one_" + heroLevel;
        ObiectEchipament weaponOneSample = LootGenerator.generateItemByType(
                ObiectEchipament.TipEchipament.WEAPON_ONE_HANDED,
                heroLevel,
                pickShopRarityForLevel(heroLevel)
        );
        weaponOneSample = applyShopFlavor(weaponOneSample);
        generatedSamples.put(weaponOneKey, weaponOneSample);

        StringBuilder weaponOneDesc = new StringBuilder();
        weaponOneDesc.append("⚔️ ARMĂ ONE-HANDED - Nivel ").append(heroLevel).append("\n\n");
        weaponOneDesc.append("📦 Exact acest item: ").append(weaponOneSample.getNume()).append("\n");
        weaponOneDesc.append("🎯 Raritate: ").append(weaponOneSample.getRaritate().getDisplayName()).append("\n");
        weaponOneDesc.append("🔧 Class: ").append(weaponOneSample.getWeaponClass()).append("\n");
        if (weaponOneSample.isOffHandCompatible()) {
            weaponOneDesc.append("✨ Poate fi folosită și în off-hand!\n");
        }
        weaponOneDesc.append("\n");
        addBonusesToDescription(weaponOneDesc, weaponOneSample);
        weaponOneDesc.append("\n✅ WYSIWYG - What You See Is What You Get!");

        items.add(new ShopItemDTO(
                weaponOneKey,
                weaponOneSample.getTip().getIcon() + " " + weaponOneSample.getNume(),
                weaponOneDesc.toString(),
                weaponOneSample.getPret(),
                ShopCategory.ECHIPAMENT,
                1
        ));

        // 🗡️ ARME TWO-HANDED
        String weaponTwoKey = "weapon_two_" + heroLevel;
        ObiectEchipament weaponTwoSample = LootGenerator.generateItemByType(
                ObiectEchipament.TipEchipament.WEAPON_TWO_HANDED,
                heroLevel,
                pickShopRarityForLevel(heroLevel)
        );
        weaponTwoSample = applyShopFlavor(weaponTwoSample);
        generatedSamples.put(weaponTwoKey, weaponTwoSample);

        StringBuilder weaponTwoDesc = new StringBuilder();
        weaponTwoDesc.append("🗡️ ARMĂ TWO-HANDED - Nivel ").append(heroLevel).append("\n\n");
        weaponTwoDesc.append("📦 Exact acest item: ").append(weaponTwoSample.getNume()).append("\n");
        weaponTwoDesc.append("🎯 Raritate: ").append(weaponTwoSample.getRaritate().getDisplayName()).append("\n");
        weaponTwoDesc.append("🔧 Class: ").append(weaponTwoSample.getWeaponClass()).append("\n");
        weaponTwoDesc.append("⚠️ Ocupă ambele mâini!\n\n");
        addBonusesToDescription(weaponTwoDesc, weaponTwoSample);
        weaponTwoDesc.append("\n✅ WYSIWYG - What You See Is What You Get!");

        items.add(new ShopItemDTO(
                weaponTwoKey,
                weaponTwoSample.getTip().getIcon() + " " + weaponTwoSample.getNume(),
                weaponTwoDesc.toString(),
                weaponTwoSample.getPret(),
                ShopCategory.ECHIPAMENT,
                1
        ));

        // 🛡️ ARMURI
        String armorKey = "armor_" + heroLevel;
        ObiectEchipament armorSample = LootGenerator.generateItemByType(
                ObiectEchipament.TipEchipament.ARMOR,
                heroLevel,
                pickShopRarityForLevel(heroLevel)
        );
        armorSample = applyShopFlavor(armorSample);
        generatedSamples.put(armorKey, armorSample);

        StringBuilder armorDesc = new StringBuilder();
        armorDesc.append("🛡️ ARMURĂ - Nivel ").append(heroLevel).append("\n\n");
        armorDesc.append("📦 Exact acest item: ").append(armorSample.getNume()).append("\n");
        armorDesc.append("🎯 Raritate: ").append(armorSample.getRaritate().getDisplayName()).append("\n\n");
        addBonusesToDescription(armorDesc, armorSample);
        armorDesc.append("\n✅ WYSIWYG - What You See Is What You Get!");

        items.add(new ShopItemDTO(
                armorKey,
                armorSample.getTip().getIcon() + " " + armorSample.getNume(),
                armorDesc.toString(),
                armorSample.getPret(),
                ShopCategory.ECHIPAMENT,
                1
        ));

        // 🛡️ SHIELDS
        String shieldKey = "shield_" + heroLevel;
        ObiectEchipament shieldSample = LootGenerator.generateItemByType(
                ObiectEchipament.TipEchipament.SHIELD,
                heroLevel,
                pickShopRarityForLevel(heroLevel)
        );
        shieldSample = applyShopFlavor(shieldSample);
        generatedSamples.put(shieldKey, shieldSample);

        StringBuilder shieldDesc = new StringBuilder();
        shieldDesc.append("🛡️ SCUT - OFF-HAND - Nivel ").append(heroLevel).append("\n\n");
        shieldDesc.append("📦 Exact acest item: ").append(shieldSample.getNume()).append("\n");
        shieldDesc.append("🎯 Raritate: ").append(shieldSample.getRaritate().getDisplayName()).append("\n");
        shieldDesc.append("🔧 Class: ").append(shieldSample.getWeaponClass()).append("\n");
        shieldDesc.append("⚠️ Nu poate fi folosit cu arme two-handed!\n\n");
        addBonusesToDescription(shieldDesc, shieldSample);
        shieldDesc.append("\n✅ WYSIWYG - What You See Is What You Get!");

        items.add(new ShopItemDTO(
                shieldKey,
                shieldSample.getTip().getIcon() + " " + shieldSample.getNume(),
                shieldDesc.toString(),
                shieldSample.getPret(),
                ShopCategory.ECHIPAMENT,
                1
        ));

        // 📦 OFF-HAND WEAPONS
        String offHandWeaponKey = "offhand_weapon_" + heroLevel;
        ObiectEchipament offHandWeaponSample = LootGenerator.generateItemByType(
                ObiectEchipament.TipEchipament.OFF_HAND_WEAPON,
                heroLevel,
                pickShopRarityForLevel(heroLevel)
        );
        offHandWeaponSample = applyShopFlavor(offHandWeaponSample);
        generatedSamples.put(offHandWeaponKey, offHandWeaponSample);

        StringBuilder offHandWeaponDesc = new StringBuilder();
        offHandWeaponDesc.append("🗡️ OFF-HAND WEAPON - Nivel ").append(heroLevel).append("\n\n");
        offHandWeaponDesc.append("📦 Exact acest item: ").append(offHandWeaponSample.getNume()).append("\n");
        offHandWeaponDesc.append("🎯 Raritate: ").append(offHandWeaponSample.getRaritate().getDisplayName()).append("\n");
        offHandWeaponDesc.append("🔧 Class: ").append(offHandWeaponSample.getWeaponClass()).append("\n");
        offHandWeaponDesc.append("⚠️ Doar pentru off-hand! Perfect pentru dual-wield!\n\n");
        addBonusesToDescription(offHandWeaponDesc, offHandWeaponSample);
        offHandWeaponDesc.append("\n✅ WYSIWYG - What You See Is What You Get!");

        items.add(new ShopItemDTO(
                offHandWeaponKey,
                offHandWeaponSample.getTip().getIcon() + " " + offHandWeaponSample.getNume(),
                offHandWeaponDesc.toString(),
                offHandWeaponSample.getPret(),
                ShopCategory.ECHIPAMENT,
                1
        ));

        // 📖 OFF-HAND MAGIC
        String offHandMagicKey = "offhand_magic_" + heroLevel;
        ObiectEchipament offHandMagicSample = LootGenerator.generateItemByType(
                ObiectEchipament.TipEchipament.OFF_HAND_MAGIC,
                heroLevel,
                pickShopRarityForLevel(heroLevel)
        );
        offHandMagicSample = applyShopFlavor(offHandMagicSample);
        generatedSamples.put(offHandMagicKey, offHandMagicSample);

        StringBuilder offHandMagicDesc = new StringBuilder();
        offHandMagicDesc.append("📖 OFF-HAND MAGIC - Nivel ").append(heroLevel).append("\n\n");
        offHandMagicDesc.append("📦 Exact acest item: ").append(offHandMagicSample.getNume()).append("\n");
        offHandMagicDesc.append("🎯 Raritate: ").append(offHandMagicSample.getRaritate().getDisplayName()).append("\n");
        offHandMagicDesc.append("🔧 Class: ").append(offHandMagicSample.getWeaponClass()).append("\n");
        offHandMagicDesc.append("✨ Perfect pentru mage builds!\n\n");
        addBonusesToDescription(offHandMagicDesc, offHandMagicSample);
        offHandMagicDesc.append("\n✅ WYSIWYG - What You See Is What You Get!");

        items.add(new ShopItemDTO(
                offHandMagicKey,
                offHandMagicSample.getTip().getIcon() + " " + offHandMagicSample.getNume(),
                offHandMagicDesc.toString(),
                offHandMagicSample.getPret(),
                ShopCategory.ECHIPAMENT,
                1
        ));

        return items;
    }

    /**
     * Helper pentru adăugarea bonusurilor în descriere
     */
    private void addBonusesToDescription(StringBuilder desc, ObiectEchipament item) {
        desc.append("📊 BONUSURI EXACTE:\n");

        if (item.getStrengthBonus() > 0) {
            desc.append("💪 +").append(item.getStrengthBonus()).append(" Strength\n");
        }
        if (item.getDexterityBonus() > 0) {
            desc.append("🎯 +").append(item.getDexterityBonus()).append(" Dexterity\n");
        }
        if (item.getIntelligenceBonus() > 0) {
            desc.append("🧠 +").append(item.getIntelligenceBonus()).append(" Intelligence\n");
        }
        if (item.getDefenseBonus() > 0) {
            desc.append("🛡️ +").append(item.getDefenseBonus()).append(" Defense\n");
        }

        // Bonusuri extra (damage, health, etc.)
        Map<String, Integer> bonuses = item.getTotalBonuses();
        bonuses.forEach((stat, bonus) -> {
            if (!stat.equals("strength") && !stat.equals("dexterity") &&
                    !stat.equals("intelligence") && !stat.equals("defense")) {
                desc.append("✨ +").append(bonus).append(" ").append(formatStatName(stat)).append("\n");
            }
        });

        if (item.getEnhancementLevel() > 0) {
            desc.append("⚡ Enhancement: +").append(item.getEnhancementLevel()).append("\n");
        }
    }


    /**
     * Helper pentru formatarea numelor de statistici
     */
    private String formatStatName(String stat) {
        return switch (stat.toLowerCase()) {
            case "attack_bonus" -> "Attack Bonus";
            case "damage_bonus" -> "Damage Bonus";
            case "crit_chance" -> "Critical Chance %";
            case "viata" -> "Health";
            case "mana" -> "Mana";
            case "damage_reduction" -> "Damage Reduction %";
            case "block_chance" -> "Block Chance %";
            default -> stat;
        };
    }

    /**
     * Consumabile speciale
     */
    private List<ShopItemDTO> getSpecialConsumables() {
        List<ShopItemDTO> items = new ArrayList<>();

        items.add(new ShopItemDTO(
                "enchant_scroll",
                "📜 Enchant Scroll",
                "Îmbunătățește un item cu +1 nivel",
                150,
                ShopCategory.CONSUMABILE,
                1
        ));

        items.add(new ShopItemDTO(
                "shaorma_revival",
                "🌯 Șaorma de Revival",
                "Te readuce la viață în luptă!",
                500,
                ShopCategory.CONSUMABILE,
                1
        ));

        return items;
    }

    /**
     * Pack-uri combo
     */
    private List<ShopItemDTO> getPacks() {
        List<ShopItemDTO> items = new ArrayList<>();

        items.add(new ShopItemDTO(
                "starter_pack",
                "📦 Starter Pack",
                "1x Strength, 1x Dexterity, 1x Intelligence (REDUCERE 10%!)",
                135, // În loc de 150
                ShopCategory.PACK_URI,
                1
        ));

        items.add(new ShopItemDTO(
                "combat_pack",
                "📦 Combat Pack",
                "2x Damage, 2x Defense, 1x Critical (REDUCERE 15%!)",
                350, // În loc de 410
                ShopCategory.PACK_URI,
                1
        ));

        return items;
    }

    /**
     * ACHIZIȚIONEAZĂ un item - logica principală
     */
    public PurchaseResult purchaseItem(Erou erou, ShopItemDTO item, int quantity) {
        int totalCost = item.getPrice() * quantity;

        // Verifică dacă are destul gold
        if (erou.getGold() < totalCost) {
            return new PurchaseResult(
                    false,
                    "Nu ai destul gold! Îți lipsesc " + (totalCost - erou.getGold()) + " gold.",
                    0
            );
        }

        // Scade gold-ul
        erou.scadeGold(totalCost);

        // Adaugă itemul în inventar
        boolean added = addItemToHero(erou, item, quantity);

        if (!added) {
            // Returnează gold-ul dacă nu s-a putut adăuga
            erou.adaugaGold(totalCost);
            return new PurchaseResult(
                    false,
                    "Inventarul este plin sau itemul nu a putut fi adăugat!",
                    0
            );
        }

        return new PurchaseResult(
                true,
                "Ai cumpărat " + quantity + "x " + item.getName() + "!",
                totalCost
        );
    }

    /**
     * Adaugă itemul cumpărat în inventarul eroului
     */
    private boolean addItemToHero(Erou erou, ShopItemDTO item, int quantity) {
        String itemId = item.getId();

        // Poțiuni de vindecare
        if (itemId.startsWith("potiune_")) {
            int healAmount = switch (itemId) {
                case "potiune_mica" -> 50;
                case "potiune_medie" -> 100;
                case "potiune_mare" -> 200;
                case "potiune_maxima" -> 500;
                default -> 0;
            };

            for (int i = 0; i < quantity; i++) {
                erou.addHealthPotion(healAmount);
            }
            return true;
        }

        // Poțiuni de buff
        if (itemId.startsWith("buff_")) {
            BuffPotion.BuffType buffType = switch (itemId) {
                case "buff_strength" -> BuffPotion.BuffType.STRENGTH;
                case "buff_dexterity" -> BuffPotion.BuffType.DEXTERITY;
                case "buff_intelligence" -> BuffPotion.BuffType.INTELLIGENCE;
                case "buff_damage" -> BuffPotion.BuffType.DAMAGE;
                case "buff_defense" -> BuffPotion.BuffType.DEFENSE;
                case "buff_critical" -> BuffPotion.BuffType.CRITICAL;
                default -> null;
            };

            if (buffType != null) {
                erou.addBuffPotion(buffType, quantity);
                return true;
            }
        }

// ✅ Echipament din cache - toate tipurile
        if (itemId.startsWith("weapon_") || itemId.startsWith("armor_") ||
                itemId.startsWith("shield_") || itemId.startsWith("offhand_")) {

            for (int i = 0; i < quantity; i++) {
                ObiectEchipament storedSample = generatedSamples.get(itemId);

                if (storedSample != null) {
                    ObiectEchipament shopItem = storedSample.createCopy();
                    boolean added = erou.getInventar().addItem(shopItem);
                    System.out.printf("🛍️ Sold exact sample: %s (added: %s)\n", shopItem.getNume(), added);
                } else {
                    System.out.printf("⚠️ No sample found for %s\n", itemId);
                }
            }
            return true;
        }


//// ✅ Echipament generat via LootGenerator (unificat cu loot-ul)
//        if (itemId.startsWith("weapon_") || itemId.startsWith("armor_")) {
//            boolean isWeapon = itemId.startsWith("weapon_");
//            int itemLevel = extractLevelFromId(itemId, erou.getNivel());
//
//            // 1) Generează folosind loot generator (un singur loc pentru logică)
//            ObiectEchipament.TipEchipament tip = isWeapon
//                    ? ObiectEchipament.TipEchipament.WEAPON
//                    : ObiectEchipament.TipEchipament.ARMOR;
//
//            // LootGenerator își alege singur raritatea/coherence; oferim o raritate corelată cu nivelul
//            ObiectEchipament.Raritate rar = pickShopRarityForLevel(itemLevel); // helper mic (vezi mai jos)
//
//            for (int i = 0; i < quantity; i++) {
//                ObiectEchipament base = LootGenerator.generateItemByType(tip, itemLevel, rar);
//
//                // 2) Aplică “shop flavor” (nume + preț ajustat) fără a rupe generatorul
//                ObiectEchipament shopItem = applyShopFlavor(base);
//
//                boolean added = erou.getInventar().addItem(shopItem);
//                System.out.printf("🛍️ Shop generated via Loot: %s (added: %s)\n", shopItem.getNume(), added);
//            }
//            return true;
//        }


        // Enchant Scroll
        if (itemId.equals("enchant_scroll")) {
            for (int i = 0; i < quantity; i++) {
                erou.addEnchantScroll(new EnchantScroll());
            }
            return true;
        }

        // Șaorma Revival
        if (itemId.equals("shaorma_revival")) {
            erou.adaugaShaormaRevival(quantity);
            return true;
        }

        // Pack-uri
        if (itemId.equals("starter_pack")) {
            erou.addBuffPotion(BuffPotion.BuffType.STRENGTH, quantity);
            erou.addBuffPotion(BuffPotion.BuffType.DEXTERITY, quantity);
            erou.addBuffPotion(BuffPotion.BuffType.INTELLIGENCE, quantity);
            return true;
        }

        if (itemId.equals("combat_pack")) {
            erou.addBuffPotion(BuffPotion.BuffType.DAMAGE, quantity * 2);
            erou.addBuffPotion(BuffPotion.BuffType.DEFENSE, quantity * 2);
            erou.addBuffPotion(BuffPotion.BuffType.CRITICAL, quantity);
            return true;
        }

        return false;
    }

    // 🔢 Extract nivel din itemId cu fallback la nivelul eroului
    private int extractLevelFromId(String itemId, int fallback) {
        try {
            String[] parts = itemId.split("_");
            if (parts.length > 1) return Integer.parseInt(parts[1]);
        } catch (Exception ignore) {}
        return fallback;
    }

//    // 🎲 Raritate pentru shop corelată cu nivelul (reutilizează schema loot-ului)
//    private ObiectEchipament.Raritate pickShopRarityForLevel(int level) {
//        // Opțional: folosește aceleași praguri ca LootGenerator.determineRarity,
//        // dar “puțin mai generoase” pentru shop (simți că plătești pentru ceva mai bun)
//        double roll = com.rpg.utils.RandomUtils.randomDouble();
//
//        if (level >= 20 && roll < 0.10) return ObiectEchipament.Raritate.LEGENDARY; // +5%
//        if (level >= 15 && roll < 0.20) return ObiectEchipament.Raritate.EPIC;      // +5%
//        if (level >= 10 && roll < 0.30) return ObiectEchipament.Raritate.RARE;      // +5%
//        if (level >= 5 && roll < 0.50)  return ObiectEchipament.Raritate.UNCOMMON;  // +10%
//        return ObiectEchipament.Raritate.COMMON;
//    }
//
//    // 🏷️ “Shop flavor”: adaugă Lv. în nume și ajustează prețul ușor în sus
//    private ObiectEchipament applyShopFlavor(ObiectEchipament base) {
//        // Copie sigură (are createCopy() în model)
//        ObiectEchipament item = base.createCopy();
//
//        // 1) Nume: adaugă “Lv.X” dacă lipsește
//        if (!item.getNume().contains("Lv.")) {
//            item.setNume(item.getNume() + " Lv." + item.getNivelNecesar());
//        }
//
//        // 2) Preț: mic mark-up pentru cumpărare (ex: +15%)
//        int adjusted = (int) Math.max(1, Math.round(item.getPret() * 1.15));
//        item.setPret(adjusted);
//
//        // Enhancement/enchant rămân exact cum le-a decis generatorul
//        return item;
//    }

    /**
     * Helper pentru alegerea rarității în shop
     */
    private ObiectEchipament.Raritate pickShopRarityForLevel(int heroLevel) {
        // Shop-ul vinde iteme ușor mai bune decât loot-ul normal
        if (heroLevel >= 15) return ObiectEchipament.Raritate.RARE;
        if (heroLevel >= 10) return ObiectEchipament.Raritate.UNCOMMON;
        if (heroLevel >= 5) return ObiectEchipament.Raritate.UNCOMMON;
        return ObiectEchipament.Raritate.COMMON;
    }

    /**
     * Aplică "shop flavor" - prețuri mai bune și nume fancy
     */
    private ObiectEchipament applyShopFlavor(ObiectEchipament item) {
        // Reduce prețul cu 10% pentru shop
        int newPrice = (int)(item.getPret() * 0.9);
        item.setPret(Math.max(1, newPrice));

        return item;
    }


    /**
     * Vinde un item din inventar
     */
    public PurchaseResult sellItem(Erou erou, ObiectEchipament item) {
        int sellPrice = item.getPret() / 2; // 50% din prețul de cumpărare

        boolean removed = erou.getInventar().removeItem(item);

        if (!removed) {
            return new PurchaseResult(
                    false,
                    "Itemul nu a putut fi vândut!",
                    0
            );
        }

        erou.adaugaGold(sellPrice);

        return new PurchaseResult(
                true,
                "Ai vândut " + item.getNume() + " pentru " + sellPrice + " gold!",
                sellPrice
        );
    }

    /**
     * Verifică dacă eroul poate cumpăra un item
     */
    public boolean canAfford(Erou erou, ShopItemDTO item, int quantity) {
        return erou.getGold() >= (item.getPrice() * quantity);
    }

    /**
     * Calculează discount pentru pack-uri
     */
    public int calculateDiscount(String packId) {
        return switch (packId) {
            case "starter_pack" -> 10; // 10% discount
            case "combat_pack" -> 15; // 15% discount
            default -> 0;
        };
    }
}