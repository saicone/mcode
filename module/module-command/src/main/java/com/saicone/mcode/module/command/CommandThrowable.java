package com.saicone.mcode.module.command;

import com.saicone.mcode.util.MStrings;
import com.saicone.mcode.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;

@FunctionalInterface
public interface CommandThrowable<SenderT> {

    void sendMessage(@NotNull SenderT sender, @NotNull String message);

    default void sendColoredMessage(@NotNull SenderT sender, @NotNull String message) {
        sendMessage(sender, MStrings.color(message));
    }

    default void sendPermissionMessage(@NotNull SenderT sender, @NotNull String permission) {
        sendColoredMessage(sender, "&cYou do not have permission &6" + permission + " &cto perform this command. Please contact the server administrators if you believe that this is a mistake.");
    }

    default void sendDelayMessage(@NotNull SenderT sender, float seconds) {
        sendColoredMessage(sender, "&cYou should wait &6" + + seconds + " &cseconds before execute this command again.");
    }

    default void sendKey(@NotNull SenderT sender, @NotNull String key, @Nullable Object... args) {
        // empty default method
    }

    default void sendUsage(@NotNull SenderT sender, @NotNull InputContext<SenderT> context) {
        final CommandNode<SenderT> node = context.getCommand();
        final StringJoiner usage = new StringJoiner(" ");
        usage.add(context.getFullInput());
        final String out;
        final String in;
        if (node.hasSubCommands()) {
            out = "";
            in = "";
        } else {
            out = "&8";
            in = "&7";
        }
        if (context.getSize() < context.getCommand().getSize()) {
            final var arguments = context.getCommand().getArguments();
            for (int i = context.getSize(); i < arguments.size(); i++) {
                final var argument = arguments.get(i);
                if (argument.isRequired(sender)) {
                    usage.add(out + '<' + in + argument.getName() + out + '>');
                } else {
                    usage.add(out + '[' + in + argument.getName() + out + ']');
                }
            }
        }

        if (node.hasSubCommands()) {
            sendColoredMessage(sender, "&e" + Strings.capitalize(context.getCommand().getName()) + " commands: &7(/" + usage + " <command>...)");
            for (CommandNode<SenderT> sub : node.getSubCommands()) {
                sendSubUsage(sender, sub);
            }
        } else {
            sendColoredMessage(sender, "&eYou should use: &c/" + usage);
        }
    }

    default void sendSubUsage(@NotNull SenderT sender, @NotNull CommandNode<SenderT> node) {
        final String description = node.getDescription(sender);
        sendColoredMessage(sender, "&6> &c" + node.getName() + (description.isBlank() ? "" : "&6 - &7" + description));
    }
}
