package com.saicone.mcode.bootstrap.velocity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.saicone.mcode.Plugin;
import com.saicone.mcode.bootstrap.Addon;
import com.saicone.mcode.bootstrap.Bootstrap;
import com.saicone.mcode.env.Env;
import com.saicone.mcode.env.Executes;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class VelocityBootstrap implements Bootstrap {

    static {
        // Put platform addons
        LIBRARY_LOADER.applyDependency(Addon.PLATFORM_VELOCITY.dependency());

        // Class load
        Env.init(VelocityBootstrap.class);
        Env.execute(Executes.BOOT, true);
    }

    private final ProxyServer proxy;
    private final Logger logger;
    private final Path folder;
    private final Set<Addon> addons;
    private final Plugin plugin;

    @Inject
    public VelocityBootstrap(ProxyServer proxy, Logger logger, @DataDirectory Path folder) {
        // Initialization
        Env.executor(build("com.saicone.mcode.velocity.env.VelocityExecutor", proxy, this));
        Env.registrar(build("com.saicone.mcode.velocity.env.VelocityRegistrar", proxy, this));
        Env.execute(Executes.BOOT, false);

        // Replace logger with Bukkit logger
        getLibraryLoader().logger((level, msg) -> {
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
                default:
                    break;
            }
        });

        this.proxy = proxy;
        this.logger = logger;
        this.folder = folder;

        // Load MCode information from velocity-plugin.json
        this.addons = new HashSet<>();
        final String pluginClass;
        try (InputStreamReader reader = new InputStreamReader(new BufferedInputStream(VelocityBootstrap.class.getClassLoader().getResourceAsStream("velocity-plugin.json")))) {
            final JsonElement plugin = JsonParser.parseReader(reader);
            final JsonObject mcode = plugin.getAsJsonObject().getAsJsonObject("mcode");
            pluginClass = mcode.getAsJsonPrimitive("plugin").getAsString();
            if (mcode.has("addons")) {
                for (JsonElement name : mcode.getAsJsonArray("addons")) {
                    final Addon addon = Addon.of(name.getAsString());
                    if (addon != null) {
                        this.addons.add(addon);
                    } else {
                        logger.warn("Found invalid addon name: {}", name);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot read velocity-plugin.json from plugin JAR file", e);
        }

        // Load addon libraries
        for (Addon addon : this.addons) {
            getLibraryLoader().loadDependency(addon.dependency());
        }
        getLibraryLoader().load();

        // Initialize addons
        build("com.saicone.mcode.velocity.VelocityPlatform", proxy);
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
            init("com.saicone.mcode.velocity.script.VelocityScripts");
        }
        if (this.addons.contains(Addon.MODULE_TASK)) {
            run("com.saicone.mcode.module.task.Task", "setScheduler", build("com.saicone.mcode.velocity.scheduler.VelocityScheduler", this.proxy, this));
        }
        if (this.addons.contains(Addon.LIBRARY_SETTINGS)) {
            init("com.saicone.mcode.velocity.settings.TomlSettingsSource");
        }
    }

    @Subscribe
    public void onInit(ProxyInitializeEvent e) {
        // Load
        Env.execute(Executes.LOAD, true);
        this.plugin.onLoad();
        Env.execute(Executes.LOAD, false);

        // Enable
        Env.execute(Executes.ENABLE, true);
        this.plugin.onEnable();
        Env.execute(Executes.ENABLE, false);
    }

    @Subscribe
    public void onShut(ProxyShutdownEvent e) {
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
        switch (level) {
            case 1:
                this.logger.error(msg.get());
                break;
            case 2:
                this.logger.warn(msg.get());
                break;
            default:
                this.logger.info(msg.get());
                break;
        }
    }

    @Override
    public void logException(int level, @NotNull Throwable throwable) {
        switch (level) {
            case 1:
                this.logger.error("", throwable);
                break;
            case 2:
                this.logger.warn("", throwable);
                break;
            default:
                this.logger.info("", throwable);
                break;
        }
    }

    @Override
    public void logException(int level, @NotNull Throwable throwable, @NotNull Supplier<String> msg) {
        switch (level) {
            case 1:
                this.logger.error(msg.get(), throwable);
                break;
            case 2:
                this.logger.warn(msg.get(), throwable);
                break;
            default:
                this.logger.info(msg.get(), throwable);
                break;
        }
    }
}
