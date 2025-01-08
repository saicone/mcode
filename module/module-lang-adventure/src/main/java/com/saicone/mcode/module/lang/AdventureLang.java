package com.saicone.mcode.module.lang;

import com.saicone.mcode.module.lang.display.*;
import com.saicone.mcode.platform.MC;
import com.saicone.mcode.platform.Text;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.TagStringIO;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.DataComponentValue;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public interface AdventureLang<SenderT> extends DisplaySupplier<SenderT> {

    @NotNull
    default Audience getAudience(@NotNull SenderT type) {
        return (Audience) type;
    }

    default boolean useMiniMessage() {
        return false;
    }

    @NotNull
    default Component deserialize(@NotNull Text text) {
        if (useMiniMessage()) {
            return MiniMessage.miniMessage().deserialize(text.getAsString().getValue());
        } else {
            return LegacyComponentSerializer.legacyAmpersand().deserialize(text.getAsString().getValue());
        }
    }

    @NotNull
    default Component deserialize(@NotNull Audience audience, @NotNull Text text) {
        if (useMiniMessage()) {
            return MiniMessageDisplay.deserialize(audience, text);
        } else {
            return LegacyComponentSerializer.legacyAmpersand().deserialize(text.getAsString().getValue());
        }
    }

    abstract class DisplayLoader<T> {

        private final AdventureLang<T> lang;

        public DisplayLoader(@NotNull AdventureLang<T> lang) {
            this.lang = lang;
        }

        @NotNull
        protected AdventureLang<T> getLang() {
            return lang;
        }

        protected Audience getAudience(@NotNull T type) {
            return lang.getAudience(type);
        }

        @NotNull
        protected Component deserialize(@NotNull Audience audience, @NotNull Text text) {
            return lang.deserialize(audience, text);
        }
    }

    class TextLoader<T> extends TextDisplay.Loader<T> {

        private final AdventureLang<T> lang;

        public TextLoader(@NotNull AdventureLang<T> lang) {
            this.lang = lang;
        }

        @Override
        protected void sendText(@NotNull T type, @NotNull Text text) {
            final Audience audience = lang.getAudience(type);
            if (lang.useMiniMessage()) {
                audience.sendMessage(MiniMessageDisplay.deserialize(audience, text));
            } else {
                for (String s : text.getAsColored().getValue().split("\n")) {
                    audience.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(s));
                }
            }
        }

        @Override
        protected TextDisplay.@NotNull Builder<T> newBuilder() {
            return new TextBuilder<>(lang);
        }
    }

    class TextBuilder<T> extends TextDisplay.Builder<T> {

        private final AdventureLang<T> lang;

        protected TextComponent.Builder builder = Component.text();

        public TextBuilder(@NotNull AdventureLang<T> lang) {
            this.lang = lang;
        }

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
        public void append(@NotNull T type, @NotNull String s, @NotNull Set<TextDisplay.Event> events) {
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
                            // TODO: Make an implementation to get this hover event directly from Paper API
                            if (protocol(type) >= MC.V_1_20_5.protocol()) {
                                try {
                                    final Map<Key, DataComponentValue> map = new HashMap<>();
                                    final CompoundBinaryTag components = TagStringIO.get().asCompound(event.getItemComponents());
                                    for (Map.Entry<String, ? extends BinaryTag> entry : components) {
                                        map.put(Key.key(entry.getKey()), BinaryTagHolder.binaryTagHolder(write(entry.getValue())));
                                    }
                                    component.hoverEvent(HoverEvent.showItem(Key.key(event.getItemId()), event.getItemCount(), map));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                final String tag = event.getItemTag();
                                component.hoverEvent(HoverEvent.showItem(
                                        Key.key(event.getItemId()),
                                        event.getItemCount(),
                                        tag == null ? null : BinaryTagHolder.of(tag) // BinaryTagHolder.binaryTagHolder(tag) on newer versions
                                ));
                            }
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

        @NotNull
        private static String write(BinaryTag tag) {
            // PD: I hate adventure
            final StringBuilder builder = new StringBuilder();
            try {
                final Class<? extends AutoCloseable> writer = Class.forName("net.kyori.adventure.nbt.TagStringWriter").asSubclass(AutoCloseable.class);
                final Constructor<? extends AutoCloseable> constructor = writer.getDeclaredConstructor(Appendable.class, String.class);
                constructor.setAccessible(true);
                final Method method = writer.getDeclaredMethod("writeTag", BinaryTag.class);

                try (AutoCloseable w = constructor.newInstance(builder, "")) {
                    method.invoke(w, tag);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return builder.toString();
        }

        @Override
        public void sendTo(@NotNull T type) {
            lang.getAudience(type).sendMessage(builder.build());
        }
    }

    class TitleLoader<T> extends TitleDisplay.Loader<T> {

        private final AdventureLang<T> lang;

        public TitleLoader(@NotNull AdventureLang<T> lang) {
            this.lang = lang;
        }

        @Override
        protected void sendTitle(@NotNull T type, @NotNull Text title, @NotNull Text subtitle, int fadeIn, int stay, int fadeOut) {
            final Audience audience = lang.getAudience(type);
            final Title.Times times = Title.Times.times(
                    Duration.ofMillis((long) fadeIn * (1000 / 20)),
                    Duration.ofMillis((long) stay * (1000 / 20)),
                    Duration.ofMillis((long) fadeOut * (1000 / 20))
            );
            final Title finalTitle = Title.title(
                    lang.deserialize(audience, title),
                    lang.deserialize(audience, subtitle),
                    times
            );
            audience.showTitle(finalTitle);
        }
    }

    class ActionBarLoader<T> extends ActionBarDisplay.Loader<T> {

        private final AdventureLang<T> lang;

        public ActionBarLoader(@NotNull AdventureLang<T> lang) {
            this.lang = lang;
        }

        @Override
        protected void sendActionbar(@NotNull T type, @NotNull Text actionbar) {
            final Audience audience = lang.getAudience(type);
            audience.sendActionBar(lang.deserialize(audience, actionbar));
        }
    }

    class SoundLoader<T> extends SoundDisplay.Loader<T> {

        private final AdventureLang<T> lang;

        public SoundLoader(@NotNull AdventureLang<T> lang) {
            this.lang = lang;
        }

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
            lang.getAudience(type).playSound((Sound) sound);
        }
    }

    class BossBarLoader<T> extends BossBarDisplay.Loader<T> {

        private final AdventureLang<T> lang;

        public BossBarLoader(@NotNull AdventureLang<T> lang) {
            this.lang = lang;
        }

        @Override
        protected BossBarDisplay.Holder newHolder(float progress, @NotNull Text text, @NotNull BossBarDisplay.Color color, @NotNull BossBarDisplay.Division division, @NotNull Set<BossBarDisplay.Flag> flags) {
            final BossBar bossBar = BossBar.bossBar(
                    lang.deserialize(text),
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

    class MiniMessageLoader<T> extends MiniMessageDisplay.Loader<T> {

        private final AdventureLang<T> lang;

        public MiniMessageLoader(@NotNull AdventureLang<T> lang) {
            this.lang = lang;
        }

        @Override
        protected void sendMiniMessage(@NotNull Audience audience, @NotNull Component miniMessage) {
            audience.sendMessage(miniMessage);
        }

        @Override
        public Audience getAudience(@NotNull T type) {
            return lang.getAudience(type);
        }
    }
}
