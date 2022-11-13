package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow protected abstract void render(boolean tick);
    @Shadow public abstract void setScreen(@Nullable Screen screen);

    @Unique private volatile boolean stopping;

    @Redirect(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;reset(Lnet/minecraft/client/gui/screen/Screen;)V"))
    private void fastQuit_doNotOpenSaveScreen(MinecraftClient client, Screen screen) {
    }

    @Redirect(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/integrated/IntegratedServer;isStopping()Z"))
    private boolean fastQuit(IntegratedServer server) {
        FastQuit.savingWorlds.add(server);
        return true;
    }

    @Inject(method = "scheduleStop", at = @At("HEAD"))
    private void fastQuit_waitForSaveOnShutdown(CallbackInfo ci) {
        this.fastQuit_waitForSave();
    }

    @Inject(method = "cleanUpAfterCrash", at = @At("HEAD"))
    private void fastQuit_waitForSaveOnCrash(CallbackInfo ci) {
        this.fastQuit_waitForSave();
    }

    private void fastQuit_waitForSave() {
        if (!this.stopping && !FastQuit.savingWorlds.isEmpty()) {
            this.stopping = true;
            String stillSaving = "Waiting for " + (FastQuit.savingWorlds.size() == 1 ? "'" + FastQuit.savingWorlds.get(0).getSaveProperties().getLevelName() + "'" : "worlds") + " to finish saving...";
            this.setScreen(new MessageScreen(Text.literal(stillSaving)));
            FastQuit.log(stillSaving);
            while(FastQuit.savingWorlds.stream().anyMatch((server) -> !server.isStopping())) {
                this.render(false);
            }
            FastQuit.log("Done.");
        }
    }
}