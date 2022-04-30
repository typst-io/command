package io.typecraft.command.bukkit;

import io.typecraft.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;

import static io.typecraft.command.Command.pair;

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

    // MyCommand = Reload | Hello
    private interface MyCommand {
        Command<MyCommand> command = Command.map(
                pair("reload", Command.present(new MyCommand.Reload())),
                pair("hello", Command.present(new MyCommand.Hello()))
        );

        class Reload implements MyCommand {
        }

        class Hello implements MyCommand {
        }
    }
}
