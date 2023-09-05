package com.saicone.mcode.bukkit.command;

import com.saicone.mcode.module.command.*;
import com.saicone.mcode.module.command.builder.CommandBuilder;
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

public class BukkitCommand extends CommandCentral<CommandSender> {

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

    public static void init() {
        if (INSTANCE == null) {
            INSTANCE = new BukkitCommand();
        }
    }

    @NotNull
    public static BukkitCommand central() {
        return (BukkitCommand) INSTANCE;
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

    @Override
    public @NotNull CommandBuilder<CommandSender> builder(@NotNull ACommand command) {
        return builder(new ABukkitCommand(command));
    }

    @NotNull
    public CommandBuilder<CommandSender> builder(@NotNull ABukkitCommand command) {
        return new BukkitCommandBuilder(command);
    }

    @NotNull
    public CommandBuilder<CommandSender> builder(@NotNull PluginCommand command) {
        final ACommand aCommand = new ACommand(new CommandKey(command.getName()).alias(command.getAliases().toArray(new String[0])));
        aCommand.setDescription(command.getDescription());

        final ABukkitCommand aBukkitCommand = new ABukkitCommand(aCommand);
        aBukkitCommand.setPermission(command.getPermission());
        aBukkitCommand.setPermissionMessage(command.getPermissionMessage());
        aBukkitCommand.setUsage(command.getUsage());

        return builder(aBukkitCommand);
    }

    @NotNull
    public CommandBuilder<CommandSender> builder(@NotNull JavaPlugin plugin, @NotNull String name) {
        final PluginCommand command = plugin.getCommand(name);
        if (command == null) {
            return builder(name);
        } else {
            return builder(command);
        }
    }

    @Override
    public CommandResult dispatch(@NotNull CommandSender user, @NotNull String id, @NotNull String input) {
        final Command command = all().get(id.toLowerCase());
        if (command == null) {
            return CommandResult.NOT_FOUND;
        }
        if (!command.testPermission(user)) {
            return CommandResult.NO_PERMISSION;
        }
        if (command instanceof ABukkitCommand) {
            if (!command.isRegistered()) {
                return CommandResult.NOT_REGISTERED;
            }
            return execute(((ABukkitCommand) command).getKey(), new InputContext<>(user, null, input));
        } else {
            try {
                return command.execute(user, id.toLowerCase(), input.split(" ")) ? CommandResult.DONE : CommandResult.RETURN;
            } catch (Throwable t) {
                t.printStackTrace();
                return CommandResult.FAIL_EXECUTION;
            }
        }
    }

    @Override
    protected void register(@NotNull CommandBuilder<CommandSender> builder) {
        if (builder instanceof BukkitCommandBuilder) {
            register(builder, ((BukkitCommandBuilder) builder).getBukkitCommand());
        } else if (builder.getCommand().isMain()) {
            register(builder, new ABukkitCommand(builder.getCommand()));
        } else {
            super.register(builder);
        }
    }

    protected void register(@NotNull CommandBuilder<CommandSender> builder, @NotNull ABukkitCommand command) {
        if (!builder.getCommand().isMain()) {
            if (command.getPermission() != null) {
                builder.function().setEval(
                        context -> command.testPermission(context.getSender()) ? CommandResult.DONE : CommandResult.FAIL_EVAL, true);
            }
            super.register(builder);
            return;
        }
        super.register(builder);

        final Map<String, Command> commands = all();
        commands.put(command.getName().toLowerCase(), command);
        for (String alias : command.getAliases()) {
            commands.put(alias.toLowerCase(), command);
        }
        command.register(this, map());
    }
}
