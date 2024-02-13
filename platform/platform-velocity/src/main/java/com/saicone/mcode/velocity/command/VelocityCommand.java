package com.saicone.mcode.velocity.command;

import com.mojang.brigadier.CommandDispatcher;
import com.saicone.mcode.module.command.*;
import com.saicone.mcode.module.command.CommandBuilder;
import com.saicone.mcode.velocity.VelocityPlatform;
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

public class VelocityCommand {

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

    @NotNull
    public static CommandBuilder<CommandSource> builder(@NotNull CommandNode<CommandSource> command) {

    }

    @NotNull
    public static CommandResult dispatch(@NotNull CommandSource user, @NotNull String id, @NotNull String input) {
        try {
            return manager().executeAsync(user, id + ' ' + input).get() ? CommandResult.DONE : CommandResult.RETURN;
        } catch (ExecutionException | InterruptedException e) {
            return CommandResult.FAIL_EXECUTION;
        }
    }
}
