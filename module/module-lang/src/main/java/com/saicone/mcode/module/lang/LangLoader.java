package com.saicone.mcode.module.lang;

import com.saicone.mcode.module.lang.display.Display;
import com.saicone.mcode.platform.Text;
import com.saicone.mcode.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class LangLoader<SenderT, PlayerT extends SenderT> {

    protected static final String DEFAULT_LANGUAGE = "en_us";

    protected static final List<String> DEFAULT_LANGUAGES = List.of("en_US");

    protected static final Map<String, String> LANGUAGE_ALIASES = Map.of(
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


    private final Class<?>[] langProviders;

    private Path[] paths = new Path[0];
    private final List<DisplayLoader<SenderT>> displayLoaders = new ArrayList<>();

    protected String defaultLanguage = DEFAULT_LANGUAGE;
    protected final Map<String, String> languageAliases = new HashMap<>();
    protected final Map<String, String> playerLanguages = new HashMap<>();

    protected final Map<String, Map<String, Display<SenderT>>> displays = new HashMap<>();

    public LangLoader(@NotNull Class<?>... langProviders) {
        final int length = langProviders.length + 1;
        this.langProviders = Arrays.copyOf(langProviders, length);
        this.langProviders[length - 1] = getClass();
    }

    public void load() {
        load(getLangFolder());
    }

    public void load(@NotNull File langFolder) {
        defaultLanguage = getDefaultLanguage().toLowerCase();
        languageAliases.clear();
        languageAliases.putAll(getLanguageAliases());

        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }
        computePaths();
        computeDisplayLoaders();
        final Map<String, List<File>> langFiles = getLangFiles(langFolder);
        for (String defaultLanguage : getDefaultLanguages()) {
            final String key = defaultLanguage.toLowerCase();
            if (!langFiles.containsKey(key)) {
                final File file = saveDefaultLang(langFolder, defaultLanguage);
                if (file != null) {
                    final List<File> list = new ArrayList<>();
                    list.add(file);
                    langFiles.put(key, list);
                }
            }
        }
        langFiles.forEach((key, list) -> list.forEach(file -> loadDisplays(key, file)));
    }

    protected void loadDisplays(@NotNull String name, @NotNull File file) {
        for (var entry : getFileObjects(file).entrySet()) {
            final Display<SenderT> display = loadDisplay(entry.getValue());
            if (display != null) {
                if (!displays.containsKey(name)) {
                    displays.put(name, new HashMap<>());
                }
                final Map<String, Display<SenderT>> map = displays.get(name);
                map.put(entry.getKey(), display);
            }
        }
    }

    @Nullable
    protected Display<SenderT> loadDisplay(@Nullable Object object) {
        if (object == null) {
            return null;
        }

        if (object instanceof List) {
            final List<Display<SenderT>> list = new ArrayList<>();
            for (Object o : (List<?>) object) {
                final Display<SenderT> display = loadDisplay(o);
                if (display != null) {
                    list.add(display);
                }
            }
            if (list.isEmpty()) {
                return null;
            }
            return Display.of(list);
        }

        if (object instanceof Map) {
            for (var entry : ((Map<?, ?>) object).entrySet()) {
                if (String.valueOf(entry.getKey()).equalsIgnoreCase("type")) {
                    return getDisplayLoader(String.valueOf(entry.getValue())).load(object);
                }
            }
        }
        return getDisplayLoader("text").load(object);
    }

    private void computePaths() {
        if (paths.length > 0) {
            return;
        }
        final List<Path> paths = new ArrayList<>();
        getFieldsFrom(getLangProviders(), field -> field.canAccess(null) && Path.class.isAssignableFrom(field.getType()), field -> {
            try {
                final Path path = (Path) field.get(null);
                path.setLoader(this);
                paths.add(path);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        this.paths = paths.toArray(new Path[0]);
    }

    @SuppressWarnings("unchecked")
    private void computeDisplayLoaders() {
        if (!displayLoaders.isEmpty()) {
            return;
        }
        getFieldsFrom(getLangProviders(), field -> field.canAccess(null) && DisplayLoader.class.isAssignableFrom(field.getType()), field -> {
            try {
                final DisplayLoader<SenderT> loader = (DisplayLoader<SenderT>) field.get(null);
                displayLoaders.add(loader);
            } catch (IllegalAccessException | ClassCastException e) {
                e.printStackTrace();
            }
        });
    }

    public void unload() {
        languageAliases.clear();
        playerLanguages.clear();
        for (var entry : displays.entrySet()) {
            entry.getValue().clear();
        }
        displays.clear();
    }

    @Nullable
    protected abstract File saveDefaultLang(@NotNull File folder, @NotNull String name);

    @NotNull
    public abstract File getLangFolder();

    @NotNull
    protected Map<String, List<File>> getLangFiles(@NotNull File langFolder) {
        final Map<String, List<File>> map = new HashMap<>();
        final File[] files = langFolder.listFiles();
        if (files == null) {
            return map;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                getLangFiles(langFolder).forEach((key, list) -> map.computeIfAbsent(key, s -> new ArrayList<>()).addAll(list));
            } else {
                final int index = file.getName().lastIndexOf('.');
                final String name = index >= 1 ? file.getName().substring(0, index) : file.getName();
                map.computeIfAbsent(name.toLowerCase(), s -> new ArrayList<>()).add(file);
            }
        }
        return map;
    }

    @NotNull
    protected abstract Map<String, Object> getFileObjects(@NotNull File file);

    @NotNull
    protected Class<?>[] getLangProviders() {
        return langProviders;
    }

    @NotNull
    public DisplayLoader<SenderT> getDisplayLoader(@NotNull String id) {
        for (DisplayLoader<SenderT> loader : getDisplayLoaders()) {
            if (loader.matches(id)) {
                return loader;
            }
        }
        return DisplayLoader.of();
    }

    @NotNull
    public List<DisplayLoader<SenderT>> getDisplayLoaders() {
        return displayLoaders;
    }

    public int getLogLevel() {
        return 2;
    }

    @NotNull
    public Path[] getPaths() {
        return paths;
    }

    @NotNull
    public String getLanguage(@NotNull String lang) {
        if (displays.containsKey(lang)) {
            return lang;
        } else {
            return languageAliases.getOrDefault(lang, defaultLanguage);
        }
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public String getLanguage(@NotNull SenderT sender) {
        if (isInstanceOfPlayer(sender)) {
            return getPlayerLanguage((PlayerT) sender);
        } else {
            return getPluginLanguage();
        }
    }

    @NotNull
    public Map<String, String> getLanguageAliases() {
        return LANGUAGE_ALIASES;
    }

    @NotNull
    public String getPluginLanguage() {
        return DEFAULT_LANGUAGE;
    }

    @NotNull
    public String getDefaultLanguage() {
        return DEFAULT_LANGUAGE;
    }

    @NotNull
    public List<String> getDefaultLanguages() {
        return DEFAULT_LANGUAGES;
    }

    @NotNull
    public String getPlayerLanguage(@NotNull PlayerT player) {
        final String name = getPlayerName(player);
        if (!playerLanguages.containsKey(name)) {
            playerLanguages.put(name, getLanguage(getPlayerLocale(player).toLowerCase()));
        }
        return playerLanguages.get(name);
    }

    @NotNull
    public Map<String, String> getPlayerLanguages() {
        return playerLanguages;
    }

    @NotNull
    protected abstract String getPlayerName(@NotNull PlayerT player);

    @NotNull
    protected abstract String getPlayerLocale(@NotNull PlayerT player);

    @NotNull
    public abstract Collection<? extends PlayerT> getPlayers();

    @NotNull
    protected abstract SenderT getConsoleSender();

    @NotNull
    public Map<String, Map<String, Display<SenderT>>> getDisplays() {
        return displays;
    }

    @NotNull
    public Map<String, Display<SenderT>> getDisplays(@NotNull SenderT sender) {
        return getDisplays(getLanguage(sender));
    }

    @NotNull
    public Map<String, Display<SenderT>> getDisplays(@NotNull String language) {
        final Map<String, Display<SenderT>> map = getDisplaysOrNull(language);
        if (map != null) {
            return map;
        } else if (!language.equals(defaultLanguage)) {
            return displays.getOrDefault(defaultLanguage, Map.of());
        } else {
            return Map.of();
        }
    }

    @Nullable
    public Map<String, Display<SenderT>> getDisplaysOrNull(@NotNull SenderT sender) {
        return getDisplaysOrNull(getLanguage(sender));
    }

    @Nullable
    public Map<String, Display<SenderT>> getDisplaysOrNull(@NotNull String language) {
        return displays.get(language);
    }

    @NotNull
    public Display<SenderT> getDisplay(@NotNull SenderT sender, @NotNull String path) {
        return getDisplay(getLanguage(sender), path);
    }

    @NotNull
    public Display<SenderT> getDisplay(@NotNull String language, @NotNull String path) {
        final Display<SenderT> display = getDisplayOrNull(language, path);
        if (display != null) {
            return display;
        } else if (!language.equals(defaultLanguage)) {
            return getDefaultDisplay(path);
        } else {
            return Display.of();
        }
    }

    @Nullable
    public Display<SenderT> getDisplayOrNull(@NotNull SenderT sender, @NotNull String path) {
        return getDisplayOrNull(getLanguage(sender), path);
    }

    @Nullable
    public Display<SenderT> getDisplayOrNull(@NotNull String language, @NotNull String path) {
        return getDisplays(language).get(path);
    }

    @NotNull
    public Display<SenderT> getDefaultDisplay(@NotNull String path) {
        return getDisplays(defaultLanguage).getOrDefault(path, Display.of());
    }

    @Nullable
    public Display<SenderT> getDefaultDisplayOrNull(@NotNull String path) {
        return getDisplays(defaultLanguage).get(path);
    }

    @NotNull
    public String getLangText(@NotNull String path) {
        return getDisplay(getPluginLanguage(), path).getText();
    }

    @NotNull
    public String getLangText(@NotNull String path, @NotNull String type) {
        return getDisplay(getPluginLanguage(), path).getText(type);
    }

    @NotNull
    public String getLangText(@NotNull SenderT sender, @NotNull String path) {
        return getDisplay(sender, path).getText();
    }

    @NotNull
    public String getLangText(@NotNull SenderT sender, @NotNull String path, @NotNull String type) {
        return getDisplay(sender, path).getText(type);
    }

    public abstract boolean isInstanceOfSender(@Nullable Object object);

    public abstract boolean isInstanceOfPlayer(@Nullable Object object);

    public void sendLog(int level, @NotNull String msg, @Nullable Object... args) {
        if (getLogLevel() < level) {
            return;
        }
        for (String s : Strings.replaceArgs(msg, args).split("\n")) {
            sendLogToConsole(level, s);
        }
    }

    protected abstract void sendLogToConsole(int level, @NotNull String msg);

    public void sendTo(@NotNull SenderT sender, @NotNull String path, @Nullable Object... args) {
        sendTo(sender, getLanguage(sender), path, args);
    }

    protected void sendTo(@NotNull SenderT sender, @NotNull String language, @NotNull String path, @Nullable Object... args) {
        getDisplay(language, path).sendTo(this, sender, args);
    }

    public void sendTo(@NotNull SenderT agent, @NotNull SenderT sender, @NotNull String path, @Nullable Object... args) {
        sendTo(agent, sender, getLanguage(sender), path, args);
    }

    protected void sendTo(@NotNull SenderT agent, @NotNull SenderT sender, @NotNull String language, @NotNull String path, @Nullable Object... args) {
        getDisplay(language, path).sendTo(this, agent, sender, args);
    }

    public void sendToConsole(@NotNull String path, @Nullable Object... args) {
        sendTo(getConsoleSender(), path, args);
    }

    public void sendToConsole(@NotNull SenderT agent, @NotNull String path, @Nullable Object... args) {
        sendTo(agent, getConsoleSender(), path, args);
    }

    public void sendToAll(@NotNull String path, @Nullable Object... args) {
        sendToAll(defaultLanguage, path, args);
    }

    public void sendToAll(@NotNull String language, @NotNull String path, @Nullable Object... args) {
        getDisplay(language, path).sendToAll(this, args);
    }

    public void sendToAll(@NotNull SenderT agent, @NotNull String path, @Nullable Object... args) {
        sendToAll(agent, defaultLanguage, path, args);
    }

    public void sendToAll(@NotNull SenderT agent, @NotNull String language, @NotNull String path, @Nullable Object... args) {
        getDisplay(language, path).sendToAll(this, agent, args);
    }

    @NotNull
    public String parse(@NotNull SenderT sender, @Nullable String text) {
        return text == null ? "null" : Text.of(text).parse(sender).getString();
    }

    @NotNull
    public String parse(@NotNull SenderT agent, @NotNull SenderT sender, @Nullable String text) {
        return text == null ? "null" : parse(sender, parseAgent(agent, text));
    }

    @NotNull
    public String parseAgent(@NotNull SenderT agent, @Nullable String text) {
        return text == null ? "null" : Text.of(text).parseAgent(agent).getString();
    }

    @NotNull
    protected String[] splitPath(@NotNull String path) {
        return path.split("\\.");
    }

    protected void getFieldsFrom(@NotNull Class<?>[] classes, @NotNull Predicate<Field> filter, @NotNull Consumer<Field> consumer) {
        for (Class<?> provided : classes) {
            // Check every superclass
            for (Class<?> clazz = provided; clazz != Object.class; clazz = clazz.getSuperclass()) {
                for (Field field : clazz.getDeclaredFields()) {
                    if (filter.test(field)) {
                        consumer.accept(field);
                    }
                }
            }
        }
    }

    public static class Path {

        private final String path;
        private final String[] oldPaths;

        private LangLoader<?, ?> loader = null;

        @NotNull
        public static Path of(@NotNull String path, @NotNull String... oldPaths) {
            return new Path(path, oldPaths);
        }

        public Path(@NotNull String path, @NotNull String[] oldPaths) {
            this.path = path;
            this.oldPaths = oldPaths;
        }

        @NotNull
        public String getPath() {
            return path;
        }

        @NotNull
        public String[] getOldPaths() {
            return oldPaths;
        }

        @Nullable
        public LangLoader<?, ?> getLoader() {
            return loader;
        }

        public void setLoader(@Nullable LangLoader<?, ?> loader) {
            this.loader = loader;
        }

        @SuppressWarnings("unchecked")
        private <SenderT> LangLoader<SenderT, ?> loader() {
            return (LangLoader<SenderT, ?>) loader;
        }

        public <SenderT> void sendTo(@NotNull SenderT sender, @Nullable Object... args) {
            loader().sendTo(sender, path, args);
        }

        public <SenderT> void sendTo(@NotNull SenderT agent, @NotNull SenderT sender, @Nullable Object... args) {
            loader().sendTo(agent, sender, path, args);
        }

        public void sendToConsole(@Nullable Object... args) {
            loader.sendToConsole(path, args);
        }

        public <SenderT> void sendToConsole(@NotNull SenderT agent, @Nullable Object... args) {
            loader().sendToConsole(agent, path, args);
        }

        public void sendToAll(@Nullable Object... args) {
            loader.sendToAll(path, args);
        }

        public void sendToAll(@NotNull String language, @Nullable Object... args) {
            loader.sendToAll(language, path, args);
        }

        public <SenderT> void sendToAll(@NotNull SenderT agent, @Nullable Object... args) {
            loader().sendToAll(agent, path, args);
        }

        public <SenderT> void sendToAll(@NotNull SenderT agent, @NotNull String language, @Nullable Object... args) {
            loader().sendToAll(agent, language, path, args);
        }
    }
}