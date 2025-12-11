package de.tecca.simplevoicemechanics;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.tecca.simplevoicemechanics.cache.EntityCache;
import de.tecca.simplevoicemechanics.command.VoiceCommand;
import de.tecca.simplevoicemechanics.handler.VoiceHandler;
import de.tecca.simplevoicemechanics.listener.MobListener;
import de.tecca.simplevoicemechanics.listener.SculkListener;
import de.tecca.simplevoicemechanics.manager.ConfigManager;
import de.tecca.simplevoicemechanics.manager.CooldownManager;
import de.tecca.simplevoicemechanics.manager.DetectionManager;
import de.tecca.simplevoicemechanics.manager.FeatureManager;
import de.tecca.simplevoicemechanics.model.DetectionConfig;
import de.tecca.simplevoicemechanics.registry.MobTypeRegistry;
import de.tecca.simplevoicemechanics.service.MobDetectionService;
import de.tecca.simplevoicemechanics.service.SculkDetectionService;
import de.tecca.simplevoicemechanics.service.VolumeCalculationService;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for SimpleVoiceMechanics.
 * Uses dependency injection pattern for clean architecture.
 */
public final class SimpleVoiceMechanics extends JavaPlugin {

    private static SimpleVoiceMechanics instance;

    // Managers
    private ConfigManager configManager;
    private DetectionManager detectionManager;
    private FeatureManager featureManager;
    private CooldownManager cooldownManager;

    // Services
    private VolumeCalculationService volumeService;
    private MobDetectionService mobDetectionService;
    private SculkDetectionService sculkDetectionService;

    // Registry & Cache
    private MobTypeRegistry mobRegistry;
    private EntityCache entityCache;

    // Handler
    private VoiceHandler voiceHandler;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize managers and services
        if (!initializeComponents()) {
            return;
        }

        // Register SimpleVoiceChat API
        if (!initializeVoiceChat()) {
            return;
        }

        // Register listeners and commands
        registerListeners();
        registerCommands();

        getLogger().info("SimpleVoiceMechanics v" + getDescription().getVersion() + " enabled!");
        getLogger().info("Mob types registered: " + mobRegistry.getRegisteredCount());
    }

    @Override
    public void onDisable() {
        // Cleanup
        if (entityCache != null) {
            entityCache.clear();
        }
        if (cooldownManager != null) {
            cooldownManager.clearAll();
        }

        getLogger().info("SimpleVoiceMechanics disabled!");
    }

    /**
     * Initializes all managers, services, and components.
     *
     * @return true if successful
     */
    private boolean initializeComponents() {
        try {
            // Load configuration
            configManager = new ConfigManager(this);
            configManager.loadConfig();

            // Initialize managers
            DetectionConfig detectionConfig = configManager.loadDetectionConfig();
            detectionManager = new DetectionManager(detectionConfig);
            featureManager = configManager.loadFeatureManager();
            cooldownManager = new CooldownManager();

            // Initialize registry and cache
            mobRegistry = new MobTypeRegistry();
            entityCache = new EntityCache();

            // Initialize services
            volumeService = new VolumeCalculationService();
            mobDetectionService = new MobDetectionService(
                    this, entityCache, mobRegistry, detectionManager, featureManager
            );
            sculkDetectionService = new SculkDetectionService(
                    this, cooldownManager, detectionManager, featureManager
            );

            return true;
        } catch (Exception e) {
            getLogger().severe("Failed to initialize components: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }
    }

    /**
     * Initializes SimpleVoiceChat API integration.
     *
     * @return true if successful
     */
    private boolean initializeVoiceChat() {
        BukkitVoicechatService service = getServer().getServicesManager().load(BukkitVoicechatService.class);

        if (service == null) {
            getLogger().severe("SimpleVoiceChat plugin not found!");
            getLogger().severe("Download: https://modrinth.com/plugin/simple-voice-chat");
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }

        try {
            voiceHandler = new VoiceHandler(this, volumeService, detectionManager);
            service.registerPlugin(voiceHandler);
            getLogger().info("Successfully hooked into SimpleVoiceChat API!");
            return true;
        } catch (Exception e) {
            getLogger().severe("Failed to initialize SimpleVoiceChat: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }
    }

    /**
     * Registers event listeners.
     */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(
                new MobListener(mobDetectionService), this
        );
        getServer().getPluginManager().registerEvents(
                new SculkListener(sculkDetectionService), this
        );
    }

    /**
     * Registers commands.
     */
    private void registerCommands() {
        getCommand("voicemechanics").setExecutor(new VoiceCommand(this));
    }

    /**
     * Reloads configuration and reinitializes components.
     */
    public void reload() {
        configManager.loadConfig();
        DetectionConfig newConfig = configManager.loadDetectionConfig();
        detectionManager.updateConfig(newConfig);
        featureManager = configManager.loadFeatureManager();
        entityCache.clear();
        cooldownManager.clearAll();
    }

    // Getters
    public static SimpleVoiceMechanics getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DetectionManager getDetectionManager() {
        return detectionManager;
    }

    public FeatureManager getFeatureManager() {
        return featureManager;
    }

    public MobTypeRegistry getMobRegistry() {
        return mobRegistry;
    }

    public EntityCache getEntityCache() {
        return entityCache;
    }

    public VoiceHandler getVoiceHandler() {
        return voiceHandler;
    }
}