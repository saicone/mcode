package com.saicone.mcode.nbt.io;

import com.saicone.mcode.nbt.Tag;
import com.saicone.mcode.nbt.TagMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Writer;

public class TagWriter<T> extends Writer {

    private final Writer writer;
    private final TagMapper<T> mapper;

    @NotNull
    public static TagWriter<Object> of(@NotNull Writer writer) {
        return of(writer, TagMapper.DEFAULT);
    }

    @NotNull
    public static <T> TagWriter<T> of(@NotNull Writer writer, @NotNull TagMapper<T> mapper) {
        return new TagWriter<>(writer, mapper);
    }

    public TagWriter(@NotNull Writer writer, @NotNull TagMapper<T> mapper) {
        this.writer = writer;
        this.mapper = mapper;
    }

    @NotNull
    public Writer getWriter() {
        return writer;
    }

    @NotNull
    public TagMapper<T> getMapper() {
        return mapper;
    }

    @SuppressWarnings("unchecked")
    public void writeTag(@Nullable T t) throws IOException {
        if (t == null) {
            return;
        }
        final Object value = mapper.extract(t);
        if (value == null) {
            return;
        }
        final Tag<Object> type = Tag.getType(value);
        if (!type.isValid()) {
            throw new IOException("Cannot write invalid tag: " + type.getName());
        }
        writer.write(type.snbt(value, (TagMapper<Object>) mapper));
    }

    @Override
    public void write(@NotNull char[] cbuf, int off, int len) throws IOException {
        writer.write(cbuf, off, len);
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
