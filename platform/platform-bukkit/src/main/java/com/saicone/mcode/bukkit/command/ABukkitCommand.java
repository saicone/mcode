package com.saicone.mcode.bukkit.command;

import com.saicone.mcode.module.command.ACommand;
import com.saicone.mcode.module.command.CommandCentral;
import com.saicone.mcode.module.command.CommandResult;
import com.saicone.mcode.module.command.InputContext;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ABukkitCommand extends Command {

    private final ACommand key;

    private Consumer<CommandSender> permissionBound;

    private CommandCentral<CommandSender> central;

    public ABukkitCommand(@NotNull ACommand key) {
        super(key.getKey().getName());
        this.key = key;
    }

    @NotNull
    public ACommand getKey() {
        return key;
    }

    @Nullable
    public Consumer<CommandSender> getPermissionBound() {
        return permissionBound;
    }

    @NotNull
    public CommandCentral<CommandSender> getCentral() {
        if (central == null) {
            throw new IllegalStateException("The command '" + getName() + "' is not registered on CommandCentral");
        }
        return central;
    }

    @NotNull
    public Function<InputContext<CommandSender>, CommandResult> getEval() {
        return context -> {
            if (getPermission() == null || context.getSender().hasPermission(getPermission())) {
                return CommandResult.DONE;
            } else {
                if (getPermissionBound() != null) {
                    getPermissionBound().accept(context.getSender());
                }
                return CommandResult.FAIL_EVAL;
            }
        };
    }

    public void setPermissionBound(@Nullable Consumer<CommandSender> permissionBound) {
        this.permissionBound = permissionBound;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        getCentral().execute(key, new InputContext<>(sender, null, String.join(" ", args)));
        return true;
    }

    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args, @Nullable Location location) throws IllegalArgumentException {
        return super.tabComplete(sender, alias, args, location);
    }

    @NotNull
    @Override
    public String getName() {
        return key.getKey().getName();
    }

    @NotNull
    @Override
    public List<String> getAliases() {
        return new ArrayList<>(key.getKey().getAliases());
    }

    @NotNull
    @Override
    public String getDescription() {
        return key.getDescription() == null ? "" : key.getDescription();
    }

    @Override
    public boolean setName(@NotNull String name) {
        if (!isRegistered()) {
            key.getKey().setLeft(name);
            return true;
        }
        return false;
    }

    @NotNull
    @Override
    public Command setAliases(@NotNull List<String> aliases) {
        key.getKey().alias(aliases.toArray(new String[0]));
        return this;
    }

    @NotNull
    @Override
    public Command setDescription(@NotNull String description) {
        key.setDescription(description);
        return this;
    }

    @Override
    public boolean testPermission(@NotNull CommandSender target) {
        if (permissionBound != null) {
            if (testPermissionSilent(target)) {
                return true;
            }

            permissionBound.accept(target);

            return false;
        }
        return super.testPermission(target);
    }

    @Override
    public boolean testPermissionSilent(@NotNull CommandSender target) {
        return super.testPermissionSilent(target);
    }

    @Override
    public boolean register(@NotNull CommandMap commandMap) {
        return register(BukkitCommand.central(), commandMap);
    }

    public boolean register(@NotNull CommandCentral<CommandSender> central, @NotNull CommandMap commandMap) {
        this.central = central;
        return super.register(commandMap);
    }
    @Override
    public boolean unregister(@NotNull CommandMap commandMap) {
        this.central = null;
        return unregister(commandMap);
    }

    @Override
    public boolean isRegistered() {
        return central != null && super.isRegistered();
    }
}