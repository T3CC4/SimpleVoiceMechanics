package de.tecca.simplevoicemechanics.manager;

import de.tecca.simplevoicemechanics.SimpleVoiceMechanics;
import de.tecca.simplevoicemechanics.model.DetectionConfig;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Handles configuration file loading and saving.
 * Delegates to specialized managers for actual feature management.
 */
public class ConfigManager {

    private final SimpleVoiceMechanics plugin;
    private FileConfiguration config;

    public ConfigManager(SimpleVoiceMechanics plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads configuration from file and initializes managers.
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        saveDefaults();
    }

    /**
     * Loads detection configuration from file.
     *
     * @return DetectionConfig instance
     */
    public DetectionConfig loadDetectionConfig() {
        double hearingRange = validateRange(config.getDouble("detection.hearing-range", 16.0));
        double volumeThreshold = validateVolume(config.getDouble("detection.volume-threshold", 0.1));
        double minVolume = validateVolume(config.getDouble("detection.min-volume", 0.2));

        return new DetectionConfig(hearingRange, volumeThreshold, minVolume);
    }

    /**
     * Loads feature manager from file.
     *
     * @return FeatureManager instance
     */
    public FeatureManager loadFeatureManager() {
        boolean mobHearing = config.getBoolean("mob-hearing.enabled", true);
        boolean hostileMobs = config.getBoolean("mob-hearing.hostile-mobs", true);
        boolean warden = config.getBoolean("mob-hearing.warden", true);
        boolean sculk = config.getBoolean("sculk-hearing.enabled", true);

        return new FeatureManager(mobHearing, hostileMobs, warden, sculk);
    }

    /**
     * Saves feature states to config.
     *
     * @param featureManager Feature manager to save
     */
    public void saveFeatures(FeatureManager featureManager) {
        config.set("mob-hearing.enabled", featureManager.isMobHearingEnabled());
        config.set("sculk-hearing.enabled", featureManager.isSculkHearingEnabled());
        plugin.saveConfig();
    }

    /**
     * Reloads configuration from disk.
     */
    public void reload() {
        loadConfig();
        plugin.getLogger().info("Configuration reloaded successfully!");
    }

    /**
     * Validates hearing range value.
     *
     * @param range Range to validate
     * @return Valid range value
     */
    private double validateRange(double range) {
        if (range < 1.0) {
            plugin.getLogger().warning("Hearing range too low (" + range + "), using minimum: 1.0");
            return 1.0;
        }
        if (range > 128.0) {
            plugin.getLogger().warning("Hearing range too high (" + range + "), using maximum: 128.0");
            return 128.0;
        }
        return range;
    }

    /**
     * Validates volume value.
     *
     * @param volume Volume to validate
     * @return Valid volume value
     */
    private double validateVolume(double volume) {
        if (volume < 0.0) {
            plugin.getLogger().warning("Volume threshold cannot be negative, using 0.0");
            return 0.0;
        }
        if (volume > 1.0) {
            plugin.getLogger().warning("Volume threshold cannot exceed 1.0, using 1.0");
            return 1.0;
        }
        return volume;
    }

    /**
     * Saves default configuration values.
     */
    private void saveDefaults() {
        config.addDefault("mob-hearing.enabled", true);
        config.addDefault("mob-hearing.hostile-mobs", true);
        config.addDefault("mob-hearing.warden", true);
        config.addDefault("sculk-hearing.enabled", true);
        config.addDefault("detection.hearing-range", 16.0);
        config.addDefault("detection.volume-threshold", 0.1);
        config.addDefault("detection.min-volume", 0.2);

        config.options().copyDefaults(true);
        plugin.saveConfig();
    }
}