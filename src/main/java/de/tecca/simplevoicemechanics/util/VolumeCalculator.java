package de.tecca.simplevoicemechanics.util;

/**
 * Utility class for calculating and processing voice volumes.
 *
 * <p>Handles all volume-related calculations including:
 * <ul>
 *   <li>Raw audio data analysis</li>
 *   <li>Volume normalization</li>
 *   <li>Distance-based attenuation</li>
 *   <li>Detection probability calculations</li>
 * </ul>
 *
 * @author Tecca
 * @version 1.0.0
 * @since 1.0.0
 */
public class VolumeCalculator {

    // ==================== AUDIO ANALYSIS CONSTANTS ====================

    /** Center value for audio data (represents silence) */
    private static final int AUDIO_CENTER = 128;

    /** Maximum centered value (128 for 8-bit audio) */
    private static final double MAX_CENTERED_VALUE = 128.0;

    /** RMS threshold for "silence" - below this is considered no voice */
    private static final double SILENCE_THRESHOLD = 10.0;

    /** RMS value that represents "normal talking" volume */
    private static final double NORMAL_TALKING_RMS = 50.0;

    /** RMS value that represents "loud talking/shouting" volume */
    private static final double LOUD_TALKING_RMS = 90.0;

    /** Minimum volume output (even for very quiet sounds) */
    private static final double MIN_VOLUME = 0.05;

    /** Maximum volume output */
    private static final double MAX_VOLUME = 1.0;

    // ==================== DISTANCE ATTENUATION CONSTANTS ====================

    /** Reference distance for inverse square law (blocks) */
    private static final double REFERENCE_DISTANCE = 4.0;

    /** Attenuation factor for mobs */
    private static final double MOB_ATTENUATION = 0.5;

    /** Attenuation factor for sculk sensors */
    private static final double SCULK_ATTENUATION = 0.6;

    /** Sensitivity boost for sculk sensors */
    private static final double SCULK_SENSITIVITY = 1.2;

    /** Minimum distance to avoid division issues */
    private static final double MIN_DISTANCE = 0.5;

    // ==================== DETECTION CONSTANTS ====================

    /** Base detection chance for mobs */
    private static final double MOB_BASE_CHANCE = 0.5;

    /** Base detection chance for sculk sensors */
    private static final double SCULK_BASE_CHANCE = 0.7;

    /** Volume chance exponent for mobs */
    private static final double MOB_VOLUME_EXPONENT = 0.7;

    /** Volume chance exponent for sculk sensors */
    private static final double SCULK_VOLUME_EXPONENT = 0.5;

    // ==================== WARDEN CONSTANTS ====================

    /** Base anger increase for Warden */
    private static final int WARDEN_BASE_ANGER = 15;

    /** Maximum anger increase for Warden */
    private static final int WARDEN_MAX_ANGER = 60;

    /** Scaling exponent for Warden anger */
    private static final double WARDEN_ANGER_EXPONENT = 1.5;

    /**
     * Private constructor to prevent instantiation.
     */
    private VolumeCalculator() {
        throw new AssertionError("Utility class cannot be instantiated");
    }

    // ==================== PUBLIC API ====================

    /**
     * Calculates normalized volume from raw Opus encoded audio data.
     *
     * <p>New approach using segmented linear scaling:
     * <ol>
     *   <li>Calculate RMS energy from audio bytes</li>
     *   <li>Map RMS to volume using reference points:
     *       <ul>
     *         <li>RMS 0-10: Silence (0.0-0.05)</li>
     *         <li>RMS 10-50: Whisper to Normal (0.05-0.5)</li>
     *         <li>RMS 50-90: Normal to Loud (0.5-0.9)</li>
     *         <li>RMS 90+: Very Loud (0.9-1.0)</li>
     *       </ul>
     *   </li>
     * </ol>
     *
     * @param audioData the opus encoded audio data
     * @return normalized volume (0.0 - 1.0)
     */
    public static float calculateRawVolume(byte[] audioData) {
        if (audioData == null || audioData.length == 0) {
            return 0.0f;
        }

        // Calculate RMS energy
        double rms = calculateRMS(audioData);

        // Map RMS to volume using segmented linear scaling
        double volume = mapRMSToVolume(rms);

        return (float) Math.min(MAX_VOLUME, Math.max(MIN_VOLUME, volume));
    }

    /**
     * Calculates effective volume at a given distance for mobs.
     *
     * <p>Uses realistic sound propagation:
     * <ul>
     *   <li>Inverse square law for near field</li>
     *   <li>Exponential decay for far field</li>
     * </ul>
     *
     * @param rawVolume the original volume (0.0 - 1.0)
     * @param distance the distance in blocks
     * @return effective volume at distance (0.0 - 1.0)
     */
    public static double calculateMobEffectiveVolume(float rawVolume, double distance) {
        if (distance < MIN_DISTANCE) {
            return rawVolume;
        }

        double attenuation = calculateAttenuation(distance, MOB_ATTENUATION);
        return rawVolume * attenuation;
    }

    /**
     * Calculates effective volume at a given distance for sculk sensors.
     *
     * <p>Sculk sensors are more sensitive than mobs.
     *
     * @param rawVolume the original volume (0.0 - 1.0)
     * @param distance the distance in blocks
     * @return effective volume at distance (0.0 - 1.0)
     */
    public static double calculateSculkEffectiveVolume(float rawVolume, double distance) {
        if (distance < MIN_DISTANCE) {
            return rawVolume * SCULK_SENSITIVITY;
        }

        double attenuation = calculateAttenuation(distance, SCULK_ATTENUATION);
        return rawVolume * attenuation * SCULK_SENSITIVITY;
    }

    /**
     * Calculates detection probability for mobs.
     *
     * @param effectiveVolume the effective volume after distance calculation
     * @param distance the distance in blocks
     * @param maxRange the maximum detection range
     * @return probability of detection (0.0 - 1.0)
     */
    public static double calculateMobDetectionChance(double effectiveVolume, double distance, double maxRange) {
        return calculateDetectionChance(
                effectiveVolume,
                distance,
                maxRange,
                MOB_BASE_CHANCE,
                MOB_VOLUME_EXPONENT
        );
    }

    /**
     * Calculates detection probability for sculk sensors.
     *
     * @param effectiveVolume the effective volume after distance calculation
     * @param distance the distance in blocks
     * @param maxRange the maximum detection range
     * @return probability of detection (0.0 - 1.0)
     */
    public static double calculateSculkDetectionChance(double effectiveVolume, double distance, double maxRange) {
        return calculateDetectionChance(
                effectiveVolume,
                distance,
                maxRange,
                SCULK_BASE_CHANCE,
                SCULK_VOLUME_EXPONENT
        );
    }

    /**
     * Calculates Warden anger increase based on effective volume.
     *
     * <p>Anger scales non-linearly with volume:
     * <ul>
     *   <li>Quiet (0.2): 15-20 anger</li>
     *   <li>Normal (0.5): 30-40 anger</li>
     *   <li>Loud (0.8): 50-60 anger</li>
     * </ul>
     *
     * @param effectiveVolume the effective volume (0.0 - 1.0)
     * @return anger increase amount
     */
    public static int calculateWardenAnger(double effectiveVolume) {
        // Non-linear scaling (louder = disproportionately more anger)
        double volumeScaling = Math.pow(effectiveVolume, WARDEN_ANGER_EXPONENT);

        // Calculate anger
        int anger = (int) (WARDEN_BASE_ANGER +
                (WARDEN_MAX_ANGER - WARDEN_BASE_ANGER) * volumeScaling);

        // Ensure minimum
        return Math.max(WARDEN_BASE_ANGER, anger);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Calculates RMS (Root Mean Square) from audio data.
     *
     * @param audioData the audio data
     * @return RMS value
     */
    private static double calculateRMS(byte[] audioData) {
        long sumOfSquares = 0;

        for (byte b : audioData) {
            // Convert to unsigned (0-255)
            int value = b & 0xFF;

            // Center around silence (128 = silence)
            int centered = value - AUDIO_CENTER;

            sumOfSquares += (long) centered * centered;
        }

        return Math.sqrt((double) sumOfSquares / audioData.length);
    }

    /**
     * Maps RMS value to volume using segmented linear scaling.
     *
     * <p>This creates clear distinctions between volume levels:
     * <ul>
     *   <li>Silence: RMS 0-10 → Volume 0.0-0.05</li>
     *   <li>Whisper: RMS 10-30 → Volume 0.05-0.25</li>
     *   <li>Quiet: RMS 30-50 → Volume 0.25-0.5</li>
     *   <li>Normal: RMS 50-70 → Volume 0.5-0.7</li>
     *   <li>Loud: RMS 70-90 → Volume 0.7-0.9</li>
     *   <li>Very Loud: RMS 90+ → Volume 0.9-1.0</li>
     * </ul>
     *
     * @param rms the RMS value
     * @return volume (0.0 - 1.0)
     */
    private static double mapRMSToVolume(double rms) {
        // Silence threshold
        if (rms < SILENCE_THRESHOLD) {
            return MIN_VOLUME * (rms / SILENCE_THRESHOLD);
        }

        // Whisper to quiet (10-30 RMS → 0.05-0.25 volume)
        if (rms < 30.0) {
            return lerp(MIN_VOLUME, 0.25, (rms - SILENCE_THRESHOLD) / (30.0 - SILENCE_THRESHOLD));
        }

        // Quiet to normal (30-50 RMS → 0.25-0.5 volume)
        if (rms < NORMAL_TALKING_RMS) {
            return lerp(0.25, 0.5, (rms - 30.0) / (NORMAL_TALKING_RMS - 30.0));
        }

        // Normal to loud (50-70 RMS → 0.5-0.7 volume)
        if (rms < 70.0) {
            return lerp(0.5, 0.7, (rms - NORMAL_TALKING_RMS) / (70.0 - NORMAL_TALKING_RMS));
        }

        // Loud to very loud (70-90 RMS → 0.7-0.9 volume)
        if (rms < LOUD_TALKING_RMS) {
            return lerp(0.7, 0.9, (rms - 70.0) / (LOUD_TALKING_RMS - 70.0));
        }

        // Very loud (90+ RMS → 0.9-1.0 volume)
        double excess = Math.min((rms - LOUD_TALKING_RMS) / 20.0, 1.0);
        return lerp(0.9, MAX_VOLUME, excess);
    }

    /**
     * Linear interpolation between two values.
     *
     * @param a start value
     * @param b end value
     * @param t interpolation factor (0.0 - 1.0)
     * @return interpolated value
     */
    private static double lerp(double a, double b, double t) {
        return a + (b - a) * Math.max(0.0, Math.min(1.0, t));
    }

    /**
     * Calculates sound attenuation based on distance.
     *
     * <p>Combines:
     * <ul>
     *   <li>Inverse square law: intensity ∝ 1/distance²</li>
     *   <li>Exponential decay: air absorption</li>
     * </ul>
     *
     * @param distance the distance in blocks
     * @param attenuationFactor the attenuation factor
     * @return attenuation multiplier (0.0 - 1.0)
     */
    private static double calculateAttenuation(double distance, double attenuationFactor) {
        // Inverse square law for near field
        double inverseSquare = REFERENCE_DISTANCE / (distance + REFERENCE_DISTANCE);

        // Exponential decay for far field
        double exponentialDecay = Math.exp(-attenuationFactor * distance / REFERENCE_DISTANCE);

        return inverseSquare * exponentialDecay;
    }

    /**
     * Generic detection chance calculation.
     *
     * @param effectiveVolume the effective volume
     * @param distance the distance
     * @param maxRange the maximum range
     * @param baseChance the base detection chance
     * @param volumeExponent the volume scaling exponent
     * @return detection probability (0.0 - 1.0)
     */
    private static double calculateDetectionChance(double effectiveVolume, double distance,
                                                   double maxRange, double baseChance,
                                                   double volumeExponent) {
        // Volume-based chance
        double volumeChance = Math.pow(effectiveVolume, volumeExponent);

        // Distance factor (closer = better)
        double distanceFactor = 1.0 - (distance / maxRange);

        // Combine
        double combined = baseChance + (1.0 - baseChance) * volumeChance * distanceFactor;

        return Math.min(1.0, combined);
    }

    // ==================== DEBUG / TESTING METHODS ====================

    /**
     * Gets detailed volume analysis for debugging.
     *
     * @param audioData the audio data
     * @return formatted debug string
     */
    public static String getVolumeDebugInfo(byte[] audioData) {
        if (audioData == null || audioData.length == 0) {
            return "No audio data";
        }

        double rms = calculateRMS(audioData);
        float volume = calculateRawVolume(audioData);

        String category = getVolumeCategory(rms);

        return String.format(
                "RMS: %.3f | Volume: %.3f | Category: %s",
                rms, volume, category
        );
    }

    /**
     * Gets volume category name for debugging.
     *
     * @param rms the RMS value
     * @return category name
     */
    private static String getVolumeCategory(double rms) {
        if (rms < SILENCE_THRESHOLD) return "SILENCE";
        if (rms < 30.0) return "WHISPER";
        if (rms < NORMAL_TALKING_RMS) return "QUIET";
        if (rms < 70.0) return "NORMAL";
        if (rms < LOUD_TALKING_RMS) return "LOUD";
        return "VERY_LOUD";
    }

    /**
     * Gets detailed attenuation analysis for debugging.
     *
     * @param rawVolume the raw volume
     * @param distance the distance
     * @return formatted debug string
     */
    public static String getAttenuationDebugInfo(float rawVolume, double distance) {
        double mobEffective = calculateMobEffectiveVolume(rawVolume, distance);
        double sculkEffective = calculateSculkEffectiveVolume(rawVolume, distance);

        return String.format(
                "Raw: %.3f | Distance: %.1f | Mob: %.3f | Sculk: %.3f",
                rawVolume, distance, mobEffective, sculkEffective
        );
    }
}