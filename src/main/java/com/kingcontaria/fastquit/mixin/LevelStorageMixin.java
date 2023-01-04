package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;
import java.util.Collections;

@Mixin(LevelStorage.class)
public abstract class LevelStorageMixin {

    @Shadow @Final Path savesDirectory;

    @Inject(method = "createSession", at = @At("HEAD"))
    private void fastQuit_waitForSaveOnSessionCreation(String levelName, CallbackInfoReturnable<LevelStorage.Session> cir) {
        FastQuit.getSavingWorld(this.savesDirectory.resolve(levelName)).ifPresent(server -> FastQuit.wait(Collections.singleton(server)));
    }

    @Inject(method = "method_43418", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"), cancellable = true, remap = false)
    private void fastQuit_addCurrentlySavingLevelsToWorldList(LevelStorage.LevelSave levelSave, CallbackInfoReturnable<LevelSummary> cir) {
        FastQuit.getSavingWorld(levelSave.path()).ifPresent(server -> {
            synchronized (FastQuit.occupiedSessions) {
                LevelStorage.Session session = ((MinecraftServerAccessor) server).getSession();
                if (((SessionAccessor) session).getLock().isValid()) {
                    synchronized (session) {
                        cir.setReturnValue(session.getLevelSummary());
                    }
                }
            }
        });
    }
}