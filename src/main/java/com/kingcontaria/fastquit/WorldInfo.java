package com.kingcontaria.fastquit;

/**
 * Saves additional information about a world.
 */
public class WorldInfo {

    /**
     * Whether the corresponding world has been deleted.
     */
    public boolean deleted = false;

    /**
     * Saves the time of instantiation which is the same as the start of saving of the corresponding world.
     */
    private final long startedSaving = System.currentTimeMillis();

    public WorldInfo() {
    }

    /**
     * @return a String representing the time passed since the start of saving the corresponding world
     */
    public String endSaving() {
        long sec = Math.round((System.currentTimeMillis() - startedSaving) / 1000.0);
        return sec < 60 ? (sec + "s") : (sec / 60 + "min " + sec % 60 + "s");
    }
}