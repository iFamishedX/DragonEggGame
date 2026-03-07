package dev.dragonslegacy.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

/**
 * Represents a single attribute-modifier entry in a Dragon's Legacy YAML config.
 *
 * <p>Used by both {@link PassiveEffectsConfig} (passive bearer attributes) and
 * {@link AbilityConfig} (Dragon's Hunger ability attributes).
 */
@ConfigSerializable
public class AttributeEntry {

    /** Namespaced identifier of the attribute, e.g. {@code "minecraft:max_health"}. */
    public String id = "minecraft:max_health";

    /** Amount to add/multiply. */
    public double amount = 0.0;

    /**
     * Operation applied to the attribute.
     * Accepted values: {@code "add_value"}, {@code "multiply_base"}, {@code "multiply_total"}.
     */
    public String operation = "add_value";

    /**
     * A UUID string that uniquely identifies this modifier so it can be cleanly
     * removed later.  Format: {@code "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"}.
     */
    public String uuid = "00000000-0000-0000-0000-000000000001";
}
