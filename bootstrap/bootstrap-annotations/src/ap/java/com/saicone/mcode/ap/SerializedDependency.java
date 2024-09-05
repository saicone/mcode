package com.saicone.mcode.ap;

import com.saicone.mcode.bootstrap.PluginDependency;

public class SerializedDependency {

    private final String id;
    private final String plugin;
    private final boolean required;
    private final String load;
    private final boolean joinClasspath;

    public static SerializedDependency of(PluginDependency dependency) {
        return new SerializedDependency(
                dependency.id(),
                dependency.value(),
                !dependency.optional() && dependency.required(),
                dependency.load(),
                dependency.joinClasspath()
        );
    }

    public static SerializedDependency required(String name) {
        return required(name, "BEFORE");
    }

    public static SerializedDependency required(String name, String load) {
        return new SerializedDependency("", name, true, load, true);
    }

    public static SerializedDependency optional(String name) {
        return optional(name, "BEFORE");
    }

    public static SerializedDependency optional(String name, String load) {
        return new SerializedDependency("", name, false, load, true);
    }

    public SerializedDependency(String id, String plugin, boolean required, String load, boolean joinClasspath) {
        this.id = id;
        this.plugin = plugin;
        this.required = required;
        this.load = load;
        this.joinClasspath = joinClasspath;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isJoinClasspath() {
        return joinClasspath;
    }

    public String getId() {
        return id.isBlank() ? plugin.toUpperCase() : id;
    }

    public String getPlugin() {
        return plugin;
    }

    public String getLoad() {
        return load;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SerializedDependency that = (SerializedDependency) o;
        return id.equals(that.id) && plugin.equals(that.plugin);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + plugin.hashCode();
        return result;
    }
}
