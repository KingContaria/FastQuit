package com.kingcontaria.fastquit;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.LoadingDisplay;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

public class WaitingScreen extends MessageScreen {

    private final CallbackInfo callbackInfo;

    public WaitingScreen(Text text, @Nullable CallbackInfo callbackInfo) {
        super(text);
        if (callbackInfo != null && !callbackInfo.isCancellable()) {
            FastQuit.warn("Provided CallbackInfo for \"" + callbackInfo.getId() + "\" is not cancellable!");
            callbackInfo = null;
        }
        this.callbackInfo = callbackInfo;
    }

    @Override
    public void init() {
        if (this.callbackInfo != null) {
            this.addDrawableChild(ButtonWidget.builder(TextHelper.BACK, button -> this.close()).dimensions(this.width - 100 - 5, this.height - 20 - 5, 100, 20).build());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        String loading = LoadingDisplay.get(Util.getMeasuringTimeMs());
        context.drawText(Objects.requireNonNull(this.client).textRenderer, loading, (this.width - this.client.textRenderer.getWidth(loading)) / 2, 95, 0x808080, false);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return this.callbackInfo != null;
    }

    @Override
    public void close() {
        super.close();
        if (this.callbackInfo != null) {
            this.callbackInfo.cancel();
        }
    }
}