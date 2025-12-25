# Simple Voice Mechanics

Realistic mob reactions to voice in Minecraft. Uses Simple Voice Chat for dynamic audio detection with volume and distance-based behavior.

## Features

- **Real-time Audio Analysis**: Opus decoding with dB calculation
- **Dynamic Range**: Louder voices heard from further away (40%-220% of base range)
- **Natural Behavior**: Probabilistic reactions, cooldowns, eye contact system
- **4 Mob Categories**: Hostile (attack), Neutral (look), Peaceful (look/flee/follow), Warden (anger system)
- **Sculk Sensors**: GameEvent-based activation on loud voices

### Mob Behavior

| Category | Reaction | Threshold | Special Features |
|----------|----------|-----------|------------------|
| **Hostile** | Attack | -40 dB | Target player |
| **Neutral** | Look (8s) | -35 dB | 60% reaction chance |
| **Peaceful** | Look (10s) | -30 dB | Flee at >-20 dB, Follow when sneaking + eye contact |
| **Warden** | Anger +15-60 | -20 dB | Volume & distance dependent |
| **Sculk** | Activate | -20 dB | Only on loud voices |

**Peaceful Mobs Details:**
- Normal speaking: Look at player
- Loud speaking (>-20 dB): Flee 3 blocks away
- Sneaking + Speaking + Eye contact: Follow for 60s

## Requirements

- Minecraft 1.18+ (Paper or compatible)
- Java 17+
- [Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat) Plugin

**Note:** Sculk Sensors require Minecraft 1.19+

## Installation

1. Install Simple Voice Chat
2. Place SimpleVoiceMechanics JAR in `plugins/` folder
3. Restart server
4. Adjust `plugins/SimpleVoiceMechanics/config.yml` as needed

## Configuration

### Global Settings
```yaml
detection:
  max-range: 16.0
  min-range: 2.0
  falloff-curve: 1.0        # 0.5-2.0 (0.5=linear, 2.0=steep)
```

### Per-Category Settings
```yaml
peaceful-mobs:
  enabled: true
  volume-threshold-db: -30.0
  max-range: 8.0
  natural-behavior:
    reaction-chance: 0.7    # 70% reaction probability
    look-duration: 10       # Look duration in seconds
    reaction-cooldown: 3    # Cooldown between reactions
  flee-behavior:
    enabled: true
    flee-volume-db: -20.0
    flee-distance: 3.0
    flee-duration: 3
  follow-when-sneaking:
    enabled: true
    require-eye-contact: true
    eye-contact-range: 4.0
    eye-contact-memory: 5
    duration: 60
    max-distance: 12.0
  blacklist: []             # Exclude EntityTypes
```

### Volume Scale (dB)

- **-60 dB**: Whispering
- **-40 dB**: Normal speaking (Default Hostile)
- **-30 dB**: Quiet to normal (Default Peaceful)
- **-20 dB**: Loud speaking (Default Warden/Sculk)
- **-10 dB**: Shouting

## Commands

- `/voicelistener reload` - Reload config
- `/voicelistener toggle <hostile|neutral|peaceful|warden|sculk>` - Toggle category
- `/voicelistener status` - Show current config

**Aliases:** `/vl`, `/voicemechanics`

## Permissions

- `voicelistener.admin` (default: op)

## Preset Examples

### Stealth Server
```yaml
hostile-mobs:
  volume-threshold-db: -50.0  # Whispers detected
  max-range: 24.0
  falloff-curve: 0.8
```

### Action Server
```yaml
hostile-mobs:
  volume-threshold-db: -25.0  # Only loud speaking
  max-range: 12.0
  falloff-curve: 1.5
```

### Warden Extreme Challenge
```yaml
warden:
  volume-threshold-db: -60.0  # Hears everything!
  max-range: 32.0
  falloff-curve: 0.5
```

## Debug Options

```yaml
debug:
  audio-logging: false
  range-logging: false
  detection-logging: false
  warden-logging: false
  sculk-logging: false
  peaceful-logging: false
```

## Technical Details

**Detection Flow:**
1. Audio packet received → Decode Opus → Calculate dB
2. Check category threshold → Calculate effective range
3. Probability-based detection → Natural behavior check
4. Execute action (Attack/Look/Flee/Follow)

**Formulas:**
```
Effective Range = Config Range × (1.0 + (dB - Threshold) / 25.0)
Capped: 0.5× to 2.5×

Detection Probability = (1 - normalized_distance)^falloff_curve
100% at ≤ min-range, 0% at ≥ max-range
```

## API Usage

```java
@EventHandler
public void onVoiceDetected(VoiceDetectedEvent event) {
    Player player = event.getPlayer();
    double decibels = event.getDecibels();
    // Custom logic
}

// Access config
ConfigManager config = plugin.getConfigManager();
double threshold = config.getHostileVolumeThresholdDb();
```

## Building

```bash
git clone https://github.com/yourusername/SimpleVoiceMechanics.git
cd SimpleVoiceMechanics
mvn clean package
```

## License

MIT License

## Credits

- Audio calculation adapted from Simple Voice Chat (henkelmax)
- Built for Paper
- SimpleVoiceChat API
