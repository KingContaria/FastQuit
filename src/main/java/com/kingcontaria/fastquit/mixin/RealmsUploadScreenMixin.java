package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import net.minecraft.client.realms.gui.screen.RealmsUploadScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.io.File;
import java.util.Collections;

@Mixin(RealmsUploadScreen.class)
public abstract class RealmsUploadScreenMixin {

    @ModifyArg(method = "method_22106", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/realms/FileUpload;<init>(Ljava/io/File;JILnet/minecraft/client/realms/dto/UploadInfo;Lnet/minecraft/client/util/Session;Ljava/lang/String;Lnet/minecraft/client/realms/UploadStatus;)V", remap = true), index = 0, remap = false)
    private File fastQuit_waitForSaveOnRealmsUpload(File file) {
        FastQuit.getSavingWorld(file.toPath()).ifPresent(server -> FastQuit.wait(Collections.singleton(server)));
        return file;
    }
}