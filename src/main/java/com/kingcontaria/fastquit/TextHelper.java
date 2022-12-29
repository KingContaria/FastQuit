package com.kingcontaria.fastquit;

import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

/**
 * Utility class for ease of porting to older Minecraft versions.
 */
public final class TextHelper {

    public static final Text OFF = ScreenTexts.OFF;
    public static final Text DONE = ScreenTexts.DONE;

    public static Text translatable(String key, Object... args) {
        return Text.translatable(key, args);
    }

    public static Text literal(String string) {
        return Text.literal(string);
    }

    public static Text onOrOff(boolean on) {
        return ScreenTexts.onOrOff(on);
    }
}