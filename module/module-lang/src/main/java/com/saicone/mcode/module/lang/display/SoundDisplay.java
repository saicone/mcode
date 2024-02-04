package com.saicone.mcode.module.lang.display;

import com.saicone.mcode.module.lang.Display;
import com.saicone.mcode.module.lang.DisplayLoader;
import com.saicone.mcode.util.DMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class SoundDisplay<SenderT, SoundT> implements Display<SenderT> {

    private final String sound;
    private final float volume;
    private final float pitch;

    public SoundDisplay(@NotNull String sound, float volume, float pitch) {
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
    public String getSound() {
        return sound;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }

    @Override
    public void sendTo(@NotNull SenderT type, @NotNull Function<String, String> parser) {
        final SoundT parsedSound = parseSound(parser.apply(sound));
        if (parsedSound != null) {
            playSound(type, parsedSound, volume, pitch);
        }
    }

    @Override
    public void sendTo(@NotNull Collection<SenderT> senders, @NotNull Function<String, String> parser, @NotNull BiFunction<SenderT, String, String> playerParser) {
        final SoundT parsedSound = parseSound(parser.apply(sound));
        if (parsedSound != null) {
            for (SenderT player : senders) {
                playSound(player, parsedSound, volume, pitch);
            }
        }
    }

    @Nullable
    protected abstract SoundT parseSound(@NotNull String s);

    protected abstract void playSound(@NotNull SenderT type, @NotNull SoundT sound, float volume, float pitch);

    public static abstract class Loader<SenderT, SoundT> extends DisplayLoader<SenderT> {

        public Loader() {
            super("(?i)(play-?)?sound?", Map.of("sound", "", "volume", 1.0f, "pitch", 1.0f));
        }

        @Override
        public @Nullable Display<SenderT> load(@NotNull DMap map) {
            final String sound = map.getBy(String::valueOf, m -> m.getRegex("(?i)value|text|sound"), "");
            if (sound.isEmpty()) {
                return null;
            }
            final float volume = map.getBy(o -> Float.parseFloat(String.valueOf(o)), m -> m.getIgnoreCase("volume"), 1.0f);
            final float pitch = map.getBy(o -> Float.parseFloat(String.valueOf(o)), m -> m.getIgnoreCase("pitch"), 1.0f);

            return new SoundDisplay<SenderT, SoundT>(sound, volume, pitch) {
                @Override
                protected @Nullable SoundT parseSound(@NotNull String s) {
                    return Loader.this.parseSound(s);
                }

                @Override
                protected void playSound(@NotNull SenderT type, @NotNull SoundT sound1, float volume1, float pitch1) {
                    Loader.this.playSound(type, sound1, volume1, pitch1);
                }
            };
        }

        @Nullable
        protected abstract SoundT parseSound(@NotNull String s);

        protected abstract void playSound(@NotNull SenderT type, @NotNull SoundT sound, float volume, float pitch);
    }
}
