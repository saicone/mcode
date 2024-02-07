package com.saicone.mcode.bukkit.lang;

import com.saicone.mcode.module.lang.display.TextDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class BukkitTextEvent extends TextDisplay.Event {

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
