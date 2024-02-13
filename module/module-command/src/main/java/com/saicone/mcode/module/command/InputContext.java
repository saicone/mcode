package com.saicone.mcode.module.command;

import com.saicone.mcode.util.Dual;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InputContext<T> {

    private final T user;
    private final T agent;
    private final List<String> inputs;

    private String[] path;
    // String
    // Dual<String, String>
    private List<Object> arguments;
    private int position = 0;
    private CommandResult result = CommandResult.DONE;

    public InputContext(@NotNull T user, @Nullable T agent, @NotNull String input) {
        this.user = user;
        this.agent = agent;
        this.inputs = new ArrayList<>();
        this.inputs.add(input);
    }

    @NotNull
    public T getUser() {
        return user;
    }

    @NotNull
    public T getPlayer() {
        return user;
    }

    @NotNull
    public T getSender() {
        return user;
    }

    @NotNull
    public T getSource() {
        return user;
    }

    @NotNull
    public T getAgent() {
        return agent == null ? user : agent;
    }

    @NotNull
    public List<Object> getArguments() {
        return arguments;
    }

    @NotNull
    public String getInput() {
        return inputs.get(position);
    }

    @NotNull
    public List<String> getInputs() {
        return inputs;
    }

    public int getPosition() {
        return position;
    }

    @NotNull
    public CommandResult getResult() {
        return result;
    }

    public void setArguments(@NotNull List<Object> arguments) {
        this.arguments = arguments;
    }

    public void setResult(@NotNull CommandResult result) {
        this.result = result;
    }

    public void addInput(@NotNull String input) {
        this.inputs.add(input);
        this.position++;
    }

    public void removeInput() {
        this.inputs.remove(this.inputs.size() - 1);
        this.position--;
    }

    public void removeLast() {
        if (this.arguments != null) {
            this.arguments.clear();
            this.arguments = null;
        }
        removeInput();
    }

    public boolean isEval() {
        return this.arguments == null;
    }

    public boolean has(int index) {
        return index < arguments.size();
    }

    public boolean has(@NotNull String name) {
        for (final Object arg : arguments) {
            if (arg instanceof Dual && name.equalsIgnoreCase((String) ((Dual<?, ?>) arg).getLeft())) {
                return true;
            }
        }
        return false;
    }

    public boolean notHas(int index) {
        return !has(index);
    }

    public boolean notHas(@NotNull String name) {
        return !has(name);
    }

    public int size() {
        return arguments.size();
    }

    public void clear() {
        inputs.clear();
        arguments.clear();
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public <A> A arg(int index) throws IllegalArgumentException {
        Object arg = arguments != null ? arguments.get(index) : null;
        if (arg instanceof Dual) {
            arg = ((Dual<?, ?>) arg).getRight();
        }
        Objects.requireNonNull(arg, "The argument at index " + index + " is null");
        try {
            return (A) arg;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("The argument at index " + index + " is not mapped into required type", e);
        }
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public <A> A arg(String name) throws IllegalArgumentException {
        if (arguments != null) {
            for (Object arg : arguments) {
                if (arg instanceof Dual && name.equalsIgnoreCase((String) ((Dual<?, ?>) arg).getLeft())) {
                    arg = ((Dual<?, ?>) arg).getRight();
                    Objects.requireNonNull(arg, "The argument with name '" + name + "' is null");
                    try {
                        return (A) arg;
                    } catch (ClassCastException e) {
                        throw new IllegalArgumentException("The argument '" + name + "' is not mapped into required type", e);
                    }
                }
            }
        }
        throw new IllegalArgumentException("The argument with name '" + name + "' doesn't exist");
    }

    @NotNull
    public String[] textArgs() {
        return arguments == null ? new String[0] : arguments.stream().map(String::valueOf).toArray(String[]::new);
    }

    @NotNull
    public Object[] allArgs() {
        if (arguments == null || arguments.isEmpty()) {
            return new Object[] { path[path.length - 1] };
        }
        final Object[] args = new Object[arguments.size() + 1];
        args[0] = path[path.length - 1];
        for (int i = 0; i < arguments.size(); i++) {
            args[i + 1] = arguments.get(i);
        }
        return args;
    }
}
