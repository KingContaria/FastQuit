package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.server.integrated.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow public abstract void setScreenAndRender(Screen screen);
    @Shadow protected abstract void render(boolean tick);

    @Unique private boolean stopping;

    @Redirect(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/integrated/IntegratedServer;isStopping()Z"))
    private boolean fastQuit(IntegratedServer server) {
        FastQuit.savingWorlds.add(server);
        return true;
    }

    // using MixinExtras' @WrapCondition would be perfect here, but it doubles the filesize and I doubt anyone else will redirect this call anyway
    @Redirect(method = "reset", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;render(Z)V"))
    private void fastQuit_doNotOpenSaveScreen(MinecraftClient client, boolean tick, Screen screen) {
        if (!(screen instanceof MessageScreen) || FastQuit.renderSavingScreen) {
            this.render(tick);
        }
    }

    @Inject(method = "scheduleStop", at = @At("HEAD"))
    private void fastQuit_waitForSaveOnShutdown(CallbackInfo ci) {
        this.fastQuit_waitForSave();
    }

    @Inject(method = "cleanUpAfterCrash", at = @At(value = "INVOKE", target = "Ljava/lang/System;gc()V", ordinal = 1))
    private void fastQuit_waitForSaveOnCrash(CallbackInfo ci) {
        this.fastQuit_waitForSave();
    }

    @ModifyArg(method = "cleanUpAfterCrash", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;disconnect(Lnet/minecraft/client/gui/screen/Screen;)V"))
    private Screen fastQuit_renderSaveAndQuitScreenOnCrash(Screen screen) {
        this.setScreenAndRender(screen);
        return screen;
    }

    private void fastQuit_waitForSave() {
        if (!this.stopping && !FastQuit.savingWorlds.isEmpty()) {
            this.stopping = true;
            FastQuit.wait(FastQuit.savingWorlds);
        }
    }
}