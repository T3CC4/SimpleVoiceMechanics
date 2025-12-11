package de.tecca.simplevoicemechanics.manager;

import de.tecca.simplevoicemechanics.SimpleVoiceMechanics;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final SimpleVoiceMechanics plugin;
    private FileConfiguration config;

    // Default Werte
    private boolean mobHearingEnabled;
    private boolean hostileMobHearingEnabled;
    private boolean wardenHearingEnabled;
    private boolean sculkHearingEnabled;
    private double hearingRange;
    private float volumeThreshold;

    public ConfigManager(SimpleVoiceMechanics plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        // Werte laden
        mobHearingEnabled = config.getBoolean("mob-hearing.enabled", true);
        hostileMobHearingEnabled = config.getBoolean("mob-hearing.hostile-mobs", true);
        wardenHearingEnabled = config.getBoolean("mob-hearing.warden", true);
        sculkHearingEnabled = config.getBoolean("sculk-hearing.enabled", true);
        hearingRange = config.getDouble("hearing-range", 16.0);
        volumeThreshold = (float) config.getDouble("volume-threshold", 0.1);

        // Config speichern falls neue Werte hinzugefügt wurden
        saveDefaults();
    }

    private void saveDefaults() {
        config.addDefault("mob-hearing.enabled", true);
        config.addDefault("mob-hearing.hostile-mobs", true);
        config.addDefault("mob-hearing.warden", true);
        config.addDefault("sculk-hearing.enabled", true);
        config.addDefault("hearing-range", 16.0);
        config.addDefault("volume-threshold", 0.1);

        config.options().copyDefaults(true);
        plugin.saveConfig();
    }

    public void reload() {
        loadConfig();
    }

    // Getter
    public boolean isMobHearingEnabled() {
        return mobHearingEnabled;
    }

    public boolean isHostileMobHearingEnabled() {
        return hostileMobHearingEnabled;
    }

    public boolean isWardenHearingEnabled() {
        return wardenHearingEnabled;
    }

    public boolean isSculkHearingEnabled() {
        return sculkHearingEnabled;
    }

    public double getHearingRange() {
        return hearingRange;
    }

    public float getVolumeThreshold() {
        return volumeThreshold;
    }

    // Setter
    public void setMobHearingEnabled(boolean enabled) {
        this.mobHearingEnabled = enabled;
        config.set("mob-hearing.enabled", enabled);
        plugin.saveConfig();
    }

    public void setSculkHearingEnabled(boolean enabled) {
        this.sculkHearingEnabled = enabled;
        config.set("sculk-hearing.enabled", enabled);
        plugin.saveConfig();
    }

    public double getMinVolumeForDetection() {
        return config.getDouble("detection.min-volume", 0.2);
    }
}