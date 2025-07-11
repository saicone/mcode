package com.saicone.mcode.util.text;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.saicone.mcode.platform.MC;
import com.saicone.nbt.Tag;
import com.saicone.nbt.TagMapper;
import com.saicone.nbt.TagType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class TextComponent {

    @SuppressWarnings("deprecation")
    private static final JsonParser JSON_PARSER = new JsonParser();

    private static final char COLOR_CHAR = '\u00a7';
    private static final Map<Character, String> COLOR_NAMES = Map.ofEntries(
            // Dark colors
            Map.entry('0', "black"),
            Map.entry('1', "dark_blue"),
            Map.entry('2', "dark_green"),
            Map.entry('3', "dark_aqua"),
            Map.entry('4', "dark_red"),
            Map.entry('5', "dark_purple"),
            Map.entry('6', "gold"),
            Map.entry('7', "gray"),
            // Light colors
            Map.entry('8', "dark_gray"),
            Map.entry('9', "blue"),
            Map.entry('a', "green"),
            Map.entry('b', "aqua"),
            Map.entry('c', "red"),
            Map.entry('d', "light_purple"),
            Map.entry('e', "yellow"),
            Map.entry('f', "white"),
            // Uppercase
            Map.entry('A', "green"),
            Map.entry('B', "aqua"),
            Map.entry('C', "red"),
            Map.entry('D', "light_purple"),
            Map.entry('E', "yellow"),
            Map.entry('F', "white")
    );
    private static final Map<String, Character> COLOR_CODES = Map.ofEntries(
            // Dark colors
            Map.entry("black", '0'),
            Map.entry("dark_blue", '1'),
            Map.entry("dark_green", '2'),
            Map.entry("dark_aqua", '3'),
            Map.entry("dark_red", '4'),
            Map.entry("dark_purple", '5'),
            Map.entry("gold", '6'),
            Map.entry("gray", '7'),
            // Light colors
            Map.entry("dark_gray", '8'),
            Map.entry("blue", '9'),
            Map.entry("green", 'a'),
            Map.entry("aqua", 'b'),
            Map.entry("red", 'c'),
            Map.entry("light_purple", 'd'),
            Map.entry("yellow", 'e'),
            Map.entry("white", 'f')
    );

    private static final JsonObject EMPTY = json(Map.of(
            "type", "text",
            "text", "",
            "italic", false
    ));
    private static final JsonObject RESET = json(Map.of(
            "type", "text",
            "text", "",
            "bold", false,
            "italic", false,
            "underlined", false,
            "strikethrough", false,
            "obfuscated", false
    ));

    @NotNull
    public static JsonArray toJson(@NotNull String s) {
        return toJson(MC.version(), s);
    }

    @NotNull
    public static JsonArray toJson(@NotNull MC version, @NotNull String s) {
        return toJson(version, s, "white");
    }

    @NotNull
    public static JsonArray toJson(@NotNull MC version, @NotNull String s, @NotNull String defaultColor) {
        final JsonArray array = new JsonArray();
        final boolean containsUrl = s.contains("http");
        if (s.isBlank() || (s.indexOf(COLOR_CHAR) < 0 && !containsUrl)) {
            array.add(new JsonPrimitive(s));
            return array;
        }

        JsonObject object = EMPTY.deepCopy();
        object.add("color", new JsonPrimitive(defaultColor));
        StringBuilder text = new StringBuilder();
        boolean obfuscated = false;
        boolean bold = false;
        boolean strikethrough = false;
        boolean underlined = false;
        boolean italic = false;

        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c == COLOR_CHAR && i + 1 < s.length()) {
                final char c1 = s.charAt(i + 1);
                String color = COLOR_NAMES.get(c1);
                if (color == null) {
                    if (c1 == '#') { // hex
                        color = MStrings.isHexFormat(s, i + 2, 1, COLOR_CHAR);
                        if (color != null) {
                            color = "#" + color;
                            i = i + 6;
                        }
                    } else if (MStrings.BUNGEE_HEX && c1 == 'x') { // bungee hex
                        color = MStrings.isHexFormat(s, i + 2, 2, COLOR_CHAR);
                        if (color != null) {
                            color = "#" + color;
                            i = i + 12;
                        }
                    }
                }
                if (color != null) { // color
                    if (text.length() > 0) {
                        object.add("text", new JsonPrimitive(text.toString()));
                        array.add(object);
                        object = new JsonObject();
                    }
                    object.add("color", new JsonPrimitive(color));
                    if (obfuscated) {
                        object.add("obfuscated", new JsonPrimitive(false));
                    }
                    if (bold) {
                        object.add("bold", new JsonPrimitive(false));
                    }
                    if (strikethrough) {
                        object.add("strikethrough", new JsonPrimitive(false));
                    }
                    if (underlined) {
                        object.add("underlined", new JsonPrimitive(false));
                    }
                    if (italic) {
                        object.add("italic", new JsonPrimitive(false));
                    }
                } else if (c1 == 'r') { // reset
                    if (text.length() > 0) {
                        object.add("text", new JsonPrimitive(text.toString()));
                        array.add(object);
                    }
                    array.add(RESET.deepCopy());
                    object = new JsonObject();
                    object.add("color", new JsonPrimitive(defaultColor));
                    text = new StringBuilder();
                } else { // style
                    switch (c1) {
                        case 'k':
                            object.add("obfuscated", new JsonPrimitive(true));
                            obfuscated = true;
                            break;
                        case 'l':
                            object.add("bold", new JsonPrimitive(true));
                            bold = true;
                            break;
                        case 'm':
                            object.add("strikethrough", new JsonPrimitive(true));
                            strikethrough = true;
                            break;
                        case 'n':
                            object.add("underlined", new JsonPrimitive(true));
                            underlined = true;
                            break;
                        case 'o':
                            object.add("italic", new JsonPrimitive(true));
                            italic = true;
                            break;
                        default:
                            text.append(c);
                            continue;
                    }
                    i++;
                    continue;
                }
                obfuscated = false;
                bold = false;
                strikethrough = false;
                underlined = false;
                italic = false;
                i++;
            } else if (c == 'h' || c == 'H') { // open url
                final String start = s.substring(i, i + 8).toLowerCase();
                if (start.equals("https://") || start.startsWith("http://")) {
                    if (text.length() > 0) {
                        final JsonObject plain = object.deepCopy();
                        plain.add("text", new JsonPrimitive(text.toString()));
                        array.add(plain);
                    }

                    int end = s.indexOf(' ', i);
                    if (end < 0) {
                        end = s.length();
                    }
                    final JsonPrimitive url = new JsonPrimitive(s.substring(i, end));
                    object.add("text", url);

                    final JsonObject clickEvent = new JsonObject();
                    clickEvent.add("action", new JsonPrimitive("open_url"));
                    if (version.isNewerThanOrEquals(MC.V_1_21_5)) {
                        clickEvent.add("url", url);
                        object.add("click_event", clickEvent);
                    } else {
                        clickEvent.add("value", url);
                        object.add("clickEvent", clickEvent);
                    }

                    array.add(object);
                    object = new JsonObject();
                    text = new StringBuilder();
                    i = end - 1;
                } else {
                    text.append(c);
                }
            } else {
                text.append(c);
            }
        }

        if (text.length() > 0) {
            object.add("text", new JsonPrimitive(text.toString()));
            array.add(object);
        }

        return array;
    }

    @NotNull
    @SuppressWarnings("deprecation")
    public static String fromJson(@NotNull String json) {
        return fromJson(JSON_PARSER.parse(json));
    }

    @NotNull
    public static String fromJson(@NotNull JsonElement element) {
        return fromJson(new StringBuilder(), element).toString();
    }

    @NotNull
    public static StringBuilder fromJson(@NotNull StringBuilder builder, @NotNull JsonElement element) {
        if (element instanceof JsonObject) {
            fromJson(builder, (JsonObject) element);
        } else if (element instanceof JsonArray) {
            fromJson(builder, (JsonArray) element);
        } else if (element instanceof JsonPrimitive) {
            fromJson(builder, (JsonPrimitive) element);
        } else {
            builder.append("null");
        }
        return builder;
    }

    private static void fromJson(@NotNull StringBuilder builder, @NotNull JsonObject object) {
        if (object.has("color")) {
            final String color = object.getAsJsonPrimitive("color").getAsString();
            if (color.startsWith("#")) {
                builder.append(MStrings.toRgb('\0', color.substring(1)));
            } else {
                final Character c = COLOR_CODES.get(color);
                if (c != null) {
                    builder.append(c);
                }
            }
        } else {
            builder.append(COLOR_CHAR).append('r');
        }
        if (object.has("obfuscated") && object.getAsJsonPrimitive("obfuscated").getAsBoolean()) {
            builder.append(COLOR_CHAR).append('k');
        }
        if (object.has("bold") && object.getAsJsonPrimitive("bold").getAsBoolean()) {
            builder.append(COLOR_CHAR).append('l');
        }
        if (object.has("strikethrough") && object.getAsJsonPrimitive("strikethrough").getAsBoolean()) {
            builder.append(COLOR_CHAR).append('m');
        }
        if (object.has("underlined") && object.getAsJsonPrimitive("underlined").getAsBoolean()) {
            builder.append(COLOR_CHAR).append('n');
        }
        if (object.has("italic") && object.getAsJsonPrimitive("italic").getAsBoolean()) {
            builder.append(COLOR_CHAR).append('o');
        }
        if (object.has("extra")) {
            fromJson(builder, object.getAsJsonArray("extra"));
        }
    }

    private static void fromJson(@NotNull StringBuilder builder, @NotNull JsonArray array) {
        for (JsonElement element : array) {
            fromJson(builder, element);
        }
    }

    private static void fromJson(@NotNull StringBuilder builder, @NotNull JsonPrimitive primitive) {
        if (primitive.isBoolean()) {
            builder.append(primitive.getAsBoolean());
        } else if (primitive.isNumber()) {
            builder.append(primitive.getAsNumber());
        } else {
            builder.append(primitive.getAsString());
        }
    }

    @NotNull
    private static JsonObject json(@NotNull Map<String, Object> map) {
        final JsonObject object = new JsonObject();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            final Object value = entry.getValue();
            if (value instanceof Boolean) {
                object.add(entry.getKey(), new JsonPrimitive((Boolean) value));
            } else if (value instanceof Number) {
                object.add(entry.getKey(), new JsonPrimitive((Number) value));
            } else if (value instanceof String) {
                object.add(entry.getKey(), new JsonPrimitive((String) value));
            } else if (value instanceof Character) {
                object.add(entry.getKey(), new JsonPrimitive((Character) value));
            }
        }

        return object;
    }

    @NotNull
    public static MC readVersion(@NotNull String s) {
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c == COLOR_CHAR && i + 1 < s.length()) {
                final char c1 = s.charAt(i + 1);
                String color = COLOR_NAMES.get(c1);
                if (color == null) {
                    if (c1 == '#') { // hex
                        color = MStrings.isHexFormat(s, i + 2, 1, COLOR_CHAR);
                        if (color != null) {
                            return MC.V_1_16;
                        }
                    } else if (MStrings.BUNGEE_HEX && c1 == 'x') { // bungee hex
                        color = MStrings.isHexFormat(s, i + 2, 2, COLOR_CHAR);
                        if (color != null) {
                            return MC.V_1_16;
                        }
                    }
                }
            }
        }
        return MC.V_1_7;
    }

    @NotNull
    public static MC readVersion(@NotNull JsonElement element) {
        final MC version = readVersion0(element);
        return version != null ? version : MC.version();
    }

    @Nullable
    private static MC readVersion0(@NotNull JsonElement element) {
        if (element.isJsonObject()) {
            return readVersion(element.getAsJsonObject());
        } else if (element.isJsonArray()) {
            MC result = MC.V_1_7;
            for (JsonElement e : element.getAsJsonArray()) {
                result = MC.max(result, readVersion0(e));
            }
            return result;
        } else {
            return null;
        }
    }

    @NotNull
    public static MC readVersion(@NotNull JsonObject object) {
        MC result = MC.V_1_7;

        if (object.has("click_event")) {
            final JsonElement action = get(object, "click_event", "action");
            if (matches(action, "custom") || matches(action, "show_dialog")) {
                result = MC.V_1_21_6;
            } else {
                result = MC.V_1_21_5;
            }
        } else if (object.has("hover_event")) {
            result = MC.V_1_21_5;
        } else if (object.has("shadow_color")) {
            result = MC.V_1_21_4;
        } else if (get(object, "hoverEvent", "contents", "id") != null || get(object, "hoverEvent", "contents", "components") != null) {
            result = MC.V_1_20_5;
        } else if (object.has("type")) {
            result = MC.V_1_20_3;
        } else if (object.has("font") || get(object, "hoverEvent", "contents") != null || matches(object.get("color"), s -> s.startsWith("#"))) {
            result = MC.V_1_16;
        } else if (matches(get(object, "clickEvent", "action"), "copy_to_clipboard") || object.has("storage")) {
            result = MC.V_1_15;
        } else if (object.has("interpret") || object.has("block") || object.has("entity")) {
            result = MC.V_1_14;
        } else if (object.has("keybind")) {
            result = MC.V_1_12;
        } else if (object.has("selector") || object.has("score") || object.has("insertion")) {
            result = MC.V_1_8;
        }

        final JsonElement extra = object.get("extra");
        if (extra != null && extra.isJsonArray()) {
            return MC.max(result, readVersion0(extra));
        }
        return result;
    }

    @NotNull
    public static <T> MC readVersion(@NotNull T tag, @NotNull TagMapper<T> mapper) {
        final MC version = readVersion0(tag, mapper);
        return version != null ? version : MC.version();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static <T> MC readVersion0(@NotNull T tag, @NotNull TagMapper<T> mapper) {
        final byte type = mapper.typeId(tag);
        if (type == Tag.COMPOUND) {
            return readVersion((Map<String, T>) mapper.extract(tag), mapper);
        } else if (type == Tag.LIST) {
            MC result = MC.V_1_7;
            for (T element : (List<T>) mapper.extract(tag)) {
                result = MC.max(result, readVersion0(element, mapper));
            }
            return result;
        } else {
            return null;
        }
    }

    @NotNull
    public static <T> MC readVersion(@NotNull Map<String, T> map, @NotNull TagMapper<T> mapper) {
        MC result = MC.V_1_7;

        if (map.containsKey("click_event")) {
            final Object action = mapper.extract(get(map, mapper, "click_event", "action"));
            if (matches(action, "custom") || matches(action, "show_dialog")) {
                result = MC.V_1_21_6;
            } else {
                result = MC.V_1_21_5;
            }
        } else if (map.containsKey("hover_event")) {
            result = MC.V_1_21_5;
        } else if (map.containsKey("shadow_color")) {
            result = MC.V_1_21_4;
        } else if (get(map, mapper, "hoverEvent", "contents", "id") != null || get(map, mapper, "hoverEvent", "contents", "components") != null) {
            result = MC.V_1_20_5;
        } else if (map.containsKey("type")) {
            result = MC.V_1_20_3;
        } else if (map.containsKey("font") || get(map, mapper, "hoverEvent", "contents") != null || matches(mapper.extract(map.get("color")), s -> s.startsWith("#"))) {
            result = MC.V_1_16;
        } else if (matches(mapper.extract(get(map, mapper, "clickEvent", "action")), "copy_to_clipboard") || map.containsKey("storage")) {
            result = MC.V_1_15;
        } else if (map.containsKey("interpret") || map.containsKey("block") || map.containsKey("entity")) {
            result = MC.V_1_14;
        } else if (map.containsKey("keybind")) {
            result = MC.V_1_12;
        } else if (map.containsKey("selector") || map.containsKey("score") || map.containsKey("insertion")) {
            result = MC.V_1_8;
        }

        final T extra = map.get("extra");
        if (mapper.typeId(extra) == Tag.LIST) {
            return MC.max(result, readVersion0(extra, mapper));
        }
        return result;
    }

    private static boolean matches(@Nullable Object object, @NotNull String s) {
        return matches(object, source -> source.equalsIgnoreCase(s));
    }

    private static boolean matches(@Nullable Object object, @NotNull Predicate<String> predicate) {
        if (object instanceof JsonPrimitive) {
            object = ((JsonPrimitive) object).getAsString();
        }
        if (object instanceof String) {
            if (((String) object).toLowerCase().startsWith("minecraft:")) {
                return predicate.test(((String) object).substring(10));
            } else {
                return predicate.test((String) object);
            }
        }
        return false;
    }

    @Nullable
    private static JsonElement get(@NotNull JsonElement object, @NotNull String... path) {
        if (object.isJsonObject()) {
            int count = 0;
            for (@NotNull String key : path) {
                count++;
                object = object.getAsJsonObject().get(key);
                if (!object.isJsonObject()) {
                    return count == path.length ? object : null;
                }
            }
            return object;
        }
        return null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static <T> T get(@NotNull Map<String, T> map, @NotNull TagMapper<T> mapper, @NotNull String... path) {
        T tag = null;
        int count = 0;
        for (@NotNull String key : path) {
            count++;
            tag = map.get(key);
            if (mapper.typeId(tag) != Tag.COMPOUND) {
                return count == path.length ? tag : null;
            }
            map = (Map<String, T>) mapper.extract(tag);
        }
        return tag;
    }

    @NotNull
    public static JsonElement apply(@NotNull JsonElement element, @NotNull UnaryOperator<String> operator) {
        if (element.isJsonObject()) {
            final JsonObject object = new JsonObject();
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                object.add(entry.getKey(), apply(entry.getValue(), operator));
            }
            return object;
        } else if (element.isJsonArray()) {
            final JsonArray array = new JsonArray();
            for (JsonElement e : element.getAsJsonArray()) {
                array.add(apply(e, operator));
            }
            return array;
        } else if (element.isJsonPrimitive() && ((JsonPrimitive) element).isString()) {
            return new JsonPrimitive(operator.apply(element.getAsString()));
        } else {
            return element;
        }
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static <T> T apply(@NotNull T tag, @NotNull TagMapper<T> mapper, @NotNull UnaryOperator<String> operator) {
        final byte type = mapper.typeId(tag);
        if (type == Tag.COMPOUND) {
            final Map<String, T> map = new HashMap<>();
            for (Map.Entry<String, T> entry : ((Map<String, T>) mapper.extract(tag)).entrySet()) {
                map.put(entry.getKey(), apply(entry.getValue(), mapper, operator));
            }
            return mapper.build(TagType.COMPOUND, map);
        } else if (type == Tag.LIST) {
            final List<T> list = new ArrayList<>();
            for (T element : (List<T>) mapper.extract(tag)) {
                list.add(apply(element, mapper, operator));
            }
            return mapper.build(TagType.LIST, list);
        } else if (type == Tag.STRING) {
            return mapper.build(TagType.STRING, operator.apply((String) mapper.extract(tag)));
        } else {
            return tag;
        }
    }
}
