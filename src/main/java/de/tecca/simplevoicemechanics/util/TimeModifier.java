package de.tecca.simplevoicemechanics.util;

import org.bukkit.World;

/**
 * Utility class for time-of-day sound modifiers.
 *
 * <p>Sound detection varies with time:
 * <ul>
 *   <li>Night: Mobs more alert → lower threshold</li>
 *   <li>Day: Normal detection</li>
 *   <li>Dusk/Dawn: Transition periods</li>
 * </ul>
 *
 * @author Tecca
 * @version 1.3.0
 */
public class TimeModifier {

    // Minecraft time constants
    private static final long DAY_START = 0;
    private static final long DUSK_START = 12000;
    private static final long NIGHT_START = 13000;
    private static final long DAWN_START = 23000;
    private static final long DAY_END = 24000;

    /**
     * Private constructor to prevent instantiation.
     */
    private TimeModifier() {
        throw new AssertionError("Utility class cannot be instantiated");
    }

    /**
     * Gets the threshold adjustment based on time of day.
     *
     * @param world the world to check
     * @return dB adjustment (negative = more sensitive)
     */
    public static double getThresholdAdjustment(World world) {
        long time = world.getTime();
        TimeOfDay timeOfDay = getTimeOfDay(time);

        switch (timeOfDay) {
            case NIGHT:
                return -8.0;  // Night: 8 dB more sensitive
            case DUSK:
            case DAWN:
                return -4.0;  // Twilight: 4 dB more sensitive
            case DAY:
            default:
                return 0.0;   // Day: Normal
        }
    }

    /**
     * Gets the range multiplier based on time of day.
     *
     * @param world the world to check
     * @return range multiplier (0.8 - 1.3)
     */
    public static double getRangeMultiplier(World world) {
        long time = world.getTime();
        TimeOfDay timeOfDay = getTimeOfDay(time);

        switch (timeOfDay) {
            case NIGHT:
                return 1.3;   // Night: 30% increased range
            case DUSK:
            case DAWN:
                return 1.15;  // Twilight: 15% increased range
            case DAY:
            default:
                return 1.0;   // Day: Normal range
        }
    }

    /**
     * Determines the time of day category.
     */
    public static TimeOfDay getTimeOfDay(long time) {
        // Normalize time to 0-24000 range
        time = time % DAY_END;

        if (time >= DAY_START && time < DUSK_START) {
            return TimeOfDay.DAY;
        } else if (time >= DUSK_START && time < NIGHT_START) {
            return TimeOfDay.DUSK;
        } else if (time >= NIGHT_START && time < DAWN_START) {
            return TimeOfDay.NIGHT;
        } else {
            return TimeOfDay.DAWN;
        }
    }

    /**
     * Checks if it's currently night time.
     */
    public static boolean isNight(World world) {
        return getTimeOfDay(world.getTime()) == TimeOfDay.NIGHT;
    }

    /**
     * Gets debug information about time modifiers.
     */
    public static String getDebugInfo(World world) {
        long time = world.getTime();
        TimeOfDay timeOfDay = getTimeOfDay(time);
        double threshold = getThresholdAdjustment(world);
        double range = getRangeMultiplier(world);

        return String.format(
                "Time: %s (%d) | Threshold: %+.1f dB | Range: %.1f%%",
                timeOfDay.name(),
                time,
                threshold,
                range * 100
        );
    }

    /**
     * Time of day categories.
     */
    public enum TimeOfDay {
        DAY,
        DUSK,
        NIGHT,
        DAWN
    }
}