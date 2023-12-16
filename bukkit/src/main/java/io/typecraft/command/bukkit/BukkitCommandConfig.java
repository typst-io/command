package io.typecraft.command.bukkit;

import io.typecraft.command.CommandConfig;
import lombok.Value;
import lombok.With;

import java.util.function.Function;

@Value(staticConstructor = "of")
@With
public class BukkitCommandConfig {
    Function<BukkitCommandHelp, String> formatter;
    boolean hideNoPermissionCommands;
    public static final BukkitCommandConfig empty = new BukkitCommandConfig(BukkitCommandHelp::format, true);

    public static BukkitCommandConfig from(CommandConfig config) {
        return new BukkitCommandConfig(help -> config.getFormatter().apply(help.toHelp()), config.isHideNoPermissionCommands());
    }
}
