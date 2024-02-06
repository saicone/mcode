package com.saicone.mcode.bukkit.lang;

import com.saicone.mcode.module.lang.display.BossBarDisplay;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.HashSet;
import java.util.Set;

public class BukkitBossBar implements BossBarDisplay.Holder {

    private final BossBar bossBar;
    private final Set<BossBarDisplay.Flag> flags = new HashSet<>();

    public BukkitBossBar(@NotNull BossBar bossBar) {
        this.bossBar = bossBar;
    }

    @NotNull
    public BossBar getBossBar() {
        return bossBar;
    }

    @Override
    public boolean isVisible() {
        return bossBar.isVisible();
    }

    @Override
    public boolean hasFlag(@NotNull BossBarDisplay.Flag flag) {
        return bossBar.hasFlag(BarFlag.values()[flag.ordinal()]);
    }

    @Override
    public float getProgress() {
        return Double.valueOf(bossBar.getProgress()).floatValue();
    }

    @Override
    public @NotNull String getText() {
        return bossBar.getTitle();
    }

    @Override
    public @NotNull BossBarDisplay.Color getColor() {
        return BossBarDisplay.Color.values()[bossBar.getColor().ordinal()];
    }

    @Override
    public @NotNull BossBarDisplay.Division getDivision() {
        return BossBarDisplay.Division.values()[bossBar.getStyle().ordinal()];
    }

    @Override
    public @UnmodifiableView @NotNull Set<BossBarDisplay.Flag> getFlags() {
        return Set.copyOf(flags);
    }

    @Override
    public void setProgress(float progress) {
        bossBar.setProgress(progress);
    }

    @Override
    public void setText(@NotNull String text) {
        bossBar.setTitle(text);
    }

    @Override
    public void setColor(@NotNull BossBarDisplay.Color color) {
        bossBar.setColor(BarColor.values()[color.ordinal()]);
    }

    @Override
    public void setDivision(@NotNull BossBarDisplay.Division division) {
        bossBar.setStyle(BarStyle.values()[division.ordinal()]);
    }

    @Override
    public void setVisible(boolean visible) {
        bossBar.setVisible(visible);
    }

    @Override
    public void addFlag(@NotNull BossBarDisplay.Flag flag) {
        bossBar.addFlag(BarFlag.values()[flag.ordinal()]);
        flags.add(flag);
    }

    @Override
    public void removeFlag(@NotNull BossBarDisplay.Flag flag) {
        bossBar.removeFlag(BarFlag.values()[flag.ordinal()]);
        flags.remove(flag);
    }

    @Override
    public void showTo(@NotNull Object type) {
        if (type instanceof Player) {
            bossBar.addPlayer((Player) type);
        }
    }

    @Override
    public void hideTo(@NotNull Object type) {
        if (type instanceof Player) {
            bossBar.removePlayer((Player) type);
        }
    }

    @Override
    public void hideToAll() {
        bossBar.removeAll();
    }
}
