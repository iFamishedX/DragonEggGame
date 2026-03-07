package dev.dragonslegacy.config;

import dev.dragonslegacy.api.DragonEggAPI.PositionType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.Map;

@ConfigSerializable
public class MainConfig {

    public static final Map<PositionType, VisibilityType> DEFAULT_VISIBILITY = Map.of(
        PositionType.BLOCK,         VisibilityType.RANDOMIZED,
        PositionType.ITEM,          VisibilityType.EXACT,
        PositionType.FALLING_BLOCK, VisibilityType.EXACT,
        PositionType.INVENTORY,     VisibilityType.EXACT,
        PositionType.ENTITY,        VisibilityType.EXACT,
        PositionType.PLAYER,        VisibilityType.HIDDEN
    );

    @Comment("Radius (blocks) used to randomise the dragon egg position display. Default: 25")
    @Setting("search_radius")
    public float searchRadius = 25f;

    @Comment("Prevent the Dragon Egg from entering an Ender Chest. Default: true")
    @Setting("block_ender_chest")
    public boolean blockEnderChest = true;

    @Comment("Prevent the Dragon Egg from entering any portable container item (Shulker Box, Bundle). Default: false")
    @Setting("block_container_items")
    public boolean blockContainerItems = false;

    @Comment("Distance in blocks around the Dragon Egg where players count as 'nearby'. Default: 64")
    @Setting("nearby_range")
    public int nearbyRange = 64;

    @Comment("Real-world days a bearer may be offline before the egg bearer designation is cleared. Default: 3.0")
    @Setting("offline_reset_days")
    public double offlineResetDays = 3.0;

    @Comment("""
        Visibility of the dragon egg for each position type.
        RANDOMIZED = randomized position shown, EXACT = exact position shown, HIDDEN = position hidden.
        """)
    public Map<PositionType, VisibilityType> visibility = DEFAULT_VISIBILITY;

    /** Returns the visibility type for the given position type, defaulting to HIDDEN if absent.
     * The null check guards against malformed YAML deserialization setting visibility to null. */
    public VisibilityType getVisibility(@Nullable PositionType type) {
        Map<PositionType, VisibilityType> vis = (visibility != null) ? visibility : DEFAULT_VISIBILITY;
        return vis.getOrDefault(type, DEFAULT_VISIBILITY.getOrDefault(type, VisibilityType.HIDDEN));
    }
}
