package com.saicone.mcode.module.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class Argument<SenderT, T, A extends Argument<SenderT, T, A>> {

    private int size = 1;
    private boolean required;
    private Predicate<SenderT> requiredPredicate;
    private Predicate<String> typeChecker;
    private Function<String, String> mapperFunction;
    private CommandSuggestion<SenderT> suggestion;

    public boolean isRequired() {
        return required;
    }

    public boolean isRequired(@Nullable SenderT sender) {
        if (sender != null && requiredPredicate != null) {
            return requiredPredicate.test(sender);
        }
        return required;
    }

    public int getSize() {
        return size;
    }

    @Nullable
    public Predicate<SenderT> getRequiredPredicate() {
        return requiredPredicate;
    }

    @Nullable
    public Predicate<String> getTypeChecker() {
        return typeChecker;
    }

    @Nullable
    public Function<String, String> getMapperFunction() {
        return mapperFunction;
    }

    @Nullable
    public CommandSuggestion<SenderT> getSuggestion() {
        return suggestion;
    }

    @NotNull
    protected abstract A argument();

    @Nullable
    public String compile(@NotNull String s) {
        final String arg;
        if (mapperFunction != null) {
            arg = mapperFunction.apply(s);
        } else {
            arg = s;
        }
        if (typeChecker != null && !typeChecker.test(arg)) {
            return null;
        }
        return arg;
    }

    @Nullable
    public T apply(@NotNull String s) {
        final String arg = compile(s);
        return arg == null ? null : parse(arg);
    }

    @Nullable
    public abstract T parse(@NotNull String s);

    @NotNull
    public A size(int size) {
        this.size = size;
        return argument();
    }

    @NotNull
    public A required(boolean required) {
        this.required = required;
        return argument();
    }

    @NotNull
    public A required(@NotNull Predicate<SenderT> predicate) {
        this.requiredPredicate = predicate;
        return argument();
    }

    @NotNull
    public A check(@NotNull Predicate<String> predicate) {
        this.typeChecker = predicate;
        return argument();
    }

    @NotNull
    public A map(@NotNull Function<String, String> function) {
        this.mapperFunction = function;
        return argument();
    }

    @NotNull
    public A suggests(@NotNull List<String> list) {
        return suggests(CommandSuggestion.of(list));
    }

    @NotNull
    public A suggests(@NotNull CommandSuggestion<SenderT> suggestion) {
        this.suggestion = suggestion;
        return argument();
    }
}
