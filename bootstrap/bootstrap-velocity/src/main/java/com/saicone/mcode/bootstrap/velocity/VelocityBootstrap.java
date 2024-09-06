package com.saicone.mcode.bootstrap.velocity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.saicone.mcode.Plugin;
import com.saicone.mcode.bootstrap.Addon;
import com.saicone.mcode.bootstrap.Bootstrap;
import com.saicone.mcode.velocity.VelocityPlatform;
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

    private final Logger logger;
    private final Path folder;
    private final Set<Addon> addons;
    private final Plugin plugin;

    @Inject
    public VelocityBootstrap(ProxyServer proxy, Logger logger, @DataDirectory Path folder) {
        // Initialization

        // Replace logger with Bukkit logger
        getLibraryLoader().logger((level, msg) -> {
            if (msg.contains("Ezlib is already initialized")) {
                return;
            }
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

        // Put platform addons
        this.addons.add(Addon.PLATFORM_VELOCITY);

        // Load addon libraries
        for (Addon addon : this.addons) {
            getLibraryLoader().loadDependency(addon.dependency());
        }
        getLibraryLoader().load();

        // Initialize addons
        new VelocityPlatform(proxy);
        initAddons();

        // Load plugin
        this.plugin = loadPlugin(pluginClass);
    }

    private void initAddons() {
        try {
            if (this.addons.contains(Addon.MODULE_SCRIPT)) {
                Class.forName("com.saicone.mcode.velocity.script.VelocityScripts");
            }
            if (this.addons.contains(Addon.LIBRARY_SETTINGS)) {
                Class.forName("com.saicone.mcode.velocity.settings.TomlSettingsSource");
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Subscribe
    public void onInit(ProxyInitializeEvent e) {
        // Load
        this.plugin.onLoad();

        // Enable
        this.plugin.onEnable();
    }

    @Subscribe
    public void onShut(ProxyShutdownEvent e) {
        // Disable
        this.plugin.onDisable();
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
