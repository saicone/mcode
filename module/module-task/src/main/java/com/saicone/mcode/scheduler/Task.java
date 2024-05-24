package com.saicone.mcode.scheduler;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Task {

    private static Scheduler<Object> SCHEDULER = null;
    private static final Map<String, TaskTimer<Object>> TIMERS = new HashMap<>();

    Task() {
    }

    @NotNull
    public static Scheduler<Object> getScheduler() {
        return SCHEDULER;
    }

    public static void setScheduler(@NotNull Scheduler<Object> scheduler) {
        if (Task.SCHEDULER == null) {
            Task.SCHEDULER = scheduler;
        }
    }

    public static Object run(@NotNull Runnable runnable) {
        return SCHEDULER.run(runnable);
    }

    public static Object run(boolean async, @NotNull Runnable runnable) {
        if (async) {
            return runAsync(runnable);
        } else {
            return run(runnable);
        }
    }

    public static Object run(@NotNull Object provider, @NotNull Runnable runnable) {
        return runBy(provider, runnable);
    }

    public static Object run(boolean async, long delay, @NotNull TimeUnit unit, @NotNull Runnable runnable) {
        if (async) {
            return laterAsync(runnable, delay, unit);
        } else {
            return later(runnable, delay, unit);
        }
    }

    public static Object run(@NotNull Object provider, long delay, @NotNull TimeUnit unit, @NotNull Runnable runnable) {
        return laterBy(provider, runnable, delay, unit);
    }

    public static Object run(boolean async, long delay, long period, @NotNull TimeUnit unit, @NotNull Runnable runnable) {
        if (async) {
            return timerAsync(runnable, delay, period, unit);
        } else {
            return timer(runnable, delay, period, unit);
        }
    }

    public static Object run(@NotNull Object provider, long delay, long period, @NotNull TimeUnit unit, @NotNull Runnable runnable) {
        return timerBy(provider, runnable, delay, period, unit);
    }

    public static Object later(@NotNull Runnable runnable, long delay, @NotNull TimeUnit unit) {
        return SCHEDULER.later(runnable, delay, unit);
    }

    public static Object timer(@NotNull Runnable runnable, long delay, long period, @NotNull TimeUnit unit) {
        return SCHEDULER.timer(runnable, delay, period, unit);
    }

    public static Object runAsync(@NotNull Runnable runnable) {
        return SCHEDULER.runAsync(runnable);
    }

    public static Object laterAsync(@NotNull Runnable runnable, long delay, @NotNull TimeUnit unit) {
        return SCHEDULER.laterAsync(runnable, delay, unit);
    }

    public static Object timerAsync(@NotNull Runnable runnable, long delay, long period, @NotNull TimeUnit unit) {
        return SCHEDULER.timerAsync(runnable, delay, period, unit);
    }

    public static Object runBy(@NotNull Object provider, @NotNull Runnable runnable) {
        return SCHEDULER.runBy(provider, runnable);
    }

    public static Object laterBy(@NotNull Object provider, @NotNull Runnable runnable, long delay, @NotNull TimeUnit unit) {
        return SCHEDULER.laterBy(provider, runnable, delay, unit);
    }

    public static Object timerBy(@NotNull Object provider, @NotNull Runnable runnable, long delay, long period, @NotNull TimeUnit unit) {
        return SCHEDULER.timerBy(provider, runnable, delay, period, unit);
    }

    @NotNull
    public static TaskTimer<Object> timer(@NotNull String id) {
        return SCHEDULER.timer(id).onClear(timer -> TIMERS.remove(timer.getId()));
    }

    public static boolean isMainThread() {
        return SCHEDULER.isMainThread();
    }

    public static void stop(Object id) {
        SCHEDULER.stop(id);
    }

    public static boolean stop(@NotNull String id) {
        if (TIMERS.containsKey(id)) {
            return TIMERS.get(id).stop();
        }
        return false;
    }

    public static boolean stopAndClear(@NotNull String id) {
        if (TIMERS.containsKey(id)) {
            TIMERS.get(id).stop();
            TIMERS.remove(id);
            return true;
        }
        return false;
    }

    public static void stopAll() {
        for (String key : TIMERS.keySet()) {
            stop(key);
        }
    }

    public static void stopAllAndClear() {
        stopAll();
        TIMERS.clear();
    }
}
