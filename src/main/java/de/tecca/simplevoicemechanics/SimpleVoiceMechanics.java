package de.tecca.simplevoicemechanics;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.tecca.simplevoicemechanics.command.VoiceCommand;
import de.tecca.simplevoicemechanics.handler.VoiceHandler;
import de.tecca.simplevoicemechanics.listener.MobListener;
import de.tecca.simplevoicemechanics.listener.SculkListener;
import de.tecca.simplevoicemechanics.manager.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleVoiceMechanics extends JavaPlugin {

    private static SimpleVoiceMechanics instance;
    private VoiceHandler voiceHandler;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;

        // Config initialisieren
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        // Voice Chat Service registrieren
        BukkitVoicechatService service = getServer().getServicesManager().load(BukkitVoicechatService.class);
        if (service != null) {
            voiceHandler = new VoiceHandler(this);
            service.registerPlugin(voiceHandler);
            getLogger().info("SimpleVoice API erfolgreich integriert!");
        } else {
            getLogger().severe("SimpleVoice Plugin nicht gefunden! Bitte installieren.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Listener registrieren
        getServer().getPluginManager().registerEvents(new MobListener(this), this);
        getServer().getPluginManager().registerEvents(new SculkListener(this), this);

        // Command registrieren
        getCommand("voicelistener").setExecutor(new VoiceCommand(this));

        getLogger().info("Plugin erfolgreich aktiviert!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin deaktiviert!");
    }

    public static SimpleVoiceMechanics getInstance() {
        return instance;
    }

    public VoiceHandler getVoiceHandler() {
        return voiceHandler;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
