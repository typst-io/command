package io.typst.command.bukkit;

import io.typst.command.Argument;
import io.typst.command.CommandHelp;
import io.typst.command.CommandSpec;
import lombok.Value;
import lombok.With;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

@Value(staticConstructor = "of")
@With
public class BukkitCommandHelp {
    CommandSender sender;
    String label;
    List<String> arguments;
    CommandSpec spec;
    String language; //  en, ko

    public CommandHelp toHelp() {
        return CommandHelp.of(
                getLabel(),
                getArguments(),
                getSpec(),
                getLanguage()
        );
    }

    public static String format(BukkitCommandHelp help) {
        CommandSpec spec = help.getSpec();
        String permission = spec.getPermission();
        // check permission
        if (permission.isEmpty() || help.getSender().hasPermission(permission)) {
            CommandSpec newSpec = help.getLanguage().equalsIgnoreCase("ko_kr")
                    ? spec.withArguments(spec.getArguments().stream()
                    .map(arg -> arg.withName(translateToKor(arg.getName())))
                    .collect(Collectors.toList()))
                    : spec;
            return CommandHelp.format(help.toHelp().withSpec(newSpec));
        }
        return "";
    }

    public static String translateToKor(String argumentName) {
        switch (argumentName) {
            case "player":
            case "offlineplayer":
                return "유저";
            case "material":
                return "아이템";
            case "potion":
                return "포션";
            case "enchant":
                return "인챈트";
        }
        return argumentName;
    }
}
