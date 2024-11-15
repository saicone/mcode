package com.saicone.mcode.ap.serializer;

import com.saicone.mcode.ap.SerializedDependency;
import com.saicone.mcode.bootstrap.PluginDescription;
import com.saicone.mcode.platform.MC;

import java.io.BufferedWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PaperPluginSerializer extends YamlSerializer {

    public PaperPluginSerializer(PluginDescription plugin, String mainClass, Map<String, Set<SerializedDependency>> dependencies) {
        super(plugin, mainClass, dependencies);
    }

    @Override
    public void write(BufferedWriter writer) {
        final Map<String, Object> map = new LinkedHashMap<>();

        // Information

        map.put("name", this.plugin.name());
        if (this.plugin.aliases().length > 0) {
            map.put("provides", List.of(this.plugin.aliases()));
        }
        if (!this.plugin.prefix().isBlank()) {
            map.put("prefix", this.plugin.prefix());
        }
        if (!this.plugin.description().isBlank()) {
            map.put("description", this.plugin.description());
        }
        map.put("version", this.plugin.version());
        if (this.plugin.authors().length > 0) {
            map.put("authors", List.of(this.plugin.authors()));
        }
        if (this.plugin.contributors().length > 0) {
            map.put("contributors", List.of(this.plugin.contributors()));
        }
        if (!this.plugin.website().isBlank()) {
            map.put("website", this.plugin.website());
        }


        // Load

        map.put("main", getMain(".mcode.bootstrap.paper.PaperBootstrap"));
        if (!this.plugin.bootstrapper().isBlank()) {
            map.put("bootstrapper", this.plugin.bootstrapper());
        }
        if (!this.plugin.loader().isBlank()) {
            map.put("loader", this.plugin.loader());
        }


        // Behaviour

        if (!this.plugin.compatibility().isBlank()) {
            final MC version = MC.fromString(this.plugin.compatibility().split("-")[0].trim());
            if (version != null) {
                if (version.isOlderThan(MC.V_1_19)) {
                    map.put("api-version", "1.19");
                } else {
                    map.put("api-version", version.major() + "." + version.feature() + "." + version.minor());
                }
            }
        }
        if (!this.plugin.load().isBlank() && !this.plugin.load().equals("POSTWORLD")) {
            map.put("load", this.plugin.load().toUpperCase());
        }
        if (this.plugin.openClassLoader()) {
            map.put("has-open-classloader", true);
        }


        // Dependencies

        if (!this.dependencies.isEmpty()) {
            final Map<String, Map<String, Object>> dependencies = new LinkedHashMap<>();
            for (Map.Entry<String, Set<SerializedDependency>> entry : this.dependencies.entrySet()) {
                final Map<String, Object> listed = new LinkedHashMap<>();
                for (SerializedDependency dependency : entry.getValue()) {
                    listed.put(dependency.getPlugin(), write(dependency));
                }
                if (!listed.isEmpty()) {
                    dependencies.put(entry.getKey(), listed);
                }
            }
            if (!dependencies.isEmpty()) {
                map.put("dependencies", dependencies);
            }
        }


        // Extra

        map.putAll(getExtra());

        write(writer, map);
    }

    private Map<String, Object> write(SerializedDependency dependency) {
        final Map<String, Object> map = new LinkedHashMap<>();
        if (!dependency.getLoad().isBlank() && !dependency.getLoad().equalsIgnoreCase("OMIT")) {
            map.put("load", dependency.getLoad().toUpperCase());
        }
        if (!dependency.isRequired()) {
            map.put("required", false);
        }
        if (!dependency.isJoinClasspath()) {
            map.put("join-classpath", false);
        }
        if (map.isEmpty()) {
            map.put("required", true);
        }
        return map;
    }
}
