package io.typst.command.bukkit;

import io.typst.command.CommandConfig;
import lombok.Value;
import lombok.With;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

@Value(staticConstructor = "of")
@With
public class BukkitCommandConfig {
    Function<BukkitCommandHelp, String> formatter;
    boolean hideNoPermissionCommands;
    @Nullable String unknownSubCommandMessage;
    @Nullable String invalidCommandMessage;
    public static final BukkitCommandConfig empty = new BukkitCommandConfig(BukkitCommandHelp::format, true, null, null);

    public static BukkitCommandConfig from(CommandConfig config) {
        return new BukkitCommandConfig(help -> config.getFormatter().apply(help.toHelp()), config.isHideNoPermissionCommands(), null, null);
    }
}
