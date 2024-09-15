package com.saicone.mcode.bungee.env;

import com.saicone.mcode.util.concurrent.DelayedExecutor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class BungeeExecutor implements DelayedExecutor {

    private final Plugin plugin;

    public BungeeExecutor(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(@NotNull Runnable command) {
        ProxyServer.getInstance().getScheduler().runAsync(this.plugin, command);
    }

    @Override
    public void execute(@NotNull Runnable command, long delay, @NotNull TimeUnit unit) {
        ProxyServer.getInstance().getScheduler().schedule(this.plugin, command, delay, unit);
    }

    @Override
    public void execute(@NotNull Runnable command, long delay, long period, @NotNull TimeUnit unit) {
        ProxyServer.getInstance().getScheduler().schedule(this.plugin, command, delay, period, unit);
    }
}
