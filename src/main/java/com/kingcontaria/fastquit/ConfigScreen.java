package com.kingcontaria.fastquit;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * FastQuit Config Screen, can be opened through ModMenu.
 */
public class ConfigScreen extends Screen {

    @Nullable
    private final Screen parent;

    public ConfigScreen(@Nullable Screen parent) {
        super(TextHelper.literal("FastQuit"));
        this.parent = parent;
    }

    @Override
    public void init() {
        this.addOptionText("showToasts", this.addDrawableChild(ButtonWidget.builder(TextHelper.onOrOff(FastQuit.showToasts), button -> button.setMessage(TextHelper.onOrOff(FastQuit.showToasts = !FastQuit.showToasts))).position(this.width / 2 + 10, 55).build()));
        this.addOptionText("renderSavingScreen", this.addDrawableChild(ButtonWidget.builder(TextHelper.onOrOff(FastQuit.renderSavingScreen), button -> button.setMessage(TextHelper.onOrOff(FastQuit.renderSavingScreen = !FastQuit.renderSavingScreen))).position(this.width / 2 + 10, 85).build()));
        this.addOptionText("backgroundPriority", this.addDrawableChild(new SliderWidget(this.width / 2 + 10, 115, 150, 20, getBackgroundPriorityText(), FastQuit.backgroundPriority / 10.0) {
            @Override
            protected void updateMessage() {
                this.setMessage(getBackgroundPriorityText());
            }

            @Override
            protected void applyValue() {
                FastQuit.backgroundPriority = (int) Math.round(this.value * 10);
            }
        }));
        this.addOptionText("showSavingTime", this.addDrawableChild(ButtonWidget.builder(getShowSavingTimeText(), button -> {
            FastQuit.showSavingTime = (FastQuit.showSavingTime + 1) % 3;
            button.setMessage(getShowSavingTimeText());
        }).position(this.width / 2 + 10, 145).build()));
        this.addDrawableChild(ButtonWidget.builder(TextHelper.DONE, button -> this.close()).position(this.width / 2 - 100, this.height / 6 + 168).width(200).build());
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 25, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void removed() {
        FastQuit.writeConfig("save");
    }

    @Override
    public void close() {
        Objects.requireNonNull(this.client).setScreen(this.parent);
    }

    /**
     * Creates a new {@link DrawableTextWithTooltip} for the option with the given translation key and adds it to the screen.
     *
     * @param reference - reference widget to determine y coordinate
     * @param optionKey - name of the option to create the translation keys
     */
    private void addOptionText(String optionKey, ClickableWidget reference) {
        this.addDrawableChild(new DrawableTextWithTooltip(this, this.textRenderer, TextHelper.translatable("options.fastquit." + optionKey), TextHelper.translatable("options.fastquit." + optionKey + ".description"), this.width / 2 - 160, reference.getY() + (20 - this.textRenderer.fontHeight) / 2, 160));
    }

    /**
     * @return - {@link Text} for {@link FastQuit#backgroundPriority} option
     */
    private static Text getBackgroundPriorityText() {
        return switch (FastQuit.backgroundPriority) {
            case 0 -> TextHelper.OFF;
            case 1, 2, 5, 10 -> TextHelper.translatable("options.fastquit.backgroundPriority." + FastQuit.backgroundPriority);
            default -> TextHelper.literal(String.valueOf(FastQuit.backgroundPriority));
        };
    }

    /**
     * @return - {@link Text} for {@link FastQuit#showSavingTime} option
     */
    private static Text getShowSavingTimeText() {
        if (FastQuit.showSavingTime == 1) {
            return TextHelper.translatable("options.fastquit.showSavingTime.toastsOnly");
        }
        return TextHelper.onOrOff(FastQuit.showSavingTime > 1);
    }
}