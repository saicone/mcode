package com.saicone.mcode.module.lang;

import com.saicone.mcode.util.DMap;
import com.saicone.mcode.util.MLocale;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

@FunctionalInterface
public interface LangSupplier {

    Locale DEFAULT_LOCALE = MLocale.fromMinecraftLocale("en_us");
    Set<Locale> DEFAULT_LOCALE_TYPES = Set.of(MLocale.fromMinecraftLocale("en_us"), MLocale.fromMinecraftLocale("es_es"));
    Map<Locale, Locale> DEFAULT_LOCALE_ALIASES = Map.of(
            MLocale.fromMinecraftLocale("en_au"), MLocale.fromMinecraftLocale("en_us"),
            MLocale.fromMinecraftLocale("en_ca"), MLocale.fromMinecraftLocale("en_us"),
            MLocale.fromMinecraftLocale("en_gb"), MLocale.fromMinecraftLocale("en_us"),
            MLocale.fromMinecraftLocale("en_nz"), MLocale.fromMinecraftLocale("en_us"),
            MLocale.fromMinecraftLocale("es_ar"), MLocale.fromMinecraftLocale("es_es"),
            MLocale.fromMinecraftLocale("es_cl"), MLocale.fromMinecraftLocale("es_es"),
            MLocale.fromMinecraftLocale("es_ec"), MLocale.fromMinecraftLocale("es_es"),
            MLocale.fromMinecraftLocale("es_mx"), MLocale.fromMinecraftLocale("es_es"),
            MLocale.fromMinecraftLocale("es_uy"), MLocale.fromMinecraftLocale("es_es"),
            MLocale.fromMinecraftLocale("es_ve"), MLocale.fromMinecraftLocale("es_es")
    );
    int DEFAULT_LOG_LEVEL = 2;

    default void load() {
        // empty default method
    }

    @NotNull Locale getDefaultLocale();

    @NotNull
    default Locale getHolderLocale(@Nullable Object holder) {
        return getDefaultLocale();
    }

    @NotNull
    default Locale getEffectiveLocale(@NotNull Locale locale) {
        if (getLocaleTypes().contains(locale)) {
            return locale;
        }

        return getLocaleAliases().getOrDefault(locale, getDefaultLocale());
    }

    @NotNull
    default Locale getEffectiveLocale(@Nullable Object object) {
        final Locale locale;
        if (object instanceof Locale) {
            locale = (Locale) object;
        } else if (object instanceof String) {
            locale = MLocale.fromMinecraftLocale((String) object, getDefaultLocale());
        } else {
            locale = getHolderLocale(object);
        }

        return getEffectiveLocale(locale);
    }

    @NotNull
    default Set<Locale> getLocaleTypes() {
        return DEFAULT_LOCALE_TYPES;
    }

    @NotNull
    default Map<Locale, Locale> getLocaleAliases() {
        return DEFAULT_LOCALE_ALIASES;
    }

    default int getLogLevel() {
        return DEFAULT_LOG_LEVEL;
    }

    class Value<T> extends Path {

        private BiFunction<Object, Object, T> parser;
        private boolean memoize;

        private transient Map<Locale, Object> values;
        private transient final Map<Locale, T> cache = new HashMap<>();

        @NotNull
        public static <T> Value<T> path(@NotNull String path, @NotNull String... aliases) {
            return new Value<>(path, aliases);
        }

        public Value(@NotNull String path, @NotNull String... aliases) {
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
        @SuppressWarnings("unchecked")
        public <SenderT> Value<T> parser(@NotNull BiFunction<SenderT, Object, T> parser) {
            this.parser = (BiFunction<Object, Object, T>) parser;
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
                final Locale locale = getHolder().getEffectiveLocale(sender);
                T cached = this.cache.get(locale);
                if (cached == null) {
                    cached = this.parser.apply(sender, getValue(locale));
                    this.cache.put(locale, cached);
                }
                return cached;
            }
            return this.parser.apply(sender, getValue(getHolder().getEffectiveLocale(sender)));
        }

        @NotNull
        public Map<Locale, Object> getValues() {
            if (values == null) {
                values = new HashMap<>();
                if (getHolder() instanceof AbstractLang) {
                    final AbstractLang<?> lang = (AbstractLang<?>) getHolder();
                    for (Map.Entry<Locale, DMap> entry : lang.getObjects().entrySet()) {
                        final Object value = compute(entry.getKey(), entry.getValue());
                        if (value != null) {
                            values.put(entry.getKey(), value);
                        }
                    }
                }
            }
            return values;
        }

        @Nullable
        public Object compute(@NotNull Locale locale, @NotNull DMap map) {
            Object value = map.getDeep(getPath().split("\\."));
            if (value == null) {
                for (String alias : getAliases()) {
                    value = map.getDeep(alias.split("\\."));
                    if (value != null) {
                        break;
                    }
                }
            }
            return value;
        }

        @Nullable
        public Object getValue(@NotNull Locale locale) {
            final Object value = getValues().get(locale);
            if (value != null) {
                return value;
            }
            return getValues().get(getHolder().getDefaultLocale());
        }

        public void setValue(@Nullable Object value) {
            setValue(getHolder().getDefaultLocale(), value);
        }

        public void setValue(@NotNull Locale locale, @Nullable Object value) {
            getValues().put(locale, value);
        }

        public void clear() {
            this.values = null;
            this.cache.clear();
        }
    }

    class Path {

        private final String path;
        private final Set<String> aliases;

        private DisplayHolder<?> holder = null;

        @NotNull
        public static Path of(@NotNull String path, @NotNull String... aliases) {
            return new Path(path, aliases);
        }

        public Path(@NotNull String path, @NotNull String... aliases) {
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

        @ApiStatus.Internal
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
