package com.saicone.mcode.bukkit.lang;

import com.saicone.mcode.module.lang.display.TextDisplay;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.UUID;

public class BukkitTextEvent extends TextDisplay.Event {

    private static final MethodHandle asNMSCopy;
    private static final MethodHandle getTag;

    static {
        final String craftBukkit = Bukkit.getServer().getClass().getPackage().getName() + ".";
        final String minecraftServer;
        final boolean universal;
        final String version;
        if (craftBukkit.startsWith("org.bukkit.craftbukkit.v1_") && Integer.parseInt((version = craftBukkit.split("\\.")[3]).split("_")[1]) <= 16) {
            minecraftServer = "net.minecraft.server." + version + ".";
            universal = false;
        } else {
            minecraftServer = "net.minecraft.";
            universal = true;
        }

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle method$asNMSCopy = null;
        MethodHandle method$getTag = null;
        try {
            method$asNMSCopy = lookup.unreflect(Class.forName(craftBukkit + "inventory.CraftItemStack").getDeclaredMethod("asNMSCopy", ItemStack.class));
            Class<?> compoundClass;
            try {
                compoundClass = Class.forName(minecraftServer + (universal ? "nbt." : "") + "NBTTagCompound");
            } catch (ClassNotFoundException e) {
                // Mojang mapped
                compoundClass = Class.forName(minecraftServer + "nbt.CompoundTag");
            }
            for (Field field : Class.forName(minecraftServer + (universal ? "world.item." : "") + "ItemStack").getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) && compoundClass.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    method$getTag = lookup.unreflectGetter(field);
                    break;
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        asNMSCopy = method$asNMSCopy;
        getTag = method$getTag;
    }

    private transient String itemTag;

    public BukkitTextEvent(TextDisplay.@NotNull Action action, @NotNull Object value) {
        super(action, value);
    }

    @Override
    public @NotNull String getItemId() {
        if (getValue() instanceof ItemStack) {
            return "minecraft:" + ((ItemStack) getValue()).getType().name().toLowerCase();
        }
        return super.getItemId();
    }

    @Override
    public int getItemCount() {
        if (getValue() instanceof ItemStack) {
            return ((ItemStack) getValue()).getAmount();
        }
        return super.getItemCount();
    }

    @Override
    public @Nullable String getItemTag() {
        if (getValue() instanceof ItemStack) {
            if (itemTag == null) {
                try {
                    final Object tag = getTag.invoke(asNMSCopy.invoke(getValue()));
                    itemTag = tag == null ? "" : tag.toString();
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            }
            return itemTag.isBlank() ? null : itemTag;
        }
        return super.getItemTag();
    }

    @Override
    public @Nullable String getEntityName() {
        if (getValue() instanceof Entity) {
            return ((Entity) getValue()).getName();
        }
        return super.getEntityName();
    }

    @Override
    public @NotNull String getEntityType() {
        if (getValue() instanceof Entity) {
            return "minecraft:" + ((Entity) getValue()).getType().getName();
        }
        return super.getEntityType();
    }

    @Override
    public @NotNull UUID getEntityUniqueId() {
        if (getValue() instanceof Entity) {
            return ((Entity) getValue()).getUniqueId();
        }
        return super.getEntityUniqueId();
    }
}
