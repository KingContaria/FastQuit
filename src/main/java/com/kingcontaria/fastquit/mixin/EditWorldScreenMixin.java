package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.screen.world.EditWorldScreen;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.integrated.IntegratedServerLoader;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EditWorldScreen.class)
public abstract class EditWorldScreenMixin {

    @WrapOperation(method = "commit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorage$Session;save(Ljava/lang/String;)V"))
    private void fastQuit_editWorldName(LevelStorage.Session session, String name, Operation<Void> original) {
        FastQuit.getSavingWorld(session.getDirectoryName()).ifPresentOrElse(server -> {
            synchronized (((MinecraftServerAccessor) server).getSession()) {
                original.call(session, name);
                ((LevelInfoAccessor) (Object) ((LevelPropertiesAccessor) server.getSaveProperties()).getLevelInfo()).setName(name);
            }
        }, () -> original.call(session, name));
    }

    @WrapOperation(method = "method_29068", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/integrated/IntegratedServerLoader;createSaveLoader(Lnet/minecraft/world/level/storage/LevelStorage$Session;Z)Lnet/minecraft/server/SaveLoader;"))
    private SaveLoader fastQuit_synchronizeExportingWorldGenSettings(IntegratedServerLoader serverLoader, LevelStorage.Session session, boolean safeMode, Operation<SaveLoader> original) {
        synchronized (session) {
            return original.call(serverLoader, session, safeMode);
        }
    }
}