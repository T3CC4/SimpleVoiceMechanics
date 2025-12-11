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

public class VoiceHandler implements VoicechatPlugin {

    private final SimpleVoiceMechanics plugin;
    private VoicechatApi voicechatApi;

    public VoiceHandler(SimpleVoiceMechanics plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getPluginId() {
        return "simplevoicelistener";
    }

    @Override
    public void initialize(VoicechatApi api) {
        this.voicechatApi = api;
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
    }

    private void onMicrophonePacket(MicrophonePacketEvent event) {
        // Spieler Position holen
        Player player = Bukkit.getPlayer(event.getSenderConnection().getPlayer().getUuid());
        if (player == null || !player.isOnline()) {
            return;
        }

        Location playerLoc = player.getLocation();
        ConfigManager config = plugin.getConfigManager();
        double hearingRange = config.getHearingRange();

        // Audio-Lautstärke berechnen
        float volume = calculateVolume(event.getPacket().getOpusEncodedData());

        // Nur wenn Lautstärke über Schwellwert
        if (volume < config.getVolumeThreshold()) {
            return;
        }

        // Voice-Event an Listener weitergeben
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

    private float calculateVolume(byte[] audioData) {
        if (audioData == null || audioData.length == 0) {
            return 0.0f;
        }

        // Einfache RMS-Berechnung für Lautstärke
        long sum = 0;
        for (byte b : audioData) {
            sum += b * b;
        }

        double rms = Math.sqrt((double) sum / audioData.length);
        return (float) (rms / 127.0); // Normalisiert auf 0-1
    }

    public VoicechatApi getVoicechatApi() {
        return voicechatApi;
    }
}