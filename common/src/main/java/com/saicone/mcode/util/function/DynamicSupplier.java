package com.saicone.mcode.util.function;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class DynamicSupplier<T> implements Supplier<T> {

    private final List<T> objects;
    private final long updateTime;

    @NotNull
    public static <T> DynamicSupplier<T> valueOf(@NotNull List<T> objects, long time, @NotNull TimeUnit unit) {
        return new DynamicSupplier<>(objects, unit.toMillis(time));
    }

    protected DynamicSupplier(@NotNull List<T> objects, long updateTime) {
        this.objects = objects;
        this.updateTime = updateTime;
    }

    @Override
    public T get() {
        if (this.objects.isEmpty()) {
            return null;
        }
        return this.objects.get((int) ((System.currentTimeMillis() / this.updateTime) % this.objects.size()));
    }

    @NotNull
    public Optional<T> getOptional() {
        return Optional.ofNullable(get());
    }

    public long getTime(@NotNull TimeUnit unit) {
        return TimeUnit.MILLISECONDS.convert(this.updateTime, unit);
    }
}
