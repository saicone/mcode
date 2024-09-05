package com.saicone.mcode.platform;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum PlatformType {

    BUKKIT("plugin.yml"),
    SPIGOT(BUKKIT, "plugin.yml"),
    PAPER(SPIGOT, "paper-plugin.yml"),
    BUNGEECORD("bungee.yml"),
    VELOCITY("velocity-plugin.json");

    private final PlatformType parent;
    private final String fileName;

    PlatformType(@NotNull String fileName) {
        this(null, fileName);
    }

    PlatformType(@Nullable PlatformType parent, @NotNull String fileName) {
        this.parent = parent;
        this.fileName = fileName;
    }

    public boolean isBackend() {
        return !isProxy();
    }

    public boolean isProxy() {
        return this == BUNGEECORD || this == VELOCITY;
    }

    public boolean isChild(@NotNull PlatformType type) {
        if (type == this) {
            return true;
        }
        if (this.parent != null) {
            return this.parent.isChild(type);
        }
        return false;
    }

    @Nullable
    public PlatformType getParent() {
        return parent;
    }

    @NotNull
    public String getFileName() {
        return fileName;
    }
}
