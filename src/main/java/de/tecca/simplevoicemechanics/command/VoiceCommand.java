package de.tecca.simplevoicemechanics.command;

import de.tecca.simplevoicemechanics.SimpleVoiceMechanics;
import de.tecca.simplevoicemechanics.manager.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class VoiceCommand implements CommandExecutor {

    private final SimpleVoiceMechanics plugin;

    public VoiceCommand(SimpleVoiceMechanics plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("voicelistener.admin")) {
            sender.sendMessage(ChatColor.RED + "Keine Berechtigung!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.getConfigManager().reload();
                sender.sendMessage(ChatColor.GREEN + "Config neu geladen!");
                break;

            case "toggle":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Verwendung: /voicelistener toggle <mobs|sculk>");
                    return true;
                }
                toggleFeature(sender, args[1]);
                break;

            case "status":
                showStatus(sender);
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== SimpleVoiceListener ===");
        sender.sendMessage(ChatColor.YELLOW + "/voicelistener reload " + ChatColor.GRAY + "- Config neu laden");
        sender.sendMessage(ChatColor.YELLOW + "/voicelistener toggle <mobs|sculk> " + ChatColor.GRAY + "- Feature aktivieren/deaktivieren");
        sender.sendMessage(ChatColor.YELLOW + "/voicelistener status " + ChatColor.GRAY + "- Status anzeigen");
    }

    private void toggleFeature(CommandSender sender, String feature) {
        ConfigManager config = plugin.getConfigManager();

        switch (feature.toLowerCase()) {
            case "mobs":
                boolean newMobState = !config.isMobHearingEnabled();
                config.setMobHearingEnabled(newMobState);
                sender.sendMessage(ChatColor.GREEN + "Mob-Hören: " +
                        (newMobState ? ChatColor.GREEN + "aktiviert" : ChatColor.RED + "deaktiviert"));
                break;

            case "sculk":
                boolean newSculkState = !config.isSculkHearingEnabled();
                config.setSculkHearingEnabled(newSculkState);
                sender.sendMessage(ChatColor.GREEN + "Sculk-Hören: " +
                        (newSculkState ? ChatColor.GREEN + "aktiviert" : ChatColor.RED + "deaktiviert"));
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unbekanntes Feature: " + feature);
                sender.sendMessage(ChatColor.GRAY + "Verfügbar: mobs, sculk");
                break;
        }
    }

    private void showStatus(CommandSender sender) {
        ConfigManager config = plugin.getConfigManager();

        sender.sendMessage(ChatColor.GOLD + "=== Status ===");
        sender.sendMessage(formatStatus("Mob-Hören", config.isMobHearingEnabled()));
        sender.sendMessage(formatStatus("  Hostile Mobs", config.isHostileMobHearingEnabled()));
        sender.sendMessage(formatStatus("  Warden", config.isWardenHearingEnabled()));
        sender.sendMessage(formatStatus("Sculk-Hören", config.isSculkHearingEnabled()));
        sender.sendMessage(ChatColor.YELLOW + "Reichweite: " + ChatColor.WHITE + config.getHearingRange() + " Blöcke");
        sender.sendMessage(ChatColor.YELLOW + "Lautstärke-Schwelle: " + ChatColor.WHITE + config.getVolumeThreshold());
    }

    private String formatStatus(String name, boolean enabled) {
        return ChatColor.YELLOW + name + ": " +
                (enabled ? ChatColor.GREEN + "✓" : ChatColor.RED + "✗");
    }
}