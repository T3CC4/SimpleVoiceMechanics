package de.tecca.simplevoicemechanics.util;

/**
 * Utility class for range-based voice detection calculations.
 *
 * <p>Handles all detection probability calculations based on:
 * <ul>
 *   <li>Distance between player and entity</li>
 *   <li>Configured max-range and min-range</li>
 *   <li>Falloff curve (how detection chance decreases with distance)</li>
 * </ul>
 *
 * @author Tecca
 * @version 1.0.0
 */
public class RangeCalculator {

    /**
     * Private constructor to prevent instantiation.
     */
    private RangeCalculator() {
        throw new AssertionError("Utility class cannot be instantiated");
    }

    /**
     * Calculates detection chance based on distance and range settings.
     *
     * <p>Formula:
     * <ol>
     *   <li>If distance ≤ min-range: 100% chance</li>
     *   <li>If distance ≥ max-range: 0% chance</li>
     *   <li>Otherwise: Smooth falloff based on falloff-curve</li>
     * </ol>
     *
     * <p>Falloff curve explanation:
     * <ul>
     *   <li>0.5 = Linear falloff (50% at midpoint)</li>
     *   <li>1.0 = Moderate falloff (stays higher longer)</li>
     *   <li>1.5 = Strong falloff (barely drops until far out)</li>
     *   <li>2.0 = Very strong falloff (almost flat until edge)</li>
     * </ul>
     *
     * @param distance the distance in blocks
     * @param minRange the minimum range (100% detection)
     * @param maxRange the maximum range (0% detection)
     * @param falloffCurve the falloff curve exponent (0.5 - 2.0)
     * @return detection chance (0.0 - 1.0)
     */
    public static double calculateDetectionChance(double distance, double minRange,
                                                  double maxRange, double falloffCurve) {
        // Within min range: always detect
        if (distance <= minRange) {
            return 1.0;
        }

        // Beyond max range: never detect
        if (distance >= maxRange) {
            return 0.0;
        }

        // Calculate normalized distance (0.0 at min-range, 1.0 at max-range)
        double normalizedDistance = (distance - minRange) / (maxRange - minRange);

        // Apply falloff curve (inverted so 1.0 = 100% at min-range, 0.0 = 0% at max-range)
        // Higher falloff curve = detection chance stays high longer
        double chance = Math.pow(1.0 - normalizedDistance, falloffCurve);

        return Math.max(0.0, Math.min(1.0, chance));
    }

    /**
     * Calculates Warden anger based on distance.
     *
     * <p>Anger scales inversely with distance:
     * <ul>
     *   <li>Close (0-3 blocks): 50-60 anger</li>
     *   <li>Medium (3-12 blocks): 30-50 anger</li>
     *   <li>Far (12-24 blocks): 15-30 anger</li>
     * </ul>
     *
     * @param distance the distance in blocks
     * @param minRange the minimum range
     * @param maxRange the maximum range
     * @return anger increase amount
     */
    public static int calculateWardenAnger(double distance, double minRange, double maxRange) {
        // Base anger values
        int minAnger = 15;
        int maxAnger = 60;

        // Within min range: maximum anger
        if (distance <= minRange) {
            return maxAnger;
        }

        // Beyond max range: minimum anger
        if (distance >= maxRange) {
            return minAnger;
        }

        // Calculate anger based on distance (closer = more angry)
        double normalizedDistance = (distance - minRange) / (maxRange - minRange);
        double angerFactor = 1.0 - normalizedDistance;

        // Non-linear scaling (closer distances = disproportionately more anger)
        angerFactor = Math.pow(angerFactor, 1.5);

        int anger = (int) (minAnger + (maxAnger - minAnger) * angerFactor);
        return Math.max(minAnger, Math.min(maxAnger, anger));
    }

    /**
     * Gets debug information for range calculations.
     *
     * @param distance the distance
     * @param minRange the min range
     * @param maxRange the max range
     * @param falloffCurve the falloff curve
     * @return formatted debug string
     */
    public static String getDebugInfo(double distance, double minRange, double maxRange, double falloffCurve) {
        double chance = calculateDetectionChance(distance, minRange, maxRange, falloffCurve);

        String rangeZone;
        if (distance <= minRange) {
            rangeZone = "GUARANTEED";
        } else if (distance >= maxRange) {
            rangeZone = "OUT_OF_RANGE";
        } else {
            double percent = ((distance - minRange) / (maxRange - minRange)) * 100;
            rangeZone = String.format("%.0f%%", percent);
        }

        return String.format(
                "Dist: %.1f | Zone: %s | Chance: %.1f%% | Curve: %.1f",
                distance, rangeZone, chance * 100, falloffCurve
        );
    }

    /**
     * Gets example detection chances at various distances.
     *
     * @param minRange the min range
     * @param maxRange the max range
     * @param falloffCurve the falloff curve
     * @return formatted example table
     */
    public static String getExampleTable(double minRange, double maxRange, double falloffCurve) {
        StringBuilder sb = new StringBuilder();
        sb.append("Detection Chances:\n");

        double[] testDistances = {
                minRange * 0.5,
                minRange,
                minRange + (maxRange - minRange) * 0.25,
                minRange + (maxRange - minRange) * 0.5,
                minRange + (maxRange - minRange) * 0.75,
                maxRange,
                maxRange * 1.5
        };

        for (double dist : testDistances) {
            double chance = calculateDetectionChance(dist, minRange, maxRange, falloffCurve);
            sb.append(String.format("  %.1f blocks: %.1f%%\n", dist, chance * 100));
        }

        return sb.toString();
    }
}