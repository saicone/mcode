package com.saicone.mcode.module.lang;

import com.saicone.mcode.Platform;
import com.saicone.settings.Settings;
import com.saicone.settings.SettingsData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class AbstractLang<SenderT> extends DisplayHolder<SenderT> implements DisplaySupplier<SenderT> {

    private static final boolean USE_SETTINGS = Platform.isAvailable("Settings");

    private final Class<?>[] langProviders;

    private Path[] paths = new Path[0];
    private final List<DisplayLoader<SenderT>> displayLoaders = new ArrayList<>();

    protected String filePrefix = ".yml";

    public AbstractLang(@NotNull Class<?>... langProviders) {
        final int length = langProviders.length + 1;
        this.langProviders = Arrays.copyOf(langProviders, length);
        this.langProviders[length - 1] = getClass();
    }

    public void load() {
        load(getLangFolder());
    }

    public void load(@NotNull File langFolder) {
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }
        computePaths();
        computeDisplayLoaders();
        final Map<String, List<File>> langFiles = getLangFiles(langFolder);
        for (String defaultLanguage : getLanguageTypes()) {
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
        for (var entry : getObjects(file).entrySet()) {
            final Display<SenderT> display = loadDisplayOrNull(entry.getValue());
            if (display != null) {
                if (!displays.containsKey(name)) {
                    displays.put(name, new HashMap<>());
                }
                final Map<String, Display<SenderT>> map = displays.get(name);
                map.put(entry.getKey(), display);
            }
        }
    }

    private void computePaths() {
        if (paths.length > 0) {
            return;
        }
        final List<Path> paths = new ArrayList<>();
        getFieldsFrom(getLangProviders(), field -> Path.class.isAssignableFrom(field.getType()), field -> {
            try {
                final Path path = (Path) field.get(null);
                path.setHolder(this);
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
        final Set<String> loaded = new HashSet<>();
        getFieldsFrom(getLangProviders(), field -> DisplayLoader.class.isAssignableFrom(field.getType()), field -> {
            if (loaded.contains(field.getName().toLowerCase())) {
                return;
            }
            try {
                final DisplayLoader<SenderT> loader = (DisplayLoader<SenderT>) field.get(this);
                displayLoaders.add(loader);
                loaded.add(field.getName().toLowerCase());
            } catch (IllegalAccessException | ClassCastException e) {
                e.printStackTrace();
            }
        });
    }

    public void unload() {
        clear();
    }

    @Nullable
    protected abstract File saveDefaultLang(@NotNull File folder, @NotNull String name);

    public void setFilePrefix(@NotNull String filePrefix) {
        this.filePrefix = filePrefix;
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
        if (!USE_SETTINGS) {
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

    @NotNull
    protected Class<?>[] getLangProviders() {
        return langProviders;
    }

    @NotNull
    @Override
    public List<DisplayLoader<SenderT>> getDisplayLoaders() {
        return displayLoaders;
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
    protected String[] splitPath(@NotNull String path) {
        return path.split("\\.");
    }

    protected void getFieldsFrom(@NotNull Class<?>[] classes, @NotNull Predicate<Field> filter, @NotNull Consumer<Field> consumer) {
        for (Class<?> provided : classes) {
            // Check every superclass
            for (Class<?> clazz = provided; clazz != Object.class; clazz = clazz.getSuperclass()) {
                for (Field field : clazz.getDeclaredFields()) {
                    if (filter.test(field)) {
                        field.setAccessible(true);
                        consumer.accept(field);
                    }
                }
            }
        }
    }
}
