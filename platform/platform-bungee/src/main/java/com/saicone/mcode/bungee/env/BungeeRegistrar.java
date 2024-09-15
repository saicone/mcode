package com.saicone.mcode.bungee.env;

import com.saicone.mcode.env.Registrar;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class BungeeRegistrar implements Registrar {

    private final Plugin plugin;

    public BungeeRegistrar(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isPresent(@NotNull String dependency) {
        return ProxyServer.getInstance().getPluginManager().getPlugin(dependency) != null;
    }

    @Override
    public void register(@NotNull Object object) {
        if (object instanceof Listener) {
            ProxyServer.getInstance().getPluginManager().registerListener(this.plugin, (Listener) object);
        }
    }
}
