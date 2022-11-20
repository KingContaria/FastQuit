package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.screen.world.EditWorldScreen;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EditWorldScreen.class)
public abstract class EditWorldScreenMixin {

    @WrapOperation(method = "commit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorage$Session;save(Ljava/lang/String;)V"))
    private void fastQuit_editWorldName(LevelStorage.Session session, String name, Operation<Void> original) {
        FastQuit.getSavingWorld(session.getDirectoryName()).ifPresentOrElse(server -> {
            synchronized (server.getSaveProperties()) {
                original.call(session, name);
                ((LevelInfoAccessor) (Object) ((LevelPropertiesAccessor) server.getSaveProperties()).getLevelInfo()).setName(name);
            }
        }, () -> original.call(session, name));
    }
}