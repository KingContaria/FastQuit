package com.kingcontaria.fastquit;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class DrawableTextWithTooltip implements Drawable, Element {

    private static DrawableTextWithTooltip lastNarration;
    private final Screen screen;
    private final TextRenderer textRenderer;
    private final Text text;
    private final Text tooltip;
    private final int x;
    private final int y;

    public DrawableTextWithTooltip(Screen screen, TextRenderer textRenderer, Text text, Text tooltip, int x, int y) {
        this.screen = screen;
        this.textRenderer = textRenderer;
        this.text = text;
        this.tooltip = tooltip;
        this.x = x;
        this.y = y;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.textRenderer.draw(matrices, this.text, this.x, this.y, 0xFFFFFF);
        if (this.isMouseOverText(mouseX, mouseY)) {
            this.screen.renderOrderedTooltip(matrices, this.textRenderer.wrapLines(this.tooltip, 200), mouseX, mouseY);
            if (lastNarration != this) {
                MinecraftClient.getInstance().getNarratorManager().narrate(this.text);
                lastNarration = this;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOverText(mouseX, mouseY)) {
            MinecraftClient.getInstance().getNarratorManager().narrate(this.tooltip);
            return true;
        }
        return false;
    }

    private boolean isMouseOverText(double mouseX, double mouseY) {
        return mouseX > this.x && mouseX < this.x + this.textRenderer.getWidth(this.text) && mouseY > this.y && mouseY < this.y + this.textRenderer.fontHeight;
    }
}
