package com.saicone.mcode.module.lang.display;

import com.saicone.mcode.module.lang.DisplayLoader;
import com.saicone.mcode.platform.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
    public void sendTo(@NotNull SenderT type, @NotNull Function<String, String> parser) {
        sendActionbar(type, parser.apply(text));
    }

    @Override
    public void sendToAll(@NotNull Function<String, String> parser) {
        String actionbar = parser.apply(text);
        for (SenderT player : players()) {
            sendActionbar(player, Text.of(actionbar).parse(player).toString());
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
