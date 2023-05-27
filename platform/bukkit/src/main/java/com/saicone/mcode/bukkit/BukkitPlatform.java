package com.saicone.mcode.bukkit;

import com.saicone.mcode.Platform;
import org.jetbrains.annotations.NotNull;

public class BukkitPlatform extends Platform {

    public BukkitPlatform() {
        setInstance(this);
    }

    @Override
    public @NotNull BukkitText getText(@NotNull String s) {
        return new BukkitText(s);
    }
}
