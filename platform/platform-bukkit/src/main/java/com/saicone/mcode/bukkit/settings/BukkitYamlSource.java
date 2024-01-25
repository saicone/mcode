package com.saicone.mcode.bukkit.settings;

import com.saicone.settings.SettingsNode;
import com.saicone.settings.SettingsSource;
import com.saicone.settings.data.DataFormat;
import com.saicone.settings.node.MapNode;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class BukkitYamlSource implements SettingsSource {

    public static final boolean ALLOW_COMMENTS;

    static {
        boolean allowComments = false;
        try {
            FileConfigurationOptions.class.getDeclaredMethod("parseComments");
            allowComments = true;
        } catch (NoSuchMethodException ignored) { }
        ALLOW_COMMENTS = allowComments;
    }

    public static void register() {
        DataFormat.addSource("yaml", BukkitYamlSource.class);
    }

    @Override
    public <T extends MapNode> T read(@NotNull Reader reader, @NotNull T parent) throws IOException {
        final YamlConfiguration config = new YamlConfiguration();
        if (ALLOW_COMMENTS) {
            config.options().parseComments(true);
        }
        try {
            config.load(reader);
        } catch (InvalidConfigurationException e) {
            throw new IOException("Cannot parse the provided reader as bukkit yaml configuration", e);
        }
        for (String path : config.getKeys(true)) {
            final Object value = config.get(path);
            if (value == null) {
                continue;
            }
            final SettingsNode node = parent.getSplit(path);
            node.setValue(value);
            if (ALLOW_COMMENTS) {
                node.setTopComment(config.getComments(path));
                node.setSideComment(config.getInlineComments(path));
            }
        }
        return parent;
    }

    @Override
    public void write(@NotNull Writer writer, @NotNull MapNode parent) throws IOException {
        final YamlConfiguration config = new YamlConfiguration();
        if (ALLOW_COMMENTS) {
            config.options().parseComments(true);
        }
        for (String[] path : parent.paths()) {
            final String joined = String.join(".", path);
            final SettingsNode node = parent.get(path);
            config.set(joined, node.asLiteralObject());
            if (ALLOW_COMMENTS) {
                config.setComments(joined, node.getTopComment());
                config.setInlineComments(joined, node.getSideComment());
            }
        }
        writer.write(config.saveToString());
    }
}
