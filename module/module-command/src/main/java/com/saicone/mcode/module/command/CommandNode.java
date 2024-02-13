package com.saicone.mcode.module.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface CommandNode<SenderT> {

    @Nullable
    CommandNode<SenderT> getParent();

    @Nullable
    Collection<CommandNode<SenderT>> getSubCommands();

    @NotNull
    String getName();

    @NotNull
    String getPath();

    @NotNull
    Collection<String> getNodeAliases();

    @NotNull
    default String getDescription() {
        return "";
    }

    @NotNull
    CommandResult execute(@NotNull InputContext<SenderT> input);
}
