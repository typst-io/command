package io.typecraft.command.bukkit;

import io.typecraft.command.CommandConfig;
import lombok.Value;
import lombok.With;

import java.util.function.Function;

@Value(staticConstructor = "of")
@With
public class BukkitCommandConfig {
    Function<BukkitCommandHelp, String> formatter;
    public static final BukkitCommandConfig empty = new BukkitCommandConfig(BukkitCommandHelp::format);

    public static BukkitCommandConfig from(CommandConfig config) {
        return new BukkitCommandConfig(help -> config.getFormatter().apply(help.toHelp()));
    }
}
