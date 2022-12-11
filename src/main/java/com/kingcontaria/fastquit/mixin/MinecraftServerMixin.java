package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import com.kingcontaria.fastquit.TextHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
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

    @Shadow @Final protected SaveProperties saveProperties;

    @Inject(method = "exit", at = @At("RETURN"))
    private void fastQuit_finishSaving(CallbackInfo ci) {
        //noinspection ConstantConditions
        if ((Object) this instanceof IntegratedServer) {
            String key = "toast.fastquit.";
            if (FastQuit.savingWorlds.remove((IntegratedServer) (Object) this)) {
                key += "description";
            } else {
                key += "deleted";
            }

            Text description = TextHelper.translatable(key, this.saveProperties.getLevelName());
            if (FastQuit.showToasts) {
                MinecraftClient.getInstance().submit(() -> MinecraftClient.getInstance().getToastManager().add(new SystemToast(SystemToast.Type.WORLD_BACKUP, TextHelper.translatable("toast.fastquit.title"), description)));
            }
            FastQuit.log(description.getString());
        }
    }

    @WrapOperation(method = "shutdown", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorage$Session;close()V"))
    private void fastQuit_synchronizedSessionClose(LevelStorage.Session session, Operation<Void> original) {
        //noinspection ConstantConditions
        if ((Object) this instanceof IntegratedServer) {
            synchronized (FastQuit.occupiedSessions) {
                if (!FastQuit.occupiedSessions.remove(session)) {
                    original.call(session);
                }
            }
        }
    }

    @WrapOperation(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorage$Session;backupLevelDataFile(Lnet/minecraft/registry/DynamicRegistryManager;Lnet/minecraft/world/SaveProperties;Lnet/minecraft/nbt/NbtCompound;)V"))
    private void fastQuit_synchronizedLevelDataSave(LevelStorage.Session session, DynamicRegistryManager registryManager, SaveProperties saveProperties, NbtCompound nbt, Operation<Void> original) {
        //noinspection ConstantConditions
        if ((Object) this instanceof IntegratedServer) {
            synchronized (session) {
                original.call(session, registryManager, saveProperties, nbt);
            }
        }
    }
}