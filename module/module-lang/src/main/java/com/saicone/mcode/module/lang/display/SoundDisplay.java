package com.saicone.mcode.module.lang.display;

import com.saicone.mcode.module.lang.DisplayLoader;
import com.saicone.mcode.module.lang.LangLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class SoundDisplay<SenderT, SoundT> extends Display<SenderT> {

    private final String sound;
    private final float volume;
    private final float pitch;

    public SoundDisplay(@NotNull String sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
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
    public void sendTo(@NotNull LangLoader<SenderT, ? extends SenderT> loader, @NotNull SenderT type, @Nullable Object... args) {
        final SoundT parsedSound = parseSound(loader.parse(type, args(sound, args)));
        if (parsedSound != null) {
            playSound(type, parsedSound, volume, pitch);
        }
    }

    @Override
    public void sendTo(@NotNull LangLoader<SenderT, ? extends SenderT> loader, @NotNull SenderT agent, @NotNull SenderT type, @Nullable Object... args) {
        final SoundT parsedSound = parseSound(loader.parse(agent, type, args(sound, args)));
        if (parsedSound != null) {
            playSound(type, parsedSound, volume, pitch);
        }
    }

    @Override
    public void sendToAll(@NotNull LangLoader<SenderT, ? extends SenderT> loader, @Nullable Object... args) {
        final SoundT parsedSound = parseSound(args(sound, args));
        if (parsedSound != null) {
            for (SenderT player : loader.getPlayers()) {
                playSound(player, parsedSound, volume, pitch);
            }
        }
    }

    @Override
    public void sendToAll(@NotNull LangLoader<SenderT, ? extends SenderT> loader, @NotNull SenderT agent, @Nullable Object... args) {
        final SoundT parsedSound = parseSound(loader.parseAgent(agent, args(sound, args)));
        if (parsedSound != null) {
            for (SenderT player : loader.getPlayers()) {
                playSound(player, parsedSound, volume, pitch);
            }
        }
    }

    @Nullable
    protected abstract SoundT parseSound(@NotNull String s);

    protected abstract void playSound(@NotNull SenderT type, @NotNull SoundT sound, float volume, float pitch);

    public static abstract class Loader<SenderT, SoundT> extends DisplayLoader<SenderT> {

        public Loader() {
            super("(?i)(play-?)?sound?", Map.of("sound", "", "volume", (float) 1.0, "pitch", (float) 1.0));
        }

        @Override
        public @Nullable Display<SenderT> load(@NotNull Map<String, Object> map) {
            final String sound = getString(map, "sound", "");
            if (sound.isEmpty()) {
                return null;
            }
            final float volume = getFloat(map, "volume", (float) 1.0);
            final float pitch = getFloat(map, "pitch", (float) 1.0);

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
