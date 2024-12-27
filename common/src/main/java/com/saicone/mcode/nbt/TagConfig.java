package com.saicone.mcode.nbt;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

public class TagConfig {

    private static final Set<Character> NUMBER_SUFFIX = Set.of('b', 's', 'L', 'f', 'd');

    @Nullable
    @Contract("!null -> !null")
    public static Object toConfigValue(@Nullable Object object) {
        return toConfigValue(object, TagMapper.DEFAULT);
    }

    @Nullable
    @Contract("!null, _ -> !null")
    @SuppressWarnings("unchecked")
    public static <T> Object toConfigValue(@Nullable T object, @NotNull TagMapper<T> mapper) {
        if (object == null) {
            return null;
        }
        final Object value = mapper.extract(object);
        if (value == null) {
            return null;
        }
        final Tag<?> type = Tag.getType(value);
        switch (type.getId()) {
            case 0: // end
                return null;
            case 3: // int
            case 8: // String
                return value;
            case 1: // byte
            case 2: // short
            case 4: // long
            case 5: // float
            case 6: // double
                return String.valueOf(value) + type.getSuffix();
            case 7: // byte[]
            case 11: // int[]
            case 12: // long[]
                return arrayToConfigValue(type, value);
            case 9: // List
                final List<Object> list = new ArrayList<>();
                for (Object o : (List<Object>) value) {
                    final Object element = toConfigValue((T) o, mapper);
                    if (element != null) {
                        list.add(element);
                    }
                }
                return list;
            case 10: // Compound
                final Map<String, Object> map = new HashMap<>();
                for (var entry : ((Map<Object, Object>) value).entrySet()) {
                    final Object element = toConfigValue((T) entry.getValue(), mapper);
                    if (element != null) {
                        map.put(String.valueOf(entry.getKey()), element);
                    }
                }
                return map;
            default:
                throw new IllegalArgumentException("Invalid tag type: " + type.getName());
        }
    }

    @NotNull
    private static String arrayToConfigValue(@NotNull Tag<?> tag, @NotNull Object array) {
        final StringJoiner joiner = new StringJoiner(", ", '[' + tag.getSuffix() + "; ", "]");
        final char suffix;
        if (tag == Tag.INT_ARRAY) {
            suffix = '\0';
        } else {
            suffix = tag.getSuffix();
        }
        final int size = Array.getLength(array);
        for (int i = 0; i < size; i++) {
            final Object value = Array.get(array, i);
            joiner.add(String.valueOf(value) + suffix);
        }
        return joiner.toString();
    }

    @Nullable
    public Object fromConfigValue(@Nullable Object value) {
        return fromConfigValue(value, TagMapper.DEFAULT);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T fromConfigValue(@Nullable Object value, @NotNull TagMapper<T> mapper) {
        if (value == null) {
            return null;
        }

        final Tag<?> type = Tag.getType(value);
        switch (type.getId()) {
            case 0:
                return null;
            case 1: // byte
            case 2: // short
            case 3: // int
            case 4: // long
            case 5: // float
            case 6: // double
            case 7: // byte[]
            case 11: // int[]
            case 12: // long[]
                return mapper.build(type, value);
            case 8: // String
                return fromConfigString((String) value, mapper);
            case 9:
                final List<T> list = new ArrayList<>();
                for (Object o : (List<Object>) value) {
                    final T v = fromConfigValue(o, mapper);
                    if (v != null) {
                        list.add(v);
                    }
                }
                return mapper.build(type, list);
            case 10:
                final Map<String, T> map = new HashMap<>();
                for (var entry : ((Map<Object, Object>) value).entrySet()) {
                    final T v = fromConfigValue(entry.getValue(), mapper);
                    if (v != null) {
                        map.put(String.valueOf(entry.getKey()), v);
                    }
                }
                return mapper.build(type, map);
            default:
                throw new IllegalArgumentException("Invalid tag type: " + type.getName());
        }
    }

    @Nullable
    private <T> T fromConfigString(@NotNull String value, @NotNull TagMapper<T> mapper) {
        if (value.length() < 2) {
            return mapper.build(Tag.STRING, value);
        }
        if (value.startsWith("[") && value.endsWith("]")) {
            if (value.startsWith("[B;")) {
                final List<Byte> list = new ArrayList<>();
                for (String s : value.substring(4).split(",")) {
                    if (!s.endsWith("B")) {
                        list.clear();
                        return mapper.build(Tag.STRING, value);
                    }
                    list.add(Byte.parseByte(s.trim().substring(0, s.length() - 1)));
                }
                final byte[] array = new byte[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    array[i] = list.get(i);
                }
                return mapper.build(Tag.BYTE_ARRAY, array);
            } else if (value.startsWith("[L;")) {
                final List<Long> list = new ArrayList<>();
                for (String s : value.substring(4).split(",")) {
                    if (!s.endsWith("L")) {
                        list.clear();
                        return mapper.build(Tag.STRING, value);
                    }
                    list.add(Long.parseLong(s.trim().substring(0, s.length() - 1)));
                }
                final long[] array = new long[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    array[i] = list.get(i);
                }
                return mapper.build(Tag.LONG_ARRAY, array);
            } else if (value.startsWith("[I;")) {
                final List<Integer> list = new ArrayList<>();
                for (String s : value.substring(4).split(",")) {
                    list.add(Integer.parseInt(s.trim()));
                }
                final int[] array = new int[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    array[i] = list.get(i);
                }
                return mapper.build(Tag.INT_ARRAY, array);
            } else {
                return mapper.build(Tag.STRING, value);
            }
        }

        final char suffix = value.charAt(value.length() - 1);
        if (NUMBER_SUFFIX.contains(suffix)) {
            final String s = value.substring(0, value.length() - 1);
            if (isNumber(s)) {
                switch (suffix) {
                    case 'b':
                        return mapper.build(Tag.BYTE, Byte.parseByte(s));
                    case 's':
                        return mapper.build(Tag.SHORT, Short.parseShort(s));
                    case 'L':
                        return mapper.build(Tag.LONG, Long.parseLong(s));
                    case 'f':
                        return mapper.build(Tag.FLOAT, Float.parseFloat(s));
                    case 'd':
                        return mapper.build(Tag.DOUBLE, Double.parseDouble(s));
                }
            }
        }
        return mapper.build(Tag.STRING, value);
    }

    private static boolean isNumber(@NotNull String s) {
        if (s.isBlank()) {
            return false;
        }
        boolean decimal = false;
        for (char c : (s.charAt(0) == '-' ? s.substring(1) : s).toCharArray()) {
            if (!Character.isDigit(c)) {
                if (!decimal && c == '.') {
                    decimal = true;
                    continue;
                }
                return false;
            }
        }
        return true;
    }
}
