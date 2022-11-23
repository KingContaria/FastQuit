package com.kingcontaria.fastquit;

import com.kingcontaria.fastquit.mixin.MinecraftServerAccessor;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.toast.Toast;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;
import net.minecraft.world.level.storage.LevelStorage;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FastQuit implements ClientModInitializer {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final File CONFIG = FabricLoader.getInstance().getConfigDir().resolve("fastquit-config.txt").toFile();
    private static final ModMetadata FASTQUIT = FabricLoader.getInstance().getModContainer("fastquit").orElseThrow().getMetadata();
    public static final Set<IntegratedServer> savingWorlds = Collections.synchronizedSet(new HashSet<>());
    public static final ConcurrentLinkedQueue<Toast> scheduledToasts = new ConcurrentLinkedQueue<>();
    public static final List<LevelStorage.Session> occupiedSessions = new ArrayList<>();
    public static boolean showToasts = true;
    public static boolean renderSavingScreen = false;
    public static int backgroundPriority = 2;

    public static void log(String msg) {
        LOGGER.info("[" + FASTQUIT.getName() + "] " + msg);
    }

    public static void error(String msg, Exception e) {
        LOGGER.error("[" + FASTQUIT.getName() + "] " + msg, e);
    }

    @Override
    public void onInitializeClient() {
        if (CONFIG.exists()) {
            try {
                boolean update = readConfig();

                if (update) {
                    try {
                        writeConfig();
                    } catch (IOException e) {
                        error("Failed to update config!", e);
                    }
                }
            } catch (IOException e) {
                error("Failed to read config!", e);
            }
        } else {
            try {
                writeConfig();
            } catch (IOException e) {
                error("Failed to write config!", e);
            }
        }
        log("Initialized");
    }

    public static void writeConfig() throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("# FastQuit Config");
        lines.add("version:" + FASTQUIT.getVersion().getFriendlyString());
        lines.add("");
        lines.add("## Determines whether a toast gets shown when a world finishes saving");
        lines.add("showToasts:" + showToasts);
        lines.add("");
        lines.add("## When playing on high render distance, quitting the world can still take a bit because the client-side chunk storage has to be cleared.");
        lines.add("## By enabling this setting the 'Saving world' screen will be rendered.");
        lines.add("renderSavingScreen:" + renderSavingScreen);
        lines.add("");
        lines.add("## Sets the thread priority of the server when saving worlds in the background");
        lines.add("## This is done to improve client performance while saving, but will make the saving take longer over all");
        lines.add("## Value has to be between 0 and 10, setting it to 0 will disable changing thread priority");
        lines.add("backgroundPriority:" + backgroundPriority);

        Files.writeString(CONFIG.toPath(), String.join(System.lineSeparator(), lines));
    }

    private static boolean readConfig() throws IOException {
        List<String> lines = Files.readAllLines(CONFIG.toPath());
        Version version = null;
        for (String line : lines) {
            try {
                if (!line.startsWith("#") && !lines.isEmpty()) {
                    String[] split = line.split(":", 2);
                    split[0] = split[0].trim();
                    split[1] = split[1].trim();

                    switch (split[0]) {
                        case "version" -> version = Version.parse(split[1]);
                        case "showToasts" -> showToasts = Boolean.parseBoolean(split[1]);
                        case "renderSavingScreen" -> renderSavingScreen = Boolean.parseBoolean(split[1]);
                        case "backgroundPriority" -> backgroundPriority = Math.max(0, Math.min(Thread.MAX_PRIORITY, Integer.parseInt(split[1])));
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return version == null || version.compareTo(FASTQUIT.getVersion()) < 0;
    }

    public static void wait(Set<IntegratedServer> servers) {
        Text stillSaving = TextHelper.translatable("screen.fastquit.waiting", String.join("\" & \"", servers.stream().map(server -> server.getSaveProperties().getLevelName()).toList()));
        Screen waitingScreen = new MessageScreen(stillSaving);
        log(stillSaving.getString());

        servers.forEach(server -> server.getThread().setPriority(Thread.NORM_PRIORITY));

        while (servers.stream().anyMatch(server -> !server.isStopping())) {
            MinecraftClient.getInstance().setScreenAndRender(waitingScreen);
        }
    }

    public static Optional<IntegratedServer> getSavingWorld(String name) {
        return savingWorlds.stream().filter(server -> ((MinecraftServerAccessor) server).getSession().getDirectoryName().equals(name)).findFirst();
    }

    public static boolean isSavingWorld(LevelStorage.Session session) {
        return savingWorlds.stream().anyMatch(server -> ((MinecraftServerAccessor) server).getSession() == session);
    }
}