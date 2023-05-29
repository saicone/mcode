package com.saicone.mcode.module.settings.parser;

import com.saicone.mcode.module.settings.Settings;
import com.saicone.mcode.module.settings.SettingsParser;
import com.saicone.mcode.module.settings.node.SettingsNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.*;
import org.yaml.snakeyaml.representer.Representer;

import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class YamlParser extends SettingsParser {

    private final PublicConstructor constructor;
    private final Representer representer;
    private Yaml yaml;

    public YamlParser() {
        final LoaderOptions loaderOptions = new LoaderOptions();
        final DumperOptions dumpOptions = new DumperOptions();
        dumpOptions.setPrettyFlow(true);
        dumpOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumpOptions.setProcessComments(true);

        this.constructor = new PublicConstructor(loaderOptions);
        this.representer = new Representer(dumpOptions);
        this.yaml = new Yaml(constructor, representer, dumpOptions, loaderOptions);
    }

    public YamlParser(@NotNull PublicConstructor constructor, @NotNull Representer representer, @NotNull Yaml yaml) {
        this.constructor = constructor;
        this.representer = representer;
        this.yaml = yaml;
    }

    private static DumperOptions defaultDumperOptions() {
        final DumperOptions options = new DumperOptions();
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setProcessComments(true);
        return options;
    }

    @NotNull
    public Yaml getYaml() {
        return yaml;
    }

    public void setYaml(@NotNull Yaml yaml) {
        this.yaml = yaml;
    }

    @Override
    public Settings read(@NotNull Reader reader) {
        return readValue(new Settings(), (MappingNode) yaml.compose(reader));
    }

    public Settings readValue(@NotNull Settings settings, @NotNull MappingNode node) {
        for (NodeTuple tuple : node.getValue()) {
            final ScalarNode keyNode = (ScalarNode) tuple.getKeyNode();
            Node valueNode = tuple.getValueNode();
            if (valueNode instanceof AnchorNode) {
                valueNode = ((AnchorNode) valueNode).getRealNode();
            }

            final String key = keyNode.getValue();

            final SettingsNode settingsNode;
            if (valueNode instanceof MappingNode) {
                settingsNode = readValue(new Settings(), (MappingNode) valueNode);
            } else {
                settingsNode = new SettingsNode(key, constructor.constructObject(valueNode));
            }

            settingsNode.setTopComment(readComment(keyNode.getBlockComments()));
            if (valueNode instanceof MappingNode || valueNode instanceof SequenceNode) {
                settingsNode.setSideComment(readComment(keyNode.getInLineComments()));
            } else {
                settingsNode.setSideComment(readComment(valueNode.getInLineComments()));
            }

            settings.set(key, settingsNode);
        }
        return settings;
    }

    public List<String> readComment(@Nullable List<CommentLine> list) {
        if (list == null) {
            return null;
        }
        final List<String> comment = new ArrayList<>();
        for (CommentLine line : list) {
            if (line.getCommentType() == CommentType.BLANK_LINE) {
                comment.add(null);
            } else {
                comment.add(line.getValue().trim());
            }
        }
        return comment;
    }

    @Override
    public void write(@NotNull Settings settings, @NotNull Writer writer) {
        yaml.serialize(writeSettings(settings), writer);
    }

    public MappingNode writeSettings(@NotNull Settings settings) {
        final List<NodeTuple> list = new ArrayList<>();
        for (SettingsNode node : settings.getValue()) {
            final Node keyNode = representer.represent(node.getKey());
            final Node valueNode;
            if (node instanceof Settings) {
                valueNode = writeSettings((Settings) node);
            } else {
                valueNode = representer.represent(node.getValue());
            }
            writeComment(node.getTopComment(), keyNode, CommentType.BLOCK);
            if (valueNode instanceof MappingNode || valueNode instanceof SequenceNode) {
                writeComment(node.getSideComment(), keyNode, CommentType.IN_LINE);
            } else {
                writeComment(node.getSideComment(), valueNode, CommentType.IN_LINE);
            }
            list.add(new NodeTuple(keyNode, valueNode));
        }
        return new MappingNode(Tag.MAP, list, DumperOptions.FlowStyle.BLOCK);
    }

    public void writeComment(@Nullable List<String> comment, @NotNull Node node, @NotNull CommentType commentType) {
        if (comment == null) {
            return;
        }
        final List<CommentLine> list = new ArrayList<>();
        for (String s : comment) {
            if (s == null) {
                list.add(new CommentLine(null, null, "", CommentType.BLANK_LINE));
            } else {
                list.add(new CommentLine(null, null, s.isBlank() ? s : " " + s, commentType));
            }
        }
        if (commentType == CommentType.BLOCK) {
            node.setBlockComments(list);
        } else if (commentType == CommentType.IN_LINE) {
            node.setInLineComments(list);
        }
    }

    public static class PublicConstructor extends Constructor {

        public PublicConstructor(@NotNull LoaderOptions options) {
            super(options);
        }

        @Override
        public Object constructObject(Node node) {
            return super.constructObject(node);
        }
    }
}
