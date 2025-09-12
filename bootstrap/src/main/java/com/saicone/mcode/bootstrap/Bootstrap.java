package com.saicone.mcode.bootstrap;

import com.saicone.ezlib.Dependencies;
import com.saicone.ezlib.Dependency;
import com.saicone.ezlib.EzlibLoader;
import com.saicone.ezlib.Repository;
import com.saicone.mcode.Plugin;
import com.saicone.mcode.env.Env;
import com.saicone.mcode.loader.Loader;
import com.saicone.mcode.platform.MC;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.function.Supplier;

public interface Bootstrap extends Loader {

    @NotNull
    Path folder();

    @NotNull
    Object logger();

    void log(int level, @NotNull Supplier<String> msg);

    void logException(int level, @NotNull Throwable throwable);

    void logException(int level, @NotNull Throwable throwable, @NotNull Supplier<String> msg);

    default void loadDependencies() {
        // Conditions
        getLibraryLoader().condition("server.plugin", EzlibLoader.Condition.valueOf(name -> Env.registrar().isPresent(name)));
        getLibraryLoader().condition("server.version", new EzlibLoader.Condition<>(MC::fromString, version -> MC.compare(version, MC.version())));

        // Annotated
        Env.annotated(Repository.class, (name, repository) -> getLibraryLoader().loadRepository(EzlibLoader.Repository.valueOf(repository)));
        Env.annotated(Dependency.class, (name, dependency) -> getLibraryLoader().loadDependency(EzlibLoader.Dependency.valueOf(dependency)));
        Env.annotated(Dependencies.class, (name, dependencies) -> EzlibLoader.Dependencies.valueOf(dependencies).load(getLibraryLoader()));

        // Loader
        getLibraryLoader().load();
    }

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
