package dev.dragonslegacy.announce;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves {@code ${placeholder}} tokens inside announcement template strings.
 *
 * <p>Tokens use the syntax {@code ${key}}.  If a key is not present in the
 * provided map the original token is left unchanged (e.g. {@code ${unknown}}).
 *
 * <p>Example:
 * <pre>{@code
 * String result = AnnouncementFormatter.format(
 *     "${player} picked up the egg at ${x},${y},${z}!",
 *     Map.of("player", "Steve", "x", "0", "y", "64", "z", "0")
 * );
 * // → "Steve picked up the egg at 0,64,0!"
 * }</pre>
 *
 * <p>This class is intentionally simple so that Phase 5 can replace the
 * template source (YAML) without changing the formatting logic.
 */
public final class AnnouncementFormatter {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    private AnnouncementFormatter() {}

    /**
     * Replaces all {@code ${key}} tokens in {@code template} with the
     * corresponding values from {@code placeholders}.
     *
     * @param template     the message template, e.g. {@code "${player} picked up the egg!"}
     * @param placeholders map of placeholder keys to their resolved values
     * @return the formatted string with all known tokens substituted
     */
    public static String format(String template, Map<String, String> placeholders) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = placeholders.getOrDefault(key, matcher.group(0));
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
