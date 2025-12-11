package de.tecca.simplevoicemechanics.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event fired when a player's voice is detected above the configured threshold.
 *
 * <p>This event provides information about:
 * <ul>
 *   <li>The speaking player</li>
 *   <li>The location where voice was detected</li>
 *   <li>The volume of the voice (0.0 - 1.0)</li>
 *   <li>The detection range</li>
 * </ul>
 *
 * <p>This event is called on the main thread and can be used by other
 * plugins to implement custom voice-based mechanics.
 *
 * @author Tecca
 * @version 1.0.0
 * @since 1.0.0
 */
public class VoiceDetectedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final Location location;
    private final float volume;
    private final double range;

    /**
     * Constructs a new VoiceDetectedEvent.
     *
     * @param player the player who spoke
     * @param location the location where voice was detected
     * @param volume the voice volume (0.0 - 1.0)
     * @param range the detection range in blocks
     */
    public VoiceDetectedEvent(Player player, Location location, float volume, double range) {
        this.player = player;
        this.location = location;
        this.volume = volume;
        this.range = range;
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

    /**
     * Gets the voice volume.
     *
     * <p>Volume is normalized between 0.0 (silent) and 1.0 (maximum).
     * The volume is calculated using RMS (Root Mean Square) of the audio data.
     *
     * @return the voice volume (0.0 - 1.0)
     */
    public float getVolume() {
        return volume;
    }

    /**
     * Gets the detection range.
     *
     * <p>Entities and blocks within this range can potentially detect the voice,
     * though effective detection also depends on volume and distance.
     *
     * @return the detection range in blocks
     */
    public double getRange() {
        return range;
    }

    /**
     * Gets the handler list for this event.
     *
     * @return the handler list
     */
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Gets the static handler list for this event.
     *
     * @return the static handler list
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}