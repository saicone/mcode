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
import java.util.StringJoiner;

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
        public void write(@NotNull DataOutput output, @NotNull Byte b, @NotNull TagMapper<Object> mapper) throws IOException {
            output.writeByte(b);
        }
    };
    public static final Tag<Boolean> BOOLEAN = new Tag<>(1, "BYTE", "TAG_Byte") {
        @Override
        public @NotNull Boolean read(@NotNull DataInput input, @NotNull TagMapper<Object> mapper) throws IOException {
            return input.readByte() == (byte) 1;
        }

        @Override
        public void write(@NotNull DataOutput output, @NotNull Boolean b, @NotNull TagMapper<Object> mapper) throws IOException {
            output.writeByte(b ? (byte) 1 : (byte) 0);
        }
    };
    public static final Tag<Short> SHORT = new Tag<>(2, "SHORT", "TAG_Short", 's') {
        @Override
        public @NotNull Short read(@NotNull DataInput input, @NotNull TagMapper<Object> mapper) throws IOException {
            return input.readShort();
        }

        @Override
        public void write(@NotNull DataOutput output, @NotNull Short s, @NotNull TagMapper<Object> mapper) throws IOException {
            output.writeShort(s);
        }
    };
    public static final Tag<Integer> INT = new Tag<>(3, "INT", "TAG_Int") {
        @Override
        public @NotNull Integer read(@NotNull DataInput input, @NotNull TagMapper<Object> mapper) throws IOException {
            return input.readInt();
        }

        @Override
        public void write(@NotNull DataOutput output, @NotNull Integer i, @NotNull TagMapper<Object> mapper) throws IOException {
            output.writeInt(i);
        }
    };
    public static final Tag<Long> LONG = new Tag<>(4, "LONG", "TAG_Long", 'l') {
        @Override
        public @NotNull Long read(@NotNull DataInput input, @NotNull TagMapper<Object> mapper) throws IOException {
            return input.readLong();
        }

        @Override
        public void write(@NotNull DataOutput output, @NotNull Long l, @NotNull TagMapper<Object> mapper) throws IOException {
            output.writeLong(l);
        }
    };
    public static final Tag<Float> FLOAT = new Tag<>(5, "FLOAT", "TAG_Float", 'f') {
        @Override
        public @NotNull Float read(@NotNull DataInput input, @NotNull TagMapper<Object> mapper) throws IOException {
            return input.readFloat();
        }

        @Override
        public void write(@NotNull DataOutput output, @NotNull Float f, @NotNull TagMapper<Object> mapper) throws IOException {
            output.writeFloat(f);
        }
    };
    public static final Tag<Double> DOUBLE = new Tag<>(6, "DOUBLE", "TAG_Double", 'd') {
        @Override
        public @NotNull String snbt(@NotNull Double d, TagMapper<Object> mapper) {
            return String.valueOf(d);
        }

        @Override
        public @NotNull Double read(@NotNull DataInput input, @NotNull TagMapper<Object> mapper) throws IOException {
            return input.readDouble();
        }

        @Override
        public void write(@NotNull DataOutput output, @NotNull Double d, @NotNull TagMapper<Object> mapper) throws IOException {
            output.writeDouble(d);
        }
    };
    public static final Tag<byte[]> BYTE_ARRAY = new Tag<>(7, "BYTE[]", "TAG_Byte_Array", 'B') {
        @Override
        public @NotNull String snbt(byte @NotNull [] bytes, TagMapper<Object> mapper) {
            final StringJoiner joiner = new StringJoiner(",", '[' + getSuffix() + ";", "]");
            for (byte b : bytes) {
                joiner.add(String.valueOf(b) + getSuffix());
            }
            return joiner.toString();
        }

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
        public void write(@NotNull DataOutput output, byte @NotNull [] bytes, @NotNull TagMapper<Object> mapper) throws IOException {
            output.writeInt(bytes.length);
            output.write(bytes);
        }
    };
    public static final Tag<boolean[]> BOOLEAN_ARRAY = new Tag<>(7, "BYTE[]", "TAG_Byte_Array", 'B') {
        @Override
        public @NotNull String snbt(boolean @NotNull [] booleans, TagMapper<Object> mapper) {
            final StringJoiner joiner = new StringJoiner(",", '[' + getSuffix() + ";", "]");
            for (boolean b : booleans) {
                joiner.add(String.valueOf(b));
            }
            return joiner.toString();
        }

        @Override
        public boolean @NotNull [] read(@NotNull DataInput input, @NotNull TagMapper<Object> mapper) throws IOException {
            final int size = input.readInt();
            if (size >= 16L * 1024 * 1024) {
                throw new IllegalArgumentException("Cannot read array with more than 16MB of data");
            }
            final byte[] array = new byte[size];
            input.readFully(array);
            return mapper.booleanArray(array);
        }

        @Override
        public void write(@NotNull DataOutput output, boolean @NotNull [] booleans, @NotNull TagMapper<Object> mapper) throws IOException {
            output.writeInt(booleans.length);
            output.write(mapper.byteArray(booleans));
        }
    };
    public static final Tag<String> STRING = new Tag<>(8, "STRING", "TAG_String") {
        @Override
        public @NotNull String snbt(@NotNull String s, TagMapper<Object> mapper) {
            return '"' + s.replace("\"", "\\\"") + '"';
        }

        @Override
        public @NotNull String read(@NotNull DataInput input, @NotNull TagMapper<Object> mapper) throws IOException {
            return input.readUTF();
        }

        @Override
        public void write(@NotNull DataOutput output, @NotNull String s, @NotNull TagMapper<Object> mapper) throws IOException {
            output.writeUTF(s);
        }
    };
    public static final Tag<List<Object>> LIST = new Tag<>(9, "LIST", "TAG_List") {
        @Override
        public @NotNull String snbt(@NotNull List<Object> objects, TagMapper<Object> mapper) {
            final StringJoiner joiner = new StringJoiner(",", "[", "]");
            for (Object object : objects) {
                final Object value = mapper.extract(object);
                final Tag<Object> type = getType(value);
                joiner.add(type.snbt(value, mapper));
            }
            return joiner.toString();
        }

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
                list.add(mapper.build(type, type.read(input, mapper)));
            }

            return list;
        }

        @Override
        public void write(@NotNull DataOutput output, @NotNull List<Object> list, @NotNull TagMapper<Object> mapper) throws IOException {
            final Tag<Object> type;
            if (list.isEmpty()) {
                type = END;
            } else {
                type = getType(mapper.extract(list.get(0)));
            }

            output.writeByte(type.getId());
            output.writeInt(list.size());

            for (Object object : list) {
                type.write(output, mapper.extract(object), mapper);
            }
        }
    };
    public static final Tag<Map<String, Object>> COMPOUND = new Tag<>(10, "COMPOUND", "TAG_Compound") {
        @Override
        public @NotNull String snbt(@NotNull Map<String, Object> map, TagMapper<Object> mapper) {
            final StringJoiner joiner = new StringJoiner(",", "{", "}");
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                final String key;
                if (isUnquoted(entry.getKey())) {
                    key = entry.getKey();
                } else {
                    key = '"' + entry.getKey() + '"';
                }
                final Object value = mapper.extract(entry.getValue());
                final Tag<Object> type = getType(value);
                joiner.add(key + ":" + type.snbt(value, mapper));
            }
            return joiner.toString();
        }

        @Override
        public @NotNull Map<String, Object> read(@NotNull DataInput input, @NotNull TagMapper<Object> mapper) throws IOException {
            final Map<String, Object> map = new HashMap<>();

            byte id;
            while ((id = input.readByte()) != END.getId()) {
                final Tag<?> type = getType(id);

                final String key = input.readUTF();
                final Object value = type.read(input, mapper);
                map.put(key, mapper.build(type, value));
            }

            return map;
        }

        @Override
        public void write(@NotNull DataOutput output, @NotNull Map<String, Object> map, @NotNull TagMapper<Object> mapper) throws IOException {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                final Object value = mapper.extract(entry.getValue());
                final Tag<Object> type = getType(value);
                output.writeByte(type.getId());
                if (type != END) {
                    output.writeUTF(entry.getKey());
                    type.write(output, value, mapper);
                }
            }
            output.writeByte(END.getId());
        }
    };
    public static final Tag<int[]> INT_ARRAY = new Tag<>(11, "INT[]", "TAG_Int_Array", 'I') {
        @Override
        public @NotNull String snbt(int @NotNull [] ints, TagMapper<Object> mapper) {
            final StringJoiner joiner = new StringJoiner(",", '[' + getSuffix() + ";", "]");
            for (int i : ints) {
                joiner.add(String.valueOf(i));
            }
            return joiner.toString();
        }

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
        public void write(@NotNull DataOutput output, int @NotNull [] ints, @NotNull TagMapper<Object> mapper) throws IOException {
            output.writeInt(ints.length);
            for (int i : ints) {
                output.writeInt(i);
            }
        }
    };
    public static final Tag<long[]> LONG_ARRAY = new Tag<>(12, "LONG[]", "TAG_Long_Array", 'L') {
        @Override
        public @NotNull String snbt(long @NotNull [] longs, TagMapper<Object> mapper) {
            final StringJoiner joiner = new StringJoiner(",", '[' + getSuffix() + ";", "]");
            for (long l : longs) {
                joiner.add(String.valueOf(l) + getSuffix());
            }
            return joiner.toString();
        }

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
        public void write(@NotNull DataOutput output, long @NotNull [] longs, @NotNull TagMapper<Object> mapper) throws IOException {
            output.writeInt(longs.length);
            for (long l : longs) {
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
    private static final Map<Class<?>, Tag<?>> CLASS_TYPES = new HashMap<>();
    private static final Map<Character, Tag<?>> SUFFIX_TYPES = new HashMap<>() {
        @Override
        public Tag<?> put(Character key, Tag<?> value) {
            super.put(Character.toLowerCase(key), value);
            return super.put(key, value);
        }
    };
    private static final Map<Character, Tag<?>> ARRAY_SUFFIX_TYPES = new HashMap<>() {
        @Override
        public Tag<?> put(Character key, Tag<?> value) {
            super.put(Character.toLowerCase(key), value);
            return super.put(key, value);
        }
    };

    static {
        CLASS_TYPES.put(Object.class, END);
        CLASS_TYPES.put(byte.class, BYTE);
        CLASS_TYPES.put(Byte.class, BYTE);
        CLASS_TYPES.put(boolean.class, BOOLEAN);
        CLASS_TYPES.put(Boolean.class, BOOLEAN);
        CLASS_TYPES.put(short.class, SHORT);
        CLASS_TYPES.put(Short.class, SHORT);
        CLASS_TYPES.put(int.class, INT);
        CLASS_TYPES.put(Integer.class, INT);
        CLASS_TYPES.put(long.class, LONG);
        CLASS_TYPES.put(Long.class, LONG);
        CLASS_TYPES.put(float.class, FLOAT);
        CLASS_TYPES.put(Float.class, FLOAT);
        CLASS_TYPES.put(double.class, DOUBLE);
        CLASS_TYPES.put(Double.class, DOUBLE);
        CLASS_TYPES.put(byte[].class, BYTE_ARRAY);
        CLASS_TYPES.put(Byte[].class, BYTE_ARRAY);
        CLASS_TYPES.put(boolean[].class, BOOLEAN_ARRAY);
        CLASS_TYPES.put(Boolean[].class, BOOLEAN_ARRAY);
        CLASS_TYPES.put(String.class, STRING);
        CLASS_TYPES.put(List.class, LIST);
        CLASS_TYPES.put(Map.class, COMPOUND);
        CLASS_TYPES.put(int[].class, INT_ARRAY);
        CLASS_TYPES.put(Integer[].class, INT_ARRAY);
        CLASS_TYPES.put(long[].class, LONG_ARRAY);
        CLASS_TYPES.put(Long[].class, LONG_ARRAY);
        SUFFIX_TYPES.put('B', BYTE);
        SUFFIX_TYPES.put('S', SHORT);
        SUFFIX_TYPES.put('L', LONG);
        SUFFIX_TYPES.put('F', FLOAT);
        SUFFIX_TYPES.put('D', DOUBLE);
        ARRAY_SUFFIX_TYPES.put('B', BYTE_ARRAY);
        ARRAY_SUFFIX_TYPES.put('I', INT_ARRAY);
        ARRAY_SUFFIX_TYPES.put('L', LONG_ARRAY);
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

    public boolean isDecimal() {
        return id == 5 || id == 6;
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
    public String snbt(@NotNull T t) {
        return snbt(t, TagMapper.DEFAULT);
    }

    @NotNull
    public String snbt(@NotNull T t, TagMapper<Object> mapper) {
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

    public void write(@NotNull DataOutput output, @NotNull T t) throws IOException {
        write(output, t, TagMapper.DEFAULT);
    }

    public void write(@NotNull DataOutput output, @NotNull T t, @NotNull TagMapper<Object> mapper) throws IOException {
        throw new IllegalStateException("Cannot write data for tag " + getName());
    }

    @Override
    public final boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Tag)) return false;

        final Tag<?> tag = (Tag<?>) object;
        return isValid() ? getId() == tag.getId() : getName().equals(tag.getName());
    }

    @Override
    public int hashCode() {
        return isValid() ? getId() : getName().hashCode();
    }

    protected boolean isUnquoted(@NotNull String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!isUnquoted(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    protected boolean isUnquoted(char c) {
        return c >= '0' && c <= '9'
            || c >= 'A' && c <= 'Z'
            || c >= 'a' && c <= 'z'
            || c == '_' || c == '-'
            || c == '.' || c == '+';
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
        Tag<?> result = CLASS_TYPES.get(type);
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

    @NotNull
    @SuppressWarnings("unchecked")
    public static <T> Tag<T> getType(char suffix) {
        final Tag<?> type = SUFFIX_TYPES.get(suffix);
        if (type == null) {
            return new Tag<>(-1, "INVALID(" + suffix + ")", "UNKNOWN_" + suffix) {
                @Override
                public boolean isValid() {
                    return false;
                }
            };
        }
        return (Tag<T>) type;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static <T> Tag<T> getArrayType(char suffix) {
        final Tag<?> type = ARRAY_SUFFIX_TYPES.get(suffix);
        if (type == null) {
            return new Tag<>(-1, "INVALID(" + suffix + ")", "UNKNOWN_" + suffix) {
                @Override
                public boolean isValid() {
                    return false;
                }
            };
        }
        return (Tag<T>) type;
    }
}
