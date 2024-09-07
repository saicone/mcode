package com.saicone.mcode.module.task;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public interface Scheduler<TaskT> {

    // Synchronously

    default TaskT run(@NotNull Runnable runnable) {
        return runAsync(runnable);
    }

    default TaskT later(@NotNull Runnable runnable, long delay, @NotNull TimeUnit unit) {
        return laterAsync(runnable, delay, unit);
    }

    default TaskT timer(@NotNull Runnable runnable, long delay, long period, @NotNull TimeUnit unit) {
        return timerAsync(runnable, delay, period, unit);
    }

    // Asynchronously

    TaskT runAsync(@NotNull Runnable runnable);

    TaskT laterAsync(@NotNull Runnable runnable, long delay, @NotNull TimeUnit unit);

    TaskT timerAsync(@NotNull Runnable runnable, long delay, long period, @NotNull TimeUnit unit);

    // By object that provides a separated scheduler

    default TaskT runBy(@NotNull Object provider, @NotNull Runnable runnable) {
        return runAsync(runnable);
    }

    default TaskT laterBy(@NotNull Object provider, @NotNull Runnable runnable, long delay, @NotNull TimeUnit unit) {
        return laterAsync(runnable, delay, unit);
    }

    default TaskT timerBy(@NotNull Object provider, @NotNull Runnable runnable, long delay, long period, @NotNull TimeUnit unit) {
        return timerAsync(runnable, delay, period, unit);
    }

    void stop(TaskT task);

    default TaskTimer<TaskT> timer(@NotNull String id) {
        return new TaskTimer<>(id) {
            @Override
            protected TaskT run(boolean async, long delay, long period, @NotNull TimeUnit unit, @NotNull Runnable runnable) {
                if (period > 0) {
                    return async ? timerAsync(runnable, delay, period, unit) : timer(runnable, delay, period, unit);
                }
                if (delay > 0) {
                    return async ? laterAsync(runnable, delay, unit) : later(runnable, delay, unit);
                }
                return async ? runAsync(runnable) : Scheduler.this.run(runnable);
            }

            @Override
            protected void stop(TaskT task) {
                Scheduler.this.stop(task);
            }
        };
    }

    default boolean isMainThread() {
        return false;
    }
}