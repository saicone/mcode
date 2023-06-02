package com.saicone.mcode.bukkit.settings;

import com.saicone.mcode.bukkit.util.ServerInstance;
import com.saicone.mcode.module.settings.Settings;
import com.saicone.mcode.module.settings.SettingsParser;
import com.saicone.mcode.module.settings.node.SettingsNode;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.io.Writer;

public class YamlParser extends SettingsParser {

    private static final boolean ALLOW_COMMENTS = ServerInstance.verNumber >= 19;

    public static void register() {
        if (!SettingsParser.contains("yml")) {
            SettingsParser.register("yml", YamlParser::new);
        }
        if (!SettingsParser.contains("yaml")) {
            SettingsParser.register("yaml", YamlParser::new);
        }
    }

    @Override
    public Settings read(@NotNull Reader reader) throws Throwable {
        final YamlConfiguration config = new YamlConfiguration();
        config.options().parseComments(true);
        config.load(reader);
        final Settings settings = new Settings();
        for (String key : config.getKeys(true)) {
            final String[] path = key.split("\\.");
            final SettingsNode node = new SettingsNode(path[path.length - 1], config.get(key));
            if (ALLOW_COMMENTS) {
                node.setTopComment(config.getComments(key));
                node.setSideComment(config.getInlineComments(key));
            }
            settings.set(path, node, true);
        }
        return settings;
    }

    @Override
    public void write(@NotNull Settings settings, @NotNull Writer writer) throws Throwable {
        final YamlConfiguration config = new YamlConfiguration();
        config.options().parseComments(true);
        write("", settings, config);
        writer.write(config.saveToString());
    }

    public void write(@NotNull String path, @NotNull Settings settings, @NotNull YamlConfiguration config) {
        for (SettingsNode node : settings.getValue()) {
            final String key = path + node.getKey();
            if (node instanceof Settings) {
                write(key + '.', (Settings) node, config);
            } else {
                config.set(key, node.getValue());
            }
            if (ALLOW_COMMENTS) {
                config.setComments(key, node.getTopComment());
                config.setInlineComments(key, node.getSideComment());
            }
        }
    }
}
