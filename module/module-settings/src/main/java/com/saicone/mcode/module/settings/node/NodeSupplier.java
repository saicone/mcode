package com.saicone.mcode.module.settings.node;

import com.saicone.mcode.module.settings.Settings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NodeSupplier<T> implements Supplier<T> {

    private final Function<Settings, T> function;
    private T cachedValue;
    private T oldValue = null;

    private Consumer<NodeSupplier<T>> updateConsumer = null;

    public NodeSupplier(@NotNull Function<Settings, T> function, T cachedValue) {
        this.function = function;
        this.cachedValue = cachedValue;
    }

    public void update(@NotNull Settings settings) {
        final T t = function.apply(settings);
        if (!Objects.equals(t, cachedValue)) {
            oldValue = cachedValue;
            cachedValue = t;
            if (updateConsumer != null) {
                updateConsumer.accept(this);
            }
        }
    }

    @NotNull
    public NodeSupplier<T> onUpdate(@Nullable Consumer<NodeSupplier<T>> updateConsumer) {
        this.updateConsumer = updateConsumer;
        return this;
    }

    @Override
    public T get() {
        return cachedValue;
    }

    @NotNull
    public Function<Settings, T> getFunction() {
        return function;
    }

    public T getCachedValue() {
        return cachedValue;
    }

    public T getOldValue() {
        return oldValue;
    }
}
