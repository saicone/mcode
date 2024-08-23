package com.saicone.mcode.bungee;

import com.saicone.mcode.Platform;
import com.saicone.mcode.util.MStrings;
import com.saicone.mcode.platform.MinecraftVersion;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.ProtocolConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
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
        // Ser current version
        MinecraftVersion version = MinecraftVersion.VALUES[MinecraftVersion.VALUES.length - 1];
        final Field[] fields = ProtocolConstants.class.getDeclaredFields();
        for (int i = fields.length; i-- > 0; ) {
            final Field field = fields[i];
            if (field.getName().startsWith("MINECRAFT_")) {
                version = MinecraftVersion.fromString(field.getName().substring(10).replace('_', '.'));
                break;
            }
        }
        MinecraftVersion.SERVER = version;
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
