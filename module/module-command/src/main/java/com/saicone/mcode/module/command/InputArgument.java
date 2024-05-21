package com.saicone.mcode.module.command;

import com.saicone.types.TypeParser;
import com.saicone.types.Types;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InputArgument<SenderT, T> extends Argument<SenderT, T, InputArgument<SenderT, T>> {

    private final String name;
    private final ArgumentType type;
    private final TypeParser<T> typeParser;

    private boolean array;
    private T min;
    private T max;

    @NotNull
    public static <SenderT> InputArgument<SenderT, String> of(@NotNull String name) {
        return of(name, String.class);
    }

    @NotNull
    public static <SenderT, T> InputArgument<SenderT, T> of(@NotNull String name, @NotNull ArgumentType type) {
        if (!Types.contains(type)) {
            throw new IllegalArgumentException("There's no type parser for argument type " + type.name());
        }
        final InputArgument<SenderT, T> argument = of(name, type, Types.of(type));
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
        return of(name, null, typeParser);
    }

    @NotNull
    public static <SenderT, T> InputArgument<SenderT, T> of(@NotNull String name, @Nullable ArgumentType type, @NotNull TypeParser<T> typeParser) {
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
        return new InputArgument<SenderT, T>(s, type, typeParser).required(required).array(array);
    }

    public InputArgument(@NotNull String name, @Nullable ArgumentType type, @Nullable TypeParser<T> typeParser) {
        this.name = name;
        this.type = type;
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
    public ArgumentType getType() {
        return type;
    }

    @Nullable
    public TypeParser<T> getTypeParser() {
        return typeParser;
    }

    @Nullable
    public T getMin() {
        return min;
    }

    @Nullable
    public T getMax() {
        return max;
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

    @NotNull
    public InputArgument<SenderT, T> min(@Nullable T min) {
        this.min = min;
        return this;
    }

    @NotNull
    public InputArgument<SenderT, T> max(@Nullable T max) {
        this.max = max;
        return this;
    }

    @Override
    public String toString() {
        if (min != null) {
            if (max != null) {
                return min + ", " + max;
            }
            return name + "(" + min + ")";
        }
        return name;
    }
}
