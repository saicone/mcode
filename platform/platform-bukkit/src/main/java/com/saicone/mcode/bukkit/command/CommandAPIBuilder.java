package com.saicone.mcode.bukkit.command;

import com.google.common.base.Suppliers;
import com.saicone.mcode.module.command.*;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.TextArgument;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class CommandAPIBuilder implements CommandBuilder<CommandSender> {

    private final Node node;

    public CommandAPIBuilder(@NotNull String name) {
        this.node = new Node(name);
    }

    @Override
    public @NotNull CommandAPIBuilder alias(@NotNull String... aliases) {
        node.command = node.command.withAliases(aliases);
        return this;
    }

    @Override
    public @NotNull CommandAPIBuilder description(@NotNull String description) {
        node.command = node.command.withFullDescription(description);
        return this;
    }

    @Override
    public @NotNull CommandAPIBuilder permission(@NotNull String... permissions) {
        node.command = node.command.withPermission(String.join(";", permissions));
        return this;
    }

    @Override
    public @NotNull CommandAPIBuilder eval(@NotNull Predicate<CommandSender> predicate) {
        node.command = node.command.withRequirement(predicate);
        return this;
    }

    @Override
    public @NotNull CommandAPIBuilder argument(@NotNull CommandArgument<CommandSender> argument) {
        new TextArgument("");
        return this;
    }

    @Override
    public @NotNull CommandAPIBuilder subCommand(@NotNull CommandNode<CommandSender> node) {
        if (this.node.subCommands == null) {
            this.node.subCommands = new ArrayList<>();
        }
        this.node.subCommands.add(node);
        if (node instanceof Node) {
            this.node.command = this.node.command.withSubcommand(((Node) node).command);
        }
        return this;
    }

    @Override
    public @NotNull CommandAPIBuilder subCommand(@NotNull String name, @NotNull Consumer<CommandBuilder<CommandSender>> consumer) {
        final CommandAPIBuilder builder = new CommandAPIBuilder(name);
        builder.node.parent = node;
        consumer.accept(builder);
        return subCommand(builder.build());
    }

    @Override
    public @NotNull CommandAPIBuilder executes(CommandExecution<CommandSender> execution) {
        node.execution = execution;
        return this;
    }

    @Override
    public @NotNull CommandAPIBuilder register() {
        node.command.register();
        return this;
    }

    @Override
    public @NotNull CommandAPIBuilder unregister() {
        CommandAPI.unregister(node.getName());
        return this;
    }

    @Override
    public @NotNull CommandNode<CommandSender> build() {
        return node;
    }

    public static class Node implements CommandNode<CommandSender> {

        private CommandAPICommand command;

        private CommandNode<CommandSender> parent;
        private List<CommandNode<CommandSender>> subCommands;
        private final Supplier<String> path = Suppliers.memoize(() -> {
            if (getParent() != null) {
                return getParent().getPath() + "." + getName();
            } else {
                return getName();
            }
        });
        private CommandExecution<CommandSender> execution;

        public Node(@NotNull String name) {
            this.command = new CommandAPICommand(name);
        }

        @Nullable
        @Override
        public CommandNode<CommandSender> getParent() {
            return parent;
        }

        @Override
        public @Nullable List<CommandNode<CommandSender>> getSubCommands() {
            return subCommands;
        }

        @Override
        public @NotNull String getName() {
            return command.getName();
        }

        @NotNull
        @Override
        public String getPath() {
            return path.get();
        }

        @Override
        public @NotNull Collection<String> getNodeAliases() {
            return List.of(command.getAliases());
        }

        @Override
        public @NotNull String getDescription() {
            return command.getFullDescription();
        }

        @Override
        public @NotNull CommandResult execute(@NotNull InputContext<CommandSender> input) {
            return execution.execute(input);
        }
    }
}
