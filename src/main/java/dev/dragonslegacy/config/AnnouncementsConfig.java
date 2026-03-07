package dev.dragonslegacy.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Announcement templates using MiniMessage format.
 * Placeholders use MiniMessage tag syntax: {@code <player>}, {@code <x>}, {@code <y>}, {@code <z>}, {@code <seconds>}.
 */
@ConfigSerializable
public class AnnouncementsConfig {

    /** Cached unmodifiable copy of the built-in default templates. */
    private static final Map<String, String> DEFAULTS = buildDefaults();

    @Comment("If true, messages will be parsed using MiniMessage formatting.")
    @Setting("use_minimessage")
    public boolean useMiniMessage = true;

    @Comment("""
        Announcement message templates broadcast to all online players.
        Supports full MiniMessage formatting (https://docs.advntr.dev/minimessage/).
        Dynamic placeholders use <tag> syntax and are resolved per event:
          egg_picked_up            -> <player>
          egg_dropped              -> (none)
          egg_placed               -> <x>, <y>, <z>
          bearer_changed           -> <player>
          bearer_cleared           -> (none)
          egg_teleported_to_spawn  -> <x>, <y>, <z>
          ability_activated        -> <player>, <seconds>
          ability_expired          -> <player>
          ability_cooldown_started -> <seconds>
          ability_cooldown_ended   -> (none)
        """)
    public Map<String, String> templates = defaultTemplates();

    /** Returns a new mutable copy of the default templates (used for YAML initialisation). */
    public static Map<String, String> defaultTemplates() {
        return new LinkedHashMap<>(DEFAULTS);
    }

    /** Returns the cached unmodifiable map of built-in defaults. */
    public static Map<String, String> defaults() {
        return DEFAULTS;
    }

    private static Map<String, String> buildDefaults() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("egg_picked_up",            "<gold>[Dragon's Legacy]</gold> <white><player> has picked up the Dragon's Egg!</white>");
        map.put("egg_dropped",              "<gold>[Dragon's Legacy]</gold> <white>The Dragon's Egg has been dropped!</white>");
        map.put("egg_placed",               "<gold>[Dragon's Legacy]</gold> <white>The Dragon's Egg has been placed at <x>, <y>, <z>!</white>");
        map.put("bearer_changed",           "<gold>[Dragon's Legacy]</gold> <white>The Dragon's Egg is now held by <player>!</white>");
        map.put("bearer_cleared",           "<gold>[Dragon's Legacy]</gold> <white>The Dragon's Egg has no bearer.</white>");
        map.put("egg_teleported_to_spawn",  "<gold>[Dragon's Legacy]</gold> <white>The Dragon's Egg has been returned to spawn at <x>, <y>, <z>!</white>");
        map.put("ability_activated",        "<gold>[Dragon's Legacy]</gold> <white><player> has activated Dragon's Hunger for <seconds> seconds!</white>");
        map.put("ability_expired",          "<gold>[Dragon's Legacy]</gold> <white>Dragon's Hunger has expired for <player>!</white>");
        map.put("ability_cooldown_started", "<gold>[Dragon's Legacy]</gold> <white>Dragon's Hunger is on cooldown for <seconds> seconds.</white>");
        map.put("ability_cooldown_ended",   "<gold>[Dragon's Legacy]</gold> <white>Dragon's Hunger is ready again!</white>");
        return Collections.unmodifiableMap(map);
    }
}
