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

    @Nullable
    public A getLeft() {
        return left;
    }

    @Nullable
    public B getRight() {
        return right;
    }

    public void setLeft(@Nullable A left) {
        this.left = left;
    }

    public void setRight(@Nullable B right) {
        this.right = right;
    }

    public boolean isEmpty() {
        return left == null && right == null;
    }

    @Override
    public String toString() {
        return "Dual{" +
                "left=" + left +
                ", right=" + right +
                '}';
    }

    @NotNull
    public String join() {
        return join(":");
    }

    @NotNull
    public String join(@NotNull String delimiter) {
        return left + delimiter + right;
    }

    public <R> R join(@NotNull BiFunction<A, B, R> consumer) {
        return consumer.apply(left, right);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return Objects.equals(left, o) || Objects.equals(right, o);
        }

        return equals((Dual<?, ?>) o);
    }

    public boolean equals(@NotNull Dual<?, ?> dual) {
        return Objects.equals(left, dual.left) && Objects.equals(right, dual.right);
    }

    @Override
    public int hashCode() {
        int result = left != null ? left.hashCode() : 0;
        result = 31 * result + (right != null ? right.hashCode() : 0);
        return result;
    }

    @NotNull
    public Dual<A, B> copy() {
        return new Dual<>(left, right);
    }

    @NotNull
    public <L, R> Dual<L, R> by(@NotNull Function<A, L> leftFunction, @NotNull Function<B, R> rightFunction) {
        return new Dual<>(leftFunction.apply(left), rightFunction.apply(right));
    }

    @NotNull
    public <L> Dual<L, B> byLeft(@NotNull Function<A, L> leftFunction) {
        return new Dual<>(leftFunction.apply(left), right);
    }

    @NotNull
    public <R> Dual<A, R> byRight(@NotNull Function<B, R> rightFunction) {
        return new Dual<>(left, rightFunction.apply(right));
    }
}
