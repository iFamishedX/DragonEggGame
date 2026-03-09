# Ability System

The **Dragon's Hunger** is Dragon's Legacy's central active ability. While the bearer holds the Dragon Egg, they can activate it to receive a dramatic power boost for a configurable duration — after which a cooldown period begins before they can use it again.

---

## Configuration File

```
config/dragonslegacy/ability.yaml
```

Default configuration:

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

---

## Default Ability Values

| Stat | Value |
|---|---|
| **Duration** | 6000 ticks (5 minutes) |
| **Cooldown** | 1200 ticks (1 minute) |
| **Strength** | Level II (amplifier 1) |
| **Speed** | Level II (amplifier 1) |
| **Max Health bonus** | +20 HP (10 hearts) |
| **Attack Damage bonus** | +4 damage |

---

## Activating and Deactivating

### Activation

The bearer activates the ability with:

```
/dl hunger on
```

Activation is blocked if:
1. The command sender is not the current bearer.
2. The ability is already active.
3. The ability is currently on cooldown.
4. The bearer is not holding the egg in their hand at the moment of activation.

### Deactivation

The ability ends naturally after `duration_ticks` ticks. It can also be ended early:

```
/dl hunger off
```

The cooldown timer begins at the moment of deactivation — whether natural or manual.

---

## Effects

Each entry in `ability.yaml`'s `effects` list applies a vanilla potion status effect for the full duration of the ability.

```yaml
effects:
  - id: "strength"
    amplifier: 1       # 0 = Level I, 1 = Level II, 2 = Level III, ...
    show_particles: true
    show_icon: true
```

| Field | Description |
|---|---|
| `id` | Minecraft effect ID (no namespace required, e.g., `strength`, `regeneration`, `haste`) |
| `amplifier` | Effect level minus one. `0` = Level I, `1` = Level II |
| `show_particles` | Show the particle cloud around the player. Set to `false` for a more subtle look |
| `show_icon` | Show the effect icon in the HUD |

You can add as many effects as you want. For example, to also add Regeneration II:

```yaml
  - id: "regeneration"
    amplifier: 1
    show_particles: false
    show_icon: true
```

---

## Attributes

Attribute modifiers are applied on top of effects, directly modifying the player's attribute values for the duration.

```yaml
attributes:
  - id: "max_health"
    amount: 20.0
    operation: "add_value"
  - id: "attack_damage"
    amount: 4.0
    operation: "add_value"
```

| Field | Description |
|---|---|
| `id` | Minecraft attribute ID without namespace (e.g., `max_health`, `attack_damage`, `movement_speed`) |
| `amount` | Numeric amount to apply |
| `operation` | How `amount` is applied (see below) |

### Attribute Operations

| Operation | Description |
|---|---|
| `add_value` | Adds `amount` directly to the base attribute value |
| `add_multiplied_base` | Adds `amount × base_value` to the attribute |
| `add_multiplied_total` | Multiplies the current total by `(1 + amount)` |

**Example — +10 hearts (20 HP) using `add_value`:**
```yaml
- id: "max_health"
  amount: 20.0
  operation: "add_value"
```

**Example — 50% more movement speed using `add_multiplied_base`:**
```yaml
- id: "movement_speed"
  amount: 0.5
  operation: "add_multiplied_base"
```

---

## Elytra Blocking

When `block_elytra: true`, the bearer cannot activate or continue using an Elytra while Dragon's Hunger is active.

```yaml
block_elytra: true
```

This prevents the bearer from combining extreme flight mobility with the ability's combat bonuses. Set to `false` if your server design permits it.

---

## Scaling

The scaling system allows the ability's power to grow dynamically based on how many players are currently online, making the bearer increasingly formidable (and increasingly challenging to defeat) as the server fills up.

```yaml
scaling:
  enabled: true
  health_multiplier: 1.0
  damage_multiplier: 0.5
  speed_multiplier: 0.0
```

When enabled, the total bonus is calculated as:

```
total_health_bonus = base_health + (health_multiplier × (online_players - 1))
total_damage_bonus = base_damage + (damage_multiplier × (online_players - 1))
```

The `-1` means scaling starts from the second player. With 1 player online, no scaling bonus is applied; with 5 players online, the multiplier is applied 4 times.

### Example

Config:
```yaml
scaling:
  enabled: true
  health_multiplier: 2.0
  damage_multiplier: 1.0
```

With 5 players online (4 additional beyond the first):

| Stat | Base | Scaling | Total |
|---|---|---|---|
| Max Health bonus | 20 HP | +8 HP (4 × 2.0) | 28 HP |
| Attack Damage bonus | 4 | +4 (4 × 1.0) | 8 |

---

## Persistence

The ability state **does not persist** across server restarts by default. If the server stops while the ability is active, the ability ends and the cooldown is reset when the server comes back up.

The bearer themselves is persisted — see [Persistence and States](Persistence-and-States.md) for details.

---

## Tick Reference

| Ticks | Real Time |
|---|---|
| 20 | 1 second |
| 200 | 10 seconds |
| 600 | 30 seconds |
| 1200 | 1 minute |
| 6000 | 5 minutes |
| 12000 | 10 minutes |
| 72000 | 1 hour |
