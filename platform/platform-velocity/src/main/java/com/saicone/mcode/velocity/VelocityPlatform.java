package com.saicone.mcode.velocity;

import com.saicone.mcode.Platform;
import com.saicone.mcode.platform.MC;
import com.saicone.mcode.platform.PlatformType;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public class VelocityPlatform extends Platform {

    @NotNull
    public static VelocityPlatform get() {
        return Platform.get();
    }

    private final ProxyServer proxy;

    public VelocityPlatform(@NotNull ProxyServer proxy) {
        super(PlatformType.VELOCITY);
        final ProtocolVersion[] versions = ProtocolVersion.values();
        MC.VERSION = MC.fromString(versions[versions.length - 1].getMostRecentSupportedVersion());
        this.proxy = proxy;
    }

    @Override
    public @NotNull UUID getUserId(@Nullable Object user) {
        if (user instanceof Player) {
            return ((Player) user).getUniqueId();
        }
        return super.getUserId(user);
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
