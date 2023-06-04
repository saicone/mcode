package com.saicone.mcode.bukkit.script;

import com.google.common.base.Suppliers;
import com.saicone.mcode.module.script.*;
import com.saicone.mcode.module.script.action.ListAction;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class BukkitScripts {

    public static final BukkitScriptCompiler REGISTRY = new BukkitScriptCompiler() {
        @Override
        public @Nullable EvalBuilder<? extends Action> putAction(@NotNull Object key, @NotNull EvalBuilder<? extends Action> action) {
            return Script.REGISTRY.putAction(key, action);
        }

        @Override
        public @Nullable EvalBuilder<? extends Condition> putCondition(@NotNull Object key, @NotNull EvalBuilder<? extends Condition> condition) {
            return Script.REGISTRY.putCondition(key, condition);
        }

        @Override
        public @Nullable ScriptFunction<EvalUser, ActionResult> putActionFunction(@NotNull Object key, @NotNull ScriptFunction<EvalUser, ActionResult> action) {
            return Script.REGISTRY.putActionFunction(key, action);
        }

        @Override
        public @Nullable ScriptFunction<EvalUser, Boolean> putConditionFunction(@NotNull Object key, @NotNull ScriptFunction<EvalUser, Boolean> condition) {
            return Script.REGISTRY.putConditionFunction(key, condition);
        }
    };

    private static final Supplier<Object> economy = Suppliers.memoize(() -> {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            final RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                return rsp.getProvider();
            }
        }
        return null;
    });

    public static void registerActions() {
        ListAction.builder("(?i)console(command|cmd)?", String::valueOf).consumer((user, action) -> {
            for (String cmd : action.getList()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), user.parse(cmd));
            }
        }).register();
        ListAction.builder("(?i)(player|sender)?(command|cmd)", String::valueOf).consumer((user, action) -> {
            for (String cmd : action.getList()) {
                Bukkit.dispatchCommand((CommandSender) user.getSubject(), user.parse(cmd));
            }
        }).register();
        ListAction.builder("(?i)(force)?(player)?(chat|say)", String::valueOf).consumer((user, action) -> {
            for (String msg : action.getList()) {
                ((Player) user.getSubject()).chat(user.parse(msg));
            }
        }).register();
        ListAction.builder("(?i)money|balance|eco", String::valueOf).consumer((user, action) -> {
            for (String s : action.getList()) {
                final String[] split = user.parse(s).split(" ", 2);
                if (split.length < 2) {
                    continue;
                }
                final double amount = Double.parseDouble(split[1].replace(" ", "").replace(",", ""));
                switch (split[0].trim().toLowerCase()) {
                    case "add":
                    case "deposit":
                        ((Economy) economy.get()).depositPlayer((OfflinePlayer) user.getSubject(), amount);
                        break;
                    case "remove":
                    case "withdraw":
                        ((Economy) economy.get()).withdrawPlayer((OfflinePlayer) user.getSubject(), amount);
                        break;
                    default:
                        break;
                }
            }
        }).register();
    }

    public static void registerConditions() {
        REGISTRY.putSenderCondition("player", sender -> sender instanceof Player);
        REGISTRY.putSenderCondition("console", sender -> !(sender instanceof Player));
        REGISTRY.putSenderCondition(EvalKey.regex("(?i)perm(ission)?s?"), (sender, perms) -> {
            for (String s : perms.split(";")) {
                if (!sender.hasPermission(s)) {
                    return false;
                }
            }
            return true;
        });
        REGISTRY.putSenderCondition(EvalKey.regex("(?i)op(erator)?"), ServerOperator::isOp);
        REGISTRY.putUserCondition("online", user -> (OfflinePlayer) user.getAgent(), OfflinePlayer::isOnline);
        REGISTRY.putUserCondition("whitelisted", user -> (OfflinePlayer) user.getAgent(), OfflinePlayer::isWhitelisted);
        REGISTRY.putConditionPredicate("level", user -> (Player) user.getAgent(), Integer::parseInt, (player, level) -> player.getLevel() >= level);
        REGISTRY.putConditionPredicate(
                EvalKey.regex("(?i)money|balance|eco"),
                user -> (OfflinePlayer) user.getAgent(),
                Double::parseDouble,
                (player, balance) -> ((Economy) economy.get()).getBalance(player) >= balance
        );
    }
}
