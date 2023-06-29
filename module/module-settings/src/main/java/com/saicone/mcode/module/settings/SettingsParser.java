package com.saicone.mcode.module.settings;

import com.saicone.mcode.module.settings.node.SettingsNode;
import com.saicone.mcode.util.Strings;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class SettingsParser {

    private static final Pattern NODE_VARIABLE = Pattern.compile("\\$\\{([^}]+)}");
    private static final Map<String, Supplier<SettingsParser>> SETTINGS_PARSER = new HashMap<>();
    public static final Map<String, BiFunction<Settings, String[], Object>> NODE_PARSER;

    static {
        register("xml", "com.saicone.mcode.module.settings.parser.XmlParser");
        register("json", "com.saicone.mcode.module.settings.parser.JsonParser");
        register("yml", "com.saicone.mcode.module.settings.parser.YamlParser");
        register("yaml", "com.saicone.mcode.module.settings.parser.YamlParser");
        register("toml", "com.saicone.mcode.module.settings.parser.TomlParser");
        register("conf", "com.saicone.mcode.module.settings.parser.HoconParser");
        NODE_PARSER = Map.of(
                "node", (settings, args) -> settings.get(args[0].split("\\.")).getValue(),
                "size", (settings, args) -> settings.get(args[0].split("\\.")).asList().size(),
                "args", (settings, args) -> Strings.replaceArgs(
                        settings.get(args[0].split("\\.")).asString("null"),
                        Arrays.copyOfRange(args, 1, args.length, Object[].class)
                ),
                "join", (settings, args) -> {
                    if (args.length < 2) {
                        return args[0];
                    }
                    final List<String> list = Arrays.asList(args[0].split("\n"));
                    final int start = args.length > 2 ? Integer.parseInt(args[2]) : 0;
                    if (start < 1) {
                        return String.join(args[0], list);
                    }
                    final int end = args.length > 3 ? Integer.parseInt(args[3]) : list.size();
                    return String.join(args[0], list.subList(start, end));
                },
                "split", (settings, args) -> args[0].split(args[1]),
                "math", (settings, args) -> {
                    Double result = null;
                    String format = "#";
                    for (int i = 0; i < args.length; i = i + 2) {
                        if (i + 1 >= args.length) {
                            format = args[i];
                            break;
                        }
                        final double number = Double.parseDouble(args[i]);
                        if (result == null) {
                            result = number;
                            continue;
                        }
                        switch (args[i - 1]) {
                            case "+":
                                result = result + number;
                                break;
                            case "-":
                                result = result - number;
                                break;
                            case "*":
                                result = result * number;
                                break;
                            case "/":
                                result = result / number;
                                break;
                            case "min":
                                result = Math.min(result, number);
                                break;
                            case "max":
                                result = Math.max(result, number);
                                break;
                        }
                    }
                    final String type;
                    final int index = format.indexOf('?');
                    if (index >= 0) {
                        format = index == 0 ? "#" : format.substring(0, index);
                        type = format.substring(index + 1);
                    } else {
                        return result == null ? "null" : new DecimalFormat(format).format(result);
                    }
                    final String formatted = new DecimalFormat(format).format(result == null ? "-1.0" : result);
                    switch (type.trim().toLowerCase()) {
                        case "byte":
                            return Byte.parseByte(formatted);
                        case "short":
                            return Short.parseShort(formatted);
                        case "int":
                        case "integer":
                            return Integer.parseInt(formatted);
                        case "float":
                            return Float.parseFloat(formatted);
                        case "long":
                            return Long.parseLong(formatted);
                        case "double":
                            return Double.parseDouble(formatted);
                        default:
                            return "<invalid type>";
                    }
                }
        );
    }

    protected Map<String, BiFunction<Settings, String[], Object>> nodeParser;

    @Nullable
    public static SettingsParser of(@NotNull String type) {
        final String key = type.toLowerCase();
        if (SETTINGS_PARSER.containsKey(key)) {
            return SETTINGS_PARSER.get(key).get();
        }
        return null;
    }

    public static boolean contains(@NotNull String type) {
        return SETTINGS_PARSER.containsKey(type);
    }

    public static void register(@NotNull String type, @NotNull Supplier<SettingsParser> supplier) {
        SETTINGS_PARSER.put(type.toLowerCase(), supplier);
    }

    public static boolean register(@NotNull String type, @NotNull String className) {
        try {
            return register(type, Class.forName(className));
        } catch (ClassNotFoundException ignored) {
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static boolean register(@NotNull String type, @NotNull Class<?> clazz) {
        try {
            if (SettingsParser.class.isAssignableFrom(clazz)) {
                final Constructor<? extends SettingsParser> constructor = (Constructor<? extends SettingsParser>) clazz.getDeclaredConstructor();
                register(type, () -> {
                    try {
                        return constructor.newInstance();
                    } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                        return null;
                    }
                });
                return true;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return false;
    }

    @NotNull
    public Map<String, BiFunction<Settings, String[], Object>> getNodeParser() {
        return nodeParser != null ? nodeParser : NODE_PARSER;
    }

    @Nullable
    public BiFunction<Settings, String[], Object> getNodeParser(@NotNull String key) {
        return getNodeParser().getOrDefault(key, getNodeParser().get("node"));
    }

    public void setNodeParser(Map<String, BiFunction<Settings, String[], Object>> nodeParser) {
        this.nodeParser = nodeParser;
    }

    public boolean load(@NotNull Reader reader, @NotNull Settings settings) {
        try {
            final Settings set = read(reader);
            parse(set, set);
            settings.clear();
            for (SettingsNode node : set.getValue()) {
                settings.set(node.getKey(), node);
            }
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    public Settings read(@NotNull Reader reader) throws Throwable {
        throw new RuntimeException("Unable to read settings");
    }

    public boolean save(@NotNull Settings settings, @NotNull Writer writer) {
        try {
            write(settings, writer);
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    public void write(@NotNull Settings settings, @NotNull Writer writer) throws Throwable {
        throw new RuntimeException("Unable to write settings");
    }

    @Nullable
    @Contract("_, !null -> !null")
    @SuppressWarnings("unchecked")
    public Object parse(@NotNull Settings settings, @Nullable Object object) {
        if (object instanceof Settings) {
            for (SettingsNode node : ((Settings) object).getValue()) {
                parse(settings, node);
            }
        } else if (object instanceof SettingsNode) {
            ((SettingsNode) object).replaceValue(parse(settings, ((SettingsNode) object).getValue()));
        } else if (object instanceof Map) {
            for (var entry : ((Map<?, Object>) object).entrySet()) {
                entry.setValue(parse(settings, entry.getValue()));
            }
        } else if (object instanceof List) {
            ((List<Object>) object).replaceAll(o -> parse(settings, o));
        } else if (object instanceof String[]) {
            final String[] array = (String[]) object;
            for (int i = 0; i < array.length; i++) {
                array[i] = parseText(settings, array[i]);
            }
        } else if (object instanceof String) {
            String s = (String) object;
            final Matcher matcher = NODE_VARIABLE.matcher(s);
            boolean first = true;
            while (matcher.find()) {
                if (first) {
                    first = false;
                    if (s.equals(matcher.group())) {
                        return parse(settings, matcher.group(1));
                    }
                }
                s = matcher.replaceFirst(parseText(settings, matcher.group(1)));
            }
            return s;
        }
        return object;
    }

    @NotNull
    protected Object parse(@NotNull Settings settings, @NotNull String text) {
        final String key;
        final String s;
        final int index = text.indexOf(':');
        if (index > 0) {
            key = text.substring(0, index);
            s = text.substring(index + 1);
        } else {
            key = "node";
            s = text;
        }
        final BiFunction<Settings, String[], Object> nodeParser = getNodeParser(key);
        if (nodeParser == null) {
            return "${" + text + "}";
        }
        final String[] args = Strings.splitBySpaces(s);
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.length() > 1 && arg.startsWith("$")) {
                args[i] = parseText(settings, arg.substring(1));
            } else {
                args[i] = arg.replace("\\$", "$");
            }
        }
        return nodeParser.apply(settings, args);
    }

    @NotNull
    protected String parseText(@NotNull Settings settings, @NotNull String text) {
        final Object parsed = parse(settings, text);
        if (parsed instanceof String) {
            return (String) parsed;
        } else if (parsed instanceof Iterable) {
            final StringJoiner joiner = new StringJoiner("\n");
            for (Object o : (Iterable<?>) parsed) {
                joiner.add(String.valueOf(o));
            }
            return joiner.toString();
        } else {
            return String.valueOf(parsed);
        }
    }
}
