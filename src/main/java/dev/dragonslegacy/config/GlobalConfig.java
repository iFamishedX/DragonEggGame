package dev.dragonslegacy.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.ArrayList;
import java.util.List;

/**
 * Global configuration loaded from {@code config/dragonslegacy/global.yaml}.
 *
 * <p>Merges what was previously split across {@code config.yaml} and
 * {@code commands.yaml}: the global permission API toggle plus all command
 * names, aliases, and per-command permission/op-level settings.
 */
@ConfigSerializable
public class GlobalConfig {

    @Setting("config_version")
    public int configVersion = 1;

    /**
     * If {@code true}, LuckPerms permission nodes are used to gate commands.
     * If {@code false}, vanilla operator levels are used instead.
     */
    @Setting("permissions_api")
    public boolean permissionsApi = true;

    public CommandsSection commands = new CommandsSection();

    // =========================================================================
    // CommandsSection
    // =========================================================================

    @ConfigSerializable
    public static class CommandsSection {

        public String root = "dragonslegacy";

        public List<String> aliases = new ArrayList<>(List.of("dl"));

        public CommandEntry help         = new CommandEntry("dragonslegacy.command.help",         0);
        public CommandEntry bearer       = new CommandEntry("dragonslegacy.command.bearer",       0);
        public CommandEntry hunger       = new CommandEntry("dragonslegacy.command.hunger",       0);
        public CommandEntry reload       = new CommandEntry("dragonslegacy.command.reload",       3);
        public CommandEntry placeholders = new CommandEntry("dragonslegacy.command.placeholders", 0);
        public CommandEntry debug        = new CommandEntry("dragonslegacy.admin.debug",          3);
    }

    // =========================================================================
    // CommandEntry
    // =========================================================================

    @ConfigSerializable
    public static class CommandEntry {

        @Setting("permission_node")
        public String permissionNode = "";

        @Setting("op_level")
        public int opLevel = 0;

        public CommandEntry() {}

        public CommandEntry(String permissionNode, int opLevel) {
            this.permissionNode = permissionNode;
            this.opLevel = opLevel;
        }
    }
}
