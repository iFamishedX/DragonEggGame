# Commands

Dragon's Legacy registers a single root command (`/dragonslegacy`, alias `/dl`) with several subcommands. All command names and aliases are configurable in `commands.yaml`.

---

## Root Command

| Form | Description |
|---|---|
| `/dragonslegacy` | Full command name |
| `/dl` | Default alias |

The root command without any subcommand shows the help menu (same as `/dl help`).

---

## Subcommands Overview

| Subcommand | Default Name | Description |
|---|---|---|
| `help` | `/dl help` | Show the command help page |
| `bearer` | `/dl bearer` | Display who currently holds the Dragon Egg |
| `hunger on` | `/dl hunger on` | Activate Dragon's Hunger (bearer only) |
| `hunger off` | `/dl hunger off` | Deactivate Dragon's Hunger (bearer or operator) |
| `reload` | `/dl reload` | Reload all config files |
| `test <key>` | `/dl test <key>` | Send a named message to yourself by its message key |
| `placeholders` | `/dl placeholders` | Print all placeholder values for your current context |

---

## Command Details

### `/dl help`

Displays the help page listing all available commands.

**Usage:**
```
/dl help
```

**Permission:** None required (all players).

---

### `/dl bearer`

Shows the name of the current bearer and, depending on the egg's visibility settings, their approximate or exact location.

**Usage:**
```
/dl bearer
```

**Example output:**
```
[Dragon's Legacy] The current bearer is Steve.
  Egg location: ~120, ~64, ~-340 (Overworld)
  Last seen: 42 seconds ago
```

**Permission:** None required (all players).

**Notes:**
- The location detail shown depends on the `visibility` settings in `egg.yaml`.
- If no one holds the egg, the output reflects the last known egg state (placed, dropped, etc.).

---

### `/dl hunger on`

Activates the Dragon's Hunger ability for the bearer.

**Usage:**
```
/dl hunger on
```

**Permission:** Bearer only (no separate permission node required — only the bearer can activate).

**Notes:**
- The ability cannot be activated if:
  - The caller is not the current bearer.
  - The ability is on cooldown.
  - The ability is already active.
- On activation, the configured effects and attribute modifiers are applied for `duration_ticks` ticks.
- If `block_elytra: true` in `ability.yaml`, the bearer cannot use an Elytra while the ability is active.

---

### `/dl hunger off`

Deactivates the Dragon's Hunger ability early.

**Usage:**
```
/dl hunger off
```

**Permission:** Bearer or server operator.

**Notes:**
- All ability effects and attributes are removed immediately.
- The cooldown timer **starts** from the moment the ability is deactivated, whether naturally or early.

---

### `/dl reload`

Reloads all seven configuration files from disk without restarting the server.

**Usage:**
```
/dl reload
```

**Permission:** Operator (`op`) level required.

**Notes:**
- Messages, effects, attributes, glow colors, and visibility settings all update immediately.
- **Command names and aliases** (`commands.yaml`) do **not** update on reload — a full server restart is required for those changes.
- If a config file has a parse error, the mod keeps the previously loaded version and logs an error.

**Example output:**
```
[Dragon's Legacy] Reloading configuration...
[Dragon's Legacy] Loaded config: egg.yaml (version 1)
[Dragon's Legacy] Loaded config: ability.yaml (version 1)
...
[Dragon's Legacy] Reload complete.
```

---

### `/dl test <key>`

Sends a specific message (by its key in `messages.yaml`) to the command sender. Useful for previewing messages without triggering in-game events.

**Usage:**
```
/dl test <message_key>
```

**Examples:**
```
/dl test help
/dl test ability_start
/dl test bearer_changed
```

**Permission:** Operator (`op`) level required.

**Notes:**
- The message is rendered with all placeholders filled in using the sender's current context.
- The message is sent only to the sender, regardless of its configured `visibility`.
- This is the primary tool for validating your `messages.yaml` edits. See [Troubleshooting](Troubleshooting.md) for a workflow.

---

### `/dl placeholders`

Prints the current resolved value of every `%dragonslegacy:*%` placeholder for the sender's context.

**Usage:**
```
/dl placeholders
```

**Permission:** Operator (`op`) level required.

**Example output:**
```
%dragonslegacy:player%        → Steve
%dragonslegacy:bearer%        → Alex
%dragonslegacy:egg_state%     → held
%dragonslegacy:egg_location%  → 120, 64, -340
%dragonslegacy:seconds%       → 120
%dragonslegacy:online%        → 7
...
```

See [Placeholders](Placeholders.md) for descriptions of each value.

---

## Renaming Commands

All command and subcommand names can be changed in `commands.yaml`:

```yaml
root: "dragon"
aliases:
  - "drg"
  - "dl"
subcommands:
  help: "?"
  bearer: "who"
  hunger: "power"
  hunger_on: "activate"
  hunger_off: "deactivate"
  reload: "reload"
  test: "preview"
  placeholders: "vars"
```

With this config the commands would become `/dragon ?`, `/dragon who`, `/dragon power activate`, etc.

> **Important:** Command name changes require a **full server restart**. They cannot be applied with `/dl reload`.

---

## Permission Summary

| Command | Required Permission |
|---|---|
| `/dl help` | None |
| `/dl bearer` | None |
| `/dl hunger on` | Bearer only |
| `/dl hunger off` | Bearer or operator |
| `/dl reload` | Operator |
| `/dl test <key>` | Operator |
| `/dl placeholders` | Operator |
