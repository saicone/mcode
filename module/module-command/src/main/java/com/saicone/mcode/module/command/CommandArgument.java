package com.saicone.mcode.module.command;

import com.saicone.types.TypeParser;
import com.saicone.types.Types;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class CommandArgument<SenderT> {

    private final String name;
    private final boolean required;
    private boolean array;

    private Predicate<SenderT> requiredPredicate;
    private int size = 1;
    private ArgumentType type;
    private TypeParser<?> typeParser;
    private Predicate<String> typeChecker;
    private Function<String, String> mapperFunction;
    private CommandSuggestion<SenderT> suggestion;

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

    @Nullable
    public Object parse(@NotNull String s) {
        final String arg;
        if (mapperFunction != null) {
            arg = mapperFunction.apply(s);
        } else {
            arg = s;
        }
        if (typeChecker != null && !typeChecker.test(arg)) {
            return null;
        }
        if (typeParser != null) {
            return typeParser.parse(arg);
        }
        return null;
    }

    @NotNull
    public String compile(@NotNull String s) {
        if (mapperFunction != null) {
            return mapperFunction.apply(s);
        } else {
            return s;
        }
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

    @NotNull
    public String getName() {
        return name;
    }

    @Nullable
    public Predicate<SenderT> getRequiredPredicate() {
        return requiredPredicate;
    }

    public int getSize() {
        return size;
    }

    @Nullable
    public ArgumentType getType() {
        return type;
    }

    @Nullable
    public TypeParser<?> getTypeParser() {
        return typeParser;
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
    public CommandArgument<SenderT> required(@NotNull Predicate<SenderT> predicate) {
        this.requiredPredicate = predicate;
        return this;
    }

    @NotNull
    public CommandArgument<SenderT> size(int size) {
        this.size = size;
        return this;
    }

    @NotNull
    public CommandArgument<SenderT> type(@NotNull ArgumentType type) {
        if (type.getType() == null && !Types.contains(type)) {
            throw new IllegalArgumentException("There's no type parser for argument type " + type.name());
        }
        if (type == ArgumentType.GREEDY_STRING) {
            this.array = true;
        }
        this.type = type;
        this.typeParser = Types.of(type.getType() != null ? type.getType() : type);
        return this;
    }

    @NotNull
    public CommandArgument<SenderT> type(@NotNull Class<?> type) {
        if (!Types.contains(type)) {
            throw new IllegalArgumentException("There's no type parser for class " + type.getName() + ", consider adding your own parser");
        }
        this.type = ArgumentType.of(type);
        this.typeParser = Types.of(type);
        return this;
    }

    @NotNull
    public CommandArgument<SenderT> type(@NotNull TypeParser<?> typeParser) {
        this.typeParser = typeParser;
        return this;
    }

    @NotNull
    public CommandArgument<SenderT> checkType(@NotNull Predicate<String> predicate) {
        this.typeChecker = predicate;
        return this;
    }

    @NotNull
    public CommandArgument<SenderT> mapper(@NotNull Function<String, String> function) {
        this.mapperFunction = function;
        return this;
    }

    @NotNull
    public CommandArgument<SenderT> suggests(@NotNull List<String> list) {
        return suggests(CommandSuggestion.of(list));
    }

    @NotNull
    public CommandArgument<SenderT> suggests(@NotNull CommandSuggestion<SenderT> suggestion) {
        this.suggestion = suggestion;
        return this;
    }
}
