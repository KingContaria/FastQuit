package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import com.kingcontaria.fastquit.TextHelper;
import com.kingcontaria.fastquit.WorldInfo;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.server.integrated.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Redirect(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/integrated/IntegratedServer;isStopping()Z"))
    private boolean fastQuit(IntegratedServer server) {
        FastQuit.savingWorlds.put(server, new WorldInfo());
        if (FastQuit.backgroundPriority != 0) {
            server.getThread().setPriority(FastQuit.backgroundPriority);
        }
        FastQuit.log("Disconnected \"" + server.getSaveProperties().getLevelName() + "\" from the client.");
        return true;
    }

    @WrapWithCondition(method = "reset", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;render(Z)V"))
    private boolean fastQuit_doNotOpenSaveScreen(MinecraftClient client, boolean tick, Screen screen) {
        return FastQuit.renderSavingScreen || !(screen instanceof MessageScreen && screen.getTitle().equals(TextHelper.translatable("menu.savingLevel")));
    }

    @Inject(method = "stop", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;disconnect()V", shift = At.Shift.AFTER))
    private void fastQuit_waitForSaveOnShutdown(CallbackInfo ci) {
        FastQuit.exit();
    }

    @Inject(method = "printCrashReport", at = @At("HEAD"))
    private static void fastQuit_waitForSaveOnCrash(CallbackInfo ci) {
        FastQuit.exit();
    }
}