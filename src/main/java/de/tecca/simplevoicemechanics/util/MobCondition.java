package de.tecca.simplevoicemechanics.util;

import org.bukkit.entity.*;

/**
 * Utility class for mob-specific behavioral conditions.
 *
 * <p>Provides checks for various mob states that should prevent
 * certain reactions (e.g., sitting pets shouldn't move, baby mobs
 * shouldn't react the same as adults, etc.).
 *
 * @author Tecca
 * @version 1.0.0
 */
public class MobCondition {

    /**
     * Private constructor to prevent instantiation.
     */
    private MobCondition() {
        throw new AssertionError("Utility class cannot be instantiated");
    }

    /**
     * Checks if a mob can follow a player.
     *
     * <p>Returns false if:
     * <ul>
     *   <li>Mob is a sitting tameable (cat, wolf, parrot)</li>
     *   <li>Mob is leashed</li>
     *   <li>Mob is in a vehicle</li>
     * </ul>
     *
     * @param mob the mob to check
     * @return true if mob can follow
     */
    public static boolean canFollow(Mob mob) {
        // Check if sitting
        if (isSitting(mob)) {
            return false;
        }

        // Check if leashed
        if (mob.isLeashed()) {
            return false;
        }

        // Check if in vehicle
        if (mob.isInsideVehicle()) {
            return false;
        }

        return true;
    }

    /**
     * Checks if a mob can look at a player.
     *
     * <p>Returns false if:
     * <ul>
     *   <li>Mob is sitting (but can still look)</li>
     *   <li>Mob is in a vehicle</li>
     * </ul>
     *
     * @param mob the mob to check
     * @return true if mob can look
     */
    public static boolean canLookAt(Mob mob) {
        // Sitting mobs can look, but not move
        // Only prevent looking if in vehicle
        return !mob.isInsideVehicle();
    }

    /**
     * Checks if a mob can flee from a player.
     *
     * <p>Returns false if:
     * <ul>
     *   <li>Mob is a sitting tameable</li>
     *   <li>Mob is leashed</li>
     *   <li>Mob is in a vehicle</li>
     *   <li>Mob is a baby (babies flee easier but adults protect)</li>
     * </ul>
     *
     * @param mob the mob to check
     * @return true if mob can flee
     */
    public static boolean canFlee(Mob mob) {
        // Check basic movement restrictions
        if (isSitting(mob) || mob.isLeashed() || mob.isInsideVehicle()) {
            return false;
        }

        return true;
    }

    /**
     * Checks if a mob is currently sitting.
     *
     * <p>Works for:
     * <ul>
     *   <li>Cats</li>
     *   <li>Wolves</li>
     *   <li>Parrots</li>
     *   <li>Foxes</li>
     * </ul>
     *
     * @param mob the mob to check
     * @return true if mob is sitting
     */
    public static boolean isSitting(Mob mob) {
        if (mob instanceof Cat) {
            return ((Cat) mob).isSitting();
        }
        if (mob instanceof Wolf) {
            return ((Wolf) mob).isSitting();
        }
        if (mob instanceof Parrot) {
            return ((Parrot) mob).isSitting();
        }
        if (mob instanceof Fox) {
            return ((Fox) mob).isSitting();
        }
        return false;
    }

    /**
     * Checks if a mob is tamed by a player.
     *
     * @param mob the mob to check
     * @return true if mob is tamed
     */
    public static boolean isTamed(Mob mob) {
        if (mob instanceof Tameable) {
            return ((Tameable) mob).isTamed();
        }
        return false;
    }

    /**
     * Checks if a mob is a baby.
     *
     * @param mob the mob to check
     * @return true if mob is a baby
     */
    public static boolean isBaby(Mob mob) {
        if (mob instanceof Ageable) {
            return !((Ageable) mob).isAdult();
        }
        if (mob instanceof Zombie) {
            return ((Zombie) mob).isBaby();
        }
        if (mob instanceof PigZombie) {
            return ((PigZombie) mob).isBaby();
        }
        return false;
    }

    /**
     * Checks if a mob should react with increased chance.
     *
     * <p>Baby mobs are more curious and react more often.
     *
     * @param mob the mob to check
     * @return multiplier for reaction chance (1.0 = normal, 1.5 = 50% more likely)
     */
    public static double getReactionMultiplier(Mob mob) {
        if (isBaby(mob)) {
            return 1.5; // Babies are more curious
        }
        return 1.0;
    }

    /**
     * Checks if a mob can attack a player.
     *
     * <p>Returns false if:
     * <ul>
     *   <li>Mob is tamed (wolves, cats)</li>
     *   <li>Mob is a baby (except baby zombies)</li>
     * </ul>
     *
     * @param mob the mob to check
     * @return true if mob can attack
     */
    public static boolean canAttack(Mob mob) {
        // Tamed mobs don't attack
        if (isTamed(mob)) {
            return false;
        }

        // Baby mobs don't attack (except baby zombies)
        if (isBaby(mob) && !(mob instanceof Zombie)) {
            return false;
        }

        return true;
    }

    /**
     * Checks if a mob requires special handling.
     *
     * <p>Some mobs have special conditions that require
     * different behavior logic.
     *
     * @param mob the mob to check
     * @return true if mob needs special handling
     */
    public static boolean needsSpecialHandling(Mob mob) {
        // Sitting tameables
        if (isSitting(mob)) {
            return true;
        }

        // Leashed mobs
        if (mob.isLeashed()) {
            return true;
        }

        // Mobs in vehicles
        if (mob.isInsideVehicle()) {
            return true;
        }

        return false;
    }

    /**
     * Gets debug information about a mob's condition.
     *
     * @param mob the mob to check
     * @return formatted debug string
     */
    public static String getDebugInfo(Mob mob) {
        StringBuilder info = new StringBuilder();
        info.append(mob.getType().name());

        if (isSitting(mob)) info.append(" [SITTING]");
        if (isTamed(mob)) info.append(" [TAMED]");
        if (isBaby(mob)) info.append(" [BABY]");
        if (mob.isLeashed()) info.append(" [LEASHED]");
        if (mob.isInsideVehicle()) info.append(" [VEHICLE]");

        info.append(" | Can: ");
        if (canFollow(mob)) info.append("Follow ");
        if (canLookAt(mob)) info.append("Look ");
        if (canFlee(mob)) info.append("Flee ");
        if (canAttack(mob)) info.append("Attack ");

        return info.toString();
    }
}