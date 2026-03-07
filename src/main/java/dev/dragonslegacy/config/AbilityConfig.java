package dev.dragonslegacy.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
public class AbilityConfig {

    @Comment("Duration of the Dragon's Hunger ability in ticks (20 ticks = 1 second). Default: 600 (30 s).")
    public int durationTicks = 600;

    @Comment("Cooldown after Dragon's Hunger expires, in ticks. Default: 6000 (5 min).")
    public int cooldownTicks = 6000;
}
