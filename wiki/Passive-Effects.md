# Passive Effects

While the bearer is **holding the Dragon Egg** (in their main hand or off-hand), they continuously receive a set of passive bonuses. Unlike Dragon's Hunger, these bonuses require no activation — they apply automatically and disappear the moment the egg leaves the bearer's hand.

---

## Configuration File

```
config/dragonslegacy/passive.yaml
```

Default configuration:

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

---

## Default Passive Bonuses

| Bonus | Value |
|---|---|
| **Resistance** | Level I (amplifier 0) — reduces all incoming damage by ~20% |
| **Saturation** | Level I (amplifier 0) — passively restores food and saturation |
| **Max Health** | +4 HP (2 hearts) |

These values are applied as long as the bearer holds the egg. The moment they swap to a different item or drop the egg, the attribute modifier is removed and the effects are no longer refreshed (they expire naturally after their last application's duration elapses).

---

## How Passive Effects Are Applied

Effects from `passive.yaml` are reapplied on every tick (or at a high frequency) while the bearer is holding the egg. The effect duration is set just long enough to ensure seamless coverage — the player will never see the effect icon flicker.

Attributes are applied the instant the bearer picks up or holds the egg, and removed the instant they stop. This is a clean add/remove cycle.

---

## Effects List Format

Each entry in `effects` applies a vanilla status effect continuously while the egg is held.

```yaml
effects:
  - id: "resistance"
    amplifier: 0
    show_particles: false
    show_icon: false
```

| Field | Description |
|---|---|
| `id` | Minecraft effect ID (no namespace needed). See list below |
| `amplifier` | Level minus one: `0` = Level I, `1` = Level II, etc. |
| `show_particles` | Whether the particle cloud is visible. Typically `false` for passive effects |
| `show_icon` | Whether the effect icon appears in the HUD |

Setting `show_particles: false` and `show_icon: false` makes the passive bonuses invisible to other players and uncluttered for the bearer. This is the default intentionally — the bearer benefits subtly without being an obvious visual target.

---

## Attributes List Format

Each entry in `attributes` applies a persistent attribute modifier while the egg is held.

```yaml
attributes:
  - id: "max_health"
    amount: 4.0
    operation: "add_value"
```

| Field | Description |
|---|---|
| `id` | Minecraft attribute ID (no namespace). See list below |
| `amount` | Numeric value to apply |
| `operation` | `add_value`, `add_multiplied_base`, or `add_multiplied_total` |

See [Ability System — Attribute Operations](Ability-System.md#attribute-operations) for a description of each operation mode.

---

## Adding Custom Passive Effects

You can add any number of effects and attributes. For example, to also give the bearer Night Vision while holding the egg:

```yaml
effects:
  - id: "resistance"
    amplifier: 0
    show_particles: false
    show_icon: false
  - id: "saturation"
    amplifier: 0
    show_particles: false
    show_icon: false
  - id: "night_vision"
    amplifier: 0
    show_particles: false
    show_icon: true
```

Or to increase movement speed passively:

```yaml
attributes:
  - id: "max_health"
    amount: 4.0
    operation: "add_value"
  - id: "movement_speed"
    amount: 0.05
    operation: "add_value"
```

---

## Commonly Used Effect IDs

| ID | Effect |
|---|---|
| `resistance` | Reduces damage taken |
| `saturation` | Restores food / saturation |
| `night_vision` | See in the dark |
| `haste` | Faster mining and attack speed |
| `regeneration` | Restores health over time |
| `fire_resistance` | Immunity to fire and lava damage |
| `water_breathing` | Breathe underwater |
| `slow_falling` | Reduced fall speed and no fall damage |
| `absorption` | Extra temporary health |
| `glowing` | Visible through walls (use cautiously) |

---

## Commonly Used Attribute IDs

| ID | Attribute |
|---|---|
| `max_health` | Maximum HP |
| `attack_damage` | Melee damage dealt |
| `movement_speed` | Walk/run speed |
| `attack_speed` | Attack swing speed |
| `armor` | Flat armor value |
| `armor_toughness` | Armor penetration resistance |
| `knockback_resistance` | Resistance to knockback |

---

## Interaction with Dragon's Hunger

When Dragon's Hunger is active, **both** the passive bonuses and the ability bonuses apply simultaneously. The max health from passive (`+4 HP`) and the max health from the ability (`+20 HP`) stack, giving the bearer a combined `+24 HP` (12 hearts) over their base max health while the ability is running and the egg is held.

If the bearer stops holding the egg while the ability is active, the passive attribute bonus is removed but the ability's effects and attribute modifiers remain until the ability expires or is cancelled.
