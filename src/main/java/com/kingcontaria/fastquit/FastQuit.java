package com.kingcontaria.fastquit;

import com.kingcontaria.fastquit.mixin.MinecraftClientAccessor;
import com.kingcontaria.fastquit.mixin.MinecraftServerAccessor;
import com.kingcontaria.fastquit.mixin.SessionAccessor;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class FastQuit implements ClientModInitializer {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final File CONFIG = FabricLoader.getInstance().getConfigDir().resolve("fastquit-config.txt").toFile();
    private static final ModMetadata FASTQUIT = FabricLoader.getInstance().getModContainer("fastquit").orElseThrow().getMetadata();
    private static final String LOG_PREFIX = "[" + FASTQUIT.getName() + "] ";

    /**
     * Synchronized {@link Map} containing all currently saving {@link IntegratedServer}'s, with a {@link WorldInfo} with more information about the world.
     */
    public static final Map<IntegratedServer, WorldInfo> savingWorlds = Collections.synchronizedMap(new HashMap<>());
    /**
     * Stores {@link LevelStorage.Session}'s used by FastQuit as to only close them if no other process is currently using them.
     */
    public static final List<LevelStorage.Session> occupiedSessions = Collections.synchronizedList(new ArrayList<>());

    /**
     * Determines whether a toast gets shown when a world finishes saving.
     */
    public static boolean showToasts = true;
    /**
     * Determines whether the "Saving world" screen gets rendered.
     */
    public static boolean renderSavingScreen = false;
    /**
     * Determines the Thread priority used for {@link IntegratedServer}'s saving in the background.
     * Value needs to be between 0 and 10, with Thread priority staying unchanged if the value is 0.
     */
    public static int backgroundPriority = 2;
    /**
     * Determines whether the time it took to save the world gets displayed on toasts and the world list.
     * Value needs to be between 0 and 2, with 0 never showing the time, 1 only on the toast and 2 also on the world list.
     */
    public static int showSavingTime = 2;

    /**
     * Logs the given message.
     */
    public static void log(String msg) {
        LOGGER.info(LOG_PREFIX + msg);
    }

    /**
     * Logs the given warning.
     */
    public static void warn(String msg) {
        LOGGER.warn(LOG_PREFIX + msg);
    }

    /**
     * Logs the given message and error.
     */
    public static void error(String msg, Throwable throwable) {
        LOGGER.error(LOG_PREFIX + msg, throwable);
    }

    @Override
    public void onInitializeClient() {
        if (CONFIG.isFile()) {
            boolean update = readConfig("read");

            if (update) {
                writeConfig("update");
            }
        } else {
            writeConfig("create");
        }

        log("Initialized");
    }

    /**
     * Writes the options to the config file.
     * 
     * @param action - used for logging possible errors
     */
    public static void writeConfig(String action) {
        String[] lines = new String[] {
                "# FastQuit Config",
                "version:" + FASTQUIT.getVersion().getFriendlyString(),
                "",
                "## Determines whether a toast gets shown when a world finishes saving.",
                "showToasts:" + showToasts,
                "",
                "## When playing on high render distance, quitting the world can still take a bit because the client-side chunk storage has to be cleared.",
                "## By enabling this setting the 'Saving world' screen will be rendered.",
                "renderSavingScreen:" + renderSavingScreen,
                "",
                "## Sets the thread priority of the server when saving worlds in the background.",
                "## This is done to improve client performance while saving, but will make the saving take longer over all.",
                "## Value has to be between 0 and 10, setting it to 0 will disable changing thread priority.",
                "backgroundPriority:" + backgroundPriority,
                "",
                "## Determines whether the time it took to save the world gets displayed on toasts and the world list.",
                "## Value has to be between 0 and 2, with 0 never showing the time, 1 only on the toast and 2 also on the world list.",
                "showSavingTime:" + showSavingTime
        };

        try {
            Files.writeString(CONFIG.toPath(), String.join(System.lineSeparator(), lines));
        } catch (IOException e) {
            error("Failed to " + action + " config!", e);
        }
    }

    /**
     * Restores the options from the config file.
     *
     * @param action - used for logging possible errors
     * @return if the version specified in the config is outdated and the config should be updated
     */
    public static boolean readConfig(String action) {
        Version version = null;

        try {
            List<String> lines = Files.readAllLines(CONFIG.toPath());

            for (String line : lines) {
                try {
                    if (!line.startsWith("#") && !line.trim().isEmpty()) {
                        String[] split = line.split(":", 2);
                        split[1] = split[1].trim();

                        switch (split[0].trim()) {
                            case "version" -> version = Version.parse(split[1]);
                            case "showToasts" -> showToasts = Boolean.parseBoolean(split[1]);
                            case "renderSavingScreen" -> renderSavingScreen = Boolean.parseBoolean(split[1]);
                            case "backgroundPriority" -> backgroundPriority = Math.max(0, Math.min(Thread.MAX_PRIORITY, Integer.parseInt(split[1])));
                            case "showSavingTime" -> showSavingTime = Math.max(0, Math.min(2, Integer.parseInt(split[1])));
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (IOException e) {
            error("Failed to " + action + " config!", e);
        }
        return version == null || version.compareTo(FASTQUIT.getVersion()) < 0;
    }

    /**
     * Waits for all {@link IntegratedServer}'s to finish saving, gets called when Minecraft is closed.
     *
     * @implNote Catches everything to avoid any issues in the areas where it's called.
     */
    public static void exit() {
        try {
            log("Exiting FastQuit.");
            wait(savingWorlds.keySet());
        } catch (Throwable throwable) {
            error("Something went horribly wrong when exiting FastQuit!", throwable);
            savingWorlds.forEach((server, info) -> {
                try {
                    server.getThread().join();
                } catch (Throwable throwable2) {
                    error("Failed to wait for \"" + server.getSaveProperties().getLevelName() + "\"", throwable2);
                }
            });
        }
    }

    /**
     * @see #wait(Collection, CallbackInfo)
     */
    public static void wait(Collection<IntegratedServer> servers) {
        wait(servers, null);
    }

    /**
     * Waits for all the {@link IntegratedServer}'s in the given {@link Collection} to finish saving and in the meantime renders a {@link WaitingScreen}.
     * If a {@link CallbackInfo} is given, the waiting can be cancelled by the user.
     *
     * @throws IllegalStateException if called on one of the given {@link IntegratedServer}'s threads, would cause a deadlock otherwise
     */
    public static void wait(Collection<IntegratedServer> servers, @Nullable CallbackInfo cancellable) {
        if (servers == null || servers.isEmpty()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        if (!client.isOnThread()) {
            if (servers.stream().anyMatch(server -> Thread.currentThread() == server.getThread())) {
                throw new IllegalStateException("Tried to call FastQuit.wait(...) from one of the servers it's supposed to wait for.");
            }

            client.submit(() -> wait(servers)).join();
            return;
        }

        Screen oldScreen = client.currentScreen;

        Text stillSaving = TextHelper.translatable("screen.fastquit.waiting", String.join("\" & \"", servers.stream().map(server -> server.getSaveProperties().getLevelName()).toList()));
        log(stillSaving.getString());

        servers.forEach(server -> server.getThread().setPriority(Thread.NORM_PRIORITY));

        try {
            client.setScreen(new WaitingScreen(stillSaving, cancellable));

            while (servers.stream().anyMatch(server -> !server.isStopping())) {
                if (cancellable != null && cancellable.isCancelled()) {
                    if (backgroundPriority != 0) {
                        servers.forEach(server -> server.getThread().setPriority(backgroundPriority));
                    }
                    log("Stopped waiting.");
                    break;
                }
                ((MinecraftClientAccessor) client).callRender(false);
            }
        } finally {
            // compatibility with "WorldGen" mod
            if (oldScreen != null && oldScreen.getClass().getName().equals("caeruleusTait.WorldGen.gui.screens.WGConfigScreen")) {
                client.currentScreen = oldScreen;
            } else {
                client.setScreenAndRender(oldScreen);
            }
        }
    }

    /**
     * @return optionally returns the currently {@link IntegratedServer} matching the given {@link Path}
     */
    public static Optional<IntegratedServer> getSavingWorld(Path path) {
        return savingWorlds.keySet().stream().filter(server -> ((SessionAccessor) ((MinecraftServerAccessor) server).getSession()).getDirectory().path().equals(path)).findFirst();
    }

    /**
     * @return optionally returns the currently saving {@link IntegratedServer} matching the given {@link LevelStorage.Session}
     */
    public static Optional<IntegratedServer> getSavingWorld(LevelStorage.Session session) {
        return savingWorlds.keySet().stream().filter(server -> ((MinecraftServerAccessor) server).getSession() == session).findFirst();
    }

    /**
     * @apiNote Remember to {@link LevelStorage.Session#close() close} the session after using it!
     * @return optionally returns the {@link LevelStorage.Session} of the currently saving {@link IntegratedServer} matching the given {@link Path}
     */
    public static Optional<LevelStorage.Session> getSession(Path path) {
        return getSavingWorld(path).flatMap(server -> {
            LevelStorage.Session session;
            synchronized (session  = ((MinecraftServerAccessor) server).getSession()) {
                if (((SessionAccessor) session).getLock().isValid()) {
                    occupiedSessions.add(session);
                    return Optional.of(session);
                }
            }
            return Optional.empty();
        });
    }
}