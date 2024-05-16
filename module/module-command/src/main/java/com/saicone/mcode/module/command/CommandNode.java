package com.saicone.mcode.module.command;

import com.saicone.mcode.util.Dual;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

public interface CommandNode<SenderT> {

    default boolean hasSubCommands() {
        final Collection<CommandNode<SenderT>> nodes = getSubCommands();
        return nodes != null && !nodes.isEmpty();
    }

    default boolean matches(@NotNull String s) {
        if (getName().equalsIgnoreCase(s)) {
            return true;
        }
        for (String alias : getNodeAliases()) {
            if (alias.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    CommandNode<SenderT> getParent();

    @Nullable
    default CommandNode<SenderT> getSubCommand(@NotNull String s) {
        final Collection<CommandNode<SenderT>> nodes = getSubCommands();
        if (nodes != null) {
            for (CommandNode<SenderT> node : nodes) {
                if (node.matches(s)) {
                    return node;
                }
            }
        }
        return null;
    }

    Collection<CommandNode<SenderT>> getSubCommands();

    @NotNull
    String getName();

    @NotNull
    String getPath();

    @NotNull
    Collection<String> getNodeAliases();

    @NotNull
    default String getDescription() {
        return getDescription(null);
    }

    @NotNull
    default String getDescription(@Nullable SenderT sender) {
        return "";
    }

    default int getSize() {
        final Collection<CommandArgument<SenderT>> arguments = getArguments();
        return arguments != null ? arguments.size() : 0;
    }

    default List<CommandArgument<SenderT>> getArguments() {
        return null;
    }

    default int getMinArgs() {
        return getMinArgs(null);
    }

    default int getMinArgs(@Nullable SenderT sender) {
        return 0;
    }

    default int getSubStart() {
        return getSubStart(null);
    }

    default int getSubStart(@Nullable SenderT sender) {
        return 0;
    }

    int parseInput(@NotNull String[] args, @NotNull BiConsumer<String, Dual<String, Object>> consumer);

    @NotNull
    CommandResult execute(@NotNull InputContext<SenderT> input);
}
