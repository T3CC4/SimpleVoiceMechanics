package de.tecca.simplevoicemechanics;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.tecca.simplevoicemechanics.command.VoiceCommand;
import de.tecca.simplevoicemechanics.handler.VoiceHandler;
import de.tecca.simplevoicemechanics.listener.MobListener;
import de.tecca.simplevoicemechanics.listener.SculkListener;
import de.tecca.simplevoicemechanics.manager.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for SimpleVoiceMechanics.
 *
 * <p>Integrates SimpleVoiceChat with Minecraft mechanics, allowing:
 * <ul>
 *   <li>Mobs to hear and react to player voices</li>
 *   <li>Sculk Sensors to detect voice activity</li>
 *   <li>Custom voice detection events for developers</li>
 * </ul>
 *
 * @author Tecca
 * @version 1.0.0
 * @since 1.0.0
 */
public final class SimpleVoiceMechanics extends JavaPlugin {

    /** Singleton instance of the plugin */
    private static SimpleVoiceMechanics instance;

    /** Handler for SimpleVoiceChat API integration */
    private VoiceHandler voiceHandler;

    /** Manager for plugin configuration */
    private ConfigManager configManager;

    /**
     * Called when the plugin is enabled.
     *
     * <p>Initializes configuration, registers SimpleVoiceChat integration,
     * sets up event listeners, and registers commands.
     */
    @Override
    public void onEnable() {
        instance = this;

        // Initialize configuration
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        // Register SimpleVoiceChat service
        if (!initializeVoiceChat()) {
            return;
        }

        // Register event listeners
        registerListeners();

        // Register commands
        registerCommands();

        getLogger().info("Plugin successfully enabled!");
    }

    /**
     * Called when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        getLogger().info("Plugin disabled!");
    }

    /**
     * Initializes SimpleVoiceChat integration.
     *
     * @return true if initialization was successful, false otherwise
     */
    private boolean initializeVoiceChat() {
        BukkitVoicechatService service = getServer().getServicesManager()
                .load(BukkitVoicechatService.class);

        if (service == null) {
            getLogger().severe("SimpleVoiceChat plugin not found! Please install it.");
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }

        voiceHandler = new VoiceHandler(this);
        service.registerPlugin(voiceHandler);
        getLogger().info("SimpleVoiceChat API successfully integrated!");
        return true;
    }

    /**
     * Registers all event listeners for the plugin.
     */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new MobListener(this), this);
        getServer().getPluginManager().registerEvents(new SculkListener(this), this);
    }

    /**
     * Registers all commands for the plugin.
     */
    private void registerCommands() {
        getCommand("voicelistener").setExecutor(new VoiceCommand(this));
    }

    /**
     * Gets the singleton instance of the plugin.
     *
     * @return the plugin instance
     */
    public static SimpleVoiceMechanics getInstance() {
        return instance;
    }

    /**
     * Gets the voice handler for SimpleVoiceChat integration.
     *
     * @return the voice handler
     */
    public VoiceHandler getVoiceHandler() {
        return voiceHandler;
    }

    /**
     * Gets the configuration manager.
     *
     * @return the config manager
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
}