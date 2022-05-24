package io.typecraft.command.bukkit;

import io.typecraft.command.Command;
import io.typecraft.command.i18n.MessageId;
import io.typecraft.command.config.CommandConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static io.typecraft.command.Command.pair;

// MyCommand = Reload | ItemAdd | ItemRemove | ItemPage
public interface MyCommand {
    MessageId langReload = MessageId.of("reload").withMessage("리로드합니다.");
    Set<MessageId> allLangs = new HashSet<>(Collections.singletonList(
            langReload
    ));
    Command<MyCommand> node = Command.mapping(
            pair("reload", Command.present(new MyCommand.Reload()).withDescriptionId(langReload))
    );

    class Reload implements MyCommand {
    }

    static void execute(CommandSender sender, MyCommand command) {
        if (command instanceof MyCommand.Reload) {
            sender.sendMessage("Reloading..");
            CommandBukkitPlugin plugin = CommandBukkitPlugin.get();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                CommandConfig config = plugin.loadCommandConfig();
                Bukkit.getScheduler().runTask(plugin, () -> {
                    plugin.setCommandConfig(config);
                    sender.sendMessage("Reloaded!");
                });
            });
        }
    }
}