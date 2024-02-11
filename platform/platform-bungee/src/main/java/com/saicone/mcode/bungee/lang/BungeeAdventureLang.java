package com.saicone.mcode.bungee.lang;

import com.saicone.mcode.module.lang.AdventureBossBar;
import com.saicone.mcode.module.lang.AdventureLang;
import com.saicone.mcode.module.lang.display.BossBarDisplay;
import com.saicone.mcode.module.lang.display.TextDisplay;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class BungeeAdventureLang extends BungeeLang {

    private final BungeeAudiences audiences;

    private final AdventureLang.ActionBarLoader<CommandSender> actionbar = new AdventureLang.ActionBarLoader<>() {
        @Override
        public Audience getAudience(@NotNull CommandSender type) {
            return getAudiences().sender(type);
        }
    };
    private final AdventureLang.BossBarLoader<CommandSender> bossBar = new AdventureLang.BossBarLoader<>() {
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
    private final AdventureLang.MiniMessageLoader<CommandSender> miniMessage = new AdventureLang.MiniMessageLoader<>() {
        @Override
        public Audience getAudience(@NotNull CommandSender type) {
            return getAudiences().sender(type);
        }
    };
    private final AdventureLang.TextLoader<CommandSender> text = new AdventureLang.TextLoader<>() {
        @Override
        public Audience getAudience(@NotNull CommandSender type) {
            return getAudiences().sender(type);
        }

        @NotNull
        @Override
        protected TextDisplay.Builder<CommandSender> newBuilder() {
            return new AdventureLang.TextBuilder<>() {
                @Override
                public Audience getAudience(@NotNull CommandSender type) {
                    return getAudiences().sender(type);
                }
            };
        }
    };
    private final AdventureLang.TitleLoader<CommandSender> title = new AdventureLang.TitleLoader<>() {
        @Override
        public Audience getAudience(@NotNull CommandSender type) {
            return getAudiences().sender(type);
        }
    };

    public BungeeAdventureLang(@NotNull Plugin plugin, @NotNull Class<?>... langProviders) {
        super(plugin, langProviders);
        this.audiences = BungeeAudiences.create(plugin);
    }

    @NotNull
    public BungeeAudiences getAudiences() {
        return audiences;
    }
}
