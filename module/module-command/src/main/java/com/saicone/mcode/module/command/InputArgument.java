package com.saicone.mcode.module.command;

import com.saicone.types.TypeParser;
import com.saicone.types.Types;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InputArgument<SenderT, T> extends Argument<SenderT, T, InputArgument<SenderT, T>> {

    private final String name;
    private final TypeParser<T> typeParser;

    private boolean array;
    @NotNull
    public static <SenderT> InputArgument<SenderT, String> of(@NotNull String name) {
        return of(name, String.class);
    }

    @NotNull
    public static <SenderT> InputArgument<SenderT, ?> of(@NotNull String name, @NotNull ArgumentType type) {
        if (!Types.contains(type)) {
            throw new IllegalArgumentException("There's no type parser for argument type " + type.name());
        }
        final InputArgument<SenderT, ?> argument = of(name, Types.of(type));
        if (type == ArgumentType.GREEDY_STRING) {
            argument.array = true;
        }
        return argument;
    }

    @NotNull
    public static <SenderT, T> InputArgument<SenderT, T> of(@NotNull String name, @NotNull Class<T> type) {
        if (!Types.contains(type)) {
            throw new IllegalArgumentException("There's no type parser for class " + type.getName() + ", consider adding your own parser");
        }
        return of(name, Types.of(type));
    }

    @NotNull
    public static <SenderT, T> InputArgument<SenderT, T> of(@NotNull String name, @NotNull TypeParser<T> typeParser) {
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
        return new InputArgument<SenderT, T>(s, typeParser).required(required).array(array);
    }

    public InputArgument(@NotNull String name, @Nullable TypeParser<T> typeParser) {
        this.name = name;
        this.typeParser = typeParser;
    }

    public boolean isArray() {
        return array;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Nullable
    public TypeParser<T> getTypeParser() {
        return typeParser;
    }

    @Override
    protected @NotNull InputArgument<SenderT, T> argument() {
        return this;
    }

    @Override
    public @Nullable T parse(@NotNull String s) {
        if (typeParser != null) {
            return typeParser.parse(s);
        }
        return null;
    }

    @NotNull
    public InputArgument<SenderT, T> array(boolean array) {
        this.array = array;
        return this;
    }
}
