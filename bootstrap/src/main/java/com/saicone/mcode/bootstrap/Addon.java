package com.saicone.mcode.bootstrap;

import com.saicone.ezlib.EzlibLoader;
import com.saicone.mcode.env.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum Addon {

    // Common
    COMMON(mcode("common"), "com.saicone.mcode.Platform"),

    // Modules
    MODULE_COMMAND(mcode("module-command"), "com.saicone.mcode.module.command.CommandNode"),
    MODULE_COMMAND_BRIGADIER(mcode("module-command-brigadier"), "com.saicone.mcode.module.command.brigadier.BrigadierCommandBuilder"),
    MODULE_ENV(mcode("module-env"), "com.saicone.mcode.env.Env"),
    MODULE_LANG(mcode("module-lang"), "com.saicone.mcode.module.lang.AbstractLang"),
    MODULE_LANG_ADVENTURE(mcode("module-lang-adventure"), "com.saicone.mcode.module.lang.AdventureLang"),
    MODULE_SCRIPT(mcode("module-script"), "com.saicone.mcode.module.script.Script"),
    MODULE_SCRIPT_JS(mcode("module-script-js")),
    MODULE_TASK(mcode("module-task"), "com.saicone.mcode.module.task.Scheduler"),

    // Extensions
    EXTENSION_CACHE(mcode("extension-cache"), "com.saicone.mcode.util.cache.Cache"),
    EXTENSION_LOOKUP(mcode("extension-lookup"), "com.saicone.mcode.util.invoke.EasyLookup"),

    // Platforms
    PLATFORM_BUKKIT(mcode("platform-bukkit"), "com.saicone.mcode.bukkit.BukkitPlatform"),
    PLATFORM_BUNGEE(mcode("platform-bungee"), "com.saicone.mcode.bungee.BungeePlatform"),
    PLATFORM_PAPER(mcode("platform-paper"), "com.saicone.mcode.paper.scheduler.PaperScheduler"),
    PLATFORM_VELOCITY(mcode("platform-velocity"), "com.saicone.mcode.velocity.VelocityPlatform"),

    // Libraries
    LIBRARY_DELIVERY4J(library("com{}saicone{}delivery4j", "delivery4j", Constants.DELIVERY4J_VERSION),
            "com.saicone.delivery4j.Broker"
    ),
    LIBRARY_SETTINGS(library("com{}saicone{}settings", "settings", Constants.SETTINGS_VERSION),
            "com.saicone.settings.Settings"
    ),
    LIBRARY_TYPES(library("com{}saicone", "types", Constants.TYPES_VERSION),
            "com.saicone.types.Types"
    );

    public static final Addon[] VALUES = values();
    public static final Map<String, String> RELOCATIONS;

    static {
        final Map<String, String> map = new HashMap<>() {
            @Override
            public String put(String key, String value) {
                return super.put(key.replace("{}", "."), value);
            }
        };

        map.put("com{}saicone{}mcode", "com.saicone.mcode");
        map.put("com{}saicone{}delivery4j", "com.saicone.delivery4j");
        map.put("com{}saicone{}ezlib", "com.saicone.ezlib");
        map.put("com{}saicone{}settings", "com.saicone.settings");
        map.put("com{}saicone{}types", "com.saicone.types");
        map.put("com{}saicone{}nbt", "com.saicone.nbt");

        map.put("com{}google{}gson", "com.google.gson");
        map.put("com{}mojang{}brigadier", "com.mojang.brigadier");
        map.put("net{}kyori{}adventure", "net.kyori.adventure");
        map.put("com{}github{}benmanes{}caffeine{}cache", "com.github.benmanes.caffeine.cache");
        map.put("com{}google{}common{}cache", "com.google.common.cache");
        map.put("com{}google{}common", "com.google.common");

        RELOCATIONS = map;
    }

    private final EzlibLoader.Dependency dependency;

    Addon(@NotNull String path, @NotNull String... test) {
        this.dependency = new EzlibLoader.Dependency()
                .path(path.replace("{}", "."))
                .repository("https://jitpack.io/")
                .test(test);
    }

    public boolean isModule() {
        switch (this) {
            case MODULE_COMMAND:
            case MODULE_COMMAND_BRIGADIER:
            case MODULE_ENV:
            case MODULE_LANG:
            case MODULE_LANG_ADVENTURE:
            case MODULE_SCRIPT:
            case MODULE_SCRIPT_JS:
            case MODULE_TASK:
                return true;
            default:
                return false;
        }
    }

    public boolean isExtension() {
        switch (this) {
            case EXTENSION_CACHE:
            case EXTENSION_LOOKUP:
                return true;
            default:
                return false;
        }
    }

    public boolean isPlatform() {
        switch (this) {
            case PLATFORM_BUKKIT:
            case PLATFORM_BUNGEE:
            case PLATFORM_PAPER:
            case PLATFORM_VELOCITY:
                return true;
            default:
                return false;
        }
    }

    public boolean isLibrary() {
        switch (this) {
            case LIBRARY_DELIVERY4J:
            case LIBRARY_SETTINGS:
            case LIBRARY_TYPES:
                return true;
            default:
                return false;
        }
    }

    @NotNull
    public EzlibLoader.Dependency dependency() {
        return dependency;
    }

    @Nullable
    public static Addon of(@NotNull String name) {
        for (Addon addon : VALUES) {
            if (addon.name().equalsIgnoreCase(name)) {
                return addon;
            }
        }
        return null;
    }

    @NotNull
    private static String mcode(@NotNull String name) {
        return library(Constants.GROUP, name, Constants.VERSION);
    }

    @NotNull
    private static String library(@NotNull String group, @NotNull String name, @NotNull String version) {
        return group + ":" + name + ":" + version;
    }
}
