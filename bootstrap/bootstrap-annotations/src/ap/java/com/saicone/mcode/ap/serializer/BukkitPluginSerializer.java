package com.saicone.mcode.ap.serializer;

import com.saicone.mcode.ap.SerializedDependency;
import com.saicone.mcode.bootstrap.PluginDescription;
import com.saicone.mcode.platform.MC;

import java.io.BufferedWriter;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BukkitPluginSerializer extends YamlSerializer {

    public BukkitPluginSerializer(PluginDescription plugin, String mainClass, Map<String, Set<SerializedDependency>> dependencies) {
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
            if (this.plugin.authors().length == 1) {
                map.put("author", this.plugin.authors()[0]);
            } else {
                map.put("authors", List.of(this.plugin.authors()));
            }
        }
        if (this.plugin.contributors().length > 0) {
            map.put("contributors", List.of(this.plugin.contributors()));
        }
        if (!this.plugin.website().isBlank()) {
            map.put("website", this.plugin.website());
        }


        // Load

        map.put("main", getMain(".mcode.bootstrap.bukkit.BukkitBootstrap"));


        // Behaviour

        if (!this.plugin.compatibility().isBlank()) {
            final MC version = MC.fromString(this.plugin.compatibility().split("-")[0].trim());
            if (version != null) {
                if (version.isOlderThan(MC.V_1_13)) {
                    map.put("api-version", "1.13");
                } else if (version.isOlderThan(MC.V_1_20_5)) {
                    map.put("api-version", version.major() + "." + version.feature());
                } else {
                    map.put("api-version", version.major() + "." + version.feature() + "." + version.minor());
                }
            }
        }
        if (!this.plugin.load().isBlank() && !this.plugin.load().equals("POSTWORLD")) {
            map.put("load", this.plugin.load().toUpperCase());
        }
        if (this.plugin.foliaSupported()) {
            map.put("folia-supported", true);
        }


        // Dependencies

        if (this.dependencies.containsKey("server")) {
            final Set<String> depend = new HashSet<>();
            final Set<String> softDepend = new HashSet<>();
            final Set<String> loadBefore = new HashSet<>();
            for (SerializedDependency dependency : this.dependencies.get("server")) {
                if (dependency.getLoad().equalsIgnoreCase("AFTER")) {
                    loadBefore.add(dependency.getPlugin());
                } else if (dependency.isRequired()) {
                    depend.add(dependency.getPlugin());
                } else {
                    softDepend.add(dependency.getPlugin());
                }
            }
            if (!depend.isEmpty()) {
                map.put("depend", depend);
            }
            if (!softDepend.isEmpty()) {
                map.put("softdepend", softDepend);
            }
            if (!loadBefore.isEmpty()) {
                map.put("loadbefore", loadBefore);
            }
        }


        // Extra

        map.putAll(getExtra());

        write(writer, map);
    }
}
