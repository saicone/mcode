package com.saicone.mcode.bukkit.command;

import com.saicone.mcode.module.command.*;
import com.saicone.mcode.module.command.impl.AbstractCommandNode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class BukkitCommandBuilder implements CommandBuilder<CommandSender, BukkitCommandBuilder> {

    private final Node node;
    private final Command bridge;

    private CommandThrowable<CommandSender> throwable = CommandSender::sendMessage;

    public BukkitCommandBuilder(@NotNull String name) {
        this.node = new Node();
        this.bridge = new Bridge(name);
    }

    public BukkitCommandBuilder(@NotNull Command command) {
        this.node = new Node();
        this.bridge = command;
    }

    @Override
    public @NotNull BukkitCommandBuilder builder() {
        return this;
    }

    @Override
    public @NotNull BukkitCommandBuilder builder(@NotNull String name) {
        return new BukkitCommandBuilder(name);
    }

    @Override
    public @NotNull Node node() {
        return node;
    }

    @Override
    public @NotNull BukkitCommandBuilder alias(@NotNull String... aliases) {
        bridge.setAliases(List.of(aliases));
        return this;
    }

    @Override
    public @NotNull BukkitCommandBuilder description(@NotNull String description) {
        bridge.setDescription(description);
        return this;
    }

    @Override
    public @NotNull BukkitCommandBuilder description(@NotNull Function<CommandSender, String> description) {
        node.setDescription(description);
        return this;
    }

    @Override
    public @NotNull BukkitCommandBuilder permission(@NotNull String... permissions) {
        bridge.setPermission(String.join(";", permissions));
        return this;
    }

    @Override
    public @NotNull BukkitCommandBuilder eval(@NotNull Predicate<CommandSender> predicate) {
        node.setPredicate(predicate);
        return this;
    }

    @Override
    public @NotNull BukkitCommandBuilder minArgs(@NotNull Function<CommandSender, Integer> minArgs) {
        node.setMinArgs(minArgs);
        return this;
    }

    @Override
    public @NotNull BukkitCommandBuilder with(@NotNull InputArgument<CommandSender, ?> argument) {
        node.addArgument(argument);
        return this;
    }

    @Override
    public @NotNull BukkitCommandBuilder sub(@NotNull Consumer<NodeArgument<CommandSender>> consumer) {
        consumer.accept(node.getCommandArgument());
        return this;
    }

    @Override
    public @NotNull BukkitCommandBuilder sub(@NotNull CommandNode<CommandSender> node) {
        this.node.addSubCommand(node);
        return this;
    }

    @Override
    public @NotNull BukkitCommandBuilder throwable(@NotNull CommandThrowable<CommandSender> throwable) {
        this.throwable = throwable;
        return this;
    }

    @Override
    public @NotNull BukkitCommandBuilder executes(CommandExecution<CommandSender> execution) {
        node.setExecution(execution);
        return this;
    }

    @Override
    public @NotNull BukkitCommandBuilder register() {
        BukkitCommand.register(bridge);
        return this;
    }

    @Override
    public @NotNull BukkitCommandBuilder unregister() {
        BukkitCommand.unregister(bridge);
        return this;
    }

    public class Node extends AbstractCommandNode<CommandSender> {

        private Predicate<CommandSender> predicate;

        public boolean eval(@NotNull CommandSender sender) {
            return predicate == null || predicate.test(sender);
        }

        public void setPredicate(@Nullable Predicate<CommandSender> predicate) {
            if (this.predicate == null || predicate == null) {
                this.predicate = predicate;
            } else {
                this.predicate = this.predicate.and(predicate);
            }
        }

        @Override
        public @NotNull String getName() {
            return bridge.getName();
        }

        @Override
        public @NotNull Collection<String> getNodeAliases() {
            return bridge.getAliases();
        }

        @Override
        public @NotNull String getDescription(@Nullable CommandSender sender) {
            final String description = super.getDescription(sender);
            if (description.isEmpty()) {
                return bridge.getDescription();
            } else {
                return description;
            }
        }
    }

    public class Bridge extends Command implements TabCompleter, CommandExecutor<CommandSender> {
        protected Bridge(@NotNull String name) {
            super(name);
        }

        protected Bridge(@NotNull String name, @NotNull String description, @NotNull String usageMessage, @NotNull List<String> aliases) {
            super(name, description, usageMessage, aliases);
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
            if (!testPermission(sender)) {
                return true;
            }
            new InputContext<>(sender, throwable).then(commandLabel, node, args);
            return true;
        }

        @Override
        public @NotNull CommandResult result(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String... args) {
            if (!testPermission(sender)) {
                return CommandResult.NO_PERMISSION;
            } else if (node.eval(sender)) {
                return new InputContext<>(sender, throwable).then(commandLabel, node, args).getResult();
            } else {
                return CommandResult.FAIL_EVAL;
            }
        }

        @Nullable
        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            return new InputContext<>(sender, throwable).suggest(label, node, args).list();
        }

        @Override
        public boolean testPermission(@NotNull CommandSender target) {
            if (testPermissionSilent(target)) {
                return true;
            }

            throwable.sendPermissionMessage(target, getPermission());

            return false;
        }
    }
}
