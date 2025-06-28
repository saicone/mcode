package com.saicone.mcode.bungee;

import com.saicone.mcode.Platform;
import com.saicone.mcode.platform.PlatformType;
import com.saicone.mcode.platform.Text;
import com.saicone.mcode.util.text.MStrings;
import com.saicone.mcode.platform.MC;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.ProtocolConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.UUID;

public class BungeePlatform extends Platform {

    @NotNull
    public static BungeePlatform get() {
        return Platform.get();
    }

    public BungeePlatform() {
        super(PlatformType.BUNGEECORD);
        MStrings.BUNGEE_HEX = true;
        // Ser current version
        MC version = MC.VALUES[MC.VALUES.length - 1];
        final Field[] fields = ProtocolConstants.class.getDeclaredFields();
        for (int i = fields.length; i-- > 0; ) {
            final Field field = fields[i];
            if (field.getName().startsWith("MINECRAFT_")) {
                version = MC.fromString(field.getName().substring(10).replace('_', '.'));
                break;
            }
        }
        MC.VERSION = version;
    }

    @Override
    public @NotNull UUID getUserId(@Nullable Object user) {
        if (user instanceof ProxiedPlayer) {
            return ((ProxiedPlayer) user).getUniqueId();
        }
        return super.getUserId(user);
    }

    @Override
    public @NotNull Text getText(byte type, @Nullable MC version, @NotNull Object object) {
        return BungeeText.valueOf(type, version, object);
    }

    @Override
    public @NotNull Collection<?> getOnlinePlayers() {
        return ProxyServer.getInstance().getPlayers();
    }
}
