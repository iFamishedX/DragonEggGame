package dev.dragonslegacy.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.List;

/**
 * Config for command names, aliases, and action triggers.
 * Command messages are now in {@code messages.yaml} (see {@link MessagesConfig}).
 */
@ConfigSerializable
public class CommandsConfig {

    @Comment("The literal name of the root player command (e.g. the part after the slash).")
    @Setting("root_command")
    public String rootCommand = "dragonslegacy";

    @Comment("Aliases for the root command. Players can type any of these as a shortcut.")
    @Setting("root_aliases")
    public List<String> rootAliases = new ArrayList<>(java.util.Arrays.asList("dl"));

    @Comment("Names of each subcommand under the root command.")
    public SubcommandNames subcommands = new SubcommandNames();

    @Comment("""
        Actions executed on certain egg event triggers.
        See the wiki for full documentation: https://github.com/iFamishedX/DragonEggGame/wiki
        """)
    public List<Action> actions = List.of();

    @ConfigSerializable
    public static class SubcommandNames {
        @Comment("Name of the subcommand that shows the current egg bearer.")
        public String bearer = "bearer";

        @Comment("Name of the subcommand that lists available commands.")
        public String help = "help";

        @Comment("Name of the Dragon's Hunger subcommand group.")
        public String hunger = "hunger";

        @Comment("Name of the subcommand that activates Dragon's Hunger.")
        @Setting("hunger_on")
        public String hungerOn = "on";

        @Comment("Name of the subcommand that deactivates Dragon's Hunger.")
        @Setting("hunger_off")
        public String hungerOff = "off";
    }
}
