package com.saicone.mcode.module.script;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Function;

@FunctionalInterface
public interface ScriptFunction<T, R> extends Function<T, R> {

    R apply(T t);

    @NotNull
    default <A> ScriptFunction<T, A> map(@NotNull Function<R, A> mapper) {
        return t -> mapper.apply(apply(t));
    }

    @NotNull
    default <A> ScriptFunction<T, A> map(@NotNull BiFunction<T, R, A> mapper) {
        return t -> mapper.apply(t, apply(t));
    }

    @NotNull
    default ScriptFunction<T, R> ifElse(@NotNull ScriptFunction<T, Boolean> condition, @Nullable ScriptFunction<T, R> orElse) {
        return t -> {
            if (condition.apply(t)) {
                return apply(t);
            } else if (orElse != null) {
                return orElse.apply(t);
            } else {
                return null;
            }
        };
    }

    @NotNull
    default ScriptFunction<T, R> ifAnd(@NotNull R expected, @NotNull ScriptFunction<T, R> orElse) {
        return t -> {
            final R result = apply(t);
            if (result == expected) {
                return orElse.apply(t);
            }
            return result;
        };
    }

    @NotNull
    default ScriptFunction<T, R> ifAnd(@NotNull ScriptFunction<T, Boolean> condition, @NotNull R expected, @Nullable ScriptFunction<T, R> orElse) {
        return t -> {
            if (condition.apply(t)) {
                final R result = apply(t);
                if (orElse != null && result.equals(expected)) {
                    return orElse.apply(t);
                }
                return result;
            } else if (orElse != null) {
                return orElse.apply(t);
            } else {
                return null;
            }
        };
    }
}
