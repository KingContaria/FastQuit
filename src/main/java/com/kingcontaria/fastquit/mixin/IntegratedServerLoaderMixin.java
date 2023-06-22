package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.server.integrated.IntegratedServerLoader;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;

@Mixin(IntegratedServerLoader.class)
public abstract class IntegratedServerLoaderMixin {

    @Shadow @Final private LevelStorage storage;
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "start(Lnet/minecraft/client/gui/screen/Screen;Ljava/lang/String;ZZ)V", at = @At("HEAD"), cancellable = true)
    private void fastquit$waitForSaveOnWorldLoad_cancellable(Screen parent, String levelName, boolean safeMode, boolean canShowBackupPrompt, CallbackInfo ci) {
        FastQuit.getSavingWorld(this.storage.getSavesDirectory().resolve(levelName)).ifPresent(server -> FastQuit.wait(Collections.singleton(server), ci));
        if (ci.isCancelled()) {
            this.client.setScreen(parent);
        }
    }
}