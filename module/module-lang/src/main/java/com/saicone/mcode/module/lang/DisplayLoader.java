package com.saicone.mcode.module.lang;

import com.saicone.mcode.util.DMap;
import com.saicone.mcode.util.text.Strings;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class DisplayLoader<SenderT> {

    private final @Language("RegExp") String regex;
    private final Pattern pattern;
    private final Map<String, Object> defaults;

    @NotNull
    public static <T> DisplayLoader<T> empty() {
        return new DisplayLoader<>("", Map.of());
    }

    public DisplayLoader(@NotNull @Language("RegExp") String regex, @NotNull Map<String, Object> defaults) {
        this.regex = regex;
        this.pattern = Pattern.compile(regex);
        this.defaults = defaults;
    }

    @NotNull
    public @Language("RegExp") String getRegex() {
        return regex;
    }

    @NotNull
    public Pattern getPattern() {
        return pattern;
    }

    @NotNull
    public Map<String, Object> getDefaults() {
        return defaults;
    }

    public boolean matches(@NotNull String s) {
        return pattern.matcher(s).matches();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DisplayLoader<?> that = (DisplayLoader<?>) o;

        if (!regex.equals(that.regex)) return false;
        return defaults.equals(that.defaults);
    }

    @Override
    public int hashCode() {
        int result = regex.hashCode();
        result = 31 * result + defaults.hashCode();
        return result;
    }


    @Nullable
    @SuppressWarnings("unchecked")
    public Display<SenderT> load(@Nullable Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof Map) {
            return load(DMap.of((Map<?, ?>) object));
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
        return load(new DMap(map));
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
        return load(new DMap(map));
    }

    @Nullable
    public Display<SenderT> load(@NotNull DMap map) {
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
            switch (s.trim().toLowerCase()) {
                case "true":
                case "yes":
                    return true;
                case "false":
                case "no":
                    return false;
                default:
                    return value;
            }
        }
        return s;
    }
}
