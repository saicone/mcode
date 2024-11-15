package com.saicone.mcode.bungee.delivery;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.saicone.delivery4j.Broker;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BungeeBroker extends Broker implements Listener {

    private final Plugin plugin;
    private final Set<String> forward;

    @NotNull
    @Contract("_ -> new")
    public static BungeeBroker of(@NotNull Plugin plugin) {
        return new BungeeBroker(plugin, new HashSet<>());
    }

    @NotNull
    @Contract("_, _ -> new")
    public static BungeeBroker of(@NotNull Plugin plugin, String... forward) {
        final Set<String> set = new HashSet<>();
        for (String s : forward) {
            if (s.equals("*")) {
                set.clear();
                set.add("*");
                break;
            } else {
                set.add(s);
            }
        }
        return new BungeeBroker(plugin, set);
    }

    public BungeeBroker(@NotNull Plugin plugin, @NotNull Set<String> forward) {
        this.plugin = plugin;
        this.forward = forward;
    }

    @Override
    public void onStart() {
        for (String channel : getSubscribedChannels()) {
            registerChannel(channel);
        }
        plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    @Override
    public void onClose() {
        plugin.getProxy().getPluginManager().unregisterListener(this);
        for (String channel : getSubscribedChannels()) {
            unregisterChannel(channel);
        }
    }

    @Override
    public void onSubscribe(@NotNull String... channels) {
        for (String channel : channels) {
            registerChannel(channel);
        }
    }

    @Override
    public void onUnsubscribe(@NotNull String... channels) {
        for (String channel : channels) {
            unregisterChannel(channel);
        }
    }

    @Override
    public void onSend(@NotNull String channel, byte[] data) {
        final boolean all = forward.contains("*");
        for (Map.Entry<String, ServerInfo> entry : plugin.getProxy().getServers().entrySet()) {
            if (all || forward.contains(entry.getKey())) {
                entry.getValue().sendData(channel, data);
            }
        }
    }

    @EventHandler
    @SuppressWarnings("all")
    public void onPluginMessageReceived(PluginMessageEvent event) {
        if (event.isCancelled() || !getSubscribedChannels().contains(event.getTag())) {
            return;
        }
        final boolean isMain = isMainChannel(event.getTag());
        if (!isMain) {
            event.setCancelled(true);
        }

        if (event.getSender() instanceof ProxiedPlayer) {
            return;
        }

        if (isMain) {
            final ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
            final String subChannel = in.readUTF();
            if ("Forward".equals(subChannel)) {
                final String server = in.readUTF();
                final String channel = in.readUTF();
                if (getSubscribedChannels().contains(channel)) {
                    final byte[] data = new byte[in.readShort()];
                    in.readFully(data);
                    try {
                        receive(channel, data);
                    } catch (IOException e) {
                        getLogger().log(2, "Cannot process received message from channel '" + channel + "'", e);
                    }
                }
            }
            return;
        } else {
            try {
                receive(event.getTag(), event.getData());
            } catch (IOException e) {
                getLogger().log(2, "Cannot process received message from channel '" + event.getTag() + "'", e);
            }
        }

        if (forward.isEmpty()) {
            return;
        }

        final boolean all = forward.contains("*");
        for (Map.Entry<String, ServerInfo> entry : plugin.getProxy().getServers().entrySet()) {
            if (all || forward.contains(entry.getKey())) {
                entry.getValue().sendData(event.getTag(), event.getData());
            }
        }
    }

    @NotNull
    public Plugin getPlugin() {
        return plugin;
    }

    @NotNull
    public Set<String> getForward() {
        return forward;
    }

    protected boolean isMainChannel(@NotNull String channel) {
        return channel.equals("BungeeCord") || channel.equals("bungeecord:main");
    }

    protected void registerChannel(@NotNull String channel) {
        if (isMainChannel(channel)) {
            return;
        }
        if (!plugin.getProxy().getChannels().contains(channel)) {
            plugin.getProxy().registerChannel(channel);
        }
    }

    protected void unregisterChannel(@NotNull String channel) {
        if (isMainChannel(channel)) {
            return;
        }
        plugin.getProxy().unregisterChannel(channel);
    }
}
