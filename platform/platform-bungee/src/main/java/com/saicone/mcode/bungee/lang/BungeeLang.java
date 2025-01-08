package com.saicone.mcode.bungee.lang;

import com.saicone.mcode.module.lang.AbstractLang;
import com.saicone.mcode.module.lang.display.ActionBarDisplay;
import com.saicone.mcode.module.lang.display.TextDisplay;
import com.saicone.mcode.module.lang.display.TitleDisplay;
import com.saicone.mcode.platform.MC;
import com.saicone.mcode.platform.Text;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Entity;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.JsonConfiguration;
import net.md_5.bungee.config.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class BungeeLang extends AbstractLang<CommandSender> {

    private static final String ITEM_HOVER = "{\"id\":\"%s\",\"count\":%d,\"components\": %s}";

    private final ActionBarLoader actionbar = new ActionBarLoader();
    private final TextLoader text = new TextLoader();
    private final TitleLoader title = new TitleLoader();

    private final Plugin plugin;

    public BungeeLang(@NotNull Plugin plugin, @NotNull Object... providers) {
        super(providers);
        this.plugin = plugin;
    }

    @NotNull
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public @NotNull String getLanguageFor(@Nullable Object object) {
        if (object instanceof CommandSender) {
            if (object instanceof ProxiedPlayer) {
                return ((ProxiedPlayer) object).getLocale().toLanguageTag().replace('-', '_');
            } else {
                return super.getLanguageFor(null);
            }
        }
        return super.getLanguageFor(object);
    }

    @Override
    protected @NotNull CommandSender getConsole() {
        return plugin.getProxy().getConsole();
    }

    @Override
    protected @NotNull Collection<? extends CommandSender> getSenders() {
        return plugin.getProxy().getPlayers();
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
    protected void saveFile(@NotNull File folder, @NotNull String name) {
        final File file = new File(folder, name);
        if (file.exists()) {
            return;
        }
        try (InputStream in = plugin.getResourceAsStream("lang/" + name)) {
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
            sendLog(2, e, "Cannot load displays from configuration at file " + file.getName());
            return new HashMap<>();
        }
        return getFileObjects(config);
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

    public static class TextLoader extends TextDisplay.Loader<CommandSender> {
        @Override
        protected void sendText(@NotNull CommandSender sender, @NotNull Text text) {
            for (String line : text.getAsColored().getValue().split("\n")) {
                sender.sendMessage(TextComponent.fromLegacyText(line));
            }
        }

        @Override
        protected TextDisplay.@NotNull Builder<CommandSender> newBuilder() {
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
        public void append(@NotNull CommandSender type, @NotNull String s, @NotNull Set<TextDisplay.Event> events) {
            final ComponentBuilder component = new ComponentBuilder();
            component.append(s);
            for (TextDisplay.Event event : events) {
                if (event.getAction().isClick()) {
                    builder.event(new ClickEvent(ClickEvent.Action.values()[event.getAction().ordinal()], event.getString()));
                } else if (event.getAction().isHover()) {
                    switch (event.getAction().hover()) {
                        case SHOW_TEXT:
                            builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new net.md_5.bungee.api.chat.hover.content.Text(event.getString())));
                            break;
                        case SHOW_ITEM:
                            if (type instanceof ProxiedPlayer player && player.getPendingConnection().getVersion() >= MC.V_1_20_5.protocol()) {
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
        public void sendTo(@NotNull CommandSender sender) {
            sender.sendMessage(builder.create());
        }
    }

    public static class TitleLoader extends TitleDisplay.Loader<CommandSender> {
        @Override
        protected void sendTitle(@NotNull CommandSender sender, @NotNull Text title, @NotNull Text subtitle, int fadeIn, int stay, int fadeOut) {
            if (sender instanceof ProxiedPlayer) {
                ((ProxiedPlayer) sender).sendTitle(ProxyServer.getInstance().createTitle()
                        .title(TextComponent.fromLegacyText(title.getAsColored().getValue()))
                        .subTitle(TextComponent.fromLegacyText(subtitle.getAsColored().getValue()))
                        .fadeIn(fadeIn)
                        .stay(stay)
                        .fadeOut(fadeOut));
            } else {
                sender.sendMessage(TextComponent.fromLegacyText(title.getAsColored().getValue()));
                sender.sendMessage(TextComponent.fromLegacyText(subtitle.getAsColored().getValue()));
            }
        }
    }

    public static class ActionBarLoader extends ActionBarDisplay.Loader<CommandSender> {
        @Override
        protected void sendActionbar(@NotNull CommandSender sender, @NotNull Text actionbar) {
            if (sender instanceof ProxiedPlayer) {
                ((ProxiedPlayer) sender).sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(actionbar.getAsColored().getValue()));
            } else {
                sender.sendMessage(TextComponent.fromLegacyText(actionbar.getAsColored().getValue()));
            }
        }
    }
}
