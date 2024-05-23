package com.saicone.mcode.module.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public interface CommandNode<SenderT> {

    default boolean hasSubCommands() {
        final var arguments = getArguments();
        return arguments != null && !arguments.isEmpty() && arguments.get(arguments.size() - 1) instanceof NodeArgument;
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

    @NotNull
    String getName();

    @NotNull
    String getPath();

    @NotNull
    Collection<String> getNodeAliases();

    @NotNull
    @SuppressWarnings("unchecked")
    default NodeArgument<SenderT> getNodeArgument() {
        final var arguments = getArguments();
        return (NodeArgument<SenderT>) arguments.get(arguments.size() - 1);
    }

    @NotNull
    default String getDescription() {
        return getDescription(null);
    }

    @NotNull
    default String getDescription(@Nullable SenderT sender) {
        return "";
    }

    default int getMinArgs() {
        return getMinArgs(null);
    }

    default int getMinArgs(@Nullable SenderT sender) {
        return 0;
    }

    default int getSize() {
        final Collection<Argument<SenderT, ?, ?>> arguments = getArguments();
        return arguments != null ? arguments.size() : 0;
    }

    @Nullable
    default Argument<SenderT, ?, ?> getArgument(int index) {
        if (getArguments() == null || index >= getArguments().size()) {
            return null;
        }
        return getArguments().get(index);
    }

    @Nullable
    default List<Argument<SenderT, ?, ?>> getArguments() {
        return null;
    }

    @Nullable
    CommandExecution<SenderT> getExecution();

    @NotNull
    @SuppressWarnings("unchecked")
    default CommandResult then(@NotNull InputContext<SenderT> context, @NotNull String... args) {
        int index = 0;
        final var arguments = getArguments();
        if (arguments != null) {
            for (var argument : arguments) {
                final int end;
                if (argument instanceof InputArgument && ((InputArgument<?, ?>) argument).isArray()) {
                    if (index < args.length) {
                        end = args.length;
                    } else {
                        break;
                    }
                } else {
                    end = index + argument.getSize();
                    if (end > args.length) {
                        return CommandResult.FAIL_SYNTAX;
                    }
                }
                final String input = String.join(" ", Arrays.copyOfRange(args, index, end));
                if (argument instanceof NodeArgument) {
                    final String name = argument.compile(input);
                    if (name == null) {
                        return CommandResult.FAIL_SYNTAX;
                    }
                    for (CommandNode<SenderT> node : ((NodeArgument<SenderT>) argument).getNodes()) {
                        if (node.matches(name)) {
                            final InputContext<SenderT> subContext = context.then(input, node, end < args.length ? Arrays.copyOfRange(args, end, args.length) : new String[0]);
                            if (subContext.getResult() == CommandResult.BREAK) {
                                break;
                            } else if (subContext.getResult() != CommandResult.CONTINUE) {
                                return subContext.getResult();
                            }
                        }
                    }
                } else if (argument instanceof InputArgument) {
                    context.addArgument(((InputArgument<?, ?>) argument).getName(), input, argument.apply(input));
                }
                index = end;
            }
        }
        final int minArgs = getMinArgs(context.getUser());
        if (context.getSize() < minArgs) {
            if (index < args.length) {
                for (int i = context.getSize(); context.getSize() < minArgs && i < args.length; i++) {
                    context.addArgument(args[i]);
                }
            }
            if (context.getSize() < minArgs) {
                return CommandResult.FAIL_SYNTAX;
            }
        }
        return execute(context);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    default CommandSuggestion<SenderT> suggest(@NotNull InputContext<SenderT> context, @NotNull String... args) {
        if (getArguments() == null) {
            return CommandSuggestion.empty();
        }
        int index = 0;
        int arg = 0;
        for (var argument : getArguments()) {
            final int end;
            if (argument instanceof InputArgument && ((InputArgument<?, ?>) argument).isArray()) {
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
            if (argument instanceof NodeArgument) {
                final String name = argument.compile(input);
                if (name == null) {
                    break;
                }
                for (CommandNode<SenderT> node : ((NodeArgument<SenderT>) argument).getNodes()) {
                    if (node.matches(name)) {
                        return context.suggest(input, node, end < args.length ? Arrays.copyOfRange(args, end, args.length) : new String[0]);
                    }
                }
            } else if (argument instanceof InputArgument) {
                context.addArgument(((InputArgument<?, ?>) argument).getName(), input, null);
                arg++;
            }
            index = end;
        }

        final CommandSuggestion<SenderT> suggestion;
        if (arg < getArguments().size()) {
            suggestion = getArguments().get(arg).getSuggestion();
        } else {
            suggestion = null;
        }
        return suggestion == null ? CommandSuggestion.empty() : suggestion.completed(context);
    }

    @NotNull
    CommandResult execute(@NotNull InputContext<SenderT> input);
}
