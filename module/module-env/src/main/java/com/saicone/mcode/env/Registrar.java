package com.saicone.mcode.env;

import org.jetbrains.annotations.NotNull;

public interface Registrar {

    boolean isPresent(@NotNull String dependency);

    void register(@NotNull Object object);
}
