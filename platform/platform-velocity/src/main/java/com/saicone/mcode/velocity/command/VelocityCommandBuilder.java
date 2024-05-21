package com.saicone.mcode.velocity.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.saicone.mcode.module.command.*;
import com.saicone.mcode.module.command.impl.AbstractCommandNode;
import com.saicone.mcode.module.command.impl.BrigadierCommandBuilder;
import com.saicone.mcode.velocity.VelocityPlatform;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class VelocityCommandBuilder implements BrigadierCommandBuilder<CommandSource, VelocityCommandBuilder> {

    static {
        // builder
        // - main<SenderT>
        ArgumentBuilder.class;
        // - command<SenderT>
        LiteralArgumentBuilder.class;
        // - argument<SenderT, ArgumentType>
        RequiredArgumentBuilder.class;

        // node
        // - main<SenderT>
        com.mojang.brigadier.tree.CommandNode.class;
        // - command<SenderT>
        LiteralCommandNode.class;
        // - argument<SenderT, ArgumentType>
        ArgumentCommandNode.class;
    }

    private final ProxyServer proxy;
    private CommandMeta.Builder metaBuilder;
    private final Node node;

    private CommandThrowable<CommandSource> throwable = (source, message) -> source.sendMessage(LegacyComponentSerializer.legacySection().deserialize(message));

    private transient CommandMeta meta;

    public VelocityCommandBuilder(@NotNull String name) {
        this(VelocityPlatform.get().getProxy(), name);
    }

    public VelocityCommandBuilder(@NotNull ProxyServer proxy, @NotNull String name) {
        this(proxy, proxy.getCommandManager().metaBuilder(name), name);
    }

    public VelocityCommandBuilder(@NotNull ProxyServer proxy, @NotNull CommandMeta.Builder metaBuilder, @NotNull String name) {
        this.proxy = proxy;
        this.metaBuilder = metaBuilder;
        this.node = new Node(name);
    }

    @NotNull
    public CommandMeta getMeta() {
        if (meta == null) {
            meta = metaBuilder.build();
        }
        return meta;
    }

    @Override
    public @Nullable Predicate<CommandSource> getRequirement() {
        return node.requirement;
    }

    @NotNull
    @Override
    public CommandThrowable<CommandSource> getThrowable() {
        return throwable;
    }

    @Override
    public @NotNull VelocityCommandBuilder builder() {
        return this;
    }

    @Override
    public @NotNull VelocityCommandBuilder builder(@NotNull String name) {
        return new VelocityCommandBuilder(proxy, name);
    }

    @Override
    public @NotNull CommandNode<CommandSource> node() {
        return node;
    }

    @Override
    public @NotNull LiteralArgumentBuilder<CommandSource> literal(@NotNull String name) {
        return BrigadierCommand.literalArgumentBuilder(name);
    }

    @Override
    public @NotNull <T> RequiredArgumentBuilder<CommandSource, T> argument(@NotNull String name, @NotNull ArgumentType<T> type) {
        return BrigadierCommand.requiredArgumentBuilder(name, type);
    }

    @Override
    public @NotNull VelocityCommandBuilder alias(@NotNull String... aliases) {
        metaBuilder = metaBuilder.aliases(aliases);
        // Reset cached meta
        meta = null;
        return this;
    }

    @Override
    public @NotNull VelocityCommandBuilder description(@NotNull Function<CommandSource, String> description) {
        node.setDescription(description);
        return this;
    }

    @Override
    public @NotNull VelocityCommandBuilder permission(@NotNull String... permissions) {
        return eval(source -> {
            for (String permission : permissions) {
                if (!source.hasPermission(permission)) {
                    return false;
                }
            }
            return true;
        });
    }

    @Override
    public @NotNull VelocityCommandBuilder eval(@NotNull Predicate<CommandSource> predicate) {
        if (node.requirement == null) {
            node.requirement = predicate;
        } else {
            node.requirement = node.requirement.and(predicate);
        }
        return this;
    }

    @Override
    public @NotNull VelocityCommandBuilder minArgs(@NotNull Function<CommandSource, Integer> minArgs) {
        node.setMinArgs(minArgs);
        return this;
    }

    @Override
    public @NotNull VelocityCommandBuilder with(@NotNull InputArgument<CommandSource, ?> argument) {
        node.addArgument(argument);
        return this;
    }

    @Override
    public @NotNull VelocityCommandBuilder sub(@NotNull Consumer<NodeArgument<CommandSource>> consumer) {
        consumer.accept(node.getCommandArgument());
        return this;
    }

    @Override
    public @NotNull VelocityCommandBuilder sub(@NotNull CommandNode<CommandSource> node) {
        this.node.addSubCommand(node);
        return this;
    }

    @Override
    public @NotNull VelocityCommandBuilder throwable(@NotNull CommandThrowable<CommandSource> throwable) {
        this.throwable = throwable;
        return this;
    }

    @Override
    public @NotNull VelocityCommandBuilder executes(CommandExecution<CommandSource> execution) {
        node.setExecution(execution);
        return this;
    }

    @Override
    public @NotNull VelocityCommandBuilder register() {
        proxy.getCommandManager().register(getMeta(), new BrigadierCommand(build(node.getName())));
        return this;
    }

    @Override
    public @NotNull VelocityCommandBuilder unregister() {
        proxy.getCommandManager().unregister(getMeta());
        return this;
    }

    public class Node extends AbstractCommandNode<CommandSource> {

        private final String name;

        private Predicate<CommandSource> requirement;

        public Node(@NotNull String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getName() {
            return name;
        }

        @Override
        public @NotNull Collection<String> getNodeAliases() {
            return getMeta().getAliases();
        }
    }
}
