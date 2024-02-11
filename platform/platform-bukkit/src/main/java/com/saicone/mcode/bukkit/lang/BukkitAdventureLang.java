package com.saicone.mcode.bukkit.lang;

import com.saicone.mcode.module.lang.AdventureBossBar;
import com.saicone.mcode.module.lang.AdventureLang;
import com.saicone.mcode.module.lang.display.BossBarDisplay;
import com.saicone.mcode.module.lang.display.TextDisplay;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class BukkitAdventureLang extends BukkitLang {

    private final BukkitAudiences audiences;

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
    private final AdventureLang.SoundLoader<CommandSender> sound = new AdventureLang.SoundLoader<>() {
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

        @Override
        protected @NotNull TextDisplay.Event newEvent(@NotNull TextDisplay.Action action, @NotNull Object value) {
            if (value instanceof ItemStack || value instanceof org.bukkit.entity.Entity) {
                return new BukkitTextEvent(action, value);
            }
            return super.newEvent(action, value);
        }
    };
    private final AdventureLang.TitleLoader<CommandSender> title = new AdventureLang.TitleLoader<>() {
        @Override
        public Audience getAudience(@NotNull CommandSender type) {
            return getAudiences().sender(type);
        }
    };

    public BukkitAdventureLang(@NotNull Plugin plugin, @NotNull Class<?>... langProviders) {
        super(plugin, langProviders);
        this.audiences = BukkitAudiences.create(plugin);
    }

    @NotNull
    public BukkitAudiences getAudiences() {
        return audiences;
    }
}
