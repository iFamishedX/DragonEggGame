# Glow System

The Glow System lets the Dragon Egg emit a colored glow. The egg glows white by default, and the bearer (or anyone who obtains the egg) can permanently change the glow color by combining the egg with a specific material in an **anvil**.

---

## Configuration File

```
config/dragonslegacy/glow.yaml
```

Default configuration:

```yaml
config_version: 1
glow:
  enabled: true
  color: "#FFFFFF"
  crafting:
    enabled: true
    type: "anvil"
    base_item: "minecraft:dragon_egg"
    materials:
      amethyst_shard: "#AA00FF"
      copper_ingot: "#B87333"
      gold_ingot: "#FFD700"
      iron_ingot: "#D8D8D8"
      netherite_ingot: "#3C2A23"
      quartz: "#E7E7E7"
      redstone: "#FF0000"
      emerald: "#00FF55"
      diamond: "#00FFFF"
```

---

## Enabling / Disabling

| Key | Effect |
|---|---|
| `glow.enabled: true` | The egg glows with its stored color |
| `glow.enabled: false` | Glow is completely disabled; no color is applied |
| `crafting.enabled: true` | Players can change the glow color using the anvil |
| `crafting.enabled: false` | Color-changing is disabled; the egg keeps its current color |

---

## Default Glow Color

```yaml
glow:
  color: "#FFFFFF"
```

This is the color the egg starts with before any crafting. Set it to any hex color to give your server a unique default:

```yaml
glow:
  color: "#8800FF"   # Deep purple default
```

---

## Anvil Crafting

To change the glow color:

1. Open an **anvil**.
2. Place the **Dragon Egg** in the left slot.
3. Place the desired **material** in the right slot.
4. Take the result from the output slot.

The egg's glow color is updated immediately. The material is consumed.

> The anvil will show an experience cost as normal. The resulting item name will be the same as the input egg's name.

---

## Default Material → Color Mapping

| Material | Item ID | Hex Color | Appearance |
|---|---|---|---|
| Amethyst Shard | `amethyst_shard` | `#AA00FF` | Purple |
| Copper Ingot | `copper_ingot` | `#B87333` | Copper / Bronze |
| Gold Ingot | `gold_ingot` | `#FFD700` | Golden Yellow |
| Iron Ingot | `iron_ingot` | `#D8D8D8` | Silver / Light Grey |
| Netherite Ingot | `netherite_ingot` | `#3C2A23` | Dark Brown |
| Quartz | `quartz` | `#E7E7E7` | Off-White |
| Redstone | `redstone` | `#FF0000` | Bright Red |
| Emerald | `emerald` | `#00FF55` | Bright Green |
| Diamond | `diamond` | `#00FFFF` | Cyan |

---

## Adding Custom Materials

You can add any item in the game as a crafting material. Add an entry to the `materials` map using the item's ID (without `minecraft:` namespace) and a hex color string:

```yaml
materials:
  amethyst_shard: "#AA00FF"
  copper_ingot: "#B87333"
  gold_ingot: "#FFD700"
  iron_ingot: "#D8D8D8"
  netherite_ingot: "#3C2A23"
  quartz: "#E7E7E7"
  redstone: "#FF0000"
  emerald: "#00FF55"
  diamond: "#00FFFF"
  lapis_lazuli: "#1F51FF"      # Custom: Lapis → Electric Blue
  blaze_powder: "#FF6600"      # Custom: Blaze Powder → Orange
  prismarine_crystals: "#64FFC8" # Custom: Prismarine → Seafoam
```

Any item that exists in vanilla Minecraft (or in a mod that is loaded alongside Dragon's Legacy) can be used.

---

## Removing Default Materials

Simply delete any entry from the `materials` map to remove it. For example, to only allow Netherite to change the color (making it a rare, prestigious choice):

```yaml
materials:
  netherite_ingot: "#3C2A23"
```

---

## Hex Color Format

All colors are expressed as HTML hex color strings:

```
"#RRGGBB"
```

| Component | Range |
|---|---|
| `RR` | Red, `00`–`FF` |
| `GG` | Green, `00`–`FF` |
| `BB` | Blue, `00`–`FF` |

You can use any web color picker to find the hex value you want.

**Examples:**

| Color | Hex |
|---|---|
| White | `#FFFFFF` |
| Black | `#000000` |
| Hot Pink | `#FF69B4` |
| Sky Blue | `#87CEEB` |
| Lava Orange | `#FF4500` |
| Void Purple | `#6A0DAD` |

---

## Crafting Type

Currently, the only supported `crafting.type` value is `"anvil"`. Future mod versions may introduce additional crafting station types (e.g., smithing table). Do not change this value unless the mod's changelog explicitly documents a new type.

```yaml
crafting:
  type: "anvil"
```

---

## Notes

- The glow color is stored **on the egg item itself** as NBT data, so the color persists when the egg is dropped, placed, or picked up by a different player.
- Placing a glowing egg as a block will display the glow color in the world.
- The color persists across server restarts.
- Each anvil operation changes the color to the new color; the old color is overwritten.
