package io.typecraft.command.bukkit;

import io.typecraft.command.Command;
import org.bukkit.ChatColor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.typecraft.command.Command.pair;
import static io.typecraft.command.StandardArguments.intArg;
import static io.typecraft.command.StandardArguments.strArg;
import static java.util.function.Function.identity;

public class CommandTest {
    @Test
    public void help() {
        List<String> msgs = BukkitCommands.getCommandUsages(
                new MockSender(),
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
                        )),
                        pair("f", Command.argument((a, b) -> null, strArg, intArg).withDescription("desc").withPermission("test.permission"))
                ),
                BukkitCommandHelp::format
        ).stream().map(ChatColor::stripColor).collect(Collectors.toList());
        Assertions.assertEquals(
                Arrays.asList(
                        "/mycmd a",
                        "/mycmd b (문자열)",
                        "/mycmd c (문자열) (정수)",
                        "/mycmd d (문자열) (정수) - desc",
                        "/mycmd e a"
                ),
                msgs
        );
    }
}
