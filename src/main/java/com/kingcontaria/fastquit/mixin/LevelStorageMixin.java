package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelStorage.class)
public abstract class LevelStorageMixin {

    @Inject(method = "method_43418", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"), cancellable = true, remap = false)
    private void fastQuit_addCurrentlySavingLevels(LevelStorage.LevelSave levelSave, CallbackInfoReturnable<LevelSummary> cir) {
        synchronized (FastQuit.occupiedSessions) {
            FastQuit.getSavingWorld(levelSave.getRootPath()).ifPresent(server -> cir.setReturnValue(((MinecraftServerAccessor) server).getSession().getLevelSummary()));
        }
    }
}