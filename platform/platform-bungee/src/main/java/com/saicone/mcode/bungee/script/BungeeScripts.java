package com.saicone.mcode.bungee.script;

import com.saicone.mcode.bungee.script.action.Connect;
import com.saicone.mcode.module.script.*;
import com.saicone.mcode.module.script.action.ListAction;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BungeeScripts {

    public static final BungeeScriptCompiler REGISTRY = new BungeeScriptCompiler() {
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

    public static void registerActions() {
        ListAction.builder("(?i)console(command|cmd)?", String::valueOf).consumer((user, action) -> {
            for (String cmd : action.getList()) {
                ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), user.parse(cmd));
            }
        }).register();
        ListAction.builder("(?i)(player|sender)?(command|cmd)", String::valueOf).consumer((user, action) -> {
            if (user.getSubject() instanceof CommandSender) {
                for (String cmd : action.getList()) {
                    ProxyServer.getInstance().getPluginManager().dispatchCommand((CommandSender) user.getSubject(), user.parse(cmd));
                }
            }
        }).register();
        ListAction.builder("(?i)(force)?(player)?(chat|say)", String::valueOf).consumer((user, action) -> {
            if (user.getSubject() instanceof ProxiedPlayer) {
                for (String msg : action.getList()) {
                    ((ProxiedPlayer) user.getSubject()).chat(user.parse(msg));
                }
            }
        }).register();
        Connect.BUILDER.register();
    }

    public static void registerConditions() {
        REGISTRY.putSenderCondition("player", sender -> sender instanceof ProxiedPlayer);
        REGISTRY.putSenderCondition("console", sender -> !(sender instanceof ProxiedPlayer));
        REGISTRY.putSenderCondition(EvalKey.regex("(?i)perm(ission)?s?"), (sender, perms) -> {
            for (String s : perms.split(";")) {
                if (!sender.hasPermission(s)) {
                    return false;
                }
            }
            return true;
        });
        REGISTRY.putUserCondition(EvalKey.regex("(?i)(current)?server"), user -> (ProxiedPlayer) user.getAgent(), (player, name) -> name.equals(player.getServer().getInfo().getName()));
    }
}
