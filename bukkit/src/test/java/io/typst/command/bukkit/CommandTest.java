package io.typst.command.bukkit;

import io.typst.command.Argument;
import io.typst.command.Command;
import org.bukkit.ChatColor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.typst.command.Command.pair;
import static io.typst.command.StandardArguments.intArg;
import static io.typst.command.StandardArguments.strArg;
import static java.util.function.Function.identity;

public class CommandTest {
    private static final Argument<String> strArgTab = strArg.withTabCompletes(() -> Collections.singletonList("tab"));
    private static final Command.Mapping<Object> command = Command.mapping(
            pair("a", Command.present(null)),
            pair("b", Command.argument(identity(), strArg)),
            pair("c", Command.argument((a, b) -> null, strArg, intArg)),
            pair("d", Command.argument((a, b) -> null, strArg, intArg).withDescription("desc")),
            pair("e", Command.mapping(
                    pair("a", Command.present(null))
            )),
            pair("f", Command.argument((a, b) -> null, strArgTab, intArg).withDescription("desc").withPermission("test.permission"))
    ).withFallback(Command.argument(identity(), strArg));

    @Test
    public void help() {
        StringBuilder output = new StringBuilder();
        MockPlayer player = new MockPlayer(new MockSender(output), "ko", UUID.randomUUID());
        List<String> msgs = BukkitCommands.getCommandUsages(
                player,
                "mycmd",
                new String[0],
                1,
                command,
                BukkitCommandConfig.empty
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

    @Test
    public void helpFallback() {
        StringBuilder output = new StringBuilder();
        MockPlayer player = new MockPlayer(new MockSender(output), "ko", UUID.randomUUID());
        List<String> msgs = BukkitCommands.getCommandUsages(
                player,
                "mycmd",
                new String[0],
                1,
                command.getFallback().orElse(null),
                BukkitCommandConfig.empty
        ).stream().map(ChatColor::stripColor).collect(Collectors.toList());
        Assertions.assertEquals(
                Collections.singletonList(
                        "/mycmd (문자열)"
                ),
                msgs
        );
    }

    @Test
    public void helpSingle() {
        StringBuilder output = new StringBuilder();
        MockPlayer player = new MockPlayer(new MockSender(output), "ko", UUID.randomUUID());
        BukkitCommands.execute(player, "mycmd", new String[]{"d"}, command, BukkitCommandConfig.empty);
        Assertions.assertEquals(" \n/mycmd d (문자열) (정수) - desc\n잘못된 명령어입니다!\n", output.toString());
        System.out.println(output);
    }

    @Test
    public void tabCompleteOnArgThatNoPermission() {
        StringBuilder output = new StringBuilder();
        MockPlayer player = new MockPlayer(new MockSender(output), "ko", UUID.randomUUID());
        List<String> completes = BukkitCommands.tabComplete(player, new String[]{"f", ""}, command, (sender, x) -> Collections.emptyList());
        Assertions.assertEquals(Collections.emptyList(), completes);
    }
}
