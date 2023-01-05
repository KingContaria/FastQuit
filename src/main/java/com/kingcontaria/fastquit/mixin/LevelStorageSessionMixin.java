package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import com.kingcontaria.fastquit.plugin.Synchronized;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.SessionLock;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.util.Collections;

@Mixin(LevelStorage.Session.class)
public abstract class LevelStorageSessionMixin {

    @Shadow @Final LevelStorage.LevelSave directory;
    @Shadow @Final private String directoryName;

    @Synchronized
    @Shadow public abstract WorldSaveHandler createSaveHandler();

    @Synchronized
    @Shadow public abstract @Nullable LevelSummary getLevelSummary();

    @Synchronized
    @Shadow public abstract @Nullable Pair<SaveProperties, DimensionOptionsRegistryHolder.DimensionsConfig> readLevelProperties(DynamicOps<NbtElement> ops, DataConfiguration dataConfiguration, Registry<DimensionOptions> dimensionOptionsRegistry, Lifecycle lifecycle);

    @Synchronized
    @Shadow public abstract @Nullable DataConfiguration getDataPackSettings();

    @Synchronized
    @Shadow public abstract void backupLevelDataFile(DynamicRegistryManager registryManager, SaveProperties saveProperties, @Nullable NbtCompound nbt);

    @Synchronized
    @Shadow public abstract void deleteSessionLock() throws IOException;

    @Synchronized
    @Shadow public abstract void save(String name) throws IOException;

    // this now acts as a fallback in case the method gets called from somewhere else than EditWorldScreen
    @Inject(method = "createBackup", at = @At("HEAD"))
    private void fastQuit_waitForSaveOnBackup(CallbackInfoReturnable<Long> cir) {
        FastQuit.getSavingWorld(this.directory.path()).ifPresent(server -> FastQuit.wait(Collections.singleton(server)));
    }

    @Inject(method = "save", at = @At("TAIL"))
    private void fastQuit_editWorldName(String name, CallbackInfo ci) {
        FastQuit.getSavingWorld(this.directory.path()).ifPresent(server -> ((LevelInfoAccessor) (Object) ((LevelPropertiesAccessor) server.getSaveProperties()).getLevelInfo()).setName(name));
    }

    @Inject(method = "deleteSessionLock", at = @At("TAIL"))
    private void fastQuit_deleteWorld(CallbackInfo ci) {
        synchronized (FastQuit.savingWorlds) {
            FastQuit.getSavingWorld(this.directory.path()).ifPresent(server -> FastQuit.savingWorlds.get(server).deleted = true);
        }
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

    @Inject(method = "checkValid", at = @At("HEAD"))
    private void fastQuit_warnIfNotSynchronizedSessionAccess(CallbackInfo ci) {
        if (!Thread.holdsLock(this) && FastQuit.getSavingWorld(this.directory.path()).isPresent()) {
            FastQuit.warn("Un-synchronized access to \"" + this.directoryName + "\" session!");
        }
    }
}