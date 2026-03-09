package dev.dragonslegacy.config;

import dev.dragonslegacy.DragonsLegacyMod;
import eu.pb4.placeholders.api.parsers.NodeParser;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Loads, saves, and reloads all Dragon's Legacy YAML configuration files
 * under {@code config/dragonslegacy/}.
 *
 * <h3>Files managed</h3>
 * <ul>
 *   <li>{@code main.yaml}            – core egg settings</li>
 *   <li>{@code ability.yaml}         – Dragon's Hunger ability timers, effects, and attributes</li>
 *   <li>{@code passive_effects.yaml} – passive effects/attributes while holding the egg</li>
 *   <li>{@code announcements.yaml}   – broadcast message templates (MiniMessage)</li>
 *   <li>{@code spawn.yaml}           – spawn-fallback &amp; BlueMap marker settings</li>
 *   <li>{@code commands.yaml}        – command names, aliases, and action triggers</li>
 *   <li>{@code messages.yaml}        – command message texts and output modes</li>
 * </ul>
 */
public class ConfigManager {

    private MainConfig          main          = new MainConfig();
    private AbilityConfig       ability       = new AbilityConfig();
    private PassiveEffectsConfig passiveEffects = new PassiveEffectsConfig();
    private AnnouncementsConfig announcements = new AnnouncementsConfig();
    private SpawnConfig         spawn         = new SpawnConfig();
    private CommandsConfig      commands      = new CommandsConfig();
    private MessagesConfig      messages      = new MessagesConfig();

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    /**
     * Ensures the config directory exists and loads all files (writing defaults if absent).
     */
    public void init() {
        try {
            Files.createDirectories(DragonsLegacyMod.CONFIG_DIR);
        } catch (IOException e) {
            DragonsLegacyMod.LOGGER.warn("[Dragon's Legacy] Could not create config directory.", e);
        }
        main          = loadOrCreate("main.yaml",            MainConfig.class,          new MainConfig());
        ability       = loadOrCreate("ability.yaml",         AbilityConfig.class,       new AbilityConfig());
        passiveEffects = loadOrCreate("passive_effects.yaml", PassiveEffectsConfig.class, new PassiveEffectsConfig());
        announcements = loadOrCreate("announcements.yaml",   AnnouncementsConfig.class, new AnnouncementsConfig());
        spawn         = loadOrCreate("spawn.yaml",           SpawnConfig.class,         new SpawnConfig());
        commands      = loadOrCreate("commands.yaml",        CommandsConfig.class,      new CommandsConfig());
        messages      = loadOrCreate("messages.yaml",        MessagesConfig.class,      new MessagesConfig());
    }

    /**
     * Re-reads all YAML files from disk.
     */
    public void reload() {
        main          = reload("main.yaml",            MainConfig.class,          main);
        ability       = reload("ability.yaml",         AbilityConfig.class,       ability);
        passiveEffects = reload("passive_effects.yaml", PassiveEffectsConfig.class, passiveEffects);
        announcements = reload("announcements.yaml",   AnnouncementsConfig.class, announcements);
        spawn         = reload("spawn.yaml",           SpawnConfig.class,         spawn);
        commands      = reload("commands.yaml",        CommandsConfig.class,      commands);
        messages      = reload("messages.yaml",        MessagesConfig.class,      messages);
        DragonsLegacyMod.LOGGER.info("[Dragon's Legacy] All configuration files reloaded.");
    }

    // -------------------------------------------------------------------------
    // Config getters
    // -------------------------------------------------------------------------

    public MainConfig          getMain()          { return main; }
    public AbilityConfig       getAbility()       { return ability; }
    public PassiveEffectsConfig getPassiveEffects() { return passiveEffects; }
    public AnnouncementsConfig getAnnouncements() { return announcements; }
    public SpawnConfig         getSpawn()         { return spawn; }
    public CommandsConfig      getCommands()      { return commands; }
    public MessagesConfig      getMessages()      { return messages; }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private <T> T loadOrCreate(String fileName, Class<T> type, T defaults) {
        Path path = DragonsLegacyMod.CONFIG_DIR.resolve(fileName);
        if (!path.toFile().isFile()) {
            boolean copied = copyDefaultResource(fileName, path);
            if (copied) {
                DragonsLegacyMod.LOGGER.info("[Dragon's Legacy] Created default {} at {}.", fileName, path);
            }
        }
        return reload(fileName, type, defaults);
    }

    private <T> T reload(String fileName, Class<T> type, T previous) {
        Path path = DragonsLegacyMod.CONFIG_DIR.resolve(fileName);
        YamlConfigurationLoader loader = buildLoader(path);
        try {
            CommentedConfigurationNode node = loader.load();
            T loaded = node.get(type);
            if (loaded == null) {
                DragonsLegacyMod.LOGGER.warn(
                    "[Dragon's Legacy] {} appears empty – using defaults.", fileName);
                return previous;
            }
            DragonsLegacyMod.LOGGER.info("[Dragon's Legacy] {} loaded.", fileName);
            return loaded;
        } catch (Exception e) {
            DragonsLegacyMod.LOGGER.warn(
                "[Dragon's Legacy] Failed to load {} – keeping previous values.", fileName, e);
            return previous;
        }
    }

    /**
     * Copies the bundled default resource for {@code fileName} to {@code dest}.
     *
     * @return {@code true} if the file was copied successfully; {@code false} otherwise
     */
    private boolean copyDefaultResource(String fileName, Path dest) {
        String resourcePath = "defaults/dragonslegacy/" + fileName;
        try (InputStream in = ConfigManager.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in != null) {
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
                return true;
            }
        } catch (IOException e) {
            DragonsLegacyMod.LOGGER.warn(
                "[Dragon's Legacy] Could not copy default resource {} – file will not be created.", fileName, e);
            return false;
        }
        DragonsLegacyMod.LOGGER.warn(
            "[Dragon's Legacy] Bundled default resource '{}' not found; config file will not be created.", resourcePath);
        return false;
    }

    private static YamlConfigurationLoader buildLoader(Path path) {
        return YamlConfigurationLoader.builder()
            .path(path)
            .defaultOptions(opts -> opts.serializers(build -> {
                build.registerAnnotatedObjects(ObjectMapper.factory());
                build.register(MessageString.class, new MessageString.Serializer(
                    NodeParser.builder()
                        .globalPlaceholders()
                        .quickText()
                        .staticPreParsing()
                        .build()
                ));
                build.register(Action.class, Action.Serializer.INSTANCE);
                build.register(CommandTemplate.class, CommandTemplate.Serializer.INSTANCE);
                build.register(Condition.class, Condition.Serializer.INSTANCE);
            }))
            .build();
    }
}
