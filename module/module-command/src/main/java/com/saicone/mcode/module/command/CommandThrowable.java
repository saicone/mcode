package com.saicone.mcode.module.command;

import com.saicone.mcode.util.MStrings;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface CommandThrowable<SenderT> {

    void sendMessage(@NotNull SenderT sender, @NotNull String message);

    default void sendPermissionMessage(@NotNull SenderT sender, @NotNull String permission) {
        sendMessage(sender, MStrings.color("&cYou do not have permission &6" + permission + " &cto perform this command.Please contact the server administrators if you believe that this is a mistake."));
    }

    default void sendDelayMessage(@NotNull SenderT sender, float seconds) {
        sendMessage(sender, MStrings.color("&cYou should wait &6" + + seconds + " &cseconds before execute this command again."));
    }

    default void sendUsage(@NotNull SenderT sender, @NotNull String syntax, @NotNull String input, @NotNull String... args) {

    }

    default void sendTitleUsage(@NotNull SenderT sender) {
        // empty default method
    }

    default void sendMainUsage(@NotNull SenderT sender) {
        // empty default method
    }

    default void sendSubUsage(@NotNull SenderT sender) {
        // empty default method
    }
}
