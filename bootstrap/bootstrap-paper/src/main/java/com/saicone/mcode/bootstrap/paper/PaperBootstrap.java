package com.saicone.mcode.bootstrap.paper;

import com.saicone.mcode.Plugin;
import com.saicone.mcode.bootstrap.Addon;
import com.saicone.mcode.bootstrap.Bootstrap;
import com.saicone.mcode.bukkit.util.ServerInstance;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
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

public class PaperBootstrap extends JavaPlugin implements Bootstrap {

    static {
        // Class load
    }

    private final Path folder;
    private final Set<Addon> addons;
    private final Plugin plugin;

    public PaperBootstrap() {
        // Initialization

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
        this.addons.add(Addon.PLATFORM_PAPER);

        // Load addon libraries
        for (Addon addon : this.addons) {
            getLibraryLoader().loadDependency(addon.dependency());
        }
        getLibraryLoader().load();

        // Initialize addons
        build("com.saicone.mcode.bukkit.BukkitPlatform");
        initAddons();

        // Load plugin
        this.plugin = loadPlugin(pluginClass);
    }

    private void initAddons() {
        if (this.addons.contains(Addon.MODULE_SCRIPT)) {
            init("com.saicone.mcode.bukkit.script.BukkitScripts");
        }
        if (this.addons.contains(Addon.MODULE_TASK)) {
            final String scheduler;
            if (ServerInstance.Platform.FOLIA) {
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
        this.plugin.onLoad();
    }

    @Override
    public void onEnable() {
        // Enable
        this.plugin.onEnable();
    }

    @Override
    public void onDisable() {
        // Disable
        this.plugin.onDisable();
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