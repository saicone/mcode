package com.saicone.mcode.bungee.scheduler;

import com.saicone.mcode.scheduler.Scheduler;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class BungeeScheduler implements Scheduler<ScheduledTask> {

    private final Plugin plugin;

    public BungeeScheduler(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public ScheduledTask runAsync(@NotNull Runnable runnable) {
        return plugin.getProxy().getScheduler().runAsync(plugin, runnable);
    }

    @Override
    public ScheduledTask laterAsync(@NotNull Runnable runnable, long delay, @NotNull TimeUnit unit) {
        return plugin.getProxy().getScheduler().schedule(plugin, runnable, delay, unit);
    }

    @Override
    public ScheduledTask timerAsync(@NotNull Runnable runnable, long delay, long period, @NotNull TimeUnit unit) {
        return plugin.getProxy().getScheduler().schedule(plugin, runnable, delay, period, unit);
    }

    @Override
    public void stop(ScheduledTask task) {
        plugin.getProxy().getScheduler().cancel(task);
    }
}
