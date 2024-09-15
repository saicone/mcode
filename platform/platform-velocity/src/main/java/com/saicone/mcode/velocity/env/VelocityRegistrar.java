package com.saicone.mcode.velocity.env;

import com.saicone.mcode.env.Registrar;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public class VelocityRegistrar implements Registrar {

    private final ProxyServer proxy;
    private final Object plugin;

    public VelocityRegistrar(@NotNull ProxyServer proxy, @NotNull Object plugin) {
        this.proxy = proxy;
        this.plugin = plugin;
    }

    @Override
    public boolean isPresent(@NotNull String dependency) {
        return this.proxy.getPluginManager().isLoaded(dependency);
    }

    @Override
    public void register(@NotNull Object object) {
        for (Method method : object.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Subscribe.class)) {
                this.proxy.getEventManager().register(this.plugin, object);
                break;
            }
        }
    }
}
