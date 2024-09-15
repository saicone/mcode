package com.saicone.mcode.velocity.env;

import com.saicone.mcode.util.concurrent.DelayedExecutor;
import com.velocitypowered.api.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class VelocityExecutor implements DelayedExecutor {

    private final ProxyServer proxy;
    private final Object plugin;

    public VelocityExecutor(@NotNull ProxyServer proxy, @NotNull Object plugin) {
        this.proxy = proxy;
        this.plugin = plugin;
    }

    @Override
    public void execute(@NotNull Runnable command) {
        this.proxy.getScheduler().buildTask(this.plugin, command).schedule();
    }

    @Override
    public void execute(@NotNull Runnable command, long delay, @NotNull TimeUnit unit) {
        this.proxy.getScheduler().buildTask(this.plugin, command).delay(delay, unit).schedule();
    }

    @Override
    public void execute(@NotNull Runnable command, long delay, long period, @NotNull TimeUnit unit) {
        this.proxy.getScheduler().buildTask(this.plugin, command).repeat(period, unit).schedule();
    }
}
