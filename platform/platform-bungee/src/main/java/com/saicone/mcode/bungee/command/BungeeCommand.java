package com.saicone.mcode.bungee.command;

import com.saicone.mcode.module.command.CommandBuilder;
import com.saicone.mcode.module.command.CommandResult;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Map;

public class BungeeCommand {

    private static final MethodHandle COMMANDS;

    static {
        MethodHandle commands = null;
        try {
            Field f = ProxyServer.getInstance().getPluginManager().getClass().getDeclaredField("commandMap");
            f.setAccessible(true);
            commands = MethodHandles.lookup().unreflectGetter(f);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        COMMANDS = commands;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static Map<String, Command> all() {
        try {
            return (Map<String, Command>) COMMANDS.invoke(ProxyServer.getInstance().getPluginManager());
        } catch (Throwable t) {
            throw new RuntimeException("Cannot get commands map from plugin manager", t);
        }
    }

    public static void register(@NotNull Command command) {
        final Map<String, Command> commands = all();
        commands.put(command.getName(), command);
        for (String alias : command.getAliases()) {
            commands.put(alias, command);
        }
    }

    public static void unregister(@NotNull String... names) {
        final Map<String, Command> commands = all();
        for (String name : names) {
            commands.remove(name);
        }
    }

    public static void unregister(@NotNull Command command) {
        final Map<String, Command> commands = all();
        commands.remove(command.getName());
        for (String alias : command.getAliases()) {
            commands.remove(alias);
        }
    }

    @NotNull
    public static CommandResult dispatch(@NotNull CommandSender user, @NotNull String id, @NotNull String input) {
        if (user instanceof ProxiedPlayer && ProxyServer.getInstance().getDisabledCommands().contains(id.toLowerCase())) {
            return CommandResult.NOT_FOUND;
        }

        final Command command = all().get(id.toLowerCase());
        if (command == null) {
            return CommandResult.NOT_FOUND;
        }

        if (!command.hasPermission(user)) {
            if (command.getPermissionMessage() == null) {
                user.sendMessage(TextComponent.fromLegacyText(ProxyServer.getInstance().getTranslation("no_permission")));
            } else {
                user.sendMessage(TextComponent.fromLegacyText(command.getPermissionMessage()));
            }
            return CommandResult.NO_PERMISSION;
        }

        try {
            if (command instanceof Executor executor) {
                return executor.result(user, input.split(" "));
            }
            command.execute(user, input.split(" "));
            return CommandResult.DONE;
        } catch (Throwable t) {
            t.printStackTrace();
            return CommandResult.FAIL_EXECUTION;
        }
    }

    @NotNull
    public static <BuilderT extends CommandBuilder<CommandSender, BuilderT>> BuilderT builder(@NotNull Command command) {

    }

    public interface Executor {
        @NotNull
        CommandResult result(@NotNull CommandSender sender, @NotNull String... args);
    }
}
