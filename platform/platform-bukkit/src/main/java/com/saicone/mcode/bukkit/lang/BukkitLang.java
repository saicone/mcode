package com.saicone.mcode.bukkit.lang;

import com.google.common.base.Enums;
import com.saicone.mcode.bukkit.util.ServerInstance;
import com.saicone.mcode.module.lang.Display;
import com.saicone.mcode.module.lang.AbstractLang;
import com.saicone.mcode.module.lang.display.*;
import com.saicone.mcode.util.DMap;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
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
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

public class BukkitLang extends AbstractLang<CommandSender, Player> {

    // Loadable display types
    public static final TextLoader TEXT = new TextLoader();
    public static final TitleLoader TITLE = new TitleLoader();
    public static final ActionbarLoader ACTIONBAR = new ActionbarLoader();
    public static final SoundLoader SOUND = new SoundLoader();
    public static final BossbarLoader BOSSBAR = new BossbarLoader();

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
