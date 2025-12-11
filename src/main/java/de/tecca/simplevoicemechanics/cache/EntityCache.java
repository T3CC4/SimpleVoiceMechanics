package de.tecca.simplevoicemechanics.cache;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches nearby entities to reduce expensive getNearbyEntities() calls.
 * Automatically expires entries after a configurable timeout.
 */
public class EntityCache {

    private static final long DEFAULT_CACHE_DURATION = 30000L; // 30 seconds
    private static final int CACHE_RADIUS = 5; // Cache entries within 5 blocks

    private final Map<CacheKey, CacheEntry> cache = new ConcurrentHashMap<>();
    private final long cacheDuration;

    public EntityCache() {
        this(DEFAULT_CACHE_DURATION);
    }

    public EntityCache(long cacheDuration) {
        this.cacheDuration = cacheDuration;
    }

    /**
     * Gets cached entities or fetches new ones if cache miss/expired.
     *
     * @param location Location to check around
     * @param range Search range
     * @return Collection of nearby mobs
     */
    public Collection<Mob> getNearbyMobs(Location location, double range) {
        CacheKey key = new CacheKey(location);
        CacheEntry entry = cache.get(key);

        long currentTime = System.currentTimeMillis();

        // Check if cache is valid
        if (entry != null && !entry.isExpired(currentTime)) {
            return entry.getMobs();
        }

        // Cache miss or expired - fetch new entities
        Collection<Mob> mobs = fetchNearbyMobs(location, range);
        cache.put(key, new CacheEntry(mobs, currentTime));

        // Cleanup old entries
        cleanupExpired(currentTime);

        return mobs;
    }

    /**
     * Fetches nearby mobs from the world.
     *
     * @param location Center location
     * @param range Search range
     * @return Collection of mobs
     */
    private Collection<Mob> fetchNearbyMobs(Location location, double range) {
        Collection<Entity> entities = location.getWorld().getNearbyEntities(
                location, range, range, range,
                entity -> entity instanceof Mob
        );

        List<Mob> mobs = new ArrayList<>();
        for (Entity entity : entities) {
            mobs.add((Mob) entity);
        }

        return mobs;
    }

    /**
     * Invalidates cache for a specific location.
     *
     * @param location Location to invalidate
     */
    public void invalidate(Location location) {
        cache.remove(new CacheKey(location));
    }

    /**
     * Clears entire cache.
     */
    public void clear() {
        cache.clear();
    }

    /**
     * Gets cache statistics.
     *
     * @return Cache size
     */
    public int getSize() {
        return cache.size();
    }

    /**
     * Removes expired entries from cache.
     *
     * @param currentTime Current timestamp
     */
    private void cleanupExpired(long currentTime) {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired(currentTime));
    }

    /**
     * Cache key based on block coordinates.
     */
    private static class CacheKey {
        private final int x;
        private final int y;
        private final int z;
        private final String world;

        CacheKey(Location location) {
            // Round to nearest CACHE_RADIUS blocks
            this.x = (location.getBlockX() / CACHE_RADIUS) * CACHE_RADIUS;
            this.y = (location.getBlockY() / CACHE_RADIUS) * CACHE_RADIUS;
            this.z = (location.getBlockZ() / CACHE_RADIUS) * CACHE_RADIUS;
            this.world = location.getWorld().getName();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CacheKey)) return false;
            CacheKey cacheKey = (CacheKey) o;
            return x == cacheKey.x && y == cacheKey.y && z == cacheKey.z && world.equals(cacheKey.world);
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z, world);
        }
    }

    /**
     * Cache entry with expiration.
     */
    private class CacheEntry {
        private final Collection<Mob> mobs;
        private final long timestamp;

        CacheEntry(Collection<Mob> mobs, long timestamp) {
            this.mobs = new ArrayList<>(mobs);
            this.timestamp = timestamp;
        }

        Collection<Mob> getMobs() {
            return mobs;
        }

        boolean isExpired(long currentTime) {
            return (currentTime - timestamp) > cacheDuration;
        }
    }
}