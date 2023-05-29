package com.saicone.mcode.module.settings.parser;

import com.saicone.mcode.module.settings.Settings;
import com.saicone.mcode.module.settings.SettingsParser;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.io.Writer;

public class HoconParser extends SettingsParser {

    @Override
    public Settings read(@NotNull Reader reader) {
        final Config config = ConfigFactory.parseReader(reader);
        final Settings settings = new Settings();
        for (var entry : config.entrySet()) {
            settings.set(entry.getKey(), entry.getValue().unwrapped());
        }
        return settings;
    }
}
