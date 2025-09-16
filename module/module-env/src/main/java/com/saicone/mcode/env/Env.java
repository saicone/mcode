package com.saicone.mcode.env;

import com.saicone.mcode.env.asm.AnnotationConsumer;
import com.saicone.mcode.util.concurrent.DelayedExecutor;
import com.saicone.mcode.util.jar.JarRuntime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Env {

    private static JarRuntime RUNTIME;
    private static DelayedExecutor EXECUTOR;
    private static Registrar REGISTRAR;
    private static final Map<String, Object> CONDITIONS = new HashMap<>();
    private static final Map<String, Executable> EXECUTABLES = new HashMap<>();
    private static final Map<Object, Object> INSTANCES = new HashMap<>();

    Env() {
    }

    public static void init(@NotNull Class<?> clazz) {
        if (RUNTIME != null) {
            throw new IllegalStateException("The plugin environment is already initialized");
        }
        RUNTIME = new JarRuntime(clazz.getClassLoader()) {
            @Override
            public @NotNull JarRuntime reload() {
                final JarRuntime result = super.reload();
                Env.annotated(Awake.class, (name, awake) -> {
                    if (EXECUTABLES.containsKey(name)) {
                        return;
                    }
                    EXECUTABLES.put(name, load(awake, runnable(name)));
                });
                return result;
            }
        };
        try {
            RUNTIME.append(clazz.getProtectionDomain().getCodeSource().getLocation());
        } catch (IOException e) {
            throw new RuntimeException("Cannot initialize JarRuntime", e);
        }
        condition("java.version", Runtime.version().feature());
        RUNTIME.reload();
    }

    @NotNull
    public static JarRuntime runtime() {
        return RUNTIME;
    }

    @NotNull
    public static DelayedExecutor executor() {
        return EXECUTOR;
    }

    public static void executor(@NotNull DelayedExecutor executor) {
        EXECUTOR = executor;
    }

    @NotNull
    public static Registrar registrar() {
        return REGISTRAR;
    }

    public static void registrar(@NotNull Registrar registrar) {
        REGISTRAR = registrar;
    }

    @Nullable
    public static Object condition(@NotNull String key) {
        return CONDITIONS.get(key);
    }

    @Nullable
    public static Object condition(@NotNull String key, @Nullable Object value) {
        if (value == null) {
            return CONDITIONS.remove(key);
        } else {
            return CONDITIONS.put(key, value);
        }
    }

    public static void execute(@NotNull Executes executes, boolean previous) {
        Comparator<Map.Entry<String, Executable>> comparator = Comparator.comparingInt(entry -> entry.getValue().priority());
        if (executes == Executes.DISABLE) {
            comparator = comparator.reversed();
        }
        EXECUTABLES.entrySet().stream()
                .filter((entry) -> entry.getValue().shouldRun(executes, previous))
                .sorted(comparator)
                .forEachOrdered(entry -> entry.getValue().run(executes));
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> instanceOf(@NotNull Class<T> type) {
        // equals
        for (Map.Entry<Object, Object> entry : INSTANCES.entrySet()) {
            if (entry.getValue().getClass().equals(type)) {
                return Optional.of((T) entry.getValue());
            }
        }
        // assignable
        for (Map.Entry<Object, Object> entry : INSTANCES.entrySet()) {
            if (type.isAssignableFrom(entry.getValue().getClass())) {
                return Optional.of((T) entry.getValue());
            }
        }
        return Optional.empty();
    }

    public static void annotated(@NotNull Class<? extends Annotation> annotation, @NotNull BiConsumer<String, Map<String, Object>> consumer) {
        annotated(annotation.getName(), consumer);
    }

    public static void annotated(@NotNull String annotation, @NotNull BiConsumer<String, Map<String, Object>> consumer) {
        final Predicate<String> predicate;
        if (annotation.charAt(0) == 'L' && annotation.charAt(annotation.length() - 1) == ';') {
            predicate = annotation::equals;
        } else {
            final String descriptor = "L" + annotation.replace('.', '/') + ";";
            predicate = descriptor::equals;
        }
        for (Map.Entry<String, Class<?>> entry : RUNTIME.entrySet()) {
            try (InputStream input = RUNTIME.getLoader().getResource(entry.getKey().replace('.', '/') + ".class").openStream()) {
                final ClassReader reader = new ClassReader(input);
                final AnnotationConsumer annotationConsumer = new AnnotationConsumer(predicate, (name, map) -> {
                    if (name == null) {
                        consumer.accept(entry.getKey(), map);
                    } else {
                        consumer.accept(entry.getKey() + name, map);
                    }
                });
                reader.accept(annotationConsumer, 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @NotNull
    private static Runnable runnable(@NotNull String name) {
        final String className;
        final String fieldName;
        final String methodName;

        final int index = name.indexOf('#');
        if (index > 0) {
            className = name.substring(0, index);
            if (name.endsWith("()")) {
                fieldName = null;
                methodName = name.substring(index + 1, name.length() - 2);
            } else {
                fieldName = name.substring(index + 1);
                methodName = null;
            }
        } else {
            className = name;
            fieldName = null;
            methodName = null;
        }

        return () -> {
            final Class<?> type = RUNTIME.get(className);
            if (type == null || type == Object.class) {
                throw new IllegalStateException("The class " + className + " is not available to execute an awake on it");
            }

            final Object result;
            try {
                if (fieldName != null) {
                    final Field field = type.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    final Object instance;
                    if (Modifier.isStatic(field.getModifiers())) {
                        instance = null;
                    } else {
                        instance = instanceFor(type);
                    }
                    result = instanceFor(instance, field);
                } else if (methodName != null) {
                    final Method method = type.getDeclaredMethod(methodName);
                    method.setAccessible(true);
                    final Object instance;
                    if (Modifier.isStatic(method.getModifiers())) {
                        instance = null;
                    } else {
                        instance = instanceFor(type);
                    }
                    result = method.invoke(instance);
                } else {
                    result = instanceFor(type);
                }
            } catch (Throwable t) {
                throw new RuntimeException("Cannot invoke " + name, t);
            }

            if (result instanceof Runnable) {
                ((Runnable) result).run();
            } else if (result instanceof CompletableFuture) {
                ((CompletableFuture<?>) result).join();
            }
        };
    }

    @NotNull
    private static Object instanceFor(@NotNull Class<?> type) throws Throwable {
        Object instance = INSTANCES.get(type);
        if (instance == null) {
            for (Field field : type.getDeclaredFields()) {
                if (field.getType() == type && Modifier.isStatic(field.getModifiers())) {
                    if (instance != null && !field.getName().equals("INSTANCE")) {
                        continue;
                    }
                    field.setAccessible(true);
                    instance = field.get(null);
                }
            }
            if (instance == null) {
                for (Constructor<?> constructor : type.getDeclaredConstructors()) {
                    if (constructor.getParameterCount() < 1) {
                        constructor.setAccessible(true);
                        instance = constructor.newInstance();
                    }
                }
                // TODO: Create instance based on cached instances
            }
            if (instance != null) {
                INSTANCES.put(type, instance);
                if (REGISTRAR != null) {
                    REGISTRAR.register(instance);
                }
            } else {
                throw new IllegalArgumentException("The type " + type + " doesn't have an INSTANCE field or valid constructor");
            }
        }
        return instance;
    }

    @NotNull
    private static Object instanceFor(@Nullable Object obj, @NotNull Field field) throws Throwable {
        Object instance = INSTANCES.get(field);
        if (instance == null) {
            instance = field.get(obj);
            if (instance != null) {
                INSTANCES.put(field, instance);
                if (REGISTRAR != null) {
                    REGISTRAR.register(instance);
                }
            } else {
                throw new IllegalStateException("The current type of field '" + field.getName() + "' is null");
            }
        }
        return instance;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    private static Executable load(@NotNull Map<String, Object> awake, @NotNull Runnable runnable) {
        final Set<Executes> when = new HashSet<>();
        for (String name : (List<String>) awake.getOrDefault("when", List.<String>of())) {
            when.add(Executes.valueOf(name));
        }
        final int priority = ((Number) awake.getOrDefault("priority", 0)).intValue();
        final long delay = ((Number) awake.getOrDefault("delay", 0)).longValue();
        final long period = ((Number) awake.getOrDefault("period", 0)).longValue();
        final TimeUnit unit = TimeUnit.valueOf((String) awake.getOrDefault("unit", "SECONDS"));

        final Executable executable;
        if (period > 0) {
            executable = new PeriodicExecutable(runnable, when, priority, condition(awake), delay, period, unit);
        } else if (delay > 0) {
            executable = new DelayedExecutable(runnable, when, priority, condition(awake), delay, unit);
        } else {
            executable = new Executable(runnable, when, priority, condition(awake));
        }
        return executable;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static Supplier<Boolean> condition(@NotNull Map<String, Object> awake) {
        final List<String> conditions = (List<String>) awake.getOrDefault("condition", List.<String>of());
        final List<String> dependsOn = (List<String>) awake.getOrDefault("dependsOn", List.<String>of());
        if (conditions.isEmpty() && dependsOn.isEmpty()) {
            return null;
        }
        final Map<String, Predicate<Object>> predicates = new HashMap<>();
        // Optimize this check
        for (String condition : conditions) {
            final String key;
            final Predicate<Object> value;

            int index = condition.indexOf('=');
            if (index > 0) {
                if (condition.charAt(index - 1) == '>') {
                    key = condition.substring(0, index - 1);
                    final int beginIndex = index + 1;
                    value = object -> object instanceof Number && ((Number) object).doubleValue() >= Double.parseDouble(condition.substring(beginIndex));
                } else if (condition.charAt(index - 1) == '<') {
                    key = condition.substring(0, index - 1);
                    final int beginIndex = index + 1;
                    value = object -> object instanceof Number && ((Number) object).doubleValue() <= Double.parseDouble(condition.substring(beginIndex));
                } else {
                    key = condition.substring(0, index);
                    final int beginIndex = condition.charAt(index + 1) == '=' ? index + 2 : index + 1;
                    value = object -> String.valueOf(object).equals(condition.substring(beginIndex));
                }
            } else if ((index = condition.indexOf('>')) > 0) {
                key = condition.substring(0, index);
                final int beginIndex = index + 1;
                value = object -> object instanceof Number && ((Number) object).doubleValue() > Double.parseDouble(condition.substring(beginIndex));
            } else if ((index = condition.indexOf('<')) > 0) {
                key = condition.substring(0, index);
                final int beginIndex = index + 1;
                value = object -> object instanceof Number && ((Number) object).doubleValue() < Double.parseDouble(condition.substring(beginIndex));
            } else {
                key = condition;
                value = Boolean.TRUE::equals;
            }
            predicates.put(key.trim(), value);
        }
        return () -> {
            for (Map.Entry<String, Predicate<Object>> entry : predicates.entrySet()) {
                if (!entry.getValue().test(CONDITIONS.get(entry.getKey()))) {
                    return false;
                }
            }
            if (REGISTRAR != null) {
                for (String dependency : dependsOn) {
                    if (!REGISTRAR.isPresent(dependency)) {
                        return false;
                    }
                }
            }
            return true;
        };
    }

    // We are not in Java 14, so this record will be look like this
    private static class Executable {

        private final Runnable runnable;
        private final Set<Executes> when;
        private final int priority;
        private final Supplier<Boolean> condition;

        public Executable(@NotNull Runnable runnable, @NotNull Set<Executes> when, int priority, @Nullable Supplier<Boolean> condition) {
            this.runnable = runnable;
            this.when = when;
            this.priority = priority;
            this.condition = condition;
        }

        public boolean shouldRun(@NotNull Executes executes, boolean previous) {
            return when().contains(executes) && previous == (priority < 0) && (condition == null || condition.get());
        }

        public void run(@NotNull Executes executes) {
            runnable().run();
        }

        @NotNull
        public Runnable runnable() {
            return runnable;
        }

        public @NotNull Set<Executes> when() {
            return when;
        }

        public int priority() {
            return priority;
        }

        @Nullable
        public Supplier<Boolean> condition() {
            return condition;
        }
    }

    private static class DelayedExecutable extends Executable {

        private final long delay;
        private final TimeUnit unit;

        public DelayedExecutable(@NotNull Runnable runnable, @NotNull Set<Executes> when, int priority, @Nullable Supplier<Boolean> condition, long delay, @NotNull TimeUnit unit) {
            super(runnable, when, priority, condition);
            this.delay = delay;
            this.unit = unit;
        }

        @Override
        public void run(@NotNull Executes executes) {
            if (EXECUTOR == null || executes == Executes.DISABLE) {
                throw new IllegalStateException("Cannot run delayed awake " + (priority() < 0 ? "before" : "on") + " " + executes.name());
            }
            EXECUTOR.execute(runnable(), delay(), unit());
        }

        public long delay() {
            return delay;
        }

        public TimeUnit unit() {
            return unit;
        }
    }

    private static class PeriodicExecutable extends DelayedExecutable {

        private final long period;

        public PeriodicExecutable(@NotNull Runnable runnable, @NotNull Set<Executes> when, int priority, @Nullable Supplier<Boolean> condition, long delay, long period, @NotNull TimeUnit unit) {
            super(runnable, when, priority, condition, delay, unit);
            this.period = period;
        }

        @Override
        public void run(@NotNull Executes executes) {
            if (EXECUTOR == null || executes == Executes.DISABLE) {
                throw new IllegalStateException("Cannot run periodic awake " + (priority() < 0 ? "before" : "on") + " " + executes.name());
            }
            EXECUTOR.execute(runnable(), delay(), period(), unit());
        }

        public long period() {
            return period;
        }
    }
}
