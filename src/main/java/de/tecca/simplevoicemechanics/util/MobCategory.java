package de.tecca.simplevoicemechanics.util;

import org.bukkit.entity.EntityType;

import java.util.EnumSet;
import java.util.Set;

/**
 * Utility class for categorizing mobs into behavioral groups.
 *
 * <p>Categories:
 * <ul>
 *   <li>HOSTILE: Mobs that attack players on sight</li>
 *   <li>NEUTRAL: Mobs that are peaceful until provoked</li>
 *   <li>PEACEFUL: Mobs that never attack</li>
 * </ul>
 *
 * <p>This class uses EnumSet for efficient lookups and handles
 * version differences by safely checking EntityType existence.
 *
 * @author Tecca
 * @version 1.0.0
 */
public class MobCategory {

    private static final Set<EntityType> HOSTILE_MOBS = EnumSet.noneOf(EntityType.class);
    private static final Set<EntityType> NEUTRAL_MOBS = EnumSet.noneOf(EntityType.class);
    private static final Set<EntityType> PEACEFUL_MOBS = EnumSet.noneOf(EntityType.class);

    static {
        initializeHostileMobs();
        initializeNeutralMobs();
        initializePeacefulMobs();
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private MobCategory() {
        throw new AssertionError("Utility class cannot be instantiated");
    }

    /**
     * Initializes hostile mob types.
     */
    private static void initializeHostileMobs() {
        // Undead
        addIfExists(HOSTILE_MOBS, "ZOMBIE");
        addIfExists(HOSTILE_MOBS, "HUSK");
        addIfExists(HOSTILE_MOBS, "DROWNED");
        addIfExists(HOSTILE_MOBS, "SKELETON");
        addIfExists(HOSTILE_MOBS, "STRAY");
        addIfExists(HOSTILE_MOBS, "WITHER_SKELETON");
        addIfExists(HOSTILE_MOBS, "ZOMBIE_VILLAGER");
        addIfExists(HOSTILE_MOBS, "PHANTOM");

        // Arthropods
        addIfExists(HOSTILE_MOBS, "SPIDER");
        addIfExists(HOSTILE_MOBS, "CAVE_SPIDER");
        addIfExists(HOSTILE_MOBS, "SILVERFISH");
        addIfExists(HOSTILE_MOBS, "ENDERMITE");

        // Illagers
        addIfExists(HOSTILE_MOBS, "PILLAGER");
        addIfExists(HOSTILE_MOBS, "VINDICATOR");
        addIfExists(HOSTILE_MOBS, "EVOKER");
        addIfExists(HOSTILE_MOBS, "RAVAGER");
        addIfExists(HOSTILE_MOBS, "VEX");
        addIfExists(HOSTILE_MOBS, "ILLUSIONER");

        // Nether
        addIfExists(HOSTILE_MOBS, "BLAZE");
        addIfExists(HOSTILE_MOBS, "GHAST");
        addIfExists(HOSTILE_MOBS, "MAGMA_CUBE");
        addIfExists(HOSTILE_MOBS, "WITHER");
        addIfExists(HOSTILE_MOBS, "HOGLIN");
        addIfExists(HOSTILE_MOBS, "ZOGLIN");
        addIfExists(HOSTILE_MOBS, "PIGLIN_BRUTE");

        // Ocean
        addIfExists(HOSTILE_MOBS, "GUARDIAN");
        addIfExists(HOSTILE_MOBS, "ELDER_GUARDIAN");

        // Other
        addIfExists(HOSTILE_MOBS, "CREEPER");
        addIfExists(HOSTILE_MOBS, "WITCH");
        addIfExists(HOSTILE_MOBS, "SLIME");
        addIfExists(HOSTILE_MOBS, "SHULKER");
        addIfExists(HOSTILE_MOBS, "BREEZE"); // 1.21+
        addIfExists(HOSTILE_MOBS, "BOGGED"); // 1.21+
    }

    /**
     * Initializes neutral mob types.
     */
    private static void initializeNeutralMobs() {
        // Naturally neutral
        addIfExists(NEUTRAL_MOBS, "ENDERMAN");
        addIfExists(NEUTRAL_MOBS, "ZOMBIFIED_PIGLIN");
        addIfExists(NEUTRAL_MOBS, "PIGLIN");
        addIfExists(NEUTRAL_MOBS, "WOLF");
        addIfExists(NEUTRAL_MOBS, "POLAR_BEAR");
        addIfExists(NEUTRAL_MOBS, "LLAMA");
        addIfExists(NEUTRAL_MOBS, "TRADER_LLAMA");
        addIfExists(NEUTRAL_MOBS, "PANDA");
        addIfExists(NEUTRAL_MOBS, "BEE");
        addIfExists(NEUTRAL_MOBS, "IRON_GOLEM");
        addIfExists(NEUTRAL_MOBS, "DOLPHIN");
        addIfExists(NEUTRAL_MOBS, "GOAT");
    }

    /**
     * Initializes peaceful mob types.
     */
    private static void initializePeacefulMobs() {
        // Farm animals
        addIfExists(PEACEFUL_MOBS, "COW");
        addIfExists(PEACEFUL_MOBS, "MOOSHROOM");
        addIfExists(PEACEFUL_MOBS, "SHEEP");
        addIfExists(PEACEFUL_MOBS, "PIG");
        addIfExists(PEACEFUL_MOBS, "CHICKEN");
        addIfExists(PEACEFUL_MOBS, "RABBIT");

        // Horses
        addIfExists(PEACEFUL_MOBS, "HORSE");
        addIfExists(PEACEFUL_MOBS, "DONKEY");
        addIfExists(PEACEFUL_MOBS, "MULE");
        addIfExists(PEACEFUL_MOBS, "SKELETON_HORSE");
        addIfExists(PEACEFUL_MOBS, "ZOMBIE_HORSE");

        // Pets
        addIfExists(PEACEFUL_MOBS, "CAT");
        addIfExists(PEACEFUL_MOBS, "OCELOT");
        addIfExists(PEACEFUL_MOBS, "PARROT");

        // Aquatic
        addIfExists(PEACEFUL_MOBS, "SQUID");
        addIfExists(PEACEFUL_MOBS, "GLOW_SQUID");
        addIfExists(PEACEFUL_MOBS, "COD");
        addIfExists(PEACEFUL_MOBS, "SALMON");
        addIfExists(PEACEFUL_MOBS, "TROPICAL_FISH");
        addIfExists(PEACEFUL_MOBS, "PUFFERFISH");
        addIfExists(PEACEFUL_MOBS, "TURTLE");
        addIfExists(PEACEFUL_MOBS, "AXOLOTL");
        addIfExists(PEACEFUL_MOBS, "TADPOLE");

        // Other
        addIfExists(PEACEFUL_MOBS, "BAT");
        addIfExists(PEACEFUL_MOBS, "FOX");
        addIfExists(PEACEFUL_MOBS, "FROG");
        addIfExists(PEACEFUL_MOBS, "ALLAY");
        addIfExists(PEACEFUL_MOBS, "VILLAGER");
        addIfExists(PEACEFUL_MOBS, "WANDERING_TRADER");
        addIfExists(PEACEFUL_MOBS, "STRIDER");
        addIfExists(PEACEFUL_MOBS, "SNOW_GOLEM");
        addIfExists(PEACEFUL_MOBS, "CAMEL"); // 1.20+
        addIfExists(PEACEFUL_MOBS, "SNIFFER"); // 1.20+
        addIfExists(PEACEFUL_MOBS, "ARMADILLO"); // 1.21+
    }

    /**
     * Safely adds an EntityType to a set if it exists in this version.
     *
     * @param set the set to add to
     * @param typeName the EntityType name
     */
    private static void addIfExists(Set<EntityType> set, String typeName) {
        try {
            EntityType type = EntityType.valueOf(typeName);
            set.add(type);
        } catch (IllegalArgumentException e) {
            // EntityType doesn't exist in this version - ignore
        }
    }

    /**
     * Checks if an EntityType is a hostile mob.
     *
     * @param type the entity type
     * @return true if the mob is hostile
     */
    public static boolean isHostile(EntityType type) {
        return HOSTILE_MOBS.contains(type);
    }

    /**
     * Checks if an EntityType is a neutral mob.
     *
     * @param type the entity type
     * @return true if the mob is neutral
     */
    public static boolean isNeutral(EntityType type) {
        return NEUTRAL_MOBS.contains(type);
    }

    /**
     * Checks if an EntityType is a peaceful mob.
     *
     * @param type the entity type
     * @return true if the mob is peaceful
     */
    public static boolean isPeaceful(EntityType type) {
        return PEACEFUL_MOBS.contains(type);
    }
}