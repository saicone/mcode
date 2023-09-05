package com.saicone.mcode.velocity.command;

import com.saicone.mcode.module.command.ACommand;
import com.saicone.mcode.module.command.CommandKey;
import com.saicone.mcode.module.command.builder.CommandBuilder;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class VelocityCommandBuilder extends CommandBuilder<CommandSource> {

    public VelocityCommandBuilder(@NotNull CommandKey key) {
        this(new AVelocityCommand(key));
    }

    public VelocityCommandBuilder(@NotNull ACommand command) {
        this(command.wrap(new AVelocityCommand(command.getKey())));
    }

    public VelocityCommandBuilder(@NotNull AVelocityCommand command) {
        super(command);
    }

    @Override
    public @NotNull AVelocityCommand getCommand() {
        return (AVelocityCommand) super.getCommand();
    }

    @Override
    public @NotNull CommandBuilder<CommandSource> permission(@Nullable String permission) {
        getCommand().setPermission(permission);
        return this;
    }

    @Override
    public @NotNull CommandBuilder<CommandSource> permissionBound(@Nullable String permissionBound) {
        return permissionBound(LegacyComponentSerializer.legacyAmpersand().deserializeOrNull(permissionBound));
    }

    @NotNull
    public CommandBuilder<CommandSource> permissionBound(@Nullable Component component) {
        if (component == null) {
            return this;
        }
        return permissionBound(source -> source.sendMessage(component));
    }

    @Override
    public @NotNull CommandBuilder<CommandSource> permissionBound(@Nullable Consumer<CommandSource> permissionBound) {
        getCommand().setPermissionBound(permissionBound);
        return this;
    }
}
