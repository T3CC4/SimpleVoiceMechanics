package de.tecca.simplevoicemechanics.listener;

import de.tecca.simplevoicemechanics.SimpleVoiceMechanics;
import de.tecca.simplevoicemechanics.event.VoiceDetectedEvent;
import de.tecca.simplevoicemechanics.util.VolumeCalculator;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.level.gameevent.GameEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_21_R6.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.craftbukkit.v1_21_R6.CraftWorld;
import net.minecraft.core.BlockPosition;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles Sculk Sensor activation from voice detection.
 *
 * <p>This listener triggers Sculk Sensors (regular and calibrated) when
 * players speak nearby, using Minecraft's native game event system.
 *
 * <p>Features:
 * <ul>
 *   <li>Activates both regular and calibrated Sculk Sensors</li>
 *   <li>Realistic distance and volume-based activation</li>
 *   <li>1-second cooldown per sensor (matches vanilla behavior)</li>
 *   <li>Uses NMS for authentic game event triggering</li>
 * </ul>
 *
 * @author Tecca
 * @version 1.0.0
 * @since 1.0.0
 */
public class SculkListener implements Listener {

    /** Cooldown duration in milliseconds (matches vanilla Sculk Sensor) */
    private static final long COOLDOWN_MILLIS = 1000;

    /** Multiplier for automatic cooldown cleanup (10x cooldown) */
    private static final long CLEANUP_MULTIPLIER = 10;

    /** Minimum effective volume for sensor activation */
    private static final double MIN_EFFECTIVE_VOLUME = 0.08;

    private final SimpleVoiceMechanics plugin;

    /** Tracks last trigger time for each Sculk Sensor location */
    private final Map<Location, Long> lastTriggerTime = new HashMap<>();

    /**
     * Constructs a new SculkListener.
     *
     * @param plugin the plugin instance
     */
    public SculkListener(SimpleVoiceMechanics plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles voice detection events for Sculk Sensor activation.
     *
     * @param event the voice detected event
     */
    @EventHandler
    public void onVoiceDetected(VoiceDetectedEvent event) {
        if (!plugin.getConfigManager().isSculkHearingEnabled()) {
            return;
        }

        Player player = event.getPlayer();

        // Only detect Survival/Adventure players
        if (!isValidGameMode(player)) {
            return;
        }

        Location loc = event.getLocation();
        double range = event.getRange();
        float volume = event.getVolume();

        // Check minimum volume threshold
        double minVolume = plugin.getConfigManager().getMinVolumeForDetection();
        if (volume < minVolume) {
            return;
        }

        // Search for and activate nearby Sculk Sensors
        processSculkSensors(player, loc, range, volume);

        // Clean up old cooldown entries
        cleanupOldTriggers();
    }

    /**
     * Checks if the player is in a valid gamemode for detection.
     *
     * @param player the player to check
     * @return true if player is in Survival or Adventure mode
     */
    private boolean isValidGameMode(Player player) {
        GameMode mode = player.getGameMode();
        return mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE;
    }

    /**
     * Processes all Sculk Sensors within range.
     *
     * @param player the speaking player
     * @param loc the voice location
     * @param range the detection range
     * @param volume the voice volume
     */
    private void processSculkSensors(Player player, Location loc, double range, float volume) {
        int searchRadius = (int) Math.ceil(range);
        Location playerLoc = player.getLocation();

        // Iterate through all blocks in range
        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius; y <= searchRadius; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    Block block = playerLoc.clone().add(x, y, z).getBlock();

                    if (isSculkSensor(block)) {
                        processSculkSensor(block, player, loc, range, volume);
                    }
                }
            }
        }
    }

    /**
     * Checks if a block is a Sculk Sensor (regular or calibrated).
     *
     * @param block the block to check
     * @return true if the block is a Sculk Sensor
     */
    private boolean isSculkSensor(Block block) {
        Material type = block.getType();
        return type == Material.SCULK_SENSOR || type == Material.CALIBRATED_SCULK_SENSOR;
    }

    /**
     * Processes a single Sculk Sensor for potential activation.
     *
     * @param block the Sculk Sensor block
     * @param player the speaking player
     * @param voiceLoc the voice location
     * @param range the detection range
     * @param volume the voice volume
     */
    private void processSculkSensor(Block block, Player player, Location voiceLoc,
                                    double range, float volume) {
        Location sensorLoc = block.getLocation();
        double distance = sensorLoc.distance(voiceLoc);

        // Check if sensor is within range
        if (distance > range) {
            return;
        }

        // Check cooldown
        if (isOnCooldown(sensorLoc)) {
            return;
        }

        // Calculate effective volume using VolumeCalculator
        double effectiveVolume = VolumeCalculator.calculateSculkEffectiveVolume(volume, distance);

        // Debug logging
        if (plugin.getConfig().getBoolean("debug.sculk-logging", false)) {
            plugin.getLogger().info(String.format(
                    "Sculk: %s | %s",
                    block.getType(),
                    VolumeCalculator.getAttenuationDebugInfo(volume, distance)
            ));
        }

        if (effectiveVolume < MIN_EFFECTIVE_VOLUME) {
            return;
        }

        // Calculate activation probability
        double activationChance = VolumeCalculator.calculateSculkDetectionChance(
                effectiveVolume,
                distance,
                range
        );

        // Probabilistic activation
        if (Math.random() > activationChance) {
            return;
        }

        // Trigger the Sculk Sensor using NMS
        triggerSculkSensor(block, player, volume);

        // Record trigger time
        lastTriggerTime.put(sensorLoc, System.currentTimeMillis());
    }

    /**
     * Checks if a sensor is currently on cooldown.
     *
     * @param sensorLoc the sensor location
     * @return true if the sensor is on cooldown
     */
    private boolean isOnCooldown(Location sensorLoc) {
        Long lastTrigger = lastTriggerTime.get(sensorLoc);
        if (lastTrigger == null) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        return (currentTime - lastTrigger) < COOLDOWN_MILLIS;
    }

    /**
     * Triggers a Sculk Sensor using Minecraft's native game event system.
     *
     * @param block the Sculk Sensor block
     * @param player the player causing the event
     * @param volume the voice volume
     */
    private void triggerSculkSensor(Block block, Player player, double volume) {
        // Get NMS level (world)
        net.minecraft.server.level.WorldServer level =
                ((CraftWorld) block.getWorld()).getHandle();

        // Get block position
        BlockPosition pos = new BlockPosition(
                block.getX(),
                block.getY(),
                block.getZ()
        );

        // Get NMS player
        EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();

        // Trigger game event (STEP event is detected by all Sculk Sensors)
        net.minecraft.core.Holder<GameEvent> gameEventHolder = GameEvent.P; // STEP event
        level.a(nmsPlayer, gameEventHolder, pos);

        // Debug logging
        if (plugin.getConfig().getBoolean("debug.sculk-logging", false) && volume > 0.5) {
            plugin.getLogger().fine(String.format(
                    "Sculk Sensor activated at %s by loud voice (%.2f)",
                    block.getLocation(), volume
            ));
        }
    }

    /**
     * Cleans up old cooldown entries to prevent memory leaks.
     *
     * <p>Removes entries older than 10x the cooldown duration.
     */
    private void cleanupOldTriggers() {
        long currentTime = System.currentTimeMillis();
        long maxAge = COOLDOWN_MILLIS * CLEANUP_MULTIPLIER;

        lastTriggerTime.entrySet().removeIf(entry ->
                (currentTime - entry.getValue()) > maxAge
        );
    }
}