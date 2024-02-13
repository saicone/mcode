package com.saicone.mcode.bukkit.command;

import com.google.common.base.Suppliers;
import com.saicone.mcode.module.command.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BukkitCommandBuilder implements CommandBuilder<CommandSender> {

    private final Node command;

    public BukkitCommandBuilder(@NotNull String name) {
        this.command = new Node(name);
    }

    @Override
    public @NotNull BukkitCommandBuilder alias(@NotNull String... aliases) {
        command.setAliases(List.of(aliases));
        return this;
    }

    @Override
    public @NotNull BukkitCommandBuilder description(@NotNull String description) {
        command.setDescription(description);
        return this;
    }

    @Override
    public @NotNull BukkitCommandBuilder permission(@NotNull String... permissions) {
        command.setPermission(String.join(";", permissions));
        return this;
    }

    @Override
    public @NotNull BukkitCommandBuilder eval(@NotNull Predicate<CommandSender> predicate) {
        command.predicate = predicate;
        return this;
    }

    @Override
    public @NotNull BukkitCommandBuilder argument(@NotNull CommandArgument<CommandSender> argument) {
        if (command.arguments == null) {
            command.arguments = new ArrayList<>();
        }
        command.arguments.add(argument);
        return this;
    }

    @Override
    public @NotNull BukkitCommandBuilder subCommand(@NotNull CommandNode<CommandSender> node) {
        if (command.subCommands == null) {
            command.subCommands = new ArrayList<>();
        }
        command.subCommands.add(node);
        return this;
    }

    @Override
    public @NotNull BukkitCommandBuilder subCommand(@NotNull String name, @NotNull Consumer<CommandBuilder<CommandSender>> consumer) {
        final BukkitCommandBuilder builder = new BukkitCommandBuilder(name);
        builder.command.parent = command;
        consumer.accept(builder);
        return subCommand(builder.build());
    }

    @Override
    public @NotNull BukkitCommandBuilder executes(CommandExecution<CommandSender> execution) {
        command.execution = execution;
        return this;
    }

    @Override
    public @NotNull BukkitCommandBuilder register() {

        return this;
    }

    @Override
    public @NotNull BukkitCommandBuilder unregister() {

        return this;
    }

    @Override
    public @NotNull Node build() {
        return command;
    }

    public static class Node extends Command implements TabCompleter, CommandNode<CommandSender> {

        private CommandNode<CommandSender> parent;
        private List<CommandNode<CommandSender>> subCommands;
        private final Supplier<String> path = Suppliers.memoize(() -> {
            if (getParent() != null) {
                return getParent().getPath() + "." + getName();
            } else {
                return getName();
            }
        });
        private Predicate<CommandSender> predicate;
        private List<CommandArgument<CommandSender>> arguments;
        private CommandExecution<CommandSender> execution;

        private transient CommandThrowable<CommandSender> throwable;

        protected Node(@NotNull String name) {
            super(name);
        }

        protected Node(@NotNull String name, @NotNull String description, @NotNull String usageMessage, @NotNull List<String> aliases) {
            super(name, description, usageMessage, aliases);
        }

        @Nullable
        @Override
        public CommandNode<CommandSender> getParent() {
            return parent;
        }

        @Nullable
        @Override
        public List<CommandNode<CommandSender>> getSubCommands() {
            return subCommands;
        }

        @NotNull
        @Override
        public String getPath() {
            return path.get();
        }

        @Override
        public @NotNull Collection<String> getNodeAliases() {
            return getAliases();
        }

        @Override
        public @NotNull CommandResult execute(@NotNull InputContext<CommandSender> input) {
            return execution.execute(input);
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
            return false;
        }

        @Nullable
        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            return null;
        }
    }
}
