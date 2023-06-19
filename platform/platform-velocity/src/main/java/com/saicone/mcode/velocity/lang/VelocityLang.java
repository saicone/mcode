package com.saicone.mcode.velocity.lang;

import com.moandjiezana.toml.Toml;
import com.saicone.mcode.module.lang.AdventureLang;
import com.saicone.mcode.module.lang.LangLoader;
import com.saicone.mcode.util.DMap;
import com.saicone.mcode.velocity.VelocityPlatform;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.*;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class VelocityLang extends LangLoader<CommandSource, Player> {

    // Loadable display types
    public static final AdventureLang.TextLoader<CommandSource> TEXT = new AdventureLang.TextLoader<>();
    public static final AdventureLang.TitleLoader<CommandSource> TITLE = new AdventureLang.TitleLoader<>();
    public static final AdventureLang.ActionbarLoader<CommandSource> ACTIONBAR = new AdventureLang.ActionbarLoader<>();
    public static final AdventureLang.SoundLoader<CommandSource> SOUND = new AdventureLang.SoundLoader<>();
    public static final AdventureLang.MiniMessageLoader<CommandSource> MINIMESSAGE = new AdventureLang.MiniMessageLoader<>();

    private final ProxyServer proxy;
    private final Object plugin;
    private final Logger logger;

    private final AdventureLang.BossbarLoader<CommandSource> bossbar = new AdventureLang.BossbarLoader<>() {
        @Override
        protected void later(@NotNull Runnable runnable, long ticks) {
            final long millis = ticks * 1000 / 20000;
            proxy.getScheduler().buildTask(getPlugin(), runnable).delay(Duration.ofMillis(millis)).schedule();
        }
    };

    public VelocityLang(@NotNull Object plugin, @NotNull Logger logger, @NotNull Class<?>... langProviders) {
        this(VelocityPlatform.get().getProxy(), plugin, logger, langProviders);
    }

    public VelocityLang(@NotNull ProxyServer proxy, @NotNull Object plugin, @NotNull Logger logger, @NotNull Class<?>... langProviders) {
        super(langProviders);
        this.proxy = proxy;
        this.plugin = plugin;
        this.logger = logger;
    }

    @Override
    protected @Nullable File saveDefaultLang(@NotNull File folder, @NotNull String name) {
        final String fileName = name + ".yml";
        final InputStream in = plugin.getClass().getClassLoader().getResourceAsStream("lang/" + fileName);
        if (in == null) {
            return null;
        }
        final File file = new File(folder, fileName);
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
        return new File(new File(new File("plugins"), proxy.getPluginManager().ensurePluginContainer(plugin).getDescription().getId()), "lang");
    }

    @Override
    protected @NotNull Map<String, Object> getFileObjects(@NotNull File file) {
        if (!file.getName().contains(".")) {
            return new HashMap<>();
        }
        final ConfigurationLoader<? extends ConfigurationNode> loader;
        switch (file.getName().substring(file.getName().lastIndexOf('.') + 1).toLowerCase().trim()) {
            case "toml":
                return new DMap(new Toml().read(file).toMap()).asDeepPath(".");
            case "yml":
            case "yaml":
                loader = YAMLConfigurationLoader.builder().setFile(file).build();
                break;
            case "json":
                loader = GsonConfigurationLoader.builder().setFile(file).build();
                break;
            case "hocon":
                loader = HoconConfigurationLoader.builder().setFile(file).build();
                break;
            default:
                return new HashMap<>();
        }
        try {
            final Object value = loader.load().getValue();
            if (value instanceof Map) {
                return DMap.of((Map<?, ?>) value).asDeepPath(".");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new HashMap<>();
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
    protected @NotNull String getPlayerName(@NotNull Player player) {
        return player.getUsername();
    }

    @Override
    protected @NotNull String getPlayerLocale(@NotNull Player player) {
        return player.getPlayerSettings().getLocale().toLanguageTag().replace('-', '_');
    }

    @Override
    public @NotNull Collection<? extends Player> getPlayers() {
        return proxy.getAllPlayers();
    }

    @Override
    protected @NotNull CommandSource getConsoleSender() {
        return proxy.getConsoleCommandSource();
    }

    @Override
    public boolean isInstanceOfSender(@Nullable Object object) {
        return object instanceof CommandSource;
    }

    @Override
    public boolean isInstanceOfPlayer(@Nullable Object object) {
        return object instanceof Player;
    }

    @Override
    protected void sendLogToConsole(int level, @NotNull String msg) {
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
}
