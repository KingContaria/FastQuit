package com.kingcontaria.fastquit;

import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

/**
 * For ease of porting to older versions.
 */
public class TextHelper {

    public static final Text OFF = ScreenTexts.OFF;
    public static final Text DONE = ScreenTexts.DONE;

    public static Text translatable(String key, Object... args) {
        return new TranslatableText(key, args);
    }

    public static Text literal(String string) {
        return new LiteralText(string);
    }

    public static Text onOrOff(boolean on) {
        return ScreenTexts.onOrOff(on);
    }
}