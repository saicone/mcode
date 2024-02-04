package com.saicone.mcode.module.lang;

import com.saicone.mcode.platform.Text;
import com.saicone.mcode.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

@FunctionalInterface
public interface Display<SenderT> {

    @NotNull
    static <T> Display<T> empty() {
        return (type, parser) -> {};
    }

    default void sendArgs(@NotNull SenderT type, @Nullable Object... args) {
        sendTo(type, s -> Text.of(s).args(args).parse(type).toString());
    }

    default void sendArgs(@NotNull Collection<SenderT> senders, @Nullable Object... args) {
        sendTo(senders, s -> Strings.replaceArgs(s, args), (player, s) -> Text.of(s).parse(player).color().getString());
    }

    default void sendArgs(@NotNull SenderT agent, @NotNull SenderT type, @Nullable Object... args) {
        sendTo(type, s -> Text.of(s).args(args).parseAgent(type, agent).toString());
    }

    default void sendArgs(@NotNull SenderT agent, @NotNull Collection<SenderT> senders, @Nullable Object... args) {
        sendTo(senders, s -> Text.of(s).args(args).parseAgent(agent).toString(), (player, s) -> Text.of(s).parse(player).color().getString());
    }

    void sendTo(@NotNull SenderT type, @NotNull Function<String, String> parser);

    default void sendArgs(@NotNull Collection<SenderT> senders, @NotNull Function<String, String> parser) {
        sendTo(senders, parser, (player, s) -> Text.of(s).parse(player).color().getString());
    }

    default void sendTo(@NotNull Collection<SenderT> senders, @NotNull Function<String, String> parser, @NotNull BiFunction<SenderT, String, String> playerParser) {
    }

    default @Nullable Object get(@NotNull String field) {
        return null;
    }

    @NotNull
    default String getText() {
        return String.valueOf(get("text"));
    }

    @Nullable
    default String getTextOrNull() {
        final Object object = get("text");
        return object == null ? null : String.valueOf(object);
    }
}
