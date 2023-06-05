package com.saicone.mcode.velocity.settings;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import com.saicone.mcode.module.settings.Settings;
import com.saicone.mcode.module.settings.SettingsParser;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.io.Writer;

public class TomlParser extends SettingsParser {

    public static void register() {
        if (!SettingsParser.contains("toml")) {
            SettingsParser.register("toml", TomlParser::new);
        }
    }

    @Override
    public Settings read(@NotNull Reader reader) {
        final Toml toml = new Toml().read(reader);
        return new Settings().set(toml.toMap());
    }

    @Override
    public void write(@NotNull Settings settings, @NotNull Writer writer) throws Throwable {
        final TomlWriter tomlWriter = new TomlWriter();
        tomlWriter.write(settings.asMap(), writer);
    }
}
