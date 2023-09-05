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

    private boolean silentFail = false;

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

    public boolean isSilentFail() {
        return silentFail;
    }

    public void setEval(@NotNull Function<InputContext<T>, CommandResult> eval) {
        setEval(eval, false);
    }

    public void setEval(@NotNull Function<InputContext<T>, CommandResult> eval, boolean before) {
        if (this.eval == null) {
            this.eval = eval;
        } else {
            final Function<InputContext<T>, CommandResult> function = this.eval;
            if (before) {
                this.eval = context -> {
                    final CommandResult result = eval.apply(context);
                    return result == CommandResult.FAIL_EVAL ? result : function.apply(context);
                };
            } else {
                this.eval = context -> {
                    final CommandResult result = function.apply(context);
                    return result == CommandResult.FAIL_EVAL ? result : eval.apply(context);
                };
            }
        }
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

    public void setSilentFail(boolean silentFail) {
        this.silentFail = silentFail;
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
        try {
            for (var execution : this.execution) {
                result = execution.apply(context);
                if (result.isFail()) {
                    sendUsage(context);
                    return result;
                }
            }
        } catch (Throwable t) {
            result = CommandResult.FAIL_EXECUTION;
            if (!silentFail) {
                t.printStackTrace();
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
