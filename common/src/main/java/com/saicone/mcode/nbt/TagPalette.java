package com.saicone.mcode.nbt;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public class TagPalette {

    public static final TagPalette COLORS = new TagPalette()
            .base("white")
            .key("aqua")
            .type("gold")
            .type(Tag.STRING, "green")
            .type(Tag.BOOLEAN, "red")
            .type(Tag.END, "dark_red")
            .suffix("red")
            .suffix(Tag.BOOLEAN, "")
            .end("reset");

    public static final TagPalette DEFAULT = new TagPalette()
            .base("\u00a7f%s")
            .key("\u00a7b%s")
            .type("\u00a76%s")
            .type(Tag.STRING, "\u00a7a%s")
            .type(Tag.BOOLEAN, "\u00a7c%s")
            .type(Tag.END, "\u00a74%s")
            .suffix("\u00a7c%s")
            .suffix(Tag.BOOLEAN, "")
            .end("\u00a7r");
    public static final TagPalette JSON = new TagPalette() {
        @Override
        public @NotNull String color(@Nullable Object value, @Nullable String indent, @NotNull TagMapper<Object> mapper) {
            final String result = super.color(value, indent, mapper);
            if (result.isBlank()) {
                return "{}";
            }
            return "[" + result.substring(0, result.length() - 1) + "]";
        }
    }
            .base("{\"type\":\"text\",\"color\":\"white\",\"text\":\"%s\"},")
            .key("{\"type\":\"text\",\"color\":\"aqua\",\"text\":\"%s\"},")
            .type("{\"type\":\"text\",\"color\":\"gold\",\"text\":\"%s\"},")
            .type(Tag.STRING, "{\"type\":\"text\",\"color\":\"green\",\"text\":\"%s\"},")
            .type(Tag.BOOLEAN, "{\"type\":\"text\",\"color\":\"red\",\"text\":\"%s\"},")
            .type(Tag.END, "{\"type\":\"text\",\"color\":\"dark_red\",\"text\":\"%s\"},")
            .suffix("{\"type\":\"text\",\"color\":\"red\",\"text\":\"%s\"},")
            .suffix(Tag.BOOLEAN, "");
    public static final TagPalette ANSI = new TagPalette()
            .base("\u001B[0m%s")
            .key("\u001B[96m%s")
            .type("\u001B[33m%s")
            .type(Tag.STRING, "\u001B[92m%s")
            .type(Tag.BOOLEAN, "\u001B[91m%s")
            .type(Tag.END, "\u001B[31m%s")
            .suffix("\u001B[91m%s")
            .suffix(Tag.BOOLEAN, "")
            .end("\u001B[0m");
    public static final TagPalette MINI_MESSAGE = new TagPalette()
            .base("<white>%s")
            .key("<aqua>%s")
            .type("<gold>%s")
            .type(Tag.STRING, "<green>%s")
            .type(Tag.BOOLEAN, "<red>%s")
            .type(Tag.END, "<dark_red>%s")
            .suffix("<red>%s")
            .suffix(Tag.BOOLEAN, "")
            .end("<reset>");

    private String base = "";
    private String bracket;
    private String colon;
    private String comma;
    private String quote;
    private String key;

    private String defaultType = "";
    private final Map<Tag<?>, String> types = new HashMap<>();

    private String defaultSuffix = "";
    private final Map<Tag<?>, String> suffixes = new HashMap<>();

    private String end = "";

    @NotNull
    public String base() {
        return base;
    }

    @NotNull
    public String bracket() {
        return bracket != null ? bracket : base;
    }

    @NotNull
    public String colon() {
        return colon != null ? colon : base;
    }

    @NotNull
    public String comma() {
        return comma != null ? comma : base;
    }

    @NotNull
    public String quote() {
        return quote != null ? quote : base;
    }

    @NotNull
    public String key() {
        return key != null ? key : base;
    }

    @NotNull
    public String type(@NotNull Tag<?> type) {
        return types.getOrDefault(type, defaultType);
    }

    @NotNull
    public String suffix(@NotNull Tag<?> type) {
        return suffixes.getOrDefault(type, defaultSuffix);
    }

    @NotNull
    public String end() {
        return end;
    }

    @NotNull
    @Contract("_ -> this")
    public TagPalette base(@NotNull String base) {
        this.base = base;
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public TagPalette bracket(@Nullable String bracket) {
        this.bracket = bracket;
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public TagPalette colon(@Nullable String colon) {
        this.colon = colon;
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public TagPalette comma(@Nullable String comma) {
        this.comma = comma;
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public TagPalette quote(@Nullable String quote) {
        this.quote = quote;
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public TagPalette key(@Nullable String key) {
        this.key = key;
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public TagPalette type(@Nullable String defaultType) {
        this.defaultType = defaultType;
        return this;
    }

    @NotNull
    @Contract("_, _ -> this")
    public TagPalette type(@NotNull Tag<?> type, @Nullable String color) {
        if (color == null) {
            this.types.remove(type);
        } else {
            this.types.put(type, color);
        }
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public TagPalette suffix(@Nullable String defaultSuffix) {
        this.defaultSuffix = defaultSuffix;
        return this;
    }

    @NotNull
    @Contract("_, _ -> this")
    public TagPalette suffix(@NotNull Tag<?> type, @Nullable String color) {
        if (color == null) {
            this.suffixes.remove(type);
        } else {
            this.suffixes.put(type, color);
        }
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public TagPalette end(@Nullable String end) {
        this.end = Objects.requireNonNullElse(end, "");
        return this;
    }

    @NotNull
    @Contract("-> new")
    public TagPalette copy() {
        final TagPalette palette = new TagPalette();
        palette.base = this.base;
        palette.bracket = this.bracket;
        palette.colon = this.colon;
        palette.comma = this.comma;
        palette.quote = this.quote;
        palette.key = this.key;
        palette.defaultType = this.defaultType;
        palette.types.putAll(this.types);
        palette.defaultSuffix = this.defaultSuffix;
        palette.suffixes.putAll(this.suffixes);
        palette.end = this.end;
        return palette;
    }

    @NotNull
    public String color(@Nullable Object value, @Nullable String indent) {
        return color(value, indent, TagMapper.DEFAULT);
    }

    @NotNull
    public String color(@Nullable Object value, @Nullable String indent, @NotNull TagMapper<Object> mapper) {
        return color(value, indent == null ? "" : indent, mapper, 0) + end();
    }

    @NotNull
    protected String color(@Nullable Object object, @NotNull String indent, @NotNull TagMapper<Object> mapper, int count) {
        final Object value = mapper.extract(object);
        final Tag<?> tag = Tag.getType(value);
        if (!tag.isValid()) {
            throw new IllegalArgumentException("The object " + value.getClass().getName() + " is not a valid tag");
        }

        switch (tag.getId()) {
            case 0: // end
                // null
                return String.format(type(Tag.END), "null");
            case 3: // int
                // <value>
                return String.format(type(tag), String.valueOf(value));
            case 1: // byte
            case 2: // short
            case 4: // long
            case 5: // float
            case 6: // double
                // <value><suffix>
                return String.format(type(tag) + suffix(tag), String.valueOf(value), String.valueOf(tag.getSuffix()));
            case 8:
                // "<value>"
                return String.format(quote() + type(Tag.STRING) + quote(), value);
            case 7: // byte[]
            case 11: // int[]
            case 12: // long[]
                // [<suffix>; <value><suffix>, <value><suffix>, <value><suffix>...]
                return colorArray(tag, value);
            case 9: // List
                // [<pretty value>, <pretty value>, <pretty value>...]
                return colorList((List<?>) value, indent, mapper, count);
            case 10: // Compound
                // {<key>: <pretty value>, <key>: <pretty value>, <key>: <pretty value>...}
                return colorCompound((Map<?, ?>) value, indent, mapper, count);
            default:
                throw new IllegalArgumentException("Invalid tag id: " + tag.getId());
        }
    }

    @NotNull
    protected String colorArray(@NotNull Tag<?> tag, @NotNull Object array) {
        final StringJoiner joiner = new StringJoiner(String.format(comma(), ", "), String.format(bracket() + suffix(tag) + comma(), "[", String.valueOf(tag.getSuffix()), "; "), String.format(bracket(), "]"));
        final String format;
        if (tag == Tag.INT_ARRAY) {
            format = type(tag);
        } else {
            format = type(tag) + suffix(tag);
        }
        final int size = Array.getLength(array);
        for (int i = 0; i < size; i++) {
            final Object value = Array.get(array, i);
            joiner.add(String.format(format, value, String.valueOf(tag.getSuffix())));
        }
        return joiner.toString();
    }

    @NotNull
    protected String colorList(@NotNull List<?> list, @NotNull String indent, @NotNull TagMapper<Object> mapper, int count) {
        if (list.isEmpty()) {
            return String.format(bracket(), "[]");
        }

        final StringJoiner joiner;
        if (indent.isEmpty() || (list.size() <= 8 && Tag.getType(mapper.extract(list.get(0))).isPrimitive())) {
            joiner = new StringJoiner(
                    String.format(comma(), ", "),
                    String.format(bracket(), "["),
                    String.format(bracket(), "]"));
        } else {
            final String s = indent.repeat(count + 1);
            joiner = new StringJoiner(
                    String.format(comma(), ",\n" + s),
                    String.format(bracket(), "[\n" + s),
                    String.format(bracket(), '\n' + indent.repeat(count) + ']'));
        }

        for (Object o : list) {
            joiner.add(color(o, indent, mapper, count + 1));
        }
        return joiner.toString();
    }

    @NotNull
    protected String colorCompound(@NotNull Map<?, ?> map, @NotNull String indent, @NotNull TagMapper<Object> mapper, int count) {
        if (map.isEmpty()) {
            return String.format(base(), "{}");
        }

        final StringJoiner joiner;
        if (indent.isEmpty()) {
            joiner = new StringJoiner(
                    String.format(comma(), ", "),
                    String.format(base(), "{"),
                    String.format(base(), "}"));
        } else {
            final String s = indent.repeat(count + 1);
            joiner = new StringJoiner(
                    String.format(comma(), ",\n" + s),
                    String.format(base(), "{\n" + s),
                    String.format(base(), '\n' + indent.repeat(count) + '}'));
        }

        for (var entry : map.entrySet()) {
            joiner.add(String.format(key() + colon(), String.valueOf(entry.getKey()), ": ") + color(entry.getValue(), indent, mapper, count + 1));
        }
        return joiner.toString();
    }
}
