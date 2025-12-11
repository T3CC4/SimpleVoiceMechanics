package de.tecca.simplevoicemechanics.model;

import org.bukkit.entity.Mob;

/**
 * Result of a mob detection check.
 * Contains the mob and calculated effective volume.
 */
public class MobDetectionResult {

    private final Mob mob;
    private final double effectiveVolume;
    private final double distance;
    private final boolean triggered;

    public MobDetectionResult(Mob mob, double effectiveVolume, double distance, boolean triggered) {
        this.mob = mob;
        this.effectiveVolume = effectiveVolume;
        this.distance = distance;
        this.triggered = triggered;
    }

    public Mob getMob() {
        return mob;
    }

    public double getEffectiveVolume() {
        return effectiveVolume;
    }

    public double getDistance() {
        return distance;
    }

    public boolean isTriggered() {
        return triggered;
    }

    @Override
    public String toString() {
        return "MobDetectionResult{" +
                "mob=" + mob.getType() +
                ", effectiveVolume=" + String.format("%.2f", effectiveVolume) +
                ", distance=" + String.format("%.1f", distance) +
                ", triggered=" + triggered +
                '}';
    }
}