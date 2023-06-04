package com.saicone.mcode.bukkit.script.action;

import com.google.common.base.Suppliers;
import com.saicone.mcode.module.script.EvalUser;
import com.saicone.mcode.module.script.action.ListAction;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class Money extends ListAction<String> {

    public static final Builder<String> BUILDER = builder("(?i)money|balance|eco", String::valueOf);

    private static final Supplier<Object> ECONOMY = Suppliers.memoize(() -> {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            final RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                return rsp.getProvider();
            }
        }
        return null;
    });

    public Money(@NotNull List<String> list) {
        super(list);
    }

    @Override
    public void accept(@NotNull EvalUser user) {
        if (isAvailable() && user.getSubject() instanceof OfflinePlayer) {
            for (String s : getList()) {
                final String[] split = user.parse(s).split(" ", 2);
                if (split.length < 2) {
                    continue;
                }
                final double amount = Double.parseDouble(split[1].replace(" ", "").replace(",", ""));
                switch (split[0].trim().toLowerCase()) {
                    case "add":
                    case "deposit":
                        deposit((OfflinePlayer) user.getSubject(), amount);
                        break;
                    case "remove":
                    case "withdraw":
                        withdraw((OfflinePlayer) user.getSubject(), amount);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public static void deposit(OfflinePlayer player, double amount) {
        ((Economy) ECONOMY.get()).depositPlayer(player, amount);
    }

    public static void withdraw(OfflinePlayer player, double amount) {
        ((Economy) ECONOMY.get()).withdrawPlayer(player, amount);
    }

    public static boolean contains(OfflinePlayer player, double amount) {
        return ((Economy) ECONOMY.get()).getBalance(player) >= amount;
    }

    public static boolean isAvailable() {
        return ECONOMY.get() != null;
    }
}
