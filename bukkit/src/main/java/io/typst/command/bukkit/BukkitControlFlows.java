package io.typst.command.bukkit;

import io.typst.command.Command;
import io.typst.command.CommandCancellationException;
import io.typst.command.CommandSpec;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class BukkitControlFlows {
    public static String getLocale(CommandSender sender) {
        if (sender instanceof Player) {
            return ((Player) sender).getLocale();
        }
        return Locale.getDefault().getLanguage();
    }

    public static Player getPlayerOrThrow(CommandSender sender) {
        if (sender instanceof Player) {
            return ((Player) sender);
        } else {
            String message = Locale.getDefault().getLanguage().equals("ko")
                    ? "§c게임에 접속해서 사용해주세요!"
                    : "§cIngame player only!";
            throw new CommandCancellationException(message);
        }
    }

    public static ItemStack getHandItemOrThrow(Player player) {
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem != null && handItem.getType() != Material.AIR) {
            return handItem;
        }
        String message = Locale.getDefault().getLanguage().equals("ko")
                ? "§c손에 아이템을 들어주세요!"
                : "§cPlease hold an item on your main hand!";
        throw new CommandCancellationException(message);
    }

    static void validatePermission(Command<?> node, CommandSender sender) {
        String perm = CommandSpec.from(node).getPermission();
        if (!perm.isEmpty() && !sender.hasPermission(perm)) {
            String message = Locale.getDefault().getLanguage().equals("ko")
                    ? "§c권한이 없습니다! `" + perm + "`"
                    : "§cNo permission! `" + perm + "`";
            throw new CommandCancellationException(message);
        }
    }
}
