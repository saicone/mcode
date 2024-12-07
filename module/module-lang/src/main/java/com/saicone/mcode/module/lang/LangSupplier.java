package com.saicone.mcode.module.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    class Path {

        private final String path;
        private final Set<String> aliases;

        private DisplayHolder<?> holder = null;

        @NotNull
        public static Path of(@NotNull String path, @NotNull String... oldPaths) {
            return new Path(path, oldPaths);
        }

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

        @Nullable
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
