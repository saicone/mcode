package com.saicone.mcode.bukkit.command;

import com.saicone.mcode.module.command.*;
import com.saicone.mcode.module.command.CommandBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Map;

public class BukkitCommand {

    private static final CommandMap COMMAND_MAP;
    private static final MethodHandle COMMANDS;

    static {
        CommandMap commandMap = null;
        MethodHandle commands = null;
        try {
            Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            f.setAccessible(true);
            commandMap = (CommandMap) f.get(Bukkit.getServer());

            Class<?> c = commandMap.getClass();
            if (c.getSimpleName().equals("CraftCommandMap")) {
                c = c.getSuperclass();
            }

            f = c.getDeclaredField("knownCommands");
            f.setAccessible(true);
            commands = MethodHandles.lookup().unreflectGetter(f);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        COMMAND_MAP = commandMap;
        COMMANDS = commands;
    }

    @NotNull
    public static CommandMap map() {
        return COMMAND_MAP;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static Map<String, Command> all() {
        try {
            return (Map<String, Command>) COMMANDS.invoke(COMMAND_MAP);
        } catch (Throwable t) {
            throw new RuntimeException("Cannot get known commands from Bukkit CommandMap", t);
        }
    }

    public static void register(@NotNull Command command) {
        command.register(map());
        final Map<String, Command> commands = all();
        commands.put(command.getName(), command);
        for (String alias : command.getAliases()) {
            commands.put(alias, command);
        }
    }

    public static void unregister(@NotNull String name, @NotNull String... aliases) {
        final Map<String, Command> commands = all();
        commands.remove(name);
        for (String alias : aliases) {
            commands.remove(alias);
        }
    }

    public static void unregister(@NotNull Command command) {
        command.unregister(map());
        final Map<String, Command> commands = all();
        commands.remove(command.getName(), command);
        for (String alias : command.getAliases()) {
            commands.remove(alias, command);
        }
    }

    @NotNull
    public static <BuilderT extends CommandBuilder<CommandSender, BuilderT>> BuilderT builder(@NotNull CommandNode<CommandSender> command) {

    }

    @NotNull
    public static <BuilderT extends CommandBuilder<CommandSender, BuilderT>> BuilderT builder(@NotNull Command command) {

    }

    @NotNull
    public static <BuilderT extends CommandBuilder<CommandSender, BuilderT>> BuilderT builder(@NotNull PluginCommand command) {

    }

    @NotNull
    public static <BuilderT extends CommandBuilder<CommandSender, BuilderT>> BuilderT builder(@NotNull JavaPlugin plugin, @NotNull String name) {

    }
}
