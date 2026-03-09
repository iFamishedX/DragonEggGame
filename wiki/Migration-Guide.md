# Migration Guide

This page covers how to migrate your Dragon's Legacy configuration when updating between major config layout changes. The most significant change was the move from a **single monolithic config file** to the current **multi-file layout** introduced in `config_version: 1`.

---

## Multi-File Layout (Current)

Since version 1, Dragon's Legacy uses seven separate YAML files instead of one large file:

```
config/dragonslegacy/
├── config.yaml
├── egg.yaml
├── ability.yaml
├── passive.yaml
├── glow.yaml
├── commands.yaml
└── messages.yaml
```

Each file has its own `config_version` key. The mod uses these versions to detect when a file is outdated and logs a warning if a newer version ships with new keys.

---

## Migrating from a Single-File Config

If you are coming from an early development build that used a single `config.yaml` for everything, follow these steps:

### Step 1 — Back Up Your Old Config

```bash
cp config/dragonslegacy/config.yaml config/dragonslegacy/config.yaml.bak
```

Never discard your old config before confirming the migration is complete.

### Step 2 — Stop the Server

Always stop the server before making config changes to avoid write conflicts.

### Step 3 — Delete or Rename the Old File

```bash
mv config/dragonslegacy/config.yaml config/dragonslegacy/config_old.yaml
```

### Step 4 — Start the Server Once to Generate New Files

Start the server. Dragon's Legacy will detect that the config files are missing and generate all seven files with default values:

```
[Dragon's Legacy] config.yaml not found, generating defaults...
[Dragon's Legacy] egg.yaml not found, generating defaults...
...
```

### Step 5 — Stop the Server Again and Transfer Your Settings

Open your old backup (`config_old.yaml`) alongside the newly generated files and move your custom values into the correct new file.

**Old key → New file mapping:**

| Old Key (example) | New File | New Key |
|---|---|---|
| `enabled` | `config.yaml` | `enabled` |
| `egg.search_radius` | `egg.yaml` | `search_radius` |
| `egg.offline_reset_days` | `egg.yaml` | `offline_reset_days` |
| `egg.visibility.*` | `egg.yaml` | `visibility.*` |
| `egg.protection.*` | `egg.yaml` | `protection.*` |
| `ability.duration_ticks` | `ability.yaml` | `duration_ticks` |
| `ability.cooldown_ticks` | `ability.yaml` | `cooldown_ticks` |
| `ability.effects` | `ability.yaml` | `effects` |
| `ability.attributes` | `ability.yaml` | `attributes` |
| `passive.effects` | `passive.yaml` | `effects` |
| `passive.attributes` | `passive.yaml` | `attributes` |
| `glow.*` | `glow.yaml` | `glow.*` |
| `commands.*` | `commands.yaml` | `root`, `aliases`, `subcommands.*` |
| `messages.*` | `messages.yaml` | `messages.*` |

### Step 6 — Start the Server and Verify

Start the server and check the logs for any validation warnings:

```
[Dragon's Legacy] Loaded config: egg.yaml (version 1)
[Dragon's Legacy] Loaded config: ability.yaml (version 1)
...
[Dragon's Legacy] Dragon's Legacy enabled.
```

If you see `[WARN] Unknown key: ...`, a key was copied into the wrong file or uses the old naming convention. Check the [Configuration](Configuration.md) page for the correct key names.

---

## Upgrading Config Version (Minor Updates)

When a new mod version adds optional keys to an existing config file, the `config_version` number in that file will be bumped. The mod will:

1. Load the file successfully using all existing keys.
2. Log a warning such as:

   ```
   [Dragon's Legacy] [WARN] egg.yaml is version 1 but the current version is 2.
   New keys will use defaults. Consider regenerating the file.
   ```

3. Apply default values for any missing keys.

To upgrade cleanly:

1. Note the warning and check the mod's changelog for what changed.
2. Stop the server.
3. Open the affected file.
4. Add the new keys with your desired values (or the defaults from the changelog).
5. Update `config_version` to the new version number.
6. Restart the server or run `/dl reload`.

---

## Upgrading Config Version (Major Restructure)

If a future version introduces a structural change (not just new keys but reorganized sections or renamed keys), the migration guide for that version will be published here as a new section.

---

## Reverting to Defaults

To completely reset a config file to defaults:

1. Stop the server.
2. Delete (or rename) the target config file.
3. Start the server — the file is regenerated with all defaults.
4. Stop the server and edit as needed.

To reset **all** config files:

```bash
rm -r config/dragonslegacy/
```

Restart the server to regenerate everything.

> **Note:** Resetting config files does **not** reset persistence data (bearer UUID, egg state). That data lives in the world's `data/dragonslegacy/` directory.

---

## Common Migration Mistakes

| Mistake | Symptom | Fix |
|---|---|---|
| Putting `effects` list in `config.yaml` instead of `passive.yaml` | Effects ignored silently | Move the block to `passive.yaml` |
| Using `&` color codes in `messages.yaml` | Raw `&a` text visible in chat | Convert to MiniMessage: `<green>` |
| Leaving old single-file `config.yaml` in place | Mod loads old file and ignores new files | Remove or rename the old file |
| Forgetting to update `config_version` | Repeated upgrade warnings on every start | Set `config_version` to the current version number |
| Editing command names and expecting `/dl reload` to apply them | Commands still use old names | Restart the server fully |
