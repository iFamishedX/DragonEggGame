package dev.dragonslegacy.config;

import eu.pb4.placeholders.api.parsers.NodeParser;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

/**
 * Configuration for all Dragon's Legacy command messages.
 *
 * <p>Loaded from {@code config/dragonslegacy/messages.yaml}. Supports:
 * <ul>
 *   <li>MiniMessage formatting (e.g. {@code <gold><bold>text<reset>})</li>
 *   <li>PB4 Placeholder API (e.g. {@code %deg:bearer%}, {@code %deg:item%})</li>
 *   <li>Full hex color codes (e.g. {@code <#FF5500>})</li>
 *   <li>Multi-line text</li>
 * </ul>
 *
 * <p>Each message entry has an {@code output} field controlling where the message
 * is displayed, and a {@code text} field with the MiniMessage content.
 */
@ConfigSerializable
public class MessagesConfig {

    /**
     * Shared node parser with global PB4 placeholders, quick-text, and static pre-parsing.
     * This mirrors the parser used throughout the rest of the mod.
     */
    private static final NodeParser PARSER = NodeParser.builder()
        .globalPlaceholders()
        .quickText()
        .staticPreParsing()
        .build();

    // -------------------------------------------------------------------------
    // Nested types
    // -------------------------------------------------------------------------

    /**
     * A single configurable message with an output mode and text content.
     *
     * <p><b>output</b> values:
     * <ul>
     *   <li>{@code chat}     – sent as a regular chat message</li>
     *   <li>{@code actionbar} – shown above the hotbar</li>
     *   <li>{@code bossbar}  – shown as a boss bar (auto-removed after 5 s)</li>
     *   <li>{@code title}    – shown as a large title on screen</li>
     *   <li>{@code subtitle} – shown as a small subtitle on screen</li>
     * </ul>
     */
    @ConfigSerializable
    public static class MessageEntry {

        @Comment("""
            Where this message is displayed.
            Allowed values: chat, actionbar, bossbar, title, subtitle
            Default: chat
            """)
        public String output = "chat";

        @Comment("""
            The message text.
            Supports MiniMessage formatting, PB4 placeholders (%deg:bearer%, %deg:item%, etc.),
            and full hex color codes (<#RRGGBB>).
            Multi-line text is supported using YAML block scalars (|).
            """)
        public MessageString text = new MessageString(PARSER, "");
    }

    // -------------------------------------------------------------------------
    // Message fields
    // -------------------------------------------------------------------------

    @Comment("""
        Message shown by /dragonslegacy help.
        Output mode: chat (recommended).
        Lists all available subcommands for the player.
        Example placeholders: none specific; use any PB4 global placeholders.
        # Example:
        # help:
        #   output: "chat"
        #   text: |
        #     <gold><bold>Dragon's Legacy Commands</bold></gold>
        #     <gray>  /dragonslegacy help</gray> - Show this help message
        #     <gray>  /dragonslegacy bearer</gray> - Show the current bearer
        #     <gray>  /dragonslegacy hunger on</gray> - Activate Dragon's Hunger
        #     <gray>  /dragonslegacy hunger off</gray> - Deactivate Dragon's Hunger
        """)
    public MessageEntry help = defaultEntry("chat",
        "<gold><bold>Dragon's Legacy Commands</bold></gold>\n"
        + "<gray>  /dragonslegacy help</gray> - Show this help message\n"
        + "<gray>  /dragonslegacy bearer</gray> - Show the current bearer\n"
        + "<gray>  /dragonslegacy hunger on</gray> - Activate Dragon's Hunger\n"
        + "<gray>  /dragonslegacy hunger off</gray> - Deactivate Dragon's Hunger"
    );

    @Setting("bearer_info")
    @Comment("""
        Shown by /dragonslegacy bearer when there is a current bearer.
        Output mode: any.
        Supported placeholders: %deg:bearer%, %deg:item%, %deg:pos%.
        # Example:
        # bearer_info:
        #   output: "chat"
        #   text: "<yellow>The %deg:item% is held by <gold>%deg:bearer%</>."
        """)
    public MessageEntry bearerInfo = defaultEntry("chat",
        "<yellow>The %deg:item% is currently held by <gold>%deg:bearer%</>."
    );

    @Setting("bearer_none")
    @Comment("""
        Shown by /dragonslegacy bearer when there is no bearer.
        Output mode: any.
        Supported placeholders: %deg:item%.
        # Example:
        # bearer_none:
        #   output: "chat"
        #   text: "<yellow>No one holds the %deg:item% yet."
        """)
    public MessageEntry bearerNone = defaultEntry("chat",
        "<yellow>No one holds the %deg:item% yet."
    );

    @Setting("hunger_activate")
    @Comment("""
        Shown to the bearer when Dragon's Hunger is activated via /dragonslegacy hunger on.
        Output mode: title or subtitle recommended for dramatic effect.
        Supported placeholders: %deg:bearer%, %deg:item%.
        # Example (title + subtitle combo):
        # hunger_activate:
        #   output: "title"
        #   text: "<#FF4500><bold>Dragon's Hunger!</bold></#FF4500>"
        """)
    public MessageEntry hungerActivate = defaultEntry("title",
        "<#FF4500><bold>Dragon's Hunger!</bold></#FF4500>"
    );

    @Setting("hunger_deactivate")
    @Comment("""
        Shown to the bearer when Dragon's Hunger is deactivated via /dragonslegacy hunger off.
        Output mode: title or actionbar recommended.
        Supported placeholders: %deg:bearer%, %deg:item%.
        # Example:
        # hunger_deactivate:
        #   output: "title"
        #   text: "<gray><italic>Dragon's Hunger fades...</italic></gray>"
        """)
    public MessageEntry hungerDeactivate = defaultEntry("title",
        "<gray><italic>Dragon's Hunger fades...</italic></gray>"
    );

    @Setting("not_bearer")
    @Comment("""
        Shown when a non-bearer player tries to use /dragonslegacy hunger on or hunger off.
        Output mode: actionbar recommended for brevity.
        Supported placeholders: %deg:bearer%, %deg:item%.
        # Example:
        # not_bearer:
        #   output: "actionbar"
        #   text: "<red>You are not the Dragon Egg bearer!"
        """)
    public MessageEntry notBearer = defaultEntry("actionbar",
        "<red>You are not the Dragon Egg bearer!"
    );

    @Setting("hunger_expired")
    @Comment("""
        Sent to the bearer when Dragon's Hunger expires naturally (duration runs out).
        Output mode: title or actionbar recommended.
        Supported placeholders: %deg:bearer%, %deg:item%.
        # Example:
        # hunger_expired:
        #   output: "title"
        #   text: "<gray><italic>Dragon's Hunger has ended.</italic></gray>"
        """)
    public MessageEntry hungerExpired = defaultEntry("title",
        "<gray><italic>Dragon's Hunger has ended.</italic></gray>"
    );

    @Setting("elytra_blocked")
    @Comment("""
        Sent to the bearer when they try to use an elytra while Dragon's Hunger is active
        and block_elytra is true in ability.yaml.
        Output mode: actionbar recommended for brevity.
        Supported placeholders: %deg:bearer%, %deg:item%.
        # Example:
        # elytra_blocked:
        #   output: "actionbar"
        #   text: "<red>You cannot use an elytra while Dragon's Hunger is active!"
        """)
    public MessageEntry elytraBlocked = defaultEntry("actionbar",
        "<red>You cannot use an elytra while Dragon's Hunger is active!"
    );

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Creates a default {@link MessageEntry} with the given {@code output} mode and {@code text}.
     *
     * @param output the output mode string
     * @param text   the MiniMessage text
     * @return a new, pre-initialised {@link MessageEntry}
     */
    private static MessageEntry defaultEntry(String output, String text) {
        MessageEntry entry = new MessageEntry();
        entry.output = output;
        entry.text = new MessageString(PARSER, text);
        return entry;
    }
}
