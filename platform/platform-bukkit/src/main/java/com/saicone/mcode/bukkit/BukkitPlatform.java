package com.saicone.mcode.bukkit;

import com.saicone.mcode.Platform;
import com.saicone.mcode.util.MStrings;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class BukkitPlatform extends Platform {

    public static void init() {
        if (INSTANCE == null) {
            new BukkitPlatform();
        }
    }

    @NotNull
    public static BukkitPlatform get() {
        return Platform.get();
    }

    BukkitPlatform() {
        super();
        setInstance(this);
        MStrings.BUNGEE_HEX = true;
    }

    @Override
    protected void initModules() {
        if (isAvailable("Command")) {
            initModule("com.saicone.mcode.bukkit.command.BukkitCommand", "init");
        }
        if (isAvailable("Script")) {
            initModule("com.saicone.mcode.bukkit.script.BukkitScripts", "registerActions", "registerConditions");
        }
        if (isAvailable("Settings")) {
            initModule("com.saicone.mcode.bukkit.settings.BukkitYamlSource", "register");
        }
        if (isAvailable("Lookup")) {
            initModule("com.saicone.mcode.bukkit.util.BukkitLookup");
        }
    }

    @Override
    public @Nullable String getUserId(@Nullable Object user) {
        if (user instanceof CommandSender) {
            if (user instanceof Entity) {
                return ((Entity) user).getUniqueId().toString();
            }
            return ((CommandSender) user).getName();
        }
        return null;
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
