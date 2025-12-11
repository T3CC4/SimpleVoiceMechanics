package de.tecca.simplevoicemechanics.model;

/**
 * Immutable configuration for detection settings.
 * Ensures thread-safe access to detection parameters.
 */
public class DetectionConfig {

    private final double hearingRange;
    private final double volumeThreshold;
    private final double minVolume;

    public DetectionConfig(double hearingRange, double volumeThreshold, double minVolume) {
        this.hearingRange = hearingRange;
        this.volumeThreshold = volumeThreshold;
        this.minVolume = minVolume;
    }

    public double getHearingRange() {
        return hearingRange;
    }

    public double getVolumeThreshold() {
        return volumeThreshold;
    }

    public double getMinVolume() {
        return minVolume;
    }

    @Override
    public String toString() {
        return "DetectionConfig{" +
                "hearingRange=" + hearingRange +
                ", volumeThreshold=" + volumeThreshold +
                ", minVolume=" + minVolume +
                '}';
    }
}