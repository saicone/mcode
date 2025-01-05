package com.saicone.mcode.bungee.nbt;

import com.saicone.nbt.TagMapper;
import com.saicone.nbt.TagType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import se.llbit.nbt.ByteArrayTag;
import se.llbit.nbt.ByteTag;
import se.llbit.nbt.CompoundTag;
import se.llbit.nbt.DoubleTag;
import se.llbit.nbt.FloatTag;
import se.llbit.nbt.IntArrayTag;
import se.llbit.nbt.IntTag;
import se.llbit.nbt.ListTag;
import se.llbit.nbt.LongArrayTag;
import se.llbit.nbt.LongTag;
import se.llbit.nbt.NamedTag;
import se.llbit.nbt.ShortTag;
import se.llbit.nbt.SpecificTag;
import se.llbit.nbt.StringTag;
import se.llbit.nbt.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BungeeTagMapper implements TagMapper<Tag> {

    public static final BungeeTagMapper INSTANCE = new BungeeTagMapper();

    @Override
    public boolean isType(@Nullable Object object) {
        return object instanceof Tag;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Tag build(@NotNull TagType<?> type, @Nullable Object object) {
        return switch (type.id()) {
            case Tag.TAG_END -> Tag.END;
            case Tag.TAG_BYTE -> new ByteTag(object instanceof Boolean bool ? (bool ? 1 : 0) : (int) object);
            case Tag.TAG_SHORT -> new ShortTag((short) object);
            case Tag.TAG_INT -> new IntTag((int) object);
            case Tag.TAG_LONG -> new LongTag((long) object);
            case Tag.TAG_FLOAT -> new FloatTag((float) object);
            case Tag.TAG_DOUBLE -> new DoubleTag((double) object);
            case Tag.TAG_BYTE_ARRAY -> new ByteArrayTag(byteArray(object));
            case Tag.TAG_STRING -> new StringTag((String) object);
            case Tag.TAG_LIST -> new ListTag(type.id(), (List<? extends SpecificTag>) object);
            case Tag.TAG_COMPOUND -> {
                final Map<String, SpecificTag> map = (Map<String, SpecificTag>) object;
                final CompoundTag compound = new CompoundTag();
                for (Map.Entry<String, SpecificTag> entry : map.entrySet()) {
                    compound.add(entry.getKey(), entry.getValue());
                }
                yield compound;
            }
            case Tag.TAG_INT_ARRAY -> new IntArrayTag(intArray(object));
            case Tag.TAG_LONG_ARRAY -> new LongArrayTag(longArray(object));
            default -> throw new IllegalArgumentException("Invalid tag type: " + type.name());
        };
    }

    @Override
    public Object extract(@Nullable Tag tag) {
        final SpecificTag sTag;
        if (tag instanceof SpecificTag) {
            sTag = (SpecificTag) tag;
        } else if (tag instanceof NamedTag named) {
            sTag = named.getTag();
        } else if (tag == null) {
            return null;
        } else {
            throw new IllegalArgumentException("Invalid tag type: " + tag);
        }
        return switch (sTag.tagType()) {
            case Tag.TAG_END -> null;
            case Tag.TAG_BYTE -> sTag.byteValue();
            case Tag.TAG_SHORT -> sTag.shortValue();
            case Tag.TAG_INT -> sTag.intValue();
            case Tag.TAG_LONG -> sTag.longValue();
            case Tag.TAG_FLOAT -> sTag.floatValue();
            case Tag.TAG_DOUBLE -> sTag.doubleValue();
            case Tag.TAG_BYTE_ARRAY -> sTag.byteArray();
            case Tag.TAG_STRING -> sTag.stringValue();
            case Tag.TAG_LIST -> sTag.asList().items;
            case Tag.TAG_COMPOUND -> {
                final Map<String, SpecificTag> map = new HashMap<>();
                for (NamedTag namedTag : sTag.asCompound()) {
                    map.put(namedTag.name(), namedTag.getTag());
                }
                yield map;
            }
            case Tag.TAG_INT_ARRAY -> sTag.intArray();
            case Tag.TAG_LONG_ARRAY -> sTag.longArray();
            default -> throw new IllegalArgumentException("Invalid tag type: " + tag);
        };
    }

    @Override
    public int size(@Nullable Tag tag) {
        if (tag instanceof ListTag list) {
            int size = TagType.LIST.size();
            size += Integer.BYTES * list.size();

            for (SpecificTag element : list) {
                size += size(element);
            }

            return size;
        } else if (tag instanceof CompoundTag compound) {
            int size = TagType.COMPOUND.size();

            for (NamedTag entry : compound) {
                size += com.saicone.nbt.Tag.MAP_KEY_SIZE + Short.BYTES * entry.name().length();
                size += com.saicone.nbt.Tag.MAP_ENTRY_SIZE + Integer.BYTES;
                size += size(entry.getTag());
            }

            return size;
        } else {
            return type(tag).size(extract(tag));
        }
    }

    @Override
    public @NotNull <A> TagType<A> type(@Nullable Tag tag) {
        if (tag instanceof SpecificTag specific) {
            return TagType.getType(specific.tagType());
        } else if (tag instanceof NamedTag named) {
            return TagType.getType(named.getTag().tagType());
        } else {
            throw new IllegalArgumentException("Invalid tag type: " + tag);
        }
    }
}
