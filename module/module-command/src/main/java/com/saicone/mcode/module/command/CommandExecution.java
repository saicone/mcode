package com.saicone.mcode.module.command;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

@FunctionalInterface
public interface CommandExecution<SenderT> {

    @NotNull
    static <T> CommandExecution<T> of(@NotNull Consumer<InputContext<T>> consumer) {
        return inputContext -> {
            consumer.accept(inputContext);
            return CommandResult.DONE;
        };
    }

    @NotNull
    static <T> CommandExecution<T> of(@NotNull Predicate<InputContext<T>> predicate) {
        return inputContext -> predicate.test(inputContext) ? CommandResult.DONE : CommandResult.FAIL_EXECUTION;
    }

    @NotNull
    CommandResult execute(@NotNull InputContext<SenderT> inputContext);
}
