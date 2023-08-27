package com.saicone.mcode.module.command;

import com.saicone.mcode.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class CommandFunction<T> {

    private Function<InputContext<T>, CommandResult> eval;
    private Consumer<InputContext<T>> usage;
    private List<Function<InputContext<T>, CommandResult>> execution;

    @Nullable
    public Function<InputContext<T>, CommandResult> getEval() {
        return eval;
    }

    @Nullable
    public Consumer<InputContext<T>> getUsage() {
        return usage;
    }

    @Nullable
    public List<Function<InputContext<T>, CommandResult>> getExecution() {
        return execution;
    }

    public void setEval(@NotNull Function<InputContext<T>, CommandResult> eval) {
        this.eval = eval;
    }

    public void setUsage(@NotNull Consumer<InputContext<T>> usage) {
        this.usage = usage;
    }

    public void addExecution(@NotNull Function<InputContext<T>, CommandResult> execution) {
        if (this.execution == null) {
            this.execution = new ArrayList<>();
        }
        this.execution.add(execution);
    }

    @NotNull
    public CommandResult execute(@NotNull CommandCentral<T> central, @NotNull ACommand command, @NotNull InputContext<T> context) {
        if (this.eval != null) {
            final CommandResult evalResult = this.eval.apply(context);
            if (evalResult.isFail()) {
                sendUsage(context);
                return evalResult;
            }
        }

        final boolean useSyntax = command.getSyntax() != null;

        if (command.getSubCommands() != null) {
            final CommandKey savedKey = context.getKey();
            final String[] args = useSyntax ? command.getSyntax().getMapper().apply(context.getInput(), 2) : Strings.splitBySpaces(context.getInput(), 2);
            if (args.length > 1) {
                for (Object id : command.getSubCommands()) {
                    final CommandResult result;
                    if (id instanceof String) {
                        if (!((String) id).equalsIgnoreCase(String.valueOf(args[0]))) {
                            continue;
                        }
                        context.addInput(args[1]);
                        result = central.execute((String) id, context);
                    } else if (!id.equals(args[0])) {
                        continue;
                    } else {
                        context.addInput(args[1]);
                        result = central.execute((ACommand) id, context);
                    }

                    if (result == CommandResult.CONTINUE) {
                        context.removeLast();
                        continue;
                    }
                    if (result == CommandResult.BREAK) {
                        break;
                    }
                    return result;
                }
            }
            context.removeLast();
            context.setKey(savedKey);
        }

        if (this.execution == null) {
            return CommandResult.DONE;
        }

        final List<Object> arguments;
        if (useSyntax) {
            try {
                arguments = command.getSyntax().parse(context.getInput());
            } catch (IllegalArgumentException e) {
                sendUsage(context);
                return CommandResult.FAIL_SYNTAX;
            }
        } else {
            arguments = new ArrayList<>();
            Collections.addAll(arguments, Strings.splitBySpaces(context.getInput()));
        }
        context.setArguments(arguments);

        CommandResult result = CommandResult.DONE;
        for (var execution : this.execution) {
            result = execution.apply(context);
            if (result.isFail()) {
                sendUsage(context);
                return result;
            }
        }
        return result;
    }

    public void sendUsage(@NotNull InputContext<T> context) {
        if (this.usage != null) {
            this.usage.accept(context);
        }
    }
}
