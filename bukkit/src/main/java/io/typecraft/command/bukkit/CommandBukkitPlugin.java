package io.typecraft.command.bukkit;

import io.typecraft.command.Command;
import lombok.Data;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;

import static io.typecraft.command.Argument.strArg;
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
        } else if (command instanceof MyCommand.ItemAdd) {
            MyCommand.ItemAdd itemAdd = (MyCommand.ItemAdd) command;
            sender.sendMessage("Add item! " + itemAdd.getName());
        } else if (command instanceof MyCommand.ItemRemove) {
            MyCommand.ItemRemove itemRemove = (MyCommand.ItemRemove) command;
            sender.sendMessage("Remove item! " + itemRemove.getName());
        }
    }

    // MyCommand = Reload | Hello
    private interface MyCommand {
        Command<MyCommand> command = Command.compound(
                pair("item", Command.compound(
                        pair("add", Command.argument(ItemAdd::new, strArg.withName("이름"))
                                .withDescription("아이템을 추가합니다.")),
                        pair("remove", Command.argument(ItemRemove::new, strArg.withName("이름"))
                                .withDescription("아이템을 제거합니다."))
                )),
                pair("reload", Command.present(new MyCommand.Reload()).withDescription("리로드합니다."))
        );

        class Reload implements MyCommand {
        }

        @Data
        class ItemAdd implements MyCommand {
            private final String name;
        }

        @Data
        class ItemRemove implements MyCommand {
            private final String name;
        }
    }
}
