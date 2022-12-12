package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import com.kingcontaria.fastquit.TextHelper;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.server.integrated.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow public abstract void setScreenAndRender(Screen screen);

    @Redirect(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/integrated/IntegratedServer;isStopping()Z"))
    private boolean fastQuit(IntegratedServer server) {
        FastQuit.savingWorlds.add(server);
        if (FastQuit.backgroundPriority != 0) {
            server.getThread().setPriority(FastQuit.backgroundPriority);
        }
        return true;
    }

    @WrapWithCondition(method = "reset", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;render(Z)V"))
    private boolean fastQuit_doNotOpenSaveScreen(MinecraftClient client, boolean tick, Screen screen) {
        return FastQuit.renderSavingScreen || !(screen instanceof MessageScreen && screen.getTitle().equals(TextHelper.translatable("menu.savingLevel")));
    }

    @Inject(method = "run", at = {@At("RETURN"), @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;printCrashReport(Lnet/minecraft/util/crash/CrashReport;)V")})
    private void fastQuit_waitForSaveOnShutdown(CallbackInfo ci) {
        if (!FastQuit.savingWorlds.isEmpty()) {
            FastQuit.wait(FastQuit.savingWorlds);
        }
    }

    @ModifyArg(method = "cleanUpAfterCrash", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;disconnect(Lnet/minecraft/client/gui/screen/Screen;)V"))
    private Screen fastQuit_renderSaveAndQuitScreenOnCrash(Screen screen) {
        if (!FastQuit.renderSavingScreen) {
            this.setScreenAndRender(screen);
        }
        return screen;
    }

    @ModifyReceiver(method = "cleanUpAfterCrash", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/integrated/IntegratedServer;stop(Z)V"))
    private IntegratedServer fastQuit_addDisconnectingServerOnCrash(IntegratedServer server, boolean join) {
        FastQuit.savingWorlds.add(server);
        return server;
    }
}