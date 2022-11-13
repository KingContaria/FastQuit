package com.kingcontaria.fastquit;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.server.integrated.IntegratedServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class FastQuit implements ClientModInitializer {

    private static final Logger LOGGER = LogManager.getLogger();
    public static final Set<IntegratedServer> savingWorlds = new HashSet<>();

    public static void log(String msg) {
        LOGGER.info("[FastQuit] " + msg);
    }

    @Override
    public void onInitializeClient() {
        log("Initializing");
    }
}