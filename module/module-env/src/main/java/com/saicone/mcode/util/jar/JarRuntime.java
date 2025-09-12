package com.saicone.mcode.util.jar;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.jar.JarFile;

public class JarRuntime extends LinkedHashMap<String, Class<?>> {

    private final ClassLoader classLoader;

    @NotNull
    public static JarRuntime of(@NotNull Class<?> clazz) throws IOException {
        return of(clazz.getClassLoader(), clazz);
    }

    @NotNull
    public static JarRuntime of(@NotNull ClassLoader classLoader, @NotNull Class<?> clazz) throws IOException {
        return of(classLoader, clazz.getProtectionDomain().getCodeSource().getLocation());
    }

    @NotNull
    public static JarRuntime of(@NotNull URL url) throws IOException {
        return of(JarRuntime.class.getClassLoader(), url);
    }

    @NotNull
    public static JarRuntime of(@NotNull ClassLoader classLoader, @NotNull URL url) throws IOException {
        return new JarRuntime(classLoader).append(url);
    }

    @NotNull
    public static JarRuntime of(@NotNull JarFile file) {
        return of(JarRuntime.class.getClassLoader(), file);
    }

    @NotNull
    public static JarRuntime of(@NotNull ClassLoader classLoader, @NotNull JarFile file) {
        return new JarRuntime(classLoader).append(file);
    }

    public JarRuntime() {
        this(JarRuntime.class.getClassLoader());
    }

    public JarRuntime(@NotNull ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @NotNull
    public ClassLoader getLoader() {
        return classLoader;
    }

    @NotNull
    public <T> Iterable<Class<? extends T>> subClasses(@NotNull Class<T> type) {
        return iterableOf(type::isAssignableFrom, clazz -> clazz.asSubclass(type));
    }

    @NotNull
    public Iterable<Class<?>> annotated(@NotNull Class<? extends Annotation> annotationType) {
        return iterableOf(clazz -> {
            if (clazz.isAnnotation() || clazz.getSuperclass() == null) {
                return false;
            }
            return clazz.isAnnotationPresent(annotationType)
                    || isAnnotationPresent(clazz.getDeclaredConstructors(), annotationType)
                    || isAnnotationPresent(clazz.getDeclaredFields(), annotationType)
                    || isAnnotationPresent(clazz.getDeclaredMethods(), annotationType);
        });
    }

    private boolean isAnnotationPresent(@NotNull AnnotatedElement[] elements, @NotNull Class<? extends Annotation> annotationType) {
        for (@NotNull AnnotatedElement element : elements) {
            if (element.isAnnotationPresent(annotationType)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    public Iterable<Class<?>> iterableOf(@NotNull Predicate<Class<?>> predicate) {
        return iterableOf(predicate, clazz -> clazz);
    }

    @NotNull
    public <T> Iterable<T> iterableOf(@NotNull Predicate<Class<?>> predicate, @NotNull Function<Class<?>, T> function) {
        return new Iterable<>() {
            @Override
            public @NotNull Iterator<T> iterator() {
                return new Iterator<>() {
                    private final Iterator<Map.Entry<String, Class<?>>> iterator = JarRuntime.this.entrySet().iterator();
                    private T found;

                    @Override
                    public boolean hasNext() {
                        if (found != null) {
                            return true;
                        }
                        while (iterator.hasNext()) {
                            final Class<?> next = iterator.next().getValue();
                            if (predicate.test(next)) {
                                found = function.apply(next);
                                return true;
                            }
                        }
                        return false;
                    }

                    @Override
                    public T next() {
                        if (found == null) {
                            throw new NoSuchElementException();
                        }
                        final T result = found;
                        found = null;
                        return result;
                    }
                };
            }
        };
    }

    @NotNull
    @Contract("-> this")
    public JarRuntime reload() {
        for (String name : new HashSet<>(this.keySet())) {
            if (this.get(name) == Object.class) {
                put(name);
            }
        }
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public JarRuntime append(@NotNull URL url) throws IOException {
        File file;
        try {
            try {
                file = new File(url.toURI());
            } catch (IllegalArgumentException e) {
                file = new File(((JarURLConnection) url.openConnection()).getJarFileURL().toURI());
            }
        } catch (URISyntaxException e) {
            file = new File(url.getPath());
        }
        try (JarFile jarFile = new JarFile(file)) {
            return append(jarFile);
        }
    }

    @NotNull
    @Contract("_ -> this")
    public JarRuntime append(@NotNull JarFile file) {
        file.stream().filter(entry -> entry.getName().endsWith(".class")).forEach(entry -> {
            final String name = entry.getName();
            final String parsedName = name.replace('/', '.').substring(0, name.length() - 6);
            this.put(parsedName);
        });
        return this;
    }

    @Nullable
    public Class<?> put(@NotNull String name) {
        try {
            // Avoid initialization, it can fail with NotClassDefFoundError
            final Class<?> clazz = Class.forName(name, false, this.classLoader);
            return this.put(name, clazz);
        } catch (Throwable t) {
            return this.put(name, Object.class);
        }
    }
}
