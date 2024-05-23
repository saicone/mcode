package com.saicone.mcode.module.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@FunctionalInterface
public interface CommandSuggestion<SenderT> {

    @NotNull
    @SuppressWarnings("unchecked")
    static <SenderT> CommandSuggestion<SenderT> empty() {
        return (CommandSuggestion<SenderT>) InputContext.EMPTY_SUGGESTION;
    }

    @NotNull
    default CommandSuggestion<SenderT> completed(@NotNull InputContext<SenderT> context) {
        return new CommandSuggestion<>() {
            @Override
            public @Nullable Map<String, String> suggest(@NotNull InputContext<SenderT> context) {
                return CommandSuggestion.this.suggest(context);
            }

            @Override
            public @Nullable Map<String, String> get() {
                return suggest(context);
            }
        };
    }

    @NotNull
    static <SenderT> CommandSuggestion<SenderT> of(@NotNull List<String> list) {
        final Map<String, String> map = new LinkedHashMap<>();
        for (String key : list) {
            map.put(key, null);
        }
        return new CommandSuggestion<>() {
            @Override
            public @NotNull Map<String, String> suggest(@NotNull InputContext<SenderT> context) {
                return map;
            }
            @Override
            public @NotNull Map<String, String> get() {
                return map;
            }
            @Override
            public @NotNull List<String> list() {
                return list;
            }
        };
    }

    @NotNull
    static <SenderT> CommandSuggestion<SenderT> of(@NotNull Function<InputContext<SenderT>, Iterable<String>> function) {
        return context -> {
            final Iterable<String> list = function.apply(context);
            if (list == null) {
                return null;
            }
            final Map<String, String> map = new LinkedHashMap<>();
            for (String key : list) {
                map.put(key, null);
            }
            return map;
        };
    }

    @NotNull
    static <SenderT, E> CommandSuggestion<SenderT> of(@NotNull Iterable<E> list, @NotNull Function<E, String> mapper) {
        return new CommandSuggestion<>() {
            @Override
            public @NotNull Map<String, String> suggest(@NotNull InputContext<SenderT> context) {
                final Map<String, String> map = new LinkedHashMap<>();
                for (E element : list) {
                    map.put(mapper.apply(element), null);
                }
                return map;
            }
            @Override
            public @NotNull Map<String, String> get() {
                final Map<String, String> map = new LinkedHashMap<>();
                for (E element : list) {
                    map.put(mapper.apply(element), null);
                }
                return map;
            }
        };
    }

    // text | tooltip
    @Nullable
    Map<String, String> suggest(@NotNull InputContext<SenderT> context);

    @Nullable
    default List<String> suggestList(@NotNull InputContext<SenderT> context) {
        final Map<String, String> map = suggest(context);
        return map == null ? null : new ArrayList<>(map.keySet());
    }

    @NotNull
    default CompletableFuture<Map<String, String>> suggestFuture(@NotNull InputContext<SenderT> context) {
        return CompletableFuture.completedFuture(suggest(context));
    }

    @NotNull
    default CompletableFuture<Map<String, String>> suggestAsync(@NotNull InputContext<SenderT> context) {
        return CompletableFuture.supplyAsync(() -> suggest(context));
    }

    @NotNull
    default CompletableFuture<Map<String, String>> suggestAsync(@NotNull InputContext<SenderT> context, @NotNull Executor executor) {
        return CompletableFuture.supplyAsync(() -> suggest(context), executor);
    }

    @Nullable
    default Map<String, String> get() {
        return null;
    }

    @Nullable
    default List<String> list() {
        final Map<String, String> map = get();
        return map == null ? null : new ArrayList<>(map.keySet());
    }

    @NotNull
    default CompletableFuture<Map<String, String>> future() {
        return CompletableFuture.completedFuture(get());
    }

    @NotNull
    default CompletableFuture<List<String>> futureList() {
        return CompletableFuture.completedFuture(list());
    }

    @NotNull
    default CompletableFuture<Map<String, String>> supplyAsync() {
        return CompletableFuture.supplyAsync(this::get);
    }

    @NotNull
    default CompletableFuture<Map<String, String>> supplyAsync(@NotNull Executor executor) {
        return CompletableFuture.supplyAsync(this::get, executor);
    }

    @NotNull
    default CompletableFuture<List<String>> supplyAsyncList() {
        return CompletableFuture.supplyAsync(this::list);
    }

    @NotNull
    default CompletableFuture<List<String>> supplyAsyncList(@NotNull Executor executor) {
        return CompletableFuture.supplyAsync(this::list, executor);
    }
}
