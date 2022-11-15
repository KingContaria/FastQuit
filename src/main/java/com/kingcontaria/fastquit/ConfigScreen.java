package com.kingcontaria.fastquit;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.io.IOException;

public class ConfigScreen extends Screen {
    private final Screen parent;
    private Text showToasts;
    private Text showToastsDescription;
    private Text backgroundPriority;
    private Text backgroundPriorityDescription;

    public ConfigScreen(Screen parent) {
        super(Text.literal("FastQuit"));
        this.parent = parent;
    }

    @Override
    public void init() {
        this.addDrawableChild(new ButtonWidget(this.width / 2 + 5, 55, 150, 20, ScreenTexts.onOrOff(FastQuit.showToasts), button -> button.setMessage(ScreenTexts.onOrOff(FastQuit.showToasts = !FastQuit.showToasts))));
        this.addDrawableChild(new SliderWidget(this.width / 2 + 5, 85, 150, 20, getBackgroundPriorityText(), FastQuit.backgroundPriority / 10.0) {
            @Override
            protected void updateMessage() {
                this.setMessage(getBackgroundPriorityText());
            }

            @Override
            protected void applyValue() {
                FastQuit.backgroundPriority = (int) Math.round(this.value * 10);
            }
        });
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height / 6 + 168, 200, 20, ScreenTexts.DONE, button -> this.close()));
        this.showToasts = Text.translatable("options.fastquit.showToasts");
        this.showToastsDescription = Text.translatable("options.fastquit.showToasts.description");
        this.backgroundPriority = Text.translatable("options.fastquit.backgroundPriority");
        this.backgroundPriorityDescription = Text.translatable("options.fastquit.backgroundPriority.description");
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 25, 0xFFFFFF);
        float textX = this.width / 2.0f - 155;
        float textYOffset = (20 - this.textRenderer.fontHeight) / 2.0f;
        this.textRenderer.draw(matrices, this.showToasts, textX, 55 + textYOffset, 0xFFFFFF);
        this.textRenderer.draw(matrices, this.backgroundPriority, textX, 85 + textYOffset, 0xFFFFFF);
        if (mouseX > textX && mouseX < textX + this.textRenderer.getWidth(this.showToasts) && mouseY > 55 + textYOffset && mouseY < 55 + textYOffset + this.textRenderer.fontHeight) {
            this.renderOrderedTooltip(matrices, this.textRenderer.wrapLines(this.showToastsDescription, 200), mouseX, mouseY);
        } else if (mouseX > textX && mouseX < textX + this.textRenderer.getWidth(this.backgroundPriority) && mouseY > 85 + textYOffset && mouseY < 85 + textYOffset + this.textRenderer.fontHeight) {
            this.renderOrderedTooltip(matrices, this.textRenderer.wrapLines(this.backgroundPriorityDescription, 200), mouseX, mouseY);
        }
        super.render(matrices, mouseX, mouseY, delta);
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
            case 0 -> ScreenTexts.OFF;
            case 1 -> Text.translatable("options.fastquit.backgroundPriority.min");
            case 2 -> Text.translatable("options.fastquit.backgroundPriority.recommended");
            case 5 -> Text.translatable("options.fastquit.backgroundPriority.default");
            case 10 -> Text.translatable("options.fastquit.backgroundPriority.max");
            default -> Text.literal(String.valueOf(FastQuit.backgroundPriority));
        };
    }
}