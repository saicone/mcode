package com.saicone.mcode.velocity.lang;

import com.moandjiezana.toml.Toml;
import com.saicone.mcode.module.lang.AdventureLang;
import com.saicone.mcode.module.lang.AbstractLang;
import com.saicone.mcode.module.lang.Displays;
import com.saicone.mcode.module.lang.display.TextDisplay;
import com.saicone.mcode.velocity.VelocityPlatform;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class VelocityLang extends AbstractLang<CommandSource> implements AdventureLang<CommandSource> {

    // Loadable display types
    public static final AdventureLang.ActionBarLoader<CommandSource> ACTIONBAR = new AdventureLang.ActionBarLoader<>();
    public static final AdventureLang.BossBarLoader<CommandSource> BOSSBAR = new AdventureLang.BossBarLoader<>();
    public static final AdventureLang.MiniMessageLoader<CommandSource> MINIMESSAGE = new AdventureLang.MiniMessageLoader<>();
    public static final AdventureLang.SoundLoader<CommandSource> SOUND = new AdventureLang.SoundLoader<>();
    public static final AdventureLang.TextLoader<CommandSource> TEXT = new AdventureLang.TextLoader<>() {
        @Override
        protected @NotNull TextDisplay.Builder<CommandSource> newBuilder() {
            return new AdventureLang.TextBuilder<>() {
                @Override
                protected int protocol(@NotNull CommandSource type) {
                    if (type instanceof Player player) {
                        return player.getProtocolVersion().getProtocol();
                    }
                    return super.protocol(type);
                }
            };
        }
    };
    public static final AdventureLang.TitleLoader<CommandSource> TITLE = new AdventureLang.TitleLoader<>();

    static {
        Displays.register("actionbar", ACTIONBAR);
        Displays.register("bossbar", BOSSBAR);
        Displays.register("minimessage", MINIMESSAGE);
        Displays.register("sound", SOUND);
        Displays.register("text", TEXT);
        Displays.register("title", TITLE);
    }

    private final ProxyServer proxy;
    private final Object plugin;
    private final Logger logger;

    public VelocityLang(@NotNull Object plugin, @NotNull Logger logger, @NotNull Object... providers) {
        this(VelocityPlatform.get().getProxy(), plugin, logger, providers);
    }

    public VelocityLang(@NotNull ProxyServer proxy, @NotNull Object plugin, @NotNull Logger logger, @NotNull Object... providers) {
        super(providers);
        this.proxy = proxy;
        this.plugin = plugin;
        this.logger = logger;
    }

    @NotNull
    public Object getPlugin() {
        return plugin;
    }

    @NotNull
    public Logger getLogger() {
        return logger;
    }

    @Override
    public @NotNull String getLanguageFor(@Nullable Object object) {
        if (object instanceof CommandSource) {
            if (object instanceof Player player) {
                return player.getPlayerSettings().getLocale().toLanguageTag().replace('-', '_');
            } else {
                return super.getLanguageFor(null);
            }
        }
        return super.getLanguageFor(object);
    }

    @Override
    protected @NotNull CommandSource getConsole() {
        return proxy.getConsoleCommandSource();
    }

    @Override
    protected @NotNull Collection<? extends CommandSource> getSenders() {
        return proxy.getAllPlayers();
    }

    @Override
    protected void log(int level, @NotNull String msg) {
        switch (level) {
            case 1:
                logger.error(msg);
                break;
            case 2:
                logger.warn(msg);
                break;
            case 3:
                logger.info(msg);
                break;
            case 4:
            default:
                logger.debug(msg);
                break;
        }
    }

    @Override
    protected void log(int level, @NotNull String msg, @NotNull Throwable exception) {
        switch (level) {
            case 1:
                logger.error(msg, exception);
                break;
            case 2:
                logger.warn(msg, exception);
                break;
            case 3:
                logger.info(msg, exception);
                break;
            case 4:
            default:
                logger.debug(msg, exception);
                break;
        }
    }

    @Override
    protected void saveFile(@NotNull File folder, @NotNull String name) {
        final File file = new File(folder, name);
        if (file.exists()) {
            return;
        }
        try (InputStream in = plugin.getClass().getClassLoader().getResourceAsStream("lang/" + name)) {
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
        return java.nio.file.Path.of("plugins", proxy.getPluginManager().ensurePluginContainer(plugin).getDescription().getId(), "lang").toFile();
    }

    @Override
    protected @NotNull Map<?, ?> getFileObjects(@NotNull File file) {
        if (!file.getName().contains(".")) {
            return new HashMap<>();
        }
        final ConfigurationLoader<? extends ConfigurationNode> loader;
        switch (file.getName().substring(file.getName().lastIndexOf('.') + 1).toLowerCase().trim()) {
            case "toml":
                return new Toml().read(file).toMap();
            case "yml":
            case "yaml":
                loader = YamlConfigurationLoader.builder().file(file).build();
                break;
            case "json":
                loader = GsonConfigurationLoader.builder().file(file).build();
                break;
            case "hocon":
                loader = HoconConfigurationLoader.builder().file(file).build();
                break;
            default:
                return new HashMap<>();
        }
        try {
            final ConfigurationNode value = loader.load();
            if (value.isMap()) {
                return (Map<?, ?>) asObject(value);
            }
        } catch (IOException e) {
            sendLog(2, e, "Cannot load displays from configuration at file " + file.getName());
        }
        return new HashMap<>();
    }

    @Nullable
    private Object asObject(@NotNull ConfigurationNode node) {
        if (node.isMap()) {
            final Map<Object, Object> map = new HashMap<>();
            for (Map.Entry<Object, ? extends ConfigurationNode> entry : node.childrenMap().entrySet()) {
                map.put(entry.getKey(), asObject(entry.getValue()));
            }
            return map;
        } else if (node.isList()) {
            final List<Object> list = new ArrayList<>();
            for (ConfigurationNode child : node.childrenList()) {
                list.add(asObject(child));
            }
            return list;
        } else if (node.isNull()) {
            return null;
        } else {
            return node.raw();
        }
    }
}
