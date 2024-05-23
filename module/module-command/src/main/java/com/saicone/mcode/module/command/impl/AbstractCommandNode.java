package com.saicone.mcode.module.command.impl;

import com.google.common.base.Suppliers;
import com.saicone.mcode.module.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractCommandNode<SenderT> implements CommandNode<SenderT> {

    private CommandNode<SenderT> parent;
    private final Supplier<String> path = Suppliers.memoize(() -> {
        if (getParent() != null) {
            return getParent().getPath() + "." + getName();
        } else {
            return getName();
        }
    });
    private Function<SenderT, String> description;
    private Function<SenderT, Integer> minArgs;
    private List<Argument<SenderT, ?, ?>> arguments;
    private CommandExecution<SenderT> execution;

    private transient Integer cachedMinArgs;

    public void setParent(@Nullable CommandNode<SenderT> parent) {
        this.parent = parent;
    }

    public void setDescription(@Nullable Function<SenderT, String> description) {
        this.description = description;
    }

    public void setArguments(@Nullable List<Argument<SenderT, ?, ?>> arguments) {
        this.arguments = arguments;
    }

    public void setExecution(@NotNull CommandExecution<SenderT> execution) {
        this.execution = execution;
    }

    public void setMinArgs(@Nullable Function<SenderT, Integer> minArgs) {
        this.minArgs = minArgs;
    }

    @SuppressWarnings("unchecked")
    public void addSubCommand(@NotNull CommandNode<SenderT> subCommand) {
        if (arguments == null) {
            arguments = new ArrayList<>();
        } else if (!arguments.isEmpty()) {
            final Argument<SenderT, ?, ?> last = arguments.get(arguments.size() - 1);
            if (last instanceof NodeArgument) {
                ((NodeArgument<SenderT>) last).getNodes().add(subCommand);
                return;
            } else if (last instanceof InputArgument && ((InputArgument<?, ?>) last).isArray()) {
                throw new IllegalArgumentException("Cannot sub command after final array argument");
            }
        }
        final NodeArgument<SenderT> nodeArgument = new NodeArgument<SenderT>().required(true);
        nodeArgument.getNodes().add(subCommand);
        arguments.add(nodeArgument);
    }

    public void addArgument(@NotNull Argument<SenderT, ?, ?> argument) {
        if (arguments == null) {
            arguments = new ArrayList<>();
        } else if (!arguments.isEmpty()) {
            final Argument<SenderT, ?, ?> last = arguments.get(arguments.size() - 1);
            if (last instanceof NodeArgument) {
                throw new IllegalArgumentException("Cannot add arguments after sub command");
            } else if (last instanceof InputArgument && ((InputArgument<?, ?>) last).isArray()) {
                throw new IllegalArgumentException("Cannot add arguments after final array argument");
            }
        }
        arguments.add(argument);
    }

    @Nullable
    @Override
    public CommandNode<SenderT> getParent() {
        return parent;
    }

    @NotNull
    @Override
    public String getPath() {
        return path.get();
    }

    @Override
    public @NotNull String getDescription(@Nullable SenderT sender) {
        if (description != null) {
            return description.apply(sender);
        }
        return CommandNode.super.getDescription(sender);
    }

    @Nullable
    @Override
    public List<Argument<SenderT, ?, ?>> getArguments() {
        return arguments;
    }

    @Nullable
    @Override
    public CommandExecution<SenderT> getExecution() {
        return execution;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public NodeArgument<SenderT> getCommandArgument() {
        if (arguments == null) {
            arguments = new ArrayList<>();
        } else if (!arguments.isEmpty()) {
            final Argument<SenderT, ?, ?> last = arguments.get(arguments.size() - 1);
            if (last instanceof NodeArgument) {
                return (NodeArgument<SenderT>) last;
            }
        }
        final NodeArgument<SenderT> nodeArgument = new NodeArgument<SenderT>().required(true);
        arguments.add(nodeArgument);
        return nodeArgument;
    }

    @Override
    public int getMinArgs(@Nullable SenderT sender) {
        if (minArgs != null) {
            return minArgs.apply(sender);
        }
        if (cachedMinArgs == null) {
            int count = 0;
            if (getArguments() != null) {
                for (Argument<SenderT, ?, ?> argument : getArguments()) {
                    if (argument.isRequired(sender)) {
                        count++;
                    } else {
                        break;
                    }
                }
            }
            cachedMinArgs = count;
        }
        return cachedMinArgs;
    }

    @Override
    public @NotNull CommandResult execute(@NotNull InputContext<SenderT> input) {
        return execution == null ? CommandResult.DONE : execution.run(input);
    }
}
