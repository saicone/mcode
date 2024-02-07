package com.saicone.mcode.bukkit.lang;

import com.saicone.mcode.bukkit.util.ServerInstance;
import com.saicone.mcode.module.lang.Display;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

public class BukkitLang extends AbstractLang<CommandSender, Player> {

    // Loadable display types
    public static final ActionbarLoader ACTIONBAR = new ActionbarLoader();
    public static final BossbarLoader BOSSBAR = new BossbarLoader();
    public static final SoundLoader SOUND = new SoundLoader();
    public static final TextLoader TEXT = new TextLoader();
    public static final TitleLoader TITLE = new TitleLoader();

    static {
        boolean register = true;
        if (ServerInstance.isPaper && ServerInstance.verNumber >= 16 && ServerInstance.release >= 3) {
            try {
                Class.forName("com.saicone.mcode.bukkit.lang.PaperLang");
                register = false;
            } catch (Throwable ignored) { }
        }
        if (register) {
            Displays.register("actionbar", ACTIONBAR);
            Displays.register("bossbar", BOSSBAR);
            Displays.register("sound", SOUND);
            Displays.register("text", TEXT);
            Displays.register("title", TITLE);
        }
    }

    // Instance parameters
    private final Plugin plugin;
    private final boolean useConfig;

    @NotNull
    public static BukkitLang of(@NotNull Plugin plugin, @NotNull Class<?>... langProviders) {
        return of(plugin, false, langProviders);
    }

    @NotNull
    public static BukkitLang of(@NotNull Plugin plugin, boolean useConfig, @NotNull Class<?>... langProviders) {
        if (ServerInstance.isPaper && ServerInstance.verNumber >= 16 && ServerInstance.release >= 3) {
            return new PaperLang(plugin, useConfig, langProviders);
        }
        return new BukkitLang(plugin, useConfig, langProviders);
    }

    public BukkitLang(@NotNull Plugin plugin,  @NotNull Class<?>... langProviders) {
        this(plugin, false, langProviders);
    }

    public BukkitLang(@NotNull Plugin plugin, boolean useConfig, @NotNull Class<?>... langProviders) {
        super(langProviders);
        this.plugin = plugin;
        this.useConfig = useConfig;
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

    @NotNull
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public int getLogLevel() {
        if (useConfig) {
            return plugin.getConfig().getInt("Locale.LogLevel", 2);
        } else {
            return super.getLogLevel();
        }
    }

    @Override
    public @NotNull Map<String, String> getLanguageAliases() {
        if (useConfig) {
            final Map<String, String> map = new HashMap<>();
            final ConfigurationSection section = plugin.getConfig().getConfigurationSection("Locale.Aliases");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    final Object object = section.get(key);
                    if (object instanceof List) {
                        for (Object o : (List<?>) object) {
                            map.put(key.toLowerCase(), String.valueOf(o).toLowerCase());
                        }
                    }
                }
            }
            return map;
        } else {
            return super.getLanguageAliases();
        }
    }

    @Override
    public @NotNull String getPluginLanguage() {
        if (useConfig) {
            return plugin.getConfig().getString("Locale.Plugin", DEFAULT_LANGUAGE).toLowerCase();
        } else {
            return super.getPluginLanguage();
        }
    }

    @Override
    public @NotNull String getDefaultLanguage() {
        if (useConfig) {
            return plugin.getConfig().getString("Locale.Default", DEFAULT_LANGUAGE).toLowerCase();
        } else {
            return super.getDefaultLanguage();
        }
    }

    @Override
    protected @NotNull String getPlayerName(@NotNull Player player) {
        return player.getName();
    }

    @Override
    protected @NotNull String getPlayerLocale(@NotNull Player player) {
        return player.getLocale();
    }

    @Override
    public @NotNull Collection<CommandSender> getPlayers() {
        return Bukkit.getOnlinePlayers();
    }

    @Override
    protected @NotNull CommandSender getConsoleSender() {
        return Bukkit.getConsoleSender();
    }

    @Override
    public boolean isInstanceOfSender(@Nullable Object object) {
        return object instanceof CommandSender;
    }

    @Override
    public boolean isInstanceOfPlayer(@Nullable Object object) {
        return object instanceof Player;
    }

    @Override
    protected void sendLogToConsole(int level, @NotNull String msg) {
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

    public static class TextLoader extends TextDisplay.Loader<CommandSender> {
        @Override
        @SuppressWarnings("unchecked")
        public @Nullable Display<CommandSender> load(@NotNull DMap map) {
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

    public static class ActionbarLoader extends ActionbarDisplay.Loader<CommandSender> {
        @Override
        protected void sendActionbar(@NotNull CommandSender sender, @NotNull String actionbar) {
            if (sender instanceof Player && ServerInstance.isSpigot) {
                ((Player) sender).spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(actionbar));
            } else {
                sender.sendMessage(actionbar);
            }
        }
    }

    public static class SoundLoader extends SoundDisplay.Loader<CommandSender, Sound> {
        @Override
        protected @Nullable Sound parseSound(@NotNull String s) {
            try {
                return Sound.valueOf(s.toUpperCase());
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void playSound(@NotNull CommandSender sender, @NotNull Sound sound, float volume, float pitch) {
            if (sender instanceof Player) {
                ((Player) sender).playSound(((Player) sender).getLocation(), sound, volume, pitch);
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
