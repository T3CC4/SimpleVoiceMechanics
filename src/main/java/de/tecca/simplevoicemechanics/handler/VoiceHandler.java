package de.tecca.simplevoicemechanics.handler;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.tecca.simplevoicemechanics.SimpleVoiceMechanics;
import de.tecca.simplevoicemechanics.event.VoiceDetectedEvent;
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
 *   <li>Fires VoiceDetectedEvent when player speaks</li>
 * </ul>
 *
 * <p>Note: Volume detection is not possible with the SimpleVoiceChat API.
 * Detection is purely range-based.
 *
 * @author Tecca
 * @version 1.0.0
 */
public class VoiceHandler implements VoicechatPlugin {

    /** Plugin ID for SimpleVoiceChat registration */
    private static final String PLUGIN_ID = "simplevoicemechanics";

    private final SimpleVoiceMechanics plugin;
    private VoicechatApi voicechatApi;

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
     * <p>Fires VoiceDetectedEvent whenever a player speaks.
     * Volume information is not available from the API.
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

        // Fire VoiceDetectedEvent on main thread
        Bukkit.getScheduler().runTask(plugin, () -> {
            VoiceDetectedEvent voiceEvent = new VoiceDetectedEvent(
                    player,
                    playerLoc
            );
            Bukkit.getPluginManager().callEvent(voiceEvent);
        });
    }

    public VoicechatApi getVoicechatApi() {
        return voicechatApi;
    }
}