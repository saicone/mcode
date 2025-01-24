package com.saicone.mcode.velocity;

import com.google.gson.JsonElement;
import com.saicone.mcode.platform.Text;
import com.saicone.nbt.io.TagReader;
import com.saicone.nbt.io.TagWriter;
import com.saicone.nbt.mapper.AdventureTagMapper;
import com.saicone.nbt.util.TagJson;
import net.kyori.adventure.nbt.BinaryTag;
import org.jetbrains.annotations.NotNull;

public class VelocityText {

    @NotNull
    public static Text valueOf(byte type, @NotNull Object value) {
        return switch (type) {
            case Text.EMPTY -> Text.empty();
            case Text.PLAIN_TEXT -> new PlainText(String.valueOf(value));
            case Text.COLORED -> new Text.Colored((String) value);
            case Text.RAW_JSON -> new RawJson((JsonElement) value);
            case Text.NBT -> {
                if (value instanceof String) {
                    yield new Nbt(TagReader.fromString((String) value, AdventureTagMapper.INSTANCE));
                }
                yield new Nbt((BinaryTag) value);
            }
            default -> throw new IllegalArgumentException("Invalid text type: " + type);
        };
    }

    public static class PlainText extends Text.PlainText {
        public PlainText(@NotNull String value) {
            super(value);
        }

        @Override
        public @NotNull Nbt<?> getAsNbt() {
            return Text.valueOf(Text.NBT, TagReader.fromString(getValue(), AdventureTagMapper.INSTANCE)).getAsNbt();
        }
    }

    public static class RawJson extends Text.RawJson {
        public RawJson(@NotNull JsonElement value) {
            super(value);
        }

        @Override
        public @NotNull Nbt<?> getAsNbt() {
            return Text.valueOf(Text.NBT, TagJson.fromJson(getValue(), AdventureTagMapper.INSTANCE)).getAsNbt();
        }
    }

    public static class Nbt extends Text.Nbt<BinaryTag> {
        public Nbt(@NotNull BinaryTag value) {
            super(value);
        }

        @Override
        public @NotNull StringText getAsString() {
            return Text.valueOf(Text.PLAIN_TEXT, TagWriter.toString(getValue(), AdventureTagMapper.INSTANCE)).getAsString();
        }

        @Override
        public @NotNull PlainText getAsPlainText() {
            return Text.valueOf(Text.PLAIN_TEXT, TagWriter.toString(getValue(), AdventureTagMapper.INSTANCE)).getAsPlainText();
        }

        @Override
        public @NotNull RawJson getAsRawJson() {
            return Text.valueOf(Text.RAW_JSON, TagJson.toJson(getValue(), AdventureTagMapper.INSTANCE)).getAsRawJson();
        }
    }
}
