package com.saicone.mcode.module.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public class CommandArgument<SenderT> {

    private final String name;
    private final boolean required;
    private final boolean array;

    private int size = 1;
    private Object type = String.class;
    private Function<String, Object> typeFunction;
    private Object mapper = String.class;
    private Function<String, Object> mapperFunction;
    private List<String> suggestionList;
    private Function<SenderT, Object> suggestionFunction;

    @NotNull
    public static <T> CommandArgument<T> of(@NotNull String name) {
        String s = name;
        boolean required = false;
        boolean array = false;
        if (name.length() > 2) {
            final char start = name.charAt(0);
            final char end = name.charAt(name.length() - 1);
            if (start == '<' && end == '>') {
                s = name.substring(1, name.length() - 1);
                required = true;
            } else if (start == '[' && end == ']') {
                s = name.substring(1, name.length() - 1);
            }
        }
        if (s.length() > 3 && s.endsWith("...")) {
            s = s.substring(0, s.length() - 3);
            array = true;
        }
        return new CommandArgument<>(s, required, array);
    }

    public CommandArgument(@NotNull String name) {
        this(name, false, false);
    }

    public CommandArgument(@NotNull String name, boolean required) {
        this(name, required, false);
    }

    public CommandArgument(@NotNull String name, boolean required, boolean array) {
        this.name = name;
        this.required = required;
        this.array = array;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isArray() {
        return array;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    @NotNull
    public Object getType() {
        return type;
    }

    @Nullable
    public Function<String, Object> getTypeFunction() {
        return typeFunction;
    }

    @NotNull
    public Object getMapper() {
        return mapper;
    }

    @Nullable
    public Function<String, Object> getMapperFunction() {
        return mapperFunction;
    }

    @Nullable
    public List<String> getSuggestionList() {
        return suggestionList;
    }

    @Nullable
    public Function<SenderT, Object> getSuggestionFunction() {
        return suggestionFunction;
    }

    @NotNull
    public CommandArgument<SenderT> size(int size) {
        this.size = size;
        return this;
    }

    @NotNull
    public CommandArgument<SenderT> type(@NotNull ArgumentType type) {
        this.type = type;
        return this;
    }

    @NotNull
    public CommandArgument<SenderT> type(@NotNull Class<?> type) {
        this.type = type;
        return this;
    }

    @NotNull
    public CommandArgument<SenderT> type(@NotNull Function<String, Object> function) {
        this.typeFunction = function;
        return this;
    }

    @NotNull
    public CommandArgument<SenderT> mapper(@NotNull ArgumentType type) {
        this.mapper = type;
        return this;
    }

    @NotNull
    public CommandArgument<SenderT> mapper(@NotNull Class<?> type) {
        this.mapper = type;
        return this;
    }

    @NotNull
    public CommandArgument<SenderT> mapper(@NotNull Function<String, Object> function) {
        this.mapperFunction = function;
        return this;
    }

    @NotNull
    public CommandArgument<SenderT> suggests(@NotNull List<String> list) {
        this.suggestionList = list;
        return this;
    }

    @NotNull
    public CommandArgument<SenderT> suggests(@NotNull Object object) {
        return suggests(sender -> object);
    }

    @NotNull
    public CommandArgument<SenderT> suggests(@NotNull Function<SenderT, Object> function) {
        this.suggestionFunction = function;
        return this;
    }
}
