package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;
import net.minecraft.world.SaveProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Shadow @Final private Thread serverThread;
    @Shadow @Final protected SaveProperties saveProperties;

    @Inject(method = "exit", at = @At("RETURN"))
    private void fastQuit_finishSaving(CallbackInfo ci) {
        //noinspection ConstantConditions
        if ((Object) this instanceof IntegratedServer) {
            FastQuit.savingWorlds.remove((IntegratedServer) (Object) this);

            Text description = Text.translatable("toast.fastquit.description", this.saveProperties.getLevelName());
            if (FastQuit.showToasts) {
                MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.TUTORIAL_HINT, Text.translatable("toast.fastquit.title"), description));
            }
            FastQuit.log(description.getString());
        }
    }

    @Inject(method = "shutdown", at = @At("HEAD"))
    private void fastQuit_lowerThreadPriority(CallbackInfo ci) {
        //noinspection ConstantConditions
        if ((Object) this instanceof IntegratedServer && FastQuit.backgroundPriority != 0) {
            this.serverThread.setPriority(FastQuit.backgroundPriority);
        }
    }
}