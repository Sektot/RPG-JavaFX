package com.rpg.utils;

import com.rpg.model.abilities.Abilitate;
import com.rpg.model.abilities.ConfiguredAbility;
import com.rpg.model.abilities.AbilityTalent;
import com.rpg.model.abilities.AbilityVariant;
import com.rpg.model.abilities.TalentTier;
import com.rpg.model.characters.Erou;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for generating detailed ability tooltips
 */
public class AbilityTooltipGenerator {

    /**
     * Generate a comprehensive tooltip for an ability (with new ability system support)
     */
    public static String generateTooltip(Abilitate ability, Erou hero) {
        // Try to find configured ability for enhanced info
        ConfiguredAbility configured = findConfiguredAbility(ability, hero);
        if (configured != null) {
            return generateEnhancedTooltip(ability, configured, hero);
        }

        // Fallback to basic tooltip
        return generateBasicTooltip(ability, hero);
    }

    /**
     * Find the configured version of an ability
     */
    private static ConfiguredAbility findConfiguredAbility(Abilitate ability, Erou hero) {
        if (hero == null || hero.getAbilityLoadout() == null) return null;

        for (ConfiguredAbility configured : hero.getAbilityLoadout().getActiveAbilities()) {
            if (configured != null && configured.getBaseAbility().getNume().equals(ability.getNume())) {
                return configured;
            }
        }
        return null;
    }

    /**
     * Generate enhanced tooltip with tier/variant information
     */
    private static String generateEnhancedTooltip(Abilitate ability, ConfiguredAbility configured, Erou hero) {
        StringBuilder tooltip = new StringBuilder();

        // Header
        tooltip.append("╔════════════════════════════════════════╗\n");

        if (ability.isUltimate()) {
            tooltip.append("║ 🌟⚡ ULTIMATE ABILITY ⚡🌟            ║\n");
        }

        tooltip.append("║ ").append(centerText(ability.getNume(), 38)).append(" ║\n");

        // Show variant
        AbilityVariant variant = configured.getSelectedVariant();
        if (variant != null) {
            tooltip.append("║ 🎭 Variant: ").append(padRight(variant.getName(), 24)).append(" ║\n");
        }

        // Determine highest tier
        String tierDisplay = null;
        if (configured.getTier3Talent() != null) {
            tierDisplay = "Tier III";
        } else if (configured.getTier2Talent() != null) {
            tierDisplay = "Tier II";
        } else if (configured.getTier1Talent() != null) {
            tierDisplay = "Tier I";
        }

        if (tierDisplay != null) {
            tooltip.append("║ ⬆️  ").append(padRight(tierDisplay, 34)).append(" ║\n");
        }

        tooltip.append("╠════════════════════════════════════════╣\n");

        return tooltip.toString() + generateBasicTooltipContent(ability, hero, configured);
    }

    /**
     * Generate basic tooltip without tier/variant info
     */
    private static String generateBasicTooltip(Abilitate ability, Erou hero) {
        StringBuilder tooltip = new StringBuilder();
        tooltip.append("╔════════════════════════════════════════╗\n");

        if (ability.isUltimate()) {
            tooltip.append("║ 🌟⚡ ULTIMATE ABILITY ⚡🌟            ║\n");
        }

        tooltip.append("║ ").append(centerText(ability.getNume(), 38)).append(" ║\n");
        tooltip.append("╠════════════════════════════════════════╣\n");

        return tooltip.toString() + generateBasicTooltipContent(ability, hero, null);
    }

    /**
     * Generate the main content of the tooltip
     */
    private static String generateBasicTooltipContent(Abilitate ability, Erou hero, ConfiguredAbility configured) {
        StringBuilder tooltip = new StringBuilder();

        // Basic info
        tooltip.append(String.format("║ 💙 Cost: %-6d %-23s ║\n",
            ability.getCostMana(), hero.getTipResursa()));

        if (ability.getCooldown() > 0) {
            tooltip.append(String.format("║ ⏱️  Cooldown: %d turn%-22s ║\n",
                ability.getCooldown(), ability.getCooldown() > 1 ? "s" : ""));
        }

        if (ability.getRequiredLevel() > 1) {
            tooltip.append(String.format("║ 🔒 Required Level: %-22d ║\n",
                ability.getRequiredLevel()));
        }

        // Damage section
        if (ability.getDamage() > 0) {
            tooltip.append("╠════════════════════════════════════════╣\n");

            // Calculate scaled damage - use HashMap to avoid immutable map issues
            Map<String, Integer> statsMap = new java.util.HashMap<>();
            statsMap.put("strength", hero.getStrengthTotal());
            statsMap.put("dexterity", hero.getDexterityTotal());
            statsMap.put("intelligence", hero.getIntelligenceTotal());

            int scaledDamage = ability.calculeazaDamage(statsMap);

            tooltip.append(String.format("║ ⚔️  Damage: %d → %d%-20s ║\n",
                ability.getDamage(), scaledDamage, ""));

            // Show scaling
            Map<String, Double> scaling = ability.getInfluentaStatistici();
            if (scaling != null && !scaling.isEmpty()) {
                tooltip.append("║ 📊 Scaling:                            ║\n");
                for (Map.Entry<String, Double> entry : scaling.entrySet()) {
                    String statName = capitalizeFirst(entry.getKey());
                    double multiplier = entry.getValue();
                    // Safely get stat value with null check
                    Integer statValue = statsMap.get(entry.getKey());
                    if (statValue != null) {
                        int bonus = (int)(statValue * multiplier);
                        tooltip.append(String.format("║    • %s: %.1fx (+%d)%-16s ║\n",
                            statName, multiplier, bonus, ""));
                    }
                }
            }
        }

        // Multi-hit
        if (ability.getNumberOfHits() > 1) {
            tooltip.append("╠════════════════════════════════════════╣\n");

            Map<String, Integer> statsMapMulti = new java.util.HashMap<>();
            statsMapMulti.put("strength", hero.getStrengthTotal());
            statsMapMulti.put("dexterity", hero.getDexterityTotal());
            statsMapMulti.put("intelligence", hero.getIntelligenceTotal());

            int totalDamage = ability.calculeazaDamage(statsMapMulti) * ability.getNumberOfHits();
            tooltip.append(String.format("║ ⚔️  Multi-Hit: %dx hits%-21s ║\n",
                ability.getNumberOfHits(), ""));
            tooltip.append(String.format("║    Total Damage: %d%-21s ║\n",
                totalDamage, ""));
        }

        // AOE
        if (ability.isAOE()) {
            tooltip.append(String.format("║ 💥 Area of Effect%-23s ║\n", ""));
        }

        // Combo system
        if (ability.getComboRequirement() != null && !ability.getComboRequirement().isEmpty()) {
            tooltip.append("╠════════════════════════════════════════╣\n");
            tooltip.append(String.format("║ 🔥 COMBO ABILITY%-24s ║\n", ""));
            tooltip.append(String.format("║    Requires: %s%-22s ║\n",
                truncate(ability.getComboRequirement(), 22), ""));
            int bonusPercent = (int)(ability.getComboBonusDamage() * 100);
            tooltip.append(String.format("║    Bonus: +%d%% damage%-20s ║\n",
                bonusPercent, ""));
        }

        // Resource generation
        if (ability.getResourceGenerated() > 0) {
            tooltip.append("╠════════════════════════════════════════╣\n");
            tooltip.append(String.format("║ ⚡ Generates %d %s%-18s ║\n",
                ability.getResourceGenerated(),
                truncate(hero.getTipResursa(), 10), ""));
        }

        // Healing
        if (ability.getHealAmount() > 0 || ability.getHealPercent() > 0) {
            tooltip.append("╠════════════════════════════════════════╣\n");
            int totalHeal = ability.getHealAmount();
            if (ability.getHealPercent() > 0) {
                int percentHeal = (int)(hero.getViataMaxima() * ability.getHealPercent());
                totalHeal += percentHeal;
                tooltip.append(String.format("║ 💚 Heals: %d + %d%% max HP%-14s ║\n",
                    ability.getHealAmount(),
                    (int)(ability.getHealPercent() * 100), ""));
            } else {
                tooltip.append(String.format("║ 💚 Heals: %d HP%-26s ║\n",
                    ability.getHealAmount(), ""));
            }
            tooltip.append(String.format("║    Total: ~%d HP%-24s ║\n",
                totalHeal, ""));
        }

        // Self-damage
        if (ability.getSelfDamage() > 0) {
            tooltip.append("╠════════════════════════════════════════╣\n");
            tooltip.append(String.format("║ 💔 Costs %d HP to activate%-17s ║\n",
                ability.getSelfDamage(), ""));
        }

        // Buffs
        if (ability.getBuffAplicat() != null && !ability.getModificatoriBuff().isEmpty()) {
            tooltip.append("╠════════════════════════════════════════╣\n");
            tooltip.append(String.format("║ ✨ Buff: %s%-28s ║\n",
                truncate(ability.getBuffAplicat(), 28), ""));
            tooltip.append(String.format("║    Duration: %d turn%-21s ║\n",
                ability.getDurataBuff(), ability.getDurataBuff() > 1 ? "s" : ""));

            for (Map.Entry<String, Double> entry : ability.getModificatoriBuff().entrySet()) {
                String statName = capitalizeFirst(entry.getKey().replace("_", " "));
                int bonusPercent = (int)((entry.getValue() - 1.0) * 100);
                tooltip.append(String.format("║    • %s: +%d%%%-20s ║\n",
                    truncate(statName, 15), bonusPercent, ""));
            }
        }

        // Debuffs
        if (ability.getDebuffAplicat() != null && ability.getDurataDebuff() > 0) {
            tooltip.append("╠════════════════════════════════════════╣\n");
            tooltip.append(String.format("║ 🔥 Debuff: %s%-26s ║\n",
                truncate(ability.getDebuffAplicat(), 26), ""));
            tooltip.append(String.format("║    Duration: %d turn%-21s ║\n",
                ability.getDurataDebuff(), ability.getDurataDebuff() > 1 ? "s" : ""));
            if (ability.getDamageDebuff() > 0) {
                tooltip.append(String.format("║    DoT: %d damage/turn%-20s ║\n",
                    ability.getDamageDebuff(), ""));
            }
        }

        // Hit chance bonus
        if (ability.getHitChanceBonus() > 0) {
            tooltip.append("╠════════════════════════════════════════╣\n");
            tooltip.append(String.format("║ 🎯 Hit Chance: +%d%%%-23s ║\n",
                ability.getHitChanceBonus(), ""));
        }

        // 🆕 ACTIVE TALENTS (from configured ability)
        if (configured != null) {
            List<AbilityTalent> activeTalents = new ArrayList<>();
            if (configured.getTier1Talent() != null) activeTalents.add(configured.getTier1Talent());
            if (configured.getTier2Talent() != null) activeTalents.add(configured.getTier2Talent());
            if (configured.getTier3Talent() != null) activeTalents.add(configured.getTier3Talent());

            if (!activeTalents.isEmpty()) {
                tooltip.append("╠════════════════════════════════════════╣\n");
                tooltip.append(String.format("║ 🎓 ACTIVE TALENTS%-24s ║\n", ""));

                for (AbilityTalent talent : activeTalents) {
                    if (talent != null) {
                        String tierIndicator = talent.getTier() == TalentTier.TIER_1 ? "[I]" :
                                             talent.getTier() == TalentTier.TIER_2 ? "[II]" : "[III]";
                        tooltip.append(String.format("║  %s %s ║\n",
                            tierIndicator, padRight(truncate(talent.getName(), 31), 31)));
                    }
                }
            }

            // 🆕 UPGRADE INFO (show what can be improved)
            List<String> upgradeHints = new ArrayList<>();

            // Check for missing tier upgrades
            if (configured.getTier1Talent() == null) {
                upgradeHints.add("Tier I talent available");
            }
            if (configured.getTier2Talent() == null) {
                upgradeHints.add("Tier II talent available");
            }
            if (configured.getTier3Talent() == null) {
                upgradeHints.add("Tier III talent available");
            }

            if (!upgradeHints.isEmpty()) {
                tooltip.append("╠════════════════════════════════════════╣\n");
                tooltip.append(String.format("║ 📈 UPGRADES%-29s ║\n", ""));
                for (String hint : upgradeHints) {
                    tooltip.append(String.format("║    • %s ║\n",
                        padRight(hint, 32)));
                }
            }
        }

        tooltip.append("╚════════════════════════════════════════╝");

        return tooltip.toString();
    }

    /**
     * Generate a short one-line summary for ability selection menus
     */
    public static String generateShortTooltip(Abilitate ability, Erou hero) {
        List<String> tags = new ArrayList<>();

        // Ultimate
        if (ability.isUltimate()) {
            tags.add("🌟ULTIMATE");
        }

        // Damage
        if (ability.getDamage() > 0) {
            Map<String, Integer> statsMap = new java.util.HashMap<>();
            statsMap.put("strength", hero.getStrengthTotal());
            statsMap.put("dexterity", hero.getDexterityTotal());
            statsMap.put("intelligence", hero.getIntelligenceTotal());

            int scaledDamage = ability.calculeazaDamage(statsMap);
            tags.add("⚔️" + scaledDamage + " dmg");
        }

        // Multi-hit
        if (ability.getNumberOfHits() > 1) {
            tags.add(ability.getNumberOfHits() + "x hits");
        }

        // AOE
        if (ability.isAOE()) {
            tags.add("💥AOE");
        }

        // Combo
        if (ability.getComboRequirement() != null && !ability.getComboRequirement().isEmpty()) {
            tags.add("🔥COMBO");
        }

        // Resource generation
        if (ability.getResourceGenerated() > 0) {
            tags.add("⚡+" + ability.getResourceGenerated());
        }

        // Healing
        if (ability.getHealAmount() > 0 || ability.getHealPercent() > 0) {
            tags.add("💚Heal");
        }

        // Buff
        if (ability.getBuffAplicat() != null) {
            tags.add("✨Buff");
        }

        // Debuff
        if (ability.getDebuffAplicat() != null) {
            tags.add("🔥Debuff");
        }

        return String.join(" | ", tags);
    }

    /**
     * Generate a detailed description for ability examination
     */
    public static String generateDetailedDescription(Abilitate ability, Erou hero) {
        StringBuilder desc = new StringBuilder();

        // Basic description
        desc.append(ability.getNume()).append("\n\n");

        if (ability.isUltimate()) {
            desc.append("⚡ This is an ULTIMATE ability - use it wisely!\n\n");
        }

        // What it does
        desc.append("Effect:\n");

        if (ability.getDamage() > 0) {
            Map<String, Integer> statsMap = new java.util.HashMap<>();
            statsMap.put("strength", hero.getStrengthTotal());
            statsMap.put("dexterity", hero.getDexterityTotal());
            statsMap.put("intelligence", hero.getIntelligenceTotal());

            int scaledDamage = ability.calculeazaDamage(statsMap);
            desc.append("• Deals ").append(scaledDamage).append(" damage");

            if (ability.getNumberOfHits() > 1) {
                desc.append(" (").append(ability.getNumberOfHits()).append("x hits = ")
                    .append(scaledDamage * ability.getNumberOfHits()).append(" total)");
            }
            desc.append("\n");
        }

        if (ability.getComboRequirement() != null) {
            desc.append("• Combo: Use after '").append(ability.getComboRequirement())
                .append("' for +").append((int)(ability.getComboBonusDamage() * 100))
                .append("% damage!\n");
        }

        if (ability.getResourceGenerated() > 0) {
            desc.append("• Generates ").append(ability.getResourceGenerated())
                .append(" ").append(hero.getTipResursa()).append("\n");
        }

        if (ability.getHealAmount() > 0 || ability.getHealPercent() > 0) {
            int totalHeal = ability.getHealAmount() +
                (int)(hero.getViataMaxima() * ability.getHealPercent());
            desc.append("• Heals for approximately ").append(totalHeal).append(" HP\n");
        }

        if (ability.getSelfDamage() > 0) {
            desc.append("• Costs ").append(ability.getSelfDamage())
                .append(" HP to activate (high risk, high reward!)\n");
        }

        if (ability.getBuffAplicat() != null) {
            desc.append("• Applies '").append(ability.getBuffAplicat())
                .append("' buff for ").append(ability.getDurataBuff()).append(" turns\n");
        }

        if (ability.getDebuffAplicat() != null) {
            desc.append("• Inflicts '").append(ability.getDebuffAplicat())
                .append("' debuff for ").append(ability.getDurataDebuff()).append(" turns");
            if (ability.getDamageDebuff() > 0) {
                desc.append(" (").append(ability.getDamageDebuff()).append(" damage/turn)");
            }
            desc.append("\n");
        }

        return desc.toString();
    }

    // Helper methods
    private static String centerText(String text, int width) {
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text + " ".repeat(width - text.length() - padding);
    }

    private static String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    private static String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    private static String padRight(String text, int width) {
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        return text + " ".repeat(width - text.length());
    }
}
