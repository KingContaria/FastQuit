package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import net.minecraft.client.gui.screen.world.EditWorldScreen;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;

@Mixin(EditWorldScreen.class)
public abstract class EditWorldScreenMixin {

    @Shadow @Final private LevelStorage.Session storageSession;

    @Inject(method = {"method_19931", "method_27029"}, at = @At("HEAD"), remap = false, cancellable = true)
    private void fastQuit_waitForSaveOnBackupOrOptimizeWorld_cancellable(CallbackInfo ci) {
        FastQuit.getSavingWorld(((SessionAccessor) this.storageSession).getDirectory().path()).ifPresent(server -> FastQuit.wait(Collections.singleton(server), ci));
    }
}