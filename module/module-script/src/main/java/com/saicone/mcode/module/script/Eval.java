package com.saicone.mcode.module.script;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface Eval<T> {

    @Nullable
    ScriptFunction<EvalUser, T> compile();

    @Nullable
    default ScriptFunction<EvalUser, T> compile(@Nullable Object object) {
        return compile();
    }
}
