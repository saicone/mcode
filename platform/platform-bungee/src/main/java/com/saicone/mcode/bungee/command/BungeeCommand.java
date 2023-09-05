package com.saicone.mcode.bungee.command;

import com.saicone.mcode.module.command.ACommand;
import com.saicone.mcode.module.command.CommandCentral;
import com.saicone.mcode.module.command.CommandResult;
import com.saicone.mcode.module.command.InputContext;
import com.saicone.mcode.module.command.builder.CommandBuilder;
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

public class BungeeCommand extends CommandCentral<CommandSender> {

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

    public static void init() {
        if (INSTANCE == null) {
            INSTANCE = new BungeeCommand();
        }
    }

    @NotNull
    public static BungeeCommand central() {
        return (BungeeCommand) INSTANCE;
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

    @Override
    public @NotNull CommandBuilder<CommandSender> builder(@NotNull ACommand command) {
        return builder(new ABungeeCommand(command));
    }

    @NotNull
    public CommandBuilder<CommandSender> builder(@NotNull ABungeeCommand command) {
        return new BungeeCommandBuilder(command);
    }

    @Override
    public CommandResult dispatch(@NotNull CommandSender user, @NotNull String id, @NotNull String input) {
        if (user instanceof ProxiedPlayer && ProxyServer.getInstance().getDisabledCommands().contains(id.toLowerCase())) {
            return CommandResult.NOT_FOUND;
        }

        final Command command = all().get(id.toLowerCase());
        if (command == null) {
            return CommandResult.NOT_FOUND;
        }

        // Use wrapped bungee command
        if (command instanceof ABungeeCommand) {
            final ABungeeCommand aCommand = (ABungeeCommand) command;

            if (!aCommand.testPermission(user)) {
                return CommandResult.NO_PERMISSION;
            }
            if (!aCommand.isRegistered()) {
                return CommandResult.NOT_REGISTERED;
            }
            return execute(aCommand.getKey(), new InputContext<>(user, null, input));
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
            command.execute(user, input.split(" "));
            return CommandResult.DONE;
        } catch (Throwable t) {
            t.printStackTrace();
            return CommandResult.FAIL_EXECUTION;
        }
    }

    @Override
    protected void register(@NotNull CommandBuilder<CommandSender> builder) {
        if (builder instanceof BungeeCommandBuilder) {
            register(builder, ((BungeeCommandBuilder) builder).getBungeeCommand());
        } else if (builder.getCommand().isMain()) {
            register(builder, new ABungeeCommand(builder.getCommand()));
        } else {
            super.register(builder);
        }
    }

    protected void register(@NotNull CommandBuilder<CommandSender> builder, @NotNull ABungeeCommand command) {
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
        commands.put(command.getName(), command);
        command.register(this);
        for (String alias : command.getAliases()) {
            commands.put(alias, command.clone(alias));
        }
    }
}
