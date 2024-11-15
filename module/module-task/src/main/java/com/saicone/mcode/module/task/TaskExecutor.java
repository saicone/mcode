package com.saicone.mcode.module.task;

import com.saicone.delivery4j.util.DelayedExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class TaskExecutor implements DelayedExecutor<Object> {
    @Override
    public @NotNull Object execute(@NotNull Runnable command) {
        return Task.runAsync(command);
    }

    @Override
    public @NotNull Object execute(@NotNull Runnable command, long delay, @NotNull TimeUnit unit) {
        return Task.laterAsync(command, delay, unit);
    }

    @Override
    public @NotNull Object execute(@NotNull Runnable command, long delay, long period, @NotNull TimeUnit unit) {
        return Task.timerAsync(command, delay, period, unit);
    }

    @Override
    public void cancel(@NotNull Object object) {
        Task.stop(object);
    }

    @Override
    public @NotNull Executor asExecutor() {
        return Task.getScheduler().asExecutor();
    }
}
