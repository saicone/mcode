package com.saicone.mcode.bukkit.env;

import com.saicone.mcode.env.Registrar;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class BukkitRegistrar implements Registrar {

    private final Plugin plugin;

    public BukkitRegistrar(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isPresent(@NotNull String dependency) {
        return Bukkit.getPluginManager().isPluginEnabled(dependency);
    }

    @Override
    public void register(@NotNull Object object) {
        if (object instanceof Listener) {
            Bukkit.getPluginManager().registerEvents((Listener) object, this.plugin);
        }
    }
}
