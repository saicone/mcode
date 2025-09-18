package com.saicone.mcode.bukkit.lang;

import com.google.common.base.Suppliers;
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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class BukkitAdventureLang extends BukkitLang implements AdventureLang<CommandSender> {

    private final Supplier<BukkitAudiences> audiences = Suppliers.memoize(() -> BukkitAudiences.create(getPlugin()));

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
    private final AdventureLang.SoundLoader<CommandSender> sound = new AdventureLang.SoundLoader<>(this);
    private final AdventureLang.TextLoader<CommandSender> text = new AdventureLang.TextLoader<>(this) {
        @Override
        protected @NotNull TextDisplay.Event newEvent(@NotNull TextDisplay.Action action, @NotNull Object value) {
            if (value instanceof ItemStack || value instanceof org.bukkit.entity.Entity) {
                return new BukkitTextEvent(action, value);
            }
            return super.newEvent(action, value);
        }
    };
    private final AdventureLang.TitleLoader<CommandSender> title = new AdventureLang.TitleLoader<>(this);

    private transient boolean useMiniMessage;

    public BukkitAdventureLang(@NotNull Plugin plugin, @NotNull Object... providers) {
        super(plugin, providers);
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
    public BukkitAudiences getAudiences() {
        return audiences.get();
    }

    @NotNull
    @Contract("_ -> this")
    public BukkitAdventureLang useMiniMessage(boolean useMiniMessage) {
        this.useMiniMessage = useMiniMessage;
        return this;
    }
}
