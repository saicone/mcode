package com.saicone.mcode.module.lang;

import com.saicone.mcode.module.lang.display.*;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Map;

public class AdventureLang {

    public static class TextLoader<T> extends TextDisplay.Loader<T> {
        @Override
        protected @Nullable Object parseAction(@NotNull String s) {
            return TextDisplay.Event.of(s);
        }

        @Override
        protected void sendText(@NotNull T type, @NotNull String text) {
            for (String s : text.split("\n")) {
                ((Audience) type).sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(s));
            }
        }

        @Override
        protected TextDisplay.Builder<T> newBuilder() {
            return new TextBuilder<>();
        }
    }

    public static class TextBuilder<T> extends TextDisplay.Builder<T> {

        private TextComponent.Builder builder = Component.text();

        @Override
        public void append(@NotNull String s, boolean before) {
            if (before) {
                final TextComponent.Builder sameBuilder = builder;
                builder = Component.text();
                builder.append(LegacyComponentSerializer.legacyAmpersand().deserialize(s));
                builder.append(sameBuilder.build());
            } else {
                builder.append(LegacyComponentSerializer.legacyAmpersand().deserialize(s));
            }
        }

        @Override
        public void append(@NotNull String s, @NotNull Map<Object, String> actions) {
            final TextComponent.Builder component = Component.text();
            component.append(LegacyComponentSerializer.legacyAmpersand().deserialize(s));
            for (var entry : actions.entrySet()) {
                if (!(entry.getKey() instanceof TextDisplay.Event)) {
                    continue;
                }
                switch ((TextDisplay.Event) entry.getKey()) {
                    case SHOW_TEXT:
                        component.hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacyAmpersand().deserialize(entry.getValue())));
                        break;
                    case OPEN_URL:
                        component.clickEvent(ClickEvent.openUrl(entry.getValue()));
                        break;
                    case OPEN_FILE:
                        component.clickEvent(ClickEvent.openFile(entry.getValue()));
                        break;
                    case RUN_COMMAND:
                        component.clickEvent(ClickEvent.runCommand(entry.getValue()));
                        break;
                    case SUGGEST_COMMAND:
                        component.clickEvent(ClickEvent.suggestCommand(entry.getValue()));
                        break;
                    case CHANGE_PAGE:
                        component.clickEvent(ClickEvent.changePage(entry.getValue()));
                        break;
                    case COPY_TO_CLIPBOARD:
                        component.clickEvent(ClickEvent.copyToClipboard(entry.getValue()));
                        break;
                    default:
                        break;
                }
            }
            builder.append(component.build());
        }

        @Override
        public void sendTo(@NotNull T type) {
            ((Audience) type).sendMessage(builder.build());
        }
    }

    public static class TitleLoader<T> extends TitleDisplay.Loader<T> {
        @Override
        protected void sendTitle(@NotNull T type, @NotNull String title, @NotNull String subtitle, int fadeIn, int stay, int fadeOut) {
            final Title.Times times = Title.Times.times(
                    Duration.ofMillis((long) fadeIn * 1000 / 20000),
                    Duration.ofMillis((long) stay * 1000 / 20000),
                    Duration.ofMillis((long) fadeOut * 1000 / 20000)
            );
            final Title finalTitle = Title.title(
                    LegacyComponentSerializer.legacyAmpersand().deserialize(title),
                    LegacyComponentSerializer.legacyAmpersand().deserialize(subtitle),
                    times
            );
            ((Audience) type).showTitle(finalTitle);
        }
    }

    public static class ActionbarLoader<T> extends ActionbarDisplay.Loader<T> {
        @Override
        protected void sendActionbar(@NotNull T type, @NotNull String actionbar) {
            ((Audience) type).sendActionBar(LegacyComponentSerializer.legacyAmpersand().deserialize(actionbar));
        }
    }

    public static class SoundLoader<T> extends SoundDisplay.Loader<T, Sound> {
        @Override
        protected @Nullable Sound parseSound(@NotNull String s) {
            final String[] split = s.split(" ", 2);
            if (split.length < 2) {
                return null;
            }
            final Sound.Source source;
            try {
                source = Sound.Source.valueOf(split[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
            return Sound.sound(Key.key(split[0]), source, 1f, 1f);
        }

        @Override
        protected void playSound(@NotNull T type, @NotNull Sound sound, float volume, float pitch) {
            ((Audience) type).playSound(Sound.sound(sound.name(), sound.source(), volume, pitch));
        }
    }

    public static class BossbarLoader<T> extends BossbarDisplay.Loader<T> {
        public BossbarLoader() {
            this(false);
        }

        public BossbarLoader(boolean register) {
            super(register);
        }

        @Override
        protected BossbarDisplay.Builder<T> newBuilder(float progress, @NotNull String color, @NotNull String overlay, long stay) {
            BossBar.Color barColor;
            try {
                barColor = BossBar.Color.valueOf(color);
            } catch (IllegalArgumentException e) {
                barColor = BossBar.Color.RED;
            }
            BossBar.Overlay barOverlay;
            try {
                barOverlay = BossBar.Overlay.valueOf(overlay);
            } catch (IllegalArgumentException e) {
                barOverlay = BossBar.Overlay.PROGRESS;
            }
            return new BossbarBuilder<>(progress, barColor, barOverlay, stay) {
                @Override
                protected void later(@NotNull Runnable runnable, long ticks) {
                    BossbarLoader.this.later(runnable, ticks);
                }
            };
        }

        protected void later(@NotNull Runnable runnable, long ticks) {
            final long millis = ticks * 1000 / 20000;
            // Dirty delay, must be overridden
            new Thread(() -> {
                try {
                    Thread.sleep(millis);
                    runnable.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    public static abstract class BossbarBuilder<T> extends BossbarDisplay.Builder<T> {

        private final BossBar.Color color;
        private final BossBar.Overlay overlay;

        public BossbarBuilder(float progress, @NotNull BossBar.Color color, @NotNull BossBar.Overlay overlay, long stay) {
            super(progress, stay);
            this.color = color;
            this.overlay = overlay;
        }

        @Override
        public void sendTo(@NotNull T type, @NotNull String text) {
            final BossBar bossBar = BossBar.bossBar(LegacyComponentSerializer.legacyAmpersand().deserialize(text), progress, color, overlay);
            ((Audience) type).showBossBar(bossBar);
            later(() -> ((Audience) type).hideBossBar(bossBar), stay);
        }

        protected abstract void later(@NotNull Runnable runnable, long ticks);
    }
}
