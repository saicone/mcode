package com.saicone.mcode.module.lang.display;

import com.saicone.mcode.module.lang.LangLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DisplayList<SenderT> extends Display<SenderT> {

    private final List<Display<SenderT>> list;

    public DisplayList(List<Display<SenderT>> list) {
        this.list = list;
    }

    public List<Display<SenderT>> getList() {
        return list;
    }

    @Override
    public void sendTo(@NotNull LangLoader<SenderT, ? extends SenderT> loader, @NotNull SenderT sender, @Nullable Object... args) {
        for (Display<SenderT> display : list) {
            display.sendTo(loader, sender, args);
        }
    }

    @Override
    public void sendTo(@NotNull LangLoader<SenderT, ? extends SenderT> loader, @NotNull SenderT agent, @NotNull SenderT sender, @Nullable Object... args) {
        for (Display<SenderT> display : list) {
            display.sendTo(loader, agent, sender, args);
        }
    }

    @Override
    public void sendToAll(@NotNull LangLoader<SenderT, ? extends SenderT> loader, @Nullable Object... args) {
        for (Display<SenderT> display : list) {
            display.sendToAll(loader, args);
        }
    }

    @Override
    public void sendToAll(@NotNull LangLoader<SenderT, ? extends SenderT> loader, @NotNull SenderT agent, @Nullable Object... args) {
        for (Display<SenderT> display : list) {
            display.sendToAll(loader, agent, args);
        }
    }
}
