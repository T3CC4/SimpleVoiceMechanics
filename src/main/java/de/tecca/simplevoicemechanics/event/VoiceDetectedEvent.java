package de.tecca.simplevoicemechanics.event;

import de.tecca.simplevoicemechanics.model.VoiceDetection;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event fired when a player's voice is detected through SimpleVoiceChat.
 */
public class VoiceDetectedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final VoiceDetection detection;

    public VoiceDetectedEvent(VoiceDetection detection) {
        this.detection = detection;
    }

    public VoiceDetection getDetection() {
        return detection;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}