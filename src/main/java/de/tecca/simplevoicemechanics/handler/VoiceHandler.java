package de.tecca.simplevoicemechanics.handler;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.tecca.simplevoicemechanics.SimpleVoiceMechanics;
import de.tecca.simplevoicemechanics.event.VoiceDetectedEvent;
import de.tecca.simplevoicemechanics.manager.DetectionManager;
import de.tecca.simplevoicemechanics.model.VoiceDetection;
import de.tecca.simplevoicemechanics.service.VolumeCalculationService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Handles SimpleVoiceChat API integration.
 * Processes microphone packets and dispatches voice detection events.
 */
public class VoiceHandler implements VoicechatPlugin {

    private final SimpleVoiceMechanics plugin;
    private final VolumeCalculationService volumeService;
    private final DetectionManager detectionManager;
    private VoicechatApi voicechatApi;

    public VoiceHandler(
            SimpleVoiceMechanics plugin,
            VolumeCalculationService volumeService,
            DetectionManager detectionManager
    ) {
        this.plugin = plugin;
        this.volumeService = volumeService;
        this.detectionManager = detectionManager;
    }

    @Override
    public String getPluginId() {
        return "simplevoicemechanics";
    }

    @Override
    public void initialize(VoicechatApi api) {
        this.voicechatApi = api;
        plugin.getLogger().info("VoiceChat API initialized successfully!");
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
    }

    /**
     * Handles microphone packet events from SimpleVoiceChat.
     *
     * @param event Microphone packet event
     */
    private void onMicrophonePacket(MicrophonePacketEvent event) {
        try {
            processVoicePacket(event);
        } catch (Exception e) {
            plugin.getLogger().warning("Error processing voice packet: " + e.getMessage());
        }
    }

    /**
     * Processes voice packet and creates detection event.
     *
     * @param event Microphone packet event
     */
    private void processVoicePacket(MicrophonePacketEvent event) {
        // Get player
        Player player = Bukkit.getPlayer(event.getSenderConnection().getPlayer().getUuid());
        if (player == null || !player.isOnline()) {
            return;
        }

        // Calculate volume
        byte[] audioData = event.getPacket().getOpusEncodedData();
        double volume = volumeService.calculateVolume(audioData);

        // Check volume threshold
        if (!detectionManager.shouldProcessVolume(volume)) {
            return;
        }

        // Create detection model
        Location location = player.getLocation();
        double range = detectionManager.getConfig().getHearingRange();
        VoiceDetection detection = new VoiceDetection(player, location, volume, range);

        // Dispatch event on main thread
        Bukkit.getScheduler().runTask(plugin, () -> {
            VoiceDetectedEvent voiceEvent = new VoiceDetectedEvent(detection);
            Bukkit.getPluginManager().callEvent(voiceEvent);
        });
    }

    public VoicechatApi getVoicechatApi() {
        return voicechatApi;
    }
}