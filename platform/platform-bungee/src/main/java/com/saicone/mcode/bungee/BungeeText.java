package com.saicone.mcode.bungee;

import com.google.gson.JsonElement;
import com.saicone.mcode.platform.MC;
import com.saicone.mcode.platform.Text;
import com.saicone.mcode.util.text.TextComponent;
import com.saicone.nbt.io.TagReader;
import com.saicone.nbt.io.TagWriter;
import com.saicone.nbt.mapper.JoNbtTagMapper;
import com.saicone.nbt.util.TagJson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import se.llbit.nbt.Tag;

public class BungeeText {

    @NotNull
    public static Text valueOf(byte type, @Nullable MC version, @NotNull Object value) {
        return switch (type) {
            case Text.EMPTY -> Text.empty();
            case Text.PLAIN_TEXT -> new PlainText(version != null ? version : MC.version(), String.valueOf(value));
            case Text.COLORED -> {
                if (version == null) {
                    yield new Text.Colored((String) value);
                }
                yield new Text.Colored(version, (String) value);
            }
            case Text.RAW_JSON -> {
                if (version == null) {
                    yield new Json((JsonElement) value);
                }
                yield new Json(version, (JsonElement) value);
            }
            case Text.NBT -> {
                if (value instanceof String) {
                    value = TagReader.fromString((String) value, JoNbtTagMapper.INSTANCE);
                }
                if (version == null) {
                    yield new Nbt((Tag) value);
                }
                yield new Nbt(version, (Tag) value);
            }
            default -> throw new IllegalArgumentException("Invalid text type: " + type);
        };
    }

    public static class PlainText extends Text.PlainText {
        public PlainText(@NotNull MC version, @NotNull String value) {
            super(version, value);
        }

        @Override
        public @NotNull Nbt<?> getAsNbt() {
            return Text.valueOf(Text.NBT, getVersion(), TagReader.fromString(getValue(), JoNbtTagMapper.INSTANCE)).getAsNbt();
        }
    }

    public static class Json extends Text.Json {
        public Json(@NotNull JsonElement value) {
            super(value);
        }

        public Json(@NotNull MC version, @NotNull JsonElement value) {
            super(version, value);
        }

        @Override
        public @NotNull Nbt<?> getAsNbt() {
            return Text.valueOf(Text.NBT, getVersion(), TagJson.fromJson(getValue(), JoNbtTagMapper.INSTANCE)).getAsNbt();
        }
    }

    public static class Nbt extends Text.Nbt<Tag> {
        public Nbt(@NotNull Tag value) {
            this(TextComponent.readVersion(value, JoNbtTagMapper.INSTANCE), value);
        }

        public Nbt(@NotNull MC version, @NotNull Tag value) {
            super(version, value);
        }

        @Override
        public @NotNull StringText getAsString() {
            return Text.valueOf(Text.PLAIN_TEXT, getVersion(), TagWriter.toString(getValue(), JoNbtTagMapper.INSTANCE)).getAsString();
        }

        @Override
        public @NotNull PlainText getAsPlainText() {
            return Text.valueOf(Text.PLAIN_TEXT, getVersion(), TagWriter.toString(getValue(), JoNbtTagMapper.INSTANCE)).getAsPlainText();
        }

        @Override
        public @NotNull Text.Json getAsJson() {
            return Text.valueOf(Text.RAW_JSON, getVersion(), TagJson.toJson(getValue(), JoNbtTagMapper.INSTANCE)).getAsJson();
        }
    }
}
