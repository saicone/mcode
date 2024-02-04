package com.saicone.mcode.module.lang.display;

import com.saicone.mcode.module.lang.Display;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DisplayList<SenderT> implements Display<SenderT> {

    private final List<Display<SenderT>> list;

    public DisplayList(List<Display<SenderT>> list) {
        this.list = list;
    }

    public List<Display<SenderT>> getList() {
        return list;
    }

    @Override
    public void sendTo(@NotNull SenderT type, @NotNull Function<String, String> parser) {
        for (Display<SenderT> display : list) {
            display.sendTo(type, parser);
        }
    }

    @Override
    public void sendTo(@NotNull Collection<SenderT> senders, @NotNull Function<String, String> parser, @NotNull BiFunction<SenderT, String, String> playerParser) {
        for (Display<SenderT> display : list) {
            display.sendTo(senders, parser, playerParser);
        }
    }
}
