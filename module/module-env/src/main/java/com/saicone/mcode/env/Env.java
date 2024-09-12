package com.saicone.mcode.env;

import com.saicone.mcode.util.concurrent.DelayedExecutor;
import com.saicone.mcode.util.jar.JarRuntime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public class Env {

    private static JarRuntime RUNTIME;
    private static DelayedExecutor EXECUTOR;
    private static Registrar REGISTRAR;
    private static final Map<String, Object> CONDITIONS = new HashMap<>();
    private static final List<Executable> EXECUTABLES = new ArrayList<>();
    private static final Map<Object, Object> INSTANCES = new HashMap<>();

    Env() {
    }

    public static void init(@NotNull Class<?> clazz) {
        if (RUNTIME != null) {
            throw new IllegalStateException("The plugin environment is already initialized");
        }
        try {
            RUNTIME = JarRuntime.of(clazz);
        } catch (IOException e) {
            throw new RuntimeException("Cannot initialize JarRuntime", e);
        }
        condition("java.version", Runtime.version().feature());
        reload();
    }

    public static void reload() {
        RUNTIME.reload();
        for (Class<?> type : RUNTIME.annotated(Awake.class)) {
            if (type.isAnnotationPresent(Awake.class)) {
                load(type.getAnnotation(Awake.class), runnable(type));
            }
            for (Constructor<?> constructor : type.getDeclaredConstructors()) {
                if (constructor.isAnnotationPresent(Awake.class)) {
                    load(constructor.getAnnotation(Awake.class), runnable(type, constructor));
                }
            }
            for (Field field : type.getDeclaredFields()) {
                if (field.isAnnotationPresent(Awake.class)) {
                    load(field.getAnnotation(Awake.class), runnable(type, field));
                }
            }
            for (Method method : type.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Awake.class)) {
                    load(method.getAnnotation(Awake.class), runnable(type, method));
                }
            }
        }
        EXECUTABLES.sort(Comparator.comparingInt(Executable::priority));
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
        for (Executable executable : EXECUTABLES) {
            if (executable.shouldRun(executes, previous)) {
                executable.run();
            }
        }
    }

    @NotNull
    private static Runnable runnable(@NotNull Class<?> type) {
        return runnable(instanceSupplier(type));
    }

    @NotNull
    private static Runnable runnable(@NotNull Class<?> type, @NotNull Field field) {
        field.setAccessible(true);
        final Supplier<Object> supplier;
        if (Modifier.isStatic(field.getModifiers())) {
            supplier = () -> null;
        } else {
            supplier = instanceSupplier(type);
        }
        return runnable(() -> {
            Object instance = INSTANCES.get(field);
            if (instance == null) {
                try {
                    instance = field.get(supplier.get());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                if (instance != null) {
                    INSTANCES.put(field, instance);
                    if (REGISTRAR != null) {
                        REGISTRAR.register(instance);
                    }
                }
            }
            return instance;
        });
    }

    @NotNull
    private static Runnable runnable(@NotNull Class<?> type, @NotNull Method method) {
        if (method.getParameterCount() > 0) {
            throw new IllegalArgumentException("Cannot create a runnable using a method with multiple parameters: " + type.getName() + "#" + method.getName());
        }
        method.setAccessible(true);
        final Supplier<Object> supplier;
        if (Modifier.isStatic(method.getModifiers())) {
            supplier = () -> null;
        } else {
            supplier = instanceSupplier(type);
        }
        return runnable(() -> {
            try {
                return method.invoke(supplier.get());
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @NotNull
    private static Runnable runnable(@NotNull Class<?> type, @NotNull Constructor<?> constructor) {
        if (constructor.getParameterCount() > 0) {
            throw new IllegalArgumentException("Cannot create a runnable using a constructor with multiple parameters: " + type.getName());
        }
        constructor.setAccessible(true);
        return runnable(() -> {
            Object instance = INSTANCES.get(constructor);
            if (instance == null) {
                try {
                    instance = constructor.newInstance();
                } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    throw new RuntimeException(e);
                }
                INSTANCES.put(constructor, instance);
                if (REGISTRAR != null) {
                    REGISTRAR.register(instance);
                }
            }
            return instance;
        });
    }

    @NotNull
    private static Runnable runnable(@NotNull Supplier<Object> supplier) {
        return () -> {
            final Object instance = supplier.get();
            if (instance instanceof Runnable) {
                ((Runnable) instance).run();
            } else if (instance instanceof CompletableFuture) {
                ((CompletableFuture<?>) instance).join();
            }
        };
    }

    @NotNull
    private static Supplier<Object> instanceSupplier(@NotNull Class<?> type) {
        return () -> {
            Object instance = INSTANCES.get(type);
            if (instance == null) {
                for (Field field : type.getDeclaredFields()) {
                    if (field.getType() == type && Modifier.isStatic(field.getModifiers())) {
                        if (instance != null && !field.getName().equals("INSTANCE")) {
                            continue;
                        }
                        try {
                            field.setAccessible(true);
                            instance = field.get(null);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                if (instance == null) {
                    for (Constructor<?> constructor : type.getDeclaredConstructors()) {
                        if (constructor.getParameterCount() < 1) {
                            try {
                                constructor.setAccessible(true);
                                instance = constructor.newInstance();
                            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
                if (instance != null) {
                    INSTANCES.put(type, instance);
                    if (REGISTRAR != null) {
                        REGISTRAR.register(instance);
                    }
                }
            }
            return instance;
        };
    }

    @Nullable
    private static Supplier<Boolean> condition(@NotNull Awake awake) {
        if (awake.condition().length < 1 && awake.dependsOn().length < 1) {
            return null;
        }
        final Map<String, Function<Object, Boolean>> conditions = new HashMap<>();
        // Optimize this check
        for (String condition : awake.condition()) {
            final String key;
            final Function<Object, Boolean> value;

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
            conditions.put(key.trim(), value);
        }
        return () -> {
            for (Map.Entry<String, Function<Object, Boolean>> entry : conditions.entrySet()) {
                if (!entry.getValue().apply(CONDITIONS.get(entry.getKey()))) {
                    return false;
                }
            }
            if (REGISTRAR != null) {
                for (String dependency : awake.dependsOn()) {
                    if (!REGISTRAR.isPresent(dependency)) {
                        return false;
                    }
                }
            }
            return true;
        };
    }

    private static void load(@NotNull Awake awake, @NotNull Runnable runnable) {
        final Executable executable;
        if (awake.period() > 0) {
            executable = new PeriodicExecutable(runnable, awake.when(), awake.priority(), condition(awake), awake.delay(), awake.period(), awake.unit());
        } else if (awake.delay() > 0) {
            executable = new DelayedExecutable(runnable, awake.when(), awake.priority(), condition(awake), awake.delay(), awake.unit());
        } else {
            executable = new Executable(runnable, awake.when(), awake.priority(), condition(awake));
        }
        EXECUTABLES.add(executable);
    }

    // We are not in Java 14, so this record will be look like this
    private static class Executable {

        private final Runnable runnable;
        private final Executes when;
        private final int priority;
        private final Supplier<Boolean> condition;

        public Executable(@NotNull Runnable runnable, @NotNull Executes when, int priority, @Nullable Supplier<Boolean> condition) {
            this.runnable = runnable;
            this.when = when;
            this.priority = priority;
            this.condition = condition;
        }

        public boolean shouldRun(@NotNull Executes executes, boolean previous) {
            return when() == executes && (previous ? priority < 0 : priority > 0) && (condition == null || condition.get());
        }

        public void run() {
            runnable().run();
        }

        @NotNull
        public Runnable runnable() {
            return runnable;
        }

        @NotNull
        public Executes when() {
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

        public DelayedExecutable(@NotNull Runnable runnable, @NotNull Executes when, int priority, @Nullable Supplier<Boolean> condition, long delay, @NotNull TimeUnit unit) {
            super(runnable, when, priority, condition);
            this.delay = delay;
            this.unit = unit;
        }

        @Override
        public void run() {
            if (EXECUTOR == null || when() == Executes.DISABLE) {
                throw new IllegalStateException("Cannot run delayed awake " + (priority() < 0 ? "before" : "on") + " " + when().name());
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

        public PeriodicExecutable(@NotNull Runnable runnable, @NotNull Executes when, int priority, @Nullable Supplier<Boolean> condition, long delay, long period, @NotNull TimeUnit unit) {
            super(runnable, when, priority, condition, delay, unit);
            this.period = period;
        }

        @Override
        public void run() {
            if (EXECUTOR == null || when() == Executes.DISABLE) {
                throw new IllegalStateException("Cannot run periodic awake " + (priority() < 0 ? "before" : "on") + " " + when().name());
            }
            EXECUTOR.execute(runnable(), delay(), period(), unit());
        }

        public long period() {
            return period;
        }
    }
}
