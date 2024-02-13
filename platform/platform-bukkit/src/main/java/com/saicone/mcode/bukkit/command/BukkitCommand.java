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

    @NotNull
    public static CommandBuilder<CommandSender> builder(@NotNull CommandNode<CommandSender> command) {

    }

    @NotNull
    public static CommandBuilder<CommandSender> builder(@NotNull Command command) {

    }

    @NotNull
    public static CommandBuilder<CommandSender> builder(@NotNull PluginCommand command) {

    }

    @NotNull
    public static CommandBuilder<CommandSender> builder(@NotNull JavaPlugin plugin, @NotNull String name) {

    }
}
