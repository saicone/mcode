package com.saicone.mcode.bukkit;

import com.google.common.base.Suppliers;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonElement;
import com.saicone.mcode.platform.MC;
import com.saicone.mcode.platform.Text;
import com.saicone.mcode.util.text.Replacer;
import com.saicone.mcode.util.text.TextComponent;
import com.saicone.nbt.io.TagReader;
import com.saicone.nbt.io.TagWriter;
import com.saicone.nbt.mapper.BukkitTagMapper;
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
    public static Text valueOf(byte type, @Nullable MC version, @NotNull Object value) {
        switch (type) {
            case Text.EMPTY:
                return Text.empty();
            case Text.PLAIN_TEXT:
                return new PlainText(version != null ? version : MC.version(), String.valueOf(value));
            case Text.COLORED:
                if (version == null) {
                    return new Colored((String) value);
                }
                return new Colored(version, (String) value);
            case Text.RAW_JSON:
                if (version == null) {
                    return new Json((JsonElement) value);
                }
                return new Json(version, (JsonElement) value);
            case Text.NBT:
                if (value instanceof String) {
                    value = TagReader.fromString((String) value, BukkitTagMapper.INSTANCE);
                }
                if (version == null) {
                    return new Nbt(value);
                }
                return new Nbt(version, value);
            default:
                throw new IllegalArgumentException("Invalid text type: " + type);
        }
    }

    public static class PlainText extends Text.PlainText {
        public PlainText(@NotNull MC version, @NotNull String value) {
            super(version, value);
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
            return Text.valueOf(Text.NBT, getVersion(), TagReader.fromString(getValue(), BukkitTagMapper.INSTANCE)).getAsNbt();
        }
    }

    public static class Colored extends Text.Colored {
        public Colored(@NotNull String value) {
            super(value);
        }

        public Colored(@NotNull MC version, @NotNull String value) {
            super(version, value);
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

    public static class Json extends Text.Json {
        public Json(@NotNull JsonElement value) {
            super(value);
        }

        public Json(@NotNull MC version, @NotNull JsonElement value) {
            super(version, value);
        }

        @Override
        public @NotNull Nbt<?> getAsNbt() {
            return Text.valueOf(Text.NBT, getVersion(), TagJson.fromJson(getValue(), BukkitTagMapper.INSTANCE)).getAsNbt();
        }
    }

    public static class Nbt extends Text.Nbt<Object> {
        public Nbt(@NotNull Object value) {
            this(TextComponent.readVersion(value, BukkitTagMapper.INSTANCE), value);
        }

        public Nbt(@NotNull MC version, @NotNull Object value) {
            super(version, value);
        }

        @Override
        public @NotNull StringText getAsString() {
            return Text.valueOf(Text.PLAIN_TEXT, getVersion(), TagWriter.toString(getValue(), BukkitTagMapper.INSTANCE)).getAsString();
        }

        @Override
        public @NotNull PlainText getAsPlainText() {
            return Text.valueOf(Text.PLAIN_TEXT, getVersion(), TagWriter.toString(getValue(), BukkitTagMapper.INSTANCE)).getAsPlainText();
        }

        @Override
        public @NotNull Text.Json getAsJson() {
            return Text.valueOf(Text.RAW_JSON, getVersion(), TagJson.toJson(getValue(), BukkitTagMapper.INSTANCE)).getAsJson();
        }
    }
}
