package com.saicone.mcode.bukkit;

import com.google.common.base.Suppliers;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonElement;
import com.saicone.mcode.bukkit.nbt.BukkitTagMapper;
import com.saicone.mcode.platform.Text;
import com.saicone.mcode.util.text.Replacer;
import com.saicone.nbt.io.TagReader;
import com.saicone.nbt.io.TagWriter;
import com.saicone.nbt.util.TagJson;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public class BukkitText {

    private static final Supplier<Boolean> USE_PLACEHOLDERAPI = Suppliers.memoize(() -> Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null);
    private static final Cache<String, Object> CACHED_PLACEHOLDERS = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.MINUTES).build();
    public static final Function<String, Replacer> PLACEHOLDER_LOOKUP = identifier -> {
        if (!USE_PLACEHOLDERAPI.get()) {
            return null;
        }
        Object cached = CACHED_PLACEHOLDERS.getIfPresent(identifier);
        if (cached == null) {
            final PlaceholderExpansion expansion = PlaceholderAPIPlugin.getInstance().getLocalExpansionManager().getExpansion(identifier);
            if (expansion == null) {
                cached = false;
            } else {
                cached = new Replacer() {
                    @Override
                    public @Nullable Object replace(@Nullable Object subject, @NotNull String params) {
                        if (subject instanceof OfflinePlayer) {
                            return expansion.onRequest((OfflinePlayer) subject, params);
                        } else {
                            return expansion.onRequest(null, params);
                        }
                    }

                    @Override
                    public @Nullable Object replace(@Nullable Object subject, @NotNull Object relative, @NotNull String params) {
                        if (expansion instanceof Relational) {
                            return ((Relational) expansion).onPlaceholderRequest((Player) subject, (Player) relative, params);
                        } else {
                            return Replacer.super.replace(subject, relative, params);
                        }
                    }
                };
            }
        }
        if (cached instanceof Boolean) {
            return null;
        } else {
            return (Replacer) cached;
        }
    };

    @NotNull
    public static Text valueOf(byte type, @NotNull Object value) {
        switch (type) {
            case Text.EMPTY:
                return Text.empty();
            case Text.PLAIN_TEXT:
                return new PlainText(String.valueOf(value));
            case Text.COLORED:
                return new Colored((String) value);
            case Text.RAW_JSON:
                return new RawJson((JsonElement) value);
            case Text.NBT:
                if (value instanceof String) {
                    return new Nbt(TagReader.fromString((String) value, BukkitTagMapper.INSTANCE));
                }
                return new Nbt(value);
            default:
                throw new IllegalArgumentException("Invalid text type: " + type);
        }
    }

    public static class PlainText extends Text.PlainText {
        public PlainText(@NotNull String value) {
            super(value);
        }

        @Override
        public @NotNull Text placeholders(@Nullable Object subject, char start, char end) {
            return placeholders(subject, start, end, PLACEHOLDER_LOOKUP);
        }

        @Override
        public @NotNull Text placeholders(@Nullable Object subject, @Nullable Object relative, char start, char end) {
            return placeholders(subject, relative, start, end, PLACEHOLDER_LOOKUP);
        }

        @Override
        public @NotNull Nbt<?> getAsNbt() {
            return Text.valueOf(Text.NBT, TagReader.fromString(getValue(), BukkitTagMapper.INSTANCE)).getAsNbt();
        }
    }

    public static class Colored extends Text.Colored {
        public Colored(@NotNull String value) {
            super(value);
        }

        @Override
        public @NotNull Text placeholders(@Nullable Object subject, char start, char end) {
            return placeholders(subject, start, end, PLACEHOLDER_LOOKUP);
        }

        @Override
        public @NotNull Text placeholders(@Nullable Object subject, @Nullable Object relative, char start, char end) {
            return placeholders(subject, relative, start, end, PLACEHOLDER_LOOKUP);
        }
    }

    public static class RawJson extends Text.RawJson {
        public RawJson(@NotNull JsonElement value) {
            super(value);
        }

        @Override
        public @NotNull Nbt<?> getAsNbt() {
            return Text.valueOf(Text.NBT, TagJson.fromJson(getValue(), BukkitTagMapper.INSTANCE)).getAsNbt();
        }
    }

    public static class Nbt extends Text.Nbt<Object> {
        public Nbt(@NotNull Object value) {
            super(value);
        }

        @Override
        public @NotNull StringText getAsString() {
            return Text.valueOf(Text.PLAIN_TEXT, TagWriter.toString(getValue(), BukkitTagMapper.INSTANCE)).getAsString();
        }

        @Override
        public @NotNull PlainText getAsPlainText() {
            return Text.valueOf(Text.PLAIN_TEXT, TagWriter.toString(getValue(), BukkitTagMapper.INSTANCE)).getAsPlainText();
        }

        @Override
        public @NotNull RawJson getAsRawJson() {
            return Text.valueOf(Text.RAW_JSON, TagJson.toJson(getValue(), BukkitTagMapper.INSTANCE)).getAsRawJson();
        }
    }
}
