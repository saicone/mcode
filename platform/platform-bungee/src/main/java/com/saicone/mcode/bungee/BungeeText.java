package com.saicone.mcode.bungee;

import com.google.gson.JsonElement;
import com.saicone.mcode.platform.Text;
import com.saicone.nbt.io.TagReader;
import com.saicone.nbt.io.TagWriter;
import com.saicone.nbt.mapper.JoNbtTagMapper;
import com.saicone.nbt.util.TagJson;
import org.jetbrains.annotations.NotNull;
import se.llbit.nbt.Tag;

public class BungeeText {

    @NotNull
    public static Text valueOf(byte type, @NotNull Object value) {
        return switch (type) {
            case Text.EMPTY -> Text.empty();
            case Text.PLAIN_TEXT -> new PlainText(String.valueOf(value));
            case Text.COLORED -> new Text.Colored((String) value);
            case Text.RAW_JSON -> new RawJson((JsonElement) value);
            case Text.NBT -> {
                if (value instanceof String) {
                    yield new Nbt(TagReader.fromString((String) value, JoNbtTagMapper.INSTANCE));
                }
                yield new Nbt((Tag) value);
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
            return Text.valueOf(Text.NBT, TagReader.fromString(getValue(), JoNbtTagMapper.INSTANCE)).getAsNbt();
        }
    }

    public static class RawJson extends Text.RawJson {
        public RawJson(@NotNull JsonElement value) {
            super(value);
        }

        @Override
        public @NotNull Nbt<?> getAsNbt() {
            return Text.valueOf(Text.NBT, TagJson.fromJson(getValue(), JoNbtTagMapper.INSTANCE)).getAsNbt();
        }
    }

    public static class Nbt extends Text.Nbt<Tag> {
        public Nbt(@NotNull Tag value) {
            super(value);
        }

        @Override
        public @NotNull StringText getAsString() {
            return Text.valueOf(Text.PLAIN_TEXT, TagWriter.toString(getValue(), JoNbtTagMapper.INSTANCE)).getAsString();
        }

        @Override
        public @NotNull PlainText getAsPlainText() {
            return Text.valueOf(Text.PLAIN_TEXT, TagWriter.toString(getValue(), JoNbtTagMapper.INSTANCE)).getAsPlainText();
        }

        @Override
        public @NotNull RawJson getAsRawJson() {
            return Text.valueOf(Text.RAW_JSON, TagJson.toJson(getValue(), JoNbtTagMapper.INSTANCE)).getAsRawJson();
        }
    }
}
