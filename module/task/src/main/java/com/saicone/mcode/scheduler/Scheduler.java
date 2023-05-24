package com.saicone.mcode.scheduler;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public interface Scheduler<T> {

    default T sync(@NotNull Runnable runnable) {
        return async(runnable);
    }

    default T syncLater(@NotNull Runnable runnable, long delay, @NotNull TimeUnit unit) {
        return asyncLater(runnable, delay, unit);
    }

    default T syncTimer(@NotNull Runnable runnable, long delay, long period, @NotNull TimeUnit unit) {
        return asyncTimer(runnable, delay, period, unit);
    }

    T async(@NotNull Runnable runnable);

    T asyncLater(@NotNull Runnable runnable, long delay, @NotNull TimeUnit unit);

    T asyncTimer(@NotNull Runnable runnable, long delay, long period, @NotNull TimeUnit unit);

    void stop(T id);

    default TaskTimer<T> timer(@NotNull String id) {
        return new TaskTimer<>(id) {
            @Override
            protected T run(boolean async, long delay, long period, @NotNull TimeUnit unit, @NotNull Runnable runnable) {
                if (period > 0) {
                    return async ? asyncTimer(runnable, delay, period, unit) : syncTimer(runnable, delay, period, unit);
                }
                if (delay > 0) {
                    return async ? asyncLater(runnable, delay, unit) : syncLater(runnable, delay, unit);
                }
                return async ? async(runnable) : sync(runnable);
            }

            @Override
            protected void stop(T id) {
                Scheduler.this.stop(id);
            }
        };
    }

    default boolean isMainThread() {
        return false;
    }
}