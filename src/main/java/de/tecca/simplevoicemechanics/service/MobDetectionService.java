package de.tecca.simplevoicemechanics.service;

import de.tecca.simplevoicemechanics.SimpleVoiceMechanics;
import de.tecca.simplevoicemechanics.cache.EntityCache;
import de.tecca.simplevoicemechanics.manager.DetectionManager;
import de.tecca.simplevoicemechanics.manager.FeatureManager;
import de.tecca.simplevoicemechanics.model.VoiceDetection;
import de.tecca.simplevoicemechanics.registry.MobTypeRegistry;
import org.bukkit.GameMode;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;

import java.util.Collection;

/**
 * Service handling mob detection logic.
 * Processes voice detections and alerts appropriate mobs.
 */
public class MobDetectionService {

    private static final int WARDEN_ANGER_MULTIPLIER = 20;

    private final SimpleVoiceMechanics plugin;
    private final EntityCache entityCache;
    private final MobTypeRegistry mobRegistry;
    private final DetectionManager detectionManager;
    private final FeatureManager featureManager;

    public MobDetectionService(
            SimpleVoiceMechanics plugin,
            EntityCache entityCache,
            MobTypeRegistry mobRegistry,
            DetectionManager detectionManager,
            FeatureManager featureManager
    ) {
        this.plugin = plugin;
        this.entityCache = entityCache;
        this.mobRegistry = mobRegistry;
        this.detectionManager = detectionManager;
        this.featureManager = featureManager;
    }

    /**
     * Processes a voice detection for mob reactions.
     *
     * @param detection Voice detection data
     */
    public void processDetection(VoiceDetection detection) {
        // Check if feature is enabled
        if (!featureManager.isMobHearingEnabled()) {
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

        // Get nearby mobs (cached)
        Collection<Mob> nearbyMobs = entityCache.getNearbyMobs(
                detection.getLocation(),
                detection.getRange()
        );

        // Process each mob
        int triggered = 0;
        for (Mob mob : nearbyMobs) {
            if (processMob(mob, detection)) {
                triggered++;
            }
        }

        // Log if any mobs were triggered
        if (triggered > 0) {
            plugin.getLogger().fine(
                    "Mob detection: " + triggered + " mobs alerted by " + detection.getPlayer().getName()
            );
        }
    }

    /**
     * Processes detection for a single mob.
     *
     * @param mob Mob to process
     * @param detection Voice detection data
     * @return true if mob was triggered
     */
    private boolean processMob(Mob mob, VoiceDetection detection) {
        // Check if this mob type should hear
        if (!shouldMobHear(mob)) {
            return false;
        }

        // Calculate distance and effective volume
        double distance = mob.getLocation().distance(detection.getLocation());

        if (!detectionManager.isWithinRange(distance)) {
            return false;
        }

        double effectiveVolume = detectionManager.calculateEffectiveVolume(
                detection.getVolume(),
                distance
        );

        // Check if effective volume is sufficient
        if (!detectionManager.canDetect(effectiveVolume)) {
            return false;
        }

        // Alert the mob
        alertMob(mob, detection.getPlayer(), effectiveVolume);

        return true;
    }

    /**
     * Alerts a mob to the player's presence.
     *
     * @param mob Mob to alert
     * @param player Player who made the sound
     * @param effectiveVolume Volume at mob's location
     */
    private void alertMob(Mob mob, Player player, double effectiveVolume) {
        // Set target if mob doesn't have one
        if (mob.getTarget() == null) {
            mob.setTarget(player);
        }

        // Special handling for Warden
        if (mob instanceof Warden warden) {
            int angerIncrease = (int) (effectiveVolume * WARDEN_ANGER_MULTIPLIER);
            warden.increaseAnger(player, angerIncrease);
        }
    }

    /**
     * Checks if a mob should hear based on type and config.
     *
     * @param mob Mob to check
     * @return true if should hear
     */
    private boolean shouldMobHear(Mob mob) {
        // Warden check
        if (mobRegistry.isSpecialMob(mob)) {
            return featureManager.isWardenHearingEnabled();
        }

        // Hostile mob check
        if (mobRegistry.isHostileMob(mob)) {
            return featureManager.isHostileMobHearingEnabled();
        }

        return false;
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