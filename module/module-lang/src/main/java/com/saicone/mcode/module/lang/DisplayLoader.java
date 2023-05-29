package com.saicone.mcode.module.lang;

import com.saicone.mcode.module.lang.display.Display;
import com.saicone.mcode.util.Strings;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class DisplayLoader<SenderT> {

    private static final DisplayLoader<?> EMPTY = new DisplayLoader<>("", Map.of()) {
        @Override
        public @Nullable Display<Object> load(@Nullable Object object) {
            return null;
        }

        @Override
        public @Nullable Display<Object> load(@NotNull String text) {
            return null;
        }

        @Override
        public @Nullable Display<Object> load(@NotNull List<Object> list) {
            return null;
        }

        @Override
        public @Nullable Display<Object> load(@NotNull Map<String, Object> map) {
            return null;
        }
    };

    private final Pattern pattern;
    private final Map<String, Object> defaults;

    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> DisplayLoader<T> of() {
        return (DisplayLoader<T>) EMPTY;
    }

    public DisplayLoader(@NotNull @Language("RegExp") String regex, @NotNull Map<String, Object> defaults) {
        this(Pattern.compile(regex), defaults);
    }

    public DisplayLoader(@NotNull Pattern pattern, @NotNull Map<String, Object> defaults) {
        this.pattern = pattern;
        this.defaults = defaults;
    }

    @NotNull
    public Pattern getPattern() {
        return pattern;
    }

    @NotNull
    public Map<String, Object> getDefaults() {
        return defaults;
    }

    @Nullable
    protected Object get(@NotNull Map<?, ?> data, @NotNull String key) {
        for (var entry : data.entrySet()) {
            if (String.valueOf(entry.getKey()).equalsIgnoreCase(key)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @NotNull
    protected String getString(@NotNull Map<String, Object> data, @NotNull String key, @NotNull String def) {
        final Object obj = get(data, key);
        return obj != null ? String.valueOf(obj) : String.valueOf(defaults.getOrDefault(key, def));
    }

    protected int getInteger(@NotNull Map<String, Object> data, @NotNull String key, int def) {
        final Object obj = get(data, key);
        return obj instanceof Integer ? (Integer) obj : (Integer) defaults.getOrDefault(key, def);
    }

    protected float getFloat(@NotNull Map<String, Object> data, @NotNull String key, float def) {
        final Object obj = get(data, key);
        return obj instanceof Float ? (Float) obj : (Float) defaults.getOrDefault(key, def);
    }

    protected boolean getBoolean(@NotNull Map<String, Object> data, @NotNull String key, boolean def) {
        final Object obj = get(data, key);
        return obj instanceof Boolean ? (Boolean) obj : (Boolean) defaults.getOrDefault(key, def);
    }

    @Contract("null, _ -> param2")
    protected Integer asInt(@Nullable Object obj, Integer def) {
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        try {
            return Integer.parseInt(String.valueOf(obj));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    @Contract("null, _ -> param2")
    protected Boolean asBoolean(@Nullable Object obj, Boolean def) {
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        switch (String.valueOf(obj).trim().toLowerCase()) {
            case "true":
            case "yes":
            case "1":
                return true;
            case "false":
            case "no":
            case "0":
                return false;
            default:
                return def;
        }
    }

    public boolean matches(@NotNull String s) {
        return pattern.matcher(s).matches();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public Display<SenderT> load(@Nullable Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof Map) {
            return load((Map<String, Object>) object);
        } else if (object instanceof List) {
            return load((List<Object>) object);
        } else {
            return load(String.valueOf(object));
        }
    }

    @Nullable
    public Display<SenderT> load(@NotNull String text) {
        if (text.isEmpty()) {
            return null;
        }
        final String[] split = Strings.splitBySpaces(text);
        final Map<String, Object> map = new HashMap<>();
        int i = 0;
        for (var entry : defaults.entrySet()) {
            if (i < split.length) {
                map.put(entry.getKey(), parseValue(entry.getValue(), split[i]));
                i++;
            } else {
                break;
            }
        }
        return load(map);
    }

    @Nullable
    public Display<SenderT> load(@NotNull List<Object> list) {
        if (list.isEmpty()) {
            return null;
        }
        final Map<String, Object> map = new HashMap<>();
        int i = 0;
        for (var entry : defaults.entrySet()) {
            if (i < list.size()) {
                map.put(entry.getKey(), parseValue(entry.getValue(), String.valueOf(list.get(i))));
                i++;
            } else {
                break;
            }
        }
        return load(map);
    }

    @Nullable
    public Display<SenderT> load(@NotNull Map<String, Object> map) {
        return null;
    }

    private Object parseValue(@NotNull Object value, @NotNull String s) {
        if (value instanceof Integer) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return value;
            }
        } else if (value instanceof Float) {
            try {
                return Float.parseFloat(s);
            } catch (NumberFormatException e) {
                return value;
            }
        } else if (value instanceof Boolean) {
            return asBoolean(s, (Boolean) value);
        }
        return s;
    }
}
