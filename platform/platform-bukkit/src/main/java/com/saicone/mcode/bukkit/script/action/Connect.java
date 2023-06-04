package com.saicone.mcode.bukkit.script.action;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.saicone.mcode.module.script.Action;
import com.saicone.mcode.module.script.EvalUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class Connect extends Action {

    public static final Builder<Connect> BUILDER = new Builder<Connect>("(?i)connect|proxy|bungee|velocity|server")
            .textSingle(map -> map.getIgnoreCase("server"), Connect::new);

    private static final String BUNGEECORD_CHANNEL = "BungeeCord";

    private Plugin plugin;
    private final String server;

    public Connect(@NotNull String server) {
        this.server = server;
    }

    @NotNull
    public Plugin getPlugin() {
        return plugin;
    }

    @NotNull
    public String getServer() {
        return server;
    }

    public void setPlugin(@NotNull Plugin plugin) {
        this.plugin = plugin;
        if (!Bukkit.getMessenger().isOutgoingChannelRegistered(plugin, BUNGEECORD_CHANNEL)) {
            Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, BUNGEECORD_CHANNEL);
        }
    }

    @Override
    public void accept(@NotNull EvalUser user) {
        if (user.getSubject() instanceof Player) {
            connect((Player) user.getSubject(), user.parse(getServer()));
        }
    }

    @SuppressWarnings("all")
    public void connect(@NotNull Player player, @NotNull String server) {
        final ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");  // Subchannel
        out.writeUTF(server);        // Server name

        player.sendPluginMessage(getPlugin(), BUNGEECORD_CHANNEL, out.toByteArray());
    }
}
