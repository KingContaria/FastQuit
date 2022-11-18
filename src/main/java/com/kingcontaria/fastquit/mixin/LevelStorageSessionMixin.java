package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.minecraft.world.level.storage.LevelStorage;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;

@Mixin(LevelStorage.Session.class)
public abstract class LevelStorageSessionMixin {

    @Shadow @Final private String directoryName;

    @Inject(method = "createBackup", at = @At("HEAD"))
    private void fastQuit_waitForSaveOnBackup(CallbackInfoReturnable<Long> cir) {
        FastQuit.getSavingWorld(this.directoryName).ifPresent(server -> FastQuit.wait(Collections.singleton(server)));
    }

    @WrapWithCondition(method = "backupLevelDataFile(Lnet/minecraft/util/registry/DynamicRegistryManager;Lnet/minecraft/world/SaveProperties;Lnet/minecraft/nbt/NbtCompound;)V", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false))
    private boolean fastQuit_doNotLogErrorServerSide(Logger logger, String s, Object o1, Object o2) {
        //noinspection ConstantConditions
        return FastQuit.isSavingWorld((LevelStorage.Session) (Object) this);
    }

    @WrapWithCondition(method = "deleteSessionLock", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false))
    private boolean fastQuit_doNotLogErrorClientSide(Logger logger, String s, Object o1, Object o2) {
        //noinspection ConstantConditions
        return !FastQuit.isSavingWorld((LevelStorage.Session) (Object) this);
    }

    @Unique private int extraTries;

    @ModifyConstant(method = "deleteSessionLock", constant = @Constant(intValue = 5, ordinal = 0))
    private int fastQuit_moreTriesForDeletion1(int tries) {
        //noinspection ConstantConditions
        if (FastQuit.isSavingWorld((LevelStorage.Session) (Object) this)) {
            this.extraTries++;
        }
        return tries + this.extraTries;
    }

    @ModifyConstant(method = "deleteSessionLock", constant = @Constant(intValue = 5, ordinal = 1))
    private int fastQuit_moreTriesForDeletion2(int tries) {
        return tries + this.extraTries;
    }
}