package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import net.minecraft.client.realms.gui.screen.RealmsUploadScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.util.Collections;

@Mixin(RealmsUploadScreen.class)
public abstract class RealmsUploadScreenMixin {

    @Inject(method = "tarGzipArchive", at = @At("HEAD"))
    private void fastquit$waitForSaveOnRealmsUpload(File pathToDirectoryFile, CallbackInfoReturnable<File> cir) {
        FastQuit.getSavingWorld(pathToDirectoryFile.toPath()).ifPresent(server -> FastQuit.wait(Collections.singleton(server)));
    }
}