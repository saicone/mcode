package com.saicone.mcode.paper.env;

import com.saicone.mcode.bukkit.util.ServerInstance;
import com.saicone.mcode.util.concurrent.DelayedExecutor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class PaperExecutor implements DelayedExecutor {

    private final Plugin plugin;

    public PaperExecutor(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(@NotNull Runnable command) {
        if (ServerInstance.Type.MULTITHREADING) {
            Bukkit.getGlobalRegionScheduler().run(this.plugin, task -> command.run());
        } else {
            Bukkit.getScheduler().runTask(this.plugin, command);
        }
    }

    @Override
    public void execute(@NotNull Runnable command, long delay, @NotNull TimeUnit unit) {
        if (ServerInstance.Type.MULTITHREADING) {
            Bukkit.getGlobalRegionScheduler().runDelayed(this.plugin, task -> command.run(), (long) (unit.toMillis(delay) * 0.02));
        } else {
            Bukkit.getScheduler().runTaskLater(this.plugin, command, (long) (unit.toMillis(delay) * 0.02));
        }
    }

    @Override
    public void execute(@NotNull Runnable command, long delay, long period, @NotNull TimeUnit unit) {
        if (ServerInstance.Type.MULTITHREADING) {
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(this.plugin, task -> command.run(), (long) (unit.toMillis(delay) * 0.02), (long) (unit.toMillis(period) * 0.02));
        } else {
            Bukkit.getScheduler().runTaskTimer(this.plugin, command, (long) (unit.toMillis(delay) * 0.02), (long) (unit.toMillis(period) * 0.02));
        }
    }
}
