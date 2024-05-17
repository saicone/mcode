package com.saicone.mcode.module.command;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface CommandExecutor<SenderT> {
    @NotNull
    CommandResult result(@NotNull SenderT sender, @NotNull String id, @NotNull String... args);
}
