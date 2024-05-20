package com.saicone.mcode.module.command;

import com.saicone.types.TypeParser;
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
    CommandNode<SenderT> node();

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
    default BuilderT minArgs(int minArgs) {
        return minArgs(sender -> minArgs);
    }

    @NotNull
    BuilderT minArgs(@NotNull Function<SenderT, Integer> minArgs);

    @NotNull
    BuilderT with(@NotNull InputArgument<SenderT, ?> argument);

    @NotNull
    default <T> BuilderT with(@NotNull InputArgument<SenderT, T> argument, @NotNull Consumer<InputArgument<SenderT, T>> consumer) {
        consumer.accept(argument);
        return with(argument);
    }

    @NotNull
    default BuilderT with(@NotNull String name) {
        return with(InputArgument.of(name));
    }

    @NotNull
    default BuilderT with(@NotNull String name, @NotNull ArgumentType type) {
        return with(InputArgument.of(name, type));
    }

    @NotNull
    default <T> BuilderT with(@NotNull String name, @NotNull Class<T> type) {
        return with(InputArgument.of(name, type));
    }

    @NotNull
    default <T> BuilderT with(@NotNull String name, @NotNull TypeParser<T> typeParser) {
        return with(InputArgument.of(name, typeParser));
    }

    @NotNull
    BuilderT sub(@NotNull Consumer<NodeArgument<SenderT>> consumer);

    @NotNull
    BuilderT sub(@NotNull CommandNode<SenderT> node);

    @NotNull
    default BuilderT sub(@NotNull String name, @NotNull Consumer<BuilderT> consumer) {
        final BuilderT builder = builder(name);
        consumer.accept(builder);
        return sub(builder.node());
    }

    @NotNull
    default BuilderT syntax(@NotNull String syntax) {
        final char[] chars = syntax.toCharArray();
        int mark = 0;
        Character looking = null;
        for (int i = 0; i < chars.length; i++) {
            final char c = chars[i];
            if (i + 1 >= chars.length) {
                with(syntax.substring(mark, i + 1));
            }
            if (looking != null) {
                if (looking == c && (i + 1 >= chars.length || chars[i + 1] == ' ')) {
                    i++;
                    with(syntax.substring(mark, i));
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
                with(syntax.substring(mark, i + 1));
                mark = i + 1;
            }
        }
        return builder();
    }

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
}
