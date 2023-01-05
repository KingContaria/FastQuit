package com.kingcontaria.fastquit.mixin;

import com.kingcontaria.fastquit.FastQuit;
import com.kingcontaria.fastquit.WorldInfo;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(WorldListWidget.WorldEntry.class)
public abstract class WorldListWidgetWorldEntryMixin {

    @Shadow @Final private SelectWorldScreen screen;
    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private LevelSummary level;

    @WrapOperation(method = {"delete", "edit", "recreate"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorage;createSession(Ljava/lang/String;)Lnet/minecraft/world/level/storage/LevelStorage$Session;"))
    private LevelStorage.Session fastQuit_editSavingWorld(LevelStorage storage, String directoryName, Operation<LevelStorage.Session> original) {
        Optional<IntegratedServer> server = FastQuit.getSavingWorld(storage.getSavesDirectory().resolve(directoryName));
        if (server.isPresent()) {
            synchronized (FastQuit.occupiedSessions) {
                LevelStorage.Session session = ((MinecraftServerAccessor) server.get()).getSession();
                if (((SessionAccessor) session).getLock().isValid()) {
                    FastQuit.occupiedSessions.add(session);
                    return session;
                }
            }
        }
        return original.call(storage, directoryName);
    }

    @WrapOperation(method = {"delete", "method_27032", "recreate"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelStorage$Session;close()V"))
    private void fastQuit_synchronizeSessionClose(LevelStorage.Session session, Operation<Void> original) {
        synchronized (FastQuit.occupiedSessions) {
            if (!FastQuit.occupiedSessions.remove(session)) {
                original.call(session);
            }
        }
    }

    // While this should not be needed anymore, I'll leave it in just in case something goes wrong.
    @Inject(method = "edit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/toast/SystemToast;addWorldAccessFailureToast(Lnet/minecraft/client/MinecraftClient;Ljava/lang/String;)V"))
    private void fastQuit_openWorldListWhenFailed(CallbackInfo ci) {
        this.client.setScreen(this.screen);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/Text;FFI)I"))
    private void fastQuit_renderSavingTimeOnWorldList(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        if (FastQuit.showSavingTime >= 2) {
            FastQuit.getSavingWorld(this.client.getLevelStorage().getSavesDirectory().resolve(this.level.getName())).ifPresent(server -> {
                WorldInfo info = FastQuit.savingWorlds.get(server);
                if (info != null) {
                    String time = info.getTimeSaving() + " ⌛";
                    this.client.textRenderer.draw(matrices, time, x + entryWidth - this.client.textRenderer.getWidth(time) - 4, y + 1, -6939106);
                }
            });
        }
    }
}