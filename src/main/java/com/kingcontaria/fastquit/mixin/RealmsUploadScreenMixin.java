package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import net.minecraft.client.realms.dto.RealmsWorldOptions;
import net.minecraft.client.realms.gui.screen.RealmsUploadScreen;
import net.minecraft.client.realms.util.RealmsUploader;
import net.minecraft.client.realms.util.UploadProgressTracker;
import net.minecraft.client.session.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.nio.file.Path;

@Mixin(RealmsUploader.class)
public abstract class RealmsUploadScreenMixin {

    @Inject(
            method = "<init>",
            at = @At("HEAD")
    )
    private static void fastquit$waitForSaveOnRealmsUpload(Path directory, RealmsWorldOptions options, Session session, long worldId, int slotId, UploadProgressTracker progressTracker, CallbackInfo ci) {
        FastQuit.getSavingWorld(directory).ifPresent(FastQuit::wait);
    }
}