package com.saicone.mcode.ap.serializer;

import com.saicone.mcode.ap.SerializedDependency;
import com.saicone.mcode.bootstrap.PluginDescription;

import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BungeecordPluginSerializer extends YamlSerializer {

    public BungeecordPluginSerializer(PluginDescription plugin, String mainClass, Map<String, Set<SerializedDependency>> dependencies) {
        super(plugin, mainClass, dependencies);
    }

    @Override
    public void write(BufferedWriter writer) {
        final Map<String, Object> map = new HashMap<>();

        // Information

        map.put("name", this.plugin.name());
        if (!this.plugin.description().isBlank()) {
            map.put("description", this.plugin.description());
        }
        map.put("version", this.plugin.version());
        if (this.plugin.authors().length > 0) {
            if (this.plugin.authors().length == 1) {
                map.put("author", this.plugin.authors()[0]);
            } else {
                map.put("author", String.join(", ", this.plugin.authors()));
            }
        }
        if (this.plugin.contributors().length > 0) {
            // Ignored value
            map.put("contributors", List.of(this.plugin.contributors()));
        }
        if (!this.plugin.website().isBlank()) {
            // Ignored value
            map.put("website", this.plugin.website());
        }


        // Load

        map.put("main", getMain("mcode.bootstrap.bungee.BungeeBootstrap"));


        // Dependencies

        if (this.dependencies.containsKey("server")) {
            final Set<String> depends = new HashSet<>();
            final Set<String> softDepends = new HashSet<>();
            for (SerializedDependency dependency : this.dependencies.get("server")) {
                if (dependency.isRequired()) {
                    depends.add(dependency.getPlugin());
                } else {
                    softDepends.add(dependency.getPlugin());
                }
            }
            if (!depends.isEmpty()) {
                map.put("depends", depends);
            }
            if (!softDepends.isEmpty()) {
                map.put("softDepends", softDepends);
            }
        }


        // Extra

        map.putAll(getExtra());

        write(writer, map);
    }
}
