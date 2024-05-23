package com.saicone.mcode.module.command.brigadier;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.saicone.mcode.module.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Predicate;

public interface BrigadierCommandBuilder<SenderT, BuilderT extends BrigadierCommandBuilder<SenderT, BuilderT>> extends CommandBuilder<SenderT, BuilderT> {

    @Nullable
    default Predicate<SenderT> getRequirement() {
        return null;
    }

    @NotNull
    CommandThrowable<SenderT> getThrowable();

    @NotNull
    @SuppressWarnings("unchecked")
    default LiteralCommandNode<SenderT> build(@NotNull CommandNode<SenderT> node) {
        final LiteralArgumentBuilder<SenderT> literal = literal(node.getName());
        if (node.getArguments() != null) {
            for (Argument<SenderT, ?, ?> argument : node.getArguments()) {
                if (argument instanceof NodeArgument) {
                    for (CommandNode<SenderT> subNode : ((NodeArgument<SenderT>) argument).getNodes()) {
                        literal.then(build(subNode));
                    }
                } else if (argument instanceof InputArgument) {
                    literal.then(build(((InputArgument<SenderT, ?>) argument)));
                }
            }
        }
        if (node.getExecution() != null) {
            literal.executes(context -> {
                final InputContext<SenderT> input = new BrigadierInputContext<>(context, getThrowable());
                node.execute(input);
                return result(input.getResult());
            });
        }
        return literal.build();
    }

    @NotNull
    default <T> ArgumentCommandNode<SenderT, T> build(@NotNull InputArgument<SenderT, T> argument) {
        final RequiredArgumentBuilder<SenderT, T> required = required(argument.getName(), argument(argument));
        if (argument.getSuggestion() != null) {
            required.suggests((context, builder) -> {
                final Map<String, String> map = argument.getSuggestion().suggest(new BrigadierInputContext<>(context, getThrowable()));
                if (map != null) {
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        if (entry.getValue() == null) {
                            builder.suggest(entry.getKey());
                        } else {
                            builder.suggest(entry.getKey(), tooltip(entry.getValue()));
                        }
                    }
                }
                return builder.buildFuture();
            });
        }
        return required.build();
    }

    @NotNull
    @SuppressWarnings("unchecked")
    default <T> ArgumentType<T> argument(@NotNull InputArgument<SenderT, T> argument) {
        final ArgumentType<T> type;
        if (argument.getType() != null) {
            type = (ArgumentType<T>) argument(argument, argument.getType());
        } else {
            type = null;
        }
        if (argument.getSuggestion() != null || type == null) {
            return new InputArgumentType<>(argument, type) {
                @Override
                protected @NotNull CommandThrowable<SenderT> getThrowable() {
                    return BrigadierCommandBuilder.this.getThrowable();
                }

                @Override
                protected @NotNull Message tooltip(@NotNull String msg) {
                    return BrigadierCommandBuilder.this.tooltip(msg);
                }
            };
        }
        return type;
    }

    @NotNull
    default ArgumentType<?> argument(@NotNull InputArgument<SenderT, ?> argument, @NotNull com.saicone.mcode.module.command.ArgumentType type) {
        switch (type) {
            case WORD:
                if (argument.isArray()) {
                    return StringArgumentType.greedyString();
                }
                return StringArgumentType.word();
            case STRING:
                return StringArgumentType.string();
            case GREEDY_STRING:
                return StringArgumentType.greedyString();
            case BOOLEAN:
                return BoolArgumentType.bool();
            case INTEGER:
                if (argument.getMin() != null) {
                    if (argument.getMax() != null) {
                        return IntegerArgumentType.integer((int) argument.getMin(), (int) argument.getMax());
                    }
                    return IntegerArgumentType.integer((int) argument.getMin());
                }
                return IntegerArgumentType.integer();
            case FLOAT:
                if (argument.getMin() != null) {
                    if (argument.getMax() != null) {
                        return FloatArgumentType.floatArg((float) argument.getMin(), (float) argument.getMax());
                    }
                    return FloatArgumentType.floatArg((float) argument.getMin());
                }
                return FloatArgumentType.floatArg();
            case LONG:
                if (argument.getMin() != null) {
                    if (argument.getMax() != null) {
                        return LongArgumentType.longArg((long) argument.getMin(), (long) argument.getMax());
                    }
                    return LongArgumentType.longArg((long) argument.getMin());
                }
                return LongArgumentType.longArg();
            case DOUBLE:
                if (argument.getMin() != null) {
                    if (argument.getMax() != null) {
                        return DoubleArgumentType.doubleArg((double) argument.getMin(), (double) argument.getMax());
                    }
                    return DoubleArgumentType.doubleArg((double) argument.getMin());
                }
                return DoubleArgumentType.doubleArg();
            default:
                return StringArgumentType.word();
        }
    }

    @NotNull
    LiteralArgumentBuilder<SenderT> literal(@NotNull String name);

    @NotNull
    <T> RequiredArgumentBuilder<SenderT, T> required(@NotNull String name, @NotNull com.mojang.brigadier.arguments.ArgumentType<T> type);

    @NotNull
    Message tooltip(@NotNull String msg);

    default int result(@NotNull CommandResult result) {
        return result.isDone() ? Command.SINGLE_SUCCESS : 0;
    }
}
