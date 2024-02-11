package com.saicone.mcode.module.lang;

import com.saicone.mcode.module.lang.display.*;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public interface AdventureLang<SenderT> extends DisplaySupplier<SenderT> {

    @NotNull
    default MiniMessageDisplay.Loader<SenderT> getMiniMessageLoader() {
        final MiniMessageDisplay.Loader<SenderT> loader = (MiniMessageDisplay.Loader<SenderT>) getDisplayLoaderOrNull("minimessage");
        return Objects.requireNonNull(loader, "The minimessage loader doesn't exist");
    }

    interface AudienceSupplier<T> {
        default Audience getAudience(@NotNull T type) {
            return (Audience) type;
        }
    }

    class TextLoader<T> extends TextDisplay.Loader<T> implements AudienceSupplier<T> {
        @Override
        protected void sendText(@NotNull T type, @NotNull String text) {
            for (String s : text.split("\n")) {
                getAudience(type).sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(s));
            }
        }

        @Override
        protected TextDisplay.@NotNull Builder<T> newBuilder() {
            return new TextBuilder<>();
        }
    }

    class TextBuilder<T> extends TextDisplay.Builder<T> implements AudienceSupplier<T> {

        protected TextComponent.Builder builder = Component.text();

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
        @SuppressWarnings("deprecation")
        public void append(@NotNull String s, @NotNull Set<TextDisplay.Event> events) {
            final TextComponent.Builder component = Component.text();
            component.append(LegacyComponentSerializer.legacyAmpersand().deserialize(s));
            for (TextDisplay.Event event : events) {
                if (event.getAction().isClick()) {
                    switch (event.getAction().click()) {
                        case OPEN_URL:
                            component.clickEvent(ClickEvent.openUrl(event.getString()));
                            break;
                        case OPEN_FILE:
                            component.clickEvent(ClickEvent.openFile(event.getString()));
                            break;
                        case RUN_COMMAND:
                            component.clickEvent(ClickEvent.runCommand(event.getString()));
                            break;
                        case SUGGEST_COMMAND:
                            component.clickEvent(ClickEvent.suggestCommand(event.getString()));
                            break;
                        case CHANGE_PAGE:
                            component.clickEvent(ClickEvent.changePage(event.getString()));
                            break;
                        case COPY_TO_CLIPBOARD:
                            component.clickEvent(ClickEvent.copyToClipboard(event.getString()));
                            break;
                        default:
                            break;
                    }
                } else if (event.getAction().isHover()) {
                    switch (event.getAction().hover()) {
                        case SHOW_TEXT:
                            component.hoverEvent(HoverEvent.showText(LegacyComponentSerializer.legacyAmpersand().deserialize(event.getString())));
                            break;
                        case SHOW_ITEM:
                            final String tag = event.getItemTag();
                            component.hoverEvent(HoverEvent.showItem(
                                    Key.key(event.getItemId()),
                                    event.getItemCount(),
                                    tag == null ? null : BinaryTagHolder.of(tag)
                            ));
                            break;
                        case SHOW_ENTITY:
                            final String name = event.getEntityName();
                            component.hoverEvent(HoverEvent.showEntity(
                                    Key.key(event.getEntityType()),
                                    event.getEntityUniqueId(),
                                    name == null ? null : LegacyComponentSerializer.legacyAmpersand().deserialize(name)
                            ));
                            break;
                        default:
                            break;
                    }
                }
            }
            builder.append(component.build());
        }

        @Override
        public void sendTo(@NotNull T type) {
            getAudience(type).sendMessage(builder.build());
        }
    }

    class TitleLoader<T> extends TitleDisplay.Loader<T> implements AudienceSupplier<T> {
        @Override
        protected void sendTitle(@NotNull T type, @NotNull String title, @NotNull String subtitle, int fadeIn, int stay, int fadeOut) {
            final Title.Times times = Title.Times.times(
                    Duration.ofMillis((long) fadeIn * (1000 / 20)),
                    Duration.ofMillis((long) stay * (1000 / 20)),
                    Duration.ofMillis((long) fadeOut * (1000 / 20))
            );
            final Title finalTitle = Title.title(
                    LegacyComponentSerializer.legacyAmpersand().deserialize(title),
                    LegacyComponentSerializer.legacyAmpersand().deserialize(subtitle),
                    times
            );
            getAudience(type).showTitle(finalTitle);
        }
    }

    class ActionBarLoader<T> extends ActionBarDisplay.Loader<T> implements AudienceSupplier<T> {
        @Override
        protected void sendActionbar(@NotNull T type, @NotNull String actionbar) {
            getAudience(type).sendActionBar(LegacyComponentSerializer.legacyAmpersand().deserialize(actionbar));
        }
    }

    class SoundLoader<T> extends SoundDisplay.Loader<T> implements AudienceSupplier<T> {
        @Override
        protected @Nullable Sound parseSound(@NotNull String s, float volume, float pitch) {
            final String[] split = s.split(" ", 2);
            final Key key = Key.key(split[0]);
            final Sound.Source source;
            try {
                if (split.length < 2) {
                    source = Sound.Source.valueOf(key.value().substring(0, key.value().indexOf('_')).toUpperCase());
                } else {
                    source = Sound.Source.valueOf(split[1].toUpperCase());
                }
            } catch (IllegalArgumentException e) {
                return null;
            }
            return Sound.sound(key, source, volume, pitch);
        }

        @Override
        protected void playSound(@NotNull T type, @NotNull Object sound, float volume, float pitch) {
            getAudience(type).playSound((Sound) sound);
        }
    }

    class BossBarLoader<T> extends BossBarDisplay.Loader<T> {
        public BossBarLoader() {
            this(false);
        }

        public BossBarLoader(boolean register) {
            super(register);
        }

        @Override
        protected BossBarDisplay.Holder newHolder(float progress, @NotNull String text, @NotNull BossBarDisplay.Color color, @NotNull BossBarDisplay.Division division, @NotNull Set<BossBarDisplay.Flag> flags) {
            final BossBar bossBar = BossBar.bossBar(
                    LegacyComponentSerializer.legacyAmpersand().deserialize(text),
                    progress,
                    BossBar.Color.values()[color.ordinal()],
                    BossBar.Overlay.values()[division.ordinal()],
                    flags.isEmpty() ? Set.of() : flags.stream().map(flag -> BossBar.Flag.values()[flag.ordinal()]).collect(Collectors.toSet())
            );
            return newHolder(bossBar);
        }

        protected BossBarDisplay.Holder newHolder(@NotNull BossBar bossBar) {
            return new AdventureBossBar(bossBar);
        }
    }

    class MiniMessageLoader<T> extends MiniMessageDisplay.Loader<T> implements AudienceSupplier<T> {
        @Override
        protected void sendMiniMessage(@NotNull T type, @NotNull Component miniMessage) {
            getAudience(type).sendMessage(miniMessage);
        }
    }
}
