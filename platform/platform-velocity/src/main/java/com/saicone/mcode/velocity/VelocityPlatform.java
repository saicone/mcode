package com.saicone.mcode.velocity;

import com.google.inject.Inject;
import com.saicone.mcode.Platform;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class VelocityPlatform extends Platform {

    public static void init() {
        if (INSTANCE == null) {
            new VelocityPlatform();
        }
    }

    @Inject
    private ProxyServer proxy;

    VelocityPlatform() {
        super();
        setInstance(this);
    }

    @Override
    protected void initModules() {
        if (isAvailable("Script")) {
            initModule("com.saicone.mcode.velocity.script.VelocityScripts", "registerActions", "registerConditions");
        }
        if (isAvailable("Settings")) {
            initModule("com.saicone.mcode.velocity.settings.TomlParser", "register");
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
}
