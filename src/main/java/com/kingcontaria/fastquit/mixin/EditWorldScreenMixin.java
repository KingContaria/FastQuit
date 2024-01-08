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

@Mixin(EditWorldScreen.class)
public abstract class EditWorldScreenMixin {

    @Shadow
    @Final
    private LevelStorage.Session storageSession;

    @Inject(
            method = {
                    "method_54595",
                    "method_54596"
            },
            at = @At("HEAD"),
            require = 2,
            remap = false,
            cancellable = true
    )
    private void waitForSaveOnBackupOrOptimizeWorld_cancellable(CallbackInfo ci) {
        FastQuit.getSavingWorld(this.storageSession).ifPresent(server -> FastQuit.wait(server, ci));
    }
}