# Placeholder System

Dragon's Legacy provides a fully configurable YAML-driven placeholder engine. External placeholders (accessible via PlaceholderAPI as `%dragonslegacy:<name>%`) are defined in `config/dragonslegacy/placeholders.yaml`.

---

## Internal vs External Placeholders

| Type | Where defined | How used |
|------|--------------|----------|
| **Internal variables** | Built into the mod | Inside `format` strings and `if` conditions in `placeholders.yaml` (e.g. `{x}`, `{state}`) |
| **External placeholders** | Defined in `placeholders.yaml` | In any `text` field in `messages.yaml`, or in third-party plugins/mods |

> **Internal variables are never exposed to PlaceholderAPI.** They exist only inside the YAML template engine.

---

## Hardcoded External Placeholders

These are always available regardless of `placeholders.yaml`:

| Placeholder | Description |
|-------------|-------------|
| `%dragonslegacy:global_prefix%` | The `prefix` from `messages.yaml` |
| `%dragonslegacy:player%` | Display name of the event/trigger player |
| `%dragonslegacy:executor%` | Display name of the command executor |
| `%dragonslegacy:executor_uuid%` | UUID of the command executor |
| `%dragonslegacy:bearer%` | Display name of the current egg bearer, or `none` |
| `%dragonslegacy:x%` | Raw X coordinate of the egg |
| `%dragonslegacy:y%` | Raw Y coordinate of the egg |
| `%dragonslegacy:z%` | Raw Z coordinate of the egg |
| `%dragonslegacy:dimension%` | World key (e.g. `minecraft:overworld`) |
| `%dragonslegacy:egg_location%` | Formatted location string |
| `%dragonslegacy:egg_state%` | Current egg state (lower-case) |
| `%dragonslegacy:last_seen%` | Seconds since bearer was last seen (stub: 0) |
| `%dragonslegacy:seconds%` | Alias for `last_seen` |
| `%dragonslegacy:ability_duration%` | Remaining ability ticks |
| `%dragonslegacy:ability_cooldown%` | Remaining cooldown ticks |
| `%dragonslegacy:online%` | Online player count |
| `%dragonslegacy:max_players%` | Server max players |

---

## Config-Driven External Placeholders (placeholders.yaml)

Default placeholders defined in `placeholders.yaml`:

| Placeholder | Description | Visibility-aware |
|-------------|-------------|:---:|
| `%dragonslegacy:xz%` | X and Z (rounded to 50) | ✅ |
| `%dragonslegacy:xyz%` | X, Y, and Z (X/Z rounded to 50, Y to 1) | ✅ |
| `%dragonslegacy:exact-xz%` | Exact X and Z (rounded to 1) | ❌ |
| `%dragonslegacy:exact-xyz%` | Exact X, Y, and Z | ❌ |
| `%dragonslegacy:pretty_location%` | Formatted Unicode coordinates | ✅ |
| `%dragonslegacy:dimension_pretty%` | Human-readable dimension name | ❌ |
| `%dragonslegacy:distance%` | Distance from executor to egg | ❌ |
| `%dragonslegacy:location_json%` | JSON array of coordinates | ✅ |
| `%dragonslegacy:bearer%` | Bearer name or `none` | ❌ |
| `%dragonslegacy:egg_state%` | Egg state (lower-case) | ✅ |

> ✅ = respects visibility rules from `egg.yaml`  
> ❌ = always returns exact values

---

## placeholders.yaml Structure

Each entry in `placeholders.yaml` under `placeholders:` defines one external placeholder.

```yaml
placeholder_name:
  ignore_visibility: false   # true = always exact, false = apply visibility rules

  conditions:                # Optional. Evaluated top-to-bottom; first match wins.
    - if: "{state} == 'HIDDEN'"
      output: "HIDDEN"
    - if: "{state} == 'PLAYER'"
      output: "Carried by {bearer}"

  format: "{round({x},50)} {round({z},50)}"  # Used when no condition matches
```

### Fields

| Field | Type | Description |
|-------|------|-------------|
| `ignore_visibility` | boolean | When `true`, visibility rules are bypassed and exact values are always used |
| `conditions` | list | Ordered condition/output pairs; first match wins |
| `format` | string | Fallback output template when no condition matches |

---

## Internal Variables

These variables are available inside `format` strings and `if` conditions:

| Variable | Description |
|----------|-------------|
| `{x}` `{y}` `{z}` | Egg coordinates (integers) |
| `{dimension}` | World key (e.g. `minecraft:overworld`) |
| `{state}` | Effective state: `PLAYER`, `PLACED`, `DROPPED`, `UNKNOWN`, or `HIDDEN` |
| `{bearer}` | Bearer display name (empty if none) |
| `{bearer_uuid}` | Bearer UUID (empty if none) |
| `{executor}` | Name of the player using the placeholder |
| `{executor_uuid}` | UUID of the player using the placeholder |
| `{last_seen}` `{seconds}` | Seconds since egg was last seen (stub: 0) |
| `{ability_duration}` | Ability ticks remaining |
| `{ability_cooldown}` | Cooldown ticks remaining |
| `{online}` `{max_players}` | Server player counts |
| `{world_time}` `{real_time}` `{tick}` | Time values |
| `{egg_age}` | Egg age (stub: 0) |
| `{bearer_health}` `{bearer_max_health}` | Bearer HP |

---

## Supported Filters

Filters are applied using `{filter_name(args)}` syntax inside format strings. Nested filters are supported.

| Filter | Signature | Description | Example |
|--------|-----------|-------------|---------|
| `upper` | `upper(value)` | Uppercase | `{upper({bearer})}` |
| `lower` | `lower(value)` | Lowercase | `{lower({state})}` |
| `capitalize` | `capitalize(value)` | First letter uppercase | `{capitalize(replace({dimension},'minecraft:',''))}` |
| `round` | `round(value, precision)` | Round to nearest multiple | `{round({x},50)}` → nearest 50 |
| `abs` | `abs(value)` | Absolute value | `{abs({x})}` |
| `format_number` | `format_number(value)` | Add thousands separator | `{format_number({x})}` → `1,234` |
| `default` | `default(value, fallback)` | Return fallback if blank | `{default({bearer},'none')}` |
| `replace` | `replace(value, target, replacement)` | String substitution | `{replace({dimension},'minecraft:','')}` |
| `if` | `if(condition, trueVal, falseVal)` | Inline conditional | `{if({bearer} == '', 'none', {bearer})}` |
| `color` | `color(value)` | Strip MiniMessage tags | `{color(<gold>text</gold>)}` |
| `json` | `json(v1, v2, ...)` | Build JSON array | `{json({x},{y},{z},{dimension})}` |
| `distance_to_player` | `distance_to_player()` | Distance from executor to egg | `{round(distance_to_player(),1)}` |

---

## Condition Evaluation

Conditions in the `conditions` list are evaluated **top-to-bottom**. The **first matching condition** wins. If **no condition matches**, the `format` field is used.

Supported operators: `==` and `!=`

```yaml
conditions:
  - if: "{state} == 'HIDDEN'"
    output: "Location unknown"
  - if: "{state} == 'PLAYER'"
    output: "Carried by a player"
  - if: "{bearer} != ''"
    output: "Bearer: {bearer}"
```

---

## Visibility Rules

The `{state}` variable reflects the **effective visibility** when `ignore_visibility: false`:

| Visibility Config | `{state}` value |
|-------------------|----------------|
| Position type maps to `HIDDEN` | `"HIDDEN"` |
| Position type maps to `EXACT` or `RANDOMIZED` | Physical state (`PLAYER`, `PLACED`, `DROPPED`) |

When `ignore_visibility: true`, `{state}` always reflects the physical state.

Configure visibility per position type in `egg.yaml`:

```yaml
visibility:
  PLAYER: HIDDEN      # Egg carried by player → {state} = "HIDDEN"
  BLOCK: RANDOMIZED   # Egg placed as block → coordinates are rounded
  ITEM: EXACT         # Dropped item → exact coordinates
```

---

## Adding Custom Placeholders

Add entries to `placeholders.yaml` under the `placeholders:` key:

```yaml
placeholders:
  my_custom_location:
    ignore_visibility: false
    conditions:
      - if: "{state} == 'HIDDEN'"
        output: "???"
    format: "Egg is around {round({x},100)}, {round({z},100)}"
```

This becomes available as `%dragonslegacy:my_custom_location%`.

Run `/dl reload` to apply changes without restarting.

---

## How to Reload

```
/dl reload
```

This reloads `placeholders.yaml` (and all other config files) from disk. New placeholder keys are registered automatically.

---

## Migration from Old Placeholders

If you were using the old `%deg:*%` placeholders, here is the mapping:

| Old | New |
|-----|-----|
| `%deg:bearer%` | `%dragonslegacy:bearer%` |
| `%deg:exact_pos%` | `%dragonslegacy:exact-xyz%` |
| `%deg:randomized_pos%` | `%dragonslegacy:xz%` |
| `%deg:pos%` | `%dragonslegacy:xz%` |

> The old `%deg:*%` namespace is still supported for backward compatibility.

---

## Checking Values In-Game

Run `/dl placeholders` to print the current value of every placeholder for your context.
