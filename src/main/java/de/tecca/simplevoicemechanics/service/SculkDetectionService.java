package de.tecca.simplevoicemechanics.service;

import de.tecca.simplevoicemechanics.SimpleVoiceMechanics;
import de.tecca.simplevoicemechanics.manager.CooldownManager;
import de.tecca.simplevoicemechanics.manager.DetectionManager;
import de.tecca.simplevoicemechanics.manager.FeatureManager;
import de.tecca.simplevoicemechanics.model.VoiceDetection;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.gameevent.GameEvent;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_21_R6.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R6.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * Service handling sculk sensor detection logic.
 * Processes voice detections and triggers appropriate sculk sensors.
 */
public class SculkDetectionService {

    private final SimpleVoiceMechanics plugin;
    private final CooldownManager cooldownManager;
    private final DetectionManager detectionManager;
    private final FeatureManager featureManager;

    public SculkDetectionService(
            SimpleVoiceMechanics plugin,
            CooldownManager cooldownManager,
            DetectionManager detectionManager,
            FeatureManager featureManager
    ) {
        this.plugin = plugin;
        this.cooldownManager = cooldownManager;
        this.detectionManager = detectionManager;
        this.featureManager = featureManager;
    }

    /**
     * Processes a voice detection for sculk sensor reactions.
     *
     * @param detection Voice detection data
     */
    public void processDetection(VoiceDetection detection) {
        // Check if feature is enabled
        if (!featureManager.isSculkHearingEnabled()) {
            return;
        }

        // Validate player can trigger detection
        if (!canTriggerDetection(detection.getPlayer())) {
            return;
        }

        // Check minimum volume
        if (!detectionManager.canDetect(detection.getVolume())) {
            return;
        }

        // Search for and trigger nearby sculk sensors
        int triggered = triggerNearbySensors(detection);

        if (triggered > 0) {
            plugin.getLogger().fine(
                    "Sculk detection: " + triggered + " sensors triggered by " + detection.getPlayer().getName()
            );
        }
    }

    /**
     * Finds and triggers sculk sensors near the detection location.
     *
     * @param detection Voice detection data
     * @return Number of sensors triggered
     */
    private int triggerNearbySensors(VoiceDetection detection) {
        Location location = detection.getLocation();
        int searchRadius = (int) Math.ceil(detection.getRange());
        int triggered = 0;

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius; y <= searchRadius; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    Block block = location.clone().add(x, y, z).getBlock();

                    if (!isSculkSensor(block.getType())) {
                        continue;
                    }

                    Location sensorLocation = block.getLocation();
                    double distance = sensorLocation.distance(location);

                    // Check range
                    if (!detectionManager.isWithinRange(distance)) {
                        continue;
                    }

                    // Check cooldown
                    if (!cooldownManager.canTrigger(sensorLocation)) {
                        continue;
                    }

                    // Calculate effective volume
                    double effectiveVolume = detectionManager.calculateEffectiveVolume(
                            detection.getVolume(),
                            distance
                    );

                    if (!detectionManager.canDetect(effectiveVolume)) {
                        continue;
                    }

                    // Trigger sensor
                    if (triggerSensor(block, detection.getPlayer())) {
                        cooldownManager.setCooldown(sensorLocation);
                        triggered++;
                    }
                }
            }
        }

        return triggered;
    }

    /**
     * Triggers a sculk sensor using NMS.
     *
     * @param block Sculk sensor block
     * @param player Player who triggered it
     * @return true if successful
     */
    private boolean triggerSensor(Block block, Player player) {
        try {
            WorldServer level = ((CraftWorld) block.getWorld()).getHandle();
            BlockPosition pos = new BlockPosition(block.getX(), block.getY(), block.getZ());
            EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();

            // Use STEP game event
            net.minecraft.core.Holder<GameEvent> gameEventHolder = GameEvent.P;
            level.a(nmsPlayer, gameEventHolder, pos);

            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to trigger sculk sensor: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a block is a sculk sensor.
     *
     * @param material Block material
     * @return true if sculk sensor
     */
    private boolean isSculkSensor(Material material) {
        return material == Material.SCULK_SENSOR || material == Material.CALIBRATED_SCULK_SENSOR;
    }

    /**
     * Checks if a player can trigger detection.
     *
     * @param player Player to check
     * @return true if can trigger
     */
    private boolean canTriggerDetection(Player player) {
        if (player == null || !player.isOnline()) {
            return false;
        }

        GameMode mode = player.getGameMode();
        return mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE;
    }
}