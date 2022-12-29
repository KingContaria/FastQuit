package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.realms.gui.screen.RealmsUploadScreen;
import net.minecraft.text.Text;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.Objects;

@Mixin(RealmsUploadScreen.class)
public abstract class RealmsUploadScreenMixin extends Screen {

    @Shadow @Final private LevelSummary selectedLevel;

    protected RealmsUploadScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "upload", at = @At("HEAD"))
    private void fastQuit_waitForSaveOnRealmsUpload(CallbackInfo ci) {
        FastQuit.getSavingWorld(Objects.requireNonNull(this.client).getLevelStorage().getSavesDirectory().resolve(this.selectedLevel.getName())).ifPresent(server -> FastQuit.wait(Collections.singleton(server)));
    }
}