package dev.dragonslegacy.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration for external PlaceholderAPI placeholders, loaded from
 * {@code config/dragonslegacy/placeholders.yaml}.
 *
 * <p>Each entry under {@code placeholders:} defines one {@code %dragonslegacy:<name>%}
 * placeholder.  Conditions are evaluated top-to-bottom; the first matching condition
 * wins.  If no condition matches, the {@code format} field is used as the output.
 *
 * <p>Internal variables ({@code {x}}, {@code {state}}, etc.) are NOT registered with
 * PlaceholderAPI; they are only available inside {@code format} strings and
 * {@code condition} expressions defined here.
 */
@ConfigSerializable
public class PlaceholdersConfig {

    @Setting("config_version")
    public int configVersion = 1;

    /**
     * Map of placeholder name → definition.
     * Keys become {@code %dragonslegacy:<name>%} placeholders.
     */
    public Map<String, PlaceholderDef> placeholders = new LinkedHashMap<>();

    // =========================================================================
    // PlaceholderDef
    // =========================================================================

    /**
     * Definition of a single external placeholder.
     *
     * <h3>Fields</h3>
     * <ul>
     *   <li>{@code ignore_visibility} – when {@code true}, always use exact values regardless
     *       of the visibility setting in egg.yaml.</li>
     *   <li>{@code conditions} – ordered list of {@link ConditionEntry}; first match wins.</li>
     *   <li>{@code format} – output template used when no condition matches.  Supports
     *       internal variables ({@code {x}}, etc.) and filter expressions
     *       ({@code {round({x},50)}}).</li>
     * </ul>
     */
    @ConfigSerializable
    public static class PlaceholderDef {

        @Setting("ignore_visibility")
        public boolean ignoreVisibility = false;

        public List<ConditionEntry> conditions = new ArrayList<>();

        public String format = "";
    }

    // =========================================================================
    // ConditionEntry
    // =========================================================================

    /**
     * A single condition/output pair inside a placeholder definition.
     *
     * <h3>Fields</h3>
     * <ul>
     *   <li>{@code if} – condition expression, e.g. {@code "{state} == 'HIDDEN'"}</li>
     *   <li>{@code output} – output string (supports filters) when the condition is true.</li>
     * </ul>
     */
    @ConfigSerializable
    public static class ConditionEntry {

        /** Condition expression, e.g. {@code "{state} == 'HIDDEN'"}. */
        @Setting("if")
        public String condition = "";

        /** Output string (supports filter expressions) when the condition matches. */
        public String output = "";
    }
}
