package de.tecca.simplevoicemechanics.manager;

import de.tecca.simplevoicemechanics.SimpleVoiceMechanics;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages plugin configuration with range-based detection system.
 *
 * <p>Handles:
 * <ul>
 *   <li>Global detection settings (max-range, min-range, falloff-curve)</li>
 *   <li>Per-category overrides (hostile, neutral, peaceful, warden, sculk)</li>
 *   <li>Per-category volume thresholds in decibels</li>
 *   <li>Mob blacklists</li>
 *   <li>Peaceful mob behaviors (look-at, follow-when-sneaking)</li>
 *   <li>Environmental modifiers (biome and weather)</li>
 * </ul>
 *
 * @author Tecca
 * @version 1.3.0
 */
public class ConfigManager {

    // Configuration paths
    private static final String PATH_DETECTION = "detection";
    private static final String PATH_MOB_HEARING = "mob-hearing";
    private static final String PATH_SCULK = "sculk-hearing";
    private static final String PATH_ENVIRONMENTAL = "environmental-modifiers";

    private final SimpleVoiceMechanics plugin;
    private FileConfiguration config;

    // Global detection settings
    private double defaultMaxRange;
    private double defaultMinRange;
    private double defaultFalloffCurve;

    // Mob hearing settings
    private boolean mobHearingEnabled;

    // Hostile mobs
    private boolean hostileMobsEnabled;
    private Double hostileMaxRange;
    private Double hostileMinRange;
    private Double hostileFalloffCurve;
    private double hostileVolumeThresholdDb;
    private Set<EntityType> hostileBlacklist;

    // Neutral mobs
    private boolean neutralMobsEnabled;
    private boolean neutralLookAtPlayer;
    private Double neutralMaxRange;
    private Double neutralMinRange;
    private Double neutralFalloffCurve;
    private double neutralVolumeThresholdDb;
    private double neutralReactionChance;
    private int neutralLookDuration;
    private int neutralReactionCooldown;
    private Set<EntityType> neutralBlacklist;

    // Peaceful mobs
    private boolean peacefulMobsEnabled;
    private boolean peacefulLookAtPlayer;
    private Double peacefulMaxRange;
    private Double peacefulMinRange;
    private Double peacefulFalloffCurve;
    private double peacefulVolumeThresholdDb;
    private double peacefulReactionChance;
    private int peacefulLookDuration;
    private int peacefulReactionCooldown;
    private Set<EntityType> peacefulBlacklist;
    // Flee behavior
    private boolean fleeEnabled;
    private double fleeVolumeDb;
    private double fleeDistance;
    private int fleeDuration;
    // Follow behavior
    private boolean followWhenSneakingEnabled;
    private boolean requireEyeContact;
    private double eyeContactRange;
    private int eyeContactMemory;
    private int followDuration;
    private double followMaxDistance;

    // Warden
    private boolean wardenEnabled;
    private Double wardenMaxRange;
    private Double wardenMinRange;
    private Double wardenFalloffCurve;
    private double wardenVolumeThresholdDb;

    // Sculk sensors
    private boolean sculkEnabled;
    private Double sculkMaxRange;
    private Double sculkMinRange;
    private Double sculkFalloffCurve;
    private double sculkVolumeThresholdDb;
    private long sculkCooldown;

    // Environmental modifiers
    private boolean environmentalModifiersEnabled;
    private boolean biomeModifiersEnabled;
    private boolean weatherModifiersEnabled;

    public ConfigManager(SimpleVoiceMechanics plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads configuration from disk.
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        loadGlobalSettings();
        loadMobHearingSettings();
        loadSculkSettings();
        loadEnvironmentalSettings();
    }

    /**
     * Loads global detection settings.
     */
    private void loadGlobalSettings() {
        defaultMaxRange = config.getDouble(PATH_DETECTION + ".max-range", 16.0);
        defaultMinRange = config.getDouble(PATH_DETECTION + ".min-range", 2.0);
        defaultFalloffCurve = config.getDouble(PATH_DETECTION + ".falloff-curve", 1.0);
    }

    /**
     * Loads all mob hearing settings.
     */
    private void loadMobHearingSettings() {
        mobHearingEnabled = config.getBoolean(PATH_MOB_HEARING + ".enabled", true);

        // Hostile mobs
        loadHostileMobSettings();

        // Neutral mobs
        loadNeutralMobSettings();

        // Peaceful mobs
        loadPeacefulMobSettings();

        // Warden
        loadWardenSettings();
    }

    /**
     * Loads hostile mob settings.
     */
    private void loadHostileMobSettings() {
        String path = PATH_MOB_HEARING + ".hostile-mobs";

        hostileMobsEnabled = config.getBoolean(path + ".enabled", true);
        hostileVolumeThresholdDb = config.getDouble(path + ".volume-threshold-db", -40.0);
        hostileMaxRange = getOptionalDouble(path + ".max-range");
        hostileMinRange = getOptionalDouble(path + ".min-range");
        hostileFalloffCurve = getOptionalDouble(path + ".falloff-curve");
        hostileBlacklist = loadBlacklist(path + ".blacklist");
    }

    /**
     * Loads neutral mob settings.
     */
    private void loadNeutralMobSettings() {
        String path = PATH_MOB_HEARING + ".neutral-mobs";

        neutralMobsEnabled = config.getBoolean(path + ".enabled", true);
        neutralLookAtPlayer = config.getBoolean(path + ".look-at-player", true);
        neutralVolumeThresholdDb = config.getDouble(path + ".volume-threshold-db", -35.0);
        neutralReactionChance = config.getDouble(path + ".natural-behavior.reaction-chance", 0.6);
        neutralLookDuration = config.getInt(path + ".natural-behavior.look-duration", 8);
        neutralReactionCooldown = config.getInt(path + ".natural-behavior.reaction-cooldown", 3);
        neutralMaxRange = getOptionalDouble(path + ".max-range");
        neutralMinRange = getOptionalDouble(path + ".min-range");
        neutralFalloffCurve = getOptionalDouble(path + ".falloff-curve");
        neutralBlacklist = loadBlacklist(path + ".blacklist");
    }

    /**
     * Loads peaceful mob settings.
     */
    private void loadPeacefulMobSettings() {
        String path = PATH_MOB_HEARING + ".peaceful-mobs";

        peacefulMobsEnabled = config.getBoolean(path + ".enabled", true);
        peacefulLookAtPlayer = config.getBoolean(path + ".look-at-player", true);
        peacefulVolumeThresholdDb = config.getDouble(path + ".volume-threshold-db", -30.0);
        peacefulReactionChance = config.getDouble(path + ".natural-behavior.reaction-chance", 0.7);
        peacefulLookDuration = config.getInt(path + ".natural-behavior.look-duration", 10);
        peacefulReactionCooldown = config.getInt(path + ".natural-behavior.reaction-cooldown", 3);
        peacefulMaxRange = getOptionalDouble(path + ".max-range");
        peacefulMinRange = getOptionalDouble(path + ".min-range");
        peacefulFalloffCurve = getOptionalDouble(path + ".falloff-curve");
        peacefulBlacklist = loadBlacklist(path + ".blacklist");

        // Flee behavior (all in seconds from config)
        String fleePath = path + ".flee-behavior";
        fleeEnabled = config.getBoolean(fleePath + ".enabled", true);
        fleeVolumeDb = config.getDouble(fleePath + ".flee-volume-db", -20.0);
        fleeDistance = config.getDouble(fleePath + ".flee-distance", 3.0);
        fleeDuration = config.getInt(fleePath + ".flee-duration", 3);  // seconds

        // Follow when sneaking (all in seconds from config)
        String followPath = path + ".follow-when-sneaking";
        followWhenSneakingEnabled = config.getBoolean(followPath + ".enabled", true);
        requireEyeContact = config.getBoolean(followPath + ".require-eye-contact", true);
        eyeContactRange = config.getDouble(followPath + ".eye-contact-range", 4.0);
        eyeContactMemory = config.getInt(followPath + ".eye-contact-memory", 5);  // seconds
        followDuration = config.getInt(followPath + ".duration", 60);  // seconds
        followMaxDistance = config.getDouble(followPath + ".max-distance", 12.0);
    }

    /**
     * Loads warden settings.
     */
    private void loadWardenSettings() {
        String path = PATH_MOB_HEARING + ".warden";

        wardenEnabled = config.getBoolean(path + ".enabled", true);
        wardenVolumeThresholdDb = config.getDouble(path + ".volume-threshold-db", -50.0);
        wardenMaxRange = getOptionalDouble(path + ".max-range");
        wardenMinRange = getOptionalDouble(path + ".min-range");
        wardenFalloffCurve = getOptionalDouble(path + ".falloff-curve");
    }

    /**
     * Loads sculk sensor settings.
     */
    private void loadSculkSettings() {
        sculkEnabled = config.getBoolean(PATH_SCULK + ".enabled", true);
        sculkVolumeThresholdDb = config.getDouble(PATH_SCULK + ".volume-threshold-db", -45.0);
        sculkMaxRange = getOptionalDouble(PATH_SCULK + ".max-range");
        sculkMinRange = getOptionalDouble(PATH_SCULK + ".min-range");
        sculkFalloffCurve = getOptionalDouble(PATH_SCULK + ".falloff-curve");
        sculkCooldown = config.getLong(PATH_SCULK + ".cooldown", 1000);
    }

    /**
     * Loads environmental modifier settings.
     */
    private void loadEnvironmentalSettings() {
        environmentalModifiersEnabled = config.getBoolean(PATH_ENVIRONMENTAL + ".enabled", true);
        biomeModifiersEnabled = config.getBoolean(PATH_ENVIRONMENTAL + ".biome-modifiers.enabled", true);
        weatherModifiersEnabled = config.getBoolean(PATH_ENVIRONMENTAL + ".weather-modifiers.enabled", true);
    }

    /**
     * Gets optional double from config (returns null if not set or null).
     */
    private Double getOptionalDouble(String path) {
        if (!config.isSet(path) || config.getString(path) == null ||
                config.getString(path).equalsIgnoreCase("null")) {
            return null;
        }
        return config.getDouble(path);
    }

    /**
     * Loads entity type blacklist from config.
     */
    private Set<EntityType> loadBlacklist(String path) {
        Set<EntityType> blacklist = new HashSet<>();
        List<String> list = config.getStringList(path);

        for (String typeName : list) {
            try {
                EntityType type = EntityType.valueOf(typeName.toUpperCase());
                blacklist.add(type);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid entity type in blacklist: " + typeName);
            }
        }

        return blacklist;
    }

    /**
     * Reloads configuration.
     */
    public void reload() {
        loadConfig();
    }

    // ==================== GETTERS ====================

    // Global settings
    public double getDefaultMaxRange() { return defaultMaxRange; }
    public double getDefaultMinRange() { return defaultMinRange; }
    public double getDefaultFalloffCurve() { return defaultFalloffCurve; }

    // Mob hearing
    public boolean isMobHearingEnabled() { return mobHearingEnabled; }

    // Hostile mobs
    public boolean isHostileMobsEnabled() { return hostileMobsEnabled; }
    public double getHostileMaxRange() { return hostileMaxRange != null ? hostileMaxRange : defaultMaxRange; }
    public double getHostileMinRange() { return hostileMinRange != null ? hostileMinRange : defaultMinRange; }
    public double getHostileFalloffCurve() { return hostileFalloffCurve != null ? hostileFalloffCurve : defaultFalloffCurve; }
    public double getHostileVolumeThresholdDb() { return hostileVolumeThresholdDb; }
    public boolean isHostileBlacklisted(EntityType type) { return hostileBlacklist.contains(type); }

    // Neutral mobs
    public boolean isNeutralMobsEnabled() { return neutralMobsEnabled; }
    public boolean shouldNeutralLookAtPlayer() { return neutralLookAtPlayer; }
    public double getNeutralMaxRange() { return neutralMaxRange != null ? neutralMaxRange : defaultMaxRange; }
    public double getNeutralMinRange() { return neutralMinRange != null ? neutralMinRange : defaultMinRange; }
    public double getNeutralFalloffCurve() { return neutralFalloffCurve != null ? neutralFalloffCurve : defaultFalloffCurve; }
    public double getNeutralVolumeThresholdDb() { return neutralVolumeThresholdDb; }
    public double getNeutralReactionChance() { return neutralReactionChance; }
    public int getNeutralLookDurationTicks() { return neutralLookDuration * 20; }  // seconds to ticks
    public long getNeutralReactionCooldownMs() { return neutralReactionCooldown * 1000L; }  // seconds to ms
    public boolean isNeutralBlacklisted(EntityType type) { return neutralBlacklist.contains(type); }

    // Peaceful mobs
    public boolean isPeacefulMobsEnabled() { return peacefulMobsEnabled; }
    public boolean shouldPeacefulLookAtPlayer() { return peacefulLookAtPlayer; }
    public double getPeacefulMaxRange() { return peacefulMaxRange != null ? peacefulMaxRange : defaultMaxRange; }
    public double getPeacefulMinRange() { return peacefulMinRange != null ? peacefulMinRange : defaultMinRange; }
    public double getPeacefulFalloffCurve() { return peacefulFalloffCurve != null ? peacefulFalloffCurve : defaultFalloffCurve; }
    public double getPeacefulVolumeThresholdDb() { return peacefulVolumeThresholdDb; }
    public double getPeacefulReactionChance() { return peacefulReactionChance; }
    public int getPeacefulLookDurationTicks() { return peacefulLookDuration * 20; }  // seconds to ticks
    public long getPeacefulReactionCooldownMs() { return peacefulReactionCooldown * 1000L; }  // seconds to ms
    public boolean isPeacefulBlacklisted(EntityType type) { return peacefulBlacklist.contains(type); }
    // Flee behavior
    public boolean isFleeEnabled() { return fleeEnabled; }
    public double getFleeVolumeDb() { return fleeVolumeDb; }
    public double getFleeDistance() { return fleeDistance; }
    public int getFleeDurationTicks() { return fleeDuration * 20; }  // seconds to ticks
    // Follow behavior
    public boolean isFollowWhenSneakingEnabled() { return followWhenSneakingEnabled; }
    public boolean requiresEyeContact() { return requireEyeContact; }
    public double getEyeContactRange() { return eyeContactRange; }
    public long getEyeContactMemoryMs() { return eyeContactMemory * 1000L; }  // seconds to ms
    public int getFollowDuration() { return followDuration; }  // already in seconds
    public double getFollowMaxDistance() { return followMaxDistance; }

    // Warden
    public boolean isWardenEnabled() { return wardenEnabled; }
    public double getWardenMaxRange() { return wardenMaxRange != null ? wardenMaxRange : defaultMaxRange; }
    public double getWardenMinRange() { return wardenMinRange != null ? wardenMinRange : defaultMinRange; }
    public double getWardenFalloffCurve() { return wardenFalloffCurve != null ? wardenFalloffCurve : defaultFalloffCurve; }
    public double getWardenVolumeThresholdDb() { return wardenVolumeThresholdDb; }

    // Sculk
    public boolean isSculkEnabled() { return sculkEnabled; }
    public double getSculkMaxRange() { return sculkMaxRange != null ? sculkMaxRange : defaultMaxRange; }
    public double getSculkMinRange() { return sculkMinRange != null ? sculkMinRange : defaultMinRange; }
    public double getSculkFalloffCurve() { return sculkFalloffCurve != null ? sculkFalloffCurve : defaultFalloffCurve; }
    public double getSculkVolumeThresholdDb() { return sculkVolumeThresholdDb; }
    public long getSculkCooldown() { return sculkCooldown; }

    // Environmental modifiers
    public boolean isEnvironmentalModifiersEnabled() { return environmentalModifiersEnabled; }
    public boolean isWeatherModifiersEnabled() { return weatherModifiersEnabled && environmentalModifiersEnabled; }

    // Legacy compatibility (deprecated)
    @Deprecated
    public boolean isHostileMobHearingEnabled() { return hostileMobsEnabled; }
    @Deprecated
    public boolean isWardenHearingEnabled() { return wardenEnabled; }
    @Deprecated
    public boolean isSculkHearingEnabled() { return sculkEnabled; }
    @Deprecated
    public double getHearingRange() { return defaultMaxRange; }
    @Deprecated
    public float getVolumeThreshold() { return 0.0f; }  // No longer used
    @Deprecated
    public double getMinVolumeForDetection() { return 0.0; }  // No longer used
    @Deprecated
    public double getVolumeThresholdDb() { return hostileVolumeThresholdDb; }  // Use category-specific

    /**
     * Gets the raw FileConfiguration for debug settings.
     */
    public FileConfiguration getConfig() {
        return config;
    }

    // Setters for commands
    public void setMobHearingEnabled(boolean enabled) {
        this.mobHearingEnabled = enabled;
        config.set(PATH_MOB_HEARING + ".enabled", enabled);
        plugin.saveConfig();
    }

    public void setSculkHearingEnabled(boolean enabled) {
        this.sculkEnabled = enabled;
        config.set(PATH_SCULK + ".enabled", enabled);
        plugin.saveConfig();
    }

    public void setEnvironmentalModifiersEnabled(boolean enabled) {
        this.environmentalModifiersEnabled = enabled;
        config.set(PATH_ENVIRONMENTAL + ".enabled", enabled);
        plugin.saveConfig();
    }

    public boolean isBiomeModifiersEnabled() {
        return config.getBoolean("environmental-modifiers.biome-modifiers.enabled", true);
    }

    public boolean isTimeModifiersEnabled() {
        return config.getBoolean("environmental-modifiers.time-modifiers.enabled", true);
    }

    // Mob Group Alert
    public boolean isMobGroupAlertEnabled() {
        return config.getBoolean("mob-hearing.hostile-mobs.group-alert.enabled", true);
    }

    public int getMaxMobAlerts() {
        return config.getInt("mob-hearing.hostile-mobs.group-alert.max-alerts", 5);
    }

    public double getGroupAlertRange(String mobType) {
        String path = "mob-hearing.hostile-mobs.group-alert.ranges." + mobType.toLowerCase();
        return config.getDouble(path, 16.0);
    }
}