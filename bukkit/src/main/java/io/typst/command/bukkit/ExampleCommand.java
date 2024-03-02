package io.typst.command.bukkit;

import io.typst.command.Command;
import lombok.Data;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;

import static io.typst.command.Command.pair;
import static io.typst.command.bukkit.BukkitArguments.enchantArg;
import static io.typst.command.bukkit.BukkitArguments.potionArg;

public interface ExampleCommand {
    Command<ExampleCommand> node = Command.mapping(
            pair("potion", Command.argument(Potion::new, potionArg)),
            pair("enchant", Command.argument(Enchant::new, enchantArg))
    );

    @Data
    class Potion implements ExampleCommand {
        private final PotionEffectType potion;
    }

    @Data
    class Enchant implements ExampleCommand {
        private final Enchantment enchantment;
    }

    static void execute(CommandSender sender, ExampleCommand x) {
        if (x instanceof Potion) {
            sender.sendMessage("입력하신 포션: " + ((Potion) x).getPotion().getName());
        } else if (x instanceof Enchant) {
            sender.sendMessage("입력하신 인챈트: " + ((Enchant) x).getEnchantment().getName());
        }
    }
}
