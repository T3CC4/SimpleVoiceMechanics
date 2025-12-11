package de.tecca.simplevoicemechanics.listener;

import de.tecca.simplevoicemechanics.SimpleVoiceMechanics;
import de.tecca.simplevoicemechanics.event.VoiceDetectedEvent;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_21_R6.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.craftbukkit.v1_21_R6.CraftWorld;
import net.minecraft.core.BlockPosition;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SculkListener implements Listener {

    private final SimpleVoiceMechanics plugin;
    private final Map<Location, Long> lastTriggerTime = new HashMap<>();
    private final long COOLDOWN = 1000; // 1 Sekunde Cooldown (wie normale Sculk Sensors)

    public SculkListener(SimpleVoiceMechanics plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onVoiceDetected(VoiceDetectedEvent event) {
        if (!plugin.getConfigManager().isSculkHearingEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        Location loc = event.getLocation();
        double range = event.getRange();
        float volume = event.getVolume();

        // Nach Sculk Sensors in der Nähe suchen
        int searchRadius = (int) Math.ceil(range);
        Location playerLoc = player.getLocation();

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius; y <= searchRadius; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    Block block = playerLoc.clone().add(x, y, z).getBlock();
                    Material type = block.getType();

                    // Nur Sculk Sensors prüfen (normale + calibrated)
                    if (type != Material.SCULK_SENSOR && type != Material.CALIBRATED_SCULK_SENSOR) {
                        continue;
                    }

                    Location sensorLoc = block.getLocation();
                    double distance = sensorLoc.distance(loc);

                    if (distance > range) {
                        continue;
                    }

                    // Cooldown prüfen
                    Long lastTrigger = lastTriggerTime.get(sensorLoc);
                    long currentTime = System.currentTimeMillis();
                    if (lastTrigger != null && (currentTime - lastTrigger) < COOLDOWN) {
                        continue;
                    }

                    // Lautstärke basierend auf Distanz
                    double effectiveVolume = volume * (1.0 - (distance / range));

                    if (effectiveVolume < 0.1) {
                        continue;
                    }

                    // Sculk Sensor RICHTIG aktivieren mit Game Event!
                    triggerSculkSensor(block, player, volume);
                    lastTriggerTime.put(sensorLoc, currentTime);
                }
            }
        }

        // Alte Einträge aufräumen
        cleanupOldTriggers();
    }

    private void triggerSculkSensor(Block block, Player player, double volume) {
        // Gamemode Check
        if (player.getGameMode() != GameMode.SURVIVAL &&
                player.getGameMode() != GameMode.ADVENTURE) {
            return;
        }

        // Lautstärke Check
        double MIN_VOLUME = plugin.getConfigManager().getMinVolumeForDetection();
        if (volume < MIN_VOLUME) {
            return;
        }

        // NMS Code
        net.minecraft.server.level.WorldServer level = ((CraftWorld) block.getWorld()).getHandle();
        BlockPosition pos = new BlockPosition(block.getX(), block.getY(), block.getZ());

        // Immer dasselbe Event (STEP) - Sensor detectet alle Events gleich
        net.minecraft.core.Holder<net.minecraft.world.level.gameevent.GameEvent> gameEventHolder =
                net.minecraft.world.level.gameevent.GameEvent.P; // STEP

        EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        level.a(nmsPlayer, gameEventHolder, pos);

        plugin.getLogger().fine("Sculk Sensor aktiviert bei " + block.getLocation());
    }

    private void cleanupOldTriggers() {
        long currentTime = System.currentTimeMillis();
        lastTriggerTime.entrySet().removeIf(entry ->
                (currentTime - entry.getValue()) > COOLDOWN * 10
        );
    }
}