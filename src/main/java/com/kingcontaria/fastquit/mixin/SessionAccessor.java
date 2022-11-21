package com.kingcontaria.fastquit.mixin;

import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.SessionLock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelStorage.Session.class)
public interface SessionAccessor {
    @Accessor
    SessionLock getLock();
}