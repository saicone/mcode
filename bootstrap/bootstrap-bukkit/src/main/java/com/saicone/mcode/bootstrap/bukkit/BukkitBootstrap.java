package com.saicone.mcode.bootstrap.bukkit;

import com.saicone.mcode.Plugin;
import com.saicone.mcode.bootstrap.Addon;
import com.saicone.mcode.bootstrap.Bootstrap;
import com.saicone.mcode.bukkit.util.ServerInstance;
import com.saicone.mcode.env.Env;
import com.saicone.mcode.env.Executes;
import com.saicone.mcode.env.Registrar;
import com.saicone.mcode.util.concurrent.DelayedExecutor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;

public class BukkitBootstrap extends JavaPlugin implements Bootstrap, DelayedExecutor, Registrar {

    static {
        // Class load
        Env.init(BukkitBootstrap.class);
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            Env.condition("multithreading", true);
        } catch (ClassNotFoundException ignored) { }
        Env.execute(Executes.BOOT, true);
    }

    private final Path folder;
    private final Set<Addon> addons;
    private final Plugin plugin;

    public BukkitBootstrap() {
        // Initialization
        Env.executor(this);
        Env.registrar(this);
        Env.execute(Executes.BOOT, false);

        // Replace logger with Bukkit logger
        getLibraryLoader().logger((level, msg) -> {
            if (msg.contains("Ezlib is already initialized")) {
                return;
            }
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

        // Load MCode information from plugin.yml
        this.addons = new HashSet<>();
        final YamlConfiguration plugin = new YamlConfiguration();
        final String pluginClass;
        try (InputStreamReader reader = new InputStreamReader(new BufferedInputStream(this.getResource("plugin.yml")))) {
            plugin.load(reader);
            pluginClass = Objects.requireNonNull(plugin.getString("mcode.plugin"), "Cannot find plugin class from mcode configuration inside plugin.yml");
            for (String name : plugin.getStringList("mcode.addons")) {
                final Addon addon = Addon.of(name);
                if (addon != null) {
                    this.addons.add(addon);
                } else {
                    getLogger().warning("Found invalid addon name: " + name);
                }
            }
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException("Cannot read plugin.yml from plugin JAR file", e);
        }

        // Put platform addons
        this.addons.add(Addon.PLATFORM_BUKKIT);
        try {
            Class.forName("com.destroystokyo.paper.Title");
            if (Runtime.version().feature() >= 21) {
                this.addons.add(Addon.PLATFORM_PAPER);
            }
        } catch (ClassNotFoundException ignored) { }

        // Load addon libraries
        for (Addon addon : this.addons) {
            getLibraryLoader().loadDependency(addon.dependency());
        }
        getLibraryLoader().load();

        // Initialize addons
        build("com.saicone.mcode.bukkit.BukkitPlatform");
        initAddons();

        // Reload Awake annotations, some methods and classes should load correctly with its dependencies loaded
        Env.reload();

        // Load plugin
        Env.execute(Executes.INIT, true);
        this.plugin = loadPlugin(pluginClass);
        Env.execute(Executes.INIT, false);
    }

    private void initAddons() {
        if (this.addons.contains(Addon.MODULE_SCRIPT)) {
            init("com.saicone.mcode.bukkit.script.BukkitScripts");
        }
        if (this.addons.contains(Addon.MODULE_TASK)) {
            final String scheduler;
            if (this.addons.contains(Addon.PLATFORM_PAPER) && ServerInstance.Platform.FOLIA) {
                scheduler = "com.saicone.mcode.folia.scheduler.FoliaScheduler";
            } else {
                scheduler = "com.saicone.mcode.bukkit.scheduler.BukkitScheduler";
            }
            run("com.saicone.mcode.module.task.Task", "setScheduler", build(scheduler, this));
        }
        if (this.addons.contains(Addon.LIBRARY_SETTINGS)) {
            init("com.saicone.mcode.bukkit.settings.BukkitYamlSource");
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
        switch (level) {
            case 1:
                return Level.SEVERE;
            case 2:
                return Level.WARNING;
            default:
                return Level.INFO;
        }
    }

    @Override
    public void execute(@NotNull Runnable command) {
        Bukkit.getScheduler().runTask(this, command);
    }

    @Override
    public void execute(@NotNull Runnable command, long delay, @NotNull TimeUnit unit) {
        Bukkit.getScheduler().runTaskLater(this, command, (long) (unit.toMillis(delay) * 0.02));
    }

    @Override
    public void execute(@NotNull Runnable command, long delay, long period, @NotNull TimeUnit unit) {
        Bukkit.getScheduler().runTaskTimer(this, command, (long) (unit.toMillis(delay) * 0.02), (long) (unit.toMillis(period) * 0.02));
    }

    @Override
    public boolean isPresent(@NotNull String dependency) {
        return Bukkit.getPluginManager().isPluginEnabled(dependency);
    }

    @Override
    public void register(@NotNull Object object) {
        if (object instanceof Listener) {
            Bukkit.getPluginManager().registerEvents((Listener) object, this);
        }
    }
}