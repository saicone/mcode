package com.saicone.mcode.bungee.command;

import com.saicone.mcode.module.command.ACommand;
import com.saicone.mcode.module.command.CommandKey;
import com.saicone.mcode.module.command.InputContext;
import com.saicone.mcode.module.command.builder.CommandBuilder;
import net.md_5.bungee.api.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class BungeeCommandBuilder extends CommandBuilder<CommandSender> {

    private final ABungeeCommand bukkitCommand;

    public BungeeCommandBuilder(@NotNull CommandKey key) {
        this(new ACommand(key));
    }

    public BungeeCommandBuilder(@NotNull ACommand command) {
        this(new ABungeeCommand(command));
    }

    public BungeeCommandBuilder(@NotNull ABungeeCommand command) {
        super(command.getKey());
        this.bukkitCommand = command;
    }

    public ABungeeCommand getBungeeCommand() {
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
}
