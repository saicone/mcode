package com.saicone.mcode.velocity.script;

import com.saicone.mcode.module.script.*;
import com.saicone.mcode.module.script.action.ListAction;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VelocityScripts {

    public static final VelocityScriptCompiler REGISTRY = new VelocityScriptCompiler() {
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
        ListAction.builder("(?i)(force)?(player)?(chat|say)", String::valueOf).consumer((user, action) -> {
            if (user.getSubject() instanceof Player) {
                for (String msg : action.getList()) {
                    ((Player) user.getSubject()).spoofChatInput(msg);
                }
            }
        }).register();
    }

    public static void registerConditions() {
        REGISTRY.putSourceCondition("player", sender -> sender instanceof Player);
        REGISTRY.putSourceCondition("console", sender -> !(sender instanceof Player));
        REGISTRY.putSourceCondition(EvalKey.regex("(?i)perm(ission)?s?"), (sender, perms) -> {
            for (String s : perms.split(";")) {
                if (!sender.hasPermission(s)) {
                    return false;
                }
            }
            return true;
        });
        REGISTRY.putUserCondition(EvalKey.regex("(?i)(current)?server"), user -> (Player) user.getAgent(), (player, name) -> {
            final ServerConnection con = player.getCurrentServer().orElse(null);
            return con != null && name.equals(con.getServerInfo().getName());
        });
    }
}
