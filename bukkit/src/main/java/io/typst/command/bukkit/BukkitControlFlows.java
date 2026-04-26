package io.typst.command.bukkit;

import io.typst.command.*;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BukkitControlFlows {
    public static LangKey getLocale(CommandSender sender) {
        if (sender instanceof Player) {
            return LangKey.getLanguageKeyFrom(((Player) sender).getLocale());
        }
        return LangKey.getJVMDefaultLanguage();
    }

    public static Player getPlayerOrThrow(CommandSender sender) {
        if (sender instanceof Player) {
            return ((Player) sender);
        } else {
            throw new CommandCancellationException(MessageKey.INGAME_PLAYER_ONLY);
        }
    }

    public static ItemStack getHandItemOrThrow(Player player) {
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem != null && handItem.getType() != Material.AIR) {
            return handItem;
        }
        throw new CommandCancellationException(MessageKey.NO_ITEM_IN_MAIN_HAND);
    }

    static void validatePermission(Command<?> node, CommandSender sender) {
        String perm = CommandSpec.from(node).getPermission();
        if (!perm.isEmpty() && !sender.hasPermission(perm)) {
            CommandCancellationException th = new CommandCancellationException(MessageKey.NO_PERMISSION);
            th.setMessageArgs(new Object[]{perm});
            throw th;
        }
    }
}
