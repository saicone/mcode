package com.saicone.mcode.bungee;

import com.saicone.mcode.Platform;
import com.saicone.mcode.util.MStrings;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class BungeePlatform extends Platform {

    public static void init() {
        if (INSTANCE == null) {
            new BungeePlatform();
        }
    }

    BungeePlatform() {
        super();
        setInstance(this);
        MStrings.BUNGEE_HEX = true;
    }

    @Override
    protected void initModules() {
        if (isAvailable("Script")) {
            initModule("com.saicone.mcode.bungee.script.BungeeScripts", "registerActions", "registerConditions");
        }
        if (isAvailable("Settings")) {
            initModule("com.saicone.mcode.bungee.settings.YamlParser", "register");
        }
    }

    @Override
    public @Nullable String getUserId(@Nullable Object user) {
        if (user instanceof CommandSender) {
            if (user instanceof ProxiedPlayer) {
                return ((ProxiedPlayer) user).getUniqueId().toString();
            }
            return ((CommandSender) user).getName();
        }
        return null;
    }

    @Override
    public @NotNull Collection<?> getOnlinePlayers() {
        return ProxyServer.getInstance().getPlayers();
    }
}
