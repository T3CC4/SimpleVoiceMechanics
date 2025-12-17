# SimpleVoiceMechanics

A Spigot/Paper plugin that integrates SimpleVoiceChat with Minecraft's game mechanics. Mobs react to player voice activity based on configurable detection ranges.

## Features

- **Range-based detection system** - No volume detection, purely distance-based with smooth falloff curves
- **Mob categories** - Different behaviors for hostile, neutral, peaceful mobs, and wardens
- **Sculk Sensor integration** - Voice triggers sculk sensors naturally
- **Per-category configuration** - Override detection ranges for each mob type
- **Version-safe** - Works across multiple Minecraft versions without NMS issues
- **Blacklist system** - Exclude specific mobs from detection per category

## Requirements

- Paper 1.21+ (or compatible fork)
- Java 21
- [SimpleVoiceChat](https://modrinth.com/plugin/simple-voice-chat) plugin

## Installation

1. Download SimpleVoiceChat and place it in your plugins folder
2. Download SimpleVoiceMechanics and place it in your plugins folder
3. Start your server
4. Configure the plugin in `plugins/SimpleVoiceMechanics/config.yml`
5. Reload with `/voicelistener reload`

## Mob Behavior

### Hostile Mobs
Zombies, Skeletons, Creepers, etc. will target speaking players within detection range.

### Neutral Mobs
Piglins, Endermen, etc. will look at speaking players but remain passive unless provoked.

### Peaceful Mobs
Cows, Sheep, Pigs, etc. will look at speaking players. When sneaking and talking, they will follow the player for a configurable duration.

### Warden
Special behavior with distance-based anger scaling. Closer voice = more anger.

### Sculk Sensors
Regular and calibrated sculk sensors detect voice activity and activate accordingly.

## Configuration

### Detection System

The plugin uses three parameters per category:

- **min-range** - Distance where detection is guaranteed (100% chance)
- **max-range** - Maximum detection distance (0% chance beyond)
- **falloff-curve** - How detection probability decreases with distance (0.5-2.0)

### Global Settings

```yaml
detection:
  max-range: 16.0      # Default maximum range
  min-range: 2.0       # Default minimum range
  falloff-curve: 1.0   # Default falloff curve
```

### Category Overrides

Each mob category can override global settings:

```yaml
mob-hearing:
  hostile-mobs:
    enabled: true
    max-range: null    # null = use global setting
    min-range: null
    falloff-curve: null
    blacklist:
      - "PHANTOM"
```

### Falloff Curve Behavior

- **0.5** - Linear falloff (50% chance at midpoint)
- **1.0** - Moderate falloff (recommended default)
- **1.5** - Strong falloff (detection stays high until edge)
- **2.0** - Very strong falloff (almost flat until max range)

## Commands

All commands require `voicelistener.admin` permission.

```
/voicelistener reload
  Reload configuration from disk

/voicelistener toggle <category>
  Toggle a mob category or feature
  Categories: hostile, neutral, peaceful, warden, sculk

/voicelistener status
  Display current configuration and detection ranges
```

## Permissions

```yaml
voicelistener.admin
  Description: Access to all plugin commands
  Default: op
```

## Example Configurations

### Stealth Mode (Hard to Detect)
```yaml
detection:
  max-range: 12.0
  min-range: 4.0
  falloff-curve: 0.7
```

### Action Mode (Easy to Detect)
```yaml
detection:
  max-range: 20.0
  min-range: 1.0
  falloff-curve: 1.5
```

### Warden Focused
```yaml
mob-hearing:
  warden:
    max-range: 32.0
    min-range: 4.0
    falloff-curve: 0.5
```

## Debug Options

Enable detailed logging for testing and tuning:

```yaml
debug:
  range-logging: false      # Range calculations
  detection-logging: false  # Mob detection attempts
  warden-logging: false     # Warden anger increases
  sculk-logging: false      # Sculk sensor activations
  peaceful-logging: false   # Peaceful mob reactions
```

## API Usage

### Custom Event Listener

```java
@EventHandler
public void onVoiceDetected(VoiceDetectedEvent event) {
    Player player = event.getPlayer();
    Location location = event.getLocation();
    
    // Your custom logic here
}
```

### Check Mob Categories

```java
EntityType type = mob.getType();

if (MobCategory.isHostile(type)) {
    // Hostile mob logic
}
```

## Building

```bash
git clone https://github.com/yourusername/SimpleVoiceMechanics.git
cd SimpleVoiceMechanics
mvn clean package
```

The compiled JAR will be in `target/SimpleVoiceMechanics-1.0.0.jar`

## Support

For issues, questions, or suggestions:
- Open an issue on GitHub
- Join our Discord (if applicable)

## License

[Your chosen license]

## Credits

- SimpleVoiceChat by henkelmax
- Built for Paper/Spigot servers