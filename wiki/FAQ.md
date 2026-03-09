# FAQ

Frequently asked questions about Dragon's Legacy, organized by topic.

---

## General

### What is Dragon's Legacy?

Dragon's Legacy is a Fabric mod for Minecraft 1.21.x that transforms the Dragon Egg into a persistent, server-wide relic. One player at a time is designated the **Bearer** — they receive passive bonuses and can activate a powerful active ability called **Dragon's Hunger**. The egg's location is tracked, protected from destruction, and persisted across server restarts.

---

### Is this a client-side or server-side mod?

Dragon's Legacy is a **server-side mod**. Players connecting to a server do not need to install it themselves. For single-player, install it like any other mod in your `mods/` folder.

---

### Does Dragon's Legacy work with other mods?

Generally yes. The mod hooks into vanilla Minecraft mechanics (item entities, blocks, status effects, attributes) and does not require specific compatibility patches for most mods. However:

- Mods that heavily modify inventory behavior or item entities may interfere with protections.
- Mods that add their own Dragon Egg variants may not be recognized as the egg to track.
- If you find a compatibility issue, please report it on the issue tracker.

---

### Can I use Dragon's Legacy on a modpack?

Yes. Dragon's Legacy is specifically designed to be drop-in. Add it to your modpack's `mods/` folder and configure it in `config/dragonslegacy/`. There are no blocklist or whitelist requirements.

---

## Bearer System

### How does a player become the Bearer?

The first player to pick up the Dragon Egg after the mod initializes becomes the Bearer. If the Bearer is reset (due to offline reset or future admin tooling), the next player to pick up the egg becomes the new Bearer.

---

### Can there be multiple Bearers at once?

No. There is exactly one Bearer at any given time — or no Bearer if the egg has not been claimed or the Bearer was reset.

---

### Can the Bearer transfer the egg to someone else?

Yes, indirectly. If the Bearer drops the egg and another player picks it up, that player becomes the new Bearer.

---

### What happens if the Bearer logs off?

The Bearer's identity is saved to disk. The egg's state at the time of logout is also persisted. When the Bearer logs back in, they are still the Bearer — until the `offline_reset_days` threshold is exceeded (default: 3 days).

---

### Can the Bearer be reset manually by an admin?

There is currently no dedicated admin command for this. Options include:

- Setting `offline_reset_days: 1` and waiting for the reset cycle.
- Stopping the server and deleting/editing the persistence file directly (advanced, not recommended).
- Watch for an admin reset command in future mod updates.

---

## Dragon's Hunger

### Can anyone use Dragon's Hunger?

Only the current Bearer can activate the ability with `/dl hunger on`.

---

### What happens if the Bearer drops the egg while Dragon's Hunger is active?

The ability continues running for its remaining duration. Passive effects (resistance, saturation) stop applying immediately since those require the egg to be held, but the active ability's effects and attribute bonuses persist until they expire or are manually stopped.

---

### Does the ability stack with passive effects?

Yes. Both the passive bonuses (while holding the egg) and the active ability bonuses are applied simultaneously when Dragon's Hunger is running and the egg is held. The max health bonuses add together — `+4 HP` from passive and `+20 HP` from the ability gives `+24 HP` (12 extra hearts).

---

### Can the ability be permanently disabled?

Not through a single flag, but you can effectively disable it by setting `duration_ticks: 0` or removing all `effects` and `attributes` from `ability.yaml`.

---

## Egg Protections

### My egg was destroyed. Why?

Check the `protection` flags in `egg.yaml`. Each flag must be set to `true` to protect against that hazard. If a flag was `false`, enable it and run `/dl reload`.

If all flags are `true` and the egg was still destroyed, this may be a bug — please report it with reproduction steps.

---

### Can players use hoppers to steal the egg?

Not when `protection.hopper: true` (the default). With this enabled, hoppers cannot pull the egg item. Set it to `false` if you want hoppers to be able to interact with the egg.

---

### Can the egg go through portals?

Not when `protection.portal: true` (the default). The egg is blocked from traveling through Nether or End portals. This prevents the egg from being relocated involuntarily.

---

### Can the egg despawn?

Not when `protection.despawn: true` (the default). The egg item entity is flagged to never despawn naturally.

---

## Glow System

### How do I change the egg's glow color?

Place the Dragon Egg in the left slot of an anvil and the desired material in the right slot. The output will have the new glow color. See [Glow System](Glow-System.md) for the full material list.

---

### Can I add my own materials for glow color changes?

Yes. Add entries to the `materials` map in `glow.yaml` using the item's ID and a hex color string. You can use any vanilla item or any item added by another loaded mod.

---

### Can I disable the glow entirely?

Yes. Set `glow.enabled: false` in `glow.yaml` and run `/dl reload`.

---

## Configuration

### Do I need to restart the server after editing config files?

For most files (`messages.yaml`, `egg.yaml`, `ability.yaml`, `passive.yaml`, `glow.yaml`): **No.** Run `/dl reload` to apply changes.

For `commands.yaml` (command names and aliases): **Yes.** A full server restart is required because Fabric registers commands at startup.

For `config.yaml`: `/dl reload` is sufficient.

---

### What format do I use for text in messages.yaml?

All text uses **MiniMessage** format. Examples:
- `<red>text</red>` — red text
- `<bold>text</bold>` — bold text
- `<gradient:#FF0000:#0000FF>text</gradient>` — color gradient
- `<hover:show_text:'tooltip'>text</hover>` — hover tooltip

Legacy `&` color codes are not supported. See [Messages and Prefixes](Messages-and-Prefixes.md) for full details.

---

### Can I rename the `/dl` command?

Yes. Edit `commands.yaml`:

```yaml
root: "mycommand"
aliases:
  - "mc"
```

Then **restart the server**. The `/dl` alias can be removed or changed freely.

---

## Placeholders

### Where can I use placeholders?

Placeholders can be used in any `text` field in `messages.yaml` and in `conditions` map keys. They are also displayed by `/dl placeholders` for debugging.

---

### A placeholder is showing as empty. Why?

The most common reasons:
- The placeholder returns empty when visibility is `HIDDEN` (for location placeholders).
- There is no current Bearer (`%dragonslegacy:bearer%` returns `none`).
- The ability is not active (`%dragonslegacy:ability_duration%` returns `0`).

Run `/dl placeholders` to see all current values and identify which one is empty and why.
