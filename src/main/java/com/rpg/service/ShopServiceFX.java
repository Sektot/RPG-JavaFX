package com.rpg.service;

import com.rpg.model.characters.Erou;
import com.rpg.model.items.BuffPotion;
import com.rpg.model.items.EnchantScroll;
import com.rpg.model.items.ObiectEchipament;
import com.rpg.service.dto.PurchaseResult;
import com.rpg.service.dto.ShopItemDTO;

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

    private Map<String, ObiectEchipament> generatedSamples = new HashMap<>();

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

//    /**
//     * ✨ GENERATOR INTEGRAT cu logica din ObiectEchipament
//     */
//    private ObiectEchipament generateShopEquipment(String itemId, int heroLevel) {
//        int itemLevel = extractLevelFromId(itemId, heroLevel);
//        boolean isWeapon = itemId.startsWith("weapon_");
//
//        // 1) Alege raritatea (folosește enumul existent din model)
//        ObiectEchipament.Raritate raritate = rollRarityForLevel(itemLevel);
//
//        // 2) Tipul
//        ObiectEchipament.TipEchipament tip = isWeapon
//                ? ObiectEchipament.TipEchipament.WEAPON
//                : ObiectEchipament.TipEchipament.ARMOR;
//
//        // 3) Nume (helper în shop, dar modelul gestionează +X automat)
//        String baseName = isWeapon ? generateWeaponName(raritate) : generateArmorName(raritate);
//        String fullName = baseName + " Lv." + itemLevel;
//
//        // 4) Bonusuri MINIMALE pentru constructorul complet
//        int str = 0, dex = 0, intl = 0, def = 0;
//
//        // Distribuit random pe stat-uri bazate pe tip și raritate
//        int statBudget = (int)((itemLevel + 2) * raritate.getMultiplier());
//        java.util.Random r = new java.util.Random();
//
//        if (isWeapon) {
//            // Arme -> focus pe stats ofensive
//            str = statBudget / 2 + r.nextInt(Math.max(1, statBudget / 4));
//            dex = statBudget / 4 + r.nextInt(Math.max(1, statBudget / 6));
//            intl = r.nextInt(Math.max(1, statBudget / 6));
//        } else {
//            // Armuri -> focus pe defense și health
//            def = statBudget / 2 + r.nextInt(Math.max(1, statBudget / 4));
//            str = statBudget / 6 + r.nextInt(Math.max(1, statBudget / 8));
//            dex = r.nextInt(Math.max(1, statBudget / 8));
//            intl = r.nextInt(Math.max(1, statBudget / 8));
//        }
//
//        // 5) Preț bazat pe nivel și multiplierul din raritate
//        int basePrice = 100 + itemLevel * 15;
//        int price = (int)(basePrice * raritate.getMultiplier());
//
//        // 6) Construiește obiectul folosind constructorul COMPLET din model
//        ObiectEchipament item = new ObiectEchipament(
//                fullName, itemLevel, raritate, tip,
//                str, dex, intl, def, price
//        );
//
//        // 7) Bonusuri SPECIALE prin setBonuses (folosește API-ul din model)
//        java.util.Map<String, Integer> extraBonuses = generateSpecialBonuses(isWeapon, itemLevel, raritate);
//
//        // Combină cu bonusurile din constructor
//        java.util.Map<String, Integer> allBonuses = item.getBonuses();
//        allBonuses.putAll(extraBonuses);
//        item.setBonuses(allBonuses);
//
//        // 8) Enhancement random pentru rarități înalte (folosește API-ul din model)
//        if (raritate.ordinal() >= ObiectEchipament.Raritate.RARE.ordinal()) {
//            int enhanceChance = switch (raritate) {
//                case RARE -> 20;      // 20% șansă +1
//                case EPIC -> 40;      // 40% șansă +1-2
//                case LEGENDARY -> 60; // 60% șansă +1-3
//                default -> 0;
//            };
//
//            if (r.nextInt(100) < enhanceChance) {
//                int enhanceLevel = 1 + r.nextInt(Math.min(3, raritate.ordinal()));
//                item.setEnhancementLevel(enhanceLevel); // Modelul gestionează automat bonusurile
//                System.out.printf("✨ Enhanced to +%d: %s\n", enhanceLevel, item.getNume());
//            }
//        }
//
//        System.out.printf("🛍️ Generated %s: %s (%s) - %d gold\n",
//                isWeapon ? "WEAPON" : "ARMOR",
//                item.getNume(), raritate.getDisplayName(), item.getPret());
//
//        return item;
//    }
//
//    /**
//     * 🎲 Generează raritatea random bazată pe nivel
//     */
//    private ObiectEchipament.Raritate rollRarityForLevel(int level) {
//        java.util.Random r = new java.util.Random();
//        double roll = r.nextDouble() * 100;
//        double bonus = Math.min(level * 1.5, 25); // max +25% la nivel înalt
//
//        if (roll < (5 + bonus)) return ObiectEchipament.Raritate.LEGENDARY; // 5-30%
//        if (roll < (15 + bonus/2)) return ObiectEchipament.Raritate.EPIC;   // 10-25%
//        if (roll < 35) return ObiectEchipament.Raritate.RARE;               // 20%
//        if (roll < 65) return ObiectEchipament.Raritate.UNCOMMON;           // 30%
//        return ObiectEchipament.Raritate.COMMON;                            // 35%
//    }
//
//    /**
//     * ⚡ Generează bonusuri speciale bazate pe tip și raritate
//     */
//    private java.util.Map<String, Integer> generateSpecialBonuses(boolean isWeapon, int level, ObiectEchipament.Raritate raritate) {
//        java.util.Map<String, Integer> bonuses = new java.util.HashMap<>();
//        java.util.Random r = new java.util.Random();
//
//        double rarityMult = raritate.getMultiplier();
//
//        if (isWeapon) {
//            // 🗡️ BONUSURI ARMĂ
//            int damage = (int)((8 + level * 2) * rarityMult);
//            bonuses.put("Damage", damage);
//
//            // Bonusuri condiționale pe raritate
//            if (raritate.ordinal() >= ObiectEchipament.Raritate.UNCOMMON.ordinal()) {
//                bonuses.put("crit_chance", 2 + raritate.ordinal() * 2);
//            }
//
//            if (raritate.ordinal() >= ObiectEchipament.Raritate.RARE.ordinal()) {
//                bonuses.put("hit_chance", 5 + raritate.ordinal() * 3);
//            }
//
//            if (raritate == ObiectEchipament.Raritate.LEGENDARY) {
//                // Legendare au bonus special random
//                String[] specialBonuses = {"lifesteal", "mana_steal", "elemental_damage"};
//                String special = specialBonuses[r.nextInt(specialBonuses.length)];
//                bonuses.put(special, 3 + level / 4);
//            }
//
//        } else {
//            // 🛡️ BONUSURI ARMURĂ
//            int health = (int)((15 + level * 4) * rarityMult);
//            bonuses.put("health", health);
//
//            if (raritate.ordinal() >= ObiectEchipament.Raritate.UNCOMMON.ordinal()) {
//                bonuses.put("dodge_chance", 1 + raritate.ordinal() * 2);
//            }
//
//            if (raritate.ordinal() >= ObiectEchipament.Raritate.RARE.ordinal()) {
//                bonuses.put("damage_reduction", 2 + raritate.ordinal() * 2);
//            }
//
//            if (raritate == ObiectEchipament.Raritate.LEGENDARY) {
//                // Legendare au rezistențe elementale
//                bonuses.put("fire_resistance", 10 + level);
//                bonuses.put("ice_resistance", 10 + level);
//            }
//        }
//
//        // 💰 Bonus gold find pentru rarități înalte
//        if (raritate.ordinal() >= ObiectEchipament.Raritate.EPIC.ordinal()) {
//            bonuses.put("gold_find", 5 + raritate.ordinal() * 5);
//        }
//
//        return bonuses;
//    }
//
//    /**
//     * 🗡️ Generează nume pentru arme bazate pe raritate
//     */
//    private String generateWeaponName(ObiectEchipament.Raritate raritate) {
//        String[] prefixes = switch (raritate) {
//            case LEGENDARY -> new String[]{"Apocalipsa", "Ragnarok", "Excalibur", "Mjolnir", "Durendal"};
//            case EPIC -> new String[]{"Flacăra", "Furia", "Ghilotina", "Răzbunarea", "Tempesta"};
//            case RARE -> new String[]{"Sabia", "Toporul", "Lancea", "Pumnalul", "Arcul"};
//            case UNCOMMON -> new String[]{"Lama", "Spada", "Ciocanul", "Băţul", "Suliţa"};
//            case COMMON -> new String[]{"Sabia", "Ciomagul", "Toporul", "Cuţitul", "Băţul"};
//        };
//
//        String[] suffixes = switch (raritate) {
//            case LEGENDARY -> new String[]{"Eternității", "Zeilor", "Infinitului", "Dragonului de Aur"};
//            case EPIC -> new String[]{"Dragonului", "Phoenixului", "Titanului", "Cavalerului Negru"};
//            case RARE -> new String[]{"Războinicului", "Vânătorului", "Lordului", "Campionului"};
//            case UNCOMMON -> new String[]{"Vitejiei", "Puterii", "Rapidității", "Preciziei"};
//            case COMMON -> new String[]{"Începătorului", "Soldatului", "Novicului", "Orășenului"};
//        };
//
//        java.util.Random r = new java.util.Random();
//        return prefixes[r.nextInt(prefixes.length)] + " " + suffixes[r.nextInt(suffixes.length)];
//    }
//
//    /**
//     * 🛡️ Generează nume pentru armuri bazate pe raritate
//     */
//    private String generateArmorName(ObiectEchipament.Raritate raritate) {
//        String[] prefixes = switch (raritate) {
//            case LEGENDARY -> new String[]{"Armura", "Platoșa", "Vestmântul", "Mantaua", "Coiful"};
//            case EPIC -> new String[]{"Armura", "Platoșa", "Vestmântul", "Haina", "Tuneca"};
//            case RARE -> new String[]{"Armura", "Vestmântul", "Platoșa", "Haina", "Tuneca"};
//            case UNCOMMON -> new String[]{"Armura", "Haina", "Vestmântul", "Tuneca", "Cămașa"};
//            case COMMON -> new String[]{"Haina", "Vestmântul", "Cămașa", "Tuneca", "Bluza"};
//        };
//
//        String[] suffixes = switch (raritate) {
//            case LEGENDARY -> new String[]{"Nemuritorului", "Zeului", "Eternității", "Dragonului Sacru"};
//            case EPIC -> new String[]{"Dragonului", "Phoenixului", "Titanului", "Gardianului"};
//            case RARE -> new String[]{"Războinicului", "Protectorului", "Apărătorului", "Lordului"};
//            case UNCOMMON -> new String[]{"Apărării", "Rezistentei", "Forței", "Curajului"};
//            case COMMON -> new String[]{"Începătorului", "Soldatului", "Novicului", "Orășenului"};
//        };
//
//        java.util.Random r = new java.util.Random();
//        return prefixes[r.nextInt(prefixes.length)] + " " + suffixes[r.nextInt(suffixes.length)];
//    }

    // 🔢 Extract nivel din itemId cu fallback la nivelul eroului
    private int extractLevelFromId(String itemId, int fallback) {
        try {
            String[] parts = itemId.split("_");
            if (parts.length > 1) return Integer.parseInt(parts[1]);
        } catch (Exception ignore) {}
        return fallback;
    }

    // 🎲 Raritate pentru shop corelată cu nivelul (reutilizează schema loot-ului)
    private ObiectEchipament.Raritate pickShopRarityForLevel(int level) {
        // Opțional: folosește aceleași praguri ca LootGenerator.determineRarity,
        // dar “puțin mai generoase” pentru shop (simți că plătești pentru ceva mai bun)
        double roll = com.rpg.utils.RandomUtils.randomDouble();

        if (level >= 20 && roll < 0.10) return ObiectEchipament.Raritate.LEGENDARY; // +5%
        if (level >= 15 && roll < 0.20) return ObiectEchipament.Raritate.EPIC;      // +5%
        if (level >= 10 && roll < 0.30) return ObiectEchipament.Raritate.RARE;      // +5%
        if (level >= 5 && roll < 0.50)  return ObiectEchipament.Raritate.UNCOMMON;  // +10%
        return ObiectEchipament.Raritate.COMMON;
    }

    // 🏷️ “Shop flavor”: adaugă Lv. în nume și ajustează prețul ușor în sus
    private ObiectEchipament applyShopFlavor(ObiectEchipament base) {
        // Copie sigură (are createCopy() în model)
        ObiectEchipament item = base.createCopy();

        // 1) Nume: adaugă “Lv.X” dacă lipsește
        if (!item.getNume().contains("Lv.")) {
            item.setNume(item.getNume() + " Lv." + item.getNivelNecesar());
        }

        // 2) Preț: mic mark-up pentru cumpărare (ex: +15%)
        int adjusted = (int) Math.max(1, Math.round(item.getPret() * 1.15));
        item.setPret(adjusted);

        // Enhancement/enchant rămân exact cum le-a decis generatorul
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