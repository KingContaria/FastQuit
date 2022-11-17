package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow public abstract void setScreenAndRender(Screen screen);

    @Shadow @Final private ToastManager toastManager;

    @Unique private Set<IntegratedServer> cached;

    @Redirect(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/integrated/IntegratedServer;isStopping()Z"))
    private boolean fastQuit(IntegratedServer server) {
        FastQuit.savingWorlds.add(server);
        this.cached = new HashSet<>(FastQuit.savingWorlds);
        return true;
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;clear()V", shift = At.Shift.AFTER))
    private void fastQuit_reAddClearedToasts(Screen screen, CallbackInfo ci) {
        if (this.cached != null) {
            this.cached.removeAll(FastQuit.savingWorlds);
            this.cached.forEach(server -> this.toastManager.add(new SystemToast(SystemToast.Type.TUTORIAL_HINT, Text.translatable("toast.fastquit.title"), Text.translatable("toast.fastquit.description", server.getSaveProperties().getLevelName()))));
            this.cached = null;
        }
    }

    @WrapWithCondition(method = "reset", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;render(Z)V"))
    private boolean fastQuit_doNotOpenSaveScreen(MinecraftClient client, boolean tick, Screen screen) {
        return FastQuit.renderSavingScreen || !(screen instanceof MessageScreen && screen.getTitle().equals(Text.translatable("menu.savingLevel")));
    }

    @Inject(method = "run", at = @At("RETURN"))
    private void fastQuit_waitForSaveOnShutdown(CallbackInfo ci) {
        if (!FastQuit.savingWorlds.isEmpty()) {
            FastQuit.wait(FastQuit.savingWorlds);
        }
    }

    @ModifyArg(method = "cleanUpAfterCrash", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;disconnect(Lnet/minecraft/client/gui/screen/Screen;)V"))
    private Screen fastQuit_renderSaveAndQuitScreenOnCrash(Screen screen) {
        this.setScreenAndRender(screen);
        return screen;
    }
}