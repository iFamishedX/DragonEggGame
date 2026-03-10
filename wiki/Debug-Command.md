# Debug Command

The `/dragonslegacy debug` command (alias: `/dl debug`) is an admin tool that toggles a real-time HUD showing exact egg information in the action-bar.

---

## Usage

```
/dl debug
```

Toggle debug mode ON or OFF. Running the command again toggles the other state.

---

## Behavior

### When debug mode is ON

- An **action-bar message** is sent to the admin every **5 ticks** (¼ second).
- The action-bar shows:
  - **Bearer:** current egg holder's name
  - **State:** current egg state
  - **XYZ:** exact coordinates (rounded to nearest whole number)
- **Visibility rules are bypassed** — the admin always sees exact coordinates regardless of the `visibility` settings in `egg.yaml`.
- The debug status is **per-player** — multiple admins can have it enabled simultaneously.
- Debug mode is **automatically cleared** when the admin disconnects.

### When debug mode is OFF

- The action-bar stops updating.
- Visibility rules return to normal for all placeholder lookups.

---

## Action-Bar Message

The action-bar text is fully customizable via `messages.yaml` under the `debug_actionbar` key:

```yaml
debug_actionbar:
  disabled: false
  channels:
    - mode: actionbar
      visibility: executor_only
      text: "%dragonslegacy:global_prefix% <yellow>Debug</yellow> | Bearer: <gold>%dragonslegacy:bearer%</gold> | State: <aqua>%dragonslegacy:egg_state%</aqua> | XYZ: <white>%dragonslegacy:exact-xyz%</white>"
```

You can use any `%dragonslegacy:*%` placeholder in the text. The `exact-xyz` placeholder always returns exact coordinates regardless of visibility settings.

---

## Permission

| Setting | Value |
|---------|-------|
| LuckPerms node | `dragonslegacy.admin.debug` |
| Vanilla op level | 3 |

Configured in `global.yaml` under `commands.debug`.

---

## Configuration

The `debug` entry in `global.yaml`:

```yaml
debug:
  permission_node: "dragonslegacy.admin.debug"
  op_level: 3
```

To change the action-bar update interval, modify `debug_actionbar` in `messages.yaml`. The interval itself (5 ticks) is currently a compile-time constant in `DebugManager.java`.

---

## Troubleshooting

**Debug mode is on but nothing appears**
- Ensure `debug_actionbar` in `messages.yaml` has `disabled: false`.
- Check that your action-bar isn't being overwritten by another mod/plugin.

**Debug mode doesn't bypass visibility**
- Make sure `exact-xyz` is defined in `placeholders.yaml` with `ignore_visibility: true`.
- If you've customized the `debug_actionbar` message, ensure you're using `%dragonslegacy:exact-xyz%` (not `%dragonslegacy:xyz%`).
