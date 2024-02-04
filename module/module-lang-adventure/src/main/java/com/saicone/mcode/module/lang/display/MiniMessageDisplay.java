package com.saicone.mcode.module.lang.display;

import com.saicone.mcode.module.lang.Display;
import com.saicone.mcode.module.lang.DisplayLoader;
import com.saicone.mcode.util.DMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class MiniMessageDisplay<SenderT> implements Display<SenderT> {
    private final String text;

    public MiniMessageDisplay(@NotNull String text) {
        this.text = text;
    }

    @Override
    public @Nullable Object get(@NotNull String field) {
        if (field.equalsIgnoreCase("text")) {
            return text;
        } else {
            return null;
        }
    }

    @NotNull
    @Override
    public String getText() {
        return text;
    }

    @Override
    public void sendTo(@NotNull SenderT type, @NotNull Function<String, String> parser) {
        sendMiniMessage(type, parser.apply(text));
    }

    @Override
    public void sendTo(@NotNull Collection<SenderT> senders, @NotNull Function<String, String> parser, @NotNull BiFunction<SenderT, String, String> playerParser) {
        String minimessage = parser.apply(text);
        for (SenderT player : senders) {
            sendMiniMessage(player, playerParser.apply(player, minimessage));
        }
    }

    protected abstract void sendMiniMessage(@NotNull SenderT type, @NotNull String miniMessage);

    public static abstract class Loader<SenderT> extends DisplayLoader<SenderT> {

        public Loader() {
            super("(?i)mini-?(message|msg)s?", Map.of("minimessage", ""));
        }

        @Override
        public @Nullable Display<SenderT> load(@NotNull String text) {
            if (text.isEmpty()) {
                return null;
            }
            return new MiniMessageDisplay<>(text) {
                @Override
                protected void sendMiniMessage(@NotNull SenderT type, @NotNull String miniMessage) {
                    Loader.this.sendMiniMessage(type, MiniMessage.miniMessage().deserialize(miniMessage));
                }
            };
        }

        @Override
        public @Nullable Display<SenderT> load(@NotNull List<Object> list) {
            final StringBuilder builder = new StringBuilder();
            for (Object object : list) {
                builder.append(object).append(' ');
            }
            return load(builder.toString());
        }

        @Override
        public @Nullable Display<SenderT> load(@NotNull DMap map) {
            return load(map.getBy(String::valueOf, m -> m.getRegex("(?i)value|text|minimessage"), ""));
        }

        protected abstract void sendMiniMessage(@NotNull SenderT type, @NotNull Component miniMessage);
    }
}
