package dev.dragonslegacy;

import dev.dragonslegacy.api.DragonEggAPI;
import dev.dragonslegacy.config.Data;
import dev.dragonslegacy.config.EggConfig;
import dev.dragonslegacy.config.PlaceholdersConfig;
import dev.dragonslegacy.config.VisibilityType;
import dev.dragonslegacy.egg.DragonsLegacy;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Core engine for evaluating Dragon's Legacy placeholder definitions.
 *
 * <p>Placeholders are defined in {@code config/dragonslegacy/placeholders.yaml} via
 * {@link PlaceholdersConfig}.  Each placeholder can have a list of conditions (evaluated
 * top-to-bottom; first match wins) and a fallback format string.
 *
 * <h3>Internal variables</h3>
 * Internal variables are gathered from game state and injected into the format
 * and condition strings at evaluation time.  They are never registered directly
 * with PlaceholderAPI.
 *
 * <ul>
 *   <li>{@code {x}}, {@code {y}}, {@code {z}} – egg coordinates</li>
 *   <li>{@code {dimension}} – egg world key (e.g. {@code minecraft:overworld})</li>
 *   <li>{@code {state}} – effective visibility state: {@code HIDDEN}, {@code EXACT},
 *       or the physical egg-state name ({@code PLAYER}, {@code PLACED}, {@code DROPPED})</li>
 *   <li>{@code {bearer}}, {@code {bearer_uuid}} – bearer display name / UUID</li>
 *   <li>{@code {executor}}, {@code {executor_uuid}} – player using the placeholder</li>
 *   <li>{@code {last_seen}}, {@code {seconds}} – time stubs</li>
 *   <li>{@code {ability_duration}}, {@code {ability_cooldown}} – ticks</li>
 *   <li>{@code {online}}, {@code {max_players}} – server player counts</li>
 *   <li>{@code {world_time}}, {@code {real_time}}, {@code {tick}} – time values</li>
 *   <li>{@code {egg_age}} – stub</li>
 *   <li>{@code {bearer_health}}, {@code {bearer_max_health}} – bearer HP</li>
 * </ul>
 *
 * <h3>Supported filters</h3>
 * <ul>
 *   <li>{@code upper(v)}</li>
 *   <li>{@code lower(v)}</li>
 *   <li>{@code capitalize(v)}</li>
 *   <li>{@code round(v,precision)} – rounds {@code v} to the nearest multiple of
 *       {@code precision}.  When precision is 1, rounds to whole number.</li>
 *   <li>{@code abs(v)}</li>
 *   <li>{@code format_number(v)}</li>
 *   <li>{@code default(v,fallback)}</li>
 *   <li>{@code replace(v,target,replacement)}</li>
 *   <li>{@code if(cond,trueVal,falseVal)}</li>
 *   <li>{@code color(v)} – strips MiniMessage tags</li>
 *   <li>{@code json(v1,v2,...)} – builds a JSON array string</li>
 *   <li>{@code distance_to_player()} – distance from executor to egg</li>
 * </ul>
 */
public final class PlaceholderEngine {

    private PlaceholderEngine() {}

    // =========================================================================
    // EvalContext
    // =========================================================================

    /**
     * Context carried through placeholder evaluation.  Provides access to the
     * executor player and the server, plus an override flag for debug mode.
     */
    public static final class EvalContext {
        /** Player who triggered the placeholder lookup (may be {@code null}). */
        public final ServerPlayer executor;
        /** The running Minecraft server (may be {@code null} during early init). */
        public final MinecraftServer server;
        /** When {@code true}, visibility rules are bypassed (used by debug mode). */
        public final boolean forceExact;

        public EvalContext(ServerPlayer executor, MinecraftServer server, boolean forceExact) {
            this.executor = executor;
            this.server   = server;
            this.forceExact = forceExact;
        }
    }

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Resolves a placeholder definition to a plain string.
     *
     * @param def  the placeholder definition from {@link PlaceholdersConfig}
     * @param ctx  evaluation context
     * @return resolved string (never {@code null})
     */
    public static String resolve(PlaceholdersConfig.PlaceholderDef def, EvalContext ctx) {
        if (def == null) return "";

        Map<String, String> vars = gatherVariables(def, ctx);

        // Evaluate conditions top-to-bottom; first match wins
        if (def.conditions != null) {
            for (PlaceholdersConfig.ConditionEntry cond : def.conditions) {
                if (cond == null || cond.condition == null) continue;
                if (evalCondition(cond.condition, vars)) {
                    return applyFilters(cond.output != null ? cond.output : "", vars);
                }
            }
        }

        // Fall back to format
        return applyFilters(def.format != null ? def.format : "", vars);
    }

    // =========================================================================
    // Internal variable gathering
    // =========================================================================

    private static Map<String, String> gatherVariables(PlaceholdersConfig.PlaceholderDef def,
                                                        EvalContext ctx) {
        Map<String, String> vars = new HashMap<>();

        Data data = DragonEggAPI.getData();

        // --- Coordinates ---------------------------------------------------
        int rawX = 0, rawY = 0, rawZ = 0;
        if (data != null) {
            rawX = data.getBlockPos().getX();
            rawY = data.getBlockPos().getY();
            rawZ = data.getBlockPos().getZ();
        }
        vars.put("x", String.valueOf(rawX));
        vars.put("y", String.valueOf(rawY));
        vars.put("z", String.valueOf(rawZ));

        // --- Dimension -----------------------------------------------------
        vars.put("dimension", data != null && data.worldId != null ? data.worldId : "unknown");

        // --- Effective state -----------------------------------------------
        String state = resolveState(def, data, ctx);
        vars.put("state", state);

        // --- Bearer --------------------------------------------------------
        String bearer     = "";
        String bearerUuid = "";
        DragonsLegacy legacy = DragonsLegacy.getInstance();
        if (legacy != null) {
            UUID bearerUUID = legacy.getEggTracker().getCurrentBearer();
            if (bearerUUID != null) {
                bearerUuid = bearerUUID.toString();
                if (ctx.server != null) {
                    ServerPlayer bp = ctx.server.getPlayerList().getPlayer(bearerUUID);
                    bearer = bp != null ? bp.getGameProfile().name() : bearerUUID.toString();
                } else {
                    bearer = bearerUUID.toString();
                }
            }
        }
        vars.put("bearer",      bearer);
        vars.put("bearer_uuid", bearerUuid);

        // --- Executor ------------------------------------------------------
        vars.put("executor",      ctx.executor != null ? ctx.executor.getGameProfile().name() : "");
        vars.put("executor_uuid", ctx.executor != null ? ctx.executor.getUUID().toString()    : "");

        // --- Time stubs ----------------------------------------------------
        vars.put("last_seen", "0");
        vars.put("seconds",   "0");

        // --- Ability -------------------------------------------------------
        if (legacy != null) {
            vars.put("ability_duration",
                String.valueOf(legacy.getAbilityEngine().getTimers().getDurationRemaining()));
            vars.put("ability_cooldown",
                String.valueOf(legacy.getAbilityEngine().getTimers().getCooldownRemaining()));
        } else {
            vars.put("ability_duration", "0");
            vars.put("ability_cooldown", "0");
        }

        // --- Server --------------------------------------------------------
        if (ctx.server != null) {
            vars.put("online",      String.valueOf(ctx.server.getPlayerList().getPlayerCount()));
            vars.put("max_players", String.valueOf(ctx.server.getPlayerList().getMaxPlayers()));
            vars.put("world_time",  String.valueOf(ctx.server.overworld().getDayTime()));
            vars.put("tick",        String.valueOf(ctx.server.getTickCount()));
        } else {
            vars.put("online",      "0");
            vars.put("max_players", "0");
            vars.put("world_time",  "0");
            vars.put("tick",        "0");
        }
        vars.put("real_time",
            LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

        // --- Egg age stub --------------------------------------------------
        vars.put("egg_age", "0");

        // --- Bearer health -------------------------------------------------
        if (legacy != null && ctx.server != null) {
            UUID bearerUUID = legacy.getEggTracker().getCurrentBearer();
            if (bearerUUID != null) {
                ServerPlayer bp = ctx.server.getPlayerList().getPlayer(bearerUUID);
                if (bp != null) {
                    vars.put("bearer_health",     String.valueOf((int) bp.getHealth()));
                    vars.put("bearer_max_health",
                        String.valueOf((int) bp.getMaxHealth()));
                } else {
                    vars.put("bearer_health",     "0");
                    vars.put("bearer_max_health", "20");
                }
            } else {
                vars.put("bearer_health",     "0");
                vars.put("bearer_max_health", "20");
            }
        } else {
            vars.put("bearer_health",     "0");
            vars.put("bearer_max_health", "20");
        }

        return vars;
    }

    /**
     * Determines the effective {@code {state}} value for use in conditions and formats.
     *
     * <ul>
     *   <li>When {@code ignore_visibility = true} or {@code forceExact = true}: returns the
     *       physical egg state string (PLAYER, PLACED, DROPPED, UNKNOWN).</li>
     *   <li>When {@code ignore_visibility = false}: checks the visibility config.  If the
     *       current position type maps to {@link VisibilityType#HIDDEN}, returns "HIDDEN"
     *       so conditions can handle it.  Otherwise returns the physical state.</li>
     * </ul>
     */
    private static String resolveState(PlaceholdersConfig.PlaceholderDef def,
                                        Data data,
                                        EvalContext ctx) {
        String physical = physicalState(data);

        if (def.ignoreVisibility || ctx.forceExact) {
            return physical;
        }

        // Apply visibility config
        if (data != null && data.type != null) {
            EggConfig eggCfg = DragonsLegacyMod.configManager.getEggConfig();
            if (eggCfg != null && eggCfg.visibility != null) {
                VisibilityType vt = eggCfg.visibility.get(data.type);
                if (vt == VisibilityType.HIDDEN) {
                    return "HIDDEN";
                }
            }
        }
        return physical;
    }

    private static String physicalState(Data data) {
        if (data == null || data.type == null) return "UNKNOWN";
        return switch (data.type) {
            case PLAYER, INVENTORY -> "PLAYER";
            case BLOCK, FALLING_BLOCK -> "PLACED";
            case ITEM -> "DROPPED";
            case ENTITY -> "UNKNOWN";
        };
    }

    // =========================================================================
    // Condition evaluation
    // =========================================================================

    /**
     * Evaluates a condition expression like {@code "{state} == 'HIDDEN'"} after
     * substituting internal variables.
     *
     * <p>Supported operators: {@code ==}, {@code !=}.
     *
     * @param condition raw condition string
     * @param vars      resolved variable map
     * @return {@code true} if the condition holds
     */
    public static boolean evalCondition(String condition, Map<String, String> vars) {
        if (condition == null || condition.isBlank()) return false;
        // Substitute simple {var} tokens first
        String resolved = substituteVars(condition, vars);

        // Try != first (before ==)
        int neIdx = resolved.indexOf("!=");
        if (neIdx >= 0) {
            String left  = stripQuotes(resolved.substring(0, neIdx).trim());
            String right = stripQuotes(resolved.substring(neIdx + 2).trim());
            return !left.equals(right);
        }

        int eqIdx = resolved.indexOf("==");
        if (eqIdx >= 0) {
            String left  = stripQuotes(resolved.substring(0, eqIdx).trim());
            String right = stripQuotes(resolved.substring(eqIdx + 2).trim());
            return left.equals(right);
        }

        // Non-empty, non-blank string treated as truthy
        return !resolved.isBlank();
    }

    // =========================================================================
    // Filter evaluation
    // =========================================================================

    /**
     * Processes all {@code {expression}} tokens in the given template string,
     * substituting variables and evaluating filters.
     *
     * @param template format string
     * @param vars     resolved variable map
     * @return fully evaluated string
     */
    public static String applyFilters(String template, Map<String, String> vars) {
        if (template == null || template.isEmpty()) return "";

        // Iteratively replace innermost {…} tokens until the string stabilises
        String result = template;
        for (int iter = 0; iter < 50; iter++) {
            String next = replaceInnermostExpressions(result, vars);
            if (next.equals(result)) break;
            result = next;
        }
        return result;
    }

    /**
     * One pass of innermost-first {@code {…}} replacement.
     * An "innermost" token is one that contains no {@code {} inside it.
     */
    private static String replaceInnermostExpressions(String s, Map<String, String> vars) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '{') {
                // Scan forward for the first { or }
                int j = i + 1;
                boolean hasInner = false;
                int end = -1;
                while (j < s.length()) {
                    char d = s.charAt(j);
                    if (d == '{') { hasInner = true; break; }
                    if (d == '}') { end = j; break; }
                    j++;
                }
                if (!hasInner && end >= 0) {
                    // Innermost token found; evaluate it
                    String expr = s.substring(i + 1, end).trim();
                    sb.append(evaluateExpression(expr, vars));
                    i = end + 1;
                } else {
                    // Either nested or unmatched — emit as-is and advance one char
                    sb.append(c);
                    i++;
                }
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }

    /**
     * Evaluates a single expression (already stripped of surrounding braces).
     *
     * <ul>
     *   <li>Plain variable name → value from vars map.</li>
     *   <li>{@code funcname(args)} → filter invocation.</li>
     *   <li>Anything else → returned as-is.</li>
     * </ul>
     */
    private static String evaluateExpression(String expr, Map<String, String> vars) {
        if (expr == null || expr.isEmpty()) return "";

        // Plain variable lookup
        if (vars.containsKey(expr)) {
            return vars.get(expr);
        }

        // Filter call: funcname(args)
        int parenOpen = expr.indexOf('(');
        if (parenOpen > 0 && expr.endsWith(")")) {
            String funcName = expr.substring(0, parenOpen).trim().toLowerCase(Locale.ROOT);
            String argsStr  = expr.substring(parenOpen + 1, expr.length() - 1);
            List<String> args = splitArgs(argsStr);
            return applyFilter(funcName, args, vars);
        }

        // Return bare text (e.g. a string literal inside a condition)
        return expr;
    }

    /**
     * Applies a named filter to its argument list.
     *
     * @param name filter name (lower-case)
     * @param args argument strings (already resolved)
     * @param vars variable map (for {@code if()} and {@code distance_to_player()})
     * @return filter result
     */
    private static String applyFilter(String name, List<String> args, Map<String, String> vars) {
        return switch (name) {

            case "upper" -> args.isEmpty() ? "" : args.get(0).toUpperCase(Locale.ROOT);

            case "lower" -> args.isEmpty() ? "" : args.get(0).toLowerCase(Locale.ROOT);

            case "capitalize" -> {
                if (args.isEmpty()) yield "";
                String s = args.get(0);
                yield s.isEmpty() ? s
                    : Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase(Locale.ROOT);
            }

            case "round" -> {
                if (args.isEmpty()) yield "0";
                if (args.size() < 2) yield args.get(0);
                try {
                    double val       = Double.parseDouble(args.get(0));
                    double precision = Double.parseDouble(args.get(1));
                    if (precision <= 0) {
                        yield String.valueOf(Math.round(val));
                    }
                    long rounded = Math.round(val / precision) * (long) precision;
                    yield String.valueOf(rounded);
                } catch (NumberFormatException e) {
                    yield args.get(0);
                }
            }

            case "abs" -> {
                if (args.isEmpty()) yield "0";
                try {
                    yield String.valueOf(Math.abs(Double.parseDouble(args.get(0))));
                } catch (NumberFormatException e) {
                    yield args.get(0);
                }
            }

            case "format_number" -> {
                if (args.isEmpty()) yield "0";
                try {
                    double val = Double.parseDouble(args.get(0));
                    DecimalFormat df = new DecimalFormat(
                        "#,##0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
                    yield df.format(val);
                } catch (NumberFormatException e) {
                    yield args.get(0);
                }
            }

            case "default" -> {
                if (args.isEmpty()) yield "";
                String v = args.get(0);
                String fallback = args.size() > 1 ? args.get(1) : "";
                yield (v == null || v.isBlank()) ? fallback : v;
            }

            case "replace" -> {
                if (args.size() < 3) yield args.isEmpty() ? "" : args.get(0);
                yield args.get(0).replace(args.get(1), args.get(2));
            }

            case "if" -> {
                if (args.size() < 2) yield "";
                String cond     = args.get(0);
                String trueVal  = args.get(1);
                String falseVal = args.size() > 2 ? args.get(2) : "";
                yield evalCondition(cond, vars) ? trueVal : falseVal;
            }

            case "color" -> {
                // Strip MiniMessage tags to return a plain-text value
                if (args.isEmpty()) yield "";
                yield args.get(0).replaceAll("<[^>]+>", "");
            }

            case "json" -> {
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < args.size(); i++) {
                    if (i > 0) json.append(",");
                    json.append("\"")
                        .append(args.get(i).replace("\\", "\\\\").replace("\"", "\\\""))
                        .append("\"");
                }
                json.append("]");
                yield json.toString();
            }

            case "distance_to_player" -> {
                // Distance from executor to egg
                try {
                    Data data = DragonEggAPI.getData();
                    if (data == null) yield "0";
                    ServerPlayer executor = null;
                    // Retrieve executor from vars marker
                    String execUuid = vars.get("executor_uuid");
                    MinecraftServer server = DragonsLegacyMod.server;
                    if (execUuid != null && !execUuid.isEmpty() && server != null) {
                        executor = server.getPlayerList().getPlayer(UUID.fromString(execUuid));
                    }
                    if (executor == null) yield "0";
                    double dx = executor.getX() - data.getPosition().x;
                    double dy = executor.getY() - data.getPosition().y;
                    double dz = executor.getZ() - data.getPosition().z;
                    double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    yield String.valueOf(Math.round(dist));
                } catch (Exception e) {
                    yield "0";
                }
            }

            default -> name + "(" + String.join(",", args) + ")";
        };
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /**
     * Replaces {@code {varName}} tokens in {@code input} using the provided map.
     * Tokens whose name is not in the map are left unchanged.
     */
    private static String substituteVars(String input, Map<String, String> vars) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < input.length()) {
            char c = input.charAt(i);
            if (c == '{') {
                int end = input.indexOf('}', i + 1);
                if (end < 0) {
                    sb.append(c);
                    i++;
                } else {
                    String varName = input.substring(i + 1, end);
                    if (vars.containsKey(varName)) {
                        sb.append(vars.get(varName));
                    } else {
                        sb.append('{').append(varName).append('}');
                    }
                    i = end + 1;
                }
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }

    /**
     * Splits a comma-separated argument string, respecting single- and double-quoted
     * strings so that commas inside quotes are not treated as separators.
     */
    static List<String> splitArgs(String argsStr) {
        List<String> result = new ArrayList<>();
        if (argsStr == null || argsStr.isEmpty()) return result;

        StringBuilder current = new StringBuilder();
        boolean inQuote = false;
        char quoteChar  = 0;

        for (int i = 0; i < argsStr.length(); i++) {
            char c = argsStr.charAt(i);
            if (inQuote) {
                if (c == quoteChar) {
                    inQuote = false; // closing quote — don't include quote char
                } else {
                    current.append(c);
                }
            } else if (c == '\'' || c == '"') {
                inQuote   = true;
                quoteChar = c;
            } else if (c == ',') {
                result.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        // Add last segment even if empty (guards against trailing comma)
        String last = current.toString().trim();
        if (!last.isEmpty() || !result.isEmpty()) {
            result.add(last);
        }
        return result;
    }

    /**
     * Strips a single pair of leading/trailing single- or double-quotes from {@code s}.
     */
    private static String stripQuotes(String s) {
        if (s == null) return "";
        if (s.length() >= 2) {
            char first = s.charAt(0);
            char last  = s.charAt(s.length() - 1);
            if ((first == '\'' && last == '\'') || (first == '"' && last == '"')) {
                return s.substring(1, s.length() - 1);
            }
        }
        return s;
    }
}
