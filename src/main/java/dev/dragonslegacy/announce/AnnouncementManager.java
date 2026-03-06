package dev.dragonslegacy.announce;

import dev.dragonslegacy.DragonsLegacyMod;
import dev.dragonslegacy.ability.event.AbilityActivatedEvent;
import dev.dragonslegacy.ability.event.AbilityCooldownEndedEvent;
import dev.dragonslegacy.ability.event.AbilityCooldownStartedEvent;
import dev.dragonslegacy.ability.event.AbilityExpiredEvent;
import dev.dragonslegacy.egg.event.DragonEggEventBus;
import dev.dragonslegacy.egg.event.EggBearerChangedEvent;
import dev.dragonslegacy.egg.event.EggDroppedEvent;
import dev.dragonslegacy.egg.event.EggPickedUpEvent;
import dev.dragonslegacy.egg.event.EggPlacedEvent;
import dev.dragonslegacy.egg.event.EggTeleportedToSpawnEvent;
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
 * announcement templates using {@link AnnouncementFormatter}, and broadcast the
 * result to every online player via
 * {@link net.minecraft.server.players.PlayerList#broadcastSystemMessage}.
 *
 * <h3>Lifecycle</h3>
 * <ol>
 *   <li>Call {@link #init(MinecraftServer, DragonEggEventBus)} once after both the
 *       server and {@link dev.dragonslegacy.egg.DragonsLegacy} have been initialised.</li>
 * </ol>
 *
 * <h3>Phase 5 migration note</h3>
 * Templates are currently hardcoded in {@link AnnouncementTemplates}.  In Phase 5
 * the templates field can be replaced with a YAML-backed implementation without
 * changing any subscriber logic here.
 */
public class AnnouncementManager {

    /** Ticks per second in vanilla Minecraft. Used to convert tick durations to seconds. */
    private static final int TICKS_PER_SECOND = 20;

    private @Nullable MinecraftServer server;

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
        ph.put("player", event.getPlayer().getGameProfile().getName());
        broadcast(format(AnnouncementTemplates.EGG_PICKED_UP, ph));
    }

    private void onEggDropped(EggDroppedEvent event) {
        broadcast(AnnouncementTemplates.EGG_DROPPED);
    }

    private void onEggPlaced(EggPlacedEvent event) {
        Map<String, String> ph = new HashMap<>();
        ph.put("x", String.valueOf(event.getPosition().getX()));
        ph.put("y", String.valueOf(event.getPosition().getY()));
        ph.put("z", String.valueOf(event.getPosition().getZ()));
        broadcast(format(AnnouncementTemplates.EGG_PLACED, ph));
    }

    private void onBearerChanged(EggBearerChangedEvent event) {
        UUID newBearer = event.getNewBearerUUID();
        if (newBearer == null) {
            broadcast(AnnouncementTemplates.BEARER_CLEARED);
        } else {
            Map<String, String> ph = new HashMap<>();
            ph.put("player", resolvePlayerName(newBearer));
            broadcast(format(AnnouncementTemplates.BEARER_CHANGED, ph));
        }
    }

    private void onEggTeleportedToSpawn(EggTeleportedToSpawnEvent event) {
        Map<String, String> ph = new HashMap<>();
        ph.put("x", String.valueOf((int) event.getSpawnPosition().x));
        ph.put("y", String.valueOf((int) event.getSpawnPosition().y));
        ph.put("z", String.valueOf((int) event.getSpawnPosition().z));
        broadcast(format(AnnouncementTemplates.EGG_TELEPORTED_TO_SPAWN, ph));
    }

    // -------------------------------------------------------------------------
    // Ability event handlers
    // -------------------------------------------------------------------------

    private void onAbilityActivated(AbilityActivatedEvent event) {
        Map<String, String> ph = new HashMap<>();
        ph.put("player", resolvePlayerName(event.getPlayerUUID()));
        ph.put("seconds", String.valueOf(event.getDuration() / TICKS_PER_SECOND));
        broadcast(format(AnnouncementTemplates.ABILITY_ACTIVATED, ph));
    }

    private void onAbilityExpired(AbilityExpiredEvent event) {
        Map<String, String> ph = new HashMap<>();
        ph.put("player", resolvePlayerName(event.getPlayerUUID()));
        broadcast(format(AnnouncementTemplates.ABILITY_EXPIRED, ph));
    }

    private void onCooldownStarted(AbilityCooldownStartedEvent event) {
        Map<String, String> ph = new HashMap<>();
        ph.put("seconds", String.valueOf(event.getCooldownTicks() / TICKS_PER_SECOND));
        broadcast(format(AnnouncementTemplates.ABILITY_COOLDOWN_STARTED, ph));
    }

    private void onCooldownEnded(AbilityCooldownEndedEvent event) {
        broadcast(AnnouncementTemplates.ABILITY_COOLDOWN_ENDED);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

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
        if (player != null) return player.getGameProfile().getName();
        // Offline fallback: check the server's game profile cache
        return server.getProfileCache()
            .get(uuid)
            .map(com.mojang.authlib.GameProfile::getName)
            .orElse(uuid.toString());
    }
}
