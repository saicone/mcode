package com.saicone.mcode.bungee.command;

import com.saicone.mcode.module.command.*;
import com.saicone.mcode.module.command.impl.AbstractCommandNode;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class BungeeCommandBuilder implements CommandBuilder<CommandSender, BungeeCommandBuilder> {

    private final Node node;
    private final Bridge mainBridge;
    private Map<String, Bridge> aliasBridges;

    private CommandThrowable<CommandSender> throwable = new CommandThrowable<>() {
        @Override
        public void sendMessage(@NotNull CommandSender sender, @NotNull String message) {
            sender.sendMessage(TextComponent.fromLegacy(message));
        }

        @Override
        public void sendPermissionMessage(@NotNull CommandSender sender, @NotNull String permission) {
            if (node.permissionMessage != null) {
                sendColoredMessage(sender, node.permissionMessage);
            } else {
                CommandThrowable.super.sendPermissionMessage(sender, permission);
            }
        }
    };

    public BungeeCommandBuilder(@NotNull String name) {
        this.node = new Node();
        this.mainBridge = new Bridge(name);
    }

    @Override
    public @NotNull BungeeCommandBuilder builder() {
        return this;
    }

    @Override
    public @NotNull BungeeCommandBuilder builder(@NotNull String name) {
        return new BungeeCommandBuilder(name);
    }

    @Override
    public @NotNull CommandNode<CommandSender> node() {
        return node;
    }

    @Override
    public @NotNull BungeeCommandBuilder alias(@NotNull String... aliases) {
        node.aliases = aliases;
        final Map<String, Bridge> map = new HashMap<>();
        for (String alias : aliases) {
            map.put(alias, new Bridge(alias));
        }
        aliasBridges = map;
        return this;
    }

    @Override
    public @NotNull BungeeCommandBuilder description(@NotNull String description) {
        node.description = description;
        return this;
    }

    @Override
    public @NotNull BungeeCommandBuilder description(@NotNull Function<CommandSender, String> description) {
        node.setDescription(description);
        return this;
    }

    @Override
    public @NotNull BungeeCommandBuilder permission(@NotNull String... permissions) {
        node.permission = String.join(";", permissions);
        return this;
    }

    @Override
    public @NotNull BungeeCommandBuilder eval(@NotNull Predicate<CommandSender> predicate) {
        node.setPredicate(predicate);
        return this;
    }

    @Override
    public @NotNull BungeeCommandBuilder minArgs(@NotNull Function<CommandSender, Integer> minArgs) {
        node.setMinArgs(minArgs);
        return this;
    }

    @Override
    public @NotNull BungeeCommandBuilder with(@NotNull InputArgument<CommandSender, ?> argument) {
        node.addArgument(argument);
        return this;
    }

    @Override
    public @NotNull BungeeCommandBuilder sub(@NotNull Consumer<NodeArgument<CommandSender>> consumer) {
        consumer.accept(node.getCommandArgument());
        return this;
    }

    @Override
    public @NotNull BungeeCommandBuilder sub(@NotNull CommandNode<CommandSender> node) {
        this.node.addSubCommand(node);
        return this;
    }

    @Override
    public @NotNull BungeeCommandBuilder throwable(@NotNull CommandThrowable<CommandSender> throwable) {
        this.throwable = throwable;
        return this;
    }

    @Override
    public @NotNull BungeeCommandBuilder executes(CommandExecution<CommandSender> execution) {
        node.setExecution(execution);
        return this;
    }

    @Override
    public @NotNull BungeeCommandBuilder register() {
        final Map<String, Command> commands = BungeeCommand.all();
        commands.put(mainBridge.getName(), mainBridge);
        if (aliasBridges != null) {
            commands.putAll(aliasBridges);
        }
        return this;
    }

    @Override
    public @NotNull BungeeCommandBuilder unregister() {
        BungeeCommand.unregister(mainBridge);
        return this;
    }

    public class Node extends AbstractCommandNode<CommandSender> {
        private String[] aliases = new String[0];
        private String permission;
        private String permissionMessage;
        private Predicate<CommandSender> predicate;
        private String description = "";

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
            return mainBridge.getName();
        }

        @Override
        public @NotNull Collection<String> getNodeAliases() {
            return aliasBridges.keySet();
        }

        @Override
        public @NotNull String getDescription(@Nullable CommandSender sender) {
            final String description = super.getDescription(sender);
            if (description.isEmpty()) {
                return this.description;
            } else {
                return description;
            }
        }
    }

    public class Bridge extends Command implements TabExecutor, CommandExecutor<CommandSender> {
        public Bridge(String name) {
            super(name);
        }

        @Override
        public boolean hasPermission(CommandSender sender) {
            if (node.permission == null || node.permission.isBlank()) {
                return true;
            }

            for (String p : node.permission.split(";")) {
                if (sender.hasPermission(p)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public String getPermission() {
            return node.permission;
        }

        @Override
        public String[] getAliases() {
            return node.aliases;
        }

        @Override
        public String getPermissionMessage() {
            return node.permissionMessage;
        }

        @Override
        protected void setPermissionMessage(String permissionMessage) {
            node.permissionMessage = permissionMessage;
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            result(sender, getName(), args);
        }

        @Override
        public @NotNull CommandResult result(@NotNull CommandSender sender, @NotNull String id, @NotNull String... args) {
            if (node.eval(sender)) {
                return new InputContext<>(sender, throwable).then(id, node, args).getResult();
            } else {
                return CommandResult.FAIL_EVAL;
            }
        }

        @Override
        public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
            final Iterable<String> list = new InputContext<>(sender, throwable).suggest(getName(), node, args).list();
            return list == null ? List.of() : list;
        }
    }
}
