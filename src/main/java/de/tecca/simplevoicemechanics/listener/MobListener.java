package de.tecca.simplevoicemechanics.listener;

import de.tecca.simplevoicemechanics.event.VoiceDetectedEvent;
import de.tecca.simplevoicemechanics.service.MobDetectionService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Listener for mob-related voice detection.
 * Delegates to MobDetectionService for business logic.
 */
public class MobListener implements Listener {

    private final MobDetectionService mobService;

    public MobListener(MobDetectionService mobService) {
        this.mobService = mobService;
    }

    @EventHandler
    public void onVoiceDetected(VoiceDetectedEvent event) {
        mobService.processDetection(event.getDetection());
    }
}