package com.saicone.mcode.ap.serializer;

import com.saicone.mcode.ap.PluginSerializer;
import com.saicone.mcode.ap.SerializedDependency;
import com.saicone.mcode.bootstrap.PluginDescription;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class YamlSerializer extends PluginSerializer {

    protected final Representer representer;
    protected final Yaml yaml;

    protected YamlSerializer(PluginDescription plugin, String mainClass, Map<String, Set<SerializedDependency>> dependencies) {
        super(plugin, mainClass, dependencies);

        final DumperOptions dumpOptions = new DumperOptions();
        dumpOptions.setPrettyFlow(true);
        dumpOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumpOptions.setProcessComments(true);

        this.representer = new Representer(dumpOptions);
        this.yaml = new Yaml(new Representer(dumpOptions), dumpOptions);
    }

    public void write(Writer writer, Map<String, Object> object) {
        final Node node = writeNode(object);
        if (this.plugin.header().length > 0 && node instanceof MappingNode) {
            writeCommentLines(List.of(this.plugin.header()), ((MappingNode) node).getValue().get(0).getKeyNode(), CommentType.BLOCK);
        }
        this.yaml.serialize(node, writer);
    }

    // Taken from https://github.com/saicone/settings, licensed under MIT license
    public Node writeNode(Object object) {
        if (object == null) {
            return null;
        }

        if (object instanceof Map) {
            final List<NodeTuple> values = new ArrayList<>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
                // Create tuple value
                final Node valueNode = writeNode(entry.getValue());
                if (valueNode == null) {
                    continue;
                }
                final Node keyNode = this.representer.represent(entry.getKey());

                // Save into values
                values.add(new NodeTuple(keyNode, valueNode));
            }
            return new MappingNode(Tag.MAP, values, this.representer.getDefaultFlowStyle());
        } else if (object instanceof Iterable) {
            final List<Node> values = new ArrayList<>();
            for (Object value : (Iterable<?>) object) {
                final Node nodeValue = writeNode(value);
                if (nodeValue == null) {
                    continue;
                }
                values.add(nodeValue);
            }
            return new SequenceNode(Tag.SEQ, values, this.representer.getDefaultFlowStyle());
        } else {
            return this.representer.represent(object);
        }
    }

    // Taken from https://github.com/saicone/settings, licensed under MIT license
    public void writeCommentLines(List<String> comment, Node node, CommentType commentType) {
        if (comment == null) {
            return;
        }
        final List<CommentLine> list = new ArrayList<>();
        for (String line : comment) {
            if (line == null || line.trim().isEmpty()) {
                list.add(new CommentLine(null, null, "", CommentType.BLANK_LINE));
            } else {
                list.add(new CommentLine(null, null, ' ' + line, commentType));
            }
        }
        if (commentType == CommentType.BLOCK) {
            node.setBlockComments(list);
        } else if (commentType == CommentType.IN_LINE) {
            node.setInLineComments(list);
        }
    }
}
