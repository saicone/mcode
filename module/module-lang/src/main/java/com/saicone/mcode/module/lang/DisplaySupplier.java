package com.saicone.mcode.module.lang;

import com.saicone.mcode.module.lang.display.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@FunctionalInterface
public interface DisplaySupplier<SenderT> {

    @NotNull
    Collection<DisplayLoader<SenderT>> getDisplayLoaders();

    @NotNull
    default DisplayLoader<SenderT> getDisplayLoader(@NotNull String name) {
        final DisplayLoader<SenderT> loader = getDisplayLoaderOrNull(name);
        return loader != null ? loader : DisplayLoader.empty();
    }

    @Nullable
    default DisplayLoader<SenderT> getDisplayLoaderOrNull(@NotNull String name) {
        for (DisplayLoader<SenderT> loader : getDisplayLoaders()) {
            if (loader.matches(name)) {
                return loader;
            }
        }
        return null;
    }

    @NotNull
    default String getDisplayType() {
        return Display.DEFAULT_TYPE;
    }

    @NotNull
    default ActionBarDisplay.Loader<SenderT> getActionBarLoader() {
        final ActionBarDisplay.Loader<SenderT> loader = (ActionBarDisplay.Loader<SenderT>) getDisplayLoaderOrNull("actionbar");
        return Objects.requireNonNull(loader, "The actionbar loader doesn't exist");
    }

    @NotNull
    default BossBarDisplay.Loader<SenderT> getBossBarLoader() {
        final BossBarDisplay.Loader<SenderT> loader = (BossBarDisplay.Loader<SenderT>) getDisplayLoaderOrNull("bossbar");
        return Objects.requireNonNull(loader, "The bossbar loader doesn't exist");
    }

    @NotNull
    default SoundDisplay.Loader<SenderT> getSoundLoader() {
        final SoundDisplay.Loader<SenderT> loader = (SoundDisplay.Loader<SenderT>) getDisplayLoaderOrNull("sound");
        return Objects.requireNonNull(loader, "The sound loader doesn't exist");
    }

    @NotNull
    default TextDisplay.Loader<SenderT> getTextLoader() {
        final TextDisplay.Loader<SenderT> loader = (TextDisplay.Loader<SenderT>) getDisplayLoaderOrNull("text");
        return Objects.requireNonNull(loader, "The text loader doesn't exist");
    }

    @NotNull
    default TitleDisplay.Loader<SenderT> getTitleLoader() {
        final TitleDisplay.Loader<SenderT> loader = (TitleDisplay.Loader<SenderT>) getDisplayLoaderOrNull("title");
        return Objects.requireNonNull(loader, "The title loader doesn't exist");
    }

    @NotNull
    default Display<SenderT> loadDisplay(@Nullable Object object) {
        final Display<SenderT> display = loadDisplayOrNull(object);
        return display != null ? display : Display.empty();
    }

    @NotNull
    default Display<SenderT> loadDisplay(@NotNull String name, @Nullable Object object) {
        final Display<SenderT> display = loadDisplayOrNull(name, object);
        return display != null ? display : Display.empty();
    }

    @Nullable
    default Display<SenderT> loadDisplayOrNull(@Nullable Object object) {
        return loadDisplayType(object, getDisplayType());
    }

    @Nullable
    default Display<SenderT> loadDisplayOrNull(@NotNull String name, @Nullable Object object) {
        final DisplayLoader<SenderT> loader = getDisplayLoaderOrNull(name);
        if (loader == null) {
            return null;
        }
        return loader.load(object);
    }

    @Nullable
    default Display<SenderT> loadDisplayType(@Nullable Object object, @NotNull String defaultType) {
        if (object == null) {
            return null;
        }

        if (object instanceof Iterable) {
            final List<Display<SenderT>> list = new ArrayList<>();
            for (Object o : (Iterable<?>) object) {
                final Display<SenderT> display = loadDisplayOrNull(o);
                if (display != null) {
                    list.add(display);
                }
            }
            if (list.isEmpty()) {
                return null;
            }
            return new DisplayList<>(list);
        }

        if (object instanceof Map) {
            for (var entry : ((Map<?, ?>) object).entrySet()) {
                if (String.valueOf(entry.getKey()).equalsIgnoreCase("type")) {
                    return loadDisplayOrNull(String.valueOf(entry.getValue()), object);
                }
            }
        }
        return loadDisplayOrNull(defaultType, object);
    }
}
