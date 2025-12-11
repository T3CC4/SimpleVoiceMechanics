package de.tecca.simplevoicemechanics.listener;

import de.tecca.simplevoicemechanics.event.VoiceDetectedEvent;
import de.tecca.simplevoicemechanics.service.SculkDetectionService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Listener for sculk sensor-related voice detection.
 * Delegates to SculkDetectionService for business logic.
 */
public class SculkListener implements Listener {

    private final SculkDetectionService sculkService;

    public SculkListener(SculkDetectionService sculkService) {
        this.sculkService = sculkService;
    }

    @EventHandler
    public void onVoiceDetected(VoiceDetectedEvent event) {
        sculkService.processDetection(event.getDetection());
    }
}