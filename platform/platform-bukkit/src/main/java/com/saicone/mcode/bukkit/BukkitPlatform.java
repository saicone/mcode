package com.saicone.mcode.bukkit;

import com.saicone.mcode.Platform;
import com.saicone.mcode.bukkit.util.ServerInstance;
import com.saicone.mcode.platform.PlatformType;
import com.saicone.mcode.platform.Text;
import com.saicone.mcode.util.text.MStrings;
import com.saicone.mcode.platform.MC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public class BukkitPlatform extends Platform {

    public BukkitPlatform() {
        super(ServerInstance.Platform.PAPER ? PlatformType.PAPER : ServerInstance.Platform.SPIGOT ? PlatformType.SPIGOT : PlatformType.BUKKIT);
        MStrings.BUNGEE_HEX = true;
        MC.VERSION = computeVersion();
    }

    @Override
    public @NotNull UUID getUserId(@Nullable Object user) {
        if (user instanceof Entity) {
            return ((Entity) user).getUniqueId();
        }
        return super.getUserId(user);
    }

    @Override
    public @NotNull Text getText(byte type, @Nullable MC version, @NotNull Object object) {
        return BukkitText.valueOf(type, version, object);
    }

    @Override
    public @NotNull Collection<?> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers();
    }

    @NotNull
    private static MC computeVersion() {
        MC version = null;
        try {
            version = MC.fromString(Bukkit.getServer().getBukkitVersion());
        } catch (Throwable t) {
            t.printStackTrace();
        }

        if (version == null) {
            final String serverPackage = Bukkit.getServer().getClass().getPackage().getName();
            if (serverPackage.startsWith("org.bukkit.craftbukkit.v1_")) {
                version = MC.findReverse(MC::bukkitPackage, serverPackage.split("\\.")[3]);
            } else {
                version = MC.findReverse(MC::dataVersion, getDataVersion(serverPackage));
            }
        }

        return version == null ? MC.last() : version;
    }

    private static int getDataVersion(@NotNull String serverPackage) {
        try {
            final Class<?> magicNumbersClass = Class.forName(serverPackage + ".util.CraftMagicNumbers");
            final Object craftMagicNumbers = magicNumbersClass.getDeclaredField("INSTANCE").get(null);
            return (int) magicNumbersClass.getDeclaredMethod("getDataVersion").invoke(craftMagicNumbers);
        } catch (Throwable t) {
            t.printStackTrace();
            return Integer.MAX_VALUE;
        }
    }
}
