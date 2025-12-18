package de.tecca.simplevoicemechanics.listener;

import de.tecca.simplevoicemechanics.SimpleVoiceMechanics;
import de.tecca.simplevoicemechanics.event.VoiceDetectedEvent;
import de.tecca.simplevoicemechanics.manager.ConfigManager;
import de.tecca.simplevoicemechanics.util.RangeCalculator;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles Sculk Sensor activation from voice detection.
 *
 * <p>Triggers Sculk Sensors (regular and calibrated) when players speak nearby.
 * Uses range-based detection with configurable min/max range and falloff curve.
 *
 * <p>Requires Minecraft 1.19+ for GameEvent API support.
 * If GameEvent API is not available, this listener will be disabled.
 *
 * @author Tecca
 * @version 1.0.0
 */
public class SculkListener implements Listener {

    private final SimpleVoiceMechanics plugin;
    private final boolean gameEventSupported;

    /** Tracks last trigger time for each Sculk Sensor location */
    private final Map<Location, Long> lastTriggerTime = new HashMap<>();

    public SculkListener(SimpleVoiceMechanics plugin) {
        this.plugin = plugin;
        this.gameEventSupported = checkGameEventSupport();

        if (!gameEventSupported) {
            plugin.getLogger().warning("GameEvent API not available - Sculk Sensor detection disabled");
            plugin.getLogger().warning("Sculk Sensors require Minecraft 1.19+");
        }
    }

    /**
     * Checks if GameEvent API is available in this version.
     */
    private boolean checkGameEventSupport() {
        try {
            Class.forName("org.bukkit.GameEvent");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Handles voice detection events for Sculk Sensor activation.
     */
    @EventHandler
    public void onVoiceDetected(VoiceDetectedEvent event) {
        if (!gameEventSupported) {
            return;
        }

        if (!plugin.getConfigManager().isSculkEnabled()) {
            return;
        }

        Player player = event.getPlayer();

        // Only detect Survival/Adventure players
        if (!isValidGameMode(player)) {
            return;
        }

        Location loc = event.getLocation();

        // Search for and activate nearby Sculk Sensors
        processSculkSensors(player, loc);

        // Clean up old cooldown entries
        cleanupOldTriggers();
    }

    /**
     * Checks if the player is in a valid gamemode for detection.
     */
    private boolean isValidGameMode(Player player) {
        GameMode mode = player.getGameMode();
        return mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE;
    }

    /**
     * Processes all Sculk Sensors within range.
     */
    private void processSculkSensors(Player player, Location loc) {
        ConfigManager config = plugin.getConfigManager();
        double maxRange = config.getSculkMaxRange();

        int searchRadius = (int) Math.ceil(maxRange);
        Location playerLoc = player.getLocation();

        // Iterate through all blocks in range
        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius; y <= searchRadius; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    Block block = playerLoc.clone().add(x, y, z).getBlock();

                    if (isSculkSensor(block)) {
                        processSculkSensor(block, player, loc);
                    }
                }
            }
        }
    }

    /**
     * Checks if a block is a Sculk Sensor (regular or calibrated).
     * Version-safe check for blocks that may not exist in older versions.
     */
    private boolean isSculkSensor(Block block) {
        Material type = block.getType();
        String typeName = type.name();
        return typeName.equals("SCULK_SENSOR") || typeName.equals("CALIBRATED_SCULK_SENSOR");
    }

    /**
     * Processes a single Sculk Sensor for potential activation.
     */
    private void processSculkSensor(Block block, Player player, Location voiceLoc) {
        ConfigManager config = plugin.getConfigManager();
        Location sensorLoc = block.getLocation();

        double distance = sensorLoc.distance(voiceLoc);
        double maxRange = config.getSculkMaxRange();
        double minRange = config.getSculkMinRange();
        double falloffCurve = config.getSculkFalloffCurve();

        // Check if sensor is within range
        if (distance > maxRange) {
            return;
        }

        // Check cooldown
        if (isOnCooldown(sensorLoc)) {
            return;
        }

        // Calculate activation probability
        double activationChance = RangeCalculator.calculateDetectionChance(
                distance, minRange, maxRange, falloffCurve
        );

        // Probabilistic activation
        if (Math.random() > activationChance) {
            return;
        }

        // Trigger the Sculk Sensor
        triggerSculkSensor(block, player);

        // Record trigger time
        lastTriggerTime.put(sensorLoc, System.currentTimeMillis());

        // Debug logging
        if (plugin.getConfig().getBoolean("debug.sculk-logging", false)) {
            plugin.getLogger().info(String.format(
                    "Sculk %s activated | %s",
                    block.getType(),
                    RangeCalculator.getDebugInfo(distance, minRange, maxRange, falloffCurve)
            ));
        }
    }

    /**
     * Checks if a sensor is currently on cooldown.
     */
    private boolean isOnCooldown(Location sensorLoc) {
        Long lastTrigger = lastTriggerTime.get(sensorLoc);
        if (lastTrigger == null) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        long cooldown = plugin.getConfigManager().getSculkCooldown();
        return (currentTime - lastTrigger) < cooldown;
    }

    /**
     * Triggers a Sculk Sensor using Paper's GameEvent API.
     * Only called when gameEventSupported is true.
     */
    private void triggerSculkSensor(Block block, Player player) {
        // Send STEP game event at block center to trigger Sculk Sensor
        block.getWorld().sendGameEvent(
                player,
                org.bukkit.GameEvent.STEP,
                block.getLocation().add(0.5, 0.5, 0.5).toVector()
        );
    }

    /**
     * Cleans up old cooldown entries to prevent memory leaks.
     */
    private void cleanupOldTriggers() {
        long currentTime = System.currentTimeMillis();
        long maxAge = plugin.getConfigManager().getSculkCooldown() * 10;

        lastTriggerTime.entrySet().removeIf(entry ->
                (currentTime - entry.getValue()) > maxAge
        );
    }
}