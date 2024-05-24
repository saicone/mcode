package com.saicone.mcode.velocity.scheduler;

import com.saicone.mcode.scheduler.Scheduler;
import com.saicone.mcode.velocity.VelocityPlatform;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class VelocityScheduler implements Scheduler<ScheduledTask> {

    private final ProxyServer proxy;
    private final Object plugin;

    public VelocityScheduler(@NotNull Object plugin) {
        this(VelocityPlatform.get().getProxy(), plugin);
    }

    public VelocityScheduler(@NotNull ProxyServer proxy, @NotNull Object plugin) {
        this.proxy = proxy;
        this.plugin = plugin;
    }

    @NotNull
    public Object getPlugin() {
        return plugin;
    }

    @Override
    public ScheduledTask runAsync(@NotNull Runnable runnable) {
        return proxy.getScheduler().buildTask(plugin, runnable).schedule();
    }

    @Override
    public ScheduledTask laterAsync(@NotNull Runnable runnable, long delay, @NotNull TimeUnit unit) {
        return proxy.getScheduler().buildTask(plugin, runnable).delay(delay, unit).schedule();
    }

    @Override
    public ScheduledTask timerAsync(@NotNull Runnable runnable, long delay, long period, @NotNull TimeUnit unit) {
        return proxy.getScheduler().buildTask(plugin, runnable).repeat(period, unit).schedule();
    }

    @Override
    public void stop(ScheduledTask task) {
        task.cancel();
    }
}
