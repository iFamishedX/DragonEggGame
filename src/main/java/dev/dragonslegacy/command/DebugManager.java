package dev.dragonslegacy.command;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which players currently have debug mode enabled.
 *
 * <p>Debug mode is toggled with {@code /dragonslegacy debug} and is automatically
 * cleared when the player disconnects.
 *
 * <p>When debug mode is active for a player:
 * <ul>
 *   <li>Visibility rules are bypassed for that player's placeholder lookups.</li>
 *   <li>An action-bar message is sent every 5 ticks showing exact coordinates.</li>
 * </ul>
 */
public final class DebugManager {

    /** How often (in server ticks) the debug action-bar is refreshed. */
    public static final int ACTIONBAR_INTERVAL_TICKS = 5;

    private static final Set<UUID> DEBUG_PLAYERS = ConcurrentHashMap.newKeySet();

    private DebugManager() {}

    /**
     * Enables debug mode for the given player.
     *
     * @param uuid player UUID
     * @return {@code true} if debug was just enabled; {@code false} if it was already enabled
     */
    public static boolean enable(UUID uuid) {
        return DEBUG_PLAYERS.add(uuid);
    }

    /**
     * Disables debug mode for the given player.
     *
     * @param uuid player UUID
     * @return {@code true} if debug was just disabled; {@code false} if it was not enabled
     */
    public static boolean disable(UUID uuid) {
        return DEBUG_PLAYERS.remove(uuid);
    }

    /**
     * Toggles debug mode for the given player.
     *
     * @param uuid player UUID
     * @return {@code true} if debug mode is now ON, {@code false} if it is now OFF
     */
    public static boolean toggle(UUID uuid) {
        if (DEBUG_PLAYERS.remove(uuid)) {
            return false; // was on, now off
        }
        DEBUG_PLAYERS.add(uuid);
        return true; // was off, now on
    }

    /**
     * Returns whether debug mode is currently enabled for the given player.
     *
     * @param uuid player UUID
     * @return {@code true} if debug mode is active
     */
    public static boolean isDebugEnabled(UUID uuid) {
        return uuid != null && DEBUG_PLAYERS.contains(uuid);
    }

    /**
     * Clears debug mode for all players (e.g. on server stop or full reset).
     */
    public static void clearAll() {
        DEBUG_PLAYERS.clear();
    }
}
