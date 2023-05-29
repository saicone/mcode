package com.saicone.mcode.bungee.settings;

import com.saicone.mcode.module.settings.Settings;
import com.saicone.mcode.module.settings.SettingsParser;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class YamlParser extends SettingsParser {

    @Override
    public Settings read(@NotNull Reader reader) {
        final Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(reader);
        final Settings settings = new Settings();
        for (String[] path : getPaths(config)) {
            settings.set(path, config.get(String.join(".", path)));
        }
        return settings;
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
    public void write(@NotNull Settings settings, @NotNull Writer writer) {
        final Configuration config = new Configuration();
        for (String[] path : settings.getDeepKeys()) {
            config.set(String.join(".", path), settings.get(path).getValue());
        }
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, writer);
    }
}
