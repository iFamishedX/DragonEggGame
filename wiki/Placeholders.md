# Placeholders

Dragon's Legacy provides 17 built-in placeholders that can be used in any `text` value within `messages.yaml`, in condition checks, and in the `/dl test` and `/dl placeholders` commands.

All placeholders follow the format:

```
%dragonslegacy:<identifier>%
```

---

## Full Placeholder Reference

| Placeholder | Description | Example Output |
|---|---|---|
| `%dragonslegacy:global_prefix%` | The value of `prefix` from `messages.yaml` | `[Dragon's Legacy] ` |
| `%dragonslegacy:player%` | Display name of the **command sender / event player** | `Steve` |
| `%dragonslegacy:executor%` | Display name of the player who executed the command | `AdminPlayer` |
| `%dragonslegacy:executor_uuid%` | UUID of the player who executed the command | `a1b2c3d4-...` |
| `%dragonslegacy:bearer%` | Display name of the current bearer, or `none` | `Alex` |
| `%dragonslegacy:x%` | X coordinate of the egg's (possibly randomized) location | `120` |
| `%dragonslegacy:y%` | Y coordinate of the egg's (possibly randomized) location | `64` |
| `%dragonslegacy:z%` | Z coordinate of the egg's (possibly randomized) location | `-340` |
| `%dragonslegacy:dimension%` | Dimension key where the egg currently is | `minecraft:overworld` |
| `%dragonslegacy:egg_location%` | Formatted location string `X, Y, Z (dimension)` | `120, 64, -340 (minecraft:overworld)` |
| `%dragonslegacy:egg_state%` | Current egg state | `held` |
| `%dragonslegacy:last_seen%` | Formatted time since the bearer was last online | `2 hours ago` |
| `%dragonslegacy:seconds%` | Seconds since the bearer was last seen (raw number) | `7320` |
| `%dragonslegacy:ability_duration%` | Remaining ability duration in ticks | `3450` |
| `%dragonslegacy:ability_cooldown%` | Remaining cooldown in ticks before ability can be used again | `800` |
| `%dragonslegacy:online%` | Number of players currently online | `12` |
| `%dragonslegacy:max_players%` | Server's maximum player slot count | `20` |

---

## Placeholder Descriptions

### `%dragonslegacy:global_prefix%`

Returns the `prefix` string defined at the top of `messages.yaml`. Use this to prefix every message consistently.

```yaml
prefix: "<dark_gray>[<gradient:#AA00FF:#00FFFF>Dragon's Legacy</gradient>]</dark_gray> "
```

### `%dragonslegacy:player%`

The display name of the player most closely associated with the current event context — typically the player being notified or the one who triggered the event. Use this when you want to address the recipient directly.

### `%dragonslegacy:executor%` and `%dragonslegacy:executor_uuid%`

The player who ran the command that caused this message to fire. In broadcast messages (e.g., a reload confirmation), this is the admin who ran `/dl reload`. The UUID is the raw UUID string and is useful for logging or click-to-message actions.

### `%dragonslegacy:bearer%`

The display name of whoever currently holds the Dragon Egg. Returns `none` (or a configurable fallback) when no bearer is set — useful in conditions:

```yaml
conditions:
  "%dragonslegacy:bearer%": "none"
```

### `%dragonslegacy:x%`, `%dragonslegacy:y%`, `%dragonslegacy:z%`

Individual coordinate components of the egg's location. The values respect the `visibility` mode configured in `egg.yaml` for the current egg state — if visibility is `HIDDEN`, these return empty strings. If `RANDOMIZED`, the coordinates are offset by a random amount.

### `%dragonslegacy:dimension%`

The namespaced dimension key where the egg currently is:
- `minecraft:overworld`
- `minecraft:the_nether`
- `minecraft:the_end`
- Any custom dimension key from a datapack

### `%dragonslegacy:egg_location%`

A pre-formatted convenience string combining X, Y, Z, and dimension. Equivalent to writing `%dragonslegacy:x%, %dragonslegacy:y%, %dragonslegacy:z% (%dragonslegacy:dimension%)`.

### `%dragonslegacy:egg_state%`

One of five string values representing where the egg currently is:

| Value | Meaning |
|---|---|
| `held` | A player is holding the egg in their hand |
| `dropped` | The egg is an item entity on the ground |
| `placed` | The egg is placed as a block |
| `entity` | The egg is attached to or inside a non-player entity |
| `unknown` | The egg's location cannot be determined |

Useful in conditions to show different messages depending on the egg's situation:

```yaml
conditions:
  "%dragonslegacy:egg_state%": "dropped"
```

### `%dragonslegacy:last_seen%`

A human-readable string showing how long ago the bearer was last online, e.g., `5 minutes ago`, `2 hours ago`, `3 days ago`. Returns `never` if the bearer has never been seen, or an empty string if the bearer is currently online.

### `%dragonslegacy:seconds%`

The raw number of seconds since the bearer was last seen. Use this for numeric condition comparisons:

```yaml
conditions:
  "%dragonslegacy:seconds%": ">=86400"
```

This example fires only when the bearer has been offline for at least 24 hours.

### `%dragonslegacy:ability_duration%`

The number of ticks remaining on the active Dragon's Hunger ability. Returns `0` when the ability is not active. Useful in action bar messages for a countdown display:

```yaml
text: "<gold>Dragon's Hunger: %dragonslegacy:ability_duration% ticks remaining</gold>"
```

### `%dragonslegacy:ability_cooldown%`

The number of ticks remaining before the bearer can activate Dragon's Hunger again. Returns `0` when the ability is ready.

### `%dragonslegacy:online%` and `%dragonslegacy:max_players%`

Current and maximum player counts. Useful for dynamic messages and for scaling conditions:

```yaml
text: "<gray>%dragonslegacy:online%/%dragonslegacy:max_players% players online</gray>"
```

---

## Using Placeholders in Conditions

Placeholders can be used as keys in the `conditions` map of a message entry. The condition is checked just before the message fires:

```yaml
conditions:
  "%dragonslegacy:egg_state%": "held"
  "%dragonslegacy:online%": ">=5"
```

Both conditions must be true for the message to send.

---

## Checking Placeholder Values In-Game

Use `/dl placeholders` to print the current value of every placeholder for your own context. This is the fastest way to debug why a condition is not firing as expected.

See [Troubleshooting](Troubleshooting.md) for a full diagnostic workflow.
