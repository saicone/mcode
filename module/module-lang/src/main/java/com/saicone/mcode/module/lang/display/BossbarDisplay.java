package com.saicone.mcode.module.lang.display;

import com.saicone.mcode.module.lang.Display;
import com.saicone.mcode.module.lang.DisplayLoader;
import com.saicone.mcode.util.DMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BossbarDisplay<SenderT> implements Display<SenderT> {

    private final String text;
    private final Builder<SenderT> builder;

    public BossbarDisplay(@NotNull String text, @NotNull Builder<SenderT> builder) {
        this.text = text;
        this.builder = builder;
    }

    @Override
    public @Nullable Object get(@NotNull String field) {
        switch (field.toLowerCase()) {
            case "text":
            case "bossbar":
            case "value":
                return text;
            default:
                return builder.get(field);
        }
    }

    @NotNull
    @Override
    public String getText() {
        return text;
    }

    @Override
    public void sendTo(@NotNull SenderT type, @NotNull Function<String, String> parser) {
        builder.sendTo(type, parser.apply(text));
    }

    @Override
    public void sendTo(@NotNull Collection<SenderT> senders, @NotNull Function<String, String> parser, @NotNull BiFunction<SenderT, String, String> playerParser) {
        final String text = parser.apply(this.text);
        for (SenderT player : senders) {
            builder.sendTo(player, playerParser.apply(player, text));
        }
    }

    public static abstract class Builder<SenderT> {

        protected final float progress;
        protected final long stay;

        public Builder(float progress, long stay) {
            this.progress = progress;
            this.stay = stay;
        }

        @Nullable
        public Object get(@NotNull String field) {
            if (field.equals("progress")) {
                return progress;
            } else if (field.equals("stay")) {
                return stay;
            } else {
                return null;
            }
        }

        public abstract void sendTo(@NotNull SenderT type, @NotNull String text);
    }

    public static abstract class Loader<SenderT> extends DisplayLoader<SenderT> {

        public Loader() {
            this(true);
        }

        public Loader(boolean register) {
            super("(?i)boss(-?bar)?", Map.of("text", "", "progress", 1.0f, "color", "RED", "style", "FLAT"), register);
        }

        @Override
        public @Nullable Display<SenderT> load(@NotNull DMap map) {
            final String text = map.getBy(String::valueOf, m -> m.getRegex("(?i)value|text"), "");
            if (text.isBlank()) {
                return null;
            }
            final float progress = map.getBy(o -> Float.parseFloat(String.valueOf(o)), m -> m.getIgnoreCase("progress"), 1.0f);
            final String color = map.getBy(String::valueOf, m -> m.getIgnoreCase("color"), "RED");
            final String style = map.getBy(String::valueOf, m -> m.getRegex("(?i)overlay|style"), "FLAT");
            final long stay = map.getBy(o -> Long.parseLong(String.valueOf(o)), m -> m.getIgnoreCase("stay"), 20L);
            return new BossbarDisplay<>(text, newBuilder(progress, color, style, stay));
        }

        protected abstract Builder<SenderT> newBuilder(float progress, @NotNull String color, @NotNull String style, long stay);
    }
}
