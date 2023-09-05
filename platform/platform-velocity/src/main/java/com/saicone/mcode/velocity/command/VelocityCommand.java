package com.saicone.mcode.velocity.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.saicone.mcode.module.command.*;
import com.saicone.mcode.module.command.builder.CommandBuilder;
import com.saicone.mcode.velocity.VelocityPlatform;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

public class VelocityCommand extends CommandCentral<CommandSource> {

    private static final MethodHandle DISPATCHER;
    private static final MethodHandle LOCK;

    static {
        MethodHandle dispatcher = null;
        MethodHandle lock = null;
        try {
            final Class<?> clazz = manager().getClass();
            final Field dField = clazz.getDeclaredField("dispatcher");
            final Field lField = clazz.getDeclaredField("lock");

            dField.setAccessible(true);
            lField.setAccessible(true);

            dispatcher = MethodHandles.lookup().unreflectGetter(dField);
            lock = MethodHandles.lookup().unreflectGetter(lField);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        DISPATCHER = dispatcher;
        LOCK = lock;
    }

    public static void init() {
        if (INSTANCE == null) {
            INSTANCE = new VelocityCommand();
        }
    }

    @NotNull
    public static VelocityCommand central() {
        return (VelocityCommand) INSTANCE;
    }

    @NotNull
    public static CommandManager manager() {
        return VelocityPlatform.get().getProxy().getCommandManager();
    }

    public static void dispatcher(@NotNull Consumer<CommandDispatcher<CommandSource>> consumer) {
        dispatcher((dispatcher) -> {
            consumer.accept(dispatcher);
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    public static <T> T dispatcher(@NotNull Function<CommandDispatcher<CommandSource>, T> function) {
        try {
            final CommandManager manager = manager();
            final ReadWriteLock lock = (ReadWriteLock) LOCK.invoke(manager);
            final CommandDispatcher<CommandSource> dispatcher = (CommandDispatcher<CommandSource>) DISPATCHER.invoke(manager);

            lock.writeLock().lock();
            try {
                return function.apply(dispatcher);
            } finally {
                lock.writeLock().unlock();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull CommandBuilder<CommandSource> builder(@NotNull CommandKey key) {
        return new VelocityCommandBuilder(key);
    }

    @Override
    public @NotNull CommandBuilder<CommandSource> builder(@NotNull ACommand command) {
        return new VelocityCommandBuilder(command);
    }

    @NotNull
    public CommandBuilder<CommandSource> builder(@NotNull AVelocityCommand command) {
        return new VelocityCommandBuilder(command);
    }

    @Override
    public CommandResult dispatch(@NotNull CommandSource user, @NotNull String id, @NotNull String input) {
        try {
            return manager().executeAsync(user, id + ' ' + input).get() ? CommandResult.DONE : CommandResult.RETURN;
        } catch (ExecutionException | InterruptedException e) {
            return CommandResult.FAIL_EXECUTION;
        }
    }

    @Override
    protected void register(@NotNull CommandBuilder<CommandSource> builder) {
        if (builder instanceof VelocityCommandBuilder) {
            register(builder, ((VelocityCommandBuilder) builder).getCommand());
        } else if (builder.getCommand() instanceof AVelocityCommand) {
            register(builder, (AVelocityCommand) builder.getCommand());
        } else if (builder.getCommand().isMain()) {
            register(builder, builder.getCommand().wrap(new AVelocityCommand(builder.getCommand().getKey())));
        } else {
            super.register(builder);
        }
    }

    protected void register(@NotNull CommandBuilder<CommandSource> builder, @NotNull AVelocityCommand command) {
        if (!command.isMain()) {
            super.register(builder);
            return;
        }
        super.register(builder);

        manager().register(command, new BrigadierCommand(build(builder, command)));
    }

    protected LiteralCommandNode<CommandSource> build(@NotNull CommandBuilder<CommandSource> builder, @NotNull AVelocityCommand command) {
        final LiteralArgumentBuilder<CommandSource> literal = LiteralArgumentBuilder.literal(command.getKey().getName());
        if (command.getPermission() != null) {
            literal.requires(command::testPermission);
        }
        if (builder.getFunction() != null) {
            final CommandFunction<CommandSource> function = builder.getFunction();
            literal.executes(context -> {
                function.execute(this, command, new InputContext<>(context.getSource(), null, context.getInput()));
                return Command.SINGLE_SUCCESS;
            });
        }
        return literal.build();
    }
}
