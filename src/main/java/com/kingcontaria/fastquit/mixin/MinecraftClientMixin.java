package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import com.kingcontaria.fastquit.TextHelper;
import com.kingcontaria.fastquit.WorldInfo;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.crash.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Redirect(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/integrated/IntegratedServer;isStopping()Z"))
    private boolean fastquit(IntegratedServer server) {
        FastQuit.savingWorlds.put(server, new WorldInfo());

        if (FastQuit.CONFIG.backgroundPriority != 0) {
            server.getThread().setPriority(FastQuit.CONFIG.backgroundPriority);
        }

        FastQuit.log("Disconnected \"" + server.getSaveProperties().getLevelName() + "\" from the client.");
        return true;
    }

    @WrapWithCondition(method = "reset", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;render(Z)V"))
    private boolean fastquit$doNotOpenSaveScreen(MinecraftClient client, boolean tick, Screen screen) {
        return FastQuit.CONFIG.renderSavingScreen || !(screen instanceof MessageScreen && screen.getTitle().equals(TextHelper.translatable("menu.savingLevel")));
    }

    @Inject(method = "stop", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;disconnect()V", shift = At.Shift.AFTER))
    private void fastquit$waitForSaveOnShutdown(CallbackInfo ci) {
        FastQuit.exit();
    }

    @Inject(method = "printCrashReport(Lnet/minecraft/client/MinecraftClient;Ljava/io/File;Lnet/minecraft/util/crash/CrashReport;)V", at = @At("HEAD"))
    private static void fastquit$waitForSaveOnCrash(MinecraftClient client, File file, CrashReport cr, CallbackInfo ci) {
        FastQuit.exit();
    }
}