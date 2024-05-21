package com.saicone.mcode.module.command.impl;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.saicone.mcode.module.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public interface BrigadierCommandBuilder<SenderT, BuilderT extends BrigadierCommandBuilder<SenderT, BuilderT>> extends CommandBuilder<SenderT, BuilderT> {

    @Nullable
    default Predicate<SenderT> getRequirement() {
        return null;
    }

    @NotNull
    CommandThrowable<SenderT> getThrowable();

    @NotNull
    default LiteralCommandNode<SenderT> build(@NotNull String name) {
        final LiteralArgumentBuilder<SenderT> literal = literal(name);
        if (node().getArguments() != null) {
            for (Argument<SenderT, ?, ?> argument : node().getArguments()) {
                if (argument instanceof InputArgument) {

                } else if (argument instanceof NodeArgument) {

                }
            }
        }
        literal.executes(context -> {

        });
        return literal.build();
    }

    @NotNull
    default ArgumentType<?> type(@NotNull InputArgument<SenderT, ?> argument) {
        switch (argument.getType()) {
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
    <T> RequiredArgumentBuilder<SenderT, T> argument(@NotNull String name, @NotNull com.mojang.brigadier.arguments.ArgumentType<T> type);
}
