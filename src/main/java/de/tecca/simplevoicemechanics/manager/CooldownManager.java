package de.tecca.simplevoicemechanics.manager;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Manages cooldowns for sculk sensors to prevent spam triggering.
 * Automatically cleans up old entries to prevent memory leaks.
 */
public class CooldownManager {

    private static final long DEFAULT_COOLDOWN = 1000L; // 1 second
    private static final long CLEANUP_THRESHOLD = 10000L; // 10 seconds

    private final Map<Location, Long> cooldowns = new HashMap<>();
    private long lastCleanup = System.currentTimeMillis();

    /**
     * Checks if a location is currently on cooldown.
     *
     * @param location Location to check
     * @return true if on cooldown
     */
    public boolean isOnCooldown(Location location) {
        Long lastTrigger = cooldowns.get(location);
        if (lastTrigger == null) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        return (currentTime - lastTrigger) < DEFAULT_COOLDOWN;
    }

    /**
     * Sets a cooldown for a location.
     *
     * @param location Location to set cooldown for
     */
    public void setCooldown(Location location) {
        cooldowns.put(location, System.currentTimeMillis());
        cleanupIfNeeded();
    }

    /**
     * Checks if cooldown has expired and can be triggered.
     *
     * @param location Location to check
     * @return true if can trigger
     */
    public boolean canTrigger(Location location) {
        return !isOnCooldown(location);
    }

    /**
     * Clears all cooldowns.
     */
    public void clearAll() {
        cooldowns.clear();
    }

    /**
     * Performs cleanup if enough time has passed.
     */
    private void cleanupIfNeeded() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastCleanup < CLEANUP_THRESHOLD) {
            return;
        }

        cleanup(currentTime);
        lastCleanup = currentTime;
    }

    /**
     * Removes expired cooldown entries.
     *
     * @param currentTime Current timestamp
     */
    private void cleanup(long currentTime) {
        Iterator<Map.Entry<Location, Long>> iterator = cooldowns.entrySet().iterator();
        int removed = 0;

        while (iterator.hasNext()) {
            Map.Entry<Location, Long> entry = iterator.next();
            long timeSinceLastTrigger = currentTime - entry.getValue();

            // Remove entries older than 10x the cooldown
            if (timeSinceLastTrigger > DEFAULT_COOLDOWN * 10) {
                iterator.remove();
                removed++;
            }
        }

        if (removed > 0) {
            // Logger can be added here if needed
        }
    }
}