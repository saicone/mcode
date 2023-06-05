package com.saicone.mcode.velocity.script;

import com.google.inject.Inject;
import com.saicone.mcode.module.script.ScriptCompiler;
import com.saicone.mcode.module.script.action.ListAction;
import com.saicone.mcode.util.function.ThrowableFunction;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class VelocityScriptCompiler extends ScriptCompiler {

    @Inject
    private ProxyServer proxy;
    private final @NotNull Object plugin;

    @SuppressWarnings("all")
    VelocityScriptCompiler() {
        this.plugin = null;
    }

    public VelocityScriptCompiler(@NotNull Object plugin) {
        this.plugin = plugin;
    }

    @NotNull
    public Object getPlugin() {
        return plugin;
    }

    protected void registerActions() {
        ListAction.builder("(?i)console(command|cmd)?", String::valueOf).consumer((user, action) -> {
            for (String cmd : action.getList()) {
                proxy.getCommandManager().executeAsync(proxy.getConsoleCommandSource(), user.parse(cmd));
            }
        }).register();
        ListAction.builder("(?i)(player|sender)?(command|cmd)", String::valueOf).consumer((user, action) -> {
            if (user.getSubject() instanceof CommandSource) {
                for (String cmd : action.getList()) {
                    proxy.getCommandManager().executeAsync((CommandSource) user.getSubject(), user.parse(cmd));
                }
            }
        }).register();
        ListAction.builder("(?i)connect|proxy|bungee|velocity|server", String::valueOf)
                .consumer((user, action) -> {
                    if (!action.isEmpty() && user.getSubject() instanceof Player) {
                        proxy.getServer(user.parse(action.getValue()))
                                .ifPresent(server -> ((Player) user.getSubject()).createConnectionRequest(server).fireAndForget());
                    }
                })
                .register(this);
    }

    protected void registerConditions() {

    }

    public void putSourceCondition(@NotNull Object key, @NotNull Predicate<CommandSource> predicate) {
        putUserCondition(key, user -> (CommandSource) user.getAgent(), predicate);
    }

    public void putSourceCondition(@NotNull Object key, @NotNull BiPredicate<CommandSource, String> predicate) {
        putUserCondition(key, user -> (CommandSource) user.getAgent(), predicate);
    }

    public <B> void putSourceCondition(@NotNull Object key, @NotNull ThrowableFunction<String, B> valueMapper, @NotNull BiPredicate<CommandSource, B> predicate) {
        putConditionPredicate(key, user -> (CommandSource) user.getAgent(), valueMapper, predicate);
    }

    @Override
    protected void run(long delay, @NotNull TimeUnit unit, @NotNull Runnable runnable) {
        proxy.getScheduler().buildTask(plugin, runnable).delay(delay, unit).schedule();
    }
}
