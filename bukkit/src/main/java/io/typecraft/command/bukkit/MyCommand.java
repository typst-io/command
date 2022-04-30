package io.typecraft.command.bukkit;

import io.typecraft.command.Command;

import static io.typecraft.command.Command.pair;

interface MyCommand {
    Command<MyCommand> command = Command.map(
            pair("reload", Command.present(new MyCommand.Reload())),
            pair("hello", Command.present(new Hello()))
    );

    class Reload implements MyCommand {
    }

    class Hello implements MyCommand {
    }
}
