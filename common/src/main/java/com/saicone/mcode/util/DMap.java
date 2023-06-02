package com.saicone.mcode.util;

import com.saicone.mcode.util.function.ThrowableFunction;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class DMap implements Map<String, Object> {

    private final Map<String, Object> map;

    @NotNull
    @SuppressWarnings("unchecked")
    public static DMap of(@NotNull Map<?, ?> map) {
        if (map instanceof DMap) {
            return (DMap) map;
        } else {
            try {
                return new DMap((Map<String, Object>) map);
            } catch (ClassCastException e) {
                final Map<String, Object> finalMap = new HashMap<>();
                for (Entry<?, ?> entry : map.entrySet()) {
                    finalMap.put(String.valueOf(entry.getKey()), entry.getValue());
                }
                return new DMap(finalMap);
            }
        }
    }

    public DMap() {
        this(new HashMap<>());
    }

    public DMap(@NotNull Map<String, Object> map) {
        this.map = map;
    }

    @Nullable
    public Object getIf(@NotNull Predicate<String> predicate) {
        for (Entry<String, Object> entry : map.entrySet()) {
            if (predicate.test(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Nullable
    public Object getIgnoreCase(@NotNull String s) {
        return getIf(str -> str.equalsIgnoreCase(s));
    }

    @Nullable
    public Object getRegex(@NotNull @Language("RegExp") String regex) {
        return getRegex(Pattern.compile(regex));
    }

    @Nullable
    public Object getRegex(@NotNull Pattern pattern) {
        return getIf(str -> pattern.matcher(str).matches());
    }

    @Nullable
    public DMap getChild(@NotNull Function<DMap, Object> getter) {
        final Object result = getter.apply(this);
        if (result instanceof Map) {
            return DMap.of((Map<?, ?>) result);
        }
        return null;
    }

    @Nullable
    public <T> T getBy(@NotNull ThrowableFunction<Object, T> mapper, @NotNull Function<DMap, Object> getter) {
        return getBy(mapper, getter, null);
    }

    @Nullable
    @Contract("_, _, !null -> !null")
    public <T> T getBy(@NotNull ThrowableFunction<Object, T> mapper, @NotNull Function<DMap, Object> getter, @Nullable T def) {
        final Object value = getter.apply(this);
        if (value == null) {
            return def;
        }
        try {
            return mapper.apply(value);
        } catch (Throwable t) {
            return def;
        }
    }

    @NotNull
    public Map<String, Object> getMap() {
        return this.map;
    }

    // Vanilla methods

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        return this.map.containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        return this.map.containsValue(o);
    }

    @Override
    public Object get(Object o) {
        return this.map.get(o);
    }

    @Nullable
    @Override
    public Object put(String s, Object o) {
        return this.map.put(s, o);
    }

    @Override
    public Object remove(Object o) {
        return this.map.remove(o);
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ?> map) {
        this.map.putAll(map);
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        return this.map.keySet();
    }

    @NotNull
    @Override
    public Collection<Object> values() {
        return this.map.values();
    }

    @NotNull
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return this.map.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return this.map.equals(o);
    }

    @Override
    public int hashCode() {
        return this.map.hashCode();
    }
}
