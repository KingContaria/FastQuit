package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.File;
import java.util.List;

@Mixin(LevelStorage.class)
public abstract class LevelStorageMixin {

    @Inject(method = "getLevelList", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false), locals = LocalCapture.CAPTURE_FAILHARD)
    private void fastQuit_addCurrentlySavingLevels(CallbackInfoReturnable<List<LevelSummary>> cir, List<LevelSummary> levelSummaries, File[] files, File[] var3, int var4, int var5, File file) {
        FastQuit.getSavingWorld(file.toPath()).ifPresent(server -> {
            synchronized (FastQuit.occupiedSessions) {
                LevelStorage.Session session = ((MinecraftServerAccessor) server).getSession();
                if (((SessionAccessor) session).getLock().isValid()) {
                    synchronized (session) {
                        levelSummaries.add(session.getLevelSummary());
                    }
                }
            }
        });
    }

    @WrapWithCondition(method = "getLevelList", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false))
    private boolean fastQuit_doNotLogWarning(Logger logger, String s, Object file, Object exception) {
        return !(file instanceof File) || FastQuit.getSavingWorld(((File) file).toPath()).isEmpty();
    }
}