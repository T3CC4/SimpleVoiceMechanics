# SimpleVoiceMechanics

Advanced voice detection mechanics for Minecraft servers using Simple Voice Chat. Adds realistic mob reactions, sculk sensor activation, and natural behavior based on voice volume and proximity.

## Features

### Voice Detection System
- **Real-time audio analysis**: Decodes Opus audio packets to calculate decibel levels
- **Per-category thresholds**: Each mob type and sculk sensors have independent volume requirements
- **Dynamic range scaling**: Louder voices are heard from further away
- **Natural behavior**: Mobs don't always react (configurable reaction chance)
- **Reaction cooldowns**: Prevents rapid re-triggering for realistic behavior

### Mob Categories

**Hostile Mobs** (Attack on detection)
- Zombies, Skeletons, Creepers, Spiders, etc.
- Target and attack players when hearing voice
- Configurable volume threshold and detection range

**Neutral Mobs** (Look at player)
- Endermen, Wolves, Piglins, Iron Golems, etc.
- Turn to look at speaking players
- Configurable look duration (default: 8 seconds)
- Natural reaction chance (60%)

**Peaceful Mobs** (Look, flee, or follow)
- Cows, Sheep, Pigs, Chickens, etc.
- Look at player when hearing normal voice
- **Flee behavior**: Run away when frightened by loud noise (>-20 dB)
- **Follow mechanic**: Follow player when sneaking + talking
    - Requires eye contact first (configurable)
    - Player must look at mob within 4 blocks
    - Mob remembers eye contact for 5 seconds
- Configurable look duration (default: 10 seconds)
- Natural reaction chance (70%)

**Warden** (Special anger mechanics)
- Requires loud speaking (-20 dB threshold)
- Increases anger based on volume and distance
- Louder voices = more anger
- Closer distance = more anger

### Sculk Sensor Detection
- Triggers sculk sensors (regular and calibrated) via GameEvent API
- Volume-based activation threshold (-20 dB default - loud speaking)
- Dynamic range: Louder voices trigger sensors from further away
- Configurable cooldown between activations
- Only triggers when volume threshold is met (realistic behavior)

### Dynamic Range System
- Voice volume affects detection range multiplicatively
- **Whispering** (-55 dB): ~40% of configured range
- **Normal speaking** (-35 dB): ~100% of configured range
- **Loud speaking** (-25 dB): ~160% of configured range
- **Shouting** (-10 dB): ~220% of configured range

### Natural Behavior Features
- **Reaction chance**: Not all mobs react every time (configurable per category)
- **Reaction cooldowns**: 3 seconds between reactions (prevents spam)
- **Look duration**: Mobs look at player for extended periods (8-10 seconds)
- **Eye contact system**: Peaceful mobs remember when player looks at them
- **Flee mechanics**: Scared mobs pathfind away for realistic escape behavior

## Requirements

- Minecraft 1.18+ (Paper or compatible fork)
- Java 17+
- [Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat) plugin

**Note:** Sculk Sensor detection requires Minecraft 1.19+. On older versions, only mob detection will work.

## Installation

1. Install [Simple Voice Chat](https://modrinth.com/plugin/simple-voice-chat) on your server
2. Download SimpleVoiceMechanics
3. Place the JAR file in your `plugins` folder
4. Restart the server
5. Configure `plugins/SimpleVoiceMechanics/config.yml` to your preferences

## Mob Behavior

### Hostile Mobs
When a player speaks within range:
- Mob calculates detection probability based on distance and volume
- If detected, mob targets and attacks the player
- Blacklist available for specific mobs

### Neutral Mobs
When a player speaks within range:
- 60% chance to react (configurable)
- Looks at player for 8 seconds
- 3 second cooldown before next reaction

### Peaceful Mobs
**Normal voice** (-30 to -40 dB):
- 70% chance to react (configurable)
- Looks at player for 10 seconds
- Tracks eye contact if player looks back

**Loud voice** (>-20 dB):
- Mob flees 3 blocks away from player
- Runs away for 3 seconds
- Overrides normal reactions

**Sneaking + talking with eye contact**:
- Player must look at mob first (within 4 blocks)
- Mob remembers eye contact for 5 seconds
- When player sneaks and talks, mob follows
- Follows for 60 seconds or until 12 blocks away

### Warden
- Requires loud speaking (-20 dB threshold)
- Increases anger based on:
    - Voice volume (louder = more anger)
    - Distance (closer = more anger)
- Anger range: 15-60 points per detection

## Configuration

### Global Detection Settings
```yaml
detection:
  max-range: 16.0           # Maximum detection range
  min-range: 2.0            # Guaranteed detection range
  falloff-curve: 1.0        # Detection falloff (0.5-2.0)
```

### Per-Category Configuration
Each category supports:
- `volume-threshold-db`: Minimum volume in decibels
- `max-range`, `min-range`, `falloff-curve`: Override global settings
- `blacklist`: List of EntityTypes to exclude
- `natural-behavior`: Reaction chance, look duration, cooldown

### Volume Thresholds (Decibels)
Default values in config.yml:
- `-40 dB`: Hostile mobs (normal speaking)
- `-35 dB`: Neutral mobs (slightly more sensitive)
- `-30 dB`: Peaceful mobs (sensitive, easily scared)
- `-20 dB`: Warden & Sculk sensors (loud speaking required)
- `-20 dB`: Flee threshold for peaceful mobs

Sensitivity scale:
- `-60 dB`: Very sensitive (detects whispers)
- `-50 dB`: Extremely sensitive (hears almost everything)
- `-40 dB`: Balanced (normal speaking)
- `-30 dB`: More sensitive (quiet speaking)
- `-20 dB`: Less sensitive (loud speaking only)

### Example: Peaceful Mobs
```yaml
peaceful-mobs:
  enabled: true
  volume-threshold-db: -30.0
  max-range: 8.0
  natural-behavior:
    reaction-chance: 0.7      # 70% chance to react
    look-duration: 10         # Look for 10 seconds
    reaction-cooldown: 3      # 3 seconds between reactions
  flee-behavior:
    enabled: true
    flee-volume-db: -20.0     # Flee when voice exceeds -20 dB
    flee-distance: 3.0        # Run 3 blocks away
    flee-duration: 3          # Flee for 3 seconds
  follow-when-sneaking:
    enabled: true
    require-eye-contact: true
    eye-contact-range: 4.0
    eye-contact-memory: 5     # Seconds
    duration: 60              # Follow for 60 seconds
    max-distance: 12.0
```

## Commands

- `/voicelistener reload` - Reload configuration
- `/voicelistener toggle <hostile|neutral|peaceful|warden|sculk>` - Toggle category
- `/voicelistener status` - Show current configuration

**Aliases:** `/vl`, `/voicemechanics`

## Permissions

- `voicelistener.admin` (default: op) - Access to all commands

## Configuration Examples

### Stealth Server
Players must be very quiet to avoid detection:
```yaml
hostile-mobs:
  volume-threshold-db: -50.0  # Detect whispers
  max-range: 24.0             # Hear from far away
  falloff-curve: 0.8          # Less falloff
```

### Action Server
Only loud voices trigger mobs:
```yaml
hostile-mobs:
  volume-threshold-db: -25.0  # Only loud speaking
  max-range: 12.0             # Shorter range
  falloff-curve: 1.5          # Strong falloff
```

### Warden Challenge
Maximum difficulty (make Warden extremely sensitive):
```yaml
warden:
  volume-threshold-db: -60.0  # Hears everything (whispers too!)
  max-range: 32.0             # Massive range
  falloff-curve: 0.5          # Linear falloff
```

**Note:** Default Warden threshold is `-20.0` dB (loud speaking). Adjust to `-50.0` or `-60.0` for extreme sensitivity.

## Debug Options

Enable detailed logging in `config.yml`:
```yaml
debug:
  audio-logging: false      # Audio level calculations
  range-logging: false      # Range calculations
  detection-logging: false  # Mob reactions
  warden-logging: false     # Warden anger
  sculk-logging: false      # Sculk activations
  peaceful-logging: false   # Peaceful mob behavior
```

## How It Works

### Detection Flow
1. Player speaks into microphone
2. Simple Voice Chat sends Opus-encoded audio packet
3. Plugin decodes audio and calculates decibel level
4. Each category checks its own volume threshold
5. If threshold met, calculate effective range based on volume
6. Probability-based detection within effective range
7. Natural behavior checks (reaction chance, cooldown)
8. Action executed (attack, look, flee, or follow)

### Dynamic Range Formula
```
Effective Range = Configured Range × Volume Multiplier

Volume Multiplier = 1.0 + (dB - Threshold) / 25.0
Capped between 0.5× and 2.5×
```

### Detection Probability
```
if distance ≤ min-range: 100%
if distance ≥ effective-max-range: 0%
else: (1 - normalized_distance)^falloff_curve
```

## API Usage

### Listen for Voice Detection
```java
@EventHandler
public void onVoiceDetected(VoiceDetectedEvent event) {
    Player player = event.getPlayer();
    Location location = event.getLocation();
    double decibels = event.getDecibels();
    
    // Your custom logic
}
```

### Access Configuration
```java
SimpleVoiceMechanics plugin = (SimpleVoiceMechanics) Bukkit.getPluginManager().getPlugin("SimpleVoiceMechanics");
ConfigManager config = plugin.getConfigManager();

double hostileThreshold = config.getHostileVolumeThresholdDb();
double wardenRange = config.getWardenMaxRange();
```

## Building from Source

```bash
git clone https://github.com/yourusername/SimpleVoiceMechanics.git
cd SimpleVoiceMechanics
mvn clean package
```

The compiled JAR will be in `target/SimpleVoiceMechanics-{version}.jar`

## Technical Details

- **Audio Processing**: Opus decoder converts compressed audio to PCM samples
- **Volume Calculation**: RMS (Root Mean Square) amplitude converted to decibels
- **Range System**: Dynamic range scaling based on logarithmic volume scale
- **Natural Behavior**: Reaction cooldowns and probabilistic detection
- **Memory Management**: Automatic cleanup of cooldowns and tracking data
- **Thread Safety**: Async audio processing with sync game logic execution
- **Performance**: Efficient entity filtering and cooldown management

## License

This project is licensed under the MIT License.

## Credits

- Audio level calculation adapted from SimpleVoiceChat by henkelmax
- Built for Paper/Spigot servers
- Uses SimpleVoiceChat API for voice packet processing