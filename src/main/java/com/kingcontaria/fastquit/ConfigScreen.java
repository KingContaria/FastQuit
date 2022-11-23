package com.kingcontaria.fastquit;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigScreen extends Screen {
    private final Screen parent;
    private final List<DrawableTextWithTooltip> texts = new ArrayList<>();

    public ConfigScreen(Screen parent) {
        super(Text.literal("FastQuit"));
        this.parent = parent;
    }

    @Override
    public void init() {
        this.addDrawableChild(new ButtonWidget(this.width / 2 + 5, 55, 150, 20, TextHelper.onOrOff(FastQuit.showToasts), button -> button.setMessage(TextHelper.onOrOff(FastQuit.showToasts = !FastQuit.showToasts))));
        this.addDrawableChild(new ButtonWidget(this.width / 2 + 5, 85, 150, 20, TextHelper.onOrOff(FastQuit.renderSavingScreen), button -> button.setMessage(TextHelper.onOrOff(FastQuit.renderSavingScreen = !FastQuit.renderSavingScreen))));
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
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height / 6 + 168, 200, 20, TextHelper.DONE, button -> this.close()));

        int textX = this.width / 2 - 155;
        int textYOffset = (20 - this.textRenderer.fontHeight) / 2;
        this.texts.add(new DrawableTextWithTooltip(this, this.textRenderer, TextHelper.translatable("options.fastquit.showToasts"), TextHelper.translatable("options.fastquit.showToasts.description"), textX, 55 + textYOffset));
        this.texts.add(new DrawableTextWithTooltip(this, this.textRenderer, TextHelper.translatable("options.fastquit.renderSavingScreen"), TextHelper.translatable("options.fastquit.renderSavingScreen.description"), textX, 85 + textYOffset));
        this.texts.add(new DrawableTextWithTooltip(this, this.textRenderer, TextHelper.translatable("options.fastquit.backgroundPriority"), TextHelper.translatable("options.fastquit.backgroundPriority.description"), textX, 115 + textYOffset));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 25, 0xFFFFFF);
        for (DrawableTextWithTooltip text : this.texts) {
            text.render(matrices, mouseX, mouseY, delta);
        }
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        for (DrawableTextWithTooltip text : this.texts) {
            if (text.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void close() {
        try {
            FastQuit.writeConfig();
        } catch (IOException e) {
            FastQuit.error("Failed to save config!", e);
        }
        assert this.client != null;
        this.client.setScreen(this.parent);
    }

    private static Text getBackgroundPriorityText() {
        return switch (FastQuit.backgroundPriority) {
            case 0 -> TextHelper.OFF;
            case 1, 2, 5, 10 -> TextHelper.translatable("options.fastquit.backgroundPriority." + FastQuit.backgroundPriority);
            default -> TextHelper.literal(String.valueOf(FastQuit.backgroundPriority));
        };
    }
}