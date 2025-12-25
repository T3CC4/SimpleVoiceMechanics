package de.tecca.simplevoicemechanics.util;

import org.bukkit.block.Biome;

import java.util.EnumMap;
import java.util.Map;

/**
 * Utility class for biome-specific audio modifiers.
 *
 * <p>Different biomes affect sound propagation:
 * <ul>
 *   <li>Caves: Echo effect → increased range</li>
 *   <li>Water biomes: Dampened sound → decreased range</li>
 *   <li>Forests: Vegetation dampening → decreased range</li>
 *   <li>Open plains: Clear sound → normal range</li>
 * </ul>
 *
 * @author Tecca
 * @version 1.3.0
 */
public class BiomeModifier {

    private static final Map<BiomeCategory, Double> CATEGORY_MULTIPLIERS = new EnumMap<>(BiomeCategory.class);

    static {
        // Initialize default multipliers
        CATEGORY_MULTIPLIERS.put(BiomeCategory.CAVE, 1.5);      // Echo amplification
        CATEGORY_MULTIPLIERS.put(BiomeCategory.WATER, 0.6);     // Water dampening
        CATEGORY_MULTIPLIERS.put(BiomeCategory.FOREST, 0.75);   // Vegetation dampening
        CATEGORY_MULTIPLIERS.put(BiomeCategory.JUNGLE, 0.65);   // Dense vegetation
        CATEGORY_MULTIPLIERS.put(BiomeCategory.MOUNTAIN, 1.2);  // Sound carries in mountains
        CATEGORY_MULTIPLIERS.put(BiomeCategory.PLAINS, 1.0);    // Normal propagation
        CATEGORY_MULTIPLIERS.put(BiomeCategory.DESERT, 1.1);    // Clear dry air
        CATEGORY_MULTIPLIERS.put(BiomeCategory.NETHER, 1.3);    // Nether acoustics
        CATEGORY_MULTIPLIERS.put(BiomeCategory.END, 0.9);       // Void dampening
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private BiomeModifier() {
        throw new AssertionError("Utility class cannot be instantiated");
    }

    /**
     * Calculates the range multiplier for a biome.
     *
     * @param biome the biome
     * @return range multiplier (0.5 - 2.0)
     */
    public static double getRangeMultiplier(Biome biome) {
        BiomeCategory category = categorize(biome);
        return CATEGORY_MULTIPLIERS.getOrDefault(category, 1.0);
    }

    /**
     * Calculates the volume threshold adjustment for a biome.
     *
     * @param biome the biome
     * @return dB adjustment (negative = more sensitive)
     */
    public static double getThresholdAdjustment(Biome biome) {
        BiomeCategory category = categorize(biome);

        // Caves are more sensitive (hear quieter sounds)
        if (category == BiomeCategory.CAVE) {
            return -5.0;  // 5 dB more sensitive
        }

        // Dense biomes are less sensitive
        if (category == BiomeCategory.JUNGLE || category == BiomeCategory.FOREST) {
            return 3.0;  // 3 dB less sensitive
        }

        return 0.0;
    }

    /**
     * Categorizes a biome into sound categories.
     */
    private static BiomeCategory categorize(Biome biome) {
        String name = String.valueOf(biome.getKey()).toLowerCase();

        // Cave biomes
        if (name.contains("cave") || name.contains("dripstone") || name.contains("deep_dark")) {
            return BiomeCategory.CAVE;
        }

        // Water biomes
        if (name.contains("ocean") || name.contains("river") || name.contains("beach")) {
            return BiomeCategory.WATER;
        }

        // Forest biomes
        if (name.contains("forest") || name.contains("taiga")) {
            return BiomeCategory.FOREST;
        }

        // Jungle biomes
        if (name.contains("jungle") || name.contains("bamboo")) {
            return BiomeCategory.JUNGLE;
        }

        // Mountain biomes
        if (name.contains("mountain") || name.contains("peak") || name.contains("hill")) {
            return BiomeCategory.MOUNTAIN;
        }

        // Desert biomes
        if (name.contains("desert") || name.contains("badlands") || name.contains("mesa")) {
            return BiomeCategory.DESERT;
        }

        // Nether biomes
        if (name.contains("nether") || name.contains("basalt") || name.contains("crimson") || name.contains("warped")) {
            return BiomeCategory.NETHER;
        }

        // End biomes
        if (name.contains("end") || name.contains("the_void")) {
            return BiomeCategory.END;
        }

        // Default to plains
        return BiomeCategory.PLAINS;
    }

    /**
     * Gets debug information about a biome's modifiers.
     */
    public static String getDebugInfo(Biome biome) {
        BiomeCategory category = categorize(biome);
        double rangeMultiplier = getRangeMultiplier(biome);
        double thresholdAdjustment = getThresholdAdjustment(biome);

        return String.format(
                "Biome: %s | Category: %s | Range: %.1f%% | Threshold: %+.1f dB",
                biome.getKey(),
                category.name(),
                rangeMultiplier * 100,
                thresholdAdjustment
        );
    }

    /**
     * Biome sound categories.
     */
    private enum BiomeCategory {
        CAVE,
        WATER,
        FOREST,
        JUNGLE,
        MOUNTAIN,
        PLAINS,
        DESERT,
        NETHER,
        END
    }
}