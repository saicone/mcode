package com.saicone.mcode.bungee;

import com.saicone.mcode.Platform;
import com.saicone.mcode.util.MStrings;
import net.md_5.bungee.api.ProxyServer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class BungeePlatform extends Platform {

    public BungeePlatform() {
        setInstance(this);
        MStrings.BUNGEE_HEX = true;
    }

    @Override
    public @NotNull Collection<?> getOnlinePlayers() {
        return ProxyServer.getInstance().getPlayers();
    }
}
