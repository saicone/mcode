package com.saicone.mcode.util.text;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class to handle Strings with different Minecraft options, like center text or colorize.<br>
 * So legacy, RGB and special colors are supported like 'rainbow' and 'gradient'.
 * <br>
 * <h2>Minecraft chat text</h2>
 * The default Minecraft font length is counted by the number of pixels that chars are using, the in-game
 * chat by default has a maximum width of 300px, so it only can display a maximum number of words per line
 * depending on total px length, this may be different on Bedrock clients that adjust font size to allow more
 * words per line. Take in count that legacy and RGB colors are ignored in text length count.
 *
 * @author Rubenicos
 */
public class MStrings {

    /**
     * Use or not Bungeecord HEX color format, false by default.
     */
    public static boolean BUNGEE_HEX = false;
    /**
     * Default Minecraft chat width, this may be different on client-side
     */
    public static final int CHAT_WIDTH = 300;
    /**
     * Unmodifiable map of character pixel-length depending on Minecraft default font.
     */
    public static final Map<Character, Integer> FONT_LENGTH;
    /**
     * Unmodifiable list of "latin letter small capital" characters (Looks cool on MC).
     */
    public static final List<Character> SMALL_FONT = List.of(
            '\u1D00', '\u0299', '\u1D04', '\u1D05', '\u1D07', '\uA730', '\u0262', '\u029C', '\u026A', '\u1D0A', '\u1D0B', '\u029F', '\u1D0D',
            '\u0274', '\u1D0F', '\u1D18', '\uA7EF', '\u0280', '\uA731', '\u1D1B', '\u1D1C', '\u1D20', '\u1D21', '\u0078', '\u028F', '\u1D22'
    );
    /**
     * Minecraft color character.
     */
    public static final char COLOR_CHAR = '\u00a7';
    /**
     * Unmodifiable set of Minecraft legacy color codes.
     */
    public static final Set<Character> COLOR_CODES = Set.of(
            // Dark colors
            '0', '1', '2', '3', '4', '5', '6', '7',
            // Light colors
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f',
            // Other
            'k', 'l', 'm', 'n', 'o', 'r',
            // Uppercase
            'A', 'B', 'C', 'D', 'E', 'F', 'K', 'L', 'M', 'N', 'O', 'R'
    );
    /**
     * Unmodifiable set of special color types supported by this class.
     */
    public static final Set<String> COLOR_SPECIAL = Set.of("rainbow", "r", "lgbt", "gradient", "g");
    /**
     * Unmodifiable set of forms to write loop option on special color parser.
     */
    public static final Set<String> COLOR_SPECIAL_LOOP = Set.of("looping", "loop", "l");
    /**
     * Text detection to stop the text colorization from special colors.
     */
    public static final String COLOR_SPECIAL_STOP = "$stop$";

    static {
        Map<Character, Integer> map = new HashMap<>();
        Arrays.asList('i', 'l', '!', ':', ';', '\'', '|', '.', ',').forEach(c -> map.put(c, 1));
        map.put('`', 2);
        Arrays.asList('I', '[', ']', '"', ' ').forEach(c -> map.put(c, 3));
        Arrays.asList('f', 'k', 't', '(', ')', '{', '}', '<', '>').forEach(c -> map.put(c, 4));
        map.put('@', 6);
        FONT_LENGTH = Collections.unmodifiableMap(map);
    }

    MStrings() {
    }

    /**
     * Get Minecraft default font length of provided character.
     *
     * @param c the character to check.
     * @return  number of pixels of the character width.
     */
    public static int getFontLength(char c) {
        return FONT_LENGTH.getOrDefault(c, 5);
    }

    /**
     * Get Minecraft default font length of provided text.
     *
     * @param s the text to check.
     * @return  number of text pixels width.
     */
    public static int getFontLength(@NotNull String s) {
        return getFontLength(s, '&');
    }

    /**
     * Get Minecraft default font length of provided text.
     *
     * @param s         the text to check.
     * @param colorChar the colored text character, other than {@link #COLOR_CHAR}.
     * @return          number of text pixels width.
     */
    public static int getFontLength(@NotNull String s, char colorChar) {
        int px = 0;
        boolean bold = false;
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            // Verify color char
            final boolean mcChar;
            if (i + 1 < s.length() && ((mcChar = (c == COLOR_CHAR)) || c == colorChar)) {
                final char c1 = s.charAt(i + 1);
                // Skip RGB color
                if (BUNGEE_HEX && c1 == 'x' && isHexFormat(s, i + 2, 2, mcChar ? COLOR_CHAR : colorChar) != null) {
                    i = i + 12;
                } else if (c1 == '#' && isHexFormat(s, i + 2, 1, mcChar ? COLOR_CHAR : colorChar) != null) {
                    i = i + 6;
                } else if (isColorCode(c1)) { // Skip legacy color code, so (un)mark text as bold depending on color char
                    switch (c1) {
                        case 'l':
                        case 'L':
                            bold = true;
                            break;
                        case 'r':
                        case 'R':
                            bold = false;
                            break;
                        default:
                            break;
                    }
                } else {
                    // Non-color character detected, so append pixel-length from verified characters
                    final int i1;
                    if (bold) {
                        if (c1 == ' ') {
                            // 2 px separator + 1 bold px for first char due the second is space
                            i1 = 3;
                        } else {
                            // 2 px separator + 2 bold px for both chars
                            i1 = 4;
                        }
                    } else {
                        // 2 px separator
                        i1 = 2;
                    }
                    px += getFontLength(c) + getFontLength(c1) + i1;
                }
                i++;
            } else {
                if (bold && c != ' ') {
                    px += getFontLength(c) + 2;
                } else {
                    px += getFontLength(c) + 1;
                }
            }
        }
        return px;
    }

    /**
     * Get small font representation of provided character.
     *
     * @param c the character to get.
     * @return  a small font representation if exists, the same character otherwise.
     */
    public static char getSmallFont(char c) {
        final int index = Character.toUpperCase(c) - 'A';
        return index < SMALL_FONT.size() ? SMALL_FONT.get(index) : c;
    }

    /**
     * Convert provided String into small font representation.
     *
     * @param s         the text to convert.
     * @return          a small font representation of text on compatible parts.
     */
    @NotNull
    public static String getSmallFont(@NotNull String s) {
        return getSmallFont(s, '&');
    }

    /**
     * Convert provided String into small font representation.
     *
     * @param s         the text to convert.
     * @param colorChar the colored text character, other than {@link #COLOR_CHAR}.
     * @return          a small font representation of text on compatible parts.
     */
    @NotNull
    public static String getSmallFont(@NotNull String s, char colorChar) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            // Verify color char
            final boolean mcChar;
            if (i + 1 < s.length() && ((mcChar = (c == COLOR_CHAR)) || c == colorChar)) {
                final char c1 = s.charAt(i + 1);
                // Skip RGB color
                if (BUNGEE_HEX && c1 == 'x' && isHexFormat(s, i + 2, 2, mcChar ? COLOR_CHAR : colorChar) != null) {
                    i = i + 12;
                    builder.append(s, i, i + 12 + 1);
                } else if (c1 == '#' && isHexFormat(s, i + 2, 1, mcChar ? COLOR_CHAR : colorChar) != null) {
                    i = i + 6;
                    builder.append(s, i, i + 6 + 1);
                } else if (!isColorCode(c1)) { // Skip legacy color code
                    builder.append(getSmallFont(c)).append(getSmallFont(c1));
                } else {
                    builder.append(c);
                }
                i++;
            } else {
                builder.append(getSmallFont(c));
            }
        }
        return builder.toString();
    }

    /**
     * Check legacy color code.
     *
     * @param c the character to check.
     * @return  true if the character is a legacy color code, false otherwise.
     */
    public static boolean isColorCode(char c) {
        return COLOR_CODES.contains(c);
    }

    /**
     * Check if the character is a color type supported by this class.<br>
     * This includes any legacy color code, hex prefix and special prefix.
     *
     * @param c the character to check.
     * @return  true if the character is a supported color type, false otherwise.
     */
    public static boolean isColorType(char c) {
        return COLOR_CODES.contains(c) || c == '#' || c == '$';
    }

    /**
     * Check if the character is any legacy or newly color code.<br>
     * Instead of {@link #isColorCode(char)}, this method check accept 'x' as
     * a valid color code if {@link #BUNGEE_HEX} is set to true.
     *
     * @param c the character to check.
     * @return  true if the character is a legacy or newly color code, false otherwise.
     */
    public static boolean isAnyColorCode(char c) {
        return COLOR_CODES.contains(c) || (BUNGEE_HEX && c == 'x');
    }

    /**
     * Check if the provided 6-length string is valid HEX format.
     *
     * @param s the text to check.
     * @return  true if the string is a valid HEX, false otherwise.
     */
    public static boolean isValidHex(@NotNull String s) {
        try {
            Integer.parseInt(s, 16);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Check if the provided char array is a valid HEX format
     * starting from given position and providing color char to ignore.
     *
     * @param s         the string to check.
     * @param start     the start position to iterate array.
     * @param sum       the amount to sum on for-loop.
     * @param colorChar the color character to ignore.
     * @return          true if the first 6 read are a valid HEX, false otherwise.
     */
    @Nullable
    public static String isHexFormat(@NotNull String s, int start, int sum, char colorChar) {
        final int max = start + (sum * 6);
        if (max > s.length()) {
            return null;
        }
        final StringBuilder builder = new StringBuilder();
        for (int i = start; i < max; i = i + sum) {
            if (s.charAt(i) != colorChar) {
                return null;
            }
            builder.append(s.charAt(i + 1));
        }
        final String hex = builder.toString();
        return isValidHex(hex) ? hex : null;
    }

    /**
     * Justify the provided text collection based on default Minecraft font.<br>
     * This method calculate line pixels width using the widest text from the collection.
     *
     * @param collection the collection to justify.
     * @return           a justified String list based on default Minecraft font.
     */
    @NotNull
    public static List<String> justifyText(@NotNull Collection<String> collection) {
        return justifyText(collection, '&');
    }

    /**
     * Justify the provided text collection based on default Minecraft font.<br>
     * This method calculate line pixels width using the widest text from the collection.
     *
     * @param collection the collection to justify.
     * @param colorChar  the color char to ignore.
     * @return           a justified String list based on default Minecraft font.
     */
    @NotNull
    public static List<String> justifyText(@NotNull Collection<String> collection, char colorChar) {
        int width = 0;
        for (String s : collection) {
            int i = getFontLength(s);
            if (i > width) {
                width = i;
            }
        }
        return justifyText(collection, width, colorChar);
    }

    /**
     * Justify the provided text collection based on default Minecraft font.
     *
     * @param collection the collection to justify.
     * @param width      the line pixels width.
     * @return           a justified String list based on default Minecraft font.
     */
    @NotNull
    public static List<String> justifyText(@NotNull Collection<String> collection, int width) {
        return justifyText(collection, width, '&');
    }

    /**
     * Justify the provided text collection based on default Minecraft font.
     *
     * @param collection the collection to justify.
     * @param width      the line pixels width.
     * @param colorChar  the color char to ignore.
     * @return           a justified String list based on default Minecraft font.
     */
    @NotNull
    public static List<String> justifyText(@NotNull Collection<String> collection, int width, char colorChar) {
        return justifyText(String.join(" ", collection), width, colorChar);
    }

    /**
     * Justify the provided text based on default Minecraft font.
     *
     * @param text  the text to justify.
     * @param width the line pixels width.
     * @return      a justified text based on default Minecraft font.
     */
    @NotNull
    public static List<String> justifyText(@NotNull String text, int width) {
        return justifyText(text, width, '&');
    }

    /**
     * Justify the provided text based on default Minecraft font.
     *
     * @param text      the text to justify.
     * @param width     the line pixels width.
     * @param colorChar the color char to ignore.
     * @return          a justified text based on default Minecraft font.
     */
    @NotNull
    public static List<String> justifyText(@NotNull String text, int width, char colorChar) {
        final List<String> list = new ArrayList<>();
        if (text.isBlank()) {
            return list;
        }
        final String[] words = text.split(" ");
        if (words.length < 2) {
            return list;
        }
        final List<String> pending = new ArrayList<>();
        int px = 0;
        for (int i = 0; i < words.length; i++) {
            final String word = words[i];
            if (word.isBlank()) {
                continue;
            }
            final int length = getFontLength(word, colorChar);
            if ((px + length + pending.size() * 4) >= width || i + 1 >= words.length) {
                if (px == 0) {
                    list.add(word);
                    continue;
                }
                final int size = pending.size() - 1;
                if (size == 0) {
                    list.add(pending.get(0));
                } else {
                    final StringBuilder builder = new StringBuilder();
                    int spaces = (width - px) / 4;
                    for (int i1 = 0; i1 < size; i1++) {
                        // Round up remaining spaces with current size
                        final int count = (spaces - 1) / (size - i1) + 1;
                        builder.append(pending.get(i1)).append(" ".repeat(count));
                        // Subtract appended spaces
                        spaces -= count;
                    }
                    builder.append(pending.get(size));
                    list.add(builder.toString());
                }
                px = 0;
                pending.clear();
            }
            px = px + length;
            pending.add(word);
        }
        return list;
    }

    /**
     * Center the provided text based on default Minecraft font.
     *
     * @param text      the text to center.
     * @return          a justified text based on default Minecraft font.
     */
    @NotNull
    public static String centerText(@NotNull String text) {
        return centerText(text, CHAT_WIDTH);
    }

    /**
     * Center the provided text based on default Minecraft font.
     *
     * @param text      the text to center.
     * @param width     the line pixels width.
     * @return          a justified text based on default Minecraft font.
     */
    @NotNull
    public static String centerText(@NotNull String text, int width) {
        return centerText(text, width, '&');
    }

    /**
     * Center the provided text based on default Minecraft font.
     *
     * @param text      the text to center.
     * @param width     the line pixels width.
     * @param colorChar the color char to ignore.
     * @return          a justified text based on default Minecraft font.
     */
    @NotNull
    public static String centerText(@NotNull String text, int width, char colorChar) {
        if (text.length() >= width) {
            return text;
        }
        return spacesToCenter(getFontLength(text, colorChar), width) + text;
    }

    /**
     * Get a String full of spaces with the amount of spaces needed to center a
     * text based on provided width of pixels and used pixels.
     *
     * @param length the used pixels.
     * @param width  the max width.
     * @return       a String full of needed spaces to center text.
     */
    @NotNull
    public static String spacesToCenter(int length, int width) {
        int px = width - length;
        // 3 px for space + 1 px separator between spaces
        if (px < 4) {
            return "";
        }
        int count = 0;
        while (px >= 4) {
            count++;
            px -= 4;
        }
        return " ".repeat(count);
    }

    /**
     * Colorize the provided text collection.<br>
     * This method accept any minecraft legacy color code, hex color and special color.
     *
     * @param list the collection to color.
     * @return     a colored text list.
     */
    @NotNull
    public static List<String> color(@NotNull Collection<String> list) {
        final List<String> finalList = new ArrayList<>();
        for (String s : list) {
            finalList.add(color(s));
        }
        return finalList;
    }

    /**
     * Colorize the provided text.<br>
     * This method accept any minecraft legacy color code, hex color and special color.
     *
     * @param s the text to color.
     * @return  a colored text.
     */
    @NotNull
    public static String color(@NotNull String s) {
        return color('&', s);
    }

    /**
     * Colorize the provided text.<br>
     * This method accept any minecraft legacy color code, hex color and special color.
     *
     * @param colorChar the color character to parse.
     * @param s         the text to color.
     * @return          a colored text.
     */
    @NotNull
    public static String color(char colorChar, @NotNull String s) {
        if (s.indexOf(colorChar) < 0) {
            return s;
        }
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c == colorChar && i + 1 < s.length()) {
                final char colorType = s.charAt(i + 1);

                if (isColorCode(colorType)) { // Legacy color
                    builder.append(COLOR_CHAR).append(colorType);
                    i++;
                    continue;
                }

                final int total;
                if (colorType == '#') { // Hex / RGB
                    total = colorHex(colorChar, builder, s, i);
                } else if (colorType == '$') { // Special type
                    total = colorSpecial(colorChar, builder, s, i);
                } else if (BUNGEE_HEX && colorType == 'x' && isHexFormat(s, i + 2, 2, colorChar) != null) {
                    total = i + 14;
                    for (int i1 = i; i1 < total; i1 += 2) {
                        builder.append(COLOR_CHAR);
                        builder.append(s.charAt(i1 + 1));
                    }
                } else {
                    builder.append(c);
                    continue;
                }

                if (total > 0) {
                    i += total;
                    continue;
                }
            }
            builder.append(c);
        }
        return builder.toString();
    }

    private static int colorHex(char colorChar, @NotNull StringBuilder builder, @NotNull String s, int i) {
        if (i + 7 >= s.length()) {
            return 0;
        }
        StringBuilder color = new StringBuilder();
        for (int j = i + 2; j < s.length() && color.length() < 6; j++) {
            color.append(s.charAt(j));
        }
        if (color.length() == 6) {
            // Format: <char>#RRGGBB
            builder.append(toRgb(colorChar, color.toString()));
            return color.length() + 1;
        } else {
            return 0;
        }
    }

    private static int colorSpecial(char colorChar, @NotNull StringBuilder builder, @NotNull String s, int i) {
        if (i + 3 >= s.length()) {
            return 0;
        }

        // Find inside: <char>$<block>$
        int blockIndex = -1;
        for (int i1 = i + 3; i1 < s.length(); i1++) {
            if (s.charAt(i1) == '$') {
                blockIndex = i1;
                break;
            }
        }
        if (blockIndex < 0 || blockIndex + 1 >= s.length()) {
            return 0;
        }

        // Block: <special color>:option1:option2:option3...
        final String[] block = s.substring(i + 2, blockIndex).split(":");
        if (block.length < 1) {
            return 0;
        }
        // Special color: <type>[#speed]
        final int speed;
        final int speedIndex = block[0].indexOf('#');
        if (speedIndex > 1) {
            speed = intValue(block[0].substring(speedIndex + 1), 0);
            block[0] = block[0].substring(0, speedIndex);
        } else {
            speed = 0;
        }
        // Verify if special color type exists
        if (!COLOR_SPECIAL.contains(block[0].toLowerCase())) {
            // Verify if special color is equal to "$stop$"
            if (block[0].equalsIgnoreCase("stop")) {
                return COLOR_SPECIAL_STOP.length() + 1;
            }
            return 0;
        }

        // Text after special color declaration
        String text = s.substring(blockIndex + 1);

        // Find stop declaration
        final String stopText = colorChar + COLOR_SPECIAL_STOP;
        int stopIndex;
        final int finalInt;
        if ((stopIndex = text.toLowerCase().indexOf(stopText)) >= 0) {
            // Stop after found: <char>$stop$
            text = color(colorChar, text.substring(0, stopIndex)); // Colorize text inside
            finalInt = stopIndex + stopText.length();
        } else if ((stopIndex = text.indexOf(colorChar)) >= 0 && stopIndex + 1 < text.length() && isColorType(text.charAt(stopIndex + 1))) {
            // Stop after found: <char><any type of color>
            text = text.substring(0, stopIndex);
            finalInt = stopIndex;
        } else {
            finalInt = text.length();
        }

        builder.append(toSpecial(text, speed, block));

        return finalInt + (blockIndex - i);
    }

    /**
     * Convert provided 6-length String into rgb color format.
     *
     * @param color the String to convert.
     * @return      a rgb color formatted text.
     */
    @NotNull
    public static String toRgb(@NotNull String color) {
        return toRgb('&', color);
    }

    /**
     * Convert provided 6-length String into rgb color format.
     *
     * @param colorChar the color character to parse with if the provided text isn't a valid hex.
     * @param color     the String to convert.
     * @return          a rgb color formatted text.
     */
    @NotNull
    public static String toRgb(char colorChar, @NotNull String color) {
        if (!isValidHex(color)) {
            return colorChar + "#" + color;
        }

        if (BUNGEE_HEX) {
            final StringBuilder hex = new StringBuilder(COLOR_CHAR + "x");
            for (char c : color.toCharArray()) {
                hex.append(COLOR_CHAR).append(c);
            }
            return hex.toString();
        } else {
            return COLOR_CHAR + '#' + color;
        }
    }

    /**
     * Convert provided color object into rgb color format.
     *
     * @param color the color object to parse.
     * @return      a rgb color formatted text.
     */
    @NotNull
    public static String toRgb(@NotNull Color color) {
        if (BUNGEE_HEX) {
            final StringBuilder hex = new StringBuilder(COLOR_CHAR + "x");
            for (char c : String.format("%08x", color.getRGB()).substring(2).toCharArray()) {
                hex.append(COLOR_CHAR).append(c);
            }
            return hex.toString();
        } else {
            return COLOR_CHAR + '#' + String.format("%08x", color.getRGB()).substring(2);
        }
    }

    /**
     * Parse special colored string using the provided arguments.
     *
     * @param text  the text to parse.
     * @param args  the special color arguments.
     * @return      a colored text is any special format was detected.
     */
    @NotNull
    public static String toSpecial(@NotNull String text, @NotNull String... args) {
        return toSpecial(text, 0, args);
    }

    /**
     * Parse special colored string using the provided arguments.
     *
     * @param text  the text to parse.
     * @param speed the movement speed.
     * @param args  the special color arguments.
     * @return      a colored text is any special format was detected.
     */
    @NotNull
    public static String toSpecial(@NotNull String text, int speed, @NotNull String... args) {
        if (args.length < 1) {
            return text;
        }
        switch (args[0].toLowerCase()) {
            case "rainbow":
            case "r":
            case "lgbt":
            case "lgtv":
                return toRainbow(text, speed, args);
            case "gradient":
            case "g":
                return toGradient(text, speed, args);
            default:
                return text;
        }
    }

    private static String toRainbow(@NotNull String text, int speed, @NotNull String... args) {
        // Argument base objects
        final float saturation = args.length > 1 ? floatValue(args[1], 1.0F) : 1.0F;
        final float brightness = args.length > 2 ? floatValue(args[2], 1.0F) : 1.0F;
        final boolean looping = args.length > 1 && COLOR_SPECIAL_LOOP.contains(args[args.length - 1].toLowerCase());

        // Text base objects
        int length = text.length();
        for (int i = 0; i < text.length() - 1; i++) {
            if (text.charAt(i) == COLOR_CHAR && isAnyColorCode(text.charAt(i + 1))) {
                length -= 2;
            }
        }
        final int totalColors = Math.max(looping ? Math.min(length, 30) : length, 1);
        final float hueStep = 1.0F / totalColors;

        float hue = speed != 0 ? (float) ((((Math.floor(System.currentTimeMillis() / 50.0)) / 360) * speed) % 1) : 0;

        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            final char c = text.charAt(i);
            if (c == COLOR_CHAR && i + 1 < text.length()) {
                final char c1 = text.charAt(i + 1);
                if (isAnyColorCode(c1)) {
                    i++;
                    builder.append(c).append(c1);
                    continue;
                }
            }
            builder.append(toRgb(Color.getHSBColor(hue, saturation, brightness))).append(c);
            hue += hueStep;
        }

        return builder.toString();
    }

    private static String toGradient(@NotNull String text, int speed, @NotNull String... args) {
        if (args.length < 3) {
            return text;
        }

        // Argument base values
        final boolean looping = COLOR_SPECIAL_LOOP.contains(args[args.length - 1].toLowerCase());
        final List<Color> colors = Arrays.stream(args)
                .filter(MStrings::isValidHex)
                .map(s -> "#" + s)
                .map(Color::decode)
                .collect(Collectors.toList());

        if (colors.isEmpty()) {
            return text;
        }
        if (colors.size() < 2) {
            return toRgb(colors.get(0)) + text;
        }

        // Text base objects
        int length = text.length();
        for (int i = 0; i < text.length() - 1; i++) {
            if (text.charAt(i) == COLOR_CHAR && isAnyColorCode(text.charAt(i + 1))) {
                length -= 2;
            }
        }
        final int totalSteps = (looping ? Math.min(length, 30) : length) - 1;

        long hexStep = speed != 0 ? System.currentTimeMillis() / speed : 0;
        final int roundSize = (colors.size() - 1) / 2 + 1;
        final float segment = (float) totalSteps / roundSize;
        final float increment = (float) totalSteps / (colors.size() - 1);

        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            final char c = text.charAt(i);
            if (c == COLOR_CHAR && i + 1 < text.length()) {
                final char c1 = text.charAt(i + 1);
                if (isAnyColorCode(c1)) {
                    i++;
                    builder.append(c).append(c1);
                    continue;
                }
            }
            // Formula taken from RoseColors and created by BomBardyGamer
            // Return the absolute rounded value of "2 * ASIN(SIN(hexStep * (PI / (2 * totalSteps))) / PI) * totalSteps"
            final int adjustedStep = (int) Math.round(Math.abs(((2 * Math.asin(Math.sin(hexStep * (Math.PI / (2 * totalSteps))))) / Math.PI) * totalSteps));

            final int index = (int) Math.min(colors.size() - 2, Math.min(Math.floor(adjustedStep / segment), roundSize - 1) * 2);

            final float lowerRange = increment * index;
            final float range = increment * (index + 1) - lowerRange;

            final Color fromColor = colors.get(index);
            final Color toColor = colors.get(index + 1);

            final Color finalColor = new Color(
                    calculateHexPiece(range, lowerRange, adjustedStep, fromColor.getRed(), toColor.getRed()),
                    calculateHexPiece(range, lowerRange, adjustedStep, fromColor.getGreen(), toColor.getGreen()),
                    calculateHexPiece(range, lowerRange, adjustedStep, fromColor.getBlue(), toColor.getBlue())
            );

            builder.append(toRgb(finalColor)).append(c);
            hexStep++;
        }

        return builder.toString();
    }

    private static int intValue(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static float floatValue(String s, float def) {
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    // Taken from RoseColors
    private static int calculateHexPiece(float range, float lowerRange, int step, int fromChannel, int toChannel) {
        final float interval = (toChannel - fromChannel) / range;
        return Math.round(interval * (step - lowerRange) + fromChannel);
    }
}
