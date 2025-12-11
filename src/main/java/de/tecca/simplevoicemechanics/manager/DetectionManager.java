package de.tecca.simplevoicemechanics.manager;

import de.tecca.simplevoicemechanics.model.DetectionConfig;

/**
 * Manages detection-specific settings and calculations.
 * Handles volume thresholds and range configurations.
 */
public class DetectionManager {

    private DetectionConfig config;

    public DetectionManager(DetectionConfig config) {
        this.config = config;
    }

    /**
     * Updates the detection configuration.
     *
     * @param config New configuration
     */
    public void updateConfig(DetectionConfig config) {
        this.config = config;
    }

    /**
     * Gets current detection configuration.
     *
     * @return Current config
     */
    public DetectionConfig getConfig() {
        return config;
    }

    /**
     * Checks if volume exceeds the processing threshold.
     *
     * @param volume Volume to check
     * @return true if volume should be processed
     */
    public boolean shouldProcessVolume(double volume) {
        return volume >= config.getVolumeThreshold();
    }

    /**
     * Checks if volume exceeds detection threshold.
     *
     * @param volume Volume to check
     * @return true if entities should detect
     */
    public boolean canDetect(double volume) {
        return volume >= config.getMinVolume();
    }

    /**
     * Calculates effective volume based on distance with linear falloff.
     *
     * @param baseVolume Original volume
     * @param distance Distance from source
     * @return Effective volume at distance
     */
    public double calculateEffectiveVolume(double baseVolume, double distance) {
        if (distance >= config.getHearingRange()) {
            return 0.0;
        }
        return baseVolume * (1.0 - (distance / config.getHearingRange()));
    }

    /**
     * Checks if a distance is within hearing range.
     *
     * @param distance Distance to check
     * @return true if within range
     */
    public boolean isWithinRange(double distance) {
        return distance <= config.getHearingRange();
    }
}