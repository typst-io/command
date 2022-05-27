package io.typecraft.command.bukkit;

import io.typecraft.command.Command;
import org.bukkit.ChatColor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static io.typecraft.command.Argument.intArg;
import static io.typecraft.command.Argument.strArg;
import static io.typecraft.command.Command.pair;
import static java.util.function.Function.identity;

public class CommandTest {
    @Test
    public void help() {
        List<String> msgs = BukkitCommand.getCommandUsages(
                Collections.emptyMap(),
                "mycmd",
                new String[0],
                1,
                Command.mapping(
                        pair("a", Command.present(null)),
                        pair("b", Command.argument(identity(), strArg)),
                        pair("c", Command.argument((a, b) -> null, strArg, intArg)),
                        pair("d", Command.argument((a, b) -> null, strArg, intArg).withDescription("desc"))
                )
        ).stream().map(ChatColor::stripColor).collect(Collectors.toList());
        Assertions.assertEquals(
                Arrays.asList(
                        "/mycmd a",
                        "/mycmd b (string)",
                        "/mycmd c (string) (int)",
                        "/mycmd d (string) (int) - desc"
                ),
                msgs
        );
    }
}
