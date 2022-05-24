package io.typecraft.command.bukkit;

import io.typecraft.command.i18n.MessageId;
import io.typecraft.command.bukkit.config.BukkitCommandConfig;
import io.typecraft.command.bukkit.i18n.BukkitLangId;
import io.typecraft.command.config.CommandConfig;
import io.typecraft.command.i18n.Language;
import io.typecraft.command.i18n.PluginLanguage;
import io.vavr.Tuple2;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandBukkitPlugin extends JavaPlugin {
    private Map<String, Set<MessageId>> extraPluginLangIds = Collections.emptyMap();
    private CommandConfig commandConfig = CommandConfig.of(Locale.getDefault(), BukkitLangId.defaultLangs, Collections.emptyMap());

    @Override
    public void onLoad() {
        addPluginLangIds(MyCommand.allLangs, this);
    }

    @Override
    public void onEnable() {
        commandConfig = loadCommandConfig();
        BukkitCommand.register(
                "bukkitcommand",
                MyCommand::execute,
                (sender, cmd) -> Collections.emptyList(),
                this,
                MyCommand.node
        );
    }

    public CommandConfig getCommandConfig() {
        return commandConfig;
    }

    void setCommandConfig(CommandConfig commandConfig) {
        this.commandConfig = commandConfig;
    }

    public void addPluginLangIds(Set<MessageId> ids, Plugin plugin) {
        Map<String, Set<MessageId>> newPluginLangIds = new LinkedHashMap<>(extraPluginLangIds);
        Set<MessageId> messageIds = new HashSet<>(newPluginLangIds.getOrDefault(plugin.getName(), Collections.emptySet()));
        messageIds.addAll(ids);
        newPluginLangIds.put(plugin.getName(), messageIds);
        this.extraPluginLangIds = newPluginLangIds;
    }

    private Map<String, Map<Locale, Map<String, String>>> getDefaultPluginLangs() {
        Map<String, Map<Locale, Map<String, String>>> pluginLangs = new HashMap<>();
        for (Map.Entry<String, Set<MessageId>> pair : extraPluginLangIds.entrySet()) {
            String pluginName = pair.getKey();
            Set<MessageId> ids = pair.getValue();
            Map<Locale, Map<String, String>> langs = pluginLangs.computeIfAbsent(pluginName, k -> new HashMap<>());
            Map<String, String> messages = ids.stream()
                    .map(id -> new Tuple2<>(id.getId(), id.getMessage()))
                    .collect(Collectors.toMap(Tuple2::_1, Tuple2::_2));
            langs.put(Locale.KOREAN, messages);
            langs.put(Locale.ENGLISH, messages);
        }
        return pluginLangs;
    }

    CommandConfig loadCommandConfig() {
        // load
        CommandConfig config = BukkitCommandConfig.load(
                getConfigFile(),
                getBaseLangFiles(),
                getPluginLangFiles()
        );
        // create default config file if not exists
        if (!getConfigFile().isFile()) {
            YamlConfiguration configYaml = new YamlConfiguration();
            configYaml.set("default-locale", Locale.getDefault().toString());
            try {
                configYaml.save(getConfigFile());
            } catch (IOException e) {
                getLogger().log(Level.WARNING, e, () -> "Error while create default config.yml");
            }
        }
        // create default base lang files if not exists
        for (Map.Entry<Locale, Map<String, String>> pair : BukkitLangId.defaultLangs.entrySet()) {
            Locale locale = pair.getKey();
            Map<String, String> defaultMessages = pair.getValue();
            if (!config.getBaseLangs().containsKey(locale)) {
                YamlConfiguration baseLangYaml = new YamlConfiguration();
                new Language(locale, defaultMessages)
                        .toMap()
                        .forEach(baseLangYaml::set);
                File file = new File(getLangFolder(), locale + ".yml");
                try {
                    baseLangYaml.save(file);
                } catch (IOException e) {
                    getLogger().log(Level.INFO, "Error while creating default base lang: " + file.getAbsolutePath());
                }
            }
        }
        // create default plugin lang files if not exists
        for (Map.Entry<String, Map<Locale, Map<String, String>>> pluginPair : getDefaultPluginLangs().entrySet()) {
            String pluginName = pluginPair.getKey();
            for (Map.Entry<Locale, Map<String, String>> pair : pluginPair.getValue().entrySet()) {
                Locale locale = pair.getKey();
                Map<String, String> defaultMessages = pair.getValue();
                Map<Locale, Map<String, String>> pluginLangs = config.getPluginLangs().getOrDefault(pluginName, Collections.emptyMap());
                if (!pluginLangs.containsKey(locale)) {
                    YamlConfiguration pluginLangYaml = new YamlConfiguration();
                    new PluginLanguage(pluginName, locale, defaultMessages)
                            .toMap()
                            .forEach(pluginLangYaml::set);
                    File file = new File(getLangFolder(), pluginName + "/" + locale + ".yml");
                    try {
                        pluginLangYaml.save(file);
                    } catch (IOException e) {
                        getLogger().log(Level.INFO, "Error while creating default plugin lang: " + file.getAbsolutePath());
                    }
                }
            }
        }
        return config;
    }

    // plugins/BukkitCommand/config.yml
    private File getConfigFile() {
        return new File(getDataFolder(), "config.yml");
    }

    // plugins/BukkitCommand/langs
    private File getLangFolder() {
        return new File(getDataFolder(), "langs");
    }

    // plugins/BukkitCommand/langs/*.yml
    private List<File> getBaseLangFiles() {
        return getFolderFiles(getLangFolder())
                .filter(file -> file.isFile() && file.getName().endsWith(".yml"))
                .collect(Collectors.toList());
    }

    // plugins/BukkitCommand/langs/**/*.yml
    private List<File> getPluginLangFiles() {
        return getFolderFiles(getLangFolder())
                .flatMap(CommandBukkitPlugin::getFolderFiles)
                .filter(file -> file.isFile() && file.getName().endsWith(".yml"))
                .collect(Collectors.toList());
    }

    public static CommandBukkitPlugin get() {
        return (CommandBukkitPlugin) Bukkit.getPluginManager().getPlugin("BukkitCommand");
    }

    private static Stream<File> getFolderFiles(File folder) {
        File[] files = folder.listFiles();
        return files != null ? Arrays.stream(files) : Stream.empty();
    }
}
