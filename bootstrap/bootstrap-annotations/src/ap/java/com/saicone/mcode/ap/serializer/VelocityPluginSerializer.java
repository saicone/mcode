package com.saicone.mcode.ap.serializer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.saicone.mcode.ap.PluginSerializer;
import com.saicone.mcode.ap.SerializedDependency;
import com.saicone.mcode.bootstrap.PluginDescription;

import java.io.BufferedWriter;
import java.util.Map;
import java.util.Set;

public class VelocityPluginSerializer extends PluginSerializer {

    public VelocityPluginSerializer(PluginDescription plugin, String mainClass, Map<String, Set<SerializedDependency>> dependencies) {
        super(plugin, mainClass, dependencies);
    }

    @Override
    public void write(BufferedWriter writer) {
        final JsonObject object = new JsonObject();

        // Information

        object.addProperty("id", this.plugin.id().isBlank() ? this.plugin.name().toLowerCase() : this.plugin.id());
        object.addProperty("name", this.plugin.name());
        if (!this.plugin.description().isBlank()) {
            object.addProperty("description", this.plugin.description());
        }
        object.addProperty("version", this.plugin.version());
        if (this.plugin.authors().length > 0) {
            final JsonArray authors = new JsonArray();
            for (String author : this.plugin.authors()) {
                authors.add(author);
            }
            object.add("authors", authors);
        }
        if (!this.plugin.website().isBlank()) {
            object.addProperty("url", this.plugin.website());
        }


        // Load

        object.addProperty("main", getMain("mcode.bootstrap.velocity.VelocityBootstrap"));


        // Dependencies

        if (this.dependencies.containsKey("server")) {
            final JsonArray dependencies = new JsonArray();
            for (SerializedDependency dependency : this.dependencies.get("server")) {
                final JsonObject depend = new JsonObject();
                depend.addProperty("id", dependency.getId());
                depend.addProperty("optional", !dependency.isRequired());
                dependencies.add(depend);
            }
            object.add("dependencies", dependencies);
        }


        // Extra

        for (Map.Entry<String, Object> entry : getExtra().entrySet()) {
            object.add(entry.getKey(), asJson(entry.getValue()));
        }

        new Gson().toJson(object, writer);
    }

    private JsonElement asJson(Object object) {
        if (object instanceof Map) {
            final JsonObject element = new JsonObject();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
                element.add(String.valueOf(entry.getKey()), asJson(entry.getValue()));
            }
            return element;
        } else if (object instanceof Iterable) {
            final JsonArray element = new JsonArray();
            for (Object o : (Iterable<?>) object) {
                element.add(asJson(o));
            }
            return element;
        } else if (object instanceof Boolean) {
            return new JsonPrimitive((Boolean) object);
        } else if (object instanceof Number) {
            return new JsonPrimitive((Number) object);
        } else if (object instanceof String) {
            return new JsonPrimitive((String) object);
        } else if (object instanceof Character) {
            return new JsonPrimitive((Character) object);
        } else {
            return JsonNull.INSTANCE;
        }
    }
}
