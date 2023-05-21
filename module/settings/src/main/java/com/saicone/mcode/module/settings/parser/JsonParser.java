package com.saicone.mcode.module.settings.parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.saicone.mcode.module.settings.Settings;
import com.saicone.mcode.module.settings.SettingsParser;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Map;

public class JsonParser extends SettingsParser {

    public static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>(){}.getType();

    private Gson gson;

    public JsonParser() {
        this(true);
    }

    public JsonParser(boolean prettyPrint) {
        this(prettyPrint ? new GsonBuilder().setPrettyPrinting().create() : GSON);
    }

    public JsonParser(@NotNull Gson gson) {
        this.gson = gson;
    }

    @NotNull
    public Gson getGson() {
        return gson;
    }

    public void setGson(@NotNull Gson gson) {
        this.gson = gson;
    }

    @Override
    public Settings read(@NotNull Reader reader) throws Throwable {
        return new Settings().set(gson.fromJson(reader, MAP_TYPE));
    }

    @Override
    public void write(@NotNull Settings settings, @NotNull Writer writer) throws Throwable {
        gson.toJson(settings.asMap(), writer);
    }
}
