package com.saicone.mcode.bukkit.script;

import com.saicone.mcode.bukkit.script.action.Connect;
import com.saicone.mcode.module.script.*;
import com.saicone.mcode.module.script.action.ListAction;
import com.saicone.mcode.util.function.ThrowableFunction;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class BukkitScriptCompiler extends ScriptCompiler {

    private final Plugin plugin;

    BukkitScriptCompiler() {
        this.plugin = null;
    }

    public BukkitScriptCompiler(@NotNull Plugin plugin) {
        this(plugin, true);
    }

    public BukkitScriptCompiler(@NotNull Plugin plugin, boolean register) {
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
        ListAction.builder("(?i)console(command|cmd)?", String::valueOf).consumer((user, action) -> {
            for (String cmd : action.getList()) {
                dispatchCommand(Bukkit.getConsoleSender(), user.parse(cmd));
            }
        }).register(this);
        ListAction.builder("(?i)(player|sender)?(command|cmd)", String::valueOf).consumer((user, action) -> {
            if (user.getSubject() instanceof CommandSender) {
                for (String cmd : action.getList()) {
                    dispatchCommand((CommandSender) user.getSubject(), user.parse(cmd));
                }
            }
        }).register(this);
        Connect.BUILDER.build(action -> action.setPlugin(plugin)).register(this);
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

    protected void dispatchCommand(@NotNull CommandSender sender, @NotNull String cmd) {
        if (Bukkit.isPrimaryThread()) {
            Bukkit.dispatchCommand(sender, cmd);
        }
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(sender, cmd));
    }

    @Override
    protected void run(long delay, @NotNull TimeUnit unit, @NotNull Runnable runnable) {
        if (Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTaskLater(plugin, runnable, unit.toMillis(delay) * 20000 / 1000);
        }
    }
}
