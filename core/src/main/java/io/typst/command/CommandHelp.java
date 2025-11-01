package io.typst.command;

import lombok.Value;
import lombok.With;

import java.util.List;
import java.util.stream.Collectors;

@Value(staticConstructor = "of")
@With
public class CommandHelp {
    String label;
    List<String> arguments;
    CommandSpec spec;
    String language;


    public static String format(CommandHelp help) {
        String label = help.getLabel();
        CommandSpec spec = help.getSpec();
        List<String> args = help.getArguments();
        String argSuffix;
        String space = args.isEmpty() ? "" : " ";
        if (help.getLanguage().equals("ko_kr")) {
            argSuffix = spec.getArguments().stream().anyMatch(arg -> !arg.getName().isEmpty())
                    ? space + "§e" + spec.getArguments().stream()
                    .map(s -> String.format("(%s)", translateToKor(s.getName())))
                    .collect(Collectors.joining(" "))
                    : "";
        } else {
            argSuffix = spec.getArguments().stream().anyMatch(arg -> !arg.getName().isEmpty())
                    ? space + "§e" + spec.getArguments().stream()
                    .map(arg -> String.format("(%s)", arg.getName()))
                    .collect(Collectors.joining(" "))
                    : "";
        }
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
