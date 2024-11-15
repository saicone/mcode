package com.saicone.mcode.bukkit;

import com.saicone.mcode.Platform;
import com.saicone.mcode.bukkit.util.ServerInstance;
import com.saicone.mcode.platform.PlatformType;
import com.saicone.mcode.util.MStrings;
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
        MC.VERSION = MC.fromString(Bukkit.getServer().getBukkitVersion());
    }

    @Override
    public @NotNull UUID getUserId(@Nullable Object user) {
        if (user instanceof Entity) {
            return ((Entity) user).getUniqueId();
        }
        return super.getUserId(user);
    }

    @Override
    public @NotNull BukkitText getText(@NotNull String s) {
        return new BukkitText(s);
    }

    @Override
    public @NotNull Collection<?> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers();
    }
}
