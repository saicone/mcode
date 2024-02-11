package com.saicone.mcode.velocity;

import com.saicone.mcode.Platform;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class VelocityPlatform extends Platform {

    public static void init(@NotNull ProxyServer proxy) {
        if (INSTANCE == null) {
            new VelocityPlatform(proxy);
        }
    }

    @NotNull
    public static VelocityPlatform get() {
        return Platform.get();
    }

    private final ProxyServer proxy;

    VelocityPlatform(@NotNull ProxyServer proxy) {
        super();
        setInstance(this);
        this.proxy = proxy;
    }

    @Override
    protected void initModules() {
        if (isAvailable("Command")) {
            initModule("com.saicone.mcode.velocity.command.VelocityCommand", "init");
        }
        if (isAvailable("Script")) {
            initModule("com.saicone.mcode.velocity.script.VelocityScripts", "registerActions", "registerConditions");
        }
        if (isAvailable("Settings")) {
            initModule("com.saicone.mcode.velocity.settings.TomlSettingsSource");
        }
    }

    @Override
    public @Nullable String getUserId(@Nullable Object user) {
        if (user instanceof Player) {
            return ((Player) user).getUniqueId().toString();
        }
        return null;
    }

    @Override
    public @NotNull Collection<?> getOnlinePlayers() {
        return proxy.getAllPlayers();
    }

    @NotNull
    public ProxyServer getProxy() {
        return proxy;
    }
}
