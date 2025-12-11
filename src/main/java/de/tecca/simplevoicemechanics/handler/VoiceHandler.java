package de.tecca.simplevoicemechanics.handler;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.tecca.simplevoicemechanics.SimpleVoiceMechanics;
import de.tecca.simplevoicemechanics.event.VoiceDetectedEvent;
import de.tecca.simplevoicemechanics.manager.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Handles SimpleVoiceChat API integration and voice packet processing.
 *
 * <p>This handler:
 * <ul>
 *   <li>Registers with SimpleVoiceChat API</li>
 *   <li>Processes microphone packets in real-time</li>
 *   <li>Calculates voice volume using RMS</li>
 *   <li>Fires VoiceDetectedEvent for volumes above threshold</li>
 * </ul>
 *
 * @author Tecca
 * @version 1.0.0
 * @since 1.0.0
 */
public class VoiceHandler implements VoicechatPlugin {

    /** Plugin ID for SimpleVoiceChat registration */
    private static final String PLUGIN_ID = "simplevoicemechanics";

    /** Maximum byte value for normalization (signed byte range) */
    private static final double MAX_BYTE_VALUE = 127.0;

    private final SimpleVoiceMechanics plugin;
    private VoicechatApi voicechatApi;

    /**
     * Constructs a new VoiceHandler.
     *
     * @param plugin the plugin instance
     */
    public VoiceHandler(SimpleVoiceMechanics plugin) {
        this.plugin = plugin;
    }

    /**
     * Gets the plugin ID for SimpleVoiceChat registration.
     *
     * @return the plugin ID
     */
    @Override
    public String getPluginId() {
        return PLUGIN_ID;
    }

    /**
     * Called when SimpleVoiceChat API is initialized.
     *
     * @param api the VoiceChat API instance
     */
    @Override
    public void initialize(VoicechatApi api) {
        this.voicechatApi = api;
    }

    /**
     * Registers event listeners with SimpleVoiceChat.
     *
     * @param registration the event registration handler
     */
    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
    }

    /**
     * Handles microphone packet events from SimpleVoiceChat.
     *
     * <p>Processes audio data, calculates volume, and fires VoiceDetectedEvent
     * if the volume exceeds the configured threshold.
     *
     * @param event the microphone packet event
     */
    private void onMicrophonePacket(MicrophonePacketEvent event) {
        // Get player from connection
        Player player = Bukkit.getPlayer(event.getSenderConnection().getPlayer().getUuid());
        if (player == null || !player.isOnline()) {
            return;
        }

        Location playerLoc = player.getLocation();
        ConfigManager config = plugin.getConfigManager();
        double hearingRange = config.getHearingRange();

        // Calculate audio volume from opus encoded data
        float volume = calculateVolume(event.getPacket().getOpusEncodedData());

        // Check if volume exceeds threshold
        if (volume < config.getVolumeThreshold()) {
            return;
        }

        // Fire VoiceDetectedEvent on main thread
        Bukkit.getScheduler().runTask(plugin, () -> {
            VoiceDetectedEvent voiceEvent = new VoiceDetectedEvent(
                    player,
                    playerLoc,
                    volume,
                    hearingRange
            );
            Bukkit.getPluginManager().callEvent(voiceEvent);
        });
    }

    /**
     * Calculates the volume of audio data using RMS (Root Mean Square).
     *
     * <p>RMS is calculated as: sqrt(sum(byte²) / length)
     * The result is normalized to a 0.0 - 1.0 range.
     *
     * @param audioData the opus encoded audio data
     * @return the normalized volume (0.0 - 1.0)
     */
    private float calculateVolume(byte[] audioData) {
        if (audioData == null || audioData.length == 0) {
            return 0.0f;
        }

        // Calculate sum of squares
        long sumOfSquares = 0;
        for (byte b : audioData) {
            sumOfSquares += (long) b * b;
        }

        // Calculate RMS (Root Mean Square)
        double rms = Math.sqrt((double) sumOfSquares / audioData.length);

        // Normalize to 0.0 - 1.0 range
        return (float) (rms / MAX_BYTE_VALUE);
    }

    /**
     * Gets the SimpleVoiceChat API instance.
     *
     * @return the VoiceChat API, or null if not initialized
     */
    public VoicechatApi getVoicechatApi() {
        return voicechatApi;
    }
}