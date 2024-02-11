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
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class DMap implements Map<String, Object> {

    private final Map<String, Object> map;

    @NotNull
    public static DMap of(@NotNull Map<?, ?> map) {
        return of(map, true);
    }

    @Nullable
    @Contract("_, true -> !null")
    @SuppressWarnings("unchecked")
    public static DMap of(@NotNull Map<?, ?> map, boolean convert) {
        if (map instanceof DMap) {
            return (DMap) map;
        } else {
            try {
                return new DMap((Map<String, Object>) map);
            } catch (ClassCastException e) {
                if (convert) {
                    final Map<String, Object> finalMap = new HashMap<>();
                    for (Entry<?, ?> entry : map.entrySet()) {
                        finalMap.put(String.valueOf(entry.getKey()), entry.getValue());
                    }
                    return new DMap(finalMap);
                }
                return null;
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

    public void forEach(@NotNull Predicate<? super String> condition, @NotNull BiConsumer<? super String, ? super Object> action) {
        forEach((key, value) -> condition.test(key), action);
    }

    public void forEach(@NotNull BiPredicate<? super String, ? super Object> condition, @NotNull BiConsumer<? super String, ? super Object> action) {
        for (Entry<String, Object> entry : entrySet()) {
            if (condition.test(entry.getKey(), entry.getValue())) {
                action.accept(entry.getKey(), entry.getValue());
            }
        }
    }

    @NotNull
    public Map<String, Object> asDeepPath(@NotNull String separator) {
        return asDeepPath(separator, null);
    }

    @NotNull
    public Map<String, Object> asDeepPath(@NotNull String separator, @Nullable Predicate<Object> filter) {
        final Map<String, Object> finalMap = new HashMap<>();
        for (Entry<String, Object> entry : map.entrySet()) {
            if (filter == null || !filter.test(entry.getValue())) {
                if (entry.getValue() instanceof Map) {
                    final DMap child = of((Map<?, ?>) entry.getValue(), false);
                    if (child != null) {
                        child.asDeepPath(separator, filter).forEach((key, value) -> finalMap.put(entry.getKey() + separator + key, value));
                        continue;
                    }
                }
            }
            finalMap.put(entry.getKey(), entry.getValue());
        }
        return finalMap;
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
