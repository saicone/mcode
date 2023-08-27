package com.saicone.mcode.module.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ACommand {

    private final CommandKey key;
    private CommandSyntax syntax;

    private List<Object> subCommands;

    public ACommand(@NotNull CommandKey key) {
        this.key = key;
    }

    @NotNull
    public CommandKey getKey() {
        return key;
    }

    @NotNull
    public String getName() {
        return key.getName();
    }

    @NotNull
    public Set<String> getAliases() {
        return key.getAliases();
    }

    @Nullable
    public CommandSyntax getSyntax() {
        return syntax;
    }

    @Nullable
    public List<Object> getSubCommands() {
        return subCommands;
    }

    public void setSyntax(@Nullable CommandSyntax syntax) {
        this.syntax = syntax;
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
