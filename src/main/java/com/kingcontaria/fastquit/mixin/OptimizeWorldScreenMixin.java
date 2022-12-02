package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.OptimizeWorldScreen;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;

@Mixin(OptimizeWorldScreen.class)
public abstract class OptimizeWorldScreenMixin {

    @Inject(method = "create", at = @At("HEAD"))
    private static void fastQuit_waitForSaveOnOptimizeWorld(MinecraftClient client, BooleanConsumer callback, DataFixer dataFixer, LevelStorage.Session session, boolean eraseCache, CallbackInfoReturnable<OptimizeWorldScreen> cir) {
        FastQuit.getSavingWorld(((SessionAccessor) session).getDirectory().path()).ifPresent(server -> FastQuit.wait(Collections.singleton(server)));
    }
}