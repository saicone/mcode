package com.saicone.mcode.module.lang.display;

import com.saicone.mcode.module.lang.LangLoader;
import com.saicone.mcode.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void sendTo(@NotNull LangLoader<SenderT, ? extends SenderT> loader, @NotNull SenderT type, @Nullable Object... args) {
    }

    public void sendTo(@NotNull LangLoader<SenderT, ? extends SenderT> loader, @NotNull SenderT agent, @NotNull SenderT type, @Nullable Object... args) {
        sendTo(loader, type, args);
    }

    public void sendToAll(@NotNull LangLoader<SenderT, ? extends SenderT> loader, @Nullable Object... args) {
    }

    public void sendToAll(@NotNull LangLoader<SenderT, ? extends SenderT> loader, @NotNull SenderT agent, @Nullable Object... args) {
        sendToAll(loader, args);
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
    protected String args(@NotNull String s, @Nullable Object... args) {
        return Strings.replaceArgs(s, args);
    }
}
