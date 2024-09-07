package com.saicone.mcode.bukkit.scheduler;

import com.saicone.mcode.module.task.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class BukkitScheduler implements Scheduler<BukkitTask> {

    private final Plugin plugin;

    public BukkitScheduler(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    public long ticks(long duration, @NotNull TimeUnit unit) {
        return (long) (unit.toMillis(duration) * 0.02);
    }

    @Override
    public BukkitTask run(@NotNull Runnable runnable) {
        return Bukkit.getScheduler().runTask(plugin, runnable);
    }

    @Override
    public BukkitTask later(@NotNull Runnable runnable, long delay, @NotNull TimeUnit unit) {
        return Bukkit.getScheduler().runTaskLater(plugin, runnable, ticks(delay, unit));
    }

    @Override
    public BukkitTask timer(@NotNull Runnable runnable, long delay, long period, @NotNull TimeUnit unit) {
        return Bukkit.getScheduler().runTaskTimer(plugin, runnable, ticks(delay, unit), ticks(period, unit));
    }

    @Override
    public BukkitTask runAsync(@NotNull Runnable runnable) {
        return Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    @Override
    public BukkitTask laterAsync(@NotNull Runnable runnable, long delay, @NotNull TimeUnit unit) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, ticks(delay, unit));
    }

    @Override
    public BukkitTask timerAsync(@NotNull Runnable runnable, long delay, long period, @NotNull TimeUnit unit) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, ticks(delay, unit), ticks(period, unit));
    }

    @Override
    public void stop(BukkitTask task) {
        task.cancel();
    }

    @Override
    public boolean isMainThread() {
        return Bukkit.isPrimaryThread();
    }
}
