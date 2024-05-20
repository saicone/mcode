package com.saicone.mcode.module.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class NodeArgument<SenderT> extends Argument<SenderT, CommandNode<SenderT>, NodeArgument<SenderT>> {

    private final List<CommandNode<SenderT>> nodes = new ArrayList<>();

    public NodeArgument() {
        suggests(CommandSuggestion.of(nodes, CommandNode::getName));
    }

    @NotNull
    public List<CommandNode<SenderT>> getNodes() {
        return nodes;
    }

    @Override
    protected @NotNull NodeArgument<SenderT> argument() {
        return this;
    }

    @Override
    public @Nullable CommandNode<SenderT> parse(@NotNull String s) {
        for (CommandNode<SenderT> node : getNodes()) {
            if (node.matches(s)) {
                return node;
            }
        }
        return null;
    }
}
