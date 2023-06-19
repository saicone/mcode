package com.saicone.mcode.velocity.delivery;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.saicone.mcode.module.delivery.DeliveryClient;
import com.saicone.mcode.velocity.VelocityPlatform;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class VelocityDelivery extends DeliveryClient {

    private final ProxyServer proxy;
    private final Object plugin;
    private final Set<String> forward;

    @NotNull
    @Contract("_ -> new")
    public static VelocityDelivery of(@NotNull Object plugin) {
        return new VelocityDelivery(plugin, new HashSet<>());
    }

    @NotNull
    @Contract("_, _ -> new")
    public static VelocityDelivery of(@NotNull Object plugin, String... forward) {
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
        return new VelocityDelivery(plugin, set);
    }

    public VelocityDelivery(@NotNull Object plugin, @NotNull Set<String> forward) {
        this(VelocityPlatform.get().getProxy(), plugin, forward);
    }

    public VelocityDelivery(@NotNull ProxyServer proxy, @NotNull Object plugin, @NotNull Set<String> forward) {
        this.proxy = proxy;
        this.plugin = plugin;
        this.forward = forward;
    }

    @Override
    public void onStart() {
        for (String channel : subscribedChannels) {
            registerChannel(channel);
        }
        proxy.getEventManager().register(plugin, this);
    }

    @Override
    public void onClose() {
        proxy.getEventManager().unregisterListener(plugin, this);
        for (String channel : subscribedChannels) {
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
        final LegacyChannelIdentifier id = new LegacyChannelIdentifier(channel);
        for (RegisteredServer server : proxy.getAllServers()) {
            if (all || forward.contains(server.getServerInfo().getName())) {
                server.sendPluginMessage(id, data);
            }
        }
    }

    @Subscribe
    @SuppressWarnings("all")
    public void onPluginMessageReceived(PluginMessageEvent e) {
        if (!e.getResult().isAllowed() || !subscribedChannels.contains(e.getIdentifier().getId())) {
            return;
        }
        final boolean isMain = isMainChannel(e.getIdentifier().getId());
        if (!isMain) {
            e.setResult(PluginMessageEvent.ForwardResult.handled());
        }

        if (e.getSource() instanceof Player) {
            return;
        }

        if (isMain) {
            final ByteArrayDataInput in = ByteStreams.newDataInput(e.getData());
            final String subChannel = in.readUTF();
            if ("Forward".equals(subChannel)) {
                final String server = in.readUTF();
                final String channel = in.readUTF();
                if (subscribedChannels.contains(channel)) {
                    final byte[] data = new byte[in.readShort()];
                    in.readFully(data);
                    receive(channel, data);
                }
            }
            return;
        } else {
            receive(e.getIdentifier().getId(), e.getData());
        }

        if (forward.isEmpty()) {
            return;
        }

        final boolean all = forward.contains("*");
        for (RegisteredServer server : proxy.getAllServers()) {
            if (all || forward.contains(server.getServerInfo().getName())) {
                server.sendPluginMessage(e.getIdentifier(), e.getData());
            }
        }
    }

    @NotNull
    public Object getPlugin() {
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
        proxy.getChannelRegistrar().register(new LegacyChannelIdentifier(channel));
    }

    protected void unregisterChannel(@NotNull String channel) {
        if (isMainChannel(channel)) {
            return;
        }
        proxy.getChannelRegistrar().unregister(new LegacyChannelIdentifier(channel));
    }
}
