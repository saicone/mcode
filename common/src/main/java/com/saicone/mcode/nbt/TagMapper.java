package com.saicone.mcode.nbt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TagMapper<T> {

    TagMapper<Object> DEFAULT = new TagMapper<>() {
        @Override
        public @Nullable Object build(@NotNull Tag<?> type, @Nullable Object object) {
            return object;
        }

        @Override
        public @Nullable Object extract(@Nullable Object object) {
            if (object instanceof Byte[]) {
                return byteArray(object);
            } else if (object instanceof Integer[]) {
                return intArray(object);
            } else if (object instanceof Long[]) {
                return longArray(object);
            }
            return object;
        }
    };

    @Nullable
    T build(@NotNull Tag<?> type, @Nullable Object object);

    @Nullable
    Object extract(@Nullable T t);

    default byte[] byteArray(@NotNull Object object) {
        if (object instanceof byte[]) {
            return (byte[]) object;
        } else if (object instanceof Byte[]) {
            final Byte[] from = (Byte[]) object;
            final byte[] array = new byte[from.length];
            for (int i = 0; i < from.length; i++) {
                array[i] = from[i];
            }
            return array;
        } else {
            throw new IllegalArgumentException("Invalid byte array: " + object);
        }
    }

    default int[] intArray(@NotNull Object object) {
        if (object instanceof int[]) {
            return (int[]) object;
        } else if (object instanceof Integer[]) {
            final Integer[] from = (Integer[]) object;
            final int[] array = new int[from.length];
            for (int i = 0; i < from.length; i++) {
                array[i] = from[i];
            }
            return array;
        } else {
            throw new IllegalArgumentException("Invalid byte array: " + object);
        }
    }

    default long[] longArray(@NotNull Object object) {
        if (object instanceof long[]) {
            return (long[]) object;
        } else if (object instanceof Long[]) {
            final Long[] from = (Long[]) object;
            final long[] array = new long[from.length];
            for (int i = 0; i < from.length; i++) {
                array[i] = from[i];
            }
            return array;
        } else {
            throw new IllegalArgumentException("Invalid byte array: " + object);
        }
    }
}
