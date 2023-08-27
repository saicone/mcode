package com.saicone.mcode.module.command.builder;

import com.saicone.mcode.module.command.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class CommandBuilder<T> {

    private ACommand command;
    private List<Object> subCommands;
    private CommandFunction<T> function;

    public CommandBuilder(@NotNull CommandKey key) {
        this(new ACommand(key));
    }

    public CommandBuilder(@NotNull ACommand command) {
        this.command = command;
    }

    @NotNull
    public ACommand getCommand() {
        return command;
    }

    @Nullable
    public List<Object> getSubCommands() {
        return subCommands;
    }

    @Nullable
    public CommandFunction<T> getFunction() {
        return function;
    }

    @NotNull
    protected List<Object> subCommands() {
        if (subCommands == null) {
            subCommands = new ArrayList<>();
        }
        return subCommands;
    }

    @NotNull
    protected CommandFunction<T> function() {
        if (function == null) {
            function = new CommandFunction<>();
        }
        return function;
    }

    @NotNull
    @Contract("_ -> this")
    public CommandBuilder<T> edit(@NotNull Consumer<CommandBuilder<T>> consumer) {
        consumer.accept(this);
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public CommandBuilder<T> alias(@NotNull String... aliases) {
        this.command.getKey().alias(aliases);
        return this;
    }

    @NotNull
    @Contract("_, _ -> this")
    public CommandBuilder<T> alias(@NotNull String alias, @NotNull Pattern pattern) {
        this.command.getKey().alias(alias, pattern);
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public CommandBuilder<T> permission(@Nullable String permission) {
        // Not supported by default
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public CommandBuilder<T> setPermissionBound(@Nullable String permissionBound) {
        // Not supported by default
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public CommandBuilder<T> setPermissionBound(@Nullable Consumer<T> permissionBound) {
        // Not supported by default
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public CommandBuilder<T> syntax(@NotNull String syntax) {
        return syntax(syntax, " ");
    }

    @NotNull
    @Contract("_, _ -> this")
    public CommandBuilder<T> syntax(@NotNull String syntax, @NotNull String delimiter) {
        final CommandSyntax commandSyntax = new CommandSyntax(delimiter);
        for (String argument : syntax.split(delimiter)) {
            final char first = argument.charAt(0);
            final char last = argument.charAt(argument.length() - 1);
            if (first == '<' && last == '>') {
                commandSyntax.addArgument(new CommandSyntax.Argument(argument.substring(1, argument.length() - 1), true));
            } else if (first == '[' && last == ']') {
                commandSyntax.addArgument(new CommandSyntax.Argument(argument.substring(1, argument.length() - 1), false));
            } else {
                commandSyntax.addArgument(new CommandSyntax.Argument(argument, true));
            }
        }
        this.command.setSyntax(commandSyntax);
        return this;
    }

    @NotNull
    @Contract("_, _ -> this")
    public CommandBuilder<T> argument(@NotNull String name, @NotNull Consumer<CommandSyntax.Argument> consumer) {
        if (this.command.getSyntax() == null) {
            throw new NullPointerException("The command syntax should be initialized before edit arguments");
        }
        consumer.accept(this.command.getSyntax().getArgument(name));
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public CommandBuilder<T> subCommand(@NotNull String cmd) {
        if (this.subCommands == null) {
            this.subCommands = new ArrayList<>();
        }
        this.subCommands.add(cmd);
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public CommandBuilder<T> subCommand(@NotNull CommandBuilder<T> builder) {
        if (this.subCommands == null) {
            this.subCommands = new ArrayList<>();
        }
        this.subCommands.add(builder);
        return this;
    }

    @NotNull
    @Contract("_, _ -> this")
    public CommandBuilder<T> subCommand(@NotNull String name, @NotNull Consumer<CommandBuilder<T>> consumer) {
        final CommandBuilder<T> sub = new CommandBuilder<>(new CommandKey(this.command.getKey(), name));
        consumer.accept(sub);
        return subCommand(sub);
    }

    @NotNull
    @Contract("_ -> this")
    public CommandBuilder<T> eval(@NotNull Predicate<InputContext<T>> eval) {
        return eval((Function<InputContext<T>, CommandResult>) context -> eval.test(context) ? CommandResult.DONE : CommandResult.FAIL_EVAL);
    }

    @NotNull
    @Contract("_ -> this")
    public CommandBuilder<T> eval(@NotNull Function<InputContext<T>, CommandResult> eval) {
        function().setEval(eval);
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public CommandBuilder<T> usage(@NotNull String usage) {
        // Not supported by default
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public CommandBuilder<T> usage(@NotNull Consumer<InputContext<T>> usage) {
        function().setUsage(usage);
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public CommandBuilder<T> run(@NotNull Consumer<InputContext<T>> run) {
        function().addExecution(context -> {
            run.accept(context);
            return context.getResult();
        });
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public CommandBuilder<T> run(@NotNull Predicate<InputContext<T>> run) {
        function().addExecution(context -> run.test(context) ? CommandResult.DONE : CommandResult.FAIL_SYNTAX);
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public CommandBuilder<T> run(@NotNull Function<InputContext<T>, CommandResult> run) {
        function().addExecution(run);
        return this;
    }

    @NotNull
    public ACommand build() {
        if (subCommands != null) {
            for (Object subCommand : subCommands) {
                if (subCommand instanceof CommandBuilder) {
                    command.addSubCommand(((CommandBuilder<?>) subCommand).build());
                } else {
                    command.addSubCommand(subCommand);
                }
            }
        }
        return command;
    }

    public void clear() {
        command = null;
        if (subCommands != null) {
            for (Object subCommand : subCommands) {
                if (subCommand instanceof CommandBuilder) {
                    ((CommandBuilder<?>) subCommand).clear();
                }
            }
            subCommands.clear();
            subCommands = null;
        }
        function = null;
    }
}
