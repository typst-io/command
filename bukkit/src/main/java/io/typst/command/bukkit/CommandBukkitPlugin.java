package io.typst.command.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandBukkitPlugin extends JavaPlugin {
    public static CommandBukkitPlugin get() {
        return (CommandBukkitPlugin) Bukkit.getPluginManager().getPlugin("BukkitCommand");
    }

    @Override
    public void onEnable() {
        BukkitCommands.register(
                "bukkitcommand",
                ExampleCommand.node,
                ExampleCommand::execute,
                this
        );
    }
}
