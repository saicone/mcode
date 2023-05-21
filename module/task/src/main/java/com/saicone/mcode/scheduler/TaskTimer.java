package com.saicone.mcode.scheduler;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

public abstract class TaskTimer<T> {

    // Parameters
    private final String id;

    // Mutable parameters
    private T runningId = null;
    private boolean runningAsync = false;
    private long runningDelay = 0;
    private long runningPeriod = 0;
    private TimeUnit runningUnit = TimeUnit.SECONDS;
    private boolean blocked;

    // Timer interactions
    private Consumer<TaskTimer<T>> onRun;
    private Consumer<Throwable> onException;
    private Consumer<TaskTimer<T>> onStop;
    private Consumer<TaskTimer<T>> onClear;
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

    public T getRunningId() {
        return runningId;
    }

    public long getRunningDelay() {
        return runningDelay;
    }

    public long getRunningPeriod() {
        return runningPeriod;
    }

    @Nullable
    public Consumer<TaskTimer<T>> getOnRun() {
        return onRun;
    }

    @Nullable
    public Consumer<Throwable> getOnException() {
        return onException;
    }

    @Nullable
    public Consumer<TaskTimer<T>> getOnStop() {
        return onStop;
    }

    @Nullable
    public Consumer<TaskTimer<T>> getOnClear() {
        return onClear;
    }

    public boolean isRunning() {
        return runningId != null;
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
    public TaskTimer<T> onRun(@Nullable Consumer<TaskTimer<T>> onRun) {
        if (this.onRun == null || onRun == null) {
            this.onRun = onRun;
        } else {
            this.onRun = this.onRun.andThen(onRun);
        }
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public TaskTimer<T> onException(@Nullable Consumer<Throwable> onException) {
        if (this.onException == null || onException == null) {
            this.onException = onException;
        } else {
            this.onException = this.onException.andThen(onException);
        }
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public TaskTimer<T> onStop(@Nullable Consumer<TaskTimer<T>> onStop) {
        if (this.onStop == null || onStop == null) {
            this.onStop = onStop;
        } else {
            this.onStop = this.onStop.andThen(onStop);
        }
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public TaskTimer<T> onClear(@Nullable Consumer<TaskTimer<T>> onClear) {
        if (this.onClear == null || onClear == null) {
            this.onClear = onClear;
        } else {
            this.onClear = this.onClear.andThen(onClear);
        }
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public TaskTimer<T> blockingOperation(boolean blockingOperation) {
        this.blockingOperation = blockingOperation;
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public TaskTimer<T> ignoreException(boolean ignoreException) {
        this.ignoreException = ignoreException;
        return this;
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
    public TaskTimer<T> run(boolean async) {
        return run(async, 0, 0, TimeUnit.SECONDS);
    }

    @NotNull
    @Contract("_, _ -> this")
    public TaskTimer<T> run(boolean async, long delay) {
        return run(async, delay, 0, TimeUnit.SECONDS);
    }

    @NotNull
    @Contract("_, _, _ -> this")
    public TaskTimer<T> run(boolean async, long delay, long period) {
        return run(async, delay, period, TimeUnit.SECONDS);
    }

    @NotNull
    @Contract("_, _, _, _ -> this")
    public TaskTimer<T> run(boolean async, long delay, long period, @NotNull TimeUnit unit) {
        if (isRunning()) {
            stop();
        }
        if (onRun == null) {
            return this;
        }
        final Consumer<TaskTimer<T>> consumer;
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
        runningAsync = async;
        runningDelay = delay;
        runningPeriod = period;
        runningUnit = unit;
        runningId = run(async, delay, period, unit, runnable);
        return this;
    }

    protected abstract T run(boolean async, long delay, long period, @NotNull TimeUnit unit, @NotNull Runnable runnable);

    public void reset() {
        if (isRunning()) {
            stop();
            run(runningAsync, runningDelay, runningPeriod, runningUnit);
        }
    }

    public boolean stop() {
        if (isRunning()) {
            stop(runningId);
            if (onStop != null) {
                onStop.accept(this);
            }
            runningId = null;
            return true;
        }
        return false;
    }

    protected abstract void stop(T id);

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
