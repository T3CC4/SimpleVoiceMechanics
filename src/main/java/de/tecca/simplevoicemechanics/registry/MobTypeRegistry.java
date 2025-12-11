package de.tecca.simplevoicemechanics.registry;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;

import java.util.*;

/**
 * Registry for mob types that can hear player voices.
 * Allows easy extension for new mob types without code changes.
 */
public class MobTypeRegistry {

    private final Set<EntityType> hostileMobs = new HashSet<>();
    private final Set<EntityType> specialMobs = new HashSet<>();

    public MobTypeRegistry() {
        registerDefaultMobs();
    }

    /**
     * Registers default hostile mobs.
     */
    private void registerDefaultMobs() {
        // Hostile mobs
        hostileMobs.add(EntityType.ZOMBIE);
        hostileMobs.add(EntityType.SKELETON);
        hostileMobs.add(EntityType.CREEPER);
        hostileMobs.add(EntityType.SPIDER);
        hostileMobs.add(EntityType.ENDERMAN);
        hostileMobs.add(EntityType.WITCH);
        hostileMobs.add(EntityType.BLAZE);
        hostileMobs.add(EntityType.GHAST);
        hostileMobs.add(EntityType.PIGLIN);
        hostileMobs.add(EntityType.HOGLIN);
        hostileMobs.add(EntityType.DROWNED);
        hostileMobs.add(EntityType.HUSK);
        hostileMobs.add(EntityType.STRAY);
        hostileMobs.add(EntityType.PHANTOM);
        hostileMobs.add(EntityType.PILLAGER);
        hostileMobs.add(EntityType.VINDICATOR);
        hostileMobs.add(EntityType.EVOKER);
        hostileMobs.add(EntityType.RAVAGER);
        hostileMobs.add(EntityType.VEX);

        // Special mobs with unique behavior
        specialMobs.add(EntityType.WARDEN);
    }

    /**
     * Checks if a mob is a hostile type.
     *
     * @param mob Mob to check
     * @return true if hostile
     */
    public boolean isHostileMob(Mob mob) {
        return hostileMobs.contains(mob.getType());
    }

    /**
     * Checks if a mob is a special type (e.g., Warden).
     *
     * @param mob Mob to check
     * @return true if special
     */
    public boolean isSpecialMob(Mob mob) {
        return specialMobs.contains(mob.getType());
    }

    /**
     * Registers a custom mob type as hostile.
     *
     * @param type Entity type to register
     */
    public void registerHostileMob(EntityType type) {
        hostileMobs.add(type);
    }

    /**
     * Registers a custom mob type as special.
     *
     * @param type Entity type to register
     */
    public void registerSpecialMob(EntityType type) {
        specialMobs.add(type);
    }

    /**
     * Unregisters a mob type.
     *
     * @param type Entity type to unregister
     */
    public void unregister(EntityType type) {
        hostileMobs.remove(type);
        specialMobs.remove(type);
    }

    /**
     * Gets all registered hostile mob types.
     *
     * @return Set of hostile mob types
     */
    public Set<EntityType> getHostileMobs() {
        return Collections.unmodifiableSet(hostileMobs);
    }

    /**
     * Gets all registered special mob types.
     *
     * @return Set of special mob types
     */
    public Set<EntityType> getSpecialMobs() {
        return Collections.unmodifiableSet(specialMobs);
    }

    /**
     * Gets count of registered mob types.
     *
     * @return Total registered mobs
     */
    public int getRegisteredCount() {
        return hostileMobs.size() + specialMobs.size();
    }
}