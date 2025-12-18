package de.tecca.simplevoicemechanics.handler;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.tecca.simplevoicemechanics.SimpleVoiceMechanics;
import de.tecca.simplevoicemechanics.event.VoiceDetectedEvent;
import de.tecca.simplevoicemechanics.manager.ConfigManager;
import de.tecca.simplevoicemechanics.util.AudioUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Handles SimpleVoiceChat API integration and voice packet processing.
 *
 * <p>This handler:
 * <ul>
 *   <li>Registers with SimpleVoiceChat API</li>
 *   <li>Decodes Opus audio packets in real-time</li>
 *   <li>Calculates audio levels in decibels</li>
 *   <li>Fires VoiceDetectedEvent for audio above threshold</li>
 * </ul>
 *
 * @author Tecca
 * @version 1.2.0
 */
public class VoiceHandler implements VoicechatPlugin {

    /** Plugin ID for SimpleVoiceChat registration */
    private static final String PLUGIN_ID = "simplevoicemechanics";

    private final SimpleVoiceMechanics plugin;
    private VoicechatApi voicechatApi;
    private OpusDecoder decoder;

    public VoiceHandler(SimpleVoiceMechanics plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getPluginId() {
        return PLUGIN_ID;
    }

    @Override
    public void initialize(VoicechatApi api) {
        this.voicechatApi = api;
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
    }

    /**
     * Handles microphone packet events from SimpleVoiceChat.
     *
     * <p>Decodes Opus audio, calculates decibel level, and fires
     * VoiceDetectedEvent with the audio level. Each listener then
     * checks against their own threshold.
     *
     * @param event the microphone packet event
     */
    private void onMicrophonePacket(MicrophonePacketEvent event) {
        // Get player from connection
        Player player = Bukkit.getPlayer(event.getSenderConnection().getPlayer().getUuid());
        if (player == null || !player.isOnline()) {
            return;
        }

        // Get audio data
        byte[] opusData = event.getPacket().getOpusEncodedData();
        if (opusData.length == 0) {
            return; // Empty packet (player stopped talking)
        }

        // Initialize decoder if needed
        if (decoder == null) {
            decoder = event.getVoicechat().createDecoder();
        }

        // Decode Opus to PCM samples
        decoder.resetState();
        short[] samples = decoder.decode(opusData);

        // Calculate audio level in decibels
        double db = AudioUtils.calculateAudioLevel(samples);

        // Debug logging
        if (plugin.getConfig().getBoolean("debug.audio-logging", false)) {
            plugin.getLogger().info(AudioUtils.getDebugInfo(samples));
        }

        // Fire VoiceDetectedEvent on main thread
        // Listeners will check their own thresholds
        Location playerLoc = player.getLocation();
        Bukkit.getScheduler().runTask(plugin, () -> {
            VoiceDetectedEvent voiceEvent = new VoiceDetectedEvent(
                    player,
                    playerLoc,
                    db
            );
            Bukkit.getPluginManager().callEvent(voiceEvent);
        });
    }

    public VoicechatApi getVoicechatApi() {
        return voicechatApi;
    }
}