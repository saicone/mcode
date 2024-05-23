package com.saicone.mcode.module.command.brigadier;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.saicone.mcode.module.command.CommandThrowable;
import com.saicone.mcode.module.command.InputArgument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class InputArgumentType<SenderT, T> implements ArgumentType<T> {

    private final InputArgument<SenderT, T> argument;
    private final ArgumentType<T> delegate;

    public InputArgumentType(@NotNull InputArgument<SenderT, T> argument, @Nullable ArgumentType<T> delegate) {
        this.argument = argument;
        this.delegate = delegate;
    }

    @NotNull
    public InputArgument<SenderT, T> getArgument() {
        return argument;
    }

    @Nullable
    public ArgumentType<T> getDelegate() {
        return delegate;
    }

    @NotNull
    protected abstract CommandThrowable<SenderT> getThrowable();

    @NotNull
    protected abstract Message tooltip(@NotNull String msg);

    @Override
    @SuppressWarnings("unchecked")
    public T parse(StringReader reader) throws CommandSyntaxException {
        if (delegate != null) {
            return delegate.parse(reader);
        }
        String s;
        if (argument.isArray()) {
            s = reader.getRemaining();
            reader.setCursor(reader.getTotalLength());
        } else {
            s = reader.readString();
        }
        s = argument.compile(s);
        if (s == null) {
            return null;
        }
        try {
            if (argument.getTypeParser() == null) {
                return (T) s;
            } else {
                return argument.parse(s);
            }
        } catch (Throwable t) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().createWithContext(reader, s);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (argument.getSuggestion() != null) {
            final Map<String, String> suggestions = argument.getSuggestion().suggest(new BrigadierInputContext<>((CommandContext<SenderT>) context, getThrowable()));
            if (suggestions != null) {
                for (Map.Entry<String, String> entry : suggestions.entrySet()) {
                    if (entry.getValue() == null) {
                        builder.suggest(entry.getKey());
                    } else {
                        builder.suggest(entry.getKey(), tooltip(entry.getValue()));
                    }
                }
                builder.buildFuture();
            }
        } else if (delegate != null) {
            return delegate.listSuggestions(context, builder);
        }
        return ArgumentType.super.listSuggestions(context, builder);
    }

    @Override
    public Collection<String> getExamples() {
        if (argument.getSuggestion() != null) {
            final List<String> list = argument.getSuggestion().list();
            if (list != null) {
                return list;
            }
        }
        if (delegate != null) {
            return delegate.getExamples();
        }
        return ArgumentType.super.getExamples();
    }
}
