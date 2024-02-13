package com.saicone.mcode.module.command;

import com.saicone.mcode.util.Dual;
import com.saicone.mcode.util.Strings;
import com.saicone.mcode.util.function.ThrowableFunction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class CommandSyntax {

    private final BiFunction<String, Integer, String[]> mapper;
    private final String delimiter;
    private final List<Argument> arguments = new ArrayList<>();

    private int minArguments = 0;
    private int maxArguments = -1;

    public CommandSyntax() {
        this(Strings::splitBySpaces);
    }

    public CommandSyntax(@NotNull BiFunction<String, Integer, String[]> mapper) {
        this(mapper, " ");
    }

    public CommandSyntax(@NotNull String delimiter) {
        this((s, limit) -> s.split(delimiter, limit), delimiter);
    }

    public CommandSyntax(@NotNull BiFunction<String, Integer, String[]> mapper, @NotNull String delimiter) {
        this.mapper = mapper;
        this.delimiter = delimiter;
    }

    @NotNull
    public BiFunction<String, Integer, String[]> getMapper() {
        return mapper;
    }

    public String getDelimiter() {
        return delimiter;
    }

    @NotNull
    public Argument getArgument(@NotNull String name) {
        for (Argument argument : arguments) {
            if (argument.name.equalsIgnoreCase(name)) {
                return argument;
            }
        }
        throw new IllegalArgumentException("The argument with name '" + name + "' doesn't exists");
    }

    @NotNull
    public Argument getArgument(@NotNull int index) {
        return arguments.get(index);
    }

    @NotNull
    public List<Argument> getArguments() {
        return arguments;
    }

    public int getMinArguments() {
        return minArguments;
    }

    public int getMaxArguments() {
        return maxArguments;
    }

    public void setMinArguments(int minArguments) {
        this.minArguments = minArguments;
    }

    public void setMaxArguments(int maxArguments) {
        this.maxArguments = maxArguments;
    }

    public void addArgument(@NotNull Argument... arguments) {
        this.arguments.addAll(Arrays.asList(arguments));
    }

    public void addArgument(int index, @NotNull Argument argument) {
        if (index < arguments.size()) {
            arguments.set(index, argument);
        }
        if (index < 1) {
            arguments.add(argument);
        }
        for (int i = 0; i < index; i++) {
            if (i >= arguments.size()) {
                arguments.add(null);
            }
        }
        arguments.add(argument);
    }

    @NotNull
    public List<Object> parse(@NotNull String s) throws IllegalArgumentException {
        // Must be a modifiable list
        final List<Object> list = new ArrayList<>();
        if (arguments.isEmpty()) {
            Collections.addAll(list, split(s));
        } else {
            final String[] split = split(s);
            for (int i = 0; i < split.length; i++) {
                final Argument argument = i < arguments.size() ? arguments.get(i) : null;
                if (argument == null) {
                    list.add(split[i]);
                } else {
                    list.add(Dual.of(argument.getName(), argument.parse(split[i])));
                }
            }
        }
        return list;
    }

    @NotNull
    public String[] split(String s) throws IllegalArgumentException {
        final String[] split = mapper.apply(s, 0);
        if (minArguments > split.length) {
            throw new IllegalArgumentException("The provided string doesn't have the minimum arguments");
        }

        if (maxArguments < 0) {
            return split;
        }

        final int max = maxArguments < 2 ? arguments.size() : Math.max(maxArguments, arguments.size());
        if (max >= split.length) {
            return split;
        }
        final int last = max - 1;
        final String[] result = new String[max];
        for (int i = 0; i < split.length; i++) {
            if (i >= last) {
                result[last] = result[last] + delimiter + split[i];
            } else {
                result[i] = split[i];
            }
        }
        return result;
    }

    public static class Argument {

        private final String name;
        private final boolean required;
        private int size = 1;
        // To use with join for mapper
        private String separator = " ";
        private String prefix = "";
        private String suffix = "";
        // Process String matcher
        private Predicate<String> matcher;
        private ThrowableFunction<String, Object> mapper;

        public Argument(@NotNull String name) {
            this(name, false);
        }

        public Argument(@NotNull String name, boolean required) {
            this.name = name;
            this.required = required;
        }

        @NotNull
        public String getName() {
            return name;
        }

        public int getSize() {
            return size;
        }

        public boolean isRequired() {
            return required;
        }

        @NotNull
        @Contract("_ -> this")
        public Argument size(@NotNull int size) {
            this.size = Math.max(1, size);
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public Argument separator(@NotNull String separator) {
            this.separator = separator;
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public Argument setPrefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public Argument setSuffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        @NotNull
        @Contract("_ -> this")
        public Argument map(@NotNull ThrowableFunction<String, Object> mapper) {
            this.mapper = mapper;
            return this;
        }

        @NotNull
        public Object parse(@NotNull String[] array, int start) throws IllegalArgumentException {
            if (size <= 1) {
                return parse(prefix + array[start] + suffix);
            }
            final StringJoiner joiner = new StringJoiner(separator, prefix, suffix);
            int count = 0;
            for (int i = start; i < array.length && count < size; i++) {
                joiner.add(array[i]);
                count++;
            }
            return parse(joiner.toString());
        }

        @NotNull
        public Object parse(@NotNull String s) throws IllegalArgumentException {
            try {
                return mapper == null ? s : mapper.apply(s);
            } catch (Throwable t) {
                throw new IllegalArgumentException("The text '" + s + "' cannot be parsed with '" + name + "' argument");
            }
        }
    }
}
