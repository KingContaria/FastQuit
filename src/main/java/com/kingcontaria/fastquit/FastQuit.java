package com.kingcontaria.fastquit;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.server.integrated.IntegratedServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class FastQuit implements ClientModInitializer {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final File CONFIG = FabricLoader.getInstance().getConfigDir().resolve("fastquit-config.txt").toFile();
    private static final ModMetadata FASTQUIT = FabricLoader.getInstance().getModContainer("fastquit").orElseThrow().getMetadata();
    public static final Set<IntegratedServer> savingWorlds = Collections.synchronizedSet(new HashSet<>());
    public static boolean showToasts = true;
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
        lines.add("## Sets the thread priority of the server when saving worlds in the background");
        lines.add("## This is done to improve client performance while saving, but will make the saving take longer over all");
        lines.add("## Value has to be between 0 and 10, setting it to 0 will disable changing thread priority");
        lines.add("backgroundPriority:" + backgroundPriority);

        Files.write(CONFIG.toPath(), String.join(System.lineSeparator(), lines).getBytes());
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
                        case "backgroundPriority" -> backgroundPriority = Math.max(0, Math.min(Thread.MAX_PRIORITY, Integer.parseInt(split[1])));
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return version == null || version.compareTo(FASTQUIT.getVersion()) < 0;
    }
}