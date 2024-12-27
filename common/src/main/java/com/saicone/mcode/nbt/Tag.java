package com.saicone.mcode.nbt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tag<T> {

    public static final Tag<Object> END = new Tag<>(0, "END", "TAG_End") {
        @Override
        public @NotNull Object read(@NotNull DataInput input, @NotNull TagMapper<Object> mapper) throws IOException {
            return END;
        }

        @Override
        public void write(@NotNull DataOutput output, @NotNull Object object, @NotNull TagMapper<Object> mapper) throws IOException {
            // empty
        }
    };
    public static final Tag<Byte> BYTE = new Tag<>(1, "BYTE", "TAG_Byte", 'b') {
        @Override
        public @NotNull Byte read(@NotNull DataInput input, @NotNull TagMapper<Object> mapper) throws IOException {
            return input.readByte();
        }

        @Override
        public void write(@NotNull DataOutput output, @NotNull Byte object, @NotNull TagMapper<Object> mapper) throws IOException {
            output.writeByte(object);
        }
    };
    protected static final Tag<Boolean> BOOLEAN = new Tag<>(1, "BYTE", "TAG_Byte") {
        @Override
        public @NotNull Boolean read(@NotNull DataInput input, @NotNull TagMapper<Object> mapper) throws IOException {
            return input.readByte() == (byte) 1;
        }

        @Override
        public void write(@NotNull DataOutput output, @NotNull Boolean object, @NotNull TagMapper<Object> mapper) throws IOException {
            output.writeByte(object ? (byte) 1 : (byte) 0);
        }
    };
    public static final Tag<Short> SHORT = new Tag<>(2, "SHORT", "TAG_Short", 's') {
        @Override
        public @NotNull Short read(@NotNull DataInput input, @NotNull TagMapper<Object> mapper) throws IOException {
            return input.readShort();
        }

        @Override
        public void write(@NotNull DataOutput output, @NotNull Short object, @NotNull TagMapper<Object> mapper) throws IOException {
            output.writeShort(object);
        }
    };
    public static final Tag<Integer> INT = new Tag<>(3, "INT", "TAG_Int") {
        @Override
        public @NotNull Integer read(@NotNull DataInput input, @NotNull TagMapper<Object> mapper) throws IOException {
            return input.readInt();
        }

        @Override
        public void write(@NotNull DataOutput output, @NotNull Integer object, @NotNull TagMapper<Object> mapper) throws IOException {
            output.writeInt(object);
        }
    };
    public static final Tag<Long> LONG = new Tag<>(4, "LONG", "TAG_Long", 'l') {
        @Override
        public @NotNull Long read(@NotNull DataInput input, @NotNull TagMapper<Object> mapper) throws IOException {
            return input.readLong();
        }

        @Override
        public void write(@NotNull DataOutput output, @NotNull Long object, @NotNull TagMapper<Object> mapper) throws IOException {
            output.writeLong(object);
        }
    };
    public static final Tag<Float> FLOAT = new Tag<>(5, "FLOAT", "TAG_Float", 'f') {
        @Override
        public @NotNull Float read(@NotNull DataInput input, @NotNull TagMapper<Object> mapper) throws IOException {
            return input.readFloat();
        }

        @Override
        public void write(@NotNull DataOutput output, @NotNull Float object, @NotNull TagMapper<Object> mapper) throws IOException {
            output.writeFloat(object);
        }
    };
    public static final Tag<Double> DOUBLE = new Tag<>(6, "DOUBLE", "TAG_Double", 'd') {
        @Override
        public @NotNull Double read(@NotNull DataInput input, @NotNull TagMapper<Object> mapper) throws IOException {
            return input.readDouble();
        }

        @Override
        public void write(@NotNull DataOutput output, @NotNull Double object, @NotNull TagMapper<Object> mapper) throws IOException {
            output.writeDouble(object);
        }
    };
    public static final Tag<byte[]> BYTE_ARRAY = new Tag<>(7, "BYTE[]", "TAG_Byte_Array", 'B') {
        @Override
        public byte @NotNull [] read(@NotNull DataInput input, @NotNull TagMapper<Object> mapper) throws IOException {
            final int size = input.readInt();
            if (size >= 16L * 1024 * 1024) {
                throw new IllegalArgumentException("Cannot read array with more than 16MB of data");
            }
            final byte[] array = new byte[size];
            input.readFully(array);
            return array;
        }

        @Override
        public void write(@NotNull DataOutput output, byte @NotNull [] object, @NotNull TagMapper<Object> mapper) throws IOException {
            output.writeInt(object.length);
            output.write(object);
        }
    };
    public static final Tag<String> STRING = new Tag<>(8, "STRING", "TAG_String") {
        @Override
        public @NotNull String read(@NotNull DataInput input, @NotNull TagMapper<Object> mapper) throws IOException {
            return input.readUTF();
        }

        @Override
        public void write(@NotNull DataOutput output, @NotNull String object, @NotNull TagMapper<Object> mapper) throws IOException {
            output.writeUTF(object);
        }
    };
    public static final Tag<List<Object>> LIST = new Tag<>(9, "LIST", "TAG_List") {
        @Override
        public @NotNull List<Object> read(@NotNull DataInput input, @NotNull TagMapper<Object> mapper) throws IOException {
            final byte id = input.readByte();
            final int size = input.readInt();
            if (id == END.getId() && size > 0) {
                throw new IllegalArgumentException("Cannot read list without tag type");
            }

            final Tag<?> type = getType(id);
            final List<Object> list = new ArrayList<>(size);

            for (int i = 0; i < size; i++) {
                list.add(mapper.build(type, type.read(input)));
            }

            return list;
        }

        @Override
        public void write(@NotNull DataOutput output, @NotNull List<Object> object, @NotNull TagMapper<Object> mapper) throws IOException {
            final Tag<Object> type;
            if (object.isEmpty()) {
                type = END;
            } else {
                type = getType(object.get(0));
            }

            output.writeByte(type.getId());
            output.writeInt(object.size());

            for (Object element : object) {
                type.write(output, mapper.extract(element));
            }
        }
    };
    public static final Tag<Map<String, Object>> COMPOUND = new Tag<>(10, "COMPOUND", "TAG_Compound") {
        @Override
        public @NotNull Map<String, Object> read(@NotNull DataInput input, @NotNull TagMapper<Object> mapper) throws IOException {
            final Map<String, Object> map = new HashMap<>();

            byte id;
            while ((id = input.readByte()) != END.getId()) {
                final Tag<?> type = getType(id);

                final String key = input.readUTF();
                final Object value = type.read(input);
                map.put(key, mapper.build(type, value));
            }

            return map;
        }

        @Override
        public void write(@NotNull DataOutput output, @NotNull Map<String, Object> object, @NotNull TagMapper<Object> mapper) throws IOException {
            for (Map.Entry<String, Object> entry : object.entrySet()) {
                final Object value = mapper.extract(entry.getValue());
                final Tag<Object> type = getType(value);
                output.writeByte(type.getId());
                if (type != END) {
                    output.writeUTF(entry.getKey());
                    type.write(output, value);
                }
            }
            output.writeByte(END.getId());
        }
    };
    public static final Tag<int[]> INT_ARRAY = new Tag<>(11, "INT[]", "TAG_Int_Array", 'I') {
        @Override
        public int @NotNull [] read(@NotNull DataInput input, @NotNull TagMapper<Object> mapper) throws IOException {
            final int size = input.readInt();
            if (size >= 16L * 1024 * 1024) {
                throw new IllegalArgumentException("Cannot read array with more than 64MB of data");
            }
            final int[] array = new int[size];
            for (int i = 0; i < size; i++) {
                array[i] = input.readInt();
            }
            return array;
        }

        @Override
        public void write(@NotNull DataOutput output, int @NotNull [] object, @NotNull TagMapper<Object> mapper) throws IOException {
            output.writeInt(object.length);
            for (int i : object) {
                output.writeInt(i);
            }
        }
    };
    public static final Tag<long[]> LONG_ARRAY = new Tag<>(12, "LONG[]", "TAG_Long_Array", 'L') {
        @Override
        public long @NotNull [] read(@NotNull DataInput input, @NotNull TagMapper<Object> mapper) throws IOException {
            final int size = input.readInt();
            final long[] array = new long[size];
            for (int i = 0; i < size; i++) {
                array[i] = input.readLong();
            }
            return array;
        }

        @Override
        public void write(@NotNull DataOutput output, long @NotNull [] object, @NotNull TagMapper<Object> mapper) throws IOException {
            output.writeInt(object.length);
            for (long l : object) {
                output.writeLong(l);
            }
        }
    };

    private static final Tag<?>[] TYPES = new Tag<?>[] {
            END,
            BYTE,
            SHORT,
            INT,
            LONG,
            FLOAT,
            DOUBLE,
            BYTE_ARRAY,
            STRING,
            LIST,
            COMPOUND,
            INT_ARRAY,
            LONG_ARRAY
    };
    private static final Map<Class<?>, Tag<?>> TYPES_MAP = new HashMap<>();

    static {
        TYPES_MAP.put(Object.class, END);
        TYPES_MAP.put(byte.class, BYTE);
        TYPES_MAP.put(Byte.class, BYTE);
        TYPES_MAP.put(boolean.class, BOOLEAN);
        TYPES_MAP.put(Boolean.class, BOOLEAN);
        TYPES_MAP.put(short.class, SHORT);
        TYPES_MAP.put(Short.class, SHORT);
        TYPES_MAP.put(int.class, INT);
        TYPES_MAP.put(Integer.class, INT);
        TYPES_MAP.put(long.class, LONG);
        TYPES_MAP.put(Long.class, LONG);
        TYPES_MAP.put(float.class, FLOAT);
        TYPES_MAP.put(Float.class, FLOAT);
        TYPES_MAP.put(double.class, DOUBLE);
        TYPES_MAP.put(Double.class, DOUBLE);
        TYPES_MAP.put(byte[].class, BYTE_ARRAY);
        TYPES_MAP.put(String.class, STRING);
        TYPES_MAP.put(List.class, LIST);
        TYPES_MAP.put(Map.class, COMPOUND);
        TYPES_MAP.put(int[].class, INT_ARRAY);
        TYPES_MAP.put(long[].class, LONG_ARRAY);
    }

    private final byte id;
    private final String name;
    private final String prettyName;
    private final char suffix;

    Tag(int id, @NotNull String name, @NotNull String prettyName) {
        this(id, name, prettyName, '\0');
    }

    Tag(int id, @NotNull String name, @NotNull String prettyName, char suffix) {
        this.id = (byte) id;
        this.name = name;
        this.prettyName = prettyName;
        this.suffix = suffix;
    }

    public boolean isValid() {
        return true;
    }

    public boolean isPrimitive() {
        return id >= 1 && id <= 6;
    }

    public boolean isValue() {
        return isPrimitive() || id == 8;
    }

    public boolean isArray() {
        switch (id) {
            case 7:  // byte[]
            case 11: // int[]
            case 12: // long[]
                return true;
            default:
                return false;
        }
    }

    public byte getId() {
        return id;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getPrettyName() {
        return prettyName;
    }

    public char getSuffix() {
        return suffix;
    }

    @NotNull
    public String asString(@NotNull T t) {
        return String.valueOf(t) + getSuffix();
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public T read(@NotNull DataInput input) throws IOException {
        return (T) read(input, TagMapper.DEFAULT);
    }

    @NotNull
    public Object read(@NotNull DataInput input, @NotNull TagMapper<Object> mapper) throws IOException {
        throw new IllegalStateException("Cannot read data for tag " + getName());
    }

    public void write(@NotNull DataOutput output, @NotNull T object) throws IOException {
        write(output, object, TagMapper.DEFAULT);
    }

    public void write(@NotNull DataOutput output, @NotNull T object, @NotNull TagMapper<Object> mapper) throws IOException {
        throw new IllegalStateException("Cannot write data for tag " + getName());
    }

    @Override
    public final boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Tag)) return false;

        final Tag<?> tag = (Tag<?>) object;
        return getId() == tag.getId();
    }

    @Override
    public int hashCode() {
        return getId();
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static <T> Tag<T> getType(byte id) {
        if (id >= 0 && id < TYPES.length) {
            return (Tag<T>) TYPES[id];
        } else {
            return new Tag<>(-1, "INVALID[" + id + "]", "UNKNOWN_" + id) {
                @Override
                public boolean isValid() {
                    return false;
                }
            };
        }
    }

    @NotNull
    public static <T> Tag<T> getType(@Nullable Object object) {
        if (object instanceof Class) {
            return getType((Class<?>) object);
        } else if (object == null) {
            return getType(Object.class);
        } else {
            return getType(object.getClass());
        }
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static <T> Tag<T> getType(@NotNull Class<?> type) {
        Tag<?> result = TYPES_MAP.get(type);
        if (result == null) {
            if (List.class.isAssignableFrom(type)) {
                result = LIST;
            } else if (Map.class.isAssignableFrom(type)) {
                result = COMPOUND;
            } else {
                result = new Tag<>(-1, "INVALID(" + type.getName() + ")", "UNKNOWN_" + type.getSimpleName()) {
                    @Override
                    public boolean isValid() {
                        return false;
                    }
                };
            }
        }
        return (Tag<T>) result;
    }
}
