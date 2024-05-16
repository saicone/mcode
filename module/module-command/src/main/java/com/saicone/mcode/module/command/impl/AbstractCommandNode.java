package com.saicone.mcode.module.command.impl;

import com.google.common.base.Suppliers;
import com.saicone.mcode.module.command.*;
import com.saicone.mcode.util.Dual;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class AbstractCommandNode<SenderT> implements CommandNode<SenderT> {

    private CommandNode<SenderT> parent;
    private List<CommandNode<SenderT>> subCommands;
    private final Supplier<String> path = Suppliers.memoize(() -> {
        if (getParent() != null) {
            return getParent().getPath() + "." + getName();
        } else {
            return getName();
        }
    });
    private Predicate<SenderT> predicate;
    private List<CommandArgument<SenderT>> arguments;
    private Function<SenderT, Integer> minArgs;
    private Function<SenderT, Integer> subStart;
    private CommandExecution<SenderT> execution;

    private transient Integer cachedMinArgs;

    public boolean eval(@NotNull SenderT sender) {
        return predicate == null || predicate.test(sender);
    }

    public void setParent(@Nullable CommandNode<SenderT> parent) {
        this.parent = parent;
    }

    public void setSubCommands(@Nullable List<CommandNode<SenderT>> subCommands) {
        this.subCommands = subCommands;
    }

    public void setPredicate(@Nullable Predicate<SenderT> predicate) {
        this.predicate = predicate;
    }

    public void setArguments(@Nullable List<CommandArgument<SenderT>> arguments) {
        this.arguments = arguments;
    }

    public void setExecution(@NotNull CommandExecution<SenderT> execution) {
        this.execution = execution;
    }

    public void setMinArgs(@Nullable Function<SenderT, Integer> minArgs) {
        this.minArgs = minArgs;
    }

    public void setSubStart(@Nullable Function<SenderT, Integer> subStart) {
        this.subStart = subStart;
    }

    public void addSubCommand(@NotNull CommandNode<SenderT> subCommand) {
        if (subCommands == null) {
            subCommands = new ArrayList<>();
        }
        subCommands.add(subCommand);
    }

    public void addArgument(@NotNull CommandArgument<SenderT> argument) {
        if (subCommands != null && !subCommands.isEmpty()) {
            throw new IllegalArgumentException("Cannot add arguments after sub command");
        }
        if (arguments == null) {
            arguments = new ArrayList<>();
        }
        if (!arguments.isEmpty() && arguments.get(arguments.size() - 1).isArray()) {
            throw new IllegalArgumentException("Cannot add arguments after final array argument");
        }
        arguments.add(argument);
    }

    @Nullable
    @Override
    public CommandNode<SenderT> getParent() {
        return parent;
    }

    @Nullable
    @Override
    public List<CommandNode<SenderT>> getSubCommands() {
        return subCommands;
    }

    @NotNull
    @Override
    public String getPath() {
        return path.get();
    }

    @Nullable
    public Predicate<SenderT> getPredicate() {
        return predicate;
    }

    @Nullable
    @Override
    public List<CommandArgument<SenderT>> getArguments() {
        return arguments;
    }

    @Override
    public int getMinArgs(@Nullable SenderT sender) {
        if (minArgs != null) {
            return minArgs.apply(sender);
        }
        if (cachedMinArgs == null) {
            int count = 0;
            for (CommandArgument<SenderT> argument : getArguments()) {
                if (argument.isRequired(sender)) {
                    count++;
                } else {
                    break;
                }
            }
            cachedMinArgs = count;
        }
        return cachedMinArgs;
    }

    @Override
    public int getSubStart(@Nullable SenderT sender) {
        if (subStart != null) {
            return subStart.apply(sender);
        }
        if (arguments != null) {
            return arguments.size();
        }
        return CommandNode.super.getSubStart(sender);
    }

    @Override
    public int parseInput(@NotNull String[] args, @NotNull BiConsumer<String, Dual<String, Object>> consumer) {
        if (arguments == null || args.length < 1) {
            return 0;
        }
        int index = 0;
        for (CommandArgument<SenderT> argument : arguments) {
            final int end;
            if (argument.isArray()) {
                if (index < args.length) {
                    end = args.length;
                } else {
                    break;
                }
            } else {
                end = index + argument.getSize();
                if (end > args.length) {
                    break;
                }
            }
            final String input = String.join(" ", Arrays.copyOfRange(args, index, end));
            final Object parsed = argument.parse(input);
            consumer.accept(argument.getName(), Dual.of(input, parsed));
            index = end;
        }
        return index;
    }

    @Override
    public @NotNull CommandResult execute(@NotNull InputContext<SenderT> input) {
        return execution == null ? CommandResult.DONE : execution.run(input);
    }
}
