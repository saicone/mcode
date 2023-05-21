package com.saicone.mcode.module.lang.display;

import com.saicone.mcode.module.lang.DisplayLoader;
import com.saicone.mcode.module.lang.LangLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

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
    public void sendTo(@NotNull LangLoader<SenderT, ? extends SenderT> loader, @NotNull SenderT type, @Nullable Object... args) {
        sendTitle(type, loader.parse(type, args(title, args)), loader.parse(type, args(subtitle, args)));
    }

    @Override
    public void sendTo(@NotNull LangLoader<SenderT, ? extends SenderT> loader, @NotNull SenderT agent, @NotNull SenderT type, @Nullable Object... args) {
        sendTitle(type, loader.parse(agent, type, args(title, args)), loader.parse(agent, type, args(subtitle, args)));
    }

    @Override
    public void sendToAll(@NotNull LangLoader<SenderT, ? extends SenderT> loader, @Nullable Object... args) {
        final String parsedTitle = args(title, args);
        final String parsedSubtitle = args(subtitle, args);
        for (SenderT player : loader.getPlayers()) {
            sendTitle(player, loader.parse(player, parsedTitle), loader.parse(player, parsedSubtitle));
        }
    }

    @Override
    public void sendToAll(@NotNull LangLoader<SenderT, ? extends SenderT> loader, @NotNull SenderT agent, @Nullable Object... args) {
        final String parsedTitle = loader.parseAgent(agent, args(title, args));
        final String parsedSubtitle = loader.parseAgent(agent, args(subtitle, args));
        for (SenderT player : loader.getPlayers()) {
            sendTitle(player, loader.parse(player, parsedTitle), loader.parse(player, parsedSubtitle));
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
        public @Nullable Display<SenderT> load(@NotNull Map<String, Object> map) {
            final String title = getString(map, "title", "");
            final String subtitle = getString(map, "subtitle", "");
            if (title.isEmpty() && subtitle.isEmpty()) {
                return null;
            }
            final int fadeIn = getInteger(map, "fadein", 10);
            final int stay = getInteger(map, "stay", 70);
            final int fadeOut = getInteger(map, "fadeout", 20);

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
