package com.saicone.mcode.velocity.scheduler;

import com.google.inject.Inject;
import com.saicone.mcode.scheduler.Scheduler;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class VelocityScheduler implements Scheduler<ScheduledTask> {

    @Inject
    private ProxyServer proxy;
    private final Object plugin;

    public VelocityScheduler(@NotNull Object plugin) {
        this.plugin = plugin;
    }

    @NotNull
    public Object getPlugin() {
        return plugin;
    }

    @Override
    public ScheduledTask async(@NotNull Runnable runnable) {
        return proxy.getScheduler().buildTask(plugin, runnable).schedule();
    }

    @Override
    public ScheduledTask asyncLater(@NotNull Runnable runnable, long delay, @NotNull TimeUnit unit) {
        return proxy.getScheduler().buildTask(plugin, runnable).delay(delay, unit).schedule();
    }

    @Override
    public ScheduledTask asyncTimer(@NotNull Runnable runnable, long delay, long period, @NotNull TimeUnit unit) {
        return proxy.getScheduler().buildTask(plugin, runnable).repeat(period, unit).schedule();
    }

    @Override
    public void stop(ScheduledTask task) {
        task.cancel();
    }
}
