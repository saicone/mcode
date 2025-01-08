package com.saicone.mcode.paper.lang;

import com.saicone.mcode.bukkit.lang.BukkitLang;
import com.saicone.mcode.bukkit.lang.BukkitTextEvent;
import com.saicone.mcode.module.lang.AdventureLang;
import com.saicone.mcode.module.lang.display.TextDisplay;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class PaperLang extends BukkitLang implements AdventureLang<CommandSender> {

    // Loadable display types
    private final AdventureLang.ActionBarLoader<CommandSender> actionbar = new AdventureLang.ActionBarLoader<>(this);
    private final AdventureLang.BossBarLoader<CommandSender> bossbar = new AdventureLang.BossBarLoader<>(this);
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

    public PaperLang(@NotNull Plugin plugin, @NotNull Object... providers) {
        super(plugin, providers);
    }

    @Override
    public boolean useMiniMessage() {
        return useMiniMessage;
    }

    @NotNull
    @Contract("_ -> this")
    public PaperLang useMiniMessage(boolean useMiniMessage) {
        this.useMiniMessage = useMiniMessage;
        return this;
    }
}
