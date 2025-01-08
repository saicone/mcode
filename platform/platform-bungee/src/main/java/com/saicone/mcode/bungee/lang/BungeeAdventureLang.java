package com.saicone.mcode.bungee.lang;

import com.saicone.mcode.module.lang.AdventureBossBar;
import com.saicone.mcode.module.lang.AdventureLang;
import com.saicone.mcode.module.lang.display.BossBarDisplay;
import com.saicone.mcode.module.lang.display.TextDisplay;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class BungeeAdventureLang extends BungeeLang implements AdventureLang<CommandSender> {

    private final BungeeAudiences audiences;

    private final AdventureLang.ActionBarLoader<CommandSender> actionbar = new AdventureLang.ActionBarLoader<>(this);
    private final AdventureLang.BossBarLoader<CommandSender> bossbar = new AdventureLang.BossBarLoader<>(this) {
        @Override
        protected BossBarDisplay.Holder newHolder(@NotNull BossBar bossBar) {
            return new AdventureBossBar(bossBar) {
                @Override
                public void showTo(@NotNull Object type) {
                    if (type instanceof CommandSender) {
                        super.showTo(getAudiences().sender((CommandSender) type));
                    }
                    super.showTo(type);
                }

                @Override
                public void hideTo(@NotNull Object type) {
                    if (type instanceof CommandSender) {
                        super.hideTo(getAudiences().sender((CommandSender) type));
                    }
                    super.hideTo(type);
                }
            };
        }
    };
    private final AdventureLang.MiniMessageLoader<CommandSender> minimessage = new AdventureLang.MiniMessageLoader<>(this);
    private final AdventureLang.TextLoader<CommandSender> text = new AdventureLang.TextLoader<>(this) {
        @Override
        protected @NotNull TextDisplay.Builder<CommandSender> newBuilder() {
            return new AdventureLang.TextBuilder<>(BungeeAdventureLang.this) {
                @Override
                protected int protocol(@NotNull CommandSender type) {
                    if (type instanceof ProxiedPlayer player) {
                        return player.getPendingConnection().getVersion();
                    }
                    return super.protocol(type);
                }
            };
        }
    };
    private final AdventureLang.TitleLoader<CommandSender> title = new AdventureLang.TitleLoader<>(this);

    private transient boolean useMiniMessage;

    public BungeeAdventureLang(@NotNull Plugin plugin, @NotNull Object... providers) {
        super(plugin, providers);
        this.audiences = BungeeAudiences.create(plugin);
    }

    @Override
    public boolean useMiniMessage() {
        return useMiniMessage;
    }

    @Override
    public @NotNull Audience getAudience(@NotNull CommandSender sender) {
        return getAudiences().sender(sender);
    }

    @NotNull
    public BungeeAudiences getAudiences() {
        return audiences;
    }

    @NotNull
    @Contract("_ -> this")
    public BungeeAdventureLang useMiniMessage(boolean useMiniMessage) {
        this.useMiniMessage = useMiniMessage;
        return this;
    }
}
