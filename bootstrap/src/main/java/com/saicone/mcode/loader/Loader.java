package com.saicone.mcode.loader;

import com.saicone.ezlib.Ezlib;
import com.saicone.ezlib.EzlibLoader;
import com.saicone.mcode.bootstrap.Addon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

public interface Loader {

    String JAR_NAME = loadJarName();
    EzlibLoader LIBRARY_LOADER = loadLibraryLoader();

    private static String loadJarName() {
        try {
            return Paths.get(Loader.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                    .getFileName()
                    .toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static EzlibLoader loadLibraryLoader() {
        final Ezlib ezlib = new Ezlib();
        ezlib.init();

        final boolean versionMatches = "${gson_version}".equals(new String(new char[] {'$','{','g','s','o','n','_','v','e','r','s','i','o','n','}'}));
        final String pkg = new String(new char[] {'c','o','m','.','g','o','o','g','l','e','.','g','s','o','n'});
        final boolean packageMatches = "com.google.gson".equals(pkg);
        if (!versionMatches || !packageMatches) {
            final var gson = ezlib.dependency("com.google.code.gson:gson:" + (versionMatches ? "2.10.1" : "${gson_version}"));
            if (!packageMatches) {
                gson.relocations(Map.of(pkg, "com.google.gson"));
            }
            gson.parent(true);
            gson.load();
        }

        final EzlibLoader libraryLoader = new EzlibLoader(Loader.class.getClassLoader(), null, ezlib).logger((level, msg) -> {
            if (level <= 3) {
                final String prefix = level == 1 ? "\u001B[31m" : level == 2 ? "\u001B[33m" : "";
                System.out.println(prefix + "[" + JAR_NAME + "] " + msg);
            }
        }).xmlParser(new EzlibLoader.XmlParser());
        libraryLoader.applyAnnotationsDependency();
        libraryLoader.loadRelocations(Addon.RELOCATIONS);
        libraryLoader.applyDependency(Addon.COMMON.dependency());
        libraryLoader.applyDependency(Addon.MODULE_ENV.dependency());
        return libraryLoader;
    }

    @NotNull
    default EzlibLoader getLibraryLoader() {
        return LIBRARY_LOADER;
    }

    default void init(@NotNull String name) {
        try {
            Class.forName(name);
        } catch (Throwable ignored) { }
    }

    @NotNull
    @SuppressWarnings("unchecked")
    default <T> T build(@NotNull String name, @NotNull Object... params) {
        try {
            final Class<?> clazz = Class.forName(name);
            if (params.length == 0) {
                return (T) clazz.getDeclaredConstructor().newInstance();
            }
            for (Constructor<?> c : clazz.getDeclaredConstructors()) {
                if (matches(c, params)) {
                    return (T) c.newInstance(params);
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException("Cannot create instance of " + name, t);
        }
        throw new RuntimeException("Cannot find instance of " + name + " with params " + Arrays.toString(params));
    }

    @Nullable
    @SuppressWarnings("unchecked")
    default <T> T run(@NotNull String name, @NotNull String method, @NotNull Object... params) {
        try {
            final Class<?> clazz = Class.forName(name);
            if (params.length == 0) {
                return (T) clazz.getDeclaredMethod(method).invoke(null);
            }
            for (Method m : clazz.getDeclaredMethods()) {
                if (matches(m, params)) {
                    return (T) m.invoke(null, params);
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException("Cannot create instance of " + name, t);
        }
        throw new RuntimeException("Cannot find method ofrom " + name + " with name '" + method + "' and params " + Arrays.toString(params));
    }

    private boolean matches(@NotNull Executable executable, @NotNull Object... params) {
        if (executable.getParameterCount() == params.length) {
            int i = 0;
            for (Parameter parameter : executable.getParameters()) {
                if (!params[i].getClass().isAssignableFrom(parameter.getType())) {
                    return false;
                }
                i++;
            }
            return true;
        }
        return false;
    }
}
