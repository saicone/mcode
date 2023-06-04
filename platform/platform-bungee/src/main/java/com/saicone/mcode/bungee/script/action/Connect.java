package com.saicone.mcode.bungee.script.action;

import com.google.common.base.Enums;
import com.saicone.mcode.module.script.Action;
import com.saicone.mcode.module.script.EvalUser;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import org.jetbrains.annotations.NotNull;

public class Connect extends Action {

    public static final Builder<Connect> BUILDER = new Builder<Connect>("(?i)connect|proxy|bungee|velocity|server")
            .map(map -> {
                final Object server = map.getIgnoreCase("server");
                return server == null ? null : new Connect(String.valueOf(server), map.getBy(String::valueOf, m -> m.getIgnoreCase("reason"), "PLUGIN"));
            })
            .textSingle(text -> new Connect(text, "PLUGIN"));

    private final String server;
    private final String reason;

    public Connect(@NotNull String server, @NotNull String reason) {
        this.server = server;
        this.reason = reason;
    }

    @NotNull
    public String getServer() {
        return server;
    }

    @NotNull
    public String getReason() {
        return reason;
    }

    @Override
    public void accept(@NotNull EvalUser user) {
        if (user.getSubject() instanceof ProxiedPlayer) {
            final ServerInfo target = ProxyServer.getInstance().getServerInfo(user.parse(server));
            if (target == null) {
                return;
            }
            ((ProxiedPlayer) user.getSubject()).connect(target, Enums.getIfPresent(ServerConnectEvent.Reason.class, user.parse(reason)).or(ServerConnectEvent.Reason.PLUGIN));
        }
    }
}
