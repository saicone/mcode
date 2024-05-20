package com.saicone.mcode.bukkit.command;

import com.saicone.mcode.module.command.*;
import com.saicone.mcode.module.command.CommandBuilder;
import com.saicone.mcode.module.command.CommandExecutor;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

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

    public static void unregister(@NotNull String... names) {
        final Set<String> set = Set.of(names);
        all().entrySet().removeIf(entry -> {
            if (set.contains(entry.getValue().getName())) {
                entry.getValue().unregister(map());
                return true;
            } else {
                return false;
            }
        });
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
    public static CommandResult dispatch(@NotNull CommandSender user, @NotNull String command) throws CommandException {
        final int index = command.indexOf(' ');
        if (index > 0 && index + 1 < command.length()) {
            return dispatch(user, command.substring(0, index), command.substring(index + 1));
        } else {
            return dispatch(user, command.trim(), "");
        }
    }

    @NotNull
    public static CommandResult dispatch(@NotNull CommandSender user, @NotNull String id, @NotNull String input) throws CommandException {
        return dispatch(user, id, input, false);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static CommandResult dispatch(@NotNull CommandSender user, @NotNull String id, @NotNull String input, boolean silent) throws CommandException {
        final Command command = all().get(id.toLowerCase());
        if (command == null) {
            if (!silent) {
                if (user instanceof Player) {
                    // TODO: Send translation "command.unknown.command"
                    user.sendMessage("Unknown command. Type \"/help\" for help.");
                } else {
                    user.sendMessage("Unknown command. Type \"help\" for help.");
                }
            }
            return CommandResult.NOT_FOUND;
        }

        try {
            if (command instanceof CommandExecutor) {
                return ((CommandExecutor<CommandSender>) command).result(user, id, input.split(" "));
            }
            final boolean result = command.execute(user, id, input.split(" "));
            return result ? CommandResult.DONE : CommandResult.FAIL_SYNTAX;
        } catch (CommandException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new CommandException("Unhandled exception executing '" + id + " " + input + "' in " + command, ex);
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
