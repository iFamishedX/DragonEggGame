package dev.dragonslegacy.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Infusion system configuration loaded from {@code config/dragonslegacy/infusion.yaml}.
 *
 * <p>The infusion system lets players merge the Dragon Egg with certain materials in
 * an anvil to change its glow color. The Dragon Egg is always the base item.
 */
@ConfigSerializable
public class InfusionConfig {

    @Setting("config_version")
    public int configVersion = 1;

    public InfusionSection infusion = new InfusionSection();

    @ConfigSerializable
    public static class InfusionSection {

        public boolean enabled = true;

        /** Default infusion color when no material has been applied. */
        @Setting("default_color")
        public String defaultColor = "#FFFFFF";

        /**
         * Materials that can be merged with the Dragon Egg in an anvil.
         * Keys are item names (namespace auto-completed to {@code minecraft:} if missing).
         */
        public Map<String, MaterialEntry> materials = buildDefaultMaterials();

        private static Map<String, MaterialEntry> buildDefaultMaterials() {
            Map<String, MaterialEntry> map = new LinkedHashMap<>();
            map.put("amethyst_shard",  new MaterialEntry("#AA00FF", "Infused with Amethyst"));
            map.put("copper_ingot",    new MaterialEntry("#B87333", "Infused with Copper"));
            map.put("gold_ingot",      new MaterialEntry("#FFD700", "Infused with Gold"));
            map.put("iron_ingot",      new MaterialEntry("#D8D8D8", "Infused with Iron"));
            map.put("netherite_ingot", new MaterialEntry("#3C2A23", "Infused with Netherite"));
            map.put("quartz",          new MaterialEntry("#FFFFFF", "Infused with Quartz"));
            map.put("redstone",        new MaterialEntry("#FF0000", "Infused with Redstone"));
            map.put("emerald",         new MaterialEntry("#00FF55", "Infused with Emerald"));
            map.put("diamond",         new MaterialEntry("#00FFFF", "Infused with Diamond"));
            return map;
        }
    }

    @ConfigSerializable
    public static class MaterialEntry {

        public String color = "#FFFFFF";

        /** Tooltip text rendered in the infusion color. */
        public String tooltip = "";

        public MaterialEntry() {}

        public MaterialEntry(String color, String tooltip) {
            this.color = color;
            this.tooltip = tooltip;
        }
    }
}
