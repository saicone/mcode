package com.saicone.mcode.util.concurrent;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public interface DelayedExecutor extends Executor {

    void execute(@NotNull Runnable command, long delay, @NotNull TimeUnit unit);

    void execute(@NotNull Runnable command, long delay, long period, @NotNull TimeUnit unit);

}
