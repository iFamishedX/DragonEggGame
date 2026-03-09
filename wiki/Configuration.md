# Configuration

Dragon's Legacy uses **seven YAML configuration files**, each focused on a specific area of the mod. All files live in:

```
config/dragonslegacy/
├── config.yaml      — Master switch and global settings
├── egg.yaml         — Egg tracking, visibility, and protections
├── ability.yaml     — Dragon's Hunger ability settings
├── passive.yaml     — Passive effects while holding the egg
├── glow.yaml        — Glow color and anvil crafting
├── commands.yaml    — Command names and aliases
└── messages.yaml    — All player-facing text
```

After editing any file, run `/dl reload` to apply changes without restarting the server. The mod validates each file on load and prints warnings for unknown or invalid keys.

---

## config.yaml

The top-level toggle for the entire mod.

```yaml
config_version: 1
enabled: true
```

| Key | Type | Default | Description |
|---|---|---|---|
| `config_version` | integer | `1` | Internal version — do not change manually |
| `enabled` | boolean | `true` | Set to `false` to disable all mod functionality without uninstalling |

---

## egg.yaml

Controls how the egg is tracked, where it can appear in the world, and what protections it has.

```yaml
config_version: 1
search_radius: 25
block_ender_chest: true
block_container_items: false
offline_reset_days: 3
nearby_range: 64
visibility:
  INVENTORY: "EXACT"
  ITEM: "EXACT"
  PLAYER: "HIDDEN"
  BLOCK: "RANDOMIZED"
  FALLING_BLOCK: "EXACT"
  ENTITY: "EXACT"
protection:
  void: true
  fire: true
  lava: true
  explosions: true
  cactus: true
  despawn: true
  hopper: true
  portal: true
```

### Top-Level Keys

| Key | Type | Default | Description |
|---|---|---|---|
| `search_radius` | integer | `25` | Block radius searched when the server tries to locate a nearby egg |
| `block_ender_chest` | boolean | `true` | Prevents the bearer from putting the egg into an Ender Chest |
| `block_container_items` | boolean | `false` | Prevents the bearer from using containers while holding the egg |
| `offline_reset_days` | integer | `3` | Days a bearer can be offline before the bearer status is cleared (0 = never reset) |
| `nearby_range` | integer | `64` | Block radius used by commands and placeholders for "nearby" egg detection |

### Visibility Modes

Each egg state context can independently show the egg's location with one of three precision levels.

| Mode | Behavior |
|---|---|
| `EXACT` | Reports the real coordinates of the egg |
| `RANDOMIZED` | Offsets the reported coordinates by a random amount |
| `HIDDEN` | Returns no location information |

| Context Key | When it Applies |
|---|---|
| `INVENTORY` | Egg is in a player's inventory (not necessarily held) |
| `ITEM` | Egg is an item entity on the ground |
| `PLAYER` | Egg is being held by a player (main/off hand) |
| `BLOCK` | Egg is placed as a block in the world |
| `FALLING_BLOCK` | Egg is a falling block entity |
| `ENTITY` | Egg is attached to or riding another entity |

### Protection Flags

| Flag | What It Prevents |
|---|---|
| `void` | Egg is teleported back on falling into the void |
| `fire` | Egg item entity is immune to fire damage |
| `lava` | Egg item entity is immune to lava |
| `explosions` | Egg item entity and placed block survive explosions |
| `cactus` | Egg item entity is immune to cactus damage |
| `despawn` | Egg item entity never despawns naturally |
| `hopper` | Hoppers cannot pick up the egg item |
| `portal` | Egg does not travel through Nether/End portals |

---

## ability.yaml

Configures the **Dragon's Hunger** ability — the active ability the bearer can trigger.

```yaml
config_version: 1
duration_ticks: 6000
cooldown_ticks: 1200
block_elytra: true
scaling:
  enabled: false
  health_multiplier: 0.0
  damage_multiplier: 0.0
  speed_multiplier: 0.0
effects:
  - id: "strength"
    amplifier: 1
    show_particles: true
    show_icon: true
  - id: "speed"
    amplifier: 1
    show_particles: true
    show_icon: true
attributes:
  - id: "max_health"
    amount: 20.0
    operation: "add_value"
  - id: "attack_damage"
    amount: 4.0
    operation: "add_value"
```

| Key | Type | Default | Description |
|---|---|---|---|
| `duration_ticks` | integer | `6000` | How long the ability lasts (6000 = 5 minutes) |
| `cooldown_ticks` | integer | `1200` | Time between uses (1200 = 1 minute) |
| `block_elytra` | boolean | `true` | Prevents the bearer from using an Elytra while the ability is active |

### Scaling

When `scaling.enabled` is `true`, ability values grow based on the number of active (online) players. Each multiplier is applied per additional player beyond the first.

| Key | Description |
|---|---|
| `health_multiplier` | Extra max health added per additional online player |
| `damage_multiplier` | Extra attack damage added per additional online player |
| `speed_multiplier` | Extra speed amplifier (fractional) per additional online player |

### Effects List

Each entry in `effects` applies a vanilla potion effect.

| Field | Description |
|---|---|
| `id` | Minecraft effect ID without namespace (e.g., `strength`, `speed`, `regeneration`) |
| `amplifier` | Level minus one: `0` = Level I, `1` = Level II |
| `show_particles` | Whether the particle cloud is visible around the player |
| `show_icon` | Whether the effect icon shows in the HUD |

### Attributes List

Each entry in `attributes` modifies a player attribute.

| Field | Description |
|---|---|
| `id` | Minecraft attribute ID without namespace (e.g., `max_health`, `attack_damage`) |
| `amount` | Numeric value to add or multiply |
| `operation` | `add_value`, `add_multiplied_base`, or `add_multiplied_total` |

---

## passive.yaml

Configures the always-on bonuses the bearer receives just from holding the egg.

```yaml
config_version: 1
effects:
  - id: "resistance"
    amplifier: 0
    show_particles: false
    show_icon: false
  - id: "saturation"
    amplifier: 0
    show_particles: false
    show_icon: false
attributes:
  - id: "max_health"
    amount: 4.0
    operation: "add_value"
```

The `effects` and `attributes` lists follow the exact same format as in `ability.yaml` (see above). Effects are reapplied every tick the bearer is holding the egg, and attributes are added/removed as the bearer picks up or puts down the egg.

See [Passive Effects](Passive-Effects.md) for a detailed explanation.

---

## glow.yaml

Controls the egg's glowing appearance and the anvil crafting system for changing its color.

```yaml
config_version: 1
glow:
  enabled: true
  color: "#FFFFFF"
  crafting:
    enabled: true
    type: "anvil"
    base_item: "minecraft:dragon_egg"
    materials:
      amethyst_shard: "#AA00FF"
      copper_ingot: "#B87333"
      gold_ingot: "#FFD700"
      iron_ingot: "#D8D8D8"
      netherite_ingot: "#3C2A23"
      quartz: "#E7E7E7"
      redstone: "#FF0000"
      emerald: "#00FF55"
      diamond: "#00FFFF"
```

| Key | Type | Default | Description |
|---|---|---|---|
| `glow.enabled` | boolean | `true` | Enables the glow effect on the egg |
| `glow.color` | hex string | `"#FFFFFF"` | Default glow color before any crafting |
| `crafting.enabled` | boolean | `true` | Enables the anvil color-crafting system |
| `crafting.type` | string | `"anvil"` | Crafting station type (currently only `anvil` is supported) |
| `crafting.base_item` | string | `"minecraft:dragon_egg"` | The item used as the base in the anvil |
| `crafting.materials` | map | *(see above)* | Map of `item_id: "#RRGGBB"` pairs for color recipes |

See [Glow System](Glow-System.md) for the full materials reference and how to add custom entries.

---

## commands.yaml

Controls the names of all commands and subcommands. Rename anything here to fit your server's style.

```yaml
config_version: 1
root: "dragonslegacy"
aliases:
  - "dl"
subcommands:
  help: "help"
  bearer: "bearer"
  hunger: "hunger"
  hunger_on: "on"
  hunger_off: "off"
  reload: "reload"
  test: "test"
  placeholders: "placeholders"
```

| Key | Description |
|---|---|
| `root` | The primary command name (registered as `/dragonslegacy`) |
| `aliases` | List of alternate command names (e.g., `/dl`) |
| `subcommands.*` | Individual subcommand names — rename any of these freely |

> **Note:** Command changes require a **full server restart** to take effect; `/dl reload` is not sufficient because Brigadier registers commands at startup.

---

## messages.yaml

Contains every player-facing text string. See [Messages and Prefixes](Messages-and-Prefixes.md) for the full format documentation.

```yaml
config_version: 1
prefix: ""
messages:
  help:
    order: 0
    cooldown_ticks: 0
    global_cooldown_ticks: 0
    conditions: {}
    channels:
      - mode: "chat"
        visibility: "everyone"
        text: "<gray>Dragon's Legacy help..."
```

Key concepts at a glance:

- All text uses **MiniMessage** format — `<red>`, `<gradient:...>`, `<hover:...>`, etc.
- Each message has one or more **channels** with a `mode` (`chat`, `actionbar`, `title`, `subtitle`, `bossbar`) and a `visibility` (`self`, `everyone`, `bearer`, `non_bearer`).
- `conditions` allows messages to be gated behind placeholder checks.
- `cooldown_ticks` and `global_cooldown_ticks` throttle how often a message fires per-player or server-wide.
