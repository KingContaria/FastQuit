package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.Entity;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
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

    @Shadow @Final private SoundManager soundManager;
    @Shadow @Nullable public Entity cameraEntity;
    @Shadow @Nullable private ClientConnection integratedServerConnection;

    @Unique private volatile boolean stopping;

    @Redirect(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;reset(Lnet/minecraft/client/gui/screen/Screen;)V"))
    private void fastQuit_doNotOpenSaveScreen(MinecraftClient client, Screen screen) {
        this.soundManager.stopAll();
        this.cameraEntity = null;
        this.integratedServerConnection = null;
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

            Text stillSaving = Text.translatable("screen.fastquit.waiting", String.join(" & ", FastQuit.savingWorlds.stream().map(server -> server.getSaveProperties().getLevelName()).toList()));
            this.setScreen(new MessageScreen(stillSaving));
            FastQuit.log(stillSaving.getString());

            FastQuit.savingWorlds.forEach(server -> server.getThread().setPriority(Thread.MAX_PRIORITY));

            while (FastQuit.savingWorlds.stream().anyMatch(server -> !server.isStopping())) {
                this.render(false);
            }
        }
    }
}