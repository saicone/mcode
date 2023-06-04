package com.saicone.mcode.module.script;

import com.saicone.mcode.util.function.ThrowableFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class Condition implements Eval<Boolean> {

    public static final ScriptFunction<EvalUser, Boolean> TRUE = (user) -> true;
    public static final ScriptFunction<EvalUser, Boolean> FALSE = (user) -> false;

    private final String value;

    private ScriptFunction<EvalUser, Boolean> script;
    private boolean compiled;

    public Condition(@Nullable Object object) {
        this(object == null ? null : String.valueOf(object));
    }

    public Condition(@Nullable String value) {
        this.value = value;
    }

    @Nullable
    public String getValue() {
        return value;
    }

    @Override
    public @Nullable ScriptFunction<EvalUser, Boolean> compile() {
        if (!compiled) {
            script = build();
            compiled = true;
        }
        return script;
    }

    public @Nullable ScriptFunction<EvalUser, Boolean> build() {
        return this::eval;
    }

    public boolean eval(@NotNull EvalUser user) {
        return eval(user.parse(this.value));
    }

    public boolean eval(@NotNull String value) {
        return false;
    }

    @NotNull
    public static Condition of(@Nullable Object object, @NotNull Predicate<String> predicate) {
        return new Condition(object) {
            @Override
            public boolean eval(@NotNull String value) {
                return predicate.test(value);
            }
        };
    }

    @NotNull
    public static Condition of(@Nullable Object object, @NotNull BiPredicate<EvalUser, String> predicate) {
        return new Condition(object) {
            @Override
            public boolean eval(@NotNull EvalUser user) {
                return predicate.test(user, user.parse(getValue()));
            }
        };
    }

    @NotNull
    public static <A, B> Condition of(@Nullable Object object, @NotNull ThrowableFunction<EvalUser, A> userMapper, @NotNull ThrowableFunction<String, B> valueMapper, @NotNull BiPredicate<A, B> predicate) {
        return new Condition(object) {
            @Override
            public boolean eval(@NotNull EvalUser user) {
                final String s = user.parse(getValue());
                // Avoid silent predicate fail
                final A a;
                final B b;
                try {
                    a = userMapper.apply(user);
                    b = valueMapper.apply(s);
                } catch (Throwable t) {
                    return false;
                }
                return predicate.test(a, b);
            }
        };
    }

    @NotNull
    public static <A> Condition ofUser(@Nullable Object object, @NotNull ThrowableFunction<EvalUser, A> userMapper, @NotNull Predicate<A> predicate) {
        return new Condition(object) {
            @Override
            public boolean eval(@NotNull EvalUser user) {
                final A a;
                try {
                    a = userMapper.apply(user);
                    if (a == null) {
                        return false;
                    }
                } catch (Throwable t) {
                    return false;
                }
                return predicate.test(a);
            }
        };
    }

    @NotNull
    public static <A> Condition ofUser(@Nullable Object object, @NotNull ThrowableFunction<EvalUser, A> userMapper, @NotNull BiPredicate<A, String> predicate) {
        return new Condition(object) {
            @Override
            public boolean eval(@NotNull EvalUser user) {
                final A a;
                try {
                    a = userMapper.apply(user);
                    if (a == null) {
                        return false;
                    }
                } catch (Throwable t) {
                    return false;
                }
                return predicate.test(a, user.parse(getValue()));
            }
        };
    }

    @NotNull
    public static <B> Condition ofValue(@Nullable Object object, @NotNull ThrowableFunction<String, B> valueMapper, @NotNull Predicate<B> predicate) {
        return new Condition(object) {
            @Override
            public boolean eval(@NotNull EvalUser user) {
                final String s = user.parse(getValue());
                final B b;
                try {
                    b = valueMapper.apply(s);
                } catch (Throwable t) {
                    return false;
                }
                return predicate.test(b);
            }
        };
    }

    @NotNull
    public static <B> Condition ofValue(@Nullable Object object, @NotNull ThrowableFunction<String, B> valueMapper, @NotNull BiPredicate<EvalUser, B> predicate) {
        return new Condition(object) {
            @Override
            public boolean eval(@NotNull EvalUser user) {
                final String s = user.parse(getValue());
                final B b;
                try {
                    b = valueMapper.apply(s);
                } catch (Throwable t) {
                    return false;
                }
                return predicate.test(user, b);
            }
        };
    }
}
