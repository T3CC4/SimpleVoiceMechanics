package de.tecca.simplevoicemechanics.model;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Represents a voice detection event with all relevant data.
 * Immutable data class for passing detection information.
 */
public class VoiceDetection {

    private final Player player;
    private final Location location;
    private final double volume;
    private final double range;
    private final long timestamp;

    public VoiceDetection(Player player, Location location, double volume, double range) {
        this.player = player;
        this.location = location.clone(); // Clone to prevent external modification
        this.volume = volume;
        this.range = range;
        this.timestamp = System.currentTimeMillis();
    }

    public Player getPlayer() {
        return player;
    }

    public Location getLocation() {
        return location.clone();
    }

    public double getVolume() {
        return volume;
    }

    public double getRange() {
        return range;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "VoiceDetection{" +
                "player=" + player.getName() +
                ", volume=" + String.format("%.2f", volume) +
                ", range=" + range +
                ", timestamp=" + timestamp +
                '}';
    }
}