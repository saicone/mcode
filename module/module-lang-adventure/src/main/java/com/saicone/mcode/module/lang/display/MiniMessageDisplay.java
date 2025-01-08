package com.saicone.mcode.module.lang.display;

import com.saicone.mcode.module.lang.Display;
import com.saicone.mcode.module.lang.DisplayLoader;
import com.saicone.mcode.platform.Text;
import com.saicone.mcode.util.DMap;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class MiniMessageDisplay<SenderT> implements Display<SenderT> {

    private static final MethodHandle GET_AUDIENCE_PLACEHOLDERS;

    static {
        MethodHandle getAudiencePlaceholders = null;
        try {
            final Class<?> miniPlaceholders = Class.forName("io.github.miniplaceholders.api.MiniPlaceholders");
            getAudiencePlaceholders = MethodHandles.lookup().findStatic(miniPlaceholders, "getAudiencePlaceholders", MethodType.methodType(TagResolver.class, Audience.class));
        } catch (Throwable ignored) { }
        GET_AUDIENCE_PLACEHOLDERS = getAudiencePlaceholders;
    }

    public static boolean isPlaceholderPresent() {
        return GET_AUDIENCE_PLACEHOLDERS != null;
    }

    @NotNull
    public static TagResolver getAudiencePlaceholders(@NotNull Audience audience) {
        try {
            return (TagResolver) GET_AUDIENCE_PLACEHOLDERS.invoke(audience);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @NotNull
    public static Component deserialize(@NotNull Audience audience, @NotNull Text text) {
        if (isPlaceholderPresent()) {
            return MiniMessage.miniMessage().deserialize(text.getAsString().getValue(), getAudiencePlaceholders(audience));
        } else {
            return MiniMessage.miniMessage().deserialize(text.getAsString().getValue());
        }
    }

    private final Text text;

    public MiniMessageDisplay(@NotNull Text text) {
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

    @Override
    public @NotNull Text getText() {
        return text;
    }

    @Override
    public void sendTo(@NotNull SenderT type, @NotNull Function<Text, Text> parser) {
        sendMiniMessage(type, parser.apply(text));
    }

    @Override
    public void sendTo(@NotNull Collection<? extends SenderT> senders, @NotNull Function<Text, Text> parser, @NotNull BiFunction<SenderT, Text, Text> playerParser) {
        Text minimessage = parser.apply(text);
        for (SenderT player : senders) {
            sendMiniMessage(player, playerParser.apply(player, minimessage));
        }
    }

    protected abstract void sendMiniMessage(@NotNull SenderT type, @NotNull Text text);

    public static abstract class Loader<SenderT> extends DisplayLoader<SenderT> {

        public Loader() {
            super("(?i)mini-?(message|msg)s?", Map.of("minimessage", ""));
        }

        @Override
        public @Nullable Display<SenderT> load(@NotNull String text) {
            if (text.isEmpty()) {
                return null;
            }
            return new MiniMessageDisplay<>(Text.valueOf(text)) {
                @Override
                protected void sendMiniMessage(@NotNull SenderT type, @NotNull Text text) {
                    final Audience audience = getAudience(type);
                    Loader.this.sendMiniMessage(audience, deserialize(audience, text));
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

        protected abstract Audience getAudience(@NotNull SenderT type);

        protected abstract void sendMiniMessage(@NotNull Audience audience, @NotNull Component miniMessage);
    }
}
