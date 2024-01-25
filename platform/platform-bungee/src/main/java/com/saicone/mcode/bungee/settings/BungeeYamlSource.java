package com.saicone.mcode.bungee.settings;

import com.saicone.settings.SettingsSource;
import com.saicone.settings.data.DataFormat;
import com.saicone.settings.node.MapNode;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class BungeeYamlSource implements SettingsSource {

    public static void register() {
        DataFormat.addSource("yaml", BungeeYamlSource.class);
    }

    @Override
    public <T extends MapNode> T read(@NotNull Reader reader, @NotNull T parent) throws IOException {
        final Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(reader);
        for (String[] path : getPaths(config)) {
            parent.get(path).setValue(config.get(String.join(".", path)));
        }
        return parent;
    }

    private List<String[]> getPaths(@NotNull Configuration config) {
        final List<String[]> paths = new ArrayList<>();
        for (String key : config.getKeys()) {
            final Object object = config.get(key);
            if (object instanceof Configuration) {
                getPaths((Configuration) object).forEach(path -> {
                    final String[] array = new String[path.length + 1];
                    array[0] = key;
                    System.arraycopy(path, 0, array, 1, path.length);
                    paths.add(array);
                });
            } else {
                paths.add(new String[] { key });
            }
        }
        return paths;
    }

    @Override
    public void write(@NotNull Writer writer, @NotNull MapNode parent) throws IOException {
        final Configuration config = new Configuration();
        for (String[] path : parent.paths()) {
            config.set(String.join(".", path), parent.get(path).asLiteralObject());
        }
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, writer);
    }
}
