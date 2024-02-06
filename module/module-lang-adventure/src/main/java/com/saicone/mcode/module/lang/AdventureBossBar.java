package com.saicone.mcode.module.lang;

import com.saicone.mcode.module.lang.display.BossBarDisplay;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AdventureBossBar implements BossBarDisplay.Holder {

    private final BossBar bossBar;
    private final transient List<Audience> audiences = new ArrayList<>();
    private transient boolean visible = true;

    public AdventureBossBar(@NotNull BossBar bossBar) {
        this.bossBar = bossBar;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public boolean hasFlag(@NotNull BossBarDisplay.Flag flag) {
        return bossBar.hasFlag(BossBar.Flag.values()[flag.ordinal()]);
    }

    @NotNull
    public BossBar getBossBar() {
        return bossBar;
    }

    @Override
    public float getProgress() {
        return bossBar.progress();
    }

    @Override
    public @NotNull String getText() {
        return LegacyComponentSerializer.legacyAmpersand().serialize(bossBar.name());
    }

    @Override
    public @NotNull BossBarDisplay.Color getColor() {
        return BossBarDisplay.Color.values()[bossBar.color().ordinal()];
    }

    @Override
    public @NotNull BossBarDisplay.Division getDivision() {
        return BossBarDisplay.Division.values()[bossBar.overlay().ordinal()];
    }

    @Override
    public @UnmodifiableView @NotNull Set<BossBarDisplay.Flag> getFlags() {
        return bossBar.flags().stream().map(flag -> BossBarDisplay.Flag.values()[flag.ordinal()]).collect(Collectors.toSet());
    }

    @Override
    public void setProgress(float progress) {
        bossBar.progress(progress);
    }

    @Override
    public void setText(@NotNull String text) {
        bossBar.name(LegacyComponentSerializer.legacyAmpersand().deserialize(text));
    }

    @Override
    public void setColor(@NotNull BossBarDisplay.Color color) {
        bossBar.color(BossBar.Color.values()[color.ordinal()]);
    }

    @Override
    public void setDivision(@NotNull BossBarDisplay.Division division) {
        bossBar.overlay(BossBar.Overlay.values()[division.ordinal()]);
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            if (!this.visible) {
                for (Audience audience : audiences) {
                    audience.showBossBar(bossBar);
                }
            }
        } else if (this.visible) {
            for (Audience audience : audiences) {
                audience.hideBossBar(bossBar);
            }
        }
        this.visible = visible;
    }

    @Override
    public void addFlag(@NotNull BossBarDisplay.Flag flag) {
        bossBar.addFlag(BossBar.Flag.values()[flag.ordinal()]);
    }

    @Override
    public void addFlags(@NotNull BossBarDisplay.Flag... flags) {
        final BossBar.Flag[] values = new BossBar.Flag[flags.length];
        System.arraycopy(BossBar.Flag.values(), 0, values, 0, flags.length);
        bossBar.addFlags(values);
    }

    @Override
    public void addFlags(@NotNull Iterable<BossBarDisplay.Flag> flags) {
        final Set<BossBar.Flag> values = new HashSet<>();
        for (BossBarDisplay.Flag flag : flags) {
            values.add(BossBar.Flag.values()[flag.ordinal()]);
        }
        bossBar.addFlags(values);
    }

    @Override
    public void removeFlag(@NotNull BossBarDisplay.Flag flag) {
        bossBar.removeFlag(BossBar.Flag.values()[flag.ordinal()]);
    }

    @Override
    public void removeFlags(@NotNull BossBarDisplay.Flag... flags) {
        final BossBar.Flag[] values = new BossBar.Flag[flags.length];
        System.arraycopy(BossBar.Flag.values(), 0, values, 0, flags.length);
        bossBar.removeFlags(values);
    }

    @Override
    public void removeFlags(@NotNull Iterable<BossBarDisplay.Flag> flags) {
        final Set<BossBar.Flag> values = new HashSet<>();
        for (BossBarDisplay.Flag flag : flags) {
            values.add(BossBar.Flag.values()[flag.ordinal()]);
        }
        bossBar.removeFlags(values);
    }

    @Override
    public void showTo(@NotNull Object type) {
        if (type instanceof Audience) {
            audiences.add((Audience) type);
            if (visible) {
                ((Audience) type).showBossBar(bossBar);
            }
        }
    }

    @Override
    public void hideTo(@NotNull Object type) {
        if (type instanceof Audience) {
            ((Audience) type).hideBossBar(bossBar);
            audiences.remove((Audience) type);
        }
    }

    @Override
    public void hideToAll() {
        for (Audience audience : audiences) {
            audience.hideBossBar(bossBar);
        }
        audiences.clear();
    }
}
