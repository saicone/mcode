package com.saicone.mcode.module.lang.display;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DisplayList<SenderT> extends Display<SenderT> {

    private final List<Display<SenderT>> list;

    public DisplayList(List<Display<SenderT>> list) {
        this.list = list;
    }

    public List<Display<SenderT>> getList() {
        return list;
    }

    @Override
    public void sendTo(@NotNull SenderT sender, @Nullable Object... args) {
        for (Display<SenderT> display : list) {
            display.sendTo(sender, args);
        }
    }

    @Override
    public void sendTo(@NotNull SenderT agent, @NotNull SenderT sender, @Nullable Object... args) {
        for (Display<SenderT> display : list) {
            display.sendTo(agent, sender, args);
        }
    }

    @Override
    public void sendTo(@NotNull SenderT type, @NotNull Function<String, String> parser) {
        for (Display<SenderT> display : list) {
            display.sendTo(type, parser);
        }
    }

    @Override
    public void sendToAll(@Nullable Object... args) {
        for (Display<SenderT> display : list) {
            display.sendToAll(args);
        }
    }

    @Override
    public void sendToAll(@NotNull SenderT agent, @Nullable Object... args) {
        for (Display<SenderT> display : list) {
            display.sendToAll(agent, args);
        }
    }

    @Override
    public void sendToAll(@NotNull Function<String, String> parser) {
        for (Display<SenderT> display : list) {
            display.sendToAll(parser);
        }
    }

    @Override
    public void sendToAll(@NotNull Function<String, String> parser, @NotNull BiFunction<SenderT, String, String> playerParser) {
        for (Display<SenderT> display : list) {
            display.sendToAll(parser, playerParser);
        }
    }
}
