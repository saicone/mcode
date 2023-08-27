package com.saicone.mcode.module.command;

import com.saicone.mcode.module.command.builder.CommandBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public abstract class CommandCentral<T> {

    protected static CommandCentral<? extends Object> INSTANCE;

    private final Map<ACommand, CommandFunction<T>> commands = new HashMap<>();

    @NotNull
    @SuppressWarnings("unchecked")
    public static CommandCentral<Object> get() {
        return (CommandCentral<Object>) INSTANCE;
    }

    @NotNull
    public CommandBuilder<T> builder(@NotNull String name, @NotNull String... aliases) {
        return new CommandBuilder<>(new CommandKey(name).alias(aliases));
    }

    @NotNull
    public CommandBuilder<T> builder(@NotNull CommandKey key) {
        return new CommandBuilder<>(key);
    }

    @NotNull
    public CommandBuilder<T> builder(@NotNull ACommand command) {
        return new CommandBuilder<>(command);
    }

    public abstract CommandResult dispatch(@NotNull T user, @NotNull String id, @NotNull String input);

    public CommandResult dispatch(@NotNull String id, @NotNull InputContext<T> context) {
        return dispatch(context.getUser(), id, context.getInput());
    }

    public CommandResult execute(@NotNull T user, @NotNull String input) {
        return execute(user, (T) null, input);
    }

    public CommandResult execute(@NotNull T user, @Nullable T agent, @NotNull String input) {
        final int index = input.indexOf(' ');
        if (index > 0) {
            return execute(user, agent, input.substring(0, index), input.substring(index + 1));
        } else {
            return execute(user, agent, input, "");
        }
    }

    public CommandResult execute(@NotNull T user, @NotNull String id, @NotNull String input) {
        return execute(user, null, id, input);
    }

    public CommandResult execute(@NotNull T user, @Nullable T agent, @NotNull String id, @NotNull String input) {
        return execute(id, new InputContext<>(user, agent, input));
    }

    public CommandResult execute(@NotNull String id, @NotNull InputContext<T> context) {
        for (var entry : commands.entrySet()) {
            if (entry.getKey().equals(id)) {
                context.setKey(entry.getKey().getKey());
                return entry.getValue().execute(this, entry.getKey(), context);
            }
        }
        context.setKey(new CommandKey(id));
        return dispatch(id, context);
    }

    public CommandResult execute(@NotNull ACommand command, @NotNull InputContext<T> context) {
        final CommandFunction<T> function = commands.get(command);
        if (function == null) {
            return CommandResult.NOT_REGISTERED;
        }
        context.setKey(command.getKey());
        return function.execute(this, command, context);
    }

    @SuppressWarnings("unchecked")
    protected void register(@NotNull CommandBuilder<T> builder) {
        commands.put(builder.build(), builder.getFunction() == null ? new CommandFunction<>() : builder.getFunction());
        if (builder.getSubCommands() != null) {
            for (Object subCommand : builder.getSubCommands()) {
                if (subCommand instanceof CommandBuilder) {
                    register((CommandBuilder<T>) subCommand);
                }
            }
        }
    }
}
