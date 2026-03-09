# Messages and Prefixes

Dragon's Legacy delivers all player-facing text through `messages.yaml`. Every message is fully customizable: you can change the text, the delivery channel (chat, action bar, title, etc.), who sees it, and when it fires.

All text is rendered using **MiniMessage**, which means you can use color tags, gradients, hover events, click events, and more anywhere in your messages.

---

## File Location

```
config/dragonslegacy/messages.yaml
```

---

## Global Prefix

The `prefix` key at the top of the file defines a string that is automatically available as the `%dragonslegacy:global_prefix%` placeholder inside message texts.

```yaml
prefix: "<dark_gray>[<gradient:#AA00FF:#00FFFF>Dragon's Legacy</gradient>]</dark_gray> "
```

Using this placeholder in your messages keeps the prefix consistent and easy to update:

```yaml
text: "%dragonslegacy:global_prefix%<white>The bearer is <yellow>%dragonslegacy:bearer%</yellow>."
```

---

## Message Entry Structure

Each named entry under `messages:` follows this structure:

```yaml
messages:
  <message_key>:
    order: <integer>
    cooldown_ticks: <integer>
    global_cooldown_ticks: <integer>
    conditions: <map>
    channels:
      - mode: <string>
        visibility: <string>
        text: <MiniMessage string>
```

### Top-Level Fields

| Field | Type | Description |
|---|---|---|
| `order` | integer | Sort order when multiple messages are displayed in sequence (lower = first) |
| `cooldown_ticks` | integer | Per-player cooldown in ticks before this message can fire for the same player again (0 = no cooldown) |
| `global_cooldown_ticks` | integer | Server-wide cooldown in ticks before this message can fire for any player again (0 = no cooldown) |
| `conditions` | map | Placeholder-based conditions that must be satisfied before the message fires |
| `channels` | list | One or more delivery channels for the message |

---

## Channels

Each entry in the `channels` list describes one way the message is sent.

### Channel Fields

| Field | Required | Description |
|---|---|---|
| `mode` | Yes | How the message is delivered (see Modes below) |
| `visibility` | Yes | Who receives the message (see Visibility below) |
| `text` | Yes | The message text in MiniMessage format |

A single message can have **multiple channels**. For example, you can send a chat message to everyone and simultaneously show an action bar message to the bearer only:

```yaml
channels:
  - mode: "chat"
    visibility: "everyone"
    text: "%dragonslegacy:global_prefix%<aqua>%dragonslegacy:bearer%</aqua> has activated Dragon's Hunger!"
  - mode: "actionbar"
    visibility: "bearer"
    text: "<gold><bold>DRAGON'S HUNGER ACTIVE</bold></gold>"
```

---

## Delivery Modes

| Mode | Description |
|---|---|
| `chat` | Standard chat message |
| `actionbar` | Text above the hotbar |
| `title` | Large title text in the center of the screen |
| `subtitle` | Smaller text below the title |
| `bossbar` | Boss health bar at the top of the screen |

---

## Visibility

| Value | Who Receives the Message |
|---|---|
| `self` | Only the player who triggered the event |
| `everyone` | All online players |
| `bearer` | Only the current bearer |
| `non_bearer` | All online players except the bearer |

---

## Conditions

The `conditions` map lets you gate a message behind placeholder checks. If all conditions pass, the message fires; if any condition fails, that message entry is skipped.

```yaml
conditions:
  "%dragonslegacy:egg_state%": "held"
```

Multiple conditions are combined with logical AND — all must be true.

```yaml
conditions:
  "%dragonslegacy:egg_state%": "held"
  "%dragonslegacy:online%": "5"
```

Conditions support both exact string matches and simple numeric comparisons (prefix the value with `>`, `<`, `>=`, or `<=`):

```yaml
conditions:
  "%dragonslegacy:online%": ">=3"
```

---

## MiniMessage Formatting

All text fields support the full [MiniMessage](https://docs.advntr.dev/minimessage/) specification. MiniMessage is **always enabled** — you cannot fall back to legacy `&` color codes.

### Common Tags

| Tag | Result |
|---|---|
| `<red>text</red>` | Red colored text |
| `<bold>text</bold>` | Bold text |
| `<gradient:#FF0000:#0000FF>text</gradient>` | Color gradient |
| `<hover:show_text:'tooltip'>text</hover>` | Hover tooltip |
| `<click:run_command:'/dl bearer'>text</click>` | Clickable text |
| `<newline>` | Line break |
| `<reset>` | Clear all formatting |

### Example Messages

**Bearer announcement:**
```yaml
bearer_changed:
  order: 0
  cooldown_ticks: 0
  global_cooldown_ticks: 200
  conditions: {}
  channels:
    - mode: "chat"
      visibility: "everyone"
      text: "%dragonslegacy:global_prefix%<yellow><bold>%dragonslegacy:bearer%</bold></yellow> <white>has claimed the Dragon Egg!</white>"
    - mode: "title"
      visibility: "bearer"
      text: "<gold><bold>You are the Bearer!</bold></gold>"
    - mode: "subtitle"
      visibility: "bearer"
      text: "<gray>Hold the egg to receive its power.</gray>"
```

**Ability activation:**
```yaml
ability_start:
  order: 0
  cooldown_ticks: 0
  global_cooldown_ticks: 0
  conditions:
    "%dragonslegacy:egg_state%": "held"
  channels:
    - mode: "actionbar"
      visibility: "bearer"
      text: "<gradient:#AA00FF:#FF0000><bold>Dragon's Hunger Awakened!</bold></gradient>"
    - mode: "chat"
      visibility: "non_bearer"
      text: "%dragonslegacy:global_prefix%<red>%dragonslegacy:bearer%</red> <white>has activated Dragon's Hunger!</white>"
```

---

## Using Placeholders in Messages

Any placeholder from the [Placeholders](Placeholders.md) page can be used in any `text` value.

```yaml
text: "Bearer: %dragonslegacy:bearer% | Location: %dragonslegacy:egg_location%"
```

See [Placeholders](Placeholders.md) for the full list.

---

## Cooldown Tips

- Set `cooldown_ticks: 0` and `global_cooldown_ticks: 0` for instantaneous triggers like command responses.
- Use `global_cooldown_ticks` for server-wide announcements to avoid spam when many players trigger the same event rapidly.
- `cooldown_ticks` is useful for per-player warnings (e.g., "you cannot do that right now") so the same player is not spammed.
