package com.saicone.mcode.module.lang.display;

import com.saicone.mcode.module.lang.DisplayLoader;
import com.saicone.mcode.util.DMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class TitleDisplay<SenderT> extends Display<SenderT> {

    private final String title;
    private final String subtitle;
    private final int fadeIn;
    private final int stay;
    private final int fadeOut;

    public TitleDisplay(@NotNull String title, @NotNull String subtitle, int fadeIn, int stay, int fadeOut) {
        this.title = title;
        this.subtitle = subtitle;
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    @NotNull
    public String getTitle() {
        return title;
    }

    @NotNull
    public String getSubtitle() {
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
    public @NotNull String getText() {
        return title + ' ' + subtitle;
    }

    @Override
    public void sendTo(@NotNull SenderT type, @NotNull Function<String, String> parser) {
        sendTitle(type, parser.apply(title), parser.apply(subtitle));
    }

    @Override
    public void sendToAll(@NotNull Function<String, String> parser, @NotNull BiFunction<SenderT, String, String> playerParser) {
        final String title = parser.apply(this.title);
        final String subtitle = parser.apply(this.subtitle);
        for (SenderT player : players()) {
            sendTitle(player, playerParser.apply(player, title), playerParser.apply(player, subtitle));
        }
    }

    protected void sendTitle(@NotNull SenderT type, @NotNull String title, @NotNull String subtitle) {
        sendTitle(type, title, subtitle, fadeIn, stay, fadeOut);
    }

    protected abstract void sendTitle(@NotNull SenderT type, @NotNull String title, @NotNull String subtitle, int fadeIn, int stay, int fadeOut);

    public static abstract class Loader<SenderT> extends DisplayLoader<SenderT> {

        public Loader() {
            super("(?i)(sub-?)?titles?", Map.of("title", "", "subtitle", "", "fadein", 10, "stay", 70, "fadeout", 20));
        }

        @Override
        public @Nullable Display<SenderT> load(@NotNull DMap map) {
            final String title = map.getBy(String::valueOf, m -> m.getRegex("(?i)value|text|title"), "");
            final String subtitle = map.getBy(String::valueOf, m -> m.getIgnoreCase("subtitle"), "");
            if (title.isBlank() && subtitle.isBlank()) {
                return null;
            }
            final int fadeIn = map.getBy(o -> Integer.parseInt(String.valueOf(o)), m -> m.getIgnoreCase("fadeIn"), 10);
            final int stay = map.getBy(o -> Integer.parseInt(String.valueOf(o)), m -> m.getIgnoreCase("stay"), 70);
            final int fadeOut = map.getBy(o -> Integer.parseInt(String.valueOf(o)), m -> m.getIgnoreCase("fadeOut"), 20);

            return new TitleDisplay<>(title, subtitle, fadeIn, stay, fadeOut) {
                @Override
                protected void sendTitle(@NotNull SenderT type, @NotNull String title, @NotNull String subtitle, int fadeIn, int stay, int fadeOut) {
                    Loader.this.sendTitle(type, title, subtitle, fadeIn, stay, fadeOut);
                }
            };
        }

        protected abstract void sendTitle(@NotNull SenderT type, @NotNull String title, @NotNull String subtitle, int fadeIn, int stay, int fadeOut);
    }
}
