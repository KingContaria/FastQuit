package com.kingcontaria.fastquit;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.List;

public class DrawableTextWithTooltip implements Drawable, Element, Selectable {

    /**
     * Last narrated {@link DrawableTextWithTooltip}.
     */
    private static DrawableTextWithTooltip lastNarration;

    private final Screen parentScreen;
    private final TextRenderer textRenderer;
    private final Text text;
    private final Text tooltip;
    private final int x;
    private final int y;
    private final int maxWidth;

    public DrawableTextWithTooltip(Screen parentScreen, TextRenderer textRenderer, Text text, Text tooltip, int x, int y, int maxWidth) {
        this.parentScreen = parentScreen;
        this.textRenderer = textRenderer;
        this.text = text;
        this.tooltip = tooltip;
        this.x = x;
        this.y = y;
        this.maxWidth = maxWidth;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        List<OrderedText> lines = this.textRenderer.wrapLines(this.text, this.maxWidth);
        int yOffset = -(((this.textRenderer.fontHeight + 2) / 2) * (lines.size() - 1));
        for (OrderedText line : lines) {
            this.textRenderer.draw(matrices, line, this.x, this.y + yOffset, 0xFFFFFF);
            yOffset += this.textRenderer.fontHeight + 2;
        }
        if (this.isMouseOver(mouseX, mouseY)) {
            this.parentScreen.renderOrderedTooltip(matrices, this.textRenderer.wrapLines(this.tooltip, 200), mouseX, mouseY);
            if (lastNarration != this) {
                MinecraftClient.getInstance().getNarratorManager().narrate(this.text);
                lastNarration = this;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY)) {
            MinecraftClient.getInstance().getNarratorManager().narrate(this.tooltip);
            return true;
        }
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        List<OrderedText> lines = this.textRenderer.wrapLines(this.text, this.maxWidth);
        return mouseX >= this.x && mouseX <= this.x + lines.stream().mapToInt(this.textRenderer::getWidth).max().orElse(0) && mouseY >= this.y - ((this.textRenderer.fontHeight + 2) / 2.0f) * (lines.size() - 1)  && mouseY <= this.y + this.textRenderer.fontHeight + ((this.textRenderer.fontHeight + 2) / 2.0f) * (lines.size() - 1);
    }

    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {
        // only here to implement Selectable, so it can be added to the config screen properly
    }

    @Override
    public void setFocused(boolean focused) {
        // Can't be focused so we just keep this empty
    }

    @Override
    public boolean isFocused() {
        return false;
    }
}