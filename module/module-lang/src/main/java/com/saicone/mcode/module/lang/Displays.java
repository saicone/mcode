package com.saicone.mcode.module.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class Displays {

    private static final Map<String, DisplayLoader<Object>> LOADER_TYPES = new HashMap<>();
    private static final DisplaySupplier<Object> DISPLAY_HOLDER = LOADER_TYPES::values;

    Displays() {
    }

    @NotNull
    public static <T> DisplayLoader<T> loader(@NotNull String name) {
        return (DisplayLoader<T>) DISPLAY_HOLDER.getDisplayLoader(name);
    }

    @Nullable
    public static <T> DisplayLoader<T> loaderOrNull(@NotNull String name) {
        return (DisplayLoader<T>) DISPLAY_HOLDER.getDisplayLoaderOrNull(name);
    }

    @UnmodifiableView
    @NotNull
    public static List<DisplayLoader<Object>> loaders() {
        return List.copyOf(LOADER_TYPES.values());
    }

    @NotNull
    public static <T> Display<T> load(@Nullable Object object) {
        return (Display<T>) DISPLAY_HOLDER.loadDisplay(object);
    }

    @NotNull
    public static <T> Display<T> load(@NotNull String name, @Nullable Object object) {
        return (Display<T>) DISPLAY_HOLDER.loadDisplay(name, object);
    }

    @Nullable
    public static <T> Display<T> loadOrNull(@Nullable Object object) {
        return (Display<T>) DISPLAY_HOLDER.loadDisplayOrNull(object);
    }

    @Nullable
    public static <T> Display<T> loadOrNull(@NotNull String name, @Nullable Object object) {
        return (Display<T>) DISPLAY_HOLDER.loadDisplayOrNull(name, object);
    }

    @Nullable
    public static <T> Display<T> loadType(@Nullable Object object, @NotNull String defaultType) {
        return (Display<T>) DISPLAY_HOLDER.loadDisplayType(object, defaultType);
    }

    @Nullable
    public static DisplayLoader<?> register(@NotNull String name, @NotNull DisplayLoader<?> loader) {
        return LOADER_TYPES.put(name.toLowerCase(), (DisplayLoader<Object>) loader);
    }

    @Nullable
    public static DisplayLoader<?> unregister(@NotNull String name) {
        return LOADER_TYPES.remove(name.toLowerCase());
    }
}
