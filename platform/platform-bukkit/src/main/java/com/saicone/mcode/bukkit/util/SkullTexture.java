package com.saicone.mcode.bukkit.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.saicone.mcode.platform.MC;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Simple utility class to get textured heads from:<br>
 * - Texture ID<br>
 * - Texture URL<br>
 * - Texture Base64<br>
 * - Player<br>
 * - Player name<br>
 * - Player UUID<br>
 *
 * @author Rubenicos
 */
@SuppressWarnings("deprecation")
public class SkullTexture {

    // Constants

    @Deprecated
    private static final String INVALID_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDZiYTYzMzQ0ZjQ5ZGQxYzRmNTQ4OGU5MjZiZjNkOWUyYjI5OTE2YTZjNTBkNjEwYmI0MGE1MjczZGM4YzgyIn19fQ==";
    @Deprecated
    private static final String LOADING_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzI0MzE5MTFmNDE3OGI0ZDJiNDEzYWE3ZjVjNzhhZTQ0NDdmZTkyNDY5NDNjMzFkZjMxMTYzYzBlMDQzZTBkNiJ9fX0=";

    private static final String TEXTURE_URL = "http://textures.minecraft.net/texture/";

    static {
        // Add reflected classes
        try {
            BukkitLookup.addBukkitClass("entity.CraftPlayer");
            BukkitLookup.addBukkitClass("inventory.CraftMetaSkull");
            if (MC.version().isComponent()) {
                BukkitLookup.addMinecraftClass("world.item.component.ResolvableProfile");
            }
            BukkitLookup.addClassId("GameProfile", GameProfile.class);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Reflected methods

    private static final MethodHandle NEW_PROFILE = BukkitLookup.find("ResolvableProfile", () -> {
        if (MC.version().isComponent()) {
            for (Method method : BukkitLookup.classOf("CraftMetaSkull").getDeclaredMethods()) {
                if (method.getName().equals("setProfile") && method.getParameters().length == 1) {
                    if (method.getParameters()[0].getType().getSimpleName().equals("ResolvableProfile")) {
                        return "(GameProfile profile)";
                    }
                }
            }
        }
        return null;
    });
    private static final MethodHandle GET_PROFILE = BukkitLookup.find("CraftPlayer", "public GameProfile getProfile()");
    private static final MethodHandle SET_PROFILE = BukkitLookup.find("CraftMetaSkull", () -> {
        if (NEW_PROFILE != null) {
            return "private void setProfile(ResolvableProfile profile)";
        } else if (MC.version().isNewerThanOrEquals(MC.V_1_15)) {
            return "private void setProfile(GameProfile profile)";
        } else {
            return "set GameProfile profile";
        }
    });
    private static final MethodHandle GET_VALUE = BukkitLookup.find(Property.class, () -> {
        for (Method method : Property.class.getDeclaredMethods()) {
            if (method.getName().equals("getValue")) {
                // Old name found
                return "public String getValue()";
            }
        }
        return "public String value()";
    });

    // Providers

    private static final JsonParser JSON_PARSER = new JsonParser();
    private static final Supplier<ItemStack> PLAYER_HEAD = () -> {
        if (MC.version().isFlat()) {
            return new ItemStack(Material.PLAYER_HEAD);
        } else {
            return new ItemStack(Material.getMaterial("SKULL_ITEM"), 1, (short) 3);
        }
    };

    // Instances

    /**
     * Get a SkullTexture instance that retrieves texture values from Mojang API
     * by providing a player name or unique id.
     *
     * @return a SkullTexture instance that use Mojang API.
     */
    public static SkullTexture mojang() {
        return Mojang.INSTANCE;
    }

    /**
     * Get a SkullTexture instance that retrieves texture values from PlayerDB API
     * by providing a player name or unique id.
     *
     * @return a SkullTexture instance that use PlayerDB API.
     */
    public static SkullTexture playerDB() {
        return PlayerDB.INSTANCE;
    }

    /**
     * Get a SkullTexture instance that retrieves texture values from CraftHead API
     * by providing a player name or unique id.
     *
     * @return a SkullTexture instance that use PlayerDB API.
     */
    public static SkullTexture craftHead() {
        return CraftHead.INSTANCE;
    }

    // The class itself

    /**
     * Cache object to save encoded textures.
     */
    protected final Cache<String, String> cache;
    /**
     * Default executor to use on async operations.
     */
    protected final Executor executor;

    /**
     * Constructs a SkullTexture instance with default parameters.
     */
    public SkullTexture() {
        this(CacheBuilder.newBuilder().expireAfterAccess(3, TimeUnit.HOURS).build());
    }

    /**
     * Constructs a SkullTexture instance with provided cache object.
     *
     * @param cache the cache to save encoded textures.
     */
    public SkullTexture(@NotNull Cache<String, String> cache) {
        this(cache, CompletableFuture.completedFuture(null).defaultExecutor());
    }

    /**
     * Constructs a SkullTexture instance with provided executor.
     *
     * @param executor the default executor to use in async operations.
     */
    public SkullTexture(@NotNull Executor executor) {
        this(CacheBuilder.newBuilder().expireAfterAccess(3, TimeUnit.HOURS).build(), executor);
    }

    /**
     * Constructs a SkullTexture instance with provided cache object and executor.
     *
     * @param cache    the cache to save encoded textures.
     * @param executor the default executor to use in async operations.
     */
    public SkullTexture(@NotNull Cache<String, String> cache, @NotNull Executor executor) {
        this.cache = cache;
        this.executor = executor;
    }

    /**
     * Return encoded texture value from cache or save it by supplier.<br>
     * This method also "caches" null values using empty strings.
     *
     * @param key      the key to get texture from cache.
     * @param supplier the supplier that provide a non-cached value.
     * @return         a cached encoded texture value, null if supplier return a null object.
     */
    @Nullable
    protected String caching(@NotNull Object key, @NotNull Supplier<String> supplier) {
        String value = cache.getIfPresent(String.valueOf(key));
        if (value == null) {
            value = supplier.get();
            if (value == null) {
                value = "";
            }
            cache.put(String.valueOf(key), value);
        }
        if (value.isBlank()) {
            return null;
        }
        return value;
    }

    /**
     * Get a player head with provided texture value.<br>
     * This method may fetch textures using external APIs,
     * use it in non-async operations if you know what are you doing.
     *
     * @param object   the object to convert into encoded texture, or the encoded texture itself.
     * @return         a player head with provided texture value if found, normal head otherwise.
     */
    @NotNull
    public ItemStack item(@NotNull Object object) {
        return setTexture(PLAYER_HEAD.get(), textureFrom(object));
    }

    /**
     * Get a player head asynchronously with provided texture value.<br>
     * If encoded texture is already cached, a completed future will be return.
     *
     * @param object   the object to convert into encoded texture, or the encoded texture itself.
     * @return         a completable future containing player head with provided texture value if found, normal head otherwise.
     */
    @NotNull
    public CompletableFuture<ItemStack> itemAsync(@NotNull Object object) {
        return itemAsync(object, this.executor);
    }

    /**
     * Get a player head asynchronously with provided texture value and executor.<br>
     * If encoded texture is already cached, a completed future will be return.
     *
     * @param object   the object to convert into encoded texture, or the encoded texture itself.
     * @param executor the executor to use for asynchronous execution
     * @return         a completable future containing player head with provided texture value if found, normal head otherwise.
     */
    @NotNull
    public CompletableFuture<ItemStack> itemAsync(@NotNull Object object, @NotNull Executor executor) {
        final Object key;
        if (object instanceof Player) {
            key = ((Player) object).getUniqueId();
        } else {
            key = object;
        }
        final String texture = this.cache.getIfPresent(String.valueOf(key));
        if (texture != null) {
            if (texture.isBlank()) {
                return CompletableFuture.completedFuture(PLAYER_HEAD.get());
            }
            return CompletableFuture.completedFuture(setTexture(PLAYER_HEAD.get(), texture));
        }
        return CompletableFuture.supplyAsync(() -> item(object), executor);
    }

    /**
     * Get encoded texture value from any supported object.<br>
     * This method automatically caches the result to reduce request times.
     *
     * @param object the object to convert into encoded texture, or the encoded texture itself.
     * @return       an encoded texture value, null if any error occurs.
     */
    @Nullable
    public String textureFrom(@NotNull Object object) {
        if (object instanceof Player) {
            return caching(((Player) object).getUniqueId(), () -> textureFromPlayer((Player) object));
        } else if (object instanceof UUID) {
            return caching(object, () -> textureFromId((UUID) object));
        } else {
            final String value = String.valueOf(object);
            return caching(value, () -> {
                if (value.length() <= 20) {
                    return textureFromName(value);
                } else if (value.length() == 32 || value.length() == 36) {
                    return textureFromId(value);
                } else if (value.length() == 64) {
                    return textureFromUrlId(value);
                } else if (value.startsWith("http")) {
                    return textureFromUrl(value);
                } else {
                    return value;
                }
            });
        }
    }

    /**
     * Get encoded texture value from online player.
     *
     * @param player the online player.
     * @return       an encoded texture value, null if player texture cannot be found.
     */
    @Nullable
    public String textureFromPlayer(@Nullable Player player) {
        return getTexture(player);
    }

    /**
     * Get encoded texture value from player name.
     *
     * @param name the player name.
     * @return     an encoded texture value, null if player texture cannot be found.
     */
    @Nullable
    public String textureFromName(@NotNull String name) {
        final String texture = textureFromPlayer(Bukkit.getPlayer(name));
        return texture != null ? texture : fetchTexture(name);
    }

    /**
     * Get encoded texture value from player unique id.<br>
     * This method accept raw ids without dashes.
     *
     * @param uniqueId the player unique id.
     * @return         an encoded texture value, null if player texture cannot be found.
     */
    @Nullable
    public String textureFromId(@NotNull String uniqueId) {
        if (uniqueId.length() == 32) {
            return textureFromId(UUID.fromString(new StringBuilder(uniqueId)
                    .insert(20, '-').insert(16, '-').insert(12, '-').insert(8, '-')
                    .toString()));
        } else {
            return textureFromId(UUID.fromString(uniqueId));
        }
    }

    /**
     * Get encoded texture value from player unique id.
     *
     * @param uniqueId the player unique id.
     * @return         an encoded texture value, null if player texture cannot be found.
     */
    @Nullable
    public String textureFromId(@NotNull UUID uniqueId) {
        final String texture = textureFromPlayer(Bukkit.getPlayer(uniqueId));
        return texture != null ? texture : fetchTexture(uniqueId);
    }

    /**
     * Get encoded texture value from texture url.
     *
     * @param url the texture url.
     * @return    an encoded texture value, null if any error occurs.
     */
    @Nullable
    public String textureFromUrl(@NotNull String url) {
        return encodeUrl(url);
    }

    /**
     * Get encoded texture value from minecraft texture ID.
     *
     * @param id the minecraft texture ID.
     * @return   an encoded texture value, null if any error occurs.
     */
    @Nullable
    public String textureFromUrlId(@NotNull String id) {
        return textureFromUrl(TEXTURE_URL + id);
    }

    /**
     * Search into provided json object that represent a minecraft session and
     * extract minecraft url from texture value defined on session.
     *
     * @param session the json session object to search into.
     * @return        a minecraft texture url if found, null otherwise.
     */
    @Nullable
    protected String decodeUrl(@NotNull JsonObject session) {
        if (session.has("properties")) {
            for (JsonElement element : session.getAsJsonArray("properties")) {
                final JsonObject property = element.getAsJsonObject();
                if (property.get("name").getAsString().equals("textures")) {
                    final String value = property.get("value").getAsString();
                    return decodeUrl(value);
                }
            }
        }
        return null;
    }

    /**
     * Decode provided texture value and extract minecraft url from it.
     *
     * @param base64 the base64 encoded value to decode.
     * @return       a minecraft texture url if found, null otherwise.
     */
    @Nullable
    protected String decodeUrl(@NotNull String base64) {
        String value;
        try {
            value = new String(Base64.getDecoder().decode(base64));
        } catch (IllegalArgumentException e) {
            // Already decoded
            value = base64;
        }
        final JsonObject texture = JSON_PARSER.parse(value).getAsJsonObject();
        if (texture != null) {
            return texture.get("textures").getAsJsonObject().get("SKIN").getAsJsonObject().get("url").getAsString();
        }
        return null;
    }

    /**
     * Encode provided texture url into minecraft profile property format.
     *
     * @param url the url value to encode.
     * @return    an encoded texture value.
     */
    @NotNull
    protected String encodeUrl(@NotNull String url) {
        return new String(Base64.getEncoder().encode(("{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}").getBytes()));
    }

    /**
     * Get url data as json object.
     *
     * @param url the url to connect.
     * @return    a json object that represent the url data.
     */
    @NotNull
    protected JsonObject fetchJson(@NotNull String url) {
        // Only compatible with Java +9
        // Older Java versions require a more complex implementation using URL connection and input stream reader
        try (InputStream stream = new URL(url).openStream()) {
            final String json = new String(stream.readAllBytes());
            if (json.isBlank()) {
                return new JsonObject();
            }
            return JSON_PARSER.parse(json).getAsJsonObject();
        } catch (Exception e) {
            return new JsonObject();
        }
    }

    /**
     * Fetch player texture using player name.
     *
     * @param name the name to find.
     * @return     an encoded texture value if found, null otherwise.
     */
    @Nullable
    protected String fetchTexture(@NotNull String name) {
        throw new IllegalStateException("Current SkullTexture instance doesn't provide texture fetching");
    }

    /**
     * Fetch player texture using player unique id.
     *
     * @param uniqueId the unique id to find.
     * @return         an encoded texture value if found, null otherwise.
     */
    @Nullable
    protected String fetchTexture(@NotNull UUID uniqueId) {
        throw new IllegalStateException("Current SkullTexture instance doesn't provide texture fetching");
    }

    // Static methods

    /**
     * Set encoded texture value into skull meta.
     *
     * @param head    skull item to set the texture.
     * @param texture encoded texture value.
     * @return        the provided item.
     * @throws IllegalArgumentException if the provided item isn't a player head.
     */
    @NotNull
    @Contract("_, _ -> param1")
    public static ItemStack setTexture(@NotNull ItemStack head, @Nullable String texture) throws IllegalArgumentException {
        if (texture == null) {
            return head;
        }
        // Since 1.20.2: The Mojang AuthLib require non-null name for game profile, so "null" will be used instead
        final GameProfile profile = new GameProfile(UUID.randomUUID(), "null");
        profile.getProperties().put("textures", new Property("textures", texture));
        return setProfile(head, profile);
    }

    /**
     * Set game profile value into skull meta.
     *
     * @param head    skull item to set the profile.
     * @param profile game profile value.
     * @return        the provided item.
     * @throws IllegalArgumentException if the provided item isn't a player head.
     */
    @NotNull
    @Contract("_, _ -> param1")
    public static ItemStack setProfile(@NotNull ItemStack head, @NotNull GameProfile profile) throws IllegalArgumentException {
        final ItemMeta meta = head.getItemMeta();
        if (!(meta instanceof SkullMeta)) {
            throw new IllegalArgumentException("The provided item isn't a player head");
        }
        try {
            if (NEW_PROFILE != null) {
                SET_PROFILE.invoke(meta, NEW_PROFILE.invoke(profile));
            } else {
                SET_PROFILE.invoke(meta, profile);
            }
        } catch (Throwable t) {
            throw new RuntimeException("Cannot set profile value to ItemStack", t);
        }
        head.setItemMeta(meta);
        return head;
    }

    /**
     * Get encoded texture value from online player profile.
     *
     * @param player the player to get the texture from.
     * @return       an encoded texture value if was found, null otherwise.
     */
    @Nullable
    @Contract("null -> null")
    public static String getTexture(@Nullable Player player) {
        final GameProfile profile = getProfile(player);
        return profile == null ? null : getTexture(profile);
    }

    /**
     * Get encoded texture value from game profile.
     *
     * @param profile the profile to extract texture.
     * @return        an encoded texture value if was found, null otherwise.
     */
    @Nullable
    public static String getTexture(@NotNull GameProfile profile) {
        for (Property texture : profile.getProperties().get("textures")) {
            if (texture != null) {
                try {
                    return (String) GET_VALUE.invoke(texture);
                } catch (Throwable t) {
                    throw new RuntimeException("Cannot get texture value from Property object");
                }
            }
        }
        return null;
    }

    /**
     * Get game profile value from online player.
     *
     * @param player the player to get the profile from.
     * @return       a game profile value.
     */
    @Nullable
    @Contract("null -> null")
    public static GameProfile getProfile(@Nullable Player player) {
        if (player == null) {
            return null;
        }
        try {
            return ((GameProfile) GET_PROFILE.invoke(player));
        } catch (Throwable t) {
            throw new RuntimeException("Cannot get online player texture from '" + player.getName() + "'", t);
        }
    }

    // Deprecated/old methods

    /**
     * Main method to get textured head and save into cache.
     *
     * @deprecated use {@link SkullTexture#mojang()}{@code .}{@link SkullTexture#item(Object)} instead.
     *
     * @param texture texture ID, URL, Base64, Player name or UUID.
     * @return        a ItemStack that represent the textured head.
     */
    @Deprecated
    public static ItemStack getTexturedHead(String texture) {
        return setTexture(PLAYER_HEAD.get(), getTextureValue(texture));
    }

    /**
     * Main method to get textured head and save into cache.
     *
     * @deprecated use {@link SkullTexture#mojang()}{@code .}{@link SkullTexture#itemAsync(Object)} instead.
     *
     * @param texture  texture ID, URL, Base64, Player name or UUID.
     * @param callback function to execute if textured head is retrieved in async operation.
     * @return         a ItemStack that represent the textured head.
     */
    @Deprecated
    public static ItemStack getTexturedHead(String texture, Consumer<ItemStack> callback) {
        if (callback == null) {
            return getTexturedHead(texture);
        }
        return setTexture(PLAYER_HEAD.get(), getTextureValue(texture, value -> callback.accept(setTexture(PLAYER_HEAD.get(), value))));
    }

    /**
     * Get Base64 encoded texture from the given texture parameter,
     * can be player name, player uuid, texture id, url or base64.
     *
     * @deprecated use {@link SkullTexture#mojang()}{@code .}{@link SkullTexture#textureFrom(Object)} instead.
     *
     * @param texture texture type.
     * @return        a Base64 encoded text.
     */
    @Deprecated
    public static String getTextureValue(String texture) {
        return getTextureValue(texture, null);
    }

    /**
     * Get Base64 encoded texture from the given texture parameter,
     * can be player name, player uuid, texture id, url or base64.
     *
     * @deprecated use {@link SkullTexture#mojang()}{@code .}{@link SkullTexture#textureFrom(Object)} instead.
     *
     * @param texture  texture type.
     * @param callback function to execute if texture value is retrieved in async operation.
     * @return         a Base64 encoded text.
     */
    @Deprecated
    public static String getTextureValue(String texture, Consumer<String> callback) {
        if (texture.length() <= 20 || texture.length() == 36) {
            final String value = mojang().cache.getIfPresent(texture);
            if (value == null) {
                mojang().cache.put(texture, LOADING_TEXTURE);
                CompletableFuture.supplyAsync(() -> {
                    if (texture.length() == 36) {
                        return mojang().textureFromId(texture);
                    } else {
                        return mojang().textureFromName(texture);
                    }
                }).thenAccept(result -> {
                    if (result != null) {
                        mojang().cache.put(texture, result);
                        if (callback != null) {
                            callback.accept(result);
                        }
                    }
                });
            }
            return LOADING_TEXTURE;
        }
        return mojang().textureFrom(texture);
    }

    /**
     * Compute textured head via making a request to Mojang API,
     * it's suggested to call this method in async environment.
     *
     * @deprecated use {@link SkullTexture#mojang()}{@code .}{@link SkullTexture#fetchTexture(String)} instead.
     *
     * @param name the player name.
     * @return     a Base64 encoded text.
     */
    @Deprecated
    public static String computePlayerTexture(@NotNull String name) {
        return computePlayerTexture(name, name);
    }

    /**
     * Compute textured head via making a request to Mojang API,
     * it's suggested to call this method in async environment.
     *
     * @deprecated use {@link SkullTexture#mojang()}{@code .}{@link SkullTexture#fetchTexture(String)} instead.
     *
     * @param key  map key to put.
     * @param name the player name.
     * @return     a Base64 encoded text.
     */
    @Deprecated
    public static String computePlayerTexture(@NotNull String key, @NotNull String name) {
        String texture = requestTextureUrl(name);
        if (texture != null) {
            texture = mojang().encodeUrl(texture);
            mojang().cache.put(key, texture);
            return texture;
        } else {
            mojang().cache.put(key, INVALID_TEXTURE);
            return INVALID_TEXTURE;
        }
    }

    /**
     * Request player texture url using Mojang API.
     *
     * @deprecated use {@link SkullTexture#mojang()}{@code .}{@link SkullTexture#fetchTexture(String)} instead.
     *
     * @param name the player name.
     * @return     a Mojang texture url if the player profile exists, null otherwise.
     */
    @Deprecated
    public static String requestTextureUrl(@NotNull String name) {
        return mojang().fetchTexture(name);
    }

    /**
     * Mojang SkullTexture implementation that retrieves textures using Mojang API.
     */
    @ApiStatus.Experimental
    public static class Mojang extends SkullTexture {

        private static final Mojang INSTANCE = new Mojang();

        private static final String USER_API = "https://api.mojang.com/users/profiles/minecraft/";
        private static final String SESSION_API = "https://sessionserver.mojang.com/session/minecraft/profile/";

        /**
         * Constructs a SkullTexture instance with default parameters.
         */
        public Mojang() {
            super();
        }

        /**
         * Constructs a SkullTexture instance with provided cache object.
         *
         * @param cache the cache to save encoded textures.
         */
        public Mojang(@NotNull Cache<String, String> cache) {
            super(cache);
        }

        /**
         * Constructs a SkullTexture instance with provided executor.
         *
         * @param executor the default executor to use in async operations.
         */
        public Mojang(@NotNull Executor executor) {
            super(executor);
        }

        /**
         * Constructs a SkullTexture instance with provided cache object and executor.
         *
         * @param cache    the cache to save encoded textures.
         * @param executor the default executor to use in async operations.
         */
        public Mojang(@NotNull Cache<String, String> cache, @NotNull Executor executor) {
            super(cache, executor);
        }

        @Override
        protected @Nullable String fetchTexture(@NotNull String name) {
            final JsonObject user = fetchJson(USER_API + name);
            if (user.has("id")) {
                return decodeUrl(fetchJson(SESSION_API + user.get("id").getAsString()));
            }
            return null;
        }

        @Override
        protected @Nullable String fetchTexture(@NotNull UUID uniqueId) {
            return decodeUrl(fetchJson(SESSION_API + uniqueId.toString().replace('-', '\0')));
        }
    }

    /**
     * PlayerDB SkullTexture implementation that retrieves textures using PlayerDB API.
     */
    @ApiStatus.Experimental
    public static class PlayerDB extends SkullTexture {

        private static final PlayerDB INSTANCE = new PlayerDB();

        private static final String API = "https://playerdb.co/api/player/minecraft/";

        /**
         * Constructs a SkullTexture instance with default parameters.
         */
        public PlayerDB() {
            super();
        }

        /**
         * Constructs a SkullTexture instance with provided cache object.
         *
         * @param cache the cache to save encoded textures.
         */
        public PlayerDB(@NotNull Cache<String, String> cache) {
            super(cache);
        }

        /**
         * Constructs a SkullTexture instance with provided executor.
         *
         * @param executor the default executor to use in async operations.
         */
        public PlayerDB(@NotNull Executor executor) {
            super(executor);
        }

        /**
         * Constructs a SkullTexture instance with provided cache object and executor.
         *
         * @param cache    the cache to save encoded textures.
         * @param executor the default executor to use in async operations.
         */
        public PlayerDB(@NotNull Cache<String, String> cache, @NotNull Executor executor) {
            super(cache, executor);
        }

        @Override
        protected @Nullable String fetchTexture(@NotNull String name) {
            return fetchAny(name);
        }

        @Override
        protected @Nullable String fetchTexture(@NotNull UUID uniqueId) {
            return fetchAny(uniqueId.toString());
        }

        /**
         * Fetch player texture using player name or id.
         *
         * @param any the name or id to find.
         * @return    an encoded texture value if found, null otherwise.
         */
        @Nullable
        protected String fetchAny(@NotNull String any) {
            final JsonObject profile = fetchJson(API + any);
            if (profile.has("data")) {
                final JsonObject data = profile.getAsJsonObject("data");
                if (data.has("player")) {
                    return decodeUrl(data.getAsJsonObject("player"));
                }
            }
            return null;
        }
    }

    /**
     * CraftHead SkullTexture implementation that retrieves textures using CraftHead API.
     */
    @ApiStatus.Experimental
    public static class CraftHead extends SkullTexture {

        private static final CraftHead INSTANCE = new CraftHead();

        private static final String API = "https://crafthead.net/profile/";

        /**
         * Constructs a SkullTexture instance with default parameters.
         */
        public CraftHead() {
            super();
        }

        /**
         * Constructs a SkullTexture instance with provided cache object.
         *
         * @param cache the cache to save encoded textures.
         */
        public CraftHead(@NotNull Cache<String, String> cache) {
            super(cache);
        }

        /**
         * Constructs a SkullTexture instance with provided executor.
         *
         * @param executor the default executor to use in async operations.
         */
        public CraftHead(@NotNull Executor executor) {
            super(executor);
        }

        /**
         * Constructs a SkullTexture instance with provided cache object and executor.
         *
         * @param cache    the cache to save encoded textures.
         * @param executor the default executor to use in async operations.
         */
        public CraftHead(@NotNull Cache<String, String> cache, @NotNull Executor executor) {
            super(cache, executor);
        }

        @Override
        protected @Nullable String fetchTexture(@NotNull String name) {
            return fetchAny(name);
        }

        @Override
        protected @Nullable String fetchTexture(@NotNull UUID uniqueId) {
            return fetchAny(uniqueId.toString());
        }

        /**
         * Fetch player texture using player name or id.
         *
         * @param any the name or id to find.
         * @return    an encoded texture value if found, null otherwise.
         */
        @Nullable
        protected String fetchAny(@NotNull String any) {
            return decodeUrl(fetchJson(API + any));
        }
    }
}