package com.saicone.mcode.bukkit.lang;

import com.saicone.mcode.bukkit.util.ServerInstance;
import com.saicone.mcode.module.lang.AbstractLang;
import com.saicone.mcode.module.lang.Displays;
import com.saicone.mcode.module.lang.display.*;
import com.saicone.mcode.util.DMap;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.hover.content.Entity;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
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
import java.util.*;
import java.util.logging.Level;

public class BukkitLang extends AbstractLang<CommandSender> {

    // Loadable display types
    public static final ActionBarLoader ACTIONBAR = new ActionBarLoader();
    public static final BossbarLoader BOSSBAR = new BossbarLoader();
    public static final SoundLoader SOUND = new SoundLoader();
    public static final TextLoader TEXT = new TextLoader();
    public static final TitleLoader TITLE = new TitleLoader();

    private static final boolean USE_ADVENTURE;
    protected static final boolean CREATE_AUDIENCE;

    static {
        boolean useAdventure = false;
        boolean createAudience = true;
        try {
            final Class<?> audience = Class.forName("net.kyori.adventure.audience.Audience");
            useAdventure = true;
            // Check native support
            if (audience.isAssignableFrom(CommandSender.class)) {
                createAudience = false;
            }
        } catch (Throwable ignored) { }
        USE_ADVENTURE = useAdventure;
        CREATE_AUDIENCE = createAudience;

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

    @NotNull
    public static BukkitLang of(@NotNull Plugin plugin, @NotNull Object... providers) {
        if (USE_ADVENTURE) {
            if (CREATE_AUDIENCE) {
                return new BukkitAdventureLang(plugin, providers);
            }
            return new PaperLang(plugin, providers);
        }
        return new BukkitLang(plugin, providers);
    }

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
        if (object instanceof Player) {
            return ((Player) object).getLocale();
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
    protected @Nullable File saveDefaultLang(@NotNull File folder, @NotNull String name) {
        final String fileName = name + filePrefix;
        final InputStream in = plugin.getResource("lang/" + fileName);
        if (in == null) {
            return null;
        }
        final File file = new File(folder, fileName);
        try (OutputStream out = new FileOutputStream(file, false)) {
            in.transferTo(out);
        } catch (IOException e) {
            return null;
        }
        return file;
    }

    @Override
    public @NotNull File getLangFolder() {
        return new File(plugin.getDataFolder(), "lang");
    }

    @Override
    protected @NotNull Map<String, Object> getFileObjects(@NotNull File file) {
        final Map<String, Object> objects = new HashMap<>();
        final String name = file.getName().toLowerCase();
        if (!name.endsWith(".yml") || !name.endsWith(".yaml")) {
            return objects;
        }
        final YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (String key : config.getKeys(true)) {
            objects.put(key, config.get(key));
        }
        return objects;
    }

    public static class TextLoader extends TextDisplay.Loader<CommandSender> {
        @Override
        @SuppressWarnings("unchecked")
        public @Nullable TextDisplay<CommandSender> load(@NotNull DMap map) {
            if (ServerInstance.isSpigot) {
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
        public void append(@NotNull String s, @NotNull Set<TextDisplay.Event> events) {
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
                            final String tag = event.getItemTag();
                            builder.event(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new Item(
                                    event.getItemId(),
                                    event.getItemCount(),
                                    tag == null ? null : ItemTag.ofNbt(tag)
                            )));
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
                if (ServerInstance.verNumber > 8) {
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
            if (sender instanceof Player && ServerInstance.isSpigot) {
                ((Player) sender).spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(actionbar));
            } else {
                sender.sendMessage(actionbar);
            }
        }
    }

    public static class SoundLoader extends SoundDisplay.Loader<CommandSender> {
        @Override
        protected @Nullable Sound parseSound(@NotNull String s, float volume, float pitch) {
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
