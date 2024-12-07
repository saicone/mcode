package com.saicone.mcode.module.lang;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

@FunctionalInterface
public interface LangSupplier {

    String DEFAULT_LANGUAGE = "en_us";
    Set<String> DEFAULT_LANGUAGE_TYPES = Set.of("en_us");
    Map<String, String> DEFAULT_LANGUAGE_ALIASES = Map.of(
            "en_au", "en_us",
            "en_ca", "en_us",
            "en_gb", "en_us",
            "en_nz", "en_us",
            "es_ar", "es_es",
            "es_cl", "es_es",
            "es_ec", "es_es",
            "es_mx", "es_es",
            "es_uy", "es_es",
            "es_ve", "es_es"
    );
    int DEFAULT_LOG_LEVEL = 2;

    default void load() {
        // empty default method
    }

    @NotNull
    String getLanguage();

    @NotNull
    default String getLanguageFor(@Nullable Object object) {
        return getLanguage();
    }

    @NotNull
    default Set<String> getLanguageTypes() {
        return DEFAULT_LANGUAGE_TYPES;
    }

    @NotNull
    default Map<String, String> getLanguageAliases() {
        return DEFAULT_LANGUAGE_ALIASES;
    }

    @NotNull
    default String getEffectiveLanguage(@NotNull Object language) {
        final String s = (language instanceof String ? (String) language : getLanguageFor(language)).toLowerCase();
        if (getLanguageTypes().contains(s)) {
            return s;
        }
        return getLanguageAliases().getOrDefault(s, getLanguage());
    }

    default int getLogLevel() {
        return DEFAULT_LOG_LEVEL;
    }

    @NotNull
    static <T> Value<T> value(@NotNull String path, @NotNull String... oldPaths) {
        return new Value<>(path, oldPaths);
    }

    @NotNull
    static Path path(@NotNull String path, @NotNull String... oldPaths) {
        return new Path(path, oldPaths);
    }

    class Value<T> extends Path {

        private BiFunction<Object, Object, T> parser;
        private boolean memoize;

        private transient final Map<String, Object> values = new HashMap<>();
        private transient final Map<String, T> cache = new HashMap<>();

        public Value(@NotNull String path, @NotNull String[] aliases) {
            super(path, aliases);
        }

        @NotNull
        @Contract("_ -> this")
        public Value<T> parser(@NotNull Function<Object, T> parser) {
            this.parser = (player, object) -> parser.apply(object);
            this.memoize = true;
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public Value<T> parser(@NotNull BiFunction<Object, Object, T> parser) {
            this.parser = parser;
            this.memoize = false;
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public Value<T> memoize(boolean memoize) {
            this.memoize = memoize;
            return this;
        }

        public <SenderT> T get(@NotNull SenderT sender) {
            if (this.memoize) {
                final String language = getHolder().getEffectiveLanguage(sender);
                T cached = this.cache.get(language);
                if (cached == null) {
                    cached = this.parser.apply(sender, getValue(language));
                    this.cache.put(language, cached);
                }
                return cached;
            }
            return this.parser.apply(sender, getValue(getHolder().getEffectiveLanguage(sender)));
        }

        @Nullable
        public Object getValue(@NotNull String language) {
            final Object value = this.values.get(language);
            if (value != null) {
                return value;
            }
            return this.values.get(getHolder().getLanguage());
        }

        public void setValue(@Nullable Object value) {
            setValue(getHolder().getLanguage(), value);
        }

        public void setValue(@NotNull String language, @Nullable Object value) {
            this.values.put(language, value);
        }

        public void clear() {
            this.values.clear();
            this.cache.clear();
        }
    }

    class Path {

        private final String path;
        private final Set<String> aliases;

        private DisplayHolder<?> holder = null;

        public Path(@NotNull String path, @NotNull String[] aliases) {
            this.path = path;
            this.aliases = Set.of(aliases);
        }

        @NotNull
        public String getPath() {
            return path;
        }

        @NotNull
        public Set<String> getAliases() {
            return aliases;
        }

        public DisplayHolder<?> getHolder() {
            return holder;
        }

        public void setHolder(@Nullable DisplayHolder<?> holder) {
            this.holder = holder;
        }

        @SuppressWarnings("unchecked")
        private <SenderT> DisplayHolder<SenderT> holder() {
            return (DisplayHolder<SenderT>) holder;
        }

        public <SenderT> void sendTo(@NotNull SenderT sender, @Nullable Object... args) {
            holder().sendTo(sender, path, args);
        }

        public <SenderT> void sendTo(@NotNull SenderT sender, @NotNull Function<String, String> parser) {
            holder().sendTo(sender, path, parser);
        }

        public <SenderT> void sendWith(@NotNull SenderT agent, @NotNull SenderT sender, @Nullable Object... args) {
            holder().sendWith(agent, sender, path, args);
        }

        public void sendToConsole(@Nullable Object... args) {
            holder.sendToConsole(path, args);
        }

        public void sendToConsole(@NotNull Function<String, String> parser) {
            holder.sendToConsole(path, parser);
        }

        public <SenderT> void sendToConsoleWith(@NotNull SenderT agent, @Nullable Object... args) {
            holder().sendToConsoleWith(agent, path, args);
        }

        public void sendToAll(@Nullable Object... args) {
            holder.sendToAll(path, args);
        }

        public void sendToAll(@NotNull String language, @Nullable Object... args) {
            holder.sendToAll(language, path, args);
        }

        public void sendToAll(@NotNull Function<String, String> parser) {
            holder.sendToAll(path, parser);
        }

        public void sendToAll(@NotNull String language, @NotNull Function<String, String> parser) {
            holder.sendToAll(language, path, parser);
        }

        public <SenderT> void sendToAll(@NotNull Function<String, String> parser, @NotNull BiFunction<SenderT, String, String> playerParser) {
            holder().sendToAll(path, parser, playerParser);
        }

        public <SenderT> void sendToAll(@NotNull String language, @NotNull Function<String, String> parser, @NotNull BiFunction<SenderT, String, String> playerParser) {
            holder().sendToAll(language, path, parser, playerParser);
        }

        public <SenderT> void sendToAllWith(@NotNull SenderT agent, @Nullable Object... args) {
            holder().sendToAllWith(agent, path, args);
        }

        public <SenderT> void sendToAllWith(@NotNull SenderT agent, @NotNull String language, @Nullable Object... args) {
            holder().sendToAllWith(agent, language, path, args);
        }
    }
}
