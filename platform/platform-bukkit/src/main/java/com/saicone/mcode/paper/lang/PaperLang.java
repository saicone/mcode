package com.saicone.mcode.paper.lang;

import com.saicone.mcode.bukkit.lang.BukkitLang;
import com.saicone.mcode.bukkit.lang.BukkitTextEvent;
import com.saicone.mcode.module.lang.AdventureLang;
import com.saicone.mcode.module.lang.Displays;
import com.saicone.mcode.module.lang.display.TextDisplay;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class PaperLang extends BukkitLang implements AdventureLang<CommandSender> {

    // Loadable display types
    public static final AdventureLang.ActionBarLoader<CommandSender> ACTIONBAR = new AdventureLang.ActionBarLoader<>();
    public static final AdventureLang.BossBarLoader<CommandSender> BOSSBAR = new AdventureLang.BossBarLoader<>();
    public static final AdventureLang.MiniMessageLoader<CommandSender> MINIMESSAGE = new AdventureLang.MiniMessageLoader<>();
    public static final AdventureLang.SoundLoader<CommandSender> SOUND = new AdventureLang.SoundLoader<>();
    public static final AdventureLang.TextLoader<CommandSender> TEXT = new AdventureLang.TextLoader<>() {
        @Override
        protected @NotNull TextDisplay.Event newEvent(@NotNull TextDisplay.Action action, @NotNull Object value) {
            if (value instanceof ItemStack || value instanceof org.bukkit.entity.Entity) {
                return new BukkitTextEvent(action, value);
            }
            return super.newEvent(action, value);
        }
    };
    public static final AdventureLang.TitleLoader<CommandSender> TITLE = new AdventureLang.TitleLoader<>();

    static {
        if (!CREATE_AUDIENCE) {
            Displays.register("actionbar", ACTIONBAR);
            Displays.register("bossbar", BOSSBAR);
            Displays.register("minimessage", MINIMESSAGE);
            Displays.register("sound", SOUND);
            Displays.register("text", TEXT);
            Displays.register("title", TITLE);
        }
    }

    public PaperLang(@NotNull Plugin plugin, @NotNull Object... providers) {
        super(plugin, providers);
    }
}
