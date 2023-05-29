package com.saicone.mcode.module.script;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

@FunctionalInterface
public interface ScriptFunction<T, R> extends Function<T, R> {

    R apply(T var1);

    @NotNull
    default ScriptFunction<T, R> ifElse(@NotNull ScriptFunction<T, Boolean> condition, @Nullable ScriptFunction<T, R> orElse) {
        return (sender) -> {
            if (condition.apply(sender)) {
                return apply(sender);
            } else if (orElse != null) {
                return orElse.apply(sender);
            } else {
                return null;
            }
        };
    }

    @NotNull
    default ScriptFunction<T, R> ifAnd(@NotNull R expected, @NotNull ScriptFunction<T, R> orElse) {
        return (sender) -> {
            final R result = apply(sender);
            if (result == expected) {
                return orElse.apply(sender);
            }
            return result;
        };
    }

    @NotNull
    default ScriptFunction<T, R> ifAnd(@NotNull ScriptFunction<T, Boolean> condition, @NotNull R expected, @Nullable ScriptFunction<T, R> orElse) {
        return (sender) -> {
            if (condition.apply(sender)) {
                final R result = apply(sender);
                if (orElse != null && result.equals(expected)) {
                    return orElse.apply(sender);
                }
                return result;
            } else if (orElse != null) {
                return orElse.apply(sender);
            } else {
                return null;
            }
        };
    }
}
