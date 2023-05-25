package com.saicone.mcode.bungee.scheduler;

import com.saicone.mcode.scheduler.Scheduler;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class BungeeScheduler implements Scheduler<Integer> {

    private final Plugin plugin;

    public BungeeScheduler(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Integer async(@NotNull Runnable runnable) {
        return plugin.getProxy().getScheduler().runAsync(plugin, runnable).getId();
    }

    @Override
    public Integer asyncLater(@NotNull Runnable runnable, long delay, @NotNull TimeUnit unit) {
        return plugin.getProxy().getScheduler().schedule(plugin, runnable, delay, unit).getId();
    }

    @Override
    public Integer asyncTimer(@NotNull Runnable runnable, long delay, long period, @NotNull TimeUnit unit) {
        return plugin.getProxy().getScheduler().schedule(plugin, runnable, delay, period, unit).getId();
    }

    @Override
    public void stop(Integer id) {
        plugin.getProxy().getScheduler().cancel(id);
    }
}
