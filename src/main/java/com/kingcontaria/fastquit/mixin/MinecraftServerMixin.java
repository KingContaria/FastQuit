package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import com.kingcontaria.fastquit.TextHelper;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Shadow @Final private static Logger LOGGER;

    @Inject(method = "exit", at = @At("RETURN"))
    private void fastQuit_finishSaving(CallbackInfo ci) {
        //noinspection ConstantConditions
        if ((Object) this instanceof IntegratedServer server) {
            String key = "toast.fastquit.";
            if (!Boolean.TRUE.equals(FastQuit.savingWorlds.remove(server))) {
                key += "description";
            } else {
                key += "deleted";
            }

            Text description = TextHelper.translatable(key, server.getSaveProperties().getLevelName());
            if (FastQuit.showToasts) {
                MinecraftClient.getInstance().submit(() -> MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.WORLD_BACKUP, TextHelper.translatable("toast.fastquit.title"), description)));
            }
            FastQuit.log(description.getString());
        }
    }

    @WrapOperation(method = "shutdown", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorage$Session;close()V"))
    private void fastQuit_synchronizeSessionClose(LevelStorage.Session session, Operation<Void> original) {
        synchronized (FastQuit.occupiedSessions) {
            if (!FastQuit.occupiedSessions.remove(session)) {
                original.call(session);
            }
        }
    }

    @WrapOperation(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorage$Session;backupLevelDataFile(Lnet/minecraft/registry/DynamicRegistryManager;Lnet/minecraft/world/SaveProperties;Lnet/minecraft/nbt/NbtCompound;)V"))
    private void fastQuit_synchronizeLevelDataSave(LevelStorage.Session session, DynamicRegistryManager registryManager, SaveProperties saveProperties, NbtCompound nbt, Operation<Void> original) {
        synchronized (session) {
            if (!Boolean.TRUE.equals(FastQuit.savingWorlds.get(this))) {
                original.call(session, registryManager, saveProperties, nbt);
            }
        }
    }

    @WrapWithCondition(method = "shutdown", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;saveAllPlayerData()V"))
    private boolean fastQuit_cancelPlayerSavingIfDeleted(PlayerManager playerManager) {
        if (Boolean.TRUE.equals(FastQuit.savingWorlds.get(this))) {
            LOGGER.info("Cancelled saving players because level was deleted");
            return false;
        }
        return true;
    }

    @Inject(method = "save", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;next()Ljava/lang/Object;"), cancellable = true)
    private void fastQuit_cancelSavingIfDeleted(CallbackInfoReturnable<Boolean> cir) {
        if (Boolean.TRUE.equals(FastQuit.savingWorlds.get(this))) {
            LOGGER.info("Cancelled saving worlds because level was deleted");
            cir.setReturnValue(false);
        }
    }
}