package com.saicone.mcode.bungee.script;

import com.saicone.mcode.module.script.ScriptCompiler;
import com.saicone.mcode.util.function.ThrowableFunction;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class BungeeScriptCompiler extends ScriptCompiler {

    private final Plugin plugin;

    BungeeScriptCompiler() {
        this.plugin = null;
    }

    public BungeeScriptCompiler(@NotNull Plugin plugin) {
        this(plugin, true);
    }

    public BungeeScriptCompiler(@NotNull Plugin plugin, boolean register) {
        this.plugin = plugin;
        if (register) {
            registerActions();
            registerConditions();
        }
    }

    @NotNull
    public Plugin getPlugin() {
        return plugin;
    }

    protected void registerActions() {

    }

    protected void registerConditions() {

    }

    public void putSenderCondition(@NotNull Object key, @NotNull Predicate<CommandSender> predicate) {
        putUserCondition(key, user -> (CommandSender) user.getAgent(), predicate);
    }

    public void putSenderCondition(@NotNull Object key, @NotNull BiPredicate<CommandSender, String> predicate) {
        putUserCondition(key, user -> (CommandSender) user.getAgent(), predicate);
    }

    public <B> void putSenderCondition(@NotNull Object key, @NotNull ThrowableFunction<String, B> valueMapper, @NotNull BiPredicate<CommandSender, B> predicate) {
        putConditionPredicate(key, user -> (CommandSender) user.getAgent(), valueMapper, predicate);
    }

    @Override
    protected void run(long delay, @NotNull TimeUnit unit, @NotNull Runnable runnable) {
        ProxyServer.getInstance().getScheduler().schedule(plugin, runnable, delay, unit);
    }
}
