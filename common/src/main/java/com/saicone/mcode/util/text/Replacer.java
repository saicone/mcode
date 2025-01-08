package com.saicone.mcode.util.text;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface Replacer {

    @Nullable
    Object replace(@Nullable Object subject, @NotNull String params);

    @Nullable
    default Object replace(@Nullable Object subject, @NotNull Object relative, @NotNull String params) {
        return params;
    }
}
