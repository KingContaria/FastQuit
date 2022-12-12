package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Function4;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.EditWorldScreen;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Function;

@Mixin(EditWorldScreen.class)
public abstract class EditWorldScreenMixin {

    @WrapOperation(method = "commit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorage$Session;save(Ljava/lang/String;)V"))
    private void fastQuit_editWorldName(LevelStorage.Session session, String name, Operation<Void> original) {
        FastQuit.getSavingWorld(((SessionAccessor) session).getDirectory()).ifPresentOrElse(server -> {
            synchronized (session) {
                original.call(session, name);
                ((LevelInfoAccessor) (Object) ((LevelPropertiesAccessor) server.getSaveProperties()).getLevelInfo()).setName(name);
            }
        }, () -> original.call(session, name));
    }

    @WrapOperation(method = "method_29068", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;createIntegratedResourceManager(Lnet/minecraft/util/registry/DynamicRegistryManager$Impl;Ljava/util/function/Function;Lcom/mojang/datafixers/util/Function4;ZLnet/minecraft/world/level/storage/LevelStorage$Session;)Lnet/minecraft/client/MinecraftClient$IntegratedResourceManager;", remap = true), remap = false)
    private MinecraftClient.IntegratedResourceManager fastQuit_synchronizeExportingWorldGenSettings(MinecraftClient client, DynamicRegistryManager.Impl registryManager, Function<LevelStorage.Session, DataPackSettings> dataPackSettingsGetter, Function4<LevelStorage.Session, DynamicRegistryManager.Impl, ResourceManager, DataPackSettings, SaveProperties> savePropertiesGetter, boolean safeMode, LevelStorage.Session session, Operation<MinecraftClient.IntegratedResourceManager> original) {
        synchronized (session) {
            return original.call(client, registryManager, dataPackSettingsGetter, savePropertiesGetter, safeMode, session);
        }
    }
}