package dev.dragonslegacy.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

/**
 * Represents a single status-effect entry in a Dragon's Legacy YAML config.
 *
 * <p>Used by both {@link PassiveEffectsConfig} (passive bearer effects) and
 * {@link AbilityConfig} (Dragon's Hunger ability effects).
 *
 * <p>The {@code level} field is 1-based (level 1 = Minecraft amplifier 0,
 * level 2 = Minecraft amplifier 1, …).
 */
@ConfigSerializable
public class EffectEntry {

    /**
     * Namespaced identifier of the effect, e.g. {@code "minecraft:speed"}.
     * A bare name without a namespace (e.g. {@code "speed"}) is also accepted;
     * {@code "minecraft:"} will be prepended automatically.
     */
    public String id = "minecraft:speed";

    /**
     * Effect level (1-based: level 1 = Minecraft amplifier 0, level 2 = amplifier 1, …).
     * Minimum value is 1.
     */
    public int level = 1;

    /** Whether to show particle effects on the player. */
    @Setting("show_particles")
    public boolean showParticles = true;

    /** Whether to show the effect icon in the HUD. */
    @Setting("show_icon")
    public boolean showIcon = true;
}
