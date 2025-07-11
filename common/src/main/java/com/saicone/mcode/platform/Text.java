package com.saicone.mcode.platform;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.saicone.mcode.Platform;
import com.saicone.mcode.util.text.MStrings;
import com.saicone.mcode.util.text.Replacer;
import com.saicone.mcode.util.text.Strings;
import com.saicone.mcode.util.text.TextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public abstract class Text {

    public static final byte EMPTY = 0;
    public static final byte PLAIN_TEXT = 1;
    public static final byte COLORED = 2;
    public static final byte RAW_JSON = 3;
    public static final byte NBT = 4;

    private static final Text EMPTY_TEXT = new Text(MC.VERSION) {
        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public @NotNull MC getVersion() {
            return MC.version();
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
        return plain(MC.version(), s);
    }

    @NotNull
    public static Text plain(@NotNull MC version, @NotNull String s) {
        return valueOf(PLAIN_TEXT, version, s);
    }

    @NotNull
    public static Text colored(@NotNull String s) {
        return valueOf(COLORED, null, s);
    }

    @NotNull
    public static Text colored(@NotNull MC version, @NotNull String s) {
        return valueOf(COLORED, version, s);
    }

    @NotNull
    @SuppressWarnings("deprecation")
    public static Text json(@NotNull String json) {
        return json(JSON_PARSER.parse(json));
    }

    @NotNull
    public static Text json(@NotNull JsonElement json) {
        return valueOf(RAW_JSON, null, json);
    }

    @NotNull
    @SuppressWarnings("deprecation")
    public static Text json(@NotNull MC version, @NotNull String json) {
        return json(version, JSON_PARSER.parse(json));
    }

    @NotNull
    public static Text json(@NotNull MC version, @NotNull JsonElement json) {
        return valueOf(RAW_JSON, version, json);
    }

    @NotNull
    public static Text nbt(@NotNull String snbt) {
        return valueOf(NBT, null, snbt);
    }

    @NotNull
    public static Text nbt(@NotNull Object tag) {
        return valueOf(NBT, null, tag);
    }

    @NotNull
    public static Text nbt(@NotNull MC version, @NotNull String snbt) {
        return valueOf(NBT, version, snbt);
    }

    @NotNull
    public static Text nbt(@NotNull MC version, @NotNull Object tag) {
        return valueOf(NBT, version, tag);
    }

    @NotNull
    public static Text valueOf(@Nullable Object object) {
        return valueOf(null, object);
    }

    @NotNull
    @SuppressWarnings("deprecation")
    public static Text valueOf(@Nullable MC version, @Nullable Object object) {
        if (object == null) {
            return empty();
        } else if (object instanceof String) {
            final String s = (String) object;

            try {
                return valueOf(RAW_JSON, version, JSON_PARSER.parse(s));
            } catch (JsonParseException ignored) { }

            if (s.indexOf(MStrings.COLOR_CHAR) >= 0) {
                return valueOf(COLORED, version, object);
            } else {
                return valueOf(PLAIN_TEXT, version, object);
            }
        } else if (object instanceof JsonElement) {
            return valueOf(RAW_JSON, version, object);
        } else {
            return valueOf(NBT, version, object);
        }
    }

    @NotNull
    public static Text valueOf(byte type, @NotNull Object object) {
        return valueOf(type, null, object);
    }

    @NotNull
    public static Text valueOf(byte type, @Nullable MC version, @NotNull Object object) {
        if (type == EMPTY || (object instanceof JsonElement && ((JsonElement) object).isJsonNull())) {
            return empty();
        }
        return Platform.getInstance().getText(type, version, object);
    }

    private final MC version;

    public Text(@NotNull MC version) {
        this.version = version;
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

    public boolean isJson() {
        return false;
    }

    public boolean isNbt() {
        return false;
    }

    @NotNull
    public MC getVersion() {
        return version;
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
    public Text.Json getAsJson() {
        throw new IllegalStateException("The current text implementation doesn't support conversion to raw json");
    }

    @NotNull
    public Nbt<?> getAsNbt() {
        throw new IllegalStateException("The current text implementation doesn't support conversion to nbt");
    }

    @NotNull
    public Text apply(@NotNull UnaryOperator<String> operator) {
        throw new IllegalStateException("The current text implementation doesn't support text transformation");
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
        return apply(s -> Strings.replaceArgs(s, args));
    }

    @NotNull
    public Text args(@NotNull Map<String, Object> args) {
        return apply(s -> Strings.replaceArgs(s, args));
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
    public Text center() {
        return center(MStrings.CHAT_WIDTH);
    }

    @NotNull
    public Text center(int width) {
        return center(width, '&');
    }

    @NotNull
    public Text center(int width, char colorChar) {
        return apply(s -> MStrings.centerText(s, width, colorChar));
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
        return apply(s -> Strings.replacePlaceholder(subject, relative, s, start, end, lookup));
    }

    public static abstract class StringText extends Text {

        private final String value;

        public StringText(@NotNull MC version, @NotNull String value) {
            super(version);
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
        public @NotNull Text apply(@NotNull UnaryOperator<String> operator) {
            return Text.valueOf(getType(), getVersion(), operator.apply(getValue()));
        }

        @Override
        public @NotNull Text color(char colorChar) {
            return Text.colored(MStrings.color(colorChar, getValue()));
        }

        @Override
        public @NotNull Text color(char colorChar, @NotNull List<String> colors) {
            return Text.colored(Strings.color(colorChar, getValue(), colors));
        }
    }

    public static class PlainText extends StringText {

        public PlainText(@NotNull MC version, @NotNull String value) {
            super(version, value);
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
            return Text.valueOf(Text.COLORED, getVersion(), MStrings.color(getValue())).getAsColored();
        }

        @Override
        @SuppressWarnings("deprecation")
        public @NotNull Text.Json getAsJson() {
            return Text.valueOf(Text.RAW_JSON, getVersion(), JSON_PARSER.parse(getValue())).getAsJson();
        }
    }

    public static class Colored extends StringText {

        public Colored(@NotNull String value) {
            this(TextComponent.readVersion(value), value);
        }

        public Colored(@NotNull MC version, @NotNull String value) {
            super(version, value);
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
        public @NotNull Text.Json getAsJson() {
            return Text.valueOf(Text.RAW_JSON, getVersion(), TextComponent.toJson(getValue())).getAsJson();
        }

        @Override
        public @NotNull Nbt<?> getAsNbt() {
            return getAsJson().getAsNbt();
        }
    }

    public static class Json extends Text {

        private final JsonElement value;

        public Json(@NotNull JsonElement value) {
            this(TextComponent.readVersion(value), value);
        }

        public Json(@NotNull MC version, @NotNull JsonElement value) {
            super(version);
            this.value = value;
        }

        @Override
        public boolean isJson() {
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
            return Text.valueOf(Text.PLAIN_TEXT, getVersion(), getValue().toString()).getAsString();
        }

        @Override
        public @NotNull PlainText getAsPlainText() {
            return Text.valueOf(Text.PLAIN_TEXT, getVersion(), getValue().toString()).getAsPlainText();
        }

        @Override
        public @NotNull Colored getAsColored() {
            return Text.valueOf(Text.COLORED, getVersion(), TextComponent.fromJson(getValue())).getAsColored();
        }

        @Override
        public @NotNull Text.Json getAsJson() {
            return this;
        }

        @Override
        public @NotNull Text apply(@NotNull UnaryOperator<String> operator) {
            return Text.json(getVersion(), TextComponent.apply(getValue(), operator));
        }
    }

    public static class Nbt<T> extends Text {

        private final T value;

        public Nbt(@NotNull MC version, @NotNull T value) {
            super(version);
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
            return getAsJson().getAsColored();
        }

        @Override
        public @NotNull Nbt<?> getAsNbt() {
            return this;
        }
    }
}
