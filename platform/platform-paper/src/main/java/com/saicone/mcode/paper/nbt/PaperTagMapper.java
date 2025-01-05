package com.saicone.mcode.paper.nbt;

import com.saicone.nbt.TagMapper;
import com.saicone.nbt.TagType;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class PaperTagMapper implements TagMapper<Tag> {

    public static final PaperTagMapper INSTANCE = new PaperTagMapper();

    private static final MethodHandle LIST_VALUE;
    private static final MethodHandle COMPOUND_VALUE;

    static {
        MethodHandle listValue = null;
        MethodHandle compoundTags = null;
        try {
            final MethodHandles.Lookup lookup = MethodHandles.lookup();

            final Field listField = ListTag.class.getDeclaredField("list");
            listField.setAccessible(true);
            listValue = lookup.unreflectGetter(listField);

            final Field compoundField = CompoundTag.class.getDeclaredField("tags");
            compoundField.setAccessible(true);
            compoundTags = lookup.unreflectGetter(compoundField);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        LIST_VALUE = listValue;
        COMPOUND_VALUE = compoundTags;
    }

    @Override
    public boolean isType(@Nullable Object object) {
        return object instanceof Tag;
    }

    @Override
    public Tag build(@NotNull TagType<?> type, @Nullable Object object) {
        return switch (type.id()) {
            case Tag.TAG_END -> EndTag.INSTANCE;
            case Tag.TAG_BYTE -> object instanceof Boolean bool ? ByteTag.valueOf(bool) : ByteTag.valueOf((byte) object);
            case Tag.TAG_SHORT -> ShortTag.valueOf((short) object);
            case Tag.TAG_INT -> IntTag.valueOf((int) object);
            case Tag.TAG_LONG -> LongTag.valueOf((long) object);
            case Tag.TAG_FLOAT -> FloatTag.valueOf((float) object);
            case Tag.TAG_DOUBLE -> DoubleTag.valueOf((double) object);
            case Tag.TAG_BYTE_ARRAY -> new ByteArrayTag(byteArray(object));
            case Tag.TAG_STRING -> StringTag.valueOf((String) object);
            case Tag.TAG_LIST -> new ListTag((List<Tag>) object, type.id());
            case Tag.TAG_COMPOUND -> {
                final CompoundTag tag = new CompoundTag();
                for (Map.Entry<String, Tag> entry : ((Map<String, Tag>) object).entrySet()) {
                    tag.put(entry.getKey(), entry.getValue());
                }
                yield tag;
            }
            case Tag.TAG_INT_ARRAY -> new IntArrayTag(intArray(object));
            case Tag.TAG_LONG_ARRAY -> new LongArrayTag(longArray(object));
            default -> throw new IllegalArgumentException("Invalid tag type: " + type.name());
        };
    }

    @Override
    public Object extract(@Nullable Tag tag) {
        if (tag == null) {
            return null;
        }
        return switch (tag.getId()) {
            case Tag.TAG_END -> null;
            case Tag.TAG_BYTE -> ((ByteTag) tag).getAsByte();
            case Tag.TAG_SHORT -> ((ShortTag) tag).getAsShort();
            case Tag.TAG_INT -> ((IntTag) tag).getAsInt();
            case Tag.TAG_LONG -> ((LongTag) tag).getAsLong();
            case Tag.TAG_FLOAT -> ((FloatTag) tag).getAsFloat();
            case Tag.TAG_DOUBLE -> ((DoubleTag) tag).getAsDouble();
            case Tag.TAG_BYTE_ARRAY -> ((ByteArrayTag) tag).getAsByteArray();
            case Tag.TAG_STRING -> ((StringTag) tag).getAsString();
            case Tag.TAG_LIST -> {
                try {
                    yield LIST_VALUE.invoke(tag);
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            }
            case Tag.TAG_COMPOUND -> {
                try {
                    yield COMPOUND_VALUE.invoke(tag);
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            }
            case Tag.TAG_INT_ARRAY -> ((IntArrayTag) tag).getAsIntArray();
            case Tag.TAG_LONG_ARRAY -> ((LongArrayTag) tag).getAsLongArray();
            default -> throw new IllegalArgumentException("Invalid tag type: " + tag);
        };
    }

    @Override
    public int size(@Nullable Tag tag) {
        return tag == null ? EndTag.INSTANCE.sizeInBytes() : tag.sizeInBytes();
    }

    @Override
    public @NotNull <A> TagType<A> type(@Nullable Tag tag) {
        return tag == null ? TagType.getType(Tag.TAG_END) : TagType.getType(tag.getId());
    }
}
