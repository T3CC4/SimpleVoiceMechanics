package de.tecca.simplevoicemechanics.command;

import de.tecca.simplevoicemechanics.SimpleVoiceMechanics;
import de.tecca.simplevoicemechanics.manager.FeatureManager;
import de.tecca.simplevoicemechanics.model.DetectionConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main command handler for SimpleVoiceMechanics.
 * Provides admin commands for configuration and status.
 */
public class VoiceCommand implements CommandExecutor, TabCompleter {

    private final SimpleVoiceMechanics plugin;

    public VoiceCommand(SimpleVoiceMechanics plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("voicemechanics.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> handleReload(sender);
            case "toggle" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /voicemechanics toggle <mobs|sculk>");
                    return true;
                }
                handleToggle(sender, args[1]);
            }
            case "status" -> handleStatus(sender);
            case "help" -> sendHelp(sender);
            default -> {
                sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + args[0]);
                sender.sendMessage(ChatColor.GRAY + "Use /voicemechanics help for available commands");
            }
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        try {
            plugin.reload();
            sender.sendMessage(ChatColor.GREEN + "Configuration reloaded successfully!");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to reload: " + e.getMessage());
            plugin.getLogger().severe("Reload error: " + e.getMessage());
        }
    }

    private void handleToggle(CommandSender sender, String feature) {
        FeatureManager features = plugin.getFeatureManager();

        switch (feature.toLowerCase()) {
            case "mobs" -> {
                boolean newState = !features.isMobHearingEnabled();
                features.setMobHearingEnabled(newState);
                plugin.getConfigManager().saveFeatures(features);
                sender.sendMessage(ChatColor.GREEN + "Mob hearing: " + formatState(newState));
            }
            case "sculk" -> {
                boolean newState = !features.isSculkHearingEnabled();
                features.setSculkHearingEnabled(newState);
                plugin.getConfigManager().saveFeatures(features);
                sender.sendMessage(ChatColor.GREEN + "Sculk hearing: " + formatState(newState));
            }
            default -> {
                sender.sendMessage(ChatColor.RED + "Unknown feature: " + feature);
                sender.sendMessage(ChatColor.GRAY + "Available: mobs, sculk");
            }
        }
    }

    private void handleStatus(CommandSender sender) {
        FeatureManager features = plugin.getFeatureManager();
        DetectionConfig config = plugin.getDetectionManager().getConfig();

        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "=== SimpleVoiceMechanics Status ===");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "Mob Hearing: " + formatState(features.isMobHearingEnabled()));
        sender.sendMessage(ChatColor.GRAY + "  ├─ Hostile: " + formatState(features.isHostileMobHearingEnabled()));
        sender.sendMessage(ChatColor.GRAY + "  └─ Warden: " + formatState(features.isWardenHearingEnabled()));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "Sculk: " + formatState(features.isSculkHearingEnabled()));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "Detection:");
        sender.sendMessage(ChatColor.GRAY + "  ├─ Range: " + ChatColor.WHITE + config.getHearingRange() + " blocks");
        sender.sendMessage(ChatColor.GRAY + "  ├─ Threshold: " + ChatColor.WHITE + String.format("%.2f", config.getVolumeThreshold()));
        sender.sendMessage(ChatColor.GRAY + "  └─ Min Volume: " + ChatColor.WHITE + String.format("%.2f", config.getMinVolume()));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "Cache: " + ChatColor.WHITE + plugin.getEntityCache().getSize() + " entries");
        sender.sendMessage("");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "=== SimpleVoiceMechanics ===");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "/voicemechanics reload " + ChatColor.GRAY + "- Reload config");
        sender.sendMessage(ChatColor.YELLOW + "/voicemechanics toggle <feature> " + ChatColor.GRAY + "- Toggle feature");
        sender.sendMessage(ChatColor.YELLOW + "/voicemechanics status " + ChatColor.GRAY + "- Show status");
        sender.sendMessage(ChatColor.YELLOW + "/voicemechanics help " + ChatColor.GRAY + "- This message");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "Features: " + ChatColor.WHITE + "mobs, sculk");
        sender.sendMessage("");
    }

    private String formatState(boolean enabled) {
        return enabled ? ChatColor.GREEN + "ENABLED ✓" : ChatColor.RED + "DISABLED ✗";
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("voicemechanics.admin")) {
            return completions;
        }

        if (args.length == 1) {
            completions.addAll(Arrays.asList("reload", "toggle", "status", "help"));
            return filterCompletions(completions, args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("toggle")) {
            completions.addAll(Arrays.asList("mobs", "sculk"));
            return filterCompletions(completions, args[1]);
        }

        return completions;
    }

    private List<String> filterCompletions(List<String> completions, String input) {
        String lower = input.toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(lower));
        return completions;
    }
}