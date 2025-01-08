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

public abstract class SoundDisplay<SenderT> implements Display<SenderT> {

    private final Text sound;
    private final float volume;
    private final float pitch;

    public SoundDisplay(@NotNull Text sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    public @Nullable Object get(@NotNull String field) {
        switch (field.toLowerCase()) {
            case "text":
            case "sound":
            case "value":
                return sound;
            case "volume":
                return volume;
            case "pitch":
                return pitch;
            default:
                return null;
        }
    }

    @NotNull
    public Text getSound() {
        return sound;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }

    @Override
    public void sendTo(@NotNull SenderT type, @NotNull Function<Text, Text> parser) {
        final Object parsedSound = parseSound(parser.apply(sound), volume, pitch);
        if (parsedSound != null) {
            playSound(type, parsedSound, volume, pitch);
        }
    }

    @Override
    public void sendTo(@NotNull Collection<? extends SenderT> senders, @NotNull Function<Text, Text> parser, @NotNull BiFunction<SenderT, Text, Text> playerParser) {
        final Object parsedSound = parseSound(parser.apply(sound), volume, pitch);
        if (parsedSound != null) {
            for (SenderT player : senders) {
                playSound(player, parsedSound, volume, pitch);
            }
        }
    }

    @Nullable
    protected abstract Object parseSound(@NotNull Text s, float volume, float pitch);

    protected abstract void playSound(@NotNull SenderT type, @NotNull Object sound, float volume, float pitch);

    public static abstract class Loader<SenderT> extends DisplayLoader<SenderT> {

        public Loader() {
            super("(?i)(play-?)?sound?", Map.of("sound", "", "volume", 1.0f, "pitch", 1.0f));
        }

        @Override
        public @Nullable SoundDisplay<SenderT> load(@Nullable Object object) {
            return (SoundDisplay<SenderT>) super.load(object);
        }

        @Override
        public @Nullable SoundDisplay<SenderT> load(@NotNull String text) {
            return (SoundDisplay<SenderT>) super.load(text);
        }

        @Override
        public @Nullable SoundDisplay<SenderT> load(@NotNull List<Object> list) {
            return (SoundDisplay<SenderT>) super.load(list);
        }

        @Override
        public @Nullable SoundDisplay<SenderT> load(@NotNull DMap map) {
            final String sound = map.getBy(String::valueOf, m -> m.getRegex("(?i)value|text|sound"), "");
            if (sound.isEmpty()) {
                return null;
            }
            final float volume = map.getBy(o -> Float.parseFloat(String.valueOf(o)), m -> m.getIgnoreCase("volume"), 1.0f);
            final float pitch = map.getBy(o -> Float.parseFloat(String.valueOf(o)), m -> m.getIgnoreCase("pitch"), 1.0f);

            return new SoundDisplay<>(Text.valueOf(sound), volume, pitch) {
                @Override
                protected @Nullable Object parseSound(@NotNull Text s, float volume, float pitch) {
                    return Loader.this.parseSound(s.getAsString().getValue(), volume, pitch);
                }

                @Override
                protected void playSound(@NotNull SenderT type, @NotNull Object sound1, float volume1, float pitch1) {
                    Loader.this.playSound(type, sound1, volume1, pitch1);
                }
            };
        }

        @Nullable
        protected abstract Object parseSound(@NotNull String s, float volume, float pitch);

        protected abstract void playSound(@NotNull SenderT type, @NotNull Object sound, float volume, float pitch);
    }
}
