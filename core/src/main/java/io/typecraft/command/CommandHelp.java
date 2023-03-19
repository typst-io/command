package io.typecraft.command;

import lombok.Data;
import lombok.With;

import java.util.List;
import java.util.stream.Collectors;

@Data(staticConstructor = "of")
@With
public class CommandHelp {
    private final String label;
    private final List<String> arguments;
    private final CommandSpec spec;

    public static String format(CommandHelp help) {
        String label = help.getLabel();
        CommandSpec spec = help.getSpec();
        List<String> args = help.getArguments();
        String argSuffix = spec.getArguments().stream().anyMatch(id -> !id.isEmpty())
                ? " §e" + spec.getArguments().stream()
                .map(s -> String.format("(%s)", translateToKor(s)))
                .collect(Collectors.joining(" "))
                : "";
        String description = spec.getDescription();
        String descSuffix = description.isEmpty()
                ? ""
                : " §f- " + description;
        return String.format("§a/%s %s", label, String.join(" ", args)) + argSuffix + descSuffix;
    }

    public static String translateToKor(String argumentName) {
        switch (argumentName) {
            case "string":
                return "문자열";
            case "int":
            case "long":
                return "정수";
            case "float":
            case "double":
                return "실수";
            case "bool":
                return "bool";
            case "strings":
                return "문자열..";
        }
        return argumentName;
    }
}
