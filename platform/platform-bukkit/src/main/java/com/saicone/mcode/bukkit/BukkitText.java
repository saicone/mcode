package com.saicone.mcode.bukkit;

import com.google.common.base.Suppliers;
import com.saicone.mcode.platform.Text;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class BukkitText extends Text {

    private static final Supplier<Boolean> USE_PLACEHOLDERAPI = Suppliers.memoize(() -> Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null);

    public BukkitText(@NotNull String string) {
        super(string);
    }

    @Override
    public @NotNull Text parse(@Nullable Object subject) {
        super.parse(subject);
        if (USE_PLACEHOLDERAPI.get() && subject instanceof OfflinePlayer) {
            setString(PlaceholderAPI.setPlaceholders((OfflinePlayer) subject, getString()));
        }
        return this;
    }

    @Override
    public @NotNull Text parseAgent(@Nullable Object agent) {
        super.parseAgent(agent);
        if (USE_PLACEHOLDERAPI.get() && agent instanceof OfflinePlayer) {
            setString(PlaceholderAPI.setBracketPlaceholders((OfflinePlayer) agent, getString()));
        }
        return this;
    }
}
