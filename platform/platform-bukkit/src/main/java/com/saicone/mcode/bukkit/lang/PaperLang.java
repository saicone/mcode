package com.saicone.mcode.bukkit.lang;

import com.saicone.mcode.module.lang.AdventureLang;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class PaperLang extends BukkitLang {

    // Loadable display types
    public static final AdventureLang.TextLoader<CommandSender> TEXT = new AdventureLang.TextLoader<>();
    public static final AdventureLang.TitleLoader<CommandSender> TITLE = new AdventureLang.TitleLoader<>();
    public static final AdventureLang.ActionbarLoader<CommandSender> ACTIONBAR = new AdventureLang.ActionbarLoader<>();
    public static final AdventureLang.SoundLoader<CommandSender> SOUND = new AdventureLang.SoundLoader<>();
    public static final AdventureLang.MiniMessageLoader<CommandSender> MINIMESSAGE = new AdventureLang.MiniMessageLoader<>();
    public static final AdventureLang.BossBarLoader<CommandSender> BOSSBAR = new AdventureLang.BossBarLoader<>();

    public PaperLang(@NotNull Plugin plugin, @NotNull Class<?>... langProviders) {
        super(plugin, langProviders);
    }

    public PaperLang(@NotNull Plugin plugin, boolean useConfig, @NotNull Class<?>... langProviders) {
        super(plugin, useConfig, langProviders);
    }
}
