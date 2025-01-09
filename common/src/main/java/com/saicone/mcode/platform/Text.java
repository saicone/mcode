package com.saicone.mcode.platform;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.saicone.mcode.Platform;
import com.saicone.mcode.util.text.MStrings;
import com.saicone.mcode.util.text.Replacer;
import com.saicone.mcode.util.text.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class Text {

    public static final byte EMPTY = 0;
    public static final byte PLAIN_TEXT = 1;
    public static final byte COLORED = 2;
    public static final byte RAW_JSON = 3;
    public static final byte NBT = 4;

    private static final Text EMPTY_TEXT = new Text() {
        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public byte getType() {
            return Text.EMPTY;
        }

        @Override
        public @NotNull Object getValue() {
            throw new IllegalStateException("An empty text doesn't have value");
        }
    };
    @SuppressWarnings("deprecation")
    private static final JsonParser JSON_PARSER = new JsonParser();

    @NotNull
    public static Text empty() {
        return EMPTY_TEXT;
    }

    @NotNull
    public static Text plain(@NotNull String s) {
        return valueOf(PLAIN_TEXT, s);
    }

    @NotNull
    public static Text colored(@NotNull String s) {
        return valueOf(COLORED, s);
    }

    @NotNull
    @SuppressWarnings("deprecation")
    public static Text rawJson(@NotNull String json) {
        return rawJson(JSON_PARSER.parse(json));
    }

    @NotNull
    public static Text rawJson(@NotNull JsonElement json) {
        return valueOf(RAW_JSON, json);
    }

    @NotNull
    public static Text nbt(@NotNull String snbt) {
        return valueOf(NBT, snbt);
    }

    @NotNull
    public static Text nbt(@NotNull Object tag) {
        return valueOf(NBT, tag);
    }

    @NotNull
    @SuppressWarnings("deprecation")
    public static Text valueOf(@Nullable Object object) {
        if (object == null) {
            return empty();
        } else if (object instanceof String) {
            final String s = (String) object;

            try {
                return valueOf(RAW_JSON, JSON_PARSER.parse(s));
            } catch (JsonParseException ignored) { }

            if (s.indexOf(MStrings.COLOR_CHAR) >= 0) {
                return valueOf(COLORED, object);
            } else {
                return valueOf(PLAIN_TEXT, object);
            }
        } else if (object instanceof JsonElement) {
            return valueOf(RAW_JSON, object);
        } else {
            return valueOf(NBT, object);
        }
    }

    @NotNull
    public static Text valueOf(byte type, @NotNull Object object) {
        return Platform.getInstance().getText(type, object);
    }

    public boolean isEmpty() {
        return false;
    }

    public boolean isString() {
        return false;
    }

    public boolean isPlainText() {
        return false;
    }

    public boolean isColored() {
        return false;
    }

    public boolean isRawJson() {
        return false;
    }

    public boolean isNbt() {
        return false;
    }

    public abstract byte getType();

    @NotNull
    public abstract Object getValue();

    @NotNull
    public StringText getAsString() {
        throw new IllegalStateException("The current text implementation doesn't support conversion to string text");
    }

    @NotNull
    public PlainText getAsPlainText() {
        throw new IllegalStateException("The current text implementation doesn't support conversion to plain text");
    }

    @NotNull
    public Colored getAsColored() {
        throw new IllegalStateException("The current text implementation doesn't support conversion to colored text");
    }

    @NotNull
    public RawJson getAsRawJson() {
        throw new IllegalStateException("The current text implementation doesn't support conversion to raw json");
    }

    @NotNull
    public Nbt<?> getAsNbt() {
        throw new IllegalStateException("The current text implementation doesn't support conversion to nbt");
    }

    @NotNull
    public Text parse(@Nullable Object subject) {
        return placeholders(subject, '%', '%');
    }

    @NotNull
    public Text parse(@Nullable Object subject, @Nullable Object agent) {
        if (agent != null) {
            return placeholders(agent, '%', '%');
        } else {
            return placeholders(subject, '%', '%');
        }
    }

    @NotNull
    public Text parseAgent(@Nullable Object agent) {
        return placeholders(agent, '{', '}');
    }

    @NotNull
    public Text parseAgent(@Nullable Object subject, @Nullable Object agent) {
        return placeholders(agent, '{', '}').placeholders(subject, '%', '%');
    }

    @NotNull
    public Text parseRelational(@Nullable Object subject, @Nullable Object relative) {
        return placeholders(subject, relative, '%', '%');
    }

    @NotNull
    public Text args(@Nullable Object... args) {
        throw new IllegalStateException("The current text implementation doesn't support argument replacement");
    }

    @NotNull
    public Text args(@NotNull Map<String, Object> args) {
        throw new IllegalStateException("The current text implementation doesn't support argument replacement");
    }

    @NotNull
    public Text color() {
        return color('&');
    }

    @NotNull
    public Text color(char colorChar) {
        throw new IllegalStateException("The current text implementation doesn't support coloring");
    }

    @NotNull
    public Text color(@NotNull List<String> colors) {
        return color('&', colors);
    }

    @NotNull
    public Text color(char colorChar, @NotNull List<String> colors) {
        throw new IllegalStateException("The current text implementation doesn't support coloring");
    }

    @NotNull
    public Text placeholders(@Nullable Object subject, char start, char end) {
        return placeholders(subject, null, start, end);
    }

    @NotNull
    public Text placeholders(@Nullable Object subject, @Nullable Object relative, char start, char end) {
        // empty by default
        return this;
    }

    @NotNull
    public Text placeholders(@Nullable Object subject, char start, char end, @NotNull Function<String, Replacer> lookup) {
        return placeholders(subject, null, start, end, lookup);
    }

    @NotNull
    public Text placeholders(@Nullable Object subject, @Nullable Object relative, char start, char end, @NotNull Function<String, Replacer> lookup) {
        throw new IllegalStateException("The current text implementation doesn't support placeholder replacement");
    }

    public static abstract class StringText extends Text {

        private final String value;

        public StringText(@NotNull String value) {
            this.value = value;
        }

        @Override
        public boolean isString() {
            return true;
        }

        @NotNull
        public String getValue() {
            return value;
        }

        @Override
        public @NotNull StringText getAsString() {
            return this;
        }

        @Override
        public @NotNull Text args(@Nullable Object... args) {
            return Text.valueOf(getType(), Strings.replaceArgs(getValue(), args));
        }

        @Override
        public @NotNull Text args(@NotNull Map<String, Object> args) {
            return Text.valueOf(getType(), Strings.replaceArgs(getValue(), args));
        }

        @Override
        public @NotNull Text color(char colorChar) {
            return Text.colored(MStrings.color(colorChar, getValue()));
        }

        @Override
        public @NotNull Text color(char colorChar, @NotNull List<String> colors) {
            return Text.colored(Strings.color(colorChar, getValue(), colors));
        }

        @Override
        public @NotNull Text placeholders(@Nullable Object subject, @Nullable Object relative, char start, char end, @NotNull Function<String, Replacer> lookup) {
            return Text.valueOf(getType(), Strings.replacePlaceholder(subject, relative, getValue(), start, end, lookup));
        }
    }

    public static class PlainText extends StringText {

        public PlainText(@NotNull String value) {
            super(value);
        }

        @Override
        public boolean isPlainText() {
            return true;
        }

        @Override
        public byte getType() {
            return Text.PLAIN_TEXT;
        }

        @Override
        public @NotNull PlainText getAsPlainText() {
            return this;
        }

        @Override
        public @NotNull Colored getAsColored() {
            return Text.valueOf(Text.COLORED, MStrings.color(getValue())).getAsColored();
        }

        @Override
        @SuppressWarnings("deprecation")
        public @NotNull RawJson getAsRawJson() {
            return Text.valueOf(Text.RAW_JSON, JSON_PARSER.parse(getValue())).getAsRawJson();
        }
    }

    public static class Colored extends StringText {

        public Colored(@NotNull String value) {
            super(value);
        }

        @Override
        public boolean isColored() {
            return true;
        }

        @Override
        public byte getType() {
            return Text.COLORED;
        }

        @Override
        public @NotNull Colored getAsColored() {
            return this;
        }

        @Override
        public @NotNull RawJson getAsRawJson() {
            return Text.valueOf(Text.RAW_JSON, com.saicone.mcode.util.text.RawJson.toJson(getValue())).getAsRawJson();
        }

        @Override
        public @NotNull Nbt<?> getAsNbt() {
            return getAsRawJson().getAsNbt();
        }
    }

    public static class RawJson extends Text {

        private final JsonElement value;

        public RawJson(@NotNull JsonElement value) {
            this.value = value;
        }

        @Override
        public boolean isRawJson() {
            return true;
        }

        @Override
        public byte getType() {
            return Text.RAW_JSON;
        }

        @Override
        @NotNull
        public JsonElement getValue() {
            return value;
        }

        @Override
        public @NotNull StringText getAsString() {
            return Text.valueOf(Text.PLAIN_TEXT, getValue().toString()).getAsString();
        }

        @Override
        public @NotNull PlainText getAsPlainText() {
            return Text.valueOf(Text.PLAIN_TEXT, getValue().toString()).getAsPlainText();
        }

        @Override
        public @NotNull Colored getAsColored() {
            return Text.valueOf(Text.COLORED, com.saicone.mcode.util.text.RawJson.fromJson(getValue())).getAsColored();
        }

        @Override
        public @NotNull RawJson getAsRawJson() {
            return this;
        }
    }

    public static class Nbt<T> extends Text {

        private final T value;

        public Nbt(@NotNull T value) {
            this.value = value;
        }

        @Override
        public boolean isNbt() {
            return true;
        }

        @Override
        public byte getType() {
            return Text.NBT;
        }

        @Override
        @NotNull
        public T getValue() {
            return value;
        }

        @Override
        public @NotNull Colored getAsColored() {
            return getAsRawJson().getAsColored();
        }

        @Override
        public @NotNull Nbt<?> getAsNbt() {
            return this;
        }
    }
}
