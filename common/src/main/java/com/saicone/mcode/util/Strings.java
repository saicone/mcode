package com.saicone.mcode.util;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Strings {

    private static final Pattern PERCENT_PLACEHOLDER = Pattern.compile("%([^%]+)%");
    private static final Pattern BRACKET_PLACEHOLDER = Pattern.compile("[{]([^%]+)}");
    private static final Pattern CUSTOM_COLOR = Pattern.compile("\\[color(?<index>\\d+)](?<text>.*)\\[/color]");

    public static boolean matchesPlaceholder(@Nullable String s) {
        return s != null && !s.isBlank() && (PERCENT_PLACEHOLDER.matcher(s).matches() || BRACKET_PLACEHOLDER.matcher(s).matches());
    }

    public static boolean matchesPlaceholder(@NotNull List<String> list) {
        for (String s : list) {
            if (matchesPlaceholder(s)) {
                return true;
            }
        }
        return false;
    }

    public static boolean matchesCustomColor(@NotNull String s) {
        return !s.isBlank() && CUSTOM_COLOR.matcher(s).matches();
    }

    public static boolean isNumber(@NotNull String s) {
        return isNumber(s, null);
    }

    public static <T extends Number> boolean isNumber(@NotNull String s, @Nullable Class<T> numberClass) {
        if (s.isBlank()) {
            return false;
        }
        String str = s.trim();
        if (str.charAt(0) == '-' || str.charAt(0) == '+') {
            if (str.length() == 1) {
                return false;
            }
            str = str.substring(1);
        }
        if (numberClass != null) {
            if (numberClass.isPrimitive()) {
                throw new IllegalArgumentException("Cannot compare primitive number type, use a number type instead");
            }
            switch (str.charAt(str.length() - 1)) {
                case 'L':
                case 'l':
                    if (numberClass == Long.class && str.length() != 1) {
                        str = str.substring(0, str.length() - 1);
                    } else {
                        return false;
                    }
                    break;
                case 'F':
                case 'f':
                    if (numberClass == Float.class && str.length() != 1) {
                        str = str.substring(0, str.length() - 1);
                    } else {
                        return false;
                    }
                    break;
                case 'D':
                case 'd':
                    if (numberClass == Double.class && str.length() != 1) {
                        str = str.substring(0, str.length() - 1);
                    } else {
                        return false;
                    }
                    break;
                default:
                    break;
            }
        }
        int integerPart = 0;
        int decimalPart = 0;
        boolean decimal = false;
        for (char c : str.toCharArray()) {
            if (decimal) {
                if (Character.isDigit(c)) {
                    decimalPart++;
                } else {
                    return false;
                }
            } else if (Character.isDigit(c)) {
                integerPart++;
            } else if (c == '.') {
                decimal = true;
            } else {
                return false;
            }
        }
        if (numberClass != null) {
            if (decimalPart > 0 && numberClass != BigDecimal.class && numberClass != Double.class && numberClass != Float.class) {
                return false;
            }
            if (numberClass == BigDecimal.class || numberClass == BigInteger.class) {
                return true;
            }
            final int max;
            if (numberClass == Double.class) {
                max = 309;
            } else if (numberClass == Float.class) {
                max = 39;
            } else if (numberClass == Long.class) {
                max = 19;
            } else if (numberClass == Integer.class) {
                max = 10;
            } else if (numberClass == Short.class) {
                max = 5;
            } else if (numberClass == Byte.class) {
                max = 3;
            } else {
                return true;
            }
            return integerPart <= max;
        }
        return true;
    }
    
    @Nullable
    @Contract("!null -> !null")
    public static String capitalize(@Nullable String s) {
        if (s == null || s.isBlank()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    @NotNull
    public static String join(@NotNull String delimiter, @Nullable Object object) {
        return join(delimiter, "", "", object);
    }

    @NotNull
    public static String join(@NotNull String delimiter, @NotNull String prefix, @NotNull String suffix, @Nullable Object object) {
        if (object == null) {
            return "";
        }
        final StringJoiner joiner = new StringJoiner(delimiter, prefix, suffix);
        if (object instanceof Iterable) {
            for (Object o : (Iterable<?>) object) {
                joiner.add(String.valueOf(o));
            }
        } else if (object instanceof Object[]) {
            for (int i = 0; i < ((Object[]) object).length; i++) {
                joiner.add(String.valueOf(((Object[]) object)[i]));
            }
        } else {
            joiner.add(String.valueOf(object));
        }
        return joiner.toString();
    }

    @NotNull
    public static String[] split(@NotNull String s, char c) {
        int end = s.indexOf(c);
        if (end < 1) {
            return new String[] { s };
        }
        int start = 0;
        final List<String> list = new ArrayList<>();
        while (end > 0) {
            if (s.charAt(end - 1) != '\\') {
                list.add(s.substring(start, end).replace("\\" + c, String.valueOf(c)));
                start = end + 1;
            }
            end = s.indexOf(c, end + 1);
        }
        list.add(s.substring(start));
        return list.toArray(new String[0]);
    }

    @NotNull
    public static String[] splitBy(@NotNull String str, @NotNull @Language("RegExp") String regex, @NotNull String delimiter, @NotNull String out) {
        return splitBy(str.split(regex), 0, delimiter, out);
    }

    @NotNull
    public static String[] splitBy(@NotNull String str, @NotNull @Language("RegExp") String regex, int limit, @NotNull String delimiter, @NotNull String out) {
        return splitBy(str.split(regex), limit, delimiter, out);
    }

    @NotNull
    public static String[] splitBy(@NotNull String[] split, @NotNull String delimiter, @NotNull String out) {
        return splitBy(split, 0, delimiter, out);
    }

    @NotNull
    public static String[] splitBy(@NotNull String[] split, int limit, @NotNull String delimiter, @NotNull String out) {
        if (split.length < 2) {
            return split;
        }
        final boolean limited = limit >= 1;
        final List<String> list = new ArrayList<>();
        final int outLength = out.length();
        for (int i = 0; i < split.length; i++) {
            if (limited && list.size() + 1 == limit) {
                list.add(String.join(delimiter, Arrays.copyOfRange(split, i, split.length)));
                break;
            }
            final String s = split[i];

            // Starts with "out"
            if (!s.startsWith(out)) {
                list.add(s);
                continue;
            }
            // Ends with "out"
            if (s.length() >= outLength * 2 && s.endsWith(out)) {
                list.add(s.substring(outLength, s.length() - outLength));
                continue;
            }
            // Just starts with "out"
            if (i + 1 >= split.length) {
                list.add(s);
                continue;
            }

            final List<String> collected = new ArrayList<>();
            while (++i < split.length) {
                final String s1 = split[i];
                if (s1.endsWith(out)) {
                    collected.add(0, s.substring(outLength));
                    collected.add(s1.substring(0, s1.length() - outLength));
                    list.add(String.join(delimiter, collected));
                    collected.clear();
                    break;
                } else {
                    collected.add(s1);
                    if (i + 1 < split.length) {
                        final String s2 = split[i + 1];
                        if (s2.startsWith(out) && s.length() > outLength) {
                            break;
                        }
                    }
                }
            }
            if (!collected.isEmpty()) {
                list.add(s);
                if (limited && list.size() + collected.size() >= limit) {
                    for (int i1 = 0; i1 < collected.size() && list.size() < limit; i1++) {
                        list.add(collected.get(i1));
                    }
                } else {
                    list.addAll(collected);
                }
                collected.clear();
            }
        }
        return list.toArray(new String[0]);
    }

    @NotNull
    public static String[] splitBySpaces(@NotNull String s) {
        return splitBySpaces(s, 0, "`");
    }

    @NotNull
    public static String[] splitBySpaces(@NotNull String s, int limit) {
        return splitBySpaces(s, limit, "`");
    }

    @NotNull
    public static String[] splitBySpaces(@NotNull String s, @NotNull String out) {
        return splitBySpaces(s, 0, out);
    }

    @NotNull
    public static String[] splitBySpaces(@NotNull String s, int limit, @NotNull String out) {
        if (limit == 1) {
            return new String[] { s };
        }
        if (!s.contains(out)) {
            return s.split(" ", limit);
        }
        return splitBy(s, " ", limit, " ", out);
    }

    @Nullable
    @Contract("null -> null")
    public static String valueOrNull(@Nullable Object o) {
        return o == null ? null : String.valueOf(o);
    }

    public static void findInside(@NotNull String s, @NotNull String start, @NotNull String end, @NotNull BiConsumer<String, Boolean> consumer) {
        if (s.length() < start.length() + end.length() || !s.contains(start) || !s.contains(end)) {
            consumer.accept(s, false);
            return;
        }

        while (true) {
            final int startIndex = s.indexOf(start);
            if (startIndex < 0) {
                consumer.accept(s, false);
                break;
            }

            final String s1 = s.substring(startIndex + start.length());
            final int endIndex = s1.indexOf(end);
            if (endIndex < 0) {
                consumer.accept(s, false);
                break;
            } else {
                if (startIndex > 0) {
                    consumer.accept(s.substring(0, startIndex), false);
                }
                consumer.accept(s1.substring(0, endIndex), true);

                int finalIndex = endIndex + end.length();
                if (finalIndex < s1.length()) {
                    s = s1.substring(finalIndex);
                } else {
                    break;
                }
            }
        }
    }

    @NotNull
    public static String color(@NotNull String s, @NotNull List<String> colors) {
        return color('&', s, colors);
    }

    @NotNull
    public static String color(char colorChar, @NotNull String s, @NotNull List<String> colors) {
        final StringBuilder builder = new StringBuilder();
        findInside(s, "[color", "[/color]", (text, found) -> {
            final int i;
            if (found && (i = text.indexOf(']')) > 0) {
                final int colorIndex;
                try {
                    colorIndex = Integer.parseInt(text.substring(0, i));
                } catch (NumberFormatException e) {
                    builder.append(text);
                    return;
                }
                if (colorIndex <= 0 && colorIndex - 1 >= colors.size()) {
                    builder.append(colorChar).append('7').append(text);
                } else {
                    final String color = colors.get(colorIndex - 1);
                    builder.append(color).append(text);
                    final char c;
                    if (color.length() > 2 && ((c = color.charAt(1)) == '#' || c == '$')) {
                        builder.append(MStrings.COLOR_SPECIAL_STOP);
                    }
                }
                return;
            }
            builder.append(text);
        });
        return MStrings.color(colorChar, builder.toString());
    }

    @Nullable
    @Contract("!null, _, _ -> !null")
    public static String replacePrefix(@Nullable String s, @NotNull String replacement, @NotNull String... prefixes) {
        if (s == null) {
            return null;
        }
        for (String prefix : prefixes) {
            if (s.startsWith(prefix)) {
                return replacement + s.substring(prefix.length());
            }
        }
        return s;
    }

    @NotNull
    public static String replaceArgs(@NotNull String s, @Nullable Object... args) {
        if (args.length < 1 || s.isBlank()) {
            return s.replace("{#}", "0").replace("{*}", "[]").replace("{-}", "");
        }
        final char[] chars = s.toCharArray();
        final StringBuilder builder = new StringBuilder(s.length());
        String all = null;
        for (int i = 0; i < chars.length; i++) {
            final int mark = i;
            if (chars[i] == '{') {
                int num = 0;
                while (i + 1 < chars.length) {
                    if (Character.isDigit(chars[i + 1])) {
                        i++;
                        num *= 10;
                        num += chars[i] - '0';
                        continue;
                    }
                    if (i == mark) {
                        final char c = chars[i + 1];
                        if (c == '#') {
                            i++;
                            num = -1;
                        } else if (c == '*') {
                            i++;
                            num = -2;
                        } else if (c == '-') {
                            i++;
                            num = -3;
                        }
                    }
                    break;
                }
                if (i != mark && i + 1 < chars.length && chars[i + 1] == '}') {
                    i++;
                    if (num == -1) {
                        builder.append(args.length);
                    } else if (num == -2) {
                        builder.append(Arrays.toString(args));
                    } else if (num == -3) {
                        if (all == null) {
                            all = Arrays.stream(args).map(String::valueOf).collect(Collectors.joining(" "));
                        }
                        builder.append(all);
                    } else if (num < args.length) { // Avoid IndexOutOfBoundsException
                        builder.append(args[num]);
                    } else {
                        builder.append('{').append(num).append('}');
                    }
                } else {
                    i = mark;
                }
            }
            if (mark == i) {
                builder.append(chars[i]);
            }
        }
        return builder.toString();
    }

    @NotNull
    public static String replaceArgs(@NotNull String s, @NotNull Map<String, Object> args) {
        if (args.size() < 1 || s.isBlank()) {
            return s;
        }
        final StringBuilder builder = new StringBuilder(s.length());
        int start = 0;
        int end;
        int index = 0;
        while ((start = s.indexOf('{', start)) >= 0 && (end = s.indexOf('}', start)) >= 0) {
            if (index < start) {
                builder.append(s, index, start);
            }
            if (end - start > 1) {
                final String key = s.substring(start + 1, end);
                if (args.containsKey(key)) {
                    builder.append(args.get(key));
                } else {
                    builder.append('{').append(key).append('}');
                }
            }
            index = end + 1;
        }
        if (index < s.length()) {
            builder.append(s, index, s.length());
        }
        return builder.toString();
    }

    @NotNull
    public static <T> String replaceBracketPlaceholder(@Nullable T type, @NotNull String s, @NotNull Predicate<String> predicate, @NotNull BiFunction<T, String, Object> function) {
        return replacePlaceholder(type, s, '{', '}', predicate, function);
    }

    @NotNull
    public static <T> String replacePlaceholder(@Nullable T type, @NotNull String s, @NotNull Predicate<String> predicate, @NotNull BiFunction<T, String, Object> function) {
        return replacePlaceholder(type, s, '%', '%', predicate, function);
    }

    @NotNull
    public static <T> String replacePlaceholder(@Nullable T type, @NotNull String s, char start, char end, @NotNull Predicate<String> predicate, @NotNull BiFunction<T, String, Object> function) {
        if (s.isBlank() || !s.contains("" + start) || s.length() < 4) {
            return s;
        }

        final char[] chars = s.toCharArray();
        final StringBuilder builder = new StringBuilder(s.length());

        int mark = 0;
        for (int i = 0; i < chars.length; i++) {
            final char c = chars[i];

            builder.append(c);
            if (c != start || i + 1 >= chars.length) {
                mark++;
                continue;
            }

            // Faster than PlaceholderAPI ;)
            final int mark1 = i + 1;
            while (++i < chars.length) {
                final char c1 = chars[i];
                if (c1 == '_') {
                    if (i > mark1 && i + 2 < chars.length) {
                        final String id = s.substring(mark1, i);
                        if (predicate.test(id)) {
                            final int mark2 = i + 1;
                            while (++i < chars.length) {
                                final char c2 = chars[i];
                                if (c2 == end) {
                                    builder.replace(mark, i, String.valueOf(function.apply(type, s.substring(mark2, i))));
                                    break;
                                } else {
                                    builder.append(c2);
                                }
                            }
                            break;
                        }
                    }
                    builder.append(c1);
                    break;
                } else {
                    builder.append(c1);
                    if (i + 1 < chars.length && chars[i + 1] == start) {
                        break;
                    }
                }
            }

            mark = builder.length();
        }

        return builder.toString();
    }
}
