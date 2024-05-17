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
    private final Bridge bridge;

    private CommandThrowable<CommandSender> throwable = CommandSender::sendMessage;

    public BukkitCommandBuilder(@NotNull String name) {
        this.node = new Node();
        this.bridge = new Bridge(name);
    }

    @Override
    public @NotNull BukkitCommandBuilder builder() {
        return this;
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
        node.description = description;
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
    public @NotNull BukkitCommandBuilder argument(@NotNull CommandArgument<CommandSender> argument) {
        node.addArgument(argument);
        return this;
    }

    @Override
    public @NotNull BukkitCommandBuilder minArgs(@NotNull Function<CommandSender, Integer> minArgs) {
        node.setMinArgs(minArgs);
        return this;
    }

    @Override
    public @NotNull BukkitCommandBuilder subStart(@NotNull Function<CommandSender, Integer> subStart) {
        node.setSubStart(subStart);
        return this;
    }

    @Override
    public @NotNull BukkitCommandBuilder subCommand(@NotNull CommandNode<CommandSender> node) {
        this.node.addSubCommand(node);
        return this;
    }

    @Override
    public @NotNull BukkitCommandBuilder subCommand(@NotNull String name, @NotNull Consumer<BukkitCommandBuilder> consumer) {
        final BukkitCommandBuilder builder = new BukkitCommandBuilder(name);
        builder.node.setParent(this.node);
        consumer.accept(builder);
        return subCommand(builder.build());
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

    @Override
    public @NotNull Node build() {
        return node;
    }

    public class Node extends AbstractCommandNode<CommandSender> {

        private Function<CommandSender, String> description;

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
            if (sender != null && description != null) {
                return description.apply(sender);
            }
            return bridge.getDescription();
        }
    }

    public class Bridge extends Command implements TabCompleter {
        protected Bridge(@NotNull String name) {
            super(name);
        }

        protected Bridge(@NotNull String name, @NotNull String description, @NotNull String usageMessage, @NotNull List<String> aliases) {
            super(name, description, usageMessage, aliases);
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
            if (node.eval(sender)) {
                new InputContext<>(sender, throwable).then(commandLabel, node, args);
            }
            return true;
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
