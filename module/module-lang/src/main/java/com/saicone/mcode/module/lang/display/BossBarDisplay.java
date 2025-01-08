package com.saicone.mcode.module.lang.display;

import com.saicone.mcode.module.lang.Display;
import com.saicone.mcode.module.lang.DisplayLoader;
import com.saicone.mcode.platform.Text;
import com.saicone.mcode.util.DMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class BossBarDisplay<SenderT> implements Display<SenderT> {

    private final float progress;
    private final Text text;
    private final Color color;
    private final Division division;
    private final Set<Flag> flags;

    public BossBarDisplay(float progress, @NotNull Text text, @NotNull Color color, @NotNull Division division, @NotNull Set<Flag> flags) {
        this.progress = progress;
        this.text = text;
        this.color = color;
        this.division = division;
        this.flags = flags;
    }

    @Override
    public @Nullable Object get(@NotNull String field) {
        switch (field.toLowerCase()) {
            case "progress":
            case "percent":
                return progress;
            case "text":
            case "bossbar":
            case "value":
                return text;
            case "color":
                return color.name();
            case "division":
            case "overlay":
            case "style":
                return division.name();
            case "flag":
                return flags.isEmpty() ? null : flags.iterator().next().name();
            case "flags":
                return flags.stream().map(Flag::name).collect(Collectors.joining("\n"));
            default:
                return null;
        }
    }

    public float getProgress() {
        return progress;
    }

    @Override
    public @NotNull Text getText() {
        return text;
    }

    @NotNull
    public Color getColor() {
        return color;
    }

    @NotNull
    public Division getDivision() {
        return division;
    }

    @NotNull
    public Set<Flag> getFlags() {
        return flags;
    }

    @Override
    public void sendTo(@NotNull SenderT type, @NotNull Function<Text, Text> parser) {
        createHolder(parser).showTo(type);
    }

    @Override
    public void sendTo(@NotNull Collection<? extends SenderT> senders, @NotNull Function<Text, Text> parser, @NotNull BiFunction<SenderT, Text, Text> playerParser) {
        final Text text = parser.apply(this.text);
        for (SenderT player : senders) {
            createHolder(playerParser.apply(player, text)).showTo(player);
        }
    }

    @NotNull
    public Holder createHolder() {
        return createHolder(text);
    }

    @NotNull
    public Holder createHolder(@NotNull Function<Text, Text> parser) {
        return createHolder(parser.apply(text));
    }

    @NotNull
    public abstract Holder createHolder(@NotNull Text text);

    public interface Holder {

        boolean isVisible();

        boolean hasFlag(@NotNull Flag flag);

        float getProgress();

        @NotNull
        String getText();

        @NotNull
        Color getColor();

        @NotNull
        Division getDivision();

        @UnmodifiableView
        @NotNull
        Set<Flag> getFlags();

        void setProgress(float progress);

        void setText(@NotNull String text);

        void setColor(@NotNull Color color);

        void setDivision(@NotNull Division division);

        void setVisible(boolean visible);

        void addFlag(@NotNull Flag flag);

        default void addFlags(@NotNull Flag... flags) {
            for (Flag flag : flags) {
                addFlag(flag);
            }
        }

        default void addFlags(@NotNull Iterable<Flag> flags) {
            for (Flag flag : flags) {
                addFlag(flag);
            }
        }

        void removeFlag(@NotNull Flag flag);

        default void removeFlags(@NotNull Flag... flags) {
            for (Flag flag : flags) {
                removeFlag(flag);
            }
        }

        default void removeFlags(@NotNull Iterable<Flag> flags) {
            for (Flag flag : flags) {
                removeFlag(flag);
            }
        }

        void showTo(@NotNull Object type);

        void hideTo(@NotNull Object type);

        void hideToAll();
    }

    public static abstract class Loader<SenderT> extends DisplayLoader<SenderT> {

        public Loader() {
            super("(?i)boss(-?bar)?", Map.of("text", "", "progress", 1.0f, "color", "RED", "style", "FLAT"));
        }

        @Override
        public @Nullable BossBarDisplay<SenderT> load(@Nullable Object object) {
            return (BossBarDisplay<SenderT>) super.load(object);
        }

        @Override
        public @Nullable BossBarDisplay<SenderT> load(@NotNull String text) {
            return (BossBarDisplay<SenderT>) super.load(text);
        }

        @Override
        public @Nullable BossBarDisplay<SenderT> load(@NotNull List<Object> list) {
            return (BossBarDisplay<SenderT>) super.load(list);
        }

        @Override
        public @Nullable BossBarDisplay<SenderT> load(@NotNull DMap map) {
            final float progress = map.getBy(
                    o -> Float.parseFloat(String.valueOf(o)),
                    m -> m.getRegex("(?i)progress|percent"),
                    1.0f);
            final String text = map.getBy(
                    String::valueOf,
                    m -> m.getRegex("(?i)value|text|title"),
                    "");
            if (text.isBlank()) {
                return null;
            }
            final Color color = map.getBy(
                    o -> Color.of(String.valueOf(o), Color.RED),
                    m -> m.getIgnoreCase("color"),
                    Color.RED);
            final Division division = map.getBy(
                    o -> Division.of(String.valueOf(o), Division.NO_DIVISION),
                    m -> m.getRegex("(?i)division|overlay|style"),
                    Division.NO_DIVISION);
            final Object obj = map.getRegex("(?i)flags?");
            final Set<Flag> flags;
            if (obj instanceof Iterable) {
                flags = new HashSet<>();
                for (Object o : (Iterable<?>) obj) {
                    final Flag flag = Flag.of(String.valueOf(o), null);
                    if (flag != null) {
                        flags.add(flag);
                    }
                }
            } else {
                final Flag flag = Flag.of(String.valueOf(obj), null);
                if (flag != null) {
                    flags = Set.of(flag);
                } else {
                    flags = Set.of();
                }
            }
            return new BossBarDisplay<>(progress, Text.valueOf(text), color, division, flags) {
                @Override
                public @NotNull Holder createHolder(@NotNull Text text) {
                    return newHolder(getProgress(), text, getColor(), getDivision(), getFlags());
                }
            };
        }

        protected abstract Holder newHolder(float progress, @NotNull Text text, @NotNull Color color, @NotNull Division division, @NotNull Set<Flag> flags);
    }

    public enum Color {
        PINK,
        BLUE,
        RED,
        GREEN,
        YELLOW,
        PURPLE,
        WHITE;

        @Nullable
        @Contract("_, !null -> !null")
        public static Color of(@NotNull String name, @Nullable Color def) {
            for (Color value : values()) {
                if (value.name().equalsIgnoreCase(name)) {
                    return value;
                }
            }
            return def;
        }
    }

    public enum Division {
        NO_DIVISION("progress", "solid"),
        NOTCHED_6("6_notches"),
        NOTCHED_10("10_notches"),
        NOTCHED_12("12_notches"),
        NOTCHED_20("20_notches");

        private final String[] aliases;

        Division(@NotNull String... aliases) {
            this.aliases = aliases;
        }

        public boolean matches(@NotNull String s) {
            if (name().equalsIgnoreCase(s)) {
                return true;
            }
            for (String alias : aliases) {
                if (alias.equalsIgnoreCase(s)) {
                    return true;
                }
            }
            return false;
        }

        @NotNull
        public String[] getAliases() {
            return aliases;
        }

        @Nullable
        @Contract("_, !null -> !null")
        public static Division of(@NotNull String name, @Nullable Division def) {
            final String s = name.replace(' ', '_');
            for (Division value : values()) {
                if (value.matches(s)) {
                    return value;
                }
            }
            return def;
        }
    }

    public enum Flag {
        DARKEN_SKY("darken_screen"),
        DRAGON_BAR("play_boss_music"),
        CREATE_FOG("create_world_fog");

        private final String[] aliases;

        Flag(@NotNull String... aliases) {
            this.aliases = aliases;
        }

        public boolean matches(@NotNull String s) {
            if (name().equalsIgnoreCase(s)) {
                return true;
            }
            for (String alias : aliases) {
                if (alias.equalsIgnoreCase(s)) {
                    return true;
                }
            }
            return false;
        }

        @NotNull
        public String[] getAliases() {
            return aliases;
        }

        @Nullable
        @Contract("_, !null -> !null")
        public static Flag of(@NotNull String name, @Nullable Flag def) {
            final String s = name.replace(' ', '_');
            for (Flag value : values()) {
                if (value.matches(s)) {
                    return value;
                }
            }
            return def;
        }
    }
}
