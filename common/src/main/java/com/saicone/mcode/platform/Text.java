package com.saicone.mcode.platform;

import com.saicone.mcode.Platform;
import com.saicone.mcode.util.text.MStrings;
import com.saicone.mcode.util.text.Strings;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Text {

    private String string;

    @NotNull
    public static Text of(@Nullable String s) {
        return Platform.getInstance().getText(String.valueOf(s));
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static <T> T of(@NotNull T object, @NotNull Consumer<Text> consumer) {
        if (object instanceof Map) {
            for (var entry : ((Map<Object, Object>) object).entrySet()) {
                entry.setValue(of(entry.getValue(), consumer));
            }
        } else if (object instanceof List) {
            ((List<Object>) object).replaceAll(o -> of(o, consumer));
        } else if (object instanceof String[]) {
            final String[] array = (String[]) object;
            for (int i = 0; i < array.length; i++) {
                array[i] = of(array[i], consumer);
            }
        } else if (object instanceof String) {
            final Text text = of((String) object);
            consumer.accept(text);
            return (T) text.getString();
        }
        return object;
    }

    public Text(@NotNull String string) {
        this.string = string;
    }

    @NotNull
    public String getString() {
        return string;
    }

    public void setString(@NotNull String string) {
        this.string = string;
    }

    @NotNull
    @Contract("_ -> this")
    public Text parse(@Nullable Object subject) {
        return this;
    }

    @NotNull
    @Contract("_, _ -> this")
    public Text parse(@Nullable Object subject, @Nullable Object agent) {
        if (agent != null) {
            return parse(agent);
        } else {
            return parse(subject);
        }
    }

    @NotNull
    @Contract("_ -> this")
    public Text parseAgent(@Nullable Object agent) {
        return this;
    }

    @NotNull
    @Contract("_, _ -> this")
    public Text parseAgent(@Nullable Object subject, @Nullable Object agent) {
        parseAgent(agent);
        parse(subject);
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public Text args(@Nullable Object... args) {
        this.string = Strings.replaceArgs(this.string, args);
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public Text args(@NotNull Map<String, Object> args) {
        this.string = Strings.replaceArgs(this.string, args);
        return this;
    }

    @NotNull
    @Contract("-> this")
    public Text color() {
        return color('&');
    }

    @NotNull
    @Contract("_ -> this")
    public Text color(char colorChar) {
        this.string = MStrings.color(colorChar, this.string);
        return this;
    }

    @NotNull
    @Contract("_ -> this")
    public Text color(@NotNull List<String> colors) {
        return color('&', colors);
    }

    @NotNull
    @Contract("_, _ -> this")
    public Text color(char colorChar, @NotNull List<String> colors) {
        this.string = Strings.color(colorChar, this.string, colors);
        return this;
    }

    @NotNull
    @Contract("_, _, _ -> this")
    public <T> Text bracketPlaceholders(@Nullable T type, @NotNull Predicate<String> predicate, @NotNull BiFunction<T, String, Object> function) {
        return placeholders(type, '{', '}', predicate, function);
    }

    @NotNull
    @Contract("_, _, _ -> this")
    public <T> Text placeholders(@Nullable T type, @NotNull Predicate<String> predicate, @NotNull BiFunction<T, String, Object> function) {
        return placeholders(type, '%', '%', predicate, function);
    }

    @NotNull
    @Contract("_, _, _, _, _ -> this")
    public <T> Text placeholders(@Nullable T type, char start, char end, @NotNull Predicate<String> predicate, @NotNull BiFunction<T, String, Object> function) {
        this.string = Strings.replacePlaceholder(type, this.string, start, end, predicate, function);
        return this;
    }
}
