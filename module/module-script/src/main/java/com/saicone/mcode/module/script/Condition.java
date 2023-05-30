package com.saicone.mcode.module.script;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Condition implements Eval<Boolean> {

    public static final ScriptFunction<EvalUser, Boolean> TRUE = (user) -> true;
    public static final ScriptFunction<EvalUser, Boolean> FALSE = (user) -> false;

    private final String value;

    private ScriptFunction<EvalUser, Boolean> script;
    private boolean compiled;

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
        return (user) -> eval(user.parse(this.value));
    }

    public boolean eval(@NotNull String value) {
        return false;
    }
}
