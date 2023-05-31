package com.saicone.mcode.module.lang.display;

import com.saicone.mcode.Platform;
import com.saicone.mcode.platform.Text;
import com.saicone.mcode.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;

public abstract class Display<SenderT> {

    private static final Map<Class<?>, List<Field>> INSTANCE_FIELDS = new HashMap<>();
    private static final Display<?> EMPTY = new Display<>() { };

    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> Display<T> of() {
        return (Display<T>) EMPTY;
    }

    @NotNull
    public static <T> Display<T> of(@NotNull List<Display<T>> list) {
        return new DisplayList<>(list);
    }

    public void sendTo(@NotNull SenderT type, @Nullable Object... args) {
        sendTo(type, s -> Text.of(s).args(args).parse(type).toString());
    }

    public void sendTo(@NotNull SenderT agent, @NotNull SenderT type, @Nullable Object... args) {
        sendTo(type, s -> Text.of(s).args(args).parseAgent(type, agent).toString());
    }

    public void sendTo(@NotNull SenderT type, @NotNull Function<String, String> parser) {
    }

    public void sendToAll(@Nullable Object... args) {
        sendToAll(s -> args(s, args));
    }

    public void sendToAll(@NotNull SenderT agent, @Nullable Object... args) {
        sendToAll(s -> Text.of(s).args(args).parseAgent(agent).toString());
    }

    public void sendToAll(@NotNull Function<String, String> parser) {
    }

    @NotNull
    public String getText() {
        return "";
    }

    @NotNull
    public String getText(@NotNull String type) {
        for (Field field : getInstanceFields(getClass())) {
            if (field.getName().equalsIgnoreCase(type)) {
                try {
                    return String.valueOf(field.get(this));
                } catch (IllegalAccessException e) {
                    return "";
                }
            }
        }
        return "";
    }

    private static List<Field> getInstanceFields(@NotNull Object obj) {
        final Class<?> objClass = obj.getClass();
        if (!INSTANCE_FIELDS.containsKey(objClass)) {
            final List<Field> list = new ArrayList<>();
            for (Class<?> clazz = objClass; clazz != Object.class; clazz = clazz.getSuperclass()) {
                for (Field field : clazz.getDeclaredFields()) {
                    if (!Modifier.isStatic(clazz.getModifiers()) && field.canAccess(obj)) {
                        list.add(field);
                    }
                }
            }
            INSTANCE_FIELDS.put(objClass, list);
        }
        return INSTANCE_FIELDS.get(objClass);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    protected Collection<SenderT> players() {
        try {
            return (Collection<SenderT>) Platform.getInstance().getOnlinePlayers();
        } catch (ClassCastException e) {
            return List.of();
        }
    }

    @NotNull
    protected String args(@NotNull String s, @Nullable Object... args) {
        return Strings.replaceArgs(s, args);
    }
}
