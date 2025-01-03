package com.saicone.mcode.spigot.delivery;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class SpigotForwardBroker extends SpigotBroker {

    private static final String BUNGEECORD_CHANNEL = "BungeeCord";

    private final String preChannel;

    @NotNull
    public static SpigotForwardBroker of(@NotNull Plugin plugin) {
        return of(plugin, BUNGEECORD_CHANNEL);
    }

    @NotNull
    public static SpigotForwardBroker of(@NotNull Plugin plugin, @NotNull String preChannel) {
        return new SpigotForwardBroker(plugin, preChannel);
    }

    public SpigotForwardBroker(@NotNull Plugin plugin, @NotNull String preChannel) {
        super(plugin);
        this.preChannel = preChannel;
    }

    @Override
    public void onStart() {
        registerPluginChannel(preChannel);
    }

    @Override
    public void onClose() {
        unregisterPluginChannel(preChannel);
    }

    @Override
    public void onSubscribe(@NotNull String... channels) {
    }

    @Override
    public void onUnsubscribe(@NotNull String... channels) {
    }

    @Override
    @SuppressWarnings("all")
    public void onSend(@NotNull String channel, byte[] data) {
        final ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");  // Subchannel
        out.writeUTF("ALL");      // Server name
        out.writeUTF(channel);       // Channel
        out.writeShort(data.length); // Bytes length
        out.write(data);             // Bytes

        super.onSend(preChannel, out.toByteArray());
    }

    @Override
    @SuppressWarnings("all")
    public void onPluginMessageReceived(@NotNull String preChannel, @NotNull Player player, byte @NotNull [] message) {
        if (this.preChannel.equals(preChannel)) {
            final ByteArrayDataInput in = ByteStreams.newDataInput(message);
            final String subChannel = in.readUTF();
            if ("Forward".equals(subChannel)) {
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
        }
    }

    @NotNull
    public String getPreChannel() {
        return preChannel;
    }
}
