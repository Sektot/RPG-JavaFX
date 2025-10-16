package com.rpg.utils;

import com.rpg.model.characters.Erou;
import com.rpg.model.characters.classes.Ardelean;
import com.rpg.model.characters.classes.Moldovean;
import com.rpg.model.characters.classes.Oltean;


/**
 * Clasă helper pentru afișarea one-liners în timpul bătăliilor.
 * Sistemul românesc de dialoguri amuzante.
 */
public class BattleOneLiners {


    /**
     * Afișează one-liner când eroul face un atac normal.
     */
    public static void displayNormalAttackOneLiner(Erou erou) {
        String[] lines = getNormalAttackLines(erou);
        if (lines != null && lines.length > 0) {
            String randomLine = lines[(int)(Math.random() * lines.length)];
            System.out.println("\n" + randomLine);
            pauseShort();
        }
    }

    /**
     * Returnează linii pentru atacuri normale pe clase.
     */
    private static String[] getNormalAttackLines(Erou erou) {
        if (erou instanceof Moldovean) {
            return new String[]{
                    "⚔️ \"Ai vrut, ai primit!\"",
                    "💪 \"Numa' bine, numa' tare!\"",
                    "🥊 \"Te-am capsat!\"",
                    "⚡ \"Prindi Karata!\""
            };
        } else if (erou instanceof Ardelean) {
            return new String[]{
                    "✨ \"Să-ți explic eu acum...\"",
                    "🔮 \"Magie pură de Cluj!\"",
                    "📚 \"Asta se învață la Cluj, fratelo!\"",
                    "⚡ \"Lovitură din creier la creier!\""
            };
        } else if (erou instanceof Oltean) {
            return new String[]{
                    "🗡️ \"Spăl cuțitu-n tine\"",
                    "⚡ \"Ești prea încet, boss!\"",
                    "🎯 \"Joc de glezne fraiere\"",
                    "💨 \"Hai noroc, te-am pupat!\""
            };
        }
        return null;
    }



    /**
     * Afișează one-liner când eroul evită un atac.
     */
    public static void displayDodgeOneLiner(Erou erou) {
        String[] lines = getDodgeLines(erou);
        if (lines != null && lines.length > 0) {
            String randomLine = lines[(int)(Math.random() * lines.length)];
            System.out.println("\n💨 " + randomLine);
            pauseShort();
        }
    }

    /**
     * Returnează linii când evită atacuri.
     */
    private static String[] getDodgeLines(Erou erou) {
        if (erou instanceof Moldovean) {
            return new String[]{
                    "Vez cî dai pi lîngî!",
                    "Nu mă prinzi tu pe mine!",
                    "Am reflex bun de la muncă!",
                    "Încearcă mai bine data viitoare!"
            };
        } else if (erou instanceof Ardelean) {
            return new String[]{
                    "Am calculat traiectoria fraiere!",
                    "Nice try Diddy, dar nu!",
                    "Prea lent chiar și pentru mine!",
                    "Am prevăzut lovitura, fratele meleu!"
            };
        } else if (erou instanceof Oltean) {
            return new String[]{
                    "Prea încet, boss!",
                    "Prea prost să mă prinzi!",
                    "M-ai ratat rău de tot!",
                    "Hai noroc boss că m-am dat la o parte!"
            };
        }
        return null;
    }

    /**
     * Pauză scurtă pentru efecte dramatice.
     */
    private static void pauseShort() {
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}