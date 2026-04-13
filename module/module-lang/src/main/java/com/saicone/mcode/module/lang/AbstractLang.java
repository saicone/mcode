package com.saicone.mcode.module.lang;

import com.saicone.mcode.util.DMap;
import com.saicone.mcode.util.MLocale;
import com.saicone.settings.SettingsData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class AbstractLang<SenderT> extends DisplayHolder<SenderT> implements DisplaySupplier<SenderT> {

    // Object parameters
    private LangSupplier langSupplier;
    private final Class<?>[] langProviders;

    // Instance fields parameters
    private List<Path> paths;
    private List<DisplayLoader<SenderT>> displayLoaders;

    // Mutable parameters
    private transient boolean useSettings;
    private transient String displayType = Display.DEFAULT_TYPE;
    private transient Map<Locale, DMap> objects = new HashMap<>();

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
        try {
            saveFiles(langFolder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Load objects
        this.objects = new HashMap<>();
        getLangFiles(langFolder).forEach((key, list) -> list.forEach(file -> loadDisplays(key, file)));
    }

    protected void loadDisplays(@NotNull Locale locale, @NotNull File file) {
        for (var entry : getObjects(locale, file).entrySet()) {
            final Display<SenderT> display = loadDisplayOrNull(entry.getValue());
            if (display != null) {
                this.put(locale, entry.getKey(), display);
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

    protected abstract void saveFiles(@NotNull File folder) throws IOException;

    public void setLangSupplier(@Nullable LangSupplier langSupplier) {
        this.langSupplier = langSupplier;
    }

    public void setUseSettings(boolean useSettings) {
        this.useSettings = useSettings;
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
    protected Map<Locale, List<File>> getLangFiles(@NotNull File langFolder) {
        final Map<Locale, List<File>> map = new HashMap<>();
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
                final Locale locale = MLocale.fromMinecraftLocale(name, null);
                if (locale != null) {
                    map.computeIfAbsent(locale, s -> new ArrayList<>()).add(file);
                }
            }
        }
        return map;
    }

    @NotNull
    public Map<Locale, DMap> getObjects() {
        return objects;
    }

    @NotNull
    private Map<String, Object> getObjects(@NotNull Locale locale, @NotNull File file) {
        final Map<String, Object> map;
        if (useSettings) {
            map = SettingsData.of(file.getName()).load(file.getParentFile()).asLiteralObject();
        } else {
            map = getFileObjects(file);
        }
        final DMap objects = DMap.of(map);
        if (this.objects.containsKey(locale)) {
            this.objects.get(locale).merge(objects);
        } else {
            this.objects.put(locale, objects);
        }
        return objects.asDeepPath(".", (pathKey, value) -> {
            for (Path path : this.getPaths()) {
                if (path.getPath().equals(pathKey) || path.getAliases().contains(pathKey)) {
                    return false;
                }
            }
            if (value instanceof Map) {
                return DMap.of((Map<?, ?>) value).getIgnoreCase(Display.TYPE_KEY) == null;
            }
            return true;
        });
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
    public List<Path> getPaths() {
        if (paths == null) {
            computePaths();
        }
        return paths;
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
    public @NotNull Locale getDefaultLocale() {
        if (langSupplier != null) {
            return langSupplier.getDefaultLocale();
        }
        return super.getDefaultLocale();
    }

    @Override
    public @NotNull Locale getHolderLocale(@Nullable Object holder) {
        if (langSupplier != null) {
            return langSupplier.getHolderLocale(holder);
        }
        return super.getHolderLocale(holder);
    }

    @Override
    public @NotNull Locale getEffectiveLocale(@Nullable Object object) {
        if (langSupplier != null) {
            return langSupplier.getEffectiveLocale(object);
        }
        return super.getEffectiveLocale(object);
    }

    @Override
    public @NotNull Set<Locale> getLocaleTypes() {
        if (langSupplier != null) {
            return langSupplier.getLocaleTypes();
        }
        return super.getLocaleTypes();
    }

    @Override
    public @NotNull Map<Locale, Locale> getLocaleAliases() {
        if (langSupplier != null) {
            return langSupplier.getLocaleAliases();
        }
        return super.getLocaleAliases();
    }

    @Override
    public int getLogLevel() {
        if (langSupplier != null) {
            return langSupplier.getLogLevel();
        }
        return super.getLogLevel();
    }

    @Nullable
    public Object getValue(@Nullable Object language, @NotNull String... path) {
        final DMap map = this.objects.get(getEffectiveLocale(language));
        if (map == null) {
            return null;
        }
        return map.getDeep(path);
    }

    @Override
    public @NotNull Path path(@NotNull String path, @NotNull String... aliases) {
        final Path result = super.path(path, aliases);
        getPaths().add(result);
        return result;
    }

    @NotNull
    public <T> Value<T> value(@NotNull String path, @NotNull String... aliases) {
        final Value<T> value = new Value<>(path, aliases);
        value.setHolder(this);
        getPaths().add(value);
        return value;
    }

    @NotNull
    public <T> Value<T> value(@NotNull Function<DMap, Object> compute) {
        return value((locale, map) -> compute.apply(map));
    }

    @NotNull
    public <T> Value<T> value(@NotNull BiFunction<Locale, DMap, Object> compute) {
        final Value<T> value = new Value<>("") {
            @Override
            public @Nullable Object compute(@NotNull Locale locale, @NotNull DMap map) {
                return compute.apply(locale, map);
            }
        };
        value.setHolder(this);
        getPaths().add(value);
        return value;
    }

    private void computePaths() {
        final List<Path> paths = new ArrayList<>();
        computeFields(getLangProviders(), Path.class, (name, path) -> {
            path.setHolder(this);
            paths.add(path);
        });
        this.paths = paths;
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
