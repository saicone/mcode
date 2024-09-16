package com.saicone.mcode.bootstrap.bungee;

import com.saicone.mcode.Plugin;
import com.saicone.mcode.bootstrap.Addon;
import com.saicone.mcode.bootstrap.Bootstrap;
import com.saicone.mcode.env.Env;
import com.saicone.mcode.env.Executes;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;

public class BungeeBootstrap extends net.md_5.bungee.api.plugin.Plugin implements Bootstrap {

    static {
        // Load platform addons
        LIBRARY_LOADER.applyDependency(Addon.PLATFORM_BUNGEE.dependency());

        // Class load
        Env.init(BungeeBootstrap.class);
        Env.execute(Executes.BOOT, true);
    }

    private final Path folder;
    private final Set<Addon> addons;
    private final Plugin plugin;

    public BungeeBootstrap() {
        // Initialization
        Env.executor(build("com.saicone.mcode.bungee.env.BungeeExecutor", this));
        Env.registrar(build("com.saicone.mcode.bungee.env.BungeeRegistrar", this));
        Env.execute(Executes.BOOT, false);

        // Replace logger with Bukkit logger
        getLibraryLoader().logger((level, msg) -> {
            switch (level) {
                case 1:
                    getLogger().severe(msg);
                    break;
                case 2:
                    getLogger().warning(msg);
                    break;
                case 3:
                    getLogger().info(msg);
                    break;
                default:
                    break;
            }
        });

        this.folder = getDataFolder().toPath();

        // Load MCode information from bungee.yml
        this.addons = new HashSet<>();
        final String pluginClass;
        try (InputStreamReader reader = new InputStreamReader(new BufferedInputStream(this.getResourceAsStream("bungee.yml")))) {
            final Configuration plugin = ConfigurationProvider.getProvider(YamlConfiguration.class).load(reader);
            pluginClass = Objects.requireNonNull(plugin.getString("mcode.plugin"), "Cannot find plugin class from mcode configuration inside bungee.yml");
            for (String name : plugin.getStringList("mcode.addons")) {
                final Addon addon = Addon.of(name);
                if (addon != null) {
                    this.addons.add(addon);
                } else {
                    getLogger().warning("Found invalid addon name: " + name);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot read bungee.yml from plugin JAR file", e);
        }

        // Load addon libraries
        for (Addon addon : this.addons) {
            getLibraryLoader().loadDependency(addon.dependency());
        }
        getLibraryLoader().load();

        // Initialize addons
        build("com.saicone.mcode.bungee.BungeePlatform");
        initAddons();

        // Reload runtime some classes should load correctly with its dependencies loaded
        Env.runtime().reload();

        // Load plugin
        Env.execute(Executes.INIT, true);
        this.plugin = loadPlugin(pluginClass);
        Env.execute(Executes.INIT, false);
    }

    private void initAddons() {
        if (this.addons.contains(Addon.MODULE_SCRIPT)) {
            init("com.saicone.mcode.bungee.script.BungeeScripts");
        }
        if (this.addons.contains(Addon.MODULE_TASK)) {
            final Object scheduler = build("com.saicone.mcode.bungee.scheduler.BungeeScheduler", this);
            run("com.saicone.mcode.module.task.Task", "setScheduler", scheduler);
        }
        if (this.addons.contains(Addon.LIBRARY_SETTINGS)) {
            init("com.saicone.mcode.bungee.settings.BungeeYamlSource");
        }
    }

    @Override
    public void onLoad() {
        // Load
        Env.execute(Executes.LOAD, true);
        this.plugin.onLoad();
        Env.execute(Executes.LOAD, false);
    }

    @Override
    public void onEnable() {
        // Enable
        Env.execute(Executes.ENABLE, true);
        this.plugin.onEnable();
        Env.execute(Executes.ENABLE, false);
    }

    @Override
    public void onDisable() {
        // Disable
        Env.execute(Executes.DISABLE, true);
        this.plugin.onDisable();
        Env.execute(Executes.DISABLE, false);
    }

    @Override
    public @NotNull Path folder() {
        return this.folder;
    }

    @Override
    public void log(int level, @NotNull Supplier<String> msg) {
        this.getLogger().log(level(level), msg);
    }

    @Override
    public void logException(int level, @NotNull Throwable throwable) {
        this.getLogger().log(level(level), throwable, () -> "");
    }

    @Override
    public void logException(int level, @NotNull Throwable throwable, @NotNull Supplier<String> msg) {
        this.getLogger().log(level(level), throwable, msg);
    }

    @NotNull
    private Level level(int level) {
        return switch (level) {
            case 1 -> Level.SEVERE;
            case 2 -> Level.WARNING;
            default -> Level.INFO;
        };
    }
}
