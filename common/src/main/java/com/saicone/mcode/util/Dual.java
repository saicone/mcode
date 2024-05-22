package com.saicone.mcode.util;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Dual<A, B> {

    private A left;
    private B right;

    @NotNull
    public static <A, B> Dual<A, B> of(@NotNull Map.Entry<A, B> entry) {
        return new Dual<>(entry.getKey(), entry.getValue());
    }

    @NotNull
    public static <A, B> Dual<A, B> of(@Nullable A a, @Nullable B b) {
        return new Dual<>(a, b);
    }

    @NotNull
    public static <T, A, B> Dual<A, B> of(@NotNull T t, @NotNull Function<T, A> a, @NotNull Function<T, B> b) {
        return new Dual<>() {
            @Override
            public A getLeft() {
                return super.getLeft() != null ? super.getLeft() : a.apply(t);
            }

            @Override
            public B getRight() {
                return super.getRight() != null ? super.getRight() : b.apply(t);
            }
        };
    }

    @NotNull
    public static Dual<String, String> ofSplit(@Nullable String s, @Language(value = "RegExp") @NotNull String regex) {
        if (s == null || s.isBlank()) {
            return new Dual<>();
        }
        final String[] split = s.split(regex, 2);
        if (split.length < 2) {
            return new Dual<>(s.equals("null") ? null : s, null);
        } else {
            return new Dual<>(split[0].equals("null") ? null : split[0], split[1].equals("null") ? null : split[1]);
        }
    }

    public Dual() {
        this(null, null);
    }

    public Dual(@Nullable A left, @Nullable B right) {
        this.left = left;
        this.right = right;
    }

    public A getLeft() {
        return left;
    }

    public Object getLeftOrRight() {
        return getLeft() != null ? getLeft() : getRight();
    }

    public B getRight() {
        return right;
    }

    public Object getRightOrLeft() {
        return getRight() != null ? getRight() : getLeft();
    }

    public void setLeft(@Nullable A left) {
        this.left = left;
    }

    public void setRight(@Nullable B right) {
        this.right = right;
    }

    public boolean isEmpty() {
        return getLeft() == null && getRight() == null;
    }

    @Override
    public String toString() {
        return "Dual{" +
                "left=" + getLeft() +
                ", right=" + getRight() +
                '}';
    }

    @NotNull
    public String join() {
        return join(":");
    }

    @NotNull
    public String join(@NotNull String delimiter) {
        return getLeft() + delimiter + getRight();
    }

    public <R> R join(@NotNull BiFunction<A, B, R> consumer) {
        return consumer.apply(getLeft(), getRight());
    }

    @NotNull
    public Dual<A, B> copy() {
        return new Dual<>(getLeft(), getRight());
    }

    @NotNull
    public <L, R> Dual<L, R> by(@NotNull Function<A, L> leftFunction, @NotNull Function<B, R> rightFunction) {
        return new Dual<>(leftFunction.apply(getLeft()), rightFunction.apply(getRight()));
    }

    @NotNull
    public <L> Dual<L, B> byLeft(@NotNull Function<A, L> leftFunction) {
        return new Dual<>(leftFunction.apply(getLeft()), getRight());
    }

    @NotNull
    public <R> Dual<A, R> byRight(@NotNull Function<B, R> rightFunction) {
        return new Dual<>(getLeft(), rightFunction.apply(getRight()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return Objects.equals(getLeft(), o) || Objects.equals(getRight(), o);
        }

        return equals((Dual<?, ?>) o);
    }

    public boolean equals(@NotNull Dual<?, ?> dual) {
        return Objects.equals(getLeft(), dual.getLeft()) && Objects.equals(getRight(), dual.getRight());
    }

    @Override
    public int hashCode() {
        int result = getLeft() != null ? getLeft().hashCode() : 0;
        result = 31 * result + (getRight() != null ? getRight().hashCode() : 0);
        return result;
    }
}
