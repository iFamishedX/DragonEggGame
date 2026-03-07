package dev.dragonslegacy.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
public class SpawnConfig {

    @Comment("When true, teleports (or spawns) the Dragon Egg to world spawn if it cannot be located. Default: true")
    public boolean fallbackEnabled = true;

    @Comment("Name shown on the BlueMap marker.")
    public String markerName = "Dragon Egg";

    @Comment("Description shown on the BlueMap area marker.")
    public String areaMarkerDescription = "The dragon egg is somewhere in this area.";

    @Comment("Description shown on the BlueMap point marker.")
    public String pointMarkerDescription = "Come and get it!";

    @Comment("URL of the BlueMap point marker icon.")
    public String pointMarkerIcon = "https://minecraft.wiki/images/thumb/Dragon_Egg_JE4.png/150px-Dragon_Egg_JE4.png";

    @Comment("Color of the BlueMap marker as a decimal value. Default: 2818132 (Purple).")
    public int markerColor = 2818132;
}
