package com.saicone.mcode.velocity.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.saicone.mcode.module.command.CommandArgument;
import com.saicone.mcode.module.command.CommandBuilder;
import com.saicone.mcode.module.command.CommandExecution;
import com.saicone.mcode.module.command.CommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class VelocityCommandBuilder implements CommandBuilder<CommandSource> {

    private LiteralArgumentBuilder<CommandSource> builder;

    public VelocityCommandBuilder(@NotNull String name) {
        this.builder = BrigadierCommand.literalArgumentBuilder(name);
    }

    @Override
    public @NotNull VelocityCommandBuilder alias(@NotNull String... aliases) {

        return this;
    }

    @Override
    public @NotNull VelocityCommandBuilder description(@NotNull String description) {

        return this;
    }

    @Override
    public @NotNull VelocityCommandBuilder permission(@NotNull String... permissions) {
        builder = builder.requires(source -> {
            for (String permission : permissions) {
                if (!source.hasPermission(permission)) {
                    return false;
                }
            }
            return true;
        });
        return this;
    }

    @Override
    public @NotNull VelocityCommandBuilder eval(@NotNull Predicate<CommandSource> predicate) {
        builder = builder.requires(predicate);
        return this;
    }

    @Override
    public @NotNull VelocityCommandBuilder argument(@NotNull CommandArgument<CommandSource> argument) {

        return this;
    }

    @Override
    public @NotNull VelocityCommandBuilder subCommand(@NotNull CommandNode<CommandSource> node) {

        return this;
    }

    @Override
    public @NotNull VelocityCommandBuilder subCommand(@NotNull String name, @NotNull Consumer<CommandBuilder<CommandSource>> consumer) {
        // TODO: Save arguments temporally as Map
        return this;
    }

    @Override
    public @NotNull VelocityCommandBuilder executes(CommandExecution<CommandSource> execution) {

        return this;
    }

    @Override
    public @NotNull VelocityCommandBuilder register() {
        return this;
    }

    @Override
    public @NotNull VelocityCommandBuilder unregister() {
        return this;
    }

    @Override
    public @NotNull CommandNode<CommandSource> build() {
        return null;
    }
}
