package com.saicone.mcode.module.lang.display;

import com.saicone.mcode.module.lang.Display;
import com.saicone.mcode.module.lang.DisplayLoader;
import com.saicone.mcode.platform.Text;
import com.saicone.mcode.util.DMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class ActionBarDisplay<SenderT> implements Display<SenderT> {

    private final Text text;

    public ActionBarDisplay(@NotNull Text text) {
        this.text = text;
    }

    @Override
    public @Nullable Object get(@NotNull String field) {
        switch (field.toLowerCase()) {
            case "text":
            case "actionbar":
            case "value":
                return text;
            default:
                return null;
        }
    }

    @Override
    public @NotNull Text getText() {
        return text;
    }

    @Override
    public void sendTo(@NotNull SenderT type, @NotNull Function<Text, Text> parser) {
        sendActionbar(type, parser.apply(text));
    }

    @Override
    public void sendTo(@NotNull Collection<? extends SenderT> senders, @NotNull Function<Text, Text> parser, @NotNull BiFunction<SenderT, Text, Text> playerParser) {
        final Text actionbar = parser.apply(text);
        for (SenderT player : senders) {
            sendActionbar(player, playerParser.apply(player, actionbar));
        }
    }

    protected abstract void sendActionbar(@NotNull SenderT type, @NotNull Text actionbar);

    public static abstract class Loader<SenderT> extends DisplayLoader<SenderT> {

        public Loader() {
            super("(?i)actionbars?", Map.of("actionbar", ""));
        }

        @Override
        public @Nullable ActionBarDisplay<SenderT> load(@Nullable Object object) {
            return (ActionBarDisplay<SenderT>) super.load(object);
        }

        @Override
        public @Nullable ActionBarDisplay<SenderT> load(@NotNull String text) {
            if (text.isEmpty()) {
                return null;
            }
            return new ActionBarDisplay<>(Text.valueOf(text)) {
                @Override
                protected void sendActionbar(@NotNull SenderT type, @NotNull Text actionbar) {
                    Loader.this.sendActionbar(type, actionbar);
                }
            };
        }

        @Override
        public @Nullable ActionBarDisplay<SenderT> load(@NotNull List<Object> list) {
            final StringBuilder builder = new StringBuilder();
            for (Object object : list) {
                builder.append(object).append(' ');
            }
            return load(builder.toString());
        }

        @Override
        public @Nullable ActionBarDisplay<SenderT> load(@NotNull DMap map) {
            return load(map.getBy(String::valueOf, m -> m.getRegex("(?i)value|text|actionbar"), ""));
        }

        protected abstract void sendActionbar(@NotNull SenderT type, @NotNull Text actionbar);
    }
}
