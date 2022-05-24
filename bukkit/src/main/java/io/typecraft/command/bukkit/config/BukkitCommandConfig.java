package io.typecraft.command.bukkit.config;

import io.typecraft.command.bukkit.BukkitConverters;
import io.typecraft.command.bukkit.i18n.BukkitLangId;
import io.typecraft.command.config.CommandConfig;
import io.typecraft.command.i18n.Language;
import io.typecraft.command.i18n.PluginLanguage;
import lombok.experimental.UtilityClass;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static io.typecraft.command.Converters.toStreamF;

@UtilityClass
public class BukkitCommandConfig {
    private static final CommandConfig defaultBukkitConfig =
            CommandConfig.of(
                    Locale.getDefault(),
                    BukkitLangId.defaultLangs,
                    Collections.emptyMap()
            );

    public static CommandConfig load(
            File configFile,
            List<File> baseLangFiles,
            List<File> pluginLangFiles
    ) {
        return CommandConfig.from(
                loadYamlFile(configFile),
                baseLangFiles.stream()
                        .map(BukkitCommandConfig::loadYamlFile)
                        .map(Language::from)
                        .collect(Collectors.toList()),
                pluginLangFiles.stream()
                        .map(BukkitCommandConfig::loadYamlFile)
                        .flatMap(toStreamF(PluginLanguage::from))
                        .collect(Collectors.toList())
        );
    }

    public static CommandConfig ofDefault() {
        return defaultBukkitConfig;
    }

    private static Map<String, Object> loadYamlFile(File file) {
        return BukkitConverters.normalizeYamlMap(YamlConfiguration.loadConfiguration(file).getValues(false));
    }
}
