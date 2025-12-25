package de.tecca.simplevoicemechanics.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;

/**
 * Utility class for calculating environmental modifiers (biome and weather).
 *
 * <p>Modifiers affect voice detection:
 * <ul>
 *   <li>Biomes: Caves amplify sound, forests dampen, water muffles</li>
 *   <li>Weather: Rain dampens, thunderstorms mask noise</li>
 * </ul>
 *
 * <p>Multipliers affect both range and dB threshold:
 * <ul>
 *   <li>1.0 = No change</li>
 *   <li>&gt;1.0 = Sound travels further (amplification)</li>
 *   <li>&lt;1.0 = Sound travels less far (dampening)</li>
 * </ul>
 *
 * @author Tecca
 * @version 1.0.0
 */
public class EnvironmentalModifier {

    /**
     * Private constructor to prevent instantiation.
     */
    private EnvironmentalModifier() {
        throw new AssertionError("Utility class cannot be instantiated");
    }

    /**
     * Calculates the total environmental modifier for a location.
     *
     * <p>Combines biome and weather modifiers:
     * <pre>
     * totalModifier = biomeModifier * weatherModifier
     * </pre>
     *
     * @param location the location to check
     * @param biomeEnabled whether biome modifiers are enabled
     * @param weatherEnabled whether weather modifiers are enabled
     * @return combined modifier (0.1 - 2.0)
     */
    public static double getEnvironmentalModifier(Location location, boolean biomeEnabled,
                                                  boolean weatherEnabled) {
        double modifier = 1.0;

        if (biomeEnabled) {
            modifier *= getBiomeModifier(location);
        }

        if (weatherEnabled) {
            modifier *= getWeatherModifier(location.getWorld());
        }

        return Math.max(0.1, Math.min(2.0, modifier));
    }

    /**
     * Gets the biome modifier for a location.
     *
     * <p>Biome categories:
     * <ul>
     *   <li>CAVE/UNDERGROUND: 1.3x (echo amplification)</li>
     *   <li>FOREST/JUNGLE: 0.7x (vegetation dampening)</li>
     *   <li>OCEAN/RIVER: 0.6x (water muffling)</li>
     *   <li>MOUNTAIN/HILLS: 1.2x (sound carries well)</li>
     *   <li>DESERT/PLAINS: 1.1x (open space)</li>
     *   <li>NETHER: 1.4x (echo in lava caves)</li>
     *   <li>END: 1.0x (void dampens)</li>
     *   <li>OTHER: 1.0x (normal)</li>
     * </ul>
     *
     * @param location the location to check
     * @return biome modifier (0.6 - 1.4)
     */
    public static double getBiomeModifier(Location location) {
        Biome biome = location.getBlock().getBiome();
        String biomeName = String.valueOf(biome.getKey());

        // CAVE/UNDERGROUND - Echo amplification
        if (isUnderground(location) || biomeName.contains("CAVE") ||
                biomeName.contains("DEEP_DARK")) {
            return 1.3;
        }

        // FOREST/JUNGLE - Vegetation dampening
        if (biomeName.contains("FOREST") || biomeName.contains("JUNGLE") ||
                biomeName.contains("TAIGA")) {
            return 0.7;
        }

        // OCEAN/RIVER - Water muffling
        if (biomeName.contains("OCEAN") || biomeName.contains("RIVER") ||
                biomeName.contains("BEACH") || isUnderwater(location)) {
            return 0.6;
        }

        // MOUNTAIN/HILLS - Sound carries well
        if (biomeName.contains("MOUNTAIN") || biomeName.contains("PEAK") ||
                biomeName.contains("HILL") || biomeName.contains("SLOPES")) {
            return 1.2;
        }

        // DESERT/PLAINS - Open space
        if (biomeName.contains("DESERT") || biomeName.contains("PLAIN") ||
                biomeName.contains("SAVANNA") || biomeName.contains("BADLANDS")) {
            return 1.1;
        }

        // NETHER - Echo in lava caves
        if (biomeName.contains("NETHER") || biomeName.contains("BASALT") ||
                biomeName.contains("WARPED") || biomeName.contains("CRIMSON") ||
                biomeName.contains("SOUL_SAND")) {
            return 1.4;
        }

        // END - Void dampens
        if (biomeName.contains("END")) {
            return 1.0;
        }

        // SWAMP - Moisture dampens
        if (biomeName.contains("SWAMP") || biomeName.contains("MANGROVE")) {
            return 0.8;
        }

        // Default
        return 1.0;
    }

    /**
     * Gets the weather modifier for a world.
     *
     * <p>Weather effects:
     * <ul>
     *   <li>CLEAR: 1.0x (no change)</li>
     *   <li>RAIN: 0.8x (rain noise dampens voice)</li>
     *   <li>THUNDERSTORM: 0.6x (thunder masks sound)</li>
     * </ul>
     *
     * @param world the world to check
     * @return weather modifier (0.6 - 1.0)
     */
    public static double getWeatherModifier(World world) {
        if (!world.hasStorm()) {
            return 1.0; // Clear weather
        }

        if (world.isThundering()) {
            return 0.6; // Thunderstorm masks sound significantly
        }

        return 0.8; // Rain dampens sound moderately
    }

    /**
     * Checks if a location is underground (cave detection).
     *
     * <p>Considers a location underground if:
     * <ul>
     *   <li>Y-level is below 50, OR</li>
     *   <li>Sky light level is 0 (covered by blocks)</li>
     * </ul>
     *
     * @param location the location to check
     * @return true if location is underground
     */
    public static boolean isUnderground(Location location) {
        // Below Y=50 is usually underground
        if (location.getY() < 50) {
            return true;
        }

        // Check if location has sky access
        byte skyLight = location.getBlock().getLightFromSky();
        return skyLight == 0;
    }

    /**
     * Checks if a location is underwater.
     *
     * @param location the location to check
     * @return true if location is in water
     */
    public static boolean isUnderwater(Location location) {
        return location.getBlock().isLiquid();
    }

    /**
     * Applies environmental modifier to a range value.
     *
     * @param range the base range
     * @param modifier the environmental modifier
     * @return modified range
     */
    public static double applyModifierToRange(double range, double modifier) {
        return range * modifier;
    }

    /**
     * Applies environmental modifier to a dB threshold.
     *
     * <p>When sound is amplified (modifier &gt; 1.0):
     * - Threshold becomes more lenient (decreased)
     * <p>When sound is dampened (modifier &lt; 1.0):
     * - Threshold becomes stricter (increased)
     *
     * @param thresholdDb the base threshold in dB
     * @param modifier the environmental modifier
     * @return modified threshold in dB
     */
    public static double applyModifierToThreshold(double thresholdDb, double modifier) {
        // Inverse relationship: higher modifier = lower threshold (easier to hear)
        // modifier 1.3 → threshold -3 dB (easier to detect)
        // modifier 0.7 → threshold +3 dB (harder to detect)
        double adjustment = (1.0 - modifier) * 10.0;
        return thresholdDb + adjustment;
    }

    /**
     * Gets a human-readable description of environmental conditions.
     *
     * @param location the location to check
     * @param biomeEnabled whether biome modifiers are enabled
     * @param weatherEnabled whether weather modifiers are enabled
     * @return formatted description
     */
    public static String getEnvironmentalDescription(Location location, boolean biomeEnabled,
                                                     boolean weatherEnabled) {
        StringBuilder desc = new StringBuilder("Environment: ");

        if (biomeEnabled) {
            double biomeModifier = getBiomeModifier(location);
            String biomeEffect = getModifierDescription(biomeModifier);
            desc.append(location.getBlock().getBiome().getKey())
                    .append(" (").append(biomeEffect).append(")");
        }

        if (weatherEnabled) {
            World world = location.getWorld();
            double weatherModifier = getWeatherModifier(world);
            String weatherEffect = getModifierDescription(weatherModifier);

            if (biomeEnabled) {
                desc.append(" + ");
            }

            desc.append(getWeatherName(world))
                    .append(" (").append(weatherEffect).append(")");
        }

        if (biomeEnabled || weatherEnabled) {
            double totalModifier = getEnvironmentalModifier(location, biomeEnabled, weatherEnabled);
            desc.append(" → Total: ").append(String.format("%.1fx", totalModifier));
        } else {
            desc.append("No modifiers active");
        }

        return desc.toString();
    }

    /**
     * Gets a human-readable description of a modifier value.
     */
    private static String getModifierDescription(double modifier) {
        if (modifier > 1.15) return "Amplified";
        if (modifier > 1.05) return "Slightly Amplified";
        if (modifier < 0.85) return "Dampened";
        if (modifier < 0.95) return "Slightly Dampened";
        return "Normal";
    }

    /**
     * Gets the current weather name for a world.
     */
    private static String getWeatherName(World world) {
        if (!world.hasStorm()) return "Clear";
        if (world.isThundering()) return "Thunderstorm";
        return "Rain";
    }

    /**
     * Gets debug information for environmental modifiers.
     *
     * @param location the location to check
     * @param biomeEnabled whether biome modifiers are enabled
     * @param weatherEnabled whether weather modifiers are enabled
     * @return formatted debug string
     */
    public static String getDebugInfo(Location location, boolean biomeEnabled, boolean weatherEnabled) {
        double biomeModifier = biomeEnabled ? getBiomeModifier(location) : 1.0;
        double weatherModifier = weatherEnabled ? getWeatherModifier(location.getWorld()) : 1.0;
        double totalModifier = biomeModifier * weatherModifier;

        return String.format(
                "Biome: %s (%.1fx) | Weather: %s (%.1fx) | Total: %.1fx | Underground: %s | Underwater: %s",
                location.getBlock().getBiome().getKey(),
                biomeModifier,
                getWeatherName(location.getWorld()),
                weatherModifier,
                totalModifier,
                isUnderground(location),
                isUnderwater(location)
        );
    }

    /**
     * Gets example modifiers for all biome categories.
     *
     * @return formatted example table
     */
    public static String getBiomeExampleTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("Biome Modifiers:\n");
        sb.append("  CAVE/UNDERGROUND: 1.3x (Echo amplification)\n");
        sb.append("  FOREST/JUNGLE: 0.7x (Vegetation dampening)\n");
        sb.append("  OCEAN/RIVER: 0.6x (Water muffling)\n");
        sb.append("  MOUNTAIN/HILLS: 1.2x (Sound carries well)\n");
        sb.append("  DESERT/PLAINS: 1.1x (Open space)\n");
        sb.append("  NETHER: 1.4x (Echo in lava caves)\n");
        sb.append("  SWAMP: 0.8x (Moisture dampens)\n");
        sb.append("  END: 1.0x (Void dampens)\n");
        sb.append("  OTHER: 1.0x (Normal)\n");
        sb.append("\n");
        sb.append("Weather Modifiers:\n");
        sb.append("  CLEAR: 1.0x (No change)\n");
        sb.append("  RAIN: 0.8x (Rain noise)\n");
        sb.append("  THUNDERSTORM: 0.6x (Thunder masks sound)\n");
        return sb.toString();
    }
}