package com.saicone.mcode.velocity.command;

import com.mojang.brigadier.tree.CommandNode;
import com.saicone.mcode.module.command.ACommand;
import com.saicone.mcode.module.command.CommandCentral;
import com.saicone.mcode.module.command.CommandKey;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class AVelocityCommand extends ACommand implements CommandMeta {

    private final List<CommandNode<CommandSource>> hints = new ArrayList<>();
    private Object plugin;
    private String permission;
    private Consumer<CommandSource> permissionBound;

    private CommandCentral<CommandSource> central;

    public AVelocityCommand(@NotNull CommandKey key) {
        super(key);
    }

    @Override
    public @NotNull List<CommandNode<CommandSource>> getHints() {
        return hints;
    }

    @Override
    public @Nullable Object getPlugin() {
        return plugin;
    }

    @Nullable
    public String getPermission() {
        return permission;
    }

    @Nullable
    public Consumer<CommandSource> getPermissionBound() {
        return permissionBound;
    }

    @NotNull
    public CommandCentral<CommandSource> getCentral() {
        if (central == null) {
            throw new IllegalStateException("The command '" + getKey().getName() + "' is not registered on CommandCentral");
        }
        return central;
    }

    @Override
    public @NotNull Set<String> getAliases() {
        final Set<String> set = new HashSet<>();
        set.add(getKey().getName());
        set.addAll(getKey().getAliases());
        return set;
    }

    public boolean isRegistered() {
        return central != null;
    }

    public void addHint(@NotNull CommandNode<CommandSource> node) {
        this.hints.add(node);
    }

    public void setPlugin(@Nullable Object plugin) {
        this.plugin = plugin;
    }

    public void setPermission(@Nullable String permission) {
        this.permission = permission;
    }

    public void setPermissionBound(@Nullable Consumer<CommandSource> permissionBound) {
        this.permissionBound = permissionBound;
    }

    public boolean testPermission(@NotNull CommandSource source) {
        if (testPermissionSilent(source)) {
            return true;
        }
        if (permissionBound != null) {
            permissionBound.accept(source);
        }
        return false;
    }

    public boolean testPermissionSilent(@NotNull CommandSource source) {
        return getPermission() == null || getPermission().isBlank() || source.hasPermission(getPermission());
    }

    public void register(@NotNull CommandCentral<CommandSource> central) {
        this.central = central;
    }
}
