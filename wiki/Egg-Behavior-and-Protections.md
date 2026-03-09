# Egg Behavior and Protections

The Dragon Egg is treated as an irreplaceable, server-defining item. `egg.yaml` controls every aspect of how the egg behaves in the world: how precisely its location is reported, what dangers it is protected from, and how long an absent bearer can stay offline before their claim is reset.

---

## Configuration File

```
config/dragonslegacy/egg.yaml
```

Default configuration:

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

---

## Top-Level Settings

| Key | Type | Default | Description |
|---|---|---|---|
| `search_radius` | integer | `25` | Radius (in blocks) scanned when the mod tries to locate the egg near a known position |
| `block_ender_chest` | boolean | `true` | Prevents the bearer from placing the egg into an Ender Chest |
| `block_container_items` | boolean | `false` | Prevents the bearer from interacting with any container while holding the egg |
| `offline_reset_days` | integer | `3` | Days of bearer offline time before the bearer status is automatically cleared (0 = never reset) |
| `nearby_range` | integer | `64` | Block radius used for "nearby" detection in commands and the `%dragonslegacy:egg_location%` placeholder |

### `block_ender_chest`

Enabling this prevents the bearer from using Ender Chests as a way to effectively hide or teleport the egg. With this on, the egg must stay in the bearer's regular inventory.

### `block_container_items`

When enabled, the bearer cannot open any container (chest, barrel, shulker box, etc.) while actively holding the egg. This is a more restrictive rule intended for PvP-focused servers where you want to prevent quick-banking.

### `offline_reset_days`

If the bearer does not log in for this many days, the bearer status is cleared. The egg's physical state remains unchanged — only the ownership record is wiped. The next player to pick up the egg becomes the new bearer.

- `0` — Disable offline reset entirely; the bearer holds the title indefinitely regardless of absence.
- `1` — Clear bearer after 1 day offline.
- `7` — A week of absence relinquishes the egg.

---

## Egg States

The egg can be in one of five states, which determine how its location is tracked and displayed.

| State | Description |
|---|---|
| `held` | A player is holding the egg in their hand (main or off-hand) |
| `dropped` | The egg is an item entity lying on the ground |
| `placed` | The egg has been placed as a block |
| `entity` | The egg is attached to, inside, or riding an entity |
| `unknown` | The mod cannot determine the egg's location |

The state is exposed via the `%dragonslegacy:egg_state%` placeholder.

---

## Visibility Modes

The `visibility` section controls how precisely the egg's location is reported for each egg state. This lets you create asymmetry: the exact block position might be known for a placed egg, but obscured when a player is carrying it.

### Modes

| Mode | Behavior |
|---|---|
| `EXACT` | The egg's real coordinates are reported |
| `RANDOMIZED` | Coordinates are offset by a random amount before being reported |
| `HIDDEN` | No coordinates are returned; location strings appear empty |

### State Contexts

| Context | When It Applies |
|---|---|
| `INVENTORY` | Egg is in a player's inventory (not in hand) |
| `ITEM` | Egg is an item entity on the ground |
| `PLAYER` | Egg is held in main hand or off-hand |
| `BLOCK` | Egg is placed as a solid block |
| `FALLING_BLOCK` | Egg is a falling block entity |
| `ENTITY` | Egg is inside or attached to a non-player entity |

### Strategy Examples

**Classic "hunt the bearer"** — the egg's rough region is known but not exact:

```yaml
visibility:
  INVENTORY: "RANDOMIZED"
  ITEM: "RANDOMIZED"
  PLAYER: "RANDOMIZED"
  BLOCK: "EXACT"
  FALLING_BLOCK: "EXACT"
  ENTITY: "RANDOMIZED"
```

**Hardcore hidden mode** — only a placed egg reveals its location:

```yaml
visibility:
  INVENTORY: "HIDDEN"
  ITEM: "HIDDEN"
  PLAYER: "HIDDEN"
  BLOCK: "EXACT"
  FALLING_BLOCK: "HIDDEN"
  ENTITY: "HIDDEN"
```

**Full transparency** — always reveal exact location:

```yaml
visibility:
  INVENTORY: "EXACT"
  ITEM: "EXACT"
  PLAYER: "EXACT"
  BLOCK: "EXACT"
  FALLING_BLOCK: "EXACT"
  ENTITY: "EXACT"
```

---

## Protections

The `protection` section defines which hazards the egg is immune to. All flags default to `true`.

| Flag | What It Does |
|---|---|
| `void` | If the egg falls into the void, it is teleported back to the nearest safe location |
| `fire` | The egg item entity cannot be destroyed by fire |
| `lava` | The egg item entity cannot be destroyed by lava |
| `explosions` | The egg block and item entity survive all explosion damage |
| `cactus` | The egg item entity cannot be destroyed by touching a cactus |
| `despawn` | The egg item entity is flagged as never-despawn (will not disappear after 5 minutes) |
| `hopper` | Hoppers cannot pick up the egg item entity |
| `portal` | The egg does not travel through Nether or End portals |

### Disabling a Protection

Set any flag to `false` to remove that protection:

```yaml
protection:
  void: true
  fire: true
  lava: true
  explosions: false   # Allow TNT to destroy the egg
  cactus: true
  despawn: true
  hopper: false       # Allow hoppers to move the egg
  portal: true
```

> **Warning:** Disabling `void` without disabling `portal` means a portal-trapped egg could fall into the void and be permanently lost if both protections are off simultaneously.

---

## Interaction Blocks

### Ender Chest (`block_ender_chest: true`)

The egg cannot be moved into an Ender Chest when this is enabled. The bearer will receive a message explaining why the action was blocked. This ensures the egg remains trackable and cannot be stored in a dimension-independent space.

### Container Items (`block_container_items: false`)

When enabled, the bearer holding the egg cannot open chests, barrels, shulker boxes, or similar containers. This is disabled by default as it is a significant restriction, but it is useful on servers where preventing inventory management while bearing the egg is a deliberate gameplay rule.
