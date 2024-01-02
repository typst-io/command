package io.typecraft.command.bukkit;

import io.typecraft.command.Command;
import io.typecraft.command.CommandCancellationException;
import io.typecraft.command.CommandSpec;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

public class BukkitControlFlows {
    public static Player tryGetPlayer(CommandSender sender) {
        if (sender instanceof Player) {
            return ((Player) sender);
        } else {
            String message = Locale.getDefault().getLanguage().equals("ko")
                    ? "§c게임에 접속해서 사용해주세요!"
                    : "§cIngame player only!";
            throw new CommandCancellationException(message);
        }
    }

    static void tryAccessPermission(Command<?> node, CommandSender sender) {
        String perm = CommandSpec.from(node).getPermission();
        if (!perm.isEmpty() && !sender.hasPermission(perm)) {
            String message = Locale.getDefault().getLanguage().equals("ko")
                    ? "§c권한이 없습니다! `" + perm + "`"
                    : "§cNo permission! `" + perm + "`";
            throw new CommandCancellationException(message);
        }
    }
}
