package de.tecca.simplevoicemechanics.task;

import de.tecca.simplevoicemechanics.model.VoiceDetection;
import de.tecca.simplevoicemechanics.service.MobDetectionService;
import de.tecca.simplevoicemechanics.service.SculkDetectionService;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Async task for processing voice detections.
 * Offloads detection processing from main thread.
 */
public class AsyncDetectionTask extends BukkitRunnable {

    private final VoiceDetection detection;
    private final MobDetectionService mobService;
    private final SculkDetectionService sculkService;

    public AsyncDetectionTask(
            VoiceDetection detection,
            MobDetectionService mobService,
            SculkDetectionService sculkService
    ) {
        this.detection = detection;
        this.mobService = mobService;
        this.sculkService = sculkService;
    }

    @Override
    public void run() {
        // Process mob detection
        mobService.processDetection(detection);

        // Process sculk detection
        sculkService.processDetection(detection);
    }
}