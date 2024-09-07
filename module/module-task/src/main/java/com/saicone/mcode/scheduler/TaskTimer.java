package com.saicone.mcode.scheduler;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

public abstract class TaskTimer<TaskT> {

    // Parameters
    private final String id;

    // Mutable parameters
    private TaskT runningTask = null;
    private Object runningProvider = null;
    private boolean runningAsync = false;
    private long runningDelay = 0;
    private long runningPeriod = 0;
    private TimeUnit runningUnit = TimeUnit.SECONDS;
    private boolean blocked;

    // Timer interactions
    private Consumer<TaskTimer<TaskT>> onRun;
    private Consumer<Throwable> onException;
    private Consumer<TaskTimer<TaskT>> onStop;
    private Consumer<TaskTimer<TaskT>> onClear;
    // Timer options
    private boolean blockingOperation;
    private boolean ignoreException;

    public TaskTimer(@NotNull String id) {
        this.id = id;
    }

    @NotNull
    public String getId() {
        return id;
    }

    public TaskT getRunningTask() {
        return runningTask;
    }

    public long getRunningDelay() {
        return runningDelay;
    }

    public long getRunningPeriod() {
        return runningPeriod;
    }

    @Nullable
    public Consumer<TaskTimer<TaskT>> getOnRun() {
        return onRun;
    }

    @Nullable
    public Consumer<Throwable> getOnException() {
        return onException;
    }

    @Nullable
    public Consumer<TaskTimer<TaskT>> getOnStop() {
        return onStop;
    }

    @Nullable
    public Consumer<TaskTimer<TaskT>> getOnClear() {
        return onClear;
    }

    public boolean isRunning() {
        return runningTask != null;
    }

    public boolean isRunningAsync() {
        return runningAsync;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public boolean isBlockingOperation() {
        return blockingOperation;
    }

    public boolean isIgnoreException() {
        return ignoreException;
    }

    @NotNull
    @Contract("_ -> this")
    public TaskTimer<TaskT> onRun(@Nullable Consumer<TaskTimer<TaskT>> onRun) {
        if (this.onRun == null || onRun == null) {
            this.onRun = onRun;
        } else {
            this.onRun = this.onRun.andThen(onRun);
        }
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public TaskTimer<TaskT> onException(@Nullable Consumer<Throwable> onException) {
        if (this.onException == null || onException == null) {
            this.onException = onException;
        } else {
            this.onException = this.onException.andThen(onException);
        }
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public TaskTimer<TaskT> onStop(@Nullable Consumer<TaskTimer<TaskT>> onStop) {
        if (this.onStop == null || onStop == null) {
            this.onStop = onStop;
        } else {
            this.onStop = this.onStop.andThen(onStop);
        }
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public TaskTimer<TaskT> onClear(@Nullable Consumer<TaskTimer<TaskT>> onClear) {
        if (this.onClear == null || onClear == null) {
            this.onClear = onClear;
        } else {
            this.onClear = this.onClear.andThen(onClear);
        }
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public TaskTimer<TaskT> blockingOperation(boolean blockingOperation) {
        this.blockingOperation = blockingOperation;
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public TaskTimer<TaskT> ignoreException(boolean ignoreException) {
        this.ignoreException = ignoreException;
        return this;
    }

    public void setRunningProvider(@Nullable Object runningProvider) {
        this.runningProvider = runningProvider;
        reset();
    }

    public void setRunningAsync(boolean runningAsync) {
        this.runningAsync = runningAsync;
        reset();
    }

    public void setRunningDelay(long runningDelay) {
        this.runningDelay = runningDelay;
        reset();
    }

    public void setRunningPeriod(long runningPeriod) {
        this.runningPeriod = runningPeriod;
        reset();
    }

    public void setRunningUnit(@NotNull TimeUnit runningUnit) {
        this.runningUnit = runningUnit;
        reset();
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    @NotNull
    @Contract("_ -> this")
    public TaskTimer<TaskT> run(@NotNull Object operator) {
        return run(operator, 0, 0, TimeUnit.SECONDS);
    }

    @NotNull
    @Contract("_, _ -> this")
    public TaskTimer<TaskT> run(@NotNull Object operator, long delay) {
        return run(operator, delay, 0, TimeUnit.SECONDS);
    }

    @NotNull
    @Contract("_, _, _ -> this")
    public TaskTimer<TaskT> run(@NotNull Object operator, long delay, long period) {
        return run(operator, delay, period, TimeUnit.SECONDS);
    }

    @NotNull
    @Contract("_, _, _, _ -> this")
    public TaskTimer<TaskT> run(@NotNull Object operator, long delay, long period, @NotNull TimeUnit unit) {
        if (isRunning()) {
            stop();
        }
        if (onRun == null) {
            return this;
        }
        final Consumer<TaskTimer<TaskT>> consumer;
        if (ignoreException) {
            consumer = onRun.ignoreException();
        } else if (onException != null) {
            consumer = onRun.catchException(onException);
        } else {
            consumer = onRun;
        }
        final Runnable runnable;
        if (blockingOperation) {
            runnable = () -> {
                if (isBlocked()) {
                    return;
                }
                setBlocked(true);
                consumer.accept(this);
                setBlocked(false);
            };
        } else {
            runnable = () -> consumer.accept(this);
        }
        runningDelay = delay;
        runningPeriod = period;
        runningUnit = unit;
        if (operator instanceof Boolean) {
            runningAsync = (Boolean) operator;
            runningTask = run((boolean) operator, delay, period, unit, runnable);
        } else {
            runningProvider = operator;
            runningAsync = true;
            runningTask = run(operator, delay, period, unit, runnable);
        }
        return this;
    }

    protected abstract TaskT run(boolean async, long delay, long period, @NotNull TimeUnit unit, @NotNull Runnable runnable);

    protected TaskT run(@NotNull Object provider, long delay, long period, @NotNull TimeUnit unit, @NotNull Runnable runnable) {
        return run(true, delay, period, unit, runnable);
    }

    public void reset() {
        if (isRunning()) {
            stop();
            run(runningProvider == null ? runningAsync : runningProvider, runningDelay, runningPeriod, runningUnit);
        }
    }

    public boolean stop() {
        if (isRunning()) {
            stop(runningTask);
            if (onStop != null) {
                onStop.accept(this);
            }
            runningTask = null;
            return true;
        }
        return false;
    }

    protected abstract void stop(TaskT task);

    public boolean stopAndClear() {
        if (onClear != null) {
            onClear.accept(this);
        }
        return stop();
    }

    @FunctionalInterface
    public interface Consumer<T> {

        void accept(@NotNull T timer);

        @NotNull
        default Consumer<T> andThen(@NotNull Consumer<T> consumer) {
            return (t) -> {
                accept(t);
                consumer.accept(t);
            };
        }

        @NotNull
        default Consumer<T> ignoreException() {
            return t -> {
                try {
                    accept(t);
                } catch (Throwable ignored) { }
            };
        }

        @NotNull
        default Consumer<T> catchException(@NotNull Consumer<Throwable> consumer) {
            return t -> {
                try {
                    accept(t);
                } catch (Throwable e) {
                    consumer.accept(e);
                }
            };
        }
    }
}
