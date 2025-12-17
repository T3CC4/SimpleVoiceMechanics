package de.tecca.simplevoicemechanics.listener;

import de.tecca.simplevoicemechanics.SimpleVoiceMechanics;
import de.tecca.simplevoicemechanics.event.VoiceDetectedEvent;
import de.tecca.simplevoicemechanics.manager.ConfigManager;
import de.tecca.simplevoicemechanics.util.MobCategory;
import de.tecca.simplevoicemechanics.util.RangeCalculator;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles mob reactions to voice detection with category-based behavior.
 *
 * <p>Mob categories:
 * <ul>
 *   <li>Hostile: Attack player (Zombies, Skeletons, Creepers, etc.)</li>
 *   <li>Neutral: Look at player (Piglins, Zombified Piglins, etc.)</li>
 *   <li>Peaceful: Look at player, follow when sneaking (Cows, Sheep, etc.)</li>
 *   <li>Warden: Special anger-based behavior</li>
 * </ul>
 *
 * @author Tecca
 * @version 1.0.0
 */
public class MobListener implements Listener {

    private static final String FOLLOW_META_KEY = "svm_following";
    private static final String FOLLOW_PLAYER_KEY = "svm_follow_player";

    private final SimpleVoiceMechanics plugin;

    // Tracks which mobs are currently following which players
    private final Map<UUID, UUID> followingMobs = new HashMap<>();  // MobUUID -> PlayerUUID
    private final Map<UUID, Long> followStartTime = new HashMap<>();  // MobUUID -> StartTime

    public MobListener(SimpleVoiceMechanics plugin) {
        this.plugin = plugin;
        startFollowCleanupTask();
    }

    /**
     * Handles voice detection events for mob mechanics.
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
        boolean isSneaking = player.isSneaking();

        // Process all nearby mobs
        processNearbyMobs(player, loc, isSneaking);
    }

    /**
     * Checks if the player is in a valid gamemode for detection.
     */
    private boolean isValidGameMode(Player player) {
        GameMode mode = player.getGameMode();
        return mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE;
    }

    /**
     * Processes all mobs near the voice location.
     */
    private void processNearbyMobs(Player player, Location loc, boolean isSneaking) {
        ConfigManager config = plugin.getConfigManager();

        // Use max possible range to check all entities
        double maxCheckRange = Math.max(
                Math.max(config.getHostileMaxRange(), config.getNeutralMaxRange()),
                Math.max(config.getPeacefulMaxRange(), config.getWardenMaxRange())
        );

        Collection<Entity> nearbyEntities = loc.getWorld()
                .getNearbyEntities(loc, maxCheckRange, maxCheckRange, maxCheckRange);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof Mob) {
                processMob((Mob) entity, player, loc, isSneaking);
            }
        }
    }

    /**
     * Processes a single mob's reaction to voice.
     */
    private void processMob(Mob mob, Player player, Location playerLoc, boolean isSneaking) {
        EntityType type = mob.getType();
        ConfigManager config = plugin.getConfigManager();

        // Determine mob category and process accordingly
        if (type == EntityType.WARDEN && config.isWardenEnabled()) {
            processWarden((Warden) mob, player, playerLoc);
        } else if (MobCategory.isHostile(type)) {
            processHostileMob(mob, player, playerLoc);
        } else if (MobCategory.isNeutral(type)) {
            processNeutralMob(mob, player, playerLoc);
        } else if (MobCategory.isPeaceful(type)) {
            processPeacefulMob(mob, player, playerLoc, isSneaking);
        }
    }

    /**
     * Processes Warden special behavior.
     */
    private void processWarden(Warden warden, Player player, Location playerLoc) {
        ConfigManager config = plugin.getConfigManager();

        double distance = warden.getLocation().distance(playerLoc);
        double maxRange = config.getWardenMaxRange();
        double minRange = config.getWardenMinRange();
        double falloffCurve = config.getWardenFalloffCurve();

        // Check if in range
        if (distance > maxRange) {
            return;
        }

        // Calculate detection chance
        double detectionChance = RangeCalculator.calculateDetectionChance(
                distance, minRange, maxRange, falloffCurve
        );

        // Probabilistic detection
        if (Math.random() > detectionChance) {
            return;
        }

        // Calculate anger based on distance
        int angerIncrease = RangeCalculator.calculateWardenAnger(distance, minRange, maxRange);
        warden.increaseAnger(player, angerIncrease);

        // Debug logging
        if (plugin.getConfig().getBoolean("debug.warden-logging", false)) {
            plugin.getLogger().info(String.format(
                    "Warden anger +%d | %s",
                    angerIncrease,
                    RangeCalculator.getDebugInfo(distance, minRange, maxRange, falloffCurve)
            ));
        }
    }

    /**
     * Processes hostile mob behavior.
     */
    private void processHostileMob(Mob mob, Player player, Location playerLoc) {
        ConfigManager config = plugin.getConfigManager();

        if (!config.isHostileMobsEnabled()) {
            return;
        }

        if (config.isHostileBlacklisted(mob.getType())) {
            return;
        }

        double distance = mob.getLocation().distance(playerLoc);
        double maxRange = config.getHostileMaxRange();
        double minRange = config.getHostileMinRange();
        double falloffCurve = config.getHostileFalloffCurve();

        if (distance > maxRange) {
            return;
        }

        double detectionChance = RangeCalculator.calculateDetectionChance(
                distance, minRange, maxRange, falloffCurve
        );

        if (Math.random() > detectionChance) {
            return;
        }

        // Target player
        if (mob.getTarget() == null) {
            mob.setTarget(player);
        }

        // Debug logging
        if (plugin.getConfig().getBoolean("debug.detection-logging", false)) {
            plugin.getLogger().info(String.format(
                    "Hostile %s detected | %s",
                    mob.getType(),
                    RangeCalculator.getDebugInfo(distance, minRange, maxRange, falloffCurve)
            ));
        }
    }

    /**
     * Processes neutral mob behavior (look at player).
     */
    private void processNeutralMob(Mob mob, Player player, Location playerLoc) {
        ConfigManager config = plugin.getConfigManager();

        if (!config.isNeutralMobsEnabled()) {
            return;
        }

        if (config.isNeutralBlacklisted(mob.getType())) {
            return;
        }

        double distance = mob.getLocation().distance(playerLoc);
        double maxRange = config.getNeutralMaxRange();
        double minRange = config.getNeutralMinRange();
        double falloffCurve = config.getNeutralFalloffCurve();

        if (distance > maxRange) {
            return;
        }

        double detectionChance = RangeCalculator.calculateDetectionChance(
                distance, minRange, maxRange, falloffCurve
        );

        if (Math.random() > detectionChance) {
            return;
        }

        // Make mob look at player
        if (config.shouldNeutralLookAtPlayer()) {
            makeMobLookAt(mob, player.getLocation());
        }

        // Debug logging
        if (plugin.getConfig().getBoolean("debug.detection-logging", false)) {
            plugin.getLogger().info(String.format(
                    "Neutral %s looked | %s",
                    mob.getType(),
                    RangeCalculator.getDebugInfo(distance, minRange, maxRange, falloffCurve)
            ));
        }
    }

    /**
     * Processes peaceful mob behavior (look at player, follow when sneaking).
     */
    private void processPeacefulMob(Mob mob, Player player, Location playerLoc, boolean isSneaking) {
        ConfigManager config = plugin.getConfigManager();

        if (!config.isPeacefulMobsEnabled()) {
            return;
        }

        if (config.isPeacefulBlacklisted(mob.getType())) {
            return;
        }

        double distance = mob.getLocation().distance(playerLoc);
        double maxRange = config.getPeacefulMaxRange();
        double minRange = config.getPeacefulMinRange();
        double falloffCurve = config.getPeacefulFalloffCurve();

        if (distance > maxRange) {
            return;
        }

        double detectionChance = RangeCalculator.calculateDetectionChance(
                distance, minRange, maxRange, falloffCurve
        );

        if (Math.random() > detectionChance) {
            return;
        }

        // Make mob look at player
        if (config.shouldPeacefulLookAtPlayer()) {
            makeMobLookAt(mob, player.getLocation());
        }

        // If sneaking: make mob follow
        if (isSneaking && config.isFollowWhenSneakingEnabled()) {
            startFollowing(mob, player);
        }

        // Debug logging
        if (plugin.getConfig().getBoolean("debug.peaceful-logging", false)) {
            plugin.getLogger().info(String.format(
                    "Peaceful %s %s | %s",
                    mob.getType(),
                    isSneaking ? "following" : "looked",
                    RangeCalculator.getDebugInfo(distance, minRange, maxRange, falloffCurve)
            ));
        }
    }

    /**
     * Makes a mob look at a location.
     */
    private void makeMobLookAt(Mob mob, Location target) {
        Location mobLoc = mob.getLocation();
        Location direction = target.clone().subtract(mobLoc);

        mobLoc.setDirection(direction.toVector());
        mob.teleport(mobLoc);
    }

    /**
     * Starts mob following player.
     */
    private void startFollowing(Mob mob, Player player) {
        UUID mobId = mob.getUniqueId();
        UUID playerId = player.getUniqueId();

        // Update tracking
        followingMobs.put(mobId, playerId);
        followStartTime.put(mobId, System.currentTimeMillis());

        // Set metadata
        mob.setMetadata(FOLLOW_META_KEY, new FixedMetadataValue(plugin, true));
        mob.setMetadata(FOLLOW_PLAYER_KEY, new FixedMetadataValue(plugin, playerId.toString()));
    }

    /**
     * Starts background task to manage following mobs.
     */
    private void startFollowCleanupTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long currentTime = System.currentTimeMillis();
            ConfigManager config = plugin.getConfigManager();
            int maxDuration = config.getFollowDuration() * 1000;  // Convert to ms
            double maxDistance = config.getFollowMaxDistance();

            followingMobs.entrySet().removeIf(entry -> {
                UUID mobId = entry.getKey();
                UUID playerId = entry.getValue();

                // Check duration
                Long startTime = followStartTime.get(mobId);
                if (startTime == null || (currentTime - startTime) > maxDuration) {
                    cleanupMob(mobId);
                    return true;
                }

                // Check distance and make mob follow
                Entity mobEntity = Bukkit.getEntity(mobId);
                Player player = Bukkit.getPlayer(playerId);

                if (mobEntity == null || player == null || !player.isOnline()) {
                    cleanupMob(mobId);
                    return true;
                }

                if (mobEntity instanceof Mob) {
                    Mob mob = (Mob) mobEntity;
                    double distance = mob.getLocation().distance(player.getLocation());

                    if (distance > maxDistance) {
                        cleanupMob(mobId);
                        return true;
                    }

                    // Make mob pathfind to player
                    mob.getPathfinder().moveTo(player.getLocation());
                }

                return false;
            });
        }, 20L, 20L);  // Run every second
    }

    /**
     * Cleans up mob following state.
     */
    private void cleanupMob(UUID mobId) {
        Entity entity = Bukkit.getEntity(mobId);
        if (entity != null) {
            entity.removeMetadata(FOLLOW_META_KEY, plugin);
            entity.removeMetadata(FOLLOW_PLAYER_KEY, plugin);

            if (entity instanceof Mob) {
                ((Mob) entity).getPathfinder().stopPathfinding();
            }
        }

        followStartTime.remove(mobId);
    }
}