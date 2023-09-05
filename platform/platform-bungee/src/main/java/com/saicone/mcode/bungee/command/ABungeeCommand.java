package com.saicone.mcode.bungee.command;

import com.saicone.mcode.module.command.ACommand;
import com.saicone.mcode.module.command.CommandCentral;
import com.saicone.mcode.module.command.InputContext;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.function.Consumer;

public class ABungeeCommand extends Command {

    private static final Field PERMISSION_FIELD;

    static {
        Field field = null;
        try {
            field = Command.class.getDeclaredField("permission");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        PERMISSION_FIELD = field;
    }

    private final ACommand key;

    private String nameKey;
    private Consumer<CommandSender> permissionBound;

    private CommandCentral<CommandSender> central;

    public ABungeeCommand(@NotNull ACommand key) {
        this(key, null);
    }

    public ABungeeCommand(@NotNull ACommand key, @Nullable String permission) {
        super(key.getKey().getName(), permission);
        this.key = key;
    }

    @NotNull
    public ACommand getKey() {
        return key;
    }

    @NotNull
    public String getNameKey() {
        return nameKey == null ? key.getKey().getName() : nameKey;
    }

    public void setNameKey(@Nullable String nameKey) {
        this.nameKey = nameKey;
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

    public void setPermissionBound(@Nullable Consumer<CommandSender> permissionBound) {
        this.permissionBound = permissionBound;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        getCentral().execute(key, new InputContext<>(sender, null, String.join(" ", args)));
    }

    @Override
    public String getName() {
        return key.getKey().getName();
    }

    @Override
    public String[] getAliases() {
        return key.getKey().getAliases().toArray(new String[0]);
    }

    public void setPermission(@Nullable String permission) {
        try {
            PERMISSION_FIELD.set(this, permission);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setPermissionMessage(@Nullable String permissionMessage) {
        super.setPermissionMessage(permissionMessage);
    }

    public boolean testPermission(@NotNull CommandSender sender) {
        if (testPermissionSilent(sender)) {
            return true;
        }
        if (permissionBound != null) {
            permissionBound.accept(sender);
        } else if (getPermissionMessage() != null) {
            sender.sendMessage(TextComponent.fromLegacyText(getPermissionMessage()));
        } else {
            sender.sendMessage(TextComponent.fromLegacyText(ProxyServer.getInstance().getTranslation("no_permission")));
        }
        return false;
    }

    public boolean testPermissionSilent(@NotNull CommandSender sender) {
        return getPermission() == null || getPermission().isBlank() || sender.hasPermission(getPermission());
    }

    public void register(@NotNull CommandCentral<CommandSender> central) {
        this.central = central;
    }

    public boolean isRegistered() {
        return central != null;
    }

    @NotNull
    public ABungeeCommand clone(@NotNull String alias) {
        final ABungeeCommand command = new ABungeeCommand(key, getPermission());
        command.setPermissionMessage(getPermissionMessage());
        command.setNameKey(alias);
        return command;
    }
}
