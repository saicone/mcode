package com.saicone.mcode.bukkit.env;

import com.saicone.mcode.util.concurrent.DelayedExecutor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class BukkitExecutor implements DelayedExecutor {

    private final Plugin plugin;

    public BukkitExecutor(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(@NotNull Runnable command) {
        Bukkit.getScheduler().runTask(this.plugin, command);
    }

    @Override
    public void execute(@NotNull Runnable command, long delay, @NotNull TimeUnit unit) {
        Bukkit.getScheduler().runTaskLater(this.plugin, command, (long) (unit.toMillis(delay) * 0.02));
    }

    @Override
    public void execute(@NotNull Runnable command, long delay, long period, @NotNull TimeUnit unit) {
        Bukkit.getScheduler().runTaskTimer(this.plugin, command, (long) (unit.toMillis(delay) * 0.02), (long) (unit.toMillis(period) * 0.02));
    }
}
