package com.kingcontaria.fastquit;

import net.minecraft.client.gui.screen.LoadingDisplay;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

public class WaitingScreen extends MessageScreen {

    private final CallbackInfo cancellable;

    public WaitingScreen(Text text, @Nullable CallbackInfo cancellable) {
        super(text);
        if (cancellable != null && !cancellable.isCancellable()) {
            FastQuit.warn("Provided CallbackInfo for \"" + cancellable.getId() + "\" is not cancellable!");
            cancellable = null;
        }
        this.cancellable = cancellable;
    }

    @Override
    public void init() {
        if (this.cancellable != null) {
            this.addDrawableChild(ButtonWidget.builder(TextHelper.BACK, button -> this.close()).dimensions(this.width - 100 - 5, this.height - 20 - 5, 100, 20).build());
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        String loading = LoadingDisplay.get(Util.getMeasuringTimeMs());
        Objects.requireNonNull(this.client).textRenderer.draw(matrices, loading, (this.width - this.client.textRenderer.getWidth(loading)) / 2.0f, 95, 0x808080);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return cancellable != null;
    }

    @Override
    public void close() {
        super.close();
        if (cancellable != null) {
            cancellable.cancel();
        }
    }
}