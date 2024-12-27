package com.saicone.mcode.nbt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UTFDataFormatException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class TagIo<T> {

    private static final StandardOpenOption[] DEFAULT_OPEN_OPTIONS = new StandardOpenOption[] {
            StandardOpenOption.SYNC,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
    };

    @NotNull
    public static TagIo<Object> unnamed() {
        return Unnamed.INSTANCE;
    }

    @NotNull
    public static TagIo<Object> any() {
        return Any.INSTANCE;
    }

    @NotNull
    public static TagIo<Object> fallback() {
        return Fallback.INSTANCE;
    }

    private final TagMapper<T> mapper;

    public TagIo(@NotNull TagMapper<T> mapper) {
        this.mapper = mapper;
    }

    @NotNull
    public TagMapper<T> getMapper() {
        return mapper;
    }

    /**
     * Check if the provided file is a GZIP file.
     *
     * @param file the file to check.
     * @return     true if the file is GZIP formatted, false otherwise.
     * @throws IOException if an I/O error occurs.
     */
    public static boolean isGzipFormat(@NotNull File file) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            int ID1 = raf.read();
            if (ID1 <= -1 || ID1 > 255) {
                return false;
            }
            int ID2 = raf.read();
            if (ID2 <= -1 || ID2 > 255) {
                return false;
            }
            return isGzipHeader(ID1, ID2);
        }
    }

    /**
     * Check if the provided InputStream is a GZIP formatted input.
     *
     * @param input the InputStream to check.
     * @return      true if the input stream is GZIP formatted, false otherwise.
     * @throws IOException if an I/O error occurs.
     */
    public static boolean isGzipFormat(@NotNull InputStream input) throws IOException {
        final InputStream in;
        if (input.markSupported()) {
            in = input;
        } else {
            in = new BufferedInputStream(input);
        }
        in.mark(2);

        int ID1 = in.read();
        if (ID1 <= -1 || ID1 > 255) {
            in.reset();
            return false;
        }
        int ID2 = in.read();
        if (ID2 <= -1 || ID2 > 255) {
            in.reset();
            return false;
        }
        in.reset();

        return isGzipHeader(ID1, ID2);
    }

    /**
     * Check if the provided byte array has GZIP header.
     *
     * @param bytes the byte array to check.
     * @return      true if it contains GZIP header.
     */
    public static boolean isGzipHeader(byte[] bytes) {
        return bytes.length > 1 && isGzipHeader(bytes[0] & 0xff, bytes[1] & 0xff);
    }

    /**
     * Check if the provided ID1 and ID2 are the same has GZIP magic.
     *
     * @param ID1 the first ID.
     * @param ID2 the second ID.
     * @return    true if the IDs are the same has GZIP magic.
     */
    public static boolean isGzipHeader(int ID1, int ID2) {
        return ((ID2 << 8) | ID1) == GZIPInputStream.GZIP_MAGIC;
    }

    /**
     * Check if the provided byte array has NBT header.
     *
     * @param bytes the byte array to check.
     * @return      true of it contains NBT header.
     */
    public boolean isNbtHeader(byte[] bytes) {
        return bytes.length > 2 && Tag.getType(bytes[0]).isValid() && bytes[1] == 0 && bytes[2] == 0;
    }

    @NotNull
    public DataInputStream createDataInput(@NotNull InputStream input) throws IOException {
        return createDataInput(input, isGzipFormat(input));
    }

    @NotNull
    public DataInputStream createDataInput(@NotNull InputStream input, boolean compressed) throws IOException {
        final InputStream in = compressed ? new GZIPInputStream(input) : input;
        return new DataInputStream(new BufferedInputStream(in));
    }

    @NotNull
    public DataOutputStream createDataOutput(@NotNull OutputStream output) throws IOException {
        return createDataOutput(output, true);
    }

    @NotNull
    public DataOutputStream createDataOutput(@NotNull OutputStream output, boolean compressed) throws IOException {
        final OutputStream out = compressed ? new GZIPOutputStream(output) : output;
        return new DataOutputStream(new BufferedOutputStream(out));
    }

    /**
     * Read NBT from file.
     *
     * @param file the file to read.
     * @return     an object instance.
     * @throws IOException if an I/O error occurs while reading.
     */
    @Nullable
    public T read(@NotNull File file) throws IOException {
        if (!file.exists()) {
            return null;
        }
        try (DataInputStream in = createDataInput(new FileInputStream(file), isGzipFormat(file))) {
            return read((DataInput) in);
        }
    }

    /**
     * Read NBT from file.
     *
     * @param path the file to read.
     * @return     an object instance.
     * @throws IOException if an I/O error occurs while reading.
     */
    @Nullable
    public T read(@NotNull Path path) throws IOException {
        if (!Files.exists(path)) {
            return null;
        }
        try (DataInputStream in = createDataInput(Files.newInputStream(path), isGzipFormat(path.toFile()))) {
            return read((DataInput) in);
        }
    }

    /**
     * Read NBT from byte array.
     *
     * @param bytes the byte array to read.
     * @return      an object instance.
     * @throws IOException if an I/O error occurs while reading.
     */
    public T read(byte[] bytes) throws IOException {
        try (DataInputStream in = createDataInput(new ByteArrayInputStream(bytes), isGzipHeader(bytes))) {
            return read((DataInput) in);
        }
    }

    /**
     * Read NBT from InputStream.
     *
     * @param input the InputStream to read.
     * @return      an object instance.
     * @throws IOException if an I/O error occurs while reading.
     */
    public T read(@NotNull InputStream input) throws IOException {
        try (DataInputStream in = createDataInput(input)) {
            return read((DataInput) in);
        }
    }

    /**
     * Read NBT from DataInputStream.
     *
     * @param input the DataInputStream to read.
     * @return      an object instance.
     * @throws IOException if an I/O error occurs while reading.
     */
    public T read(@NotNull DataInputStream input) throws IOException {
        return read((DataInput) input);
    }

    /**
     * Read NBT from DataInput.
     *
     * @param input the DataInput to read.
     * @return      an object instance.
     * @throws IOException if an I/O error occurs while reading.
     */
    @SuppressWarnings("unchecked")
    public T read(@NotNull DataInput input) throws IOException {
        final byte id = input.readByte();
        final Tag<Object> type = Tag.getType(id);
        if (type == Tag.END) {
            return getMapper().build(type, null);
        }
        input.skipBytes(input.readUnsignedShort());
        return (T) type.read(input, (TagMapper<Object>) getMapper());
    }

    /**
     * Write NBT to file.
     *
     * @param tag  the tag to write.
     * @param file file to write in.
     * @throws IOException if an I/O error occurs while writing.
     */
    public void write(@Nullable T tag, @NotNull File file, boolean compressed) throws IOException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            write(tag, out, compressed);
            out.getFD().sync();
        }
    }

    /**
     * Write NBT to file.
     *
     * @param tag  the tag to write.
     * @param file file to write in.
     * @throws IOException if an I/O error occurs while writing.
     */
    public void write(@Nullable T tag, @NotNull Path file, boolean compressed) throws IOException {
        try (OutputStream out = Files.newOutputStream(file, DEFAULT_OPEN_OPTIONS)) {
            write(tag, out, compressed);
        }
    }

    /**
     * Write NBT to OutputStream.
     *
     * @param tag    the tag to write.
     * @param output OutputStream to write in.
     * @throws IOException if an I/O error occurs while writing.
     */
    public void write(@Nullable T tag, @NotNull OutputStream output, boolean compressed) throws IOException {
        try (DataOutputStream out = createDataOutput(output, compressed)) {
            write(tag, (DataOutput) out);
        }
    }

    /**
     * Write NBT to DataOutputStream.
     *
     * @param tag    the tag to write.
     * @param output DataOutputStream to write in.
     * @throws IOException if an I/O error occurs while writing.
     */
    public void write(@Nullable T tag, @NotNull DataOutputStream output) throws IOException {
        write(tag, (DataOutput) output);
    }

    /**
     * Write NBT to DataOutput.
     *
     * @param tag    the tag to write.
     * @param output DataOutput to write in.
     * @throws IOException if an I/O error occurs while writing.
     */
    @SuppressWarnings("unchecked")
    public void write(@Nullable T tag, @NotNull DataOutput output) throws IOException {
        final Object value = tag == null ? null : getMapper().extract(tag);
        final Tag<Object> type = Tag.getType(value);
        output.writeByte(type.getId());
        if (type == Tag.END) {
            return;
        }
        output.writeUTF("");
        type.write(output, value, (TagMapper<Object>) getMapper());
    }

    public static class Unnamed<T> extends TagIo<T> {

        public static final Unnamed<Object> INSTANCE = new Unnamed<>(TagMapper.DEFAULT);

        public Unnamed(@NotNull TagMapper<T> mapper) {
            super(mapper);
        }
    }

    public static class Any<T> extends TagIo<T> {

        public static final Any<Object> INSTANCE = new Any<>(TagMapper.DEFAULT);

        public Any(@NotNull TagMapper<T> mapper) {
            super(mapper);
        }

        @Override
        public boolean isNbtHeader(byte[] bytes) {
            return bytes.length > 0 && Tag.getType(bytes[0]).isValid();
        }

        @Override
        @SuppressWarnings("unchecked")
        public T read(@NotNull DataInput input) throws IOException {
            final byte id = input.readByte();
            final Tag<Object> type = Tag.getType(id);
            if (type == Tag.END) {
                return getMapper().build(type, null);
            }
            return (T) type.read(input, (TagMapper<Object>) getMapper());
        }

        @Override
        @SuppressWarnings("unchecked")
        public void write(@Nullable T tag, @NotNull DataOutput output) throws IOException {
            final Object value = tag == null ? null : getMapper().extract(tag);
            final Tag<Object> type = Tag.getType(value);
            output.writeByte(type.getId());
            if (type == Tag.END) {
                return;
            }
            type.write(output, value, (TagMapper<Object>) getMapper());
        }
    }

    public static class Fallback<T> extends TagIo<T> {

        public static final Fallback<Object> INSTANCE = new Fallback<>(TagMapper.DEFAULT);

        public Fallback(@NotNull TagMapper<T> mapper) {
            super(mapper);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void write(@Nullable T tag, @NotNull DataOutput output) throws IOException {
            final Object value = tag == null ? null : getMapper().extract(tag);
            final Tag<Object> type = Tag.getType(value);
            output.writeByte(type.getId());
            if (type == Tag.END) {
                return;
            }
            output.writeUTF("");
            type.write(new FallbackDataOutput(output), value, (TagMapper<Object>) getMapper());
        }
    }

    public static class FallbackDataOutput implements DataOutput {

        private final DataOutput output;

        public FallbackDataOutput(@NotNull DataOutput output) {
            this.output = output;
        }

        @Override
        public void write(int b) throws IOException {
            this.output.write(b);
        }

        @Override
        public void write(@NotNull byte[] b) throws IOException {
            this.output.write(b);
        }

        @Override
        public void write(@NotNull byte[] b, int off, int len) throws IOException {
            this.output.write(b, off, len);
        }

        @Override
        public void writeBoolean(boolean v) throws IOException {
            this.output.writeBoolean(v);
        }

        @Override
        public void writeByte(int v) throws IOException {
            this.output.writeByte(v);
        }

        @Override
        public void writeShort(int v) throws IOException {
            this.output.writeShort(v);
        }

        @Override
        public void writeChar(int v) throws IOException {
            this.output.writeChar(v);
        }

        @Override
        public void writeInt(int v) throws IOException {
            this.output.writeInt(v);
        }

        @Override
        public void writeLong(long v) throws IOException {
            this.output.writeLong(v);
        }

        @Override
        public void writeFloat(float v) throws IOException {
            this.output.writeFloat(v);
        }

        @Override
        public void writeDouble(double v) throws IOException {
            this.output.writeDouble(v);
        }

        @Override
        public void writeBytes(@NotNull String s) throws IOException {
            try {
                this.output.writeBytes(s);
            } catch (UTFDataFormatException e) {
                this.output.writeBytes("");
            }
        }

        @Override
        public void writeChars(@NotNull String s) throws IOException {
            try {
                this.output.writeChars(s);
            } catch (UTFDataFormatException e) {
                this.output.writeChars("");
            }
        }

        @Override
        public void writeUTF(@NotNull String s) throws IOException {
            try {
                this.output.writeUTF(s);
            } catch (UTFDataFormatException e) {
                this.output.writeUTF("");
            }
        }
    }
}
