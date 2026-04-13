package com.saicone.mcode.module.lang;

import com.saicone.mcode.platform.Text;
import com.saicone.mcode.util.text.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class DisplayHolder<SenderT> implements LangSupplier {

    private final Map<Locale, Map<String, Display<SenderT>>> displays = new HashMap<>();

    @Nullable
    public Display<SenderT> put(@NotNull Locale locale, @NotNull String key, @NotNull Display<SenderT> display) {
        if (!displays.containsKey(locale)) {
            displays.put(locale, new HashMap<>());
        }
        return displays.get(locale).put(key, display);
    }

    @Override
    public @NotNull Locale getDefaultLocale() {
        return DEFAULT_LOCALE;
    }

    @NotNull
    public Display<SenderT> getDisplay(@NotNull String key) {
        return getDisplay(getDefaultLocale(), key);
    }

    @NotNull
    public Display<SenderT> getDisplay(@NotNull Locale locale, @NotNull String key) {
        final Display<SenderT> display = getDisplayOrNull(locale, key);
        return display != null ? display : Display.empty();
    }

    @Nullable
    public Display<SenderT> getDisplayOrNull(@NotNull String key) {
        return getDisplayOrNull(getDefaultLocale(), key);
    }

    @Nullable
    public Display<SenderT> getDisplayOrNull(@NotNull Locale locale, @NotNull String key) {
        final Map<String, Display<SenderT>> displays = getDisplays(locale);
        if (displays == null) {
            return null;
        }
        return displays.get(key);
    }

    @NotNull
    public Display<SenderT> getDisplayOrDefault(@NotNull Locale locale, @NotNull String key) {
        final Display<SenderT> display = getDisplayOrNull(locale, key);
        if (display != null) {
            return display;
        }
        return Display.empty();
    }

    @Nullable
    public Map<String, Display<SenderT>> getDisplays(@NotNull Locale locale) {
        return displays.get(locale);
    }

    @NotNull
    protected abstract SenderT getConsole();

    @NotNull
    protected abstract Collection<? extends SenderT> getSenders();

    public void clear() {
        for (var entry : displays.entrySet()) {
            entry.getValue().clear();
        }
        displays.clear();
    }

    public void clear(@NotNull Locale locale) {
        displays.entrySet().removeIf(entry -> {
            if (entry.getKey().equals(locale)) {
                entry.getValue().clear();
                return true;
            }
            return false;
        });
    }

    @NotNull
    public <T> Value<T> value(@NotNull String path, @NotNull String... aliases) {
        final Value<T> value = new Value<>(path, aliases);
        value.setHolder(this);
        return value;
    }

    @NotNull
    public Path path(@NotNull String path, @NotNull String... aliases) {
        final Path p = new Path(path, aliases);
        p.setHolder(this);
        return p;
    }

    protected abstract void log(int level, @NotNull String msg);

    protected void log(int level, @NotNull Throwable exception) {
        log(level, "", exception);
    }

    protected abstract void log(int level, @NotNull String msg, @NotNull Throwable exception);

    public void sendLog(int level, @NotNull String msg, @Nullable Object... args) {
        if (getLogLevel() < level) {
            return;
        }
        log(level, Strings.replaceArgs(msg, args));
    }

    public void sendLog(int level, @NotNull Throwable exception) {
        if (getLogLevel() < level) {
            return;
        }
        log(level, exception);
    }

    public void sendLog(int level, @NotNull Throwable exception, @NotNull String msg, @Nullable Object... args) {
        if (getLogLevel() < level) {
            return;
        }
        log(level, Strings.replaceArgs(msg, args), exception);
    }

    public void sendTo(@NotNull SenderT sender, @NotNull String path, @Nullable Object... args) {
        sendTo(sender, getHolderLocale(sender), path, args);
    }

    protected void sendTo(@NotNull SenderT sender, @NotNull Locale locale, @NotNull String path, @Nullable Object... args) {
        getDisplayOrDefault(getEffectiveLocale(locale), path).sendArgs(sender, args);
    }

    public void sendTo(@NotNull SenderT sender, @NotNull String path, @NotNull Function<String, String> parser) {
        sendTo(sender, getHolderLocale(sender), path, parser);
    }

    protected void sendTo(@NotNull SenderT sender, @NotNull Locale locale, @NotNull String path, @NotNull Function<Text, Text> parser) {
        getDisplayOrDefault(getEffectiveLocale(locale), path).sendTo(sender, parser);
    }

    public void sendWith(@NotNull SenderT agent, @NotNull SenderT sender, @NotNull String path, @Nullable Object... args) {
        sendWith(agent, sender, getHolderLocale(sender), path, args);
    }

    protected void sendWith(@NotNull SenderT agent, @NotNull SenderT sender, @NotNull Locale locale, @NotNull String path, @Nullable Object... args) {
        getDisplayOrDefault(getEffectiveLocale(locale), path).sendArgsWith(agent, sender, args);
    }

    public void sendToConsole(@NotNull String path, @Nullable Object... args) {
        getDisplay(getHolderLocale(null), path).sendArgs(getConsole(), args);
    }

    public void sendToConsole(@NotNull String path, @NotNull Function<Text, Text> parser) {
        getDisplay(getHolderLocale(null), path).sendTo(getConsole(), parser);
    }

    public void sendToConsoleWith(@NotNull SenderT agent, @NotNull String path, @Nullable Object... args) {
        getDisplay(getHolderLocale(null), path).sendArgsWith(agent, getConsole(), args);
    }

    public void sendToAll(@NotNull String path, @Nullable Object... args) {
        getDisplay(getDefaultLocale(), path).sendArgs(getSenders(), args);
    }

    public void sendToAll(@NotNull Locale locale, @NotNull String path, @Nullable Object... args) {
        getDisplayOrDefault(getEffectiveLocale(locale), path).sendArgs(getSenders(), args);
    }

    public void sendToAll(@NotNull String path, @NotNull Function<Text, Text> parser) {
        getDisplay(getDefaultLocale(), path).sendTo(getSenders(), parser);
    }

    public void sendToAll(@NotNull Locale locale, @NotNull String path, @NotNull Function<Text, Text> parser) {
        getDisplayOrDefault(getEffectiveLocale(locale), path).sendTo(getSenders(), parser);
    }

    public void sendToAll(@NotNull String path, @NotNull Function<Text, Text> parser, @NotNull BiFunction<SenderT, Text, Text> playerParser) {
        getDisplay(getDefaultLocale(), path).sendTo(getSenders(), parser, playerParser);
    }

    public void sendToAll(@NotNull Locale locale, @NotNull String path, @NotNull Function<Text, Text> parser, @NotNull BiFunction<SenderT, Text, Text> playerParser) {
        getDisplayOrDefault(getEffectiveLocale(locale), path).sendTo(getSenders(), parser, playerParser);
    }

    public void sendToAllWith(@NotNull SenderT agent, @NotNull String path, @Nullable Object... args) {
        getDisplay(getDefaultLocale(), path).sendArgsWith(agent, getSenders(), args);
    }

    public void sendToAllWith(@NotNull SenderT agent, @NotNull Locale locale, @NotNull String path, @Nullable Object... args) {
        getDisplayOrDefault(getEffectiveLocale(locale), path).sendArgsWith(agent, getSenders(), args);
    }
}
