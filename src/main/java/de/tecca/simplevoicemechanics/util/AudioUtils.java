package de.tecca.simplevoicemechanics.util;

/**
 * Utility class for calculating audio levels from PCM samples.
 *
 * <p>Based on SimpleVoiceChat's audio level calculation.
 * Calculates decibel (dB) values from decoded audio samples.
 *
 * @author henkelmax (SimpleVoiceChat)
 * @author Tecca (adapted)
 * @version 1.0.0
 */
public class AudioUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private AudioUtils() {
        throw new AssertionError("Utility class cannot be instantiated");
    }

    /**
     * Calculates the audio level of a signal with specific samples.
     *
     * <p>Formula:
     * <ol>
     *   <li>Calculate RMS (Root Mean Square) amplitude</li>
     *   <li>Convert to decibels: dB = 20 * log10(RMS)</li>
     *   <li>Clamp between -127 dB and 0 dB</li>
     * </ol>
     *
     * @param samples the decoded PCM samples (short array)
     * @return the audio level in decibels (-127 to 0 dB)
     */
    public static double calculateAudioLevel(short[] samples) {
        double rms = 0D; // root mean square (RMS) amplitude

        for (int i = 0; i < samples.length; i++) {
            double sample = (double) samples[i] / (double) Short.MAX_VALUE;
            rms += sample * sample;
        }

        int sampleCount = samples.length / 2;
        rms = (sampleCount == 0) ? 0 : Math.sqrt(rms / sampleCount);

        double db;
        if (rms > 0D) {
            db = Math.min(Math.max(20D * Math.log10(rms), -127D), 0D);
        } else {
            db = -127D;
        }

        return db;
    }

    /**
     * Converts decibel value to normalized volume (0.0 - 1.0).
     *
     * <p>Mapping:
     * <ul>
     *   <li>-127 dB (silence) → 0.0</li>
     *   <li>-60 dB (quiet) → 0.2</li>
     *   <li>-30 dB (normal) → 0.5</li>
     *   <li>-10 dB (loud) → 0.8</li>
     *   <li>0 dB (max) → 1.0</li>
     * </ul>
     *
     * @param db the decibel value (-127 to 0)
     * @return normalized volume (0.0 to 1.0)
     */
    public static double dbToNormalizedVolume(double db) {
        // Clamp input
        db = Math.max(-127D, Math.min(0D, db));

        // Linear mapping: -127 to 0 dB → 0.0 to 1.0
        return (db + 127D) / 127D;
    }

    /**
     * Checks if audio level exceeds a threshold in decibels.
     *
     * @param samples the decoded PCM samples
     * @param thresholdDb the threshold in decibels (e.g., -40.0)
     * @return true if audio level is above threshold
     */
    public static boolean isAboveThreshold(short[] samples, double thresholdDb) {
        return calculateAudioLevel(samples) > thresholdDb;
    }

    /**
     * Gets a human-readable audio level category.
     *
     * @param db the decibel value
     * @return category string (SILENCE, QUIET, NORMAL, LOUD, VERY_LOUD)
     */
    public static String getAudioCategory(double db) {
        if (db < -100) return "SILENCE";
        if (db < -60) return "QUIET";
        if (db < -30) return "NORMAL";
        if (db < -10) return "LOUD";
        return "VERY_LOUD";
    }

    /**
     * Gets debug information for audio level.
     *
     * @param samples the decoded PCM samples
     * @return formatted debug string
     */
    public static String getDebugInfo(short[] samples) {
        double db = calculateAudioLevel(samples);
        double normalized = dbToNormalizedVolume(db);
        String category = getAudioCategory(db);

        return String.format(
                "dB: %.2f | Normalized: %.2f | Category: %s",
                db, normalized, category
        );
    }
}