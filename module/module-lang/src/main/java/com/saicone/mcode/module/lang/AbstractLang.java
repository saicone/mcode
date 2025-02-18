package com.saicone.mcode.module.lang;

import com.saicone.mcode.util.DMap;
import com.saicone.settings.SettingsData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;

public abstract class AbstractLang<SenderT> extends DisplayHolder<SenderT> implements DisplaySupplier<SenderT> {

    // Object parameters
    private LangSupplier langSupplier;
    private final Class<?>[] langProviders;

    // Instance fields parameters
    private Path[] paths;
    private List<DisplayLoader<SenderT>> displayLoaders;

    // Mutable parameters
    private transient boolean useSettings;
    private transient String fileSuffix = ".yml";
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
        langProviders[langProviders.length - 1] = this.getClass();
    }

    public void load() {
        load(getLangFolder());
    }

    public void load(@NotNull File langFolder) {
        if (langSupplier != null) {
            langSupplier.load();
        }
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }
        // Save language files
        for (String language : getLanguageTypes()) {
            saveFile(langFolder, language + fileSuffix);
            final int index = language.indexOf('_');
            if (index > 0) {
                final String formatted = language.substring(0, index).toLowerCase() + "_" + language.substring(index + 1).toUpperCase();
                if (!formatted.equals(language)) {
                    saveFile(langFolder, formatted + fileSuffix);
                }
            }
        }
        getLangFiles(langFolder).forEach((key, list) -> list.forEach(file -> loadDisplays(key, file)));
    }

    protected void loadDisplays(@NotNull String language, @NotNull File file) {
        for (var entry : getObjects(language, file).entrySet()) {
            final Display<SenderT> display = loadDisplayOrNull(entry.getValue());
            if (display != null) {
                this.put(language, entry.getKey(), display);
            }
        }
    }

    public void unload() {
        clear();
    }

    public void reload() {
        reload(getLangFolder());
    }

    public void reload(@NotNull File langFolder) {
        clear();
        load(langFolder);
    }

    @Override
    public void clear() {
        super.clear();
        for (Path path : paths) {
            if (path instanceof Value) {
                ((Value<?>) path).clear();
            }
        }
    }

    protected abstract void saveFile(@NotNull File folder, @NotNull String name);

    public void setLangSupplier(@Nullable LangSupplier langSupplier) {
        this.langSupplier = langSupplier;
    }

    public void setUseSettings(boolean useSettings) {
        this.useSettings = useSettings;
    }

    public void setFileSuffix(@NotNull String fileSuffix) {
        this.fileSuffix = fileSuffix;
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
    private Map<String, Object> getObjects(@NotNull String language, @NotNull File file) {
        final Map<?, ?> objects;
        if (useSettings) {
            objects = SettingsData.of(file.getName()).load(file.getParentFile()).asLiteralObject();
        } else {
            objects = getFileObjects(file);
        }
        return DMap.of(objects).asDeepPath(".", (pathKey, value) -> {
            for (Path path : this.getPaths()) {
                if (path.getPath().equals(pathKey) || path.getAliases().contains(pathKey)) {
                    if (path instanceof Value) {
                        ((Value<?>) path).setValue(language, value);
                    }
                    return false;
                }
            }
            if (value instanceof Map) {
                return DMap.of((Map<?, ?>) value).getIgnoreCase("type") == null;
            }
            return true;
        });
    }

    @NotNull
    protected abstract Map<?, ?> getFileObjects(@NotNull File file);

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
        if (paths == null) {
            computePaths();
        }
        return paths;
    }

    @NotNull
    public String getFileSuffix() {
        return fileSuffix;
    }

    @NotNull
    @Override
    public List<DisplayLoader<SenderT>> getDisplayLoaders() {
        if (displayLoaders == null) {
            computeDisplayLoaders();
        }
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
    public @NotNull String getLanguageFor(@Nullable Object object) {
        if (langSupplier != null) {
            return langSupplier.getLanguageFor(object);
        }
        return super.getLanguageFor(object);
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
    public @NotNull String getEffectiveLanguage(@NotNull Object language) {
        if (langSupplier != null) {
            return langSupplier.getEffectiveLanguage(language);
        }
        return super.getEffectiveLanguage(language);
    }

    @Override
    public int getLogLevel() {
        if (langSupplier != null) {
            return langSupplier.getLogLevel();
        }
        return super.getLogLevel();
    }

    private void computePaths() {
        final List<Path> paths = new ArrayList<>();
        computeFields(getLangProviders(), Path.class, (name, path) -> {
            path.setHolder(this);
            paths.add(path);
        });
        this.paths = paths.toArray(new Path[0]);
    }

    @SuppressWarnings("unchecked")
    private void computeDisplayLoaders() {
        displayLoaders = new ArrayList<>();
        final Set<String> loaded = new HashSet<>();
        computeFields(getLangProviders(), DisplayLoader.class, (name, loader) -> {
            if (loaded.contains(name)) {
                return;
            }
            displayLoaders.add(loader);
            loaded.add(name);
            Displays.register(name.toLowerCase(), loader);
        });
    }

    @SuppressWarnings("unchecked")
    private <T> void computeFields(@NotNull Class<?>[] classes, @NotNull Class<T> classType, @NotNull BiConsumer<String, T> consumer) {
        for (Class<?> provided : classes) {
            if (provided == null) continue;
            // Check every superclass
            for (Class<?> clazz = provided; clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
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
