package com.saicone.mcode.module.command;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface CommandBuilder<SenderT, BuilderT extends CommandBuilder<SenderT, BuilderT>> {

    @NotNull
    BuilderT builder();

    @NotNull
    BuilderT builder(@NotNull String name);

    @NotNull
    BuilderT alias(@NotNull String... aliases);

    @NotNull
    default BuilderT description(@NotNull String description) {
        return description(sender -> description);
    }

    @NotNull
    BuilderT description(@NotNull Function<SenderT, String> description);

    @NotNull
    BuilderT permission(@NotNull String... permissions);

    @NotNull
    BuilderT eval(@NotNull Predicate<SenderT> predicate);

    @NotNull
    default BuilderT syntax(@NotNull String syntax) {
        final char[] chars = syntax.toCharArray();
        int mark = 0;
        Character looking = null;
        for (int i = 0; i < chars.length; i++) {
            final char c = chars[i];
            if (i + 1 >= chars.length) {
                argument(syntax.substring(mark, i + 1));
            }
            if (looking != null) {
                if (looking == c && (i + 1 >= chars.length || chars[i + 1] == ' ')) {
                    i++;
                    argument(syntax.substring(mark, i));
                    mark = i;
                    looking = null;
                }
                continue;
            }
            if (mark == i) {
                if (c == '<') {
                    looking = '>';
                    continue;
                } else if (c == '[') {
                    looking = ']';
                    continue;
                }
            }
            if (c == ' ' && mark < i) {
                argument(syntax.substring(mark, i + 1));
                mark = i + 1;
            }
        }
        return builder();
    }

    @NotNull
    BuilderT argument(@NotNull CommandArgument<SenderT> argument);

    @NotNull
    default BuilderT argument(@NotNull String name) {
        return argument(CommandArgument.of(name));
    }

    @NotNull
    default BuilderT argument(@NotNull String name, @NotNull ArgumentType type) {
        final CommandArgument<SenderT> argument = CommandArgument.of(name);
        argument.type(type);
        return argument(argument);
    }

    @NotNull
    default BuilderT argument(@NotNull String name, @NotNull Class<?> type) {
        final CommandArgument<SenderT> argument = CommandArgument.of(name);
        argument.type(type);
        return argument(argument);
    }

    @NotNull
    default BuilderT argument(@NotNull String name, @NotNull Consumer<CommandArgument<SenderT>> consumer) {
        final CommandArgument<SenderT> argument = CommandArgument.of(name);
        consumer.accept(argument);
        return argument(argument);
    }

    @NotNull
    default BuilderT minArgs(int minArgs) {
        return minArgs(sender -> minArgs);
    }

    @NotNull
    BuilderT minArgs(@NotNull Function<SenderT, Integer> minArgs);

    @NotNull
    default BuilderT subStart(int subStart) {
        return subStart(sender -> subStart);
    }

    @NotNull
    BuilderT subStart(@NotNull Function<SenderT, Integer> subStart);

    @NotNull
    BuilderT subCommand(@NotNull CommandNode<SenderT> node);

    @NotNull
    BuilderT subCommand(@NotNull String name, @NotNull Consumer<BuilderT> consumer);

    @NotNull
    BuilderT throwable(@NotNull CommandThrowable<SenderT> throwable);

    @NotNull
    default BuilderT executes(@NotNull Consumer<InputContext<SenderT>> execution) {
        return executes(CommandExecution.of(execution));
    }

    @NotNull
    BuilderT executes(CommandExecution<SenderT> execution);

    @NotNull
    default BuilderT test(@NotNull Predicate<InputContext<SenderT>> execution) {
        return executes(CommandExecution.of(execution));
    }

    @NotNull
    BuilderT register();

    @NotNull
    BuilderT unregister();

    @NotNull
    CommandNode<SenderT> build();
}
