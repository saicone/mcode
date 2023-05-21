package com.saicone.mcode.module.lang.display;

import com.saicone.mcode.module.lang.DisplayLoader;
import com.saicone.mcode.module.lang.LangLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public abstract class ActionbarDisplay<SenderT> extends Display<SenderT> {

    private final String text;

    public ActionbarDisplay(@NotNull String text) {
        this.text = text;
    }

    @NotNull
    @Override
    public String getText() {
        return text;
    }

    @Override
    public void sendTo(@NotNull LangLoader<SenderT, ? extends SenderT> loader, @NotNull SenderT type, @Nullable Object... args) {
        sendActionbar(type, loader.parse(type, args(text, args)));
    }

    @Override
    public void sendTo(@NotNull LangLoader<SenderT, ? extends SenderT> loader, @NotNull SenderT agent, @NotNull SenderT type, @Nullable Object... args) {
        sendActionbar(type, loader.parse(agent, type, args(text, args)));
    }

    @Override
    public void sendToAll(@NotNull LangLoader<SenderT, ? extends SenderT> loader, @Nullable Object... args) {
        String actionbar = args(text, args);
        for (SenderT player : loader.getPlayers()) {
            sendActionbar(player, loader.parse(player, actionbar));
        }
    }

    @Override
    public void sendToAll(@NotNull LangLoader<SenderT, ? extends SenderT> loader, @NotNull SenderT agent, @Nullable Object... args) {
        String actionbar = loader.parseAgent(agent, args(text, args));
        for (SenderT player : loader.getPlayers()) {
            sendActionbar(player, loader.parse(player, actionbar));
        }
    }

    protected abstract void sendActionbar(@NotNull SenderT type, @NotNull String actionbar);

    public static abstract class Loader<SenderT> extends DisplayLoader<SenderT> {

        public Loader() {
            super("(?i)actionbars?", Map.of("actionbar", ""));
        }

        @Override
        public @Nullable Display<SenderT> load(@NotNull String text) {
            if (text.isEmpty()) {
                return null;
            }
            return new ActionbarDisplay<>(text) {
                @Override
                protected void sendActionbar(@NotNull SenderT type, @NotNull String actionbar) {
                    Loader.this.sendActionbar(type, actionbar);
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
        public @Nullable Display<SenderT> load(@NotNull Map<String, Object> map) {
            return load(getString(map, "actionbar", ""));
        }

        protected abstract void sendActionbar(@NotNull SenderT type, @NotNull String actionbar);
    }
}
