package com.saicone.mcode.bukkit.command;

import com.saicone.mcode.module.command.ACommand;
import com.saicone.mcode.module.command.CommandKey;
import com.saicone.mcode.module.command.InputContext;
import com.saicone.mcode.module.command.builder.CommandBuilder;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class BukkitCommandBuilder extends CommandBuilder<CommandSender> {

    private final ABukkitCommand bukkitCommand;

    public BukkitCommandBuilder(@NotNull CommandKey key) {
        this(new ACommand(key));
    }

    public BukkitCommandBuilder(@NotNull ACommand command) {
        this(new ABukkitCommand(command));
    }

    public BukkitCommandBuilder(@NotNull ABukkitCommand command) {
        super(command.getKey());
        this.bukkitCommand = command;
    }

    public ABukkitCommand getBukkitCommand() {
        return bukkitCommand;
    }

    @Override
    public @NotNull CommandBuilder<CommandSender> permission(@Nullable String permission) {
        bukkitCommand.setPermission(permission);
        return super.permission(permission);
    }

    @Override
    public @NotNull CommandBuilder<CommandSender> permissionBound(@Nullable String permissionBound) {
        bukkitCommand.setPermissionMessage(permissionBound);
        return super.permissionBound(permissionBound);
    }

    @Override
    public @NotNull CommandBuilder<CommandSender> permissionBound(@Nullable Consumer<CommandSender> permissionBound) {
        bukkitCommand.setPermissionBound(permissionBound);
        return super.permissionBound(permissionBound);
    }

    @Override
    public @NotNull CommandBuilder<CommandSender> runAny(@NotNull Object object) throws ClassCastException {
        if (object instanceof CommandExecutor) {
            final CommandExecutor executor = (CommandExecutor) object;
            runTest((Predicate<InputContext<CommandSender>>) (context) ->
                    executor.onCommand(context.getAgent(), bukkitCommand, context.getKey().getName(), context.textArgs())
            );
            return this;
        }
        return super.runAny(object);
    }
}
