# Commands & Permissions

Dragon's Legacy registers a single root command (`/dragonslegacy`, alias `/dl`) with several subcommands. The root command name, aliases, and per-command permissions are configured in `global.yaml`.

---

## Root Command

| Form | Description |
|------|-------------|
| `/dragonslegacy` | Full command name |
| `/dl` | Default alias |

The root command without any subcommand shows the help menu (same as `/dl help`).

---

## Subcommands Overview

| Subcommand | Description | Default Permission |
|-----------|-------------|-------------------|
| `/dl help` | Show the command help page | Everyone |
| `/dl bearer` | Display the current egg bearer | Everyone |
| `/dl hunger on` | Activate Dragon's Hunger (bearer only) | Everyone (bearer-only) |
| `/dl hunger off` | Deactivate Dragon's Hunger | Everyone (bearer-only) |
| `/dl reload` | Reload all config files | Op level 3 |
| `/dl placeholders` | Print all placeholder values | Everyone |
| `/dl debug` | Toggle real-time debug HUD | Op level 3 |
| `/dl info` | Show full egg/ability status | Op level 4 |
| `/dl setbearer <player>` | Force-assign the egg bearer | Op level 4 |
| `/dl clearability` | Forcefully deactivate Dragon's Hunger | Op level 4 |
| `/dl resetcooldown` | Reset the ability cooldown | Op level 4 |

---

## Command Details

### `/dl help`

Displays the help page listing all available commands.

**Permission:** `commands.help` (default: everyone)

---

### `/dl bearer`

Shows the name of the current bearer.

**Permission:** `commands.bearer` (default: everyone)

---

### `/dl hunger on` / `/dl hunger off`

Activates or deactivates the Dragon's Hunger ability.

**Notes:**
- Only the current bearer can use these commands (non-bearers receive the `not_bearer` message).
- Activation fails if the ability is already active or on cooldown.
- If `block_elytra: true` in `ability.yaml`, the bearer cannot glide while the ability is active.

**Permission:** `commands.hunger` (default: everyone — bearer enforcement is built-in)

---

### `/dl reload`

Reloads all configuration files from disk without restarting the server.

**Notes:**
- All config files are reloaded: `global.yaml`, `egg.yaml`, `ability.yaml`, `passive.yaml`, `infusion.yaml`, `messages.yaml`, `logging.yaml`, and `placeholders.yaml`.
- New placeholder keys from `placeholders.yaml` are registered automatically.
- Command names and aliases in `global.yaml` do **not** take effect until a server restart.
- If a config file has a parse error, the previous values are kept and an error is logged.

**Permission:** `commands.reload` (default: op level 3)

---

### `/dl placeholders`

Prints the current resolved value of every `%dragonslegacy:*%` placeholder for the sender.

**Permission:** `commands.placeholders` (default: everyone)

---

### `/dl debug`

Toggles debug mode ON/OFF for the executing admin.

**Behavior when ON:**
- An action-bar message is sent every 5 ticks (configurable in `messages.yaml` under `debug_actionbar`).
- The action-bar shows: bearer name, egg state, exact XYZ coordinates.
- Visibility rules are bypassed — exact coordinates are always shown regardless of `egg.yaml` settings.
- The debug status is automatically cleared when the admin disconnects.

**Behavior when OFF:**
- The action-bar stops updating.
- Visibility rules return to normal.

**Permission:** `commands.debug` (default: op level 3)

> Debug mode is per-player. Multiple admins can have it enabled simultaneously.

---

### `/dl info`

Displays full internal status: bearer, egg state, location, ability state, and remaining timers.

**Permission:** `deg.admin.dragonslegacy.info` *(legacy)* (op level 4)

---

### `/dl setbearer <player>`

Force-assigns a player as the new egg bearer. The egg is removed from its current location and given to the target player.

**Permission:** `deg.admin.dragonslegacy.setbearer` *(legacy)* (op level 4)

---

### `/dl clearability`

Forcefully deactivates Dragon's Hunger and resets the cooldown.

**Permission:** `deg.admin.dragonslegacy.clearability` *(legacy)* (op level 4)

---

### `/dl resetcooldown`

Resets the ability cooldown immediately.

**Permission:** `deg.admin.dragonslegacy.resetcooldown` *(legacy)* (op level 4)

---

## Permission Configuration

All public command permissions are configured in `global.yaml` under `commands`:

```yaml
permissions_api: true

commands:
  root: "dragonslegacy"
  aliases:
    - "dl"

  help:
    permission_node: "dragonslegacy.command.help"
    op_level: 0

  reload:
    permission_node: "dragonslegacy.command.reload"
    op_level: 3

  debug:
    permission_node: "dragonslegacy.admin.debug"
    op_level: 3
```

- When `permissions_api: true` → `permission_node` is checked (LuckPerms or similar).
- When `permissions_api: false` → `op_level` is used (vanilla operator levels).

---

## Permission Summary

| Command | LuckPerms Node | Default Op Level |
|---------|---------------|:---:|
| `/dl help` | `dragonslegacy.command.help` | 0 |
| `/dl bearer` | `dragonslegacy.command.bearer` | 0 |
| `/dl hunger` | `dragonslegacy.command.hunger` | 0 |
| `/dl reload` | `dragonslegacy.command.reload` | 3 |
| `/dl placeholders` | `dragonslegacy.command.placeholders` | 0 |
| `/dl debug` | `dragonslegacy.admin.debug` | 3 |
| `/dl info` | `deg.admin.dragonslegacy.info` *(legacy)* | 4 |
| `/dl setbearer` | `deg.admin.dragonslegacy.setbearer` *(legacy)* | 4 |
| `/dl clearability` | `deg.admin.dragonslegacy.clearability` *(legacy)* | 4 |
| `/dl resetcooldown` | `deg.admin.dragonslegacy.resetcooldown` *(legacy)* | 4 |

---

## Troubleshooting

**Commands don't appear in tab-complete**
- Ensure the server has started fully. Commands are registered during the `ServerStarted` lifecycle event.
- Check `server.properties` — the server must be in survival mode or have cheats enabled.

**`/dl` alias doesn't work**
- Confirm `aliases` in `global.yaml` contains `"dl"`.
- Command name changes require a full server restart to take effect.

**Permission denied even as op**
- If `permissions_api: true` and you're using LuckPerms, grant the node explicitly: `lp user <name> permission set dragonslegacy.command.reload true`
- To use vanilla op levels instead, set `permissions_api: false` in `global.yaml` and reload.
