package com.kingcontaria.fastquit;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * FastQuit Config Screen, can be opened through ModMenu.
 */
public class ConfigScreen extends Screen {

    private final Screen parent;
    private final List<DrawableTextWithTooltip> texts = new ArrayList<>();

    public ConfigScreen(Screen parent) {
        super(TextHelper.literal("FastQuit"));
        this.parent = parent;
    }

    @Override
    public void init() {
        this.addDrawableChild(ButtonWidget.builder(TextHelper.onOrOff(FastQuit.showToasts), button -> button.setMessage(TextHelper.onOrOff(FastQuit.showToasts = !FastQuit.showToasts))).position(this.width / 2 + 5, 55).build());
        this.addDrawableChild(ButtonWidget.builder(TextHelper.onOrOff(FastQuit.renderSavingScreen), button -> button.setMessage(TextHelper.onOrOff(FastQuit.renderSavingScreen = !FastQuit.renderSavingScreen))).position(this.width / 2 + 5, 85).build());
        this.addDrawableChild(new SliderWidget(this.width / 2 + 5, 115, 150, 20, getBackgroundPriorityText(), FastQuit.backgroundPriority / 10.0) {
            @Override
            protected void updateMessage() {
                this.setMessage(getBackgroundPriorityText());
            }

            @Override
            protected void applyValue() {
                FastQuit.backgroundPriority = (int) Math.round(this.value * 10);
            }
        });
        this.addDrawableChild(ButtonWidget.builder(TextHelper.DONE, button -> this.close()).position(this.width / 2 - 100, this.height / 6 + 168).width(200).build());

        this.addOptionText("showToasts", 55);
        this.addOptionText("renderSavingScreen", 85);
        this.addOptionText("backgroundPriority", 115);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 25, 0xFFFFFF);
        this.texts.forEach(text -> text.render(matrices, mouseX, mouseY, delta));
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button) || this.texts.stream().anyMatch(text -> text.mouseClicked(mouseX, mouseY, button));
    }

    @Override
    public void close() {
        try {
            FastQuit.writeConfig();
        } catch (IOException e) {
            FastQuit.error("Failed to save config!", e);
        }
        Objects.requireNonNull(this.client).setScreen(this.parent);
    }

    /**
     * Creates a new {@link DrawableTextWithTooltip} for the option with the given translation key and adds it to the screen.
     */
    private void addOptionText(String optionKey, int y) {
        this.texts.add(new DrawableTextWithTooltip(this, this.textRenderer, TextHelper.translatable("options.fastquit." + optionKey), TextHelper.translatable("options.fastquit." + optionKey + ".description"), this.width / 2 - 155, y + (20 - this.textRenderer.fontHeight) / 2));
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
}