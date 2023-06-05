package com.saicone.mcode.bukkit.lang;

import com.google.common.base.Enums;
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
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Map;

public class PaperLang extends BukkitLang {

    // Loadable display types
    public static final TextLoader TEXT = new TextLoader();
    public static final TitleLoader TITLE = new TitleLoader();
    public static final ActionbarLoader ACTIONBAR = new ActionbarLoader();
    public static final SoundLoader SOUND = new SoundLoader();

    private final BossbarLoader bossbar = new BossbarLoader();

    public PaperLang(@NotNull Plugin plugin, @NotNull Class<?>... langProviders) {
        super(plugin, langProviders);
    }

    public PaperLang(@NotNull Plugin plugin, boolean useConfig, @NotNull Class<?>... langProviders) {
        super(plugin, useConfig, langProviders);
    }

    public enum TextEvent {
        SHOW_TEXT, OPEN_URL, OPEN_FILE, RUN_COMMAND, SUGGEST_COMMAND, CHANGE_PAGE, COPY_TO_CLIPBOARD;
    }

    public static class TextLoader extends TextDisplay.Loader<CommandSender> {
        @Override
        protected @Nullable Object parseAction(@NotNull String s) {
            return Enums.getIfPresent(TextEvent.class, s.toUpperCase()).orNull();
        }

        @Override
        protected void sendText(@NotNull CommandSender sender, @NotNull String text) {
            for (String s : text.split("\n")) {
                ((Audience) sender).sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(s));
            }
        }

        @Override
        protected TextDisplay.Builder<CommandSender> newBuilder() {
            return new TextBuilder();
        }
    }

    public static class TextBuilder extends TextDisplay.Builder<CommandSender> {

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
                if (!(entry.getKey() instanceof TextEvent)) {
                    continue;
                }
                switch ((TextEvent) entry.getKey()) {
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
        public void sendTo(@NotNull CommandSender sender) {
            ((Audience) sender).sendMessage(builder.build());
        }
    }

    public static class TitleLoader extends TitleDisplay.Loader<CommandSender> {
        @Override
        protected void sendTitle(@NotNull CommandSender sender, @NotNull String title, @NotNull String subtitle, int fadeIn, int stay, int fadeOut) {
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
            ((Audience) sender).showTitle(finalTitle);
        }
    }

    public static class ActionbarLoader extends ActionbarDisplay.Loader<CommandSender> {
        @Override
        protected void sendActionbar(@NotNull CommandSender sender, @NotNull String actionbar) {
            ((Audience) sender).sendActionBar(LegacyComponentSerializer.legacyAmpersand().deserialize(actionbar));
        }
    }

    public static class SoundLoader extends SoundDisplay.Loader<CommandSender, Sound> {
        @Override
        protected @Nullable Sound parseSound(@NotNull String s) {
            final String[] split = s.split(" ", 2);
            if (split.length < 2) {
                return null;
            }
            final Sound.Source source = Enums.getIfPresent(Sound.Source.class, split[1].toUpperCase()).orNull();
            if (source == null) {
                return null;
            }
            return Sound.sound(Key.key(split[0]), source, 1f, 1f);
        }

        @Override
        protected void playSound(@NotNull CommandSender sender, @NotNull Sound sound, float volume, float pitch) {
            ((Audience) sender).playSound(Sound.sound(sound.name(), sound.source(), volume, pitch));
        }
    }

    public class BossbarLoader extends BossbarDisplay.Loader<CommandSender> {
        public BossbarLoader() {
            super(false);
        }

        @Override
        protected BossbarDisplay.Builder<CommandSender> newBuilder(float progress, @NotNull String color, @NotNull String overlay, long stay) {
            final BossBar.Color barColor = Enums.getIfPresent(BossBar.Color.class, color).or(BossBar.Color.RED);
            final BossBar.Overlay barOverlay = Enums.getIfPresent(BossBar.Overlay.class, overlay).or(BossBar.Overlay.PROGRESS);
            return new BossbarBuilder(progress, barColor, barOverlay, stay);
        }
    }

    public class BossbarBuilder extends BossbarDisplay.Builder<CommandSender> {

        private final BossBar.Color color;
        private final BossBar.Overlay overlay;

        public BossbarBuilder(float progress, @NotNull BossBar.Color color, @NotNull BossBar.Overlay overlay, long stay) {
            super(progress, stay);
            this.color = color;
            this.overlay = overlay;
        }

        @Override
        public void sendTo(@NotNull CommandSender sender, @NotNull String text) {
            final BossBar bossBar = BossBar.bossBar(LegacyComponentSerializer.legacyAmpersand().deserialize(text), progress, color, overlay);
            ((Audience) sender).showBossBar(bossBar);
            Bukkit.getScheduler().runTaskLater(getPlugin(), () -> ((Audience) sender).hideBossBar(bossBar), stay);
        }
    }
}
