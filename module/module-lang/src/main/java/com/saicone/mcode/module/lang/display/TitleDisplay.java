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

public abstract class TitleDisplay<SenderT> implements Display<SenderT> {

    private final Text title;
    private final Text subtitle;
    private final int fadeIn;
    private final int stay;
    private final int fadeOut;

    public TitleDisplay(@NotNull Text title, @NotNull Text subtitle, int fadeIn, int stay, int fadeOut) {
        this.title = title;
        this.subtitle = subtitle;
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    @Override
    public @Nullable Object get(@NotNull String field) {
        switch (field.toLowerCase()) {
            case "text":
            case "title":
            case "value":
                return title;
            case "subtitle":
                return subtitle;
            case "fadein":
                return fadeIn;
            case "stay":
                return stay;
            case "fadeout":
                return fadeOut;
            default:
                return null;
        }
    }

    @Override
    public @NotNull Text getText() {
        return title;
    }

    @NotNull
    public Text getTitle() {
        return title;
    }

    @NotNull
    public Text getSubtitle() {
        return subtitle;
    }

    public int getFadeIn() {
        return fadeIn;
    }

    public int getStay() {
        return stay;
    }

    public int getFadeOut() {
        return fadeOut;
    }

    @Override
    public void sendTo(@NotNull SenderT type, @NotNull Function<Text, Text> parser) {
        sendTitle(type, parser.apply(title), parser.apply(subtitle));
    }

    @Override
    public void sendTo(@NotNull Collection<? extends SenderT> senders, @NotNull Function<Text, Text> parser, @NotNull BiFunction<SenderT, Text, Text> playerParser) {
        final Text title = parser.apply(this.title);
        final Text subtitle = parser.apply(this.subtitle);
        for (SenderT player : senders) {
            sendTitle(player, playerParser.apply(player, title), playerParser.apply(player, subtitle));
        }
    }

    protected void sendTitle(@NotNull SenderT type, @NotNull Text title, @NotNull Text subtitle) {
        sendTitle(type, title, subtitle, fadeIn, stay, fadeOut);
    }

    protected abstract void sendTitle(@NotNull SenderT type, @NotNull Text title, @NotNull Text subtitle, int fadeIn, int stay, int fadeOut);

    public static abstract class Loader<SenderT> extends DisplayLoader<SenderT> {

        public Loader() {
            super("(?i)(sub-?)?titles?", Map.of("title", "", "subtitle", "", "fadein", 10, "stay", 70, "fadeout", 20));
        }

        @Override
        public @Nullable TitleDisplay<SenderT> load(@Nullable Object object) {
            return (TitleDisplay<SenderT>) super.load(object);
        }

        @Override
        public @Nullable TitleDisplay<SenderT> load(@NotNull String text) {
            return (TitleDisplay<SenderT>) super.load(text);
        }

        @Override
        public @Nullable TitleDisplay<SenderT> load(@NotNull List<Object> list) {
            return (TitleDisplay<SenderT>) super.load(list);
        }

        @Override
        public @Nullable TitleDisplay<SenderT> load(@NotNull DMap map) {
            final String title = map.getBy(String::valueOf, m -> m.getRegex("(?i)value|text|title"), "");
            final String subtitle = map.getBy(String::valueOf, m -> m.getIgnoreCase("subtitle"), "");
            if (title.isBlank() && subtitle.isBlank()) {
                return null;
            }
            final int fadeIn = map.getBy(o -> Integer.parseInt(String.valueOf(o)), m -> m.getIgnoreCase("fadeIn"), 10);
            final int stay = map.getBy(o -> Integer.parseInt(String.valueOf(o)), m -> m.getIgnoreCase("stay"), 70);
            final int fadeOut = map.getBy(o -> Integer.parseInt(String.valueOf(o)), m -> m.getIgnoreCase("fadeOut"), 20);

            return new TitleDisplay<>(Text.valueOf(title), Text.valueOf(subtitle), fadeIn, stay, fadeOut) {
                @Override
                protected void sendTitle(@NotNull SenderT type, @NotNull Text title, @NotNull Text subtitle, int fadeIn, int stay, int fadeOut) {
                    Loader.this.sendTitle(type, title, subtitle, fadeIn, stay, fadeOut);
                }
            };
        }

        protected abstract void sendTitle(@NotNull SenderT type, @NotNull Text title, @NotNull Text subtitle, int fadeIn, int stay, int fadeOut);
    }
}
