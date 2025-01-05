package com.saicone.mcode.bukkit.nbt;

import com.saicone.mcode.bukkit.util.BukkitLookup;
import com.saicone.mcode.platform.MC;
import com.saicone.nbt.Tag;
import com.saicone.nbt.TagMapper;
import com.saicone.nbt.TagType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class BukkitTagMapper implements TagMapper<Object> {

    public static final BukkitTagMapper INSTANCE = new BukkitTagMapper();

    private static final Class<?> TAG_TYPE = BukkitLookup.classById("Tag");
    private static final MethodHandle TAG_ID = BukkitLookup.find("Tag", "byte getId()", MC.supply(
            "b", MC.V_1_19_3,
            "a", MC.V_1_18,
            "getTypeId"
    ));
    private static final MethodHandle TAG_SIZE = BukkitLookup.find("Tag", MC.supply("int sizeInBytes()", MC.V_1_19_3), () -> "a");

    private static final MethodHandle NEW_END = BukkitLookup.find("EndTag", MC.supply(
            "INSTANCE", MC.V_1_15,
            "()"
    ), () -> "b");
    private static final MethodHandle NEW_BYTE = BukkitLookup.find("ByteTag", MC.supply(
            "public static ByteTag valueOf(byte b)", MC.V_1_15,
            "(byte b)"
    ), () -> "a");
    private static final MethodHandle NEW_BOOLEAN = BukkitLookup.find("ByteTag", MC.supply(
            "public static ByteTag valueOf(boolean b)", MC.V_1_15,
            "(byte b)"
    ), () -> "a");
    private static final MethodHandle NEW_SHORT = BukkitLookup.find("ShortTag", MC.supply(
            "public static ShortTag valueOf(short s)", MC.V_1_15,
            "(short s)"
    ), () -> "a");
    private static final MethodHandle NEW_INT = BukkitLookup.find("IntTag", MC.supply(
            "public static IntTag valueOf(int i)", MC.V_1_15,
            "(int i)"
    ), () -> "a");
    private static final MethodHandle NEW_LONG = BukkitLookup.find("LongTag", MC.supply(
            "public static LongTag valueOf(long l)", MC.V_1_15,
            "(long l)"
    ), () -> "a");
    private static final MethodHandle NEW_FLOAT = BukkitLookup.find("FloatTag", MC.supply(
            "public static FloatTag valueOf(float f)", MC.V_1_15,
            "(float f)"
    ), () -> "a");
    private static final MethodHandle NEW_DOUBLE = BukkitLookup.find("DoubleTag", MC.supply(
            "public static DoubleTag valueOf(double d)", MC.V_1_15,
            "(double d)"
    ), () -> "a");
    private static final MethodHandle NEW_BYTE_ARRAY = BukkitLookup.find("ByteArrayTag", "(byte[] data)");
    private static final MethodHandle NEW_STRING = BukkitLookup.find("StringTag", MC.supply(
            "public static StringTag valueOf(String s)", MC.V_1_15,
            "(String s)"
    ), () -> "a");
    private static final MethodHandle NEW_LIST = BukkitLookup.find("ListTag", MC.supply(
            "(List<Tag> list, byte type)", MC.V_1_15,
            "()"
    ));
    private static final MethodHandle NEW_COMPOUND = BukkitLookup.find("CompoundTag", MC.supply(
            "(Map<String, Tag> map)", MC.V_1_15,
            "()"
    ));
    private static final MethodHandle NEW_INT_ARRAY = BukkitLookup.find("IntArrayTag", "(int[] data)");
    private static final MethodHandle NEW_LONG_ARRAY = BukkitLookup.find("LongArrayTag", MC.supply("(long[] data)", MC.V_1_12));

    private static final MethodHandle GET_BYTE = BukkitLookup.find("ByteTag", "data", MC.supply(
            "x", MC.V_1_17,
            "data"
    ));
    private static final MethodHandle GET_SHORT = BukkitLookup.find("ShortTag", "data", MC.supply(
            "c", MC.V_1_17,
            "data"
    ));
    private static final MethodHandle GET_INT = BukkitLookup.find("IntTag", "data", MC.supply(
            "c", MC.V_1_17,
            "data"
    ));
    private static final MethodHandle GET_LONG = BukkitLookup.find("LongTag", "data", MC.supply(
            "c", MC.V_1_17,
            "data"
    ));
    private static final MethodHandle GET_FLOAT = BukkitLookup.find("FloatTag", "data", MC.supply(
            "w", MC.V_1_17,
            "data"
    ));
    private static final MethodHandle GET_DOUBLE = BukkitLookup.find("DoubleTag", "data", MC.supply(
            "w", MC.V_1_17,
            "data"
    ));
    private static final MethodHandle GET_BYTE_ARRAY = BukkitLookup.find("ByteArrayTag", "data", MC.supply(
            "c", MC.V_1_17,
            "data"
    ));
    private static final MethodHandle GET_STRING = BukkitLookup.find("StringTag", "data", MC.supply(
            "A", MC.V_1_17,
            "data"
    ));
    private static final MethodHandle GET_LIST = BukkitLookup.find("ListTag", "list", MC.supply(
            "c", MC.V_1_17,
            "list"
    ));
    private static final MethodHandle GET_COMPOUND = BukkitLookup.find("CompoundTag", "tags", MC.supply(
            "x", MC.V_1_17,
            "map"
    ));
    private static final MethodHandle GET_INT_ARRAY = BukkitLookup.find("IntArrayTag", "data", MC.supply(
            "c", MC.V_1_17,
            "data"
    ));
    private static final MethodHandle GET_LONG_ARRAY = BukkitLookup.find("LongArrayTag", MC.supply("data", MC.V_1_12), () -> {
        if (MC.version().isNewerThanOrEquals(MC.V_1_17)) {
            return "c";
        } else if (MC.version().isBetween(MC.V_1_13, MC.V_1_14_4)) {
            return "f";
        } else {
            return "b";
        }
    });

    private static final MethodHandle SET_LIST_TYPE = BukkitLookup.find("ListTag", "set type", MC.supply(
            "set w", MC.V_1_17,
            "set type"
    ));


    @Override
    public boolean isType(@Nullable Object object) {
        return TAG_TYPE.isInstance(object);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object build(@NotNull TagType<?> type, @Nullable Object object) {
        try {
            switch (type.id()) {
                case Tag.END:
                    return NEW_END.invoke();
                case Tag.BYTE:
                    return object instanceof Boolean ? NEW_BOOLEAN.invoke(object) : NEW_BYTE.invoke(object);
                case Tag.SHORT:
                    return NEW_SHORT.invoke(object);
                case Tag.INT:
                    return NEW_INT.invoke(object);
                case Tag.LONG:
                    return NEW_LONG.invoke(object);
                case Tag.FLOAT:
                    return NEW_FLOAT.invoke(object);
                case Tag.DOUBLE:
                    return NEW_DOUBLE.invoke(object);
                case Tag.BYTE_ARRAY:
                    return NEW_BYTE_ARRAY.invoke(object);
                case Tag.STRING:
                    return NEW_STRING.invoke(object);
                case Tag.LIST:
                    if (MC.version().isNewerThanOrEquals(MC.V_1_15)) {
                        return NEW_LIST.invoke(object, type.id());
                    } else {
                        final Object list = NEW_LIST.invoke();
                        final List<Object> value = (List<Object>) GET_LIST.invoke(list);
                        value.addAll((Collection<Object>) object);
                        SET_LIST_TYPE.invoke(list, type.id());
                        return list;
                    }
                case Tag.COMPOUND:
                    if (MC.version().isNewerThanOrEquals(MC.V_1_15)) {
                        return NEW_COMPOUND.invoke(object);
                    } else {
                        final Object compound = NEW_COMPOUND.invoke();
                        final Map<String, Object> map = (Map<String, Object>) GET_COMPOUND.invoke(compound);
                        map.putAll((Map<String, Object>) object);
                        return compound;
                    }
                case Tag.INT_ARRAY:
                    return NEW_INT_ARRAY.invoke(object);
                case Tag.LONG_ARRAY:
                    return NEW_LONG_ARRAY.invoke(object);
                default:
                    throw new IllegalArgumentException("Invalid tag type: " + type.name());
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public Object extract(@Nullable Object object) {
        if (object == null) {
            return null;
        }
        try {
            switch ((byte) TAG_ID.invoke(object)) {
                case Tag.END:
                    return null;
                case Tag.BYTE:
                    return GET_BYTE.invoke(object);
                case Tag.SHORT:
                    return GET_SHORT.invoke(object);
                case Tag.INT:
                    return GET_INT.invoke(object);
                case Tag.LONG:
                    return GET_LONG.invoke(object);
                case Tag.FLOAT:
                    return GET_FLOAT.invoke(object);
                case Tag.DOUBLE:
                    return GET_DOUBLE.invoke(object);
                case Tag.BYTE_ARRAY:
                    return GET_BYTE_ARRAY.invoke(object);
                case Tag.STRING:
                    return GET_STRING.invoke(object);
                case Tag.LIST:
                    return GET_LIST.invoke(object);
                case Tag.COMPOUND:
                    return GET_COMPOUND.invoke(object);
                case Tag.INT_ARRAY:
                    return GET_INT_ARRAY.invoke(object);
                case Tag.LONG_ARRAY:
                    return GET_LONG_ARRAY.invoke(object);
                default:
                    throw new IllegalArgumentException("Invalid tag type: " + object);
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public int size(@Nullable Object object) {
        try {
            return (int) TAG_SIZE.invoke(object);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public @NotNull <A> TagType<A> type(@Nullable Object object) {
        try {
            return TagType.getType((byte) TAG_ID.invoke(object));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
