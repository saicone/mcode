package com.saicone.mcode.module.command.brigadier;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import com.saicone.mcode.module.command.CommandThrowable;
import com.saicone.mcode.module.command.InputContext;
import com.saicone.mcode.util.Dual;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Map;

public class BrigadierInputContext<SenderT> extends InputContext<SenderT> {

    private static final MethodHandle ARGUMENTS;

    static {
        MethodHandle arguments = null;
        try {
            final MethodHandles.Lookup lookup = MethodHandles.lookup();
            final Field argumentsField = CommandContext.class.getDeclaredField("arguments");
            argumentsField.setAccessible(true);
            arguments = lookup.unreflectGetter(argumentsField);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        ARGUMENTS = arguments;
    }

    private final CommandContext<SenderT> context;

    public BrigadierInputContext(@NotNull CommandContext<SenderT> context, @NotNull CommandThrowable<SenderT> throwable) {
        super(context.getSource(), throwable);
        this.context = context;
        for (Map.Entry<String, ParsedArgument<SenderT, ?>> entry : getContextArguments().entrySet()) {
            addArgument(entry.getKey(), Dual.of(null, entry.getValue().getResult()));
        }
    }
    @NotNull
    public CommandContext<SenderT> getContext() {
        return context;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public Map<String, ParsedArgument<SenderT, ?>> getContextArguments() {
        try {
            return (Map<String, ParsedArgument<SenderT, ?>>) ARGUMENTS.invoke(this.context);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
