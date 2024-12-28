package com.saicone.mcode.nbt;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagJson {

    @NotNull
    public static JsonElement toJson(@Nullable Object object) {
        return toJson(object, TagMapper.DEFAULT);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static <T> JsonElement toJson(@Nullable T object, @NotNull TagMapper<T> mapper) {
        if (object == null) {
            return JsonNull.INSTANCE;
        }
        final Object value = mapper.extract(object);
        if (value instanceof Boolean) {
            return new JsonPrimitive((Boolean) value);
        }
        final Tag<?> type = Tag.getType(value);
        switch (type.getId()) {
            case 0: // end
                return JsonNull.INSTANCE;
            case 1: // byte
            case 2: // short
            case 3: // int
            case 4: // long
            case 5: // float
            case 6: // double
                return new JsonPrimitive((Number) value);
            case 8: // String
                return new JsonPrimitive((String) value);
            case 7: // byte[]
            case 11: // int[]
            case 12: // long[]
                final int size = Array.getLength(value);
                final JsonArray array = new JsonArray(size);
                for (int i = 0; i < size; i++) {
                    final Object element = Array.get(value, i);
                    array.add(new JsonPrimitive((Number) element));
                }
                return array;
            case 9: // List
                final JsonArray list = new JsonArray(((List<Object>) value).size());
                for (Object o : (List<Object>) value) {
                    final JsonElement element = toJson((T) o, mapper);
                    list.add(element);
                }
                return list;
            case 10: // Compound
                final JsonObject map = new JsonObject();
                for (var entry : ((Map<Object, Object>) value).entrySet()) {
                    final JsonElement element = toJson((T) entry.getValue(), mapper);
                    map.add(String.valueOf(entry.getKey()), element);
                }
                return map;
            default:
                throw new IllegalArgumentException("Invalid tag type: " + type.getName());
        }
    }

    @Nullable
    public static Object fromJson(@NotNull JsonElement element) {
        return fromJson(element, TagMapper.DEFAULT);
    }

    @Nullable
    public static <T> T fromJson(@NotNull JsonElement element, @NotNull TagMapper<T> mapper) {
        if (element.isJsonNull()) {
            return mapper.build(Tag.END, null);
        } else if (element.isJsonPrimitive()) {
            final JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return mapper.build(Tag.BYTE, primitive.getAsBoolean() ? (byte) 1 : (byte) 0);
            } else if (primitive.isNumber()) {
                final Number number = primitive.getAsNumber();
                return mapper.build(Tag.getType(number), number);
            } else if (primitive.isString()) {
                return mapper.build(Tag.STRING, primitive.getAsString());
            }
        } else if (element.isJsonArray()) {
            final JsonArray array = element.getAsJsonArray();
            if (array.isEmpty()) {
                return mapper.build(Tag.LIST, new ArrayList<T>());
            }
            final JsonElement first = array.get(0);
            if (first.isJsonPrimitive()) {
                final JsonPrimitive primitive = first.getAsJsonPrimitive();
                if (primitive.isNumber()) {
                    final Number number = primitive.getAsNumber();
                    if (number instanceof Byte) {
                        final byte[] bytes = new byte[array.size()];
                        int i = 0;
                        for (JsonElement e : array) {
                            bytes[i] = e.getAsByte();
                            i++;
                        }
                        return mapper.build(Tag.BYTE_ARRAY, bytes);
                    } else if (number instanceof Integer) {
                        final int[] integers = new int[array.size()];
                        int i = 0;
                        for (JsonElement e : array) {
                            integers[i] = e.getAsInt();
                            i++;
                        }
                        return mapper.build(Tag.INT_ARRAY, integers);
                    } else if (number instanceof Long) {
                        final long[] longs = new long[array.size()];
                        int i = 0;
                        for (JsonElement e : array) {
                            longs[i] = e.getAsLong();
                            i++;
                        }
                        return mapper.build(Tag.LONG_ARRAY, longs);
                    }
                }
            }
            final List<T> list = new ArrayList<>();
            for (JsonElement e : array) {
                final T t = fromJson(e, mapper);
                if (t != null) {
                    list.add(t);
                }
            }
            return mapper.build(Tag.LIST, list);
        } else if (element.isJsonObject()) {
            final JsonObject json = element.getAsJsonObject();
            final Map<String, T> map = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                final T e = fromJson(entry.getValue(), mapper);
                if (e != null) {
                    map.put(entry.getKey(), e);
                }
            }
            return mapper.build(Tag.COMPOUND, map);
        }
        throw new IllegalArgumentException("Cannot get value from json: " + element);
    }
}
