package com.saicone.mcode.module.lang;

import com.saicone.mcode.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class DisplayHolder<SenderT> implements LangSupplier {

    protected final Map<String, Map<String, Display<SenderT>>> displays = new HashMap<>();

    @Nullable
    public Display<SenderT> put(@NotNull String language, @NotNull String key, @NotNull Display<SenderT> display) {
        if (!displays.containsKey(language)) {
            displays.put(language, new HashMap<>());
        }
        return displays.get(language).put(key, display);
    }

    @Override
    public @NotNull String getLanguage() {
        return DEFAULT_LANGUAGE;
    }

    @NotNull
    public Display<SenderT> getDisplay(@NotNull String key) {
        return getDisplay(getLanguage(), key);
    }

    @NotNull
    public Display<SenderT> getDisplay(@NotNull Object language, @NotNull String key) {
        final Display<SenderT> display = getDisplayOrNull(language, key);
        return display != null ? display : Display.empty();
    }

    @Nullable
    public Display<SenderT> getDisplayOrNull(@NotNull String key) {
        return getDisplayOrNull(getLanguage(), key);
    }

    @Nullable
    public Display<SenderT> getDisplayOrNull(@NotNull Object language, @NotNull String key) {
        final Map<String, Display<SenderT>> displays = getDisplays(language);
        if (displays == null) {
            return null;
        }
        return displays.get(key);
    }

    @NotNull
    public Display<SenderT> getDisplayOrDefault(@NotNull Object language, @NotNull String key) {
        final Display<SenderT> display = getDisplayOrNull(language, key);
        if (display == null && !getLanguage().equals(language)) {
            return getDisplay(key);
        }
        return Display.empty();
    }

    @Nullable
    public Map<String, Display<SenderT>> getDisplays(@NotNull Object language) {
        return displays.get(language instanceof String ? language : getLanguageFor(language));
    }

    @NotNull
    protected abstract SenderT getConsole();

    @NotNull
    protected abstract Collection<? extends SenderT> getSenders();

    public void clear() {
        for (Map.Entry<String, Map<String, Display<SenderT>>> entry : displays.entrySet()) {
            entry.getValue().clear();
        }
        displays.clear();
    }

    public void clear(@NotNull String language) {
        displays.entrySet().removeIf(entry -> {
            if (entry.getKey().equalsIgnoreCase(language)) {
                entry.getValue().clear();
                return true;
            }
            return false;
        });
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
        sendTo(sender, getLanguageFor(sender), path, args);
    }

    protected void sendTo(@NotNull SenderT sender, @NotNull String language, @NotNull String path, @Nullable Object... args) {
        getDisplayOrDefault(getEffectiveLanguage(language), path).sendArgs(sender, args);
    }

    public void sendTo(@NotNull SenderT sender, @NotNull String path, @NotNull Function<String, String> parser) {
        sendTo(sender, getLanguageFor(sender), path, parser);
    }

    protected void sendTo(@NotNull SenderT sender, @NotNull String language, @NotNull String path, @NotNull Function<String, String> parser) {
        getDisplayOrDefault(getEffectiveLanguage(language), path).sendTo(sender, parser);
    }

    public void sendWith(@NotNull SenderT agent, @NotNull SenderT sender, @NotNull String path, @Nullable Object... args) {
        sendWith(agent, sender, getLanguageFor(sender), path, args);
    }

    protected void sendWith(@NotNull SenderT agent, @NotNull SenderT sender, @NotNull String language, @NotNull String path, @Nullable Object... args) {
        getDisplayOrDefault(getEffectiveLanguage(language), path).sendArgsWith(agent, sender, args);
    }

    public void sendToConsole(@NotNull String path, @Nullable Object... args) {
        getDisplay(getLanguageFor(null), path).sendArgs(getConsole(), args);
    }

    public void sendToConsole(@NotNull String path, @NotNull Function<String, String> parser) {
        getDisplay(getLanguageFor(null), path).sendTo(getConsole(), parser);
    }

    public void sendToConsoleWith(@NotNull SenderT agent, @NotNull String path, @Nullable Object... args) {
        getDisplay(getLanguageFor(null), path).sendArgsWith(agent, getConsole(), args);
    }

    public void sendToAll(@NotNull String path, @Nullable Object... args) {
        getDisplay(getLanguage(), path).sendArgs(getSenders(), args);
    }

    public void sendToAll(@NotNull String language, @NotNull String path, @Nullable Object... args) {
        getDisplayOrDefault(getEffectiveLanguage(language), path).sendArgs(getSenders(), args);
    }

    public void sendToAll(@NotNull String path, @NotNull Function<String, String> parser) {
        getDisplay(getLanguage(), path).sendTo(getSenders(), parser);
    }

    public void sendToAll(@NotNull String language, @NotNull String path, @NotNull Function<String, String> parser) {
        getDisplayOrDefault(getEffectiveLanguage(language), path).sendTo(getSenders(), parser);
    }

    public void sendToAll(@NotNull String path, @NotNull Function<String, String> parser, @NotNull BiFunction<SenderT, String, String> playerParser) {
        getDisplay(getLanguage(), path).sendTo(getSenders(), parser, playerParser);
    }

    public void sendToAll(@NotNull String language, @NotNull String path, @NotNull Function<String, String> parser, @NotNull BiFunction<SenderT, String, String> playerParser) {
        getDisplayOrDefault(getEffectiveLanguage(language), path).sendTo(getSenders(), parser, playerParser);
    }

    public void sendToAllWith(@NotNull SenderT agent, @NotNull String path, @Nullable Object... args) {
        getDisplay(getLanguage(), path).sendArgsWith(agent, getSenders(), args);
    }

    public void sendToAllWith(@NotNull SenderT agent, @NotNull String language, @NotNull String path, @Nullable Object... args) {
        getDisplayOrDefault(getEffectiveLanguage(language), path).sendArgsWith(agent, getSenders(), args);
    }
}
