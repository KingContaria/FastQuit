package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.SessionLock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;

@Mixin(LevelStorage.Session.class)
public abstract class LevelStorageSessionMixin {

    @Shadow @Final LevelStorage.LevelSave directory;
    @Shadow @Final private String directoryName;

    // this now acts as a fallback in case the method gets called from somewhere else than EditWorldScreen
    @Inject(method = "createBackup", at = @At("HEAD"))
    private void fastQuit_waitForSaveOnBackup(CallbackInfoReturnable<Long> cir) {
        FastQuit.getSavingWorld(this.directory.path()).ifPresent(server -> FastQuit.wait(Collections.singleton(server)));
    }

    @WrapOperation(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/SessionLock;close()V"))
    private void fastQuit_synchronizeSessionClose_fallback(SessionLock lock, Operation<Void> original) {
        synchronized (FastQuit.occupiedSessions) {
            if (!FastQuit.occupiedSessions.remove(this)) {
                original.call(lock);
            } else {
                FastQuit.warn("Something tried to close the \"" + this.directoryName + "\" session without checking if it was occupied. Please open an issue on github!");
            }
        }
    }
}