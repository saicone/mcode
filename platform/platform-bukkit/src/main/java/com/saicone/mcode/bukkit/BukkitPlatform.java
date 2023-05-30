package com.saicone.mcode.bukkit;

import com.saicone.mcode.Platform;
import com.saicone.mcode.util.MStrings;
import org.jetbrains.annotations.NotNull;

public class BukkitPlatform extends Platform {

    public BukkitPlatform() {
        setInstance(this);
        MStrings.BUNGEE_HEX = true;
    }

    @Override
    public @NotNull BukkitText getText(@NotNull String s) {
        return new BukkitText(s);
    }
}
