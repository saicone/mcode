package com.saicone.mcode.bungee.command;

import com.google.common.base.Suppliers;
import com.saicone.mcode.module.command.*;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BungeeCommandBuilder implements CommandBuilder<CommandSender> {

    private final Node command;

    public BungeeCommandBuilder(@NotNull String name) {
        this.command = new Node(name);
    }

    @Override
    public @NotNull BungeeCommandBuilder alias(@NotNull String... aliases) {
        command.aliases = aliases;
        return this;
    }

    @Override
    public @NotNull BungeeCommandBuilder description(@NotNull String description) {
        command.description = description;
        return this;
    }

    @Override
    public @NotNull BungeeCommandBuilder permission(@NotNull String... permissions) {
        command.permissions = permissions;
        return this;
    }

    @Override
    public @NotNull BungeeCommandBuilder eval(@NotNull Predicate<CommandSender> predicate) {
        command.predicate = predicate;
        return this;
    }

    @Override
    public @NotNull BungeeCommandBuilder argument(@NotNull CommandArgument<CommandSender> argument) {
        if (command.arguments == null) {
            command.arguments = new ArrayList<>();
        }
        command.arguments.add(argument);
        return this;
    }

    @Override
    public @NotNull BungeeCommandBuilder subCommand(@NotNull CommandNode<CommandSender> node) {
        if (command.subCommands == null) {
            command.subCommands = new ArrayList<>();
        }
        command.subCommands.add(node);
        return this;
    }

    @Override
    public @NotNull BungeeCommandBuilder subCommand(@NotNull String name, @NotNull Consumer<CommandBuilder<CommandSender>> consumer) {
        final BungeeCommandBuilder builder = new BungeeCommandBuilder(name);
        builder.command.parent = command;
        consumer.accept(builder);
        return subCommand(builder.build());
    }

    @Override
    public @NotNull BungeeCommandBuilder executes(CommandExecution<CommandSender> execution) {
        command.execution = execution;
        return this;
    }

    @Override
    public @NotNull BungeeCommandBuilder register() {

        return this;
    }

    @Override
    public @NotNull BungeeCommandBuilder unregister() {

        return this;
    }

    @Override
    public @NotNull CommandNode<CommandSender> build() {
        return command;
    }

    public static class Node extends Command implements CommandNode<CommandSender> {
        private CommandNode<CommandSender> parent;
        private List<CommandNode<CommandSender>> subCommands;
        private final Supplier<String> path = Suppliers.memoize(() -> {
            if (getParent() != null) {
                return getParent().getPath() + "." + getName();
            } else {
                return getName();
            }
        });
        private String description;
        private Predicate<CommandSender> predicate;
        private List<CommandArgument<CommandSender>> arguments;
        private CommandExecution<CommandSender> execution;

        private String[] permissions = new String[0];
        private String[] aliases = new String[0];

        public Node(@NotNull String name) {
            super(name);
        }

        @Override
        public boolean hasPermission(CommandSender sender) {
            if (permissions.length > 0) {
                for (String permission : permissions) {
                    if (!sender.hasPermission(permission)) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        @Override
        public String getPermission() {
            return permissions.length > 0 ? permissions[0] : null;
        }

        @Override
        public String[] getAliases() {
            return aliases;
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
            return Set.of(getAliases());
        }

        @NotNull
        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public @NotNull CommandResult execute(@NotNull InputContext<CommandSender> input) {
            return execution.execute(input);
        }

        @Override
        public void execute(CommandSender commandSender, String[] strings) {

        }
    }
}
