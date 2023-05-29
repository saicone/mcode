package com.saicone.mcode.module.settings;

import com.saicone.mcode.module.settings.node.NodeSupplier;
import com.saicone.mcode.module.settings.node.SettingsNode;
import com.saicone.mcode.util.type.OptionalType;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class Settings extends SettingsNode {

    private final List<String> keys = new ArrayList<>();

    private List<NodeSupplier<?>> suppliers;
    private Memory memory;
    private boolean memorizing;

    public Settings() {
        this(null, "@root", new HashSet<>());
    }

    public Settings(@Nullable Settings parent) {
        this(parent, "@root", new HashSet<>());
    }

    public Settings(@NotNull Set<SettingsNode> nodes) {
        this(null, "@root", nodes);
    }

    public Settings(@NotNull String key) {
        this(null, key, new HashSet<>());
    }

    public Settings(@NotNull String key, @NotNull Set<SettingsNode> nodes) {
        this(null, key, nodes);
    }

    public Settings(@Nullable Settings parent, @NotNull String key) {
        super(parent, key, new HashSet<>());
    }

    public Settings(@Nullable Settings parent, @NotNull Set<SettingsNode> nodes) {
        this(parent, "@root", nodes);
    }

    public Settings(@Nullable Settings parent, @NotNull String key, @NotNull Set<SettingsNode> nodes) {
        super(parent, key, nodes);
        for (SettingsNode node : nodes) {
            keys.add(node.getKey());
        }
    }

    @NotNull
    public SettingsNode get(@NotNull String key) {
        if (memorizing) {
            return getOrSave(key, () -> getIf(s -> s.equals(key)));
        }
        return getIf(s -> s.equals(key));
    }

    @NotNull
    public SettingsNode get(@NotNull String... path) {
        if (memorizing) {
            final String id = String.join(".", path);
            return getOrSave(id, () -> getIf(String::equals, path));
        }
        return getIf(String::equals, path);
    }

    @NotNull
    protected SettingsNode getIf(@NotNull Predicate<String> condition) {
        for (SettingsNode node : getValue()) {
            if (condition.test(node.getKey())) {
                return node;
            }
        }
        return SettingsNode.EMPTY;
    }

    @NotNull
    protected SettingsNode getIf(@NotNull BiPredicate<String, String> condition, @NotNull String... path) {
        SettingsNode node = this;
        for (String key : path) {
            if (node instanceof Settings) {
                node = ((Settings) node).getIfType(condition, key);
                continue;
            }
            return SettingsNode.EMPTY;
        }
        return node;
    }

    @NotNull
    protected <T> SettingsNode getIf(@NotNull Function<String, T> keyConversion, @NotNull BiPredicate<String, T> condition, @NotNull String... path) {
        SettingsNode node = this;
        for (String key : path) {
            if (node instanceof Settings) {
                node = ((Settings) node).getIfType(condition, keyConversion.apply(key));
                continue;
            }
            return SettingsNode.EMPTY;
        }
        return node;
    }

    @NotNull
    protected <T> SettingsNode getIfType(@NotNull BiPredicate<String, T> condition, @NotNull T type) {
        for (SettingsNode node : getValue()) {
            if (condition.test(node.getKey(), type)) {
                return node;
            }
        }
        return SettingsNode.EMPTY;
    }

    @NotNull
    public SettingsNode getIgnoreCase(@NotNull String key) {
        if (memorizing) {
            return getOrSave(key, () -> getIf(s -> s.equalsIgnoreCase(key)));
        }
        return getIf(s -> s.equalsIgnoreCase(key));
    }

    @NotNull
    public SettingsNode getIgnoreCase(@NotNull String... path) {
        if (memorizing) {
            final String id = String.join(".", path);
            return getOrSave(id, () -> getIf(String::equalsIgnoreCase, path));
        }
        return getIf(String::equalsIgnoreCase, path);
    }

    @NotNull
    public SettingsNode getRegex(@NotNull @Language(value = "RegExp") String regex) {
        if (memorizing) {
            final String id = "$RegExp" + regex;
            return getOrSave(id, () -> {
                final Pattern pattern = Pattern.compile(regex);
                return getIf(s -> pattern.matcher(s).matches());
            });
        }
        final Pattern pattern = Pattern.compile(regex);
        return getIf(s -> pattern.matcher(s).matches());
    }

    @NotNull
    public SettingsNode getRegex(@NotNull @Language(value = "RegExp") String... regexPath) {
        if (memorizing) {
            final String id = "$RegExp" + String.join("||", regexPath);
            return getOrSave(id, () -> getIf(Pattern::compile, (s, pattern) -> pattern.matcher(s).matches(), regexPath));
        }
        return getIf(Pattern::compile, (s, pattern) -> pattern.matcher(s).matches(), regexPath);
    }

    @NotNull
    protected SettingsNode getOrSave(@NotNull String id, @NotNull Supplier<@NotNull SettingsNode> supplier) {
        SettingsNode node = memory.get(id);
        if (node != null) {
            return node;
        }
        node = supplier.get();
        memory.save(id, node);
        return node;
    }

    @Override
    @SuppressWarnings("all")
    public @NotNull Set<SettingsNode> getValue() {
        return (Set<SettingsNode>) super.getValue();
    }

    @Override
    public void clear() {
        super.clear();
        if (memorizing) {
            memory.clear();
        }
    }

    @Override
    public @Nullable Settings getParent() {
        return (Settings) super.getParent();
    }

    @NotNull
    public List<String> getKeys() {
        return Collections.unmodifiableList(keys);
    }

    @NotNull
    public List<String[]> getDeepKeys() {
        final List<String[]> list = new ArrayList<>();
        for (SettingsNode node : getValue()) {
            if (node instanceof Settings) {
                for (String[] path : ((Settings) node).getDeepKeys()) {
                    final String[] deepPath = new String[path.length + 1];
                    deepPath[0] = node.getKey();
                    System.arraycopy(path, 0, deepPath, 1, path.length);
                    list.add(deepPath);
                }
            } else {
                list.add(new String[] {node.getKey()});
            }
        }
        return list;
    }

    @Nullable
    public Memory getMemory() {
        return memory;
    }

    @NotNull
    @Contract("_ -> this")
    public Settings set(@Nullable Map<?, ?> map) {
        return set(map, false);
    }

    @NotNull
    @Contract("_, _ -> this")
    public Settings set(@Nullable Map<?, ?> map, boolean replace) {
        if (map != null) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                set(String.valueOf(entry.getKey()), entry.getValue(), replace);
            }
        }
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public Settings set(@Nullable Set<SettingsNode> nodes) {
        return set(nodes, false);
    }

    @NotNull
    @Contract("_, _ -> this")
    public Settings set(@Nullable Set<SettingsNode> nodes, boolean replace) {
        if (nodes != null) {
            for (SettingsNode node : nodes) {
                set(node.getKey(), node, replace);
            }
        }
        return this;
    }

    public boolean set(@NotNull String key, @Nullable Object object) {
        return set(key, object, false);
    }

    public boolean set(@NotNull String key, @Nullable Object object, boolean replace) {
        if (object == null) {
            remove(key);
            return true;
        }
        final SettingsNode node;
        if (object instanceof SettingsNode) {
            if (!((SettingsNode) object).exists()) {
                return false;
            }
            node = (SettingsNode) object;
            node.delete();
            node.setParent(this);
            node.setKey(key);
        } else if (object instanceof OptionalType) {
            return set(key, ((OptionalType) object).getValue(), replace);
        } else if (object instanceof Map) {
            node = new Settings(this, key).set((Map<?, ?>) object, replace);
        } else {
            node = new SettingsNode(this, key, object);
        }

        if (getValue().add(node)) {
            keys.add(key);
            return true;
        } else if (replace) {
            remove(node.getKey());
            getValue().add(node);
            return true;
        } else if (node instanceof Settings) {
            final SettingsNode actualNode = get(key);
            if (actualNode instanceof Settings) {
                boolean result = false;
                for (SettingsNode childNode : ((Settings) node).getValue()) {
                    if (((Settings) actualNode).set(childNode.getKey(), childNode, false)) {
                        result = true;
                    }
                }
                return result;
            }
        }
        return false;
    }

    public boolean set(@NotNull String[] path, @Nullable Object object) {
        return set(path, object, false);
    }

    public boolean set(@NotNull String[] path, @Nullable Object object, boolean replace) {
        if (object == null) {
            return get(path).delete();
        }

        if (path.length < 1) {
            if (object instanceof Map) {
                set((Map<?, ?>) object, replace);
                return true;
            }
            return false;
        }
        if (path.length == 1) {
            return set(path[0], object, replace);
        }
        Settings settings = this;
        for (String key : Arrays.copyOf(path, path.length - 1)) {
            SettingsNode node = null;
            for (SettingsNode value : settings.getValue()) {
                if (value.getKey().equals(key)) {
                    node = value;
                    break;
                }
            }
            final Settings toAdd;
            if (node != null) {
                if (node instanceof Settings) {
                    settings = (Settings) node;
                    continue;
                } else {
                    if (!replace) {
                        return false;
                    }
                    settings.remove(key);
                    toAdd = new Settings(this, key);
                    toAdd.setTopComment(node.getTopComment());
                    toAdd.setSideComment(node.getSideComment());
                }
            } else {
                toAdd = new Settings(this, key);
            }
            settings.getValue().add(toAdd);
            settings.keys.add(key);
            settings = toAdd;
        }
        return settings.set(path[path.length - 1], object);
    }

    @NotNull
    @Contract("_ -> this")
    public Settings setMemory(@Nullable Memory memory) {
        this.memory = memory;
        this.memorizing = memory != null;
        return this;
    }

    @NotNull
    @Contract("-> this")
    public Settings setMapMemory() {
        return setMemory(new MapMemory());
    }

    public boolean remove(@NotNull String key) {
        return remove(key, false);
    }

    public boolean remove(@NotNull String key, boolean deep) {
        return removeIf(node -> node.getKey().equals(key), deep);
    }

    public boolean removeIf(@NotNull Predicate<SettingsNode> predicate) {
        return removeIf(predicate, false);
    }

    public boolean removeIf(@NotNull Predicate<SettingsNode> predicate, boolean deep) {
        final boolean bool = getValue().removeIf(node -> {
            if (predicate.test(node)) {
                keys.remove(node.getKey());
                if (memorizing) {
                    memory.remove(node);
                }
                return true;
            }
            return false;
        });
        if (deep && getValue().isEmpty() && getParent() != null) {
            return getParent().remove(getKey(), true);
        }
        return bool;
    }

    public boolean isMemorizing() {
        return memorizing;
    }

    @Override
    public <T> T as(@NotNull Type type) {
        return OptionalType.GSON.fromJson(asJson(), type);
    }

    @Override
    public <T> T as(@NotNull Class<T> type) {
        return OptionalType.GSON.fromJson(asJson(), type);
    }

    @NotNull
    public Map<String, Object> asMap() {
        final Map<String, Object> map = new HashMap<>();
        for (SettingsNode node : getValue()) {
            if (node instanceof Settings) {
                map.put(node.getKey(), ((Settings) node).asMap());
            } else {
                map.put(node.getKey(), node.getValue());
            }
        }
        return map;
    }

    public String asJson() {
        if (keys.isEmpty()) {
            return "{}";
        }
        final StringJoiner joiner = new StringJoiner(", ", "{", "}");
        for (SettingsNode node : getValue()) {
            joiner.add("\"").add(node.getKey()).add("\": ").add(asJson(node.getValue()));
        }
        return joiner.toString();
    }

    protected String asJson(Object object) {
        if (object == null) {
            return "null";
        }

        if (object instanceof Settings) {
            return ((Settings) object).asJson();
        } else if (object instanceof OptionalType && !((OptionalType) object).isIterable()) {
            return asJson(((OptionalType) object).getValue());
        }
        if (object instanceof Iterable) {
            final StringJoiner joiner = new StringJoiner(", ", "[", "]");
            for (Object o : (Iterable<?>) object) {
                joiner.add(asJson(o));
            }
            return joiner.toString();
        } else if (object instanceof Map) {
            final StringJoiner joiner = new StringJoiner(", ", "{", "}");
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
                joiner.add(asJson(entry.getKey())).add(": ").add(asJson(entry.getValue()));
            }
            return joiner.toString();
        } else if (object instanceof Boolean || object instanceof Number) {
            return String.valueOf(object);
        } else {
            return '"' + String.valueOf(object) + '"';
        }
    }

    @NotNull
    public <T> NodeSupplier<T> supplier(@NotNull Function<Settings, T> function) {
        final NodeSupplier<T> supplier = new NodeSupplier<>(function, function.apply(this));
        if (suppliers == null) {
            suppliers = new ArrayList<>();
        }
        suppliers.add(supplier);
        return supplier;
    }

    public void updateSuppliers() {
        if (suppliers != null) {
            for (NodeSupplier<?> supplier : suppliers) {
                supplier.update(this);
            }
        }
    }

    public interface Memory {

        @Nullable
        SettingsNode get(@NotNull String id);

        void save(@NotNull String id, @NotNull SettingsNode node);

        void remove(@NotNull SettingsNode node);

        void clear();
    }

    public static class MapMemory implements Memory {

        private final Map<String, SettingsNode> map = new HashMap<>();

        @Override
        public @Nullable SettingsNode get(@NotNull String id) {
            return map.get(id);
        }

        @Override
        public void save(@NotNull String id, @NotNull SettingsNode node) {
            map.put(id, node);
        }

        @Override
        public void remove(@NotNull SettingsNode node) {
            while (map.values().remove(node));
        }

        @Override
        public void clear() {
            map.clear();
        }
    }
}
