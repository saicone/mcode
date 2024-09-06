package com.saicone.mcode.bukkit.script;

import com.saicone.mcode.bukkit.script.action.Money;
import com.saicone.mcode.module.script.*;
import com.saicone.mcode.module.script.action.ListAction;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.ServerOperator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    static {
        registerActions();
        registerConditions();
    }

    BukkitScripts() {
    }

    private static void registerActions() {
        ListAction.builder("(?i)console(command|cmd)?", String::valueOf).consumer((user, action) -> {
            for (String cmd : action.getList()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), user.parse(cmd));
            }
        }).register();
        ListAction.builder("(?i)(player|sender)?(command|cmd)", String::valueOf).consumer((user, action) -> {
            if (user.getSubject() instanceof CommandSender) {
                for (String cmd : action.getList()) {
                    Bukkit.dispatchCommand((CommandSender) user.getSubject(), user.parse(cmd));
                }
            }
        }).register();
        ListAction.builder("(?i)(force)?(player)?(chat|say)", String::valueOf).consumer((user, action) -> {
            if (user.getSubject() instanceof Player) {
                for (String msg : action.getList()) {
                    ((Player) user.getSubject()).chat(user.parse(msg));
                }
            }
        }).register();
        Money.BUILDER.register();
    }

    private static void registerConditions() {
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
        REGISTRY.putConditionPredicate(EvalKey.regex("(?i)money|balance|eco"), user -> (OfflinePlayer) user.getAgent(), Double::parseDouble, Money::contains);
    }
}
