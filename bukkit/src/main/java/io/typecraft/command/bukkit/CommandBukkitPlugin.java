package io.typecraft.command.bukkit;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;

public class CommandBukkitPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        BukkitCommand.register(
                "bukkitcommand",
                CommandBukkitPlugin::execute,
                (sender, cmd) -> Collections.emptyList(),
                this,
                MyCommand.command
        );
    }

    private static void execute(CommandSender sender, MyCommand command) {
        if (command instanceof MyCommand.Reload) {
            sender.sendMessage("Reloading..");
            // Reload!
        } else if (command instanceof MyCommand.Hello) {
            sender.sendMessage("Hello!");
        }
    }
}
