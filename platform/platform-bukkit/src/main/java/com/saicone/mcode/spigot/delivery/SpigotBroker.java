package com.saicone.mcode.spigot.delivery;

import com.google.common.collect.Iterables;
import com.saicone.delivery4j.Broker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class SpigotBroker extends Broker implements PluginMessageListener {

    private final Plugin plugin;

    @NotNull
    public static SpigotBroker of(@NotNull Plugin plugin) {
        return new SpigotBroker(plugin);
    }

    public SpigotBroker(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onStart() {
        for (String subscribedChannel : getSubscribedChannels()) {
            registerPluginChannel(subscribedChannel);
        }
    }

    @Override
    public void onClose() {
        for (String subscribedChannel : getSubscribedChannels()) {
            unregisterPluginChannel(subscribedChannel);
        }
    }

    @Override
    public void onSubscribe(@NotNull String... channels) {
        for (String channel : channels) {
            registerPluginChannel(channel);
        }
    }

    @Override
    public void onUnsubscribe(@NotNull String... channels) {
        for (String channel : channels) {
            unregisterPluginChannel(channel);
        }
    }

    @Override
    public void onSend(@NotNull String channel, byte[] data) {
        new BukkitRunnable() {
            @Override
            public void run() {
                final Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
                if (player == null) {
                    return;
                }

                player.sendPluginMessage(plugin, channel, data);
            }
        }.runTaskTimer(plugin, 1L, 80L);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        if (getSubscribedChannels().contains(channel)) {
            try {
                receive(channel, message);
            } catch (IOException e) {
                getLogger().log(2, "Cannot process received message from channel '" + channel + "'", e);
            }
        }
    }

    @NotNull
    public Plugin getPlugin() {
        return plugin;
    }

    protected void registerPluginChannel(@NotNull String channel) {
        Bukkit.getServer().getMessenger().registerIncomingPluginChannel(plugin, channel, this);
        if (!Bukkit.getServer().getMessenger().isOutgoingChannelRegistered(plugin, channel)) {
            Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(plugin, channel);
        }
    }

    protected void unregisterPluginChannel(@NotNull String channel) {
        Bukkit.getServer().getMessenger().unregisterIncomingPluginChannel(plugin, channel, this);
        Bukkit.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin, channel);
    }
}
