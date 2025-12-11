package de.tecca.simplevoicemechanics.listener;

import de.tecca.simplevoicemechanics.SimpleVoiceMechanics;
import de.tecca.simplevoicemechanics.event.VoiceDetectedEvent;
import de.tecca.simplevoicemechanics.manager.ConfigManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Collection;

public class MobListener implements Listener {

    private final SimpleVoiceMechanics plugin;

    public MobListener(SimpleVoiceMechanics plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onVoiceDetected(VoiceDetectedEvent event) {
        if (!plugin.getConfigManager().isMobHearingEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        Location loc = event.getLocation();
        double range = event.getRange();
        float volume = event.getVolume();

        // Gamemode Check - Nur Survival/Adventure
        if (player.getGameMode() != GameMode.SURVIVAL &&
                player.getGameMode() != GameMode.ADVENTURE) {
            return; // Creative/Spectator werden nicht detektiert
        }

        // Minimale Lautstärke für Detection
        double MIN_VOLUME = plugin.getConfigManager().getMinVolumeForDetection();
        if (volume < MIN_VOLUME) {
            return; // Zu leise
        }

        // Alle Entities in Reichweite finden
        Collection<Entity> nearbyEntities = loc.getWorld().getNearbyEntities(loc, range, range, range);

        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof Mob)) {
                continue;
            }

            Mob mob = (Mob) entity;

            // Prüfen ob Mob-Typ aktiviert ist
            if (!shouldMobHear(mob)) {
                continue;
            }

            // Distanz prüfen
            double distance = mob.getLocation().distance(loc);
            if (distance > range) {
                continue;
            }

            // Lautstärke basierend auf Distanz anpassen
            double effectiveVolume = volume * (1.0 - (distance / range));

            if (effectiveVolume < 0.1) {
                continue;
            }

            // Mob auf Spieler aufmerksam machen
            if (mob.getTarget() == null) {
                mob.setTarget(player);
            }

            // Warden spezielle Behandlung
            if (mob instanceof Warden) {
                Warden warden = (Warden) mob;
                warden.increaseAnger(player, (int) (effectiveVolume * 20));
            }
        }
    }

    private boolean shouldMobHear(Mob mob) {
        ConfigManager config = plugin.getConfigManager();

        // Warden immer hören lassen
        if (mob instanceof Warden) {
            return config.isWardenHearingEnabled();
        }

        // Hostile Mobs
        if (mob instanceof Monster) {
            return config.isHostileMobHearingEnabled();
        }

        // Andere Mobs
        return false;
    }
}