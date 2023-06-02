package com.saicone.mcode.bukkit.lang;

import com.google.common.base.Enums;
import com.saicone.mcode.bukkit.util.ServerInstance;
import com.saicone.mcode.module.lang.LangLoader;
import com.saicone.mcode.module.lang.display.*;
import com.saicone.mcode.util.DMap;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class BukkitLang extends LangLoader<CommandSender, Player> {

    // Loadable display types
    public static final TextLoader TEXT = new TextLoader();
    public static final TitleLoader TITLE = new TitleLoader();
    public static final ActionbarLoader ACTIONBAR = new ActionbarLoader();
    public static final SoundLoader SOUND = new SoundLoader();

    // Instance parameters
    private final Plugin plugin;
    private final boolean useConfig;

    private final BossbarLoader bossbar = new BossbarLoader();

    public BukkitLang(@NotNull Plugin plugin,  @NotNull Class<?>... langProviders) {
        this(plugin, false, langProviders);
    }

    public BukkitLang(@NotNull Plugin plugin, boolean useConfig, @NotNull Class<?>... langProviders) {
        super(langProviders);
        this.plugin = plugin;
        this.useConfig = useConfig;
    }

    @NotNull
    public BossbarLoader bossbar() {
        return bossbar;
    }

    @Override
    protected @Nullable File saveDefaultLang(@NotNull File folder, @NotNull String name) {
        final InputStream in = plugin.getResource("lang/" + name + ".yml");
        if (in == null) {
            return null;
        }
        final File file = new File(folder, name + ".yml");
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
    public @NotNull Collection<? extends Player> getPlayers() {
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
        protected @Nullable Object parseAction(@NotNull String s) {
            Object object = Enums.getIfPresent(HoverEvent.Action.class, s.toUpperCase()).orNull();
            if (object == null) {
                object = Enums.getIfPresent(ClickEvent.Action.class, s.toUpperCase()).orNull();
            }
            return object;
        }

        @Override
        protected void sendText(@NotNull CommandSender sender, @NotNull String text) {
            sender.sendMessage(text.split("\n"));
        }

        @Override
        protected TextBuilder newBuilder() {
            return new TextBuilder();
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
        public void append(@NotNull String s, @NotNull Map<Object, String> actions) {
            final ComponentBuilder component = new ComponentBuilder();
            component.append(s);
            for (var entry : actions.entrySet()) {
                final Object action = entry.getKey();
                if (action instanceof HoverEvent.Action) {
                    builder.event(new HoverEvent((HoverEvent.Action) action, new Text(entry.getValue())));
                } else if (action instanceof ClickEvent.Action) {
                    builder.event(new ClickEvent((ClickEvent.Action) action, entry.getValue()));
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
                sender.sendMessage(title, subtitle);
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

    public class BossbarLoader extends BossbarDisplay.Loader<CommandSender> {
        public BossbarLoader() {
            super(false);
        }

        @Override
        protected BossbarDisplay.Builder<CommandSender> newBuilder(float progress, @NotNull String color, @NotNull String style, long stay) {
            final BarColor barColor = Enums.getIfPresent(BarColor.class, color).or(BarColor.RED);
            final BarStyle barStyle = Enums.getIfPresent(BarStyle.class, style).or(BarStyle.SOLID);
            return new BossbarBuilder(progress, barColor, barStyle, stay);
        }
    }

    public class BossbarBuilder extends BossbarDisplay.Builder<CommandSender> {

        private final BarColor color;
        private final BarStyle style;

        public BossbarBuilder(float progress, BarColor color, BarStyle style, long stay) {
            super(progress, stay);
            this.color = color;
            this.style = style;
        }

        @Override
        public void sendTo(@NotNull CommandSender sender, @NotNull String text) {
            if (sender instanceof Player) {
                final BossBar bossBar = Bukkit.createBossBar(text, color, style);
                bossBar.setProgress(progress);
                bossBar.addPlayer((Player) sender);
                Bukkit.getScheduler().runTaskLater(plugin, () -> bossBar.removePlayer((Player) sender), stay);
            }
        }
    }
}
