package com.saicone.mcode.ap;

import com.saicone.mcode.bootstrap.Addon;
import com.saicone.mcode.bootstrap.PluginDescription;
import com.saicone.mcode.util.text.Strings;

import java.io.BufferedWriter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public abstract class PluginSerializer {

    protected final PluginDescription plugin;
    protected final String mainClass;
    protected final String pluginClass;
    protected final Map<String, Set<SerializedDependency>> dependencies;

    protected PluginSerializer(PluginDescription plugin, String mainClass, Map<String, Set<SerializedDependency>> dependencies) {
        this.plugin = plugin;
        if (mainClass.startsWith("!")) {
            this.mainClass = mainClass.contains(".") ? mainClass.substring(1, mainClass.lastIndexOf('.')) : "";
            this.pluginClass = mainClass.substring(1);
        } else {
            this.mainClass = mainClass;
            this.pluginClass = null;
        }
        this.dependencies = dependencies;
    }

    public abstract void write(BufferedWriter writer);

    public String getMain(String bootstrap) {
        return this.pluginClass != null ? this.mainClass + bootstrap : this.mainClass;
    }

    public Map<String, Object> getExtra() {
        final Map<String, Object> map = new LinkedHashMap<>();
        if (this.plugin.addons().length > 0) {
            final Map<String, Object> mcode = new LinkedHashMap<>();
            if (this.pluginClass != null) {
                mcode.put("plugin", this.pluginClass);
            }
            final Set<String> addons = new LinkedHashSet<>();
            for (Addon addon : this.plugin.addons()) {
                addons.add(addon.name());
            }
            mcode.put("addons", addons);
            map.put("mcode", mcode);
        }
        for (String s : this.plugin.extra()) {
            final String key;
            final Object value;
            if (s.contains("=")) {
                final String[] split = s.split("=", 2);
                key = split[0];
                value = relative(split[1]);
            } else {
                key = s;
                value = true;
            }
            put(map, key.split("\\."), value);
        }
        return map;
    }

    private Object relative(String s) {
        if (s.equalsIgnoreCase("true")) {
            return true;
        } else if (s.equalsIgnoreCase("false")) {
            return false;
        } else if (Strings.isNumber(s)) {
            if (s.contains(".")) {
                return Double.parseDouble(s);
            } else {
                return Long.parseLong(s);
            }
        } else {
            return s;
        }
    }

    @SuppressWarnings("unchecked")
    private void put(Map<String, Object> map, String[] path, Object value) {
        for (int i = 0; i < path.length; i++) {
            if (i + 1 >= path.length) {
                break;
            }
            final String key = path[i];
            if (map.containsKey(key)) {
                map = (Map<String, Object>) map.get(key);
            } else {
                final Map<String, Object> sub = new LinkedHashMap<>();
                map.put(key, sub);
                map = sub;
            }
        }
        map.put(path[path.length - 1], value);
    }
}
