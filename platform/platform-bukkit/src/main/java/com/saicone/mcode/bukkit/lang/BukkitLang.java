package com.saicone.mcode.bukkit.lang;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.messages.ActionBar;
import com.cryptomorin.xseries.messages.Titles;
import com.google.gson.Gson;
import com.saicone.mcode.bukkit.util.ServerInstance;
import com.saicone.mcode.module.lang.AbstractLang;
import com.saicone.mcode.module.lang.Displays;
import com.saicone.mcode.module.lang.display.*;
import com.saicone.mcode.util.DMap;
import com.saicone.mcode.platform.MC;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Entity;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;

public class BukkitLang extends AbstractLang<CommandSender> {

    private static final String ITEM_HOVER = "{\"id\":\"%s\",\"count\":%d,\"components\": %s}";

    // Loadable display types
    public static final ActionBarLoader ACTIONBAR = new ActionBarLoader();
    public static final BossbarLoader BOSSBAR = new BossbarLoader();
    public static final SoundLoader SOUND = new SoundLoader();
    public static final TextLoader TEXT = new TextLoader();
    public static final TitleLoader TITLE = new TitleLoader();

    protected static final boolean CREATE_AUDIENCE;
    private static final boolean USE_XSERIES;

    static {
        boolean createAudience = true;
        try {
            final Class<?> audience = Class.forName("net.kyori.adventure.audience.Audience");
            // Check native support
            if (audience.isAssignableFrom(CommandSender.class)) {
                createAudience = false;
            }
        } catch (Throwable ignored) { }
        CREATE_AUDIENCE = createAudience;

        boolean useXSeries = false;
        try {
            Class.forName("com.cryptomorin.xseries.messages.ActionBar");
            Class.forName("com.cryptomorin.xseries.messages.Titles");
            Class.forName("com.cryptomorin.xseries.XSound");
            useXSeries = true;
        } catch (Throwable ignored) { }
        USE_XSERIES = useXSeries;

        if (CREATE_AUDIENCE) {
            Displays.register("actionbar", ACTIONBAR);
            Displays.register("bossbar", BOSSBAR);
            Displays.register("sound", SOUND);
            Displays.register("text", TEXT);
            Displays.register("title", TITLE);
        }
    }

    // Instance parameters
    private final Plugin plugin;
    private boolean useConfig;

    private transient Map<String, String> cachedAliases;

    public BukkitLang(@NotNull Plugin plugin, @NotNull Object... providers) {
        super(providers);
        this.plugin = plugin;
    }

    public boolean isUseConfig() {
        return useConfig;
    }

    @NotNull
    public Plugin getPlugin() {
        return plugin;
    }

    @NotNull
    @Contract("_ -> this")
    public BukkitLang useConfig(boolean useConfig) {
        this.useConfig = useConfig;
        return this;
    }

    @Override
    public @NotNull String getLanguage() {
        if (useConfig) {
            return plugin.getConfig().getString("Locale.Plugin", DEFAULT_LANGUAGE).toLowerCase();
        }
        return super.getLanguage();
    }

    @Override
    public @NotNull String getLanguageFor(@Nullable Object object) {
        if (object instanceof CommandSender) {
            if (object instanceof Player) {
                return ((Player) object).getLocale();
            } else {
                return super.getLanguageFor(null);
            }
        }
        return super.getLanguageFor(object);
    }

    @Override
    public @NotNull Set<String> getLanguageTypes() {
        if (useConfig) {
            final ConfigurationSection section = plugin.getConfig().getConfigurationSection("Locale.Aliases");
            if (section != null) {
                return section.getKeys(false);
            }
        }
        return super.getLanguageTypes();
    }

    @Override
    public @NotNull Map<String, String> getLanguageAliases() {
        if (useConfig) {
            if (cachedAliases == null) {
                cachedAliases = new HashMap<>();
                final ConfigurationSection section = plugin.getConfig().getConfigurationSection("Locale.Aliases");
                if (section != null) {
                    for (String key : section.getKeys(false)) {
                        final Object object = section.get(key);
                        if (object instanceof List) {
                            for (Object o : (List<?>) object) {
                                cachedAliases.put(key.toLowerCase(), String.valueOf(o).toLowerCase());
                            }
                        }
                    }
                }
            }
            return cachedAliases;
        } else {
            return super.getLanguageAliases();
        }
    }

    @Override
    public int getLogLevel() {
        if (useConfig) {
            return plugin.getConfig().getInt("Locale.LogLevel", DEFAULT_LOG_LEVEL);
        } else {
            return super.getLogLevel();
        }
    }

    @Override
    protected @NotNull CommandSender getConsole() {
        return Bukkit.getConsoleSender();
    }

    @Override
    protected @NotNull Collection<? extends CommandSender> getSenders() {
        return Bukkit.getOnlinePlayers();
    }

    @Override
    protected void log(int level, @NotNull String msg) {
        switch (level) {
            case 1:
                plugin.getLogger().severe(msg);
                break;
            case 2:
                plugin.getLogger().warning(msg);
                break;
            case 3:
                plugin.getLogger().info(msg);
                break;
            case 4:
            default:
                plugin.getLogger().log(Level.INFO, msg);
        }
    }

    @Override
    protected void log(int level, @NotNull String msg, @NotNull Throwable exception) {
        switch (level) {
            case 1:
                plugin.getLogger().log(Level.SEVERE, msg, exception);
                break;
            case 2:
                plugin.getLogger().log(Level.WARNING, msg, exception);
                break;
            case 3:
                plugin.getLogger().log(Level.INFO, msg, exception);
                break;
            case 4:
            default:
                plugin.getLogger().log(Level.INFO, msg, exception);
        }
    }

    @Override
    public void load(@NotNull File langFolder) {
        cachedAliases = null;
        super.load(langFolder);
    }

    @Override
    protected void saveFile(@NotNull File folder, @NotNull String name) {
        final File file = new File(folder, name);
        if (file.exists()) {
            return;
        }
        try (InputStream in = plugin.getResource("lang/" + name)) {
            if (in == null) {
                return;
            }
            Files.copy(in, file.toPath());
        } catch (IOException e) {
            sendLog(2, e);
        }
    }

    @Override
    public @NotNull File getLangFolder() {
        return new File(plugin.getDataFolder(), "lang");
    }

    @Override
    protected @NotNull Map<?, ?> getFileObjects(@NotNull File file) {
        final String name = file.getName().trim().toLowerCase();
        if (name.endsWith(".yaml") || name.endsWith(".yml")) {
            final YamlConfiguration config = new YamlConfiguration();
            try {
                config.load(file);
            } catch (Exception e) {
                sendLog(2, e, "Cannot load displays from yaml configuration at file " + file.getName());
            }
            return getConfigObjects(config);
        } else if (name.endsWith(".json")) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                return new Gson().fromJson(reader, Map.class);
            } catch (IOException e) {
                sendLog(2, e, "Cannot load displays from json configuration at file " + file.getName());
            }
        }
        return new HashMap<>();
    }

    @NotNull
    protected Map<String, Object> getConfigObjects(@NotNull ConfigurationSection config) {
        final Map<String, Object> objects = new HashMap<>();
        for (String key : config.getKeys(false)) {
            final Object value = config.get(key);
            if (value instanceof ConfigurationSection) {
                objects.put(key, getConfigObjects((ConfigurationSection) value));
            } else {
                objects.put(key, value);
            }
        }
        return objects;
    }

    public static class TextLoader extends TextDisplay.Loader<CommandSender> {
        @Override
        @SuppressWarnings("unchecked")
        public @Nullable TextDisplay<CommandSender> load(@NotNull DMap map) {
            if (ServerInstance.Platform.SPIGOT) {
                return super.load(map);
            }
            final Object obj = map.getRegex("(?i)value|text");
            if (obj == null) {
                return null;
            }
            if (obj instanceof List) {
                return load((List<Object>) obj);
            } else {
                return load(String.valueOf(obj));
            }
        }

        @Override
        protected void sendText(@NotNull CommandSender sender, @NotNull String text) {
            sender.sendMessage(text.split("\n"));
        }

        @Override
        protected @NotNull TextBuilder newBuilder() {
            return new TextBuilder();
        }

        @Override
        protected @NotNull TextDisplay.Event newEvent(@NotNull TextDisplay.Action action, @NotNull Object value) {
            if (value instanceof ItemStack || value instanceof org.bukkit.entity.Entity) {
                return new BukkitTextEvent(action, value);
            }
            return super.newEvent(action, value);
        }
    }

    public static class TextBuilder extends TextDisplay.Builder<CommandSender> {

        private ComponentBuilder builder = new ComponentBuilder();

        @Override
        public void append(@NotNull String s, boolean before) {
            if (before) {
                final ComponentBuilder sameBuilder = builder;
                builder = new ComponentBuilder(s);
                builder.append(sameBuilder.create());
            } else {
                builder.append(s);
            }
        }

        @Override
        public void append(@NotNull CommandSender type, @NotNull String s, @NotNull Set<TextDisplay.Event> events) {
            final ComponentBuilder component = new ComponentBuilder();
            component.append(s);
            for (TextDisplay.Event event : events) {
                if (event.getAction().isClick()) {
                    builder.event(new ClickEvent(ClickEvent.Action.values()[event.getAction().ordinal()], event.getString()));
                } else if (event.getAction().isHover()) {
                    switch (event.getAction().hover()) {
                        case SHOW_TEXT:
                            builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(event.getString())));
                            break;
                        case SHOW_ITEM:
                            if (MC.version().isComponent()) {
                                final String nbt = String.format(ITEM_HOVER, event.getItemId(), event.getItemCount(), event.getItemComponents());
                                builder.event(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[]{ new TextComponent(nbt) }));
                            } else {
                                final String tag = event.getItemTag();
                                builder.event(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new Item(
                                        event.getItemId(),
                                        event.getItemCount(),
                                        tag == null ? null : ItemTag.ofNbt(tag)
                                )));
                            }
                            break;
                        case SHOW_ENTITY:
                            builder.event(new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new Entity(
                                    event.getEntityType(),
                                    event.getEntityUniqueId().toString(),
                                    net.md_5.bungee.api.chat.TextComponent.fromLegacyText(event.getEntityName())[0]
                            )));
                            break;
                        default:
                            break;
                    }
                }
            }
            builder.append(component.create());
        }

        @Override
        public void sendTo(@NotNull CommandSender type) {
            type.spigot().sendMessage(builder.create());
        }
    }

    public static class TitleLoader extends TitleDisplay.Loader<CommandSender> {
        @Override
        @SuppressWarnings("deprecation")
        protected void sendTitle(@NotNull CommandSender sender, @NotNull String title, @NotNull String subtitle, int fadeIn, int stay, int fadeOut) {
            if (sender instanceof Player) {
                if (USE_XSERIES) {
                    Titles.sendTitle((Player) sender, fadeIn, stay, fadeOut, title, subtitle);
                } else if (MC.version().isNewerThanOrEquals(MC.V_1_9)) {
                    ((Player) sender).sendTitle(title, subtitle, fadeIn, stay, fadeOut);
                } else {
                    ((Player) sender).sendTitle(title, subtitle);
                }
            } else {
                sender.sendMessage(title);
                sender.sendMessage(subtitle);
            }
        }
    }

    public static class ActionBarLoader extends ActionBarDisplay.Loader<CommandSender> {
        @Override
        protected void sendActionbar(@NotNull CommandSender sender, @NotNull String actionbar) {
            if (sender instanceof Player) {
                if (USE_XSERIES) {
                    ActionBar.sendActionBar((Player) sender, actionbar);
                } else if (ServerInstance.Platform.SPIGOT) {
                    ((Player) sender).spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(actionbar));
                } else if (Bukkit.isPrimaryThread() && MC.version().isNewerThanOrEquals(MC.V_1_11)) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "minecraft:title " + sender.getName() + " actionbar {\"text\":\"" + ChatColor.stripColor(actionbar) + "\"}");
                } else {
                    sender.sendMessage(actionbar);
                }
            } else {
                sender.sendMessage(actionbar);
            }
        }
    }

    public static class SoundLoader extends SoundDisplay.Loader<CommandSender> {
        @Override
        protected @Nullable Sound parseSound(@NotNull String s, float volume, float pitch) {
            if (USE_XSERIES) {
                return XSound.matchXSound(s).map(XSound::parseSound).orElse(null);
            }
            try {
                return Sound.valueOf(s.replace('.', '_').toUpperCase());
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void playSound(@NotNull CommandSender sender, @NotNull Object sound, float volume, float pitch) {
            if (sender instanceof Player) {
                ((Player) sender).playSound(((Player) sender).getLocation(), (Sound) sound, volume, pitch);
            }
        }
    }

    public static class BossbarLoader extends BossBarDisplay.Loader<CommandSender> {
        public BossbarLoader() {
            super(false);
        }

        @Override
        protected BossBarDisplay.Holder newHolder(float progress, @NotNull String text, @NotNull BossBarDisplay.Color color, @NotNull BossBarDisplay.Division division, @NotNull Set<BossBarDisplay.Flag> flags) {
            final BarFlag[] values = new BarFlag[flags.size()];
            int i = 0;
            for (BossBarDisplay.Flag flag : flags) {
                values[i] = BarFlag.values()[flag.ordinal()];
            }
            final BossBar bossBar = Bukkit.createBossBar(
                    text,
                    BarColor.values()[color.ordinal()],
                    BarStyle.values()[division.ordinal()],
                    values
            );
            bossBar.setProgress(progress);
            return new BukkitBossBar(bossBar);
        }
    }
}
