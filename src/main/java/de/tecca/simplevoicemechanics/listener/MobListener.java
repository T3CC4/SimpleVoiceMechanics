package de.tecca.simplevoicemechanics.listener;

import de.tecca.simplevoicemechanics.SimpleVoiceMechanics;
import de.tecca.simplevoicemechanics.event.VoiceDetectedEvent;
import de.tecca.simplevoicemechanics.manager.ConfigManager;
import de.tecca.simplevoicemechanics.util.MobCategory;
import de.tecca.simplevoicemechanics.util.MobCondition;
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
 * @version 1.2.0
 */
public class MobListener implements Listener {

    private static final String FOLLOW_META_KEY = "svm_following";
    private static final String FOLLOW_PLAYER_KEY = "svm_follow_player";
    private static final String EYE_CONTACT_META_KEY = "svm_eye_contact";
    private static final String EYE_CONTACT_TIME_KEY = "svm_eye_contact_time";
    private static final String FLEE_META_KEY = "svm_fleeing";

    private final SimpleVoiceMechanics plugin;

    // Tracks which mobs are currently following which players
    private final Map<UUID, UUID> followingMobs = new HashMap<>();  // MobUUID -> PlayerUUID
    private final Map<UUID, Long> followStartTime = new HashMap<>();  // MobUUID -> StartTime

    // Reaction cooldowns to prevent rapid re-triggering
    private final Map<UUID, Long> reactionCooldowns = new HashMap<>();  // MobUUID -> LastReactionTime

    // Look-at tracking for duration
    private final Map<UUID, Long> lookAtStartTime = new HashMap<>();  // MobUUID -> StartTime

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
        double decibels = event.getDecibels();
        boolean isSneaking = player.isSneaking();

        // Process all nearby mobs
        processNearbyMobs(player, loc, isSneaking, decibels);
    }

    /**
     * Checks if a mob is on reaction cooldown.
     */
    private boolean isOnReactionCooldown(Mob mob, long cooldownMs) {
        Long lastReaction = reactionCooldowns.get(mob.getUniqueId());
        if (lastReaction == null) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        return (currentTime - lastReaction) < cooldownMs;
    }

    /**
     * Sets reaction cooldown for a mob.
     */
    private void setReactionCooldown(Mob mob) {
        reactionCooldowns.put(mob.getUniqueId(), System.currentTimeMillis());
    }

    /**
     * Makes mob look at location with scheduled stop after duration.
     */
    private void makeMobLookAtWithDuration(Mob mob, Location target, int durationTicks) {
        UUID mobId = mob.getUniqueId();

        // Start looking
        makeMobLookAt(mob, target);
        lookAtStartTime.put(mobId, System.currentTimeMillis());

        // Schedule stop looking after duration
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (mob.isValid()) {
                // Stop looking by resetting pathfinder (mob will look forward again)
                mob.getPathfinder().stopPathfinding();
                lookAtStartTime.remove(mobId);
            }
        }, durationTicks);
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
    private void processNearbyMobs(Player player, Location loc, boolean isSneaking, double decibels) {
        ConfigManager config = plugin.getConfigManager();

        // Use max possible range to check all entities
        double maxCheckRange = Math.max(
                Math.max(config.getHostileMaxRange(), config.getNeutralMaxRange()),
                Math.max(config.getPeacefulMaxRange(), config.getWardenMaxRange())
        );

        // Account for volume boost (loud voices = larger search area)
        maxCheckRange = RangeCalculator.calculateEffectiveRange(maxCheckRange, decibels, config.getVolumeThresholdDb());

        Collection<Entity> nearbyEntities = loc.getWorld()
                .getNearbyEntities(loc, maxCheckRange, maxCheckRange, maxCheckRange);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof Mob) {
                processMob((Mob) entity, player, loc, isSneaking, decibels);
            }
        }
    }

    /**
     * Processes a single mob's reaction to voice.
     */
    private void processMob(Mob mob, Player player, Location playerLoc, boolean isSneaking, double decibels) {
        EntityType type = mob.getType();
        ConfigManager config = plugin.getConfigManager();

        // Determine mob category and process accordingly
        if (type == EntityType.WARDEN && config.isWardenEnabled()) {
            processWarden((Warden) mob, player, playerLoc, decibels);
        } else if (MobCategory.isHostile(type)) {
            processHostileMob(mob, player, playerLoc, decibels);
        } else if (MobCategory.isNeutral(type)) {
            processNeutralMob(mob, player, playerLoc, decibels);
        } else if (MobCategory.isPeaceful(type)) {
            processPeacefulMob(mob, player, playerLoc, isSneaking, decibels);
        }
    }

    /**
     * Processes Warden special behavior.
     */
    private void processWarden(Warden warden, Player player, Location playerLoc, double decibels) {
        ConfigManager config = plugin.getConfigManager();

        // Check threshold
        if (decibels < config.getWardenVolumeThresholdDb()) {
            return;
        }

        double distance = warden.getLocation().distance(playerLoc);
        double maxRange = config.getWardenMaxRange();
        double minRange = config.getWardenMinRange();
        double falloffCurve = config.getWardenFalloffCurve();
        double volumeThresholdDb = config.getWardenVolumeThresholdDb();

        // Calculate effective range based on volume
        double effectiveMaxRange = RangeCalculator.calculateEffectiveRange(maxRange, decibels, volumeThresholdDb);

        // Check if in effective range
        if (distance > effectiveMaxRange) {
            return;
        }

        // Calculate detection chance with dynamic range
        double detectionChance = RangeCalculator.calculateDynamicDetectionChance(
                distance, minRange, maxRange, falloffCurve, decibels, volumeThresholdDb
        );

        // Probabilistic detection
        if (Math.random() > detectionChance) {
            return;
        }

        // Calculate anger based on distance and volume
        int angerIncrease = RangeCalculator.calculateWardenAnger(distance, minRange, maxRange, decibels, volumeThresholdDb);
        warden.increaseAnger(player, angerIncrease);

        // Debug logging
        if (plugin.getConfig().getBoolean("debug.warden-logging", false)) {
            plugin.getLogger().info(String.format(
                    "Warden anger +%d | %s",
                    angerIncrease,
                    RangeCalculator.getDynamicDebugInfo(distance, minRange, maxRange, falloffCurve, decibels, volumeThresholdDb)
            ));
        }
    }

    /**
     * Processes hostile mob behavior.
     */
    private void processHostileMob(Mob mob, Player player, Location playerLoc, double decibels) {
        ConfigManager config = plugin.getConfigManager();

        if (!config.isHostileMobsEnabled()) {
            return;
        }

        if (config.isHostileBlacklisted(mob.getType())) {
            return;
        }

        // Check if mob can attack (not tamed, not baby)
        if (!MobCondition.canAttack(mob)) {
            return;
        }

        // Check threshold
        if (decibels < config.getHostileVolumeThresholdDb()) {
            return;
        }

        double distance = mob.getLocation().distance(playerLoc);
        double maxRange = config.getHostileMaxRange();
        double minRange = config.getHostileMinRange();
        double falloffCurve = config.getHostileFalloffCurve();
        double volumeThresholdDb = config.getHostileVolumeThresholdDb();

        // Calculate effective range based on volume
        double effectiveMaxRange = RangeCalculator.calculateEffectiveRange(maxRange, decibels, volumeThresholdDb);

        if (distance > effectiveMaxRange) {
            return;
        }

        // Calculate detection chance with dynamic range
        double detectionChance = RangeCalculator.calculateDynamicDetectionChance(
                distance, minRange, maxRange, falloffCurve, decibels, volumeThresholdDb
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
                    RangeCalculator.getDynamicDebugInfo(distance, minRange, maxRange, falloffCurve, decibels, volumeThresholdDb)
            ));
        }
    }

    /**
     * Processes neutral mob behavior (look at player).
     */
    private void processNeutralMob(Mob mob, Player player, Location playerLoc, double decibels) {
        ConfigManager config = plugin.getConfigManager();

        if (!config.isNeutralMobsEnabled()) {
            return;
        }

        if (config.isNeutralBlacklisted(mob.getType())) {
            return;
        }

        // Check threshold
        if (decibels < config.getNeutralVolumeThresholdDb()) {
            return;
        }

        // Check reaction cooldown - prevents rapid re-triggering
        if (isOnReactionCooldown(mob, config.getNeutralReactionCooldownMs())) {
            return;
        }

        double distance = mob.getLocation().distance(playerLoc);
        double maxRange = config.getNeutralMaxRange();
        double minRange = config.getNeutralMinRange();
        double falloffCurve = config.getNeutralFalloffCurve();
        double volumeThresholdDb = config.getNeutralVolumeThresholdDb();

        // Calculate effective range based on volume
        double effectiveMaxRange = RangeCalculator.calculateEffectiveRange(maxRange, decibels, volumeThresholdDb);

        if (distance > effectiveMaxRange) {
            return;
        }

        // Calculate detection chance with dynamic range
        double detectionChance = RangeCalculator.calculateDynamicDetectionChance(
                distance, minRange, maxRange, falloffCurve, decibels, volumeThresholdDb
        );

        if (Math.random() > detectionChance) {
            return;
        }

        // Apply reaction multiplier for special mobs (e.g., babies)
        double reactionChance = config.getNeutralReactionChance() * MobCondition.getReactionMultiplier(mob);
        if (Math.random() > reactionChance) {
            return;
        }

        // Set cooldown to prevent immediate re-reaction
        setReactionCooldown(mob);

        // Make mob look at player with duration (only if can look)
        if (config.shouldNeutralLookAtPlayer() && MobCondition.canLookAt(mob)) {
            makeMobLookAtWithDuration(mob, player.getLocation(), config.getNeutralLookDurationTicks());
        }

        // Debug logging
        if (plugin.getConfig().getBoolean("debug.detection-logging", false)) {
            plugin.getLogger().info(String.format(
                    "Neutral %s looked | %s",
                    mob.getType(),
                    RangeCalculator.getDynamicDebugInfo(distance, minRange, maxRange, falloffCurve, decibels, volumeThresholdDb)
            ));
        }
    }

    /**
     * Processes peaceful mob behavior (look at player, flee from loud noise, follow when sneaking).
     */
    private void processPeacefulMob(Mob mob, Player player, Location playerLoc, boolean isSneaking, double decibels) {
        ConfigManager config = plugin.getConfigManager();

        if (!config.isPeacefulMobsEnabled()) {
            return;
        }

        if (config.isPeacefulBlacklisted(mob.getType())) {
            return;
        }

        // Check threshold
        if (decibels < config.getPeacefulVolumeThresholdDb()) {
            return;
        }

        double distance = mob.getLocation().distance(playerLoc);
        double maxRange = config.getPeacefulMaxRange();
        double minRange = config.getPeacefulMinRange();
        double falloffCurve = config.getPeacefulFalloffCurve();
        double volumeThresholdDb = config.getPeacefulVolumeThresholdDb();

        // Calculate effective range based on volume
        double effectiveMaxRange = RangeCalculator.calculateEffectiveRange(maxRange, decibels, volumeThresholdDb);

        if (distance > effectiveMaxRange) {
            return;
        }

        // Calculate detection chance with dynamic range
        double detectionChance = RangeCalculator.calculateDynamicDetectionChance(
                distance, minRange, maxRange, falloffCurve, decibels, volumeThresholdDb
        );

        if (Math.random() > detectionChance) {
            return;
        }

        // Apply reaction multiplier for special mobs (e.g., babies)
        double reactionChance = config.getPeacefulReactionChance() * MobCondition.getReactionMultiplier(mob);
        if (Math.random() > reactionChance) {
            return;
        }

        // Check if mob should flee from loud noise (flee overrides cooldown)
        if (config.isFleeEnabled() && decibels > config.getFleeVolumeDb() &&
                !mob.hasMetadata(FLEE_META_KEY) && MobCondition.canFlee(mob)) {
            setReactionCooldown(mob);
            makeMobFlee(mob, player);

            if (plugin.getConfig().getBoolean("debug.peaceful-logging", false)) {
                plugin.getLogger().info(String.format(
                        "Peaceful %s fleeing | dB: %.1f | Distance: %.1f",
                        mob.getType(), decibels, distance
                ));
            }
            return;
        }

        // Check reaction cooldown for normal behaviors (look/follow)
        // But allow eye contact tracking even on cooldown
        boolean onCooldown = isOnReactionCooldown(mob, config.getPeacefulReactionCooldownMs());

        // Always track eye contact if player is looking at mob (even on cooldown)
        if (config.shouldPeacefulLookAtPlayer() && isPlayerLookingAtMob(player, mob, config.getEyeContactRange())) {
            recordEyeContact(mob, player);
        }

        // Skip other reactions if on cooldown
        if (onCooldown) {
            return;
        }

        // Set cooldown for this reaction
        setReactionCooldown(mob);

        // Make mob look at player with duration (only if can look)
        if (config.shouldPeacefulLookAtPlayer() && MobCondition.canLookAt(mob)) {
            makeMobLookAtWithDuration(mob, player.getLocation(), config.getPeacefulLookDurationTicks());
        }

        // If sneaking: check eye contact requirement and make mob follow (only if can follow)
        if (isSneaking && config.isFollowWhenSneakingEnabled() && MobCondition.canFollow(mob)) {
            if (config.requiresEyeContact()) {
                // Check if player has established eye contact recently
                if (hasRecentEyeContact(mob, player)) {
                    startFollowing(mob, player);
                }
            } else {
                // No eye contact required
                startFollowing(mob, player);
            }
        }

        // Debug logging
        if (plugin.getConfig().getBoolean("debug.peaceful-logging", false)) {
            String action = isSneaking ? "following" : "looked";
            plugin.getLogger().info(String.format(
                    "Peaceful %s %s | %s",
                    mob.getType(),
                    action,
                    RangeCalculator.getDynamicDebugInfo(distance, minRange, maxRange, falloffCurve, decibels, volumeThresholdDb)
            ));
        }
    }

    /**
     * Makes a mob flee from the player.
     */
    private void makeMobFlee(Mob mob, Player player) {
        ConfigManager config = plugin.getConfigManager();

        // Set metadata to prevent repeated flee triggers
        mob.setMetadata(FLEE_META_KEY, new FixedMetadataValue(plugin, true));

        // Calculate flee location (away from player)
        Location mobLoc = mob.getLocation();
        Location playerLoc = player.getLocation();

        // Vector from player to mob (direction to flee)
        org.bukkit.util.Vector direction = mobLoc.toVector().subtract(playerLoc.toVector()).normalize();

        // Target location: flee-distance blocks away
        Location fleeTarget = mobLoc.clone().add(direction.multiply(config.getFleeDistance()));

        // Make mob pathfind away
        mob.getPathfinder().moveTo(fleeTarget);

        // Remove flee metadata after duration (in ticks)
        int fleeDurationTicks = config.getFleeDurationTicks();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (mob.isValid()) {
                mob.removeMetadata(FLEE_META_KEY, plugin);
                mob.getPathfinder().stopPathfinding();
            }
        }, fleeDurationTicks);
    }

    /**
     * Checks if player is looking at a mob within range.
     */
    private boolean isPlayerLookingAtMob(Player player, Mob mob, double range) {
        Location playerEye = player.getEyeLocation();
        Location mobLoc = mob.getLocation().add(0, mob.getHeight() / 2, 0);

        double distance = playerEye.distance(mobLoc);
        if (distance > range) {
            return false;
        }

        // Check if mob is in player's line of sight
        org.bukkit.util.Vector toMob = mobLoc.toVector().subtract(playerEye.toVector()).normalize();
        org.bukkit.util.Vector playerDirection = playerEye.getDirection();

        // Dot product to check angle (1.0 = directly looking, 0.0 = perpendicular)
        double dotProduct = toMob.dot(playerDirection);

        // Allow ~45 degree cone (cos(45°) ≈ 0.7)
        return dotProduct > 0.7;
    }

    /**
     * Records eye contact between player and mob.
     */
    private void recordEyeContact(Mob mob, Player player) {
        mob.setMetadata(EYE_CONTACT_META_KEY, new FixedMetadataValue(plugin, player.getUniqueId().toString()));
        mob.setMetadata(EYE_CONTACT_TIME_KEY, new FixedMetadataValue(plugin, System.currentTimeMillis()));
    }

    /**
     * Checks if player has recent eye contact with mob.
     */
    private boolean hasRecentEyeContact(Mob mob, Player player) {
        if (!mob.hasMetadata(EYE_CONTACT_META_KEY)) {
            return false;
        }

        // Check if eye contact was with this player
        String contactedPlayer = mob.getMetadata(EYE_CONTACT_META_KEY).get(0).asString();
        if (!contactedPlayer.equals(player.getUniqueId().toString())) {
            return false;
        }

        // Check if eye contact is recent enough
        if (!mob.hasMetadata(EYE_CONTACT_TIME_KEY)) {
            return false;
        }

        long contactTime = mob.getMetadata(EYE_CONTACT_TIME_KEY).get(0).asLong();
        long currentTime = System.currentTimeMillis();
        long memoryDurationMs = plugin.getConfigManager().getEyeContactMemoryMs();

        return (currentTime - contactTime) < memoryDurationMs;
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
     * Starts mob following player (only if mob can follow).
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

                    // Check if mob can still follow
                    if (!MobCondition.canFollow(mob)) {
                        cleanupMob(mobId);
                        return true;
                    }

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

            // Clean up old reaction cooldowns (remove entries older than 30 seconds)
            reactionCooldowns.entrySet().removeIf(entry ->
                    (currentTime - entry.getValue()) > 30000L  // 30 seconds
            );
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