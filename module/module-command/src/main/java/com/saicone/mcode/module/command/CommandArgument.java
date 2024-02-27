package com.saicone.mcode.module.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class CommandArgument<SenderT> {

    private final String name;
    private final boolean required;
    private final boolean array;

    private Predicate<SenderT> requiredPredicate;
    private boolean preview;
    private Function<String, Object> previewFunction;
    private int size = 1;
    private Object type = String.class;
    private Function<String, Object> typeFunction;
    private Predicate<String> typeChecker;
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
        return isRequired(null);
    }

    public boolean isRequired(@Nullable SenderT sender) {
        if (sender != null && requiredPredicate != null) {
            return requiredPredicate.test(sender);
        }
        return required;
    }

    public boolean isArray() {
        return array;
    }

    public boolean isPreview() {
        return preview;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Nullable
    public Predicate<SenderT> getRequiredPredicate() {
        return requiredPredicate;
    }

    @Nullable
    public Function<String, Object> getPreviewFunction() {
        return previewFunction;
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

    @Nullable
    public Predicate<String> getTypeChecker() {
        return typeChecker;
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
    public CommandArgument<SenderT> required(@NotNull Predicate<SenderT> predicate) {
        this.requiredPredicate = predicate;
        return this;
    }

    @NotNull
    public CommandArgument<SenderT> preview(boolean preview) {
        this.preview = preview;
        return this;
    }

    @NotNull
    public CommandArgument<SenderT> preview(@NotNull Function<String, Object> preview) {
        this.preview = true;
        this.previewFunction = preview;
        return this;
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
    public CommandArgument<SenderT> checkType(@NotNull Predicate<String> predicate) {
        this.typeChecker = predicate;
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
