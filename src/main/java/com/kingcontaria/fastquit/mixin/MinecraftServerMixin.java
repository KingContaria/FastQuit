package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Inject(method = "exit", at = @At("RETURN"))
    private void fastQuit_finishSaving(CallbackInfo ci) {
        //noinspection ConstantConditions
        if ((Object) this instanceof IntegratedServer) {
            FastQuit.savingWorlds.remove((IntegratedServer) (Object) this);
        }
    }
}