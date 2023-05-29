package com.saicone.mcode.module.script;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface EvalBuilder<E extends Eval<?>> {

    @Nullable
    E build(@Nullable Object object);
}
