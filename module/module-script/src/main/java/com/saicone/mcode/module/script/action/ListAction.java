package com.saicone.mcode.module.script.action;

import com.saicone.mcode.module.script.Action;
import com.saicone.mcode.util.function.ThrowableFunction;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ListAction<E> extends Action {

    private final List<E> list;

    public ListAction(@NotNull List<E> list) {
        this.list = list;
    }

    @NotNull
    public List<E> getList() {
        return list;
    }

    public static <E> Builder<E> builder(@NotNull @Language("RegExp") String regex, @NotNull ThrowableFunction<Object, E> mapper) {
        return builder(regex, s -> s.split("\n"), mapper);
    }

    public static <E> Builder<E> builder(@NotNull @Language("RegExp") String regex, @NotNull ThrowableFunction<String, String[]> splitFunction, @NotNull ThrowableFunction<Object, E> mapper) {
        return new Builder<>(regex, splitFunction, mapper);
    }

    public static class Builder<E> extends Action.Builder<ListAction<E>> {

        protected final ThrowableFunction<String, String[]> splitFunction;
        protected final ThrowableFunction<Object, E> mapper;

        public Builder(@NotNull @Language("RegExp") String regex, @NotNull ThrowableFunction<String, String[]> splitFunction, @NotNull ThrowableFunction<Object, E> mapper) {
            super(regex);
            this.splitFunction = splitFunction;
            this.mapper = mapper;
        }

        @NotNull
        public ThrowableFunction<Object, E> getMapper() {
            return mapper;
        }

        @Override
        public @Nullable ListAction<E> build(@NotNull List<Object> list) {
            final List<E> eList = new ArrayList<>();
            boolean fail = false;
            for (Object o : list) {
                try {
                    eList.add(mapper.apply(o));
                } catch (Throwable t) {
                    fail = true;
                }
            }
            if (fail && eList.isEmpty()) {
                return super.build(list);
            }
            return new ListAction<>(eList);
        }

        @Override
        public @Nullable ListAction<E> build(@NotNull String text) {
            final String[] split;
            try {
                split = splitFunction.apply(text);
            } catch (Throwable t) {
                return super.build(text);
            }
            final List<E> eList = new ArrayList<>();
            boolean fail = false;
            for (String s : split) {
                try {
                    eList.add(mapper.apply(s));
                } catch (Throwable t) {
                    fail = true;
                }
            }
            if (fail && eList.isEmpty()) {
                return super.build(text);
            }
            return new ListAction<>(eList);
        }
    }
}
