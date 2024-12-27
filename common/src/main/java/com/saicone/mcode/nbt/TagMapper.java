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
            return object;
        }
    };

    @Nullable
    T build(@NotNull Tag<?> type, @Nullable Object object);

    @Nullable
    Object extract(@Nullable T t);
}
