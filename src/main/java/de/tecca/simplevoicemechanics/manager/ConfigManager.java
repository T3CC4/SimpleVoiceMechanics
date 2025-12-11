package de.tecca.simplevoicemechanics.manager;

import de.tecca.simplevoicemechanics.SimpleVoiceMechanics;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Manages plugin configuration settings.
 *
 * <p>Handles loading, saving, and accessing all configuration values
 * including:
 * <ul>
 *   <li>Mob hearing settings</li>
 *   <li>Sculk Sensor detection</li>
 *   <li>Detection ranges and thresholds</li>
 * </ul>
 *
 * @author Tecca
 * @version 1.0.0
 * @since 1.0.0
 */
public class ConfigManager {

    // Configuration keys
    private static final String KEY_MOB_ENABLED = "mob-hearing.enabled";
    private static final String KEY_MOB_HOSTILE = "mob-hearing.hostile-mobs";
    private static final String KEY_MOB_WARDEN = "mob-hearing.warden";
    private static final String KEY_SCULK_ENABLED = "sculk-hearing.enabled";
    private static final String KEY_HEARING_RANGE = "hearing-range";
    private static final String KEY_VOLUME_THRESHOLD = "volume-threshold";
    private static final String KEY_MIN_VOLUME = "detection.min-volume";

    // Default values
    private static final boolean DEFAULT_MOB_ENABLED = true;
    private static final boolean DEFAULT_HOSTILE_ENABLED = true;
    private static final boolean DEFAULT_WARDEN_ENABLED = true;
    private static final boolean DEFAULT_SCULK_ENABLED = true;
    private static final double DEFAULT_HEARING_RANGE = 16.0;
    private static final double DEFAULT_VOLUME_THRESHOLD = 0.1;
    private static final double DEFAULT_MIN_VOLUME = 0.2;

    private final SimpleVoiceMechanics plugin;
    private FileConfiguration config;

    // Cached configuration values
    private boolean mobHearingEnabled;
    private boolean hostileMobHearingEnabled;
    private boolean wardenHearingEnabled;
    private boolean sculkHearingEnabled;
    private double hearingRange;
    private float volumeThreshold;

    /**
     * Constructs a new ConfigManager.
     *
     * @param plugin the plugin instance
     */
    public ConfigManager(SimpleVoiceMechanics plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads the configuration from disk.
     *
     * <p>Creates default config if it doesn't exist, then loads
     * all values into memory for fast access.
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        // Load values
        mobHearingEnabled = config.getBoolean(KEY_MOB_ENABLED, DEFAULT_MOB_ENABLED);
        hostileMobHearingEnabled = config.getBoolean(KEY_MOB_HOSTILE, DEFAULT_HOSTILE_ENABLED);
        wardenHearingEnabled = config.getBoolean(KEY_MOB_WARDEN, DEFAULT_WARDEN_ENABLED);
        sculkHearingEnabled = config.getBoolean(KEY_SCULK_ENABLED, DEFAULT_SCULK_ENABLED);
        hearingRange = config.getDouble(KEY_HEARING_RANGE, DEFAULT_HEARING_RANGE);
        volumeThreshold = (float) config.getDouble(KEY_VOLUME_THRESHOLD, DEFAULT_VOLUME_THRESHOLD);

        // Save defaults if any were missing
        saveDefaults();
    }

    /**
     * Saves default configuration values.
     *
     * <p>Ensures all configuration keys exist with proper default values.
     */
    private void saveDefaults() {
        config.addDefault(KEY_MOB_ENABLED, DEFAULT_MOB_ENABLED);
        config.addDefault(KEY_MOB_HOSTILE, DEFAULT_HOSTILE_ENABLED);
        config.addDefault(KEY_MOB_WARDEN, DEFAULT_WARDEN_ENABLED);
        config.addDefault(KEY_SCULK_ENABLED, DEFAULT_SCULK_ENABLED);
        config.addDefault(KEY_HEARING_RANGE, DEFAULT_HEARING_RANGE);
        config.addDefault(KEY_VOLUME_THRESHOLD, DEFAULT_VOLUME_THRESHOLD);
        config.addDefault(KEY_MIN_VOLUME, DEFAULT_MIN_VOLUME);

        config.options().copyDefaults(true);
        plugin.saveConfig();
    }

    /**
     * Reloads the configuration from disk.
     */
    public void reload() {
        loadConfig();
    }

    /**
     * Gets whether mob hearing is enabled.
     *
     * @return true if mob hearing is enabled
     */
    public boolean isMobHearingEnabled() {
        return mobHearingEnabled;
    }

    /**
     * Gets whether hostile mob hearing is enabled.
     *
     * @return true if hostile mobs can hear
     */
    public boolean isHostileMobHearingEnabled() {
        return hostileMobHearingEnabled;
    }

    /**
     * Gets whether Warden hearing is enabled.
     *
     * @return true if Wardens can hear
     */
    public boolean isWardenHearingEnabled() {
        return wardenHearingEnabled;
    }

    /**
     * Gets whether Sculk Sensor detection is enabled.
     *
     * @return true if Sculk Sensors detect voice
     */
    public boolean isSculkHearingEnabled() {
        return sculkHearingEnabled;
    }

    /**
     * Gets the hearing range in blocks.
     *
     * @return the hearing range
     */
    public double getHearingRange() {
        return hearingRange;
    }

    /**
     * Gets the volume threshold for initial detection.
     *
     * <p>Voice below this volume will not trigger any events.
     *
     * @return the volume threshold (0.0 - 1.0)
     */
    public float getVolumeThreshold() {
        return volumeThreshold;
    }

    /**
     * Gets the minimum volume required for mob detection.
     *
     * <p>After distance calculation, effective volume must exceed
     * this threshold for mobs to detect the player.
     *
     * @return the minimum detection volume (0.0 - 1.0)
     */
    public double getMinVolumeForDetection() {
        return config.getDouble(KEY_MIN_VOLUME, DEFAULT_MIN_VOLUME);
    }

    /**
     * Sets whether mob hearing is enabled.
     *
     * <p>Saves the value to disk immediately.
     *
     * @param enabled whether mob hearing should be enabled
     */
    public void setMobHearingEnabled(boolean enabled) {
        this.mobHearingEnabled = enabled;
        config.set(KEY_MOB_ENABLED, enabled);
        plugin.saveConfig();
    }

    /**
     * Sets whether Sculk Sensor detection is enabled.
     *
     * <p>Saves the value to disk immediately.
     *
     * @param enabled whether Sculk Sensor detection should be enabled
     */
    public void setSculkHearingEnabled(boolean enabled) {
        this.sculkHearingEnabled = enabled;
        config.set(KEY_SCULK_ENABLED, enabled);
        plugin.saveConfig();
    }
}