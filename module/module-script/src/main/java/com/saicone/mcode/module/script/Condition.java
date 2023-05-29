package com.saicone.mcode.module.script;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Condition implements Eval<Boolean> {

    public static final ScriptFunction<EvalUser, Boolean> TRUE = (user) -> true;
    public static final ScriptFunction<EvalUser, Boolean> FALSE = (user) -> false;

    private final String value;

    public Condition(@Nullable String value) {
        this.value = value;
    }

    @Nullable
    public String getValue() {
        return value;
    }

    @Override
    public @Nullable ScriptFunction<EvalUser, Boolean> compile() {
        return (user) -> eval(user.parse(this.value));
    }

    public boolean eval(@NotNull String value) {
        return false;
    }
}
