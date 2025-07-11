package com.saicone.mcode.velocity;

import com.google.gson.JsonElement;
import com.saicone.mcode.platform.MC;
import com.saicone.mcode.platform.Text;
import com.saicone.mcode.util.text.TextComponent;
import com.saicone.nbt.io.TagReader;
import com.saicone.nbt.io.TagWriter;
import com.saicone.nbt.mapper.AdventureTagMapper;
import com.saicone.nbt.util.TagJson;
import net.kyori.adventure.nbt.BinaryTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

public class VelocityText {

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
                    value = TagReader.fromString((String) value, AdventureTagMapper.INSTANCE);
                }
                if (version == null) {
                    yield new Nbt((BinaryTag) value);
                }
                yield new Nbt(version, (BinaryTag) value);
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
            return Text.valueOf(Text.NBT, getVersion(), TagReader.fromString(getValue(), AdventureTagMapper.INSTANCE)).getAsNbt();
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
            return Text.valueOf(Text.NBT, getVersion(), TagJson.fromJson(getValue(), AdventureTagMapper.INSTANCE)).getAsNbt();
        }
    }

    public static class Nbt extends Text.Nbt<BinaryTag> {
        public Nbt(@NotNull BinaryTag value) {
            this(TextComponent.readVersion(value, AdventureTagMapper.INSTANCE), value);
        }

        public Nbt(@NotNull MC version, @NotNull BinaryTag value) {
            super(version, value);
        }

        @Override
        public @NotNull StringText getAsString() {
            return Text.valueOf(Text.PLAIN_TEXT, getVersion(), TagWriter.toString(getValue(), AdventureTagMapper.INSTANCE)).getAsString();
        }

        @Override
        public @NotNull PlainText getAsPlainText() {
            return Text.valueOf(Text.PLAIN_TEXT, getVersion(), TagWriter.toString(getValue(), AdventureTagMapper.INSTANCE)).getAsPlainText();
        }

        @Override
        public @NotNull Text.Json getAsJson() {
            return Text.valueOf(Text.RAW_JSON, getVersion(), TagJson.toJson(getValue(), AdventureTagMapper.INSTANCE)).getAsJson();
        }

        @Override
        public @NotNull Text apply(@NotNull UnaryOperator<String> operator) {
            return Text.nbt(getVersion(), TextComponent.apply(getValue(), AdventureTagMapper.INSTANCE, operator));
        }
    }
}
