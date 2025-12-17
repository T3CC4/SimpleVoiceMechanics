package de.tecca.simplevoicemechanics.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event fired when a player's voice is detected.
 *
 * <p>This event provides:
 * <ul>
 *   <li>The speaking player</li>
 *   <li>The location where voice was detected</li>
 * </ul>
 *
 * <p>Note: Volume information is not available from the SimpleVoiceChat API.
 * Detection range is configured per entity category in the config.yml.
 *
 * @author Tecca
 * @version 1.0.0
 */
public class VoiceDetectedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Location location;

    /**
     * Constructs a new VoiceDetectedEvent.
     *
     * @param player the player who spoke
     * @param location the location where voice was detected
     */
    public VoiceDetectedEvent(Player player, Location location) {
        this.player = player;
        this.location = location;
    }

    /**
     * Gets the player who spoke.
     *
     * @return the speaking player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the location where voice was detected.
     *
     * @return the voice location
     */
    public Location getLocation() {
        return location;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}