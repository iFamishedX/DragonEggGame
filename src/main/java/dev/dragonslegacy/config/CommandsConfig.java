package dev.dragonslegacy.config;

import eu.pb4.placeholders.api.parsers.NodeParser;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.List;

/**
 * Config for /dragon_egg command messages and Actions list.
 * Messages support MiniMessage formatting and PB4 placeholders (%deg:bearer%, etc.).
 */
@ConfigSerializable
public class CommandsConfig {

    @Comment("Messages shown by /dragon_egg commands.")
    public Messages messages = new Messages();

    @Comment("""
        Actions executed on certain egg event triggers.
        See the wiki for full documentation: https://github.com/iFamishedX/DragonEggGame/wiki
        """)
    public List<Action> actions = List.of();

    @ConfigSerializable
    public static class Messages {
        private static final NodeParser PARSER = NodeParser
            .builder()
            .globalPlaceholders()
            .quickText()
            .staticPreParsing()
            .build();

        @Comment("Shown by '/dragon_egg bearer' when position visibility is EXACT.")
        public MessageString bearerExact = new MessageString(
            PARSER,
            "<yellow>The %deg:item% is currently held by <gold>%deg:bearer%</> and was last seen at <gold>%deg:pos%</>."
        );

        @Comment("Shown by '/dragon_egg bearer' when position visibility is RANDOMIZED.")
        public MessageString bearerRandomized = new MessageString(
            PARSER,
            "<yellow>The %deg:item% is currently held by <gold>%deg:bearer%</> and was last seen around <gold>%deg:pos%</>."
        );

        @Comment("Shown by '/dragon_egg bearer' when position visibility is HIDDEN.")
        public MessageString bearerHidden = new MessageString(
            PARSER,
            "<yellow>The %deg:item% is currently held by <gold>%deg:bearer%</>."
        );

        @Comment("Shown by '/dragon_egg bearer' when there is no bearer.")
        public MessageString noBearer = new MessageString(PARSER, "<yellow>No one has snatched the %deg:item% yet.");

        @Comment("Shown by '/dragon_egg bearer' on error.")
        public MessageString bearerError = new MessageString(PARSER, "<red>Currently not available.");

        @Comment("Broadcast when the bearer changes.")
        public MessageString bearerChanged = new MessageString(
            PARSER,
            "<yellow><gold>%deg:bearer%</> now has the %deg:item%!"
        );

        @Comment("Shown by '/dragon_egg info'.")
        public MessageString info = new MessageString(
            PARSER,
            """
                
                
                
                <aqua><bold>The Dragon Egg Server Game</*>
                <gray>----------------------------</*>
                <yellow>Whoever has the %deg:item%, must place the %deg:item% <gold><hover show_text "\
                    When arriving at the base, you should quickly know where to look \
                    and the time needed for the search should be appropriate.\
                ">obvious</></> and <gold><hover show_text "\
                    You shouldn't have to destroy anything to get to the %deg:item%.\
                ">accessible for everyone</></> in the own base. \
                You can <gold><hover show_text "\
                    It's supposed to be fun for everybody, so please look out for another and fight fair. \
                    (It's best if you don't fight at all!)
                    The defense should not go beyond your own base and lost items (e.g. because of death) must be returned.\
                ">protect</></> it with traps and your own life, or put it in a huge vault, \
                but it has to be <gold><hover show_text "\
                    When arriving at the base, you should quickly know where to look \
                    and the time needed for the search should be appropriate.\
                ">obvious</></> where the %deg:item% is. \
                Everyone else now can steal the %deg:item% and has to place it in their base respectively.</*>
                <red><italic>You may only steal the egg, if the current egg bearer is online \
                or if they have been offline for at least 3 days!\
                """.replace("  ", "")
        );
    }
}
