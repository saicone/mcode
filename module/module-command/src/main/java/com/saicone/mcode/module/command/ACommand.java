package com.saicone.mcode.module.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ACommand {

    private final CommandKey key;
    private CommandSyntax syntax;
    private String description;

    private List<Object> subCommands;

    public ACommand(@NotNull CommandKey key) {
        this.key = key;
    }

    public boolean isMain() {
        return key.getParent() == null;
    }

    @NotNull
    public CommandKey getKey() {
        return key;
    }

    @Nullable
    public CommandSyntax getSyntax() {
        return syntax;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    @Nullable
    public List<Object> getSubCommands() {
        return subCommands;
    }

    public void setSyntax(@Nullable CommandSyntax syntax) {
        this.syntax = syntax;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    public void setSubCommands(@Nullable List<Object> subCommands) {
        this.subCommands = subCommands;
    }

    public void addSubCommand(@NotNull Object... subCommands) {
        if (subCommands.length < 1) {
            return;
        }
        if (this.subCommands == null) {
            this.subCommands = new ArrayList<>();
        }
        this.subCommands.addAll(Arrays.asList(subCommands));
    }

    @NotNull
    public <T extends ACommand> T wrap(T command) {
        command.setDescription(getDescription());
        command.setSubCommands(getSubCommands());
        command.setSyntax(getSyntax());
        return command;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (getClass() != o.getClass()) {
            if (o instanceof String) {
                return key.matches((String) o);
            }
            return false;
        }

        ACommand aCommand = (ACommand) o;

        return key.equals(aCommand.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
