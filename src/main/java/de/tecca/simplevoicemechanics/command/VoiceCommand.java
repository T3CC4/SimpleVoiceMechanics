package de.tecca.simplevoicemechanics.command;

import de.tecca.simplevoicemechanics.SimpleVoiceMechanics;
import de.tecca.simplevoicemechanics.manager.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Handles all plugin commands for administration.
 *
 * <p>Provides commands for:
 * <ul>
 *   <li>Reloading configuration</li>
 *   <li>Toggling features (mobs, sculk)</li>
 *   <li>Viewing plugin status</li>
 * </ul>
 *
 * @author Tecca
 * @version 1.0.0
 * @since 1.0.0
 */
public class VoiceCommand implements CommandExecutor {

    private final SimpleVoiceMechanics plugin;

    /**
     * Constructs a new VoiceCommand executor.
     *
     * @param plugin the plugin instance
     */
    public VoiceCommand(SimpleVoiceMechanics plugin) {
        this.plugin = plugin;
    }

    /**
     * Executes the given command.
     *
     * @param sender the command sender
     * @param command the command being executed
     * @param label the alias used
     * @param args the command arguments
     * @return true if the command was handled successfully
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("voicelistener.admin")) {
            sender.sendMessage(ChatColor.RED + "No permission!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;

            case "toggle":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /voicelistener toggle <mobs|sculk>");
                    return true;
                }
                handleToggle(sender, args[1]);
                break;

            case "status":
                handleStatus(sender);
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    /**
     * Sends the help message with all available commands.
     *
     * @param sender the command sender
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== SimpleVoiceListener ===");
        sender.sendMessage(ChatColor.YELLOW + "/voicelistener reload " +
                ChatColor.GRAY + "- Reload configuration");
        sender.sendMessage(ChatColor.YELLOW + "/voicelistener toggle <mobs|sculk> " +
                ChatColor.GRAY + "- Toggle feature on/off");
        sender.sendMessage(ChatColor.YELLOW + "/voicelistener status " +
                ChatColor.GRAY + "- Display current status");
    }

    /**
     * Handles the reload command.
     *
     * @param sender the command sender
     */
    private void handleReload(CommandSender sender) {
        plugin.getConfigManager().reload();
        sender.sendMessage(ChatColor.GREEN + "Configuration reloaded!");
    }

    /**
     * Handles the toggle command for features.
     *
     * @param sender the command sender
     * @param feature the feature to toggle (mobs or sculk)
     */
    private void handleToggle(CommandSender sender, String feature) {
        ConfigManager config = plugin.getConfigManager();

        switch (feature.toLowerCase()) {
            case "mobs":
                toggleMobHearing(sender, config);
                break;

            case "sculk":
                toggleSculkHearing(sender, config);
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown feature: " + feature);
                sender.sendMessage(ChatColor.GRAY + "Available: mobs, sculk");
                break;
        }
    }

    /**
     * Toggles mob hearing feature.
     *
     * @param sender the command sender
     * @param config the configuration manager
     */
    private void toggleMobHearing(CommandSender sender, ConfigManager config) {
        boolean newState = !config.isMobHearingEnabled();
        config.setMobHearingEnabled(newState);
        sender.sendMessage(ChatColor.GREEN + "Mob Hearing: " +
                (newState ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
    }

    /**
     * Toggles sculk sensor hearing feature.
     *
     * @param sender the command sender
     * @param config the configuration manager
     */
    private void toggleSculkHearing(CommandSender sender, ConfigManager config) {
        boolean newState = !config.isSculkHearingEnabled();
        config.setSculkHearingEnabled(newState);
        sender.sendMessage(ChatColor.GREEN + "Sculk Hearing: " +
                (newState ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
    }

    /**
     * Handles the status command, displaying current plugin configuration.
     *
     * @param sender the command sender
     */
    private void handleStatus(CommandSender sender) {
        ConfigManager config = plugin.getConfigManager();

        sender.sendMessage(ChatColor.GOLD + "=== Status ===");
        sender.sendMessage(formatStatus("Mob Hearing", config.isMobHearingEnabled()));
        sender.sendMessage(formatStatus("  Hostile Mobs", config.isHostileMobHearingEnabled()));
        sender.sendMessage(formatStatus("  Warden", config.isWardenHearingEnabled()));
        sender.sendMessage(formatStatus("Sculk Hearing", config.isSculkHearingEnabled()));
        sender.sendMessage(ChatColor.YELLOW + "Range: " +
                ChatColor.WHITE + config.getHearingRange() + " blocks");
        sender.sendMessage(ChatColor.YELLOW + "Volume Threshold: " +
                ChatColor.WHITE + config.getVolumeThreshold());
    }

    /**
     * Formats a status line with enabled/disabled indicator.
     *
     * @param name the feature name
     * @param enabled whether the feature is enabled
     * @return formatted status string
     */
    private String formatStatus(String name, boolean enabled) {
        return ChatColor.YELLOW + name + ": " +
                (enabled ? ChatColor.GREEN + "✓" : ChatColor.RED + "✗");
    }
}