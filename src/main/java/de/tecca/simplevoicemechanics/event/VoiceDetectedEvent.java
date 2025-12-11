package de.tecca.simplevoicemechanics.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class VoiceDetectedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Location location;
    private final float volume;
    private final double range;

    public VoiceDetectedEvent(Player player, Location location, float volume, double range) {
        this.player = player;
        this.location = location;
        this.volume = volume;
        this.range = range;
    }

    public Player getPlayer() {
        return player;
    }

    public Location getLocation() {
        return location;
    }

    public float getVolume() {
        return volume;
    }

    public double getRange() {
        return range;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}