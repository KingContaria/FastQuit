package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.level.storage.LevelStorage;
import org.slf4j.Logger;
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

    @Inject(method = "createBackup", at = @At("HEAD"))
    private void fastQuit_waitForSaveOnBackup(CallbackInfoReturnable<Long> cir) {
        FastQuit.getSavingWorld(this.directory.path()).ifPresent(server -> FastQuit.wait(Collections.singleton(server)));
    }

    @WrapWithCondition(method = "backupLevelDataFile(Lnet/minecraft/util/registry/DynamicRegistryManager;Lnet/minecraft/world/SaveProperties;Lnet/minecraft/nbt/NbtCompound;)V", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false))
    private boolean fastQuit_doNotLogErrorIfDeleted(Logger logger, String s, Object o1, Object o2) {
        IntegratedServer server = MinecraftClient.getInstance().getServer();
        //noinspection ConstantConditions
        return (server != null && ((MinecraftServerAccessor) server).getSession() == (Object) this) || FastQuit.getSavingWorld(this.directory.path()).isPresent();
    }
}