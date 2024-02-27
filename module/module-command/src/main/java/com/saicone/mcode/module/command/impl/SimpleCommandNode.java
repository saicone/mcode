package com.saicone.mcode.module.command.impl;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class SimpleCommandNode<SenderT> extends AbstractCommandNode<SenderT> {

    private final String name;
    private Set<String> aliases = Set.of();
    private String description = "";

    public SimpleCommandNode(@NotNull String name) {
        this.name = name;
    }

    public void setAliases(@NotNull String... aliases) {
        setAliases(Set.of(aliases));
    }

    public void setAliases(@NotNull Set<String> aliases) {
        this.aliases = aliases;
    }

    public void setDescription(@NotNull String description) {
        this.description = description;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull Set<String> getNodeAliases() {
        return aliases;
    }

    @Override
    public @NotNull String getDescription() {
        return description;
    }
}
