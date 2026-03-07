package dev.dragonslegacy.config;

import dev.dragonslegacy.DragonsLegacyMod;
import eu.pb4.placeholders.api.parsers.NodeParser;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads, saves, and reloads all five Dragon's Legacy YAML configuration files
 * under {@code config/dragonslegacy/}.
 *
 * <h3>Files managed</h3>
 * <ul>
 *   <li>{@code main.yaml}          – core egg settings</li>
 *   <li>{@code ability.yaml}       – Dragon's Hunger ability timers</li>
 *   <li>{@code announcements.yaml} – broadcast message templates (MiniMessage)</li>
 *   <li>{@code spawn.yaml}         – spawn-fallback &amp; BlueMap marker settings</li>
 *   <li>{@code commands.yaml}      – /dragon_egg messages and action triggers</li>
 * </ul>
 */
public class ConfigManager {

    private MainConfig          main          = new MainConfig();
    private AbilityConfig       ability       = new AbilityConfig();
    private AnnouncementsConfig announcements = new AnnouncementsConfig();
    private SpawnConfig         spawn         = new SpawnConfig();
    private CommandsConfig      commands      = new CommandsConfig();

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
        main          = loadOrCreate("main.yaml",          MainConfig.class,          new MainConfig());
        ability       = loadOrCreate("ability.yaml",       AbilityConfig.class,       new AbilityConfig());
        announcements = loadOrCreate("announcements.yaml", AnnouncementsConfig.class, new AnnouncementsConfig());
        spawn         = loadOrCreate("spawn.yaml",         SpawnConfig.class,         new SpawnConfig());
        commands      = loadOrCreate("commands.yaml",      CommandsConfig.class,      new CommandsConfig());
    }

    /**
     * Re-reads all five YAML files from disk.
     */
    public void reload() {
        main          = reload("main.yaml",          MainConfig.class,          main);
        ability       = reload("ability.yaml",       AbilityConfig.class,       ability);
        announcements = reload("announcements.yaml", AnnouncementsConfig.class, announcements);
        spawn         = reload("spawn.yaml",         SpawnConfig.class,         spawn);
        commands      = reload("commands.yaml",      CommandsConfig.class,      commands);
        DragonsLegacyMod.LOGGER.info("[Dragon's Legacy] All configuration files reloaded.");
    }

    // -------------------------------------------------------------------------
    // Config getters
    // -------------------------------------------------------------------------

    public MainConfig          getMain()          { return main; }
    public AbilityConfig       getAbility()       { return ability; }
    public AnnouncementsConfig getAnnouncements() { return announcements; }
    public SpawnConfig         getSpawn()         { return spawn; }
    public CommandsConfig      getCommands()      { return commands; }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private <T> T loadOrCreate(String fileName, Class<T> type, T defaults) {
        Path path = DragonsLegacyMod.CONFIG_DIR.resolve(fileName);
        if (!path.toFile().isFile()) {
            save(fileName, type, defaults);
            DragonsLegacyMod.LOGGER.info("[Dragon's Legacy] Created default {} at {}.", fileName, path);
            return defaults;
        }
        return reload(fileName, type, defaults);
    }

    private <T> T reload(String fileName, Class<T> type, T previous) {
        Path path = DragonsLegacyMod.CONFIG_DIR.resolve(fileName);
        YamlConfigurationLoader loader = buildLoader(path, type);
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

    private <T> void save(String fileName, Class<T> type, T value) {
        Path path = DragonsLegacyMod.CONFIG_DIR.resolve(fileName);
        YamlConfigurationLoader loader = buildLoader(path, type);
        try {
            CommentedConfigurationNode node = loader.createNode();
            node.set(type, value);
            loader.save(node);
        } catch (Exception e) {
            DragonsLegacyMod.LOGGER.warn("[Dragon's Legacy] Failed to save {}.", fileName, e);
        }
    }

    private static <T> YamlConfigurationLoader buildLoader(Path path, Class<T> type) {
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
