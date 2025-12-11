package de.tecca.simplevoicemechanics.manager;

/**
 * Manages feature toggles for the plugin.
 * Centralized control for enabling/disabling features.
 */
public class FeatureManager {

    private boolean mobHearingEnabled;
    private boolean hostileMobHearingEnabled;
    private boolean wardenHearingEnabled;
    private boolean sculkHearingEnabled;

    public FeatureManager(boolean mobHearing, boolean hostileMobs, boolean warden, boolean sculk) {
        this.mobHearingEnabled = mobHearing;
        this.hostileMobHearingEnabled = hostileMobs;
        this.wardenHearingEnabled = warden;
        this.sculkHearingEnabled = sculk;
    }

    // Mob Hearing
    public boolean isMobHearingEnabled() {
        return mobHearingEnabled;
    }

    public void setMobHearingEnabled(boolean enabled) {
        this.mobHearingEnabled = enabled;
    }

    public boolean isHostileMobHearingEnabled() {
        return hostileMobHearingEnabled && mobHearingEnabled;
    }

    public void setHostileMobHearingEnabled(boolean enabled) {
        this.hostileMobHearingEnabled = enabled;
    }

    public boolean isWardenHearingEnabled() {
        return wardenHearingEnabled && mobHearingEnabled;
    }

    public void setWardenHearingEnabled(boolean enabled) {
        this.wardenHearingEnabled = enabled;
    }

    // Sculk Hearing
    public boolean isSculkHearingEnabled() {
        return sculkHearingEnabled;
    }

    public void setSculkHearingEnabled(boolean enabled) {
        this.sculkHearingEnabled = enabled;
    }

    /**
     * Gets a summary of all feature states.
     *
     * @return Feature status string
     */
    public String getStatusSummary() {
        return String.format(
                "Mob Hearing: %s (Hostile: %s, Warden: %s) | Sculk: %s",
                formatStatus(mobHearingEnabled),
                formatStatus(hostileMobHearingEnabled),
                formatStatus(wardenHearingEnabled),
                formatStatus(sculkHearingEnabled)
        );
    }

    private String formatStatus(boolean enabled) {
        return enabled ? "ON" : "OFF";
    }
}