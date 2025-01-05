package com.saicone.mcode.velocity.nbt;

import com.saicone.nbt.Tag;
import com.saicone.nbt.TagMapper;
import com.saicone.nbt.TagType;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.ByteArrayBinaryTag;
import net.kyori.adventure.nbt.ByteBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.kyori.adventure.nbt.EndBinaryTag;
import net.kyori.adventure.nbt.FloatBinaryTag;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.LongArrayBinaryTag;
import net.kyori.adventure.nbt.LongBinaryTag;
import net.kyori.adventure.nbt.ShortBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class VelocityTagMapper implements TagMapper<BinaryTag> {

    public static final VelocityTagMapper INSTANCE = new VelocityTagMapper();

    private static final MethodHandle LIST_TAGS;
    private static final MethodHandle COMPOUND_TAGS;

    static {
        MethodHandle listTags = null;
        MethodHandle compoundTags = null;
        try {
            final MethodHandles.Lookup lookup = MethodHandles.lookup();

            final Class<? extends ListBinaryTag> listClass = Class.forName("net.kyori.adventure.nbt.ListBinaryTagImpl").asSubclass(ListBinaryTag.class);
            final Field listField = listClass.getDeclaredField("tags");
            listField.setAccessible(true);
            listTags = lookup.unreflectGetter(listField);

            final Class<? extends CompoundBinaryTag> compoundClass = Class.forName("net.kyori.adventure.nbt.CompoundBinaryTagImpl").asSubclass(CompoundBinaryTag.class);
            final Field compoundField = compoundClass.getDeclaredField("tags");
            compoundField.setAccessible(true);
            compoundTags = lookup.unreflectGetter(compoundField);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        LIST_TAGS = listTags;
        COMPOUND_TAGS = compoundTags;
    }

    @Override
    public boolean isType(@Nullable Object object) {
        return object instanceof BinaryTag;
    }

    @Override
    @SuppressWarnings("unchecked")
    public BinaryTag build(@NotNull TagType<?> type, @Nullable Object object) {
        return switch (type.id()) {
            case Tag.END -> EndBinaryTag.endBinaryTag();
            case Tag.BYTE -> object instanceof Boolean bool ? (bool ? ByteBinaryTag.ONE : ByteBinaryTag.ZERO) : ByteBinaryTag.byteBinaryTag((byte) object);
            case Tag.SHORT -> ShortBinaryTag.shortBinaryTag((short) object);
            case Tag.INT -> IntBinaryTag.intBinaryTag((int) object);
            case Tag.LONG -> LongBinaryTag.longBinaryTag((long) object);
            case Tag.FLOAT -> FloatBinaryTag.floatBinaryTag((float) object);
            case Tag.DOUBLE -> DoubleBinaryTag.doubleBinaryTag((double) object);
            case Tag.BYTE_ARRAY -> ByteArrayBinaryTag.byteArrayBinaryTag(byteArray(object));
            case Tag.STRING -> StringBinaryTag.stringBinaryTag((String) object);
            case Tag.LIST -> ListBinaryTag.from((Iterable<? extends BinaryTag>) object);
            case Tag.COMPOUND -> CompoundBinaryTag.from((Map<String, ? extends BinaryTag>) object);
            case Tag.INT_ARRAY -> IntArrayBinaryTag.intArrayBinaryTag(intArray(object));
            case Tag.LONG_ARRAY -> LongArrayBinaryTag.longArrayBinaryTag(longArray(object));
            default -> throw new IllegalArgumentException("Invalid tag type: " + type.name());
        };
    }

    @Override
    public Object extract(@Nullable BinaryTag tag) {
        if (tag == null) {
            return null;
        }
        return switch (tag.type().id()) {
            case Tag.END -> null;
            case Tag.BYTE -> ((ByteBinaryTag) tag).value();
            case Tag.SHORT -> ((ShortBinaryTag) tag).value();
            case Tag.INT -> ((IntBinaryTag) tag).value();
            case Tag.LONG -> ((LongBinaryTag) tag).value();
            case Tag.FLOAT -> ((FloatBinaryTag) tag).value();
            case Tag.DOUBLE -> ((DoubleBinaryTag) tag).value();
            case Tag.BYTE_ARRAY -> ((ByteArrayBinaryTag) tag).value();
            case Tag.STRING -> ((StringBinaryTag) tag).value();
            case Tag.LIST -> {
                try {
                    yield LIST_TAGS.invoke(tag);
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            }
            case Tag.COMPOUND -> {
                try {
                    yield COMPOUND_TAGS.invoke(tag);
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            }
            case Tag.INT_ARRAY -> ((IntArrayBinaryTag) tag).value();
            case Tag.LONG_ARRAY -> ((LongArrayBinaryTag) tag).value();
            default -> throw new IllegalArgumentException("Invalid tag type: " + tag);
        };
    }

    @Override
    public int size(@Nullable BinaryTag tag) {
        final Object value = extract(tag);
        if (value instanceof List<?> list) {
            int size = TagType.LIST.size();
            size += Integer.BYTES * list.size();

            for (Object object : list) {
                size += size((BinaryTag) object);
            }

            return size;
        } else if (value instanceof Map<?,?> map) {
            int size = TagType.COMPOUND.size();

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                size += Tag.MAP_KEY_SIZE + Short.BYTES * String.valueOf(entry.getKey()).length();
                size += Tag.MAP_ENTRY_SIZE + Integer.BYTES;
                size += size((BinaryTag) entry.getValue());
            }

            return size;
        } else {
            return type(tag).size(value);
        }
    }

    @Override
    public @NotNull <A> TagType<A> type(@Nullable BinaryTag tag) {
        return tag == null ? TagType.getType(Tag.END) : TagType.getType(tag.type().id());
    }
}
