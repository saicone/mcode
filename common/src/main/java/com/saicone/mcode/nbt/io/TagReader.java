package com.saicone.mcode.nbt.io;

import com.saicone.mcode.nbt.Tag;
import com.saicone.mcode.nbt.TagMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class TagReader<T> extends Reader {

    private static final int UNKNOWN_CHARACTER = -1;
    private static final Set<Character> NUMBER_SUFFIX = Set.of('b', 'B', 's', 'S', 'l', 'L', 'f', 'F', 'd', 'D');

    private final Reader reader;
    private final TagMapper<T> mapper;

    @NotNull
    public static TagReader<Object> of(@NotNull String s) {
        return of(s, TagMapper.DEFAULT);
    }

    @NotNull
    public static <T> TagReader<T> of(@NotNull String s, @NotNull TagMapper<T> mapper) {
        return of(new StringReader(s), mapper);
    }

    @NotNull
    public static TagReader<Object> of(@NotNull Reader reader) {
        return of(reader, TagMapper.DEFAULT);
    }

    @NotNull
    public static <T> TagReader<T> of(@NotNull Reader reader, @NotNull TagMapper<T> mapper) {
        return new TagReader<>(reader, mapper);
    }

    public TagReader(@NotNull Reader reader, @NotNull TagMapper<T> mapper) {
        this.reader = reader.markSupported() ? reader : new BufferedReader(reader);
        this.mapper = mapper;
    }

    protected boolean isQuote(int c) {
        return c == '"' || c == '\'';
    }

    protected boolean isUnquoted(int c) {
        return c >= '0' && c <= '9'
                || c >= 'A' && c <= 'Z'
                || c >= 'a' && c <= 'z'
                || c == '_' || c == '-'
                || c == '.' || c == '+';
    }

    protected boolean isLeadingSign(int c) {
        return c == '-' || c == '+';
    }

    @Nullable
    protected Boolean isNumber(@NotNull String s) {
        if (s.isBlank()) {
            return false;
        }
        boolean decimal = false;
        for (char c : (isLeadingSign(s.charAt(0)) ? s.substring(1) : s).toCharArray()) {
            if (!Character.isDigit(c)) {
                if (!decimal && c == '.') {
                    decimal = true;
                    continue;
                }
                return false;
            }
        }
        return decimal ? null : true;
    }

    @Nullable
    public T readTag() throws IOException {
        skipSpaces();
        mark(3);
        final int first = read();
        if (first == '{') {
            return readCompoundTag0();
        } else if (first == '[') {
            final int id = read();
            if (id == 'B' || id == 'I' || id == 'L') {
                final int separator = read();
                if (separator == ';') {
                    return readArrayTag(id);
                }
            }
            reset();
            return readListTag();
        } else {
            return readValueTag(first);
        }
    }

    @Nullable
    public T readValueTag() throws IOException {
        return readValueTag(read());
    }

    @Nullable
    protected T readValueTag(int first) throws IOException {
        if (first == UNKNOWN_CHARACTER) {
            return null;
        }
        if (isQuote(first)) {
            return readQuotedTag(first);
        } else if (isUnquoted(first)) {
            return readUnquotedTag(first);
        } else {
            return null;
        }
    }

    @NotNull
    public String readKey() throws IOException {
        mark(1);
        final int first = read();
        if (first == UNKNOWN_CHARACTER) {
            return "";
        }
        if (isQuote(first)) {
            return readQuoted(first);
        } else if (isUnquoted(first)) {
            return readUnquoted(first);
        } else {
            reset();
            return "";
        }
    }

    @NotNull
    public T readUnquotedTag() throws IOException {
        final int first = read();
        if (isUnquoted(first)) {
            return readUnquotedTag(first);
        }
        throw new IOException("Cannot read '" + first + "' as quoted tag");
    }

    @NotNull
    protected T readUnquotedTag(int first) throws IOException {
        String unquoted = readUnquoted(first);

        final char last = unquoted.charAt(unquoted.length() - 1);

        final Boolean result;
        final Tag<?> type;
        if (unquoted.length() > 1 && NUMBER_SUFFIX.contains(last)) { // Number with suffix
            final String s = unquoted.substring(0, unquoted.length() - 1);
            result = isNumber(s);
            if (!Boolean.FALSE.equals(result)) {
                unquoted = s;
                type = Tag.getType(last);
            } else {
                type = null;
            }
        } else if ((result = isNumber(unquoted)) == null) { // Decimal
            type = Tag.DOUBLE;
        } else if (result) { // Integer
            type = Tag.INT;
        } else if (unquoted.equals("true")) { // boolean
            return mapper.build(Tag.BYTE, (byte) 1);
        } else if (unquoted.equals("false")) { // boolean
            return mapper.build(Tag.BYTE, (byte) 0);
        } else {
            type = null;
        }

        if (type != null) {
            if (result == null && !type.isDecimal()) {
                throw new IOException("Cannot read decimal number '" + unquoted + "' as " + type.getPrettyName());
            }
            switch (type.getId()) {
                case 1:
                    return mapper.build(Tag.BYTE, Byte.parseByte(unquoted));
                case 2:
                    return mapper.build(Tag.SHORT, Short.parseShort(unquoted));
                case 3:
                    return mapper.build(Tag.INT, Integer.parseInt(unquoted));
                case 4:
                    return mapper.build(Tag.LONG, Long.parseLong(unquoted));
                case 5:
                    return mapper.build(Tag.FLOAT, Float.parseFloat(unquoted));
                case 6:
                    return mapper.build(Tag.DOUBLE, Double.parseDouble(unquoted));
            }
        }
        return mapper.build(Tag.STRING, unquoted);
    }

    @NotNull
    protected String readUnquoted() throws IOException {
        final int first = read();
        if (first == UNKNOWN_CHARACTER) {
            throw new IOException("Cannot read unquoted string");
        }
        return readUnquoted(first);
    }

    @NotNull
    protected String readUnquoted(int first) throws IOException {
        final StringBuilder builder = new StringBuilder();
        builder.append((char) first);
        mark(1);
        int i;
        while ((i = read()) != -1) {
            if (isUnquoted(i)) {
                mark(1);
                builder.append((char) i);
            } else {
                break;
            }
        }
        reset();
        return builder.toString();
    }

    @NotNull
    public T readQuotedTag() throws IOException {
        final int first = read();
        if (isQuote(first)) {
            return readQuotedTag(first);
        }
        throw new IOException("Cannot read quoted tag without leading quote character");
    }

    @NotNull
    protected T readQuotedTag(int quote) throws IOException {
        return mapper.build(Tag.STRING, readQuoted(quote));
    }

    @NotNull
    protected String readQuoted(int quote) throws IOException {
        final StringBuilder builder = new StringBuilder();
        boolean escape = false;
        int i;
        while ((i = read()) != UNKNOWN_CHARACTER) {
            if (i == quote) {
                if (escape) {
                    escape = false;
                    builder.setCharAt(builder.length() - 1, (char) i);
                } else {
                    return builder.toString();
                }
            } else if (i == '\\') {
                escape = true;
            }
            builder.append((char) i);
        }
        throw new IOException("Non closed quoted string: " + builder);
    }

    @NotNull
    public T readArrayTag() throws IOException {
        if (read() != '[') {
            throw new IOException("Array tag must start with '['");
        }
        final int id = read();
        if (id == UNKNOWN_CHARACTER) {
            throw new IOException("Array tag must have id");
        }
        if (read() != ';') {
            throw new IOException("Array tag must have ';' after id");
        }
        return readArrayTag(id);
    }

    @NotNull
    protected T readArrayTag(int id) throws IOException {
        final Tag<Object> type = Tag.getArrayType((char) id);
        if (type.isValid()) {
            switch (type.getId()) {
                case 7:
                    return readArrayTag(type, true, Byte::parseByte, mapper::byteArray);
                case 11:
                    return readArrayTag(type, false, Integer::parseInt, mapper::intArray);
                case 12:
                    return readArrayTag(type, true, Long::parseLong, mapper::longArray);
            }
        }
        throw new IOException("Cannot read invalid tag array: " + type.getName());
    }

    @NotNull
    protected T readArrayTag(@NotNull Tag<Object> type, boolean suffix, @NotNull Function<String, Object> valueFunction, @NotNull Function<List<Object>, Object> arrayFunction) throws IOException {
        final List<Object> array = new ArrayList<>();
        String unquoted;
        while (!(unquoted = readUnquoted()).isEmpty()) {
            if (suffix) {
                final char last = unquoted.charAt(unquoted.length() - 1);
                if (last == type.getSuffix() || last == Character.toLowerCase(type.getSuffix())) {
                    unquoted = unquoted.substring(0, unquoted.length() - 1);
                }
            }
            if (unquoted.equals("true")) {
                array.add(valueFunction.apply("1"));
            } else if (unquoted.equals("false")) {
                array.add(valueFunction.apply("0"));
            } else {
                array.add(valueFunction.apply(unquoted));
            }
            if (skip(',')) {
                skipSpaces();
            } else {
                break;
            }
        }
        if (!skip(']')) {
            throw new IOException("Array tag must end with ']': " + array);
        }
        return mapper.build(type, arrayFunction.apply(array));
    }

    @NotNull
    public T readListTag() throws IOException {
        if (read() != '[') {
            throw new IOException("List tag must start with '['");
        }
        final List<T> list = new ArrayList<>();
        T value;
        while ((value = readTag()) != null) {
            list.add(value);
            if (skip(',')) {
                skipSpaces();
            } else {
                break;
            }
        }
        if (!skip(']')) {
            throw new IOException("List tag must end with ']': " + list);
        }
        return mapper.build(Tag.LIST, list);
    }

    @NotNull
    public T readCompoundTag() throws IOException {
        if (read() != '{') {
            throw new IOException("Compound tag must start with '{'");
        }
        return readCompoundTag0();
    }

    @NotNull
    protected T readCompoundTag0() throws IOException {
        final Map<String, T> map = new HashMap<>();
        String key;
        while (!(key = readKey()).isEmpty()) {
            if (!skip(':')) {
                throw new IOException("Compound key must have colon separator: " + key);
            }
            final T value = readTag();
            if (value == null) {
                break;
            }
            map.put(key, value);
            if (skip(',')) {
                skipSpaces();
            } else {
                break;
            }
        }
        if (!skip('}')) {
            throw new IOException("Compound tag must end with '}': " + map);
        }
        return mapper.build(Tag.COMPOUND, map);
    }

    @Override
    public int read() throws IOException {
        return reader.read();
    }

    @Override
    public int read(@NotNull char[] cbuf, int off, int len) throws IOException {
        return reader.read(cbuf, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return reader.skip(n);
    }

    public boolean skip(char c) throws IOException {
        mark(1);
        int i;
        while (Character.isWhitespace((i = read()))) {
            mark(1);
        }
        if (i != c) {
            reset();
            return false;
        }
        return true;
    }

    public long skipSpaces() throws IOException {
        mark(1);
        long count = 0;
        while (Character.isWhitespace(read())) {
            count++;
            mark(1);
        }
        reset();
        return count;
    }

    @Override
    public boolean ready() throws IOException {
        return reader.ready();
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        reader.mark(readAheadLimit);
    }

    @Override
    public void reset() throws IOException {
        reader.reset();
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
