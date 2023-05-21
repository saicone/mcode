package com.saicone.mcode.util;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
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

    @NotNull
    public static String[] splitBy(@NotNull String str, @NotNull @Language("RegExp") String regex, @NotNull String delimiter, @NotNull String out) {
        final int outLength = out.length();
        final String[] split = str.split(regex);
        if (split.length < 2) {
            return split;
        }
        final List<String> list = new ArrayList<>();

        for (int i = 0; i < split.length; i++) {
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
                list.addAll(collected);
            }
        }
        return list.toArray(new String[0]);
    }

    @NotNull
    public static String[] splitBySpaces(@NotNull String s) {
        return splitBySpaces(s, "`");
    }

    @NotNull
    public static String[] splitBySpaces(@NotNull String s, @NotNull String out) {
        return splitBy(s, " ", " ", out);
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
    public static String replacePlaceholder(@NotNull String s, char start, @NotNull String id, char end, @NotNull Function<String, String> function) {
        if (s.isBlank() || !s.contains("" + start) || s.length() < (id.length() + 4)) {
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
                    if (i > mark1 && i + 2 < chars.length && s.substring(mark1, i).equalsIgnoreCase(id)) {
                        final int mark2 = i + 1;
                        while (++i < chars.length) {
                            final char c2 = chars[i];
                            if (c2 == end) {
                                builder.replace(mark, i, function.apply(s.substring(mark2, i)));
                                break;
                            } else {
                                builder.append(c2);
                            }
                        }
                    } else {
                        builder.append(c1);
                    }
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
