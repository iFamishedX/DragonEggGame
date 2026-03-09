package dev.dragonslegacy.announce;

import dev.dragonslegacy.DragonsLegacyMod;
import dev.dragonslegacy.ability.event.AbilityActivatedEvent;
import dev.dragonslegacy.ability.event.AbilityCooldownEndedEvent;
import dev.dragonslegacy.ability.event.AbilityCooldownStartedEvent;
import dev.dragonslegacy.ability.event.AbilityExpiredEvent;
import dev.dragonslegacy.config.AnnouncementsConfig;
import dev.dragonslegacy.egg.event.DragonEggEventBus;
import dev.dragonslegacy.egg.event.EggBearerChangedEvent;
import dev.dragonslegacy.egg.event.EggDroppedEvent;
import dev.dragonslegacy.egg.event.EggPickedUpEvent;
import dev.dragonslegacy.egg.event.EggPlacedEvent;
import dev.dragonslegacy.egg.event.EggTeleportedToSpawnEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Central controller for all Dragon's Legacy server-wide announcements.
 *
 * <p>Subscribe to all relevant events on the {@link DragonEggEventBus}, format
 * announcement templates using MiniMessage, and broadcast the result to every
 * online player via
 * {@link net.minecraft.server.players.PlayerList#broadcastSystemMessage}.
 *
 * <h3>Lifecycle</h3>
 * <ol>
 *   <li>Call {@link #init(MinecraftServer, DragonEggEventBus)} once after both the
 *       server and {@link dev.dragonslegacy.egg.DragonsLegacy} have been initialised.</li>
 * </ol>
 *
 * <h3>Templates</h3>
 * Templates are loaded from {@link AnnouncementsConfig} and use MiniMessage format.
 * Dynamic placeholders use MiniMessage tag syntax (e.g. {@code <player>}).
 */
public class AnnouncementManager {

    /** Ticks per second in vanilla Minecraft. Used to convert tick durations to seconds. */
    private static final int TICKS_PER_SECOND = 20;

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    private @Nullable MinecraftServer server;

    /** Whether to parse templates as MiniMessage. Default {@code true}. */
    private boolean useMiniMessage = true;

    /**
     * Runtime announcement templates, populated from {@link AnnouncementsConfig}
     * after config is loaded.  Falls back to the MiniMessage defaults in
     * {@link AnnouncementsConfig#defaultTemplates()} when a key is absent.
     */
    private Map<String, String> templates = new HashMap<>();

    // -------------------------------------------------------------------------
    // Configuration
    // -------------------------------------------------------------------------

    /**
     * Replaces the current template map.  Called by the Dragon's Legacy coordinator
     * whenever the config is (re-)loaded.
     *
     * @param templates the new template map (may be {@code null} to reset to defaults)
     */
    public void setTemplates(Map<String, String> templates) {
        this.templates = templates != null ? templates : new HashMap<>();
    }

    /**
     * Sets whether broadcast messages are parsed as MiniMessage.
     *
     * @param useMiniMessage {@code true} to enable MiniMessage parsing (default), {@code false} for plain text
     */
    public void setUseMiniMessage(boolean useMiniMessage) {
        this.useMiniMessage = useMiniMessage;
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    /**
     * Initialises the manager by subscribing to all relevant events on
     * {@code bus}.
     *
     * @param server the running {@link MinecraftServer}
     * @param bus    the shared {@link DragonEggEventBus}
     */
    public void init(MinecraftServer server, DragonEggEventBus bus) {
        this.server = server;

        // Egg events
        bus.subscribe(EggPickedUpEvent.class,        this::onEggPickedUp);
        bus.subscribe(EggDroppedEvent.class,         this::onEggDropped);
        bus.subscribe(EggPlacedEvent.class,          this::onEggPlaced);
        bus.subscribe(EggBearerChangedEvent.class,   this::onBearerChanged);
        bus.subscribe(EggTeleportedToSpawnEvent.class, this::onEggTeleportedToSpawn);

        // Ability events
        bus.subscribe(AbilityActivatedEvent.class,      this::onAbilityActivated);
        bus.subscribe(AbilityExpiredEvent.class,        this::onAbilityExpired);
        bus.subscribe(AbilityCooldownStartedEvent.class, this::onCooldownStarted);
        bus.subscribe(AbilityCooldownEndedEvent.class,  this::onCooldownEnded);

        DragonsLegacyMod.LOGGER.info("[Dragon's Legacy] AnnouncementManager initialised.");
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Broadcasts a pre-formatted message to every online player and the server
     * console.
     *
     * @param message the fully formatted message string (may contain § color codes)
     */
    public void broadcast(String message) {
        if (server == null) return;
        Component component = Component.literal(message);
        server.getPlayerList().broadcastSystemMessage(component, false);
    }

    /**
     * Formats {@code template} by substituting all {@code ${key}} tokens with
     * values from {@code placeholders}.
     *
     * @param template     the message template
     * @param placeholders the placeholder key–value pairs
     * @return the formatted string
     */
    public String format(String template, Map<String, String> placeholders) {
        return AnnouncementFormatter.format(template, placeholders);
    }

    // -------------------------------------------------------------------------
    // Egg event handlers
    // -------------------------------------------------------------------------

    private void onEggPickedUp(EggPickedUpEvent event) {
        Map<String, String> ph = new HashMap<>();
        ph.put("player", event.getPlayer().getGameProfile().name());
        broadcastMiniMessage(getTemplate("egg_picked_up"), ph);
    }

    private void onEggDropped(EggDroppedEvent event) {
        broadcastMiniMessage(getTemplate("egg_dropped"), Map.of());
    }

    private void onEggPlaced(EggPlacedEvent event) {
        Map<String, String> ph = new HashMap<>();
        ph.put("x", String.valueOf(event.getPosition().getX()));
        ph.put("y", String.valueOf(event.getPosition().getY()));
        ph.put("z", String.valueOf(event.getPosition().getZ()));
        broadcastMiniMessage(getTemplate("egg_placed"), ph);
    }

    private void onBearerChanged(EggBearerChangedEvent event) {
        UUID newBearer = event.getNewBearerUUID();
        if (newBearer == null) {
            broadcastMiniMessage(getTemplate("bearer_cleared"), Map.of());
        } else {
            Map<String, String> ph = new HashMap<>();
            ph.put("player", resolvePlayerName(newBearer));
            broadcastMiniMessage(getTemplate("bearer_changed"), ph);
        }
    }

    private void onEggTeleportedToSpawn(EggTeleportedToSpawnEvent event) {
        Map<String, String> ph = new HashMap<>();
        ph.put("x", String.valueOf((int) event.getSpawnPosition().x));
        ph.put("y", String.valueOf((int) event.getSpawnPosition().y));
        ph.put("z", String.valueOf((int) event.getSpawnPosition().z));
        broadcastMiniMessage(getTemplate("egg_teleported"), ph);
    }

    // -------------------------------------------------------------------------
    // Ability event handlers
    // -------------------------------------------------------------------------

    private void onAbilityActivated(AbilityActivatedEvent event) {
        Map<String, String> ph = new HashMap<>();
        ph.put("player", resolvePlayerName(event.getPlayerUUID()));
        ph.put("seconds", String.valueOf(event.getDuration() / TICKS_PER_SECOND));
        broadcastMiniMessage(getTemplate("ability_activated"), ph);
    }

    private void onAbilityExpired(AbilityExpiredEvent event) {
        Map<String, String> ph = new HashMap<>();
        ph.put("player", resolvePlayerName(event.getPlayerUUID()));
        broadcastMiniMessage(getTemplate("ability_expired"), ph);
    }

    private void onCooldownStarted(AbilityCooldownStartedEvent event) {
        Map<String, String> ph = new HashMap<>();
        ph.put("seconds", String.valueOf(event.getCooldownTicks() / TICKS_PER_SECOND));
        broadcastMiniMessage(getTemplate("ability_cooldown_started"), ph);
    }

    private void onCooldownEnded(AbilityCooldownEndedEvent event) {
        broadcastMiniMessage(getTemplate("ability_cooldown_ended"), Map.of());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the template for {@code key} from the runtime map, or the
     * MiniMessage default from {@link AnnouncementsConfig#defaults()} if absent.
     */
    private String getTemplate(String key) {
        String value = templates.get(key);
        return value != null ? value : AnnouncementsConfig.defaults().getOrDefault(key, "");
    }

    /**
     * Parses {@code template} as MiniMessage (if {@link #useMiniMessage} is enabled),
     * resolves {@code placeholders} as unparsed tags (e.g. {@code <player>}),
     * converts the result to a Minecraft {@link Component}, and broadcasts it to every
     * online player.
     *
     * <p>When MiniMessage is disabled the template is broadcast as plain text after
     * substituting {@code ${key}} tokens with values from {@code placeholders}.
     *
     * @param template     message template
     * @param placeholders map of placeholder keys to plain-text values
     */
    private void broadcastMiniMessage(String template, Map<String, String> placeholders) {
        if (server == null) return;
        Component mcComponent;
        if (useMiniMessage) {
            TagResolver.Builder resolverBuilder = TagResolver.builder();
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                resolverBuilder.resolver(Placeholder.unparsed(entry.getKey(), entry.getValue()));
            }
            net.kyori.adventure.text.Component adventureComponent =
                MINI_MESSAGE.deserialize(template, resolverBuilder.build());
            String legacyText = LEGACY_SERIALIZER.serialize(adventureComponent);
            mcComponent = Component.literal(legacyText);
        } else {
            // Plain-text path: the default templates use <key> MiniMessage syntax, so we
            // replace those first.  ${key} style placeholders (custom templates) are then
            // handled by AnnouncementFormatter.  Finally, any remaining MiniMessage tags
            // are stripped so the message renders as clean plain text.
            String plainText = template;
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                plainText = plainText.replace("<" + entry.getKey() + ">", entry.getValue());
            }
            plainText = AnnouncementFormatter.format(plainText, placeholders);
            // Strip leftover MiniMessage tags with a simple regex.
            plainText = plainText.replaceAll("<[^>]+>", "");
            mcComponent = Component.literal(plainText);
        }
        server.getPlayerList().broadcastSystemMessage(mcComponent, false);
    }

    /**
     * Resolves a player's display name from their UUID.
     *
     * <p>If the player is online their current display name is used.
     * If they are offline the UUID string is returned as a fallback so that
     * announcements are never blocked by offline-player lookups.
     *
     * @param uuid the player UUID to resolve
     * @return the player's name, or the UUID string if unknown
     */
    private String resolvePlayerName(UUID uuid) {
        if (server == null) return uuid.toString();
        ServerPlayer player = server.getPlayerList().getPlayer(uuid);
        if (player != null) return player.getGameProfile().name();
        // Offline fallback: try the server's name-to-id resolver cache
        try {
            net.minecraft.server.players.CachedUserNameToIdResolver userCache =
                (net.minecraft.server.players.CachedUserNameToIdResolver) server.services().nameToIdCache();
            if (userCache != null) {
                var result = userCache.get(uuid);
                if (result.isPresent()) return result.get().name();
            }
        } catch (Exception ignored) {}
        return uuid.toString();
    }
}
