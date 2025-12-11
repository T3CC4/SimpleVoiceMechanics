package de.tecca.simplevoicemechanics.service;

/**
 * Service for audio volume calculations.
 * Handles RMS calculation and volume normalization.
 */
public class VolumeCalculationService {

    private static final double AUDIO_NORMALIZATION_FACTOR = 127.0;

    /**
     * Calculates volume from audio data using RMS (Root Mean Square).
     *
     * @param audioData Opus encoded audio data
     * @return Normalized volume (0.0 to 1.0)
     */
    public double calculateVolume(byte[] audioData) {
        if (audioData == null || audioData.length == 0) {
            return 0.0;
        }

        try {
            long sum = 0;
            for (byte b : audioData) {
                sum += b * b;
            }

            double rms = Math.sqrt((double) sum / audioData.length);
            return normalizeVolume(rms);
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Normalizes RMS value to 0-1 range.
     *
     * @param rms RMS value
     * @return Normalized volume
     */
    private double normalizeVolume(double rms) {
        double normalized = rms / AUDIO_NORMALIZATION_FACTOR;
        return Math.max(0.0, Math.min(1.0, normalized));
    }
}