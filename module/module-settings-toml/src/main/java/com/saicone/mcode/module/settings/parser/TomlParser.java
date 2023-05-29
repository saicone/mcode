package com.saicone.mcode.module.settings.parser;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.saicone.mcode.module.settings.Settings;
import com.saicone.mcode.module.settings.SettingsParser;
import com.saicone.mcode.module.settings.node.SettingsNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.io.Writer;
import java.util.*;

public class TomlParser extends SettingsParser {

    private TomlFormat format;

    public TomlParser() {
        this(TomlFormat.instance());
    }

    public TomlParser(@NotNull TomlFormat format) {
        this.format = format;
    }

    public TomlFormat getFormat() {
        return format;
    }

    public void setFormat(@NotNull TomlFormat format) {
        this.format = format;
    }

    @Override
    public Settings read(@NotNull Reader reader) {
        final CommentedConfig config = format.createParser().parse(reader);
        return readValue(null, config);
    }

    public Settings readValue(@Nullable Settings parent, @NotNull Config config) {
        final Settings settings = new Settings(parent);
        for (var entry : config.valueMap().entrySet()) {
            settings.set(entry.getKey(),
                    readValue(
                            settings,
                            (config instanceof CommentedConfig ? ((CommentedConfig) config).getComment(entry.getKey()) : null),
                            entry.getKey(),
                            entry.getValue()
                    ));
        }
        return settings;
    }

    public SettingsNode readValue(@NotNull Settings parent, @Nullable String comment, @NotNull String key, @NotNull Object value) {
        final SettingsNode node;
        if (value instanceof Config) {
            node = readValue(parent, (Config) value);
        } else if (value instanceof Map) {
            node = new Settings(parent).set((Map<?, ?>) value);
        } else {
            node = new SettingsNode(key, value);
        }
        if (comment != null) {
            final List<String> topComment = new ArrayList<>(Arrays.asList(comment.split("\n")));
            node.setTopComment(topComment);
        }
        return node;
    }

    @Override
    public void write(@NotNull Settings settings, @NotNull Writer writer) {
        final CommentedConfig config = CommentedConfig.inMemory();
        for (SettingsNode node : settings.getValue()) {
            writeValue(config, node);
        }
        format.createWriter().write(config, writer);
    }

    public void writeValue(@NotNull CommentedConfig config, @NotNull SettingsNode node) {
        final Object value;
        if (node instanceof Settings) {
            final CommentedConfig subConfig = CommentedConfig.inMemory();
            for (SettingsNode subNode : ((Settings) node).getValue()) {
                writeValue(subConfig, subNode);
            }
            value = subConfig;
        } else {
            value = node.getValue();
        }
        config.set(node.getKey(), value);
        if (node.getTopComment() != null) {
            config.setComment(node.getKey(), String.join("\n", node.getTopComment()));
        }
    }
}
