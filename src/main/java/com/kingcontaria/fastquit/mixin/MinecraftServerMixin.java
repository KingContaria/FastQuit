package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;
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
            String key;
            if (FastQuit.savingWorlds.remove((IntegratedServer) (Object) this)) {
                key = "toast.fastquit.description";
            } else {
                key = "toast.fastquit.deleted";
            }

            Text description = Text.translatable(key, this.saveProperties.getLevelName());
            if (FastQuit.showToasts) {
                FastQuit.scheduledToasts.add(new SystemToast(SystemToast.Type.WORLD_BACKUP, Text.translatable("toast.fastquit.title"), description));
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

    @WrapOperation(method = "shutdown", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorage$Session;close()V"))
    private void fastQuit_synchronizedSessionClose(LevelStorage.Session session, Operation<?> original) {
        synchronized (FastQuit.occupiedSessions) {
            if (!FastQuit.occupiedSessions.remove(session)) {
                original.call(session);
            }
        }
    }
}