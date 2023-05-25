package com.saicone.mcode.bungee.lang;

import com.saicone.mcode.module.lang.LangLoader;
import com.saicone.mcode.module.lang.display.ActionbarDisplay;
import com.saicone.mcode.module.lang.display.TextDisplay;
import com.saicone.mcode.module.lang.display.TitleDisplay;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.JsonConfiguration;
import net.md_5.bungee.config.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class BungeeLang extends LangLoader<CommandSender, ProxiedPlayer> {

    public static final TextLoader TEXT = new TextLoader();
    public static final TitleLoader TITLE = new TitleLoader();
    public static final ActionbarLoader ACTIONBAR = new ActionbarLoader();

    private final Plugin plugin;

    public BungeeLang(@NotNull Plugin plugin, @NotNull Class<?>... langProviders) {
        super(langProviders);
        this.plugin = plugin;
    }

    @Override
    protected @Nullable File saveDefaultLang(@NotNull File folder, @NotNull String name) {
        final InputStream in = plugin.getResourceAsStream("lang/" + name + ".yml");
        if (in == null) {
            return null;
        }
        final File file = new File(folder, name + ".yml");
        try (OutputStream out = new FileOutputStream(file, false)) {
            in.transferTo(out);
            in.close();
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
        final String name = file.getName().toLowerCase();
        final Configuration config;
        try {
            if (name.endsWith(".yaml") || name.endsWith(".yml")) {
                config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            } else if (name.endsWith(".json")) {
                config = ConfigurationProvider.getProvider(JsonConfiguration.class).load(file);
            } else {
                return new HashMap<>();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
        return getFileObjects(config);
    }

    @NotNull
    public Plugin getPlugin() {
        return plugin;
    }

    @NotNull
    protected Map<String, Object> getFileObjects(@NotNull Configuration config) {
        final Map<String, Object> map = new HashMap<>();
        for (String key : config.getKeys()) {
            final Object value = config.get(key);
            if (value instanceof Configuration) {
                map.put(key, getFileObjects((Configuration) value));
            } else {
                map.put(key, value);
            }
        }
        return map;
    }

    @Override
    protected @NotNull String getPlayerName(@NotNull ProxiedPlayer player) {
        return player.getName();
    }

    @Override
    protected @NotNull String getPlayerLocale(@NotNull ProxiedPlayer player) {
        return player.getLocale().toLanguageTag().replace('-', '_');
    }

    @Override
    public @NotNull Collection<? extends ProxiedPlayer> getPlayers() {
        return plugin.getProxy().getPlayers();
    }

    @Override
    protected @NotNull CommandSender getConsoleSender() {
        return plugin.getProxy().getConsole();
    }

    @Override
    public boolean isInstanceOfSender(@Nullable Object object) {
        return object instanceof CommandSender;
    }

    @Override
    public boolean isInstanceOfPlayer(@Nullable Object object) {
        return object instanceof ProxiedPlayer;
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
        protected @Nullable Object parseAction(@NotNull String s) {
            return null;
        }

        @Override
        protected void sendText(@NotNull CommandSender sender, @NotNull String text) {
            for (String line : text.split("\n")) {
                sender.sendMessage(TextComponent.fromLegacyText(line));
            }
        }

        @Override
        protected TextDisplay.Builder<CommandSender> newBuilder() {
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
        public void sendTo(@NotNull CommandSender sender) {
            sender.sendMessage(builder.create());
        }
    }

    public static class TitleLoader extends TitleDisplay.Loader<CommandSender> {
        @Override
        protected void sendTitle(@NotNull CommandSender sender, @NotNull String title, @NotNull String subtitle, int fadeIn, int stay, int fadeOut) {
            if (sender instanceof ProxiedPlayer) {
                ((ProxiedPlayer) sender).sendTitle(ProxyServer.getInstance().createTitle()
                        .title(TextComponent.fromLegacyText(title))
                        .subTitle(TextComponent.fromLegacyText(subtitle))
                        .fadeIn(fadeIn)
                        .stay(stay)
                        .fadeOut(fadeOut));
            } else {
                sender.sendMessage(TextComponent.fromLegacyText(title));
                sender.sendMessage(TextComponent.fromLegacyText(subtitle));
            }
        }
    }

    public static class ActionbarLoader extends ActionbarDisplay.Loader<CommandSender> {
        @Override
        protected void sendActionbar(@NotNull CommandSender sender, @NotNull String actionbar) {
            if (sender instanceof ProxiedPlayer) {
                ((ProxiedPlayer) sender).sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(actionbar));
            } else {
                sender.sendMessage(TextComponent.fromLegacyText(actionbar));
            }
        }
    }
}
