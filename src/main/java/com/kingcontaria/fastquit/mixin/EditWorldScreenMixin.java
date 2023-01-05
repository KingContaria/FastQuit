package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.screen.world.EditWorldScreen;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.integrated.IntegratedServerLoader;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;

@Mixin(EditWorldScreen.class)
public abstract class EditWorldScreenMixin {

    @Shadow @Final private LevelStorage.Session storageSession;

    @WrapOperation(method = "method_29068", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/integrated/IntegratedServerLoader;createSaveLoader(Lnet/minecraft/world/level/storage/LevelStorage$Session;Z)Lnet/minecraft/server/SaveLoader;", remap = true), remap = false)
    private SaveLoader fastQuit_synchronizeExportingWorldGenSettings(IntegratedServerLoader serverLoader, LevelStorage.Session session, boolean safeMode, Operation<SaveLoader> original) {
        synchronized (session) {
            return original.call(serverLoader, session, safeMode);
        }
    }

    @Inject(method = {"method_19931", "method_27029"}, at = @At("HEAD"), remap = false, cancellable = true)
    private void fastQuit_waitForSaveOnBackupOrOptimizeWorld_cancellable(CallbackInfo ci) {
        FastQuit.getSavingWorld(((SessionAccessor) this.storageSession).getDirectory().path()).ifPresent(server -> FastQuit.wait(Collections.singleton(server), ci));
    }
}