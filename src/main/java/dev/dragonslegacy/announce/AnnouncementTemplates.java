package dev.dragonslegacy.announce;

/**
 * Hardcoded announcement templates for all Dragon's Legacy broadcast messages.
 *
 * <p>Templates use {@code ${placeholder}} tokens that are resolved by
 * {@link AnnouncementFormatter}.  Available placeholders differ per message
 * and are documented inline.
 *
 * <h3>Phase 5 migration note</h3>
 * All constants defined here will be replaced by equivalent entries in a
 * YAML configuration file.  The field names serve as the future YAML key
 * names, making the migration straightforward.
 */
public final class AnnouncementTemplates {

    private AnnouncementTemplates() {}

    /** §6 – gold prefix color applied to the "[Dragon's Legacy]" tag. */
    private static final String PREFIX_COLOR = "\u00a76";

    /** §f – white body text color. */
    private static final String TEXT_COLOR = "\u00a7f";

    /** Formatted "[Dragon's Legacy]" prefix used by every template. */
    private static final String PREFIX = PREFIX_COLOR + "[Dragon's Legacy] " + TEXT_COLOR;

    // -------------------------------------------------------------------------
    // Egg events
    // -------------------------------------------------------------------------

    /** Placeholders: {@code ${player}} */
    public static final String EGG_PICKED_UP =
        PREFIX + "${player} has picked up the Dragon's Egg!";

    /** No placeholders. */
    public static final String EGG_DROPPED =
        PREFIX + "The Dragon's Egg has been dropped!";

    /** Placeholders: {@code ${x}}, {@code ${y}}, {@code ${z}} */
    public static final String EGG_PLACED =
        PREFIX + "The Dragon's Egg has been placed at ${x}, ${y}, ${z}!";

    /** Placeholders: {@code ${player}} */
    public static final String BEARER_CHANGED =
        PREFIX + "The Dragon's Egg is now held by ${player}!";

    /** No placeholders. */
    public static final String BEARER_CLEARED =
        PREFIX + "The Dragon's Egg has no bearer.";

    /** Placeholders: {@code ${x}}, {@code ${y}}, {@code ${z}} */
    public static final String EGG_TELEPORTED_TO_SPAWN =
        PREFIX + "The Dragon's Egg has been returned to spawn at ${x}, ${y}, ${z}!";

    // -------------------------------------------------------------------------
    // Ability events
    // -------------------------------------------------------------------------

    /** Placeholders: {@code ${player}}, {@code ${seconds}} */
    public static final String ABILITY_ACTIVATED =
        PREFIX + "${player} has activated Dragon's Hunger for ${seconds} seconds!";

    /** Placeholders: {@code ${player}} */
    public static final String ABILITY_EXPIRED =
        PREFIX + "Dragon's Hunger has expired for ${player}!";

    /** Placeholders: {@code ${seconds}} */
    public static final String ABILITY_COOLDOWN_STARTED =
        PREFIX + "Dragon's Hunger is on cooldown for ${seconds} seconds.";

    /** No placeholders. */
    public static final String ABILITY_COOLDOWN_ENDED =
        PREFIX + "Dragon's Hunger is ready again!";
}
