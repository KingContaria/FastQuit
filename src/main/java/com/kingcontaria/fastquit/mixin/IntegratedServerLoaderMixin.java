package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import net.minecraft.server.integrated.IntegratedServerLoader;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;

@Mixin(IntegratedServerLoader.class)
public abstract class IntegratedServerLoaderMixin {

    @Shadow @Nullable protected abstract LevelStorage.Session createSession(String levelName);

    @Inject(method = "createSession", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false), cancellable = true)
    private void fastQuit_waitForSaveOnWorldLoad(String levelName, CallbackInfoReturnable<LevelStorage.Session> cir) {
        FastQuit.savingWorlds.stream().filter(server -> ((MinecraftServerAccessor) server).getSession().getDirectoryName().equals(levelName)).findFirst().ifPresent(server -> {
            FastQuit.wait(Collections.singleton(server));
            cir.setReturnValue(this.createSession(levelName));
        });
    }
}