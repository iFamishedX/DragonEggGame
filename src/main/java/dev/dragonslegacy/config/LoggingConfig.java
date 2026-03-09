package dev.dragonslegacy.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

/**
 * Logging configuration loaded from {@code config/dragonslegacy/logging.yaml}.
 *
 * <p>Controls which categories of log output are written to the console and
 * sent to online operators.
 */
@ConfigSerializable
public class LoggingConfig {

    public LoggingSection logging = new LoggingSection();

    @ConfigSerializable
    public static class LoggingSection {

        public boolean enabled = true;

        public LogTarget messages          = new LogTarget(true, true);

        @org.spongepowered.configurate.objectmapping.meta.Setting("config_validation")
        public LogTarget configValidation  = new LogTarget(true, true);

        @org.spongepowered.configurate.objectmapping.meta.Setting("state_changes")
        public LogTarget stateChanges      = new LogTarget(true, true);

        @org.spongepowered.configurate.objectmapping.meta.Setting("egg_events")
        public LogTarget eggEvents         = new LogTarget(true, true);

        @org.spongepowered.configurate.objectmapping.meta.Setting("ability_events")
        public LogTarget abilityEvents     = new LogTarget(true, true);

        public LogTarget errors            = new LogTarget(true, true);
    }

    @ConfigSerializable
    public static class LogTarget {

        public boolean console = true;
        public boolean ops     = true;

        public LogTarget() {}

        public LogTarget(boolean console, boolean ops) {
            this.console = console;
            this.ops     = ops;
        }
    }
}
