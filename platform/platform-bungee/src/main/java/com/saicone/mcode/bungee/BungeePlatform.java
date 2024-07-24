package com.saicone.mcode.bungee;

import com.saicone.mcode.Platform;
import com.saicone.mcode.util.MStrings;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public class BungeePlatform extends Platform {

    public static void init() {
        if (INSTANCE == null) {
            new BungeePlatform();
        }
    }

    @NotNull
    public static BungeePlatform get() {
        return Platform.get();
    }

    BungeePlatform() {
        super();
        setInstance(this);
        MStrings.BUNGEE_HEX = true;
    }

    @Override
    protected void initModules() {
        if (isAvailable("Command")) {
            initModule("com.saicone.mcode.bungee.command.BungeeCommand", "init");
        }
        if (isAvailable("Script")) {
            initModule("com.saicone.mcode.bungee.script.BungeeScripts", "registerActions", "registerConditions");
        }
        if (isAvailable("Settings")) {
            initModule("com.saicone.mcode.bungee.settings.BungeeYamlSource");
        }
    }

    @Override
    public @NotNull UUID getUserId(@Nullable Object user) {
        if (user instanceof ProxiedPlayer) {
            return ((ProxiedPlayer) user).getUniqueId();
        }
        return super.getUserId(user);
    }

    @Override
    public @NotNull Collection<?> getOnlinePlayers() {
        return ProxyServer.getInstance().getPlayers();
    }
}
