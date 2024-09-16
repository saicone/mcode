package com.saicone.mcode.bootstrap;

import com.saicone.mcode.Plugin;
import com.saicone.mcode.loader.Loader;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.function.Supplier;

public interface Bootstrap extends Loader {

    @NotNull
    Path folder();

    void log(int level, @NotNull Supplier<String> msg);

    void logException(int level, @NotNull Throwable throwable);

    void logException(int level, @NotNull Throwable throwable, @NotNull Supplier<String> msg);

    @NotNull
    @SuppressWarnings("unchecked")
    default Plugin loadPlugin(@NotNull String pluginClass) {
        try {
            final Field field = Plugin.class.getDeclaredField("BOOTSTRAP");
            field.setAccessible(true);
            field.set(null, this);
        } catch (Throwable t) {
            logException(1, t, () -> "Cannot set BOOTSTRAP instance field");
        }
        try {
            final Class<? extends Plugin> clazz = (Class<? extends Plugin>) Class.forName(pluginClass);
            Plugin found = null;
            for (Field field : clazz.getDeclaredFields()) {
                final int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers) && field.getType().isAssignableFrom(clazz)) {
                    field.setAccessible(true);
                    found = (Plugin) field.get(null);
                }
            }
            if (found == null) {
                found = clazz.getDeclaredConstructor().newInstance();
            }
            return found;
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new RuntimeException("Cannot found Plugin class", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot get Plugin instance from static field", e);
        } catch (InvocationTargetException | InstantiationException | NoSuchMethodException e) {
            throw new RuntimeException("Cannot initialize Plugin instance", e);
        }
    }
}
