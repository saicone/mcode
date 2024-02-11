package com.saicone.mcode.module.lang;

import com.saicone.mcode.Platform;
import com.saicone.settings.Settings;
import com.saicone.settings.SettingsData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;

public abstract class AbstractLang<SenderT> extends DisplayHolder<SenderT> implements DisplaySupplier<SenderT> {

    private static final boolean ALLOW_SETTINGS = Platform.isAvailable("Settings");

    // Object parameters
    private LangSupplier langSupplier;
    private final Class<?>[] langProviders;

    // Instance fields parameters
    private Path[] paths = new Path[0];
    private final List<DisplayLoader<SenderT>> displayLoaders = new ArrayList<>();

    // Mutable parameters
    private transient boolean useSettings;
    private transient String filePrefix = ".yml";
    private transient String displayType = Display.DEFAULT_TYPE;

    public AbstractLang(@NotNull Object... providers) {
        LangSupplier langSupplier = null;
        final Class<?>[] langProviders = new Class[providers.length + 1];
        int i = 0;
        for (Object provider : providers) {
            if (langSupplier == null && provider instanceof LangSupplier) {
                langSupplier = (LangSupplier) provider;
            }
            langProviders[i] = provider instanceof Class ? (Class<?>) provider : provider.getClass();
            i++;
        }
        this.langSupplier = langSupplier;
        this.langProviders = langProviders;
        computePaths();
        computeDisplayLoaders();
    }

    public void load() {
        load(getLangFolder());
    }

    public void load(@NotNull File langFolder) {
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }
        // Save language files
        for (String language : getLanguageTypes()) {
            saveFile(langFolder, language + filePrefix);
            final int index = language.indexOf('_');
            if (index > 0) {
                final String formatted = language.substring(0, index).toLowerCase() + "_" + language.substring(index + 1).toUpperCase();
                if (!formatted.equals(language)) {
                    saveFile(langFolder, formatted + filePrefix);
                }
            }
        }
        getLangFiles(langFolder).forEach((key, list) -> list.forEach(file -> loadDisplays(key, file)));
    }

    protected void loadDisplays(@NotNull String language, @NotNull File file) {
        for (var entry : getObjects(file).entrySet()) {
            final Display<SenderT> display = loadDisplayOrNull(entry.getValue());
            if (display != null) {
                if (!displays.containsKey(language)) {
                    displays.put(language, new HashMap<>());
                }
                final Map<String, Display<SenderT>> map = displays.get(language);
                map.put(entry.getKey(), display);
            }
        }
    }

    public void unload() {
        clear();
    }

    protected abstract void saveFile(@NotNull File folder, @NotNull String name);

    public void setLangSupplier(@Nullable LangSupplier langSupplier) {
        this.langSupplier = langSupplier;
    }

    public void setUseSettings(boolean useSettings) {
        this.useSettings = useSettings;
    }

    public void setFilePrefix(@NotNull String filePrefix) {
        this.filePrefix = filePrefix;
    }

    public void setDisplayType(@NotNull String displayType) {
        this.displayType = displayType;
    }

    public boolean isUseSettings() {
        return useSettings;
    }

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
    private Map<String, Object> getObjects(@NotNull File file) {
        if (!useSettings) {
            return getFileObjects(file);
        }
        final Settings settings = SettingsData.of(file.getName()).load(file.getParentFile());
        final Map<String, Object> map = new HashMap<>();
        for (String[] path : settings.paths()) {
            map.put(String.join(".", path), settings.get(path).asLiteralObject());
        }
        return map;
    }

    @NotNull
    protected abstract Map<String, Object> getFileObjects(@NotNull File file);

    @Nullable
    public LangSupplier getLangSupplier() {
        return langSupplier;
    }

    @NotNull
    protected Class<?>[] getLangProviders() {
        return langProviders;
    }

    @NotNull
    public Path[] getPaths() {
        return paths;
    }

    @NotNull
    public String getFilePrefix() {
        return filePrefix;
    }

    @NotNull
    @Override
    public List<DisplayLoader<SenderT>> getDisplayLoaders() {
        return displayLoaders;
    }

    @NotNull
    @Override
    public String getDisplayType() {
        return displayType;
    }

    @Override
    public @NotNull String getLanguage() {
        if (langSupplier != null) {
            return langSupplier.getLanguage();
        }
        return super.getLanguage();
    }

    @Override
    public @NotNull Set<String> getLanguageTypes() {
        if (langSupplier != null) {
            return langSupplier.getLanguageTypes();
        }
        return super.getLanguageTypes();
    }

    @Override
    public @NotNull Map<String, String> getLanguageAliases() {
        if (langSupplier != null) {
            return langSupplier.getLanguageAliases();
        }
        return super.getLanguageAliases();
    }

    @Override
    public int getLogLevel() {
        if (langSupplier != null) {
            return langSupplier.getLogLevel();
        }
        return super.getLogLevel();
    }

    private void computePaths() {
        if (paths.length > 0) {
            return;
        }
        final List<Path> paths = new ArrayList<>();
        computeFields(getLangProviders(), Path.class, (name, path) -> {
            path.setHolder(this);
            paths.add(path);
        });
        this.paths = paths.toArray(new Path[0]);
    }

    @SuppressWarnings("unchecked")
    private void computeDisplayLoaders() {
        if (!displayLoaders.isEmpty()) {
            return;
        }
        final Set<String> loaded = new HashSet<>();
        computeFields(getLangProviders(), DisplayLoader.class, (name, loader) -> {
            if (loaded.contains(name)) {
                return;
            }
            displayLoaders.add(loader);
            loaded.add(name);
        });
    }

    @SuppressWarnings("unchecked")
    private <T> void computeFields(@NotNull Class<?>[] classes, @NotNull Class<T> classType, @NotNull BiConsumer<String, T> consumer) {
        for (Class<?> provided : classes) {
            // Check every superclass
            for (Class<?> clazz = provided; clazz != Object.class; clazz = clazz.getSuperclass()) {
                for (Field field : clazz.getDeclaredFields()) {
                    if (classType.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        try {
                            consumer.accept(field.getName().toLowerCase(), (T) field.get(this));
                        } catch (IllegalAccessException | ClassCastException e) {
                            sendLog(2, e);
                        }
                    }
                }
            }
        }
    }
}
