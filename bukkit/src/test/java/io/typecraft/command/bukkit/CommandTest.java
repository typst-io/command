package io.typecraft.command.bukkit;

import io.typecraft.command.Command;
import io.typecraft.command.CommandHelp;
import org.bukkit.ChatColor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.typecraft.command.StandardArguments.intArg;
import static io.typecraft.command.StandardArguments.strArg;
import static io.typecraft.command.Command.pair;
import static java.util.function.Function.identity;

public class CommandTest {
    @Test
    public void help() {
        List<String> msgs = BukkitCommands.getCommandUsages(
                CommandHelp::format,
                "mycmd",
                new String[0],
                1,
                Command.mapping(
                        pair("a", Command.present(null)),
                        pair("b", Command.argument(identity(), strArg)),
                        pair("c", Command.argument((a, b) -> null, strArg, intArg)),
                        pair("d", Command.argument((a, b) -> null, strArg, intArg).withDescription("desc")),
                        pair("e", Command.mapping(
                                pair("a", Command.present(null))
                        ))
                )
        ).stream().map(ChatColor::stripColor).collect(Collectors.toList());
        Assertions.assertEquals(
                Arrays.asList(
                        "/mycmd a",
                        "/mycmd b (string)",
                        "/mycmd c (string) (int)",
                        "/mycmd d (string) (int) - desc",
                        "/mycmd e a"
                ),
                msgs
        );
    }
}
