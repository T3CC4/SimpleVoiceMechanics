package de.tecca.simplevoicemechanics.listener;

import de.tecca.simplevoicemechanics.SimpleVoiceMechanics;
import de.tecca.simplevoicemechanics.event.VoiceDetectedEvent;
import de.tecca.simplevoicemechanics.manager.ConfigManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Collection;

/**
 * Handles mob reactions to voice detection.
 *
 * <p>This listener makes mobs aware of player voices:
 * <ul>
 *   <li>Hostile mobs target speaking players</li>
 *   <li>Warden anger increases based on volume</li>
 *   <li>Distance affects detection intensity</li>
 *   <li>Only affects Survival/Adventure mode players</li>
 * </ul>
 *
 * @author Tecca
 * @version 1.0.0
 * @since 1.0.0
 */
public class MobListener implements Listener {

    /** Minimum effective volume required for mob detection */
    private static final double MIN_EFFECTIVE_VOLUME = 0.1;

    /** Multiplier for Warden anger increase (per volume unit) */
    private static final int WARDEN_ANGER_MULTIPLIER = 20;

    private final SimpleVoiceMechanics plugin;

    /**
     * Constructs a new MobListener.
     *
     * @param plugin the plugin instance
     */
    public MobListener(SimpleVoiceMechanics plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles voice detection events for mob mechanics.
     *
     * <p>When a player speaks, nearby mobs may react based on:
     * <ul>
     *   <li>Mob type (hostile, warden)</li>
     *   <li>Distance to player</li>
     *   <li>Voice volume</li>
     *   <li>Player gamemode</li>
     * </ul>
     *
     * @param event the voice detected event
     */
    @EventHandler
    public void onVoiceDetected(VoiceDetectedEvent event) {
        if (!plugin.getConfigManager().isMobHearingEnabled()) {
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

        // Process all nearby mobs
        processNearbyMobs(player, loc, range, volume);
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
     * Processes all mobs near the voice location.
     *
     * @param player the speaking player
     * @param loc the voice location
     * @param range the detection range
     * @param volume the voice volume
     */
    private void processNearbyMobs(Player player, Location loc, double range, float volume) {
        Collection<Entity> nearbyEntities = loc.getWorld()
                .getNearbyEntities(loc, range, range, range);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof Mob) {
                processMob((Mob) entity, player, loc, range, volume);
            }
        }
    }

    /**
     * Processes a single mob's reaction to voice.
     *
     * @param mob the mob to process
     * @param player the speaking player
     * @param loc the voice location
     * @param range the detection range
     * @param volume the voice volume
     */
    private void processMob(Mob mob, Player player, Location loc, double range, float volume) {
        // Check if this mob type should detect voice
        if (!shouldMobHear(mob)) {
            return;
        }

        // Calculate distance-based volume
        double distance = mob.getLocation().distance(loc);
        if (distance > range) {
            return;
        }

        double effectiveVolume = calculateEffectiveVolume(volume, distance, range);

        if (effectiveVolume < MIN_EFFECTIVE_VOLUME) {
            return;
        }

        // Make mob aware of player
        makeMobAware(mob, player, effectiveVolume);
    }

    /**
     * Calculates effective volume based on distance.
     *
     * <p>Volume decreases linearly with distance using the formula:
     * effectiveVolume = volume × (1 - distance/range)
     *
     * @param volume the original volume
     * @param distance the distance from source
     * @param range the maximum range
     * @return the effective volume at the given distance
     */
    private double calculateEffectiveVolume(float volume, double distance, double range) {
        return volume * (1.0 - (distance / range));
    }

    /**
     * Makes a mob aware of and potentially aggressive toward the player.
     *
     * @param mob the mob to affect
     * @param player the target player
     * @param effectiveVolume the effective volume at mob's location
     */
    private void makeMobAware(Mob mob, Player player, double effectiveVolume) {
        // Target player if mob has no current target
        if (mob.getTarget() == null) {
            mob.setTarget(player);
        }

        // Special handling for Warden
        if (mob instanceof Warden) {
            increaseWardenAnger((Warden) mob, player, effectiveVolume);
        }
    }

    /**
     * Increases Warden anger based on voice volume.
     *
     * @param warden the warden to affect
     * @param player the target player
     * @param effectiveVolume the effective volume
     */
    private void increaseWardenAnger(Warden warden, Player player, double effectiveVolume) {
        int angerIncrease = (int) (effectiveVolume * WARDEN_ANGER_MULTIPLIER);
        warden.increaseAnger(player, angerIncrease);
    }

    /**
     * Determines if a mob should be able to hear voices.
     *
     * @param mob the mob to check
     * @return true if the mob should hear voices
     */
    private boolean shouldMobHear(Mob mob) {
        ConfigManager config = plugin.getConfigManager();

        // Warden always hears (if enabled)
        if (mob instanceof Warden) {
            return config.isWardenHearingEnabled();
        }

        // Hostile mobs (Monster interface)
        if (mob instanceof Monster) {
            return config.isHostileMobHearingEnabled();
        }

        // Other mobs don't hear by default
        return false;
    }
}