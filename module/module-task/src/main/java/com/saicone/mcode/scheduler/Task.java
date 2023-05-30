package com.saicone.mcode.scheduler;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Task {

    private static Scheduler<Object> scheduler = null;
    private static final Map<String, TaskTimer<Object>> timers = new HashMap<>();

    Task() {
    }

    @NotNull
    public static Scheduler<Object> getScheduler() {
        return scheduler;
    }

    public static void setScheduler(@NotNull Scheduler<Object> scheduler) {
        if (Task.scheduler == null) {
            Task.scheduler = scheduler;
        }
    }

    public static Object run(@NotNull Runnable runnable) {
        return sync(runnable);
    }

    public static Object run(long delay, @NotNull Runnable runnable) {
        return run(delay, TimeUnit.SECONDS, runnable);
    }

    public static Object run(long delay, @NotNull TimeUnit unit, @NotNull Runnable runnable) {
        return run(!isMainThread(), delay, unit, runnable);
    }

    public static Object run(long delay, long period, @NotNull Runnable runnable) {
        return run(delay, period, TimeUnit.SECONDS, runnable);
    }

    public static Object run(long delay, long period, @NotNull TimeUnit unit, @NotNull Runnable runnable) {
        return run(!isMainThread(), delay, period, unit, runnable);
    }

    public static Object run(boolean async, @NotNull Runnable runnable) {
        if (async) {
            return async(runnable);
        } else {
            return sync(runnable);
        }
    }

    public static Object run(boolean async, long delay, @NotNull Runnable runnable) {
        return run(async, delay, TimeUnit.SECONDS, runnable);
    }

    public static Object run(boolean async, long delay, @NotNull TimeUnit unit, @NotNull Runnable runnable) {
        if (async) {
            return asyncLater(runnable, delay, unit);
        } else {
            return syncLater(runnable, delay, unit);
        }
    }

    public static Object run(boolean async, long delay, long period, @NotNull Runnable runnable) {
        return run(async, delay, period, TimeUnit.SECONDS, runnable);
    }

    public static Object run(boolean async, long delay, long period, @NotNull TimeUnit unit, @NotNull Runnable runnable) {
        if (async) {
            return asyncTimer(runnable, delay, period, unit);
        } else {
            return syncTimer(runnable, delay, period, unit);
        }
    }

    public static Object sync(@NotNull Runnable runnable) {
        return scheduler.sync(runnable);
    }

    public static Object syncLater(@NotNull Runnable runnable, long delay) {
        return syncLater(runnable, delay, TimeUnit.SECONDS);
    }

    public static Object syncLater(@NotNull Runnable runnable, long delay, @NotNull TimeUnit unit) {
        return scheduler.syncLater(runnable, delay, unit);
    }

    public static Object syncTimer(@NotNull Runnable runnable, long delay, long period) {
        return syncTimer(runnable, delay, period, TimeUnit.SECONDS);
    }

    public static Object syncTimer(@NotNull Runnable runnable, long delay, long period, @NotNull TimeUnit unit) {
        return scheduler.syncTimer(runnable, delay, period, unit);
    }

    public static Object async(@NotNull Runnable runnable) {
        return scheduler.async(runnable);
    }

    public static Object asyncLater(@NotNull Runnable runnable, long delay) {
        return asyncLater(runnable, delay, TimeUnit.SECONDS);
    }

    public static Object asyncLater(@NotNull Runnable runnable, long delay, @NotNull TimeUnit unit) {
        return scheduler.asyncLater(runnable, delay, unit);
    }

    public static Object asyncTimer(@NotNull Runnable runnable, long delay, long period) {
        return asyncTimer(runnable, delay, period, TimeUnit.SECONDS);
    }

    public static Object asyncTimer(@NotNull Runnable runnable, long delay, long period, @NotNull TimeUnit unit) {
        return scheduler.asyncTimer(runnable, delay, period, unit);
    }

    @NotNull
    public static TaskTimer<Object> timer(@NotNull String id) {
        return scheduler.timer(id).onClear(timer -> timers.remove(timer.getId()));
    }

    public static boolean isMainThread() {
        return scheduler.isMainThread();
    }

    public static void stop(Object id) {
        scheduler.stop(id);
    }

    public static boolean stop(@NotNull String id) {
        if (timers.containsKey(id)) {
            return timers.get(id).stop();
        }
        return false;
    }

    public static boolean stopAndClear(@NotNull String id) {
        if (timers.containsKey(id)) {
            timers.get(id).stop();
            timers.remove(id);
            return true;
        }
        return false;
    }

    public static void stopAll() {
        for (String key : timers.keySet()) {
            stop(key);
        }
    }

    public static void stopAllAndClear() {
        stopAll();
        timers.clear();
    }
}
