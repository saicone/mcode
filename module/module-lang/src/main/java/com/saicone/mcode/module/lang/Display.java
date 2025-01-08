package com.saicone.mcode.module.lang;

import com.saicone.mcode.platform.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

@FunctionalInterface
public interface Display<SenderT> {

    String DEFAULT_TYPE = "text";

    @NotNull
    static <T> Display<T> empty() {
        return (type, parser) -> {};
    }

    default void sendArgs(@NotNull SenderT type, @Nullable Object... args) {
        sendTo(type, text -> text.args(args).parse(type));
    }

    default void sendArgs(@NotNull Collection<? extends SenderT> senders, @Nullable Object... args) {
        sendTo(senders, text -> text.args(args), (player, text) -> text.parse(player));
    }

    default void sendArgsWith(@NotNull SenderT agent, @NotNull SenderT type, @Nullable Object... args) {
        sendTo(type, text -> text.args(args).parseAgent(type, agent));
    }

    default void sendArgsWith(@NotNull SenderT agent, @NotNull Collection<? extends SenderT> senders, @Nullable Object... args) {
        sendTo(senders, text -> text.args(args).parseAgent(agent), (player, text) -> text.parse(player));
    }

    void sendTo(@NotNull SenderT type, @NotNull Function<Text, Text> parser);

    default void sendTo(@NotNull Collection<? extends SenderT> senders, @NotNull Function<Text, Text> parser) {
        sendTo(senders, parser, (player, text) -> text.parse(player));
    }

    default void sendTo(@NotNull Collection<? extends SenderT> senders, @NotNull Function<Text, Text> parser, @NotNull BiFunction<SenderT, Text, Text> playerParser) {
        for (SenderT sender : senders) {
            sendTo(sender, parser);
        }
    }

    default @Nullable Object get(@NotNull String field) {
        return null;
    }

    default @NotNull Text getText() {
        return Text.valueOf(get("text"));
    }

    default @Nullable Text getTextOrNull() {
        final Object object = get("text");
        return object == null ? null : Text.valueOf(object);
    }
}
