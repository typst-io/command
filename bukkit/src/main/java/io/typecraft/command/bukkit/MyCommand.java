package io.typecraft.command.bukkit;

import io.typecraft.command.Command;
import io.typecraft.command.LangId;
import io.typecraft.command.config.CommandConfig;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static io.typecraft.command.Argument.intArg;
import static io.typecraft.command.Argument.strArg;
import static io.typecraft.command.Command.pair;

// MyCommand = Reload | ItemAdd | ItemRemove | ItemPage
public interface MyCommand {
    LangId langAddItem = LangId.of("add-item").withMessage("아이템을 추가합니다.");
    LangId langRemoveItem = LangId.of("remove-item").withMessage("아이템을 제거합니다.");
    LangId langPage = LangId.of("page").withMessage("페이지");
    LangId langReload = LangId.of("reload").withMessage("리로드합니다.");
    Set<LangId> allLangs = new HashSet<>(Arrays.asList(
            langAddItem,
            langRemoveItem,
            langPage,
            langReload
    ));
    Command<MyCommand> node = Command.mapping(
            pair("item", Command.mapping(
                    pair("add", Command.argument(ItemAdd::new, strArg)
                            .withDescriptionId(langAddItem)),
                    pair("remove", Command.argument(ItemRemove::new, strArg)
                            .withDescriptionId(langRemoveItem)),
                    pair("page", Command.argument(ItemPage::new, intArg.withId(langPage)))
            )),
            pair("reload", Command.present(new MyCommand.Reload()).withDescriptionId(langReload))
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

    @Data
    class ItemPage implements MyCommand {
        private final Number page;
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
        } else if (command instanceof MyCommand.ItemAdd) {
            MyCommand.ItemAdd itemAdd = (MyCommand.ItemAdd) command;
            sender.sendMessage("Add item! " + itemAdd.getName());
        } else if (command instanceof MyCommand.ItemRemove) {
            MyCommand.ItemRemove itemRemove = (MyCommand.ItemRemove) command;
            sender.sendMessage("Remove item! " + itemRemove.getName());
        } else if (command instanceof MyCommand.ItemPage) {
            MyCommand.ItemPage itemPage = ((MyCommand.ItemPage) command);
            sender.sendMessage("Paging item! " + itemPage.getPage());
        }
    }
}