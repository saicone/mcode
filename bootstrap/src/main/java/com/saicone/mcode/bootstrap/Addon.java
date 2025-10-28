package com.saicone.mcode.bootstrap;

import com.saicone.ezlib.EzlibLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum Addon {

    // Common
    COMMON("${mcode_group}:common:${mcode_version}",
            "com.saicone.mcode.Platform"
    ),

    // Modules
    MODULE_COMMAND("${mcode_group}:module-command:${mcode_version}",
            "com.saicone.mcode.module.command.CommandNode"
    ),
    MODULE_COMMAND_BRIGADIER("${mcode_group}:module-command-brigadier:${mcode_version}",
            "com.saicone.mcode.module.command.brigadier.BrigadierCommandBuilder"
    ),
    MODULE_ENV("${mcode_group}:module-env:${mcode_version}",
            "com.saicone.mcode.env.Env"
    ),
    MODULE_LANG("${mcode_group}:module-lang:${mcode_version}",
            "com.saicone.mcode.module.lang.AbstractLang"
    ),
    MODULE_LANG_ADVENTURE("${mcode_group}:module-lang-adventure:${mcode_version}",
            "com.saicone.mcode.module.lang.AdventureLang"
    ),
    MODULE_SCRIPT("${mcode_group}:module-script:${mcode_version}",
            "com.saicone.mcode.module.script.Script"
    ),
    MODULE_SCRIPT_JS("${mcode_group}:module-script-js:${mcode_version}"),
    MODULE_TASK("${mcode_group}:module-task:${mcode_version}",
            "com.saicone.mcode.module.task.Scheduler"
    ),

    // Extensions
    EXTENSION_CACHE("${mcode_group}:extension-cache:${mcode_version}",
            "com.saicone.mcode.util.cache.Cache"
    ),
    EXTENSION_LOOKUP("${mcode_group}:extension-lookup:${mcode_version}",
            "com.saicone.mcode.util.invoke.EasyLookup"
    ),

    // Platforms
    PLATFORM_BUKKIT("${mcode_group}:platform-bukkit:${mcode_version}",
            "com.saicone.mcode.bukkit.BukkitPlatform"
    ),
    PLATFORM_BUNGEE("${mcode_group}:platform-bungee:${mcode_version}",
            "com.saicone.mcode.bungee.BungeePlatform"
    ),
    PLATFORM_PAPER("${mcode_group}:platform-paper:${mcode_version}",
            "com.saicone.mcode.paper.scheduler.PaperScheduler"
    ),
    PLATFORM_VELOCITY("${mcode_group}:platform-velocity:${mcode_version}",
            "com.saicone.mcode.velocity.VelocityPlatform"
    ),

    // Libraries
    LIBRARY_DELIVERY4J("com{}saicone{}delivery4j:delivery4j:${delivery4j_version}",
            "com.saicone.delivery4j.Broker"
    ),
    LIBRARY_SETTINGS("com{}saicone{}settings:settings:${settings_version}",
            "com.saicone.settings.Settings"
    ),
    LIBRARY_TYPES("com{}saicone:types:${types_version}",
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
}
