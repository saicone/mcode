package com.saicone.mcode.velocity.settings;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import com.saicone.settings.SettingsSource;
import com.saicone.settings.data.DataFormat;
import com.saicone.settings.node.MapNode;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class TomlSettingsSource implements SettingsSource {

    public static void register() {
        DataFormat.addSource("toml", TomlSettingsSource.class);
    }

    @Override
    public <T extends MapNode> T read(@NotNull Reader reader, @NotNull T parent) throws IOException {
        final Toml toml = new Toml().read(reader);
        parent.merge(toml.toMap());
        return parent;
    }

    @Override
    public void write(@NotNull Writer writer, @NotNull MapNode parent) throws IOException {
        final TomlWriter tomlWriter = new TomlWriter();
        tomlWriter.write(parent.asLiteralObject(), writer);
    }
}
