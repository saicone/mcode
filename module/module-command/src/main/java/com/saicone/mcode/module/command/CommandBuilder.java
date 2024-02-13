package com.saicone.mcode.module.command;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface CommandBuilder<SenderT> {

    @NotNull
    CommandBuilder<SenderT> alias(@NotNull String... aliases);

    @NotNull
    CommandBuilder<SenderT> description(@NotNull String description);

    @NotNull
    CommandBuilder<SenderT> permission(@NotNull String... permissions);

    @NotNull
    CommandBuilder<SenderT> eval(@NotNull Predicate<SenderT> predicate);

    @NotNull
    default CommandBuilder<SenderT> syntax(@NotNull String syntax) {
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
        return this;
    }

    @NotNull
    CommandBuilder<SenderT> argument(@NotNull CommandArgument<SenderT> argument);

    @NotNull
    default CommandBuilder<SenderT> argument(@NotNull String name) {
        return argument(CommandArgument.of(name));
    }

    @NotNull
    default CommandBuilder<SenderT> argument(@NotNull String name, @NotNull Consumer<CommandArgument<SenderT>> consumer) {
        final CommandArgument<SenderT> argument = CommandArgument.of(name);
        consumer.accept(argument);
        return argument(argument);
    }

    @NotNull
    CommandBuilder<SenderT> subCommand(@NotNull CommandNode<SenderT> node);

    @NotNull
    CommandBuilder<SenderT> subCommand(@NotNull String name, @NotNull Consumer<CommandBuilder<SenderT>> consumer);

    @NotNull
    default CommandBuilder<SenderT> executes(@NotNull Consumer<InputContext<SenderT>> execution) {
        return executes(CommandExecution.of(execution));
    }

    @NotNull
    CommandBuilder<SenderT> executes(CommandExecution<SenderT> execution);

    @NotNull
    default CommandBuilder<SenderT> test(@NotNull Predicate<InputContext<SenderT>> execution) {
        return executes(CommandExecution.of(execution));
    }

    @NotNull
    CommandBuilder<SenderT> register();

    @NotNull
    CommandBuilder<SenderT> unregister();

    @NotNull
    CommandNode<SenderT> build();
}
